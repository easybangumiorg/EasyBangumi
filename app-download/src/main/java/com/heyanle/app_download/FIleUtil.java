package com.heyanle.app_download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;

/**
 * Created by heyanlin on 2023/8/29.
 */
public class FIleUtil {

    public static boolean mergeFile(String targetPath, List<String> subPaths) {
        //Log.d(TAG, "开始合并文件");
        File file = new File(targetPath);
        FileOutputStream fos = null;
        FileChannel foc = null;
        long startTime = System.currentTimeMillis();
        try {
            if (file.exists() && file.isDirectory()) {
                //ALog.w(TAG, String.format("路径【%s】是文件夹，将删除该文件夹", targetPath));
                FileUtil.deleteDir(file);
            }
            if (!file.exists()) {
                FileUtil.createFile(file);
            }

            fos = new FileOutputStream(targetPath);
            foc = fos.getChannel();
            List<FileInputStream> streams = new LinkedList<>();
            long fileLen = 0;
            for (String subPath : subPaths) {
                File f = new File(subPath);
                if (!f.exists()) {
                    //ALog.d(TAG, String.format("合并文件失败，文件【%s】不存在", subPath));
                    for (FileInputStream fis : streams) {
                        fis.close();
                    }
                    streams.clear();

                    return false;
                }
                FileInputStream fis = new FileInputStream(subPath);
                FileChannel fic = fis.getChannel();
                foc.transferFrom(fic, fileLen, f.length());
                fileLen += f.length();
                fis.close();
            }
            //ALog.d(TAG, String.format("合并文件耗时：%sms", (System.currentTimeMillis() - startTime)));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (foc != null) {
                    foc.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
