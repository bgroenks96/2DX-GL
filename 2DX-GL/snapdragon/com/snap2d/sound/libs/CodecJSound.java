/*
 * Copyright © 2011-2012 Brian Groenke
 * All rights reserved.
 * 
 *  This file is part of the 2DX Graphics Library.
 *
 *  2DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  2DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 2DX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.snap2d.sound.libs;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.sound.sampled.*;

import paulscode.sound.*;

/**
 * Implementation of ICodec from the PaulsCode Sound System library for all JavaSound supported file formats.
 * This class simply utilizes the JavaSound API's pre-existing codecs.
 * @author Brian Groenke
 *
 */
public class CodecJSound implements ICodec {

	private AudioInputStream audioIn;
	private Semaphore sync = new Semaphore(1, true);
	private volatile boolean init, eos;

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#cleanup()
	 */
	@Override
	public void cleanup() {
		try {
			if(audioIn != null)
				audioIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		audioIn = null;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#endOfStream()
	 */
	@Override
	public boolean endOfStream() {
		boolean eos = false;
		try {
			sync.acquire();
			eos = this.eos;
			return eos;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sync.release();
		}
		return eos;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#getAudioFormat()
	 */
	@Override
	public AudioFormat getAudioFormat() {
		if(audioIn != null)
			return audioIn.getFormat();
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#initialize(java.net.URL)
	 */
	@Override
	public boolean initialize(URL sound) {
		try {
			sync.acquire();
			audioIn = AudioSystem.getAudioInputStream(sound);
			eos = false;
			init = true;
			sync.release();
			return true;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#initialized()
	 */
	@Override
	public boolean initialized() {
		boolean init = false;
		try {
			sync.acquire();
			init = this.init;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sync.release();
		}
		return init;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#read()
	 */
	@Override
	public SoundBuffer read() {
		if(audioIn == null || audioIn.getFormat() == null) {
			System.err.println("CodecJSound: null stream/format");
			return null;
		}

		byte[] buff = null;
		try {
			buff = new byte[SoundSystemConfig.getStreamingBufferSize()];
			int read = 0, tot = 0;
			boolean shouldBreak = false;
			while(!endOfStream() && tot < buff.length) {
				sync.acquire();
				try {
					read = audioIn.read(buff, tot, buff.length - read);
					if(read <= 0) {
						eos = true;
						shouldBreak = true;
					}
					tot+=read;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					sync.release();
				}
				
				if(shouldBreak)
					break;
			}

			if(tot <= 0)
				return null;
			else if(tot < buff.length)
				buff = trimArray(buff, tot);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return new SoundBuffer(buff, audioIn.getFormat());
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#readAll()
	 */
	@Override
	public SoundBuffer readAll() {
		if(audioIn == null || audioIn.getFormat() == null) {
			System.err.println("CodecJSound: null stream/format");
			return null;
		}
		
		if(endOfStream())
			return null;
		
		SoundBuffer buffer = null;
		try {
			AudioFormat format = audioIn.getFormat();
			sync.acquire();
			byte[] fullBuffer = null;
			int fileSize = format.getChannels()
					* (int) audioIn.getFrameLength()
					* format.getSampleSizeInBits() / 8;
			if(fileSize > 0) {
				fullBuffer = new byte[fileSize];
				int read = 0, tot = 0;
				while((read=audioIn.read(fullBuffer, tot, fullBuffer.length - tot)) !=-1 && tot < SoundSystemConfig.getMaxFileSize())
					tot+=read;
			} else {
				byte[] smallBuffer = new byte[SoundSystemConfig.getFileChunkSize()];
				int read, tot = 0;
				while((read=audioIn.read(smallBuffer)) != -1) {
					tot+=read;
					if(fullBuffer == null)
						fullBuffer = new byte[tot];
					if(fullBuffer.length < tot) {
						fullBuffer = Arrays.copyOf(fullBuffer, tot);
						System.arraycopy(smallBuffer, 0, fullBuffer, fullBuffer.length - read - 1, read);
					}
				}
			}
			buffer = new SoundBuffer(fullBuffer, format);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			sync.release();
		}

		return buffer;
	}

	/* (non-Javadoc)
	 * @see paulscode.sound.ICodec#reverseByteOrder(boolean)
	 */
	@Override
	public void reverseByteOrder(boolean arg0) {
		// shouldn't be an issue for AU files
	}

	private byte[] trimArray(byte[] bytes, int maxLen) {
		if(maxLen <= 0 || bytes == null || bytes.length <= 0)
			return null;
		byte[] newArr = new byte[maxLen];
		System.arraycopy(bytes, 0, newArr, 0, maxLen);
		return newArr;
	}

}
