package com.mcserver.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TcpServer implements SmartLifecycle {
    private final TcpRequestHandler requestHandler;
    private final ObjectMapper objectMapper;

    @Value("${server.host:0.0.0.0}")
    private String host;

    @Value("${server.port:5000}")
    private int port;

    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService clientExecutor;
    private Thread acceptThread;

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }

        try {
            ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(host, port));
            serverSocket = socket;
            clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
            running = true;
            acceptThread = Thread.ofPlatform()
                    .name("tcp-server-accept")
                    .start(this::acceptConnections);
            log.info("TCP server listening on {}:{}", host, socket.getLocalPort());
        } catch (IOException e) {
            throw new IllegalStateException("Could not start TCP server on " + host + ":" + port, e);
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                clientExecutor.submit(() -> handleClient(client));
            } catch (SocketException e) {
                if (running) {
                    log.error("TCP server socket failed", e);
                }
                return;
            } catch (IOException e) {
                if (running) {
                    log.error("Could not accept TCP client", e);
                }
            }
        }
    }

    private void handleClient(Socket client) {
        try (client;
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter output = new BufferedWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = input.readLine()) != null) {
                TcpResponse response = requestHandler.handle(line);
                output.write(objectMapper.writeValueAsString(response));
                output.newLine();
                output.flush();
            }
        } catch (IOException e) {
            if (running) {
                log.warn("TCP client connection ended with an I/O error: {}", e.getMessage());
            }
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.warn("Could not close TCP server socket", e);
            }
        }
        if (clientExecutor != null) {
            clientExecutor.shutdownNow();
        }
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
