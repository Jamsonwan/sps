package com.qut.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qut.dao.VideoDao;

public class VideoService {

	public final List<Map<String, String>> doSearch() {
		List<Map<String, String>> list = null;
		VideoDao dao = new VideoDao();
		list = dao.search(null);
		return list;
	}

	public final List<Map<String, String>> doSearchByInId(String s) {
		List<Map<String, String>> list = null;
		VideoDao dao = new VideoDao();
		Map<String, String> where = new HashMap<String, String>();
		where.put("id", s);
		list = dao.search(where);
		return list;
	}

	public final List<Map<String, String>> doSearchByType(String type) {
		List<Map<String, String>> list = null;
		VideoDao dao = new VideoDao();
		Map<String, String> where = new HashMap<String, String>();
		where.put("type", type);
		list = dao.search(where);
		return list;
	}

}
