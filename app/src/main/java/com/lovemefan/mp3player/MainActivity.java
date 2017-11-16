package com.lovemefan.mp3player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ServiceConnection {
    ArrayList<Music> musics = new ArrayList<>();
    MediaPlayer mediaPlayer;//媒体播放器
    ImageView playButton;//播放按钮
    SeekBar progressBar ;//进度条
    TextView title;//标题,用于显示歌名
    TextView currentTime;//显示当前播放的时间
    TextView duration;//显示歌曲时长
    Handler handler ;//用于其他线程更新
    CircleImageView cover;//显示歌曲的封面
    LinearLayout root;//根面板
    int musicProcess=0;//歌曲的进度;
    int musicCurIndex = 0;//当前播放的歌曲的下标
    Animation rotateAnimation ;

    //用于handle的handleMessage处理不同的事件
    static final int UpdateCurrentTime=0;//刷新当前播放时间标识符
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);;//去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_main);
        activityInit();//相关控件初始化
        bindService(new Intent(MainActivity.this,MusicPlayService.class), MainActivity.this, Context.BIND_AUTO_CREATE);//绑定服务

        musics.add(new Music("ninelie",R.drawable.nineliecover,R.raw.aimer_ninelie));
        musics.add(new Music("像风一样",R.drawable.linkwinds,R.raw.like_winds));
        musics.add(new Music("Let It Out",R.drawable.let_it_out,R.raw.let_it_out));
        refreshMusicInfo(musics.get(0));
        //
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                musicProcess = i;
                //自动播放下一曲
                if(mediaPlayer.getDuration() - musicProcess < 500){
                    musicCurIndex = (musicCurIndex + 1) % musics.size();
                    refreshMusicInfo(musics.get(musicCurIndex));
                    play();//播放音乐
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(musicProcess);//跳转到
            }
        });

        handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case UpdateCurrentTime:currentTime.setText(secondsToMinutes(mediaPlayer.getCurrentPosition()));break;
            }
            return true;
        }
        });
        findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

    }
    public void activityInit(){
        playButton = (ImageView) findViewById(R.id.playButton);
        progressBar = ( SeekBar) this.findViewById(R.id.progressBar);
        currentTime = (TextView) findViewById(R.id.curTime);
        duration = (TextView) findViewById(R.id.duration);
        title = (TextView) findViewById(R.id.musicName);
        cover = (CircleImageView) findViewById(R.id.musicCover);
        root = (LinearLayout) findViewById(R.id.root);
        rotateAnimation =  AnimationUtils.loadAnimation(this,R.anim.rotation);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new LinearInterpolator());

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(MainActivity.this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        System.out.println("绑定成功");
        mediaPlayer = ((MusicPlayService.MyBinder)iBinder).getMediaPlayer();
        progressBar.setMax(mediaPlayer.getDuration());//设置进度条
        duration.setText(""+secondsToMinutes(mediaPlayer.getDuration()));
        currentTime.setText("00:00");
        System.out.println(mediaPlayer.getDuration());

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
    public String secondsToMinutes(int seconds){
        int minute = seconds/60000;
        int second = (seconds/1000)%60;
        String  m = ""+ minute;
        String  s = "" + second;
        if (minute<10){
            m = "0" + minute;
        }
        if(second<10){
           s = "0" + second;
        }
        return m + ":" + s;
    }

    public void play() {
        if(mediaPlayer == null ){
            System.out.println("mediaplayer初始化失败");
        }else{
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                cover.clearAnimation();
                playButton.setImageResource(R.drawable.play);
            }else{
                mediaPlayer.start();

                cover.startAnimation(rotateAnimation);
                playButton.setImageResource(R.drawable.pause);
            }
            System.out.println(mediaPlayer.isPlaying());
            new MyThread().start();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void refreshMusicInfo(Music curMusic){
        title.setText(curMusic.getName());//设置歌曲名
        //        获取需要被模糊的原图bitmap
        Resources res = getResources();
        Bitmap scaledBitmap = BitmapFactory.decodeResource(res, curMusic.getCoverId());
        //        scaledBitmap为目标图像，25是缩放的倍数（越大模糊效果越高）
        Bitmap blurBitmap = BlurImage.toBlur(scaledBitmap, 50);
        root.setBackground(new BitmapDrawable(blurBitmap));//设置根面板背景
        cover.setImageResource(curMusic.getCoverId());//设置封面
        if(mediaPlayer == null){
            System.out.println("mediaplayer初始化失败");
        }else{
            try {
                AssetFileDescriptor afd = this.getResources().openRawResourceFd(curMusic.getResourceId());
                if (afd == null) {
                    Toast.makeText(this,"音乐打开失败",Toast.LENGTH_SHORT).show();
                }else{
                    mediaPlayer.stop();
                    final AudioAttributes aa = new AudioAttributes.Builder().build();
                    mediaPlayer.setAudioAttributes(aa);
                    mediaPlayer.setAudioSessionId(mediaPlayer.getAudioSessionId() + 1);
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());//设置音乐源
                    afd.close();//关闭文件
                    mediaPlayer.prepare();//音乐准备
                }
            } catch (IOException e) {
                Toast.makeText(this,"音乐播放失败",Toast.LENGTH_SHORT).show();
            }
                currentTime.setText("00:00");//设置开始时间
                duration.setText(secondsToMinutes(mediaPlayer.getDuration()));//获取音乐时长,并设置时间
                progressBar.setProgress(0);//将播放进度条清零
                playButton.setImageResource(R.drawable.play);//重新将按钮设置为播放
            }
        }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void previouMusic(View view) {
        //可实现循环切换
        musicCurIndex = (musicCurIndex + musics.size() -1) % musics.size() ;//加上music.size()防止出现负数
        refreshMusicInfo(musics.get(musicCurIndex));
        play();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void nextMusic(View view) {
        //可实现循环切换
        musicCurIndex = (musicCurIndex + 1) % musics.size();
        refreshMusicInfo(musics.get(musicCurIndex));
        play();
    }

    class MyThread extends Thread{
        public  void run(){
            while(mediaPlayer.isPlaying()){
                progressBar.setMax(mediaPlayer.getDuration());
                progressBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.sendMessage(Message.obtain());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
