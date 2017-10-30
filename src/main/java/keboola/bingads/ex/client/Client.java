/*
 */
package keboola.bingads.ex.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.microsoft.bingads.ApiEnvironment;
import com.microsoft.bingads.AuthorizationData;
import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.PasswordAuthentication;
import com.microsoft.bingads.ServiceClient;
import com.microsoft.bingads.v11.bulk.ArrayOfDownloadEntity;
import com.microsoft.bingads.v11.bulk.BulkOperationProgressInfo;
import com.microsoft.bingads.v11.bulk.BulkServiceManager;
import com.microsoft.bingads.v11.bulk.DataScope;
import com.microsoft.bingads.v11.bulk.DownloadEntity;
import com.microsoft.bingads.v11.bulk.DownloadFileType;
import com.microsoft.bingads.v11.bulk.DownloadParameters;
import com.microsoft.bingads.v11.bulk.Progress;
import com.microsoft.bingads.v11.customermanagement.AccountInfo;
import com.microsoft.bingads.v11.customermanagement.GetAccountsInfoRequest;
import com.microsoft.bingads.v11.customermanagement.GetAccountsInfoResponse;
import com.microsoft.bingads.v11.customermanagement.ICustomerManagementService;
import com.microsoft.bingads.v11.reporting.AdApiError;
import com.microsoft.bingads.v11.reporting.AdApiFaultDetail_Exception;
import com.microsoft.bingads.v11.reporting.ApiFaultDetail_Exception;
import com.microsoft.bingads.v11.reporting.BatchError;
import com.microsoft.bingads.v11.reporting.OperationError;
import com.microsoft.bingads.v11.reporting.ReportRequest;
import com.microsoft.bingads.v11.reporting.ReportingDownloadParameters;
import com.microsoft.bingads.v11.reporting.ReportingServiceManager;

