/*
 */
package keboola.bingads.ex.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.microsoft.bingads.AuthorizationData;
import com.microsoft.bingads.OAuthDesktopMobileAuthCodeGrant;
import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.ServiceClient;
import com.microsoft.bingads.customermanagement.AccountInfo;
import com.microsoft.bingads.customermanagement.GetAccountsInfoRequest;
import com.microsoft.bingads.customermanagement.GetAccountsInfoResponse;
import com.microsoft.bingads.customermanagement.ICustomerManagementService;
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
    private int numberRetries = 4;
    private long failoverInterval = 60000;

    /**
     *
     * @param clientId
     * @param developerToken
     * @param refreshToken
     * @param clientSecret
     * @param accessToken
     * @param expiresIn
     * @param customerId
     * @param accountId
     */
    public Client(String clientId, String developerToken, String refreshToken, String clientSecret, String accessToken, Long expiresIn, Long customerId, Long accountId) {
        oAuthCodeGrant = new OAuthKbcAppCodeGrant(clientId, clientSecret, accessToken, refreshToken, expiresIn);
        OAuthDesktopMobileAuthCodeGrant oAuthWebAuthCodeGrant = new OAuthDesktopMobileAuthCodeGrant(clientId, refreshToken);

        authorizationData = new AuthorizationData();

        authorizationData.setDeveloperToken(developerToken);
        //authorizationData.setAccountId(accountId);
        authorizationData.setCustomerId(customerId);
        authorizationData.setAuthentication(oAuthCodeGrant);
        //authorizationData.setAccountId(B015L3PC);

        this.DEVELOPER_TOKEN = developerToken;

    }

    public void setFailoverParams(int nrRetries, long waitInterval) {
    	this.numberRetries = nrRetries;
    	this.failoverInterval = waitInterval;
    }

    /**
     *
     * @param request
     * @param resultPath
     * @param lastSync
     * @return
     * @throws ClientException
     */
    public ReportResult downloadReport(BReportRequest request, String resultPath, Calendar lastSync, long accountId) throws ClientException, ResultException {

    	OAuthTokens tokens = oAuthCodeGrant.refreshTokensIfNeeded(true);
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
            time.setPredefinedTime(com.microsoft.bingads.reporting.ReportTimePeriod.fromValue(request.getReportPeriod()));
        }

        ReportRequest r = ReportRequestFactory.createReportRequest(request, accountId, time);

        ReportResult res = tryPerformReportRequest(r, resultPath, request.getType().name() + accountId + ".csv");
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
    public BulkResult downloadBulkData(String type, boolean qualityScore, boolean performanceData, String resultFolderPath, Calendar lastSync, long accountId) throws ClientException {

        File resultFile = null;
        try {
        	
            OAuthTokens tokens = oAuthCodeGrant.refreshTokensIfNeeded(true);
            authorizationData.setAccountId(accountId);
            BulkServiceManager bulkService = new BulkServiceManager(authorizationData);
// Poll for downloads at reasonable intervals. You know your data better than anyone.
// If you download an account that is well less than one million keywords, consider polling
// at 15 to 20 second intervals. If the account contains about one million keywords, consider polling
// at one minute intervals after waiting five minutes. For accounts with about four million keywords,
// consider polling at one minute intervals after waiting 10 minutes.
            bulkService.setStatusPollIntervalInMilliseconds(5000);

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
            if (qualityScore) {
                dataScope.add(DataScope.QUALITY_SCORE_DATA);
            }

            downloadParameters.setDataScope(dataScope);
            downloadParameters.setCampaignIds(null);
            downloadParameters.setFileType(DownloadFileType.CSV);
            downloadParameters.setLastSyncTimeInUTC(lastSync);

            ArrayList<BulkDownloadEntity> bulkDownloadEntities = new ArrayList<BulkDownloadEntity>();
            bulkDownloadEntities.add(BulkDownloadEntity.valueOf(type));

            downloadParameters.setEntities(bulkDownloadEntities);
            File directory = new File(resultFolderPath);
            if (! directory.exists()){
                directory.mkdir();
            }

            downloadParameters.setResultFileDirectory(directory);
            downloadParameters.setResultFileName(type.toLowerCase() + accountId + ".csv");
            downloadParameters.setOverwriteResultFile(true);

            // Submit the download request, and the results file will be downloaded to the specified local file.
            System.out.println("Downloading bulk data: " + type);
            resultFile = bulkService.downloadFileAsync(
                    downloadParameters,
                    progress,
                    null).get();

        } catch (InterruptedException ex) {
            throw new ClientException("Error downloading bulk data: " + type + " " + ex, ex);
        } catch (ExecutionException ex) {
        	ex.printStackTrace();
            String message = "";
            Throwable cause = ex.getCause().getCause().getCause();
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
            throw new ClientException("Error downloading report " + message, ex);

        }
        BulkResult result;
        if (resultFile != null) {
            try {
                result = new BulkResult(resultFile);
            } catch (Exception ex) {
                throw new ClientException("Error proccessing report query result: " + type + " " + ex.getMessage(), ex);
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Try request with simple failover strategy
     * @param request
     * @param resultFolderPath
     * @param resultFileName
     * @return
     * @throws Exception
     */
	public ReportResult tryPerformReportRequest(ReportRequest request, String resultFolderPath, String resultFileName) throws ClientException, ResultException {
		ReportResult res = null;
		boolean cont;
		int retries = 0;
		do {
			cont = false;
			retries++;
			try {
				res = performReportRequest(request, resultFolderPath, resultFileName);
			} catch (ClientException | ResultException e) {
				if (retries >= numberRetries) {
					throw e;
				}
				cont = true;
				System.out.println("Failed to perform ReportRequest. Retrying for " + retries + ". time.");
			}
		} while (cont);
		return res;
	}
    /**
     *
     * @param request
     * @param resultFolderPath
     * @param resultFileName
     * @return
     * @throws ClientException
     */
    public ReportResult performReportRequest(ReportRequest request, String resultFolderPath, String resultFileName) throws ClientException, ResultException {
        OAuthTokens tokens = oAuthCodeGrant.refreshTokensIfNeeded(true);
        ReportingServiceManager rm = new ReportingServiceManager(authorizationData);

        ReportingDownloadParameters reportingDownloadParameters = new ReportingDownloadParameters();
        reportingDownloadParameters.setReportRequest(request);
        File directory = new File(resultFolderPath);
        if (! directory.exists()){
            directory.mkdir();
        }
        reportingDownloadParameters.setResultFileDirectory(directory);
        reportingDownloadParameters.setResultFileName(resultFileName);
        reportingDownloadParameters.setOverwriteResultFile(true);

        File resultFile;
        try {
            System.out.println("Downloading report data: " + resultFileName);
            resultFile = rm.downloadFileAsync(
                    reportingDownloadParameters,
                    null).get();
        } catch (InterruptedException ex) {
            throw new ClientException("Error downloading report: " + resultFileName + " " + ex, ex);
        } catch (ExecutionException ex) {
            String message = "";
            Throwable cause = getApiFaultDetail(ex);
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
            throw new ClientException("Error downloading report " + message, ex);
        }

        ReportResult res = null;
        if (resultFile != null) {
            try {
                res = new ReportResult(resultFile, null);
                System.out.println("Complete.");
            } catch (ResultException rx) {
                throw rx;
            } catch (Exception ex) {
                throw new ClientException("Error proccessing report query result: " + request.getReportName() + " " + ex, ex);
            }
        }
        return res;
    }

    /**
     * Gets api fault detail  
     * @return
     */
    private Throwable getApiFaultDetail(ExecutionException ex) {
    	Throwable c = ex;
    	Throwable prevC = null;
    	while (!(c == null || c instanceof ApiFaultDetail_Exception || c instanceof AdApiFaultDetail_Exception)){
    		prevC = c;
    		c = prevC.getCause();
    	}
    	return c;
	}

	public List<Long> getAllAccountIds() throws Exception {
    	List<Long> accIds = new ArrayList<>();
    	ServiceClient<ICustomerManagementService> cs = new ServiceClient<ICustomerManagementService>(
    			authorizationData, 
    			ICustomerManagementService.class);
    	GetAccountsInfoRequest params =  new GetAccountsInfoRequest();
    	params.setCustomerId(authorizationData.getCustomerId());
    	GetAccountsInfoResponse resp = cs.getService().getAccountsInfo(params);
    	for(AccountInfo accInfo : resp.getAccountsInfo().getAccountInfos()) {
    		accIds.add(accInfo.getId());
    	}
    	return accIds;
    }

    public List<AccountInfo> getAllAccountsInfo() throws Exception {
    	List<Long> accIds = new ArrayList<>();
    	ServiceClient<ICustomerManagementService> cs = new ServiceClient<ICustomerManagementService>(
    			authorizationData, 
    			ICustomerManagementService.class);
    	GetAccountsInfoRequest params =  new GetAccountsInfoRequest();
    	params.setCustomerId(authorizationData.getCustomerId());
    	GetAccountsInfoResponse resp = cs.getService().getAccountsInfo(params);
    	if(resp == null) {
    		return Collections.emptyList();
    	}
    	return resp.getAccountsInfo().getAccountInfos();
    }
    
    
}
