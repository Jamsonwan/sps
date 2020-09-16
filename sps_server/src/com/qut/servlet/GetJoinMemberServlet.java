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

import net.sf.json.JSONArray;

/**
 * Servlet implementation class GetJoinMemberServlet
 */
@WebServlet("/GetJoinMemberServlet")
public class GetJoinMemberServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetJoinMemberServlet() {
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
		
		List<String> params = new ArrayList<>();
		String EMGroupId = request.getParameter("EMGroupId");
		params.add(EMGroupId);
		
		String sql = "select leaderAccount,nickName,account,users_info.iconUrl iconUrl,users_info.id id "
				+"from users_info,dance,groups where users_info.account = dance.leaderAccount or users_info.account"
				+"= dance.memberAccount and groups.EMGroupId = dance.EMGroupId and dance.EMGroupId = ?";
		
		DanceService service = new DanceService();
		List<Map<String,String>> list;
		
		List<Map<String,String>> resultList = new ArrayList<>();
		Map<String,String> result;
		
		list = service.doSearchBySql(sql, params);
		if(null != list && list.size() > 0){
			for(Map<String,String> map : list){
				result = new HashMap<>();
				if(null !=  map.get("leaderaccount")){
					continue;
				}else{
					result.put("iconUrl", map.get("iconurl"));
					result.put("id", map.get("id"));
					if(null != map.get("nickname")){
						result.put("name", map.get("nickname"));
					}else{
						result.put("name", map.get("account"));
					}
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
