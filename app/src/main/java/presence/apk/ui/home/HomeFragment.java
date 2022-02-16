package presence.apk.ui.home;

import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import presence.apk.MusicService;
import presence.apk.R;

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
    private Button countdownButton;

    private MusicService.MusicController musicController;
    private Intent intent;
    private MusicServiceConn conn;

    private CountDownTimer countDownTimer;
    private long timeLeftInMiliseconds = 30000; //20mins
    private long StartTimeInMiliseconds = 30000; //20mins
    private boolean timerRunning;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        countdownText = view.findViewById(R.id.countdown_text);
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

        updateTimer();
        return view;
    }



    public void startStop(){
        if(timerRunning){
            resetTimer();
            stop();
        }else{
            startTimer();
            play();
        }

    }

    public void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMiliseconds, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMiliseconds = l;
                updateTimer();

            }

            @Override
            public void onFinish() {
                resetTimer();
                stop();
            }
        }.start();
        countdownButton.setText("Reset");
        timerRunning = true;
    }

    public void stopTimer(){
        countDownTimer.cancel();
        countdownButton.setText("Resume");
        timerRunning = false;
    }

    public void resetTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        timeLeftInMiliseconds= StartTimeInMiliseconds;
        countdownButton.setText("Start running");
        updateTimer();
    }

    public void updateTimer(){
        int minutes = (int)timeLeftInMiliseconds / 60000;
        int seconds = (int)timeLeftInMiliseconds % 60000 / 1000;

        String timeLeftText;

        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;

        countdownText.setText(timeLeftText);
    }

    public void play(){
        musicController.play();
    }

    public void stop(){
        musicController.stop();
    }


    class MusicServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicController = (MusicService.MusicController)service;
        }
        @Override
        public  void onServiceDisconnected(ComponentName name){
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}


