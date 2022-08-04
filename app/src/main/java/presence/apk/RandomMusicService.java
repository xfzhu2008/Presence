package presence.apk;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.valueOf;

import android.app.Service;
import android.content.Intent;
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

import com.huawei.hihealthkit.data.HiHealthKitConstant;

import java.util.ArrayList;
import java.util.Collections;

public class RandomMusicService extends Service implements LifecycleOwner {
    ArrayList<Integer> BeatsList;
    private static int  i= 0, j = 0, FadeFlag = 0;
    private static int MusicName;
    private static String Range;
    private MediaPlayer player;
    private MediaPlayer mplayer;
    private MediaPlayer bPlayer;
    private final Handler mHandler = new Handler();
    private final Handler nHandler = new Handler();
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        return new RanMusicController();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    public class RanMusicController extends Binder {
            public void play(){
                RandomMusicService.this.NoisePlay();
                RandomMusicService.this.BeatsPlay();
            }
            public void stop(){
                i= 0; j = 0; FadeFlag = 0;
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

        BeatsList = new ArrayList<>();
        BeatsList.add(R.raw.b78);
        BeatsList.add(R.raw.b83);
        BeatsList.add(R.raw.b88);
        BeatsList.add(R.raw.b93);
        BeatsList.add(R.raw.b98);
        BeatsList.add(R.raw.b103);
        BeatsList.add(R.raw.b108);
        BeatsList.add(R.raw.b113);
        BeatsList.add(R.raw.b118);
        BeatsList.add(R.raw.b123);
        BeatsList.add(R.raw.b128);
        BeatsList.add(R.raw.b133);
        Collections.shuffle(BeatsList);
        MusicName = valueOf(BeatsList.get(j));
        RangeDisplay();
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
        Intent intent = new Intent("action.Status");
        if(j==0){
            intent.putExtra("Status","Adjust your pace to the most comfortable in 1 minute.");
            Runnable mRun = new Runnable() {
                @Override
                public void run() {
                    mplayer = MediaPlayer.create(getApplicationContext(), BeatsList.get(j));
                    mplayer.start();
                    FadeIn.volumeGradient(mplayer, 0, 0.3f);
                    Intent intent1 = new Intent("action.CheckFlagStatus");
                    intent1.putExtra("CheckFlagStatus", Range);
                    sendBroadcast(intent1);
                    mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            j = j + 1;
                            MusicName = valueOf(BeatsList.get(j));
                            RangeDisplay();
                            Intent intent1 = new Intent("action.CheckFlagStatus");
                            intent1.putExtra("CheckFlagStatus", Range);
                            sendBroadcast(intent1);
                            mplayer.stop();
                            mplayer.release();
                            mplayer = null;
                            if (j < 9) {
                                BeatsPlay();
                            }
                        }
                    });
                }
            };
            mHandler.postDelayed(mRun, 60000);
        } else {
            intent.putExtra("Status","Well done! Keep your pace.");
            mplayer = MediaPlayer.create(getApplicationContext(), BeatsList.get(j));
            mplayer.start();
            mplayer.setVolume(0.3f, 0.3f);
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    j = j + 1;
                    MusicName = valueOf(BeatsList.get(j));
                    RangeDisplay();
                    Intent intent1 = new Intent("action.CheckFlagStatus");
                    intent1.putExtra("CheckFlagStatus", Range);
                    sendBroadcast(intent1);
                    mplayer.stop();
                    mplayer.release();
                    mplayer = null;
                    if (j < 9) {
                        BeatsPlay();
                    }
                }
            });
        }
        sendBroadcast(intent);
    }

    public void RemoveMusicCallBack(){
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(nHandler != null){
            nHandler.removeCallbacksAndMessages(null);
        }
    }


    public void NoisePlay(){
        if (bPlayer != null && bPlayer.isPlaying()){}
        else {
            bPlayer = MediaPlayer.create(this, R.raw.f2);
            bPlayer.start();
            Intent intent = new Intent("action.NoiseFlagStatus");
            intent.putExtra("NoiseFlagStatus", "2Hz");
            sendBroadcast(intent);
            FadeIn.volumeGradient(bPlayer, 0, 1);
            Repeat();
        }
    }

    public void Repeat() {
        Runnable nRun = new Runnable() {
            @Override
            public void run() {
                NoiseChoice();
            }
        };
        if (i < 1) {
            nHandler.postDelayed(nRun, 64000);//延时66秒
        }
        else{
            nHandler.postDelayed(nRun, 61000);//延时60秒
        }
    }

    public void NoiseChoice(){
        if (MusicName == 2131755035 || MusicName == 2131755036) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "4Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f4);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "4Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f4);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        } else if (MusicName == 2131755037 || MusicName == 2131755038) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "6Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f6);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "6Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f6);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        } else if (MusicName == 2131755039 || MusicName == 2131755028) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "8Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f8);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "8Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f8);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        } else if (MusicName == 2131755029 || MusicName == 2131755030) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "12Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f12);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "12Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f12);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        } else if (MusicName == 2131755031 || MusicName == 2131755032) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "16Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f16);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "16Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f16);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        } else if (MusicName == 2131755033 || MusicName == 2131755034) {
            if (FadeFlag == 0) {
                FadeFlag = 1;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "24Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(bPlayer, 1, 0);
                player = MediaPlayer.create(getApplicationContext(), R.raw.f24);
                player.start();
                FadeIn.volumeGradient(player, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bPlayer.reset();
                    }
                }, 6000); // 延时6秒
            } else {
                FadeFlag = 0;
                Intent intent = new Intent("action.NoiseFlagStatus");
                intent.putExtra("NoiseFlagStatus", "24Hz");
                sendBroadcast(intent);
                FadeIn.volumeGradient(player, 1, 0);
                bPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f24);
                bPlayer.start();
                FadeIn.volumeGradient(bPlayer, 0, 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        player.reset();
                    }
                }, 6000); // 延时6秒
            }
            i = i + 1;
        }
        if (i < 9) {
            Repeat();
        }
    }

    public void RangeDisplay(){
        switch(MusicName){
            case 2131755035: Range = "75-80";break;
            case 2131755036: Range = "80-85";break;
            case 2131755037: Range = "85-90";break;
            case 2131755038: Range = "90-95";break;
            case 2131755039: Range = "95-100";break;
            case 2131755028: Range = "100-105";break;
            case 2131755029: Range = "105-110";break;
            case 2131755030: Range = "110-115";break;
            case 2131755031: Range = "115-120";break;
            case 2131755032: Range = "120-125";break;
            case 2131755033: Range = "125-130";break;
            case 2131755034: Range = "130-135";break;
            default: Range = "--";
        }
    }
}



