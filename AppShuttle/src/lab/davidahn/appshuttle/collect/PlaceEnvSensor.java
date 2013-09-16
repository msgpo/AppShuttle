package lab.davidahn.appshuttle.collect;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.context.env.UserLoc.Validity;
import lab.davidahn.appshuttle.context.env.UserPlace;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class PlaceEnvSensor implements EnvSensor {
	private static PlaceEnvSensor placeEnvSensor;
	private Geocoder geocoder;
	private UserPlace prevUPlace;
	private UserPlace currUPlace;
	private LocEnvSensor locEnvCollector;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	
	private PlaceEnvSensor(Context cxt){
		geocoder = new Geocoder(cxt);
		locEnvCollector = LocEnvSensor.getInstance(cxt);
		prevUPlace = null;
		currUPlace = null;
	}
	
	public synchronized static PlaceEnvSensor getInstance(Context cxt){
		if(placeEnvSensor == null) placeEnvSensor = new PlaceEnvSensor(cxt);
		return placeEnvSensor;
	}
	
	public UserPlace sense(){
		prevUPlace = currUPlace;
		currUPlace = new UserPlace(0, 0, null, Validity.INVALID);
		UserLoc currLoc = locEnvCollector.getCurrULoc();
		if(prevUPlace != null && 
				prevUPlace.isValid() && 
				currLoc.isValid() && 
				!locEnvCollector.isChanged()){
			currUPlace = prevUPlace;
		} else {
			try {
				double currLongitude = currLoc.getLongitude();
				double currLatitude = currLoc.getLatitude();
				try {
					List<Address> geocoded = geocoder.getFromLocation(currLatitude, currLongitude, 1);
					if(geocoded != null && !geocoded.isEmpty()) {
						Address addr = geocoded.get(0);
						String addressLine = addr.getAddressLine(0);
						
						if(addressLine == null) {
							return currUPlace;
						}

						String adminArea = addr.getAdminArea();
						String subAdminArea = addr.getSubAdminArea();
						String locality = addr.getLocality();
						String subLocality = addr.getSubLocality();
						
						StringBuilder sb = new StringBuilder();
						if(adminArea != null)
							sb.append(" ").append(adminArea);
						if(subAdminArea != null)
							sb.append(" ").append(subAdminArea);
						if(locality != null)
							sb.append(" ").append(locality);
						if(subLocality != null)
							sb.append(" ").append(subLocality);
						
						String placeName = sb.toString();
						
						double placeLongitude = currLongitude;
						double placeLatitude = currLatitude;
						
						if(addr.hasLongitude())
							placeLongitude = addr.getLongitude();
						if(addr.hasLatitude())
							placeLatitude = addr.getLatitude();
						
						currUPlace = new UserPlace(placeLongitude, placeLatitude, placeName);
					}
				} catch (IOException e) {
					;
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		return currUPlace;
	}
	
	public boolean isChanged(){
		boolean changed = false;
		if(prevUPlace != null){
			try {
				if(!currUPlace.isSame(prevUPlace)) {
					changed = true;
					Log.i("changed env", "place moved");
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		return changed;
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged()){
				durationUserEnvBuilder.setEndTime(currTimeDate);
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return new DurationUserEnv.Builder()
			.setTime(currTimeDate)
			.setEndTime(currTimeDate)
			.setTimeZone(currTimeZone)
			.setEnvType(EnvType.PLACE)
			.setUserEnv(uEnv);
	}
}
	
//	public DurationUserEnv extractDurationUserEnv(SnapshotUserCxt uCxt) {
//		DurationUserEnv res = null;
//		if(durationUserEnvBuilder == null) {
//			durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
//		} else {
//			if(isChanged()){
//				durationUserEnvBuilder.setEndTime(uCxt.getTimeDate());
//				res = durationUserEnvBuilder.build();
//				durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
//			}
//		}
//		return res;
//	}
//	
//	private DurationUserEnv.Builder makeDurationUserEnvBuilder(SnapshotUserCxt uCxt) {
//		return new DurationUserEnv.Builder()
//			.setTime(uCxt.getTimeDate())
//			.setEndTime(uCxt.getTimeDate())
//			.setTimeZone(uCxt.getTimeZone())
//			.setUserEnv(uCxt.getUserEnv(EnvType.PLACE));
//	}
//}

//StringTokenizer st = new StringTokenizer(addressLine);
//int numWord = preferenceSettings.getInt("collection.place.num_address_prefix_words", 3);

//while(st.hasMoreTokens() && numWord-- > 0){
//	sb.append(st.nextToken()).append(" ");
//}
//sb.deleteCharAt(sb.length()-1);
//String countryName = addr.getCountryName();
//if(countryName != null)
//	sb.append(countryName);