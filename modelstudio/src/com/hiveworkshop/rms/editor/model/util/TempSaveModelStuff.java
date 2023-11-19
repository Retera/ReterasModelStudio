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
		return toMdlx(model, false);
	}

	public static MdlxModel toMdlx(EditableModel model, boolean clearUnused) {
		System.out.println("Preparing Model for saving");
		doSavePreps(model, clearUnused);
		System.out.println("Fixing Bone geoset refs");
		Map<Bone, BoneGeosets> boneGeosetsMap = getBoneGeosetMap(model.getGeosets());

		System.out.println("Packing general Model stuff");
		final MdlxModel mdlxModel = new MdlxModel();
		mdlxModel.comments.addAll(model.getComments());

		mdlxModel.version = model.getFormatVersion();
		mdlxModel.name = model.getName();
		mdlxModel.blendTime = model.getBlendTime();
		mdlxModel.extent = model.getExtents().toMdlx();

		System.out.println("Packing Animations");
		model.getAnims().forEach(sequence       -> mdlxModel.sequences.add(AnimToMdlx.toMdlx(sequence)));
		model.getGlobalSeqs().forEach(sequence  -> mdlxModel.globalSequences.add((long) sequence.getLength()));

		System.out.println("Packing Material stuff");
		model.getTextures().forEach(texture     -> mdlxModel.textures.add(toMdlx(texture)));
		model.getTexAnims().forEach(animation   -> mdlxModel.textureAnimations.add(toMdlx(animation, model)));
		model.getMaterials().forEach(material   -> mdlxModel.materials.add(MaterialToMdlx.toMdlx(material, model)));

		System.out.println("Packing Geosets");
		model.getGeosets().forEach(geoset       -> mdlxModel.geosets.add(GeosetToMdlx.toMdlx(geoset, model)));
		model.getGeosets().stream().filter(Geoset::hasAnim)
				.forEach(geoset -> mdlxModel.geosetAnimations.add(GeosetToMdlx.animatedToMdlx(geoset, model)));


		System.out.println("Packing Nodes and Node animations");
		model.getBones().forEach(bone                   -> mdlxModel.bones.add(IdObjectToMdlx.toMdlx(bone, model, boneGeosetsMap.get(bone))));
		model.getLights().forEach(light                 -> mdlxModel.lights.add(IdObjectToMdlx.toMdlx(light, model)));
		model.getHelpers().forEach(helper               -> mdlxModel.helpers.add(IdObjectToMdlx.toMdlx(helper, model)));
		model.getAttachments().forEach(attachment       -> mdlxModel.attachments.add(IdObjectToMdlx.toMdlx(attachment, model)));
		model.getParticleEmitters().forEach(emitter     -> mdlxModel.particleEmitters.add(IdObjectToMdlx.toMdlx(emitter, model)));
		model.getParticleEmitter2s().forEach(emitter    -> mdlxModel.particleEmitters2.add(IdObjectToMdlx.toMdlx(emitter, model)));
		model.getPopcornEmitters().forEach(emitter      -> mdlxModel.particleEmittersPopcorn.add(IdObjectToMdlx.toMdlx(emitter, model)));
		model.getRibbonEmitters().forEach(emitter       -> mdlxModel.ribbonEmitters.add(IdObjectToMdlx.toMdlx(emitter, model)));
		model.getEvents().forEach(object                -> mdlxModel.eventObjects.add(IdObjectToMdlx.toMdlx(object, model)));
		model.getCameras().forEach(camera               -> mdlxModel.cameras.add(camera.toMdlx(model)));
		model.getColliders().forEach(shape              -> mdlxModel.collisionShapes.add(IdObjectToMdlx.toMdlx(shape, model)));

		System.out.println("Packing Pivot Points");
		model.getIdObjects().forEach(idObject           -> mdlxModel.pivotPoints.add(idObject.getPivotPoint().toFloatArray()));
		model.getFaceEffects().forEach(effect           -> mdlxModel.faceEffects.add(effect.toMdlx()));


		if (model.isUseBindPose()) {
			System.out.println("Packing BindPoses");
			mdlxModel.bindPose.addAll(getBindPoses(model).getBindPoses());
		}

		return mdlxModel;
	}

	public static void doSavePreps(EditableModel model) {
		doSavePreps(model, true);
	}

	public static void doSavePreps(EditableModel model, boolean clearUnused) {
		System.out.println("Sorting nodes");
		sortNodes(model, false);
		System.out.println("Removing empty geosets");
		model.getGeosets().removeIf(Geoset::isEmpty);

		if (clearUnused) {
			System.out.println("Removing unused events");
			removeUnusedEvents(model);
			model.getEvents().removeIf(eventObject -> model.getAllSequences().stream().noneMatch(eventObject::usedIn));
		}

		System.out.println("rebuildMaterialList");
		rebuildMaterialList(model, clearUnused);

		System.out.println("rebuildTextureAnimList");
		rebuildTextureAnimList(model, clearUnused);

		System.out.println("rebuildTextureList");
		rebuildTextureList(model, clearUnused);

		System.out.println("rebuildGlobalSeqList");
		rebuildGlobalSeqList(model, clearUnused);

		// Animations
		System.out.println("fixAnimIntervals");
		fixAnimIntervals(model);
		System.out.println("removeEmptyAnimFlags");
		removeEmptyAnimFlags(model);

		// Geosets
		for (final Geoset geoset : model.getGeosets()) {
//			if ((Short.MAX_VALUE * 2) < geoset.getVertices().size()) {
//				System.out.println("Too Large geoset!");
//
//			}
			System.out.println("fixing geoset");
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

	private static void sortNodes(EditableModel model, boolean realSort) {
		NodeUtils.getNodeCircles(model.getIdObjects()).forEach(c -> c.get(0).setParent(null));
		if (realSort) {
			List<IdObject> topNodes = model.getIdObjects().stream().filter(n -> n.getParent() == null).collect(Collectors.toList());
			Set<IdObject> idObjects = NodeUtils.collectChildren(topNodes, null, true);
			model.clearAllIdObjects();
			idObjects.forEach(model::add);
		}
		model.sortIdObjects();

	}

	private static void removeUnusedEvents(EditableModel model) {
		model.getEvents().removeIf(eventObject -> model.getAllSequences().stream().noneMatch(eventObject::usedIn));
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
		if (!clearUnused) {
			materials.addAll(model.getMaterials());
		}
		model.getGeosets().forEach(g -> materials.add(g.getMaterial()));
		model.getRibbonEmitters().forEach(r -> materials.add(r.getMaterial()));
		materials.remove(null);
		model.clearMaterials();
		materials.forEach(model::add);
	}

	public static void rebuildTextureList(EditableModel model, boolean clearUnused) {
		Set<Bitmap> bitmapSet = new LinkedHashSet<>();
		if (!clearUnused) bitmapSet.addAll(model.getTextures());
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				for (Layer.Texture texture : layer.getTextureSlots()) {
					BitmapAnimFlag animFlag = texture.getFlipbookTexture();
					if (animFlag != null) {
						for (TreeMap<Integer, Entry<Bitmap>> entryMap : animFlag.getAnimMap().values()) {
							for (Entry<Bitmap> entry : entryMap.values()) {
								if (entry != null) {
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
		for (final Material material : model.getMaterials()) {
			material.getLayers().forEach(layer -> textureAnimSet.add(layer.getTextureAnim()));
		}
		textureAnimSet.remove(null);
		model.clearTexAnims();
		textureAnimSet.forEach(model::add);
	}


	public static void rebuildGlobalSeqList(EditableModel model, boolean clearUnused) {
		Set<GlobalSeq> globalSeqSet = new TreeSet<>();
		if (!clearUnused) model.getGlobalSeqs().stream().filter(Objects::nonNull).forEach(globalSeqSet::add);

		ModelUtils.doForAnimFlags(model, animFlag -> {
			if (animFlag.hasGlobalSeq()) {
				globalSeqSet.add(animFlag.getGlobalSeq());
			}
		});

		model.getEvents().stream().filter(EventObject::hasGlobalSeq).forEach(e -> globalSeqSet.add(e.getGlobalSeq()));

		model.clearGlobalSeqs();
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
		Map<Bone, Set<Geoset>> boneToGeosets = getBoneToGeosets(modelGeosets);

		Map<Set<Geoset>, BoneGeosets> setToGeosetsMap = new HashMap<>();
		for (Set<Geoset> geosets : boneToGeosets.values()) {
			if (!setToGeosetsMap.containsKey(geosets)) {
				BoneGeosets boneGeosets = new BoneGeosets();
				for (Geoset geoset : geosets) {
					boneGeosets.compareAndSet(geoset);
				}
				setToGeosetsMap.put(geosets, boneGeosets);
			}
		}

		Map<Bone, BoneGeosets> boneGeosetsMap = new HashMap<>();
		for (final Bone bone : boneToGeosets.keySet()) {
			boneGeosetsMap.put(bone, setToGeosetsMap.get(boneToGeosets.get(bone)));
		}
		return boneGeosetsMap;
	}

	private static Map<Bone, Set<Geoset>> getBoneToGeosets(List<Geoset> modelGeosets) {
		Map<Bone, Set<Geoset>> boneToGeosets = new HashMap<>();
		for (final Geoset geoset : modelGeosets) {
			for (final Matrix matrix : geoset.collectMatrices()) {
				for (final Bone bone : matrix.getBones()) {
					boneToGeosets.computeIfAbsent(bone, k -> new HashSet<>()).add(geoset);
					IdObject parent = bone.getParent();
					int maxDepth = 500; // to not get stuck in a loop if there's cyclic parenting
					while (parent != null && 0 < maxDepth) {
						if (parent instanceof Bone) {
							boneToGeosets.computeIfAbsent((Bone) parent, k -> new HashSet<>()).add(geoset);
						}
						parent = parent.getParent();
						maxDepth--;
					}
				}
			}
		}
		return boneToGeosets;
	}

	public static class BoneGeosets{
		private Geoset geoset;
		private Geoset animatedGeoset;
		private boolean multiGeo = false;
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

		public void compareAndSet(Geoset geoToCheck) {
			if (!multiGeo && geoset == null) {
				// The bone has been found by no prior matrices
				animatedGeoset = geoToCheck;
				geoset = geoToCheck;
			} else if (!multiGeo && this.geoset != geoToCheck) {
				// The bone has only been found by ONE matrix
				multiGeo = true;
				geoset = null;
				animatedGeoset = getMostVisible(geoToCheck, animatedGeoset);
				if (animatedGeoset != null) {
					multiGeo = false;
					geoset = animatedGeoset;
				}
			} else if (multiGeo) {
				// The bone has been found by more than one matrix
				animatedGeoset = getMostVisible(geoToCheck, animatedGeoset);
			}
		}

		public static Geoset getMostVisible(Geoset geoset1, Geoset geoset2) {
			if (geoset1 == geoset2) {
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

					if (aVal<bVal && mostVisible == null) {
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
	}

	public static void purifyFaces(Geoset geoset) {
		List<Triangle> triangles = geoset.getTriangles();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle refTri = triangles.get(i);
			for (int j = triangles.size()-1; i < j; j--) {
				Triangle triToCheck = triangles.get(j);
				if (refTri.equalRefs(triToCheck)) {
					triangles.remove(triToCheck);
				}
			}
		}
	}

	public static MdlxTexture toMdlx(Bitmap bitmap) {
		MdlxTexture mdlxTexture = new MdlxTexture();

		mdlxTexture.path = bitmap.getPath();
		mdlxTexture.replaceableId = bitmap.getReplaceableId();

		for (Bitmap.WrapFlag flag : bitmap.getWrapFlags()) {
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
