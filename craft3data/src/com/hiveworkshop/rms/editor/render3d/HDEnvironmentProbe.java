package com.hiveworkshop.rms.editor.render3d;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL32;

import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

/**
 * The image-based lighting probe of the Reforged HD shader: one of the game's
 * own baked {@code Environment/EnvironmentMap/*_IBL.dds} cube-map arrays (cube
 * 0 is the irradiance convolution, cube 1 the roughness-prefiltered radiance)
 * plus the split-sum BRDF lookup table the shader reads next to it.
 * <p>
 * The decoded CPU copy is cached per probe across GL contexts; each shader
 * pipeline instance uploads its own GL textures from it. A probe that cannot be
 * read (no game data mounted, an unexpected format) resolves to the shader's
 * "no probe" state, which is what the game itself renders without one: no
 * environment term at all, only the analytic ambient. Nothing here raises a
 * dialog.
 */
public final class HDEnvironmentProbe {
	/** Display names for the preference combo box, indexed like {@link #PROBE_PATHS}. */
	public static final String[] PROBE_NAMES = { "Portrait (game default)", "Lordaeron Summer, day",
			"Lordaeron Summer, night", "Dungeon, night", "Northrend, sunset" };
	public static final String[] PROBE_PATHS = { "Environment\\EnvironmentMap\\Portraits\\PortraitDefault_IBL.dds",
			"Environment\\EnvironmentMap\\LordaeronSummer\\Day_IBL.dds",
			"Environment\\EnvironmentMap\\LordaeronSummer\\Night_IBL.dds",
			"Environment\\EnvironmentMap\\Dungeon\\Night_IBL.dds",
			"Environment\\EnvironmentMap\\Northrend\\Sunset_IBL.dds" };
	public static final int DEFAULT_PROBE = 0;

	private static final int LUT_SIZE = 128;
	private static final int LUT_SAMPLES = 128;

	private static final int DDS_MAGIC = 0x20534444; // "DDS "
	private static final int FOURCC_DX10 = 0x30315844; // "DX10"
	private static final int FOURCC_DXT1 = 0x31545844; // "DXT1"
	private static final int DXGI_R8G8B8A8_UNORM = 28;
	private static final int DXGI_R8G8B8A8_UNORM_SRGB = 29;
	private static final int DXGI_BC1_UNORM = 71;
	private static final int DXGI_BC1_UNORM_SRGB = 72;
	private static final int DDSCAPS2_CUBEMAP = 0x200;
	private static final int DDS_RESOURCE_MISC_TEXTURECUBE = 0x4;

	/** A decoded probe: RGBA8 faces for every cube, face and mip. */
	public static final class ProbeData {
		public final int faceSize;
		public final int mipCount;
		/** Indexed [cube][face][mip]; RGBA8, row-major, top-down as stored. */
		public final byte[][][][] faces;

		ProbeData(final int faceSize, final int mipCount, final byte[][][][] faces) {
			this.faceSize = faceSize;
			this.mipCount = mipCount;
			this.faces = faces;
		}

		/**
		 * The mip count Warcraft III 3.0 hands the HD pixel shader for this probe:
		 * the full chain length for the face size, whatever the file stores.
		 */
		public float engineMipEnd() {
			int count = 0;
			for (int s = faceSize; s > 0; s >>= 1) {
				count++;
			}
			return count;
		}
	}

	private static final Map<Integer, ProbeData> decodedProbes = new HashMap<>();
	private static final Map<Integer, Boolean> failedProbes = new HashMap<>();
	private static int cacheGeneration = 0;
	private static int selectedProbe = DEFAULT_PROBE;
	private static byte[] splitSumLut;

	private HDEnvironmentProbe() {
	}

	public static void setSelectedProbe(final int index) {
		selectedProbe = Math.max(0, Math.min(PROBE_PATHS.length - 1, index));
	}

	public static int getSelectedProbe() {
		return selectedProbe;
	}

