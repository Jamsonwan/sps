package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.DateService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class InsertDateInfoServlet
 */
@WebServlet("/InsertDateInfoServlet")
public class InsertDateInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertDateInfoServlet() {
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
		DateService dateService=new DateService();
		
		int row=dateService.doInsert(params);
		Map<String,String> map=new HashMap<>();
		if(row>0) {
			map.put("result", "预约成功");
		}else {
			map.put("result", "预约失败");
		}
		String resp=JSONObject.fromObject(map).toString();
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().append(resp).flush();
		
	}

}
