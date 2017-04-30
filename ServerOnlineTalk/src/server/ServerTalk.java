package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import entity.User;

public class ServerTalk {
	public static Map<String,User> users = new HashMap<>();
	public static void main(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(8888);
		System.out.println("服务器准备好");
		while(true){
			Socket socket = server.accept();
			SocketHandler hander =new SocketHandler(socket);
			Thread thread = new Thread(hander);
			thread.start();
		}
	}

}
