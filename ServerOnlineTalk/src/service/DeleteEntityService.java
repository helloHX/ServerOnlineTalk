package service;

import dao.CommonDAO;

public class DeleteEntityService {
	public CommonDAO cd = new CommonDAO();
	public void deleteFriend(String userID,String friendID){
		String deleteSql = "delete from friend where friend.userID = ?"
				+ " and friend.friendID = ? ";
		cd.executeUpdate(deleteSql, new Object[] {
				userID,friendID});
		cd.executeUpdate(deleteSql, new Object[] {
				friendID,userID});
	}
}
