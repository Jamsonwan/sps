package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.qut.service.Sms;

import net.sf.json.JSONObject;

/**
 * 鑾峰彇楠岃瘉鐮侊紝浼犲叆鍊间负鎵嬫満鍙风爜锛岃繑鍥炲�间负楠岃瘉鐮佸彂閫佹槸鍚︽垚鍔熶互鍙婃墍鍙戦�佺殑楠岃瘉鐮�
 * 
 */
@WebServlet("/GetCodeServlet")
public class GetCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetCodeServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		int number = (int) (Math.random() * 9000 + 1000);
		String code = "{\"code\":" + number + "}";
		SendSmsResponse response1 = null;
		Map<String, String> map = new HashMap<>();
		String phoneNumber = request.getParameter("phoneNumber");
		// String phoneNumber = "17854271103";
		try {
			response1 = Sms.sendSms(phoneNumber, code);
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(response1.getCode());
		if ("OK".equals(response1.getMessage())) {
			map.put("code", number + "");
			map.put("message", "发送成功");
		} else {
			map.put("code", "400");
			map.put("message", "发送失败");
		}
		System.out.println(number);
		response.setContentType("text/html; charset=utf-8");
		String str = JSONObject.fromObject(map).toString();
		response.getWriter().append(str).flush();
	}

}
