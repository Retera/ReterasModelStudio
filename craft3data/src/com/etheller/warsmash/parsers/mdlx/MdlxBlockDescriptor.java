package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.util.Descriptor;

public interface MdlxBlockDescriptor<E> extends Descriptor<E> {

	public static final MdlxBlockDescriptor<MdlxAttachment> ATTACHMENT = new MdlxBlockDescriptor<MdlxAttachment>() {
		@Override
		public MdlxAttachment create() {
			return new MdlxAttachment();
		}
	};

	public static final MdlxBlockDescriptor<MdlxBone> BONE = new MdlxBlockDescriptor<MdlxBone>() {
		@Override
		public MdlxBone create() {
			return new MdlxBone();
		}
	};

	public static final MdlxBlockDescriptor<MdlxCamera> CAMERA = new MdlxBlockDescriptor<MdlxCamera>() {
		@Override
		public MdlxCamera create() {
			return new MdlxCamera();
		}
	};

	public static final MdlxBlockDescriptor<MdlxCollisionShape> COLLISION_SHAPE = new MdlxBlockDescriptor<MdlxCollisionShape>() {
		@Override
		public MdlxCollisionShape create() {
			return new MdlxCollisionShape();
		}
	};

	public static final MdlxBlockDescriptor<MdlxEventObject> EVENT_OBJECT = new MdlxBlockDescriptor<MdlxEventObject>() {
		@Override
		public MdlxEventObject create() {
			return new MdlxEventObject();
		}
	};

	public static final MdlxBlockDescriptor<MdlxGeoset> GEOSET = new MdlxBlockDescriptor<MdlxGeoset>() {
		@Override
		public MdlxGeoset create() {
			return new MdlxGeoset();
		}
	};

	public static final MdlxBlockDescriptor<MdlxGeosetAnimation> GEOSET_ANIMATION = new MdlxBlockDescriptor<MdlxGeosetAnimation>() {
		@Override
		public MdlxGeosetAnimation create() {
			return new MdlxGeosetAnimation();
		}
	};

	public static final MdlxBlockDescriptor<MdlxHelper> HELPER = new MdlxBlockDescriptor<MdlxHelper>() {
		@Override
		public MdlxHelper create() {
			return new MdlxHelper();
		}
	};

	public static final MdlxBlockDescriptor<MdlxLight> LIGHT = new MdlxBlockDescriptor<MdlxLight>() {
		@Override
		public MdlxLight create() {
			return new MdlxLight();
		}
	};

	public static final MdlxBlockDescriptor<MdlxLayer> LAYER = new MdlxBlockDescriptor<MdlxLayer>() {
		@Override
		public MdlxLayer create() {
			return new MdlxLayer();
		}
	};

	public static final MdlxBlockDescriptor<MdlxMaterial> MATERIAL = new MdlxBlockDescriptor<MdlxMaterial>() {
		@Override
		public MdlxMaterial create() {
			return new MdlxMaterial();
		}
	};

	public static final MdlxBlockDescriptor<MdlxParticleEmitter> PARTICLE_EMITTER = new MdlxBlockDescriptor<MdlxParticleEmitter>() {
		@Override
		public MdlxParticleEmitter create() {
			return new MdlxParticleEmitter();
		}
	};

	public static final MdlxBlockDescriptor<MdlxParticleEmitter2> PARTICLE_EMITTER2 = new MdlxBlockDescriptor<MdlxParticleEmitter2>() {
		@Override
		public MdlxParticleEmitter2 create() {
			return new MdlxParticleEmitter2();
		}
	};

	public static final MdlxBlockDescriptor<MdlxParticleEmitterPopcorn> PARTICLE_EMITTER_POPCORN = new MdlxBlockDescriptor<MdlxParticleEmitterPopcorn>() {
		@Override
		public MdlxParticleEmitterPopcorn create() {
			return new MdlxParticleEmitterPopcorn();
		}
	};

	public static final MdlxBlockDescriptor<MdlxRibbonEmitter> RIBBON_EMITTER = new MdlxBlockDescriptor<MdlxRibbonEmitter>() {
		@Override
		public MdlxRibbonEmitter create() {
			return new MdlxRibbonEmitter();
		}
	};

	public static final MdlxBlockDescriptor<MdlxSequence> SEQUENCE = new MdlxBlockDescriptor<MdlxSequence>() {
		@Override
		public MdlxSequence create() {
			return new MdlxSequence();
		}
	};

	public static final MdlxBlockDescriptor<MdlxTexture> TEXTURE = new MdlxBlockDescriptor<MdlxTexture>() {
		@Override
		public MdlxTexture create() {
			return new MdlxTexture();
		}
	};

	public static final MdlxBlockDescriptor<MdlxTextureAnimation> TEXTURE_ANIMATION = new MdlxBlockDescriptor<MdlxTextureAnimation>() {
		@Override
		public MdlxTextureAnimation create() {
			return new MdlxTextureAnimation();
		}
	};
}
