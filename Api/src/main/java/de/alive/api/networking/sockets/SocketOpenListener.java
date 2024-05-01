package de.alive.api.networking.sockets;

import jakarta.websocket.Session;

public interface SocketOpenListener {
    void onOpen(Session session);
}
