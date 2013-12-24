package com.snap2d.script;

import static java.lang.Math.*;

import java.math.*;
import java.util.*;

abstract class MathRef {

	private static final GME gme = new GME();

	private static HashMap<Character, Integer> pmap = new HashMap<Character, Integer>();
	private static HashMap<Character, Assoc> amap = new HashMap<Character, Assoc>();
	private static HashMap<Character, MathOp> opMap = new HashMap<Character, MathOp>();

	public static final char AND_BOOL = '@', OR_BOOL = '$', EQUALS = '=', NOT_EQUALS = '\u00AC', LESS_EQUALS = '\u00AB', GREAT_EQUALS = '\u00BB';
	protected static final char[] OPERATORS = new char[] { '+', '-', '*', '/',
		'|', '&', '~', '%', '^', EQUALS, '>', '<', NOT_EQUALS, LESS_EQUALS, GREAT_EQUALS, AND_BOOL, OR_BOOL};
	protected static final char[] NUM_CHARS = new char[] { 'E' };
	protected static final char MULTIPLY = OPERATORS[2];

	public static int floatNum = 6;

	// init the built in operations/functions
	static {
		for (int i = 0; i < OPERATORS.length; i++) {
			int p = 0;
			MathOp mathOp = null;
			Assoc assc = Assoc.LEFT;
			switch (i) {
			case 0:
				mathOp = new AdditionOp();
			case 1:
				if (mathOp == null) {
					mathOp = new SubtractionOp();
				}
				p = 1;
				break;
			case 2:
				if (mathOp == null) {
					mathOp = new MultiplicationOp();
				}
			case 3:
				if (mathOp == null) {
					mathOp = new DivisionOp();
				}
			case 4:
				if (mathOp == null) {
					mathOp = new BitorOp();
				}
			case 5:
				if (mathOp == null) {
					mathOp = new BitandOp();
				}
			case 6:
				if (mathOp == null) {
					mathOp = new BitxorOp();
				}
			case 7:
				if (mathOp == null) {
					mathOp = new RemainderOp();
				}
				p = 2;
				break;
			case 8:
				if (mathOp == null) {
					mathOp = new PowerOp();
				}
				p = 3;
				assc = Assoc.RIGHT;
				break;
			case 9:
				if (mathOp == null) {
					mathOp = new EqualityOp();
				}
			case 10:
				if (mathOp == null) {
					mathOp = new GreaterOp();
				}
			case 11:
				if (mathOp == null) {
					mathOp = new LesserOp();
				}
			case 12:
				if(mathOp == null) {
					mathOp = new NotEqualsOp();
				}
			case 13:
				if(mathOp == null) {
					mathOp = new LessEqualsOp();
				}
			case 14:
				if(mathOp == null) {
					mathOp = new GreatEqualsOp();
				}
				p = 0;
				assc = Assoc.UNDEF;
				break;
			case 15:
				if(mathOp == null) {
					mathOp = new AndOp();
				}
			case 16:
				if(mathOp == null) {
					mathOp =  new OrOp();
				}
				p = -1;
				assc = Assoc.UNDEF;
				break;
			}
			pmap.put(OPERATORS[i], p);
			opMap.put(OPERATORS[i], mathOp);
			amap.put(OPERATORS[i], assc);
		}
	}

	/* Definitions of operator associativity */
	public static enum Assoc {
		LEFT, RIGHT, UNDEF;
	}

	public static void putOperator(char c, int precedence, MathOp operation) {
		opMap.put(c, operation);
		pmap.put(c, precedence);
	}

	public static char getDefaultMultiplyOp() {
		return MULTIPLY;
	}

	public static boolean isOperator(char c) {
		return opMap.containsKey(c);
	}

	/*
	 * Fetches operator precedence value
	 */
	public static int getOpPreced(char c) {
		return pmap.get(c);
	}

