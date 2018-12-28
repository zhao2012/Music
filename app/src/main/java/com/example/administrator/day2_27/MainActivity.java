package com.example.administrator.day2_27;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.day2_27.Adapter.SongAdapter;
import com.example.administrator.day2_27.EntityClass.Song;
import com.example.administrator.day2_27.EntityClass.SongIdNPath;
import com.example.administrator.day2_27.Interface.SongAdapterOnClickListener;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;
import com.timmy.tdialog.listener.OnViewClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MusicPlayer";

    private static List<Song> songList = new ArrayList<>();
    private List<Song> searchList = new ArrayList<>();

    private Box<Music> box;
    private static BoxStore boxStore;
    private SongAdapter adapter;

    private TextView emptyTips;
    private EditText toolBarEditText;
    private RecyclerView recyclerView;
    private ImageView playPause;
    private ImageView albumImage;
    private TextView currentSongName;
    private TextView currentSongSinger;

    private Thread thread;
    private boolean isExit = false;
    private boolean isExitProgram;

    private SongAdapterOnClickListener saListener = new SongAdapterOnClickListener() {
        @Override
        public void changeAlbumImage(String albumId) {
            Bitmap bitmap = getImage(albumId, MainActivity.this);
            if (bitmap == null) {
                albumImage.setImageResource(R.drawable.music);
            } else {
                albumImage.setImageBitmap(bitmap);
            }
        }

        @Override
        public void changeSongName(String songName) {
            currentSongName.setText(songName);
        }

        @Override
        public void changeSongSinger(String singer) {
            currentSongSinger.setText(singer);
        }

        @Override
        public void changePlayPauseImage(boolean isPlaying) {
            if (isPlaying) {
                playPause.setImageResource(R.drawable.pause);
            } else {
                playPause.setImageResource(R.drawable.play);
            }
        }

        @Override
        public void playMusic(int position) {
            mBinder.start(new SongIdNPath(songList.get(position).getSongPath(), position));
            saListener.changePlayPauseImage(true);
        }

        @Override
        public void showEditTDialog(int position) {
            final Song song = songList.get(position);

            TDialog tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.edit_tdialog)    //设置弹窗展示的xml布局
//                .setDialogView(view)  //设置弹窗布局,直接传入View
                    .setWidth(600)  //设置弹窗宽度(px)
                    .setHeight(800)  //设置弹窗高度(px)
                    .setScreenWidthAspect(MainActivity.this, 1f)   //设置弹窗宽度(参数aspect为屏幕宽度比例 0 - 1f)
                    .setScreenHeightAspect(MainActivity.this, 0.35f)  //设置弹窗高度(参数aspect为屏幕宽度比例 0 - 1f)
                    .setGravity(Gravity.BOTTOM)     //设置弹窗展示位置
                    .setTag("DialogTest")   //设置Tag
                    .setDimAmount(0.6f)     //设置弹窗背景透明度(0-1f)
                    .setCancelableOutside(true)     //弹窗在界面外是否可以点击取消
                    .setCancelable(true)    //弹窗是否可以取消
                    .setOnBindViewListener(new OnBindViewListener() {   //通过BindViewHolder拿到控件对象,进行修改
                        @Override
                        public void bindView(BindViewHolder bindViewHolder) {
                            bindViewHolder.setText(R.id.edit_tdialog_song_title, "歌名：" + song.getSongName());
                            bindViewHolder.setText(R.id.edit_tdialog_singer, "歌手：" + song.getSinger());
                            bindViewHolder.setText(R.id.edit_tdialog_song_path, "歌曲位置：" + song.getSongPath());
                            Bitmap bitmap = getImage(song.getAlbum_id(), MainActivity.this);
                            if (bitmap == null) {
                                bindViewHolder.setImageResource(R.id.edit_tdialog_album_image, R.drawable.music);
                            } else {
                                bindViewHolder.setImageBitmap(R.id.edit_tdialog_album_image,getImage(song.getAlbum_id(),MainActivity.this));
                            }
                        }
                    })
                    .addOnClickListener(R.id.edit_tdialog_edit_btn)   //添加进行点击控件的id
                    .setOnViewClickListener(new OnViewClickListener() {     //View控件点击事件回调
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            switch (view.getId()) {
                                case R.id.edit_tdialog_edit_btn:
                                    showEditTDialog2(song);
                                    tDialog.dismiss();
                                    break;
                            }
                        }
                    })
                    .create()   //创建TDialog
                    .show();    //展示
        }
    };

    private void showEditTDialog2(final Song song) {
        TDialog tDialog = new TDialog.Builder(getSupportFragmentManager())
                .setLayoutRes(R.layout.edit_tdialog2)    //设置弹窗展示的xml布局
                .setWidth(600)  //设置弹窗宽度(px)
                .setHeight(800)  //设置弹窗高度(px)
                .setScreenWidthAspect(MainActivity.this, 0.8f)   //设置弹窗宽度(参数aspect为屏幕宽度比例 0 - 1f)
                .setScreenHeightAspect(MainActivity.this, 0.4f)  //设置弹窗高度(参数aspect为屏幕宽度比例 0 - 1f)
                .setGravity(Gravity.CENTER)     //设置弹窗展示位置
                .setTag("DialogTest")   //设置Tag
                .setDimAmount(0.6f)     //设置弹窗背景透明度(0-1f)
                .setCancelableOutside(true)     //弹窗在界面外是否可以点击取消
                .setCancelable(true)    //弹窗是否可以取消
                .setOnBindViewListener(new OnBindViewListener() {   //通过BindViewHolder拿到控件对象,进行修改
                    @Override
                    public void bindView(BindViewHolder bindViewHolder) {
                        bindViewHolder.setText(R.id.edit_tdialog2_title, "原歌名：" + song.getSongName());
                        bindViewHolder.setText(R.id.edit_tdialog2_singer, "原歌手：" + song.getSinger());
                    }
                })
                .addOnClickListener(R.id.edit_tdialog2_confirm)   //添加进行点击控件的id
                .setOnViewClickListener(new OnViewClickListener() {     //View控件点击事件回调
                    @Override
                    public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                        switch (view.getId()) {
                            case R.id.edit_tdialog2_confirm:
                                //更新数据库
                                EditText newTitleEdit = viewHolder.getView(R.id.edit_tdialog2_new_title);
                                EditText newSingerEdit = viewHolder.getView(R.id.edit_tdialog2_new_singer);
                                String newTitle = newTitleEdit.getText().toString().trim();
                                String newSinger = newSingerEdit.getText().toString().trim();

                                //找过song在box的位置id
                                List<Music> list = box.find(Music_.url, song.getSongPath());
                                if (list.size() == 1) {
                                    Music m = list.get(0);
                                    //修改信息
                                    if (!newTitle.equals("")) {
                                        m.setTitle(newTitle);
                                    }
                                    if (!newSinger.equals("")) {
                                        m.setArtist(newSinger);
                                    }

                                    if (!newTitle.equals("") || !newSinger.equals("")) {
                                        box.put(m);
                                        initSongList();
                                    }
                                } else {
                                    //数据库找不到
                                    break;
                                }
                                tDialog.dismiss();
                                break;
                        }
                    }
                })
                .create()   //创建TDialog
                .show();    //展示
    }

    public static MyMusicService.MusicBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MyMusicService.MusicBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent startService = new Intent(this, MyMusicService.class);
        bindService(startService, connection, BIND_AUTO_CREATE);

        //适配安卓6.0权限问题，在这里需用代码确认一遍
        //需要WRITE_EXTERNAL_STORAGE 才能读U盘
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (boxStore == null) {
            boxStore = MyObjectBox.builder().androidContext(this).build();
        }
        box = boxStore.boxFor(Music.class);

        //当数据库不为空时，就隐藏该提示
        emptyTips = (TextView) findViewById(R.id.empty_tips);
        if (box.count() > 0) {
            emptyTips.setVisibility(View.GONE);
        }

        toolBarEditText = (EditText) findViewById(R.id.toolbar_edit_text);
        toolBarEditText.addTextChangedListener(new MyEditTextChangeListener());

        adapter = new SongAdapter(songList, saListener);
        initSongList();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        albumImage = (ImageView) findViewById(R.id.album_image);
        currentSongName = (TextView) findViewById(R.id.current_song_title);
        currentSongName.setSelected(true);
        currentSongSinger = (TextView) findViewById(R.id.current_song_singer);
        currentSongSinger.setSelected(true);
        playPause = (ImageView) findViewById(R.id.play_pause);

        albumImage.setOnClickListener(this);
        currentSongName.setOnClickListener(this);
        currentSongSinger.setOnClickListener(this);
        playPause.setOnClickListener(this);
    }

    private void initSongList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                songList.clear();
                for (Music m : box.getAll()) {
                    Song s = new Song(m.getTitle(),
                            m.getArtist(),
                            m.get_display_name(),
                            m.getAlbum_id(),
                            m.getUrl());
                    songList.add(s);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        if (box.count() > 0) {
                            emptyTips.setVisibility(View.GONE);
                        } else {
                            emptyTips.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.album_image:
            case R.id.current_song_singer:
            case R.id.current_song_title:
                Intent playMusicIntent = new Intent(this, PlayMusicActivity.class);
                startActivity(playMusicIntent);
                break;

            case R.id.play_pause:
                if (songList.size() > 0) {
                    if (!mBinder.isSetData()) {
                        mBinder.start(new SongIdNPath(songList.get(0).getSongPath(), 0));
                    } else {
                        mBinder.playPause();
                    }
                }
                break;
        }
    }

    //设置两次返回键才退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit() {
        if (!isExitProgram) {
            isExitProgram = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new MessageEvent("重置退出程序计时", 2000));
        } else {
            finish();
        }
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getMsg()) {
            case "重置退出程序计时":
                try {
                    Thread.sleep(event.getCount());
                    isExitProgram = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //更新当前歌曲信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent2(MessageEvent event) {
        switch (event.getMsg()) {
            case "更新当前歌曲信息":
                int position = event.getCount();
                saListener.changeAlbumImage(songList.get(position).getAlbum_id());
                saListener.changeSongName(songList.get(position).getSongName());
                saListener.changeSongSinger(songList.get(position).getSinger());
                saListener.changePlayPauseImage(mBinder.isPlaying());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (mBinder != null) {
            mBinder.updateSongInfo();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private class MyEditTextChangeListener implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(final Editable editable) {

            //先把旧的匹配子线程杀掉
            if (thread != null) {
                try {
                    isExit = true;
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    isExit = false;
                    searchList.clear();
                    String searchContent = editable.toString();
                    if (!searchContent.equals("")) {
                        List<Music> list = box.getAll();
                        for (int i = 0; i < list.size() && !isExit; i++) {
                            Music m = list.get(i);
                            if (m.get_display_name().contains(searchContent)
                                    || m.getTitle().contains(searchContent)
                                    || m.getArtist().contains(searchContent)) {

                                Song song = new Song(m.getTitle(),
                                        m.getArtist(),
                                        m.get_display_name(),
                                        m.getAlbum_id(),
                                        m.getUrl());

                                searchList.add(song);
                            }
                        }
                        if (!isExit) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } else {
                        searchList.clear();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length == 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED
                        && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝授权将无法使用应用！", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                //扫描完成，更新列表
            case 2:
                //删除了歌曲，更新列表
                if (resultCode == RESULT_OK) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            songList.clear();
                            for (Music m : box.getAll()) {
                                Song s = new Song(m.getTitle(),
                                        m.getArtist(),
                                        m.get_display_name(),
                                        m.getAlbum_id(),
                                        m.getUrl());
                                songList.add(s);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    if (box.count() > 0) {
                                        emptyTips.setVisibility(View.GONE);
                                    } else {
                                        emptyTips.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_scan:
                Intent scanIntent = new Intent(this, ScanActivity.class);
                startActivityForResult(scanIntent, 1);
                break;

            case R.id.toolbar_clear:
                showClearDialog();
                break;

            case R.id.toolbar_search:
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
                toolBarEditText.setVisibility(View.VISIBLE);

                //editText获取焦点
                toolBarEditText.setFocusable(true);
                toolBarEditText.setFocusableInTouchMode(true);
                toolBarEditText.requestFocus();
                //弹出软键盘
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                adapter = new SongAdapter(searchList, saListener);
                recyclerView.setAdapter(adapter);

                break;

            case R.id.toolbar_multiselect:
                Intent multiIntent = new Intent(this, MultiListActivity.class);
                startActivityForResult(multiIntent, 2);
                break;

            case android.R.id.home:
                ActionBar actionBar1 = getSupportActionBar();
                if (actionBar1 != null) {
                    actionBar1.setDisplayHomeAsUpEnabled(false);
                }
                toolBarEditText.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                adapter = new SongAdapter(songList, saListener);
                recyclerView.setAdapter(adapter);

                break;
            default:
        }
        return true;
    }

    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空列表 - 安全警告");
        builder.setMessage("应用的数据库将被清空！\n你确定要清空歌曲列表吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        box.removeAll();
                        songList.clear();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                emptyTips.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public static BoxStore getBoxStore(){
        return boxStore;
    }

    //获取专辑封面
    public static Bitmap getImage(String id,Context context) {
        String albumArt = getAlbumArt(id,context);
        Bitmap bm;
        if (albumArt == null) {
            bm = null;
        } else {
            bm = BitmapFactory.decodeFile(albumArt);
        }
        return bm;
    }
    private static String getAlbumArt(String album_id,Context context) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = context.getContentResolver().query(  Uri.parse(mUriAlbums + "/" + album_id),  projection, null, null, null);
        String album_art = null;
        if (cur != null && cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
            cur.close();
        }
        return album_art;
    }

    //获取下一首歌的路径
    public static SongIdNPath getNextSongPath(int position) {
        if (songList.size() > 0) {
            if (position == (songList.size() - 1)) {
                return new SongIdNPath(songList.get(0).getSongPath(), 0);
            } else {
                return new SongIdNPath(songList.get(position + 1).getSongPath(), position + 1);
            }
        } else {
            return null;
        }
    }

    //获取上一首歌的路径
    public static SongIdNPath getLastSongPath(int position) {
        int path;
        if (songList.size() > 0) {
            if (position == 0) {
                return new SongIdNPath(songList.get(songList.size() - 1).getSongPath(), songList.size() - 1);
            } else {
                return new SongIdNPath(songList.get(position - 1).getSongPath(), position - 1);
            }
        } else {
            return null;
        }
    }

    //获取列表随机一首歌的路径
    public static SongIdNPath getRandomSongPath(){
        Random random = new Random();
        int position = random.nextInt(songList.size());
        String path = songList.get(position).getSongPath();
        return new SongIdNPath(path, position);
    }

    public static List<Song> getSongList(){
        return songList;
    }
}
