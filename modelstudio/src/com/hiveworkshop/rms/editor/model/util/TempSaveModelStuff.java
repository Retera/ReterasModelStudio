package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.AnimToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.GeosetToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.IdObjectToMdlx;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.MaterialToMdlx;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTextureAnimation;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TempSaveModelStuff {
	public static boolean DISABLE_BONE_GEO_ID_VALIDATOR = false;

	public static MdlxModel toMdlx(EditableModel model) {
		doSavePreps(model); // restores all GeosetID, ObjectID, TextureID,
		// MaterialID stuff all based on object references in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())" without a problem, or even
		// "mdl.add(otherMdl.getGeoset(5))" and have the geoset's textures and
		// materials all be carried over with it via object references in java

		// also this re-creates all matrices, which are consumed by the
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
//
//		for (final GeosetAnim animation : model.getGeosetAnims()) {
//			mdlxModel.geosetAnimations.add(GeosetToMdlx.toMdlx(animation, model));
//		}

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
		updateObjectIds(model);
		// We want to print out the right ObjectIds!

		// Geosets
		if (model.getGeosets() != null) {
			if (model.getGeosets().size() > 0) {
				for (final Geoset geoset : model.getGeosets()) {
					doSavePrep(geoset, model);
				}
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
			r.setMaterialId(model.computeMaterialID(r.getMaterial())); // -1 if null
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
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				if ((layer.getTextureBitmap() != null)
						&& !model.contains(layer.getTextureBitmap())
						&& (layer.find(MdlUtils.TOKEN_TEXTURE_ID) == null
						|| layer.find(MdlUtils.TOKEN_TEXTURE_ID).size() == 0)) {
					model.add(layer.getTextureBitmap());
				} else {
					if (layer.find("TextureID") != null) {
						for (final Bitmap bitmap : layer.getTextures()) {
							if (!model.contains(bitmap)) {
								model.add(bitmap);
							}
						}
					}
				}
				updateLayerTextureIds(model, layer);
				// keep those Ids straight, will be -1 if null
			}
		}
		final List<ParticleEmitter2> particles = model.getParticleEmitter2s();
		for (final ParticleEmitter2 pe : particles) {
			if ((pe.getTexture() != null) && !model.contains(pe.getTexture())) {
				model.add(pe.getTexture());
			}
			pe.setTextureId(model.getTextureId(pe.getTexture()));
			// will be -1 if null
		}
	}

	public static void updateLayerTextureIds(EditableModel model, Layer layer) {
//		layer.setTextureId(model.getTextureId(layer.getTextureBitmap()));
		layer.setTextureId(model.getTextureId(layer.firstTexture()));
		layer.setTVertexAnimId(model.getTextureAnimId(layer.getTextureAnim()));
		IntAnimFlag txFlag = (IntAnimFlag) layer.find(MdlUtils.TOKEN_TEXTURE_ID);
		if(txFlag != null){
			for (Sequence anim : txFlag.getAnimMap().keySet()){
				for (int i = 0; i < txFlag.size(); i++) {
					Bitmap tempBitmap = layer.getTexture(txFlag.getValueFromIndex(anim, i));
					int newerTextureId = model.getTextureId(tempBitmap);

					txFlag.getEntryAt(anim, txFlag.getTimeFromIndex(anim, i)).setValue(newerTextureId);
					layer.putTexture(newerTextureId, tempBitmap);
				}
			}
		}
	}

	public static void rebuildTextureAnimList(EditableModel model) {
		model.clearTexAnims();
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				if ((lay.getTextureAnim() != null) && !model.contains(lay.getTextureAnim())) {
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
//			af.updateGlobalSeqId(model);// keep the ids straight
		}
		for (final EventObject af : evtObjs) {
			if (af.getGlobalSeq() != null && !model.contains(af.getGlobalSeq())) {
				model.add(af.getGlobalSeq());
			}
//			af.updateGlobalSeqId(model);// keep the ids straight
		}
	}

	public static void updateObjectIds(EditableModel model) {
		model.sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
		model.setBindPoseChunk(null);
		for (final IdObject obj : model.getIdObjects()) {
			final Collection<AnimFlag<?>> animFlags = obj.getAnimFlags();
			final List<AnimFlag<?>> bad = new ArrayList<>();
			for (final AnimFlag<?> flag : animFlags) {
				if (flag.size() <= 0) {
					bad.add(flag);
				}
			}
			for (final AnimFlag<?> badFlag : bad) {
				System.err.println("Gleaning out " + badFlag.getName() + " chunk with size of 0 (\"" + obj.getName() + "\")");
				animFlags.remove(badFlag);
			}
		}
		// -- end injected ---

		final List<Bone> bones = model.getBones();
		final List<? extends Bone> helpers = model.getHelpers();
		bones.addAll(helpers);

		List<IdObject> allObjects = model.getIdObjects();
		for (int i = 0; i < allObjects.size(); i++) {
			final IdObject obj = allObjects.get(i);
//			obj.setObjectId(model.getObjectId(obj));
//			obj.setParentId(model.getObjectId(obj.getParent()));
			if (obj.getBindPose() != null) {
				if (model.getBindPoseChunk() == null) {
					model.setBindPoseChunk(new BindPose(allObjects.size() + model.getCameras().size()));
				}
				model.getBindPoseChunk().bindPose[i] = obj.getBindPose();
			}
		}
		for (final Bone b : bones) {
			b.setGeosetId(model.getGeosets().indexOf(b.getGeoset()));
			b.setGeosetAnimId(model.getGeosetAnims().indexOf(b.getGeosetAnim()));
		}
		for (int i = 0; i < model.getCameras().size(); i++) {
			final Camera obj = model.getCameras().get(i);
			if (obj.getBindPose() != null) {
				if (model.getBindPoseChunk() == null) {
					model.setBindPoseChunk(new BindPose(allObjects.size() + model.getCameras().size()));
				}
				model.getBindPoseChunk().bindPose[i + allObjects.size()] = obj.getBindPose();
			}
		}
	}

	public static void cureBoneGeoAnimIds(EditableModel model) {
		if (DISABLE_BONE_GEO_ID_VALIDATOR) {
			return;
		}
//		final List<Bone> bones = (List<Bone>) sortedIdObjects(Bone.class);
		final List<Bone> bones = model.getBones();
		for (final Bone b : bones) {
			b.setMultiGeoId(false);
			b.setGeoset(null);
			b.setGeosetAnim(null);
		}
		for (final Geoset geoset : model.getGeosets()) {
			final GeosetAnim ga = getGeosetAnimOfGeoset(model.getGeosetAnims(), model.getGeosets(), geoset);
			for (final Matrix matrix : geoset.getMatrices()) {
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
					IdObject boneParent = bone.getParent();
					while (boneParent != null) {
						if (boneParent.getClass() == Bone.class) {
							final Bone b2 = (Bone) boneParent;
							if (!b2.isMultiGeo()) {
								if (b2.getGeoset() == null) {
									// The bone has been found by no prior matrices
									b2.setGeosetAnim(ga);
									b2.setGeoset(geoset);
								} else if (b2.getGeoset() != geoset) {
									// The bone has only been found by ONE matrix
									b2.setMultiGeoId(true);
									b2.setGeoset(null);
									if (ga != null) {
										b2.setGeosetAnim(ga.getMostVisible(b2.getGeosetAnim()));
										if (b2.getGeosetAnim() != null) {
											b2.setGeoset(b2.getGeosetAnim().getGeoset());
											b2.setMultiGeoId(false);
										}
									}

								}
							} else if ((ga != null) && (ga != b2.getGeosetAnim())) {
								b2.setGeosetAnim(ga.getMostVisible(b2.getGeosetAnim()));
							}
						}
						boneParent = boneParent.getParent();
					}
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
		geoset.getMatrices().clear();
		System.out.println("Prepping geoset for saving: " + model.getName() + ": " + geoset.getName());
		for (final GeosetVertex geosetVertex : geoset.getVertices()) {
			if (geosetVertex.getSkinBoneBones() != null) {
				if (geoset.getMatrices().isEmpty()) {
					List<Bone> bones = model.getBones();
					for (int j = 0; (j < bones.size()) && (j < 256); j++) {
						List<Bone> singleBoneList = new ArrayList<>();
						singleBoneList.add(bones.get(j));
						Matrix matrix = new Matrix(singleBoneList);
//						matrix.cureBones(model);
//						matrix.updateIds(model);
						geoset.getMatrices().add(matrix);
					}
				}
				int skinIndex = 0;
				for (Bone bone : geosetVertex.getSkinBoneBones()) {
					if (bone != null) {
						List<Bone> singleBoneList = new ArrayList<>();
						singleBoneList.add(bone);
						Matrix newTemp = new Matrix(singleBoneList);
						int index = -1;
						for (int m = 0; (m < geoset.getMatrices().size()) && (index == -1); m++) {
							if (newTemp.equals(geoset.getMatrices().get(m))) {
								index = m;
							}
						}
					}
				}
//				geosetVertex.setVertexGroup(-1);
			} else {
				Matrix matrix = geosetVertex.getMatrix();
				matrix.cureBones(model);

//				matrix.updateIds(model);
				if (!geoset.getMatrices().contains(matrix)) {
					System.out.println("matrix size: " + matrix.size());
					geoset.getMatrices().add(matrix);
//					matrix.updateIds(model);
				}
//				geosetVertex.setVertexGroup(geoset.getMatrices().indexOf(matrix));
//				geosetVertex.setMatrix(matrix);
			}
		}
		for (Triangle triangle : geoset.getTriangles()) {
			triangle.updateVertexIds(geoset);
		}
		int boneRefCount = 0;
		for (Matrix matrix : geoset.getMatrices()) {
			boneRefCount += matrix.getBones().size();
		}
		for (Matrix matrix : geoset.getMatrices()) {
			matrix.cureBones(model);
		}
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
