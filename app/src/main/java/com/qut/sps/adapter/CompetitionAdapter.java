package com.qut.sps.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qut.sps.R;
import com.qut.sps.aty.CompetitionContextActivity;
import com.qut.sps.util.HttpUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by 13686 on 2017/8/16.
 */

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.ViewHolder> {
    private Context mContext;

    private List<Map<String,String>> mapList;

    public class ViewHolder extends RecyclerView.ViewHolder{

        CardView  cardView;
        ImageView competitionlayout;
        TextView  competitionText;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            competitionlayout = itemView.findViewById(R.id.competition_layout);
            competitionText = itemView.findViewById(R.id.competition_name);
        }
    }

    public CompetitionAdapter(List<Map<String, String>> mapList) {
        this.mapList = mapList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.competition_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Map<String, String> map = mapList.get(position);
        holder.competitionText.setText(map.get("name"));
        Glide.with(mContext).load(HttpUtil.SPS_SOURCE_URL +"competitionPoster/"+ map.get("imageUrl")).into(holder.competitionlayout);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CompetitionContextActivity.class);
                intent.putExtra(CompetitionContextActivity.NAME,map.get("name"));
                intent.putExtra(CompetitionContextActivity.USER_ID,map.get("userId"));
                intent.putExtra(CompetitionContextActivity.START_TIME,map.get("startTime"));
                intent.putExtra(CompetitionContextActivity.END_TIME,map.get("endTime"));
                intent.putExtra(CompetitionContextActivity.PLACE,map.get("place"));
                intent.putExtra(CompetitionContextActivity.TEL,map.get("tel"));
                intent.putExtra(CompetitionContextActivity.DESCRIPTION,map.get("description"));
                intent.putExtra(CompetitionContextActivity.IMAGE_URL,map.get("imageUrl"));
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }
}
