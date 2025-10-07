package com.zeroinbox.api.auth;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStore {
  public static class Tokens {
    public String accessToken;
    public String refreshToken;
    public Instant expiresAt;
  }

  private final ConcurrentHashMap<String, Tokens> map = new ConcurrentHashMap<>();
  public void put(String userId, Tokens t) { map.put(userId, t); }
  public Tokens get(String userId) { return map.get(userId); }
}