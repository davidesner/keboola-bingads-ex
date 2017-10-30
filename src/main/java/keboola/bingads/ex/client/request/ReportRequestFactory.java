package keboola.bingads.ex.client.request;

import java.util.Calendar;
import java.util.List;

import com.microsoft.bingads.v11.reporting.Date;
import com.microsoft.bingads.v11.reporting.ReportFormat;
import com.microsoft.bingads.v11.reporting.ReportRequest;
import com.microsoft.bingads.v11.reporting.ReportTime;
import com.microsoft.bingads.v11.reporting.ReportTimePeriod;

import keboola.bingads.ex.config.pojos.BReportRequest;
import keboola.bingads.ex.config.pojos.BReportRequest.ReportType;

/**
 * @author David Esner
 */
@SuppressWarnings("rawtypes")
public class ReportRequestFactory {

	public static ReportRequest buildFromConfig(List<Long> accIds, BReportRequest reportReqParams, Calendar lastSync)
			throws Exception {
		RequestBuilderProcessor builder = getBuilderByType(reportReqParams.getType());
		builder.setDefaultCols();
		builder.setAggregation(reportReqParams.getAggregationPeriod());
		builder.setTime(buildTimePeriod(reportReqParams, lastSync));
		builder.setScopeAcounts(accIds);
		builder.setFormat(ReportFormat.CSV);
		builder.setReturnOnlyComplete(reportReqParams.isCompleteData());
		builder.setExcludeReportFooter(true);
		builder.setExcludeReportHeader(true);
		return builder.build();

	}

	private static ReportTime buildTimePeriod(BReportRequest request, Calendar lastSync) {
		Date startDate = new Date();
		Date endDate = new Date();
		Calendar curr = Calendar.getInstance();

		ReportTime time = new ReportTime();
		if (request.getReportPeriod() == null) {
			if (lastSync == null) {
				startDate.setDay(request.getStartDay());
				startDate.setMonth(request.getStartMonth()+1);
				startDate.setYear(request.getStartYear());
			} else {
				startDate.setDay(lastSync.get(Calendar.DAY_OF_MONTH));
				startDate.setMonth(lastSync.get(Calendar.MONTH)+1);
				startDate.setYear(lastSync.get(Calendar.YEAR));
			}

			endDate.setDay(curr.get(Calendar.DAY_OF_MONTH));
			endDate.setMonth(curr.get(Calendar.MONTH)+1);
			endDate.setYear(curr.get(Calendar.YEAR));

			time.setCustomDateRangeStart(startDate);
			time.setCustomDateRangeEnd(endDate);
		} else {
			time.setPredefinedTime(ReportTimePeriod.fromValue(request.getReportPeriod()));
		}

		return time;
	}

	private static RequestBuilderProcessor getBuilderByType(ReportType type) throws Exception {
		switch (type) {
		case AdsPerformance:
			return new AdPerformanceReportProccessor();
		case AdExtensionByAd:
			return new AdExtensionByAdDetailReportProccessor();
		case AdExtensionByKeyWord:
			return new AdExtensionByKeywordDetailReportProccessor();
		case AdExtensionDetail:
			return new AdExtensionDetailReportProccessor();
		case KeywordPerformance:
			return new KeywordPerformanceReportProccessor();
		default:
			throw new IllegalArgumentException("Unsupported report type " + type.name());
		}
	}
}
