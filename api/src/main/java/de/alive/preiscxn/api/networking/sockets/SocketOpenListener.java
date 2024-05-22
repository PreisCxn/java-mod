package de.alive.preiscxn.api.networking.sockets;

import jakarta.websocket.Session;

public interface SocketOpenListener {
    void onOpen(Session session);
}
