package com.qut.sps.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qut.sps.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/8/21.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    private List<Map<String,String>> musicList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public MusicListAdapter(List<Map<String,String>>musicList){
        this.musicList = musicList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        Map<String,String> map = musicList.get(position);
        final String musicName = map.get("musicName");
        final String singerName = map.get("singerName");

        holder.musicSingerView.setText(singerName);
        holder.musicNameView.setText(musicName);
        holder.musicNOView.setText(String.valueOf(position+1));

        holder.singleMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClick(holder.getLayoutPosition());
            }
        });
    }

    public interface OnItemClickListener{
        void OnItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView musicNOView;
        public TextView musicNameView;
        public TextView musicSingerView;
        public RelativeLayout singleMusic;
        public ViewHolder(View itemView) {
            super(itemView);

            singleMusic = itemView.findViewById(R.id.music);
            musicNameView = itemView.findViewById(R.id.music_name);
            musicNOView = itemView.findViewById(R.id.music_NO);
            musicSingerView = itemView.findViewById(R.id.music_singer);
        }
    }
}
