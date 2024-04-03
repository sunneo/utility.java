package com.example.sharp.rpc;

public interface IJsonRpc {
    void send(JSONRPCMessage m);
    JSONRPCMessage recv();
    public IJsonRpcClient newClient();
    public IJsonRpcServer newServer();
}
