package com.qut.sps.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.db.FriendMessage;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.view.MyFragment;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lenovo on 2017/8/8.
 */

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private List<Map<String,String>> messageList;
    private Context mContext;

    public ChatRoomAdapter(List<Map<String,String>> messageList){
        this.messageList = messageList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
       holder.setIsRecyclable(false);
        Map<String,String> map = messageList.get(position);
        String content="";
        String iconUrl="";
        int type = 0;
        String messageType = "";
        String note = "";
        for (Map.Entry<String,String> entry:map.entrySet()){
            if (entry.getKey().equals("content")){
                content = entry.getValue();
            }else if (entry.getKey().equals("iconUrl")){
                iconUrl = entry.getValue();
            }else if (entry.getKey().equals("type")){
                type = Integer.parseInt(entry.getValue());
            }else if(entry.getKey().equals("messageType")){
                messageType = entry.getValue();
            }else if (entry.getKey().equals("note")){
                note = entry.getValue();
            }
        }
        iconUrl = HttpUtil.SPS_SOURCE_URL+iconUrl;

        if (type == FriendMessage.RECEIVEMESSAGE){
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftContent.setText(content);
            Glide.with(mContext).load(iconUrl).into(holder.leftIcon);
            if (messageType.equals(MyFragment.GROUP_LIST)){
                holder.memberNoteView.setVisibility(View.VISIBLE);
                holder.memberNoteView.setText(note);
            }
        }else if (type == FriendMessage.SENDMESSAGE){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightContent.setText(content);
            Glide.with(mContext).load(iconUrl).into(holder.rightIcon);
            if (messageType.equals(MyFragment.GROUP_LIST)){
                holder.myNoteView.setVisibility(View.VISIBLE);
                holder.myNoteView.setText(note);
            }
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout leftLayout;
        public RelativeLayout rightLayout;

        public CircleImageView leftIcon;
        public CircleImageView rightIcon;

        public TextView leftContent;
        public TextView rightContent;

        public TextView memberNoteView;
        public TextView myNoteView;

        public ViewHolder(View itemView) {
            super(itemView);

            leftLayout = itemView.findViewById(R.id.receive_message);
            rightLayout = itemView.findViewById(R.id.send_message);

            leftIcon = itemView.findViewById(R.id.left_message_icon);
            rightIcon = itemView.findViewById(R.id.right_message_icon);

            leftContent = itemView.findViewById(R.id.receive_content);
            rightContent = itemView.findViewById(R.id.send_content);

            memberNoteView = itemView.findViewById(R.id.member_note);
            myNoteView = itemView.findViewById(R.id.my_note);
        }
    }
}
