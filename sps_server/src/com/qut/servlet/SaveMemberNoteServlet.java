package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.GroupInfoService;
import com.qut.service.GroupsService;

/**
 * Servlet implementation class SaveMemberNoteServlet
 */
@WebServlet("/SaveMemberNoteServlet")
public class SaveMemberNoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveMemberNoteServlet() {
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
		response.setContentType("text/html;charset=utf-8");
		
		String groupId = request.getParameter("groupId");
		String userId = request.getParameter("memberId");
		String userNote = request.getParameter("memberNote");
		
		Map<String,String> where;
		
		where = new HashMap<>();
		where.put("id", groupId);
		where.put("userId", userId);
		
		Map<String,String> map ;
		map = new HashMap<>();
		map.put("userNote", userNote);
		
		GroupsService service = new GroupsService();
		GroupInfoService service2 = new GroupInfoService();
		
		int row = service.doUpdate(map, where);
		
		String result = null;
		
		if(row > 0){
			result = "ok";
		}else{
			where = new HashMap<>();
			where.put("groupId", groupId);
			where.put("memberId", userId);
			
			map = new HashMap<>();
			map.put("memberNote", userNote);
			
			int row1 = service2.doUpdate(map, where);
			if(row1 > 0){
				result = "ok";
			}
		}
		
		if(null != result){
			response.getWriter().append(result).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
