package lab.davidahn.appshuttle.predict.matcher;

import java.util.ArrayList;
import java.util.List;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.predict.matcher.conf.RecentMatcherConf;

public abstract class RecentMatcher extends BaseMatcher<RecentMatcherConf>{
	
	public RecentMatcher(RecentMatcherConf conf){
		super(conf);
	}
	
	@Override
	protected List<MatcherCountUnit> mergeHistoryByCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevDurationUserBhv = null;
		MatcherCountUnit.Builder mergedDurationUserBhvBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevDurationUserBhv == null){
				mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
			} else {
				if(durationUserBhv.getTimeDate().getTime() - prevDurationUserBhv.getEndTimeDate().getTime()
						< conf.getAcceptanceDelay()){
				} else {
					res.add(mergedDurationUserBhvBuilder.build());
					mergedDurationUserBhvBuilder = new MatcherCountUnit.Builder(durationUserBhv.getUserBhv());
				}
			}
			mergedDurationUserBhvBuilder.addRelatedDurationUserBhv(durationUserBhv);
			prevDurationUserBhv = durationUserBhv;
		}
		
		if(mergedDurationUserBhvBuilder != null)
			res.add(mergedDurationUserBhvBuilder.build());
		return res;
	}
	
	@Override
	protected double computeRelatedness(MatcherCountUnit durationUserBhv,
			SnapshotUserCxt uCxt) {
		return 1;
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		
		double score = 1 + likelihood;
		
		assert(1 <= score && score <=2);
		return score;
	}
}

//	private int getNumTotalCxt(UserCxt uCxt){
//		int numTotalHistory = 0;
//		long etime = uCxt.getTime().getTime();
//		long sTime = etime - settings.getLong("matcher.duration", 5 * AlarmManager.INTERVAL_DAY);
//
//		for(UserBhv uBhv : userBhvManager.retrieveBhv()){
//			List<RfdUserCxt> durationUserBhvList = contextManager.retrieveRfdCxtByBhv(sTime, etime, uBhv);
//			numTotalHistory+=durationUserBhvList.size();
//		}
//		return numTotalHistory;
//	}
	
//	@Override
//	protected List<MergedRfdUserCxt> mergeCxtByCountUnit(List<RfdUserCxt> durationUserBhvList) {
//		List<MergedRfdUserCxt> res = new ArrayList<MergedRfdUserCxt>();
//		Map<UserBhv, RfdUserCxt> ongoingBhvMap = new HashMap<UserBhv, RfdUserCxt>();
//
//		for(RfdUserCxt durationUserBhv : durationUserBhvList){
//			UserBhv uBhv = durationUserBhv.getBhv();
//			if(ongoingBhvMap.isEmpty()) {
//				ongoingBhvMap.put(uBhv, durationUserBhv);
//			} else {
//				if(ongoingBhvMap.containsKey(uBhv)){
//					RfdUserCxt prevDurationUserBhv = ongoingBhvMap.get(uBhv);
//					if(durationUserBhv.getStartTime().getTime() - prevDurationUserBhv.getEndTime().getTime()
//							< settings.getLong("matcher.recent.frequently.acceptance_delay", AlarmManager.INTERVAL_HOUR / 6)){
//						MergedRfdUserCxt mergedDurationUserBhv = prevDurationUserBhv;
//						mergedDurationUserBhv.setEndTime(durationUserBhv.getEndTime());
//						mergedDurationUserBhv.setLocs(durationUserBhv.getLocs());
//						mergedDurationUserBhv.setPlaces(durationUserBhv.getPlaces());
//						ongoingBhvMap.put(uBhv, mergedDurationUserBhv);
//					} else {
//						res.add(ongoingBhvMap.remove(uBhv));
//						ongoingBhvMap.put(uBhv, durationUserBhv);
//					}
//				} else {
//					ongoingBhvMap.put(uBhv, durationUserBhv);
//				}
//			}
//		}
//		for(UserBhv ongoingBhv : ongoingBhvMap.keySet()){
//			RfdUserCxt restDurationUserBhv = ongoingBhvMap.get(ongoingBhv);
//			res.add(restDurationUserBhv);
//		}
//		return res;
//	}
