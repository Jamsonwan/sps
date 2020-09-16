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

import com.qut.service.DanceService;
import com.qut.service.UsersInfoService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class BecomeLeaderServlet
 */
@WebServlet("/BecomeLeaderServlet")
public class BecomeLeaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BecomeLeaderServlet() {
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
		
		String leaderAccount = request.getParameter("leaderAccount");
		Map<String,String> result = new HashMap<>();
		
		Map<String,String[]> params =request.getParameterMap();
		DanceService service = new DanceService();
		
		int row =  service.doInsert(params);
		if(row > 0){
			String sql = "select iconUrl,nickName,account from users_info where account = ?";
			List<String> where = new ArrayList<>();
			where.add(leaderAccount);
			
			UsersInfoService service1 = new UsersInfoService();
			List<Map<String,String>> list = service1.doSearchBySql(sql, where);
			
			if(null != list && list.size() > 0){
				for(Map<String,String> map:list){
					result.put("iconUrl", map.get("iconurl"));
					if(null != map.get("nickname")){
						result.put("name", map.get("nickname"));
					}else{
						result.put("name", map.get("account"));
					}
				}
				response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
			}else{
				response.getWriter().flush();
			}
		}else{
			response.getWriter().flush();
		}
	}

}
