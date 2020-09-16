package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.GroupsDao;

public class GroupsService {
	
	private GroupsDao groupsDao;
	
	public GroupsService(){
		this.groupsDao = new GroupsDao();
	}
	
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list = null;
		Map<String,String> where = ConvertUtil.convertMap(params);
		list = groupsDao.search(where);
		return list;
	}
	
	public int doUpdate(Map<String,String> map,Map<String,String> where){
		int row = 0;
		row = groupsDao.update(map, where);
		return row;
	}
	
	public List<Map<String,String>> doSearchBySql(String sql,List<String> params){
		List<Map<String,String>> list = null;
		list = groupsDao.executeQuery(sql, params);
		return list;
	}
	
	public int doDelete(Map<String,String[]> params){
		int row = 0;
		Map<String,String> where = ConvertUtil.convertMap(params);
		row = groupsDao.delete(where);
		return row;
	}
	
	public boolean doInsert(Map<String,String[]> params){
		boolean flag=false;
		Map<String,String> map = ConvertUtil.convertMap(params);
		//insert方法返回的是受影响的行数
		int row = groupsDao.insert(map);
		//如果行数大于0，说明插入成功
		if(row>0){
			flag=true;
		}
		return flag;
	}

}
