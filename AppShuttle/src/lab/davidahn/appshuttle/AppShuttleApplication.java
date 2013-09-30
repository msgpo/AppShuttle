package lab.davidahn.appshuttle;

import java.util.Set;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.UserBhv;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class AppShuttleApplication extends Application {
	private static AppShuttleApplication instance;
	private SharedPreferences preferenceSettings;
	
	private SnapshotUserCxt currUserCxt;
	private Set<UserBhv> recentPredictedBhvSet;
	private Set<UserBhv> recentPredictedBhvSetForView;
	
	
	public AppShuttleApplication(){
		instance = this;
	}
	public static AppShuttleApplication getContext(){
		return instance;
	}
	
	public SharedPreferences getPreferenceSettings(){
		if(preferenceSettings == null)
			preferenceSettings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		return preferenceSettings;
	}

	public SnapshotUserCxt getCurrUserCxt() {
		return currUserCxt;
	}
	public void setCurrUserCxt(SnapshotUserCxt currUserCxt) {
		this.currUserCxt = currUserCxt;
	}
	public Set<UserBhv> getRecentPredictedBhvSet() {
		return recentPredictedBhvSet;
	}
	public void setRecentPredictedBhvSet(Set<UserBhv> recentPredictedBhvSet) {
		this.recentPredictedBhvSet = recentPredictedBhvSet;
	}
	public Set<UserBhv> getRecentPredictedBhvSetForView() {
		return recentPredictedBhvSetForView;
	}
	public void setRecentPredictedBhvSetForView(
			Set<UserBhv> recentPredictedBhvSetForView) {
		this.recentPredictedBhvSetForView = recentPredictedBhvSetForView;
	}
}
