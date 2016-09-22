package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class Node {
	public String name = "";
	public int objectId;
	public int parentId;
	public int flags;
	public GeosetTranslation geosetTranslation;
	public GeosetRotation geosetRotation;
	public GeosetScaling geosetScaling;

	public void load(BlizzardDataInputStream in) throws IOException {
		int inclusiveSize = in.readInt();
		name = in.readCharsAsString(80);
		objectId = in.readInt();
		parentId = in.readInt();
		flags = in.readInt();
		for (int i = 0; i < 3; i++) {
			if (MdxUtils.checkOptionalId(in, geosetTranslation.key)) {
				geosetTranslation = new GeosetTranslation();
				geosetTranslation.load(in);
			} else if (MdxUtils.checkOptionalId(in, geosetRotation.key)) {
				geosetRotation = new GeosetRotation();
				geosetRotation.load(in);
			} else if (MdxUtils.checkOptionalId(in, geosetScaling.key)) {
				geosetScaling = new GeosetScaling();
				geosetScaling.load(in);
			}

		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		out.writeInt(getSize());// InclusiveSize
		out.writeNByteString(name, 80);
		out.writeInt(objectId);
		out.writeInt(parentId);
		out.writeInt(flags);
		if (geosetTranslation != null) {
			geosetTranslation.save(out);
		}
		if (geosetRotation != null) {
			geosetRotation.save(out);
		}
		if (geosetScaling != null) {
			geosetScaling.save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 80;
		a += 4;
		a += 4;
		a += 4;
		if (geosetTranslation != null) {
			a += geosetTranslation.getSize();
		}
		if (geosetRotation != null) {
			a += geosetRotation.getSize();
		}
		if (geosetScaling != null) {
			a += geosetScaling.getSize();
		}

		return a;
	}
}
