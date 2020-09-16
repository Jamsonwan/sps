package com.qut.sps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qut.sps.R;

import java.util.List;

/**
 * Created by 潘攀 on 2017/8/11.
 */

public class DateAdapter extends ArrayAdapter<DateOfData> {
    private int resourceId;
    public DateAdapter(Context context,int textViewResourceId,List<DateOfData> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        DateOfData dateData=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView profImg = view.findViewById(R.id.date_img);
        TextView profName = view.findViewById(R.id.date_name);
        profImg.setImageResource(dateData.getImageId());
        profName.setText(dateData.getName());
        return view;
    }
}

