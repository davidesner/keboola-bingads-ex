/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
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
public class KBCConfig {

    String validationError;
    @JsonProperty("storage")
    private KBCStorage storage;
    @JsonProperty("parameters")
    private KBCParameters params;
    @JsonProperty("authorization")
    private KBCAuthorization auth;

    public KBCConfig() {
        validationError = null;
    }

    public KBCConfig(KBCStorage storage, KBCParameters params, KBCAuthorization auth) {
        this.storage = storage;
        this.params = params;
        this.auth = auth;
    }

    public boolean validate() {
        List<String> misPar = params.getMissingFields();
        misPar.addAll(auth.getMissingFields());
        if (misPar.isEmpty()) {
            try {
                if (!params.getBulkRequests().validate() | !params.validateReportRequests()) {
                    return false;
                }
            } catch (ValidationException ex) {
                this.validationError = ex.getMessage();
                return false;
            }

            return true;
        } else {
            setValidationError(misPar);
            return false;
        }

    }

    public String getValidationError() {
        return validationError;
    }

    private void setValidationError(List<String> missingFields) {
        this.validationError = "Required config fields are missing: ";
        int i = 0;
        for (String fld : missingFields) {
            if (i < missingFields.size()) {
                this.validationError += fld + ", ";
            } else {
                this.validationError += fld;
            }
        }
    }

    public KBCStorage getStorage() {
        return storage;
    }

    public void setStorage(KBCStorage storage) {
        this.storage = storage;
    }

    public KBCParameters getParams() {
        return params;
    }

    public void setParams(KBCParameters params) {
        this.params = params;
    }

    public KBCAuthorization getAuth() {
        return auth;
    }

    public OAuthCredentials getOAuthCredentials() {
        return this.auth.getoAuthApi().credentials;
    }

    public void setAuth(KBCAuthorization auth) {
        this.auth = auth;
    }

    public Map<Integer, KBCOutputMapping> getOutputTables() {
        return this.storage.getOutputTables().getTables();
    }
}
