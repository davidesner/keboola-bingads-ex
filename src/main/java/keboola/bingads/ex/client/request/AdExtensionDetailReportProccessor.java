package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v12.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v12.reporting.AdExtensionDetailReportColumn;
import com.microsoft.bingads.v12.reporting.AdExtensionDetailReportRequest;
import com.microsoft.bingads.v12.reporting.ArrayOfAdExtensionDetailReportColumn;
import com.microsoft.bingads.v12.reporting.ArrayOflong;
import com.microsoft.bingads.v12.reporting.ReportAggregation;
import com.microsoft.bingads.v12.reporting.ReportTime;

/**
 * @author David Esner
 */
public class AdExtensionDetailReportProccessor extends RequestBuilderProcessor<AdExtensionDetailReportRequest> {

	public AdExtensionDetailReportProccessor() throws Exception {
		super(AdExtensionDetailReportRequest.class);
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
		ArrayOfAdExtensionDetailReportColumn columns = new ArrayOfAdExtensionDetailReportColumn();
		columns.getAdExtensionDetailReportColumns().addAll(Arrays.asList(AdExtensionDetailReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfAdExtensionDetailReportColumn columns = new ArrayOfAdExtensionDetailReportColumn();
		for (String col : colNames) {
			try {
				columns.getAdExtensionDetailReportColumns().add(AdExtensionDetailReportColumn.fromValue(col));
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
