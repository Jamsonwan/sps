package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.UsersInfoService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class ChangeUsersInfoServlet
 */
@WebServlet("/ChangeUsersInfoServlet")
public class ChangeUsersInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangeUsersInfoServlet() {
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
		Map<String,String> map = new HashMap<>();
		Map<String,String> where = new HashMap<>();
		Map<String,String> map1 = new HashMap<>();
		
		String id = request.getParameter("id");
		String nickName=request.getParameter("nickName");
		String sex=request.getParameter("sex");
		String age=request.getParameter("age");
		String tel=request.getParameter("tel");
		String address=request.getParameter("address");
		String prof=request.getParameter("profession");
		
		UsersInfoService service = new UsersInfoService();
		map.put("nickName", nickName);
		map.put("sex", sex);
		map.put("age", age);
		map.put("tel", tel);
		map.put("address", address);
		map.put("profession", prof);
		where.put("id", id);
        int row = service.doUpdate(map, where);
		if(row>0) {
			map1.put("result", "修改成功");
		}else {
			map1.put("result", "修改失败");
		}
		String resp=JSONObject.fromObject(map1).toString();
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().append(resp).flush();
	}

}
