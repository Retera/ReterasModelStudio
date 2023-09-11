package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.AnimToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.GeosetToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.IdObjectToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.MaterialToMdlx;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTextureAnimation;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.*;
import java.util.stream.Collectors;

public class TempSaveModelStuff {
	public static boolean DISABLE_BONE_GEO_ID_VALIDATOR = false;

	public static MdlxModel toMdlx(EditableModel model) {
		doSavePreps(model);
		Map<Bone, BoneGeosets> boneGeosetsMap = getBoneGeosetMap(model.getGeosets());
		// restores all GeosetID, ObjectID, TextureID, MaterialID stuff
		// all based on object references in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())"
		// without a problem, or even
		// "mdl.add(otherMdl.getGeoset(5))"
		// and have the geoset's textures and  materials all be carried
		// over with it via object references in java.
		// This re-creates all matrices, which are consumed by the
		// MatrixEater at runtime in doPostRead() in favor of each vertex
		// having its own attachments list, no vertex groups)

		final MdlxModel mdlxModel = new MdlxModel();
		mdlxModel.comments.addAll(model.getComments());

		mdlxModel.version = model.getFormatVersion();
		mdlxModel.name = model.getName();
		mdlxModel.blendTime = model.getBlendTime();
		mdlxModel.extent = model.getExtents().toMdlx();

		for (final Animation sequence : model.getAnims()) {
			mdlxModel.sequences.add(AnimToMdlx.toMdlx(sequence));
		}

		for (final GlobalSeq sequence : model.getGlobalSeqs()) {
			mdlxModel.globalSequences.add((long) sequence.getLength());
		}

		for (final Bitmap texture : model.getTextures()) {
			mdlxModel.textures.add(toMdlx(texture));
		}

		for (final TextureAnim animation : model.getTexAnims()) {
			mdlxModel.textureAnimations.add(toMdlx(animation, model));
		}

		for (final Material material : model.getMaterials()) {
			mdlxModel.materials.add(MaterialToMdlx.toMdlx(material, model));
		}

		for (final Geoset geoset : model.getGeosets()) {
			mdlxModel.geosets.add(GeosetToMdlx.toMdlx(geoset, model));
			if (geoset.hasAnim()) {
				mdlxModel.geosetAnimations.add(GeosetToMdlx.animatedToMdlx(geoset, model));
			}
		}

		for (final Bone bone : model.getBones()) {
			mdlxModel.bones.add(IdObjectToMdlx.toMdlx(bone, model, boneGeosetsMap.get(bone)));
		}

		for (final Light light : model.getLights()) {
			mdlxModel.lights.add(IdObjectToMdlx.toMdlx(light, model));
		}

		for (final Helper helper : model.getHelpers()) {
			mdlxModel.helpers.add(IdObjectToMdlx.toMdlxHelper(helper, model));
		}

		for (final Attachment attachment : model.getAttachments()) {
			mdlxModel.attachments.add(IdObjectToMdlx.toMdlx(attachment, model));
		}

		for (final ParticleEmitter emitter : model.getParticleEmitters()) {
			mdlxModel.particleEmitters.add(IdObjectToMdlx.toMdlx(emitter, model));
		}

		for (final ParticleEmitter2 emitter : model.getParticleEmitter2s()) {
			mdlxModel.particleEmitters2.add(IdObjectToMdlx.toMdlx(emitter, model));
		}

		for (final ParticleEmitterPopcorn emitter : model.getPopcornEmitters()) {
			mdlxModel.particleEmittersPopcorn.add(IdObjectToMdlx.toMdlx(emitter, model));
		}

		for (final RibbonEmitter emitter : model.getRibbonEmitters()) {
			mdlxModel.ribbonEmitters.add(IdObjectToMdlx.toMdlx(emitter, model));
		}

		for (final EventObject object : model.getEvents()) {
			mdlxModel.eventObjects.add(IdObjectToMdlx.toMdlx(object, model));
		}

		for (final Camera camera : model.getCameras()) {
			mdlxModel.cameras.add(camera.toMdlx(model));
		}

		for (final CollisionShape shape : model.getColliders()) {
			mdlxModel.collisionShapes.add(IdObjectToMdlx.toMdlx(shape, model));
		}

		for (final IdObject idObject : model.getIdObjects()) {
			mdlxModel.pivotPoints.add(idObject.getPivotPoint().toFloatArray());
		}

		for (final FaceEffect effect : model.getFaceEffects()) {
			mdlxModel.faceEffects.add(effect.toMdlx());
		}

		if (model.isUseBindPose()) {
			mdlxModel.bindPose.addAll(getBindPoses(model).getBindPoses());
		}

		return mdlxModel;
	}

