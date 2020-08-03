package com.hiveworkshop.wc3.mdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdl.MDLReader;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class FaceEffectsChunk {
	public FaceEffect[] faceEffects = new FaceEffect[0];
	public static final String key = "FAFX";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "FAFX");
		int chunkSize = in.readInt();
		final List<FaceEffect> faceEffectList = new ArrayList<>();
		while (chunkSize >= 340) {
			final FaceEffect faceEffect = new FaceEffect();
			faceEffect.faceEffectTarget = in.readCharsAsString(80);
			faceEffect.faceEffect = in.readCharsAsString(260);
			faceEffectList.add(faceEffect);
			chunkSize -= 340;
		}
		faceEffects = faceEffectList.toArray(new FaceEffect[faceEffectList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("FAFX", 4);
		out.writeInt(getSize() - 8);// ChunkSize

		for (final FaceEffect faceEffect : faceEffects) {
			out.writeNByteString(faceEffect.faceEffectTarget, 80);
			out.writeNByteString(faceEffect.faceEffect, 260);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (final FaceEffect faceEffect : faceEffects) {
			a += 80;
			a += 260;
		}
		return a;
	}

	public static class FaceEffect {
		public String faceEffectTarget;
		public String faceEffect;

		public FaceEffect() {
		}

		public static FaceEffect read(final BufferedReader mdl) {
			String line = MDLReader.nextLine(mdl);
			if (line.contains("FaceFX")) {
				final FaceEffect e = new FaceEffect();
				e.faceEffectTarget = MDLReader.readName(line);
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
				while ((!line.contains("}") || line.contains("},") || line.contains("\t}"))
						&& !line.equals("COMPLETED PARSING")) {
					if (line.contains("Path")) {
						e.faceEffect = MDLReader.readName(line);
					}
					MDLReader.mark(mdl);
					line = MDLReader.nextLine(mdl);
				}
				return e;
			} else {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						"Unable to parse FaceEffect: Missing or unrecognized open statement.");
			}
			return null;
		}

	}

}
