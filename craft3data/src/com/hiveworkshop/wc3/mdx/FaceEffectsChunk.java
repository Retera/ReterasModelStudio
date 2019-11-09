package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class FaceEffectsChunk {
	public static final String key = "FAFX";

	public String faceEffectTarget;
	public String faceEffect;

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "FAFX");
		final int chunkSize = in.readInt();
		if (chunkSize != 340) {
			throw new IllegalStateException("Unsupported FAFX chunk");
		}
		faceEffectTarget = in.readCharsAsString(80);
		faceEffect = in.readCharsAsString(260);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("FAFX", 4);
		out.writeInt(getSize() - 8);// ChunkSize

		out.writeNByteString(faceEffectTarget, 80);
		out.writeNByteString(faceEffect, 260);

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		a += 80;
		a += 260;

		return a;
	}

}
