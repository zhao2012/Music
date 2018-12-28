package com.example.administrator.day2_27;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.administrator.day2_27.Fragment.ScanDesignated;
import com.example.administrator.day2_27.Fragment.ScanOption;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";
    private ScanOption scanOption;
    private ScanDesignated scanDesignated;
    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Toolbar scanToolbar = (Toolbar) findViewById(R.id.scan_toolbar);
        setSupportActionBar(scanToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        manager = getSupportFragmentManager();

        initScanOptionFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
        }
        return true;
    }

    private void initScanOptionFragment(){
        FragmentTransaction ft = manager.beginTransaction();

        if (scanOption == null) {
            scanOption = new ScanOption();
        }

        ft.replace(R.id.scan_container, scanOption);
        ft.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getMsg()) {
            case "完成扫描":
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("完成扫描");
                builder.setMessage("本次扫描新增了" + event.getCount() + "首音乐。");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                builder.show();
                break;

            case "切换扫描指定文件夹界面":
                initScanDesignatedFragment();
                break;

            case "切换扫描选择界面":
                initScanOptionFragment();
                break;
        }
    }

    private void initScanDesignatedFragment() {
        FragmentTransaction ft = manager.beginTransaction();

        if (scanDesignated == null) {
            scanDesignated = new ScanDesignated();
        }

        ft.replace(R.id.scan_container, scanDesignated);
        ft.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
