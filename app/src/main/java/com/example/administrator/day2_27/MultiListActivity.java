package com.example.administrator.day2_27;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.administrator.day2_27.Adapter.MultiListAdapter;
import com.example.administrator.day2_27.EntityClass.MultiSelectItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class MultiListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MultiListActivity";
    private List<MultiSelectItem> mMultiList = new ArrayList<>();
    private MultiListAdapter adapter;
    private Box<Music> box;

    private AlertDialog mDialog;
    private List<MultiSelectItem> countList = new ArrayList<>();


    private boolean isDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_list);

        box = MainActivity.getBoxStore().boxFor(Music.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.multi_list_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        adapter = new MultiListAdapter(mMultiList);
        initMultiList();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.multi_list_rv);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);


        Button btnMultiListDelete = (Button) findViewById(R.id.multi_list_delete);
        Button btnMultiListInverse = (Button) findViewById(R.id.multi_list_Inverse);
        Button btnMultiListAll = (Button) findViewById(R.id.multi_list_all);

        btnMultiListAll.setOnClickListener(this);
        btnMultiListInverse.setOnClickListener(this);
        btnMultiListDelete.setOnClickListener(this);

    }

    private void initMultiList() {
        mMultiList.clear();
        for (Music m : box.getAll()) {

            MultiSelectItem item = new MultiSelectItem();
            item.setSelect(false);
            item.setSinger(m.getArtist());
            item.setSongName(m.getTitle());
            item.setSongUrl(m.getUrl());

            mMultiList.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isDelete) {
                    isDelete = false;
                    //要提醒MainActivity更新列表
                    setResult(RESULT_OK);
                }
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isDelete) {
            isDelete = false;
            //要提醒MainActivity更新列表
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.multi_list_delete:
                //记录选中项目
                countList.clear();
                for (int i = 0;i<mMultiList.size();i++) {
                    if (mMultiList.get(i).getCheckBox()) {
                        countList.add(mMultiList.get(i));
                    }
                }

                //如果勾选了项目 即count>0 弹出删除提示
                if (countList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("删除 - 安全警告");
                    builder.setMessage("你确定要将勾选的" + countList.size() + "首音乐从歌曲列表中删除?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MultiListActivity.this);
                            builder1.setTitle("正在删除选中的歌曲...");
                            builder1.setView(new ProgressBar(MultiListActivity.this));
                            builder1.setCancelable(false);
                            mDialog = builder1.show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    //遍历所有项目，删除应用数据库的歌曲而已
                                    for (int i = 0;i<countList.size();i++) {
                                        String url = countList.get(i).getSongUrl();
                                        List<Music> findList = box.find(Music_.url, url);
                                        if (findList.size() > 0) {
                                            for (int a = 0; a < findList.size(); a++) {
                                                box.remove(findList.get(a).getId());
                                                isDelete = true;
                                            }
                                        } else {
                                            //这种情况是应用数据库找不到...
                                            Toast.makeText(MultiListActivity.this,
                                                    "删除中止，找不到"+countList.get(i).getSongName(),
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }

                                    //更新列表
                                    if (isDelete) {
                                        mMultiList.clear();
                                        for (Music m : box.getAll()) {
                                            MultiSelectItem item = new MultiSelectItem();
                                            item.setSelect(false);
                                            item.setSinger(m.getArtist());
                                            item.setSongName(m.getTitle());
                                            item.setSongUrl(m.getUrl());

                                            mMultiList.add(item);
                                        }
                                        mDialog.dismiss();

                                        //更新UI界面需要返回UI主线程
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
                break;

            case R.id.multi_list_Inverse:
                //反选按钮，遍历列表所有项目，根据每个项目当前select而定checkbox状态
                for (int i = 0;i<mMultiList.size();i++) {
                    if (mMultiList.get(i).getCheckBox()) {
                        mMultiList.get(i).setSelect(false);
                    } else {
                        mMultiList.get(i).setSelect(true);
                    }
                }
                adapter.notifyDataSetChanged();
                break;

            case R.id.multi_list_all:
                //全选按钮，遍历列表所有项目，将Checkbox和select设置为true
                for (int i = 0;i<mMultiList.size();i++) {
                    mMultiList.get(i).setSelect(true);
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }


}
