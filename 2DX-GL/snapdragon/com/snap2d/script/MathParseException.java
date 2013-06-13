package com.snap2d.script;

public class MathParseException extends ScriptCompilationException {

	/**
	 * @param message
	 * @param source
	 * @param charPos
	 */
	public MathParseException(String message) {
		super(message, null, 0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5617820949413283255L;

	public static final int UNSPECIFIED_POS = -1;

	int pos = UNSPECIFIED_POS;

	/*
	 * Value of -1 means not specified.
	 */
	public int getPos() {
		return pos;
	}
}