	/** Forget the decoded probes, e.g. after the data sources changed. */
	public static synchronized void dropCache() {
		decodedProbes.clear();
		failedProbes.clear();
		cacheGeneration++;
	}

	public static synchronized int getCacheGeneration() {
		return cacheGeneration;
	}

	/** The decoded probe, or null when it is not available; failures are remembered until {@link #dropCache()}. */
	public static synchronized ProbeData getProbe(final int index) {
		final int clamped = Math.max(0, Math.min(PROBE_PATHS.length - 1, index));
		final ProbeData cached = decodedProbes.get(clamped);
		if (cached != null) {
			return cached;
		}
		if (failedProbes.containsKey(clamped)) {
			return null;
		}
		ProbeData data = null;
		try {
			final DataSource dataSource = MpqCodebase.get();
			final String path = PROBE_PATHS[clamped];
			if ((dataSource != null) && dataSource.has(path)) {
				try (InputStream stream = dataSource.getResourceAsStream(path)) {
					if (stream != null) {
						data = decode(readAll(stream));
					}
				}
			}
			if (data == null) {
				System.err.println("HD environment probe not available, the HD shader renders without one: " + path);
			}
		} catch (final Throwable e) {
			System.err.println("HD environment probe failed to load, the HD shader renders without one: " + e);
			data = null;
		}
		if (data == null) {
			failedProbes.put(clamped, Boolean.TRUE);
		} else {
			decodedProbes.put(clamped, data);
		}
		return data;
	}

