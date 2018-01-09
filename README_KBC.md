# BingAds extractor
Keboola Connection docker app for extracting data from BingAds API.
## Funcionality
The component retrieves data from two BingAds API services:

- Bulk Service 
- Reporting Service 

The Bulk Service provides complete Entity Data for BingAds object.
The component currently allows to retrieve these data sets:

- **ads** 
- **adgroups** 
- **campaigns** 
- **site_links_ad_extensions** 
- **keywords** 

The Reporting Service provides performance data (facts) on
specified level of aggregation. Currently supported datasets and their column lists :

- **AdsPerformance**	*[[AdPerformanceReportColumns](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-adperformancereportcolumn(v=msads.90).aspx)]*
- **KeywordPerformance**	 *[[KeywordPerformanceReportColumns](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-keywordperformancereportcolumn(v=msads.90).aspx)]*
- **AdExtensionDetail**	*[[AdExtensionDetailReportColumns](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-adextensiondetailreportcolumn(v=msads.90).aspx)]*
- **AdExtensionByKeyWord**	*[[AdExtensionByKeywordReportColumns](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-adextensionbykeywordreportcolumn(v=msads.90).aspx)]* 
- **AdExtensionByAd**	*[[AdExtensionByAdReportColumns](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-adextensionbyadreportcolumn(v=msads.90).aspx)]*

For each report user can specify aggregation level, report period and column set to be retrieved. By default the aggregation level is *DAILY* and all columns are retrieved.

## Configuration

### Authentication

Firstly, you need to apply for a Developer Token to be able to access the API. 

