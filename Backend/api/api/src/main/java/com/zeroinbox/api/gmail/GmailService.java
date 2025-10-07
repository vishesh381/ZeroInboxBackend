package com.zeroinbox.api.gmail;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GmailService {
  private final NetHttpTransport transport = new NetHttpTransport();
  private final GsonFactory json = GsonFactory.getDefaultInstance();

  private Gmail build(String accessToken) {
    HttpRequestInitializer init = req -> req.getHeaders().setAuthorization("Bearer " + accessToken);
    return new Gmail.Builder(transport, json, init).setApplicationName("ZeroInbox").build();
  }

  public List<Message> listUnread(String token, long max) throws IOException {
    return build(token).users().messages().list("me")
        .setQ("is:unread").setMaxResults(max).execute().getMessages();
  }

  public Message get(String token, String id) throws IOException {
    return build(token).users().messages().get("me", id).setFormat("FULL").execute();
  }

  public void markRead(String token, String id) throws IOException {
    ModifyMessageRequest req = new ModifyMessageRequest().setRemoveLabelIds(List.of("UNREAD"));
    build(token).users().messages().modify("me", id, req).execute();
  }
}
