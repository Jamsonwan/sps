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

import com.qut.dao.MyFriendsDao;
import com.qut.service.UsersInfoService;

/**
 * Servlet implementation class AddNewFriendServlet
 */
@WebServlet("/AddNewFriendServlet")
public class AddNewFriendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddNewFriendServlet() {
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
		
		String userId = request.getParameter("userId");
		String account = request.getParameter("friendAccount");
		
		List<String> where = new ArrayList<>();
		where.add(account);
		String sql = "select id from users_info where account = ?";
		UsersInfoService infoService = new UsersInfoService();
		
		List<Map<String,String>> list = infoService.doSearchBySql(sql, where);
		String friendId = null;
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				friendId = map.get("id");
			}
			if(null != friendId){
				MyFriendsDao service = new MyFriendsDao();
				Map<String,String> map;
				map = new HashMap<>();
				map.put("userId", userId);
				map.put("friendId", friendId);
				int row = service.insert(map);
				map = new HashMap<>();
				map.put("userId", friendId);
				map.put("friendId", userId);
				int row1 = service.insert(map);
				if(row > 0 && row1 > 0){
					response.getWriter().append("OK").flush();
				}else{
					response.getWriter().append("ERROR3").flush();
				}
			}else{
				response.getWriter().append("ERROR2").flush();
			}
		}else{
			response.getWriter().append("ERROR1").flush();
		}
	}

}
