package lab.davidahn.appshuttle.predict.matcher.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lab.davidahn.appshuttle.collect.SnapshotUserCxt;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhv;
import lab.davidahn.appshuttle.collect.bhv.DurationUserBhvDao;
import lab.davidahn.appshuttle.collect.bhv.UserBhv;
import lab.davidahn.appshuttle.predict.matcher.Matcher;
import lab.davidahn.appshuttle.predict.matcher.MatcherConf;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit;
import lab.davidahn.appshuttle.predict.matcher.MatcherCountUnit.Builder;
import lab.davidahn.appshuttle.predict.matcher.MatcherResult;
import lab.davidahn.appshuttle.predict.matcher.MatcherType;
import lab.davidahn.appshuttle.utils.Time;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public abstract class TimeMatcher extends Matcher {

	public TimeMatcher(MatcherConf conf){
		super(conf);
	}
	
	@Override
	public abstract MatcherType getType();
	
	@Override
	protected List<DurationUserBhv> getInvolvedDurationUserBhv(UserBhv uBhv, SnapshotUserCxt currUCxt) {
		DurationUserBhvDao durationUserBhvDao = DurationUserBhvDao.getInstance();

		long toTime = currUCxt.getTime() - conf.getTimeTolerance();
		long fromTime = toTime - conf.getDuration();
		
		List<DurationUserBhv> durationUserBhvList = durationUserBhvDao.retrieveByBhv(fromTime, toTime, uBhv);
		List<DurationUserBhv> pureDurationUserBhvList = new ArrayList<DurationUserBhv>();
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
//			if(durationUserBhv.getEndTime() - durationUserBhv.getTime()	< noiseTimeTolerance)
//				continue;
			pureDurationUserBhvList.add(durationUserBhv);
		}
		return pureDurationUserBhvList;
	}
	
	@Override
	protected List<MatcherCountUnit> makeMatcherCountUnit(List<DurationUserBhv> durationUserBhvList, SnapshotUserCxt uCxt) {
		List<MatcherCountUnit> res = new ArrayList<MatcherCountUnit>();

		DurationUserBhv prevDurationUserBhv = null;
		MatcherCountUnit.Builder matcherCountUnitBuilder = null;
		for(DurationUserBhv durationUserBhv : durationUserBhvList){
			if(prevDurationUserBhv == null){
				matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv);
			} else {
				if(durationUserBhv.getTime() - prevDurationUserBhv.getEndTime()
						< conf.getAcceptanceDelay()){
					matcherCountUnitBuilder.setProperty("endTime", durationUserBhv.getEndTime());
				} else {
					res.add(matcherCountUnitBuilder.build());
					matcherCountUnitBuilder = makeMatcherCountUnitBuilder(durationUserBhv);
				}
			}
			prevDurationUserBhv = durationUserBhv;
		}
		
		if(matcherCountUnitBuilder != null)
			res.add(matcherCountUnitBuilder.build());
		
		return res;
	}
	
	private Builder makeMatcherCountUnitBuilder(DurationUserBhv durationUserBhv) {
		return new MatcherCountUnit.Builder(durationUserBhv.getUserBhv())
		.setProperty("time", durationUserBhv.getTime())
		.setProperty("endTime", durationUserBhv.getEndTime())
		.setProperty("timeZone", durationUserBhv.getTimeZone());
	}
	
	@Override
	protected double computeInverseEntropy(List<MatcherCountUnit> matcherCountUnitList) {
		
		double inverseEntropy = 0;
		Set<Long> uniqueTime = new HashSet<Long>();
		
		for(MatcherCountUnit unit : matcherCountUnitList){
			long time = (Long)unit.getProperty("time");
			long timePeriodic = time % conf.getTimePeriod();
			Iterator<Long> it = uniqueTime.iterator();
			boolean unique = true;
			if(!uniqueTime.isEmpty()){
				while(it.hasNext()){
					Long uniqueTimeElem = it.next();
					long from = (uniqueTimeElem - conf.getTimeTolerance() + conf.getTimePeriod()) % conf.getTimePeriod();
					long to = (uniqueTimeElem + conf.getTimeTolerance()) % conf.getTimePeriod();
					if(Time.isIncludedIn(from, timePeriodic, to, conf.getTimePeriod())){
						unique = false;
						break;
					}
				}
			}
			if(unique)
				uniqueTime.add(timePeriodic);
		}
		int entropy = uniqueTime.size();
		if(entropy > 0) {
			inverseEntropy = 1.0 / entropy;
		} else {
			inverseEntropy = 0;
		}
		
		assert(0 <= inverseEntropy && inverseEntropy <= 1);
		
		return inverseEntropy;
	}

	@Override
	protected double computeRelatedness(MatcherCountUnit unit, SnapshotUserCxt uCxt) {
		double relatedness = 0;
		
		long currTime = uCxt.getTime();
		long currTimePeriodic = currTime % conf.getTimePeriod();
		long targetTime = (Long) unit.getProperty("time");
		long targetTimePeriodic = targetTime % conf.getTimePeriod();
		
		long mean = currTimePeriodic;
		long std = conf.getTimeTolerance() / 2;
		NormalDistribution nd = new NormalDistribution(mean, std);

		long from = (currTimePeriodic - conf.getTimeTolerance() + conf.getTimePeriod()) % conf.getTimePeriod();
		long to = (currTimePeriodic + conf.getTimeTolerance()) % conf.getTimePeriod();
				
		if(Time.isIncludedIn(from, targetTimePeriodic, to, conf.getTimePeriod())){
			relatedness = nd.density(targetTimePeriodic) / nd.density(mean);
		} else {
			relatedness = 0;
		}
		return relatedness;
	}
	
	@Override
	protected double computeLikelihood(int numTotalHistory,
		Map<MatcherCountUnit, Double> relatedHistoryMap,
		SnapshotUserCxt uCxt) {

		if(numTotalHistory <= 0)
			return 0;

		SummaryStatistics relatednessStat = new SummaryStatistics();
		for(double relatedness : relatedHistoryMap.values())
			relatednessStat.addValue(relatedness);
		
		return relatednessStat.getMean();
	}
	
	@Override
	protected double computeScore(MatcherResult matcherResult) {
		double likelihood = matcherResult.getLikelihood();
		double inverseEntropy = matcherResult.getInverseEntropy();
		double relatedHistoryRatio = 1.0 * matcherResult.getNumRelatedHistory() / conf.getDuration();
		
		double score = 1 + 0.5 * (inverseEntropy * relatedHistoryRatio) + 0.1 * likelihood;

		assert(1 <= score && score <=2);
		
		return score;
	}
	
	protected boolean isWeekDay(long date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(date));
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return false;
		
		return true;
	}
}
