package presence.apk.ui.home;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import presence.apk.MainActivity;
import presence.apk.R;
import presence.apk.databinding.FragmentHomeBinding;

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
    private MediaPlayer player;

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
        updateTimer();
        return view;
    }



    public void startStop(){
        if(timerRunning){
            resetTimer();
        }else{
            startTimer();
        }
        if (player == null)
        {
            player = MediaPlayer.create(getActivity(), R.raw.alarm03);
            player.start();
            player.setLooping(true);
        }else{
            player.release();
            player = null;
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
                player.release();
                player = null;
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}


