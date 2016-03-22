/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KBCAuthorization {

    @JsonProperty("oauth_api")
    private OAuthApiParamsWrapper oAuthApi;

    public OAuthApiParamsWrapper getoAuthApi() {
        return oAuthApi;
    }

    public void setoAuthApi(OAuthApiParamsWrapper oAuthApi) {
        this.oAuthApi = oAuthApi;
    }

    public KBCAuthorization(OAuthApiParamsWrapper oAuthApi) {
        this.oAuthApi = oAuthApi;
    }

    public KBCAuthorization() {
    }

    /**
     * Returns list of required fields missing in config
     *
     * @return
     */
    public List<String> getMissingFields() {
        String[] reqFields = oAuthApi.credentials.getREQUIRED_FIELDS();
        Map<String, Object> parametersMap = oAuthApi.credentials.getParametersMap();
        List<String> missing = new ArrayList<>();
        for (String reqField : reqFields) {
            Object value = parametersMap.get(reqField);
            if (value == null) {
                missing.add(reqField);
            }
        }

        return missing;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class OAuthApiParamsWrapper {

        @JsonProperty("credentials")
        public OAuthCredentials credentials;

        public OAuthApiParamsWrapper() {
        }

        public OAuthApiParamsWrapper(OAuthCredentials credentials) {
            this.credentials = credentials;
        }
    }

}
