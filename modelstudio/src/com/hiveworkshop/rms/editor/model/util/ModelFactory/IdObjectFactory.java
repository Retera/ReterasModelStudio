package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.TreeMap;

public class IdObjectFactory {
	public static Bone createBone(MdlxBone mdlxBone, EditableModel model) {
		Bone bone = new Bone();
		warnUnmarkedType(mdlxBone, 0x100);

		loadObject(bone, mdlxBone, model);

		return bone;
	}


	public static void loadObject(IdObject idObject, final MdlxGenericObject object, EditableModel model) {
		idObject.setName(object.name);

		EnumSet<IdObject.NodeFlag> nodeFlags = IdObject.NodeFlag.fromBits(object.flags);
		for (IdObject.NodeFlag flag : nodeFlags) {
			idObject.setFlag(flag, true);
		}

		if (idObject.getDontInheritTranslation()) {
			System.out.println(idObject.getName() + " dontInheritTranslation!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		if (idObject.getDontInheritRotation()) {
			System.out.println(idObject.getName() + " dontInheritRotation!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		if (idObject.getDontInheritScaling()) {
			System.out.println(idObject.getName() + " dontInheritScaling!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}

		idObject.loadTimelines(object, model);
	}

	public static Light createLight(MdlxLight mdlxLight, EditableModel model) {
		Light light = new Light();
		warnUnmarkedType(mdlxLight, 0x200);

		loadObject(light, mdlxLight, model);

		light.setType(mdlxLight.type);
		light.setAttenuationStart(mdlxLight.attenuation[0]);
		light.setAttenuationEnd(mdlxLight.attenuation[1]);
		light.setStaticColor(new Vec3(mdlxLight.color));
		light.setIntensity(mdlxLight.intensity);
		light.setStaticAmbColor(new Vec3(mdlxLight.ambientColor));
		light.setAmbIntensity(mdlxLight.ambientIntensity);
		return light;
	}

	public static Helper createHelper(MdlxHelper mdlxHelper, EditableModel model) {
		Helper helper = new Helper();
		warnUnmarkedType(mdlxHelper, 0x0);
//		if ((mdlxHelper.flags & 0x100) != 0) {
//			System.err.println("MDX -> MDL error: Helper '" + mdlxHelper.name + "' not flagged as Helper in MDX!");
//		}

		loadObject(helper, mdlxHelper, model);
		return helper;
	}

	public static Attachment createAttachment(MdlxAttachment mdlxAttachment, EditableModel model) {
		Attachment attachment = new Attachment();
		warnUnmarkedType(mdlxAttachment, 0x800);

		loadObject(attachment, mdlxAttachment, model);

		attachment.setAttachmentID(mdlxAttachment.attachmentId);
		attachment.setPath(mdlxAttachment.path);
		return attachment;
	}

	public static ParticleEmitter createParticleEmitter(MdlxParticleEmitter mdlxEmitter, EditableModel model) {
		ParticleEmitter particleEmitter = new ParticleEmitter();
		warnUnmarkedType(mdlxEmitter, 0x1000);

		loadObject(particleEmitter, mdlxEmitter, model);

		particleEmitter.setEmissionRate(mdlxEmitter.emissionRate);
		particleEmitter.setGravity(mdlxEmitter.gravity);
		particleEmitter.setInitVelocity(mdlxEmitter.speed);
		particleEmitter.setLatitude(mdlxEmitter.latitude);
		particleEmitter.setLifeSpan(mdlxEmitter.lifeSpan);
		particleEmitter.setLongitude(mdlxEmitter.longitude);
		particleEmitter.setPath(mdlxEmitter.path);

		boolean isMDLEmitter = (mdlxEmitter.flags & 0x8000) == 0x8000;
		particleEmitter.setMDLEmitter(isMDLEmitter);

		if (!isMDLEmitter && (mdlxEmitter.flags & (0x100)) == 0x100) {
			System.err.println("WARNING in MDX -> MDL: ParticleEmitter of unknown type! "
					+ "Defaults to EmitterUsesTGA in my MDL code!");
		}
		return particleEmitter;
	}

	public static ParticleEmitter2 createParticleEmitter2(MdlxParticleEmitter2 mdlxEmitter, EditableModel model) {
		ParticleEmitter2 particleEmitter2 = new ParticleEmitter2();
		warnUnmarkedType(mdlxEmitter, 0x1000);

		loadObject(particleEmitter2, mdlxEmitter, model);
		EnumSet<ParticleEmitter2.P2Flag> p2Flags = ParticleEmitter2.P2Flag.fromBits(mdlxEmitter.flags);
		for (ParticleEmitter2.P2Flag flag : p2Flags) {
			particleEmitter2.setFlag(flag, true);
		}

		particleEmitter2.setSpeed(mdlxEmitter.speed);
		particleEmitter2.setVariation(mdlxEmitter.variation);
		particleEmitter2.setLatitude(mdlxEmitter.latitude);
		particleEmitter2.setGravity(mdlxEmitter.gravity);
		particleEmitter2.setLifeSpan(mdlxEmitter.lifeSpan);
		particleEmitter2.setEmissionRate(mdlxEmitter.emissionRate);
		particleEmitter2.setLength(mdlxEmitter.length);
		particleEmitter2.setWidth(mdlxEmitter.width);
		particleEmitter2.setFilterMode(mdlxEmitter.filterMode);

		particleEmitter2.setRows((int) mdlxEmitter.rows);
		particleEmitter2.setColumns((int) mdlxEmitter.columns);

		particleEmitter2.setHead((mdlxEmitter.headTailFlag+1 & 0x1) != 0);
		particleEmitter2.setTail((mdlxEmitter.headTailFlag+1 & 0x2) != 0);

		particleEmitter2.setTailLength(mdlxEmitter.tailLength);
		particleEmitter2.setTime(mdlxEmitter.timeMiddle);


		float[][] colors = mdlxEmitter.segmentColors;
		// SegmentColor - Inverse order for MDL!
		for (int i = 0; i < 3; i++) {
			particleEmitter2.setSegmentColor(i, colors[i]);
//			setSegmentColor(i, new Vec3(ModelUtils.flipRGBtoBGR(colors[i])));
		}
		Vec3 temp = new Vec3();

		short[] alphas = mdlxEmitter.segmentAlphas;
		particleEmitter2.setAlpha(temp.set(alphas[0], alphas[1], alphas[2]));
		particleEmitter2.setParticleScaling(temp.set(mdlxEmitter.segmentScaling));

		long[][] head = mdlxEmitter.headIntervals;
		long[][] tail = mdlxEmitter.tailIntervals;

		particleEmitter2.setHeadUVAnim(temp.set(head[0][0], head[0][1], head[0][2]));
		particleEmitter2.setHeadDecayUVAnim(temp.set(head[1][0], head[1][1], head[1][2]));
		particleEmitter2.setTailUVAnim(temp.set(tail[0][0], tail[0][1], tail[0][2]));
		particleEmitter2.setTailDecayUVAnim(temp.set(tail[1][0], tail[1][1], tail[1][2]));

		particleEmitter2.setTexture(model.getTexture(mdlxEmitter.textureId));

		particleEmitter2.setSquirt(mdlxEmitter.squirt == 1);

		particleEmitter2.setPriorityPlane(mdlxEmitter.priorityPlane);
		particleEmitter2.setReplaceableId((int) mdlxEmitter.replaceableId);

		return particleEmitter2;
	}

	public static ParticleEmitterPopcorn createParticleEmitterPopcorn(MdlxParticleEmitterPopcorn mdlxEmitter, EditableModel model) {
		ParticleEmitterPopcorn particleEmitterPopcorn = new ParticleEmitterPopcorn();
		loadObject(particleEmitterPopcorn, mdlxEmitter, model);

		particleEmitterPopcorn.setLifeSpan(mdlxEmitter.lifeSpan);
		particleEmitterPopcorn.setEmissionRate(mdlxEmitter.emissionRate);
		particleEmitterPopcorn.setInitVelocity(mdlxEmitter.speed);
		System.out.println("emitter color: " + Arrays.toString(mdlxEmitter.color));
		particleEmitterPopcorn.setColor(new Vec3(mdlxEmitter.color));
//		color = new Vec3(ModelUtils.flipRGBtoBGR(emitter.color));
		particleEmitterPopcorn.setAlpha(mdlxEmitter.alpha);
		particleEmitterPopcorn.setReplaceableId(mdlxEmitter.replaceableId);
		particleEmitterPopcorn.setPath(mdlxEmitter.path);
		particleEmitterPopcorn.setAnimVisibilityGuide(mdlxEmitter.animationVisiblityGuide);
		System.out.println("VisGuid: " + mdlxEmitter.animationVisiblityGuide);
		particleEmitterPopcorn.initAnimsVisStates(model.getAnims());
		return particleEmitterPopcorn;
	}

	public static RibbonEmitter createRibbonEmitter(MdlxRibbonEmitter mdlxEmitter, ModelInfoHolder infoHolder, EditableModel model) {
		RibbonEmitter ribbonEmitter = new RibbonEmitter();
		warnUnmarkedType(mdlxEmitter, 0x4000);

		loadObject(ribbonEmitter, mdlxEmitter, model);

		ribbonEmitter.setTextureSlot((int) mdlxEmitter.textureSlot);
		ribbonEmitter.setHeightAbove(mdlxEmitter.heightAbove);
		ribbonEmitter.setHeightBelow(mdlxEmitter.heightBelow);
		ribbonEmitter.setAlpha(mdlxEmitter.alpha);
//		ribbonEmitter.setStaticColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxEmitter.color)));
		ribbonEmitter.setStaticColor(new Vec3(mdlxEmitter.color));
		ribbonEmitter.setLifeSpan(mdlxEmitter.lifeSpan);
		ribbonEmitter.setEmissionRate((int) mdlxEmitter.emissionRate);
		ribbonEmitter.setRows((int) mdlxEmitter.rows);
		ribbonEmitter.setColumns((int) mdlxEmitter.columns);
		ribbonEmitter.setMaterial(infoHolder.materials.get(mdlxEmitter.materialId));
		ribbonEmitter.setGravity(mdlxEmitter.gravity);

		return ribbonEmitter;
	}

	public static EventObject createEventObject(MdlxEventObject mdlxObject, EditableModel model) {
		EventObject eventObject = new EventObject();
		warnUnmarkedType(mdlxObject, 0x400);

		loadObject(eventObject, mdlxObject, model);

		final int globalSequenceId = mdlxObject.globalSequenceId;
		GlobalSeq globalSeq = model.getGlobalSeq(globalSequenceId);

		if (globalSeq != null) {
			eventObject.setGlobalSeq(globalSeq);
		}

		TreeMap<Integer, Animation> animationTreeMap = new TreeMap<>();
		model.getAnims().forEach(a -> animationTreeMap.put(a.getStart(), a));

		for (final long val : mdlxObject.keyFrames) {
			if (globalSeq != null) {
				eventObject.addTrack(globalSeq, (int) val);
			} else if (animationTreeMap.floorEntry((int) val) != null) {
				Sequence sequence = animationTreeMap.floorEntry((int) val).getValue();
				eventObject.addTrack(sequence, (int) val - sequence.getStart());
			}
		}
		return eventObject;
	}

	public static CollisionShape createCollisionShape(MdlxCollisionShape mdlxShape, EditableModel model) {
		CollisionShape collisionShape = new CollisionShape(mdlxShape.type);
		warnUnmarkedType(mdlxShape, 0x2000);

		loadObject(collisionShape, mdlxShape, model);

		float[][] vertices = mdlxShape.vertices;

		collisionShape.setVertex(0, new Vec3(vertices[0]));

		if (collisionShape.getType() != MdlxCollisionShape.Type.SPHERE) {
			collisionShape.setVertex(1, new Vec3(vertices[1]));
		}

		if (collisionShape.getType() == MdlxCollisionShape.Type.CYLINDER) {
			System.out.println("CYLINDER!!!____________________________________");
		}
		if (collisionShape.getType() == MdlxCollisionShape.Type.PLANE) {
			System.out.println("PLANE!!!____________________________________");
		}

		if (collisionShape.getType() == MdlxCollisionShape.Type.SPHERE || collisionShape.getType() == MdlxCollisionShape.Type.CYLINDER) {
			collisionShape.setBoundsRadius(mdlxShape.boundsRadius);
		}
		return collisionShape;
	}
	private static void warnUnmarkedType(MdlxGenericObject object, int typeFlag) {
		int allFlags = 0x007f00;
		if ((object.flags & allFlags) != typeFlag) {
			String typeName = object.getClass().getSimpleName().substring(4);
			System.err.println("MDX -> MDL error: " + typeName + " '" + object.name + "' (" + object.objectId + ") not flagged as " + typeName + " in MDX!");
		}
	}
}
