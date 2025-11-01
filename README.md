# MiniZooKeeper-Distributed-Coordination-Service

This project is a simplified version of Apache ZooKeeper, where multiple nodes maintain a small configuration tree and elect a leader to manage updates.

Key Features:

Leader election using heartbeat messages.

Write requests only accepted by the leader; followers sync periodically.

Watchers (listeners) get notified when data changes.

Fault detection with timeout-based leader reelection.

Learning: Leader election, quorum replication, coordination consistency.
