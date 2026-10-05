package io.stratixops.engine.render.platform;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;

import io.stratixops.engine.spi.AudioDevice;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/**
 * OpenAL Soft implementation of {@link AudioDevice}. Sound effects are decoded
 * fully into AL buffers; music is decoded from OGG into a single looping buffer
 * (streaming is a post-MVP refinement). Playback is voice-capped to
 * {@value #VOICE_CAP} overlapping SFX.
 *
 * <p>If no device can be opened the whole implementation degrades to no-ops and
 * logs one warning, so gameplay never crashes (AC6.4).
 */
public final class OpenAlAudioDevice implements AudioDevice, AutoCloseable {

    private static final Logger LOG = Logger.getLogger(OpenAlAudioDevice.class.getName());
    private static final int VOICE_CAP = 16;

    private final Set<Integer> buffers = new LinkedHashSet<>();
    private final int[] sources;
    private final int musicSource;
    private final boolean available;
    private long device;
    private long context;
    private int nextSource;

    /**
     * Opens the default device and creates the mixer state.
     */
    public OpenAlAudioDevice() {
        long openedDevice = ALC10.alcOpenDevice((ByteBuffer) null);
        if (openedDevice == 0L) {
            this.device = 0L;
            this.context = 0L;
            this.sources = new int[0];
            this.musicSource = 0;
            this.available = false;
            LOG.warning("No audio device; running muted");
            return;
        }
        this.device = openedDevice;
        this.context = ALC10.alcCreateContext(openedDevice, (IntBuffer) null);
        if (context == 0L || !ALC10.alcMakeContextCurrent(context)) {
            closeDevice();
            this.sources = new int[0];
            this.musicSource = 0;
            this.available = false;
            LOG.warning("No audio device; running muted");
            return;
        }
        ALCCapabilities alcCapabilities = ALC.createCapabilities(openedDevice);
        AL.createCapabilities(alcCapabilities);

        this.sources = new int[VOICE_CAP];
        alGenSources(sources);
        for (int source : sources) {
            alSourcef(source, AL_GAIN, 1f);
        }
        this.musicSource = alGenSources();
        alSourcef(musicSource, AL_GAIN, 1f);
        this.available = true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setMasterGain(float gain) {
        if (available) {
            alListenerf(AL_GAIN, clamp(gain));
        }
    }

    @Override
    public int loadClip(String path) {
        if (!available) {
            return 0;
        }
        byte[] bytes = readBytes(path);
        if (bytes == null) {
            LOG.warning("Missing sound effect '" + path + "'");
            return 0;
        }
        try (AudioInputStream source = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
            AudioFormat base = source.getFormat();
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(), 16, base.getChannels(), base.getChannels() * 2,
                base.getSampleRate(), false);
            byte[] data;
            if (base.matches(pcm)) {
                data = source.readAllBytes();
            } else {
                try (AudioInputStream converted = AudioSystem.getAudioInputStream(pcm, source)) {
                    data = converted.readAllBytes();
                }
            }
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data).flip();
            int id = alGenBuffers();
            int format = base.getChannels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            alBufferData(id, format, buffer, (int) base.getSampleRate());
            buffers.add(id);
            return id;
        } catch (Exception e) {
            LOG.warning("Failed to decode sound effect '" + path + "': " + e.getMessage());
            return 0;
        }
    }

    @Override
    public void playClip(int clip, float volume) {
        if (!available || clip == 0) {
            return;
        }
        int source = acquireSource();
        alSourceStop(source);
        alSourcei(source, AL_BUFFER, clip);
        alSourcei(source, AL_LOOPING, AL_FALSE);
        alSourcef(source, AL_GAIN, clamp(volume));
        alSourcePlay(source);
    }

    @Override
    public void stopClip(int clip) {
        if (!available || clip == 0) {
            return;
        }
        for (int source : sources) {
            if (alGetSourcei(source, AL_BUFFER) == clip) {
                alSourceStop(source);
            }
        }
    }

    @Override
    public int openMusic(String path) {
        if (!available) {
            return 0;
        }
        byte[] bytes = readBytes(path);
        if (bytes == null) {
            LOG.warning("Missing music '" + path + "'");
            return 0;
        }
        ByteBuffer input = MemoryUtil.memAlloc(bytes.length);
        try {
            input.put(bytes).flip();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer error = stack.mallocInt(1);
                long decoder = STBVorbis.stb_vorbis_open_memory(input, error, null);
                if (decoder == 0L) {
                    LOG.warning("Failed to decode music '" + path + "'");
                    return 0;
                }
                STBVorbisInfo info = STBVorbisInfo.malloc();
                try {
                    STBVorbis.stb_vorbis_get_info(decoder, info);
                    int channels = info.channels();
                    int sampleRate = info.sample_rate();
                    int capacity = STBVorbis.stb_vorbis_stream_length_in_samples(decoder) * channels;
                    ShortBuffer pcm = MemoryUtil.memAllocShort(Math.max(capacity, 1));
                    int frames = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
                    pcm.limit(frames * channels);
                    int id = alGenBuffers();
                    int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
                    alBufferData(id, format, pcm, sampleRate);
                    MemoryUtil.memFree(pcm);
                    buffers.add(id);
                    return id;
                } catch (Exception e) {
                    LOG.warning("Failed to decode music '" + path + "': " + e.getMessage());
                    return 0;
                } finally {
                    info.free();
                    STBVorbis.stb_vorbis_close(decoder);
                }
            }
        } finally {
            MemoryUtil.memFree(input);
        }
    }

    @Override
    public void playMusic(int music, boolean loop, float volume) {
        if (!available || music == 0) {
            return;
        }
        alSourceStop(musicSource);
        alSourcei(musicSource, AL_BUFFER, music);
        alSourcei(musicSource, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcef(musicSource, AL_GAIN, clamp(volume));
        alSourcePlay(musicSource);
    }

    @Override
    public void stopMusic() {
        if (available) {
            alSourceStop(musicSource);
        }
    }

    @Override
    public void dispose() {
        if (!available) {
            return;
        }
        for (int source : sources) {
            alSourceStop(source);
        }
        alSourceStop(musicSource);
        alDeleteSources(sources);
        alDeleteSources(musicSource);
        if (!buffers.isEmpty()) {
            int[] ids = new int[buffers.size()];
            int i = 0;
            for (int id : buffers) {
                ids[i++] = id;
            }
            alDeleteBuffers(ids);
            buffers.clear();
        }
        closeDevice();
    }

    @Override
    public void close() {
        dispose();
    }

    private int acquireSource() {
        for (int source : sources) {
            if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
                return source;
            }
        }
        int source = sources[nextSource % sources.length];
        nextSource = (nextSource + 1) % sources.length;
        return source;
    }

    private void closeDevice() {
        if (context != 0L) {
            ALC10.alcMakeContextCurrent(0L);
            ALC10.alcDestroyContext(context);
            context = 0L;
        }
        if (device != 0L) {
            ALC10.alcCloseDevice(device);
            device = 0L;
        }
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static byte[] readBytes(String path) {
        String stripped = path.startsWith("/") ? path.substring(1) : path;
        InputStream stream = OpenAlAudioDevice.class.getClassLoader().getResourceAsStream(stripped);
        if (stream != null) {
            try (InputStream in = stream) {
                return in.readAllBytes();
            } catch (IOException e) {
                return null;
            }
        }
        Path file = Paths.get(path);
        if (Files.isReadable(file)) {
            try {
                return Files.readAllBytes(file);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}
