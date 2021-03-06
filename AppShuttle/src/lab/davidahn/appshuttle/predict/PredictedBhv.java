package lab.davidahn.appshuttle.predict;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.collect.bhv.UserBhvType;
import lab.davidahn.appshuttle.collect.env.EnvType;
import lab.davidahn.appshuttle.collect.env.UserEnv;
import lab.davidahn.appshuttle.predict.matcher.MatcherResultElem;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import android.content.Intent;

public class PredictedBhv implements UserBhv, Comparable<PredictedBhv> {
	private final UserBhv uBhv;
	private final long time;
	private final TimeZone timeZone;
	private final Map<EnvType, UserEnv> uEnvs;
	private final EnumMap<MatcherType, MatcherResultElem> matchersWithResult;
	private final double score;

	public PredictedBhv(long _time, TimeZone _timeZone, Map<EnvType, UserEnv> _userEnvs, UserBhv _uBhv, EnumMap<MatcherType, MatcherResultElem> _matcherResults, double _score){
		time = _time;
		timeZone = _timeZone;
		uEnvs = _userEnvs;
		uBhv = _uBhv;
		matchersWithResult = _matcherResults;
		score = _score;
	}

	public long getTime() {
		return time;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public Map<EnvType, UserEnv> getUserEnvMap() {
		return uEnvs;
	}
	public UserEnv getUserEnv(EnvType envType) {
		return uEnvs.get(envType);
	}
	public UserBhv getUserBhv() {
		return uBhv;
	}

	public Map<MatcherType, MatcherResultElem> getMatchersWithResult() {
		return matchersWithResult;
	}
	
	public MatcherResultElem getMatcherResult(MatcherType matcherType) {
		return matchersWithResult.get(matcherType);
	}

	public double getScore() {
		return score;
	}
	
//	public EnumMap<MatcherType, MatcherResultElem> getAllLeafMatcherWithResult(){
//		EnumMap<MatcherType, MatcherResultElem> matcherResultMap = new EnumMap<MatcherType, MatcherResultElem>(MatcherType.class);
//		for(MatcherType matcherType : matcherResults.keySet())
//			matcherResultMap.putAll(matcherResults.get(matcherType).getChildMatcherWithResult());
//		return matcherResultMap;
//	}
	
	@Override
	public boolean equals(Object o) {
		if ((o instanceof UserBhv)
				&& uBhv.getBhvName().equals(
						((UserBhv) o).getBhvName())
				&& uBhv.getBhvType() == ((UserBhv) o).getBhvType())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		return uBhv.hashCode();
	}
	
	@Override
	public int compareTo(PredictedBhv predictedBhvInfo){
		if(time > predictedBhvInfo.time) return 1;
		else if(time == predictedBhvInfo.time) return 0;
		else return -1;
//		return time.compareTo(predictedBhvInfo.time);
	}

	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("bhv: ").append(uBhv.toString()).append(", ");
		msg.append("time: ").append(new Date(time).toString()).append(", ");
		msg.append("matcherResults: ").append(matchersWithResult.toString()).append(", ");
		msg.append("score: ").append(score);
		return msg.toString();
	}

	public static List<PredictedBhv> getPredictedBhvList() {
		return new ArrayList<PredictedBhv>(AppShuttleApplication.predictedBhvMap.values());
	}
	
	protected static void updatePredictedBhvList(List<PredictedBhv> list) {
		Map<UserBhv, PredictedBhv> map = new HashMap<UserBhv, PredictedBhv>();
		for(PredictedBhv bhv : list)
			map.put(bhv.getUserBhv(), bhv);
		AppShuttleApplication.predictedBhvMap = map;
	}
	
	public static PredictedBhv getPredictedBhv(UserBhv bhv) {
		return AppShuttleApplication.predictedBhvMap.get(bhv);
	}
	
//	protected static void updatePredictedBhv(PredictedBhv bhv) {
//		AppShuttleApplication.predictedBhvMap.put(bhv.getUserBhv(), bhv);
//	}

	@Override
	public UserBhvType getBhvType() {
		return uBhv.getBhvType();
	}

	@Override
	public void setBhvType(UserBhvType bhvType) {
		uBhv.setBhvType(bhvType);
	}

	@Override
	public String getBhvName() {
		return uBhv.getBhvName();
	}

	@Override
	public void setBhvName(String bhvName) {
		uBhv.setBhvName(bhvName);
	}

	@Override
	public Map<String, Object> getMetas() {
		return uBhv.getMetas();
	}
	@Override
	public void setMetas(Map<String, Object> metas) {
		uBhv.setMetas(metas);
	}
	@Override
	public boolean isValid() {
		return uBhv.isValid();
	}

	
	public Intent getLaunchIntent(){
		if (uBhv == null)
			return null;
		
		return uBhv.getLaunchIntent(); 
	}
}
