package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import service.DeleteEntityService;
import service.InsertEntityService;
import service.QueryMessageService;
import service.QueryUserService;
import util.EntityIDFactory;
import entity.Message;
import entity.User;

public class SocketHandler implements Runnable {
	private Socket mysocket;
	public static QueryUserService queryUserservice = new QueryUserService();
	public static final String userPath = "C:\\ServeronlineChart";

	public SocketHandler(Socket socket) {
		this.mysocket = socket;
	}

	@Override
	public void run() {
		try {
			boolean online = true;
			PrintWriter writer = new PrintWriter((new OutputStreamWriter(
					mysocket.getOutputStream(), "UTF-8")));
			InputStreamReader reader = new InputStreamReader(
					mysocket.getInputStream(), "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(reader);
			while (online) {
				online = dispatch(getData(bufferedReader.readLine()), writer);
			}
			writer.close();
			bufferedReader.close();
			mysocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Element getData(String mes) {
		System.out.println("Server_getData" + mes);
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
			return requestFriend(root);
		case "deleteFriend":
			return deleteFriend(root, writer);
		case "result":
			return result(root, writer);
		case "message":
			return message(root);
		case "file":
			return file(root, writer);
		case "image":
			return image(root, writer);
		case "logout":
			return logout(root, writer);
		}
		return true;
	}

	public boolean result(Element root, PrintWriter writer) {
		String command = root.getAttribute("command");
		String state = root.getAttribute("state");
		String message = root.getAttribute("message");
		
		switch (command) {
		case "file":
		case "image":
			if (state.equals("ok")) {
				UploadHandler uploadHandler = new UploadHandler(message,
						mysocket.getInetAddress().getHostAddress());
				uploadHandler.start();
			} else
				System.out.println(message);
			break;
		case "makeFriend":
			if (state.equals("ok")) {
				addfriends(message, writer);
			} else {
				// 添加失败回复
			}
			break;
		}
		return true;
	}
	private boolean deleteFriend(Element root, PrintWriter writer) {
		DeleteEntityService service = new DeleteEntityService();
		String userID = root.getAttribute("userID");
		String friendID = root.getAttribute("friendID");
		service.deleteFriend(userID, friendID);
		sentMessage("<result command='deleteFriend' state='ok'/>", writer);
		getFriends(userID, writer);
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

			transmitFile(message, path);// 准备转发
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

			transmitIMG(message, path);// 准备转发图片
		} catch (Exception e) {
			e.printStackTrace();
			sentMessage("<result command='image' message='服务器准备失败"
					+ "' state='error'/>", writer);
		}
		return true;
	}

	public void transmitFile(Message message, String Path) {
		File file = new File(Path);
		String fileInfo = "<file fileSentIime = '" + message.getMessageTime()
				+ "' " + "fileName = '" + file.getAbsolutePath()
				+ "' fileLength = '" + file.length() + "' fromId = '"
				+ message.getFormId() + "' toId = '" + message.getToId()
				+ "'/>";
		sentMessage(message, fileInfo, message.getToId());
	}

	public void transmitIMG(Message message, String Path) {
		File img = new File(Path);
		String imginfo = "<image imageSentIime = '" + message.getMessageTime()
				+ "' " + "imageName = '" + img.getAbsolutePath()
				+ "' imageLength = '" + img.length() + "' fromId = '"
				+ message.getFormId() + "' toId = '" + message.getToId()
				+ "'/>";
		sentMessage(message, imginfo, message.getToId());
	}

	private void notificatFriend(String userID, boolean state) {
		List<User> friendsList = queryUserservice.queryFriends(userID);
		for (User friend : friendsList) {// 添加在线状态
			User currentFriend = ServerTalk.users.get(friend.getUserID());
			if (currentFriend != null)
				sentMessage("<changeFriendState friendID = '" + userID
						+ "' state = '" + state + "'/>",
						currentFriend.getWriter());
		}
	}

	public boolean getFriends(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		return getFriends(userID, writer);
	}

	public boolean getFriends(String userID, PrintWriter writer) {
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
		if (user != null) {
			String userJson = JSONObject.fromObject(user).toString();
			sentMessage("<result command='queryUser' message='" + userJson
					+ "' state='ok'/>", writer);
		} else {
			sentMessage(
					"<result command='queryUser' message='查无此人' state='error'/>",
					writer);
		}
		return true;
	}

	private boolean requestFriend(Element root) {

		String userID = root.getAttribute("userID");
		String friendID = root.getAttribute("friendID");

		Message message = new Message();
		message.setFormId(userID);
		message.setToId(friendID);
		message.setMessageType("MKF");
		Date currentDate = new Date();
		String messageTime = currentDate.toLocaleString();
		message.setMessageTime(messageTime);

		requestFriend(message);
		return true;
	}

	private void requestFriend(Message message) {
		String reqMes = "<makeFriend userID = '" + message.getFormId()
				+ "' friendID = '" + message.getToId() + "'/>";
		sentMessage(message, reqMes, message.getToId());
	}

	public boolean login(Element root, PrintWriter writer) {
		String userID = root.getAttribute("userID");
		String password = root.getAttribute("password");
		if (!ServerTalk.users.containsKey(userID)) {
			User user = verify(userID, password);
			if (user != null) {
				String userJson = JSONObject.fromObject(user).toString();
				System.out.println("serveLogin" + userJson);
				ServerTalk.users.put(user.getUserID(), user);
				user.setWriter(writer);
				sentMessage("<result command='login' message='" + userJson
						+ "' state='ok'/>", writer);
				getFriends(userID, writer);// 获取好友列表
				clearMesCache(userID, writer);// 清空服务器消息缓存
				notificatFriend(userID, true);
			} else {
				sentMessage("<result command='login' message='" + "不存在账号或已经登录"
						+ "' state='error'/>", writer);
				return false;
			}
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
				transmitIMG(messages.get(i), messages.get(i).getMessage());
				break;
			case "MKF":
				requestFriend(messages.get(i));
				break;
			case "RMKF":
				addfriends(messages.get(i));
				break;
			case "FILE":
				transmitFile(messages.get(i),messages.get(i)
						.getMessage());
				break;
			case "MESSAGE":
				message(messages.get(i));
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



	private void addfriends(String idInfo, PrintWriter writer) {

		String[] ids = idInfo.split(",");
		InsertEntityService service = new InsertEntityService();
		service.addFriend(ids[0], ids[1]);// id0请求方

		Message message = new Message();
		message.setFormId(ids[0]);
		message.setToId(ids[1]);
		Date currentTime = new Date();
		message.setMessageTime(currentTime.toLocaleString());
		message.setMessageType("RMKF");
		
		addfriends(message);
		
		getFriends(ids[1], writer);
	}
	
	private void addfriends(Message message){
		String result = "<result command='makeFriend' message='" + message.getToId()
				+ "' state='ok'/>";
		sentMessage(message, result,message.getFormId());
	}

	public void sentMessage(Message message, String data, String id) {
		User toUser = ServerTalk.users.get(id);
		if (toUser != null) {
			System.out.println(toUser.getUserName());
			sentMessage(data, toUser.getWriter());
			message.setStatus(0);
		} else {
			message.setStatus(1);
		}
		saveMessage(message);
	}

	public boolean message(Element root) {
		Message message = new Message();
		message.setFormId(root.getAttribute("from"));
		message.setToId(root.getAttribute("to"));
		message.setMessageType("MESSAGE");
		message.setMessageTime(root.getAttribute("messageTime"));
		message.setMessage(root.getAttribute("message"));

		message(message);
		return true;
	}
	
	private void message(Message message) {
		String mes = "<message  messageTime='" + message.getMessageTime()
				+ "' message='" + message.getMessage() + "' from='"
				+ message.getFormId() + "' to='" + message.getToId() + "'/>";
		sentMessage(message, mes, message.getToId());
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
			notificatFriend(user.getUserID(), false);
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
