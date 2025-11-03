package com.minizk.node;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Node {

    private final String nodeId;
    private final int port;
    private final Map<String, RPCClient> peers = new ConcurrentHashMap<>();
    private final RPCServer rpcServer;
    private final ElectionModule electionModule;

    public Node(String nodeId, int port, List<NodeConfig> peerConfigs) {
        this.nodeId = nodeId;
        this.port = port;
        this.rpcServer = new RPCServer(nodeId, port);
        this.electionModule = rpcServer.getElectionModule();

        for (NodeConfig peer : peerConfigs) {
            if (!peer.getNodeId().equals(nodeId)) {
                peers.put(peer.getNodeId(), new RPCClient("localhost", peer.getPort()));
            }
        }
    }

    public void start() throws IOException, InterruptedException {
        // Start the gRPC server
        rpcServer.start();

        // Start a heartbeat loop (only if leader)
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (electionModule.isLeader()) {
                broadcastHeartbeats();
            }
        }, 2, 2, TimeUnit.SECONDS);

        rpcServer.blockUntilShutdown();
    }

    private void broadcastHeartbeats() {
        String leaderId = nodeId;
        System.out.println("📡 Broadcasting heartbeats from leader " + leaderId);

        for (Map.Entry<String, RPCClient> entry : peers.entrySet()) {
            String peerId = entry.getKey();
            RPCClient client = entry.getValue();

            boolean success = client.sendHeartbeat(leaderId);
            if (!success) {
                System.err.println("⚠️ Failed to send heartbeat to peer " + peerId);
            }
        }
    }

    public void initiateElection() {
        System.out.println("🗳️ " + nodeId + " initiating vote requests...");

        int grantedVotes = 1; // vote for self
        for (Map.Entry<String, RPCClient> entry : peers.entrySet()) {
            RPCClient client = entry.getValue();
            boolean voteGranted = client.requestVote(nodeId);
            if (voteGranted) grantedVotes++;
        }

        if (grantedVotes > (peers.size() + 1) / 2) {
            System.out.println("✅ " + nodeId + " won the election (quorum achieved).");
        } else {
            System.out.println("❌ " + nodeId + " failed to achieve quorum.");
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getPort() {
        return port;
    }

    public boolean isLeader() {
        return electionModule.isLeader();
    }
}
