package com.hwp.huiweipu;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.hwp.huiweipu.utils.FileUtil;

import java.util.ArrayList;
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

import static com.hwp.huiweipu.R.id.video_view;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, View.OnTouchListener {

    private EditText editText;
    private Context mContext;
    private Button button;
    private VideoView videoView;
    private ProgressBar progressBar;
    private int viewtype = 2;//1.全屏 2.窗口
    private int intPositionWhenPause;//记录播放位置
    private boolean showDanmaku;
    private DanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;

    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initVideoView();
    }


    private void initView() {
        findViewById(R.id.btn_send).setOnClickListener(this);
        editText = (EditText) findViewById(R.id.et_content);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        mDanmakuView.enableDanmakuDrawingCache(true);
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });

        mDanmakuContext = DanmakuContext.create();
        mDanmakuView.prepare(parser, mDanmakuContext);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });


    }

    private void initVideoView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        videoView = (VideoView) findViewById(video_view);
        //初始化videoview控制条
        MediaController mediaController = new MediaController(this);
        //设置videoview的控制条
        videoView.setMediaController(mediaController);
        //设置显示控制条
        mediaController.show(0);
        //设置播放完成以后监听
        videoView.setOnCompletionListener(this);
        //设置发生错误监听，如果不设置videoview会向用户提示发生错误
        videoView.setOnErrorListener(this);
        //设置在视频文件在加载完毕以后的回调函数
        videoView.setOnPreparedListener(this);
        //设置videoView的点击监听
        videoView.setOnTouchListener(this);

        ArrayList<FileUtil.VideoInfo> videoInfo = FileUtil.getVideoInfo(mContext);
        Log.i("TAG", "filePath1"+videoInfo.get(3).filePath);
        videoView.setVideoPath(videoInfo.get(3).filePath);
    }



    @Override
    public void onClick(View view) {
        String content = editText.getText().toString();
        if(!TextUtils.isEmpty(content)){
            addDanmaku(content, true);
            editText.setText("");
        }
    }


    /**
     * 向弹幕View中添加一条弹幕
     * @param content
     *          弹幕的具体内容
     * @param  withBorder
     *          弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        mDanmakuView.addDanmaku(danmaku);
    }


    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "弹幕" + time;
                    addDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * 设置videiview的全屏和窗口模式
     * @param paramsType 标识 1为全屏模式 2为窗口模式
     */
    public void setVideoViewLayoutParams(int paramsType){

        if(paramsType == 1){/**********全屏模式***********/
            //设置充满整个父布局
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            //设置相对于父布局四边对齐
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            videoView.setLayoutParams(layoutParams);
        }else{/**********窗口模式***********/
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            //设置窗口模式距离边框50
            int videoWidth = displayMetrics.widthPixels - 50;
            int videoHeight = displayMetrics.heightPixels - 50;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(videoWidth,videoHeight);
            videoView.setLayoutParams(layoutParams);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //启动视频播放
        videoView.start();
        //设置获取焦点
        videoView.setFocusable(true);
    }

    /**
     * 页面暂停效果处理
     */
    @Override
    protected void onPause() {
        super.onPause();
        //如果当前页面暂停则保存当前播放位置，全局变量保存
        intPositionWhenPause=videoView.getCurrentPosition();
        //停止回放视频文件
        videoView.stopPlayback();
    }


    /**
     * 页面从暂停中恢复
     */
    @Override
    protected void onResume() {
        super.onResume();
        //跳转到暂停时保存的位置
        if(intPositionWhenPause > 0){
            videoView.seekTo(intPositionWhenPause);
            //初始播放位置
            intPositionWhenPause=-1;
        }
    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        videoView.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }


    /**
     * 视频文件加载文成后调用的回调函数
     * @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //如果文件加载成功,隐藏加载进度条
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(viewtype == 1){
            viewtype = 2;
        }else{
            viewtype = 1;
        }
        setVideoViewLayoutParams(viewtype);
        return false;
    }
}
