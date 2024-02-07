package com.udemy.jaime.vertx_websockets;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler implements Handler<ServerWebSocket> {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandler.class);
  public static final String PATH = "/ws/simple/prices";
  private final PriceBroadcast broadcast;

  public WebSocketHandler(final Vertx vertx) {
    this.broadcast = new PriceBroadcast(vertx);
  }

  @Override
  public void handle(ServerWebSocket ws) {
    if(!PATH.equalsIgnoreCase(ws.path())){
      LOG.info("Rejected wrong path: {}", ws.path());
      ws.writeFinalTextFrame("Wrong path. Only "+ PATH + " is accepted!");
      closeClient(ws);
      return;
    }
    ws.accept();
    LOG.info("Opening websocket connection: {}, {}",ws.path() ,ws.textHandlerID());
    ws.frameHandler(getFrameHandler(ws));
    ws.endHandler(onClosed -> {
      LOG.info("connection terminated: {}", ws.textHandlerID());
      broadcast.unregister(ws);
    });
    ws.exceptionHandler(err -> LOG.error("Failed: ",err));
    ws.writeTextMessage("Connected!");
    broadcast.register(ws);
  }

  private static Handler<WebSocketFrame> getFrameHandler(ServerWebSocket ws) {
    return received -> {
      final String message = received.textData();
      LOG.debug("Received message: {} from client {}", message, ws.textHandlerID());
      if ("disconnect me".equalsIgnoreCase((message))) {
        LOG.info("Client close requested!");
        closeClient(ws);
      } else {
        ws.writeTextMessage("Not supported -> (" + message + ")");
      }
    };
  }

  private static void closeClient(ServerWebSocket ws) {
    ws.close((short) 1000 , "Normal Closure");
  }
}
