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
 * Servlet implementation class JoinDanceServlet
 */
@WebServlet("/JoinDanceServlet")
public class JoinDanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JoinDanceServlet() {
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
		DanceService service  = new DanceService();
		
		String account = request.getParameter("memberAccount");
		String sql = "select id,account,nickName,iconUrl from users_info where"
				+" account = ?";
		List<String> where = new ArrayList<>();
		where.add(account);
		
		List<Map<String,String>> list;
		Map<String,String> result = new HashMap<>();
		
		int row = service.doInsert(params);
		
		if(row > 0){
			UsersInfoService userInfoService = new UsersInfoService();
			list = userInfoService.doSearchBySql(sql, where);
			if(null != list && list.size() > 0){
				for(Map<String,String> map:list){
					result.put("iconUrl", map.get("iconurl"));
					result.put("id", map.get("id"));
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
