package wc3Data.mdx;

import java.io.IOException;
import java.util.*;

public class LightChunk {
	public Light[] light = new Light[0];

	public static final String key = "LITE";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "LITE");
		int chunkSize = in.readInt();
		List<Light> lightList = new ArrayList();
		int lightCounter = chunkSize;
		while (lightCounter > 0) {
			Light templight = new Light();
			lightList.add(templight);
			templight.load(in);
			lightCounter -= templight.getSize();
		}
		light = lightList.toArray(new Light[lightList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfLights = light.length;
		out.writeNByteString("LITE", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < light.length; i++) {
			light[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < light.length; i++) {
			a += light[i].getSize();
		}

		return a;
	}

	public class Light {
		public Node node = new Node();
		public int type;
		public int attenuationStart;
		public int attenuationEnd;
		public float[] color = new float[3];
		public float intensity;
		public float[] ambientColor = new float[3];
		public float ambientIntensity;
		public LightVisibility lightVisibility;
		public LightColor lightColor;
		public LightIntensity lightIntensity;
		public LightAmbientColor lightAmbientColor;
		public LightAmbientIntensity lightAmbientIntensity;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			node = new Node();
			node.load(in);
			type = in.readInt();
			attenuationStart = in.readInt();
			attenuationEnd = in.readInt();
			color = MdxUtils.loadFloatArray(in, 3);
			intensity = in.readFloat();
			ambientColor = MdxUtils.loadFloatArray(in, 3);
			ambientIntensity = in.readFloat();
			for (int i = 0; i < 5; i++) {
				if (MdxUtils.checkOptionalId(in, lightVisibility.key)) {
					lightVisibility = new LightVisibility();
					lightVisibility.load(in);
				} else if (MdxUtils.checkOptionalId(in, lightColor.key)) {
					lightColor = new LightColor();
					lightColor.load(in);
				} else if (MdxUtils.checkOptionalId(in, lightIntensity.key)) {
					lightIntensity = new LightIntensity();
					lightIntensity.load(in);
				} else if (MdxUtils.checkOptionalId(in, lightAmbientColor.key)) {
					lightAmbientColor = new LightAmbientColor();
					lightAmbientColor.load(in);
				} else if (MdxUtils.checkOptionalId(in,
						lightAmbientIntensity.key)) {
					lightAmbientIntensity = new LightAmbientIntensity();
					lightAmbientIntensity.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeInt(type);
			out.writeInt(attenuationStart);
			out.writeInt(attenuationEnd);
			if (color.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array color needs either the length 3 or a multiple of this number. (got "
								+ color.length + ")");
			}
			MdxUtils.saveFloatArray(out, color);
			out.writeFloat(intensity);
			if (ambientColor.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array ambientColor needs either the length 3 or a multiple of this number. (got "
								+ ambientColor.length + ")");
			}
			MdxUtils.saveFloatArray(out, ambientColor);
			out.writeFloat(ambientIntensity);
			if (lightVisibility != null) {
				lightVisibility.save(out);
			}
			if (lightColor != null) {
				lightColor.save(out);
			}
			if (lightIntensity != null) {
				lightIntensity.save(out);
			}
			if (lightAmbientColor != null) {
				lightAmbientColor.save(out);
			}
			if (lightAmbientIntensity != null) {
				lightAmbientIntensity.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += node.getSize();
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			a += 4;
			a += 12;
			a += 4;
			if (lightVisibility != null) {
				a += lightVisibility.getSize();
			}
			if (lightColor != null) {
				a += lightColor.getSize();
			}
			if (lightIntensity != null) {
				a += lightIntensity.getSize();
			}
			if (lightAmbientColor != null) {
				a += lightAmbientColor.getSize();
			}
			if (lightAmbientIntensity != null) {
				a += lightAmbientIntensity.getSize();
			}

			return a;
		}
	}
}
