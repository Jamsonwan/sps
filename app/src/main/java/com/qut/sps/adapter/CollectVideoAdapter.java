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

public class CollectVideoAdapter extends RecyclerView.Adapter<CollectVideoAdapter.ViewHolder> {

    public static String deleteId = null;               //要删除的video的Id

    public static boolean isDelete = false;             //判断是否进行删除

    private Context mContext;

    private List<Map<String,String>> mVideoList;

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout linear;
        ImageView videoImage;
        TextView videoName;
        TextView collectDate;
        TextView delete;
        LinearLayout linearTv;

        public ViewHolder(View view){
            super(view);
            linear = (LinearLayout) view;
            videoImage = view.findViewById(R.id.video_image);
            videoName = view.findViewById(R.id.video_name);
            collectDate = view.findViewById(R.id.collect_date);
            delete = view.findViewById(R.id.delete);
            linearTv = view.findViewById(R.id.linear_tv);
        }
    }

    public CollectVideoAdapter(List<Map<String,String>> videoList){
        mVideoList = videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.collect_video,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.linear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                holder.linearTv.setVisibility(View.INVISIBLE);
                int position = holder.getAdapterPosition();
                Map<String,String> map = mVideoList.get(position);
                Intent intent = new Intent(mContext, PlayVideoActivity.class);
                intent.putExtra("id",map.get("id"));
                intent.putExtra("videoName",map.get("videoName"));
                mContext.startActivity(intent);
            }
        });
        holder.linear.setLongClickable(true);
        holder.linear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.linearTv.setVisibility(View.VISIBLE);
                return true;
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.linearTv.setVisibility(View.INVISIBLE);
                int position = holder.getAdapterPosition();
                Map<String,String> map = mVideoList.get(position);
                mVideoList.remove(position);           //从视频列表中删除选中的视频
                deleteId = map.get("id");
                isDelete = true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String,String> map = mVideoList.get(position);
        Glide.with(mContext).load(map.get("imageUrl")).into(holder.videoImage);
        holder.videoName.setText(map.get("videoName"));
        holder.collectDate.setText(map.get("date"));
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }
}
