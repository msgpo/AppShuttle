package lab.davidahn.appshuttle.context.bhv;

public interface UserBhv {
	public BhvType getBhvType();
	public void setBhvType(BhvType bhvType);
	public String getBhvName();
	public void setBhvName(String bhvName);
	public Object getMeta(String key);
	public void setMeta(String key, Object val);
}
