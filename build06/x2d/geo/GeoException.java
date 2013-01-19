package bg.x2d.geo;

import bg.x2d.TDXException;

public class GeoException extends TDXException {

	/**
	 * The standard exception thrown by all classes in the bg.tdx.geo package when a Geometric error occurs (often caused by invalid arguments passed to a geometry method).<br>
	 * This class does nothing but subclass type TDXException and slap its own name onto it.
	 */
	private static final long serialVersionUID = 975059383220158740L;

	public GeoException() {
		super("Geometric error");
	}

	public GeoException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public GeoException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public GeoException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
