package com.qut.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.neu.dao.GenerateSQLDao;

public class UsersInfoDao extends GenerateSQLDao {

	public UsersInfoDao() {
		super("users_info");
	}
	
	public List<Map<String,String>> findById(String id){
		List<Map<String,String>> list=null;
		String sql="select * from users_info where id=?";
		List<String> params=new ArrayList<>();
		params.add(id);
		list=super.executeQuery(sql, params);
		return list;
	}

}
