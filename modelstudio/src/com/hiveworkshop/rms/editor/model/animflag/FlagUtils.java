package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.War3ID;

public class FlagUtils {
	public static War3ID getWar3ID(String name, TimelineContainer container) {
		AnimationMap id = getAnimationMap(name, container);

		if (id == null) {
			String type = "" + container;
			if (container instanceof IdObject) {
				type += " (" + ((IdObject) container).getName() + ")";
			}
			throw new RuntimeException("Got an unknown timeline name: " + name + " for " + type);
		}

		return id.getWar3id();
	}

	public static AnimationMap getAnimationMap(String name, TimelineContainer container) {
		if (container instanceof Layer) {
			return switch (name) {
				case MdlUtils.TOKEN_TEXTURE_ID -> AnimationMap.KMTF;
				case MdlUtils.TOKEN_ALPHA -> AnimationMap.KMTA;
				case MdlUtils.TOKEN_EMISSIVE_GAIN -> AnimationMap.KMTE;
				case MdlUtils.TOKEN_EMISSIVE -> AnimationMap.KMTE;
				case MdlUtils.TOKEN_FRESNEL_COLOR -> AnimationMap.KFC3;
				case MdlUtils.TOKEN_FRESNEL_OPACITY -> AnimationMap.KFCA;
				case MdlUtils.TOKEN_FRESNEL_TEAM_COLOR -> AnimationMap.KFTC;
				default -> null;
			};
		} else if (container instanceof TextureAnim) {
			return switch (name) {
				case MdlUtils.TOKEN_TRANSLATION -> AnimationMap.KTAT;
				case MdlUtils.TOKEN_ROTATION -> AnimationMap.KTAR;
				case MdlUtils.TOKEN_SCALING -> AnimationMap.KTAS;
				default -> null;
			};
		} else if (container instanceof GeosetAnim) {
			return switch (name) {
				case MdlUtils.TOKEN_ALPHA -> AnimationMap.KGAO;
				case MdlUtils.TOKEN_COLOR -> AnimationMap.KGAC;
				default -> null;
			};
		} else if (container instanceof Light) {
			return switch (name) {
				case MdlUtils.TOKEN_ATTENUATION_START -> AnimationMap.KLAS;
				case MdlUtils.TOKEN_ATTENUATION_END -> AnimationMap.KLAE;
				case MdlUtils.TOKEN_COLOR -> AnimationMap.KLAC;
				case MdlUtils.TOKEN_INTENSITY -> AnimationMap.KLAI;
				case MdlUtils.TOKEN_AMB_INTENSITY -> AnimationMap.KLBI;
				case MdlUtils.TOKEN_AMB_COLOR -> AnimationMap.KLBC;
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KLAV;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof Attachment) {
			return switch (name) {
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KATV;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof ParticleEmitter) {
			return switch (name) {
				case MdlUtils.TOKEN_EMISSION_RATE -> AnimationMap.KPEE;
				case MdlUtils.TOKEN_GRAVITY -> AnimationMap.KPEG;
				case MdlUtils.TOKEN_LONGITUDE -> AnimationMap.KPLN;
				case MdlUtils.TOKEN_LATITUDE -> AnimationMap.KPLT;
				case MdlUtils.TOKEN_LIFE_SPAN -> AnimationMap.KPEL;
				case MdlUtils.TOKEN_INIT_VELOCITY -> AnimationMap.KPES;
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KPEV;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof ParticleEmitter2) {
			return switch (name) {
				case MdlUtils.TOKEN_SPEED -> AnimationMap.KP2S;
				case MdlUtils.TOKEN_VARIATION -> AnimationMap.KP2R;
				case MdlUtils.TOKEN_LATITUDE -> AnimationMap.KP2L;
				case MdlUtils.TOKEN_GRAVITY -> AnimationMap.KP2G;
				case MdlUtils.TOKEN_EMISSION_RATE -> AnimationMap.KP2E;
				case MdlUtils.TOKEN_LENGTH -> AnimationMap.KP2N;
				case MdlUtils.TOKEN_WIDTH -> AnimationMap.KP2W;
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KP2V;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof ParticleEmitterPopcorn) {
			return switch (name) {
				case MdlUtils.TOKEN_ALPHA -> AnimationMap.KPPA;
				case MdlUtils.TOKEN_COLOR -> AnimationMap.KPPC;
				case MdlUtils.TOKEN_EMISSION_RATE -> AnimationMap.KPPE;
				case MdlUtils.TOKEN_LIFE_SPAN -> AnimationMap.KPPL;
				case MdlUtils.TOKEN_SPEED -> AnimationMap.KPPS;
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KPPV;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof RibbonEmitter) {
			return switch (name) {
				case MdlUtils.TOKEN_HEIGHT_ABOVE -> AnimationMap.KRHA;
				case MdlUtils.TOKEN_HEIGHT_BELOW -> AnimationMap.KRHB;
				case MdlUtils.TOKEN_ALPHA -> AnimationMap.KRAL;
				case MdlUtils.TOKEN_COLOR -> AnimationMap.KRCO;
				case MdlUtils.TOKEN_TEXTURE_SLOT -> AnimationMap.KRTX;
				case MdlUtils.TOKEN_VISIBILITY -> AnimationMap.KRVS;
				default -> getIdObjectAnimationMap(name);
			};
		} else if (container instanceof Camera.SourceNode) {
			return switch (name) {
				case MdlUtils.TOKEN_TRANSLATION -> AnimationMap.KCTR;
				case MdlUtils.TOKEN_ROTATION -> AnimationMap.KCRL;
				default -> null;
			};
		} else if (container instanceof Camera.TargetNode) {
			return switch (name) {
				case MdlUtils.TOKEN_TRANSLATION -> AnimationMap.KTTR;
				default -> null;
			};
		}

		if (container instanceof IdObject) {
			return getIdObjectAnimationMap(name);
		}

		return null;
	}

	public static AnimationMap getIdObjectAnimationMap(String name) {
		return switch (name) {
			case MdlUtils.TOKEN_TRANSLATION -> AnimationMap.KGTR;
			case MdlUtils.TOKEN_ROTATION -> AnimationMap.KGRT;
			case MdlUtils.TOKEN_SCALING -> AnimationMap.KGSC;
			default -> null;
		};
	}
}
