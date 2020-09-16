package com.qut.sps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lenovo on 2017/8/5.
 */

public class FriendsAndTeamAdapter extends RecyclerView.Adapter<FriendsAndTeamAdapter.BaseViewHolder>{

    private List<Map<String,String>> friendsAndTeamList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private Context mContext;
    public boolean flag = false;
    public boolean isChecked = false;
    /**
     * 需要传入map类型的list集合
     * @param friendsAndTeamList
     */
    public FriendsAndTeamAdapter(List<Map<String,String >> friendsAndTeamList){
        this.friendsAndTeamList = friendsAndTeamList;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_team_item,parent,false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        Map<String,String> map = friendsAndTeamList.get(position);
        String iconUri = "";//头像路径
        String friendAndTeamName="";//昵称或者备注
        String id = "";
        for (Map.Entry<String ,String> entry:map.entrySet()){
            if (entry.getKey().equals("iconUrl")){
                iconUri = entry.getValue();
            }else if(entry.getKey().equals("name")){
                friendAndTeamName = entry.getValue();
            }else if(entry.getKey().equals("id")) {
                id = entry.getValue();
            }
        }
        final String Id = id;
        iconUri = HttpUtil.SPS_SOURCE_URL + iconUri;

        Glide.with(mContext).load(iconUri).into(holder.friendsAndTeamIcon);
        holder.friendsAndTeamName.setText(friendAndTeamName);
        holder.setIsRecyclable(false);
        //点击事件
        holder.clickFriendsAndTeamView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClick(holder.clickFriendsAndTeamView,
                        holder.getLayoutPosition(),Id);
            }
        });
        if(flag){
            holder.checkBox.setVisibility(View.VISIBLE);
        }else{
            holder.checkBox.setVisibility(View.GONE);
        }
        holder.checkBox.setChecked(false);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isChecked = !isChecked;
                onItemClickListener.OnItemCheckClick(holder.checkBox,holder.getLayoutPosition(),isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsAndTeamList.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView friendsAndTeamIcon;
        public TextView friendsAndTeamName;
        public LinearLayout clickFriendsAndTeamView;
        public CheckBox checkBox;
        public BaseViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_box);
            friendsAndTeamIcon = itemView.findViewById(R.id.friend_team_icon);
            friendsAndTeamName = itemView.findViewById(R.id.friend_team_name);
            clickFriendsAndTeamView = itemView.findViewById(R.id.click_friend_team);
        }
    }

    public interface OnItemClickListener{
        void OnItemClick(View view,int position,String id);
        void OnItemCheckClick(View view,int position,boolean isChecked);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}