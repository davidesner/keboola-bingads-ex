/*
 */
package keboola.bingads.ex.client;

import java.util.Arrays;

import com.microsoft.bingads.v13.reporting.AccountThroughAdGroupReportScope;
import com.microsoft.bingads.v13.reporting.AdExtensionByAdReportColumn;
import com.microsoft.bingads.v13.reporting.AdExtensionByAdReportRequest;
import com.microsoft.bingads.v13.reporting.AdExtensionByKeywordReportColumn;
import com.microsoft.bingads.v13.reporting.AdExtensionByKeywordReportRequest;
import com.microsoft.bingads.v13.reporting.AdExtensionDetailReportColumn;
import com.microsoft.bingads.v13.reporting.AdExtensionDetailReportRequest;
import com.microsoft.bingads.v13.reporting.AdPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.AdPerformanceReportRequest;
import com.microsoft.bingads.v13.reporting.ArrayOfAdExtensionByAdReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOfAdExtensionByKeywordReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOfAdExtensionDetailReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOfAdPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.ArrayOfKeywordPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.KeywordPerformanceReportColumn;
import com.microsoft.bingads.v13.reporting.KeywordPerformanceReportRequest;
import com.microsoft.bingads.v13.reporting.ReportAggregation;
import com.microsoft.bingads.v13.reporting.ReportFormat;
import com.microsoft.bingads.v13.reporting.ReportRequest;
import com.microsoft.bingads.v13.reporting.ReportTime;

