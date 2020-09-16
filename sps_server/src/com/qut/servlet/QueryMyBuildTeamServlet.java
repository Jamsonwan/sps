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
 * Servlet implementation class QueryMyBuildTeamServlet
 */
@WebServlet("/QueryMyBuildTeamServlet")
public class QueryMyBuildTeamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryMyBuildTeamServlet() {
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
		Map<String,String[]> params = request.getParameterMap();
		GroupsService service = new GroupsService();
		
		List<Map<String,String>> list = service.doSearch(params);
		List<Map<String,String>> result = new ArrayList<>();
		Map<String,String> responseData;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				responseData = new HashMap<>();
				responseData.put("groupId", map.get("id"));
				responseData.put("iconUrl", map.get("iconurl"));
				responseData.put("name", map.get("groupname"));
				responseData.put("description", map.get("description"));
				responseData.put("EMGroupId", map.get("emgroupid"));
				result.add(responseData);
			}
		}
		if(result.size() > 0){
			response.setContentType("text/html;charset=utf-8");
		    response.getWriter().append(JSONArray.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
