package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHander {
	
	//自定义处理结果集
	public Object doHander(ResultSet rs) throws SQLException;
	
}
