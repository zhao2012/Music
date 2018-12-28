package com.example.administrator.day2_27.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.administrator.day2_27.MainActivity;
import com.example.administrator.day2_27.MessageEvent;
import com.example.administrator.day2_27.Music;
import com.example.administrator.day2_27.Util.MusicUtil;
import com.example.administrator.day2_27.R;

import org.greenrobot.eventbus.EventBus;

import io.objectbox.Box;

/**
 * Created by Administrator on 2018/2/27 0027.
 */

public class ScanOption extends Fragment implements View.OnClickListener {

    private static final String TAG = "ScanOption";
    Box<Music> box;
    Context mContext;

    AlertDialog dialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContext == null) {
            mContext = inflater.getContext();
        }
        box = MainActivity.getBoxStore().boxFor(Music.class);

        View view = inflater.inflate(R.layout.frag_scan_option, container, false);

        Button btnScanAll = view.findViewById(R.id.frag_scan_all);
        Button btnScanDesignated = view.findViewById(R.id.frag_scan_designated);

        btnScanAll.setOnClickListener(this);
        btnScanDesignated.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frag_scan_all:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("扫描整机");
                builder.setMessage("请耐心等待...");
                builder.setView(new ProgressBar(mContext));
                builder.setCancelable(false);
                dialog = builder.show();

                //扫描并添加机器里所有的音乐文件
                scanAllMusic();

                break;

            case R.id.frag_scan_designated:
                EventBus.getDefault().post(new MessageEvent("切换扫描指定文件夹界面",0));
                break;
        }
    }

    /**
     * 扫描并添加机器里所有的音乐文件
     * 因为扫描是一个耗时操作
     * 所以需要开子线程
     */
    private void scanAllMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = MusicUtil.getDesignatedPathMusic(mContext,null);//第二个参数为null时，则扫描整机
                dialog.dismiss();
                EventBus.getDefault().post(new MessageEvent("完成扫描", count));
            }
        }).start();
    }
}
