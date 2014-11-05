/*
 *  Copyright (C) 2011-2014 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.script;

import java.util.*;

import bg.x2d.math.DoubleMath;

import com.snap2d.script.MathRef.MathOp;
import com.snap2d.script.VecMath.VecOp;

/**
 * @author Brian Groenke
 *
 */
public class MathParser {

	public static final String SEP = ":";

	private static final String[] CONSTANT_NAMES = new String[] {"pi", "e"};
	private static final double[] CONSTANT_VALS = new double[] {Math.PI, Math.E};
	private static final HashMap<String, Double> constMap = new HashMap<String, Double>();

	public int roundTo = 6;

	static {
		for(int i=0; i < CONSTANT_NAMES.length; i++) {
			constMap.put(CONSTANT_NAMES[i], CONSTANT_VALS[i]);
		}
	}

	/**
	 * Parses a mathematical String expression and returns the result as an Operand.
	 * Result may be a scalar or vector depending on expression.
	 * @param input
	 * @return the result as a VecMath.Operand type (sub-type Scalar or Vec).
	 * @throws MathParseException
	 */
	public Operand parse(String input) throws MathParseException {
		StringBuilder sb = new StringBuilder(input);
		sb.replace(0, sb.length(), findConstants(sb.toString()));
		format(sb);
		String rpn = shuntingYard(sb.toString());
		Operand result = calculate(rpn);
		return result;
	}
	
	/**
	 * Parses a mathematical String expression and returns the result as a double.
	 * All operands in the expression must be scalars.  For vector support, use
	 * {@link #parse(String)}
	 * @param input
	 * @return the result as a double value
	 * @throws MathParseException
	 */
	public double parseScalars(String input) throws MathParseException {
		StringBuilder sb = new StringBuilder(input);
		sb.replace(0, sb.length(), findConstants(sb.toString()));
		format(sb);
		String rpn = shuntingYard(sb.toString());
		double result = calculateScalars(rpn);
		return result;
	}

	protected void format(StringBuilder sb) {
		sb.replace(0, sb.length(), sb.toString().replaceAll("\\s", ""));
		sb.replace(0, sb.length(), findNegatives(sb.toString()));
		sb.replace(0, sb.length(), checkDelimiters(sb.toString()));
		if (mismatch) {
			System.out.println("MathParser: Assuming mismatched delimeter positions: "
					+ sb.toString());
		}
	}

	protected String findConstants(String input) {
		for(String s : constMap.keySet()) {
			input = input.replaceAll(s, String.valueOf(constMap.get(s)));
		}
		return input;
	}

	protected String findNegatives(String input) {
		StringBuilder sb = new StringBuilder(input);
		char neg = '-';
		int rep = 0;
		for (int i = 0; i < sb.length(); i++) {
			if (i == 0 && sb.charAt(i) == neg) {
				sb.insert(0, rep);
				continue;
			}

			if (sb.charAt(i) == neg && isNumber(sb.charAt(i + 1))) {
				if (MathRef.isOperator(sb.charAt(i - 1))
						|| isNonClosingDelimiter(sb.charAt(i - 1))) {
					for (int ii = i + 1; ii < sb.length(); ii++) {
						if (ii == sb.length() - 1) {
							sb.insert(ii + 1, ')');
							sb.insert(i, rep);
							sb.insert(i, '(');
							break;
						} else {
							if (!isNumber(sb.charAt(ii))) {
								sb.insert(ii, ')');
								sb.insert(i, rep);
								sb.insert(i, '(');
								break;
							}
						}
					}
				}
			}
		}
		return sb.toString();
	}

	public static final char BRACE_OPEN = '{', BRACE_CLOSE = '}';

