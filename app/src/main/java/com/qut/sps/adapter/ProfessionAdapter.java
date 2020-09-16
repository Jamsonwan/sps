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
 * Created by 潘攀 on 2017/8/12.
 */
public class ProfessionAdapter extends ArrayAdapter<ProfessionData> {

    private int resourceId;

    public ProfessionAdapter(Context context,int textViewResourceId,List<ProfessionData> objects) {
        super(context, textViewResourceId, objects);
        resourceId=textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ProfessionData professionData=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView profImg = view.findViewById(R.id.prof_img);
        TextView profName = view.findViewById(R.id.prof_name);
        profImg.setImageResource(professionData.getImageId());
        profName.setText(professionData.getName());
        return view;
    }
}

