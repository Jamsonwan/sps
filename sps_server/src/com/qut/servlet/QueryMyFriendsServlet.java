package com.qut.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.MyFriendsService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@WebServlet("/QueryMyFriendsServlet")
public class QueryMyFriendsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    public QueryMyFriendsServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String param = request.getParameter("userId");
		
		List<String> params = new ArrayList<>();
		params.add(param);
		
		MyFriendsService service = new MyFriendsService();
		List<Map<String,String>> list;
		JSONArray resultJson = new JSONArray();
		Map<String,String> map;
		
		String sql = "select * from my_friends,users_info where my_friends.friendId="
				+ "users_info.id and my_friends.userId=?";
		list = service.doSearchBySql(sql, params);
		
		if(null != list && list.size()>0){
			for(Map<String,String> map1:list){
				map = new HashMap<>();
				map.put("friendId", map1.get("friendid"));
				map.put("note", map1.get("note"));
				map.put("nickName", map1.get("nickname"));
				map.put("account", map1.get("account"));
				map.put("iconUrl", map1.get("iconurl"));
				map.put("profession", map1.get("profession"));
				map.put("address", map1.get("address"));
				map.put("sex", map1.get("sex"));
				map.put("tel", map1.get("tel"));
				map.put("age", map1.get("age"));
				resultJson.add(JSONObject.fromObject(map));
			}
		}
	
		if(resultJson.size() > 0){
			String result =resultJson.toString();
			response.setContentType("text/html;charset=utf-8");
		    response.getWriter().append(result).flush();
		}else{
			response.getWriter().flush();
		}
	}
}
