package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.GroupsService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetOneGroupInfoServlet
 */
@WebServlet("/GetOneGroupInfoServlet")
public class GetOneGroupInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetOneGroupInfoServlet() {
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
		
		Map<String,String[]> params = request.getParameterMap();
		GroupsService service = new GroupsService();
		List<Map<String,String>> list = service.doSearch(params);
		
		Map<String,String> result= null;
		if(null != list && list.size() > 0){
			result = new HashMap<>();
			for(Map<String,String> map:list){
				result.put("groupId", map.get("id"));
				result.put("iconUrl", map.get("iconurl"));
				result.put("description", map.get("description"));
				result.put("EMGroupId", map.get("emgroupid"));
				result.put("groupName", map.get("groupname"));
				result.put("userNote", map.get("usernote"));
				result.put("userId", map.get("userid"));
			}
		}
		if(null != result && result.size() > 0){
			response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
