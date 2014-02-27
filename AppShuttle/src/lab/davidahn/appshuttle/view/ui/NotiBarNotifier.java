package lab.davidahn.appshuttle.view.ui;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.AppShuttlePreferences;
import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.view.FavoriteBhv;
import lab.davidahn.appshuttle.view.FavoriteBhvManager;
import lab.davidahn.appshuttle.view.PresentBhv;
import lab.davidahn.appshuttle.view.Viewable;
import lab.davidahn.appshuttle.view.ViewableUserBhv;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

public class NotiBarNotifier {

	private static final int UPDATE_NOTI_VIEW = 1;
	
	private NotificationManager notificationManager;
//	private LayoutInflater layoutInflater;

	private AppShuttleApplication cxt = AppShuttleApplication.getContext();

	private static NotiBarNotifier notifier = new NotiBarNotifier();
	private NotiBarNotifier(){
		notificationManager = (NotificationManager)cxt.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	public static NotiBarNotifier getInstance(){
		return notifier;
	}
	
	public void updateNotification() {
		if(AppShuttlePreferences.isSleepMode())
			hideNotibar();
		else
			updateNotibar();
	}
	
	public void updateNotibar() {
		List<ViewableUserBhv> viewableUserBhvList = new ArrayList<ViewableUserBhv>();
		
		List<FavoriteBhv> notifiableFavoriteBhvList = FavoriteBhvManager.getInstance().getNotifiableFavoriteBhvList();
		viewableUserBhvList.addAll(notifiableFavoriteBhvList.subList(0, getNumFavoriteElem()));
		viewableUserBhvList.addAll(PresentBhv.getPresentBhvListFilteredSorted(getNumPredictedElem()));
		
		updateNotiView(viewableUserBhvList);
		
//		Log.d("notifier", "notibar updated.");
	}
	
	public int getNumElem() {
		int maxNumElem = cxt.getPreferences().getInt("viewer.noti.max_num", 24);
		int NotibarIconAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_icon_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		int NotibarBhvAreaWidth = (int) ((cxt.getResources().getDimension(R.dimen.notibar_bhv_area_width) / 
				cxt.getResources().getDisplayMetrics().density));
		return Math.min(maxNumElem, (getNotibarWidth() - NotibarIconAreaWidth) / NotibarBhvAreaWidth);
	}
	
	public int getNumFavoriteElem() {
		return Math.min(FavoriteBhvManager.getInstance().getNotifiableFavoriteBhvList().size(), getNumElem());
	}
	
	public int getNumPredictedElem() {
		return getNumElem() - getNumFavoriteElem();
	}
	
	public void hideNotibar() {
		notificationManager.cancel(UPDATE_NOTI_VIEW);
	}
	
	private <T extends UserBhv & Viewable> void updateNotiView(List<T> viewableUserBhv) {
		Notification noti;
		RemoteViews notiView = createNotiRemoteViews(viewableUserBhv);
			noti = new NotificationCompat.Builder(cxt)
				.setSmallIcon(R.drawable.appshuttle)
				.setContent(notiView)
				.setOngoing(true)
				.setPriority((AppShuttlePreferences.isSystemAreaIconHidden()) ? Notification.PRIORITY_MIN : Notification.PRIORITY_MAX)
				.build();
		notificationManager.notify(UPDATE_NOTI_VIEW, noti);
	}
	
	private <T extends UserBhv & Viewable> RemoteViews createNotiRemoteViews(List<T> viewableUserBhvList) {
		RemoteViews notiRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar);

		//clean
		notiRemoteView.removeAllViews(R.id.noti_favorite_container);
		notiRemoteView.removeAllViews(R.id.noti_present_container);
		
		notiRemoteView.setOnClickPendingIntent(R.id.noti_icon, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));

		if(viewableUserBhvList.isEmpty()){
			RemoteViews noResultRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar_no_result);
			notiRemoteView.addView(R.id.noti_present_container, noResultRemoteView);
			notiRemoteView.setOnClickPendingIntent(R.id.noti_present_container, PendingIntent.getActivity(cxt, 0, new Intent(cxt, AppShuttleMainActivity.class), 0));
			return notiRemoteView;
		}
		
		for(T viewableUserBhv : viewableUserBhvList) {
			UserBhvType bhvType = viewableUserBhv.getBhvType();
			RemoteViews notiElemRemoteView = new RemoteViews(cxt.getPackageName(), R.layout.notibar_element);
			
			Intent intent = viewableUserBhv.getLaunchIntent();
			if(intent != null){
				PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 0, intent, 0);
				if(pendingIntent != null)
					notiElemRemoteView.setOnClickPendingIntent(R.id.noti_elem, pendingIntent);
			}

			BitmapDrawable iconDrawable = (BitmapDrawable)viewableUserBhv.getIcon();
			notiElemRemoteView.setImageViewBitmap(R.id.noti_elem_icon, iconDrawable.getBitmap());
			
			if (bhvType == UserBhvType.CALL/* || bhvType == UserBhvType.SENSOR_ON*/){
//				Bitmap callContactIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sym_action_call);
				notiElemRemoteView.setTextViewText(R.id.noti_elem_text, viewableUserBhv.getBhvNameText());

				float textSize = cxt.getResources().getDimension(R.dimen.notibar_text_size);
				final int sdkVersion = android.os.Build.VERSION.SDK_INT;
				if(sdkVersion < Build.VERSION_CODES.JELLY_BEAN) {
					notiElemRemoteView.setFloat(R.id.noti_elem_text, "setTextSize", textSize);
				} else {
					notiElemRemoteView.setTextViewTextSize(R.id.noti_elem_text, 
							TypedValue.COMPLEX_UNIT_PX, 
							textSize);
				}
			}
			
			Integer notibarContainerId = viewableUserBhv.getNotibarContainerId();
			if(notibarContainerId == null)
				continue;
			
			notiRemoteView.addView(notibarContainerId, notiElemRemoteView);
		}

		return notiRemoteView;
	}
	
	public int getNotibarWidth(){
		WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
	    DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = cxt.getResources().getDisplayMetrics().density;
	    return (int)(outMetrics.widthPixels / density);
	}
}

//View notiLayout = layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);
//ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//ImageView iconSlot = (ImageView) notiLayout.findViewById(viewIdList.get(i));
//LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//LinearLayout notiViewLayout = (LinearLayout)layoutInflater.inflate(notiRemoteViews.getLayoutId(), null);

//private int getNotiViewWidth() {
//WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
//Display display = wm.getDefaultDisplay();
//Point size = new Point();
//display.getSize(size);
//return size.x;
//}
