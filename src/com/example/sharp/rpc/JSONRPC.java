package com.example.sharp.rpc;

import com.example.JSON;
import com.example.sharp.BaseDictionary;
import com.example.sharp.CString;
import com.example.sharp.Delegates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;


public class JSONRPC {
    public static class StreamJsonRpc implements IJsonRpc{
        BufferedWriter writer;
        BufferedReader reader;
        public StreamJsonRpc(Reader reader,Writer writer) {
            this.writer = new BufferedWriter(writer);
            this.reader = new BufferedReader(reader);
        }


        @Override
        public void send(JSONRPCMessage msg) {
            String txt = JSON.serialize(msg);
            try {
                writer.write(txt);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }


        @Override
        public JSONRPCMessage recv() {
            String line = "";
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(CString.IsNullOrEmpty(line)) return null;
            try {
                return JSON.deserialize(line, JSONRPCMessage.class);
            }catch(Exception ee) {
                ee.printStackTrace();
                return null;
            }
        }
        public static class StreamJsonRpcClient implements IJsonRpcClient{
            StreamJsonRpc rpc;
            public StreamJsonRpcClient(StreamJsonRpc rpc) {
                this.rpc = rpc;
            }
            @Override
            public IJsonRpc getRpc() {
                return rpc;
            }

            @Override
            public JSONRPCMessage invoke(JSONRPCMessage msg) {
                rpc.send(msg);
                JSONRPCMessage ret = null;
                while(true) {
                    ret = rpc.recv();
                    if(ret == null) break;
                    if(ret.method.equals(msg.method)) break;
                }
                return ret;
            }

            @Override
            public JSONRPCMessage invoke(JSONRPCMessage msg, Delegates.Func1<JSONRPCMessage, JSONRPCMessage> callback) {
                rpc.send(msg);
                JSONRPCMessage ret = null;
                while(true) {
                    ret = rpc.recv();
                    if(ret == null) break;
                    if(ret.method.equals(msg.method)) {
                        break;
                    } else {
                        if(callback != null) {
                            JSONRPCMessage callbackRes = callback.Invoke(ret);
                            rpc.send(callbackRes);
                        }
                    }
                    
                }
                return ret;
            }
            
        }
        public static class StreamJsonRpcServer implements IJsonRpcServer{
            StreamJsonRpc rpc;
            BaseDictionary<String, Delegates.Action2<IJsonRpc, JSONRPCMessage>> callbacks = new BaseDictionary<>();
            public StreamJsonRpcServer(StreamJsonRpc rpc) {
                this.rpc = rpc;
            }
            @Override
            public void bindMethod(String method, Delegates.Action2<IJsonRpc, JSONRPCMessage> handler) {
                callbacks.set(method, handler);
            }

            @Override
            public void start() {
                while(true) {
                    try {
                        JSONRPCMessage msg = rpc.recv();
                        if(msg == null) continue;
                        if(CString.IsNullOrEmpty(msg.method)) {
                            // unable to handle
                            msg.result=false;
                            rpc.send(msg);
                            continue;
                        }
                        if(!callbacks.containsKey(msg.method)) {
                            // unable to handle
                            rpc.send(msg);
                            msg.result=false;
                            continue;
                        }
                        callbacks.get(msg.method).Invoke(rpc, msg);
                    }catch(Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            
        }
        @Override
        public IJsonRpcClient newClient() {
            return new StreamJsonRpcClient(this);
        }


        @Override
        public IJsonRpcServer newServer() {
            return new StreamJsonRpcServer(this);
        }
    }
    public IJsonRpc newConsoleRpc() {
        return new StreamJsonRpc(new InputStreamReader(System.in), new OutputStreamWriter(System.out));
    }
    public JSONRPC() {
        // TODO Auto-generated constructor stub
    }

}
