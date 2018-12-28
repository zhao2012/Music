package com.example.administrator.day2_27;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

/**
 * Created by Administrator on 2018/2/27 0027.
 */

@Entity
public class Music {

    @Id
    private long id;

    @Index
    private String _display_name;
    private String url;
    private String album_id;
    private String title;
    private String artist;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void set_display_name(String _display_name) {
        this._display_name = _display_name;
    }

    public String get_display_name() {
        return _display_name;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
