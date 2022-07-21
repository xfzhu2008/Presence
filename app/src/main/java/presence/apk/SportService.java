package presence.apk;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.huawei.hihealth.error.HiHealthError;
import com.huawei.hihealth.listener.ResultCallback;
import com.huawei.hihealthkit.data.HiHealthKitConstant;
import com.huawei.hihealthkit.data.store.HiHealthDataStore;
import com.huawei.hihealthkit.data.store.HiSportDataCallback;

import presence.apk.ui.home.HomeFragment;

public class SportService extends Service {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.i(TAG, "service is create.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SportController();
    }

    public class SportController extends Binder {
        public void onStart() {
            SportService.this.onStartCommand();
        }

        public void onStop() {
            SportService.this.onStopCommand();
        }
    }

    public void onStartCommand() {
        // Invoke the real-time callback interface of the HealthKit.
        getRemoteService();

        // Start Sport
        StartSport();

        // Binding a notification bar
        getNotification();
    }

    public void onStopCommand() {
        try{
            StopSport();
            StopRemoteService();
            cancelNotification();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void getRemoteService() {
        HiHealthDataStore.registerSportData(context, new HiSportDataCallback() {

            @Override
            public void onResult(int resultCode) {
                // 接口调用结果
                Log.i(TAG, "registerSportData onResult resultCode:" + resultCode);
            }

            @Override
            public void onDataChanged(int state, Bundle bundle) {
                // 实时数据变化回调
                Log.i(TAG, "registerSportData onChange state: " + state);
                StringBuffer stringBuffer = new StringBuffer("");
                if (state == HiHealthKitConstant.SPORT_STATUS_RUNNING) {
                    Log.i(TAG, "Cadence : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE));
                    Log.i(TAG, "heart rate : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE));
                    Log.i(TAG, "distance : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_DISTANCE));
                    Log.i(TAG, "duration : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_DURATION));
                    Log.i(TAG, "calorie : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_CALORIE));
                    Log.i(TAG, "totalSteps : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_TOTAL_STEPS));
                    Log.i(TAG, "totalCreep : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_TOTAL_CREEP));
                    Log.i(TAG, "totalDescent : " + bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_TOTAL_DESCENT));

                    Intent intent = new Intent("action.sport");
                    intent.putExtra(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE, bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_STEP_RATE));
                    intent.putExtra(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE, bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_HEARTRATE));
                    intent.putExtra(HiHealthKitConstant.BUNDLE_KEY_DISTANCE, bundle.getInt(HiHealthKitConstant.BUNDLE_KEY_DISTANCE));
                    sendBroadcast(intent);
                }
            }
        });
    }

    private void StartSport() {
        // 户外跑步
        int sportType = HiHealthKitConstant.SPORT_TYPE_RUN;
        HiHealthDataStore.startSport(context, sportType, new ResultCallback() {
            @Override
            public void onResult(int resultCode, Object message) {
                if (resultCode == HiHealthError.SUCCESS) {
                    Log.i(TAG, "start sport success");
                }
            }
        });
    }

    private void getNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "2").setContentTitle("Real-time Sport Running...")
                .setContentText("Real-time BioData Counting...")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, new Intent(this, HomeFragment.class), 0))
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel("2", "subscribeName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(2, notification);
    }

    private void cancelNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        stopForeground(true);
        notificationManager.cancelAll();
    }

    private void StopSport() {
        HiHealthDataStore.stopSport(context, new ResultCallback() {
            @Override
            public void onResult(int resultCode, Object message) {
                if (resultCode == HiHealthError.SUCCESS) {
                    Log.i(TAG, "stop sport success");
                }
            }
        });
    }

    private void StopRemoteService() {
        HiHealthDataStore.unregisterSportData(context, new HiSportDataCallback() {
            @Override
            public void onResult(int resultCode) {
                // 接口调用结果
                Log.i(TAG, "unregisterSportData onResult resultCode:" + resultCode);
            }
            @Override
            public void onDataChanged(int state, Bundle bundle) {
                // 此时不会被调用
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SportService is destroy.");
    }
}