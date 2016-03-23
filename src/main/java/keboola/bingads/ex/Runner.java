/*
 */
package keboola.bingads.ex;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import keboola.bingads.ex.client.BulkResult;
import keboola.bingads.ex.client.Client;
import keboola.bingads.ex.client.ClientException;
import keboola.bingads.ex.client.ReportResult;
import keboola.bingads.ex.config.KBCConfig;
import keboola.bingads.ex.config.YamlConfigParser;
import keboola.bingads.ex.config.pojos.BReportRequest;
import keboola.bingads.ex.config.pojos.BulkRequests;
import keboola.bingads.ex.config.tableconfig.ManifestBuilder;
import keboola.bingads.ex.config.tableconfig.ManifestFile;
import keboola.bingads.ex.state.LastState;
import keboola.bingads.ex.state.YamlStateWriter;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Runner {

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        if (args.length == 0) {
            System.out.print("No parameters provided.");
            System.exit(1);
        }
        String dataPath = args[0];
        String outTablesPath = dataPath + File.separator + "out" + File.separator + "tables"; //parse config
        KBCConfig config = null;
        File confFile = new File(args[0] + File.separator + "config.yml");
        if (!confFile.exists()) {
            System.out.println("config.yml does not exist!");
            System.err.println("config.yml does not exist!");
            System.exit(1);
        }
        //Parse config file
        try {
            if (confFile.exists() && !confFile.isDirectory()) {
                config = YamlConfigParser.parseFile(confFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to parse config file");
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
        //retrieve stateFile
        File stateFile = new File(dataPath + File.separator + "in" + File.separator + "state.yml");
        LastState lastState = null;
        if (stateFile.exists()) {
            try {
                lastState = (LastState) YamlConfigParser.parseFile(stateFile, LastState.class);
            } catch (IOException ex) {
                Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("State file does not exist. (first run?)");
        }

        Client cl = new Client(config.getOAuthCredentials().getAppKey(), config.getParams().getDevKey(),
                config.getOAuthCredentials().getRefreshToken(), config.getOAuthCredentials().getAppSecret(), "", new Long(1));

        Map<String, Boolean> bulkReqestsToDownload = config.getParams().getBulkRequests().getBulkFiles();
        Map<String, Date> lastBulkRequests = null;
        Map<String, Date> lastReportRequests = null;

        //set download parameters according to previous runs
        if (lastState != null) {
            try {
                lastBulkRequests = lastState.getBulkRequests();
                lastReportRequests = lastState.getReportRequests();

            } catch (NullPointerException ex) {
                System.out.println("No mathing state.");
            }
        }

        LastState newState = new LastState(new HashMap(), new HashMap());

        System.out.println("Downloading bulk data...");
        /*Download bulk data*/
        Calendar lastSync = null;
        for (Entry<String, Boolean> br : bulkReqestsToDownload.entrySet()) {
            if (!br.getValue()) {
                continue;
            }
            //last run
            if (lastBulkRequests != null && config.getParams().getSinceLast()) {
                lastSync = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                lastSync.setTime(lastBulkRequests.get(br.getKey()));
            }
            boolean qScore = false;
            if (br.getKey().equals("CAMPAIGNS")) {
                qScore = config.getParams().getBulkRequests().getCampQualityScore();
                if (qScore) {
                    lastSync = null;
                }
            }
            BulkResult res = null;
            try {
                res = cl.downloadBulkData(br.getKey(), qScore, false, outTablesPath, lastSync);
            } catch (ClientException ex) {
                System.out.println(ex.getMessage());
                System.err.println(ex.getMessage());
                System.exit(ex.getSeverity());
            }

            //set new state
            newState.getBulkRequests().put(br.getKey(), res.getLastSync());

            //bulid manifest file
            ManifestFile man = new ManifestFile(config.getParams().getBucket() + "." + br.getKey().toLowerCase(), true, new String[]{"Id"}, ",", "\"");
            try {
                ManifestBuilder.buildManifestFile(man, outTablesPath, res.getResultFile().getName());
            } catch (IOException ex) {
                System.out.println("Error building manifest file " + ex.getMessage());
                System.err.println(ex.getMessage());
                System.exit(2);
            }

        }

        System.out.println("Downloading report queries...");
        /*Download reports */
        List<BReportRequest> repRequests = config.getParams().getReportRequests();
        lastSync = null;
        for (BReportRequest repReq : repRequests) {

            if (lastReportRequests != null && config.getParams().getSinceLast()) {
                lastSync = Calendar.getInstance();
                Date dt = lastReportRequests.get(repReq.getType().name());
                if (dt != null) {
                    lastSync.setTime(dt);
                }
            }

            ReportResult rResult = null;

            try {
                rResult = cl.downloadReport(repReq, outTablesPath, lastSync);
            } catch (ClientException ex) {
                System.out.println(ex.getMessage());
                System.err.println(ex.getMessage());
                System.exit(ex.getSeverity());
            }

            //set new state
            newState.getReportRequests().put(repReq.getType().name(), rResult.getLastSync());

            //bulid manifest file
            ManifestFile man = new ManifestFile(config.getParams().getBucket() + "." + repReq.getType().name(), true, repReq.getPkey(), ",", "\"");
            try {
                ManifestBuilder.buildManifestFile(man, outTablesPath, rResult.getResultFile().getName());
            } catch (IOException ex) {
                System.out.println("Error building manifest file " + ex.getMessage());
                System.err.println(ex.getMessage());
                System.exit(2);
            }
        }

        /*Write state file*/
        try {
            YamlStateWriter.writeStateFile(dataPath + File.separator + "out" + File.separator + "state.yml", newState);
        } catch (IOException ex) {
            System.out.println("Error building state file " + ex.getMessage());
            System.err.println(ex.getMessage());
            System.exit(1);
        }

    }
}
