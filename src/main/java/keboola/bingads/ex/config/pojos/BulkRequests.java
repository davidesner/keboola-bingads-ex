/*
 */
package keboola.bingads.ex.config.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import keboola.bingads.ex.config.ValidationException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class BulkRequests {

    private Map<String, Boolean> bulkFiles;

    @JsonProperty("ads")
    private Boolean ads;
    @JsonProperty("campaigns")
    private Boolean campaigns;
    @JsonProperty("campQualityScore")
    private Boolean campQualityScore;
    @JsonProperty("keywords")
    private Boolean keywords;
    @JsonProperty("siteLinkAddExtensions")
    private Boolean siteLinkAddExtensions;

    @JsonCreator
    public BulkRequests(@JsonProperty("ads") Boolean ads, @JsonProperty("campaigns") Boolean campaigns, @JsonProperty("campQualityScore") Boolean campQualityScore,
            @JsonProperty("keywords") Boolean keywords, @JsonProperty("siteLinkAddExtensions") Boolean siteLinkAddExtensions) {
        this.bulkFiles = new HashMap();

        this.ads = ads;

        this.campaigns = campaigns;
        if (campQualityScore == null) {
            this.campQualityScore = true;
        } else {
            this.campQualityScore = campQualityScore;
        }
        this.keywords = keywords;
        this.siteLinkAddExtensions = siteLinkAddExtensions;

        this.bulkFiles.put("CAMPAIGNS", campaigns);
        this.bulkFiles.put("ADS", ads);
        this.bulkFiles.put("KEYWORDS", keywords);
        this.bulkFiles.put("SITE_LINKS_AD_EXTENSIONS", keywords);

    }

    public Map<String, Boolean> getBulkFiles() {
        return bulkFiles;
    }

    public boolean validate() throws ValidationException {
        String message = "Missing Bulk request parameter: ";
        int l = message.length();
        if (ads == null) {
            message += "ads parameter is missing! ";
        }
        if (campaigns == null) {
            message += " campaigns parameter is missing! ";
        }
        if (keywords == null) {
            message += " keywords parameter is missing! ";
        }
        if (siteLinkAddExtensions == null) {
            message += " siteLinkAddExtensions parameter is missing! ";
        }

        if (message.length() > l) {
            throw new ValidationException(message);
        }
        return true;
    }

    public Boolean getAds() {
        return ads;
    }

    public void setAds(Boolean ads) {
        this.ads = ads;
    }

    public Boolean getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(Boolean campaigns) {
        this.campaigns = campaigns;
    }

    public Boolean getCampQualityScore() {
        return campQualityScore;
    }

    public void setCampQualityScore(Boolean campQualityScore) {
        this.campQualityScore = campQualityScore;
    }

    public Boolean getKeywords() {
        return keywords;
    }

    public void setKeywords(Boolean keywords) {
        this.keywords = keywords;
    }

    public Boolean getSiteLinkAddExtensions() {
        return siteLinkAddExtensions;
    }

    public void setSiteLinkAddExtensions(Boolean siteLinkAddExtensions) {
        this.siteLinkAddExtensions = siteLinkAddExtensions;
    }

}
