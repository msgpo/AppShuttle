package lab.davidahn.appshuttle.collect.env;


public class InvalidUserLoc extends UserLoc {
	private static InvalidUserLoc invalidUserLoc = new InvalidUserLoc();
	
	private InvalidUserLoc() {
		super(0,0);
	}
	
	public static InvalidUserLoc getInstance(){
		return invalidUserLoc;
	}
	
	@Override
	public double getLongitude() throws InvalidUserEnvException {
		throw new InvalidUserEnvException(EnvType.INVALID_LOCATION, this);
	}
		
	@Override		
	public double getLatitude() throws InvalidUserEnvException {
		throw new InvalidUserEnvException(EnvType.INVALID_LOCATION, this);
	}
	
	@Override
	public boolean isValid(){
		return false;
	}
	
//	@Override
//	public boolean isSame(UserLoc uLoc) throws InvalidUserEnvException {
//		throw new InvalidUserEnvException(EnvType.INVALID_LOCATION, this);
//	}
	
	@Override
	public boolean proximity(UserLoc uLoc, int toleranceInMeter) throws InvalidUserEnvException {
		throw new InvalidUserEnvException(EnvType.INVALID_LOCATION, this);
	}
	
	@Override
	public double distanceTo(UserLoc uLoc) throws InvalidUserEnvException {
		throw new InvalidUserEnvException(EnvType.INVALID_LOCATION, this);
	}

	
	@Override
	public EnvType getEnvType(){
		return EnvType.INVALID_LOCATION;
	}
	
	@Override
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append("invalid");
		return msg.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof InvalidUserLoc)
			return true;
		else 
			return false;
	}
	
	@Override
	public int hashCode(){
		return 0;
	}
}