package presence.apk;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class ConstantMusicService extends Service implements LifecycleOwner {
    private static int HeartRate = 0, Cadence = 0, NoiseFlag = 0, CheckFlag = 0, CaCheckFlag = 0, i = 0, j = 0;
    private final static int BEGIN_AFTER = 1000, INTERVAL = 10000;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MediaPlayer bPlayer;
    private SoundPool soundpool;
    private int hurryup, slowdown;
    private MusicReceiver receiver;
    private final Timer timer = new Timer();
    private final Handler mHandler = new Handler();
    private final Handler nHandler = new Handler();
    MusicServiceCaViewModel musicServiceCaViewModel = new MusicServiceCaViewModel();
    NoiseFlagViewModel noiseFlagViewModel = new NoiseFlagViewModel();
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
            ConstantMusicService.this.play();
            ConstantMusicService.this.BeatsPlay();
        }
        public void stop(){
            CaCheckFlag = 0;
            CheckFlag = 0;
            NoiseFlag = 0;
            i=0;
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
                    ConstantMusicService.this.MusicStop();
                }
            },6000); // 延时6秒
        }
        public void setData(){ setDataInMusic(); }
        public void RemoveCallBack(){
            RemoveMusicCallBack();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CaIniComponent();
        FlagIniComponent();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        receiver = new MusicReceiver();
        IntentFilter filter = new IntentFilter("action.sport");
        this.registerReceiver(receiver, filter);

        player = new MediaPlayer();
        mplayer = new MediaPlayer();
        bPlayer = new MediaPlayer();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
        soundpool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build();
        hurryup = soundpool.load(getApplicationContext(),R.raw.hurry,1);
        slowdown = soundpool.load(getApplicationContext(),R.raw.slow,1);
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
        unregisterReceiver(receiver);
        soundpool.release();
        soundpool = null;
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
                        CaCheckFlag = 1;
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
                            CaCheckFlag = 1;
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
                                CaCheckFlag = 1;
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
                                    CaCheckFlag = 1;
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
                                        CaCheckFlag = 1;
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
                                            CaCheckFlag = 1;
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
                                                CaCheckFlag = 1;
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
                                                    CaCheckFlag = 1;
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
                                                        CaCheckFlag = 1;
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
                                                            CaCheckFlag = 1;
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
                                                                CaCheckFlag = 1;
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
                                                                    CaCheckFlag = 1;
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
                                                                        CaCheckFlag = 1;
                                                                        CheckFlag = 13;
                                                                        Intent intent1 = new Intent("action.CheckFlagStatus");
                                                                        intent1.putExtra("CheckFlagStatus", String.valueOf(CheckFlag));
                                                                        sendBroadcast(intent1);
                                                                    } else {
                                                                        Intent intent = new Intent("action.Status");
                                                                        intent.putExtra("Status", "Too fast! Walk slower.");
                                                                        sendBroadcast(intent);
                                                                        CaCheckFlag = 1;
                                                                        CheckFlag = 14;
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
                    SetBeatsOnCompletionListener();
                }
            };
            mHandler.postDelayed(mRun, 60000);
        }
    }

    public void SetBeatsOnCompletionListener(){
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                j = j + 1;
                if (j < 9) {
                    BeatsRepeat();
                }else{
                    mplayer.stop();
                    mplayer.release();
                    mplayer = null;
                    CaCheckFlag = 0;
                }
            }
        });
    }

    public void BeatsRepeat(){
        switch(CheckFlag){
            case 2: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b78);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 3: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b83);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 4: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b88);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 5: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b93);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 6: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b98);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 7: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b103);
                    mplayer.setVolume(0.3f,0.3f);
                    mplayer.start();
                    SetBeatsOnCompletionListener();
                    break;
            case 8:  mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b108);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
            case 9:  mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b113);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
            case 10: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b118);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
            case 11: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b123);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
            case 12: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b128);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
            case 13: mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b133);
                     mplayer.setVolume(0.3f,0.3f);
                     mplayer.start();
                     SetBeatsOnCompletionListener();
                     break;
        }
    }

    public void RemoveMusicCallBack(){
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(nHandler != null) {
            nHandler.removeCallbacksAndMessages(null);
        }
    }

    public void play(){
        if (bPlayer != null && bPlayer.isPlaying()){}
        else {
            bPlayer = MediaPlayer.create(this, R.raw.f2);
            bPlayer.start();
            FadeIn.volumeGradient(bPlayer, 0, 1);
            Runnable nRun = new Runnable() {
                @Override
                public void run() {
                    FadeIn.volumeGradient(bPlayer, 1, 0);
                    NoiseSelect();
                }
            };
            nHandler.postDelayed(nRun, 60000);
        }
    }

    public void setDataInMusic(){
        HeartRate = 0;
        Cadence = 0;
    }

    public void NoiseSelect(){
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
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                i = i + 1;
                if (i < 2) {
                    NoiseSelect();
                }else{
                    player.stop();
                    player.release();
                    player = null;
                    CaCheckFlag = 0;
                }
            }
        });
    }

    private void CaIniComponent()
    {
        //通过.observe()实现对ViewModel中数据变化的观察
        MusicServiceCaViewModel.getsInstance().getCurrentCa().observe(this, new Observer<Integer>()
        {
            @Override
            public void onChanged(@Nullable Integer Cadence)
            {
                if(CaCheckFlag == 1){
                    if (Cadence < 75){
                        NoiseFlag = 1;
                    }else
                    if(Cadence < 85){
                        NoiseFlag = 3;
                    }
                    else
                    if(Cadence < 95){
                        NoiseFlag = 5;
                    }
                    else
                    if(Cadence < 105){
                        NoiseFlag = 7;
                    }
                    else
                    if(Cadence < 115){
                        NoiseFlag = 9;
                    }
                    else
                    if(Cadence < 125){
                        NoiseFlag = 11;
                    }
                    else
                    if(Cadence < 135){
                        NoiseFlag = 13;
                    }
                    else{
                        NoiseFlag = 14;
                    }
                    Intent intent = new Intent("action.NoiseFlagStatus");
                    intent.putExtra("NoiseFlagStatus", String.valueOf(NoiseFlag));
                    sendBroadcast(intent);
                }
            }
        });
        CaStartPost();
    }

    private void FlagIniComponent()
    {
        //通过.observe()实现对ViewModel中数据变化的观察
        NoiseFlagViewModel.getsInstance().getCurrentFlag().observe(this, new Observer<Integer>()
        {
            @Override
            public void onChanged(@Nullable Integer Flag)
            {
                if(CaCheckFlag==1){
                    if(CheckFlag>NoiseFlag){
                        soundpool.play(hurryup,1,1,0,0,1);
                    }
                        else if(CheckFlag==NoiseFlag){}
                            else {
                             soundpool.play(slowdown,1,1,0,0,1);
                            }
                }
            }
        });
        FlagStartPost();
    }


    public void CaStartPost(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                musicServiceCaViewModel =  MusicServiceCaViewModel.getsInstance();
                final MutableLiveData<Integer> liveData = (MutableLiveData<Integer>)musicServiceCaViewModel.getCurrentCa();
                liveData.postValue(Cadence);
            }
        }, BEGIN_AFTER, INTERVAL);
    }

    public void FlagStartPost(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                noiseFlagViewModel =  NoiseFlagViewModel.getsInstance();
                final MutableLiveData<Integer> liveData = (MutableLiveData<Integer>)noiseFlagViewModel.getCurrentFlag();
                liveData.postValue(NoiseFlag);
            }
        }, BEGIN_AFTER, INTERVAL);
    }

    private static class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.sport".equals(intent.getAction())) {
                Cadence = intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE, 0);
                HeartRate = intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE, 0);
                Log.d(TAG, " Music HeartRate: " + HeartRate);
                Log.d(TAG, " Music Cadence: " + Cadence);
            }
        }
    }
}




