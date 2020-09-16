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

import com.qut.dao.GroupsDao;
import com.qut.service.GroupInfoService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetMemberNoteServlet
 */
@WebServlet("/GetMemberNoteServlet")
public class GetMemberNoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMemberNoteServlet() {
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
		GroupInfoService service = new GroupInfoService();
		
		String groupId = request.getParameter("groupId");
		String userId = request.getParameter("memberId");
		Map<String,String> buildMap = new HashMap<>();
		
		buildMap.put("id", groupId);
		buildMap.put("userId", userId);
		
		List<Map<String,String>> list = null;
		list = service.doSearch(params);
		Map<String,String> memberNote = null;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map : list){
				if(null == map.get("membernote")){
					continue;
				}
				memberNote = new HashMap<>();
				memberNote.put("memberNote", map.get("membernote"));
			}
		}else{
			GroupsDao groupsService = new GroupsDao();
			list = groupsService.search(buildMap);
			if(null != list && list.size() > 0){
				for(Map<String,String> map1 : list){
					if(null == map1.get("usernote")){
						continue;
					}
					memberNote = new HashMap<>();
					memberNote.put("memberNote", map1.get("usernote"));
				}
			}
		}
		
		if(null != memberNote){
			response.getWriter().append(JSONObject.fromObject(memberNote).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
