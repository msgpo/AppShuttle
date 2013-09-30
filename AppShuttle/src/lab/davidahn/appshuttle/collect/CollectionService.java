package lab.davidahn.appshuttle.collect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.SnapshotUserCxtDao;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.context.bhv.UserBhvManager;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.DurationUserEnvDao;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.UserEnv;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class CollectionService extends Service {
	private Date _currTimeDate;
	private TimeZone _currTimeZone;
	private Map<EnvType, EnvSensor> _sensors;
    private List<BhvCollector> _collectors;
	
//	public CollectionService() {
//		this("CollectionService");
//	}
//	
//	public CollectionService(String name) {
//		super(name);
//	}

	public void onCreate() {
		super.onCreate();
		
		_sensors = new HashMap<EnvType, EnvSensor>();
		_sensors.put(EnvType.LOCATION, LocEnvSensor.getInstance());
		_sensors.put(EnvType.PLACE, PlaceEnvSensor.getInstance());
		
		_collectors = new ArrayList<BhvCollector>();
		_collectors.add(AppBhvCollector.getInstance());
		_collectors.add(CallBhvCollector.getInstance());

		preCollectCollectDurationUserContext();
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		_currTimeDate = new Date(System.currentTimeMillis());
		_currTimeZone = Calendar.getInstance().getTimeZone();
		
		SnapshotUserCxt uCxt = CollectSnapshotUserContext();
		CollectDurationUserContext(uCxt);

		AppShuttleApplication.getContext().setCurrUserCxt(uCxt);
		
		return START_NOT_STICKY;
	}
	
	public IBinder onBind(Intent intent){
		return null;
	}
	
	public void onDestroy() {
		super.onDestroy();
		
		postCollectDurationUserContext();
	}
	
	private void preCollectCollectDurationUserContext() {
		_currTimeDate = new Date(System.currentTimeMillis());
		_currTimeZone = Calendar.getInstance().getTimeZone();

		for(EnvSensor sensor : _sensors.values()){
			List<DurationUserEnv> preExtractedDurationUserEnvList = sensor.preExtractDurationUserEnv(_currTimeDate, _currTimeZone);
			for(DurationUserEnv preExtractedDurationUserEnv : preExtractedDurationUserEnvList)
				storeDurationUserEnv(preExtractedDurationUserEnv);
		}
		
		for(BhvCollector collector : _collectors){
			List<DurationUserBhv> preExtractedDurationUserBhvList = 
					collector.preExtractDurationUserBhv(_currTimeDate, _currTimeZone);
			storeDurationUserBhv(preExtractedDurationUserBhvList);

			registerEachBhv(preExtractedDurationUserBhvList);
		}
		Log.d("collection", "pre collection");
	}
	
	private SnapshotUserCxt CollectSnapshotUserContext() {
		SnapshotUserCxt uCxt = new SnapshotUserCxt();

		uCxt.setTime(_currTimeDate);
		uCxt.setTimeZone(_currTimeZone);
		
		for(EnvType envType : _sensors.keySet()){
			EnvSensor sensor = _sensors.get(envType);
			UserEnv uEnv = sensor.sense();
			uCxt.addUserEnv(envType, uEnv);
		}
		
		for(BhvCollector collector : _collectors){
			List<UserBhv> userBhvList = collector.collect();
			uCxt.addUserBhvAll(userBhvList);
		}
		
		storeSnapshotCxt(uCxt);
		
		return uCxt;
	}

	private void CollectDurationUserContext(SnapshotUserCxt uCxt) {
		Date currTimeDate = uCxt.getTimeDate();
		TimeZone currTimeZone = uCxt.getTimeZone();
		
		for(EnvType envType : _sensors.keySet()){
			EnvSensor sensor = _sensors.get(envType);
			DurationUserEnv durationUserEnv = sensor.extractDurationUserEnv(currTimeDate, currTimeZone, uCxt.getUserEnv(envType));
			storeDurationUserEnv(durationUserEnv);
		}
		
		for(BhvCollector collector : _collectors){
			List<DurationUserBhv> durationUserBhvList = 
					collector.extractDurationUserBhv(currTimeDate, currTimeZone, uCxt.getUserBhvs());
			storeDurationUserBhv(durationUserBhvList);

			registerEachBhv(durationUserBhvList);
		}
	}
	
	private void postCollectDurationUserContext() {
		_currTimeDate = new Date(System.currentTimeMillis());
		_currTimeZone = Calendar.getInstance().getTimeZone();

		postCollectDurationUserBhv();
		postCollectDurationUserEnv();
		
		Log.d("collection", "post collection");
	}

	private void postCollectDurationUserEnv() {
		for(EnvSensor sensor : _sensors.values()){
			DurationUserEnv postExtractedDurationUserEnv = sensor.postExtractDurationUserEnv(_currTimeDate, _currTimeZone);
			storeDurationUserEnv(postExtractedDurationUserEnv);
		}
	}

	private void postCollectDurationUserBhv() {
		for(BhvCollector collector : _collectors){
			List<DurationUserBhv> postExtractedDurationUserBhvList = 
					collector.postExtractDurationUserBhv(_currTimeDate, _currTimeZone);
			storeDurationUserBhv(postExtractedDurationUserBhvList);

			registerEachBhv(postExtractedDurationUserBhvList);
		}
	}

	private void registerEachBhv(List<DurationUserBhv> durationUserBhvList) {
		UserBhvManager userBhvManager = UserBhvManager.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			UserBhv uBhv = durationUserBhv.getBhv();
			if(uBhv.isValid())
				userBhvManager.registerBhv(uBhv);
		}
	}

	private void storeSnapshotCxt(SnapshotUserCxt uCxt) {
//		preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
//		SharedPreferences preferenceSettings = ((AppShuttleApplication)getApplicationContext()).getPreferenceSettings();
		SharedPreferences preferenceSettings = AppShuttleApplication.getContext().getPreferenceSettings();

		if(!preferenceSettings.getBoolean("collection.store_cxt.enabled", false))
			return;
		SnapshotUserCxtDao snapshotUserCxtDao = SnapshotUserCxtDao.getInstance();
		snapshotUserCxtDao.storeCxt(uCxt);
	}

	private void storeDurationUserEnv(DurationUserEnv durationUserEnv) {
		if(durationUserEnv == null)
			return;
		DurationUserEnvDao durationUserEnvDao = DurationUserEnvDao.getInstance();
		durationUserEnvDao.store(durationUserEnv);
	}
	
	private void storeDurationUserBhv(List<DurationUserBhv> durationUserBhvList) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			durationUserBhvDao.store(durationUserBhv);
		}		
	}
}
