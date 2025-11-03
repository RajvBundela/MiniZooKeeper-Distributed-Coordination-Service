package com.minizk.node;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import proto.MiniZooKeeperProto;
import proto.MiniZooKeeperServiceGrpc;

import java.util.concurrent.TimeUnit;

public class RPCClient {

    private final String targetHost;
    private final int targetPort;
    private final ManagedChannel channel;
    private final MiniZooKeeperServiceGrpc.MiniZooKeeperServiceBlockingStub blockingStub;

    public RPCClient(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.channel = ManagedChannelBuilder.forAddress(targetHost, targetPort)
                .usePlaintext()
                .build();
        this.blockingStub = MiniZooKeeperServiceGrpc.newBlockingStub(channel);
    }

    public boolean sendHeartbeat(String leaderId) {
        try {
            MiniZooKeeperProto.HeartbeatRequest request = MiniZooKeeperProto.HeartbeatRequest.newBuilder()
                    .setLeaderId(leaderId)
                    .build();

            MiniZooKeeperProto.HeartbeatResponse response = blockingStub.heartbeat(request);
            return response.getAck();
        } catch (Exception e) {
            System.err.println("❌ Heartbeat failed to " + targetHost + ":" + targetPort + " - " + e.getMessage());
            return false;
        }
    }

    public boolean requestVote(String candidateId) {
        try {
            MiniZooKeeperProto.VoteRequest request = MiniZooKeeperProto.VoteRequest.newBuilder()
                    .setCandidateId(candidateId)
                    .build();

            MiniZooKeeperProto.VoteResponse response = blockingStub.requestVote(request);
            return response.getVoteGranted();
        } catch (Exception e) {
            System.err.println("❌ Vote request failed to " + targetHost + ":" + targetPort + " - " + e.getMessage());
            return false;
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }
}
