/*
 *  Copyright © 2011-2013 Brian Groenke
 *  All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  This Source Code Form is subject to the terms of the
 *  Mozilla Public License, v. 2.0. If a copy of the MPL 
 *  was not distributed with this file, You can obtain one at 
 *  http://mozilla.org/MPL/2.0/.
 */

package com.snap2d.input;

import com.jogamp.newt.event.KeyEvent;

/**
 * @author Brian Groenke
 *
 */
public class GLKeyEvent {
	
	public static final int EVENT_KEY_PRESSED = KeyEvent.EVENT_KEY_PRESSED,
			EVENT_KEY_RELEASED = KeyEvent.EVENT_KEY_RELEASED;
	public static final char NULL_CHAR = KeyEvent.NULL_CHAR;
	
	// ---- begin NEWT key code field binding ---- //
	public static final short VK_0 = KeyEvent.VK_0;
	public static final short VK_1 = KeyEvent.VK_1;
	public static final short VK_2 = KeyEvent.VK_2;
	public static final short VK_3 = KeyEvent.VK_3;
	public static final short VK_4 = KeyEvent.VK_4;
	public static final short VK_5 = KeyEvent.VK_5;
	public static final short VK_6 = KeyEvent.VK_6;
	public static final short VK_7 = KeyEvent.VK_7;
	public static final short VK_8 = KeyEvent.VK_8;
	public static final short VK_9 = KeyEvent.VK_9;
	public static final short VK_A = KeyEvent.VK_A;
	public static final short VK_ACCEPT = KeyEvent.VK_ACCEPT;
	public static final short VK_ADD = KeyEvent.VK_ADD;
	public static final short VK_AGAIN = KeyEvent.VK_AGAIN;
	public static final short VK_ALL_CANDIDATES = KeyEvent.VK_ALL_CANDIDATES;
	public static final short VK_ALPHANUMERIC = KeyEvent.VK_ALPHANUMERIC;
	public static final short VK_ALT = KeyEvent.VK_ALT;
	public static final short VK_ALT_GRAPH = KeyEvent.VK_ALT_GRAPH;
	public static final short VK_AMPERSAND = KeyEvent.VK_AMPERSAND;
	public static final short VK_ASTERISK = KeyEvent.VK_ASTERISK;
	public static final short VK_AT = KeyEvent.VK_AT;
	public static final short VK_B = KeyEvent.VK_B;
	public static final short VK_BACK_QUOTE = KeyEvent.VK_BACK_QUOTE;
	public static final short VK_BACK_SLASH = KeyEvent.VK_BACK_SLASH;
	public static final short VK_BACK_SPACE = KeyEvent.VK_BACK_SPACE;
	public static final short VK_BEGIN = KeyEvent.VK_BEGIN;
	public static final short VK_C = KeyEvent.VK_C;
	public static final short VK_CANCEL = KeyEvent.VK_CANCEL;
	public static final short VK_CAPS_LOCK = KeyEvent.VK_CAPS_LOCK;
	public static final short VK_CIRCUMFLEX = KeyEvent.VK_CIRCUMFLEX;
	public static final short VK_CLEAR = KeyEvent.VK_CLEAR;
	public static final short VK_CLOSE_BRACKET = KeyEvent.VK_CLOSE_BRACKET;
	public static final short VK_CODE_INPUT = KeyEvent.VK_CODE_INPUT;
	public static final short VK_COLON = KeyEvent.VK_COLON;
	public static final short VK_COMMA = KeyEvent.VK_COMMA;
	public static final short VK_COMPOSE = KeyEvent.VK_COMPOSE;
	public static final short VK_CONTEXT_MENU = KeyEvent.VK_CONTEXT_MENU;
	public static final short VK_CONTROL = KeyEvent.VK_CONTROL;
	public static final short VK_CONVERT = KeyEvent.VK_CONVERT;
	public static final short VK_COPY = KeyEvent.VK_COPY;
	public static final short VK_CUT = KeyEvent.VK_CUT;
	public static final short VK_D = KeyEvent.VK_D;
	public static final short VK_DECIMAL = KeyEvent.VK_DECIMAL;
	public static final short VK_DELETE = KeyEvent.VK_DELETE;
	public static final short VK_DIVIDE = KeyEvent.VK_DIVIDE;
	public static final short VK_DOLLAR = KeyEvent.VK_DOLLAR;
	public static final short VK_DOWN = KeyEvent.VK_DOWN;
	public static final short VK_E = KeyEvent.VK_E;
	public static final short VK_END = KeyEvent.VK_END;
	public static final short VK_ENTER = KeyEvent.VK_ENTER;
	public static final short VK_EQUALS = KeyEvent.VK_EQUALS;
	public static final short VK_ESCAPE = KeyEvent.VK_ESCAPE;
	public static final short VK_EURO_SIGN = KeyEvent.VK_EURO_SIGN;
	public static final short VK_EXCLAMATION_MARK = KeyEvent.VK_EXCLAMATION_MARK;
	public static final short VK_F = KeyEvent.VK_F;
	public static final short VK_F1 = KeyEvent.VK_F1;
	public static final short VK_F10 = KeyEvent.VK_F10;
	public static final short VK_F11 = KeyEvent.VK_F11;
	public static final short VK_F12 = KeyEvent.VK_F12;
	public static final short VK_F13 = KeyEvent.VK_F13;
	public static final short VK_F14 = KeyEvent.VK_F14;
	public static final short VK_F15 = KeyEvent.VK_F15;
	public static final short VK_F16 = KeyEvent.VK_F16;
	public static final short VK_F17 = KeyEvent.VK_F17;
	public static final short VK_F18 = KeyEvent.VK_F18;
	public static final short VK_F19 = KeyEvent.VK_F19;
	public static final short VK_F2 = KeyEvent.VK_F2;
	public static final short VK_F20 = KeyEvent.VK_F20;
	public static final short VK_F21 = KeyEvent.VK_F21;
	public static final short VK_F22 = KeyEvent.VK_F22;
	public static final short VK_F23 = KeyEvent.VK_F23;
	public static final short VK_F24 = KeyEvent.VK_F24;
	public static final short VK_F3 = KeyEvent.VK_F3;
	public static final short VK_F4 = KeyEvent.VK_F4;
	public static final short VK_F5 = KeyEvent.VK_F5;
	public static final short VK_F6 = KeyEvent.VK_F6;
	public static final short VK_F7 = KeyEvent.VK_F7;
	public static final short VK_F8 = KeyEvent.VK_F8;
	public static final short VK_F9 = KeyEvent.VK_F9;
	public static final short VK_FINAL = KeyEvent.VK_FINAL;
	public static final short VK_FIND = KeyEvent.VK_FIND;
	public static final short VK_FULL_WIDTH = KeyEvent.VK_FULL_WIDTH;
	public static final short VK_G = KeyEvent.VK_G;
	public static final short VK_GREATER = KeyEvent.VK_GREATER;
	public static final short VK_H = KeyEvent.VK_H;
	public static final short VK_HALF_WIDTH = KeyEvent.VK_HALF_WIDTH;
	public static final short VK_HELP = KeyEvent.VK_HELP;
	public static final short VK_HIRAGANA = KeyEvent.VK_HIRAGANA;
	public static final short VK_HOME = KeyEvent.VK_HOME;
	public static final short VK_I = KeyEvent.VK_I;
	public static final short VK_INPUT_METHOD_ON_OFF = KeyEvent.VK_INPUT_METHOD_ON_OFF;
	public static final short VK_INSERT = KeyEvent.VK_INSERT;
	public static final short VK_INVERTED_EXCLAMATION_MARK = KeyEvent.VK_INVERTED_EXCLAMATION_MARK;
	public static final short VK_J = KeyEvent.VK_J;
	public static final short VK_JAPANESE_HIRAGANA = KeyEvent.VK_JAPANESE_HIRAGANA;
	public static final short VK_JAPANESE_KATAKANA = KeyEvent.VK_JAPANESE_KATAKANA;
	public static final short VK_JAPANESE_ROMAN = KeyEvent.VK_JAPANESE_ROMAN;
	public static final short VK_K = KeyEvent.VK_K;
	public static final short VK_KANA_LOCK = KeyEvent.VK_KANA_LOCK;
	public static final short VK_KATAKANA = KeyEvent.VK_KATAKANA;
	public static final short VK_KEYBOARD_INVISIBLE = KeyEvent.VK_KEYBOARD_INVISIBLE;
	public static final short VK_L = KeyEvent.VK_L;
	public static final short VK_LEFT = KeyEvent.VK_LEFT;
	public static final short VK_LEFT_BRACE = KeyEvent.VK_LEFT_BRACE;
	public static final short VK_LEFT_PARENTHESIS = KeyEvent.VK_LEFT_PARENTHESIS;
	public static final short VK_LESS = KeyEvent.VK_LESS;
	public static final short VK_M = KeyEvent.VK_M;
	public static final short VK_META = KeyEvent.VK_META;
	public static final short VK_MINUS = KeyEvent.VK_MINUS;
	public static final short VK_MODECHANGE = KeyEvent.VK_MODECHANGE;
	public static final short VK_MULTIPLY = KeyEvent.VK_MULTIPLY;
	public static final short VK_N = KeyEvent.VK_N;
	public static final short VK_NONCONVERT = KeyEvent.VK_NONCONVERT;
	public static final short VK_NUM_LOCK = KeyEvent.VK_NUM_LOCK;
	public static final short VK_NUMBER_SIGN = KeyEvent.VK_NUMBER_SIGN;
	public static final short VK_NUMPAD0 = KeyEvent.VK_NUMPAD0;
	public static final short VK_NUMPAD1 = KeyEvent.VK_NUMPAD1;
	public static final short VK_NUMPAD2 = KeyEvent.VK_NUMPAD2;
	public static final short VK_NUMPAD3 = KeyEvent.VK_NUMPAD3;
	public static final short VK_NUMPAD4 = KeyEvent.VK_NUMPAD4;
	public static final short VK_NUMPAD5 = KeyEvent.VK_NUMPAD5;
	public static final short VK_NUMPAD6 = KeyEvent.VK_NUMPAD6;
	public static final short VK_NUMPAD7 = KeyEvent.VK_NUMPAD7;
	public static final short VK_NUMPAD8 = KeyEvent.VK_NUMPAD8;
	public static final short VK_NUMPAD9 = KeyEvent.VK_NUMPAD9;
	public static final short VK_O = KeyEvent.VK_O;
	public static final short VK_OPEN_BRACKET = KeyEvent.VK_OPEN_BRACKET;
	public static final short VK_P = KeyEvent.VK_P;
	public static final short VK_PAGE_DOWN = KeyEvent.VK_PAGE_DOWN;
	public static final short VK_PAGE_UP = KeyEvent.VK_PAGE_UP;
	public static final short VK_PASTE = KeyEvent.VK_PASTE;
	public static final short VK_PAUSE = KeyEvent.VK_PAUSE;
	public static final short VK_PERCENT = KeyEvent.VK_PERCENT;
	public static final short VK_PERIOD = KeyEvent.VK_PERIOD;
	public static final short VK_PIPE = KeyEvent.VK_PIPE;
	public static final short VK_PLUS = KeyEvent.VK_PLUS;
	public static final short VK_PREVIOUS_CANDIDATE = KeyEvent.VK_PREVIOUS_CANDIDATE;
	public static final short VK_PRINTSCREEN = KeyEvent.VK_PRINTSCREEN;
	public static final short VK_PROPS = KeyEvent.VK_PROPS;
	public static final short VK_Q = KeyEvent.VK_Q;
	public static final short VK_QUESTIONMARK = KeyEvent.VK_QUESTIONMARK;
	public static final short VK_QUOTE = KeyEvent.VK_QUOTE;
	public static final short VK_QUOTEDBL = KeyEvent.VK_QUOTEDBL;
	public static final short VK_R = KeyEvent.VK_R;
	public static final short VK_RIGHT = KeyEvent.VK_RIGHT;
	public static final short VK_RIGHT_BRACE = KeyEvent.VK_RIGHT_BRACE;
	public static final short VK_RIGHT_PARENTHESIS = KeyEvent.VK_RIGHT_PARENTHESIS;
	public static final short VK_ROMAN_CHARACTERS = KeyEvent.VK_ROMAN_CHARACTERS;
	public static final short VK_S = KeyEvent.VK_S;
	public static final short VK_SCROLL_LOCK = KeyEvent.VK_SCROLL_LOCK;
	public static final short VK_SEMICOLON = KeyEvent.VK_SEMICOLON;
	public static final short VK_SEPARATOR = KeyEvent.VK_SEPARATOR;
	public static final short VK_SHIFT = KeyEvent.VK_SHIFT;
	public static final short VK_SLASH = KeyEvent.VK_SLASH;
	public static final short VK_SPACE = KeyEvent.VK_SPACE;
	public static final short VK_STOP = KeyEvent.VK_STOP;
	public static final short VK_SUBTRACT = KeyEvent.VK_SUBTRACT;
	public static final short VK_T = KeyEvent.VK_T;
	public static final short VK_TAB = KeyEvent.VK_TAB;
	public static final short VK_TILDE = KeyEvent.VK_TILDE;
	public static final short VK_U = KeyEvent.VK_U;
	public static final short VK_UNDEFINED = KeyEvent.VK_UNDEFINED;
	public static final short VK_UNDERSCORE = KeyEvent.VK_UNDERSCORE;
	public static final short VK_UNDO = KeyEvent.VK_UNDO;
	public static final short VK_UP = KeyEvent.VK_UP;
	public static final short VK_V = KeyEvent.VK_V;
	public static final short VK_W = KeyEvent.VK_W;
	public static final short VK_WINDOWS = KeyEvent.VK_WINDOWS;
	public static final short VK_X = KeyEvent.VK_X;
	public static final short VK_Y = KeyEvent.VK_Y;
	public static final short VK_Z = KeyEvent.VK_Z;
	// ---- //
	
