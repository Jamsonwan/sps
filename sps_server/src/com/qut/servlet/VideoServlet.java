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
 * Servlet implementation class VideoServlet
 */
@WebServlet(description = "������Ƶ����", urlPatterns = { "/VideoServlet" })
public class VideoServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	Map<String, String> param;

	List<Map<String, String>> result;
	
	String id;
	
	String s;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VideoServlet() {
		super();
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
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		Map<String, String> map = new HashMap<String, String>();
		map = ConvertUtil.convertMap(request.getParameterMap());
		String signal = map.get("signal");
		id = map.get("id");

		if(signal.equals("video")) {
			handleForVideo(response);
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
	
	private void handleForVideo(HttpServletResponse response) {
		result = new ArrayList<>();
		VideoService service = new VideoService();
		List<Map<String, String>> list = service.doSearchByInId(id);
		int id1 = Integer.parseInt(id);
		int id2;
		int id3;
		if(id1<=40) {
		id2 = id1+1;
		id3 = id2+1;
		s = id2+","+id3;
		}else {
			id2 = id1-1;
			id3 = id1-2;
			s = id2+","+id3;
		}
	    System.out.println(s);
		List<Map<String, String>> list1 = service.doSearchByInId(s);
		String videoUrl;
		String description;
		for (Map<String, String> map1 : list) {
			param = new HashMap<String, String>();
			videoUrl = map1.get("videourl");
			description = map1.get("description");
			param.put("videoUrl", videoUrl);
			param.put("description", description);
			result.add(param);
		}
		String id;
		String videoName;
		String imageUrl;
		for (Map<String, String> map1 : list1) {
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
		if (result != null&&result.size()>0) {
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
