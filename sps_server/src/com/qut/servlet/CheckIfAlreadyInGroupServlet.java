package com.qut.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.GroupInfoService;

/**
 * Servlet implementation class CheckIfAlreadyInGroupServlet
 */
@WebServlet("/CheckIfAlreadyInGroupServlet")
public class CheckIfAlreadyInGroupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckIfAlreadyInGroupServlet() {
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
		
		Map<String,String[]> map = request.getParameterMap();
		

		GroupInfoService service = new GroupInfoService();
		List<Map<String,String>>  list =service.doSearch(map);
		
		boolean flag = list.size() > 0 ? true:false;
		
		response.setContentType("text/html;charset=utf-8");
		if(flag){
			response.getWriter().write("已存在");
		}else{
			response.getWriter().write("不存在");
		}
	}

}
