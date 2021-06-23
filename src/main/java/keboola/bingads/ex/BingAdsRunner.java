/*
 */
package keboola.bingads.ex;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.LogManager;

import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.v13.customermanagement.AccountInfo;

import esnerda.keboola.components.ComponentRunner;
import esnerda.keboola.components.KBCException;
import esnerda.keboola.components.configuration.handler.ConfigHandlerBuilder;
import esnerda.keboola.components.configuration.handler.KBCConfigurationEnvHandler;
import esnerda.keboola.components.configuration.tableconfig.ManifestFile;
import esnerda.keboola.components.logging.DefaultLogger;
import esnerda.keboola.components.logging.KBCLogger;
import esnerda.keboola.components.result.ResultFileMetadata;
import esnerda.keboola.components.result.impl.DefaultBeanResultWriter;
import keboola.bingads.ex.client.ApiDownloadResult;
import keboola.bingads.ex.client.BulkResult;
import keboola.bingads.ex.client.Client;
import keboola.bingads.ex.client.ClientException;
import keboola.bingads.ex.client.ReportResult;
import keboola.bingads.ex.client.ResultException;
import keboola.bingads.ex.config.BingAuthTokens;
import keboola.bingads.ex.config.BingParameters;
import keboola.bingads.ex.config.pojos.BReportRequest;
import keboola.bingads.ex.state.BingLastState;
import keboola.bingads.ex.utils.CsvUtils;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class BingAdsRunner extends ComponentRunner {

	private Client cl;
	private BingParameters config;
	private KBCLogger log;

	private DefaultBeanResultWriter<AccountInfo> accInfoWriter;

	public BingAdsRunner(String[] args) {		
		log = new DefaultLogger(BingAdsRunner.class);
		log.info("version: v1.2.2");
		log.info("Configuring environment...");
		handler = initHandler(args, log);
		config = (BingParameters) handler.getParameters();
	}

	public void run() throws Exception {
		LogManager.getLogManager().reset();
		setUpClient();
		initWriters();
		List<ResultFileMetadata> results = new ArrayList<>();
		// retrieve stateFile
		BingLastState lastState = (BingLastState) handler.getStateFile();

		List<Long> accountIds = loadAccountIds();
		try {
			results.addAll(getAndStoreAccounts());
		} catch (Exception e) {
			log.error("Failed to download accounts table!" + e.getMessage(), e);
			this.accInfoWriter.close();
			new File(handler.getOutputTablesPath() + File.separator
					+ this.accInfoWriter.getFileName()).delete();
		}

		Map<String, Boolean> bulkReqestsToDownload = config.getBulkRequests().getBulkFiles();

		BingLastState newState = new BingLastState(new Date());
		Calendar lastSync = null;
		//last run
		if(lastState != null && lastState.getLastRun() != null && config.getSinceLast()) {
			lastSync.setTime(lastState.getLastRun());
		}

		/* iterate through all accounts */

		/* Download bulk data */
		log.getLogger().info("Downloading bulk data...");
		results.addAll(downloadBulkData(lastSync, accountIds));

		log.getLogger().info("Downloading report queries...");
		/* Download reports */
		results.addAll(downloadReports(lastSync, accountIds));

		finalize(results, lastState);
		log.getLogger().info("Extraction finished successfully!");

	}

	private List<ResultFileMetadata> getAndStoreAccounts() throws Exception {
		List<ResultFileMetadata> res = accInfoWriter.writeAndRetrieveResuts(cl.getAllAccountsInfo());
		return res;

	}

	private List<ResultFileMetadata> downloadReports(Calendar lastSync, List<Long> accIds) throws Exception {
		List<ResultFileMetadata> results = new ArrayList<>();
		List<BReportRequest> repRequests = config.getReportRequests();
		
		for (BReportRequest repReq : repRequests) {
			String resfolder = handler.getOutputTablesPath() + File.separator + repReq.getType().name() + ".csv";
			if (!config.getSinceLast()) {
				lastSync = null;				
			}

			String[] resultHeader = downloadAndStoreReportsForAccounts(accIds, repReq, lastSync, resfolder);
			if (resultHeader != null) {
				log.getLogger().info("Building manifest files..");
				// bulid manifest file
				// bulid result
				ResultFileMetadata res = new ResultFileMetadata(new File(resfolder),
						config.getBucket() + "." + repReq.getType().name().toLowerCase(), repReq.getPkey(), resultHeader);
				results.add(res);
			}
		}
		return results;
	}

	private String[] downloadAndStoreReportsForAccounts(List<Long> accIds, BReportRequest repReq, Calendar lastSync,
			String resfolder) throws Exception {
		ReportResult rResult = null;
		List<ApiDownloadResult> results = new ArrayList<>();

		log.getLogger().info("For Accounts " + accIds);
		try {
			rResult = cl.downloadReport(repReq, resfolder, lastSync, accIds);
			if (rResult == null) {
				log.warning("No results for report " + repReq.getType() + " in period since: " + repReq.getStartDate(), null);
			} else {
				results.add(rResult);
			}

		} catch (ClientException | ResultException ex) {
			log.error(ex.getMessage(), ex);
			System.exit(ex.getSeverity());
		}

		log.getLogger().info("Preparing sliced tables..");
		try {
			return prepareSlicedTables(results);
		} catch (Exception e) {
			log.error("Error reading report header " + e.getMessage(), e);
			System.exit(2);
		}
		return null;
	}

	private List<Long> loadAccountIds() {
		List<Long> accountIds = new ArrayList<>();
		try {
			accountIds = retrieveAccountIds(cl);
		} catch (Exception e) {
			log.error("Failed to retrieve accounts!" + e.getMessage(), e);
			System.err.println("Failed to retrieve accounts!" + e.getMessage());
			System.exit(1);
		}
		return accountIds;
	}

	private List<Long> retrieveAccountIds(Client cl) throws Exception {
		if (config.getAccountId() != null) {
			return Collections.singletonList(config.getAccountId());
		}
		return cl.getAllAccountIds();
	}

	private List<ResultFileMetadata> downloadBulkData(Calendar lastSync, List<Long> accIds) {
		List<ResultFileMetadata> results = new ArrayList<>();
		Map<String, Boolean> bulkReqestsToDownload = config.getBulkRequests().getBulkFiles();


		for (Entry<String, Boolean> br : bulkReqestsToDownload.entrySet()) {
			String resfolder = handler.getOutputTablesPath() + File.separator + br.getKey() + ".csv";		
			if (!br.getValue()) {
				continue;
			}
			String[] resultHeader = downloadAndStoreBulkForAccounts(accIds, br, lastSync, resfolder);
			if (resultHeader != null) {

				// bulid result
				ResultFileMetadata res = new ResultFileMetadata(new File(resfolder),
						config.getBucket() + "." + br.getKey().toLowerCase(), new String[] { "Id" }, resultHeader);
				results.add(res);
			}
		}
		return results;
	}

	private String[] downloadAndStoreBulkForAccounts(List<Long> accIds, Entry<String, Boolean> br, Calendar lastSync,
			String resFolder) {

		List<ApiDownloadResult> results = new ArrayList<>();
		for (Long accId : accIds) {
			log.getLogger().info("For Account ID" + accId);
			BulkResult res = null;
			try {
				res = cl.downloadBulkData(br.getKey(), config.getBulkRequests().getCampQualityScore(), false, resFolder, lastSync, accId);
				if (res == null) {
					log.warning("No results for " + br.getKey(), null);
				} else {
					results.add(res);
				}
			} catch (ClientException ex) {
				log.error(ex.getMessage(), ex);
				System.exit(ex.getSeverity());
			}
		}
		try {
			return prepareSlicedTables(results);
		} catch (Exception e) {
			log.error("Error building sliced tables " + e.getMessage(), e);
			System.exit(2);
		}
		return null;

	}

	private String[] prepareSlicedTables(List<ApiDownloadResult> results) throws Exception {
		List<File> files = new ArrayList<>();

		for (ApiDownloadResult rs : results) {
			files.add(rs.getResultFile());
		}
		String[] headerCols = null;
		if (!files.isEmpty()) {
		
			// get colums
			headerCols = CsvUtils.readHeader(files.get(0), ',', '"', '\\', false, false);
			// remove headers and create results
			for (File file : files) {
				CsvUtils.removeHeaderFromCsv(file);
			}
		}
		// in case some files did not contain any data
		System.out.println("cleaning");
		CsvUtils.deleteEmptyFiles(files);
		CsvUtils.deleteEmptyDirectories(new File(handler.getOutputTablesPath()));

		return headerCols;

	}

	private void setUpClient() throws Exception {
		BingAuthTokens bTokens = handler.getConfig().getAuthParams(BingAuthTokens.class);
		OAuthTokens tokens = new OAuthTokens(bTokens.getAccess_token(), 1L, bTokens.getRefresh_token());
		cl = new Client(handler.getOAuthCredentials().getAppKey(), config.getDevKey(),
				handler.getOAuthCredentials().getAppSecret(), tokens, config.getCustomerId(), config.isDebug());
	}

	@Override
	protected KBCConfigurationEnvHandler initHandler(String[] args, KBCLogger log) {
		KBCConfigurationEnvHandler handler = null;
		try {
			handler = ConfigHandlerBuilder.create(BingParameters.class).setStateFileType(BingLastState.class).build();
			// process the configuration
			handler.processConfigFile(args);
		} catch (KBCException ex) {
			log.error(ex.getMessage(), ex);
			System.exit(1);
		}
		setHandler(handler);
		return handler;
	}

	@Override
	protected void initWriters() throws Exception {
		this.accInfoWriter = new DefaultBeanResultWriter<>("accounts.csv", new String[] { "id" }, config.getBucket() + "." + "accounts");
		accInfoWriter.initWriter(handler.getOutputTablesPath(), AccountInfo.class);
	}

	@Override
	public KBCLogger getLogger() {
		return log;
	}

	@Override
	protected long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected ManifestFile generateManifestFile(ResultFileMetadata result) throws KBCException {
		return ManifestFile.Builder.buildDefaultFromResult(result).setColumns(result.getColumns()).build();
	}
}
