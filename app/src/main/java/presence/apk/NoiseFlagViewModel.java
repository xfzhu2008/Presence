package presence.apk;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NoiseFlagViewModel {
    //将“秒钟”这个字段用MutableLiveData包装起来
    private static volatile NoiseFlagViewModel sInstance;
    private MutableLiveData<Integer> currentFlag;

    NoiseFlagViewModel(){}

    public LiveData<Integer> getCurrentFlag()
    {
        if (currentFlag == null) {
            currentFlag = new MutableLiveData<>();
        }
        return currentFlag;
    }

    public static NoiseFlagViewModel getsInstance(){
        if(sInstance == null){
            synchronized (NoiseFlagViewModel.class){
                sInstance = new NoiseFlagViewModel();
            }
        }
        return sInstance;
    }
}