	KeyEvent evt;

	/**
	 * 
	 */
	public GLKeyEvent(KeyEvent evt) {
		this.evt = evt;
	}
	
	public char getKeyChar() {
		return evt.getKeyChar();
	}
	
	public short getKeyCode() {
		return evt.getKeyCode();
	}
	
	public short getKeySymbol() {
		return evt.getKeySymbol();
	}
	
	public int getButtonDownCount() {
		return evt.getButtonDownCount();
	}
	
	public boolean isModifierKey() {
		return evt.isModifierKey();
	}
	
	public boolean isAltDown() {
		return evt.isAltDown();
	}
	
	public boolean isAltGraphDown() {
		return evt.isAltGraphDown();
	}
	
	public boolean isAnyButtonDown() {
		return evt.isAnyButtonDown();
	}
	
	public boolean isAutoRepeat() {
		return evt.isAutoRepeat();
	}
	
	public boolean isButtonDown(int btn) {
		return evt.isButtonDown(btn);
	}
	
	public boolean isConfined() {
		return evt.isConfined();
	}
	
	public boolean isControlDown() {
		return evt.isControlDown();
	}
	
	public boolean isInvisible() {
		return evt.isInvisible();
	}
	
	public boolean isMetaDown() {
		return evt.isMetaDown();
	}
	
	public boolean isShiftDown() {
		return evt.isShiftDown();
	}
	
	public boolean isConsumed() {
		return evt.isConsumed();
	}
	
	public long getWhen() {
		return evt.getWhen();
	}
	
	public short getEventType() {
		return evt.getEventType();
	}
	
	public void setConsumed(boolean consumed) {
		evt.setConsumed(consumed);
	}
	
	@Override
	public String toString() {
		return evt.toString();
	}
}
