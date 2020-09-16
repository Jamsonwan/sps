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

import com.qut.service.DanceService;

import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetMusicAndControlServlet
 */
@WebServlet("/GetMusicAndControlServlet")
public class GetMusicAndControlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMusicAndControlServlet() {
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
		
		String EMGroupId = request.getParameter("EMGroupId");
		String leaderAccount = request.getParameter("leaderAccount");
		
		List<String> where = new ArrayList<>();
		where.add(EMGroupId);
		where.add(leaderAccount);
		
		String sql = "select dance.musicUrl musicUrl,musicName,singer,control from dance,music "
				+"where dance.musicUrl = music.musicUrl and EMGroupId = ? and leaderAccount = ?";
		
		
		DanceService service = new DanceService();
		List<Map<String,String>> list = service.doSearchBySql(sql,where);
		
		Map<String,String> result = new HashMap<>();
		if(null != list && list.size() > 0){
			for(Map<String,String> map:list){
				result.put("musicUrl",map.get("musicurl"));
				result.put("control", map.get("control"));
				result.put("singer", map.get("singer"));
				result.put("musicName", map.get("musicname"));
			}
		}
		if(result.size() > 0){
			response.getWriter().append(JSONObject.fromObject(result).toString()).flush();
		}else{
			response.getWriter().flush();
		}
	}

}
