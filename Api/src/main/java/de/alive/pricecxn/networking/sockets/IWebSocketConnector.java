package de.alive.pricecxn.networking.sockets;

public interface IWebSocketConnector {

    String DEFAULT_WEBSOCKET_URI = "wss://socket.preiscxn.de";

    boolean isConnected();

    void addMessageListener(SocketMessageListener listener);

    void addCloseListener(SocketCloseListener listener);

    void addOpenListener(SocketOpenListener listener);

    void removeMessageListener(SocketMessageListener listener);

    void removeCloseListener(SocketCloseListener listener);

    void removeOpenListener(SocketOpenListener listener);

}
