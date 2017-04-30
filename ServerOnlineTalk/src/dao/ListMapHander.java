package dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMapHander implements ResultSetHander {

	@Override
	public Object doHander(ResultSet rs) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>(); 
		ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        
        HashMap<String,Object> m = null;
        
        //遍历结果集
        while(rs.next()){
        	m = new HashMap<String,Object>();
        	//将结果集中的数据保存到HashMap中
        	for (int i = 1; i <= cols; i++) {
				m.put(rsmd.getColumnLabel(i), rs.getObject(i));
			}
        	resultList.add(m);
        }
		return resultList;
	}

}
