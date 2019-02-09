package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdx.AttachmentVisibility;
import com.hiveworkshop.wc3.mdx.CameraPositionTranslation;
import com.hiveworkshop.wc3.mdx.CameraRotation;
import com.hiveworkshop.wc3.mdx.CameraTargetTranslation;
import com.hiveworkshop.wc3.mdx.GeosetAlpha;
import com.hiveworkshop.wc3.mdx.GeosetColor;
import com.hiveworkshop.wc3.mdx.GeosetRotation;
import com.hiveworkshop.wc3.mdx.GeosetScaling;
import com.hiveworkshop.wc3.mdx.GeosetTranslation;
import com.hiveworkshop.wc3.mdx.LightAmbientColor;
import com.hiveworkshop.wc3.mdx.LightAmbientIntensity;
import com.hiveworkshop.wc3.mdx.LightColor;
import com.hiveworkshop.wc3.mdx.LightIntensity;
import com.hiveworkshop.wc3.mdx.LightVisibility;
import com.hiveworkshop.wc3.mdx.MaterialAlpha;
import com.hiveworkshop.wc3.mdx.MaterialTextureId;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2EmissionRate;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Latitude;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Length;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Speed;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Visibility;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Width;
import com.hiveworkshop.wc3.mdx.ParticleEmitterVisibility;
import com.hiveworkshop.wc3.mdx.RibbonEmitterHeightAbove;
import com.hiveworkshop.wc3.mdx.RibbonEmitterHeightBelow;
import com.hiveworkshop.wc3.mdx.RibbonEmitterVisibility;
import com.hiveworkshop.wc3.mdx.TextureRotation;
import com.hiveworkshop.wc3.mdx.TextureScaling;
import com.hiveworkshop.wc3.mdx.TextureTranslation;
import com.hiveworkshop.wc3.util.MathUtils;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public class AnimFlag {
	public static String getInterpType(final int id) {
		switch (id) {
		case 0:
			return "DontInterp";
		case 1:
			return "Linear";
		case 2:
			return "Hermite";
		case 3:
			return "Bezier";
		default:
			return "DontInterp";
		}
	}

	public int getInterpType() {
		for (final String tag : tags) {
			switch (tag) {
			case "DontInterp":
				return 0;
			case "Linear":
				return 1;
			case "Hermite":
				return 2;
			case "Bezier":
				return 3;
			default:
				break;
			}
		}
		return 0;
	}

	public InterpolationType getInterpTypeAsEnum() {
		switch (getInterpType()) {
		case 0:
			return InterpolationType.DONT_INTERP;
		case 1:
			return InterpolationType.LINEAR;
		case 2:
			return InterpolationType.HERMITE;
		case 3:
			return InterpolationType.BEZIER;
		}
		throw new IllegalStateException();
	}
	// 0: none
	// 1: linear
	// 2: hermite
	// 3: bezier

	// Types of AnimFlags:
	// 0 Alpha
	public static final int ALPHA = 0;
	// 1 Scaling
	public static final int SCALING = 1;
	// 2 Rotation
	public static final int ROTATION = 2;
	// 3 Translation
	public static final int TRANSLATION = 3;
	// 4 Color
	public static final int COLOR = 4;
	// 5 TextureID
	public static final int TEXTUREID = 5;

	/**
	 * Use for titles like "Intensity", "AmbIntensity", and other extraneous things
	 * not included in the options above.
	 */
	public static final int OTHER_TYPE = 0;

	ArrayList<String> tags = new ArrayList<>();
	String title;
	Integer globalSeq;
	int globalSeqId = -1;
	boolean hasGlobalSeq = false;
	ArrayList<Integer> times = new ArrayList<>();
	ArrayList values = new ArrayList();
	ArrayList inTans = new ArrayList();
	ArrayList outTans = new ArrayList();
	int typeid = 0;

	public boolean equals(final AnimFlag o) {
		boolean does = o instanceof AnimFlag;
		if (!does) {
			return false;
		}
		final AnimFlag af = o;
		does = title.equals(af.title);
		does = hasGlobalSeq == af.hasGlobalSeq;
		does = values.equals(af.values) && (globalSeq == null ? af.globalSeq == null : globalSeq.equals(af.globalSeq))
				&& (tags == null ? af.tags == null : tags.equals(af.tags))
				&& (inTans == null ? af.inTans == null : inTans.equals(af.inTans))
				&& (outTans == null ? af.outTans == null : outTans.equals(af.outTans)) && typeid == af.typeid;
		return does;
	}

	public void setGlobSeq(final Integer inte) {
		globalSeq = inte;
		hasGlobalSeq = inte != null;
	}

	public Integer getGlobalSeq() {
		return globalSeq;
	}

	// begin constructors from ogre-lord's API:
	private static Double box(final float f) {
		return new Double(f);
	}

	public AnimFlag(final MaterialTextureId source) {
		title = "TextureID";
		generateTypeId();
		addTag(AnimFlag.getInterpType(source.interpolationType));
		if (source.globalSequenceId >= 0) {
			setGlobalSeqId(source.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = source.interpolationType > 1;
		for (final MaterialTextureId.ScalingTrack track : source.scalingTrack) {
			if (tans) {
				addEntry(track.time, track.textureId, track.inTan, track.outTan);
			} else {
				addEntry(track.time, track.textureId);
			}
		}
	}

	public AnimFlag(final MaterialAlpha source) {
		title = "Alpha";
		generateTypeId();
		addTag(AnimFlag.getInterpType(source.interpolationType));
		if (source.globalSequenceId >= 0) {
			setGlobalSeqId(source.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = source.interpolationType > 1;
		for (final MaterialAlpha.ScalingTrack track : source.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.alpha), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.alpha));
			}
		}
	}

	public AnimFlag(final TextureRotation textureData) {
		title = "Rotation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(textureData.interpolationType));
		if (textureData.globalSequenceId >= 0) {
			setGlobalSeqId(textureData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = textureData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???;
		for (final TextureRotation.TranslationTrack track : textureData.translationTrack) {
			if (tans) {
				addEntry(track.time, new QuaternionRotation(track.rotation), new QuaternionRotation(track.inTan),
						new QuaternionRotation(track.outTan));
			} else {
				addEntry(track.time, new QuaternionRotation(track.rotation));
			}
		}
	}

	public AnimFlag(final TextureScaling textureData) {
		title = "Scaling";
		generateTypeId();
		addTag(AnimFlag.getInterpType(textureData.interpolationType));
		if (textureData.globalSequenceId >= 0) {
			setGlobalSeqId(textureData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = textureData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final TextureScaling.TranslationTrack track : textureData.translationTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.scaling), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.scaling));
			}
		}
	}

	public AnimFlag(final TextureTranslation textureData) {
		title = "Translation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(textureData.interpolationType));
		if (textureData.globalSequenceId >= 0) {
			setGlobalSeqId(textureData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = textureData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final TextureTranslation.TranslationTrack track : textureData.translationTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.translation), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.translation));
			}
		}
	}

	public AnimFlag(final GeosetAlpha geosetAlpha) {
		title = "Alpha";
		generateTypeId();
		addTag(AnimFlag.getInterpType(geosetAlpha.interpolationType));
		if (geosetAlpha.globalSequenceId >= 0) {
			setGlobalSeqId(geosetAlpha.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = geosetAlpha.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final GeosetAlpha.ScalingTrack track : geosetAlpha.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.alpha), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.alpha));
			}
		}
	}

	public AnimFlag(final GeosetColor geosetColor) {
		title = "Color";
		generateTypeId();
		addTag(AnimFlag.getInterpType(geosetColor.interpolationType));
		if (geosetColor.globalSequenceId >= 0) {
			setGlobalSeqId(geosetColor.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = geosetColor.interpolationType > 1;
		// NOTE: autoreplaced from a > 0 check, Linear shouldn't have 'tans'???
		for (final GeosetColor.ScalingTrack track : geosetColor.scalingTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.color), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.color));
			}
		}
	}

	public AnimFlag(final GeosetTranslation geosetTranslation) {
		title = "Translation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(geosetTranslation.interpolationType));
		if (geosetTranslation.globalSequenceId >= 0) {
			setGlobalSeqId(geosetTranslation.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = geosetTranslation.interpolationType > 1;
		// NOTE: autoreplaced from a > 0 check, Linear shouldn't have 'tans'???
		for (final GeosetTranslation.TranslationTrack track : geosetTranslation.translationTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.translation), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.translation));
			}
		}
	}

	public AnimFlag(final GeosetRotation geosetData) {
		title = "Rotation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(geosetData.interpolationType));
		if (geosetData.globalSequenceId >= 0) {
			setGlobalSeqId(geosetData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = geosetData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final GeosetRotation.RotationTrack track : geosetData.rotationTrack) {
			if (tans) {
				addEntry(track.time, new QuaternionRotation(track.rotation), new QuaternionRotation(track.inTan),
						new QuaternionRotation(track.outTan));
			} else {
				addEntry(track.time, new QuaternionRotation(track.rotation));
			}
		}
	}

	public AnimFlag(final GeosetScaling geosetData) {
		title = "Scaling";
		generateTypeId();
		addTag(AnimFlag.getInterpType(geosetData.interpolationType));
		if (geosetData.globalSequenceId >= 0) {
			setGlobalSeqId(geosetData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = geosetData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final GeosetScaling.ScalingTrack track : geosetData.scalingTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.scaling), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.scaling));
			}
		}
	}

	public AnimFlag(final LightVisibility trackData) {
		title = "Visibility";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final LightVisibility.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.visibility), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.visibility));
			}
		}
	}

	public AnimFlag(final LightColor trackData) {
		title = "Color";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final LightColor.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.color), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.color));
			}
		}
	}

	public AnimFlag(final LightIntensity trackData) {
		title = "Intensity";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final LightIntensity.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.intensity), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.intensity));
			}
		}
	}

	public AnimFlag(final LightAmbientIntensity trackData) {
		title = "AmbIntensity";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final LightAmbientIntensity.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.ambientIntensity), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.ambientIntensity));
			}
		}
	}

	public AnimFlag(final LightAmbientColor trackData) {
		title = "AmbColor";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final LightAmbientColor.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.ambientColor), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.ambientColor));
			}
		}
	}

	public AnimFlag(final AttachmentVisibility trackData) {
		title = "Visibility";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final AttachmentVisibility.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.visibility), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.visibility));
			}
		}
	}

	public AnimFlag(final ParticleEmitterVisibility trackData) {
		title = "Visibility";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitterVisibility.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.visibility), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.visibility));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2Visibility trackData) {
		title = "Visibility";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2Visibility.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.visibility), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.visibility));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2EmissionRate trackData) {
		title = "EmissionRate";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2EmissionRate.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.emissionRate), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.emissionRate));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2Latitude trackData) {
		title = "Latitude";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2Latitude.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.speed), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.speed));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2Length trackData) {
		title = "Length";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2Length.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.length), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.length));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2Speed trackData) {
		title = "Speed";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2Speed.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.speed), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.speed));
			}
		}
	}

	public AnimFlag(final ParticleEmitter2Width trackData) {
		title = "Width";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final ParticleEmitter2Width.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.width), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.width));
			}
		}
	}

	public AnimFlag(final RibbonEmitterVisibility trackData) {
		title = "Visibility";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final RibbonEmitterVisibility.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.visibility), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.visibility));
			}
		}
	}

	public AnimFlag(final RibbonEmitterHeightAbove trackData) {
		title = "HeightAbove";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final RibbonEmitterHeightAbove.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.heightAbove), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.heightAbove));
			}
		}
	}

	public AnimFlag(final RibbonEmitterHeightBelow trackData) {
		title = "HeightBelow";
		generateTypeId();
		addTag(AnimFlag.getInterpType(trackData.interpolationType));
		if (trackData.globalSequenceId >= 0) {
			setGlobalSeqId(trackData.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = trackData.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final RibbonEmitterHeightBelow.ScalingTrack track : trackData.scalingTrack) {
			if (tans) {
				addEntry(track.time, box(track.heightBelow), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.heightBelow));
			}
		}
	}

	public AnimFlag(final CameraPositionTranslation translation) {
		title = "Translation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(translation.interpolationType));
		if (translation.globalSequenceId >= 0) {
			setGlobalSeqId(translation.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = translation.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final CameraPositionTranslation.TranslationTrack track : translation.translationTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.translation), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.translation));
			}
		}
	}

	public AnimFlag(final CameraTargetTranslation translation) {
		title = "Translation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(translation.interpolationType));
		if (translation.globalSequenceId >= 0) {
			setGlobalSeqId(translation.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = translation.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final CameraTargetTranslation.TranslationTrack track : translation.translationTrack) {
			if (tans) {
				addEntry(track.time, new Vertex(track.translation), new Vertex(track.inTan), new Vertex(track.outTan));
			} else {
				addEntry(track.time, new Vertex(track.translation));
			}
		}
	}

	public AnimFlag(final CameraRotation translation) {
		title = "Rotation";
		generateTypeId();
		addTag(AnimFlag.getInterpType(translation.interpolationType));
		if (translation.globalSequenceId >= 0) {
			setGlobalSeqId(translation.globalSequenceId);
			setHasGlobalSeq(true);
		}
		final boolean tans = translation.interpolationType > 1; // NOTE:
																// autoreplaced
																// from a > 0
																// check, Linear
																// shouldn't
																// have
																// 'tans'???
		for (final CameraRotation.TranslationTrack track : translation.translationTrack) {
			if (tans) {
				addEntry(track.time, box(track.rotation), box(track.inTan), box(track.outTan));
			} else {
				addEntry(track.time, box(track.rotation));
			}
		}
	}

	// end special constructors
	public AnimFlag(final String title, final ArrayList<Integer> times, final ArrayList values) {
		this.title = title;
		this.times = times;
		this.values = values;
	}

	public AnimFlag(final String title) {
		this.title = title;
		tags.add("DontInterp");
	}

	public static AnimFlag createEmpty2018(final String title, final InterpolationType interpolationType,
			final Integer globalSeq) {
		final AnimFlag flag = new AnimFlag();
		flag.title = title;
		switch (interpolationType) {
		case BEZIER:
			flag.tags.add("Bezier");
			break;
		case HERMITE:
			flag.tags.add("Hermite");
			break;
		case LINEAR:
			flag.tags.add("Linear");
			break;
		default:
		case DONT_INTERP:
			flag.tags.add("DontInterp");
			break;
		}
		flag.generateTypeId();
		flag.setGlobSeq(globalSeq);
		return flag;
	}

	public void setInterpType(final InterpolationType interpolationType) {
		System.err.println("Unsafe call to setInterpType, please rewrite code in AnimFlag class");
		tags.clear();// we're pretty sure this is just interp type now
		switch (interpolationType) {
		case BEZIER:
			tags.add("Bezier");
			break;
		case HERMITE:
			tags.add("Hermite");
			break;
		case LINEAR:
			tags.add("Linear");
			break;
		default:
		case DONT_INTERP:
			tags.add("DontInterp");
			break;
		}
	}

	public int size() {
		return times.size();
	}

	public int length() {
		return times.size();
	}

	public AnimFlag(final AnimFlag af) {
		title = af.title;
		tags = af.tags;
		globalSeq = af.globalSeq;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		typeid = af.typeid;
		times = new ArrayList<>(af.times);
		values = deepCopy(af.values);
		inTans = deepCopy(af.inTans);
		outTans = deepCopy(af.outTans);
	}

	public static AnimFlag buildEmptyFrom(final AnimFlag af) {
		final AnimFlag na = new AnimFlag(af.title);
		na.tags = af.tags;
		na.globalSeq = af.globalSeq;
		na.globalSeqId = af.globalSeqId;
		na.hasGlobalSeq = af.hasGlobalSeq;
		na.typeid = af.typeid;
		return na;
	}

	public void addTag(final String tag) {
		tags.add(tag);
	}

	public void generateTypeId() {
		typeid = 0;
		if (title.equals("Scaling")) {
			typeid = 1;
		} else if (title.equals("Rotation")) {
			typeid = 2;
		} else if (title.equals("Translation")) {
			typeid = 3;
		} else if (title.equals("TextureID"))// aflg.title.equals("Visibility")
												// || -- 100.088% visible in
												// UndeadCampaign3D OutTans! Go
												// look!
		{
			typeid = 5;
		} else if (title.contains("Color"))// AmbColor
		{
			typeid = 4;
		}
	}

	public int getGlobalSeqId() {
		return globalSeqId;
	}

	public void setGlobalSeqId(final int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean hasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(final boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public void addEntry(final Integer time, final Object value) {
		times.add(time);
		values.add(value);
	}

	public void addEntry(final Integer time, final Object value, final Object inTan, final Object outTan) {
		times.add(time);
		values.add(value);
		inTans.add(inTan);
		outTans.add(outTan);
	}

	public void setEntry(final Integer time, final Object value) {
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
				if (tans()) {
					inTans.set(index, value);
					outTans.set(index, value);
				}
			}
		}
	}

	/**
	 * This class is a small shell of an example for how my "AnimFlag" class
	 * should've been implemented. It's currently only used for the
	 * {@link AnimFlag#getEntry(int)} function.
	 *
	 * @author Eric
	 *
	 */
	public static class Entry {
		public Integer time;
		public Object value, inTan, outTan;

		public Entry(final Integer time, final Object value, final Object inTan, final Object outTan) {
			super();
			this.time = time;
			this.value = value;
			this.inTan = inTan;
			this.outTan = outTan;
		}

		public Entry(final Integer time, final Object value) {
			super();
			this.time = time;
			this.value = value;
		}
	}

	public Entry getEntry(final int index) {
		if (tans()) {
			return new Entry(times.get(index), values.get(index), inTans.get(index), outTans.get(index));
		} else {
			return new Entry(times.get(index), values.get(index));
		}
	}

	public static Object cloneValue(final Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return value;
		} else if (value instanceof Double) {
			return value;
		} else if (value instanceof Vertex) {
			final Vertex vertex = (Vertex) value;
			final Vertex clonedVertex = new Vertex(vertex);
			return clonedVertex;
		} else if (value instanceof QuaternionRotation) {
			final QuaternionRotation vertex = (QuaternionRotation) value;
			final QuaternionRotation clonedVertex = new QuaternionRotation(vertex);
			return clonedVertex;
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public Object valueAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return values.get(i);
			}
		}
		return null;
	}

	public Object inTanAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return inTans.get(i);
			}
		}
		return null;
	}

	public Object outTanAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return outTans.get(i);
			}
		}
		return null;
	}

	public void setValuesTo(final AnimFlag af) {
		title = af.title;
		tags = af.tags;
		globalSeq = af.globalSeq;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		typeid = af.typeid;
		times = new ArrayList<>(af.times);
		values = deepCopy(af.values);
		inTans = deepCopy(af.inTans);
		outTans = deepCopy(af.outTans);
	}

	private <T> ArrayList<T> deepCopy(final ArrayList<T> source) {

		final ArrayList<T> copy = new ArrayList<>();
		for (final T item : source) {
			T toAdd = item;
			if (item instanceof Vertex) {
				final Vertex v = (Vertex) item;
				toAdd = (T) v;
			} else if (item instanceof QuaternionRotation) {
				final QuaternionRotation r = (QuaternionRotation) item;
				toAdd = (T) r;
			}
			copy.add(toAdd);
		}
		return copy;
	}

	public String getName() {
		return title;
	}

	public int getTypeId() {
		return typeid;
	}

	private AnimFlag() {

	}

	public static AnimFlag find(final List<AnimFlag> flags, final String name, final Integer globalSeq) {
		// TODO make flags be a map and remove this method, this is 2018
		// not 2012 anymore, and I learned basic software dev
		for (final AnimFlag flag : flags) {
			if (flag.getName().equals(name) && (globalSeq == null && flag.globalSeq == null
					|| globalSeq != null && globalSeq.equals(flag.globalSeq))) {
				return flag;
			}
		}
		return null;
	}

	public static AnimFlag find(final List<AnimFlag> flags, final String name) {
		// TODO make flags be a map and remove this method, this is 2018
		// not 2012 anymore, and I learned basic software dev
		for (final AnimFlag flag : flags) {
			if (flag.getName().equals(name)) {
				return flag;
			}
		}
		return null;
	}

	public static AnimFlag parseText(final String[] line) {

		final AnimFlag aflg = new AnimFlag();
		aflg.title = MDLReader.readIntTitle(line[0]);
		// Types of AnimFlags:
		// 0 Alpha
		// 1 Scaling
		// 2 Rotation
		// 3 Translation
		int typeid = 0;
		if (aflg.title.equals("Scaling")) {
			typeid = 1;
		} else if (aflg.title.equals("Rotation")) {
			typeid = 2;
		} else if (aflg.title.equals("Translation")) {
			typeid = 3;
		} else if (!aflg.title.equals("Alpha")) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse \"" + aflg.title + "\": Missing or unrecognized open statement.");
		}
		aflg.typeid = typeid;
		for (int i = 1; i < line.length; i++) {
			if (line[i].contains("Tan")) {
				ArrayList target = null;
				if (line[i].contains("In"))// InTan
				{
					target = aflg.inTans;
				} else// OutTan
				{
					target = aflg.outTans;
				}
				switch (typeid) {
				case 0: // Alpha
					// A single double is used to store alpha data
					target.add(new Double(MDLReader.readDouble(line[i])));
					break;
				case 1: // Scaling
					// A vertex is used to store scaling data
					target.add(Vertex.parseText(line[i]));
					break;
				case 2: // Rotation
					// A quaternion set of four values is used to store rotation
					// data
					target.add(QuaternionRotation.parseText(line[i]));
					break;
				case 3: // Translation
					// A vertex is used to store translation data
					target.add(Vertex.parseText(line[i]));
					break;
				}
			} else if (line[i].contains(":")) {
				switch (typeid) {
				case 0: // Alpha
					// A single double is used to store alpha data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
					aflg.values.add(new Double(MDLReader.readDouble(line[i])));
					break;
				case 1: // Scaling
					// A vertex is used to store scaling data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
					aflg.values.add(Vertex.parseText(line[i]));
					break;
				case 2: // Rotation
					// A quaternion set of four values is used to store rotation
					// data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
					aflg.values.add(QuaternionRotation.parseText(line[i]));
					break;
				case 3: // Translation
					// A vertex is used to store translation data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line[i])));
					aflg.values.add(Vertex.parseText(line[i]));
					break;
				}
			} else if (line[i].contains("GlobalSeqId")) {
				if (!aflg.hasGlobalSeq) {
					aflg.globalSeqId = MDLReader.readInt(line[i]);
					aflg.hasGlobalSeq = true;
				} else {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(), "Error while parsing " + aflg.title
							+ ": More than one Global Sequence Id is present in the same " + aflg.title + "!");
				}
			} else {
				aflg.tags.add(MDLReader.readFlag(line[i]));
			}
		}
		return aflg;
	}

	public static AnimFlag read(final BufferedReader mdl) {

		final AnimFlag aflg = new AnimFlag();
		aflg.title = MDLReader.readIntTitle(MDLReader.nextLine(mdl));
		// Types of AnimFlags:
		// 0 Alpha
		// 1 Scaling
		// 2 Rotation
		// 3 Translation
		int typeid = 0;
		if (aflg.title.equals("Scaling")) {
			typeid = 1;
		} else if (aflg.title.equals("Rotation")) {
			typeid = 2;
		} else if (aflg.title.equals("Translation")) {
			typeid = 3;
		} else if (aflg.title.equals("TextureID"))// aflg.title.equals("Visibility")
													// || -- 100.088% visible in
													// UndeadCampaign3D OutTans!
													// Go look!
		{
			typeid = 5;
		} else if (aflg.title.contains("Color"))// AmbColor
		{
			typeid = 4;
		} else if (!aflg.title.equals("Alpha")) {
			// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Unable
			// to parse \""+aflg.title+"\": Missing or unrecognized open
			// statement.");
			// Having BS random AnimFlags is okay now, they all use double
			// entries
		}
		aflg.typeid = typeid;
		String line = "";
		while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
			if (line.contains("Tan")) {
				ArrayList target = null;
				if (line.contains("In"))// InTan
				{
					target = aflg.inTans;
				} else// OutTan
				{
					target = aflg.outTans;
				}
				switch (typeid) {
				case 0: // Alpha
					// A single double is used to store alpha data
					target.add(new Double(MDLReader.readDouble(line)));
					break;
				case 1: // Scaling
					// A vertex is used to store scaling data
					target.add(Vertex.parseText(line));
					break;
				case 2: // Rotation
					// A quaternion set of four values is used to store rotation
					// data
					try {
						target.add(QuaternionRotation.parseText(line));
					} catch (final Exception e) {
						// typeid = 0;//Yay! random bad model.
						target.add(new Double(MDLReader.readDouble(line)));
					}
					break;
				case 3: // Translation
					// A vertex is used to store translation data
					target.add(Vertex.parseText(line));
					break;
				case 4: // Translation
					// A vertex is used to store translation data
					target.add(Vertex.parseText(line));
					break;
				case 5: // Alpha
					// A single double is used to store alpha data
					target.add(new Integer(MDLReader.readInt(line)));
					break;
				}
			} else if (line.contains(":")) {
				switch (typeid) {
				case 0: // Alpha
					// A single double is used to store alpha data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
					aflg.values.add(new Double(MDLReader.readDouble(line)));
					break;
				case 1: // Scaling
					// A vertex is used to store scaling data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
					aflg.values.add(Vertex.parseText(line));
					break;
				case 2: // Rotation
					// A quaternion set of four values is used to store rotation
					// data
					try {

						aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
						aflg.values.add(QuaternionRotation.parseText(line));
					} catch (final Exception e) {
						// JOptionPane.showMessageDialog(null,e.getStackTrace());
						// typeid = 0;
						// aflg.times.add(new
						// Integer(MDLReader.readBeforeColon(line)));
						aflg.values.add(new Double(MDLReader.readDouble(line)));
					}
					break;
				case 3: // Translation
					// A vertex is used to store translation data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
					aflg.values.add(Vertex.parseText(line));
					break;
				case 4: // Color
					// A vertex is used to store translation data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
					aflg.values.add(Vertex.parseText(line));
					break;
				case 5: // Visibility
					// A vertex is used to store translation data
					aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
					aflg.values.add(new Integer(MDLReader.readInt(line)));
					break;
				}
			} else if (line.contains("GlobalSeqId")) {
				if (!aflg.hasGlobalSeq) {
					aflg.globalSeqId = MDLReader.readInt(line);
					aflg.hasGlobalSeq = true;
				} else {
					JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(), "Error while parsing " + aflg.title
							+ ": More than one Global Sequence Id is present in the same " + aflg.title + "!");
				}
			} else {
				aflg.tags.add(MDLReader.readFlag(line));
			}
		}
		return aflg;
	}

	public void updateGlobalSeqRef(final MDL mdlr) {
		if (hasGlobalSeq) {
			globalSeq = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(final MDL mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeq);
		}
	}

	public String flagToString(final Object o) {
		if (o.getClass() == double.class) {
			return MDLReader.doubleToString((Double) o);
		} else if (o.getClass() == Double.class) {
			return MDLReader.doubleToString(((Double) o).doubleValue());
		} else {
			return o.toString();
		}
	}

	public void flipOver(final byte axis) {
		if (typeid == 2) {
			// Rotation
			for (int k = 0; k < values.size(); k++) {
				final QuaternionRotation rot = (QuaternionRotation) values.get(k);
				final Vertex euler = rot.toEuler();
				switch (axis) {
				case 0:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					break;
				case 1:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				case 2:
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				}
				values.set(k, new QuaternionRotation(euler));
			}
			for (int k = 0; k < inTans.size(); k++) {
				final QuaternionRotation rot = (QuaternionRotation) inTans.get(k);
				final Vertex euler = rot.toEuler();
				switch (axis) {
				case 0:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					break;
				case 1:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				case 2:
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				}
				inTans.set(k, new QuaternionRotation(euler));
			}
			for (int k = 0; k < outTans.size(); k++) {
				final QuaternionRotation rot = (QuaternionRotation) outTans.get(k);
				final Vertex euler = rot.toEuler();
				switch (axis) {
				case 0:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					break;
				case 1:
					euler.setCoord((byte) 0, -euler.getCoord((byte) 0));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				case 2:
					euler.setCoord((byte) 1, -euler.getCoord((byte) 1));
					euler.setCoord((byte) 2, -euler.getCoord((byte) 2));
					break;
				}
				outTans.set(k, new QuaternionRotation(euler));
			}
		} else if (typeid == 3) {
			// Translation
			for (int k = 0; k < values.size(); k++) {
				final Vertex trans = (Vertex) values.get(k);
				// trans.setCoord(axis,-trans.getCoord(axis));
				switch (axis) {
				// case 0:
				// trans.setCoord((byte)2,-trans.getCoord((byte)2));
				// break;
				// case 1:
				// trans.setCoord((byte)0,-trans.getCoord((byte)0));
				// break;
				// case 2:
				// trans.setCoord((byte)1,-trans.getCoord((byte)1));
				// break;
				case 0:
					trans.setCoord((byte) 0, -trans.getCoord((byte) 0));
					break;
				case 1:
					trans.setCoord((byte) 1, -trans.getCoord((byte) 1));
					break;
				case 2:
					trans.setCoord((byte) 2, -trans.getCoord((byte) 2));
					break;
				}
			}
			for (int k = 0; k < inTans.size(); k++) {
				final Vertex trans = (Vertex) inTans.get(k);
				// trans.setCoord(axis,-trans.getCoord(axis));
				switch (axis) {
				// case 0:
				// trans.setCoord((byte)2,-trans.getCoord((byte)2));
				// break;
				// case 1:
				// trans.setCoord((byte)0,-trans.getCoord((byte)0));
				// break;
				// case 2:
				// trans.setCoord((byte)1,-trans.getCoord((byte)1));
				// break;
				case 0:
					trans.setCoord((byte) 0, -trans.getCoord((byte) 0));
					break;
				case 1:
					trans.setCoord((byte) 1, -trans.getCoord((byte) 1));
					break;
				case 2:
					trans.setCoord((byte) 2, -trans.getCoord((byte) 2));
					break;
				}
			}
			for (int k = 0; k < outTans.size(); k++) {
				final Vertex trans = (Vertex) outTans.get(k);
				// trans.setCoord(axis,-trans.getCoord(axis));
				switch (axis) {
				// case 0:
				// trans.setCoord((byte)2,-trans.getCoord((byte)2));
				// break;
				// case 1:
				// trans.setCoord((byte)0,-trans.getCoord((byte)0));
				// break;
				// case 2:
				// trans.setCoord((byte)1,-trans.getCoord((byte)1));
				// break;
				case 0:
					trans.setCoord((byte) 0, -trans.getCoord((byte) 0));
					break;
				case 1:
					trans.setCoord((byte) 1, -trans.getCoord((byte) 1));
					break;
				case 2:
					trans.setCoord((byte) 2, -trans.getCoord((byte) 2));
					break;
				}
			}
		}
	}

	public void printTo(final PrintWriter writer, final int tabHeight) {
		if (size() > 0) {
			sort();
			String tabs = "";
			for (int i = 0; i < tabHeight; i++) {
				tabs = tabs + "\t";
			}
			writer.println(tabs + title + " " + times.size() + " {");
			for (int i = 0; i < tags.size(); i++) {
				writer.println(tabs + "\t" + tags.get(i) + ",");
			}
			if (hasGlobalSeq) {
				writer.println(tabs + "\tGlobalSeqId " + globalSeqId + ",");
			}
			boolean tans = false;
			if (inTans.size() > 0) {
				tans = true;
			}
			for (int i = 0; i < times.size(); i++) {
				writer.println(tabs + "\t" + times.get(i) + ": " + flagToString(values.get(i)) + ",");
				if (tans) {
					writer.println(tabs + "\t\tInTan " + flagToString(inTans.get(i)) + ",");
					writer.println(tabs + "\t\tOutTan " + flagToString(outTans.get(i)) + ",");
				}
			}
			// switch (typeid )
			// {
			// case 0: //Alpha
			// //A single double is used to store alpha data
			// for( int i = 0; i < times.size(); i++ )
			// {
			// writer.println(tabs+"\t"+times.get(i)+":
			// "+((Double)values.get(i)).doubleValue()+",");
			// if( tans )
			// {
			// writer.println(tabs+"\t\tInTan
			// "+((Double)inTans.get(i)).doubleValue()+",");
			// writer.println(tabs+"\t\tOutTan
			// "+((Double)outTans.get(i)).doubleValue()+",");
			// }
			// }
			// break;
			// case 1: //Scaling
			// //A vertex is used to store scaling data
			// for( int i = 0; i < times.size(); i++ )
			// {
			// writer.println(tabs+"\t"+times.get(i)+":
			// "+((Vertex)values.get(i)).toString()+",");
			// if( tans )
			// {
			// writer.println(tabs+"\t\tInTan
			// "+((Vertex)inTans.get(i)).toString()+",");
			// writer.println(tabs+"\t\tOutTan
			// "+((Double)outTans.get(i)).doubleValue()+",");
			// }
			// }
			// break;
			// case 2: //Rotation
			// //A quaternion set of four values is used to store rotation data
			// aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
			// aflg.values.add(QuaternionRotation.parseText(line));
			// break;
			// case 3: //Translation
			// //A vertex is used to store translation data
			// aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
			// aflg.values.add(Vertex.parseText(line));
			// break;
			// case 4: //Color
			// //A vertex is used to store translation data
			// aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
			// aflg.values.add(Vertex.parseText(line));
			// break;
			// case 5: //Visibility
			// //A vertex is used to store translation data
			// aflg.times.add(new Integer(MDLReader.readBeforeColon(line)));
			// aflg.values.add(new Integer(MDLReader.readInt(line)));
			// break;
			// }
			writer.println(tabs + "}");
		}
	}

	public AnimFlag getMostVisible(final AnimFlag partner) {
		if (partner != null) {
			if (typeid == 0 && partner.typeid == 0) {
				final ArrayList<Integer> atimes = new ArrayList<>(times);
				final ArrayList<Integer> btimes = new ArrayList<>(partner.times);
				final ArrayList<Double> avalues = new ArrayList(values);
				final ArrayList<Double> bvalues = new ArrayList(partner.values);
				AnimFlag mostVisible = null;
				for (int i = atimes.size() - 1; i >= 0; i--)
				// count down from top, meaning that removing the current value
				// causes no harm
				{
					final Integer currentTime = atimes.get(i);
					final Double currentVal = avalues.get(i);

					if (btimes.contains(currentTime)) {
						final Double partVal = bvalues.get(btimes.indexOf(currentTime));
						if (partVal.doubleValue() > currentVal.doubleValue()) {
							if (mostVisible == null) {
								mostVisible = partner;
							} else if (mostVisible == this) {
								return null;
							}
						} else if (partVal.doubleValue() < currentVal.doubleValue()) {
							if (mostVisible == null) {
								mostVisible = this;
							} else if (mostVisible == partner) {
								return null;
							}
						} else {
							// System.out.println("Equal entries spell success");
						}
						// btimes.remove(currentTime);
						// bvalues.remove(partVal);
					} else if (currentVal.doubleValue() < 1) {
						if (mostVisible == null) {
							mostVisible = partner;
						} else if (mostVisible == this) {
							return null;
						}
					}
				}
				for (int i = btimes.size() - 1; i >= 0; i--)
				// count down from top, meaning that removing the current value
				// causes no harm
				{
					final Integer currentTime = btimes.get(i);
					final Double currentVal = bvalues.get(i);

					if (atimes.contains(currentTime)) {
						final Double partVal = avalues.get(atimes.indexOf(currentTime));
						if (partVal.doubleValue() > currentVal.doubleValue()) {
							if (mostVisible == null) {
								mostVisible = this;
							} else if (mostVisible == partner) {
								return null;
							}
						} else if (partVal.doubleValue() < currentVal.doubleValue()) {
							if (mostVisible == null) {
								mostVisible = partner;
							} else if (mostVisible == this) {
								return null;
							}
						}
					} else if (currentVal.doubleValue() < 1) {
						if (mostVisible == null) {
							mostVisible = this;
						} else if (mostVisible == partner) {
							return null;
						}
					}
				}
				if (mostVisible == null) {
					return partner;// partner has priority!
				} else {
					return mostVisible;
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Error: Program attempted to compare visibility with non-visibility animation component.\nThis... probably means something is horribly wrong. Save your work, if you can.");
			}
		}
		return null;
	}

	public boolean tans() {
		return tags.contains("Bezier") || tags.contains("Hermite") || inTans.size() > 0;
	}

	public void linearize() {
		if (tags.remove("Bezier") || tags.remove("Hermite")) {
			tags.add("Linear");
			inTans.clear();
			outTans.clear();
		}
	}

	public void copyFrom(final AnimFlag source) {
		times.addAll(source.times);
		values.addAll(source.values);
		final boolean stans = source.tans();
		final boolean mtans = tans();
		if (stans && mtans) {
			inTans.addAll(source.inTans);
			outTans.addAll(source.outTans);
		} else if (mtans) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			inTans.clear();
			outTans.clear();
			tags = source.tags;
			// Probably makes this flag linear, but certainly makes it more like
			// the copy source
		}
	}

	public void deleteAnim(final Animation anim) {
		if (!hasGlobalSeq) {
			final boolean tans = tans();
			for (int index = times.size() - 1; index >= 0; index--) {
				final Integer inte = times.get(index);
				final int i = inte.intValue();
				// int index = times.indexOf(inte);
				if (i >= anim.getStart() && i <= anim.getEnd()) {
					// If this "i" is a part of the anim being removed

					times.remove(index);
					values.remove(index);
					if (tans) {
						inTans.remove(index);
						outTans.remove(index);
					}
				}
			}
		} else {
			System.out.println("KeyFrame deleting was blocked by a GlobalSequence");
		}

		// BOOM magic happens
	}

	public void deleteAt(final int index) {
		times.remove(index);
		values.remove(index);
		if (tans()) {
			inTans.remove(index);
			outTans.remove(index);
		}
	}

	/**
	 * Copies time track data from a certain interval into a different, new
	 * interval. The AnimFlag source of the data to copy cannot be same AnimFlag
	 * into which the data is copied, or else a ConcurrentModificationException will
	 * be thrown.
	 *
	 * @param source
	 * @param sourceStart
	 * @param sourceEnd
	 * @param newStart
	 * @param newEnd
	 */
	public void copyFrom(final AnimFlag source, final int sourceStart, final int sourceEnd, final int newStart,
			final int newEnd) {
		// Timescales a part of the AnimFlag from the source into the new time
		// "newStart" to "newEnd"
		boolean tans = source.tans();
		if (tans && tags.contains("Linear")) {
			final int x = JOptionPane.showConfirmDialog(null,
					"ERROR! A source was found to have Linear and Nonlinear motion simultaneously. Does the following have non-zero data? "
							+ source.inTans,
					"Help This Program!", JOptionPane.YES_NO_OPTION);
			if (x == JOptionPane.NO_OPTION) {
				tans = false;
			}
		}
		for (final Integer inte : source.times) {
			final int i = inte.intValue();
			final int index = source.times.indexOf(inte);
			if (i >= sourceStart && i <= sourceEnd) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - sourceStart) / (double) (sourceEnd - sourceStart);
				times.add(new Integer((int) (newStart + ratio * (newEnd - newStart))));
				values.add(cloneValue(source.values.get(index)));
				if (tans) {
					inTans.add(cloneValue(source.inTans.get(index)));
					outTans.add(cloneValue(source.outTans.get(index)));
				}
			}
		}

		sort();

		// BOOM magic happens
	}

	public void timeScale(final int start, final int end, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		// if( newEnd > newStart )
		// {
		for (int z = 0; z < times.size(); z++)// Integer inte: times )
		{
			final Integer inte = times.get(z);
			final int i = inte.intValue();
			if (i >= start && i <= end) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				times.set(z, new Integer((int) (newStart + ratio * (newEnd - newStart))));
			}
		}
		// }
		// else
		// {
		// for( Integer inte: times )
		// {
		// int i = inte.intValue();
		// if( i >= end && i <= start )
		// {
		// //If this "i" is a part of the anim being rescaled
		// double ratio = (double)(i-start)/(double)(end-start);
		// times.set(times.indexOf(inte),new Integer((int)(newStart + ( ratio *
		// ( newStart - newEnd ) ) ) ) );
		// }
		// }
		// }

		sort();

		// BOOM magic happens
	}

	public void sort() {
		final int low = 0;
		final int high = times.size() - 1;
		if (size() > 1) {
			quicksort(low, high);
		}
	}

	private void quicksort(final int low, final int high) {
		// Thanks to Lars Vogel for the quicksort concept code (something to
		// look at), found on google
		// (re-written by Eric "Retera" for use in AnimFlags)
		int i = low, j = high;
		final Integer pivot = times.get(low + (high - low) / 2);

		while (i <= j) {
			while (times.get(i).intValue() < pivot.intValue()) {
				i++;
			}
			while (times.get(j).intValue() > pivot.intValue()) {
				j--;
			}
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j) {
			quicksort(low, j);
		}
		if (i < high) {
			quicksort(i, high);
		}
	}

	private void exchange(final int i, final int j) {
		final Integer iTime = times.get(i);
		final Object iValue = values.get(i);

		times.set(i, times.get(j));
		try {
			values.set(i, values.get(j));
		} catch (final Exception e) {
			e.printStackTrace();
			// System.out.println(getName()+":
			// "+times.size()+","+values.size());
			// System.out.println(times.get(0)+": "+values.get(0));
			// System.out.println(times.get(1));
		}

		times.set(j, iTime);
		values.set(j, iValue);

		if (inTans.size() > 0)// if we have to mess with Tans
		{
			final Object iInTan = inTans.get(i);
			final Object iOutTan = outTans.get(i);

			inTans.set(i, inTans.get(j));
			outTans.set(i, outTans.get(j));

			inTans.set(j, iInTan);
			outTans.set(j, iOutTan);
		}
	}

	public ArrayList getValues() {
		return values;
	}

	public ArrayList<Integer> getTimes() {
		return times;
	}

	public ArrayList getInTans() {
		return inTans;
	}

	public ArrayList getOutTans() {
		return outTans;
	}

	public int ceilIndex(final int time) {
		if (times.size() == 0) {
			return 0;
		}
		final int ceilIndex = ceilIndex(time, 0, times.size() - 1);
		if (ceilIndex == -1) {
			return times.size() - 1;
		}
		return ceilIndex;
	}

	/*
	 * Rather than spending time visualizing corner cases for these, I borrowed
	 * logic from: https://www.geeksforgeeks.org/ceiling-in-a-sorted-array/
	 */
	private int ceilIndex(final int time, final int lo, final int hi) {
		if (time <= times.get(lo)) {
			return lo;
		}
		if (time > times.get(hi)) {
			return -1;
		}
		final int mid = (lo + hi) / 2;
		final Integer midTime = times.get(mid);
		if (midTime == time) {
			return mid;
		} else if (midTime < time) {
			if (mid + 1 <= hi && time <= times.get(mid + 1)) {
				return mid + 1;
			} else {
				return ceilIndex(time, mid + 1, hi);
			}
		} else {
			if (mid - 1 >= lo && time > times.get(mid - 1)) {
				return mid;
			} else {
				return ceilIndex(time, lo, mid - 1);
			}
		}
	}

	public int floorIndex(final int time) {
		if (times.size() == 0) {
			return 0;
		}
		final int floorIndex = floorIndex(time, 0, times.size() - 1);
		if (floorIndex == -1) {
			return 0;
		}
		return floorIndex;
	}

	/*
	 * Rather than spending time visualizing corner cases for these, I borrowed
	 * logic from: https://www.geeksforgeeks.org/floor-in-a-sorted-array/
	 */
	private int floorIndex(final int time, final int lo, final int hi) {
		if (lo > hi) {
			return -1;
		}
		if (time >= times.get(hi)) {
			return hi;
		}
		final int mid = (lo + hi) / 2;
		final Integer midTime = times.get(mid);
		if (times.get(mid) == time) {
			return mid;
		}
		if (mid > 0 && times.get(mid - 1) <= time && time < midTime) {
			return mid - 1;
		}
		if (time > midTime) {
			return floorIndex(time, mid + 1, hi);
		} else {
			return floorIndex(time, lo, mid - 1);
		}
	}

	public static final QuaternionRotation ROTATE_IDENTITY = new QuaternionRotation(0, 0, 0, 1);
	public static final Vertex SCALE_IDENTITY = new Vertex(1, 1, 1);
	public static final Vertex TRANSLATE_IDENTITY = new Vertex(0, 0, 0);

	private Object identity(final int typeid) {
		switch (typeid) {
		case ALPHA | OTHER_TYPE: {
			return 1.;
		}
		case TRANSLATION:
			return TRANSLATE_IDENTITY;
		case SCALING:
		case COLOR: {
			return SCALE_IDENTITY;
		}
		case ROTATION: {
			return ROTATE_IDENTITY;
		}
		case TEXTUREID:
		// Integer
		{
			System.err.println("Texture identity used in renderer... TODO make this function more intelligent.");
			return 0;
		}
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * Interpolates at a given time. The lack of generics on this function is
	 * abysmal, but currently this is how the codebase is.
	 *
	 * @param time
	 * @param animation
	 * @return
	 */
	public Object interpolateAt(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		int localTypeId = typeid;
		if (localTypeId == ROTATION && size() > 0 && values.get(0) instanceof Double) {
			localTypeId = ALPHA; // magic Camera rotation!
		}
		if (times.isEmpty()) {
			return identity(localTypeId);
		}
		// TODO ghostwolf says to stop using binary search, because linear walking is
		// faster for the small MDL case
		int time;
		int ceilIndex;
		int floorIndex;
		Object floorInTan;
		Object floorOutTan;
		Object floorValue;
		Object ceilValue;
		Integer floorIndexTime;
		Integer ceilIndexTime;
		if (hasGlobalSeq() && getGlobalSeq() >= 0) {
			time = animatedRenderEnvironment.getGlobalSeqTime(getGlobalSeq());
			final int floorAnimStartIndex = floorIndex(1);
			final int floorAnimEndIndex = floorIndex(getGlobalSeq());
			floorIndex = floorIndex(time);
			ceilIndex = ceilIndex(time);
			if (ceilIndex < floorIndex) {
				// retarded repeated keyframes issue, see Peasant's Bone_Chest
				// at time 18300
				ceilIndex = floorIndex;
			}
			floorValue = values.get(floorIndex);
			floorInTan = tans() ? inTans.get(floorIndex) : null;
			floorOutTan = tans() ? outTans.get(floorIndex) : null;
			ceilValue = values.get(ceilIndex);
			floorIndexTime = times.get(floorIndex);
			ceilIndexTime = times.get(ceilIndex);
			if (ceilIndexTime < 0) {
				return identity(localTypeId);
			}
			if (floorIndexTime > getGlobalSeq()) {
				return identity(localTypeId);
			}
			if (floorIndexTime < 0 && ceilIndexTime > getGlobalSeq()) {
				return identity(localTypeId);
			} else if (floorIndexTime < 0) {
				floorValue = identity(localTypeId);
				floorInTan = floorOutTan = identity(localTypeId);
			} else if (ceilIndexTime > getGlobalSeq()) {
				ceilValue = values.get(floorAnimStartIndex);
				ceilIndex = floorAnimStartIndex;
			}
			if (floorIndex == ceilIndex) {
				return floorValue;
			}
		} else {
			final BasicTimeBoundProvider animation = animatedRenderEnvironment.getCurrentAnimation();
			time = animation.getStart() + animatedRenderEnvironment.getAnimationTime();
			final int floorAnimStartIndex = floorIndex(animation.getStart() + 1);
			final int floorAnimEndIndex = floorIndex(animation.getEnd());
			floorIndex = floorIndex(time);
			ceilIndex = ceilIndex(time);
			if (ceilIndex < floorIndex) {
				// retarded repeated keyframes issue, see Peasant's Bone_Chest
				// at time 18300
				ceilIndex = floorIndex;
			}
			floorValue = values.get(floorIndex);
			floorInTan = tans() ? inTans.get(floorIndex) : null;
			floorOutTan = tans() ? outTans.get(floorIndex) : null;
			ceilValue = values.get(ceilIndex);
			floorIndexTime = times.get(floorIndex);
			ceilIndexTime = times.get(ceilIndex);
			if (ceilIndexTime < animation.getStart()) {
				return identity(localTypeId);
			}
			if (floorIndexTime > animation.getEnd()) {
				return identity(localTypeId);
			}
			if (floorIndexTime < animation.getStart() && ceilIndexTime > animation.getEnd()) {
				return identity(localTypeId);
			} else if (floorIndexTime < animation.getStart()) {
				if (times.get(floorAnimEndIndex) == animation.getEnd()) {
					floorIndex = floorAnimEndIndex;
					floorValue = values.get(floorAnimEndIndex);
					floorIndexTime = animation.getStart();
					if (tans()) {
						floorInTan = inTans.get(floorAnimEndIndex);
						floorOutTan = inTans.get(floorAnimEndIndex);
//						floorIndexTime = times.get(floorAnimEndIndex);
					}
				} else {
					floorValue = identity(localTypeId);
					floorInTan = floorOutTan = identity(localTypeId);
					floorIndexTime = animation.getStart();
				}
			} else if (ceilIndexTime > animation.getEnd()
					|| ceilIndexTime < time && times.get(floorAnimEndIndex) < time) {
				if (times.get(floorAnimStartIndex) == animation.getStart()) {
					ceilValue = values.get(floorAnimStartIndex);
					ceilIndex = floorAnimStartIndex;
					ceilIndexTime = animation.getEnd();
				}
				// NOTE: we just let it be in this case, based on
				// Water Elemental's birth
			}
			if (floorIndex == ceilIndex) {
				return floorValue;
			}
		}
		switch (localTypeId) {
		case ALPHA | OTHER_TYPE: {
			// Double
			final Double previous = (Double) floorValue;
			final Double next = (Double) ceilValue;
			switch (getInterpTypeAsEnum()) {
			case BEZIER: {
				final Double previousOutTan = (Double) floorOutTan;
				final Double nextInTan = (Double) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final double bezier = MathUtils.bezier(previous, previousOutTan, nextInTan, next,
						(float) (time - floorTime) / (float) (ceilTime - floorTime));
				return bezier;
			}
			case DONT_INTERP:
				return floorValue;
			case HERMITE: {
				final Double previousOutTan = (Double) floorOutTan;
				final Double nextInTan = (Double) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final double hermite = MathUtils.hermite(previous, previousOutTan, nextInTan, next,
						(float) (time - floorTime) / (float) (ceilTime - floorTime));
				return hermite;
			}
			case LINEAR:
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final double lerp = MathUtils.lerp(previous, next,
						(float) (time - floorTime) / (float) (ceilTime - floorTime));
				return lerp;
			default:
				throw new IllegalStateException();
			}
		}
		case TRANSLATION:
		case SCALING:
		case COLOR: {
			// Vertex
			final Vertex previous = (Vertex) floorValue;
			final Vertex next = (Vertex) ceilValue;
			switch (getInterpTypeAsEnum()) {
			case BEZIER: {
				final Vertex previousOutTan = (Vertex) floorOutTan;
				final Vertex nextInTan = (Vertex) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final Vertex bezier = new Vertex(
						MathUtils.bezier(previous.x, previousOutTan.x, nextInTan.x, next.x, timeFactor),
						MathUtils.bezier(previous.y, previousOutTan.y, nextInTan.y, next.y, timeFactor),
						MathUtils.bezier(previous.z, previousOutTan.z, nextInTan.z, next.z, timeFactor));
				return bezier;
			}
			case DONT_INTERP:
				return floorValue;
			case HERMITE: {
				final Vertex previousOutTan = (Vertex) floorOutTan;
				final Vertex nextInTan = (Vertex) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final Vertex hermite = new Vertex(
						MathUtils.hermite(previous.x, previousOutTan.x, nextInTan.x, next.x, timeFactor),
						MathUtils.hermite(previous.y, previousOutTan.y, nextInTan.y, next.y, timeFactor),
						MathUtils.hermite(previous.z, previousOutTan.z, nextInTan.z, next.z, timeFactor));
				return hermite;
			}
			case LINEAR:
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final Vertex lerp = new Vertex(MathUtils.lerp(previous.x, next.x, timeFactor),
						MathUtils.lerp(previous.y, next.y, timeFactor), MathUtils.lerp(previous.z, next.z, timeFactor));
				return lerp;
			default:
				throw new IllegalStateException();
			}
		}
		case ROTATION: {
			// Quat
			final QuaternionRotation previous = (QuaternionRotation) floorValue;
			final QuaternionRotation next = (QuaternionRotation) ceilValue;
			switch (getInterpTypeAsEnum()) {
			case BEZIER: {
				final QuaternionRotation previousOutTan = (QuaternionRotation) floorOutTan;
				final QuaternionRotation nextInTan = (QuaternionRotation) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final QuaternionRotation result = new QuaternionRotation(0, 0, 0, 0);
				return QuaternionRotation.ghostwolfSquad(result, previous, previousOutTan, nextInTan, next, timeFactor);
			}
			case DONT_INTERP:
				return floorValue;
			case HERMITE: {
				final QuaternionRotation previousOutTan = (QuaternionRotation) floorOutTan;
				final QuaternionRotation nextInTan = (QuaternionRotation) inTans.get(ceilIndex);
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final QuaternionRotation result = new QuaternionRotation(0, 0, 0, 0);
				return QuaternionRotation.ghostwolfSquad(result, previous, previousOutTan, nextInTan, next, timeFactor);
			}
			case LINEAR:
				final Integer floorTime = floorIndexTime;
				final Integer ceilTime = ceilIndexTime;
				final float timeFactor = (float) (time - floorTime) / (float) (ceilTime - floorTime);
				final QuaternionRotation result = new QuaternionRotation(0, 0, 0, 0);
				return QuaternionRotation.slerp(result, previous, next, timeFactor);
			default:
				throw new IllegalStateException();
			}
		}
		case TEXTUREID:
		// Integer
		{
			final Integer previous = (Integer) floorValue;
			switch (getInterpTypeAsEnum()) {
			case DONT_INTERP:
			case BEZIER: // dont use bezier on these, does that even make any sense?
			case HERMITE: // dont use hermite on these, does that even make any sense?
			case LINEAR: // dont use linear on these, does that even make any sense?
				return previous;
			default:
				throw new IllegalStateException();
			}
		}
		}
		throw new IllegalStateException();
	}

	public void removeKeyframe(final int trackTime) {
		final int keyframeIndex = floorIndex(trackTime);
		if (keyframeIndex >= size() || times.get(keyframeIndex) != trackTime) {
			throw new IllegalStateException("Attempted to remove keyframe, but no keyframe was found (" + keyframeIndex
					+ " @ time " + trackTime + ")");
		} else {
			times.remove(keyframeIndex);
			values.remove(keyframeIndex);
			if (tans()) {
				inTans.remove(keyframeIndex);
				outTans.remove(keyframeIndex);
			}
		}
	}

	public void addKeyframe(final int trackTime, final Object value) {
		int keyframeIndex = ceilIndex(trackTime);
		if (keyframeIndex == times.size() - 1) {
			if (times.isEmpty()) {
				keyframeIndex = 0;
			} else if (trackTime > times.get(times.size() - 1)) {
				keyframeIndex = times.size();
			}
		}
		times.add(keyframeIndex, trackTime);
		values.add(keyframeIndex, value);
	}

	public void addKeyframe(final int trackTime, final Object value, final Object inTan, final Object outTan) {
		int keyframeIndex = ceilIndex(trackTime);
		if (keyframeIndex == times.size() - 1) {
			if (times.isEmpty()) {
				keyframeIndex = 0;
			} else if (trackTime > times.get(times.size() - 1)) {
				keyframeIndex = times.size();
			}
		}
		times.add(keyframeIndex, trackTime);
		values.add(keyframeIndex, value);
		inTans.add(keyframeIndex, inTan);
		outTans.add(keyframeIndex, outTan);
	}

	public void setKeyframe(final Integer time, final Object value) {
		if (tans()) {
			throw new IllegalStateException();
		}
		// TODO maybe binary search, ghostwolf says it's not worth it
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
			}
		}
	}

	public void setKeyframe(final Integer time, final Object value, final Object inTan, final Object outTan) {
		if (!tans()) {
			throw new IllegalStateException();
		}
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
				inTans.set(index, inTan);
				outTans.set(index, outTan);
			}
		}
	}

	public void slideKeyframe(final int startTrackTime, final int endTrackTime) {
		if (times.size() < 1) {
			throw new IllegalStateException("Unable to slide keyframe: no frames exist");
		}
		final int startIndex = floorIndex(startTrackTime);
		final int endIndex = floorIndex(endTrackTime);
		if (times.get(endIndex) == endTrackTime) {
			throw new IllegalStateException("Sliding this keyframe would create duplicate entries at one time!");
		}
		times.set(startIndex, endTrackTime);
		sort();
	}
}
