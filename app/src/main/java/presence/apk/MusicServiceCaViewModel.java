package presence.apk;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MusicServiceCaViewModel {
    //将“秒钟”这个字段用MutableLiveData包装起来
    private static volatile MusicServiceCaViewModel sInstance;
    private MutableLiveData<Integer> currentCa;

    MusicServiceCaViewModel(){}

    public LiveData<Integer> getCurrentCa()
    {
        if (currentCa == null) {
            currentCa = new MutableLiveData<>();
        }
        return currentCa;
    }

    public static MusicServiceCaViewModel getsInstance(){
        if(sInstance == null){
            synchronized (MusicServiceCaViewModel.class){
                sInstance = new MusicServiceCaViewModel();
            }
        }
        return sInstance;
    }
}
