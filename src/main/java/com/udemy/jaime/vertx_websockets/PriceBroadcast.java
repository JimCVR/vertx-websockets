package com.udemy.jaime.vertx_websockets;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PriceBroadcast {
  private static final Logger LOG = LoggerFactory.getLogger(PriceBroadcast.class);
  private final Map<String, ServerWebSocket> connectedClients = new HashMap<>();

  public PriceBroadcast(final Vertx vertx){
    periodicUpdate(vertx);
  }

  private void periodicUpdate(final Vertx vertx) {
    vertx.setPeriodic(Duration.ofSeconds(1).toMillis(), id -> {
      LOG.debug("Push update to {} clients", connectedClients.size());
      var price = new JsonObject()
        .put("symbol", "AMZN")
        .put("price", new Random().nextInt(1000))
        .toString();
      connectedClients.values().forEach(ws ->
        ws.writeTextMessage(price));
    });
  }

  public void register(ServerWebSocket ws) {
    connectedClients.put(ws.textHandlerID(), ws);
  }

  public void unregister(ServerWebSocket ws) {
    connectedClients.remove(ws.textHandlerID());
  }
}
