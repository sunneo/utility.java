package com.example.sharp.rpc;


import com.example.sharp.Delegates;

public interface IJsonRpcClient {
    IJsonRpc getRpc();
    JSONRPCMessage invoke(JSONRPCMessage msg);
    JSONRPCMessage invoke(JSONRPCMessage msg, Delegates.Func1<JSONRPCMessage,JSONRPCMessage> callback);
}