	public static void doSavePreps(EditableModel model) {
		// restores all GeosetID, ObjectID, TextureID, MaterialID stuff,
		// all based on object references in the Java (this is so that you
		// can write a program that does something like  "mdl.add(new Bone())" without a problem,
		// or even "mdl.add(otherMdl.getGeoset(5))" and have the geoset's textures and
		// materials all be carried over with it via object references in java

		// also this re-creates all matrices, which are consumed by the
		// MatrixEater at runtime in doPostRead() in favor of each vertex
		// having its own attachments list, no vertex groups)

		sortNodes(model, false);
		removeEmptyGeosets(model);

		rebuildMaterialList(model, true);
		rebuildTextureAnimList(model, true);
		rebuildTextureList(model, true);
		rebuildGlobalSeqList(model, true);
//		rebuildLists(model);

		// If rebuilding the lists is to crash, then we want to crash the thread
		// BEFORE clearing the file

		// Animations
		fixAnimIntervals(model);
		removeEmptyAnimFlags(model);

		// Geosets
		for (final Geoset geoset : model.getGeosets()) {
			purifyFaces(geoset);

			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				if (geosetVertex.getSkinBoneBones() == null) {
					geosetVertex.getMatrix().cureBones(model.getBones());
				}
			}
			for (Triangle triangle : geoset.getTriangles()) {
				triangle.setGeoset(geoset);
			}
		}
	}

	private static void sortNodes(EditableModel model, boolean realSort){
		NodeUtils.getNodeCircles(model.getIdObjects()).forEach(c -> c.get(0).setParent(null));
		if (realSort) {
			List<IdObject> topNodes = model.getIdObjects().stream().filter(n -> n.getParent() == null).collect(Collectors.toList());
			Set<IdObject> idObjects = NodeUtils.collectChildren(topNodes, null, true);
			model.clearAllIdObjects();
			idObjects.forEach(model::add);
		}
		model.sortIdObjects();

	}

	private static void removeEmptyGeosets(EditableModel model) {
//		new ArrayList<>(model.getGeosets()).stream().filter(Geoset::isEmpty).forEach(model::remove);
		List<Geoset> emptyGeosets = new ArrayList<>();
		for (Geoset geoset : model.getGeosets()) {
			if (geoset.isEmpty()) {
				emptyGeosets.add(geoset);
			}
		}
		emptyGeosets.forEach(model::remove);
	}

	private static void fixAnimIntervals(EditableModel model) {
		int animSpacing = 100;
		int lastAnimEndWs = animSpacing;
		boolean forceRespacing = false;
		for (Animation animation : model.getAnims()) {
			if (animation.getStart() < lastAnimEndWs || forceRespacing) {
				animation.setStart(lastAnimEndWs);
			}
			lastAnimEndWs = animation.getEnd() + animSpacing;
		}
		model.getAnims().sort(Animation::compareTo);
	}

	public static void rebuildMaterialList(EditableModel model, boolean clearUnused) {
		LinkedHashSet<Material> materials = new LinkedHashSet<>();
		if (!clearUnused) materials.addAll(model.getMaterials());
		model.getGeosets().forEach(g -> materials.add(g.getMaterial()));
		model.getRibbonEmitters().forEach(r -> materials.add(r.getMaterial()));
		materials.remove(null);
		model.clearMaterials();
		materials.forEach(model::add);
	}

	public static void rebuildLists(EditableModel model) {
		rebuildMaterialList(model, true);
		rebuildTextureAnimList(model, true);
		rebuildTextureList(model, true);
//		rebuildGlobalSeqList(model, true);
	}

	public static void rebuildTextureList(EditableModel model, boolean clearUnused) {
		Set<Bitmap> bitmapSet = new LinkedHashSet<>();
		if (!clearUnused) bitmapSet.addAll(model.getTextures());
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				for (Layer.Texture texture : layer.getTextureSlots()){
					BitmapAnimFlag animFlag = texture.getFlipbookTexture();
					if (animFlag != null) {
						for (TreeMap<Integer, Entry<Bitmap>> entryMap : animFlag.getAnimMap().values()){
							for(Entry<Bitmap> entry : entryMap.values()){
								if(entry != null){
									bitmapSet.add(entry.getValue());
								}
							}
						}
					} else {
						bitmapSet.add(texture.getTexture());
					}
				}
			}
		}
		final List<ParticleEmitter2> particles = model.getParticleEmitter2s();
		for (final ParticleEmitter2 pe : particles) {
			bitmapSet.add(pe.getTexture());
		}
		bitmapSet.remove(null);
		model.clearTextures();
		bitmapSet.forEach(model::add);
	}

	public static void rebuildTextureAnimList(EditableModel model, boolean clearUnused) {
		Set<TextureAnim> textureAnimSet = new LinkedHashSet<>();
		if (!clearUnused) textureAnimSet.addAll(model.getTexAnims());
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				textureAnimSet.add(lay.getTextureAnim());
			}
		}
		textureAnimSet.remove(null);
		model.clearTexAnims();
		textureAnimSet.forEach(model::add);
	}


	public static void rebuildGlobalSeqList(EditableModel model, boolean clearUnused) {
		Set<GlobalSeq> globalSeqSet = new TreeSet<>();
		if (!clearUnused) globalSeqSet.addAll(model.getGlobalSeqs());

		ModelUtils.doForAnimFlags(model, animFlag -> {
			if(animFlag.hasGlobalSeq()) {
				globalSeqSet.add(animFlag.getGlobalSeq());
			}
		});

		model.getEvents().forEach(e -> globalSeqSet.add(e.getGlobalSeq()));

		model.clearGlobalSeqs();
		globalSeqSet.remove(null);
		globalSeqSet.stream().sorted().forEach(model::add);

	}

	private static BindPose getBindPoses(EditableModel model) {
		BindPose bindPoseChunk = new BindPose();
		for (IdObject obj : model.getIdObjects()) {
			bindPoseChunk.addBindPose(obj.getBindPoseM4().getBindPose());
		}
		for (Camera obj : model.getCameras()) {
			bindPoseChunk.addBindPose(obj.getBindPoseM4().getBindPose());
		}
		return bindPoseChunk;
	}

	private static void removeEmptyAnimFlags(EditableModel model) {
		// Delete empty rotation/translation/scaling
		for (IdObject obj : model.getIdObjects()) {
			Collection<AnimFlag<?>> animFlags = obj.getAnimFlags();
			List<AnimFlag<?>> bad = new ArrayList<>();
			for (AnimFlag<?> flag : animFlags) {
				if (flag.size() <= 0) {
					bad.add(flag);
				}
			}
			for (AnimFlag<?> badFlag : bad) {
				System.err.println("Gleaning out " + badFlag.getName() + " chunk with size of 0 (\"" + obj.getName() + "\")");
				animFlags.remove(badFlag);
			}
		}
	}

	public static Map<Bone, BoneGeosets> getBoneGeosetMap(List<Geoset> modelGeosets) {
		if (DISABLE_BONE_GEO_ID_VALIDATOR) {
			return new HashMap<>();
		}
		Map<Bone, Set<Geoset>> boneToGeosets = new HashMap<>();
		for (final Geoset geoset : modelGeosets) {
			for (final Matrix matrix : geoset.collectMatrices()) {
				for (final Bone bone : matrix.getBones()) {
					boneToGeosets.computeIfAbsent(bone, k -> new HashSet<>()).add(geoset);
					IdObject parent = bone.getParent();
					int maxDepth = 500; // to not get stuck in a loop if there's cyclic parenting
					while (parent != null && 0 < maxDepth){
						if(parent instanceof Bone){
							boneToGeosets.computeIfAbsent((Bone) parent, k -> new HashSet<>()).add(geoset);
						}
						parent = parent.getParent();
						maxDepth--;
					}
				}
			}
		}

		Map<Bone, BoneGeosets> boneGeosetsMap = new HashMap<>();
		for (final Bone bone : boneToGeosets.keySet()) {
			Set<Geoset> geosets = boneToGeosets.get(bone);
			boolean multiGeo = false;
			Geoset animatedGeoset = null;
			Geoset mainGeoset = null;
			for(Geoset geoset : geosets){
				if (!multiGeo && mainGeoset == null) {
					// The bone has been found by no prior matrices
					animatedGeoset = geoset;
					mainGeoset = geoset;
				} else if (!multiGeo && mainGeoset != geoset) {
					// The bone has only been found by ONE matrix
					multiGeo = true;
					mainGeoset = null;
					animatedGeoset = getMostVisible(geoset, animatedGeoset);
					if (animatedGeoset != null) {
						mainGeoset = animatedGeoset;
						multiGeo = false;
					}

				} else if (multiGeo) {
					// The bone has been found by more than one matrix
					animatedGeoset = getMostVisible(geoset, animatedGeoset);
				}
			}
			boneGeosetsMap.put(bone, new BoneGeosets().setGeoset(mainGeoset).setAnimatedGeoset(animatedGeoset));
		}
		return boneGeosetsMap;
	}

	public static Map<Bone, BoneGeosets> getBoneGeosetMap2(List<Geoset> modelGeosets, List<Bone> modelBones) {
		if (DISABLE_BONE_GEO_ID_VALIDATOR) {
			return new HashMap<>();
		}
		Map<Bone, Set<Geoset>> boneToGeosets = new HashMap<>();
		for (final Geoset geoset : modelGeosets) {
			for (final Matrix matrix : geoset.collectMatrices()) {
				for (final Bone bone : matrix.getBones()) {
					boneToGeosets.computeIfAbsent(bone, k -> new HashSet<>()).add(geoset);
					IdObject parent = bone.getParent();
					int maxDepth = 500; // to not get stuck in a loop if there's cyclic parenting
					while (parent != null && 0 < maxDepth){
						if(parent instanceof Bone){
							boneToGeosets.computeIfAbsent((Bone) parent, k -> new HashSet<>()).add(geoset);
						}
						parent = parent.getParent();
						maxDepth--;
					}
				}
			}
		}

		Map<Set<Geoset>, BoneGeosets> setToGeosetsMap = new HashMap<>();
		for (Set<Geoset> geosets : boneToGeosets.values()) {
			if (!setToGeosetsMap.containsKey(geosets)){
				boolean multiGeo = false;
				Geoset animatedGeoset = null;
				Geoset mainGeoset = null;
				for(Geoset geoset : geosets){
					if (!multiGeo && mainGeoset == null) {
						// The bone has been found by no prior matrices
						animatedGeoset = geoset;
						mainGeoset = geoset;
					} else if (!multiGeo && mainGeoset != geoset) {
						// The bone has only been found by ONE matrix
						multiGeo = true;
						mainGeoset = null;
						animatedGeoset = getMostVisible(geoset, animatedGeoset);
						if (animatedGeoset != null) {
							mainGeoset = animatedGeoset;
							multiGeo = false;
						}

					} else if (multiGeo) {
						// The bone has been found by more than one matrix
						animatedGeoset = getMostVisible(geoset, animatedGeoset);
					}
				}
				setToGeosetsMap.put(geosets, new BoneGeosets().setGeoset(mainGeoset).setAnimatedGeoset(animatedGeoset));
			}
		}

		Map<Bone, BoneGeosets> boneGeosetsMap = new HashMap<>();
		for (final Bone bone : boneToGeosets.keySet()) {
			boneGeosetsMap.put(bone, setToGeosetsMap.get(boneToGeosets.get(bone)));
		}
		return boneGeosetsMap;
	}

	public static Geoset getMostVisible(Geoset geoset1, Geoset geoset2) {
		if(geoset1 == geoset2){
			return geoset1;
		} if (geoset1 != null && geoset2 != null) {
			FloatAnimFlag visFlag1 = (FloatAnimFlag) geoset1.getVisibilityFlag();
			FloatAnimFlag visFlag2 = (FloatAnimFlag) geoset2.getVisibilityFlag();
			if (visFlag1 != null && visFlag2 != null) {
				FloatAnimFlag result = getMostVissibleAnimFlag(visFlag1, visFlag2);
				if (result == visFlag1) {
					return geoset1;
				} else if (result == visFlag2) {
					return geoset2;
				}
			}
		}
		return null;
	}

	private static FloatAnimFlag getMostVissibleAnimFlag(FloatAnimFlag aFlag, FloatAnimFlag bFlag) {
		FloatAnimFlag mostVisible = null;
		for (Sequence anim : aFlag.getAnimMap().keySet()) {
			final TreeMap<Integer, Entry<Float>> aEntryMap = aFlag.getEntryMap(anim);
			final TreeMap<Integer, Entry<Float>> bEntryMap = bFlag.getEntryMap(anim);

			TreeSet<Integer> timeSet = new TreeSet<>();
			if (aEntryMap != null) {
				timeSet.addAll(aEntryMap.keySet());
			}
			if (bEntryMap != null) {
				timeSet.addAll(bEntryMap.keySet());
			}

			for (int time : timeSet) {
				Float aVal = aFlag.interpolateAt(anim, time);
				Float bVal = bFlag.interpolateAt(anim, time);

				if(aVal<bVal && mostVisible == null){
					mostVisible = bFlag;
				} else if (bVal<aVal && mostVisible == null) {
					mostVisible = aFlag;
				} else if (!bVal.equals(aVal)) {
					return null;
				}
			}
		}

		return mostVisible;
	}

	public static class BoneGeosets{
		private Geoset geoset;
		private Geoset animatedGeoset;
		public Geoset getGeoset() {
			return geoset;
		}

		public BoneGeosets setGeoset(Geoset geoset) {
			this.geoset = geoset;
			return this;
		}

		public Geoset getAnimatedGeoset() {
			return animatedGeoset;
		}

		public BoneGeosets setAnimatedGeoset(Geoset animatedGeoset) {
			this.animatedGeoset = animatedGeoset;
			return this;
		}
	}

	public static void purifyFaces(Geoset geoset) {
		List<Triangle> triangles = geoset.getTriangles();
		for (int i = triangles.size()-1; 0 <= i; i--) {
			Triangle triToCheck = triangles.get(i);
			for(int j = 0; j < i; j++){
				Triangle refTri = triangles.get(j);
				if(refTri != triToCheck && refTri.equalRefs(triToCheck)) {
					triangles.remove(triToCheck);
					break;
				}
			}
		}
	}

	public static void purifyFaces2(Geoset geoset) {
		List<Triangle> triangles = geoset.getTriangles();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle refTri = triangles.get(i);
			for(int j = triangles.size()-1; i < j; j--){
				Triangle triToCheck = triangles.get(j);
				if(refTri.equalRefs(triToCheck)) {
					triangles.remove(triToCheck);
				}
			}
		}

	}

	public static MdlxTexture toMdlx(Bitmap bitmap) {
		MdlxTexture mdlxTexture = new MdlxTexture();

		mdlxTexture.path = bitmap.getPath();
		mdlxTexture.replaceableId = bitmap.getReplaceableId();

		for (Bitmap.WrapFlag flag : bitmap.getWrapFlags()){
			mdlxTexture.wrapFlag |= flag.getFlagBit();
		}

		return mdlxTexture;
	}

	public static MdlxTextureAnimation toMdlx(TextureAnim textureAnim, EditableModel model) {
		final MdlxTextureAnimation animation = new MdlxTextureAnimation();

		textureAnim.timelinesToMdlx(animation, model);

		return animation;
	}
}
