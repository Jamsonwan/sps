package com.qut.servlet;

import java.io.File;
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.qut.dao.MyDao;
import com.qut.service.TimerDelete;

/**
 * Servlet implementation class CompetitionCreat
 */
@WebServlet("/CompetitionCreatServlet")
public class CompetitionCreatSrevlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CompetitionCreatSrevlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		//璁剧疆瀹㈡埛鏈虹殑缂栫爜鏂瑰紡锛屼慨鏀� response 涓殑缂栫爜鏂瑰紡
		response.setContentType("text/html;charset=utf-8");
		

		String message = "";
		
		Map<String, String> params = new HashMap<>();
		try {
			DiskFileItemFactory dff = new DiskFileItemFactory();
			ServletFileUpload sfu = new ServletFileUpload(dff);
			sfu.setHeaderEncoding("UTF-8");
			List<FileItem> items = sfu.parseRequest(request);

			// 鑾峰彇涓婁紶瀛楁

			for (FileItem fileItem : items) {
		
				if (fileItem.isFormField()) {
					String values = new String(fileItem.getString().getBytes("ISO8859_1"), "utf-8");
					params.put(fileItem.getFieldName(), values);
				} else {// 鍥剧墖
						// 鏇存敼鏂囦欢鍚嶄负鍞竴鐨�
					String filename = fileItem.getName();
					if (filename != null) {
						filename = System.currentTimeMillis() + ".png";
					}
					// 鐢熸垚瀛樺偍璺緞
					String storeDirectory = "C://spsSource/competitionPoster";
					File file = new File(storeDirectory);
					if (!file.exists()) {
						file.mkdir();
					}
					// 澶勭悊鏂囦欢鐨勪笂浼�
					try {
						fileItem.write(new File(storeDirectory + "/", filename));
						params.put("imageUrl", filename);
					} catch (Exception e) {
						message = e.getMessage();
					}
				}
			}

		} catch (Exception e) {
			message = e.getMessage();
		}

		if (message.equals("")) {
			MyDao competitionDao = new MyDao("competitions");
			Map<String, String> map = new HashMap<>();
			List<Map<String, String>> list = new ArrayList<>();
			map.put("competitionName", params.get("competitionName"));
			list = competitionDao.search(map);
			if (list.isEmpty() && list.size() == 0) {
				int row = 0;
				try{
					row =  competitionDao.insert(params);
				}catch(Exception e){
					message = e.getMessage();
				}
				if(row > 0){
					message = "上传成功";
				}
				TimerDelete timer = new TimerDelete(params.get("competitionName"), params.get("startTime"));
				timer.start();
			} else {
				message = "上传失败";
			}
		}
		
		response.getWriter().append(message).flush();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request, response);
	}

}
