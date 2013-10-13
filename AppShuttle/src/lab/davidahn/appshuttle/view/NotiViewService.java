package lab.davidahn.appshuttle.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttleMainActivity;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.bhv.BhvType;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfo;
import lab.davidahn.appshuttle.mine.matcher.PredictedBhvInfoDao;
import lab.davidahn.appshuttle.mine.matcher.Predictor;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.util.TypedValue;
import android.widget.RemoteViews;

public class NotiViewService extends Service {
	private static final int NOTI_UPDATE = 1;
	
	private NotificationManager _notificationManager;
	private PackageManager _packageManager;
//	private LayoutInflater layoutInflater;
	
	private Set<UserBhv> _recentPredictedBhvSet;

	public void onCreate(){
		super.onCreate();
		_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		_packageManager = getPackageManager();
//		layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		updateNotiView(predictAndGetBhv());
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onDestroy() {
		super.onDestroy();
		_notificationManager.cancelAll();
	}

	private List<PredictedBhvInfo> predictAndGetBhv() {
		Predictor predictor = Predictor.getInstance();
		List<PredictedBhvInfo> predictedBhvInfoList = predictor.predict(Integer.MAX_VALUE);

		@SuppressWarnings("unused")
		boolean stored = storeNewPredictedBhv(predictedBhvInfoList);

		return predictedBhvInfoList;
	}

	@SuppressLint("NewApi")
	private void updateNotiView(List<PredictedBhvInfo> predictedBhvInfoList) {
		RemoteViews notiView = createNotiRemoteViews(predictedBhvInfoList);

		Notification notiUpdate = new Notification.Builder(NotiViewService.this)
			.setSmallIcon(R.drawable.appshuttle)
			.setContent(notiView)
			.setOngoing(true)
			.setWhen(AppShuttleApplication.getContext().getLaunchTime())
			.setPriority(Notification.PRIORITY_MAX)
			.build();
		_notificationManager.notify(NOTI_UPDATE, notiUpdate);
	}

	private RemoteViews createNotiRemoteViews(List<PredictedBhvInfo> predictedBhvInfoList) {
		RemoteViews notiRemoteView = new RemoteViews(getPackageName(), R.layout.noti);

		//clean
		notiRemoteView.removeAllViews(R.id.noti_elem_container);
		
		notiRemoteView.setOnClickPendingIntent(R.id.noti_icon, PendingIntent.getActivity(this, 0, new Intent(this, AppShuttleMainActivity.class), 0));

		int maxNumElem = AppShuttleApplication.getContext().getPreferenceSettings().getInt("viewer.noti.max_num_elem", 5);
		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			BhvType bhvType = predictedBhv.getBhvType();
			String bhvName = predictedBhv.getBhvName();
			
			Intent launchIntent = null;
			RemoteViews notiElemRemoteView = new RemoteViews(getPackageName(), R.layout.noti_element);
			if(bhvType == BhvType.APP){
				launchIntent = _packageManager.getLaunchIntentForPackage(bhvName);
				if(launchIntent == null){
					continue;
				} else {
					try {
						BitmapDrawable iconDrawable = (BitmapDrawable) _packageManager.getApplicationIcon(bhvName);
						notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_image, iconDrawable.getBitmap());
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			} else if (bhvType == BhvType.CALL){
				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_image, callContactIcon);

				launchIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+ bhvName));
				notiElemRemoteView.setTextViewText(R.id.noti_elem_text, 
						(String) predictedBhv.getMeta("cachedName"));
				notiElemRemoteView.setTextViewTextSize(R.id.noti_elem_text, TypedValue.COMPLEX_UNIT_SP, 10);
			} else {
				continue;
			}
			
			if(launchIntent != null)
				notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, PendingIntent.getActivity(this, 0, launchIntent, 0));
			
			notiRemoteView.addView(R.id.noti_elem_container, notiElemRemoteView);
			
			if(--maxNumElem <=0 )
				break;
		}

		return notiRemoteView;
	}
	
	private boolean storeNewPredictedBhv(List<PredictedBhvInfo> predictedBhvInfoList) {
		Set<UserBhv> lastPredictedBhvSet = _recentPredictedBhvSet;
		
		PredictedBhvInfoDao predictedBhvDao = PredictedBhvInfoDao.getInstance();
		boolean stored = false;

		Set<UserBhv> currPredictedBhvSet = new HashSet<UserBhv>();
		for(PredictedBhvInfo predictedBhvInfo : predictedBhvInfoList) {
			UserBhv predictedBhv = predictedBhvInfo.getUserBhv();
			if(lastPredictedBhvSet == null || !lastPredictedBhvSet.contains(predictedBhv)) {
				predictedBhvDao.storePredictedBhv(predictedBhvInfo);
				stored = true;
			}
			currPredictedBhvSet.add(predictedBhv);
		}

		_recentPredictedBhvSet = currPredictedBhvSet;
		
		return stored;
	}
}

//View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));
//LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

//private int getNotiViewWidth() {
//WindowManager wm = (WindowManager) AppShuttleApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
//Display display = wm.getDefaultDisplay();
//Point size = new Point();
//display.getSize(size);
//return size.x;
//}

