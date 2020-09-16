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

import com.neu.util.ConvertUtil;
import com.qut.service.VideoService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class RefreshServlet
 */
@WebServlet("/RefreshServlet")
public class RefreshServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
	Map<String, String> param;

	List<Map<String, String>> result;
	
	 /**
     * @see HttpServlet#HttpServlet()
     */
    public RefreshServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		Map<String, String> map = new HashMap<String, String>();
		map = ConvertUtil.convertMap(request.getParameterMap());
		String signal = map.get("signal");

		if (signal.equals("refresh_image")) {
			handleForRefreshImage(response);
		}else{
			handleForFailure(response);
		}
	}
	
	private void handleForFailure(HttpServletResponse response) {
		param = new HashMap<String, String>();
		param.put("result", "request failed");
		String responseStr = JSONObject.fromObject(param).toString();
		response.setContentType("text/html;charset=UTF-8");
		try {
			response.getWriter().append(responseStr).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleForRefreshImage(HttpServletResponse response) {
		result = new ArrayList<>();
		VideoService service = new VideoService();
		String s = "1,2,3,4";
		List<Map<String, String>> list = service.doSearchByInId(s);
		String id;
		String videoName;
		String imageUrl;
		for (Map<String, String> map1 : list) {
			param = new HashMap<String, String>();
			imageUrl = map1.get("imageurl");
			videoName = map1.get("videoname");
			id = map1.get("id");
			param.put("imageUrl", imageUrl);
			param.put("videoName", videoName);
			param.put("id", id);
			result.add(param);
		}
		System.out.println(result);
		if (result != null && result.size() > 0) {
			String responseStr = JSONArray.fromObject(result).toString();
			response.setContentType("text/html;charset=UTF-8");
			try {
				response.getWriter().append(responseStr).flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				response.getWriter().flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