	/*
	 * Checks to see if the operator precedes the other based both on precedence
	 * AND associativity.
	 */
	public static boolean opPreceeds(char o1, char o2)
			throws MathParseException {
		int o1p = pmap.get(o1);
		int o2p = pmap.get(o2);
		boolean rightAssoc = false;
		if (o1 == o2) {
			Assoc assc = amap.get(o1);
			if (assc.equals(Assoc.RIGHT)) {
				rightAssoc = true;
			} else if (assc.equals(Assoc.UNDEF)) {
				throw (new MathParseException("syntax: operator " + o1
						+ " has undefined associativity"));
			}
		}

		return rightAssoc ? o2p < o1p : o2p <= o1p;
	}

	public static double doOperator(char op, double a, double b) {
		double ans;
		MathOp mathOp = opMap.get(op);
		if(mathOp == null)
			return 0.0f;
		else
			ans = mathOp.eval(a, b);
		return ans;
	}

	public static MathOp getOperatorOp(char op) {
		return opMap.get(op);
	}
	
	public static char matchBytecode(byte b) {
		switch(b) {
		case Bytecodes.ADD:
			return OPERATORS[0];
		case Bytecodes.SUBTRACT:
			return OPERATORS[1];
		case Bytecodes.MULTIPLY:
			return OPERATORS[2];
		case Bytecodes.DIVIDE:
			return OPERATORS[3];
		case Bytecodes.BITOR:
			return OPERATORS[4];
		case Bytecodes.BITAND:
			return OPERATORS[5];
		case Bytecodes.BITXOR:
			return OPERATORS[6];
		case Bytecodes.MODULO:
			return OPERATORS[7];
		case Bytecodes.POW:
			return OPERATORS[8];
		case Bytecodes.EQUALS:
			return OPERATORS[9];
		case Bytecodes.GREATER:
			return OPERATORS[10];
		case Bytecodes.LESSER:
			return OPERATORS[11];
		case Bytecodes.NOT_EQUALS:
			return OPERATORS[12];
		case Bytecodes.LESS_EQUALS:
			return OPERATORS[13];
		case Bytecodes.GREAT_EQUALS:
			return OPERATORS[14];
		case Bytecodes.AND:
			return OPERATORS[15];
		case Bytecodes.OR:
			return OPERATORS[16];
		default:
			return 0;
		}
	}

	public static boolean isNumChar(char c) {
		for (char nc : NUM_CHARS) {
			if (c == nc) {
				return true;
			}
		}
		return false;
	}

	private static class GME {

		// These variables are part of the GME base structure.
		protected double a, b, c;
		protected int polyFound;

		public static final double PI = 3.14159265358979323846264338327950288419716;
		public static final double E = 2.7182818284590452353602874713526624977572470;
		public static final String STRING_PI = "3.14159265358979323846264338327950288419716";
		public static final String STRING_E = "2.7182818284590452353602874713526624977572470";

		public static boolean rads;

		// ---------------------------------------------------

		public double last() {
			return c;
		}

		public double sqr(double a) {
			b = sqrt(a);
			return b;
		}

		public double root(double a, double root) {
			c = Math.pow(a, 1 / root);
			return c;
		}

		public double absv(double a) {
			b = abs(a);
			return b;
		}

		public double pwr(double a, double b) {
			c = pow(a, b);
			return c;
		}

		public double log(double a) {
			b = log10(a);
			return b;
		}

		public double ln(double a) {
			b = log(a);
			return b;
		}

		public double max(double a, double b) {
			c = (a > b) ? a : b;
			return c;
		}

		public double min(double a, double b) {
			c = (a < b) ? a : b;
			return c;
		}

		public double tan(double a) {
			c = Math.tan(rads ? a : toRadians(a));
			return c;
		}

		public double cos(double a) {
			c = Math.cos(rads ? a : toRadians(a));
			return c;
		}

		public double sin(double a) {
			c = Math.sin(rads ? a : toRadians(a));
			return c;
		}

		public double cot(double a) {
			b = 1 / tan(a);
			return b;
		}

		public double sec(double a) {
			b = 1 / cos(a);
			return b;
		}

