package presence.apk;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class MusicService extends Service {

private MediaPlayer player;
ArrayList<Integer> NoiseList;

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
        NoiseList = new ArrayList<>();
        NoiseList.add(R.raw.oceanwaves);
        NoiseList.add(R.raw.garden);
        NoiseList.add(R.raw.land);
        NoiseList.add(R.raw.night);
        NoiseList.add(R.raw.forest);
        NoiseList.add(R.raw.river);
        NoiseList.add(R.raw.thunder);
        Collections.shuffle(NoiseList);
        player = MediaPlayer.create(this, NoiseList.get(0));
        player.start();
    }
}