package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.UsersInfoDao;

public class UsersInfoService {
	
	private UsersInfoDao usersInfoDao;
	
	public UsersInfoService(){
		this.usersInfoDao = new UsersInfoDao();
	}
	
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list = null;
		Map<String,String> map = ConvertUtil.convertMap(params);
		list = usersInfoDao.search(map);
		return list;
	}
	
	public List<Map<String,String>> doSearchBySql(String sql,List<String> where){
		List<Map<String,String>> list = null;
		list = usersInfoDao.executeQuery(sql, where);
		return list;
	}
	
	public List<Map<String,String>> mySearch(String sql,List<String>params){
		List<Map<String, String>>list = null;
		try {
			list= usersInfoDao.executeQuery(sql, params);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public int doUpdate(Map<String,String> map,Map<String,String> where) {
		int row  = 0;
		row = usersInfoDao.update(map, where);
		return row;
	}

	public List<Map<String,String>> findById(Map<String,String[]> params){
		List<Map<String,String>> list=null;
		Map<String,String> map=ConvertUtil.convertMap(params);
		
		String name=map.get("userId");
		list=usersInfoDao.findById(name);
		return list;
	}
	
}
