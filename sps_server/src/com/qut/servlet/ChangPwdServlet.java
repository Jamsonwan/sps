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
 * Servlet implementation class AlterPasswordServlet
 */
@WebServlet("/ChangPwdServlet")
public class ChangPwdServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangPwdServlet() {
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
		String pwd = request.getParameter("pwd");
		String id = request.getParameter("id");
		
		map.put("pwd", pwd);
		where.put("id", id);
		
		UsersInfoService service = new UsersInfoService();
		
		int row = service.doUpdate(map, where);
		
		if(row > 0) {
			map1.put("result", "�޸ĳɹ�");
		}else {
			map1.put("result", "�޸�ʧ��");
		}
		
		String resp=JSONObject.fromObject(map1).toString();
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().append(resp).flush();
		
	}

}
