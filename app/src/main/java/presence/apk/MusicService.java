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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service implements LifecycleOwner {
    private static int MusicList = 0, HeartRate = 0, Cadence = 0, NoiseFlag = 0, HRFlag = 1, i = 0, j = 0;
    private final static int BEGIN_AFTER = 1000, INTERVAL = 10000;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MediaPlayer bPlayer;
    private MusicReceiver receiver;
    ArrayList<Integer> NoiseList, MusicList1, MusicList2, MusicList3;
    private Timer timer = new Timer();
    private Handler mHandler = new Handler();
    MusicServiceViewModel musicServiceViewModel = new MusicServiceViewModel();
    MusicServiceCaViewModel musicServiceCaViewModel = new MusicServiceCaViewModel();
    NoiseFlagViewModel noiseFlagViewModel = new NoiseFlagViewModel();
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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
                MusicService.this.MusicPlay();
            }
            public void stop(){
                if (mplayer != null && mplayer.isPlaying()) {
                    if(HRFlag==1){FadeIn.volumeGradient(mplayer, 1f, 0);}
                    if(HRFlag==0){FadeIn.volumeGradient(mplayer, 0.3f, 0);}
                }
                if (bPlayer != null && bPlayer.isPlaying()) {
                    FadeIn.volumeGradient(bPlayer, 1, 0);}
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
        iniComponent();
        CaIniComponent();
        FlagIniComponent();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        receiver = new MusicReceiver();
        IntentFilter filter = new IntentFilter("action.sport");
        this.registerReceiver(receiver, filter);

        player = new MediaPlayer();
        mplayer = new MediaPlayer();

        NoiseList = new ArrayList<>();
        NoiseList.add(R.raw.oceanwaves);
        NoiseList.add(R.raw.river);
        NoiseList.add(R.raw.thunder);
        Collections.shuffle(NoiseList);
        MusicList1 = new ArrayList<>();
        MusicList1.add(R.raw.miles);
        MusicList1.add(R.raw.jazen120);
        MusicList1.add(R.raw.tearscity);
        MusicList1.add(R.raw.kumamoto);
        Collections.shuffle(MusicList1);
        MusicList2 = new ArrayList<>();
        MusicList2.add(R.raw.japannight);
        MusicList2.add(R.raw.jazen132);
        MusicList2.add(R.raw.monet);
        Collections.shuffle(MusicList2);
        MusicList3 = new ArrayList<>();
        MusicList3.add(R.raw.solarechoes);
        MusicList3.add(R.raw.jazen150);
        MusicList3.add(R.raw.ori);
        Collections.shuffle(MusicList3);
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

    public void MusicStopRunning(){
        Intent intent = new Intent("action.StopRunning");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        unregisterReceiver(receiver);
    }

    public void MusicPlay(){
        if(mplayer != null && mplayer.isPlaying()){}else {
            Intent intent = new Intent("action.Status");
            intent.putExtra("Status","Adjust your pace to the most comfortable in 1 minute.");
            sendBroadcast(intent);
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    if (HeartRate < 100) {
                        Intent intent = new Intent("action.Status");
                        intent.putExtra("Status","Too slow! Run faster.");
                        sendBroadcast(intent);
                        if (player != null && player.isPlaying()) {
                            MusicStopRunning();
                        }
                        if (bPlayer != null && bPlayer.isPlaying()) {
                            MusicStopRunning();
                        }
                    } else {
                        if (HeartRate < 130) {
                            mplayer = MediaPlayer.create(getApplicationContext(), MusicList1.get(j));
                            mplayer.start();
                            FadeIn.volumeGradient(mplayer, 0, 1);
                            MusicList = 1;
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);
                        } else {
                            if (HeartRate < 150) {
                                mplayer = MediaPlayer.create(getApplicationContext(), MusicList2.get(j));
                                mplayer.start();
                                FadeIn.volumeGradient(mplayer, 0, 1);
                                MusicList = 2;
                                Intent intent = new Intent("action.Status");
                                intent.putExtra("Status","Well done! Keep your pace.");
                                sendBroadcast(intent);
                            } else {
                                if (HeartRate < 170) {
                                    mplayer = MediaPlayer.create(getApplicationContext(), MusicList3.get(j));
                                    mplayer.start();
                                    FadeIn.volumeGradient(mplayer, 0, 1);
                                    MusicList = 3;
                                    Intent intent = new Intent("action.Status");
                                    intent.putExtra("Status","Well done! Keep your pace.");
                                    sendBroadcast(intent);
                                } else {
                                    Intent intent = new Intent("action.Status");
                                    intent.putExtra("Status","Too fast! Run slower.");
                                    sendBroadcast(intent);
                                    if (player != null && player.isPlaying()) {
                                        MusicStopRunning();
                                    }
                                    if (bPlayer != null && bPlayer.isPlaying()) {
                                        MusicStopRunning();
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
                            if (j < 3) {
                                MusicPlay();
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
        if (player != null && player.isPlaying()){}else{
            if(i==3){i=0;}
            player = MediaPlayer.create(this, NoiseList.get(i));
            player.start();
            FadeIn.volumeGradient(player, 0, 1);

       player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           public void onCompletion(MediaPlayer mp) {
                i=i+1;
                player.stop();
                player.release();
                player = null;
                play();
           }
       }); }
    }

    public void setDataInMusic(){
        HeartRate = 0;
        Cadence = 0;
    }

    private void iniComponent()
    {
        //通过.observe()实现对ViewModel中数据变化的观察
        MusicServiceViewModel.getsInstance().getCurrentHR().observe(this, new Observer<Integer>()
        {
            @Override
            public void onChanged(@Nullable Integer HR)
            {
                if(mplayer != null && mplayer.isPlaying()){
                    switch(MusicList){
                        case 1: if(HR<100 || HR>130){
                            Intent intent = new Intent("action.HRStatus");
                            intent.putExtra("HRStatus","Heart rate out of range(100-130)! Retry.");
                            sendBroadcast(intent);
                            MusicStopRunning();
                        }else if(HR<125 && HR>105){
                            if(HRFlag==0){FadeIn.volumeGradient(mplayer, 0.3f, 1f); }
                            HRFlag=1;}
                            else{
                            if(HRFlag==1){FadeIn.volumeGradient(mplayer, 1f, 0.3f);}
                            HRFlag=0;}
                        break;
                        case 2: if(HR<120 || HR>150){
                            Intent intent = new Intent("action.HRStatus");
                            intent.putExtra("HRStatus","Heart rate out of range(130-150)! Retry.");
                            sendBroadcast(intent);
                            MusicStopRunning();
                        }else if(HR<145 && HR>125){
                            if(HRFlag==0){FadeIn.volumeGradient(mplayer, 0.3f, 1f); }
                            HRFlag=1;}
                        else{
                            if(HRFlag==1){FadeIn.volumeGradient(mplayer, 1f, 0.3f);}
                            HRFlag=0;}
                        break;
                        case 3: if(HR<140 || HR>170){
                            Intent intent = new Intent("action.HRStatus");
                            intent.putExtra("HRStatus","Heart rate out of range(150-170)! Retry.");
                            sendBroadcast(intent);
                            MusicStopRunning();
                        }else if(HR<165 && HR>145){
                            if(HRFlag==0){FadeIn.volumeGradient(mplayer, 0.3f, 1f); }
                            HRFlag=1;}
                        else{
                            if(HRFlag==1){FadeIn.volumeGradient(mplayer, 1f, 0.3f);}
                            HRFlag=0;}
                        break;
                    }
                }
            }
        });
        startPost();
    }

    private void CaIniComponent()
    {
        //通过.observe()实现对ViewModel中数据变化的观察
        MusicServiceCaViewModel.getsInstance().getCurrentCa().observe(this, new Observer<Integer>()
        {
            @Override
            public void onChanged(@Nullable Integer Cadence)
            {
                if(mplayer != null && mplayer.isPlaying()){
                    switch(MusicList){
                        case 1: if(Cadence<100 || Cadence>130){
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Cadence out of Range(100-130)!");
                            sendBroadcast(intent);
                            NoiseFlag = 1;
                        }else{
                            NoiseFlag = 0;
                            Intent intent = new Intent("action.Status");
                            intent.putExtra("Status","Well done! Keep your pace.");
                            sendBroadcast(intent);;
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
                    }
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
                switch (Flag){
                    case 1: if (bPlayer != null && bPlayer.isPlaying()){}else{
                        FadeIn.volumeGradient(player, 1, 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    player.stop();
                                    player.release();
                                    player = null;
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.brownoise);
                                bPlayer.start();
                                bPlayer.setLooping(true);
                            }
                        },6000); // 延时6秒
                }
                break;
                    case 0:if(bPlayer != null && bPlayer.isPlaying()){FadeIn.volumeGradient(bPlayer, 1, 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    bPlayer.stop();
                                    bPlayer.release();
                                    bPlayer = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },6000); // 延时6秒
                        play();
                        break;}
            }
        }
        }) ;
        FlagStartPost();
    }

    public void startPost(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                musicServiceViewModel =  MusicServiceViewModel.getsInstance();
                final MutableLiveData<Integer> liveData = (MutableLiveData<Integer>)musicServiceViewModel.getCurrentHR();
                liveData.postValue(HeartRate);
            }
        }, BEGIN_AFTER, INTERVAL);
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



