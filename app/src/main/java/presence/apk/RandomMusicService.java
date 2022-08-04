package presence.apk;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.huawei.hihealthkit.data.HiHealthKitConstant;

import java.util.Timer;
import java.util.TimerTask;

public class RandomMusicService extends Service implements LifecycleOwner {
    private static int Cadence = 0, CheckFlag = 0, j = 0;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MediaPlayer bPlayer;
    private final Handler mHandler = new Handler();
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        return new MusicController();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    public class MusicController extends Binder {
            public void play(){
                RandomMusicService.this.play();
                RandomMusicService.this.BeatsPlay();
            }
            public void stop(){
                CheckFlag = 0;
                j=0;
                if (mplayer != null && mplayer.isPlaying()) {
                    FadeIn.volumeGradient(mplayer, 0.3f, 0);
                }
                if (player != null && player.isPlaying()) {
                    FadeIn.volumeGradient(player, 1, 0);}
                if (bPlayer != null && bPlayer.isPlaying()) {
                    FadeIn.volumeGradient(bPlayer, 1, 0);}
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RandomMusicService.this.MusicStop();
                    }
                },6000); // 延时6秒
            }
            public void RemoveCallBack(){
                RemoveMusicCallBack();
            }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        player = new MediaPlayer();
        mplayer = new MediaPlayer();
        bPlayer = new MediaPlayer();

    }

    public void MusicStop(){
        if (player != null && player.isPlaying()){
            try{
                player.stop();
                player.release();
                player = null;
            }catch(Exception e){
                e.printStackTrace();
            }}
        if (mplayer != null && mplayer.isPlaying()){
            try{
                mplayer.stop();
                mplayer.release();
                mplayer = null;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if (bPlayer != null && bPlayer.isPlaying()){
            try{
                bPlayer.stop();
                bPlayer.release();
                bPlayer = null;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    public void BeatsPlay(){
        if(mplayer != null && mplayer.isPlaying()){}else {
            Intent intent = new Intent("action.Status");
            intent.putExtra("Status","Adjust your pace to the most comfortable in 1 minute.");
            sendBroadcast(intent);
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    if (Cadence < 75) {
                        Intent intent = new Intent("action.Status");
                        intent.putExtra("Status","Too slow! Walk faster.");
                        sendBroadcast(intent);
                        CheckFlag = 1;
                        Intent intent1 = new Intent("action.CheckFlagStatus");
                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                        sendBroadcast(intent1);
                    } else {
                        if (Cadence < 80) {
                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b78);
                            mplayer.start();
                            FadeIn.volumeGradient(mplayer, 0, 0.3f);
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                            CheckFlag = 2;
                            Intent intent1 = new Intent("action.CheckFlagStatus");
                            intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                            sendBroadcast(intent1);
                        } else {
                            if (Cadence < 85) {
                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b83);
                                mplayer.start();
                                FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                Intent intent = new Intent("action.Status");
                                intent.putExtra("Status","Well done! Keep your pace.");
                                sendBroadcast(intent);
                                CheckFlag = 3;
                                Intent intent1 = new Intent("action.CheckFlagStatus");
                                intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                sendBroadcast(intent1);
                            } else {
                                if (Cadence < 90) {
                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b88);
                                    mplayer.start();
                                    FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                    Intent intent = new Intent("action.Status");
                                    intent.putExtra("Status","Well done! Keep your pace.");
                                    sendBroadcast(intent);
                                    CheckFlag = 4;
                                    Intent intent1 = new Intent("action.CheckFlagStatus");
                                    intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                    sendBroadcast(intent1);
                                }
                                else {
                                    if (Cadence < 95) {
                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b93);
                                        mplayer.start();
                                        FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                        Intent intent = new Intent("action.Status");
                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                        sendBroadcast(intent);
                                        CheckFlag = 5;
                                        Intent intent1 = new Intent("action.CheckFlagStatus");
                                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                        sendBroadcast(intent1);
                                    }
                                    else {
                                        if (Cadence < 100) {
                                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b98);
                                            mplayer.start();
                                            FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                            Intent intent = new Intent("action.Status");
                                            intent.putExtra("Status", "Well done! Keep your pace.");
                                            sendBroadcast(intent);
                                            CheckFlag = 6;
                                            Intent intent1 = new Intent("action.CheckFlagStatus");
                                            intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                            sendBroadcast(intent1);
                                        }
                                        else {
                                            if (Cadence < 105) {
                                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b103);
                                                mplayer.start();
                                                FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                Intent intent = new Intent("action.Status");
                                                intent.putExtra("Status", "Well done! Keep your pace.");
                                                sendBroadcast(intent);
                                                CheckFlag = 7;
                                                Intent intent1 = new Intent("action.CheckFlagStatus");
                                                intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                sendBroadcast(intent1);
                                            } else {
                                                if (Cadence < 110) {
                                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b108);
                                                    mplayer.start();
                                                    FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                    Intent intent = new Intent("action.Status");
                                                    intent.putExtra("Status", "Well done! Keep your pace.");
                                                    sendBroadcast(intent);
                                                    CheckFlag = 8;
                                                    Intent intent1 = new Intent("action.CheckFlagStatus");
                                                    intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                    sendBroadcast(intent1);
                                                } else {
                                                    if (Cadence < 115) {
                                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b113);
                                                        mplayer.start();
                                                        FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                        Intent intent = new Intent("action.Status");
                                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                                        sendBroadcast(intent);
                                                        CheckFlag = 9;
                                                        Intent intent1 = new Intent("action.CheckFlagStatus");
                                                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                        sendBroadcast(intent1);
                                                    } else {
                                                        if (Cadence < 120) {
                                                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b118);
                                                            mplayer.start();
                                                            FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                            Intent intent = new Intent("action.Status");
                                                            intent.putExtra("Status", "Well done! Keep your pace.");
                                                            sendBroadcast(intent);
                                                            CheckFlag = 10;
                                                            Intent intent1 = new Intent("action.CheckFlagStatus");
                                                            intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                            sendBroadcast(intent1);
                                                        } else {
                                                            if (Cadence < 125) {
                                                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b123);
                                                                mplayer.start();
                                                                FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                                Intent intent = new Intent("action.Status");
                                                                intent.putExtra("Status", "Well done! Keep your pace.");
                                                                sendBroadcast(intent);
                                                                CheckFlag = 11;
                                                                Intent intent1 = new Intent("action.CheckFlagStatus");
                                                                intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                                sendBroadcast(intent1);
                                                            } else {
                                                                if (Cadence < 130) {
                                                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b128);
                                                                    mplayer.start();
                                                                    FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                                    Intent intent = new Intent("action.Status");
                                                                    intent.putExtra("Status", "Well done! Keep your pace.");
                                                                    sendBroadcast(intent);
                                                                    CheckFlag = 12;
                                                                    Intent intent1 = new Intent("action.CheckFlagStatus");
                                                                    intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                                    sendBroadcast(intent1);
                                                                } else {
                                                                    if (Cadence < 135) {
                                                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b133);
                                                                        mplayer.start();
                                                                        FadeIn.volumeGradient(mplayer, 0, 0.3f);
                                                                        Intent intent = new Intent("action.Status");
                                                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                                                        sendBroadcast(intent);
                                                                        CheckFlag = 13;
                                                                        Intent intent1 = new Intent("action.CheckFlagStatus");
                                                                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                                        sendBroadcast(intent1);
                                                                    } else {
                                                                        Intent intent = new Intent("action.Status");
                                                                        intent.putExtra("Status", "Too fast! Walk slower.");
                                                                        sendBroadcast(intent);
                                                                        CheckFlag = 1;
                                                                        Intent intent1 = new Intent("action.CheckFlagStatus");
                                                                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                                        sendBroadcast(intent1);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            j = j + 1;
                            mplayer.stop();
                            mplayer.release();
                            mplayer = null;
                            if (j < 2) {
                                BeatsPlay();
                            }
                        }
                    });
                }
            };
            mHandler.postDelayed(mRun, 60000);
        }
    }

    public void RemoveMusicCallBack(){
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void play(){
        if (bPlayer != null && bPlayer.isPlaying()){}
        else {
            bPlayer = MediaPlayer.create(this, R.raw.f2);
            bPlayer.start();
            FadeIn.volumeGradient(bPlayer, 0, 1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FadeIn.volumeGradient(bPlayer, 1, 0);
                    if (Cadence < 75 || Cadence > 135) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.warning);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else if (Cadence < 85) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f4);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else if (Cadence < 95) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f6);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else if (Cadence < 105) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f8);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else if (Cadence < 115) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f12);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else if (Cadence < 125) {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f16);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    } else {
                        player = MediaPlayer.create(getApplicationContext(), R.raw.f24);
                        player.start();
                        FadeIn.volumeGradient(player, 0, 1);
                    }
                }
            }, 54000); // 延时6秒
        }
    }

}



