package lab.davidahn.appshuttle;

import android.app.AlarmManager;
import android.content.SharedPreferences;

public class AppShuttlePreferences {
	public static void setDefaultPreferences() {
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		SharedPreferences.Editor editor = pref.edit();
		
		//general
		editor.putBoolean("mode.debug", true);
		editor.putString("database.name", "AppShuttle.db");

		//collection
		editor.putLong("collection.common.auto_extraction_duration", AlarmManager.INTERVAL_HOUR);
		editor.putBoolean("collection.store_snapshot_cxt.enabled", false);

		editor.putBoolean("collection.env.enabled", true);
		editor.putLong("collection.env.period", 300000);
		editor.putLong("collection.env.location.tolerance.time", 60000);
		editor.putInt("collection.env.location.tolerance.distance", 500);
		editor.putInt("collection.env.place.num_address_prefix_words", 6);
		editor.putLong("collection.env.place.auto_extraction_duration", AlarmManager.INTERVAL_FIFTEEN_MINUTES);

		editor.putBoolean("collection.bhv.enabled", true);
		editor.putLong("collection.bhv.period", 30000);
		editor.putLong("collection.bhv.app.pre.depreciation", AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5);
		editor.putLong("collection.bhv.call.pre.period", 21 * AlarmManager.INTERVAL_DAY);

		//compaction
		editor.putBoolean("compaction.enabled", true);
		editor.putLong("compaction.period", AlarmManager.INTERVAL_DAY);
		editor.putLong("compaction.expiration", 21 * AlarmManager.INTERVAL_DAY);
		
		//report
		editor.putString("report.email.sender_addr", "appshuttle2@gmail.com");
		editor.putString("report.email.sender_pwd", "appshuttle2@");
		editor.putString("report.email.receiver_addr", "andabi412@gmail.com");
		
		//predictor
		editor.putBoolean("predictor.store", false);
		editor.putLong("predictor.period", 300000);
		editor.putLong("predictor.delay_ignorance", 150000);
		editor.putLong("predictor.max_duration", 21 * AlarmManager.INTERVAL_DAY);
		
		editor.putLong("matcher.recent.frequently.duration", 36 * AlarmManager.INTERVAL_HOUR);
		editor.putLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putInt("matcher.recent.frequently.min_num_related_history", 3);
		
		editor.putLong("matcher.recent.instantly.duration", AlarmManager.INTERVAL_HOUR / 3);
		editor.putLong("matcher.recent.instantly.acceptance_delay", 0);
		editor.putInt("matcher.recent.instantly.min_num_related_history", 1);

		editor.putLong("matcher.time.daily.duration", 4 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily.min_likelihood", Float.MIN_VALUE);
		editor.putFloat("matcher.time.daily.min_inverse_entropy", Float.MIN_VALUE);
		editor.putInt("matcher.time.daily.min_num_related_history", 3);
		editor.putLong("matcher.time.daily.tolerance", AlarmManager.INTERVAL_HOUR);
		
		editor.putLong("matcher.time.daily_weekday.duration", 7 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily_weekday.min_likelihood", 0.5f);
		editor.putFloat("matcher.time.daily_weekday.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.time.daily_weekday.min_num_related_history", 3);
		editor.putLong("matcher.time.daily_weekday.tolerance", 3 * AlarmManager.INTERVAL_HALF_HOUR);

		editor.putLong("matcher.time.daily_weekend.duration", 21 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.time.daily_weekend.min_likelihood", 0.5f);
		editor.putFloat("matcher.time.daily_weekend.min_inverse_entropy", 0.2f);
		editor.putInt("matcher.time.daily_weekend.min_num_related_history", 3);
		editor.putLong("matcher.time.daily_weekend.tolerance", 2 * AlarmManager.INTERVAL_HOUR);

		editor.putLong("matcher.position.move.duration", 7 * AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.position.move.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.position.move.min_likelihood", 0.3f);
		editor.putInt("matcher.position.move.min_num_related_history", 3);

		editor.putLong("matcher.position.loc.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.position.loc.min_likelihood", 0);
		editor.putFloat("matcher.position.loc.min_inverse_entropy", 0);
		editor.putInt("matcher.position.loc.min_num_history", 5);
		editor.putInt("matcher.position.loc.tolerance", 500); //in meter

		editor.putLong("matcher.position.loc_time.duration", 5 * AlarmManager.INTERVAL_DAY);
		editor.putFloat("matcher.position.loc_time.min_likelihood", 0);
		editor.putFloat("matcher.position.loc_time.min_inverse_entropy", 0);
		editor.putInt("matcher.position.loc_time.min_num_history", 3);
		editor.putInt("matcher.position.loc_time.tolerance", 500); //in meter
		editor.putLong("matcher.position.loc_time.tolerance_time", 2 * AlarmManager.INTERVAL_HOUR);

//		editor.putLong("matcher.position.place.duration", 5 * AlarmManager.INTERVAL_DAY);
//		editor.putLong("matcher.position.place.acceptance_delay", AlarmManager.INTERVAL_HOUR);
//		editor.putFloat("matcher.position.place.min_likelihood", 0.3f);
//		editor.putFloat("matcher.position.place.min_inverse_entropy", 0.1f);
//		editor.putInt("matcher.position.place.min_num_history", 3);

		editor.putLong("matcher.headset.duration", 7 * AlarmManager.INTERVAL_DAY);
		editor.putLong("matcher.headset.acceptance_delay", AlarmManager.INTERVAL_HOUR);
		editor.putFloat("matcher.headset.min_likelihood", 0.5f);
		editor.putInt("matcher.headset.min_num_related_history", 3);
		
		//view
		editor.putLong("view.update_period", 10000);
		editor.putInt("viewer.noti.max_num", 24);
		editor.putInt("viewer.noti.proper_num_favorite", 6);
		
		// TODO: 통계관련 패러미터 추가
		
		editor.commit();
	}

	public static boolean isSleepMode() {
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		if(pref.getBoolean("settings_pref_sleep_mode_key", false))
			return true;
		else
			return false;
	}
	
	public static boolean isSystemAreaIconHidden(){
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		if(pref.getBoolean("settings_pref_system_area_icon_hide_key", false))
			return true;
		else
			return false;
	}

	public static final String SLEEP_MODE = "lab.davidahn.appshuttle.SLEEP_MODE";
	
//	public boolean isHidden(){
//	SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
//	if(pref.getBoolean("settings_pref_noti_view_hide_key", false))
//		return true;
//	else
//		return false;
//}

}
