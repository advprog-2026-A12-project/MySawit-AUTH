package id.ac.ui.cs.advprog.auth.service.oauth;

import id.ac.ui.cs.advprog.auth.service.utils.GoogleUserInfo;

public interface OAuthClient {
    GoogleUserInfo authenticate(String authorizationCode, String redirectUri);
}
