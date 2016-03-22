/*
 */
package keboola.bingads.ex.config.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.bingads.reporting.ReportAggregation;
import com.microsoft.bingads.reporting.ReportTimePeriod;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import keboola.bingads.ex.config.ValidationException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class BReportRequest {

    public enum reportTypes {
        AdsPerformance,
        KeywordPerformance,
        AdExtensionPerformanceDetail,
        AdExtensionByKeyWord,
        AdExtensionByAd
    }
    private final String type;
    //start date of fetched interval in format: 05-10-2015 21:00
    private String startDate;
    private Date date_from;

    private final String[] columns;

    private final String aggregationPeriod;

    private final String reportPeriod;

//default 1
    private final Integer incremental;
//required if incremental = 1
    private final String[] pkey;

    public BReportRequest(@JsonProperty("type") String type, @JsonProperty("startDate") String startDate, @JsonProperty("aggregationPeriod") String aggregationPeriod,
            @JsonProperty("incremental") Integer incremental, @JsonProperty("columns") String[] columns,
            @JsonProperty("reportPeriod") String reportPeriod, @JsonProperty("pkey") String[] pkey) throws ParseException {

        if (incremental != null) {
            this.incremental = incremental;
        } else {
            this.incremental = 0;
        }
        this.pkey = pkey;
        this.columns = columns;
        this.type = type;
        this.reportPeriod = reportPeriod;
        if (aggregationPeriod == null) {
            this.aggregationPeriod = "DAILY";
        } else {
            this.aggregationPeriod = aggregationPeriod;
        }
        if (startDate != null) {
            this.startDate = startDate;
            setDate_from(startDate);
        }
    }

    public boolean validate() throws ValidationException {
        String message = "Report request configuration error: ";
        int l = message.length();
        if (type == null) {
            message += "type parameter is missing! ";
        }
        if (startDate == null && reportPeriod == null) {
            message += "One of startDate or reportPeriod parameters must be set! ";
        }
        if (!isValidType(type)) {
            message += " type parameter: '" + type + "' is invalid, check for supported types! ";
        }
        if (!isValidPeriod(reportPeriod) && reportPeriod != null) {
            message += " reportPeriod parameter: '" + reportPeriod + "' is invalid, check for supported types! ";
        }
        if (!isValidAggregation(aggregationPeriod)) {
            message += " Parameter aggregationPeriod: '" + aggregationPeriod + "' is invalid, check for supported types! ";
        }
        if (incremental == 1 && pkey == null) {
            message += "pKey parameter has to be set for incremental import! ";
        }

        if (message.length() > l) {
            throw new ValidationException(message);
        }
        return true;
    }

    private boolean isValidType(String type) {
        for (reportTypes c : reportTypes.values()) {
            if (c.name().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidAggregation(String type) {
        try {
            ReportAggregation a = ReportAggregation.valueOf(type);
            return a != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isValidPeriod(String type) {
        try {
            ReportTimePeriod a = ReportTimePeriod.valueOf(type);
            return a != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    private void setDate_from(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.date_from = format.parse(dateString);

    }

    public Date getDate_to() {
        return date_from;
    }

    public Integer getIncremental() {
        return incremental;
    }

    public String[] getPkey() {
        return pkey;
    }

    public reportTypes getType() {
        for (reportTypes c : reportTypes.values()) {
            if (c.name().equals(this.type)) {
                return c;
            }
        }
        return null;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getStartDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date_from);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getStartMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date_from);
        return cal.get(Calendar.MONTH);
    }

    public int getStartYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date_from);
        return cal.get(Calendar.YEAR);
    }

    public Date getDate_from() {
        return date_from;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getAggregationPeriod() {
        return aggregationPeriod;
    }

}
