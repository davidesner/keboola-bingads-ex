/*
 */
package keboola.bingads.ex.client;

import com.microsoft.bingads.AuthorizationData;
import com.microsoft.bingads.OAuthDesktopMobileAuthCodeGrant;
import com.microsoft.bingads.OAuthTokens;

import com.microsoft.bingads.reporting.AdApiError;
import com.microsoft.bingads.reporting.AdApiFaultDetail_Exception;
import com.microsoft.bingads.reporting.ApiFaultDetail_Exception;
import com.microsoft.bingads.reporting.BatchError;
import com.microsoft.bingads.reporting.Date;
import com.microsoft.bingads.reporting.OperationError;
import com.microsoft.bingads.reporting.ReportRequest;
import com.microsoft.bingads.reporting.ReportTime;
import com.microsoft.bingads.reporting.ReportingDownloadParameters;
import com.microsoft.bingads.reporting.ReportingServiceManager;

import com.microsoft.bingads.v10.bulk.BulkDownloadEntity;
import com.microsoft.bingads.v10.bulk.BulkOperationProgressInfo;
import com.microsoft.bingads.v10.bulk.BulkServiceManager;
import com.microsoft.bingads.v10.bulk.DataScope;
import com.microsoft.bingads.v10.bulk.DownloadFileType;
import com.microsoft.bingads.v10.bulk.DownloadParameters;
import com.microsoft.bingads.v10.bulk.Progress;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import keboola.bingads.ex.config.pojos.BReportRequest;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Client {

    private String DEVELOPER_TOKEN;
    private final OAuthKbcAppCodeGrant oAuthCodeGrant;
    private final AuthorizationData authorizationData;

    /**
     *
     * @param clientId
     * @param developerToken
     * @param refreshToken
     * @param clientSecret
     * @param accessToken
     * @param expiresIn
     */
    public Client(String clientId, String developerToken, String refreshToken, String clientSecret, String accessToken, Long expiresIn) {
        oAuthCodeGrant = new OAuthKbcAppCodeGrant(clientId, clientSecret, accessToken, refreshToken, expiresIn);
        OAuthDesktopMobileAuthCodeGrant oAuthWebAuthCodeGrant = new OAuthDesktopMobileAuthCodeGrant(clientId, refreshToken);

        authorizationData = new AuthorizationData();
        authorizationData.setDeveloperToken(developerToken);
        authorizationData.setAccountId(45027777);
        authorizationData.setCustomerId(16221187);
        authorizationData.setAuthentication(oAuthCodeGrant);
        //authorizationData.setAccountId(B015L3PC);

        this.DEVELOPER_TOKEN = developerToken;

    }

    /**
     *
     * @param request
     * @param resultPath
     * @param lastSync
     * @return
     * @throws ClientException
     */
    public ReportResult downloadReport(BReportRequest request, String resultPath, Calendar lastSync) throws ClientException {

        Date startDate = new Date();
        Date endDate = new Date();
        Calendar curr = Calendar.getInstance();

        ReportTime time = new ReportTime();
        if (request.getReportPeriod() == null) {
            if (lastSync == null) {
                startDate.setDay(request.getStartDay());
                startDate.setMonth(request.getStartMonth());
                startDate.setYear(request.getStartYear());
            } else {
                startDate.setDay(lastSync.get(Calendar.DAY_OF_MONTH));
                startDate.setMonth(lastSync.get(Calendar.MONTH));
                startDate.setYear(lastSync.get(Calendar.YEAR));
            }

            endDate.setDay(curr.get(Calendar.DAY_OF_MONTH));
            endDate.setMonth(curr.get(Calendar.MONTH));
            endDate.setYear(curr.get(Calendar.YEAR));

            time.setCustomDateRangeStart(startDate);
            time.setCustomDateRangeEnd(endDate);
        } else {
            time.setPredefinedTime(com.microsoft.bingads.reporting.ReportTimePeriod.valueOf(request.getReportPeriod()));
        }

        ReportRequest r = ReportRequestFactory.createReportRequest(request, authorizationData.getAccountId(), time);

        ReportResult res = performReportRequest(r, resultPath, request.getType().name() + ".csv");
        res.setLastSync(curr.getTime());
        return res;
    }

    /**
     *
     * @param type
     * @param qualityScore
     * @param performanceData
     * @param resultFolderPath
     * @param lastSync
     * @return
     * @throws ClientException
     */
    public BulkResult downloadBulkData(String type, boolean qualityScore, boolean performanceData, String resultFolderPath, Calendar lastSync) throws ClientException {

        File resultFile = null;
        try {

            OAuthTokens tokens = oAuthCodeGrant.refreshTokensIfNeeded(true);

            BulkServiceManager BulkService = new BulkServiceManager(authorizationData);

// Poll for downloads at reasonable intervals. You know your data better than anyone.
// If you download an account that is well less than one million keywords, consider polling
// at 15 to 20 second intervals. If the account contains about one million keywords, consider polling
// at one minute intervals after waiting five minutes. For accounts with about four million keywords,
// consider polling at one minute intervals after waiting 10 minutes.
            BulkService.setStatusPollIntervalInMilliseconds(5000);

            Progress<BulkOperationProgressInfo> progress = new Progress<BulkOperationProgressInfo>() {
                @Override
                public void report(BulkOperationProgressInfo value) {
                    System.out.println(value.getPercentComplete() + "% Complete\n");
                }
            };
            //set params
            DownloadParameters downloadParameters = new DownloadParameters();

            ArrayList<DataScope> dataScope = new ArrayList<DataScope>();
            dataScope.add(DataScope.ENTITY_DATA);
            //if(perfData) dataScope.add(DataScope.ENTITY_PERFORMANCE_DATA);
            if (qualityScore) {
                dataScope.add(DataScope.QUALITY_SCORE_DATA);
            }

            downloadParameters.setDataScope(dataScope);
            downloadParameters.setCampaignIds(null);
            downloadParameters.setFileType(DownloadFileType.CSV);
            downloadParameters.setLastSyncTimeInUTC(lastSync);
            /* in case of performance data*/
 /* PerformanceStatsDateRange dr = new PerformanceStatsDateRange();
            dr.setPredefinedTime(ReportTimePeriod.LAST_WEEK);
            //downloadParameters.setPerformanceStatsDateRange(dr);*/

            ArrayList<BulkDownloadEntity> bulkDownloadEntities = new ArrayList<BulkDownloadEntity>();
            bulkDownloadEntities.add(BulkDownloadEntity.valueOf(type));

            downloadParameters.setEntities(bulkDownloadEntities);
            downloadParameters.setResultFileDirectory(new File(resultFolderPath));
            downloadParameters.setResultFileName(type.toLowerCase() + ".csv");
            downloadParameters.setOverwriteResultFile(true);

            // Submit the download request, and the results file will be downloaded to the specified local file.
            System.out.println("Downloading bulk data: " + type);
            resultFile = BulkService.downloadFileAsync(
                    downloadParameters,
                    progress,
                    null).get();

        } catch (InterruptedException ex) {
            throw new ClientException("Error downloading bulk data: " + type + " " + ex);
        } catch (ExecutionException ex) {
            String message = "";
            Throwable cause = ex.getCause();
            if (cause instanceof com.microsoft.bingads.v10.bulk.AdApiFaultDetail_Exception) {
                com.microsoft.bingads.v10.bulk.AdApiFaultDetail_Exception ee = (com.microsoft.bingads.v10.bulk.AdApiFaultDetail_Exception) cause;
                message += "The operation failed with the following faults:\n";

                for (com.microsoft.bingads.v10.bulk.AdApiError error : ee.getFaultInfo().getErrors().getAdApiErrors()) {
                    message += "AdApiError\n";
                    message += String.format("Code: %d\nError Code: %s\nMessage: %s\n\n",
                            error.getCode(), error.getErrorCode(), error.getMessage());
                }
            } else if (cause instanceof com.microsoft.bingads.v10.bulk.ApiFaultDetail_Exception) {
                com.microsoft.bingads.v10.bulk.ApiFaultDetail_Exception ee = (com.microsoft.bingads.v10.bulk.ApiFaultDetail_Exception) cause;
                message += "The operation failed with the following faults:\n";

                for (com.microsoft.bingads.v10.bulk.BatchError error : ee.getFaultInfo().getBatchErrors().getBatchErrors()) {
                    message += String.format("BatchError at Index: %d\n", error.getIndex());
                    message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
                }

                for (com.microsoft.bingads.v10.bulk.OperationError error : ee.getFaultInfo().getOperationErrors().getOperationErrors()) {
                    message += "OperationError\n";
                    message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
                }
            } else {
                message += ex.getMessage();
            }
            throw new ClientException("Error downloading report " + message);

        }
        BulkResult result;
        if (resultFile != null) {
            try {
                result = new BulkResult(resultFile);
            } catch (Exception ex) {
                throw new ClientException("Error proccessing report query result: " + type + " " + ex.getMessage());
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     *
     * @param request
     * @param resultFolderPath
     * @param resultFileName
     * @return
     * @throws ClientException
     */
    public ReportResult performReportRequest(ReportRequest request, String resultFolderPath, String resultFileName) throws ClientException {
        OAuthTokens tokens = oAuthCodeGrant.refreshTokensIfNeeded(true);
        ReportingServiceManager rm = new ReportingServiceManager(authorizationData);

        ReportingDownloadParameters reportingDownloadParameters = new ReportingDownloadParameters();
        reportingDownloadParameters.setReportRequest(request);
        reportingDownloadParameters.setResultFileDirectory(new File(resultFolderPath));
        reportingDownloadParameters.setResultFileName(resultFileName);
        reportingDownloadParameters.setOverwriteResultFile(true);

        File resultFile;
        try {
            System.out.println("Downloading report data: " + resultFileName);
            resultFile = rm.downloadFileAsync(
                    reportingDownloadParameters,
                    null).get();
        } catch (InterruptedException ex) {
            throw new ClientException("Error downloading report: " + resultFileName + " " + ex);
        } catch (ExecutionException ex) {
            String message = "";
            Throwable cause = ex.getCause();
            if (cause instanceof AdApiFaultDetail_Exception) {
                AdApiFaultDetail_Exception ee = (AdApiFaultDetail_Exception) cause;
                message += "The operation failed with the following faults:\n";

                for (AdApiError error : ee.getFaultInfo().getErrors().getAdApiErrors()) {
                    message += "AdApiError\n";
                    message += String.format("Code: %d\nError Code: %s\nMessage: %s\n\n",
                            error.getCode(), error.getErrorCode(), error.getMessage());
                }
            } else if (cause instanceof ApiFaultDetail_Exception) {
                ApiFaultDetail_Exception ee = (ApiFaultDetail_Exception) cause;
                message += "The operation failed with the following faults:\n";

                for (BatchError error : ee.getFaultInfo().getBatchErrors().getBatchErrors()) {
                    message += String.format("BatchError at Index: %d\n", error.getIndex());
                    message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
                }

                for (OperationError error : ee.getFaultInfo().getOperationErrors().getOperationErrors()) {
                    message += "OperationError\n";
                    message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
                }
            } else {
                message += ex.getMessage();
            }
            throw new ClientException("Error downloading report " + message);
        }

        ReportResult res = null;
        if (resultFile != null) {
            try {
                res = new ReportResult(resultFile, null);
                System.out.println("Complete.");
            } catch (Exception ex) {
                throw new ClientException("Error proccessing report query result: " + request.getReportName() + " " + ex);
            }
        }
        return res;
    }

}
