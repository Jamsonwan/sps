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

import com.qut.service.UsersInfoService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetUserInfoServlet
 */
@WebServlet("/GetUserInfoServlet")
public class GetUserInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetUserInfoServlet() {
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
		UsersInfoService service = new UsersInfoService();
		List<Map<String,String>> list = service.doSearch(params);
		Map<String,String> result = null;
		
		if(null != list && list.size() > 0){
			result = new HashMap<>();
			for(Map<String,String> map:list){
				result.put("id", map.get("id"));
				result.put("iconUrl", map.get("iconurl"));
				result.put("nickName", map.get("nickname"));
				result.put("account", map.get("account"));
				result.put("sex", map.get("sex"));
				result.put("age",map.get("age"));
				result.put("tel", map.get("tel"));
				result.put("address", map.get("address"));
				result.put("profession", map.get("profession"));
			}
		}
		if(null != result && result.size() > 0){
			response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
