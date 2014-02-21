package lab.davidahn.appshuttle.collect;

import java.util.Date;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvManager;
import lab.davidahn.appshuttle.collect.env.DurationUserEnvManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class CompactionService extends IntentService {
	private SnapshotUserCxt currUserCxt;
	
	public CompactionService() {
		this("CompactionService");
	}
	
	public CompactionService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
		currUserCxt = AppShuttleApplication.currUserCxt;
	}
	
	public void onHandleIntent(Intent intent){
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferences();

		long expirationDuration = preferenceSettings.getLong("compaction.expiration", 15 * AlarmManager.INTERVAL_DAY);
		Date expirationBoundTimeDate = new Date(currUserCxt.getTimeDate().getTime() - expirationDuration);
		
		compactHistoryUserBhv(expirationBoundTimeDate);
		compactHistoryUserEnv(expirationBoundTimeDate);
		compactUserBhv(expirationBoundTimeDate);
	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	private void compactHistoryUserBhv(Date expirationBoundTimeDate) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		durationUserBhvDao.deleteBefore(expirationBoundTimeDate);
	}

	private void compactHistoryUserEnv(Date expirationBoundTimeDate) {
		DurationUserEnvManager durationUserEnvManager = DurationUserEnvManager.getInstance();
		durationUserEnvManager.deleteAllBefore(expirationBoundTimeDate);
	}
	
	private void compactUserBhv(Date expirationBoundTimeDate) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		
		for(UserBhv uBhv : userBhvManager.getNormalBhvSet()){
			List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(expirationBoundTimeDate, currUserCxt.getTimeDate(), uBhv);
			if(!durationUserBhvList.isEmpty())
				continue;
			userBhvManager.unregisterBhv(uBhv);
		}
	}
}
