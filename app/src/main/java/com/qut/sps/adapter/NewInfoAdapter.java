package com.qut.sps.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.qut.sps.R;
import com.qut.sps.db.NewInfoMessage;
import com.qut.sps.db.UsersInfo;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;
import com.qut.sps.util.MyApplication;
import com.qut.sps.view.MyFragment;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/8/24.
 */

public class NewInfoAdapter extends RecyclerView.Adapter<NewInfoAdapter.ViewHolder> {

    private Context mContext;
    private List<Map<String,String >> newInfoList = new ArrayList<>();

    public NewInfoAdapter(List<Map<String,String>> newInfoList){
        this.newInfoList = newInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.new_info_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Map<String,String> map = newInfoList.get(position);
        String iconUrl = HttpUtil.SPS_SOURCE_URL + map.get("iconUrl");
        final String infoFrom = map.get("infoFrom");
        String otherInfo = map.get("otherInfo");
        String isNeedChoose = map.get("isNeedChoose");
        final String EMGroupId = map.get("EMGroupId");
        final String type = map.get("type");

        Glide.with(mContext).load(iconUrl).into(holder.iconView);
        holder.infoFromView.setText(infoFrom);
        holder.otherInfoView.setText(otherInfo);

        if (isNeedChoose.equals(NewInfoMessage.NO_NEED_CHOOSE)){
            holder.buttonLayout.setVisibility(View.GONE);
        }

        holder.refuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals(MyFragment.FRIEND_LIST)){
                    refuseFriend(infoFrom);
                }else{
                    refuseGroupInvite(infoFrom,EMGroupId);
                }
                holder.buttonLayout.setVisibility(View.GONE);
            }
        });

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals(MyFragment.GROUP_LIST)){
                    acceptGroupInvite(infoFrom,EMGroupId);
                }else {
                    acceptFriendInvite(infoFrom);
                }
                holder.buttonLayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 接收申请加为好友
     * @param infoFrom
     */
    private void acceptFriendInvite(final String infoFrom) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().acceptInvitation(infoFrom);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d("accept","接受好友邀请失败！"+e.getMessage());
                }
            }
        }).start();

        NewInfoMessage newInfoMessage = new NewInfoMessage();
        newInfoMessage.setIsNeedChoose(NewInfoMessage.NO_NEED_CHOOSE);
        newInfoMessage.updateAll("infoFrom = ?",infoFrom);

        String url = HttpUtil.SPS_URL+"AddNewFriendServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("userId",MyFragment.userId)
                .add("friendAccount",infoFrom)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.body().string().equals("OK")){
                    Log.d("ERROR","添加到服务器失败！");
                }
            }
        });
    }

    /**
     * 接受加入群
     * @param infoFrom
     * @param emGroupId
     */
    private void acceptGroupInvite(final String infoFrom, final String emGroupId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   EMClient.getInstance().groupManager().acceptApplication(infoFrom,emGroupId);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d("accept","接收群邀请失败！"+e.getMessage());
                }
            }
        }).start();
        NewInfoMessage newInfoMessage = new NewInfoMessage();
        newInfoMessage.setIsNeedChoose(NewInfoMessage.NO_NEED_CHOOSE);
        newInfoMessage.updateAll("EMGroupId = ?",emGroupId);

        String url = HttpUtil.SPS_URL+"JoinInGroupServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("EMGroupId",emGroupId)
                .add("memberAccount",infoFrom)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.body().string().equals("OK")){
                    Log.d("ERROR","加入群服务器失败！");
                }
            }
        });
    }

    /**
     * 拒绝加入群
     * @param infoFrom
     * @param EMGroupId
     */
    private void refuseGroupInvite(final String infoFrom, final String EMGroupId) {
        String userName = getUserName();
        final String reason;
        if (userName != null){
            reason = userName+"拒绝了你的请求！";
        }else {
            reason = infoFrom + "拒绝了你的请求！";
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().declineApplication(infoFrom,EMGroupId,reason);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d("refuse","拒绝群邀请失败！"+e.getMessage());
                }
            }
        }).start();
        NewInfoMessage newInfoMessage = new NewInfoMessage();
        newInfoMessage.setIsNeedChoose(NewInfoMessage.NO_NEED_CHOOSE);
        newInfoMessage.updateAll("EMGroupId = ?",EMGroupId);
    }

    /**
     * 得到本账号的名字
     * @return
     */
    private String getUserName() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String userAccount = preferences.getString(Constant.CURRENT_ACCOUNT,null);
        if (userAccount == null){
            Log.d("ERROR","获取当前用户账号错误！");
        }
        List<UsersInfo> userInfoList = DataSupport.where("account = ?",userAccount).find(UsersInfo.class);
        for (UsersInfo userInfo:userInfoList){
            if (!userInfo.getNickName().equals("null")){
                return userInfo.getNickName();
            }else {
                return userInfo.getAccount();
            }
        }
        return null;
    }

    /**
     * 拒绝申请加为好友
     * @param infoFrom
     */
    private void refuseFriend(final String infoFrom) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().declineInvitation(infoFrom);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    Log.d("refuse","拒绝好友邀请失败！"+e.getMessage());
                }
            }
        }).start();
        NewInfoMessage newInfoMessage = new NewInfoMessage();
        newInfoMessage.setIsNeedChoose(NewInfoMessage.NO_NEED_CHOOSE);
        newInfoMessage.updateAll("infoFrom = ?",infoFrom);
    }

    @Override
    public int getItemCount() {
       return newInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView iconView;
        private TextView infoFromView;
        private TextView otherInfoView;
        private Button refuseButton;
        private Button acceptButton;
        private RelativeLayout buttonLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.new_info_icon);
            infoFromView = itemView.findViewById(R.id.new_info_from);
            otherInfoView = itemView.findViewById(R.id.new_info_message);

            refuseButton = itemView.findViewById(R.id.refuse);
            acceptButton = itemView.findViewById(R.id.accept);
            buttonLayout = itemView.findViewById(R.id.button_layout);
        }
    }
}
