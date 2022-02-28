package presence.apk.ui.home;


import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.POWER_SERVICE;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.huawei.hihealthkit.data.HiHealthKitConstant;

import java.util.TimerTask;

import presence.apk.MusicService;
import presence.apk.MusicServiceViewModel;
import presence.apk.R;
import presence.apk.SportService;

public class HomeFragment extends Fragment {

    /* private HomeViewModel homeViewModel;
     private FragmentHomeBinding binding;

     public View onCreateView(@NonNull LayoutInflater inflater,
                              ViewGroup container, Bundle savedInstanceState) {
         homeViewModel =
                 new ViewModelProvider(this).get(HomeViewModel.class);

         binding = FragmentHomeBinding.inflate(inflater, container, false);
         View root = binding.getRoot();

         final TextView textView = binding.textHome;
         homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
             @Override
             public void onChanged(@Nullable String s) {
                 textView.setText(s);
             }
         });
         return root;*/
    private TextView countdownText;
    private TextView HeartRateText;
    private TextView CadenceText;
    private Button countdownButton;

    private MusicService.MusicController musicController;
    private Intent intent;
    private MusicServiceConn conn;

    private PowerManager.WakeLock wl;

    private CountDownTimer countDownTimer;
    private long timeLeftInMiliseconds = 1200000; //20mins
    private long StartTimeInMiliseconds = 1200000; //20mins
    private boolean timerRunning;

    private Intent spIntent;
    private SportService.SportController spController;
    private SportServiceConn spConn;

    private SportReceiver receiver;
    private StopRunReceiver receiver1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        countdownText = view.findViewById(R.id.countdown_text);
        HeartRateText = view.findViewById(R.id.HeartRate);
        CadenceText = view.findViewById(R.id.Cadence);
        countdownButton = view.findViewById(R.id.countdownbutton);

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStop();
            }
        });

        intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        conn = new MusicServiceConn();
        getActivity().bindService(intent, conn, BIND_AUTO_CREATE);

        spIntent = new Intent(getActivity(), SportService.class);
        getActivity().startService(spIntent);
        spConn = new SportServiceConn();
        getActivity().bindService(spIntent, spConn, BIND_AUTO_CREATE);

        receiver = new SportReceiver();
        IntentFilter filter = new IntentFilter("action.sport");
        getActivity().registerReceiver(receiver, filter);

        receiver1 = new StopRunReceiver();
        IntentFilter filter1 = new IntentFilter("action.StopRunning");
        getActivity().registerReceiver(receiver1, filter1);

        updateTimer();
        return view;
    }

    public void startStop() {
        if (timerRunning) {
            RemoveCallBack();
            stop();
            countdownButton.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    spStop();
                    ReleaseWakeLock();
                    resetTimer();
                    SetTextToNull();
                }
            },6000); // 延时6秒
        } else {
            spRecord();
            AddWakeLock();
            startTimer();
            play();
        }
    }

    public void AddWakeLock() {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager pm = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "presence::wlTag");
        wl.acquire();
        Log.i(TAG, " wakelock wl.acquire(); ");
    }

    public void ReleaseWakeLock() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wl.release();
        wl = null;
        Log.i(TAG, " wakelock wl.release(); ");
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMiliseconds, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMiliseconds = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                stop();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        spStop();
                        ReleaseWakeLock();
                        resetTimer();
                        SetTextToNull();
                    }
                },6000); // 延时6秒
            }
        }.start();
        countdownButton.setText("Reset");
        timerRunning = true;
    }

    public void resetTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        timeLeftInMiliseconds = StartTimeInMiliseconds;
        countdownButton.setEnabled(true);
        countdownButton.setText("Start running");
        updateTimer();
    }

    public void updateTimer() {
        int minutes = (int) timeLeftInMiliseconds / 60000;
        int seconds = (int) timeLeftInMiliseconds % 60000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);
    }

    public void play() {
        musicController.play();
    }

    public void stop() {
        musicController.stop();
    }

    public void RemoveCallBack(){ musicController.RemoveCallBack(); }

    public void setDataInMusic(){ musicController.setData();}

    public void spRecord() { spController.onStart();}

    public void spStop() { spController.onStop();}


    class MusicServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicController = (MusicService.MusicController) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    class SportServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            spController = (SportService.SportController)service;
        }
        @Override
        public  void onServiceDisconnected(ComponentName name){
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().stopService(spIntent);
        getActivity().stopService(intent);
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(receiver1);
    }

    public void SetTextToNull(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HeartRateText.setText("--");
                CadenceText.setText("--");
                setDataInMusic();
            }
        },5000); // 延时5秒
    }

    class SportReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.sport".equals(intent.getAction())) {
                CadenceText.setText(String.valueOf(intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE, 0)));
                HeartRateText.setText(String.valueOf(intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE, 0)));
            }
        }
    }

    class StopRunReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.StopRunning".equals(intent.getAction())) {
                startStop();
            }
        }
    }
}





