package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.*;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TempOpenModelStuff {

	public static EditableModel createEditableModel(MdlxModel mdlxModel) {
		EditableModel model = new EditableModel(mdlxModel.name);
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the MDL class
		ModelInfoHolder infoHolder = new ModelInfoHolder(mdlxModel.version);

		model.setFormatVersion(mdlxModel.version);
		model.setBlendTime((int) mdlxModel.blendTime);
		model.setExtents(new ExtLog(mdlxModel.extent));


		// Step 2: fetch pivot points and bindPose
		for (final float[] point : mdlxModel.pivotPoints) {
			infoHolder.addPivot(new Vec3(point));
		}

		if (mdlxModel.bindPose.size() > 0) {
			infoHolder.setBindPose(mdlxModel.bindPose);
		}


		// Step 3:
		// convert "IdObjects" (as I called them in my high school mdl code) (nodes)

		// Bones
		for (final MdlxBone mdlxBone : mdlxModel.bones) {
//			System.out.println("MdlxBone, id: " + mdlxBone.objectId + " name: " + mdlxBone.name);
			Bone x = IdObjectFactory.createBone(mdlxBone);
			infoHolder.add(mdlxBone, x);
			model.add(x);
		}

		// Lights
		for (final MdlxLight mdlxLight : mdlxModel.lights) {
			Light x = IdObjectFactory.createLight(mdlxLight);
			infoHolder.add(mdlxLight, x);
			model.add(x);
		}

		// Helpers
		for (final MdlxHelper mdlxHelper : mdlxModel.helpers) {
			Helper x = IdObjectFactory.createHelper(mdlxHelper);
			infoHolder.add(mdlxHelper, x);
			model.add(x);
		}

		// Attachment
		for (final MdlxAttachment mdlxAttachment : mdlxModel.attachments) {
			Attachment x = IdObjectFactory.createAttachment(mdlxAttachment);
			infoHolder.add(mdlxAttachment, x);
			model.add(x);
		}

		// ParticleEmitter (number 1 kind)
		for (final MdlxParticleEmitter mdlxEmitter : mdlxModel.particleEmitters) {
			ParticleEmitter x = IdObjectFactory.createParticleEmitter(mdlxEmitter);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// ParticleEmitter2
		for (final MdlxParticleEmitter2 mdlxEmitter : mdlxModel.particleEmitters2) {
			ParticleEmitter2 x = IdObjectFactory.createParticleEmitter2(mdlxEmitter);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// PopcornFxEmitter
		for (final MdlxParticleEmitterPopcorn mdlxEmitter : mdlxModel.particleEmittersPopcorn) {
			ParticleEmitterPopcorn x = IdObjectFactory.createParticleEmitterPopcorn(mdlxEmitter);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// RibbonEmitter
		for (final MdlxRibbonEmitter mdlxEmitter : mdlxModel.ribbonEmitters) {
			RibbonEmitter x = IdObjectFactory.createRibbonEmitter(mdlxEmitter);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// EventObject
		for (final MdlxEventObject mdlxObject : mdlxModel.eventObjects) {
			com.hiveworkshop.rms.editor.model.EventObject x = IdObjectFactory.createEventObject(mdlxObject);
			infoHolder.add(mdlxObject, x);
			model.add(x);
		}


		// CollisionShape
		for (final MdlxCollisionShape mdlxShape : mdlxModel.collisionShapes) {
			CollisionShape x = IdObjectFactory.createCollisionShape(mdlxShape);
			infoHolder.add(mdlxShape, x);
			model.add(x);
		}


		// Step 3: Convert the Sequences
		for (final MdlxSequence sequence : mdlxModel.sequences) {
			model.add(createAnimation(sequence));
		}

		// Step 4: Convert any global sequences
		for (final long sequence : mdlxModel.globalSequences) {
			model.add((int) sequence);
		}

		// Step 5: Convert Texture refs
		for (final MdlxTexture mdlxTexture : mdlxModel.textures) {
			model.add(MaterialFactory.createBitmap(mdlxTexture));
		}

		// Step 6: Convert TVertexAnims
		for (final MdlxTextureAnimation mdlxTextureAnimation : mdlxModel.textureAnimations) {
			model.add(new TextureAnim(mdlxTextureAnimation));
		}

		// Step 7: Convert Material refs
		for (final MdlxMaterial mdlxMaterial : mdlxModel.materials) {
			Material x = MaterialFactory.createMaterial(mdlxMaterial, model);
			infoHolder.add(x);
			model.add(x);
		}

		// Step 8: Geoset
		for (final MdlxGeoset mdlxGeoset : mdlxModel.geosets) {
			Geoset x = GeosetFactory.createGeoset(mdlxGeoset, infoHolder);
			x.setParentModel(model);
			infoHolder.add(x);
			model.add(x);
		}

		// Step 9: GeosetAnims
		for (final MdlxGeosetAnimation mdlxGeosetAnimation : mdlxModel.geosetAnimations) {
			if (mdlxGeosetAnimation.geosetId != -1) {
				GeosetAnim geosetAnim = createGeosetAnim(mdlxGeosetAnimation, infoHolder);

				model.add(geosetAnim);
			}
		}

		for (final MdlxFaceEffect mdlxEffect : mdlxModel.faceEffects) {
			model.addFaceEffect(new FaceEffect(mdlxEffect.type, mdlxEffect.path));
		}

		// Cameras last! Since they have no Id the number of IdObjects + number of Cameras
		// is used to fetch the correct BindPose
		for (final MdlxCamera mdlxCamera : mdlxModel.cameras) {
			Camera x = new Camera(mdlxCamera);
			model.add(x);
			infoHolder.add(x);
		}

		infoHolder.fixIdObjectParents();

		doPostRead(model); // fixes all the things

		return model;
	}

	public static void doPostRead(EditableModel model) {
		System.out.println("doPostRead for model: " + model.getName());
		updateBoneGeosetReferences(model);
//		for (Geoset geo : model.getGeosets()) {
//			GeosetFactory.updateToObjects(geo, model);
//		}
		removeGeosetAnimsWOGeoset(model);

		final List<AnimFlag<?>> animFlags = model.getAllAnimFlags();// laggggg!
		for (final AnimFlag<?> af : animFlags) {
			af.updateGlobalSeqRef(model);
			if (!af.getName().equals("Scaling")
					&& !af.getName().equals("Translation")
					&& !af.getName().equals("Rotation")) {
			}
		}

		final List<com.hiveworkshop.rms.editor.model.EventObject> evtObjs = model.getEvents();
		for (final com.hiveworkshop.rms.editor.model.EventObject af : evtObjs) {
			af.updateGlobalSeqRef(model);
		}

		for (final ParticleEmitter2 temp : model.getParticleEmitter2s()) {
			temp.updateTextureRef(model.getTextures());
		}

		for (final RibbonEmitter emitter : model.getRibbonEmitters()) {
			emitter.updateMaterialRef(model.getMaterials());
		}
		for (ParticleEmitterPopcorn popcorn : model.getPopcornEmitters()) {
			popcorn.initAnimsVisStates(model.getAnims());
		}
	}

	private static void removeGeosetAnimsWOGeoset(EditableModel model) {
		List<GeosetAnim> badAnims = new ArrayList<>();
		for (GeosetAnim geoAnim : model.getGeosetAnims()) {
			if (geoAnim.getGeoset() == null) {
				badAnims.add(geoAnim);
			}
		}
		if (!badAnims.isEmpty()) {
			JOptionPane.showMessageDialog(null, "We discovered GeosetAnim data pointing to an invalid GeosetID! Bad data will be deleted. Please backup your model file.");
		}
		for (final GeosetAnim bad : badAnims) {
			model.remove(bad);
		}
	}

	public static void updateBoneGeosetReferences(EditableModel model) {
		final List<Bone> bones = model.getBones();
		final List<? extends Bone> helpers = model.getHelpers();
		bones.addAll(helpers);
		for (final Bone b : bones) {
			if ((b.getGeosetId() != -1) && (b.getGeosetId() < model.getGeosets().size())) {
				b.setGeoset(model.getGeoset(b.getGeosetId()));
			}
			if ((b.getGeosetAnimId() != -1) && (b.getGeosetAnimId() < model.getGeosetAnims().size())) {
				b.setGeosetAnim(model.getGeosetAnim(b.getGeosetAnimId()));
			}
		}
//		List<Camera> cameras = model.getCameras();
//		for (int i = 0; i < cameras.size(); i++) {
//			final Camera camera = cameras.get(i);
//			if (model.getBindPoseChunk() != null) {
//				camera.setBindPose(model.getBindPoseChunk().bindPose[i + model.getAllObjects().size()]);
//			}
//		}
	}

	public static Animation createAnimation(MdlxSequence sequence) {
		long[] interval = sequence.interval;

		Animation animation = new Animation(sequence.name, (int) interval[0], (int) interval[1]);

		animation.setExtents(new ExtLog(sequence.extent));
		animation.setMoveSpeed(sequence.moveSpeed);

		if (sequence.flags == 1) {
			animation.setNonLooping(true);
		}

		animation.setRarity(sequence.rarity);
		return animation;
	}

	public static GeosetAnim createGeosetAnim(MdlxGeosetAnimation mdlxAnimation, ModelInfoHolder infoHolder) {
		Geoset geoset = infoHolder.geosets.get(mdlxAnimation.geosetId);

		GeosetAnim geosetAnim = new GeosetAnim(geoset);
		geosetAnim.setStaticAlpha(mdlxAnimation.alpha);
		geosetAnim.setStaticColor(new Vec3(ModelUtils.flipRGBtoBGR(mdlxAnimation.color)));

		geosetAnim.setDropShadow(((mdlxAnimation.flags & 1) == 1));

		geosetAnim.loadTimelines(mdlxAnimation);
		if (geoset != null) {
			geoset.setGeosetAnim(geosetAnim);
		}
		return geosetAnim;
	}

}
