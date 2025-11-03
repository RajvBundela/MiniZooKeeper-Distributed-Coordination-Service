package com.minizk.node;
import java.util.List;
public class NodeConfig {
    public String id;
    public String host;
    public int port;
    public List<Peer> peers;

    public static class Peer {
        public String id;
        public String host;
        public int port;
    }
}