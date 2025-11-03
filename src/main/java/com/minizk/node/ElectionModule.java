package node;

import io.grpc.stub.StreamObserver;
import proto.MiniZooKeeperProto;
import proto.MiniZooKeeperServiceGrpc;

import java.util.concurrent.*; 
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ElectionModule extends MiniZooKeeperServiceGrpc.MiniZooKeeperServiceImplBase {
    private final String nodeId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private final AtomicLong lastHeartbeat = new AtomicLong(System.currentTimeMillis());

    private String currentLeaderId = null;
    private final long heartbeatInterval = 2000; // 2 seconds
    private final long heartbeatTimeout = 5000; // 5 seconds

    public ElectionModule(String nodeId) {
        this.nodeId = nodeId;
        startHeartbeatChecker();
    }

    private void startHeartbeatChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (isLeader.get()) {
                sendHeartbeats();
            } else if (now - lastHeartbeat.get() > heartbeatTimeout) {
                System.out.println(nodeId + ": Leader timeout detected. Starting re-election...");
                startElection();
            }
        }, 0, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        // In real implementation: send heartbeat RPC to all peers
        System.out.println(nodeId + " (Leader) sending heartbeats...");
    }

    private void startElection() {
        System.out.println(nodeId + ": Initiating election...");
        // For simplicity, assume node with lexicographically highest ID becomes leader
        // In real-world, we'd broadcast RequestVote RPCs and collect responses
        currentLeaderId = nodeId;
        isLeader.set(true);
        System.out.println(nodeId + " elected as new Leader.");
    }

    @Override
    public void heartbeat(MiniZooKeeperProto.HeartbeatRequest request,
                        StreamObserver<MiniZooKeeperProto.HeartbeatResponse> responseObserver) {
        String leaderId = request.getLeaderId();
        lastHeartbeat.set(System.currentTimeMillis());
        currentLeaderId = leaderId;
        isLeader.set(false);

        MiniZooKeeperProto.HeartbeatResponse response = MiniZooKeeperProto.HeartbeatResponse.newBuilder()
                .setAck(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void requestVote(MiniZooKeeperProto.VoteRequest request,
                            StreamObserver<MiniZooKeeperProto.VoteResponse> responseObserver) {
        // For simplicity, always vote yes if no current leader
        boolean grantVote = (currentLeaderId == null) || (request.getCandidateId().compareTo(currentLeaderId) > 0);

        if (grantVote) {
            currentLeaderId = request.getCandidateId();
        }

        MiniZooKeeperProto.VoteResponse response = MiniZooKeeperProto.VoteResponse.newBuilder()
                .setVoteGranted(grantVote)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public String getCurrentLeaderId() {
        return currentLeaderId;
    }
}