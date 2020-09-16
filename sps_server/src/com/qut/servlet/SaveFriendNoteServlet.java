package com.qut.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.service.MyFriendsService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class SaveFriendNoteServlet
 */
@WebServlet("/SaveFriendNoteServlet")
public class SaveFriendNoteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveFriendNoteServlet() {
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
		response.setContentType("text/html;charset=utf-8");
		
		String friendId = request.getParameter("friendId");
		String userId = request.getParameter("userId");
		String newNote = request.getParameter("note");
		
		Map<String,String> map = new HashMap<>();
		map.put("note", newNote);
		
		Map<String,String> where = new HashMap<>();
		where.put("friendId", friendId);
		where.put("userId", userId);
		
		MyFriendsService service = new MyFriendsService();
		int row = service.doUpdate(map, where);
		
		Map<String,String> result = new HashMap<>();
		
		if(row > 0){
			result.put("result", "OK");
		}else{
			result.put("result", "Failure");
		}
		response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
	}

}
