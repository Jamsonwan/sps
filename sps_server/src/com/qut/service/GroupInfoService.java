package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.GroupInfoDao;

public class GroupInfoService {
	
	private GroupInfoDao groupInfoDao;
	
	public GroupInfoService(){
		this.groupInfoDao = new GroupInfoDao();
	}
	
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list  = null;
		Map<String,String> where = ConvertUtil.convertMap(params);
		list = groupInfoDao.search(where);
		return list;
	}
	
	public List<Map<String,String>> doSearchBySql(String sql,List<String> params){
		List<Map<String,String>> list = null;
		list = groupInfoDao.executeQuery(sql, params);
		return list;
	}
	
	public int doUpdate(Map<String,String> map,Map<String,String> where){
		int row = 0;
		
		row = groupInfoDao.update(map, where);
		
		return row;
	}
	
	public int doDelete(Map<String,String[]> params){
		int row = 0;
		Map<String,String> where = ConvertUtil.convertMap(params);
		row = groupInfoDao.delete(where);
		return row;
	}
}
