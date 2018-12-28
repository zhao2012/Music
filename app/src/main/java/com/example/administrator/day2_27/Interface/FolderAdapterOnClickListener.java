package com.example.administrator.day2_27.Interface;

import java.io.File;

/**
 * Created by Administrator on 2018/3/2 0002.
 */

public interface FolderAdapterOnClickListener {
    void insideStorage(File file);

    void externalStorage();

    void normalClick(File file);
}
