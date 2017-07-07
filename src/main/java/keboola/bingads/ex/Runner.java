/*
 */
package keboola.bingads.ex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.microsoft.bingads.customermanagement.AccountInfo;

import esnerda.keboola.components.result.ResultFileMetadata;
import esnerda.keboola.components.result.impl.DefaultBeanResultWriter;
import keboola.bingads.ex.client.ApiDownloadResult;
import keboola.bingads.ex.client.BulkResult;
import keboola.bingads.ex.client.Client;
import keboola.bingads.ex.client.ClientException;
import keboola.bingads.ex.client.ReportResult;
import keboola.bingads.ex.client.ResultException;
import keboola.bingads.ex.config.JsonConfigParser;
import keboola.bingads.ex.config.KBCConfig;
import keboola.bingads.ex.config.KBCParameters;
import keboola.bingads.ex.config.pojos.BReportRequest;
import keboola.bingads.ex.config.tableconfig.ManifestBuilder;
import keboola.bingads.ex.config.tableconfig.ManifestFile;
import keboola.bingads.ex.state.JsonlStateWriter;
import keboola.bingads.ex.state.LastState;
import keboola.bingads.ex.utils.CsvUtils;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Runner {

		private  Client cl;
	private  KBCConfig config;
	private  KBCParameters params;
	private  String outTablesPath;
	private  String dataPath;

    public  void run(String dataPath) throws Exception{
        LogManager.getLogManager().reset();        
        this.dataPath = dataPath;
        
        initEnvironmentVariables(dataPath);
              
        //retrieve stateFile
        LastState lastState = retrieveStateFile(dataPath);

        setUpClient();        
        
        List<Long> accountIds = loadAccountIds();
        try {
			getAndStoreAccounts();
		} catch (Exception e) {
			System.err.print("Failed to download accounts table!" + e.getMessage());
		}
        

        Map<String, Boolean> bulkReqestsToDownload = config.getParams().getBulkRequests().getBulkFiles();
        Map<String, Date> lastBulkRequests = null;
        Map<String, Date> lastReportRequests = null;
        Calendar lastSync = null;

        //set download parameters according to previous runs
        if (lastState != null) {
            try {
                lastBulkRequests = lastState.getBulkRequests();
                lastReportRequests = lastState.getReportRequests();

            } catch (NullPointerException ex) {
                System.out.println("No matching state.");
            }
        }

        LastState newState = new LastState(new HashMap(), new HashMap());
        
        /* iterate through all accounts*/
        
        /*Download bulk data*/	
        System.out.println("Downloading bulk data...");
        downloadBulkData(lastBulkRequests, lastState, newState, accountIds);
        
      
        System.out.println("Downloading report queries...");
        /*Download reports */
       downloadReports(lastReportRequests, lastState, newState, accountIds);
        
       
       

        /*Write state file*/
        try {
            JsonlStateWriter.writeStateFile(dataPath + File.separator + "out" + File.separator + "state.json", newState);
        } catch (IOException ex) {
            System.err.println("Error building state file " + ex.getMessage());
            System.exit(1);
        }

    }

	private  void getAndStoreAccounts() throws Exception {
		DefaultBeanResultWriter<AccountInfo> accInfoWriter = new DefaultBeanResultWriter<>("accounts.csv", new String[]{"id"});
		accInfoWriter.initWriter(outTablesPath, AccountInfo.class);
		List<ResultFileMetadata> res = accInfoWriter.writeAndRetrieveResuts(cl.getAllAccountsInfo());
		
		ManifestFile manFile = new ManifestFile.Builder("accounts.csv", config.getParams().getBucket() + "." + "accounts")
				.setIncrementalLoad(true).setPrimaryKey(new String[]{"id"}).setDelimiter(",").setEnclosure("\"")
				.build();

		try {
			ManifestBuilder.buildManifestFile(manFile, outTablesPath, "accounts.csv");
		} catch (IOException ex) {
			System.err.println("Error building manifest file " + ex.getMessage());
			System.exit(2);
		}
		
	}

	private  void downloadReports(Map<String, Date> lastReportRequests, LastState lastState, LastState newState, List<Long> accIds) {
		List<BReportRequest> repRequests = config.getParams().getReportRequests();
		Calendar lastSync = null;
		 Date lastRun = Calendar.getInstance().getTime();
		for (BReportRequest repReq : repRequests) {
			String resfolder = outTablesPath + File.separator + repReq.getType().name() + ".csv";
			if (lastReportRequests != null && config.getParams().getSinceLast()) {
				lastSync = Calendar.getInstance();
				Date dt = lastReportRequests.get(repReq.getType().name());
				if (dt != null) {
					lastSync.setTime(dt);
				}
			}

			String[] resultHeader = downloadAndStoreReportsForAccounts(accIds, repReq, lastSync, resfolder);			

			// set new state
			System.out.println("Building manifest files..");
			newState.getReportRequests().put(repReq.getType().name(), lastRun);

			// bulid manifest file
			ManifestFile manFile = new ManifestFile.Builder(new File(resfolder).getName(), config.getParams().getBucket() + "." + repReq.getType().name().toLowerCase())
					.setIncrementalLoad(true).setPrimaryKey(repReq.getPkey()).setDelimiter(",").setEnclosure("\"")
					.setColumns(resultHeader).build();

			try {
				ManifestBuilder.buildManifestFile(manFile, outTablesPath, new File(resfolder).getName());
			} catch (IOException ex) {
				System.err.println("Error building manifest file " + ex.getMessage());
				System.exit(2);
			}
		}

	}

	private  String[] downloadAndStoreReportsForAccounts(List<Long> accIds, BReportRequest repReq,
			Calendar lastSync, String resfolder) {
		ReportResult rResult = null;
		List<ApiDownloadResult> results = new ArrayList<>();
		for (Long accId : accIds) {
			System.out.println("For Account ID" + accId);
			try {
				rResult = cl.downloadReport(repReq, resfolder, lastSync, accId);
				results.add(rResult);
			} catch (ClientException | ResultException ex) {
				System.err.println(ex.getMessage());
				System.exit(ex.getSeverity());
			}
		}
		System.out.println("Preparing sliced tables..");
		try {
			return prepareSlicedTables(results);
		} catch (Exception e) {
			System.err.println("Error reading report headedr " + e.getMessage());
			System.exit(2);
		}
		return null;
	}

	private  List<Long> loadAccountIds() {
 		List<Long> accountIds = new ArrayList<>();
        try {
			accountIds = retrieveAccountIds(cl, params);
		} catch (Exception e) {
             System.err.println("Failed to retrieve accounts!" + e.getMessage());
             System.exit(1);
		}
        return accountIds;
	}

	private  void setUpClient() {
		cl = new Client(config.getOAuthCredentials().getAppKey(), params.getDevKey(),
                config.getOAuthCredentials().getRefreshToken(), config.getOAuthCredentials().getAppSecret(), "", new Long(1), params.getCustomerId(), params.getAccountId());
		
	}

	private  void initEnvironmentVariables(String dataPath) {
		outTablesPath = dataPath + File.separator + "out" + File.separator + "tables"; //parse config
        
	       //Parse config file
	        config = parseConfigFile(dataPath);
	       
	        //retrieve stateFile
	        LastState lastState = retrieveStateFile(dataPath);

	        params = config.getParams();		
	}

	private  void downloadBulkData(Map<String, Date> lastBulkRequests, LastState lastState, LastState newState, List<Long> accIds) {
        Map<String, Boolean> bulkReqestsToDownload = config.getParams().getBulkRequests().getBulkFiles();
        Date lastRun = Calendar.getInstance().getTime();


	        for (Entry<String, Boolean> br : bulkReqestsToDownload.entrySet()) {
	        	String resfolder = outTablesPath + File.separator + br.getKey() + ".csv";
	    		Calendar lastSync = null;
	   		 //last run
	          if (lastBulkRequests != null && config.getParams().getSinceLast()) {
	              lastSync = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	              Date dt = lastBulkRequests.get(br.getKey());
	              if (dt != null) {
	                  lastSync.setTime(dt);
	              }
	          }	          
	            if (!br.getValue()) {
	            	continue;
	            }
	            String[] resultHeader = downloadAndStoreBulkForAccounts(accIds, br, lastSync, resfolder);
	           

	            //set new state
	            newState.getBulkRequests().put(br.getKey(), lastRun);

	            //bulid manifest file
	            ManifestFile manFile = new ManifestFile.Builder(new File(resfolder).getName(), config.getParams().getBucket() + "." + br.getKey().toLowerCase())
						.setIncrementalLoad(true).setPrimaryKey(new String[]{"Id"})
						.setDelimiter(",").setEnclosure("\"")
						.setColumns(resultHeader).build();

				try {
	                ManifestBuilder.buildManifestFile(manFile, outTablesPath, new File(resfolder).getName());
	            } catch (IOException ex) {
	                System.err.println("Error building manifest file " + ex.getMessage());
	                System.exit(2);
	            }
	        }	
	}

	private  String[] downloadAndStoreBulkForAccounts(List<Long> accIds, Entry<String, Boolean> br, Calendar lastSync, String resFolder) {
		boolean qScore = false;
		if (br.getKey().equals("CAMPAIGNS")) {
			qScore = config.getParams().getBulkRequests().getCampQualityScore();
			if (qScore) {
				lastSync = null;
			}
		}
		List<ApiDownloadResult> results = new ArrayList<>();
		for (Long accId : accIds) {
			System.out.println("For Account ID" + accId);
			BulkResult res = null;
			try {
				res = cl.downloadBulkData(br.getKey(), qScore, false, resFolder, lastSync, accId);
				results.add(res);
			} catch (ClientException ex) {
				System.err.println(ex.getMessage());
				System.exit(ex.getSeverity());
			}
		}
		try {
			return prepareSlicedTables(results);
		} catch (Exception e) {
			System.err.println("Error building sliced tables " + e.getMessage());
            System.exit(2);
		}
		return null;

	}

	private  String[] prepareSlicedTables(List<ApiDownloadResult> results) throws Exception {
		List<File> resultFiles = new ArrayList<>();
		List<File> files = new ArrayList<>();
		
		for (ApiDownloadResult rs : results) {
			files.add(rs.getResultFile());
		}
		// get colums
				String[] headerCols = CsvUtils.readHeader(files.get(0),
						',', '"', '\\', false, false);
				// remove headers and create results
				for (File file : files) {
					CsvUtils.removeHeaderFromCsv(file);					
				}
				//in case some files did not contain any data
				CsvUtils.deleteEmptyFiles(files);				
				return headerCols;
		
	}

	private  List<Long> retrieveAccountIds(Client cl, KBCParameters params) throws Exception {
		if(params.getAccountId() != null){
			return Collections.singletonList(params.getAccountId());
		}
		return cl.getAllAccountIds();
	}

	private  LastState retrieveStateFile(String dataPath) {
		File stateFile = new File(dataPath + File.separator + "in" + File.separator + "state.json");
        LastState lastState = null;
        if (stateFile.exists()) {
            try {
                lastState = (LastState) JsonConfigParser.parseFile(stateFile, LastState.class);
            } catch (IOException ex) {
                Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("State file does not exist. (first run?)");
        }
        return lastState;
	}

	private  KBCConfig parseConfigFile(String dataPath) {
		 KBCConfig config = null;
		 File confFile = new File(dataPath + File.separator + "config.json");
	        if (!confFile.exists()) {
	            System.err.println("config.json does not exist!");
	            System.exit(1);
	        }
	        try {
	            if (confFile.exists() && !confFile.isDirectory()) {
	                config = JsonConfigParser.parseFile(confFile);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            System.out.println("Failed to parse config file.");
	            System.err.println(ex.getMessage());
	            System.exit(1);
	        }
	        try {
	            if (!config.validate()) {
	                System.out.println(config.getValidationError());
	                System.err.println(config.getValidationError());
	                System.exit(1);
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        return config;
		
	}
}
