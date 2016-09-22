package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class GeosetAnimationChunk {
	public GeosetAnimation[] geosetAnimation = new GeosetAnimation[0];

	public static final String key = "GEOA";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "GEOA");
		int chunkSize = in.readInt();
		List<GeosetAnimation> geosetAnimationList = new ArrayList();
		int geosetAnimationCounter = chunkSize;
		while (geosetAnimationCounter > 0) {
			GeosetAnimation tempgeosetAnimation = new GeosetAnimation();
			geosetAnimationList.add(tempgeosetAnimation);
			tempgeosetAnimation.load(in);
			geosetAnimationCounter -= tempgeosetAnimation.getSize();
		}
		geosetAnimation = geosetAnimationList
				.toArray(new GeosetAnimation[geosetAnimationList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfGeosetAnimations = geosetAnimation.length;
		out.writeNByteString("GEOA", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < geosetAnimation.length; i++) {
			geosetAnimation[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < geosetAnimation.length; i++) {
			a += geosetAnimation[i].getSize();
		}

		return a;
	}

	public class GeosetAnimation {
		public float alpha;
		public int flags;
		public float[] color = new float[3];
		public int geosetId;
		public GeosetAlpha geosetAlpha;
		public GeosetColor geosetColor;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			alpha = in.readFloat();
			flags = in.readInt();
			color = MdxUtils.loadFloatArray(in, 3);
			geosetId = in.readInt();
			for (int i = 0; i < 2; i++) {
				if (MdxUtils.checkOptionalId(in, geosetAlpha.key)) {
					geosetAlpha = new GeosetAlpha();
					geosetAlpha.load(in);
				} else if (MdxUtils.checkOptionalId(in, geosetColor.key)) {
					geosetColor = new GeosetColor();
					geosetColor.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			out.writeFloat(alpha);
			out.writeInt(flags);
			if (color.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array color needs either the length 3 or a multiple of this number. (got "
								+ color.length + ")");
			}
			MdxUtils.saveFloatArray(out, color);
			out.writeInt(geosetId);
			if (geosetAlpha != null) {
				geosetAlpha.save(out);
			}
			if (geosetColor != null) {
				geosetColor.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			a += 4;
			if (geosetAlpha != null) {
				a += geosetAlpha.getSize();
			}
			if (geosetColor != null) {
				a += geosetColor.getSize();
			}

			return a;
		}
	}
}
