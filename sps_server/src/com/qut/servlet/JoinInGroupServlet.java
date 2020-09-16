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

import com.qut.dao.GroupInfoDao;
import com.qut.service.GroupsService;
import com.qut.service.UsersInfoService;

/**
 * Servlet implementation class JoinInGroupServlet
 */
@WebServlet("/JoinInGroupServlet")
public class JoinInGroupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JoinInGroupServlet() {
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
		
		String EMGroupId = request.getParameter("EMGroupId");
		String memberAccount = request.getParameter("memberAccount");
		
		List<String> infoList = new ArrayList<>();
		infoList.add(memberAccount);
		
		UsersInfoService userInfoService = new UsersInfoService();
		String userSql = "select id from users_info where account= ?";
		List<Map<String,String>> userInfoList = userInfoService.doSearchBySql(userSql, infoList);
		
		String memberId = null;
		if(null != userInfoList && userInfoList.size() > 0){
			for(Map<String,String> info:userInfoList){
				memberId = info.get("id");
			}
		}
		
		List<String> where = new ArrayList<>();
		where.add(EMGroupId);
		String sql = "select id from groups where EMGroupId = ?";
		GroupsService groupService = new GroupsService();
		List<Map<String,String>> list = groupService.doSearchBySql(sql, where);
		String groupId = null;
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				groupId = map.get("id");
			}
			if(null != groupId && null != memberId){
				GroupInfoDao infoService = new GroupInfoDao();
				Map<String,String> map = new HashMap<>();
				map.put("groupId", groupId);
				map.put("memberId", memberId);
				int row = infoService.insert(map);
				if(row > 0){
					response.getWriter().append("OK").flush();
				}else{
					response.getWriter().append("Error3").flush();
				}
			}else{
				response.getWriter().append("Error2").flush();
			}
		}else{
			response.getWriter().append("Error1").flush();
		}
	}

}
