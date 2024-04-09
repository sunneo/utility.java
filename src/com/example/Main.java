package com.example;

import java.net.InetSocketAddress;
import java.net.Socket;

import com.example.sharp.CString;
import com.example.sharp.IniReader;
import com.example.sharp.IniWriter;
import com.example.sharp.annotations.FlattenArrayLengthName;
import com.example.sharp.coroutine.AsyncTask;
import com.example.sharp.rpc.IJsonRpc;
import com.example.sharp.rpc.IJsonRpcClient;
import com.example.sharp.rpc.JSONRPC;
import com.example.sharp.rpc.JSONRPCMessage;

public class Main {
	public static class ExampleClass {
		public Integer a;
		public Double b;

		public static class Inner {
			public int a2;
			public double b2;
		}

		Inner inner = new Inner();
	}

	public static class SubItem {
		public Integer key;
		public String value;

		public SubItem() {

		}

		public SubItem(Integer k, String v) {
			this.key = k;
			this.value = v;
		}
	}

	public static class NestSubItem {
		public String Id;
		@FlattenArrayLengthName(name = "subItemCount")
		public SubItem[] subItems;

	}

	public static class JSONRPCMessageStrings extends JSONRPCMessage {
		@FlattenArrayLengthName(name = "stringLen")
		public String[] stringArray;

		@FlattenArrayLengthName(name = "subItemCount")
		public SubItem[] subItems;
		@FlattenArrayLengthName(name = "nestSubItemCount")
		public NestSubItem[] subNestItems;
		public Integer stringLen;
	}

	public static void main(String[] args) {
		try {
			JSONRPCMessage msg = new JSONRPCMessage();
			msg.method = "test";
			msg.assignParam("hello ", "world");
			String json = JSON.serialize(msg);
			JSONRPCMessage msg2 = JSON.deserialize(json, JSONRPCMessage.class);
			System.out.println();
			JSONRPCMessageStrings str = new JSONRPCMessageStrings();
			str.stringArray = new String[] { "a", "b", "c" };
			str.subItems = new SubItem[] { new SubItem(1, "a"), new SubItem(2, "b"), new SubItem(3, "c"),
					new SubItem(4, "d") };
			str.subNestItems = new NestSubItem[] { new NestSubItem(), new NestSubItem() };
			str.subNestItems[0].Id = "QAQ1";
			str.subNestItems[0].subItems = new SubItem[] { new SubItem(5, "e"), new SubItem(6, "f"), };
			str.subNestItems[1].Id = "QAQ2";
			str.subNestItems[1].subItems = new SubItem[] { new SubItem(7, "g"), new SubItem(8, "h"), };
			str.stringLen = str.stringArray.length;
			String ini = IniWriter.SerializetoString(str);
			if (!CString.IsNullOrEmpty(ini)) {
				System.err.println("Deserialize:" + ini);
				JSONRPCMessageStrings msgFromIni = IniReader.DeserializeString(ini, JSONRPCMessageStrings.class);
				if (msgFromIni != null) {
					System.err.println("Deserialize Again " + IniWriter.SerializetoString(msgFromIni));
				}
				String jsonFromMsgFromIni = JSON.serialize(msgFromIni);
				if (jsonFromMsgFromIni != null) {
					System.err.println("JSON Serialize Again" + jsonFromMsgFromIni);
					JSONRPCMessageStrings msgFromJson = JSON.deserialize(jsonFromMsgFromIni,
							JSONRPCMessageStrings.class);
					System.err.println("Derialize from JSON " + "");
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		JSONRPC rpc = new JSONRPC();
		try {
			AsyncTask.ThreadingFuture<IJsonRpcClient> clientFuture = AsyncTask.runAsync(() -> {
				try {
					Socket sckClient = new Socket();
					InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 9999);
					sckClient.connect(isa);
					IJsonRpc jsonRpc = rpc.newStreamRpc(sckClient.getInputStream(), sckClient.getOutputStream());
					return jsonRpc.newClient();
				} catch (Exception ee) {

				}
				return null;
			});
			String res = clientFuture.thenRun((clnt) -> {
				JSONRPCMessage msg = new JSONRPCMessage();
				msg.method = "GetConfigFile";
				JSONRPCMessage rpcRet = clnt.invoke(msg);
				System.err.println("Result"+ String.valueOf(rpcRet.Result));
				return String.valueOf(rpcRet.Result);
			}).get();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
