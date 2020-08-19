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
	KMTF(MdlUtils.TOKEN_TEXTURE_ID, MdlxTimelineDescriptor.UINT32_TIMELINE),
	KMTA(MdlUtils.TOKEN_ALPHA, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KMTE("EmissiveGain", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KFC3("FresnelColor", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KFCA("FresnelOpacity", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KFTC("FresnelTeamColor", MdlxTimelineDescriptor.UINT32_TIMELINE),
	// TextureAnimation
	KTAT(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KTAR(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.VECTOR4_TIMELINE),
	KTAS(MdlUtils.TOKEN_SCALING, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	// GeosetAnimation
	KGAO(MdlUtils.TOKEN_ALPHA, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KGAC(MdlUtils.TOKEN_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	// Light
	KLAS(MdlUtils.TOKEN_ATTENUATION_START, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KLAE(MdlUtils.TOKEN_ATTENUATION_END, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KLAC(MdlUtils.TOKEN_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KLAI(MdlUtils.TOKEN_INTENSITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KLBI(MdlUtils.TOKEN_AMB_INTENSITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KLBC(MdlUtils.TOKEN_AMB_COLOR, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KLAV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// Attachment
	KATV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// ParticleEmitter
	KPEE(MdlUtils.TOKEN_EMISSION_RATE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPEG(MdlUtils.TOKEN_GRAVITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPLN(MdlUtils.TOKEN_LONGITUDE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPLT(MdlUtils.TOKEN_LATITUDE, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPEL(MdlUtils.TOKEN_LIFE_SPAN, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPES(MdlUtils.TOKEN_INIT_VELOCITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KPEV(MdlUtils.TOKEN_VISIBILITY, MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// ParticleEmitter2
	KP2S("Speed", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2R("Variation", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2L("Latitude", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2G("Gravity", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2E("EmissionRate", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2N("Length", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2W("Width", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KP2V("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 // ParticleEmitterCorn
	 KPPA("Alpha", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 KPPC("Color", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	 KPPE("EmissionRate", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 KPPL("LifeSpan", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 KPPS("Speed", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	 KPPV("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// RibbonEmitter
	KRHA("HeightAbove", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KRHB("HeightBelow", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KRAL("Alpha", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	KRCO("Color", MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KRTX("TextureSlot", MdlxTimelineDescriptor.UINT32_TIMELINE),
	KRVS("Visibility", MdlxTimelineDescriptor.FLOAT_TIMELINE),
	// Camera
	KCTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KTTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KCRL(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.UINT32_TIMELINE),
	// GenericObject
	KGTR(MdlUtils.TOKEN_TRANSLATION, MdlxTimelineDescriptor.VECTOR3_TIMELINE),
	KGRT(MdlUtils.TOKEN_ROTATION, MdlxTimelineDescriptor.VECTOR4_TIMELINE),
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
