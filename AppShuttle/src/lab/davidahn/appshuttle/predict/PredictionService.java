package lab.davidahn.appshuttle.predict;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.view.NotiBarNotifier;
import android.app.IntentService;
import android.content.Intent;

public class PredictionService extends IntentService {
	public PredictionService() {
		super("PredictionService");
	}
	public PredictionService(String name) {
		super(name);
	}

	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_VISIBLE"));
		Predictor.getInstance().predict(AppShuttleApplication.currUserCxt);
		NotiBarNotifier.getInstance().notification();
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.UPDATE_VIEW"));
		sendBroadcast(new Intent().setAction("lab.davidahn.appshuttle.PROGRESS_INVISIBLE"));
	}
}