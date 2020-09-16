package com.qut.sps.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.aty.PlayVideoActivity;

import java.util.List;
import java.util.Map;

import static com.qut.sps.util.MyApplication.getContext;

/**
 * Created by 梅茹 on 2017/8/8.
 */

public class CardVideoAdapter extends RecyclerView.Adapter<CardVideoAdapter.ViewHolder> {

    private Context mContext;

    private List<Map<String,String>> mVideoList;

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout linear;
        ImageView videoImage;
        TextView videoName;

        public ViewHolder(View view){
            super(view);
            linear = (LinearLayout) view;
            videoImage = view.findViewById(R.id.video_image);
            videoName = view.findViewById(R.id.video_name);
        }
    }

    public CardVideoAdapter(List<Map<String,String>> videoList){
        mVideoList = videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.card_video_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.linear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Map<String,String> map = mVideoList.get(position);
                Intent intent = new Intent(mContext, PlayVideoActivity.class);
                intent.putExtra("id",map.get("id"));
                intent.putExtra("videoName",map.get("videoName"));
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String,String> map = mVideoList.get(position);
        Glide.with(mContext).load(map.get("imageUrl")).into(holder.videoImage);
        holder.videoName.setText(map.get("videoName"));
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }
}
