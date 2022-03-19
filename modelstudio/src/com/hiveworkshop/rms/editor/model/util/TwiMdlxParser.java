//package com.hiveworkshop.rms.editor.model.util;
//
//import com.hiveworkshop.rms.editor.model.*;
//import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
//import com.hiveworkshop.rms.parsers.mdlx.*;
//import com.hiveworkshop.rms.util.Vec2;
//import com.hiveworkshop.rms.util.Vec3;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class TwiMdlxParser {
//
//	List<Bone> bones = new ArrayList<>();
//	List<Helper> helpers = new ArrayList<>();
//	List<Animation> animations = new ArrayList<>();
//	List<Geoset> geosets = new ArrayList<>();
//	List<GeosetAnim> geosetAnims = new ArrayList<>();
//	List<Vec3> pivots = new ArrayList<>();
//	List<Camera> cameras = new ArrayList<>();
//	List<Bitmap> bitmaps = new ArrayList<>();
//	List<TextureAnim> textureAnims = new ArrayList<>();
//	List<Material> materials = new ArrayList<>();
//
//	public EditableModel makeModel(final MdlxModel mdlxModel) {
//		// Step 1: Convert the Model Chunk
//		// For MDL api, this is currently embedded right inside the MDL class
//		Map<IdObject, Integer> parentMap = new HashMap<>();
//		Map<Integer, IdObject> idMap = new HashMap<>();
//		Map<IdObject, Integer> objIdMap = new HashMap<>();
//
//		EditableModel uggModel = new EditableModel();
//		uggModel.setFormatVersion(mdlxModel.version);
//		uggModel.setName(mdlxModel.name);
//		uggModel.setBlendTime((int) mdlxModel.blendTime);
//		uggModel.setExtents(new ExtLog(mdlxModel.extent));
//
//		// Step 2: Convert the Sequences
//		for (final MdlxSequence sequence : mdlxModel.sequences) {
//			animations.add(createAnimation(sequence));
//		}
//
//		// Step 3: Convert any global sequences
//		for (final long sequence : mdlxModel.globalSequences) {
//			uggModel.add((int) sequence);
//		}
//
//		// Step 4: Convert Texture refs
//		for (final MdlxTexture texture : mdlxModel.textures) {
//			bitmaps.add(new Bitmap(texture));
//		}
//
//		// Step 6: Convert TVertexAnims
//		for (final MdlxTextureAnimation animation : mdlxModel.textureAnimations) {
//			textureAnims.add(new TextureAnim(animation));
//		}
//
//		// Step 5: Convert Material refs
//		for (final MdlxMaterial material : mdlxModel.materials) {
//			materials.add(new Material(material, uggModel));
//		}
//
//		// Step 7: Geoset
//		for (final MdlxGeoset geoset : mdlxModel.geosets) {
//			geosets.add(new Geoset(geoset, uggModel));
//		}
//
//		// Step 8: GeosetAnims
//		for (final MdlxGeosetAnimation animation : mdlxModel.geosetAnimations) {
//			if (animation.geosetId != -1) {
//				final GeosetAnim geosetAnim = new GeosetAnim(animation, uggModel);
//
//				geosetAnims.add(geosetAnim);
//
//				if (geosetAnim.getGeoset() != null) {
//					geosetAnim.getGeoset().setGeosetAnim(geosetAnim);
//				}
//			}
//		}
//
//		// Step 9:
//		// convert "IdObjects" as I called them in my high school mdl code (nodes)
//
//		// Bones
//		for (final MdlxBone bone : mdlxModel.bones) {
////			System.out.println("MdlxBone, id: " + bone.objectId + " name: " + bone.name);
//			Bone x = new Bone(bone);
//			objIdMap.put(x, bone.objectId);
//			idMap.put(bone.objectId, x);
//			parentMap.put(x, bone.parentId);
//			bones.add(x);
//		}
//
//		// Lights
//		for (final MdlxLight light : mdlxModel.lights) {
//			Light x = new Light(light);
//			objIdMap.put(x, light.objectId);
//			idMap.put(light.objectId, x);
//			parentMap.put(x, light.parentId);
//			uggModel.add(x);
//		}
//
//		// Helpers
//		for (final MdlxHelper helper : mdlxModel.helpers) {
////			System.out.println("MdlxHelper");
//			Helper x = new Helper(helper);
//			objIdMap.put(x, helper.objectId);
//			idMap.put(helper.objectId, x);
//			parentMap.put(x, helper.parentId);
//			helpers.add(x);
//		}
//
//		// Attachment
//		for (final MdlxAttachment attachment : mdlxModel.attachments) {
//			Attachment x = new Attachment(attachment);
//			objIdMap.put(x, attachment.objectId);
//			idMap.put(attachment.objectId, x);
//			parentMap.put(x, attachment.parentId);
//			uggModel.add(x);
//		}
//
//		// ParticleEmitter (number 1 kind)
//		for (final MdlxParticleEmitter emitter : mdlxModel.particleEmitters) {
//			ParticleEmitter x = new ParticleEmitter(emitter);
//			objIdMap.put(x, emitter.objectId);
//			idMap.put(emitter.objectId, x);
//			parentMap.put(x, emitter.parentId);
//			uggModel.add(x);
//		}
//
//		// ParticleEmitter2
//		for (final MdlxParticleEmitter2 emitter : mdlxModel.particleEmitters2) {
//			ParticleEmitter2 x = new ParticleEmitter2(emitter);
//			objIdMap.put(x, emitter.objectId);
//			idMap.put(emitter.objectId, x);
//			parentMap.put(x, emitter.parentId);
//			uggModel.add(x);
//		}
//
//		// PopcornFxEmitter
//		for (final MdlxParticleEmitterPopcorn emitter : mdlxModel.particleEmittersPopcorn) {
//			ParticleEmitterPopcorn x = new ParticleEmitterPopcorn(emitter);
//			objIdMap.put(x, emitter.objectId);
//			idMap.put(emitter.objectId, x);
//			parentMap.put(x, emitter.parentId);
//			uggModel.add(x);
//		}
//
//		// RibbonEmitter
//		for (final MdlxRibbonEmitter emitter : mdlxModel.ribbonEmitters) {
//			RibbonEmitter x = new RibbonEmitter(emitter);
//			objIdMap.put(x, emitter.objectId);
//			idMap.put(emitter.objectId, x);
//			parentMap.put(x, emitter.parentId);
//			uggModel.add(x);
//		}
//
//		// EventObject
//		for (final MdlxEventObject object : mdlxModel.eventObjects) {
//			EventObject x = new EventObject(object);
//			objIdMap.put(x, object.objectId);
//			idMap.put(object.objectId, x);
//			parentMap.put(x, object.parentId);
//			uggModel.add(x);
//		}
//
//		for (final MdlxCamera camera : mdlxModel.cameras) {
//			cameras.add(new Camera(camera));
//		}
//
//		// CollisionShape
//		for (final MdlxCollisionShape shape : mdlxModel.collisionShapes) {
//			CollisionShape x = new CollisionShape(shape);
//			objIdMap.put(x, shape.objectId);
//			idMap.put(shape.objectId, x);
//			parentMap.put(x, shape.parentId);
//			uggModel.add(x);
//		}
//
//		for (final float[] point : mdlxModel.pivotPoints) {
//			uggModel.addPivotPoint(new Vec3(point));
//		}
//
//		for (final MdlxFaceEffect effect : mdlxModel.faceEffects) {
//			uggModel.addFaceEffect(new FaceEffect(effect.type, effect.path));
//		}
//
//		if (mdlxModel.bindPose.size() > 0) {
//			uggModel.bindPose = new BindPose(mdlxModel.bindPose);
//		}
//
//		for (IdObject idObject : parentMap.keySet()) {
//			int parentId = parentMap.get(idObject);
//			if (parentId != -1 && idMap.containsKey(parentId)) {
//				idObject.setParent(idMap.get(parentId));
////				if(bindPose.size()>objIdMap.get(idObject)){
////					idObject.setPivotPoint(pivots.get(objIdMap.get(idObject)));
////				}
//			}
//			Integer objId = objIdMap.get(idObject);
//			if (uggModel.getPivots().size() > objId && objId > -1) {
////				System.out.println("set pivot to: " + pivots.get(objIdMap.get(idObject)));
//				idObject.setPivotPoint(uggModel.getPivots().get(objId));
//			} else {
//				System.out.println("set {0, 0, 0} pivot");
//				idObject.setPivotPoint(new Vec3(0, 0, 0));
//
//			}
//			if (uggModel.getBindPoseChunk() != null && objId > -1) {
//				idObject.setBindPose(uggModel.getBindPoseChunk().bindPose[objId]);
//			}
////
//		}
//
//		doPostRead(); // fixes all the things
//	}
//
//	public void doPostRead() {
//		System.out.println("doPostRead for model: " + name);
//		updateIdObjectReferences();
//		for (final Geoset geo : geosets) {
//			geo.updateToObjects(this);
//		}
//		final List<GeosetAnim> badAnims = new ArrayList<>();
//		for (final GeosetAnim geoAnim : geosetAnims) {
//			if (geoAnim.geoset == null) {
//				badAnims.add(geoAnim);
//			}
//		}
//		if (badAnims.size() > 0) {
//			JOptionPane.showMessageDialog(null,
//					"We discovered GeosetAnim data pointing to an invalid GeosetID! Bad data will be deleted. Please backup your model file.");
//		}
//		for (final GeosetAnim bad : badAnims) {
//			geosetAnims.remove(bad);
//		}
//		final List<AnimFlag<?>> animFlags = getAllAnimFlags();// laggggg!
//		for (final AnimFlag<?> af : animFlags) {
//			af.updateGlobalSeqRef(this);
//			if (!af.getName().equals("Scaling") && !af.getName().equals("Translation")
//					&& !af.getName().equals("Rotation")) {
//			}
//		}
////		final List<EventObject> evtObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
//		final List<EventObject> evtObjs = getEvents();
//		for (final EventObject af : evtObjs) {
//			af.updateGlobalSeqRef(this);
//		}
////		for (final ParticleEmitter2 temp : (List<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class)) {
//		for (final ParticleEmitter2 temp : getParticleEmitter2s()) {
//			temp.updateTextureRef(textures);
//		}
////		for (final RibbonEmitter emitter : (List<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class)) {
//		for (final RibbonEmitter emitter : getRibbonEmitters()) {
//			emitter.updateMaterialRef(materials);
//		}
//		for (ParticleEmitterPopcorn popcorn : getPopcornEmitters()) {
//			popcorn.initAnimsVisStates(getAnims());
//		}
//	}
//
//	public void updateIdObjectReferences() {
//		final List<Bone> bones = getBones();
//		final List<? extends Bone> helpers = getHelpers();
//		bones.addAll(helpers);
//		boolean canWarnPivot = true;
//		System.out.println("pivots: " + pivots.size());
//		System.out.println("idObjects: " + modelIdObjects.getIdObjectsSize());
////		for (int i = 0; i < modelIdObjects.getIdObjectsSize(); i++) {
////			final IdObject obj = modelIdObjects.getIdObject(i);
//////			if (obj.parentId != -1) {
//////				obj.setParent(modelIdObjects.getIdObject(obj.parentId));
//////			}
////			if (i >= pivots.size()) {
////				System.out.println("idOb size: " + modelIdObjects.getIdObjectsSize() + ", pivots: " + pivots.size() + ", i: " + i);
////				if (canWarnPivot) {
////					JOptionPane.showMessageDialog(null,
////							"Error: More objects than PivotPoints were found." +
////									"\nAdditional pivot at {0,0,0} will be added.");
////					System.out.println("Error: More objects than PivotPoints were found." +
////							"\nAdditional pivot at {0,0,0} will be added.");
////					canWarnPivot = false;
////				}
////				pivots.add(new Vec3(0, 0, 0));
////			}
//////			obj.setPivotPoint(pivots.get(i));
//////			if (bindPose != null) {
//////				obj.bindPose = bindPose.bindPose[i];
//////			}
////		}
//		for (final Bone b : bones) {
//			if ((b.geosetId != -1) && (b.geosetId < geosets.size())) {
//				b.geoset = geosets.get(b.geosetId);
//			}
//			if ((b.geosetAnimId != -1) && (b.geosetAnimId < geosetAnims.size())) {
//				b.geosetAnim = geosetAnims.get(b.geosetAnimId);
//			}
//		}
//		for (int i = 0; i < cameras.size(); i++) {
//			final Camera camera = cameras.get(i);
//			if (bindPose != null) {
//				camera.setBindPose(bindPose.bindPose[i + modelIdObjects.getIdObjectsSize()]);
//			}
//		}
//	}
//
//	public Animation createAnimation(final MdlxSequence mdlxSequence) {
//		Animation animation = new Animation(mdlxSequence.name, (int)mdlxSequence.interval[0], (int)mdlxSequence.interval[1]);
//		animation.setExtents(new ExtLog(new Vec3(mdlxSequence.extent.min), new Vec3(mdlxSequence.extent.max), mdlxSequence.extent.boundsRadius));
//		animation.setMoveSpeed(mdlxSequence.moveSpeed);
//
//		if (mdlxSequence.flags == 1) {
//			animation.setNonLooping(true);
//		}
//		animation.setRarity(mdlxSequence.rarity);
//		return animation;
//	}
//
//	public Geoset createGeoset(final MdlxGeoset mdlxGeoset, final EditableModel model) {
//		Geoset geoset = new Geoset();
//		geoset.setExtLog(new ExtLog(mdlxGeoset.extent));
//
//		for (final MdlxExtent extent : mdlxGeoset.sequenceExtents) {
//			final ExtLog extents = new ExtLog(extent);
//			final Animation anim = new Animation(extents);
//			geoset.add(anim);
//		}
//
//		geoset.setMaterial(materials.get((int) mdlxGeoset.materialId));
//
//		final float[] mdlxVertices = mdlxGeoset.vertices;
//		final float[] mdlxNormals = mdlxGeoset.normals;
//		final float[][] uvSets = mdlxGeoset.uvSets;
//		final short[] vertexGroups = mdlxGeoset.vertexGroups;
//
//		List<GeosetVertex> geosetVertices = new ArrayList<>();
//
//		for (int i = 0; i < mdlxVertices.length / 3; i++) {
//			final GeosetVertex gv = new GeosetVertex(mdlxVertices[(i * 3)], mdlxVertices[(i * 3) + 1], mdlxVertices[(i * 3) + 2]);
//
//			geosetVertices.add(gv);
//
//			if (i >= vertexGroups.length) {
//				gv.setVertexGroup(-1);
//			} else {
//				gv.setVertexGroup((256 + vertexGroups[i]) % 256);
//			}
//			// this is an unsigned byte, the other guys java code will read as signed
//			if (mdlxNormals.length > 0) {
//
//				gv.setNormal(new Vec3(mdlxNormals[(i * 3)], mdlxNormals[(i * 3) + 1], mdlxNormals[(i * 3) + 2]));
//			}
//
//			for (float[] uvSet : uvSets) {
//				gv.addTVertex(new Vec2(uvSet[(i * 2)], uvSet[(i * 2) + 1]));
//			}
//		}
//		geoset.addVerticies(geosetVertices);
//		// guys I didn't code this to allow experimental non-triangle faces that were suggested to exist
//		// on the web (i.e. quads). if you wanted to fix that, you'd want to do it below
//		final int[] faces = mdlxGeoset.faces;
//		List<Triangle> triangles = new ArrayList<>();
//		for (int i = 0; i < faces.length; i += 3) {
//			Triangle triangle = new Triangle(geosetVertices.get(faces[i]), geosetVertices.get(faces[i + 1]), geosetVertices.get(faces[i + 2]));
//			triangles.add(triangle);
//		}
//		geoset.addTriangles(triangles);
//
//		if (mdlxGeoset.selectionFlags == 4) {
//			geoset.setUnselectable(true);
//		}
//
//		geoset.setSelectionGroup((int)mdlxGeoset.selectionGroup);
//		geoset.setLevelOfDetail(mdlxGeoset.lod);
//		geoset.setLevelOfDetailName(mdlxGeoset.lodName);
//
//		int index = 0;
//		for (final long size : mdlxGeoset.matrixGroups) {
//			final Matrix m = new Matrix();
//			for (int i = 0; i < size; i++) {
//				m.addId((int)mdlxGeoset.matrixIndices[index++]);
//			}
//			geoset.addMatrix(m);
//		}
//
//		if (mdlxGeoset.tangents != null) {
//			final float[] mdlxTangents = mdlxGeoset.tangents;
//			final short[] mdlxSkin = mdlxGeoset.skin;
//
//			// version 900
//			List<short[]> skin = new ArrayList<>();
//			List<float[]> tangents = new ArrayList<>();
//			for (int i = 0; i < mdlxTangents.length; i += 4) {
//				tangents.add(new float[] { mdlxTangents[i], mdlxTangents[i + 1], mdlxTangents[i + 2], mdlxTangents[i + 3] });
//			}
//			for (int i = 0; i < mdlxSkin.length; i += 8) {
//				skin.add(new short[] { mdlxSkin[i], mdlxSkin[i + 1], mdlxSkin[i + 2], mdlxSkin[i + 3], mdlxSkin[i + 4], mdlxSkin[i + 5], mdlxSkin[i + 6], mdlxSkin[i + 7] });
//			}
//		}
//		return geoset;
//	}
//}
