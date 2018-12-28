package com.example.administrator.day2_27.EntityClass;

import android.widget.CheckBox;

/**
 * Created by Administrator on 2018/3/3 0003.
 */

public class MultiSelectItem {
    private String songName;
    private String singer;
    private String songUrl;
    private boolean select;

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongName() {
        return songName;
    }

    public String getSinger() {
        return singer;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public boolean getCheckBox(){
        return select;
    }
}
