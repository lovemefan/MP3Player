package com.lovemefan.mp3player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;

public class MusicPlayService extends Service {
    private MediaPlayer mediaPlayer = null;
    public MusicPlayService() {
    }
    class MyBinder extends Binder{
        public MediaPlayer getMediaPlayer(){
            return mediaPlayer;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }
    @Override
    public void onCreate() {
        mediaPlayer = MediaPlayer.create(this,R.raw.aimer_ninelie);//设置初始值
//        mediaPlayer.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
