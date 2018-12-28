package com.example.administrator.day2_27.Interface;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public interface SongAdapterOnClickListener {
    void changeAlbumImage(String albumId);

    void changeSongName(String songName);

    void changeSongSinger(String singer);

    void changePlayPauseImage(boolean isPlaying);

    void playMusic(int position);

    void showEditTDialog(int position);
}
