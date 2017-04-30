package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import service.InsertEntityService;
import service.QueryMessageService;
import service.QueryUserService;
import util.EntityIDFactory;
import util.WriteByteData;
import entity.Message;
import entity.User;

public class SocketHandler implements Runnable {
	private Socket mysocket;
	public static QueryUserService queryUserservice = new QueryUserService();
	public static final String userPath = "D:\\测试垃圾\\ServeronlineChart";

	public SocketHandler(Socket socket) {
		this.mysocket = socket;
	}

	@Override
	public void run() {
		try {
			boolean online = true;
			PrintWriter writer = new PrintWriter(mysocket.getOutputStream());
			InputStreamReader reader = new InputStreamReader(
					mysocket.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(reader);

			while (online) {
				String mes = bufferedReader.readLine();
				System.out.println("server_run" + mes);
				online = dispatch(getData(mes), writer);
			}
			writer.close();
			bufferedReader.close();
			mysocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Element getData(String mes) {
		Element root = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder
					.parse(new InputSource(new StringReader(mes)));
			root = doc.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}

	public boolean dispatch(Element root, PrintWriter writer) {
		switch (root.getTagName()) {
		case "login":
			return login(root, writer);
		case "friends":
			return getFriends(root, writer);
		case "queryUser":
			return queryUser(root, writer);
		case "makeFriend":
			return makeFriend(root, writer);
		case "result":
			return result(root, writer);
		case "message":
			return message(root, writer);
		case "file":
			return file(root, writer);
		case "image":
			return image(root, writer);
		case "logout":
			return logout(root, writer);
		}
		return true;
	}

	

	public boolean file(Element root, PrintWriter writer) {

		String fileSentTime = root.getAttribute("fileSentTime");
		String fileName = root.getAttribute("fileName");
		String fileLength = root.getAttribute("fileLength");
		String fromId = root.getAttribute("fromId");
		String toId = root.getAttribute("toId");
		File file = new File(fileName);
		String path = userPath + "\\" + toId + "\\" + fromId + "\\"
				+ file.getName();

		Message message = new Message();
		message.setMessage(path);
		message.setMessageTime(fileSentTime);
		message.setMessageType("FILE");
		message.setFormId(fromId);
		message.setToId(toId);

		try {
			CountDownLatch downOverSingal = new CountDownLatch(1);
			DownLoadHandler downLoadHandler = new DownLoadHandler(
					Long.parseLong(fileLength), path, downOverSingal);
			downLoadHandler.start();
			sentMessage("<result command='file' message='" + fileName
					+ "' state='ok'/>", writer);
			downOverSingal.await();

			transmitFile(message, new File(path));// 准备转发
		} catch (Exception e) {
			e.printStackTrace();
			sentMessage("<result command='file' message='服务器准备失败"
					+ "' state='error'/>", writer);
		}

		return true;
	}

	public boolean image(Element root, PrintWriter writer) {
		System.out.println("准备接受图片");
		String imageSentIime = root.getAttribute("imageSentIime");
		String imageName = root.getAttribute("imageName");
		String imageLength = root.getAttribute("imageLength");
		String fromId = root.getAttribute("fromId");
		String toId = root.getAttribute("toId");
		File file = new File(imageName);
		String path = userPath + "\\" + toId + "\\" + fromId + "\\"
				+ file.getName();

		Message message = new Message();
		message.setMessage(path);
		message.setMessageTime(imageSentIime);
		message.setMessageType("IMG");
		message.setFormId(fromId);
		message.setToId(toId);

		try {
			CountDownLatch downOverSingal = new CountDownLatch(1);
			DownLoadHandler downLoadHandler = new DownLoadHandler(
					Long.parseLong(imageLength), path, downOverSingal);
			downLoadHandler.start();
			sentMessage("<result command='image' message='" + imageName
					+ "' state='ok'/>", writer);
			downOverSingal.await();

			transmitIMG(message, new File(path));// 准备转发图片
		} catch (Exception e) {
			e.printStackTrace();
			sentMessage("<result command='image' message='服务器准备失败"
					+ "' state='error'/>", writer);
		}
		return true;
	}

	public void transmitFile(Message message, File file) {
		User toUser = ServerTalk.users.get(message.getToId());
		if (toUser != null) {
			System.out.println(toUser.getUserName());
			sentMessage("<file fileSentIime = '" + message.getMessageTime()
					+ "' " + "fileName = '" + file.getAbsolutePath()
					+ "' fileLength = '" + file.length() + "' fromId = '"
					+ message.getFormId() + "' toId = '" + message.getToId()
					+ "'/>", toUser.getWriter());
			message.setStatus(0);
		} else {
			message.setStatus(1);
		}
		saveMessage(message);
	}

	public void transmitIMG(Message message, File img) {
		User toUser = ServerTalk.users.get(message.getToId());
		if (toUser != null) {
			System.out.println(toUser.getUserName());
			sentMessage("<image imageSentIime = '" + message.getMessageTime()
					+ "' " + "imageName = '" + img.getAbsolutePath()
					+ "' imageLength = '" + img.length() + "' fromId = '"
					+ message.getFormId() + "' toId = '" + message.getToId()
					+ "'/>", toUser.getWriter());
			message.setStatus(0);
		} else {
			message.setStatus(1);
		}
		saveMessage(message);
	}

	public boolean getFriends(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		return getFriends(userID, writer);
	}
	public boolean getFriends(String userID,PrintWriter writer) {
		List<User> friendsList = queryUserservice.queryFriends(userID);
		for (User friend : friendsList) {// 添加在线状态
			if (ServerTalk.users.containsKey(friend.getUserID()))
				friend.setUserStatus(true);
		}
		String friendsJson = JSONArray.fromObject(friendsList).toString();
		System.out.println("server_getFriends" + friendsJson);
		sentMessage("<result command='friends' message='" + friendsJson
				+ "' state='ok'/>", writer);
		return true;
	}

	public boolean queryUser(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		User user = queryUserservice.queryUser(userID);
		if(user != null){
		String userJson = JSONObject.fromObject(user).toString();
		sentMessage("<result command='queryUser' message='" + userJson
				+ "' state='ok'/>", writer);
		}else{
			sentMessage("<result command='queryUser' message='查无此人' state='error'/>", writer);
		}
		return true;
	}

	private boolean makeFriend(Element root, PrintWriter writer) {
		InsertEntityService service = new InsertEntityService();
		String userID = root.getAttribute("userID");
		String friendID = root.getAttribute("friendID");
		service.addFriend(userID, friendID);
		sentMessage("<result command='makeFriend' state='ok'/>", writer);
		getFriends(userID,writer);
		return true;
	}
	public boolean login(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		String password = root.getAttribute("password");
		if (!ServerTalk.users.containsKey(userID)) {
			User user = verify(userID, password);
			String userJson = JSONObject.fromObject(user).toString();
			System.out.println("serveLogin" + userJson);
			ServerTalk.users.put(user.getUserID(), user);
			user.setWriter(writer);
			if (user != null) {
				sentMessage("<result command='login' message='" + userJson
						+ "' state='ok'/>", writer);
				clearMesCache(userID, writer);
			}
		} else {
			sentMessage("<result command='login' message='" + "不存在账号或已经登录"
					+ "' state='error'/>", writer);
		}
		return true;
	}

	public void clearMesCache(String userId, PrintWriter writer) {
		QueryMessageService serivce = new QueryMessageService();
		List<Message> messages = serivce.queryMessage(userId, 1);
		for (int i = 0; i < messages.size(); i++) {
			System.out.println("mesType" + messages.get(i).getMessageType());
			switch (messages.get(i).getMessageType()) {
			case "IMG":
				transmitIMG(messages.get(i), new File(messages.get(i)
						.getMessage()));
				break;
			case "FILE":
				transmitFile(messages.get(i), new File(messages.get(i)
						.getMessage()));
				break;
			case "MESSAGE":
				message(messages.get(i), writer);
				break;
			}

		}
	}

	public User verify(String userID, String password) {

		List<Object> result = queryUserservice.login(userID, password);
		if ((boolean) result.get(0)) {
			return (User) result.get(1);
		} else {
			return null;
		}
	}

	public boolean result(Element root, PrintWriter writer) {
		String command = root.getAttribute("command");
		String state = root.getAttribute("state");
		String message = root.getAttribute("message");
		switch (command) {
		case "file":
		case "image":
			if (state.equals("ok")) {
				System.out.println("Server_result ip"
						+ mysocket.getInetAddress().toString());
				UploadHandler uploadHandler = new UploadHandler(message,
						mysocket.getInetAddress().getHostAddress());
				uploadHandler.start();
			} else
				System.out.println(message);
			return true;
		}
		return true;
	}

	public boolean message(Element root, PrintWriter writer) {
		Message message = new Message();
		message.setFormId(root.getAttribute("from"));
		message.setToId(root.getAttribute("to"));
		message.setMessageType("MESSAGE");
		message.setMessageTime(root.getAttribute("messageTime"));
		message.setMessage(root.getAttribute("message"));
		return message(message, writer);
	}

	public boolean message(Message message, PrintWriter writer) {
		User toUser = ServerTalk.users.get(message.getToId());
		if (toUser != null) {
			System.out.println(toUser.getUserName());
			sentMessage("<message  messageTime='" + message.getMessageTime()
					+ "' message='" + message.getMessage() + "' from='"
					+ message.getFormId() + "' to='" + message.getToId()
					+ "'/>", toUser.getWriter());
			message.setStatus(0);
		} else {
			message.setStatus(1);
		}
		saveMessage(message);

		return true;
	}

	public static void saveMessage(Message message) {
		InsertEntityService service = new InsertEntityService();
		try {// 服务器保存消息
			message.setMessageId(EntityIDFactory.createId());
			service.insertEntity(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean logout(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		if (ServerTalk.users.containsKey(userID)) {
			User user = ServerTalk.users.remove(userID);
			sentMessage("<result command = 'logout' state = 'ok' />",
					user.getWriter());
			return false;
		} else {
			sentMessage("<result command = 'logout'"
					+ " state ='error' message='登陆失败" + userID + "'/>", writer);
			return true;
		}
	}

	public void sentMessage(String mes, PrintWriter writer) {
		System.out.println("server_sent" + mes);
		try {
			writer.println(mes);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
