package com.elfmcys.yesstevemodel.audio;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SeekableAudioStream implements IAudioStreamSupport {

    private static final ByteBuffer EMPTY_BUFFER = BufferUtils.createByteBuffer(0);

    private final ByteBuffer audioData;

    private final IntArrayList seekPoints;

    private final AudioFormat audioFormat;

    private int position;

    private int readLimit;

    private volatile boolean closed;

    public SeekableAudioStream(ByteBuffer byteBuffer, IntArrayList intArrayList, AudioFormat audioFormat) throws UnsupportedAudioFileException {
        if (audioFormat.getChannels() != 1) {
            throw new UnsupportedAudioFileException();
        }
        this.audioData = byteBuffer;
        this.seekPoints = intArrayList;
        this.audioFormat = audioFormat;
    }

    @NotNull
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @NotNull
    public ByteBuffer read(int i) throws IOException {
        if (this.readLimit >= this.seekPoints.size() || this.closed) {
            return EMPTY_BUFFER;
        }
        // PORT-DROP: int i2 = this.seekPoints.getIntOr(this.readLimit, 0);
        // PORT-DROP: ByteBuffer byteBufferSlice = this.audioData.slice(this.position, i2);
        this.readLimit++;
        // PORT-DROP: this.position += i2;
        return null;
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}