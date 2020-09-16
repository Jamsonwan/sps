package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.dao.MyDao;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class ForgetPwd
 */
@WebServlet("/ForgetPwdServlet")
public class ForgetPwdServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ForgetPwdServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		
		Map<String, String> map = new HashMap<>();
		Map<String, String> where = new HashMap<>();
		Map<String, String> map1 = new HashMap<>();
		
		String pwd = request.getParameter("pwd");
		String account = request.getParameter("account");

		map.put("pwd", pwd);
		where.put("account", account);
		MyDao usersDao = new MyDao("users_info");

		int row = usersDao.update(map, where);

		if (row > 0) {
			map1.put("message", "修改成功");
		} else {
			map1.put("message", "修改失败");
		}
	
		response.setContentType("text/html; charset=utf-8");
		String resp = JSONObject.fromObject(map1).toString();
		response.getWriter().append(resp).flush();
	}

}
