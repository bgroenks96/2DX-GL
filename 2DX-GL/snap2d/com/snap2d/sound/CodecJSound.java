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

package com.snap2d.sound;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;

/**
 * Implementation of ICodec from the PaulsCode Sound System library for all
 * JavaSound supported file formats. This class simply utilizes the JavaSound
 * API's pre-existing codecs.
 * 
 * @author Brian Groenke
 * 
 */
public class CodecJSound implements ICodec {

    private AudioInputStream audioIn;
    private final Semaphore sync = new Semaphore(1, true);
    private volatile boolean init, eos;

    @Override
    public void cleanup() {

        try {
            if (audioIn != null) {
                audioIn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioIn = null;
    }

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

    @Override
    public AudioFormat getAudioFormat() {

        if (audioIn != null) {
            return audioIn.getFormat();
        } else {
            return null;
        }
    }

    @Override
    public boolean initialize(final URL sound) {

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

    @Override
    public SoundBuffer read() {

        if (audioIn == null || audioIn.getFormat() == null) {
            System.err.println("CodecJSound: null stream/format");
            return null;
        }

        byte[] buff = null;
        try {
            buff = new byte[SoundSystemConfig.getStreamingBufferSize()];
            int read = 0, tot = 0;
            boolean shouldBreak = false;
            while (!endOfStream() && tot < buff.length) {
                sync.acquire();
                try {
                    read = audioIn.read(buff, tot, buff.length - read);
                    if (read <= 0) {
                        eos = true;
                        shouldBreak = true;
                    }
                    tot += read;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    sync.release();
                }

                if (shouldBreak) {
                    break;
                }
            }

            if (tot <= 0) {
                return null;
            } else if (tot < buff.length) {
                buff = trimArray(buff, tot);
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return new SoundBuffer(buff, audioIn.getFormat());
    }

    @Override
    public SoundBuffer readAll() {

        if (audioIn == null || audioIn.getFormat() == null) {
            System.err.println("CodecJSound: null stream/format");
            return null;
        }

        if (endOfStream()) {
            return null;
        }

        SoundBuffer buffer = null;
        try {
            AudioFormat format = audioIn.getFormat();
            sync.acquire();
            byte[] fullBuffer = null;
            int fileSize = format.getChannels() * (int) audioIn.getFrameLength() * format.getSampleSizeInBits() / 8;
            if (fileSize > 0) {
                fullBuffer = new byte[fileSize];
                int read = 0, tot = 0;
                while ( (read = audioIn.read(fullBuffer, tot, fullBuffer.length - tot)) != -1
                        && tot < SoundSystemConfig.getMaxFileSize()) {
                    tot += read;
                }
            } else {
                byte[] smallBuffer = new byte[SoundSystemConfig.getFileChunkSize()];
                int read, tot = 0;
                while ( (read = audioIn.read(smallBuffer)) != -1) {
                    tot += read;
                    if (fullBuffer == null) {
                        fullBuffer = new byte[tot];
                    }
                    if (fullBuffer.length < tot) {
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

    @Override
    public void reverseByteOrder(final boolean arg0) {

        // shouldn't be an issue for JavaSound audio streams
    }

    private byte[] trimArray(final byte[] bytes, final int maxLen) {

        if (maxLen <= 0 || bytes == null || bytes.length <= 0) {
            return null;
        }
        byte[] newArr = new byte[maxLen];
        System.arraycopy(bytes, 0, newArr, 0, maxLen);
        return newArr;
    }

}
