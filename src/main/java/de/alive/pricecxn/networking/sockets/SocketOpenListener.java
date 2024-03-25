package de.alive.pricecxn.networking.sockets;

import javax.websocket.Session;

public interface SocketOpenListener {
    void onOpen(Session session);
}
