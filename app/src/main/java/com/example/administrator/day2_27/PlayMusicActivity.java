package com.example.administrator.day2_27;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.day2_27.EntityClass.Song;
import com.example.administrator.day2_27.EntityClass.SongIdNPath;
import com.vansuita.gaussianblur.GaussianBlur;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.example.administrator.day2_27.MainActivity.getImage;
import static com.example.administrator.day2_27.MainActivity.mBinder;

public class PlayMusicActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PlayMusicActivity";
    private TextView title;
    private TextView singer;
    private GramophoneView gramophoneView;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView totalTime;
    private ImageView mode;
    private ImageView playPause;
    private ImageView playMusicBackground;

    private List<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        Toolbar toolbar = (Toolbar) findViewById(R.id.play_music_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ImageView last = (ImageView) findViewById(R.id.play_music_last);
        ImageView next = (ImageView) findViewById(R.id.play_music_next);
        title = (TextView) findViewById(R.id.play_music_title);
        singer = (TextView) findViewById(R.id.play_music_singer);
        gramophoneView = (GramophoneView) findViewById(R.id.gramophone_view);
        seekBar = (SeekBar) findViewById(R.id.play_music_seekBar);
        currentTime = (TextView) findViewById(R.id.play_music_current_time);
        totalTime = (TextView) findViewById(R.id.play_music_total_time);
        mode = (ImageView) findViewById(R.id.play_music_mode);
        playPause = (ImageView) findViewById(R.id.play_music_play_pause);
        playMusicBackground = (ImageView) findViewById(R.id.play_music_background);

        last.setOnClickListener(this);
        next.setOnClickListener(this);
        playPause.setOnClickListener(this);
        mode.setOnClickListener(this);
        playMusicBackground.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(this);

        mBinder.updateSongInfo();
    }

    //更新当前歌曲信息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getMsg()) {
            case "更新当前歌曲信息":
                int position = event.getCount();
                songList = MainActivity.getSongList();

                title.setText(songList.get(position).getSongName());
                singer.setText(songList.get(position).getSinger());
                currentTime.setText(mBinder.getCurrentTime());
                totalTime.setText(mBinder.getTotalTime());
                seekBar.setProgress(mBinder.getProgress());


                Bitmap b = getImage(songList.get(position).getAlbum_id(),this);
                gramophoneView.setPictureRes(b);
                if (b == null) {
                    GaussianBlur.with(this).put(R.drawable.ic_blackground, playMusicBackground);
                } else {
                    GaussianBlur.with(this).put(b, playMusicBackground);
                }

                if (mBinder.isPlaying()) {
                    gramophoneView.setPlaying(true);
                    playPause.setImageResource(R.drawable.pause);
                } else {
                    gramophoneView.setPlaying(false);
                    playPause.setImageResource(R.drawable.play);
                }

                Log.d(TAG, "onMessageEvent: " + mBinder.getMode());
                switch (mBinder.getMode()) {
                    case MyMusicService.SINGLE_CYCLE:
                        mode.setImageResource(R.drawable.single);
                        break;

                    case MyMusicService.ALL_CYCLE:
                        mode.setImageResource(R.drawable.cycle);
                        break;

                    case MyMusicService.RANDOM_PLAY:
                        mode.setImageResource(R.drawable.random);
                        break;
                }
                break;

            case "实时更新":
                seekBar.setProgress(mBinder.getProgress());
                currentTime.setText(mBinder.getCurrentTime());
                break;
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_music_last:
                mBinder.playLastSong();
                break;

            case R.id.play_music_next:
                mBinder.playNextSong();
                break;

            case R.id.play_music_play_pause:
                if (!mBinder.isSetData()) {
                    songList = MainActivity.getSongList();
                    mBinder.start(new SongIdNPath(songList.get(0).getSongPath(), 0));
                } else {
                    mBinder.playPause();
                }
                break;

            case R.id.play_music_mode:
                switch (mBinder.changeMode()) {
                    case MyMusicService.SINGLE_CYCLE:
                        mode.setImageResource(R.drawable.single);
                        break;

                    case MyMusicService.ALL_CYCLE:
                        mode.setImageResource(R.drawable.cycle);
                        break;

                    case MyMusicService.RANDOM_PLAY:
                        mode.setImageResource(R.drawable.random);
                        break;
                }
                break;

            default:

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mBinder.isSetData()) {
            mBinder.pause();
            mBinder.stopSSupdate();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mBinder.isSetData()) {
            mBinder.seekTo((int) (mBinder.getDuration() * seekBar.getProgress() / 100));
            mBinder.playPause();
            mBinder.startSSupdate();
        }
    }
}
