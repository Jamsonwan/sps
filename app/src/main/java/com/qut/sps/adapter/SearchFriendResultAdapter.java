package com.qut.sps.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.aty.ShowAddFriendActivity;
import com.qut.sps.util.HttpUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by nyy on 2017/8/11.
 */

public class SearchFriendResultAdapter extends RecyclerView.Adapter<SearchFriendResultAdapter.ViewHolder> {

    private List<Map<String,String>> maps;
    private Context mContext;
    static class ViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        CircleImageView icon;
        TextView name;

        public ViewHolder(View view){
            super(view);
            itemView = view;
            icon = view.findViewById(R.id.y_friend_icon);
            name = view.findViewById(R.id.y_friend_name);
        }
    }
    public SearchFriendResultAdapter(List<Map<String,String>> mapList){
        maps = mapList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_friend_item,parent,false);
        this.mContext = parent.getContext();
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Map<String,String> map = maps.get(position);
                Intent intent = new Intent(view.getContext(), ShowAddFriendActivity.class);
                intent.putExtra("info",(Serializable)map);
                view.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String,String>  map = maps.get(position);

       String url= HttpUtil.SPS_SOURCE_URL+"usersIcon/"+map.get("头像");
        Glide.with(mContext).load(url).into(holder.icon);
        holder.name.setText(map.get("昵称")+"("+map.get("账号")+")");
    }

    @Override
    public int getItemCount() {
        return maps.size();
    }

}
