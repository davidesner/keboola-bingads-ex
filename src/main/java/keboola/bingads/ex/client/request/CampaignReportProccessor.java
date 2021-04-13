package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v13.reporting.AccountThroughCampaignReportScope;
import com.microsoft.bingads.v13.reporting.ArrayOfCampaignPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOflong;
import com.microsoft.bingads.v13.reporting.CampaignPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.CampaignPerformanceReportRequest;
import com.microsoft.bingads.v13.reporting.ReportAggregation;
import com.microsoft.bingads.v13.reporting.ReportTime;

/**
 * @author David Esner
 */
public class CampaignReportProccessor extends RequestBuilderProcessor<CampaignPerformanceReportRequest> {

	public CampaignReportProccessor() throws Exception {
		super(CampaignPerformanceReportRequest.class);
	}

	@Override
	void setAggregation(String aggregationPeriod) {
		reportRequest.setAggregation(ReportAggregation.fromValue(aggregationPeriod));
	}

	@Override
	void setTime(ReportTime reportTime) {
		reportRequest.setTime(reportTime);
	}

	@Override
	void setDefaultCols() {
		ArrayOfCampaignPerformanceReportColumn columns = new ArrayOfCampaignPerformanceReportColumn();
		columns.getCampaignPerformanceReportColumns().addAll(Arrays.asList(CampaignPerformanceReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfCampaignPerformanceReportColumn columns = new ArrayOfCampaignPerformanceReportColumn();
		for (String col : colNames) {
			try {
				columns.getCampaignPerformanceReportColumns().add(CampaignPerformanceReportColumn.fromValue(col));
			} catch (IllegalArgumentException ex) {
				throw new Exception("Unable to proccess request " + reportRequest.getReportName() + ". '" + col
						+ "' is not a valid column name, check request specs.\n");
			}
		}
		reportRequest.setColumns(columns);
	}

	@Override
	void setScopeAcounts(List<Long> accountIds) {
		AccountThroughCampaignReportScope sc = new AccountThroughCampaignReportScope();
		ArrayOflong aIds = new ArrayOflong();
		aIds.getLongs().addAll(accountIds);
		sc.setAccountIds(aIds);
		reportRequest.setScope(sc);

	}

}
