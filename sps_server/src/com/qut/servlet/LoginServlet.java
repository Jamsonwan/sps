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
 * Servlet implementation class LoginandRegisterServlet
 */
@WebServlet("/LoginServlet")
/**
 * 娉ㄥ唽鐨剆ervlet 鎺ユ敹鐨勪负APP绔紶杩囨潵鐨勮处鍙峰拰瀵嗙爜 锛岃繑鍥炲�间负鐧诲綍鏄惁鎴愬姛
 * 
 * @author 13686
 *
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String account = request.getParameter("account");
		String pwd = request.getParameter("pwd");
		String message = null;
		String code = null;
		
		Map<String, String> map = new HashMap<>();
		List<Map<String, String>> list = new ArrayList<>();
		map.put("account", account);
		MyDao usersDao = new MyDao("users_info");
		list = usersDao.search(map);
		if (list != null && !list.isEmpty()) {
			for (Map<String, String> map2 : list) {
				if (pwd.equals(map2.get("pwd"))) {
					code = "200";
					message = map2.get("id");
				} else {
					code = "400";
					message = "密码错误";
				}
			}
		} else {
			code = "401";
			message = "该账号尚未注册";
		}
		map.put("code", code);
		map.put("message", message);
		
		response.setContentType("text/html; charset=utf-8");
		String str = JSONObject.fromObject(map).toString();
		response.getWriter().append(str).flush();
	}

}
