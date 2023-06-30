package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.*;
import com.hiveworkshop.rms.util.Vec3;

public class TempOpenModelStuff {

	public static EditableModel createEditableModel(MdlxModel mdlxModel) {
		EditableModel model = new EditableModel(mdlxModel.name);
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the MDL class
		System.out.println("version: " + mdlxModel.version);
		ModelInfoHolder infoHolder = new ModelInfoHolder(mdlxModel.version);

		model.setFormatVersion(mdlxModel.version);
		model.setBlendTime((int) mdlxModel.blendTime);
		model.getExtents().set(mdlxModel.extent.min, mdlxModel.extent.max, mdlxModel.extent.boundsRadius);

		// Step ?: add comments
		for (final String comment : mdlxModel.comments) {
			System.out.println("adding comment: \"" + comment + "\"");
			model.addComment(comment);
		}

		// Step 5: Convert Texture refs
		for (final MdlxTexture mdlxTexture : mdlxModel.textures) {
			Bitmap bitmap = MaterialFactory.createBitmap(mdlxTexture);
			model.add(bitmap);
			infoHolder.add(bitmap);
		}

		// Step 4: Convert any global sequences
		for (final long sequence : mdlxModel.globalSequences) {
			model.add(new GlobalSeq((int) sequence));
		}

		// Step 3: Convert the Sequences
		for (final MdlxSequence sequence : mdlxModel.sequences) {
			model.add(createAnimation(sequence));
		}

		// Step 2: fetch pivot points and bindPose
		for (final float[] point : mdlxModel.pivotPoints) {
			infoHolder.addPivot(new Vec3(point));
		}

		if (0 < mdlxModel.bindPose.size()) {
			infoHolder.setBindPose(mdlxModel.bindPose);
			model.setUseBindPose(true);
		}

		// Step 6: Convert TVertexAnims
		for (final MdlxTextureAnimation mdlxTextureAnimation : mdlxModel.textureAnimations) {
			model.add(new TextureAnim(mdlxTextureAnimation, model));
		}

		// Step 7: Convert Material refs
		for (final MdlxMaterial mdlxMaterial : mdlxModel.materials) {
			Material x = MaterialFactory.createMaterial(mdlxMaterial, model);
			infoHolder.add(x);
			model.add(x);
		}


		// Step 3:
		// convert "IdObjects" (as I called them in my high school mdl code) (nodes)

		// Bones
		for (final MdlxBone mdlxBone : mdlxModel.bones) {
//			System.out.println("MdlxBone, id: " + mdlxBone.objectId + " name: " + mdlxBone.name);
			Bone x = IdObjectFactory.createBone(mdlxBone, model);
			infoHolder.add(mdlxBone, x);
			model.add(x);
		}

		// Lights
		for (final MdlxLight mdlxLight : mdlxModel.lights) {
			Light x = IdObjectFactory.createLight(mdlxLight, model);
			infoHolder.add(mdlxLight, x);
			model.add(x);
		}

		// Helpers
		for (final MdlxHelper mdlxHelper : mdlxModel.helpers) {
			Helper x = IdObjectFactory.createHelper(mdlxHelper, model);
			infoHolder.add(mdlxHelper, x);
			model.add(x);
		}

		// Attachment
		for (final MdlxAttachment mdlxAttachment : mdlxModel.attachments) {
			Attachment x = IdObjectFactory.createAttachment(mdlxAttachment, model);
			infoHolder.add(mdlxAttachment, x);
			model.add(x);
		}

		// ParticleEmitter (number 1 kind)
		for (final MdlxParticleEmitter mdlxEmitter : mdlxModel.particleEmitters) {
			ParticleEmitter x = IdObjectFactory.createParticleEmitter(mdlxEmitter, model);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// ParticleEmitter2
		for (final MdlxParticleEmitter2 mdlxEmitter : mdlxModel.particleEmitters2) {
			ParticleEmitter2 x = IdObjectFactory.createParticleEmitter2(mdlxEmitter, model);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// PopcornFxEmitter
		for (final MdlxParticleEmitterPopcorn mdlxEmitter : mdlxModel.particleEmittersPopcorn) {
			ParticleEmitterPopcorn x = IdObjectFactory.createParticleEmitterPopcorn(mdlxEmitter, model);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// RibbonEmitter
//		if(!mdlxModel.ribbonEmitters.isEmpty()){
//			JOptionPane.showMessageDialog(null, mdlxModel.ribbonEmitters.size() + " RIBBON EMITTER(S)!!!");
//		}

		for (final MdlxRibbonEmitter mdlxEmitter : mdlxModel.ribbonEmitters) {
			RibbonEmitter x = IdObjectFactory.createRibbonEmitter(mdlxEmitter, infoHolder, model);
			infoHolder.add(mdlxEmitter, x);
			model.add(x);
		}

		// EventObject
		for (final MdlxEventObject mdlxObject : mdlxModel.eventObjects) {
			com.hiveworkshop.rms.editor.model.EventObject x = IdObjectFactory.createEventObject(mdlxObject, model);
			infoHolder.add(mdlxObject, x);
			model.add(x);
		}


		// CollisionShape
		for (final MdlxCollisionShape mdlxShape : mdlxModel.collisionShapes) {
			CollisionShape x = IdObjectFactory.createCollisionShape(mdlxShape, model);
			infoHolder.add(mdlxShape, x);
			model.add(x);
		}

		// Step 8: Geoset
		if (model.getBones().isEmpty() && !mdlxModel.geosets.isEmpty()) {
			model.add(new Bone("Found No Bones"));
		}
		for (final MdlxGeoset mdlxGeoset : mdlxModel.geosets) {
			Geoset x = GeosetFactory.createGeoset(mdlxGeoset, infoHolder, model);
			x.setParentModel(model);
			infoHolder.add(x);
			model.add(x);
		}

		// Step 9: GeosetAnims
		for (final MdlxGeosetAnimation mdlxGeosetAnimation : mdlxModel.geosetAnimations) {
			if (mdlxGeosetAnimation.geosetId != -1) {
				Geoset geosetAnim = createGeosetAnim(mdlxGeosetAnimation, infoHolder, model);
				infoHolder.animatedGeosets.add(geosetAnim);
			}
		}

		for (final MdlxFaceEffect mdlxEffect : mdlxModel.faceEffects) {
			model.addFaceEffect(new FaceEffect(mdlxEffect.type, mdlxEffect.path));
		}

		// Cameras last! Since they have no Id the number of IdObjects + number of Cameras
		// is used to fetch the correct BindPose
		for (final MdlxCamera mdlxCamera : mdlxModel.cameras) {
			Camera x = new Camera(mdlxCamera, model);
			infoHolder.add(x);
			model.add(x);
		}

		infoHolder.fixIdObjectParents();

		return model;
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

	public static Geoset createGeosetAnim(MdlxGeosetAnimation mdlxAnimation, ModelInfoHolder infoHolder, EditableModel model) {
		Geoset geoset = infoHolder.geosets.get(mdlxAnimation.geosetId);

		if (geoset != null) {
			geoset.setStaticAlpha(mdlxAnimation.alpha);
			geoset.setStaticColor(new Vec3(mdlxAnimation.color));

			geoset.setDropShadow(((mdlxAnimation.flags & 1) == 1));

			geoset.loadTimelines(mdlxAnimation, model);
		}

		return geoset;
	}

}
