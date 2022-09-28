package presence.apk.ui.dashboard;


import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.POWER_SERVICE;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import com.google.android.material.snackbar.Snackbar;
import com.huawei.hihealthkit.data.HiHealthKitConstant;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;


import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import presence.apk.ConstantMusicService;
import presence.apk.MyDataBaseHelper;
import presence.apk.R;
import presence.apk.SportService;
import presence.apk.ui.home.HomeFragment;

public class DashboardFragment extends Fragment {

    private TextView countdownText, HeartRateText, CadenceText, CheckFlagText, NoiseFlagText, StatusText, DistanceText;
    private Button countdownButton;
    private Button ClearDatabase;
    private Button ExportXlsx;

    private ConstantMusicService.MusicController ConstantMusicController;
    private Intent CIntent;
    private ConstantMusicServiceConn CConn;

    private PowerManager.WakeLock wl;
    private MyDataBaseHelper dbHelper;
    private CountDownTimer countDownTimer;
    private long timeLeftInMiliseconds = 300000; //9mins
    private final long StartTimeInMiliseconds = 300000; //9mins
    private boolean timerRunning;

    private Intent spIntent;
    private SportService.SportController spController;
    private SportServiceConn spConn;

    private SportReceiver receiver;
    private StatusInfoReceiver receiver2;
    private CheckFlagReceiver receiver3;
    private NoiseFlagReceiver receiver4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        countdownText = view.findViewById(R.id.countdown_text);
        HeartRateText = view.findViewById(R.id.HeartRate);
        CadenceText = view.findViewById(R.id.Cadence);
        CheckFlagText = view.findViewById(R.id.CheckFlag);
        NoiseFlagText = view.findViewById(R.id.NoiseFlag);
        DistanceText = view.findViewById(R.id.Distance);
        StatusText = view.findViewById(R.id.Status);
        countdownButton = view.findViewById(R.id.countdownbutton);
        CircularProgressBar circularProgressBar = view.findViewById(R.id.circularProgressBar);
        ClearDatabase = view.findViewById(R.id.ClearDB);
        ExportXlsx = view.findViewById(R.id.ExportData);

        dbHelper = new MyDataBaseHelper(getActivity(), "BioStore.db",null,2);

        countdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timerRunning){
                    Snackbar.make(view, "BioData stop recording...", Snackbar.LENGTH_SHORT).show();
                    circularProgressBar.setProgressWithAnimation(0f, 1000L); // =1s
                }else{
                    Snackbar.make(view, "BioData recording...", Snackbar.LENGTH_SHORT).show();
                    circularProgressBar.setProgressWithAnimation(100f, 300000L); //
                }
                startStop();
            }
        });
        ClearDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL(" DELETE FROM " + MyDataBaseHelper.TABLE_NAME);
                Snackbar.make(view, "DataBase Cleared.", Snackbar.LENGTH_SHORT).show();
                dbHelper.close();
            }
        });
        ExportXlsx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExportExcel();
            }
        });


// Set Progress Max
        circularProgressBar.setProgressMax(100f);

// Set ProgressBar Color with gradient
        circularProgressBar.setProgressBarColorStart(Color.BLUE);
        circularProgressBar.setProgressBarColorEnd(Color.RED);
        circularProgressBar.setProgressBarColorDirection(CircularProgressBar.GradientDirection.TOP_TO_BOTTOM);

// Set background ProgressBar Color
        circularProgressBar.setBackgroundProgressBarColor(Color.GRAY);

// Set Width
        circularProgressBar.setProgressBarWidth(4f); // in DP
        circularProgressBar.setBackgroundProgressBarWidth(4f); // in DP

