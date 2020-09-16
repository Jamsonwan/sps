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

import com.qut.dao.GroupsDao;


/**
 * Servlet implementation class UpLoadFileServlet
 */
@WebServlet("/UpLoadFileServlet")
public class UpLoadFileServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    public UpLoadFileServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=utf-8");
 
        request.setCharacterEncoding("utf-8");
        Map<String,String> params = new HashMap<>();
        String message = "";
        String iconUrl = "";
        
		try{
			DiskFileItemFactory dff = new DiskFileItemFactory();
			ServletFileUpload sfu = new ServletFileUpload(dff);
			List<FileItem> items = sfu.parseRequest(request);
			// 
			
			for(FileItem fileItem:items){
				//
				if(fileItem.isFormField()){
				    params.put(fileItem.getFieldName(), fileItem.getString());
					//iconUrl = fileItem.getString();
				}else{//
					// 
			        String filename = fileItem.getName();
			        if (filename != null) {
			        	filename = System.currentTimeMillis()+".png";
			        }
			        // 文件的保存路径
			        String storeDirectory = "C://spsSource/usersIcon";
			        File file = new File(storeDirectory);
			        if (!file.exists()) {
			        	file.mkdir();
			        }
			        String path = storeDirectory +"/"+filename;
			        // 
			        try {
			        	fileItem.write(new File(storeDirectory+"/", filename));
			        	params.put("iconUrl", filename);
			        	message = path;
			        } catch (Exception e) {
			        	message = e.getMessage();
			        }
				}
			}
			GroupsDao service = new GroupsDao();
	
			int row = service.insert(params);
			if(row> 0){
				
			}else{
				
			}
		} catch (Exception e) {
			message = e.getMessage();
		} finally {
			response.getWriter().append(message).append(iconUrl).flush();
		}
	}
}
