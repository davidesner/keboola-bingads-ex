package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)

public class OAuthCredentials {

    private final String[] REQUIRED_FIELDS = {"data", "appKey", "appSecret", "refresh_token"};
    private final Map<String, Object> parametersMap;

    private final Map<String, String> oAuthAPIResponseMap;

    @JsonProperty("#data")
    private String data;
    @JsonProperty("appKey")
    private String appKey;
    @JsonProperty("#appSecret")
    private String appSecret;

    public OAuthCredentials() {
        parametersMap = new HashMap<>();
        oAuthAPIResponseMap = new HashMap<>();

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    @JsonCreator
    public OAuthCredentials(@JsonProperty("#data") String data, @JsonProperty("appKey") String appKey,
            @JsonProperty("#appSecret") String appSecret) throws ParseException {
        parametersMap = new HashMap<>();

        this.data = data;
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.oAuthAPIResponseMap = convertOauthApiResponse(data);
        //set param map
        parametersMap.put("refresh_token", oAuthAPIResponseMap.get("refresh_token"));

        parametersMap.put("data", data);
        parametersMap.put("appKey", appKey);
        parametersMap.put("appSecret", appSecret);

    }

    public String[] getREQUIRED_FIELDS() {
        return REQUIRED_FIELDS;
    }

    public Map<String, Object> getParametersMap() {
        return parametersMap;
    }

    private Map<String, String> convertOauthApiResponse(String data) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> params = null;
        try {
            params = mapper.readValue(data, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException ex) {
            Logger.getLogger(KBCAuthorization.class.getName()).log(Level.SEVERE, null, ex);

        }
        return params;
    }

    public String getRefreshToken() {
        return (String) parametersMap.get("refresh_token");
    }
}
