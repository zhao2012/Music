package com.example.administrator.day2_27.EntityClass;

import java.io.File;

/**
 * Created by Administrator on 2018/3/2 0002.
 */

public class Folder {
    private String folder_name;
    private File folderFile;

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public void setFolderFile(File folderFile) {
        this.folderFile = folderFile;
    }

    public File getFolderFile() {
        return folderFile;
    }
}
