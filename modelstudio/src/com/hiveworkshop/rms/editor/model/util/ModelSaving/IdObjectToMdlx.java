package com.hiveworkshop.rms.editor.model.util.ModelSaving;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class IdObjectToMdlx {

	public static MdlxBone toMdlx(Bone bone, EditableModel model) {
		MdlxBone mdlxBone = new MdlxBone();

		objectToMdlx(bone, mdlxBone, model);

		mdlxBone.geosetId = model.getGeosets().indexOf(bone.getGeoset());
		mdlxBone.geosetAnimationId = model.getGeosetAnims().indexOf(bone.getGeosetAnim());

		return mdlxBone;
	}

	public static MdlxHelper toMdlxHelper(Helper helper, EditableModel model) {
		final MdlxHelper mdlxHelper = new MdlxHelper();

		objectToMdlx(helper, mdlxHelper, model);

		return mdlxHelper;
	}

	public static MdlxAttachment toMdlx(Attachment attachment, EditableModel model) {
		MdlxAttachment mdlxAttachment = new MdlxAttachment();

		objectToMdlx(attachment, mdlxAttachment, model);

		mdlxAttachment.attachmentId = attachment.getAttachmentID(model);

		if (attachment.getPath() != null) {
			mdlxAttachment.path = attachment.getPath();
		}

		return mdlxAttachment;
	}

	public static MdlxCollisionShape toMdlx(CollisionShape collisionShape, EditableModel model) {
		MdlxCollisionShape mdlxShape = new MdlxCollisionShape();

		objectToMdlx(collisionShape, mdlxShape, model);

		mdlxShape.type = collisionShape.getType();

		mdlxShape.vertices[0] = collisionShape.getVertex(0).toFloatArray();

		if (mdlxShape.type != MdlxCollisionShape.Type.SPHERE) {
			mdlxShape.vertices[1] = collisionShape.getVertex(1).toFloatArray();
		}

		if (mdlxShape.type == MdlxCollisionShape.Type.SPHERE || mdlxShape.type == MdlxCollisionShape.Type.CYLINDER) {
			mdlxShape.boundsRadius = (float) collisionShape.getBoundsRadius();
		}

		return mdlxShape;
	}

	public static MdlxEventObject toMdlx(EventObject eventObject, EditableModel model) {
		final MdlxEventObject object = new MdlxEventObject();

		objectToMdlx(eventObject, object, model);

		if (eventObject.hasGlobalSeq()) {
			object.globalSequenceId = eventObject.getGlobalSeqId(model);
		}

		List<Integer> keyframes = new ArrayList<>();

		for (Sequence sequence : model.getAllSequences()) {
			TreeSet<Integer> tracks = eventObject.getEventTrack(sequence);
			if (tracks != null) {
				for (int track : tracks) {
					if (track > sequence.getLength()) {
						break;
					}
					keyframes.add(track + sequence.getStart());
				}
			}
		}

//		object.keyFrames = new long[eventObject.size()];
		object.keyFrames = new long[Math.max(keyframes.size(), 1)];

		for (int i = 0; i < keyframes.size(); i++) {
			object.keyFrames[i] = keyframes.get(i).longValue();
		}

		if(keyframes.size() == 0){
			object.keyFrames[0] = 0;
		}

		return object;
	}

	public static MdlxLight toMdlx(Light light, EditableModel model) {
		final MdlxLight mdlxLight = new MdlxLight();

		objectToMdlx(light, mdlxLight, model);

		mdlxLight.type = light.getType();
		mdlxLight.attenuation[0] = light.getAttenuationStart();
		mdlxLight.attenuation[1] = light.getAttenuationEnd();
//		mdlxLight.color = ModelUtils.flipRGBtoBGR(light.getStaticColor().toFloatArray());
		mdlxLight.color = light.getStaticColor().toFloatArray();
		mdlxLight.intensity = (float) light.getIntensity();
//		mdlxLight.ambientColor = ModelUtils.flipRGBtoBGR(light.getStaticAmbColor().toFloatArray());
		mdlxLight.ambientColor = light.getStaticAmbColor().toFloatArray();
		mdlxLight.ambientIntensity = (float) light.getAmbIntensity();

		return mdlxLight;
	}

	public static MdlxParticleEmitter toMdlx(ParticleEmitter particleEmitter, EditableModel model) {
		MdlxParticleEmitter emitter = new MdlxParticleEmitter();

		objectToMdlx(particleEmitter, emitter, model);

		emitter.emissionRate = (float) particleEmitter.getEmissionRate();
		emitter.gravity = (float) particleEmitter.getGravity();
		emitter.speed = (float) particleEmitter.getInitVelocity();
		emitter.latitude = (float) particleEmitter.getLatitude();
		emitter.lifeSpan = (float) particleEmitter.getLifeSpan();
		emitter.longitude = (float) particleEmitter.getLongitude();
		emitter.path = particleEmitter.getPath();

		if (particleEmitter.isMDLEmitter()) {
			emitter.flags |= 0x8000;
		} else {
			emitter.flags |= 0x10000;
		}

		return emitter;
	}

	public static MdlxParticleEmitter2 toMdlx(ParticleEmitter2 particleEmitter2, EditableModel model) {
		MdlxParticleEmitter2 mdlxEmitter = new MdlxParticleEmitter2();

		objectToMdlx(particleEmitter2, mdlxEmitter, model);

		if (particleEmitter2.getUnshaded()) {
			mdlxEmitter.flags |= 0x8000;
		}
		if (particleEmitter2.getSortPrimsFarZ()) {
			mdlxEmitter.flags |= 0x10000;
		}
		if (particleEmitter2.getLineEmitter()) {
			mdlxEmitter.flags |= 0x20000;
		}
		if (particleEmitter2.getUnfogged()) {
			mdlxEmitter.flags |= 0x40000;
		}
		if (particleEmitter2.getModelSpace()) {
			mdlxEmitter.flags |= 0x80000;
		}
		if (particleEmitter2.getXYQuad()) {
			mdlxEmitter.flags |= 0x100000;
		}
		if (particleEmitter2.getSquirt()) {
			mdlxEmitter.squirt = 1;
		}

		mdlxEmitter.filterMode = particleEmitter2.getFilterMode();
		mdlxEmitter.headOrTail = particleEmitter2.getHeadOrTail();

		mdlxEmitter.speed = (float) particleEmitter2.getSpeed();
		mdlxEmitter.variation = (float) particleEmitter2.getVariation();
		mdlxEmitter.latitude = (float) particleEmitter2.getLatitude();
		mdlxEmitter.gravity = (float) particleEmitter2.getGravity();
		mdlxEmitter.lifeSpan = (float) particleEmitter2.getLifeSpan();
		mdlxEmitter.emissionRate = (float) particleEmitter2.getEmissionRate();
		mdlxEmitter.length = (float) particleEmitter2.getLength();
		mdlxEmitter.width = (float) particleEmitter2.getWidth();
		mdlxEmitter.rows = particleEmitter2.getRows();
		mdlxEmitter.columns = particleEmitter2.getCols();
		mdlxEmitter.tailLength = (float) particleEmitter2.getTailLength();
		mdlxEmitter.timeMiddle = (float) particleEmitter2.getTime();

		mdlxEmitter.segmentColors[0] = particleEmitter2.getSegmentColor(0).toFloatArray();
		mdlxEmitter.segmentColors[1] = particleEmitter2.getSegmentColor(1).toFloatArray();
		mdlxEmitter.segmentColors[2] = particleEmitter2.getSegmentColor(2).toFloatArray();
//		mdlxEmitter.segmentColors[0] = ModelUtils.flipRGBtoBGR(getSegmentColor(0).toFloatArray());
//		mdlxEmitter.segmentColors[1] = ModelUtils.flipRGBtoBGR(getSegmentColor(1).toFloatArray());
//		mdlxEmitter.segmentColors[2] = ModelUtils.flipRGBtoBGR(getSegmentColor(2).toFloatArray());

		mdlxEmitter.segmentAlphas = particleEmitter2.getAlpha().toShortArray();
		mdlxEmitter.segmentScaling = particleEmitter2.getParticleScaling().toFloatArray();

		mdlxEmitter.headIntervals[0] = particleEmitter2.getHeadUVAnim().toLongArray();
		mdlxEmitter.headIntervals[1] = particleEmitter2.getHeadDecayUVAnim().toLongArray();
		mdlxEmitter.tailIntervals[0] = particleEmitter2.getTailUVAnim().toLongArray();
		mdlxEmitter.tailIntervals[1] = particleEmitter2.getTailDecayUVAnim().toLongArray();

		mdlxEmitter.textureId = model.getTextureId(particleEmitter2.getTexture());
		mdlxEmitter.priorityPlane = particleEmitter2.getPriorityPlane();
		mdlxEmitter.replaceableId = particleEmitter2.getReplaceableId();

		return mdlxEmitter;
	}

	public static MdlxParticleEmitterPopcorn toMdlx(ParticleEmitterPopcorn particleEmitterPopcorn, EditableModel model) {
		MdlxParticleEmitterPopcorn mdlxEmitter = new MdlxParticleEmitterPopcorn();

		objectToMdlx(particleEmitterPopcorn, mdlxEmitter, model);

		mdlxEmitter.lifeSpan = particleEmitterPopcorn.getLifeSpan();
		mdlxEmitter.emissionRate = particleEmitterPopcorn.getEmissionRate();
		mdlxEmitter.speed = particleEmitterPopcorn.getInitVelocity();
		mdlxEmitter.color = particleEmitterPopcorn.getColor().toFloatArray();
//		mdlxEmitter.color = ModelUtils.flipRGBtoBGR(color.toFloatArray());
		mdlxEmitter.alpha = particleEmitterPopcorn.getAlpha();
		mdlxEmitter.replaceableId = particleEmitterPopcorn.getReplaceableId();
		mdlxEmitter.path = particleEmitterPopcorn.getPath();
//		mdlxEmitter.animationVisiblityGuide = animVisibilityGuide;
		mdlxEmitter.animationVisiblityGuide = particleEmitterPopcorn.getAnimVisibilityGuide();

		return mdlxEmitter;
	}

	public static MdlxRibbonEmitter toMdlx(RibbonEmitter ribbonEmitter, EditableModel model) {
		final MdlxRibbonEmitter mdlxEmitter = new MdlxRibbonEmitter();

		objectToMdlx(ribbonEmitter, mdlxEmitter, model);

		mdlxEmitter.textureSlot = (long) ribbonEmitter.getTextureSlot();
		mdlxEmitter.heightAbove = (float) ribbonEmitter.getHeightAbove();
		mdlxEmitter.heightBelow = (float) ribbonEmitter.getHeightBelow();
		mdlxEmitter.alpha = (float) ribbonEmitter.getAlpha();
//		mdlxEmitter.color = ModelUtils.flipRGBtoBGR(ribbonEmitter.getStaticColor().toFloatArray());
		mdlxEmitter.color = ribbonEmitter.getStaticColor().toFloatArray();
		mdlxEmitter.lifeSpan = (float) ribbonEmitter.getLifeSpan();
		mdlxEmitter.emissionRate = ribbonEmitter.getEmissionRate();
		mdlxEmitter.rows = ribbonEmitter.getRows();
		mdlxEmitter.columns = ribbonEmitter.getColumns();
		mdlxEmitter.materialId = model.computeMaterialID(ribbonEmitter.getMaterial());
		mdlxEmitter.gravity = (float) ribbonEmitter.getGravity();

		return mdlxEmitter;
	}

	public static void objectToMdlx(IdObject idObject, MdlxGenericObject mdlxObject, EditableModel model) {
		mdlxObject.name = idObject.getName();
		mdlxObject.objectId = model.getObjectId(idObject);
//		mdlxObject.parentId = getParent() == null ? -1 : model.getObjectId(getParent());
		mdlxObject.parentId = model.getObjectId(idObject.getParent());

		if (idObject.getDontInheritTranslation()) {
			mdlxObject.flags |= 0x1;
		}

		if (idObject.getDontInheritRotation()) {
			mdlxObject.flags |= 0x2;
		}

		if (idObject.getDontInheritScaling()) {
			mdlxObject.flags |= 0x4;
		}

		if (idObject.getBillboarded()) {
			mdlxObject.flags |= 0x8;
		}

		if (idObject.getBillboardLockX()) {
			mdlxObject.flags |= 0x10;
		}

		if (idObject.getBillboardLockY()) {
			mdlxObject.flags |= 0x20;
		}

		if (idObject.getBillboardLockZ()) {
			mdlxObject.flags |= 0x40;
		}

		idObject.timelinesToMdlx(mdlxObject, model);
	}
}
