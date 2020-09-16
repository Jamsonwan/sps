package com.qut.service;

import java.util.List;
import java.util.Map;

import com.neu.util.ConvertUtil;
import com.qut.dao.MusicDao;

public class MusicService {
	
	MusicDao musicDao;
	
	public MusicService(){
		this.musicDao = new MusicDao();
	}
	
	public List<Map<String,String>> doSearch(Map<String,String[]> params){
		List<Map<String,String>> list = null;
		
		Map<String,String> where = ConvertUtil.convertMap(params);
		list = musicDao.search(where);
		
		return list;
	}
}
