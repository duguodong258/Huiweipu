package com.hwp.huiweipu.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * @author 咸鱼
 * @date 2016/12/30 0030
 * @des ${TODO}
 */

public class FileUtil {

    public static Uri VIDEOURI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;//视频
    public static Uri AUDIOURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//音频
    public static Uri IMAGEURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//图片

//    public static void getVideoInfo(Context context,String tag){
//        String[] projections = new String[]{MediaStore.Video.VideoColumns.ALBUM,MediaStore.Video.VideoColumns.ARTIST};
//        Cursor cursor = context.getContentResolver().query(VIDEOURI, projections, null, null, null);
//        int albumIndex = cursor.getColumnIndexOrThrow(projections[0]);
//        int titleIndex = cursor.getColumnIndexOrThrow(projections[1]);
////        Log.i("TAG", "albumIndex: "+ albumIndex);
////        Log.i("TAG", "titleIndex: "+ titleIndex);
//        Log.i("TAG", "cursor.getCount(): "+ cursor.getCount());
//        while(cursor.moveToNext()) {
//            String album = cursor.getString(albumIndex);
//            String title = cursor.getString(titleIndex);
//            Log.i("TAG", album+" : "+title);
//        }
//        cursor.close();
//    }


    /**
     * 获得所有mp4 3gp格式视频文件（videoView只支持这些格式）
     * @param context
     */
    public static ArrayList<VideoInfo> getVideoInfo(Context context){
        String[] thumbColumns = new String[]{
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };

        String[] mediaColumns = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE
        };

        //首先检索SDcard上所有的video
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
        ArrayList<VideoInfo> videoList = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                VideoInfo info = new VideoInfo();

                info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                info.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));

                //获取当前Video对应的Id，然后根据该ID获取其Thumb
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
                String[] selectionArgs = new String[]{
                        id+""
                };
                Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);

                if(thumbCursor.moveToFirst()){
                    info.thumbPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                }
                if(info.filePath.endsWith(".mp4") || info.filePath.endsWith(".3gp")){
                    //然后将其加入到videoList
                    videoList.add(info);
                }
            }while(cursor.moveToNext());
        }
        return videoList;
    }

    public static class VideoInfo{
        public String filePath;
        public String mimeType;
        public String thumbPath;
        public String title;
    }
}
