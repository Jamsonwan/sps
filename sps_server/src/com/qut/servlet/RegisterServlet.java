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

import com.qut.dao.MyDao;

import net.sf.json.JSONObject;

/**
 * 娉ㄥ唽鐢� 鎺ユ敹淇℃伅涓虹敤鎴峰悕鍜屽瘑鐮� 杩斿洖鍊间负鎻愮ず淇℃伅锛�
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RegisterServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		request.setCharacterEncoding("UTF-8");
		//
		response.setContentType("text/html;charset=utf-8");
		
		String message = "";
		String code = null;
		String nickName = request.getParameter("nickName");
		String account = request.getParameter("account");
		String pwd = request.getParameter("pwd");
		
		Map<String, String> map = new HashMap<>();
		List<Map<String, String>> list = new ArrayList<>();
		map.put("account", account);
		MyDao registerDao = new MyDao("users_info");
		list = registerDao.search(map);
		if (!(list != null && !list.isEmpty())) {
			if (!pwd.equals("null")) {
				map.put("pwd", pwd);
				map.put("nickName", nickName);
				registerDao.insert(map);
				code = "200";
				message = "注册成功";
			} else {
				code = "400";
				message = "该账号尚未注册";
			}
		} else {
			code = "401";
			message = "该账号已经注册";

		}

		map.clear();
		map.put("code", code);
		map.put("message", message);
	
		
		String str = JSONObject.fromObject(map).toString();
		response.getWriter().append(str).flush();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);

	}
}
