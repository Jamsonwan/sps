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
 * Servlet implementation class GetGroupOwnerServlet
 */
@WebServlet("/GetGroupOwnerServlet")
public class GetGroupOwnerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetGroupOwnerServlet() {
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
		
		String groupId = request.getParameter("id");
		List<String> params = new ArrayList<>();
		params.add(groupId);
		
		String sql = "select users_info.iconUrl iconUrl,account,nickName,userNote from users_info"
				+",groups where users_info.id = groups.userId and groups.id = ?";
		GroupsService service = new GroupsService();
		
		List<Map<String,String>> list = service.doSearchBySql(sql, params);
		List<Map<String,String>> resultList = new ArrayList<>();
		Map<String,String> result;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map : list){
				result = new HashMap<>();
				result.put("iconUrl", map.get("iconurl"));
				if(null != map.get("usernote")){
					result.put("name", map.get("usernote"));
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
