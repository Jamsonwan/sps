package com.qut.sps.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.aty.ChatRoomActivity;
import com.qut.sps.aty.GroupChatRoomActivity;
import com.qut.sps.db.NotifyMessage;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.view.MyFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lenovo on 2017/8/23.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Map<String,String>> messageList = new ArrayList<>();
    private Context mContext;

    public MessageAdapter(List<Map<String,String>> messageList){
        this.messageList = messageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Map<String,String> message = messageList.get(position);
        final String id = message.get("id");
        final String type = message.get("type");
        String iconUrl = message.get("iconUrl");
        String content = message.get("content");
        String name = message.get("name");
        final String isRead = message.get("isRead");

        iconUrl = HttpUtil.SPS_SOURCE_URL + iconUrl;
        Glide.with(mContext).load(iconUrl).into(holder.senderIcon);
        holder.messageSenderView.setText(name);
        holder.messageContentView.setText(content);
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRead.equals(NotifyMessage.UNREAD)){
                    upDateNotifyMessage(id);
                }
                if (type.equals(MyFragment.FRIEND_LIST)){
                    ChatRoomActivity.startChatRoomActivity(mContext,id);
                }else {
                    GroupChatRoomActivity.starGroupChatRoomActivity(mContext,id, Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        });
        if (isRead.equals(NotifyMessage.READ)){
            holder.isReadView.setVisibility(View.GONE);
        }else{
            holder.isReadView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新当前消息为已读
     * @param id
     */
    private void upDateNotifyMessage(String id) {
        NotifyMessage message = new NotifyMessage();
        message.setIsRead(true);
        message.updateAll("friendOrGroupId = ?",id);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private RelativeLayout messageLayout;
        private TextView messageSenderView;
        private TextView messageContentView;
        private CircleImageView senderIcon;
        private ImageView isReadView;

        public ViewHolder(View itemView) {
            super(itemView);

            isReadView = itemView.findViewById(R.id.is_read_img);
            messageLayout = itemView.findViewById(R.id.message);
            messageSenderView = itemView.findViewById(R.id.message_sender);
            messageContentView = itemView.findViewById(R.id.message_content);
            senderIcon = itemView.findViewById(R.id.receive_icon);
        }
    }
}
