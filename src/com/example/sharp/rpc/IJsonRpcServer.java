package com.example.sharp.rpc;

import com.example.sharp.Delegates;

public interface IJsonRpcServer {
    void bindMethod(String method,Delegates.Action2<IJsonRpc,JSONRPCMessage> handler);
    void start();
}
