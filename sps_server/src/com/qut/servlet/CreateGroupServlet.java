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

import net.sf.json.JSONArray;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.qut.dao.GroupsDao;

/**
 * Servlet implementation class CreateGroupServlet
 */
@WebServlet("/CreateGroupServlet")
public class CreateGroupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateGroupServlet() {
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

        Map<String,String> params = new HashMap<>();
        String message=null;
        String result=null;
		try{
			DiskFileItemFactory dff = new DiskFileItemFactory();
		
			ServletFileUpload sfu = new ServletFileUpload(dff);
			List<FileItem> items = sfu.parseRequest(request);
			
			
			for(FileItem fileItem:items){
				
				if(fileItem.isFormField()){
					String values = new String(fileItem.getString().getBytes("ISO8859_1"),"utf-8");
				    params.put(fileItem.getFieldName(), values);
				}else{
					
			        String filename = fileItem.getName();
			        if (filename != null) {
			        	filename = System.currentTimeMillis()+".png";
			        }
			       
			        String storeDirectory = "D://spsSource/groupsIcon";
			        File file = new File(storeDirectory);
			        if (!file.exists()) {
			        	file.mkdir();
			        }
			        try {
			        	
			        	fileItem.write(new File(storeDirectory+"/", filename));
			        	params.put("iconUrl", filename);
			        } catch (Exception e) {
			        	message = "失败";
			        }
				}
			}
			
			GroupsDao groupDao = new GroupsDao();
            
			int row = groupDao.insert(params);
			if(row > 0){
				List<Map<String,String>> list = groupDao.search(params);
				
				result = JSONArray.fromObject(list).toString();			
			}else{
				if(message == null){
					message = "失败";
				}
			}
		} catch (Exception e) {
			message = "失败";
		} finally {
			if(message==null){
				
				response.getWriter().append(result).flush();
			}else{
				
				response.getWriter().append(message).flush();
			}
		}
	}

}
