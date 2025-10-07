package com.zeroinbox.api.web;

import com.zeroinbox.api.auth.GoogleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/google")
public class AuthController {
  private final GoogleAuthService auth;
  public AuthController(GoogleAuthService auth) { this.auth = auth; }

  @PostMapping("/exchange")
  public ResponseEntity<?> exchange(@RequestBody Map<String, String> body) throws Exception {
    String code         = body.get("serverAuthCode");
    String codeVerifier = body.get("codeVerifier");        // present in native PKCE flow
    String clientId     = body.get("clientId");            // ANDROID client id from app.json
    String redirectUri  = body.get("redirectUri");         // e.g. zeroinbox://...

    if (code == null || code.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "missing serverAuthCode"));
    }

    // If PKCE fields are present, use the native flow
    if (codeVerifier != null && !codeVerifier.isBlank()
        && clientId != null && !clientId.isBlank()
        && redirectUri != null && !redirectUri.isBlank()) {
      auth.exchangeAndSavePkce("me", code, codeVerifier, clientId, redirectUri);
      return ResponseEntity.ok(Map.of("ok", true, "flow", "pkce"));
    }

    // Otherwise, fall back to web flow (kept for compatibility)
    auth.exchangeAndSave("me", code);
    return ResponseEntity.ok(Map.of("ok", true, "flow", "web"));
  }
}
