package bg.x2d;

public class TDXException extends Exception {

	/**
	 * Generic exception thrown by classes in the 2DX software when internal errors occur.  This class should not be extended, nor thrown by objects outside of the 2DX API itself.
	 */
	private static final long serialVersionUID = 975059383220158740L;

	public TDXException() {
		// TODO Auto-generated constructor stub
	}

	public TDXException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TDXException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public TDXException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
