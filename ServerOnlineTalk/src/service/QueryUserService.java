package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dao.CommonDAO;
import entity.User;

public class QueryUserService {
	public CommonDAO cd = new CommonDAO();

	public List<Object> login(String userID, String userPassword) {
		String sql = "select * from user where UserID=? and UserPassword=?";
		List<Map<String, Object>> lm = cd.executeQuery(sql, new Object[] {
				userID, userPassword});

		List<Object> rl = new ArrayList<Object>();
		// 登录成功
		if (lm.size() > 0) {
			Map<String, Object> userMap = lm.get(0);
			User user = new User();
			user.setUserID((String)userMap.get("userID"));
			user.setUserName((String) userMap.get("userName"));
			rl.add(true);
			rl.add(user);
		}else{
			rl.add(false);
		}
		return rl;
	}
	
	//查询相应用户的好友信息
	public List<User> queryFriends(String userID) {
		String sql = "select USER.userID as friendId, "
				+ "USER.userName as friendName "
				+ "FROM USER,friend WHERE user.userID = friend.friendID "
				+ "AND friend.userID =?";
		List<Map<String, Object>> lm = cd.executeQuery(sql, new Object[] {
				userID});
		
		List<User> rl = new ArrayList<User>();
		for (int i = 0; i < lm.size(); i++) {
			User user = new User();
			user.setUserID((String) lm.get(i).get("friendId"));
			user.setUserName((String) lm.get(i).get("friendName"));
			rl.add(user);
		}
		return rl;
	}
	
	public User queryUser(String userID){
		String sql = "select user.userID,user.userName from user where user.userID = ?";
		List<Map<String, Object>> lm = cd.executeQuery(sql, new Object[] {
				userID});
		User user = null;
		for (int i = 0; i < lm.size(); i++) {
			user = new User();
			user.setUserID((String) lm.get(i).get("userID"));
			user.setUserName((String) lm.get(i).get("userName"));
		}
		return user;
	}
	

}
