package presence.apk;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.ValueAnimator;
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
import android.view.animation.LinearInterpolator;

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

import presence.apk.ui.home.HomeFragment;

public class MusicService extends Service implements LifecycleOwner {
    private int i = 0;
    private int j = 0;
    private static int MusicList = 0;
    private static int HeartRate = 0;
    private static int Cadence = 0;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MusicReceiver receiver;
    ArrayList<Integer> NoiseList;
    ArrayList<Integer> MusicList1;
    ArrayList<Integer> MusicList2;
    ArrayList<Integer> MusicList3;
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
                    FadeIn.volumeGradient(mplayer, 1, 0);}
                if (player != null && player.isPlaying()) {
                    FadeIn.volumeGradient(player, 1, 0);}
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicService.this.MusicStop();
                    }
                },6000); // 延时6秒
            }
            public void setData(){setDataInMusic();}

    }

    @Override
    public void onCreate() {
        super.onCreate();
        HRListen();
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
 //       MusicList1.add(R.raw.river);
        Collections.shuffle(MusicList1);
        MusicList2 = new ArrayList<>();
        MusicList2.add(R.raw.japannight);
 //       MusicList2.add(R.raw.river);
        Collections.shuffle(MusicList2);
        MusicList3 = new ArrayList<>();
        MusicList3.add(R.raw.solarechoes);
 //       MusicList3.add(R.raw.river);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        unregisterReceiver(receiver);
    }

    public void MusicPlay(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(HeartRate<100){
                    if (player != null && player.isPlaying()) {
                        FadeIn.volumeGradient(player, 1, 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                player.stop();
                                player.release();
                                player = null;
                                HomeFragment HomeController = new HomeFragment();
                                SportService SportController = new SportService();
                                SportController.onStopCommand();
                                HomeController.ReleaseWakeLock();
                                HomeController.resetTimer();
                                HomeController.SetTextToNull();
                            }
                        },6000); // 延时6秒
                    }
                }else{ if(HeartRate<130){
                    mplayer = MediaPlayer.create(getApplicationContext(), MusicList1.get(j));
                    mplayer.start();
                    FadeIn.volumeGradient(mplayer, 0, 1);
                    MusicList = 1;
                }else{ if(HeartRate<150){
                    mplayer = MediaPlayer.create(getApplicationContext(), MusicList2.get(j));
                    mplayer.start();
                    FadeIn.volumeGradient(mplayer, 0, 1);
                    MusicList = 2;
                }else{ if(HeartRate<170){
                    mplayer = MediaPlayer.create(getApplicationContext(), MusicList3.get(j));
                    mplayer.start();
                    FadeIn.volumeGradient(mplayer, 0, 1);
                    MusicList = 3;
                }else{
                    if (player != null && player.isPlaying()) {
                        FadeIn.volumeGradient(player, 1, 0);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                player.stop();
                                player.release();
                                player = null;
                                HomeFragment HomeController = new HomeFragment();
                                HomeController.spStop();
                                HomeController.ReleaseWakeLock();
                                HomeController.resetTimer();
                                HomeController.SetTextToNull();
                            }
                        },6000); // 延时6秒
                    }
                }
                }
                }
                }
            }
        },60000); // 延时60秒
        if(mplayer != null && mplayer.isPlaying()){
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mmp) {
                j=j+1;
                mplayer.stop();
                mplayer.release();
                mplayer = null;
                if(j<3) {MusicPlay();}
            }
        });}

    }

    public void play(){
        if (player != null && player.isPlaying()) {

        }else{
            if(i==3){i=0;}
            player = MediaPlayer.create(this, NoiseList.get(i));
            player.start();
            FadeIn.volumeGradient(player, 0, 1);
        }
       player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           public void onCompletion(MediaPlayer mp) {
                i=i+1;
                player.stop();
                player.release();
                player = null;
                play();
           }
       });
    }

    public void setDataInMusic(){
        HeartRate = 0;
        Cadence = 0;
    }

    public void HRListen(){
        MutableLiveData<Integer> listen = new MutableLiveData<>();
        listen.setValue(HeartRate);
        listen.observe(this,new Observer<Integer>() {
            @Override
            public void onChanged(Integer HR) {
                //Do something with the changed value
                if(mplayer != null && mplayer.isPlaying()){
                    switch(MusicList){
                        case 1: if(HR<100 || HR>130){Log.d(TAG, " Out of Range1! " );}
                        case 2: if(HR<130 || HR>150){Log.d(TAG, " Out of Range2! " );}
                        case 3: if(HR<150 || HR>170){Log.d(TAG, " Out of Range3! " );}
                    }
                }
            }
        });
    }

    private class MusicReceiver extends BroadcastReceiver {

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


class FadeIn {
    public static void volumeGradient(final MediaPlayer mediaPlayer,
                                      final float from, final float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(6000); // 淡入时间
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator it) {
                float volume = (float) it.getAnimatedValue();
                try {
                    // 此时可能 mediaPlayer 状态发生了改变
                    //,所以用try catch包裹,一旦发生错误,立马取消
                    mediaPlayer.setVolume(volume, volume);
                } catch (Exception e) {
                    it.cancel();
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }
}

