package com.example.administrator.day2_27.Adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.administrator.day2_27.EntityClass.Folder;
import com.example.administrator.day2_27.Interface.FolderAdapterOnClickListener;
import com.example.administrator.day2_27.R;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2 0002.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private List<Folder> mFolderList;
    private Context mContext;

    private FolderAdapterOnClickListener mListener;

    public FolderAdapter(List<Folder> folderList,FolderAdapterOnClickListener listener) {
        super();
        mFolderList = folderList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.folder_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.folderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Folder folder = mFolderList.get(position);
                if (folder.getFolder_name().equals("内置存储")) {
                    //将列表更新为内置SD卡的文件夹列表
                    mListener.insideStorage(Environment.getExternalStorageDirectory());
                } else if (folder.getFolder_name().equals("外置存储")) {
                    //将列表更新为外置SD卡的文件夹列表
                    mListener.externalStorage();
                } else {
                    //根据文件名，添加到当前目录，然后再更新文件夹列表
                    mListener.normalClick(folder.getFolderFile());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Folder folder = mFolderList.get(position);
        holder.folderName.setText(folder.getFolder_name());
    }

    @Override
    public int getItemCount() {
        return mFolderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView folderName;
        View folderView;

        ViewHolder(View itemView) {
            super(itemView);
            folderView = itemView;
            folderName = itemView.findViewById(R.id.folder_item_name);
        }
    }
}