	/*
	 * Should be called with input string before calling calculate(String)
	 * Converts to RPN using the shunting yard algorithm.
	 */
	protected String shuntingYard(String input) throws MathParseException {
		StringBuilder output = new StringBuilder();
		ArrayDeque<Character> stack = new ArrayDeque<Character>();
		char[] chars = input.toCharArray();

		char last = '0';
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if(isBrace(c)) {
				int end = findBraceClose(input, i);
				output.append(((output.length() > 0) ? SEP:"") + input.substring(i, end + 1));
				i = end;
				continue;
			}

			if (MathRef.isOperator(c)) {
				Character peek = stack.peekFirst();
				while (peek != null && peek != '(' && peek != ')'
						&& MathRef.opPreceeds(peek, c)) {
					output.append(SEP + stack.poll());
					peek = stack.peekFirst();
				}
				stack.push(c);
			} else if (isNumber(c) || isTypedChar(c) || isBrace(c)) {
				if (isNumber(last) || isTypedChar(last) || output.length() == 0) {
					output.append(c);
				} else {
					output.append(SEP + c);
				}
			} else if (c == '(') {
				if (isNumber(last) && i > 0) {
					stack.push(MathRef.getDefaultMultiplyOp());
				}
				stack.push(c);
			} else if (c == ')') {
				char next = stack.poll();
				while (next != '(') {
					output.append(SEP + next);
					next = stack.poll();
				}

				if (i < chars.length - 1 && !MathRef.isOperator(chars[i + 1]) && chars[i + 1] != ')') {
					//stack.push(MathRef.getDefaultMultiplyOp());
				}
			} else if(c == '[') {
				if(output.length() == 0)
				    output.append('[');
				else
					output.append(SEP + '[');
				i++;
				for(;chars[i] != ']';i++) {
					c = chars[i];
					if(c == '[')
						throw(new MathParseException("found '[' before close in vector declaration"));
					output.append(c);
				}
				output.append(']');
				i--;
			} else if (Character.toString(c).equals(Keyword.CAST_INT.sym))
				output.append(c);
			last = c;
		}

		Character next = stack.poll();
		while (next != null) {
			output.append(SEP + next);
			next = stack.poll();
		}

