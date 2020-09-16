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

import com.qut.service.GroupInfoService;

import net.sf.json.JSONArray;

/**
 * Servlet implementation class GetGroupMemberListServlet
 */
@WebServlet("/GetGroupMemberListServlet")
public class GetGroupMemberListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetGroupMemberListServlet() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		
		String groupId = request.getParameter("groupId");
		List<String> params = new ArrayList<>();
		params.add(groupId);
		
		String sql = "select users_info.iconUrl iconUrl,nickName,account,memberNote,memberId from "
				+"users_info,group_info where users_info.id = group_info.memberId and groupId = ?";
		GroupInfoService service = new GroupInfoService();
		
		List<Map<String,String>> resultList = new ArrayList<>();
		List<Map<String,String>> list = service.doSearchBySql(sql, params);
		Map<String,String> result;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map : list){
				result = new HashMap<>();
				result.put("memberId", map.get("memberid"));
				result.put("iconUrl", map.get("iconurl"));
				if(null != map.get("membernote")){
					result.put("name", map.get("membernote"));
				}else if(null != map.get("nickname")){
					result.put("name", map.get("nickname"));
				}else{
					result.put("name", map.get("account"));
				}
				resultList.add(result);
			}
		}
		if(resultList.size() > 0){
			response.getWriter().append(JSONArray.fromObject(resultList).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
