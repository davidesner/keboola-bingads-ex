package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v11.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v11.reporting.ArrayOfKeywordPerformanceReportColumn;
import com.microsoft.bingads.v11.reporting.ArrayOflong;
import com.microsoft.bingads.v11.reporting.KeywordPerformanceReportColumn;
import com.microsoft.bingads.v11.reporting.KeywordPerformanceReportRequest;
import com.microsoft.bingads.v11.reporting.ReportAggregation;
import com.microsoft.bingads.v11.reporting.ReportTime;

/**
 * @author David Esner
 */
public class KeywordPerformanceReportProccessor extends RequestBuilderProcessor<KeywordPerformanceReportRequest> {

	public KeywordPerformanceReportProccessor() throws Exception {
		super(KeywordPerformanceReportRequest.class);
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
		ArrayOfKeywordPerformanceReportColumn columns = new ArrayOfKeywordPerformanceReportColumn();
		columns.getKeywordPerformanceReportColumns().addAll(Arrays.asList(KeywordPerformanceReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfKeywordPerformanceReportColumn columns = new ArrayOfKeywordPerformanceReportColumn();
		for (String col : colNames) {
			try {
				columns.getKeywordPerformanceReportColumns().add(KeywordPerformanceReportColumn.fromValue(col));
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
