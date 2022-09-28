package presence.apk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDataBaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    public static final String TABLE_NAME = "Biodata";
    public static final String TIME_LEFT = "TimeLeft";
    public static final String HEART_RATE = "HeartRate";
    public static final String CADENCE = "Cadence";

    public MyDataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Biodata(_id integer primary key autoincrement, TimeLeft int(20), HeartRate int(20), Cadence int(20))");
        Log.e("Create Table","success!" );
        Toast.makeText(mContext,"Create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        onCreate(db);
    }
}
