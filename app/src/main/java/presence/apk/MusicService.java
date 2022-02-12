package presence.apk;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service {

private MediaPlayer player;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MusicController();
    }

    public class MusicController extends Binder {
            public void play(){
                MusicService.this.play();
            }
            public void stop(){
                MusicService.this.onDestroy();
            }

    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();

}
    @Override
    public void onDestroy() {
    super.onDestroy();
    player.stop();
    player.release();
    player = null;
    }

    public void play(){
        try{
            player = MediaPlayer.create(this, R.raw.alarm03);
            player.start();
            player.setLooping(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
