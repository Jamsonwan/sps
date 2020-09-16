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

import com.qut.service.DateService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetDateServlet
 */
@WebServlet("/GetDateServlet")
public class GetDateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		
		String param=request.getParameter("memberId");
		List<String> params=new ArrayList<>();
		params.add(param);
		
		DateService dateService=new DateService();
		
		List<Map<String,String>>list;
		JSONArray array=new JSONArray();
		Map<String,String>map;
		String sql = "select distinct groupName,nickName,account,time,place from "
				+"group_info,date,groups,users_info "
				+"where groups.id = date.groupId and "
				+"date.userId = users_info.id and "
				+"groups.id = group_info.groupId and groups.id in ( "
				+"select group_info.groupId from group_info where "
				+"group_info.memberId = ?)";
		list=dateService.mySearch(sql, params);
	
		if(null != list){
			System.out.println(list.size());
			for(Map<String,String>map1:list) {
				map=new HashMap();
				map.put("groupName", map1.get("groupname"));
				map.put("nickName", map1.get("nickname"));
				map.put("account", map1.get("account"));
				map.put("time", map1.get("time"));
				map.put("place",map1.get("place"));
				array.add(JSONObject.fromObject(map));
			}
		}
		list.clear();
		list = null;
		String sql2 = "select distinct groupName,nickName,account,time,place from "
				+"groups,users_info,date where date.userId = users_info.id "
				+" and date.groupId = groups.id "
				+ " and groups.id in (select groups.id from "+
				"groups where groups.userId = ?)";
		list = dateService.mySearch(sql2, params);

		if(null != list){
			System.out.println(list.size());
			for(Map<String,String>map1:list) {
				map=new HashMap();
				map.put("groupName", map1.get("groupname"));
				map.put("nickName", map1.get("nickname"));
				map.put("account", map1.get("account"));
				map.put("time", map1.get("time"));
				map.put("place",map1.get("place"));
				array.add(JSONObject.fromObject(map));
			}
		}
		String result = null;
		if(array.size()>0) {
			result=array.toString();
			response.getWriter().append(result).flush();
		}
		else {
			response.getWriter().flush();
		}
	}
}
