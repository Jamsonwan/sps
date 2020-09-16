package com.qut.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import com.qut.dao.UsersInfoDao;
import com.qut.service.GroupsService;

/**
 * Servlet implementation class SearchGroupServlet
 */
@WebServlet("/SearchGroupServlet")
public class SearchGroupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchGroupServlet() {
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
		Map<String, String[]> params=request.getParameterMap();
		
		GroupsService service = new GroupsService();
		List<Map<String,String>> list = service.doSearch(params);
		

		response.setContentType("text/html;charset=utf-8");
		if(null != list && list.size() > 0){
			UsersInfoDao usersInfoDao = new UsersInfoDao();
			String name =list.get(0).get("userid");
			System.out.println(name);
			List<Map<String,String>> list_t =  usersInfoDao.findById(name);
			//保存团队搜索结果
			Map<String,String> map = list.get(0);
			//添加团长昵称
			map.put("username",list_t.get(0).get("nickname"));
			//删除原来的结果
			list.remove(0);
			//添加新结果
			list.add(map);
			String result = JSONArray.fromObject(list).toString();
			response.getWriter().append(result).flush();
		}
		else{
			response.getWriter().flush();
		}
	}

}