See [bing-ads-getting-started](https://msdn.microsoft.com/en-us/library/bing-ads-getting-started.aspx)

Then you can authorize with your Microsoft account to retrieve Refresh tokens. This can be done simply by clicking  *Authorize Account* button in component's configuration.
![](https://raw.githubusercontent.com/davidesner/keboola-bingads-ex/master/img/screen.png)

### Parameters

- **#devKey** – *(REQ)* your developer token 
- **customerId** – 	*(REQ)* customer identifier of your BingAds account. 
	 
- **accountId** – 	*(REQ)* account identifier. To get customer and account ids see
	[Getting-Your-Account-ID-and-Customer-ID](https://msdn.microsoft.com/en-us/library/bing-ads-getting-started.aspx#Getting-Your-Account-ID-and-Customer-ID) 
- **bucket** – *(REQ)* name of bucket where result is stored 
- **sinceLast** – *(OPT)* continue download from last state (retrieves data since last download was perfomed) *DEFAULT: TRUE* 
- **bulkRequests** 
    -         **ads** –	*(REQ)* include dataset in query (true or false) 
    -         **campaigns** – *(REQ)* include dataset in query (true or false)
    -          **keywords**– *(REQ)* include dataset in query (true or false) 
    -         **campQualityScore**	– *(REQ)* include dataset in query (true or false) 
    -         **siteLinkAddExtensions** – *(REQ)* include dataset in query (true or false) 
    -         **adGroups**	– *(REQ)* include dataset in query (true or false) 
- **reportRequests** – list of report queries 
    - **Report query**	– *(REQ)* type of report request. Supported values: [`AdsPerformance`, `KeywordPerformance`, `AdExtensionDetail`, `AdExtensionByKeyWord`, `AdExtensionByAd`]
    - **Return complete data only** - Determines whether you want the service to generate the report only if all the data has been processed and is available. If true, the request fails if the system has not finished processing all the data based on the aggregation, scope, and time period values that you specify. You must set this value to 'No' if you are using report period prefixed by 'This*'
    - **startDate**	– *(REQ)* date from which to retrieve data in format: `dd-mm-yyyy.`	If `sinceLast` is set, his boundary is considered only the first time extractor runs. 
    - **reportPeriod**– *(OPT)* time period of report data (case sensitive). For list of	supported values see	[Values](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-reporttimeperiod.aspx#Values)  ***NOTE:*** If this parameter is set startDate parameter is ignored. If you are reporting on multiple accounts the timezone considered may differ from what you expect see [Values](https://docs.microsoft.com/en-us/bingads/guides/reports#aggregation-time#Values)
    - **aggregationPeriod**	– *(OPT)* aggregation level of result data (case sensitive).  For	list of supported values see [Values](https://msdn.microsoft.com/en-us/library/bing-ads-reporting-reportaggregation(v=msads.90).aspx#Values)	. *DEFAULT:* `Daily` (lowest supported) 
    - **incremental**	– *(OPT)* upload incrementally. *DEFAULT*: FALSE 
    - **columns**	– *(OPT)* list of supported columns according to report type. 
		 

### Sample configurations / use cases

#### Use case 1

Downloads all supported bulk datasets along with all supported reports since date `21-03-2016`. The import is by default not incremental and since the parameter `sinceLast` is set to *false*, each subsequent run will retrieve all data since `startDate`.

    {
        "#devKey": "yourdevkey",
        "customerId": 123456,
        "accountId": 123456,    
        "bucket": "in.c-test",
        "sinceLast" : false
        "bulkRequests": {
          "ads": true,
          "campaigns": true,
          "keywords": true,
          "campQualityScore": true,
          "siteLinkAddExtensions": true,
          "adGroups": true
        },
        "reportRequests": [
          {
            "type": "AdsPerformance",
            "startDate": "21-03-2016"
          },
          {
            "type": "KeywordPerformance",
            "startDate": "21-03-2016"
          },
          {
            "type": "AdExtensionDetail",
            "startDate": "21-03-2016"
          },
          {
            "type": "AdExtensionByKeyWord",
            "startDate": "21-03-2016"
          },
          {
            "type": "AdExtensionByAd",
            "startDate": "21-03-2016"
          }
        ]
      }

#### Use case 2

Downloads all supported bulk datasets along with all supported report data within this month. The sinceLast parameter is ignored.

    {
        "#devKey": "yourdevkey",
        "customerId": 123456,
        "accountId": 123456,    
        "bucket": "in.c-test",
        "sinceLast" : false
        "bulkRequests": {
          "ads": true,
          "campaigns": true,
          "keywords": true,
          "campQualityScore": true,
          "siteLinkAddExtensions": true,
          "adGroups": true
        },
        "reportRequests": [
          {
            "type": "AdsPerformance",
            "reportPeriod": "ThisMonth"
          },
          {
            "type": "KeywordPerformance",
            "reportPeriod": "ThisMonth"
          },
          {
            "type": "AdExtensionDetail",
            "reportPeriod": "ThisMonth"
          },
          {
            "type": "AdExtensionByKeyWord",
            "reportPeriod": "ThisMonth"
          },
          {
            "type": "AdExtensionByAd",
            "reportPeriod": "ThisMonth"
          }
        ]
      }

#### Use case 3

Downloads all supported bulk datasets along with `AdsPerformance`, `KeywordPerformance` and `AdExtensionDetail` report data `sinceStart` date. The result will be incrementally uploaded to storage using specified primary keys. Each subsequent run will retrieve data from the date of last run.

    {
        "#devKey": "yourdevkey",
        "customerId": 123456,
        "accountId": 123456,    
        "bucket": "in.c-test",
        "incremental" : true
        "bulkRequests": {
          "ads": true,
          "campaigns": true,
          "keywords": true,
          "campQualityScore": true,
          "siteLinkAddExtensions": true,
          "adGroups": true
        },
        "reportRequests": [
          {
            "type": "AdsPerformance",
            "startDate": "21-03-2016",
          "pkey": [
            "ID1",”ID2”,”ID3”
          ]
          },
          {
            "type": "KeywordPerformance",
            "startDate": "21-03-2016",
          "pkey": [
            "ID1",”ID2”,”ID3”
          ]
          },
          {
            "type": "AdExtensionDetail",
            "startDate": "21-03-2016",
          "pkey": [
            "ID1",”ID2”,”ID3”
          ]
          }
        ]
      }


