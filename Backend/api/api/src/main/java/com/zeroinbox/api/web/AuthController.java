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

  // For now, single demo userId = "me"
  @PostMapping("/exchange")
  public ResponseEntity<?> exchange(@RequestBody Map<String, String> body) throws Exception {
    String code = body.get("serverAuthCode");
    if (code == null || code.isBlank()) return ResponseEntity.badRequest().body(Map.of("error","missing serverAuthCode"));
    auth.exchangeAndSave("me", code);
    return ResponseEntity.ok(Map.of("ok", true));
  }
}
