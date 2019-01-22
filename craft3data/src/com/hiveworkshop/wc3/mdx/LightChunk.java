package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class LightChunk {
	public Light[] light = new Light[0];

	public static final String key = "LITE";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "LITE");
		final int chunkSize = in.readInt();
		final List<Light> lightList = new ArrayList();
		int lightCounter = chunkSize;
		while (lightCounter > 0) {
			final Light templight = new Light();
			lightList.add(templight);
			templight.load(in);
			lightCounter -= templight.getSize();
		}
		light = lightList.toArray(new Light[lightList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfLights = light.length;
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

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
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
				if (MdxUtils.checkOptionalId(in, LightVisibility.key)) {
					lightVisibility = new LightVisibility();
					lightVisibility.load(in);
				} else if (MdxUtils.checkOptionalId(in, LightColor.key)) {
					lightColor = new LightColor();
					lightColor.load(in);
				} else if (MdxUtils.checkOptionalId(in, LightIntensity.key)) {
					lightIntensity = new LightIntensity();
					lightIntensity.load(in);
				} else if (MdxUtils.checkOptionalId(in, LightAmbientColor.key)) {
					lightAmbientColor = new LightAmbientColor();
					lightAmbientColor.load(in);
				} else if (MdxUtils.checkOptionalId(in, LightAmbientIntensity.key)) {
					lightAmbientIntensity = new LightAmbientIntensity();
					lightAmbientIntensity.load(in);
				}

			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			node.save(out);
			out.writeInt(type);
			out.writeInt(attenuationStart);
			out.writeInt(attenuationEnd);
			if (color.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array color needs either the length 3 or a multiple of this number. (got " + color.length
								+ ")");
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

		public Light() {

		}

		public Light(final com.hiveworkshop.wc3.mdl.Light light) {
			node = new Node(light);
			node.flags |= 0x200;
			// more to do here
			for (final AnimFlag af : light.getAnimFlags()) {
				if (af.getName().equals("Visibility")) {
					lightVisibility = new LightVisibility();
					lightVisibility.globalSequenceId = af.getGlobalSeqId();
					lightVisibility.interpolationType = af.getInterpType();
					lightVisibility.scalingTrack = new LightVisibility.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final LightVisibility.ScalingTrack mdxEntry = lightVisibility.new ScalingTrack();
						lightVisibility.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Color")) {
					lightColor = new LightColor();
					lightColor.globalSequenceId = af.getGlobalSeqId();
					lightColor.interpolationType = af.getInterpType();
					lightColor.scalingTrack = new LightColor.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final LightColor.ScalingTrack mdxEntry = lightColor.new ScalingTrack();
						lightColor.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.color = ((Vertex) mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
						}
					}
				} else if (af.getName().equals("Intensity")) {
					lightIntensity = new LightIntensity();
					lightIntensity.globalSequenceId = af.getGlobalSeqId();
					lightIntensity.interpolationType = af.getInterpType();
					lightIntensity.scalingTrack = new LightIntensity.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final LightIntensity.ScalingTrack mdxEntry = lightIntensity.new ScalingTrack();
						lightIntensity.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.intensity = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("AmbIntensity")) {
					lightAmbientIntensity = new LightAmbientIntensity();
					lightAmbientIntensity.globalSequenceId = af.getGlobalSeqId();
					lightAmbientIntensity.interpolationType = af.getInterpType();
					lightAmbientIntensity.scalingTrack = new LightAmbientIntensity.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final LightAmbientIntensity.ScalingTrack mdxEntry = lightAmbientIntensity.new ScalingTrack();
						lightAmbientIntensity.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.ambientIntensity = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("AmbColor")) {
					lightAmbientColor = new LightAmbientColor();
					lightAmbientColor.globalSequenceId = af.getGlobalSeqId();
					lightAmbientColor.interpolationType = af.getInterpType();
					lightAmbientColor.scalingTrack = new LightAmbientColor.ScalingTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final LightAmbientColor.ScalingTrack mdxEntry = lightAmbientColor.new ScalingTrack();
						lightAmbientColor.scalingTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.ambientColor = ((Vertex) mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
						}
					}
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
			// other components of light, copied regardless currently (if this
			// becomes an issue, fix here)
			if (light.getStaticColor() != null) {
				color = light.getStaticColor().toFloatArray();
			} else {
				color = new float[] { 1, 1, 1 };
			}
			if (light.getStaticAmbColor() != null) {
				ambientColor = light.getStaticAmbColor().toFloatArray();
			} else {
				ambientColor = new float[] { 1, 1, 1 };
			}
			attenuationStart = light.getAttenuationStart();
			attenuationEnd = light.getAttenuationEnd();
			intensity = (float) light.getIntensity(); // copied regardless
														// currently
			ambientIntensity = (float) light.getAmbIntensity(); // copied
																// regardless
																// currently
			for (final String flag : light.getFlags()) {
				switch (flag) {
				case "Omnidirectional":
					type = 0;
					break;
				case "Directional":
					type = 1;
					break;
				case "Ambient":
					type = 2;
					break;
				default:
					break;
				}
			}
		}
	}
}
