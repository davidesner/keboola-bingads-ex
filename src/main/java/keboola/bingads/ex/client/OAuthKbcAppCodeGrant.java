/*
 */
package keboola.bingads.ex.client;

import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.internal.LiveComOAuthService;
import com.microsoft.bingads.internal.OAuthWithAuthorizationCode;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class OAuthKbcAppCodeGrant extends OAuthWithAuthorizationCode {

    public OAuthKbcAppCodeGrant(String clientId, String clientSecret, OAuthTokens tokens) {
        super(clientId, clientSecret, LiveComOAuthService.DESKTOP_REDIRECT_URL, tokens.getRefreshToken());
        this.oAuthTokens = tokens;
    }
}