		public double csc(double a) {
			b = 1 / sin(a);
			return b;
		}

		public double arcTan(double a) {
			c = atan(rads ? a : toRadians(a));
			return rads ? c : toDegrees(c);
		}

		public double arcCos(double a) {
			c = acos(rads ? a : toRadians(a));
			return rads ? c : toDegrees(c);
		}

		public double arcSin(double a) {
			c = asin(rads ? a : toRadians(a));
			return rads ? c : toDegrees(c);
		}

		/*
		 * The rounding method uses the BigDecimal method of rounding half up.
		 * Decimals 1-4 round to nearest floating floor while decimals 5-9 round to
		 * nearest floating ceiling
		 */
		public double roundNum(double toRound, int places) {
			BigDecimal bd = new BigDecimal(toRound);
			bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
			a = bd.doubleValue();
			return a;
		}
	}
	
	protected interface MathOp {

		public double eval(double... args);

		public int argCount();
	}
	
	// ----Built in operator functions-------//

	private static class AdditionOp implements MathOp {
		@Override
		public double eval(double... args) {
			return args[0] + args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class SubtractionOp implements MathOp {
		@Override
		public double eval(double... args) {
			return args[0] - args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class MultiplicationOp implements MathOp {
		@Override
		public double eval(double... args) {
			return args[0] * args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class DivisionOp implements MathOp {
		@Override
		public double eval(double... args) {
			return args[0] / args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class BitorOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (int) args[0] | (int) args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class BitandOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (int) args[0] & (int) args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class BitxorOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (int) args[0] ^ (int) args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class RemainderOp implements MathOp {
		@Override
		public double eval(double... args) {
			return args[0] % args[1];
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class PowerOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (float) gme.pwr(args[0], args[1]);
		}

		@Override
		public int argCount() {
			return 2;
		}
	}

	public static class EqualityOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (args[0] == args[1]) ? 1:0;
		}

		@Override
		public int argCount() {
			return 2;
		}
	}
	
	public static class GreaterOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (args[0] > args[1]) ? 1:0;
		}
		
		@Override
		public int argCount() {
			return 2;
		}
	}
	
	public static class LesserOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (args[0] < args[1]) ? 1:0;
		}
		
		@Override
		public int argCount() {
			return 2;
		}
	}
	
	public static class AndOp implements MathOp {

		/**
		 *
		 */
		@Override
		public double eval(double... args) {
			return (args[0] != 0 && args[1] != 0) ? 1:0;
		}

		/**
		 *
		 */
		@Override
		public int argCount() {
			return 2;
		}
	}
	
	public static class OrOp implements MathOp {
		
		/**
		 *
		 */
		@Override
		public double eval(double... args) {
			return (args[0] != 0|| args[1] != 0) ? 1:0;
		}
		
		/**
		 *
		 */
		@Override
		public int argCount() {
			return 2;
		}
	}
	
	public static class NotEqualsOp implements MathOp {
		@Override
		public double eval(double... args) {
			return (args[0] != args[1]) ? 1:0;
		}
		
		@Override
		public int argCount() {
			return 1;
		}
	}
	
	public static class NotOp implements MathOp {
		
		@Override
		public double eval(double... args) {
			if(args[0] == 0)
				return 1;
			else
				return 0;
		}
		
		@Override
		public int argCount() {
			return 1;
		}
	}
	
	public static class LessEqualsOp implements MathOp {

		/**
		 *
		 */
		@Override
		public double eval(double... args) {
			return (args[0] <= args[1]) ? 1:0;
		}

		/**
		 *
		 */
		@Override
		public int argCount() {
			return 2;
		}
		
	}
	
	public static class GreatEqualsOp implements MathOp {
		
		/**
		 *
		 */
		@Override
		public double eval(double... args) {
			return (args[0] >= args[1]) ? 1:0;
		}
		
		/**
		 *
		 */
		@Override
		public int argCount() {
			return 2;
		}
		
	}
}
