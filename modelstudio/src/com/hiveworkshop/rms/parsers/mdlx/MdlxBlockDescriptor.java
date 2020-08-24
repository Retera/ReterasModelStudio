package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.util.Descriptor;

public interface MdlxBlockDescriptor<E> extends Descriptor<E> {

	MdlxBlockDescriptor<MdlxAttachment> ATTACHMENT = new MdlxBlockDescriptor<MdlxAttachment>() {
		@Override
		public MdlxAttachment create() {
			return new MdlxAttachment();
		}
	};

	MdlxBlockDescriptor<MdlxBone> BONE = new MdlxBlockDescriptor<MdlxBone>() {
		@Override
		public MdlxBone create() {
			return new MdlxBone();
		}
	};

	MdlxBlockDescriptor<MdlxCamera> CAMERA = new MdlxBlockDescriptor<MdlxCamera>() {
		@Override
		public MdlxCamera create() {
			return new MdlxCamera();
		}
	};

	MdlxBlockDescriptor<MdlxCollisionShape> COLLISION_SHAPE = new MdlxBlockDescriptor<MdlxCollisionShape>() {
		@Override
		public MdlxCollisionShape create() {
			return new MdlxCollisionShape();
		}
	};

	MdlxBlockDescriptor<MdlxEventObject> EVENT_OBJECT = new MdlxBlockDescriptor<MdlxEventObject>() {
		@Override
		public MdlxEventObject create() {
			return new MdlxEventObject();
		}
	};

	MdlxBlockDescriptor<MdlxGeoset> GEOSET = new MdlxBlockDescriptor<MdlxGeoset>() {
		@Override
		public MdlxGeoset create() {
			return new MdlxGeoset();
		}
	};

	MdlxBlockDescriptor<MdlxGeosetAnimation> GEOSET_ANIMATION = new MdlxBlockDescriptor<MdlxGeosetAnimation>() {
		@Override
		public MdlxGeosetAnimation create() {
			return new MdlxGeosetAnimation();
		}
	};

	MdlxBlockDescriptor<MdlxHelper> HELPER = new MdlxBlockDescriptor<MdlxHelper>() {
		@Override
		public MdlxHelper create() {
			return new MdlxHelper();
		}
	};

	MdlxBlockDescriptor<MdlxLight> LIGHT = new MdlxBlockDescriptor<MdlxLight>() {
		@Override
		public MdlxLight create() {
			return new MdlxLight();
		}
	};

	MdlxBlockDescriptor<MdlxLayer> LAYER = new MdlxBlockDescriptor<MdlxLayer>() {
		@Override
		public MdlxLayer create() {
			return new MdlxLayer();
		}
	};

	MdlxBlockDescriptor<MdlxMaterial> MATERIAL = new MdlxBlockDescriptor<MdlxMaterial>() {
		@Override
		public MdlxMaterial create() {
			return new MdlxMaterial();
		}
	};

	MdlxBlockDescriptor<MdlxParticleEmitter> PARTICLE_EMITTER = new MdlxBlockDescriptor<MdlxParticleEmitter>() {
		@Override
		public MdlxParticleEmitter create() {
			return new MdlxParticleEmitter();
		}
	};

	MdlxBlockDescriptor<MdlxParticleEmitter2> PARTICLE_EMITTER2 = new MdlxBlockDescriptor<MdlxParticleEmitter2>() {
		@Override
		public MdlxParticleEmitter2 create() {
			return new MdlxParticleEmitter2();
		}
	};

	MdlxBlockDescriptor<MdlxParticleEmitterPopcorn> PARTICLE_EMITTER_POPCORN = new MdlxBlockDescriptor<MdlxParticleEmitterPopcorn>() {
		@Override
		public MdlxParticleEmitterPopcorn create() {
			return new MdlxParticleEmitterPopcorn();
		}
	};

	MdlxBlockDescriptor<MdlxRibbonEmitter> RIBBON_EMITTER = new MdlxBlockDescriptor<MdlxRibbonEmitter>() {
		@Override
		public MdlxRibbonEmitter create() {
			return new MdlxRibbonEmitter();
		}
	};

	MdlxBlockDescriptor<MdlxSequence> SEQUENCE = new MdlxBlockDescriptor<MdlxSequence>() {
		@Override
		public MdlxSequence create() {
			return new MdlxSequence();
		}
	};

	MdlxBlockDescriptor<MdlxTexture> TEXTURE = new MdlxBlockDescriptor<MdlxTexture>() {
		@Override
		public MdlxTexture create() {
			return new MdlxTexture();
		}
	};

	MdlxBlockDescriptor<MdlxTextureAnimation> TEXTURE_ANIMATION = new MdlxBlockDescriptor<MdlxTextureAnimation>() {
		@Override
		public MdlxTextureAnimation create() {
			return new MdlxTextureAnimation();
		}
	};

	MdlxBlockDescriptor<MdlxFaceEffect> FACE_EFFECT = new MdlxBlockDescriptor<MdlxFaceEffect>() {
		@Override
		public MdlxFaceEffect create() {
			return new MdlxFaceEffect();
		}
	};
}
