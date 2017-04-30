package service;

import java.lang.reflect.Field;
import java.util.ArrayList;

import annotation.Column;
import annotation.Entity;
import dao.CommonDAO;

public class InsertEntityService {
	public CommonDAO cd = new CommonDAO();
	private Class<?> clazz;
	public void  insertEntity(Object object) throws Exception{
		clazz = object.getClass();
		ArrayList<Object> params = new ArrayList<Object>();
		Entity entity = (Entity) clazz.getAnnotation(Entity.class);
		String insertSql = "insert " + entity.tabName() + " (";
		String values = " VALUES(";
		Field[] fields = clazz.getDeclaredFields();
		for(int i = 0;i < fields.length;i++){
			fields[i].setAccessible(true);
			Column column = (Column)fields[i].getAnnotation(Column.class);
			if(column != null){
				insertSql += column.columnName() + ",";
				values += "?,";
				params.add(fields[i].get(object));
			}
		}
		insertSql = insertSql.substring(0, insertSql.length() - 1) + ")";
		insertSql += values.substring(0, values.length() - 1) + ")";
		cd.executeUpdate(insertSql, params.toArray());
	}
	
	public void addFriend(String userID,String friendID){
		String sql = "insert into friend(userID,friendId) value(?,?)";
		cd.executeUpdate(sql, new Object[] {
				userID,friendID});
		cd.executeUpdate(sql, new Object[] {
				friendID,userID});
	}
}
