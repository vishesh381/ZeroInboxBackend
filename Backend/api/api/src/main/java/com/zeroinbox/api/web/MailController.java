package com.zeroinbox.api.web;


import com.google.api.services.gmail.model.Message;
import com.zeroinbox.api.auth.GoogleAuthService;
import com.zeroinbox.api.gmail.GmailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mail")
public class MailController {
  private final GoogleAuthService auth;
  private final GmailService gmail;

  public MailController(GoogleAuthService auth, GmailService gmail) {
    this.auth = auth; this.gmail = gmail;
  }

  @GetMapping("/unread")
  public List<Message> unread(@RequestParam(defaultValue = "10") long max) throws Exception {
    String token = auth.ensureAccessToken("me");
    return gmail.listUnread(token, max);
  }

  @GetMapping("/{id}")
  public Message get(@PathVariable String id) throws Exception {
    String token = auth.ensureAccessToken("me");
    return gmail.get(token, id);
  }

  @PostMapping("/{id}/mark-read")
  public Map<String, Object> markRead(@PathVariable String id) throws Exception {
    String token = auth.ensureAccessToken("me");
    gmail.markRead(token, id);
    return Map.of("ok", true);
  }
}

