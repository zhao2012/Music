package com.example.administrator.day2_27.Util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.administrator.day2_27.MainActivity;
import com.example.administrator.day2_27.Music;

import java.io.File;

import io.objectbox.Box;

/**
 * Created by Administrator on 2018/3/1 0001.
 */

public class MusicUtil {
    private static final String TAG = "MusicUtil";

    /**
     * 判断文件是否存在
     * @param path 文件的路径
     * @return
     */
    private static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     *筛选一下铃声等，过滤一下
     * @param songName
     * @return
     */
    private static boolean filter(String songName){
        return songName.contains(".amr")
                || songName.contains(".ogg");
    }

    /**
     * 获取指定文件夹的歌曲
     * @param context
     * @param dest 指定文件夹路径
     *              当dest为null时，则扫描整机
     * @return
     */
    public static int getDesignatedPathMusic(Context context,String dest){
        Cursor c = null;
        int count = 0;

        Box<Music> box = MainActivity.getBoxStore().boxFor(Music.class);

        try {

            //强制扫描内置SD卡而已 更新
            MediaScannerConnection.scanFile(context, new String[] { Environment
                    .getExternalStorageDirectory().getAbsolutePath() }, null, null);

            c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            while (c != null && c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径

                if (dest != null) {
                    if (!path.contains(dest)) {
                        //如果歌曲的路径不包含指定路径dest,就跳过该歌曲
                        continue;
                    }
                }


                if (!isExists(path)) {
                    //这种情况是手机数据库表格有
                    //但是实际SD卡已经不存在了，删除了
                    //手机数据库没更新
                    continue;
                }

                boolean isAdded = false;
                for (Music m : box.getAll()) {
                    if (m.getUrl().equals(path)){
                        //存在同路径的文件，所以为了防止重复添加就跳过这个
                        isAdded = true;
                        break;
                    }
                }

                //先过滤
                String fileName = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));//文件名
                if (filter(fileName)) {
                    continue;
                }

                if (!isAdded) {
                    //遍历应用数据库还是没有
                    String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)); // 歌曲名
                    String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
                    String albumId = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑ID

                    Music music = new Music();
                    music.setTitle(name);
                    music.setArtist(artist);
                    music.set_display_name(fileName);
                    music.setAlbum_id(albumId);
                    music.setUrl(path);

                    box.put(music);

                    count++;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return count;
    }
}
