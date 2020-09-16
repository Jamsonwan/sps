package com.qut.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qut.dao.CollectVideoDao;


public class CollectVideoService {
	
	    public final int doInsert(Map<String,String> map) {
	    	int row = 0;
	    	CollectVideoDao dao = new CollectVideoDao();
	    	row = dao.insert(map);
	    	return row;
	    }
	    
	    public final int doDelete(Map<String,String> map) {
	    	int row = 0;
	    	CollectVideoDao dao = new CollectVideoDao();
	    	row = dao.delete(map);
	    	return row;
	    }
	    
		public final List<Map<String, String>> doSearch() {
			List<Map<String, String>> list = null;
			CollectVideoDao dao = new CollectVideoDao();
			list = dao.search(null);
			return list;
		}

		// 1.��������ѯһ���������¼
		public final List<Map<String, String>> doSearchByInId(String s) {
			List<Map<String, String>> list = null;
			CollectVideoDao dao = new CollectVideoDao();
			Map<String, String> where = new HashMap<String, String>();
			where.put("id", s);
			list = dao.search(where);
			return list;
		}

		public final List<Map<String, String>> doSearchByType(String type) {
			List<Map<String, String>> list = null;
			CollectVideoDao dao = new CollectVideoDao();
			Map<String, String> where = new HashMap<String, String>();
			where.put("type", type);
			list = dao.search(where);
			return list;
		}
		
	
		public final List<Map<String, String>> doSearchForCollect(
				 Map<String,String> where) {
			List<Map<String, String>> list = null;
			CollectVideoDao dao = new CollectVideoDao();
			list = dao.search(where);
			return list;
		}
		
		public List<Map<String,String>> doSearchBySql(String sql,List<String> params){
	  		List<Map<String,String>> list = null;
	  		CollectVideoDao dao = new CollectVideoDao();
	  		list = dao.executeQuery(sql, params);
	  		return list;
	  	}
}
