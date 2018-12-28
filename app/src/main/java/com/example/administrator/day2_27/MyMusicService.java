package com.example.administrator.day2_27;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.administrator.day2_27.EntityClass.SongIdNPath;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class MyMusicService extends Service {
    private static final String TAG = "MyMusicService";

    private int mPosition;
    private boolean isSetData = false;
    private boolean isRun = true;

    //播放模式
    public static final int SINGLE_CYCLE = 1;     //单曲循环
    public static final int ALL_CYCLE = 2;        //全部循环
    public static final int RANDOM_PLAY = 3;      //随机播放

    private int MODE = ALL_CYCLE;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    while (isRun) {
                        if (isSetData) {
                            EventBus.getDefault().post(new MessageEvent("实时更新", -1));
                        }
                        try {
                            Thread.sleep(240);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(240);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void playMusic(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            isSetData = true;
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    EventBus.getDefault().post(new MessageEvent("更新当前歌曲信息",mPosition));
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switch (MODE) {
                        case SINGLE_CYCLE:
                            mediaPlayer.start();
                            break;

                        case ALL_CYCLE:
                            isSetData = false;
                            mBinder.playNextSong();
                            break;

                        case RANDOM_PLAY:
                            isSetData = false;
                            mBinder.start(MainActivity.getRandomSongPath());
                            break;

                        default:
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            isSetData = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isSetData = false;
        isRun = false;
    }

    class MusicBinder extends Binder {

        void start(SongIdNPath songIdNPath){
            if (songIdNPath != null) {
                Log.d(TAG, "start: mBinder.start()");
                mPosition = songIdNPath.getPosition();
                playMusic(songIdNPath.getSongPath());
            }
        }

        boolean playPause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
            EventBus.getDefault().post(new MessageEvent("更新当前歌曲信息",mPosition));
            return mediaPlayer.isPlaying();
        }

        void  pause(){
            if (isSetData)
                mediaPlayer.pause();
        }

        void playNextSong(){
            if (MODE == ALL_CYCLE) {
                start(MainActivity.getNextSongPath(mPosition));
            } else {
                start(MainActivity.getRandomSongPath());
            }
        }

        void playLastSong(){
            if (MODE == ALL_CYCLE) {
                start(MainActivity.getLastSongPath(mPosition));
            } else {
                start(MainActivity.getRandomSongPath());
            }
        }

        void updateSongInfo(){
            if (isSetData)
                EventBus.getDefault().post(new MessageEvent("更新当前歌曲信息",mPosition));
        }

        boolean isSetData(){
            return isSetData;
        }

        boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }

        String getCurrentTime(){
            if (isSetData) {
                return timeParse(mediaPlayer.getCurrentPosition());
            } else {
                return "00:00";
            }
        }

        String getTotalTime(){
            if (isSetData) {
                return timeParse(mediaPlayer.getDuration());
            } else {
                return "00:00";
            }
        }

        int changeMode(){
            switch (MODE) {
                case SINGLE_CYCLE:
                    MODE = ALL_CYCLE;
                    break;

                case ALL_CYCLE:
                    MODE = RANDOM_PLAY;
                    break;

                case RANDOM_PLAY:
                    MODE = SINGLE_CYCLE;
                    break;
            }
            return MODE;
        }

        int getMode(){
            return MODE;
        }

        int getProgress(){
            if (isSetData)
                return (int) ((float)mediaPlayer.getCurrentPosition() / (float)mediaPlayer.getDuration() * 100);

            return 0;
        }

        void seekTo(int progress) {
            if (isSetData) {
                mediaPlayer.seekTo(progress);
            }
        }

        long getDuration(){
            return mediaPlayer.getDuration();
        }

        void stopSSupdate(){
            isRun = false;
        }

        void startSSupdate(){
            isRun = true;
        }
    }

    private MusicBinder mBinder = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //将long值毫秒转成 00:00 格式
    public static String timeParse(long duration) {
        String time = "" ;
        long minute = duration / 60000 ;
        long seconds = duration % 60000 ;
        long second = Math.round((float)seconds/1000) ;

        if( minute < 10 ){
            time += "0" ;
        }
        time += minute+":" ;
        if( second < 10 ){
            time += "0" ;
        }
        time += second ;

        return time ;
    }
}
