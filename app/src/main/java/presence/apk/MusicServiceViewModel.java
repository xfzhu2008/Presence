package presence.apk;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MusicServiceViewModel {
    //将“秒钟”这个字段用MutableLiveData包装起来
    private static volatile MusicServiceViewModel sInstance;
    private MutableLiveData<Integer> currentHR;

    MusicServiceViewModel(){}

    public LiveData<Integer> getCurrentHR()
    {
        if (currentHR == null) {
            currentHR = new MutableLiveData<>();
        }
        return currentHR;
    }

    public static MusicServiceViewModel getsInstance(){
        if(sInstance == null){
            synchronized (MusicServiceViewModel.class){
                sInstance = new MusicServiceViewModel();
            }
        }
        return sInstance;
    }
}
