package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.MyFriendsDao;

public class MyFriendsService {
	
	private MyFriendsDao myFriendsDao;
	
	public MyFriendsService(){
		this.myFriendsDao = new MyFriendsDao();
	}
	
    /**
     * 进行单表查询
     * @param params
     * @return
     */
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list = null;
		Map<String,String> where = ConvertUtil.convertMap(params);
		list = myFriendsDao.search(where);
		return list;
	}
	
	/**
	 * 进行多表的镶嵌查询
	 * 注意?不能是中文的
	 * @param sql 格式为select * from table1,table2 where table1.id=?
	 * @param params 对应sql里面的"?"的值
	 * @return
	 */
	public List<Map<String,String>> doSearchBySql(String sql,List<String> params){
		List<Map<String,String>> list = null;
		list = myFriendsDao.executeQuery(sql, params);
		return list;
	}
	
	public int doUpdate(Map<String,String> map,Map<String,String> where){
		int row = 0;
		row = myFriendsDao.update(map, where);
		return row;
	}
	public int doDelete(Map<String,String[]> params){
		int row = 0;
		Map<String,String> map = ConvertUtil.convertMap(params);
		row = myFriendsDao.delete(map);
		return row;
	}
}
