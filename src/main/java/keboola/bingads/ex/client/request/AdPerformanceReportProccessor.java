package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v12.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v12.reporting.AdPerformanceReportColumn;
import com.microsoft.bingads.v12.reporting.AdPerformanceReportRequest;
import com.microsoft.bingads.v12.reporting.ArrayOfAdPerformanceReportColumn;
import com.microsoft.bingads.v12.reporting.ArrayOflong;
import com.microsoft.bingads.v12.reporting.ReportAggregation;
import com.microsoft.bingads.v12.reporting.ReportTime;

/**
 * @author David Esner
 */
public class AdPerformanceReportProccessor extends RequestBuilderProcessor<AdPerformanceReportRequest> {

	public AdPerformanceReportProccessor() throws Exception {
		super(AdPerformanceReportRequest.class);
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
		ArrayOfAdPerformanceReportColumn columns = new ArrayOfAdPerformanceReportColumn();
		columns.getAdPerformanceReportColumns().addAll(Arrays.asList(AdPerformanceReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfAdPerformanceReportColumn columns = new ArrayOfAdPerformanceReportColumn();
		for (String col : colNames) {
			try {
				columns.getAdPerformanceReportColumns().add(AdPerformanceReportColumn.fromValue(col));
			} catch (IllegalArgumentException ex) {
				throw new Exception("Unable to proccess request " + reportRequest.getReportName() + ". '" + col
						+ "' is not a valid column name, check request specs.\n");
			}
		}
		reportRequest.setColumns(columns);
	}

	@Override
	void setScopeAcounts(List<Long> accountIds) {
		AccountThroughAdGroupReportScope sc = new AccountThroughAdGroupReportScope();
		ArrayOflong aIds = new ArrayOflong();
		aIds.getLongs().addAll(accountIds);
		sc.setAccountIds(aIds);
		reportRequest.setScope(sc);

	}

}
