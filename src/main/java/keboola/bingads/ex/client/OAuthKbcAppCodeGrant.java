/*
 */
package keboola.bingads.ex.client;

import com.microsoft.bingads.OAuthTokens;
import com.microsoft.bingads.internal.LiveComOAuthService;
import com.microsoft.bingads.internal.OAuthWithAuthorizationCode;
import java.net.URL;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class OAuthKbcAppCodeGrant extends OAuthWithAuthorizationCode {

    public OAuthKbcAppCodeGrant(String clientId, String clientSecret, String accessToken, String refreshToken, Long expiresIn) {
        super(clientId, clientSecret, LiveComOAuthService.DESKTOP_REDIRECT_URL, refreshToken);
        this.oAuthTokens = new OAuthTokens(accessToken, expiresIn, refreshToken);
    }
}
