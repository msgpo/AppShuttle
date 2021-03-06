package lab.davidahn.appshuttle.collect.env;

public class InvalidUserEnvException extends Exception {

	private static final long serialVersionUID = 7722839526630857076L;

	public InvalidUserEnvException(EnvType envType, UserEnv uEnv){
		super(envType.toString()+": "+uEnv.toString());
	}
}
