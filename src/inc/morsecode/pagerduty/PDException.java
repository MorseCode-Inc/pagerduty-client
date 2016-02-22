package inc.morsecode.pagerduty;

public class PDException extends RuntimeException {

	private static final long serialVersionUID = -653261567933098137L;

	public PDException() {
		super();
	}

	public PDException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public PDException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PDException(String arg0) {
		super(arg0);
	}

	public PDException(Throwable arg0) {
		super(arg0);
	}

}
