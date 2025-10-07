package com.zeroinbox.api.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GoogleAuthService {
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;

  private final NetHttpTransport transport = new NetHttpTransport();
  private final GsonFactory json = GsonFactory.getDefaultInstance();
  private final TokenStore store = new TokenStore();

  public GoogleAuthService(
    @Value("${google.oauth.client-id}") String clientId,
    @Value("${google.oauth.client-secret}") String clientSecret,
    @Value("${google.oauth.redirect-uri}") String redirectUri) {  // <-- no default
  this.clientId = clientId;
  this.clientSecret = clientSecret;
  this.redirectUri = redirectUri;
}

  /** Exchange serverAuthCode from the app for access+refresh tokens and save them */
  public void exchangeAndSave(String userId, String serverAuthCode) throws Exception {
    GoogleTokenResponse resp = new GoogleAuthorizationCodeTokenRequest(
        transport, json,
        "https://oauth2.googleapis.com/token",
        clientId, clientSecret,
        serverAuthCode, redirectUri   // <-- keep "postmessage"
    ).execute();

    TokenStore.Tokens t = new TokenStore.Tokens();
    t.accessToken = resp.getAccessToken();
    t.refreshToken = resp.getRefreshToken(); // may be null if consent not granted
    Long exp = resp.getExpiresInSeconds();
    t.expiresAt = (exp != null) ? Instant.now().plusSeconds(exp) : Instant.now().plusSeconds(3000);
    store.put(userId, t);
  }

  /** Returns a valid access token, refreshing with the stored refresh token if needed */
  public String ensureAccessToken(String userId) throws Exception {
    TokenStore.Tokens t = store.get(userId);
    if (t == null) throw new IllegalStateException("No tokens stored for user " + userId);

    if (t.expiresAt != null && t.expiresAt.isAfter(Instant.now().plusSeconds(60))) {
      return t.accessToken;
    }
    if (t.refreshToken == null) throw new IllegalStateException("No refresh token available");

    GoogleTokenResponse refresh = new GoogleRefreshTokenRequest(
        transport, json, t.refreshToken, clientId, clientSecret
    ).execute();

    t.accessToken = refresh.getAccessToken();
    Long exp = refresh.getExpiresInSeconds();
    t.expiresAt = (exp != null) ? Instant.now().plusSeconds(exp) : Instant.now().plusSeconds(3000);
    return t.accessToken;
  }
}
