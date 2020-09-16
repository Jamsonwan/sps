package com.qut.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.UsersInfoService;

import net.sf.json.JSONArray;

/**
 * Servlet implementation class GetUsersInforServlet
 */
@WebServlet("/GetUsersInforServlet")
public class GetUsersInforServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetUsersInforServlet() {
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
		Map<String, String[]> params=request.getParameterMap();
		UsersInfoService InfoService=new UsersInfoService();
		List<Map<String, String>> list=InfoService.doSearch(params);
		String resp=JSONArray.fromObject(list).toString();
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().append(resp).flush();
	}

}
