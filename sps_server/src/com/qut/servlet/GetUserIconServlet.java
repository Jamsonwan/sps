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
 * Servlet implementation class GetUserIconServlet
 */
@WebServlet("/GetUserIconServlet")
public class GetUserIconServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetUserIconServlet() {
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
		String iconUrl = null;
		Map<String,String> result;
		
		if(null != list && list.size() > 0){
			for(Map<String,String> map: list){
				iconUrl = map.get("iconurl");
			}
			if(iconUrl != null){
				result = new HashMap<>();
				iconUrl = "usersIcon/"+iconUrl;
				result.put("iconUrl", iconUrl);
				response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
			}else{
				response.getWriter().flush();
			}
		}else{
			response.getWriter().flush();
		}
	}

}
