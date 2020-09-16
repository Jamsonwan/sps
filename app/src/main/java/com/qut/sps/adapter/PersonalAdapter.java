package com.qut.sps.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.qut.sps.R;

import java.util.List;

/**
 * Created by 潘攀 on 2017/8/6.
 */

public class PersonalAdapter extends ArrayAdapter<PersonalData> {
    private int resourceId;

    public PersonalAdapter(Context context, int textViewResourceId, List<PersonalData>objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PersonalData personalData=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView name = view.findViewById(R.id.name);
        TextView data = view.findViewById(R.id.data);
        name.setText(personalData.getName());
        data.setText(personalData.getData());

        return view;
    }

}
