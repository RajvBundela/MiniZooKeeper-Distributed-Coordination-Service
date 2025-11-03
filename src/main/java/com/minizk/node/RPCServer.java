package com.minizk.node;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class RPCServer {
    private final int port;
    private final String nodeId;
    private final ElectionModule electionModule;
    private Server server;

    public RPCServer(String nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
        this.electionModule = new ElectionModule(nodeId);
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(electionModule)
                .build()
                .start();

        System.out.println("✅ Node " + nodeId + " started gRPC server on port " + port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down node " + nodeId);
            RPCServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public ElectionModule getElectionModule() {
        return electionModule;
    }
}