// Other
        circularProgressBar.setRoundBorder(true);
        circularProgressBar.setStartAngle(0f);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);

        CIntent = new Intent(getActivity(), ConstantMusicService.class);
        getActivity().startService(CIntent);
        CConn = new ConstantMusicServiceConn();
        getActivity().bindService(CIntent, CConn, BIND_AUTO_CREATE);

        spIntent = new Intent(getActivity(), SportService.class);
        getActivity().startService(spIntent);
        spConn = new SportServiceConn();
        getActivity().bindService(spIntent, spConn, BIND_AUTO_CREATE);

        receiver = new SportReceiver();
        IntentFilter filter = new IntentFilter("action.sport");
        getActivity().registerReceiver(receiver, filter);

        receiver2 = new StatusInfoReceiver();
        IntentFilter filter2 = new IntentFilter("action.Status");
        getActivity().registerReceiver(receiver2, filter2);

        receiver3 = new CheckFlagReceiver();
        IntentFilter filter3 = new IntentFilter("action.CheckFlagStatus");
        getActivity().registerReceiver(receiver3, filter3);

        receiver4 = new NoiseFlagReceiver();
        IntentFilter filter4 = new IntentFilter("action.NoiseFlagStatus");
        getActivity().registerReceiver(receiver4, filter4);
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
                    dbHelper.close();
                    spStop();
                    ReleaseWakeLock();
                    resetTimer();
                    SetTextToNull();
                }
            },6000); // 延时6秒
        } else {
            dbHelper.getWritableDatabase();
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
                View view = getView();
                Snackbar.make(view, "BioData stop recording...", Snackbar.LENGTH_SHORT).show();
                CircularProgressBar circularProgressBar = view.findViewById(R.id.circularProgressBar);
                circularProgressBar.setProgressWithAnimation(0f, 1000L); // =1s
                stop();
                countdownButton.setEnabled(false);
                StatusText.setText("Congratulation! Walking creativity succeed");
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

    public void ExportExcel(){
        List<Student> students = query(dbHelper.getReadableDatabase());
        XSSFWorkbook mWorkbook = new XSSFWorkbook();
        XSSFSheet mSheet = mWorkbook.createSheet();
        createExcelHead(mSheet);
        if(students != null){
            for(Student student: students){
                createCell(student.TimeLeft,student.HeartRate,student.Cadence,mSheet);
            }
        }
        File xlsxFile = new File("/storage/emulated/0/Download/", "excel.xlsx");
        try{
            if (!xlsxFile.exists()) {
                xlsxFile.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(xlsxFile);
            mWorkbook.write(fileOut);// 或者以流的形式写入文件
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Student> query(SQLiteDatabase db) {
        List<Student> students = null;

        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDataBaseHelper.TABLE_NAME, null);
        if (cursor != null && cursor.getCount() > 0) {

            students = new ArrayList<>();

            while (cursor.moveToNext()) {
                Student student = new Student();
                student.TimeLeft = cursor.getInt(cursor.getColumnIndexOrThrow(MyDataBaseHelper.TIME_LEFT));
                student.HeartRate = cursor.getInt(cursor.getColumnIndexOrThrow(MyDataBaseHelper.HEART_RATE));
                student.Cadence = cursor.getInt(cursor.getColumnIndexOrThrow(MyDataBaseHelper.CADENCE));

                students.add(student);
            }
            cursor.close();
        }
        db.close();
        return students;
    }

    //数据容器，装载从数据库中读出的数据内容。
    private class Student {
        public int HeartRate;
        public int Cadence;
        public int TimeLeft;
    }

    // 创建Excel标题行，第一行。
    private void createExcelHead(XSSFSheet mSheet) {
        XSSFRow headRow = mSheet.createRow(0);
        headRow.createCell(0).setCellValue(MyDataBaseHelper.TIME_LEFT);
        headRow.createCell(1).setCellValue(MyDataBaseHelper.HEART_RATE);
        headRow.createCell(2).setCellValue(MyDataBaseHelper.CADENCE);
    }

    // 创建Excel的一行数据。
    private static void createCell(int TimeLeft, int HeartRate, int Cadence, XSSFSheet sheet) {
        XSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
        dataRow.createCell(0).setCellValue(TimeLeft);
        dataRow.createCell(1).setCellValue(HeartRate);
        dataRow.createCell(2).setCellValue(Cadence);
    }

    public void play() {
        ConstantMusicController.play();
    }

    public void stop() {
        ConstantMusicController.stop();
    }

    public void RemoveCallBack(){ ConstantMusicController.RemoveCallBack(); }

    public void setDataInMusic(){ ConstantMusicController.setData();}

    public void spRecord() { spController.onStart();}

    public void spStop() { spController.onStop();}


    class ConstantMusicServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConstantMusicController = (ConstantMusicService.MusicController) service;
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
        getActivity().stopService(CIntent);
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(receiver2);
        getActivity().unregisterReceiver(receiver3);
        getActivity().unregisterReceiver(receiver4);
    }

    public void SetTextToNull(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HeartRateText.setText("--");
                CadenceText.setText("--");
                NoiseFlagText.setText("--");
                CheckFlagText.setText("--");
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
                float distance = intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_DISTANCE, 0);
                String dis = String.valueOf(distance/1000);
                DistanceText.setText(dis + " km");

                ContentValues values = new ContentValues();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                values.put(MyDataBaseHelper.TIME_LEFT, intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_DURATION, 0));
                values.put(MyDataBaseHelper.HEART_RATE, intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE, 0));
                values.put(MyDataBaseHelper.CADENCE,intent.getIntExtra(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE, 0));
                db.insert(MyDataBaseHelper.TABLE_NAME,null,values);
            }
        }
    }

    class StatusInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.Status".equals(intent.getAction())) {
                StatusText.setText(intent.getStringExtra("Status"));
            }
        }
    }

    class CheckFlagReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.CheckFlagStatus".equals(intent.getAction())) {
                CheckFlagText.setText(intent.getStringExtra("CheckFlagStatus"));
            }
        }
    }

    class NoiseFlagReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.NoiseFlagStatus".equals(intent.getAction())) {
                NoiseFlagText.setText(intent.getStringExtra("NoiseFlagStatus"));
            }
        }
    }
}





