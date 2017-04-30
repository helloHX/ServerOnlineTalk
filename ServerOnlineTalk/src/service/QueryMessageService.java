package service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dao.CommonDAO;
import entity.Message;

public class QueryMessageService {
	public CommonDAO cd = new CommonDAO();
	public List<Message> queryMessage(String toId,int type){
		ArrayList<Message> messageList = new ArrayList<Message>();
		String querySql = "select * from message where message.status = ? "
				+ " and toId =? order by messageCreateTime";
		String updataSql = "update message SET status = ? where message.toId = ?";
		List<Map<String, Object>> lm = cd.executeQuery(querySql, new Object[] {type,
				toId});
		cd.executeUpdate(updataSql, new Object[] {0,
				toId});
		for (int i = 0; i < lm.size(); i++) {
			Message mes = new Message();
			mes.setFormId((String) lm.get(i).get("fromID"));
			mes.setToId((String) lm.get(i).get("toID"));
			mes.setMessageTime((String)lm.get(i).get("messageCreateTime"));
			mes.setMessage((String) lm.get(i).get("message"));
			mes.setMessageType((String) lm.get(i).get("messageType"));
			mes.setMessageId((String) lm.get(i).get("messageID"));
			messageList.add(mes);
		}
		return messageList;
	}
}
