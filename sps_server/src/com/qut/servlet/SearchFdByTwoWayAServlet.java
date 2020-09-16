package com.qut.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import com.qut.service.UsersInfoService;

/**
 * Servlet implementation class SearchFdByNameServlet
 */
@WebServlet("/SearchFdByTwoWayServlet")
public class SearchFdByTwoWayAServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchFdByTwoWayAServlet() {
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
		Map<String, String[]> params=request.getParameterMap();
		
		UsersInfoService service = new UsersInfoService();
		List<Map<String,String>> list = service.doSearch(params);
		
		response.setContentType("text/html;charset=utf-8");
		
		if(null != list && list.size() > 0){
			String result = JSONArray.fromObject(list).toString();
			response.getWriter().append(result).flush();
		}
		else{
			response.getWriter().flush();
		}
		
		
	}

}
