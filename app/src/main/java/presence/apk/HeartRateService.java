package presence.apk;

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
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.huawei.hihealth.error.HiHealthError;
import com.huawei.hihealthkit.data.store.HiHealthDataStore;
import com.huawei.hihealthkit.data.store.HiRealTimeListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import presence.apk.ui.home.HomeFragment;

/**
 * Defining a Frontend Service
 *
 * @since 2020-09-05
 */
public class HeartRateService extends Service {
    private static final String TAG = "HeartRateService";

    // HMS Health AutoRecorderController
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.i(TAG, "service is create.");

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RealtimeDataController();
    }

    public class RealtimeDataController extends Binder {

        public void onStart(){HeartRateService.this.onStartCommand();}
        public void onstop(){HeartRateService.this.onDestroy();}
    }

    public void onStartCommand() {
        // Invoke the real-time callback interface of the HealthKit.
        getRemoteService();

        // Binding a notification bar
        getNotification();
    }


    /**
     * Callback Interface for Starting the Total heartrate Count
     */
    private void getRemoteService() {
        // Start recording real-time heartrate.
        HiHealthDataStore.startReadingHeartRate(context, new HiRealTimeListener() {


            @Override
            public void onResult(int state) {
                // 获取实时心率数据结果
                Log.i(TAG,"ReadingHeartRate onResult state:"+state);
            }
            @Override
            public void onChange(int resultCode, String value) {
                // 获取实时心率变化回调
                Log.i(TAG,"startReadingHeartRate onChange resultCode: "+ resultCode +" value: "+value);
                if (resultCode == HiHealthError.SUCCESS) {
                    try {
                        JSONObject jsonObject = new JSONObject(value);
                        Log.i(TAG, "hr_info : " + jsonObject.getInt("hr_info"));
                        Log.i(TAG, "time_info : " + jsonObject.getLong("time_info"));
                        Log.i(TAG, "heartRateCredibility : " + jsonObject.getInt("heartRateCredibility"));
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException e" + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Bind the service to the notification bar so that the service can be changed to a foreground service.
     */
    private void getNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "1").setContentTitle("Real-time heartrate counting")
                .setContentText("Real-time heartrate counting...")
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, new Intent(this, HomeFragment.class), 0))
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = 
                    new NotificationChannel("1", "subscribeName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(1, notification);
    }

    private void StopRemoteService(){
        HiHealthDataStore.stopReadingHeartRate(context, new HiRealTimeListener() {
            @Override
            public void onResult(int state) {
                // 停止获取实时心率接口结果
                Log.i(TAG,"stopReadingHeartRate onResult state:"+state);
            }
            @Override
            public void onChange(int resultCode, String value) {
                // 此时该接口不会被调用
            }
        });
    }

    @Override
    public void onDestroy() {
        StopRemoteService();
        super.onDestroy();
        Log.i(TAG, "HeartRateService is destroy.");

    }
}



