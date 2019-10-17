package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v13.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v13.reporting.AdExtensionByKeywordReportColumn;
import com.microsoft.bingads.v13.reporting.AdExtensionByKeywordReportRequest;
import com.microsoft.bingads.v13.reporting.ArrayOfAdExtensionByKeywordReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOflong;
import com.microsoft.bingads.v13.reporting.ReportAggregation;
import com.microsoft.bingads.v13.reporting.ReportTime;

/**
 * @author David Esner
 */
public class AdExtensionByKeywordDetailReportProccessor
		extends RequestBuilderProcessor<AdExtensionByKeywordReportRequest> {

	public AdExtensionByKeywordDetailReportProccessor() throws Exception {
		super(AdExtensionByKeywordReportRequest.class);
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
		ArrayOfAdExtensionByKeywordReportColumn columns = new ArrayOfAdExtensionByKeywordReportColumn();
		columns.getAdExtensionByKeywordReportColumns().addAll(Arrays.asList(AdExtensionByKeywordReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfAdExtensionByKeywordReportColumn columns = new ArrayOfAdExtensionByKeywordReportColumn();
		for (String col : colNames) {
			try {
				columns.getAdExtensionByKeywordReportColumns().add(AdExtensionByKeywordReportColumn.fromValue(col));
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
