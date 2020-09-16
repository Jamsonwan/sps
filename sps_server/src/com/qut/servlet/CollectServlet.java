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
import com.qut.service.CollectVideoService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class CollectServlet
 */
@WebServlet("/CollectServlet" )
public class CollectServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	CollectVideoService service;
	
	Map<String, String> result;
	
    Map<String,String> map;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CollectServlet() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		    request.setCharacterEncoding("UTF-8");
	        
		    map = new HashMap<String,String>();
	 	    map = ConvertUtil.convertMap(request.getParameterMap());
	 	   
			String signal = map.get("signal");
			
            if(signal.equals("addcollection")) {
				handleForAddCollection(response);
			}else if(signal.equals("deletecollection")){
				handleForDeleteCollection(response);
			}else if(signal.equals("collect")){
				handleForCollect(response);
			}else{
				handleForFailure(response);
			}
	}
	
	private void handleForFailure(HttpServletResponse response) {
		result = new HashMap<String, String>();
		result.put("result", "request failed");
		String responseStr = JSONObject.fromObject(result).toString();
		response.setContentType("text/html;charset=UTF-8");
		try {
			response.getWriter().append(responseStr).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void handleForCollect(HttpServletResponse response) {           //锟斤拷示锟斤拷锟斤拷
		 List<Map<String, String>> result = new ArrayList<>();
		 Map<String,String> param;
		 List<String> params = new ArrayList<>();
		 String userId = map.get("userId");
		 params.add(userId);
		 String sql = "select video.id id,videoName,imageUrl,date "+"from video,collect_video "
        		+ "where video.id = collect_video.videoId and collect_video.userId = ?";
		 service = new CollectVideoService();
		 List<Map<String,String>> list = service.doSearchBySql(sql, params);
		 if(list != null&&list.size()>0) {
			 String id;
		     String videoName;
		     String imageUrl;
		     String date;
		     for (Map<String, String> map1 : list) {
			    param = new HashMap<String, String>();
			    date = map1.get("date");
			    imageUrl = map1.get("imageurl");
			    videoName = map1.get("videoname");
			    id = map1.get("id");
			    param.put("date", date);
			    param.put("imageUrl", imageUrl);
			    param.put("videoName", videoName);
			    param.put("id", id);
			    result.add(param);
		     }
		     System.out.println(result);
		 }
		if (result != null&&result.size()>0) {
			String responseStr = JSONArray.fromObject(result).toString();
			response.setContentType("text/html;charset=UTF-8");
			try {
				response.getWriter().append(responseStr).flush();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}else{
			try {
				response.getWriter().flush();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleForDeleteCollection(HttpServletResponse response) {
    	service = new CollectVideoService();
    	result = new HashMap<String,String>();
    	Map<String,String> where = new HashMap<String,String>();
        where.put("userId",map.get("userId"));
 	    where.put("videoId",map.get("videoId"));
 	    
 	    int row = service.doDelete(where);
 	    if(row>0) {
 	        result.put("result", "成功删除");
 	    }else{
 	        result.put("result", "删除失败");
 	    }
 	    
 	    String responseStr = JSONObject.fromObject(result).toString();
 	    response.setContentType("text/html;charset=UTF-8");
 	    try {
			response.getWriter().append(responseStr).flush();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
    private void handleForAddCollection(HttpServletResponse response) {
    	service = new CollectVideoService();
    	result = new HashMap<String,String>();
    	Map<String,String> where = new HashMap<String,String>();
        where.put("userId",map.get("userId"));
 	    where.put("videoId",map.get("videoId"));
 	    List<Map<String,String>> list = service.doSearchForCollect(where);
 	    if(list.size()>0) {
 	    	result.put("result", "该视频已收藏");
 	    }else {
 	    	Map<String,String> map1 = new HashMap<>();
 	    	map1.put("userId", map.get("userId"));
 	    	map1.put("videoId", map.get("videoId"));
 	    	map1.put("date", map.get("date"));
 	    	int row = service.doInsert(map1);
 	        if(row>0) {
 	        	result.put("result", "成功添加至我的收藏");
 	        }else{
 	        	result.put("result", "收藏失败");
 	        }
 	    }
 	    String responseStr = JSONObject.fromObject(result).toString();
 	    response.setContentType("text/html;charset=UTF-8");
 	    try {
			response.getWriter().append(responseStr).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
