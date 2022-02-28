package presence.apk.ui.dashboard;


import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.lang.Object;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import presence.apk.R;

public class DashboardFragment extends Fragment {

    ListView listView;
    private List<File> mySongs = new ArrayList<>();
    private SongAdapter adapter;
    private Handler handler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            adapter.setSongs(mySongs);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        listView = view.findViewById(R.id.listViewSong);
        runtimePermission();

        return view;
    }

    public void runtimePermission() {
        Dexter.withContext(getActivity()).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void findSong(File file) {

        File[] files = file.listFiles();

        if (files != null) {//要判空，否则会闪退
            for (File singlefile : files) {

                if (isDetached() || isRemoving()) {
                    return;
                }

                if (singlefile.isDirectory() && !singlefile.isHidden()) {
                    findSong(singlefile);
                } else {
                    if (singlefile.getName().endsWith(".flac") || singlefile.getName().endsWith(".mp3")) {
                        mySongs.add(singlefile);
                        /**
                         * 因为是在线程中遍历文件，所以操作UI界面必须使用handler方式
                         */
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        }

    }

    public void displaySongs() {

        adapter = new SongAdapter();
        listView.setAdapter(adapter);
        /**
         * 开启线程来遍历sdcard，否则文件太多会导致卡顿
         */
        new Thread(new Runnable() {
            @Override
            public void run() {

                mySongs.clear();
                findSong(Environment.getExternalStorageDirectory());
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class SongAdapter extends BaseAdapter {

        private List<File> songs = new ArrayList<>();

        @Override
        public int getCount() {
            return songs == null ? 0 : songs.size();
        }

        @Override
        public Object getItem(int position) {
            return songs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public List<File> getSongs() {
            return songs;
        }

        public void setSongs(List<File> songs) {
            this.songs.clear();
            this.songs.addAll(songs);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_lv_song, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String fileName = songs.get(position).getName().replace(".flac", "").replace(".mp3", "");
            holder.tv_name.setText(fileName);


            return convertView;
        }

        class ViewHolder {
            public View rootView;
            public TextView tv_name;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.tv_name = (TextView) rootView.findViewById(R.id.tv_name);
            }

        }
    }

}