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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service implements LifecycleOwner {
    private static int HeartRate = 0, Cadence = 0, NoiseFlag = 0, HRFlag = 1, CheckFlag = 0, CaCheckFlag = 0, i = 0, j = 0;
    private final static int BEGIN_AFTER = 1000, INTERVAL = 10000;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MusicReceiver receiver;
    private final Timer timer = new Timer();
    private final Handler mHandler = new Handler();
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
                MusicService.this.play();
                MusicService.this.BeatsPlay();
            }
            public void stop(){
                CheckFlag = 0;
                CaCheckFlag = 0;
                if (mplayer != null && mplayer.isPlaying()) {
                    if(HRFlag==1){FadeIn.volumeGradient(mplayer, 1f, 0);}
                    if(HRFlag==0){FadeIn.volumeGradient(mplayer, 0.3f, 0);}
                }
                if (player != null && player.isPlaying()) {
                    FadeIn.volumeGradient(player, 1, 0);}
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicService.this.MusicStop();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        unregisterReceiver(receiver);
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
                    } else {
                        if (Cadence < 80) {
                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b75);
                            mplayer.start();
                            FadeIn.volumeGradient(mplayer, 0, 1);
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                            CaCheckFlag = 1;
                            CheckFlag = 2;
                        } else {
                            if (Cadence < 85) {
                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b80);
                                mplayer.start();
                                FadeIn.volumeGradient(mplayer, 0, 1);
                                Intent intent = new Intent("action.Status");
                                intent.putExtra("Status","Well done! Keep your pace.");
                                sendBroadcast(intent);
                                CaCheckFlag = 1;
                                CheckFlag = 3;
                            } else {
                                if (Cadence < 90) {
                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b85);
                                    mplayer.start();
                                    FadeIn.volumeGradient(mplayer, 0, 1);
                                    Intent intent = new Intent("action.Status");
                                    intent.putExtra("Status","Well done! Keep your pace.");
                                    sendBroadcast(intent);
                                    CaCheckFlag = 1;
                                    CheckFlag = 4;
                                }
                                else {
                                    if (Cadence < 95) {
                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b90);
                                        mplayer.start();
                                        FadeIn.volumeGradient(mplayer, 0, 1);
                                        Intent intent = new Intent("action.Status");
                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                        sendBroadcast(intent);
                                        CaCheckFlag = 1;
                                        CheckFlag = 5;
                                    }
                                    else {
                                        if (Cadence < 100) {
                                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b95);
                                            mplayer.start();
                                            FadeIn.volumeGradient(mplayer, 0, 1);
                                            Intent intent = new Intent("action.Status");
                                            intent.putExtra("Status", "Well done! Keep your pace.");
                                            sendBroadcast(intent);
                                            CaCheckFlag = 1;
                                            CheckFlag = 6;
                                        }
                                        else {
                                            if (Cadence < 105) {
                                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b100);
                                                mplayer.start();
                                                FadeIn.volumeGradient(mplayer, 0, 1);
                                                Intent intent = new Intent("action.Status");
                                                intent.putExtra("Status", "Well done! Keep your pace.");
                                                sendBroadcast(intent);
                                                CaCheckFlag = 1;
                                                CheckFlag = 7;
                                            } else {
                                                if (Cadence < 110) {
                                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b105);
                                                    mplayer.start();
                                                    FadeIn.volumeGradient(mplayer, 0, 1);
                                                    Intent intent = new Intent("action.Status");
                                                    intent.putExtra("Status", "Well done! Keep your pace.");
                                                    sendBroadcast(intent);
                                                    CaCheckFlag = 1;
                                                    CheckFlag = 8;
                                                } else {
                                                    if (Cadence < 115) {
                                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b110);
                                                        mplayer.start();
                                                        FadeIn.volumeGradient(mplayer, 0, 1);
                                                        Intent intent = new Intent("action.Status");
                                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                                        sendBroadcast(intent);
                                                        CaCheckFlag = 1;
                                                        CheckFlag = 9;
                                                    } else {
                                                        if (Cadence < 120) {
                                                            mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b115);
                                                            mplayer.start();
                                                            FadeIn.volumeGradient(mplayer, 0, 1);
                                                            Intent intent = new Intent("action.Status");
                                                            intent.putExtra("Status", "Well done! Keep your pace.");
                                                            sendBroadcast(intent);
                                                            CaCheckFlag = 1;
                                                            CheckFlag = 10;
                                                        } else {
                                                            if (Cadence < 125) {
                                                                mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b120);
                                                                mplayer.start();
                                                                FadeIn.volumeGradient(mplayer, 0, 1);
                                                                Intent intent = new Intent("action.Status");
                                                                intent.putExtra("Status", "Well done! Keep your pace.");
                                                                sendBroadcast(intent);
                                                                CaCheckFlag = 1;
                                                                CheckFlag = 11;
                                                            } else {
                                                                if (Cadence < 130) {
                                                                    mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b125);
                                                                    mplayer.start();
                                                                    FadeIn.volumeGradient(mplayer, 0, 1);
                                                                    Intent intent = new Intent("action.Status");
                                                                    intent.putExtra("Status", "Well done! Keep your pace.");
                                                                    sendBroadcast(intent);
                                                                    CaCheckFlag = 1;
                                                                    CheckFlag = 12;
                                                                } else {
                                                                    if (Cadence < 135) {
                                                                        mplayer = MediaPlayer.create(getApplicationContext(), R.raw.b130);
                                                                        mplayer.start();
                                                                        FadeIn.volumeGradient(mplayer, 0, 1);
                                                                        Intent intent = new Intent("action.Status");
                                                                        intent.putExtra("Status", "Well done! Keep your pace.");
                                                                        sendBroadcast(intent);
                                                                        CaCheckFlag = 1;
                                                                        CheckFlag = 13;
                                                                    } else {
                                                                        Intent intent = new Intent("action.Status");
                                                                        intent.putExtra("Status", "Too fast! Walk slower.");
                                                                        sendBroadcast(intent);
                                                                        CaCheckFlag = 1;
                                                                        CheckFlag = 1;
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
                            CaCheckFlag = 0;
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
        if (player != null && player.isPlaying()){}
        else{
            if(i==0){
                player = MediaPlayer.create(this, R.raw.f2);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
            }

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           public void onCompletion(MediaPlayer mp) {
                i=i+1;
               player.reset();
               if(Cadence < 75){
               try {
                   player.setDataSource(String.valueOf(R.raw.warning));
                   player.prepare();
                   player.start();
               } catch (IOException e) {
                   e.printStackTrace();
               } }else
                   if(Cadence < 85 || Cadence > 135){
                   try {
                       player.setDataSource(String.valueOf(R.raw.f4));
                       player.prepare();
                       player.start();
                   } catch (IOException e) {
                       e.printStackTrace();
                   } }else
                   if(Cadence < 95){
                       try {
                           player.setDataSource(String.valueOf(R.raw.f6));
                           player.prepare();
                           player.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       } }
                   else
                   if(Cadence < 105){
                       try {
                           player.setDataSource(String.valueOf(R.raw.f8));
                           player.prepare();
                           player.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       } }else
                   if(Cadence < 115){
                       try {
                           player.setDataSource(String.valueOf(R.raw.f12));
                           player.prepare();
                           player.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       } }else
                   if(Cadence < 125){
                       try {
                           player.setDataSource(String.valueOf(R.raw.f16));
                           player.prepare();
                           player.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       } }else
                   if(Cadence < 135){
                       try {
                           player.setDataSource(String.valueOf(R.raw.f24));
                           player.prepare();
                           player.start();
                       } catch (IOException e) {
                           e.printStackTrace();
                       } }
           }
       }); }
    }

    public void setDataInMusic(){
        HeartRate = 0;
        Cadence = 0;
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
                      if(Cadence < 80){
                          NoiseFlag = 2;
                  }else
                      if(Cadence < 85){
                          NoiseFlag = 3;
                  }
                  else
                      if(Cadence < 90){
                          NoiseFlag = 4;
                  }
                      else
                      if(Cadence < 95){
                          NoiseFlag = 5;
                      }
                      else
                      if(Cadence < 100){
                          NoiseFlag = 6;
                      }
                      else
                      if(Cadence < 105){
                          NoiseFlag = 7;
                      }
                      else
                      if(Cadence < 110){
                          NoiseFlag = 8;
                      }
                      else
                      if(Cadence < 115){
                          NoiseFlag = 9;
                      }
                      else
                      if(Cadence < 120){
                          NoiseFlag = 10;
                      }
                      else
                      if(Cadence < 125){
                          NoiseFlag = 11;
                      }
                      else
                      if(Cadence < 130){
                          NoiseFlag = 12;
                      }
                      else
                      if(Cadence < 135){
                          NoiseFlag = 13;
                      }
                      else{
                          NoiseFlag = 1;
                      }
              /*      switch(MusicList){
                        case 1: if(Cadence<100 || Cadence>130){
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Cadence out of Range(100-130)!");
                            sendBroadcast(intent);
                            NoiseFlag = 1;
                        }else{
                            NoiseFlag = 0;
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                        }break;
                        case 2: if(Cadence<120 || Cadence>150){
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Cadence out of Range(130-150)!");
                            sendBroadcast(intent);
                            NoiseFlag = 1;
                        }else{
                            NoiseFlag = 0;
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                        }break;
                        case 3: if(Cadence<140 || Cadence>170){
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Cadence out of Range(150-170)!");
                            sendBroadcast(intent);
                            NoiseFlag = 1;
                        }else{
                            NoiseFlag = 0;
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                        }break;
                    }*/
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
                if(CheckFlag==NoiseFlag){}else{
                    CheckFlag=NoiseFlag;
                    if(NoiseFlag==1){
                        mplayer.reset();
                        player.reset();
                        try {
                            player.setDataSource(String.valueOf(R.raw.warning));
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(NoiseFlag==2){
                        mplayer.reset();
                        try {
                            mplayer.setDataSource(String.valueOf(R.raw.b75));
                            mplayer.prepare();
                            mplayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        player.reset();
                        try {
                            player.setDataSource(String.valueOf(R.raw.f4));
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
        }) ;
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
                if(mplayer != null && mplayer.isPlaying()){
                liveData.postValue(NoiseFlag);}
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



