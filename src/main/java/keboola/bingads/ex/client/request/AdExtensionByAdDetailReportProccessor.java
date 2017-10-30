package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v11.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v11.reporting.AdExtensionByAdReportColumn;
import com.microsoft.bingads.v11.reporting.AdExtensionByAdReportRequest;
import com.microsoft.bingads.v11.reporting.ArrayOfAdExtensionByAdReportColumn;
import com.microsoft.bingads.v11.reporting.ArrayOflong;
import com.microsoft.bingads.v11.reporting.ReportAggregation;
import com.microsoft.bingads.v11.reporting.ReportTime;

/**
 * @author David Esner
 */
public class AdExtensionByAdDetailReportProccessor
		extends RequestBuilderProcessor<AdExtensionByAdReportRequest> {

	public AdExtensionByAdDetailReportProccessor() throws Exception {
		super(AdExtensionByAdReportRequest.class);
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
		ArrayOfAdExtensionByAdReportColumn columns = new ArrayOfAdExtensionByAdReportColumn();
		columns.getAdExtensionByAdReportColumns().addAll(Arrays.asList(AdExtensionByAdReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfAdExtensionByAdReportColumn columns = new ArrayOfAdExtensionByAdReportColumn();
		for (String col : colNames) {
			try {
				columns.getAdExtensionByAdReportColumns().add(AdExtensionByAdReportColumn.fromValue(col));
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