		return output.toString();
	}

	/*
	 * Calculates the result of the String 'input' result of shuntingYard(String)
	 * Supports all math operations defined in MathRef and VecMath
	 */
	protected Operand calculate(String input) throws MathParseException {
		StringBuilder num = new StringBuilder();
		Vector<Operand> numStack = new Vector<Operand>(); // Vector collection NOT VecMath.Vec ....
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (isNumber(chars[i]) || isDelimiter(chars[i]) || chars[i] == '-' && 
					(i+1 < chars.length && isNumber(chars[i+1]))) //check for negatives
			{
				num.append(chars[i]);
			} else if (Character.toString(chars[i]).equals(SEP)) {
				if (num.length() > 0) {
					if(num.toString().startsWith("[")) {
						String[] pts = num.toString().substring(1, num.length() - 1).split(",");
						double x = Double.parseDouble(pts[0]);
						double y = Double.parseDouble(pts[1]);
						numStack.add(new Vec2(x, y));
						num.delete(0, num.length());
						num.trimToSize();
					} else {
						try {
							double val = Double.parseDouble(num.toString());
							numStack.add(new Scalar(val));
							num.delete(0, num.length());
							num.trimToSize();
						} catch (NumberFormatException e) {
							// e.printStackTrace();
							throw (new MathParseException("error parsing input: "
									+ num.toString()));
						}
					}
				}
			} else if (MathRef.isOperator(chars[i])) {
				if (numStack.size() < 2) {
					System.err.println("MATH-PARSE: " + input);
					throw (new MathParseException(
							"error parsing input: too few operands"));
				}

				Operand x1 = numStack.get(numStack.size() - 2);
				Operand x2 = numStack.get(numStack.size() - 1);
				VecOp vecOp = VecMath.getOp(chars[i]);
				Operand res;
				if(vecOp != null && (x1.isVector() || x2.isVector()))
					res = vecOp.eval(x1, x2);
				else {
					if(x1.isVector() || x2.isVector())
						throw(new MathParseException("operator not valid for vector type: " + chars[i]));
					MathOp mathOp = MathRef.getOperatorOp(chars[i]);
					double result = mathOp.eval((Double)x1.getValue(), (Double)x2.getValue());
					res = new Scalar(result);
				}
				numStack.set(numStack.size() - 2, res);
				numStack.setSize(numStack.size() - 1);
			}
		}

		if (num.length() > 0) {
			if(num.toString().startsWith("[")) {
				String[] pts = num.toString().substring(1, num.length() - 1).split(",");
				double x = Double.parseDouble(pts[0]);
				double y = Double.parseDouble(pts[1]);
				numStack.add(new Vec2(x, y));
			} else {
				try {
					double val = Double.parseDouble(num.toString());
					numStack.add(new Scalar(val));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw (new MathParseException("error parsing input: "
							+ num.toString()));
				}
			}
		}
		if (numStack.size() != 1) {
			throw (new MathParseException(
					"error parsing input: operand evaluation did not complete properly"));
		}

		Operand ans = numStack.firstElement();

		return ans;
	}

	private boolean mismatch;

	private String checkDelimiters(String input) {
		int rt = 0;
		int lt = 0;
		char[] chars = input.toCharArray();
		ArrayList<Integer> mInserts = new ArrayList<Integer>();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '(') {
				lt++;
			} else if (chars[i] == ')') {
				rt++;
				if(i + 1 < chars.length && !MathRef.isOperator(chars[i+1]) && isNonClosingDelimiter(chars[i+1]))
					mInserts.add(i+1);
			}
		}

		StringBuilder sb = new StringBuilder(input);
		for(int i:mInserts)
			sb.insert(i, MathRef.getDefaultMultiplyOp());

		mismatch = rt != lt;

		if (rt > lt) {
			for (int i = 0; i < rt - lt; i++) {
				sb.insert(0, '(');
			}
		} else if (lt > rt) {
			for (int i = 0; i < lt - rt; i++) {
				sb.append(')');
			}
		}

		return sb.toString();
	}

	/*
	 * Checks if the character is a numerical (Arabic) digit 0-9 or a decimal
	 * point to accommodate for floating point numbers.
	 */
	public boolean isNumber(char c) {
		if (Character.isDigit(c) || c == '.' || MathRef.isNumChar(c)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Checks for letters and any other variable name valid characters
	 */
	public boolean isTypedChar(char c) {
		return Character.isLetter(c) || c == '_';
	}

	public boolean isDelimiter(char c) {
		return (c == '(' || c == ')' || c == ',' || c == '[' || c == ']');
	}

	public boolean isNonClosingDelimiter(char c) {
		return isDelimiter(c) && c != ']' && c != ')';
	}

	public boolean isNonOpeningDelimter(char c) {
		return isDelimiter(c) && c != '(' && c != '[';
	}

	public boolean isBrace(char c) {
		return c == BRACE_OPEN || c == BRACE_CLOSE;
	}

	public int findBraceClose(String input, int pos) {
		char[] chars = input.toCharArray();
		int start=0,end=0;
		for(int i=pos; i < input.length(); i++) {
			if(chars[i] == '{')
				start++;
			else if(chars[i] == '}') {
				end++;
				if(start == end)
					return i;
			}
		}

		return -1;
	}
	
	/*
	 * Calculates the result of the String 'input' result of shuntingYard(String)
	 * Old version - no vector support
	 */
	protected double calculateScalars(String input) throws MathParseException {
		StringBuilder num = new StringBuilder();
		Vector<Double> numStack = new Vector<Double>(1, 1);
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (isNumber(chars[i]) || chars[i] == '-' && (i+1 < chars.length && isNumber(chars[i+1]))) { //check for negatives
				num.append(chars[i]);
			} else if (Character.toString(chars[i]).equals(SEP)) {
				if (num.length() > 0) {
					try {
						double val = Double.parseDouble(num.toString());
						numStack.add(val);
						num.delete(0, num.length());
						num.trimToSize();
					} catch (NumberFormatException e) {
						// e.printStackTrace();
						throw (new MathParseException("error parsing input: "
								+ num.toString()));
					}
				}
			} else if (MathRef.isOperator(chars[i])) {
				if (numStack.size() < 2) {
					System.err.println("MATH-PARSE: " + input);
					throw (new MathParseException(
							"error parsing input: too few operands"));
				}

				double x1 = numStack.get(numStack.size() - 2);
				double x2 = numStack.get(numStack.size() - 1);
				double res = MathRef.doOperator(chars[i], x1, x2);
				numStack.set(numStack.size() - 2, res);
				numStack.setSize(numStack.size() - 1);
			}
		}

		if (num.length() > 0) {
			try {
				double val = Double.parseDouble(num.toString());
				numStack.add(val);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw (new MathParseException("error parsing input: "
						+ num.toString()));
			}
		}
		if (numStack.size() != 1) {
			throw (new MathParseException(
					"error parsing input: operand evaluation did not complete properly"));
		}

		double ans = numStack.firstElement();

		return DoubleMath.round(ans, roundTo);
	}
}
