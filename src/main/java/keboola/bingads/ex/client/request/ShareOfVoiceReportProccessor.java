package keboola.bingads.ex.client.request;

import java.util.Arrays;
import java.util.List;

import com.microsoft.bingads.v13.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v13.reporting.ArrayOfShareOfVoiceReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOflong;
import com.microsoft.bingads.v13.reporting.ReportAggregation;
import com.microsoft.bingads.v13.reporting.ReportTime;
import com.microsoft.bingads.v13.reporting.ShareOfVoiceReportColumn;
import com.microsoft.bingads.v13.reporting.ShareOfVoiceReportRequest;

/**
 * @author David Esner
 */
public class ShareOfVoiceReportProccessor extends RequestBuilderProcessor<ShareOfVoiceReportRequest> {

	public ShareOfVoiceReportProccessor() throws Exception {
		super(ShareOfVoiceReportRequest.class);
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
		ArrayOfShareOfVoiceReportColumn columns = new ArrayOfShareOfVoiceReportColumn();
		columns.getShareOfVoiceReportColumns().addAll(Arrays.asList(ShareOfVoiceReportColumn.values()));
		reportRequest.setColumns(columns);
	}

	@Override
	void setCustomColumns(List<String> colNames) throws Exception {
		ArrayOfShareOfVoiceReportColumn columns = new ArrayOfShareOfVoiceReportColumn();
		for (String col : colNames) {
			try {
				columns.getShareOfVoiceReportColumns().add(ShareOfVoiceReportColumn.fromValue(col));
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
