package de.alive.pricecxn.networking.sockets;

import jakarta.websocket.Session;

public interface SocketOpenListener {
    void onOpen(Session session);
}
