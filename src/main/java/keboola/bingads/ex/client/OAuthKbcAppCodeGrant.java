/*
 */
package keboola.bingads.ex.client;

import java.net.MalformedURLException;
import java.net.URL;

import com.microsoft.bingads.ApiEnvironment;
import com.microsoft.bingads.InternalException;
import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.internal.OAuthWithAuthorizationCode;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class OAuthKbcAppCodeGrant extends OAuthWithAuthorizationCode {
	static {
		try {
			DESKTOP_REDIRECT_URL = new URL("https://login.live.com/oauth20_desktop.srf");
		} catch (MalformedURLException e) {
			throw new InternalException(e);
		}
	}
	public static final URL DESKTOP_REDIRECT_URL;

	public OAuthKbcAppCodeGrant(String clientId, String clientSecret, OAuthTokens tokens, ApiEnvironment env) {
		super(clientId, clientSecret, DESKTOP_REDIRECT_URL, tokens.getRefreshToken(), env);
		this.oAuthTokens = tokens;
	}
}
