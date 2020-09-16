package com.qut.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.qut.dao.MyDao;

public class TimerDelete {

	private String name;

	private String time;

	public TimerDelete(String name, String time) {
		super();
		this.name = name;
		this.time = time;
	}

	public void start() {
		Timer timer = new Timer();
		Date date = new Date();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			date = sdf.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MyDao deleteDao = new MyDao("competitions");
				Map<String, String> map = new HashMap<>();
				map.put("competitionName", name);
				deleteDao.delete(map);
			}
		}, date);
	}

}
