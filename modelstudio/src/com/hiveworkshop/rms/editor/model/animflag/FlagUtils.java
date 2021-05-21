package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.War3ID;

public class FlagUtils {
	public static War3ID getWar3ID(String name, TimelineContainer container) {
		AnimationMap id = getAnimationMap(name, container);

		if (id == null) {
			throw new RuntimeException("Got an unknown timeline name: " + name);
		}

		return id.getWar3id();
	}

	public static AnimationMap getAnimationMap(String name, TimelineContainer container) {
		if (container instanceof Layer) {
			switch (name) {
				case MdlUtils.TOKEN_TEXTURE_ID:
					return AnimationMap.KMTF;
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KMTA;
				case MdlUtils.TOKEN_EMISSIVE_GAIN:
					return AnimationMap.KMTE;
				case MdlUtils.TOKEN_EMISSIVE:
					return AnimationMap.KMTE;
				case MdlUtils.TOKEN_FRESNEL_COLOR:
					return AnimationMap.KFC3;
				case MdlUtils.TOKEN_FRESNEL_OPACITY:
					return AnimationMap.KFCA;
				case MdlUtils.TOKEN_FRESNEL_TEAM_COLOR:
					return AnimationMap.KFTC;
			}
		} else if (container instanceof TextureAnim) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KTAT;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KTAR;
				case MdlUtils.TOKEN_SCALING:
					return AnimationMap.KTAS;
			}
		} else if (container instanceof GeosetAnim) {
			switch (name) {
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KGAO;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KGAC;
			}
		} else if (container instanceof Light) {
			switch (name) {
				case MdlUtils.TOKEN_ATTENUATION_START:
					return AnimationMap.KLAS;
				case MdlUtils.TOKEN_ATTENUATION_END:
					return AnimationMap.KLAE;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KLAC;
				case MdlUtils.TOKEN_INTENSITY:
					return AnimationMap.KLAI;
				case MdlUtils.TOKEN_AMB_INTENSITY:
					return AnimationMap.KLBI;
				case MdlUtils.TOKEN_AMB_COLOR:
					return AnimationMap.KLBC;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KLAV;
			}
		} else if (container instanceof Attachment) {
			switch (name) {
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KATV;
			}
		} else if (container instanceof ParticleEmitter) {
			switch (name) {
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KPEE;
				case MdlUtils.TOKEN_GRAVITY:
					return AnimationMap.KPEG;
				case MdlUtils.TOKEN_LONGITUDE:
					return AnimationMap.KPLN;
				case MdlUtils.TOKEN_LATITUDE:
					return AnimationMap.KPLT;
				case MdlUtils.TOKEN_LIFE_SPAN:
					return AnimationMap.KPEL;
				case MdlUtils.TOKEN_INIT_VELOCITY:
					return AnimationMap.KPES;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KPEV;
			}
		} else if (container instanceof ParticleEmitter2) {
			switch (name) {
				case MdlUtils.TOKEN_SPEED:
					return AnimationMap.KP2S;
				case MdlUtils.TOKEN_VARIATION:
					return AnimationMap.KP2R;
				case MdlUtils.TOKEN_LATITUDE:
					return AnimationMap.KP2L;
				case MdlUtils.TOKEN_GRAVITY:
					return AnimationMap.KP2G;
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KP2E;
				case MdlUtils.TOKEN_LENGTH:
					return AnimationMap.KP2N;
				case MdlUtils.TOKEN_WIDTH:
					return AnimationMap.KP2W;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KP2V;
			}
		} else if (container instanceof ParticleEmitterPopcorn) {
			switch (name) {
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KPPA;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KPPC;
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KPPE;
				case MdlUtils.TOKEN_LIFE_SPAN:
					return AnimationMap.KPPL;
				case MdlUtils.TOKEN_SPEED:
					return AnimationMap.KPPS;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KPPV;
			}
		} else if (container instanceof RibbonEmitter) {
			switch (name) {
				case MdlUtils.TOKEN_HEIGHT_ABOVE:
					return AnimationMap.KRHA;
				case MdlUtils.TOKEN_HEIGHT_BELOW:
					return AnimationMap.KRHB;
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KRAL;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KRCO;
				case MdlUtils.TOKEN_TEXTURE_SLOT:
					return AnimationMap.KRTX;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KRVS;
			}
		} else if (container instanceof Camera.SourceNode) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KCTR;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KCRL;
			}
		} else if (container instanceof Camera.TargetNode) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KTTR;
			}
		}

		if (container instanceof IdObject) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KGTR;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KGRT;
				case MdlUtils.TOKEN_SCALING:
					return AnimationMap.KGSC;
			}
		}

		return null;
	}
}
