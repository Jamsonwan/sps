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

import net.sf.json.JSONObject;


/**
 * Servlet implementation class GetMemberIconAndNoteServlet
 */
@WebServlet("/GetMemberIconAndNoteServlet")
public class GetMemberIconAndNoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMemberIconAndNoteServlet() {
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
		
		String groupId = request.getParameter("groupId");
		String account = request.getParameter("account");
		
		List<String> params = new ArrayList<>();
		params.add(account);
		params.add(groupId);
		
		String sql = "select iconUrl,nickName,memberNote from users_info,group_info "
				+"where users_info.id = group_info.memberId and users_info.account = ? and "
				+"group_info.groupId = ?";
		GroupInfoService  service = new GroupInfoService();	
		List<Map<String,String>> resultList = service.doSearchBySql(sql, params);
		
		String iconUlr = null;
		String note = null;
		
		if(null != resultList && resultList.size() > 0){
			for(Map<String,String> map : resultList){
				iconUlr = map.get("iconurl");
				if(map.get("membernote") != null){
					note = map.get("membernote");
				}else if(map.get("nickname") != null){
					note = map.get("nickname");
				}else{
					note = account;
				}
			}
		}
		System.out.println(iconUlr);
		if(iconUlr==null){
			
			String sql2 = "select users_info.iconUrl iconUrl,userNote,nickName from users_info,groups "
					+"where users_Info.id = userId and account = ? and groups.id = ?";
			List<Map<String,String>> list = service.doSearchBySql(sql2, params);
			if(null != list && list.size() > 0){
				for(Map<String,String> map1:list){
					iconUlr = map1.get("iconurl");
					if(map1.get("usernote") != null){
						note = map1.get("usernote");
					}else if(map1.get("nickname") != null){
						note = map1.get("nickname");
					}else{
						note = account;
					}
				}
			}
		}
		Map<String,String> result = new HashMap<>();
		result.put("iconUrl", iconUlr);
		result.put("note", note);
		
		if(result.size() > 0){
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
