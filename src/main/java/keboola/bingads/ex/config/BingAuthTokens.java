package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Esner
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BingAuthTokens {
	
	@JsonProperty("refresh_token")
	private String refresh_token;
	@JsonProperty("access_token")
	private String access_token;

	
	public BingAuthTokens() {
	}

	public BingAuthTokens(String refreshToken, String accessToken) {
		super();
		this.refresh_token = refresh_token;
		this.access_token = accessToken;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public String getAccess_token() {
		return access_token;
	}


}
