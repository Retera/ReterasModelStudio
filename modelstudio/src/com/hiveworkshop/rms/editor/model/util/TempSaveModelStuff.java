package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.AnimToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.GeosetToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.IdObjectToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.MaterialToMdlx;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTextureAnimation;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class TempSaveModelStuff {
	public static boolean DISABLE_BONE_GEO_ID_VALIDATOR = false;

	public static MdlxModel toMdlx(EditableModel model) {
		doSavePreps(model);
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
			if (geoset.getGeosetAnim() != null) {
				mdlxModel.geosetAnimations.add(GeosetToMdlx.toMdlx(geoset.getGeosetAnim(), model));
			}
		}

		for (final Bone bone : model.getBones()) {
			mdlxModel.bones.add(IdObjectToMdlx.toMdlx(bone, model));
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

		for (final Vec3 point : model.getPivots()) {
			mdlxModel.pivotPoints.add(point.toFloatArray());
		}

		for (final FaceEffect effect : model.getFaceEffects()) {
			mdlxModel.faceEffects.add(effect.toMdlx());
		}

		if (model.getBindPoseChunk() != null) {
			mdlxModel.bindPose = model.getBindPoseChunk().toMdlx();
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

		rebuildLists(model);
		// If rebuilding the lists is to crash, then we want to crash the thread
		// BEFORE clearing the file

		// Animations
		fixAnimIntervals(model);

		// Geosets -- delete if empty
		List<Geoset> emptyGeosets = new ArrayList<>();
		for (Geoset geoset : model.getGeosets()) {
			if (geoset.isEmpty()) {
				emptyGeosets.add(geoset);
				if (geoset.getGeosetAnim() != null) {
					model.remove(geoset.getGeosetAnim());
				}
			}
		}
		emptyGeosets.forEach(model::remove);

		cureBoneGeoAnimIds(model);
		removeEmptyAnimFlags(model);
		collectBindPoses(model);

		// Geosets
		if (model.getGeosets() != null && 0 < model.getGeosets().size()) {
			for (final Geoset geoset : model.getGeosets()) {
				doSavePrep(geoset, model);
			}
		}

		// Clearing pivot points
		model.clearPivots();
		for (final IdObject idObject : model.getIdObjects()) {
			model.addPivotPoint(idObject.getPivotPoint());
		}
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

	public static void rebuildMaterialList(EditableModel model) {
		model.clearMaterials();
		for (final Geoset geoset : model.getGeosets()) {
			if ((geoset.getMaterial() != null) && !model.contains(geoset.getMaterial())) {
				model.add(geoset.getMaterial());
			}
		}
		final List<RibbonEmitter> ribbons = model.getRibbonEmitters();
		for (final RibbonEmitter r : ribbons) {
			if ((r.getMaterial() != null) && !model.contains(r.getMaterial())) {
				model.add(r.getMaterial());
			} else {
				// JOptionPane.showMessageDialog(null,"Null material found for
				// ribbon at temporary object id: "+m_idobjects.indexOf(r));
			}
		}
	}

	public static void rebuildLists(EditableModel model) {
		rebuildMaterialList(model);
		rebuildTextureList(model);// texture anims handled inside textures
//		rebuildGlobalSeqList(model);
	}

	public static void rebuildTextureList(EditableModel model) {
		rebuildTextureAnimList(model);
		model.clearTextures();
		Set<Bitmap> bitmapSet = new LinkedHashSet<>();
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
		bitmapSet.remove(null);
		for (final Bitmap bitmap : bitmapSet) {
			if (!model.contains(bitmap)) {
				model.add(bitmap);
			}
		}
		final List<ParticleEmitter2> particles = model.getParticleEmitter2s();
		for (final ParticleEmitter2 pe : particles) {
			if ((pe.getTexture() != null) && !model.contains(pe.getTexture())) {
				model.add(pe.getTexture());
			}
		}
	}

	public static void rebuildTextureAnimList(EditableModel model) {
		model.clearTexAnims();
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				if (lay.getTextureAnim() != null && !model.contains(lay.getTextureAnim())) {
					model.add(lay.getTextureAnim());
				}
			}
		}
	}

	public static void rebuildGlobalSeqList(EditableModel model) {
		model.clearGlobalSeqs();
		final List<AnimFlag<?>> animFlags = ModelUtils.getAllAnimFlags(model);// laggggg!
		final List<EventObject> evtObjs = model.getEvents();
		for (final AnimFlag<?> af : animFlags) {
			if (af.getGlobalSeq() != null && !model.contains(af.getGlobalSeq())) {
				model.add(af.getGlobalSeq());
			}
		}
		for (final EventObject af : evtObjs) {
			if (af.getGlobalSeq() != null && !model.contains(af.getGlobalSeq())) {
				model.add(af.getGlobalSeq());
			}
		}
	}

	private static void collectBindPoses(EditableModel model) {
		model.sortIdObjects();
		model.setBindPoseChunk(null);
		BindPose bindPoseChunk = new BindPose();
		for (IdObject obj : model.getIdObjects()) {
			if (obj.getBindPose() != null) {
				bindPoseChunk.addBindPose(obj.getBindPose());
			}
		}
		for (Camera obj : model.getCameras()) {
			if (obj.getBindPose() != null) {
				bindPoseChunk.addBindPose(obj.getBindPose());
			}
		}
		if(bindPoseChunk.getSize()>0){
			model.setBindPoseChunk(bindPoseChunk);
		}
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

	public static void cureBoneGeoAnimIds(EditableModel model) {
		if (DISABLE_BONE_GEO_ID_VALIDATOR) {
			return;
		}
		final List<Bone> bones = model.getBones();
		for (final Bone b : bones) {
			b.setMultiGeoId(false);
			b.setGeoset(null);
			b.setGeosetAnim(null);
		}
		for (final Geoset geoset : model.getGeosets()) {
			final GeosetAnim ga = getGeosetAnimOfGeoset(model.getGeosetAnims(), model.getGeosets(), geoset);
			for (final Matrix matrix : geoset.collectMatrices()) {
				for (final Bone bone : matrix.getBones()) {
					if (!bone.isMultiGeo()) {
						if (bone.getGeoset() == null) {
							// The bone has been found by no prior matrices
							bone.setGeosetAnim(ga);
							bone.setGeoset(geoset);
						} else if (bone.getGeoset() != geoset) {
							// The bone has only been found by ONE matrix
							bone.setMultiGeoId(true);
							bone.setGeoset(null);
							if (ga != null) {
								bone.setGeosetAnim(ga.getMostVisible(bone.getGeosetAnim()));
							} else {
								bone.setGeosetAnim(null);
							}

						}
					} else if (ga != null) {
						if (ga != bone.getGeosetAnim()) {
							bone.setGeosetAnim(ga.getMostVisible(bone.getGeosetAnim()));
						}
					} else {
						bone.setGeosetAnim(null);
					}
					fixBoneChain(geoset, ga, bone);
				}
			}
		}
	}

	private static void fixBoneChain(Geoset geoset, GeosetAnim ga, IdObject boneParent) {
		while ((boneParent = boneParent.getParent()) != null) {
			if (boneParent instanceof Bone && !(boneParent instanceof Helper)) {
				final Bone bone = (Bone) boneParent;
				if (!bone.isMultiGeo()) {
					if (bone.getGeoset() == null) {
						// The bone has been found by no prior matrices
						bone.setGeosetAnim(ga);
						bone.setGeoset(geoset);
					} else if (bone.getGeoset() != geoset) {
						// The bone has only been found by ONE matrix
						bone.setMultiGeoId(true);
						bone.setGeoset(null);
						if (ga != null) {
							bone.setGeosetAnim(ga.getMostVisible(bone.getGeosetAnim()));
							if (bone.getGeosetAnim() != null) {
								bone.setGeoset(bone.getGeosetAnim().getGeoset());
								bone.setMultiGeoId(false);
							}
						}

					}
				} else if ((ga != null) && (ga != bone.getGeosetAnim())) {
					bone.setGeosetAnim(ga.getMostVisible(bone.getGeosetAnim()));
				}
			}
		}
	}

	public static GeosetAnim getGeosetAnimOfGeoset(List<GeosetAnim> geosetAnims, List<Geoset> allModelGeosets, Geoset geoset) {
		if (geoset.getGeosetAnim() == null) {
			boolean noIds = geosetAnims.stream().noneMatch(ga -> ga.getGeoset() != null);

			if (noIds) {
				int geosetIndex = allModelGeosets.indexOf(geoset);
				if (geosetAnims.size() > geosetIndex) {
					geoset.setGeosetAnim(geosetAnims.get(geosetIndex));
				} else {
					return null;
				}
			} else {
				for (final GeosetAnim ga : geosetAnims) {
					if (ga.getGeoset() == geoset) {
						geoset.setGeosetAnim(ga);
						break;
					}
				}
			}
		}
		return geoset.getGeosetAnim();
	}

	public static void purifyFaces(Geoset geoset) {
		List<Triangle> triangles = geoset.getTriangles();
		for (int i = triangles.size() - 1; i >= 0; i--) {
			final Triangle tri = triangles.get(i);
			for (int ix = 0; ix < triangles.size(); ix++) {
				final Triangle trix = triangles.get(ix);
				if (trix != tri) {
					if (trix.equalRefsNoIds(tri)) {
						// Changed this from "sameVerts" -- this means that triangles with the same
						// vertices but in a different order will no longer be purged automatically.
						triangles.remove(tri);
						break;
					}
				}
			}
		}
	}

	public static void doSavePrep(Geoset geoset, final EditableModel model) {
		purifyFaces(geoset);

		// Clearing matrix list
		geoset.clearMatrices();
		System.out.println("Prepping geoset for saving: " + model.getName() + ": " + geoset.getName());
		for (final GeosetVertex geosetVertex : geoset.getVertices()) {
			if (geosetVertex.getSkinBoneBones() == null) {
				geosetVertex.getMatrix().cureBones(model);

			}
		}
		for (Triangle triangle : geoset.getTriangles()) {
			triangle.setGeoset(geoset);
		}
		geoset.reMakeMatrixList();
	}

	public static MdlxTexture toMdlx(Bitmap bitmap) {
		MdlxTexture mdlxTexture = new MdlxTexture();

		mdlxTexture.path = bitmap.getPath();
		mdlxTexture.replaceableId = bitmap.getReplaceableId();
		mdlxTexture.wrapMode = bitmap.getWrapMode();

		return mdlxTexture;
	}

	public static MdlxTextureAnimation toMdlx(TextureAnim textureAnim, EditableModel model) {
		final MdlxTextureAnimation animation = new MdlxTextureAnimation();

		textureAnim.timelinesToMdlx(animation, model);

		return animation;
	}
}
