package com.example.administrator.day2_27.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.administrator.day2_27.EntityClass.MultiSelectItem;
import com.example.administrator.day2_27.R;

import java.util.List;

/**
 * Created by Administrator on 2018/3/3 0003.
 */

public class MultiListAdapter extends RecyclerView.Adapter<MultiListAdapter.ViewHolder> {
    private Context mContext;
    private List<MultiSelectItem> mMultiSelectItemList;

    public MultiListAdapter(List<MultiSelectItem> list) {
        super();
        mMultiSelectItemList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.multi_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                MultiSelectItem item = mMultiSelectItemList.get(position);
                if (item.getCheckBox()) {
                    holder.checkBox.setChecked(false);
                    item.setSelect(false);
                } else {
                    holder.checkBox.setChecked(true);
                    item.setSelect(true);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MultiSelectItem item = mMultiSelectItemList.get(position);
        holder.songName.setText(item.getSongName());
        holder.singer.setText(item.getSinger());
        holder.songFileUrl.setText(item.getSongUrl());
        holder.checkBox.setChecked(item.getCheckBox());
    }

    @Override
    public int getItemCount() {
        return mMultiSelectItemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView songName;
        private TextView singer;
        private TextView songFileUrl;
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.multi_list_song_name);
            singer = itemView.findViewById(R.id.multi_list_singer);
            songFileUrl = itemView.findViewById(R.id.multi_list_url);
            checkBox = itemView.findViewById(R.id.multi_list_checkBox);
        }
    }
}
