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
import com.qut.sps.aty.ClothesActivity;
import com.qut.sps.db.Costume;
import com.qut.sps.util.HttpUtil;

import java.util.List;

/**
 * Created by 13686 on 2017/8/13.
 */

public class ClothesAdapter extends RecyclerView.Adapter<ClothesAdapter.ViewHolder> {

    private Context mContext;

    private List<Costume> list;

    public class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView clothesImage;
        TextView clothesText;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            clothesImage = itemView.findViewById(R.id.clothes_image);
            clothesText = itemView.findViewById(R.id.clothes_name);
        }
    }

    public ClothesAdapter(List<Costume> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.clothes_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Costume obj = list.get(position);
        holder.clothesText.setText(obj.getImageName());
        Glide.with(mContext).load(HttpUtil.SPS_SOURCE_URL+obj.getImageUrl()).into(holder.clothesImage);
        //CardView 的 点击相应事件
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ClothesActivity.class);
                intent.putExtra(ClothesActivity.NAME,obj.getImageName());
                intent.putExtra(ClothesActivity.TEL,obj.getTel());
                intent.putExtra(ClothesActivity.DESCRIPTION,obj.getDescription());
                intent.putExtra(ClothesActivity.URL,obj.getImageUrl());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return  list.size();
    }
}
