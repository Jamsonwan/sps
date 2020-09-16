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

import com.qut.service.GroupsService;

import net.sf.json.JSONArray;

/**
 * Servlet implementation class QueryMyJoinedTeamServlet
 */
@WebServlet("/QueryMyJoinedTeamServlet")
public class QueryMyJoinedTeamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryMyJoinedTeamServlet() {
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
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		String userId = request.getParameter("userId");
		List<String> params = new ArrayList<>();
		params.add(userId);
		
		String sql = "select groupId,iconUrl,groupName,description,EMGroupId from "
				+"groups,group_info where groups.id = groupId and memberId = ?";
		GroupsService service = new GroupsService();
		
		List<Map<String,String>> list = service.doSearchBySql(sql, params);
		List<Map<String,String>> resultList = new ArrayList<>();
		Map<String,String> result;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				result = new HashMap<>();
				result.put("groupId", map.get("groupid"));
				result.put("iconUrl", map.get("iconurl"));
				result.put("name", map.get("groupname"));
				result.put("description", map.get("description"));
				result.put("EMGroupId", map.get("emgroupid"));
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
