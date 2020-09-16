package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.DanceService;

/**
 * Servlet implementation class StartDanceServlet
 */
@WebServlet("/StartDanceServlet")
public class StartDanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StartDanceServlet() {
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
		String leaderAccount = request.getParameter("leaderAccount");
		
		Map<String,String> map = new HashMap<>();
		map.put("state", 1+"");
		
		Map<String,String> where = new HashMap<>();
		where.put("EMGroupId", EMGroupId);
		where.put("leaderAccount", leaderAccount);
		
		DanceService service = new DanceService();
		int row = service.doUpdate(map, where);
		if(row > 0){
			response.getWriter().append("OK").flush();
		}else{
			response.getWriter().flush();
		}
	}

}
