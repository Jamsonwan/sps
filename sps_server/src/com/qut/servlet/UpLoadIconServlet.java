package com.qut.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


import com.qut.service.UsersInfoService;

import net.sf.json.JSONObject;





/**
 * Servlet implementation class UpLoadIconServlet
 */
@WebServlet("/UpLoadIconServlet")
public class UpLoadIconServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpLoadIconServlet() {
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
		
		Map<String,String> where=new HashMap<>();
		Map<String,String> map1=new HashMap<>();
		Map<String,String> map=new HashMap<>();

		String message="";
		
		try {
			
			DiskFileItemFactory dff = new DiskFileItemFactory();
			ServletFileUpload sfu = new ServletFileUpload(dff);
			List<FileItem> items = sfu.parseRequest(request);
		
			for(FileItem fileItem:items){
			
				if(fileItem.isFormField()){
				    where.put(fileItem.getFieldName(), fileItem.getString());
				}else{
				
			        String filename = fileItem.getName();
			        if (filename != null) {
			        	filename = System.currentTimeMillis()+".png";
			        }
			      
			        String storeDirectory = "C://spsSource/usersIcon";
			        File file = new File(storeDirectory);
			        if (!file.exists()) {
			        	file.mkdir();
			        }
			        String path = filename;
			       
			        try {
			        	fileItem.write(new File(storeDirectory+"/", filename));
			        	map1.put("iconUrl", path);
			        	message = path;
			        } catch (Exception e) {
			        	message = e.getMessage();
			        	e.printStackTrace();
			        	
			        }
				}
			}
	
			UsersInfoService infoService=new UsersInfoService();
			
			int row= infoService.doUpdate(map1, where);
			
			if(row> 0){	
				map.put("result",message);
			}else{
				map.put("result", "Error");
			}
			String resp=JSONObject.fromObject(map).toString();
			response.getWriter().append(resp).flush();
		} catch (Exception e) {
			message = e.getMessage();
			response.getWriter().flush();
		}
	}

}
