package com.example.administrator.day2_27.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.day2_27.R;
import com.example.administrator.day2_27.EntityClass.Song;
import com.example.administrator.day2_27.Interface.SongAdapterOnClickListener;

import java.util.List;

/**
 * Created by Administrator on 2018/2/27 0027.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private List<Song> songList;
    private Context mContext;
    private SongAdapterOnClickListener saListener;

    public SongAdapter(List<Song> songList,SongAdapterOnClickListener listener) {
        super();
        this.songList = songList;
        saListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.song_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.songView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                saListener.playMusic(position);
            }
        });

        holder.songOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                saListener.showEditTDialog(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.songName.setText(song.getSongName());
        holder.singer.setText(song.getSinger());
        holder.songFileName.setText(song.getSongFileName());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView songName;
        TextView singer;
        TextView songFileName;
        View songView;
        ImageView songOption;

        public ViewHolder(View itemView) {
            super(itemView);
            songView = itemView;
            songOption = (ImageView) itemView.findViewById(R.id.song_item_option);
            songName = (TextView) itemView.findViewById(R.id.song_Name);
            singer = (TextView) itemView.findViewById(R.id.song_Singer);
            songFileName = (TextView) itemView.findViewById(R.id.song_FileName);
        }
    }
}