	private static byte[] readAll(final InputStream stream) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
		final byte[] chunk = new byte[1 << 16];
		int read;
		while ((read = stream.read(chunk)) > 0) {
			out.write(chunk, 0, read);
		}
		return out.toByteArray();
	}

	/**
	 * Decodes a DDS cube-map array with a DX10 header (BC1 or RGBA8) into RGBA8
	 * faces, in file face order: 3.0 samples the probe in a frame whose +Z is the
	 * main light and the shipped files are baked that way, so no face remap.
	 * Returns null for anything else.
	 */
	static ProbeData decode(final byte[] bytes) {
		if ((bytes == null) || (bytes.length < 128)) {
			return null;
		}
		final ByteBuffer header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		if (header.getInt(0) != DDS_MAGIC) {
			return null;
		}
		final int height = header.getInt(12);
		final int width = header.getInt(16);
		int mipCount = Math.max(1, header.getInt(28));
		final int pfFlags = header.getInt(80);
		final int fourCC = header.getInt(84);
		final int caps2 = header.getInt(112);
		int dataOffset = 128;
		int cubeCount = 1;
		boolean bc1;
		if (fourCC == FOURCC_DX10) {
			if (bytes.length < 148) {
				return null;
			}
			final int dxgiFormat = header.getInt(128);
			final int miscFlag = header.getInt(136);
			final int arraySize = Math.max(1, header.getInt(140));
			if ((miscFlag & DDS_RESOURCE_MISC_TEXTURECUBE) == 0) {
				return null;
			}
			if ((dxgiFormat == DXGI_BC1_UNORM) || (dxgiFormat == DXGI_BC1_UNORM_SRGB)) {
				bc1 = true;
			} else if ((dxgiFormat == DXGI_R8G8B8A8_UNORM) || (dxgiFormat == DXGI_R8G8B8A8_UNORM_SRGB)) {
				bc1 = false;
			} else {
				return null;
			}
			cubeCount = arraySize;
			dataOffset = 148;
		} else if (((pfFlags & 0x4) != 0) && (fourCC == FOURCC_DXT1) && ((caps2 & DDSCAPS2_CUBEMAP) != 0)) {
			bc1 = true;
		} else {
			return null;
		}
		if ((width != height) || (width <= 0) || (cubeCount < 1)) {
			return null;
		}
		// the shader needs both cubes; a single-cube file serves as both
		final byte[][][][] faces = new byte[2][6][mipCount][];
		int offset = dataOffset;
		for (int cube = 0; cube < cubeCount; cube++) {
			for (int face = 0; face < 6; face++) {
				for (int mip = 0; mip < mipCount; mip++) {
					final int w = Math.max(1, width >> mip);
					final int h = Math.max(1, height >> mip);
					final byte[] rgba = new byte[w * h * 4];
					if (bc1) {
						final int blocksW = (w + 3) / 4;
						final int blocksH = (h + 3) / 4;
						final int size = blocksW * blocksH * 8;
						if ((offset + size) > bytes.length) {
							return null;
						}
						decodeBC1(bytes, offset, w, h, rgba);
						offset += size;
					} else {
						final int size = w * h * 4;
						if ((offset + size) > bytes.length) {
							return null;
						}
						System.arraycopy(bytes, offset, rgba, 0, size);
						offset += size;
					}
					if (cube < 2) {
						faces[cube][face][mip] = rgba;
					}
				}
			}
		}
		if (cubeCount == 1) {
			faces[1] = faces[0];
		}
		return new ProbeData(width, mipCount, faces);
	}

	/** Plain BC1 (DXT1) block decoding, opaque and 1-bit-alpha blocks alike. */
	static void decodeBC1(final byte[] src, final int start, final int width, final int height, final byte[] dst) {
		final int blocksW = (width + 3) / 4;
		final int blocksH = (height + 3) / 4;
		final int[] r = new int[4];
		final int[] g = new int[4];
		final int[] b = new int[4];
		final int[] a = new int[4];
		int offset = start;
		for (int by = 0; by < blocksH; by++) {
			for (int bx = 0; bx < blocksW; bx++) {
				final int c0 = (src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8);
				final int c1 = (src[offset + 2] & 0xFF) | ((src[offset + 3] & 0xFF) << 8);
				final int bits = (src[offset + 4] & 0xFF) | ((src[offset + 5] & 0xFF) << 8)
						| ((src[offset + 6] & 0xFF) << 16) | ((src[offset + 7] & 0xFF) << 24);
				offset += 8;
				r[0] = expand5(c0 >> 11);
				g[0] = expand6((c0 >> 5) & 0x3F);
				b[0] = expand5(c0 & 0x1F);
				r[1] = expand5(c1 >> 11);
				g[1] = expand6((c1 >> 5) & 0x3F);
				b[1] = expand5(c1 & 0x1F);
				a[0] = a[1] = a[2] = 255;
				if (c0 > c1) {
					r[2] = ((2 * r[0]) + r[1]) / 3;
					g[2] = ((2 * g[0]) + g[1]) / 3;
					b[2] = ((2 * b[0]) + b[1]) / 3;
					r[3] = (r[0] + (2 * r[1])) / 3;
					g[3] = (g[0] + (2 * g[1])) / 3;
					b[3] = (b[0] + (2 * b[1])) / 3;
					a[3] = 255;
				} else {
					r[2] = (r[0] + r[1]) / 2;
					g[2] = (g[0] + g[1]) / 2;
					b[2] = (b[0] + b[1]) / 2;
					r[3] = g[3] = b[3] = 0;
					a[3] = 0;
				}
				for (int py = 0; py < 4; py++) {
					final int y = (by * 4) + py;
					if (y >= height) {
						break;
					}
					for (int px = 0; px < 4; px++) {
						final int x = (bx * 4) + px;
						if (x >= width) {
							break;
						}
						final int index = (bits >> (((py * 4) + px) * 2)) & 0x3;
						final int o = ((y * width) + x) * 4;
						dst[o] = (byte) r[index];
						dst[o + 1] = (byte) g[index];
						dst[o + 2] = (byte) b[index];
						dst[o + 3] = (byte) a[index];
					}
				}
			}
		}
	}

	private static int expand5(final int v) {
		return ((v & 0x1F) * 255 + 15) / 31;
	}

	private static int expand6(final int v) {
		return ((v & 0x3F) * 255 + 31) / 63;
	}

	/**
	 * The split-sum environment BRDF table, the same construction WhiteoutFlakes
	 * feeds the shader (Hammersley GGX importance sampling, Smith G with
	 * k = a^2 / 2): red is the Fresnel scale, green the bias, u = N.V and v =
	 * roughness. RGBA8, {@link #LUT_SIZE} square.
	 */
	public static synchronized byte[] getSplitSumLut() {
		if (splitSumLut == null) {
			splitSumLut = generateSplitSumLut(LUT_SIZE, LUT_SAMPLES);
		}
		return splitSumLut;
	}

	public static int getSplitSumLutSize() {
		return LUT_SIZE;
	}

	static byte[] generateSplitSumLut(final int size, final int sampleCount) {
		final byte[] out = new byte[size * size * 4];
		final float[] h = new float[3];
		for (int iy = 0; iy < size; iy++) {
			final float roughness = ((((iy + 0.5f) / size) * 255f) + 1f) / 256f;
			for (int ix = 0; ix < size; ix++) {
				final float noV = (ix + 0.5f) / size;
				final float vx = (float) Math.sqrt(Math.max(0f, 1f - (noV * noV)));
				final float vz = noV;
				float termA = 0f;
				float termB = 0f;
				for (int i = 0; i < sampleCount; i++) {
					final float xiX = (float) i / sampleCount;
					final float xiY = radicalInverseVdC(i);
					importanceSampleGGX(xiX, xiY, roughness, h);
					final float voH = (vx * h[0]) + (vz * h[2]);
					float lx = (2f * voH * h[0]) - vx;
					float ly = 2f * voH * h[1];
					float lz = (2f * voH * h[2]) - vz;
					final float lLen = (float) Math.sqrt((lx * lx) + (ly * ly) + (lz * lz));
					if (lLen > 1e-6f) {
						lx /= lLen;
						ly /= lLen;
						lz /= lLen;
					}
					final float noL = Math.max(0f, lz);
					final float noH = Math.max(0f, h[2]);
					final float voHc = Math.max(0f, voH);
					if (noL > 0f) {
						final float k = (roughness * roughness) * 0.5f;
						final float gV = noV / ((noV * (1f - k)) + k);
						final float gL = noL / ((noL * (1f - k)) + k);
						float gVis = ((gV * gL) * voHc) / (noH * noV);
						gVis = Math.max(0f, Math.min(1f, gVis));
						final float fc = (float) Math.pow(1f - voHc, 5.0);
						termA += (1f - fc) * gVis;
						termB += fc * gVis;
					}
				}
				termA /= sampleCount;
				termB /= sampleCount;
				final int idx = ((iy * size) + ix) * 4;
				out[idx] = pack8(termA);
				out[idx + 1] = pack8(termB);
				out[idx + 2] = 0;
				out[idx + 3] = 0;
			}
		}
		return out;
	}

	private static byte pack8(final float v) {
		return (byte) (int) (Math.max(0f, Math.min(1f, v)) * 255f);
	}

	private static float radicalInverseVdC(int bits) {
		bits = (bits << 16) | (bits >>> 16);
		bits = ((bits & 0x55555555) << 1) | ((bits & 0xAAAAAAAA) >>> 1);
		bits = ((bits & 0x33333333) << 2) | ((bits & 0xCCCCCCCC) >>> 2);
		bits = ((bits & 0x0F0F0F0F) << 4) | ((bits & 0xF0F0F0F0) >>> 4);
		bits = ((bits & 0x00FF00FF) << 8) | ((bits & 0xFF00FF00) >>> 8);
		return (float) ((bits & 0xFFFFFFFFL) * 2.3283064365386963e-10);
	}

	/** GGX half vector for N = +Z, as in the reference (its tangent frame degenerates the same way). */
	private static void importanceSampleGGX(final float xiX, final float xiY, final float roughness,
			final float[] out) {
		final float a = roughness * roughness;
		final float phi = (float) (2.0 * Math.PI * xiX);
		final float cosTh = (float) Math.sqrt((1f - xiY) / (1f + (((a * a) - 1f) * xiY)));
		final float sinTh = (float) Math.sqrt(Math.max(0f, 1f - (cosTh * cosTh)));
		final float htx = (float) Math.cos(phi) * sinTh;
		final float hty = (float) Math.sin(phi) * sinTh;
		final float htz = cosTh;
		// N = (0, 0, 1): up = (1, 0, 0), T = up x N = (0, 1, 0), B = N x T = (-1, 0, 0)
		float sx = -hty;
		float sy = htx;
		float sz = htz;
		final float sLen = (float) Math.sqrt((sx * sx) + (sy * sy) + (sz * sz));
		if (sLen > 1e-6f) {
			sx /= sLen;
			sy /= sLen;
			sz /= sLen;
		}
		out[0] = sx;
		out[1] = sy;
		out[2] = sz;
	}

	/** The GL side of a probe for one context: two cube maps and the LUT. */
	public static final class GLProbe {
		public int irradianceTexture = -1;
		public int radianceTexture = -1;
		public int lutTexture = -1;
		public float envMipEnd = 0f;
		private int loadedProbe = -1;
		private int loadedGeneration = -1;

		/** Uploads (or re-uploads after a change) the selected probe; cheap when nothing changed. */
		public void ensureLoaded() {
			final int probe = getSelectedProbe();
			final int generation = getCacheGeneration();
			if ((probe == loadedProbe) && (generation == loadedGeneration) && (lutTexture != -1)) {
				return;
			}
			loadedProbe = probe;
			loadedGeneration = generation;
			if (lutTexture == -1) {
				lutTexture = uploadLut();
			}
			final ProbeData data = getProbe(probe);
			irradianceTexture = uploadCube(irradianceTexture, data, 0);
			radianceTexture = uploadCube(radianceTexture, data, 1);
			envMipEnd = data == null ? 0f : data.engineMipEnd();
		}

		public void delete() {
			if (irradianceTexture != -1) {
				GL11.glDeleteTextures(irradianceTexture);
				irradianceTexture = -1;
			}
			if (radianceTexture != -1) {
				GL11.glDeleteTextures(radianceTexture);
				radianceTexture = -1;
			}
			if (lutTexture != -1) {
				GL11.glDeleteTextures(lutTexture);
				lutTexture = -1;
			}
			loadedProbe = -1;
		}

		private static int uploadLut() {
			final byte[] lut = getSplitSumLut();
			final int size = getSplitSumLutSize();
			final ByteBuffer buffer = ByteBuffer.allocateDirect(lut.length).order(ByteOrder.nativeOrder());
			buffer.put(lut).flip();
			final int id = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
			// the game binds the table through a linear, wrapping sampler
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, size, size, 0, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, buffer);
			return id;
		}

		/**
		 * Uploads one cube of the probe as an sRGB cube map with its full mip chain,
		 * the way the game creates its IBL views; a missing probe becomes a black
		 * 1x1 cube so the shader's "no probe" branch has something bound.
		 */
		private static int uploadCube(final int existing, final ProbeData data, final int cube) {
			final int id = existing == -1 ? GL11.glGenTextures() : existing;
			GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, id);
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
			if (data == null) {
				final ByteBuffer black = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_MAX_LEVEL, 0);
				for (int face = 0; face < 6; face++) {
					black.clear();
					GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL21.GL_SRGB8_ALPHA8, 1, 1, 0,
							GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, black);
				}
				return id;
			}
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER,
					GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_MAX_LEVEL, data.mipCount - 1);
			ByteBuffer buffer = null;
			for (int face = 0; face < 6; face++) {
				for (int mip = 0; mip < data.mipCount; mip++) {
					final byte[] rgba = data.faces[cube][face][mip];
					final int w = Math.max(1, data.faceSize >> mip);
					if ((buffer == null) || (buffer.capacity() < rgba.length)) {
						buffer = ByteBuffer.allocateDirect(rgba.length).order(ByteOrder.nativeOrder());
					}
					buffer.clear();
					buffer.put(rgba).flip();
					GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, mip, GL21.GL_SRGB8_ALPHA8, w, w, 0,
							GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
				}
			}
			return id;
		}
	}
}
