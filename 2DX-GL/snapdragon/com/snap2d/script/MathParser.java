/*
 *  Copyright � 2011-2013 Brian Groenke
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

/**
 * @author Brian Groenke
 *
 */
class MathParser {

	public static final String SEP = ":";

	protected float parse(String input) throws MathParseException {
		StringBuilder sb = new StringBuilder(input);
		sb.replace(0, sb.length(), sb.toString().replaceAll("\\s", ""));
		sb.replace(0, sb.length(), findNegatives(sb.toString()));
		sb.replace(0, sb.length(), checkDelimiters(sb.toString()));
		if (mismatch) {
			System.out.println("Assuming mismatched delimeter positions: "
					+ sb.toString());
		}
		
		float result = calculate(sb.toString());
		return result;
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

	/*
	 * Calculates the result with the String parsed by the shuntingYard(String)
	 */
	protected float calculate(String input) throws MathParseException {
		input = shuntingYard(input);
		StringBuilder num = new StringBuilder();
		Vector<Float> numStack = new Vector<Float>(1, 1);
		char[] chars = input.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (isNumber(chars[i])) {
				num.append(chars[i]);
			} else if (Character.toString(chars[i]).equals(SEP)) {
				if (num.length() > 0) {
					try {
						float val = Float.parseFloat(num.toString());
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
					throw (new MathParseException(
							"error parsing input: too few operands"));
				}

				float x1 = numStack.get(numStack.size() - 2);
				float x2 = numStack.get(numStack.size() - 1);
				float res = MathRef.doOperator(chars[i], x1, x2);
				numStack.set(numStack.size() - 2, res);
				numStack.setSize(numStack.size() - 1);
			}
		}

		if (num.length() > 0) {
			try {
				float val = Float.parseFloat(num.toString());
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

		float ans = numStack.firstElement();

		return ans;
	}

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
			if (MathRef.isOperator(c)) {
				Character peek = stack.peekFirst();
				while (peek != null && peek != '(' && peek != ')'
						&& MathRef.opPreceeds(peek, c)) {
					output.append(SEP + stack.poll());
					peek = stack.peekFirst();
				}
				stack.push(c);
			} else if (isNumber(c) || Character.isLetter(c)) {
				if (isNumber(last) || Character.isLetter(c)) {
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
			}
			last = c;
		}

		Character next = stack.poll();
		while (next != null) {
			output.append(SEP + next);
			next = stack.poll();
		}

		return output.toString();
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

	public boolean isDelimter(char c) {
		return (c == '(' || c == ')' || c == ',' || c == '[' || c == ']');
	}
	
	public boolean isNonClosingDelimiter(char c) {
		return isDelimter(c) && c != ']' && c != ')';
	}
	
	public boolean isNonOpeningDelimter(char c) {
		return isDelimter(c) && c != '(' && c != '[';
	}
}
