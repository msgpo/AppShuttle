package lab.davidahn.appshuttle.context.env;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.DBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChangedUserEnvDao {
	private static ChangedUserEnvDao changedUserEnvDao;
	private SQLiteDatabase db;

	private ChangedUserEnvDao(Context cxt) {
		db = DBHelper.getInstance(cxt).getWritableDatabase();
	}

	public static ChangedUserEnvDao getInstance(Context cxt) {
		if (changedUserEnvDao == null)
			changedUserEnvDao = new ChangedUserEnvDao(cxt);
		return changedUserEnvDao;
	}

	public void storeChangedUserEnv(ChangedUserEnv changedUserEnv) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();

		ContentValues row = new ContentValues();
		row.put("time", changedUserEnv.getTime().getTime());
		row.put("timezone", changedUserEnv.getTimeZone().getID());
		row.put("env_type", changedUserEnv.getEnvType().toString());
		row.put("from_value", gson.toJson(changedUserEnv.getFromUserEnv()));
		row.put("to_value", gson.toJson(changedUserEnv.getToUserEnv()));
		db.insert("changed_env", null, row);
		Log.i("stored changed env", changedUserEnv.toString());
	}
	
	public List<ChangedUserEnv> retrieveChangedUserEnv(Date sTime, Date eTime, EnvType envType) {
		Gson gson = new GsonBuilder().setDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").create();
		
		Cursor cur = db.rawQuery("SELECT * FROM changed_env WHERE time >= "
				+ sTime.getTime() + " AND time <= " + eTime.getTime() +" AND env_type = '" + envType.toString() + "';", null);
		List<ChangedUserEnv> res = new ArrayList<ChangedUserEnv>();
		while (cur.moveToNext()) {
			Date time = new Date(cur.getLong(0));
			TimeZone timezone = TimeZone.getTimeZone(cur.getString(1));
			UserEnv from = gson.fromJson(cur.getString(3), UserEnv.class);
			UserEnv to = gson.fromJson(cur.getString(4), UserEnv.class);

			ChangedUserEnv changedUserEnv = new ChangedUserEnv(time, timezone, envType, from, to);
			Log.i("retrieved changed env", changedUserEnv.toString());
			res.add(changedUserEnv);
		}
		cur.close();
		return res;
	}
	
	public void deleteChangedUserEnv(long time){
		db.execSQL("DELETE FROM changed_env WHERE time < " + time +";");
	}
}
