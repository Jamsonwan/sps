package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.DateDao;

public class DateService {
	
	private DateDao dateDao;
	public DateService() {
		this.dateDao=new DateDao();
	}
	
	public List<Map<String,String>> mySearch(String sql,List<String> params){
		List<Map<String, String>>list = null;
		try {
			list= dateDao.executeQuery(sql, params);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public int doInsert(Map<String, String[]> params) {
		int row=0;
		Map<String, String> map=ConvertUtil.convertMap(params);
		try {
			row=dateDao.insert(map);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return row;
	}
	
}