import keboola.bingads.ex.config.pojos.BReportRequest;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ReportRequestFactoryOld {

//FIXME REFACTOR!! code redundance, unclear, maybe use generics?
	
    public static ReportRequest createReportRequest(BReportRequest br, long accountId, ReportTime time) throws ClientException {
        ReportRequest request = null;

        //setscope
        try {
            AccountThroughAdGroupReportScope sc = new AccountThroughAdGroupReportScope();
            com.microsoft.bingads.v13.reporting.ArrayOflong aIds = new com.microsoft.bingads.v13.reporting.ArrayOflong();
            aIds.getLongs().add(accountId);
            sc.setAccountIds(aIds);

            //build adExtension
            if (br.getType() == BReportRequest.ReportType.AdExtensionByAd) {
                AdExtensionByAdReportRequest reportReq = new AdExtensionByAdReportRequest();
                reportReq.setAggregation(ReportAggregation.fromValue(br.getAggregationPeriod()));
                //validate pkey
                String colValidError = "";
                /*if (br.getPkey() != null && br.getPkey().length > 0) {
                    for (String col : br.getPkey()) {
                        try {
                            AdExtensionByAdReportColumn.fromValue(col);
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". Primary key specified:'" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }*/
                //set columns     
                ArrayOfAdExtensionByAdReportColumn columns = new ArrayOfAdExtensionByAdReportColumn();
                if (br.getColumns() == null) {
                    columns.getAdExtensionByAdReportColumns().addAll(Arrays.asList(AdExtensionByAdReportColumn.values()));
                } else {
                    for (String col : br.getColumns()) {
                        try {
                            columns.getAdExtensionByAdReportColumns().add(AdExtensionByAdReportColumn.fromValue(col));
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". '" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }

                if (!colValidError.equals("")) {
                    throw new ClientException(colValidError, null);
                }
                reportReq.setColumns(columns);
                reportReq.setTime(time);
                reportReq.setScope(sc);

                request = reportReq;
            }
            //build AdExtensionByKeyWord request
            if (br.getType() == BReportRequest.ReportType.AdExtensionByKeyWord) {
                AdExtensionByKeywordReportRequest reportReq = new AdExtensionByKeywordReportRequest();
                reportReq.setAggregation(ReportAggregation.fromValue(br.getAggregationPeriod()));
                //validate pkey
                String colValidError = "";
                /*if (br.getPkey() != null && br.getPkey().length > 0) {
                    for (String col : br.getPkey()) {
                        try {
                            AdExtensionByKeywordReportColumn.fromValue(col);
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". Primary key specified:'" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }*/
                //set columns     
                ArrayOfAdExtensionByKeywordReportColumn columns = new ArrayOfAdExtensionByKeywordReportColumn();
                if (br.getColumns() == null) {
                    columns.getAdExtensionByKeywordReportColumns().addAll(Arrays.asList(AdExtensionByKeywordReportColumn.values()));
                } else {
                    for (String col : br.getColumns()) {
                        try {
                            columns.getAdExtensionByKeywordReportColumns().add(AdExtensionByKeywordReportColumn.fromValue(col));
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". '" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }

                if (!colValidError.equals("")) {
                    throw new ClientException(colValidError, null);
                }
                reportReq.setColumns(columns);
                reportReq.setTime(time);
                reportReq.setScope(sc);

                request = reportReq;
            }
            //build AdExtensionDetail request
            if (br.getType() == BReportRequest.ReportType.AdExtensionDetail) {
                AdExtensionDetailReportRequest reportReq = new AdExtensionDetailReportRequest();
                reportReq.setAggregation(ReportAggregation.fromValue(br.getAggregationPeriod()));
                //validate pkey
                String colValidError = "";
                /*if (br.getPkey() != null && br.getPkey().length > 0) {
                    for (String col : br.getPkey()) {
                        try {
                            AdExtensionDetailReportColumn.fromValue(col);
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". Primary key specified:'" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }*/
                //set columns     
                ArrayOfAdExtensionDetailReportColumn columns = new ArrayOfAdExtensionDetailReportColumn();
                if (br.getColumns() == null) {
                    columns.getAdExtensionDetailReportColumns().addAll(Arrays.asList(AdExtensionDetailReportColumn.values()));

                } else {
                    for (String col : br.getColumns()) {
                        try {
                            columns.getAdExtensionDetailReportColumns().add(AdExtensionDetailReportColumn.fromValue(col));
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". '" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }

                if (!colValidError.equals("")) {
                    throw new ClientException(colValidError, null);
                }
                reportReq.setColumns(columns);
                reportReq.setTime(time);
                reportReq.setScope(sc);

                request = reportReq;
            }

            //build AdsPerformance request
            if (br.getType() == BReportRequest.ReportType.AdsPerformance) {
                AdPerformanceReportRequest reportReq = new AdPerformanceReportRequest();
                reportReq.setAggregation(ReportAggregation.fromValue(br.getAggregationPeriod()));
                //validate pkey
                String colValidError = "";
                /*if (br.getPkey() != null && br.getPkey().length > 0) {
                    for (String col : br.getPkey()) {
                        try {
                            AdPerformanceReportColumn.fromValue(col);
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". Primary key specified:'" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }*/
                //set columns     
                ArrayOfAdPerformanceReportColumn columns = new ArrayOfAdPerformanceReportColumn();
                if (br.getColumns() == null) {
                    columns.getAdPerformanceReportColumns().addAll(Arrays.asList(AdPerformanceReportColumn.values()));
                } else {
                    for (String col : br.getColumns()) {
                        try {
                            columns.getAdPerformanceReportColumns().add(AdPerformanceReportColumn.fromValue(col));
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". '" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }

                if (!colValidError.equals("")) {
                    throw new ClientException(colValidError, null);
                }
                reportReq.setColumns(columns);
                reportReq.setTime(time);
                reportReq.setScope(sc);

                request = reportReq;
            }

            //build KeywordPerformance request
            if (br.getType() == BReportRequest.ReportType.KeywordPerformance) {
                KeywordPerformanceReportRequest reportReq = new KeywordPerformanceReportRequest();
                reportReq.setAggregation(ReportAggregation.fromValue(br.getAggregationPeriod()));
                //validate pkey
                String colValidError = "";
                /*if (br.getPkey() != null && br.getPkey().length > 0) {
                    for (String col : br.getPkey()) {
                        try {
                            KeywordPerformanceReportColumn.fromValue(col);
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". Primary key specified:'" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }
                }*/
                //set columns     
                ArrayOfKeywordPerformanceReportColumn columns = new ArrayOfKeywordPerformanceReportColumn();
                if (br.getColumns() == null) {
                    columns.getKeywordPerformanceReportColumns().addAll(Arrays.asList(KeywordPerformanceReportColumn.values()));
                    //columns.getKeywordPerformanceReportColumns().remove(KeywordPerformanceReportColumn.KEYWORD_MATCH_TYPE_ID);
                } else {

                    for (String col : br.getColumns()) {
                        try {
                            columns.getKeywordPerformanceReportColumns().add(KeywordPerformanceReportColumn.fromValue(col));
                        } catch (IllegalArgumentException ex) {//validate columns
                            colValidError += "Unable to proccess request " + br.getType().name() + ". '" + col + "' is not a valid column name, check request specs.\n";
                        }
                    }

                    if (!colValidError.equals("")) {
                        throw new ClientException(colValidError, null);
                    }
                }
                reportReq.setColumns(columns);
                reportReq.setTime(time);
                reportReq.setScope(sc);

                request = reportReq;
            }
        } catch (Exception ex) {
            throw new ClientException("Unable to proccess request " + br.getType().name() + " " + ex.getMessage(), ex);
        }

        if (request == null) {
            throw new ClientException(br.getType().name() + " report type is not supported", null);
        }

        request.setReturnOnlyCompleteData(br.isCompleteData());
        request.setFormat(ReportFormat.CSV);
        return request;
    }

}