import keboola.bingads.ex.client.request.ReportRequestFactory;
import keboola.bingads.ex.config.pojos.BReportRequest;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Client {

	private String DEVELOPER_TOKEN;
	private final ApiEnvironment environment;
	private final OAuthKbcAppCodeGrant oAuthCodeGrant;
	private final AuthorizationData authorizationData;
	private int numberRetries = 4;
	private long failoverInterval = 60000;

	private ReportingServiceManager repServiceMgr;
	private BulkServiceManager bulkServiceMgr;
	private ServiceClient<ICustomerManagementService> custMgmtService;

	public Client(String appKey, String developerToken, String appSecret, OAuthTokens tokens, Long customerId) {
		oAuthCodeGrant = new OAuthKbcAppCodeGrant(appKey, appSecret, tokens);
		OAuthTokens toks = oAuthCodeGrant.refreshTokensIfNeeded(true);
		authorizationData = new AuthorizationData();
		authorizationData.setDeveloperToken(developerToken);
		authorizationData.setCustomerId(customerId);
		authorizationData.setAuthentication(oAuthCodeGrant);
		this.DEVELOPER_TOKEN = developerToken;
		this.environment = ApiEnvironment.PRODUCTION;
	}

	/**
	 * For sandbox testing account
	 */
	public Client(String username, String pass, Long customerId, String developerToken) {
		oAuthCodeGrant = null;
		authorizationData = new AuthorizationData();
		authorizationData.setDeveloperToken(developerToken);
		authorizationData.setCustomerId(customerId);
		authorizationData.setAuthentication(new PasswordAuthentication(username, pass));
		this.DEVELOPER_TOKEN = developerToken;
		this.environment = ApiEnvironment.SANDBOX;
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
	 * @throws Exception
	 */
	public ReportResult downloadReport(BReportRequest request, String resultPath, Calendar lastSync, List<Long> accountIds)
			throws Exception {

		ReportRequest r = ReportRequestFactory.buildFromConfig(accountIds, request,	lastSync);
		ReportResult res = tryPerformReportRequest(r, resultPath, request.getType().name() + ".csv");
		if (res != null) {
			res.setLastSync(new Date());
		}
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
	public BulkResult downloadBulkData(String type, boolean qualityScore, boolean performanceData,
			String resultFolderPath, Calendar lastSync, long accountId) throws ClientException {

		File resultFile = null;
		if ("CAMPAIGNS".equals(type)) {
			if (qualityScore) {
				lastSync = null;
			}
		} else {
			qualityScore = false;
		}

		try {
			authorizationData.setAccountId(accountId);
			BulkServiceManager bulkService = getBulkServiceMgr();
			bulkService.setStatusPollIntervalInMilliseconds(5000);

			Progress<BulkOperationProgressInfo> progress = new Progress<BulkOperationProgressInfo>() {
				@Override
				public void report(BulkOperationProgressInfo value) {
					System.out.println(value.getPercentComplete() + "% Complete\n");
				}
			};
			// set params
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

			ArrayOfDownloadEntity bulkDownloadEntities = new ArrayOfDownloadEntity();
			bulkDownloadEntities.getDownloadEntities().add(DownloadEntity.valueOf(type));

			downloadParameters.setDownloadEntities(bulkDownloadEntities);
			File directory = new File(resultFolderPath);
			if (!directory.exists()) {
				directory.mkdir();
			}

			downloadParameters.setResultFileDirectory(directory);
			downloadParameters.setResultFileName(type.toLowerCase() + accountId + ".csv");
			downloadParameters.setOverwriteResultFile(true);

			// Submit the download request, and the results file will be
			// downloaded to the specified local file.
			System.out.println("Downloading bulk data: " + type);
			resultFile = bulkService.downloadFileAsync(downloadParameters, progress, null).get();

		} catch (InterruptedException ex) {
			throw new ClientException("Error downloading bulk data: " + type + " " + ex, ex);
		} catch (ExecutionException ex) {
			String message = getBulkExecutionExceptionMessage(ex);
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
	 * 
	 * @param request
	 * @param resultFolderPath
	 * @param resultFileName
	 * @return
	 * @throws Exception
	 */
	public ReportResult tryPerformReportRequest(ReportRequest request, String resultFolderPath, String resultFileName)
			throws ClientException, ResultException {
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
	public ReportResult performReportRequest(ReportRequest request, String resultFolderPath, String resultFileName)
			throws ClientException, ResultException {

		ReportingDownloadParameters reportingDownloadParameters = new ReportingDownloadParameters();
		reportingDownloadParameters.setReportRequest(request);
		File directory = new File(resultFolderPath);
		if (!directory.exists()) {
			directory.mkdir();
		}
		reportingDownloadParameters.setResultFileDirectory(directory);
		reportingDownloadParameters.setResultFileName(resultFileName);
		reportingDownloadParameters.setOverwriteResultFile(true);

		File resultFile;
		try {
			System.out.println("Downloading report data: " + resultFileName);
			resultFile = getReportingServiceMgr().downloadFileAsync(reportingDownloadParameters, null).get();
		} catch (InterruptedException ex) {
			throw new ClientException("Error downloading report: " + resultFileName + " " + ex, ex);
		} catch (ExecutionException ex) {
			String message = getExecutionExceptionMessage(ex);
			throw new ClientException("Error downloading report " + message, ex);
		}

		ReportResult res = null;
		if (resultFile != null) {
			try {
				res = new ReportResult(resultFile, null);
			} catch (ResultException rx) {
				throw rx;
			} catch (Exception ex) {
				throw new ClientException(
						"Error proccessing report query result: " + request.getReportName() + " " + ex, ex);
			}
		}
		return res;
	}

	private String getExecutionExceptionMessage(ExecutionException ex) {
		String message = "";
		Throwable cause = getApiFaultDetail(ex);
		if (cause instanceof AdApiFaultDetail_Exception) {
			AdApiFaultDetail_Exception ee = (AdApiFaultDetail_Exception) cause;
			message += "The operation failed with the following faults:\n";

			for (AdApiError error : ee.getFaultInfo().getErrors().getAdApiErrors()) {
				message += "AdApiError\n";
				message += String.format("Code: %d\nError Code: %s\nMessage: %s\n\n", error.getCode(),
						error.getErrorCode(), error.getMessage());
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

		return message;

	}

	private String getBulkExecutionExceptionMessage(ExecutionException ex) {
		ex.printStackTrace();
		String message = "";
		Throwable cause = ex.getCause().getCause().getCause();
		if (cause instanceof com.microsoft.bingads.v11.bulk.AdApiFaultDetail_Exception) {
			com.microsoft.bingads.v11.bulk.AdApiFaultDetail_Exception ee = (com.microsoft.bingads.v11.bulk.AdApiFaultDetail_Exception) cause;
			message += "The operation failed with the following faults:\n";

			for (com.microsoft.bingads.v11.bulk.AdApiError error : ee.getFaultInfo().getErrors().getAdApiErrors()) {
				message += "AdApiError\n";
				message += String.format("Code: %d\nError Code: %s\nMessage: %s\n\n", error.getCode(),
						error.getErrorCode(), error.getMessage());
			}
		} else if (cause instanceof com.microsoft.bingads.v11.bulk.ApiFaultDetail_Exception) {
			com.microsoft.bingads.v11.bulk.ApiFaultDetail_Exception ee = (com.microsoft.bingads.v11.bulk.ApiFaultDetail_Exception) cause;
			message += "The operation failed with the following faults:\n";

			for (com.microsoft.bingads.v11.bulk.BatchError error : ee.getFaultInfo().getBatchErrors()
					.getBatchErrors()) {
				message += String.format("BatchError at Index: %d\n", error.getIndex());
				message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
			}

			for (com.microsoft.bingads.v11.bulk.OperationError error : ee.getFaultInfo().getOperationErrors()
					.getOperationErrors()) {
				message += "OperationError\n";
				message += String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
			}
		} else {
			message += ex.getMessage();
		}
		return message;
	}

	/**
	 * Gets api fault detail
	 * 
	 * @return
	 */
	private Throwable getApiFaultDetail(ExecutionException ex) {
		Throwable c = ex;
		Throwable prevC = null;
		while (!(c == null || c instanceof ApiFaultDetail_Exception || c instanceof AdApiFaultDetail_Exception)) {
			prevC = c;
			c = prevC.getCause();
		}
		return c;
	}

	public List<Long> getAllAccountIds() throws Exception {
		List<Long> accIds = new ArrayList<>();
		ServiceClient<ICustomerManagementService> cs = getcustMgmtkServiceMgr();
		GetAccountsInfoRequest params = new GetAccountsInfoRequest();
		params.setCustomerId(authorizationData.getCustomerId());
		GetAccountsInfoResponse resp = cs.getService().getAccountsInfo(params);
		for (AccountInfo accInfo : resp.getAccountsInfo().getAccountInfos()) {
			accIds.add(accInfo.getId());
		}
		return accIds;
	}

	public List<AccountInfo> getAllAccountsInfo() throws Exception {
		List<Long> accIds = new ArrayList<>();
		ServiceClient<ICustomerManagementService> cs = getcustMgmtkServiceMgr();
		GetAccountsInfoRequest params = new GetAccountsInfoRequest();
		params.setCustomerId(authorizationData.getCustomerId());
		GetAccountsInfoResponse resp = cs.getService().getAccountsInfo(params);
		if (resp == null) {
			return Collections.emptyList();
		}
		return resp.getAccountsInfo().getAccountInfos();
	}

	private ReportingServiceManager getReportingServiceMgr() {
		if (repServiceMgr == null) {
			this.repServiceMgr = new ReportingServiceManager(authorizationData, environment);
		}
		return repServiceMgr;
	}

	private BulkServiceManager getBulkServiceMgr() {
		if (bulkServiceMgr == null) {
			this.bulkServiceMgr = new BulkServiceManager(authorizationData, environment);
		}
		return bulkServiceMgr;
	}

	private ServiceClient<ICustomerManagementService> getcustMgmtkServiceMgr() {
		if (custMgmtService == null) {
			this.custMgmtService = new ServiceClient<ICustomerManagementService>(authorizationData, environment,
					ICustomerManagementService.class);
			;
		}
		return custMgmtService;
	}

}
