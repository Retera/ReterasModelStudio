package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class TextureAnimationChunk {
	public TextureAnimation[] textureAnimation = new TextureAnimation[0];

	public static final String key = "TXAN";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "TXAN");
		int chunkSize = in.readInt();
		List<TextureAnimation> textureAnimationList = new ArrayList();
		int textureAnimationCounter = chunkSize;
		while (textureAnimationCounter > 0) {
			TextureAnimation temptextureAnimation = new TextureAnimation();
			textureAnimationList.add(temptextureAnimation);
			temptextureAnimation.load(in);
			textureAnimationCounter -= temptextureAnimation.getSize();
		}
		textureAnimation = textureAnimationList
				.toArray(new TextureAnimation[textureAnimationList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTextureAnimations = textureAnimation.length;
		out.writeNByteString("TXAN", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < textureAnimation.length; i++) {
			textureAnimation[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < textureAnimation.length; i++) {
			a += textureAnimation[i].getSize();
		}

		return a;
	}

	public class TextureAnimation {
		public TextureTranslation textureTranslation;
		public TextureRotation textureRotation;
		public TextureScaling textureScaling;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			for (int i = 0; i < 3; i++) {
				if (MdxUtils.checkOptionalId(in, textureTranslation.key)) {
					textureTranslation = new TextureTranslation();
					textureTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, textureRotation.key)) {
					textureRotation = new TextureRotation();
					textureRotation.load(in);
				} else if (MdxUtils.checkOptionalId(in, textureScaling.key)) {
					textureScaling = new TextureScaling();
					textureScaling.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			if (textureTranslation != null) {
				textureTranslation.save(out);
			}
			if (textureRotation != null) {
				textureRotation.save(out);
			}
			if (textureScaling != null) {
				textureScaling.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			if (textureTranslation != null) {
				a += textureTranslation.getSize();
			}
			if (textureRotation != null) {
				a += textureRotation.getSize();
			}
			if (textureScaling != null) {
				a += textureScaling.getSize();
			}

			return a;
		}
	}
}
