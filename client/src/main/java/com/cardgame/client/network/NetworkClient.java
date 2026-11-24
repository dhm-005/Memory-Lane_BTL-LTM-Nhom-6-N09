
package com.cardgame.client.network;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.cardgame.client.util.Json;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;

public final class NetworkClient {
    private final String host;
    private final int port;
    private final Consumer<ObjectNode> messageHandler;
    private final Consumer<String> errorHandler;

    private volatile Socket socket;
    private BufferedWriter writer;
    private volatile boolean connected;
    private volatile boolean connecting;
    private boolean errorReported;

    public NetworkClient(String host, int port,
                         Consumer<ObjectNode> messageHandler,
                         Consumer<String> errorHandler) {
        this.host = host;
        this.port = port;
        this.messageHandler = messageHandler;
        this.errorHandler = errorHandler;
    }

    public synchronized void connect() {
        if (connected || connecting) {
            return;
        }

        connecting = true;

        Thread connector = new Thread(() -> {
            Socket newSocket = new Socket();

            try {
                newSocket.connect(new InetSocketAddress(host, port), 5000);
                newSocket.setKeepAlive(true);

                BufferedWriter newWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                newSocket.getOutputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                newSocket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                synchronized (this) {
                    socket = newSocket;
                    writer = newWriter;
                    connected = true;
                    connecting = false;
                    errorReported = false;
                }

                Thread readerThread = new Thread(
                        () -> readMessages(reader, newSocket),
                        "server-reader"
                );
                readerThread.setDaemon(true);
                readerThread.start();

            } catch (IOException e) {
                try {
                    newSocket.close();
                } catch (IOException ignored) {
                }

                synchronized (this) {
                    connecting = false;
                    connected = false;
                }

                reportError(
                        "Không kết nối được Server tại "
                                + host + ":" + port
                                + ". Hãy kiểm tra IP, cổng và trạng thái Server."
                );
            }
        }, "server-connector");

        connector.setDaemon(true);
        connector.start();
    }

    private void readMessages(BufferedReader reader, Socket readerSocket) {
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                ObjectNode message = (ObjectNode) Json.parse(line);

                if ("SESSION_REPLACED".equals(Json.str(message, "type"))) {
                    connected = false;
                    messageHandler.accept(message);
                    return;
                }

                messageHandler.accept(message);
            }

            if (connected && socket == readerSocket) {
                reportError("Kết nối tới Server đã đóng.");
            }

        } catch (Exception e) {
            if (connected && socket == readerSocket) {
                reportError(
                        "Mất kết nối tới Server. Hãy kiểm tra Server đang chạy."
                );
            }

        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }

            synchronized (this) {
                if (socket == readerSocket) {
                    connected = false;
                    socket = null;
                    writer = null;
                }
            }

            try {
                readerSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void reportError(String message) {
        synchronized (this) {
            if (errorReported) {
                return;
            }
            errorReported = true;
        }

        errorHandler.accept(message);
    }

    public synchronized void send(String type, Object... pairs)
            throws IOException {
        if (!isConnected() || writer == null) {
            throw new IOException("Chưa kết nối Server.");
        }

        ObjectNode message = Json.object(type);

        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Object value = pairs[i + 1];

            if (value instanceof Number number) {
                message.set(key, Json.MAPPER.valueToTree(number));
            } else if (value instanceof Boolean bool) {
                message.put(key, bool);
            } else if (value instanceof JsonNode element) {
                message.set(key, element);
            } else if (value instanceof Collection<?> values) {
                ArrayNode array = Json.MAPPER.createArrayNode();

                for (Object item : values) {
                    array.add(String.valueOf(item));
                }

                message.set(key, array);
            } else if (value == null) {
                message.putNull(key);
            } else {
                message.put(key, String.valueOf(value));
            }
        }

        Json.send(writer, message);
    }

    public boolean isConnected() {
        Socket currentSocket = socket;

        return connected
                && currentSocket != null
                && currentSocket.isConnected()
                && !currentSocket.isClosed();
    }

    public synchronized void close() {
        connected = false;
        connecting = false;
        writer = null;

        Socket currentSocket = socket;
        socket = null;

        try {
            if (currentSocket != null) {
                currentSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
