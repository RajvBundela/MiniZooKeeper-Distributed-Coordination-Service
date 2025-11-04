# MiniZooKeeper-Distributed-Coordination-Service

This project is a simplified version of Apache ZooKeeper, where multiple nodes maintain a small configuration tree and elect a leader to manage updates.

Key Features:

Leader election using heartbeat messages.

Write requests only accepted by the leader; followers sync periodically.

Watchers (listeners) get notified when data changes.

Fault detection with timeout-based leader reelection.

Learning: Leader election, quorum replication, coordination consistency.


                   +-------------------+
                   |     Client CLI    |
                   |-------------------|
                   | - get/set/delete  |
                   | - registerWatch   |
                   +---------+---------+
                             |
                             | gRPC
                             v
       ┌───────────────────────────────────────────────────────────┐
       │                  MiniZooKeeper Cluster                    │
       │                                                           │
       │ ┌─────────────────────────────────────────────────────┐   │
       │ │                       Node                         │   │
       │ │-----------------------------------------------------│   │
       │ │ ElectionModule      → Leader Election, Heartbeats   │   │
       │ │ ReplicationModule   → Quorum Write Replication       │   │
       │ │ ConfigStore         → Hierarchical Key-Value Store   │   │
       │ │ WatchService        → Watchers & Notifications       │   │
       │ │ RPCServer/RPCClient → gRPC Communication             │   │
       │ └─────────────────────────────────────────────────────┘   │
       │       ^                     ^                     ^       │
       │       │ Heartbeat/Vote RPCs │ AppendUpdate RPCs   │ gRPC  │
       │   +---+---------+     +-----+---------+     +-----+------+
       │   |   Node 1    |     |   Node 2      |     |   Node 3   |
       │   | (Leader)    |     | (Follower)    |     | (Follower) |
       │   +-------------+     +---------------+     +-------------+
       │                                                           │
       └───────────────────────────────────────────────────────────┘
                             |
                             | Watch Notifications (gRPC stream)
                             ▼
                   +--------------------+
                   | Watcher Callback   |
                   +--------------------+

