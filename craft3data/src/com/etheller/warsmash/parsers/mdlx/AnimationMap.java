package com.etheller.warsmash.parsers.mdlx;

import java.util.HashMap;
import java.util.Map;

import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

/**
 * A map from MDX animation tags to their equivalent MDL tokens, and the
 * implementation objects.
 *
 * <p>
 * Based on the works of Chananya Freiman.
 *
 */
public enum AnimationMap {
	// Layer
	/**
	 * Layer Texture ID
	 */
	KMTF(MdlUtils.TOKEN_TEXTURE_ID, MdlxTimelineDescriptor.UINT32_TIMELINE),
	/**
	 * Layer Alpha
	 */
	KMTA(MdlUtils.TOKEN_ALPHA, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Layer Emissive Gain
	 */
	KMTE("EmissiveGain", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Layer Fresnel Color
	 */
	KFC3("FresnelColor", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Layer Fresnel Opacity
	 */
	KFCA("FresnelOpacity", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Layer Fresnel Team Color
	 */
	KFTC("FresnelTeamColor", MdlxTimelineDescriptor.UINT32_TIMELINE),
	// TextureAnimation
	/**
	 * Texture Animation Translation
	 */
	KTAT(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Texture Animation Rotation
	 */
	KTAR(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.VECTOR4_TIMELINE),
	/**
	 * Texture Animation Scaling
	 */
	KTAS(MdlUtils.TOKEN_SCALING, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	// GeosetAnimation
	/**
	 * Geoset Animation Alpha
	 */
	KGAO(MdlUtils.TOKEN_ALPHA, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Geoset Animation Color
	 */
	KGAC(MdlUtils.TOKEN_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	// Light
	/**
	 * Light Attenuation Start
	 */
	KLAS(MdlUtils.TOKEN_ATTENUATION_START, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Light Attenuation End
	 */
	KLAE(MdlUtils.TOKEN_ATTENUATION_END, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Light Color
	 */
	KLAC(MdlUtils.TOKEN_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Light Intensity
	 */
	KLAI(MdlUtils.TOKEN_INTENSITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Light Ambient Intensity
	 */
	KLBI(MdlUtils.TOKEN_AMB_INTENSITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Light Ambient Color
	 */
	KLBC(MdlUtils.TOKEN_AMB_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Light Visibility
	 */
	KLAV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// Attachment
	/**
	 * Attachment Visibility
	 */
	KATV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// ParticleEmitter
	/**
	 * Particle Emitter Emission Rate
	 */
	KPEE(MdlUtils.TOKEN_EMISSION_RATE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Gravity
	 */
	KPEG(MdlUtils.TOKEN_GRAVITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Longitude
	 */
	KPLN(MdlUtils.TOKEN_LONGITUDE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Latitude
	 */
	KPLT(MdlUtils.TOKEN_LATITUDE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Lifespan
	 */
	KPEL(MdlUtils.TOKEN_LIFE_SPAN, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Initial Velocity
	 */
	KPES(MdlUtils.TOKEN_INIT_VELOCITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter Visibility
	 */
	KPEV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// ParticleEmitter2
	/**
	 * Particle Emitter 2 Speed
	 */
	KP2S("Speed", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Variation
	 */
	KP2R("Variation", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Latitude
	 */
	KP2L("Latitude", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Gravity
	 */
	KP2G("Gravity", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Emission Rate
	 */
	KP2E("EmissionRate", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Length
	 */
	KP2N("Length", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Width
	 */
	KP2W("Width", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Particle Emitter 2 Visibility
	 */
	KP2V("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 // ParticleEmitterCorn
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPA("Alpha", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPC("Color", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPE("EmissionRate", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPL("LifeSpan", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPS("Speed", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 /**
	 * Particle Emitter Popcorn Emission
	 */
	 KPPV("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// RibbonEmitter
	/**
	 * Ribbon Emitter Emission
	 */
	KRHA("HeightAbove", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Ribbon Emitter Emission
	 */
	KRHB("HeightBelow", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Ribbon Emitter Emission
	 */
	KRAL("Alpha", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	/**
	 * Ribbon Emitter Emission
	 */
	KRCO("Color", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Ribbon Emitter Emission
	 */
	KRTX("TextureSlot", MdlxTimelineDescriptor.UINT32_TIMELINE),
	/**
	 * Ribbon Emitter Emission
	 */
	KRVS("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// Camera
	/**
	 * Camera Source Translation
	 */
	KCTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Camera Target Translation
	 */
	KTTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Camera Source Rotation
	 */
	KCRL(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.UINT32_TIMELINE),
	// GenericObject
	/**
	 * Generic object Translation
	 */
	KGTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	/**
	 * Generic object Rotation
	 */
	KGRT(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.VECTOR4_TIMELINE),
	/**
	 * Generic Object Scaling
	 */
	KGSC(MdlUtils.TOKEN_SCALING, MdlxTimelineDescriptor.VECTOR3_TIMELINE);

	private final String mdlToken;
	private final MdlxTimelineDescriptor implementation;
	private final War3ID war3id;

	private AnimationMap(final String mdlToken, final MdlxTimelineDescriptor implementation) {
		this.mdlToken = mdlToken;
		this.implementation = implementation;
		this.war3id = War3ID.fromString(this.name());
	}

	public String getMdlToken() {
		return this.mdlToken;
	}

	public MdlxTimelineDescriptor getImplementation() {
		return this.implementation;
	}

	public War3ID getWar3id() {
		return this.war3id;
	}

	public static final Map<War3ID, AnimationMap> ID_TO_TAG = new HashMap<>();

	static {
		for (final AnimationMap tag : AnimationMap.values()) {
			ID_TO_TAG.put(tag.getWar3id(), tag);
		}
	}
}
