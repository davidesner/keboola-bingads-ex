package keboola.bingads.ex.client.request;
/**
 * @author David Esner
 */

import java.lang.reflect.Constructor;
import java.util.List;

import com.microsoft.bingads.v12.reporting.ReportFormat;
import com.microsoft.bingads.v12.reporting.ReportRequest;
import com.microsoft.bingads.v12.reporting.ReportTime;

public abstract class RequestBuilderProcessor<T extends ReportRequest> {

	protected static final ReportFormat DEFAULT_FORMAT = ReportFormat.CSV;
	protected final T reportRequest;

	public RequestBuilderProcessor(Class<T> type) throws Exception {
		this.reportRequest = getReportRequestInstance(type);
	}

	private T getReportRequestInstance(Class<T> type) throws Exception {
		Constructor<T> constructorStr = type.getConstructor();
		return (T) constructorStr.newInstance();
	}

	public void setFormat(ReportFormat format) {
		if (format == null) {
			reportRequest.setFormat(DEFAULT_FORMAT);
		} else {
			reportRequest.setFormat(format);
		}
	}

	public void setReturnOnlyComplete(boolean value) {
		reportRequest.setReturnOnlyCompleteData(value);
	}

	public void setExcludeReportFooter(boolean val) {
		reportRequest.setExcludeReportFooter(val);
	}

	public void setExcludeReportHeader(boolean val) {
		reportRequest.setExcludeReportHeader(val);
	}

	public ReportRequest build() {
		return reportRequest;
	}

	abstract void setAggregation(String aggregationPeriod)  throws Exception;

	abstract void setTime(ReportTime reportTime);

	abstract void setDefaultCols();

	abstract void setCustomColumns(List<String> colNames)  throws Exception;
	
	abstract void setScopeAcounts(List<Long> accountIds);

}
