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
 * Servlet implementation class GetLeaderInfoServlet
 */
@WebServlet("/GetLeaderInfoServlet")
public class GetLeaderInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetLeaderInfoServlet() {
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
		
		Map<String,String[]> params = request.getParameterMap();
		List<Map<String,String>> list;
		DanceService service = new DanceService();
		
		list = service.doSearch(params);
		Map<String,String> result = new HashMap<>();
		String leaderAccount = null;
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				if(null != map.get("leaderaccount")){
					result.put("leaderAccount", map.get("leaderaccount"));
					leaderAccount = map.get("leaderaccount");
				}
			}
		}
		
		if(null != leaderAccount){
			UsersInfoService userInfoService = new UsersInfoService();
			String sql = "select iconUrl,nickName,account from users_info where account = ?";
			List<String> where = new ArrayList<>();
			where.add(leaderAccount);
			
			list = userInfoService.doSearchBySql(sql, where);
			if(null != list && list.size() > 0){
				for(Map<String,String> map1:list){
					result.put("iconUrl", map1.get("iconurl"));
					if(null != map1.get("nickname")){
						result.put("name", map1.get("nickname"));
					}else{
						result.put("name", map1.get("account"));
					}
				}
			}
			response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
