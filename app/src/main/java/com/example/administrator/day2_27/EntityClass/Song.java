package com.example.administrator.day2_27.EntityClass;

/**
 * Created by Administrator on 2018/2/27 0027.
 */

public class Song {
    private String songName;
    private String singer;
    private String songFileName;
    private String album_id;
    private String songPath;

    public Song(String songName,String singer,String songFileName,String album_id,String songPath) {
        super();
        this.songName = songName;
        this.singer = singer;
        this.songFileName = songFileName;
        this.album_id = album_id;
        this.songPath = songPath;
    }

    public String getSongName() {
        return songName;
    }

    public String getSinger() {
        return singer;
    }

    public String getSongFileName() {
        return songFileName;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public String getSongPath() {
        return songPath;
    }
}
