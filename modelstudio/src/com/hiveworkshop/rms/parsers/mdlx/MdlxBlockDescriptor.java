package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.util.Descriptor;

public interface MdlxBlockDescriptor<E> extends Descriptor<E> {

	MdlxBlockDescriptor<MdlxAttachment> ATTACHMENT = () -> new MdlxAttachment();

	MdlxBlockDescriptor<MdlxBone> BONE = () -> new MdlxBone();

	MdlxBlockDescriptor<MdlxCamera> CAMERA = () -> new MdlxCamera();

	MdlxBlockDescriptor<MdlxCollisionShape> COLLISION_SHAPE = () -> new MdlxCollisionShape();

	MdlxBlockDescriptor<MdlxEventObject> EVENT_OBJECT = () -> new MdlxEventObject();

	MdlxBlockDescriptor<MdlxGeoset> GEOSET = () -> new MdlxGeoset();

	MdlxBlockDescriptor<MdlxGeosetAnimation> GEOSET_ANIMATION = () -> new MdlxGeosetAnimation();

	MdlxBlockDescriptor<MdlxHelper> HELPER = () -> new MdlxHelper();

	MdlxBlockDescriptor<MdlxLight> LIGHT = () -> new MdlxLight();

	MdlxBlockDescriptor<MdlxLayer> LAYER = () -> new MdlxLayer();

	MdlxBlockDescriptor<MdlxMaterial> MATERIAL = () -> new MdlxMaterial();

	MdlxBlockDescriptor<MdlxParticleEmitter> PARTICLE_EMITTER = () -> new MdlxParticleEmitter();

	MdlxBlockDescriptor<MdlxParticleEmitter2> PARTICLE_EMITTER2 = () -> new MdlxParticleEmitter2();

	MdlxBlockDescriptor<MdlxParticleEmitterPopcorn> PARTICLE_EMITTER_POPCORN = () -> new MdlxParticleEmitterPopcorn();

	MdlxBlockDescriptor<MdlxRibbonEmitter> RIBBON_EMITTER = () -> new MdlxRibbonEmitter();

	MdlxBlockDescriptor<MdlxSequence> SEQUENCE = () -> new MdlxSequence();

	MdlxBlockDescriptor<MdlxTexture> TEXTURE = () -> new MdlxTexture();

	MdlxBlockDescriptor<MdlxTextureAnimation> TEXTURE_ANIMATION = () -> new MdlxTextureAnimation();

	MdlxBlockDescriptor<MdlxFaceEffect> FACE_EFFECT = () -> new MdlxFaceEffect();
}
