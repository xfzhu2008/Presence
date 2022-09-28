package presence.apk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.huawei.hihealthkit.data.HiHealthExtendScope;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;

import presence.apk.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int REQUEST_SIGN_IN_LOGIN = 1002;
    private static final String TAG = "HihealthKitMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            //设置ShowHideFragmentNavigator
            navController.getNavigatorProvider().addNavigator(
                    new ShowHideFragmentNavigator(this, navHostFragment.getChildFragmentManager(), R.id.nav_host_fragment_activity_main));
            navController.setGraph(R.navigation.mobile_navigation); //必须使用代码显示设置
        }
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        signIn();
        binding.navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // 避免再次点击重复创建
                if (item.isChecked()) {
                    return true;
                }
                // 避免B返回到A重复创建
                boolean popBackStack = navController.popBackStack(item.getItemId(), false);
                if (popBackStack) {
                    // 已创建
                    return true;
                } else {
                    // 未创建
                    boolean result = NavigationUI.onNavDestinationSelected(item, navController);
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(item.getTitle());
                    }
                    return result;
                }
            }
        });

        ignoreBatteryOptimization(this);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //没有权限则申请权限
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            } //有权限直接执行
        } //小于6.0，不用申请权限，直接执行
    }

    /**
     * 授权登录方法，如果之前没有经过当前帐号的授权，会拉起授权界面
     */
    private void signIn() {
        Log.i(TAG, "begin sign in");
        List<Scope> scopeList = new ArrayList<>();

        // 添加需要申请的权限，这里只是举例说明，开发者需要根据实际情况添加所需的权限
 /*       // 包含步数统计、步数详情、距离、热量、运动中高强度
        scopeList.add(new Scope(HiHealthExtendScope.HEALTHKIT_EXTEND_SPORT_READ));

        // 包含性别、出生日期、身高、体重。
        scopeList.add(new Scope(HiHealthExtendScope.HEALTHKIT_EXTEND_HEALTHBEHAVIOR_READ));

        // 包含睡眠得分、睡眠时长等科学睡眠和普通睡眠数据。
        scopeList.add(new Scope(HiHealthAtomicScope.HEALTHKIT_SLEEP_READ));*/

        // 获取实时心率
        //scopeList.add(new Scope(HiHealthExtendScope.HEALTHKIT_EXTEND_REALTIME_HEART_READ));

        // 控制和获取实时运动权限
        scopeList.add(new Scope(HiHealthExtendScope.HEALTHKIT_EXTEND_SPORT_READ));

        // 设置授权参数
        HuaweiIdAuthParamsHelper authParamsHelper = new HuaweiIdAuthParamsHelper(
                HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        HuaweiIdAuthParams authParams = authParamsHelper.setIdToken()
                .setAccessToken()
                .setScopeList(scopeList)
                .createParams();

        // 初始化HuaweiIdAuthService对象
        final HuaweiIdAuthService authService = HuaweiIdAuthManager.getService(this.getApplicationContext(),
                authParams);

        // 静默登录，如果之前已经通过当前帐号的授权，调用该接口不会拉起授权界面，为异步方法
        Task<AuthHuaweiId> authHuaweiIdTask = authService.silentSignIn();

        // 增加调用结果的回调
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId huaweiId) {
                // 静默登录成功
                Log.i(TAG, "silentSignIn success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // 静默登录失败，说明之前没有通过该帐号的授权
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.i(TAG, "sign failed status:" + apiException.getStatusCode());
                    Log.i(TAG, "begin sign in by intent");

                    // 通过getSignInIntent()方式调用登录接口
                    Intent signInIntent = authService.getSignInIntent();

                    // 通过Activity的startActivityForResult()方法拉起授权界面
                    // 开发者可以将这里的HihealthKitMainActivity改为实际使用的Activity
                   MainActivity.this.startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理登录响应
        handleSignInResult(requestCode, data);
    }

    /**
     * 授权结果响应处理方法
     *
     * @param requestCode 拉起授权界面的请求码
     * @param data 授权结果响应
     */
    private void handleSignInResult(int requestCode, Intent data) {
        // 只处理授权的响应
        if (requestCode != REQUEST_SIGN_IN_LOGIN) {
            return;
        }

        // 从intent中获取授权响应
        HuaweiIdAuthResult result = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
        Log.d(TAG, "handleSignInResult status = " + result.getStatus() + ", result = " + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "sign in is success");
        }
    }

    public void ignoreBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                /**
                 * Check whether the current app is added to the battery optimization trust list,
                 * If not, a dialog box is displayed for you to add a battery optimization trust list.
                 */
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
                if (!hasIgnored) {
                    Intent newIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    newIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                    startActivity(newIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
