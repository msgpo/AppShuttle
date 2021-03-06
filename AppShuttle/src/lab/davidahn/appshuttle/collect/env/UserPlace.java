package lab.davidahn.appshuttle.collect.env;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;


public class UserPlace extends UserEnv {
	protected String name;
	protected UserLoc coordinates;
//	protected Validity _validity;

//	public UserPlace() {}

	public UserPlace(String name, UserLoc coordinates) {
		this.name = name;
		this.coordinates = coordinates;
	}
	
	public static UserPlace create(String name, UserLoc coordinates) {
		if(name == null || name.equals(""))
			return InvalidUserPlace.getInstance();
		else
			return new UserPlace(name, coordinates);
	}
//	public UserPlace(double longitude, double latitude, String name, Validity validity) {
//		coordinates = new UserLoc(longitude, latitude, validity);
//		name = name;
//	}
	public String getName() throws InvalidUserEnvException {
//		if(_validity == Validity.INVALID)
//			throw new InvalidUserEnvException();
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UserLoc getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(UserLoc coordinates) {
		this.coordinates = coordinates;
	}
	
//	public boolean isSame(UserPlace uPlace) throws InvalidUserEnvException {
////		if(_validity == Validity.INVALID || !uPlace.isValid()) 
////			throw new InvalidUserEnvException();
//		if(equals(uPlace)) 
//			return true;
//		else 
//			return false;
//	}
	
	public EnvType getEnvType(){
		return EnvType.PLACE;
	}
	
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(name);
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof UserPlace)){
			return false;
		}
		
		SharedPreferences pref = AppShuttleApplication.getContext().getPreferences();
		int numPrefix = pref.getInt("collection.env.place.num_address_prefix_words", 6);
		
		String[] addrs = name.split(" ");
		String[] userAddrs = ((UserPlace)o).name.split(" ");
		
		int maxIndex = Math.min(numPrefix, Math.min(addrs.length, userAddrs.length));
		int index = 0;
		while(index < maxIndex){
			if(!addrs[index].equals(userAddrs[index]))
				return false;
			index++;
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
}