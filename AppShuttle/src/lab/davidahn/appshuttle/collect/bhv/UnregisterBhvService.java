package lab.davidahn.appshuttle.collect.bhv;

import lab.davidahn.appshuttle.predict.PredictionService;
import android.app.IntentService;
import android.content.Intent;

public class UnregisterBhvService extends IntentService {
	public UnregisterBhvService() {
		this("UnregisterBhvService");
	}

	public UnregisterBhvService(String name) {
		super(name);
	}

	public void onCreate() {
		super.onCreate();
	}

	public void onHandleIntent(Intent intent) {
		UserBhvType bhvType = (UserBhvType) intent.getExtras().get("bhv_type");
		String bhvName = intent.getExtras().getString("bhv_name");
		UserBhv targetBhv = UserBhvManager.getInstance().getRegisteredUserBhv(bhvType, bhvName);
		if(targetBhv == null)
			return;
		UserBhvManager.getInstance().unregister(targetBhv);
		sendBroadcast(new Intent().setAction(PredictionService.PREDICT).putExtra("isForce", true));
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
