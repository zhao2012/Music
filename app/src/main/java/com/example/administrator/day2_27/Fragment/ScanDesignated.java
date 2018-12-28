package com.example.administrator.day2_27.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.day2_27.EntityClass.Folder;
import com.example.administrator.day2_27.Adapter.FolderAdapter;
import com.example.administrator.day2_27.Interface.FolderAdapterOnClickListener;
import com.example.administrator.day2_27.MessageEvent;
import com.example.administrator.day2_27.Util.MusicUtil;
import com.example.administrator.day2_27.R;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/2 0002.
 */

public class ScanDesignated extends Fragment implements View.OnClickListener {

    private static final String TAG = "ScanDesignated";
    private Context mContext;
    private List<Folder> mFolderList = new ArrayList<>();
    private FolderAdapter adapter;
    private AlertDialog dialog;

    private TextView scanDesignatedCurrentPath;

    private FolderAdapterOnClickListener faListener = new FolderAdapterOnClickListener() {
        @Override
        public void insideStorage(File file) {
            initFolderList(file);
        }

        @Override
        public void externalStorage() {
            boolean ONCE = true;

            List<String> esList = getExtSDCardPathList();
            //因为0是内置SD卡，所以从1开始
            for (int i=1;i<esList.size();i++) {
                String path = esList.get(i);
                File file = new File(path);
                if (file.exists()) {
                    //如果存在外置存储就清空mFolderList,仅清空一次
                    if (ONCE) {
                        mFolderList.clear();
                        ONCE=false;
                    }
                    Folder folder = new Folder();
                    folder.setFolder_name(file.getName());
                    folder.setFolderFile(file);
                    mFolderList.add(folder);
                } else {
                    Toast.makeText(mContext,"扫描外置存储的路径出错",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "externalStorage: 外置存储的路径" + path + "有问题！");
                }
            }

            adapter.notifyDataSetChanged();
        }

        @Override
        public void normalClick(File file) {
            initFolderList(file);
        }
    };

    private void initFolderList(File file) {
        if (file.exists()) {
            scanDesignatedCurrentPath.setText(file.getAbsolutePath());
            mFolderList.clear();
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    Folder folder = new Folder();
                    folder.setFolder_name(f.getName());
                    folder.setFolderFile(f);
                    mFolderList.add(folder);
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext,"路径有问题，无法更新列表！",Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContext == null) {
            mContext = inflater.getContext();
        }
        View view = inflater.inflate(R.layout.frag_scan_designated, container, false);

        scanDesignatedCurrentPath = view.findViewById(R.id.frag_scan_designated_currentPath);
        Button btnFragScanDesignatedCancel = view.findViewById(R.id.frag_scan_designated_cancel);
        Button btnFragScanDesignatedConfirm = view.findViewById(R.id.frag_scan_designated_confirm);
        ImageView ivBack = view.findViewById(R.id.frag_scan_designated_back);

        btnFragScanDesignatedCancel.setOnClickListener(this);
        btnFragScanDesignatedConfirm.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        adapter = new FolderAdapter(mFolderList,faListener);
        initHomePage();
        GridLayoutManager manager = new GridLayoutManager(mContext, 4);
        RecyclerView recyclerView = view.findViewById(R.id.frag_scan_designated_rv);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void initHomePage() {
        mFolderList.clear();
        scanDesignatedCurrentPath.setText("(空)");

        Folder f1 = new Folder();
        f1.setFolder_name("内置存储");
        f1.setFolderFile(Environment.getExternalStorageDirectory());
        mFolderList.add(f1);

        if (getExtSDCardPathList().size() > 1) {
            Folder f2 = new Folder();
            f2.setFolder_name("外置存储");
            f2.setFolderFile(null);
            mFolderList.add(f2);
        }

        adapter.notifyDataSetChanged();
    }


    /**
     * 获取外置SD卡路径以及TF卡的路径
     * <p>
     * 返回的数据：paths.get(0)肯定是外置SD卡的位置，因为它是primary external storage.
     *
     * @return 所有可用于存储的不同的卡的位置，用一个List来保存
     */
    public static List<String> getExtSDCardPathList() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    @Override
    public void onClick(View v) {
        String path = scanDesignatedCurrentPath.getText().toString();
        switch (v.getId()) {
            case R.id.frag_scan_designated_cancel:
                EventBus.getDefault().post(new MessageEvent("切换扫描选择界面", 0));
                break;

            case R.id.frag_scan_designated_back:

                if (!path.equals("(空)")) {
                    File file = new File(path);
                    if (file.exists()) {
                        List<String> list = getExtSDCardPathList();
                        boolean isBackExtPage = false;
                        boolean isBackHomePage = false;
                        if (path.equals(list.get(0))) {
                            isBackHomePage = true;
                        } else {
                            for (int i = 1; i < list.size(); i++) {
                                if (path.equals(list.get(i))) {
                                    isBackExtPage = true;
                                    break;
                                }
                            }
                        }
                        if (isBackHomePage) {
                            initHomePage();
                        } else if (isBackExtPage) {
                            faListener.externalStorage();
                            scanDesignatedCurrentPath.setText("(空)");
                        } else {
                            initFolderList(file.getParentFile());
                        }

                    } else {
                        Toast.makeText(mContext, "当前路径出错！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    initHomePage();
                }
                break;

            case R.id.frag_scan_designated_confirm:
                if (!path.equals("(空)")) {
                    File file = new File(path);
                    if (file.exists()) {
                        //如果选择的是外置SD卡时，则另作判断

                        //根据当前路径扫描添加歌曲
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("扫描指定文件夹");
                        builder.setMessage("请耐心等待...");
                        builder.setView(new ProgressBar(mContext));
                        builder.setCancelable(false);
                        dialog = builder.show();
                        //扫描并添加机器里所有的音乐文件
                        scanDesignatedMusic(path);
                    } else {
                        Toast.makeText(mContext, "当前路径出错！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext,"无法扫描，当前选择路径为(空)。",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * 扫描并添加机器里的音乐文件
     * 因为扫描是一个耗时操作
     * 所以需要开子线程
     * @param path 指定文件夹的路径
     */
    private void scanDesignatedMusic(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = MusicUtil.getDesignatedPathMusic(mContext,path);
                dialog.dismiss();
                EventBus.getDefault().post(new MessageEvent("完成扫描", count));
            }
        }).start();
    }
}
