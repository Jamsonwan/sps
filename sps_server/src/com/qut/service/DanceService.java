package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.DanceDao;

public class DanceService {
	
	private DanceDao danceDao;
	
	public DanceService(){
		this.danceDao = new DanceDao();
	}
	
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list = null;
		Map<String,String> where = ConvertUtil.convertMap(params);
		list = danceDao.search(where);
		return list;
	}
	
	public int doInsert(Map<String,String[]> params){
		int row  = 0;
		Map<String,String> map = ConvertUtil.convertMap(params);
		row = danceDao.insert(map);
		return row;
	}
	
	public int doDelete(Map<String,String[]> params){
		int row = 0;
		Map<String,String> where = ConvertUtil.convertMap(params);
		row = danceDao.delete(where);
		return row;
	}
	
	public List<Map<String,String>> doSearchBySql(String sql,List<String> params){
		List<Map<String,String>> resultList = null;
		resultList = danceDao.executeQuery(sql, params);
		return resultList;
	}
	
	public int doUpdate(Map<String,String> map,Map<String,String> where){
		int row = 0;
		row = danceDao.update(map, where);
		return row;
	}
	
}
