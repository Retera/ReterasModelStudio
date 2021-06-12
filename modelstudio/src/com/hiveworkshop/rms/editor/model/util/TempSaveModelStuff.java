package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelSaving.GeosetToMdlx;
import com.hiveworkshop.rms.parsers.mdlx.*;
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

		mdlxModel.version = model.getFormatVersion();
		mdlxModel.name = model.getName();
		mdlxModel.blendTime = model.getBlendTime();
		mdlxModel.extent = model.getExtents().toMdlx();

		for (final Animation sequence : model.getAnims()) {
			mdlxModel.sequences.add(toMdlx(sequence));
		}

		for (final Integer sequence : model.getGlobalSeqs()) {
			mdlxModel.globalSequences.add(sequence.longValue());
		}

		for (final Bitmap texture : model.getTextures()) {
			mdlxModel.textures.add(toMdlx(texture));
		}

		for (final TextureAnim animation : model.getTexAnims()) {
			mdlxModel.textureAnimations.add(toMdlx(animation));
		}

		for (final Material material : model.getMaterials()) {
			mdlxModel.materials.add(toMdlx(material));
		}

		for (final Geoset geoset : model.getGeosets()) {
			mdlxModel.geosets.add(GeosetToMdlx.toMdlx(geoset, model));
		}

		for (final GeosetAnim animation : model.getGeosetAnims()) {
			mdlxModel.geosetAnimations.add(GeosetToMdlx.toMdlx(animation, model));
		}

		for (final Bone bone : model.getBones()) {
			mdlxModel.bones.add(toMdlx(bone, model));
		}

		for (final Light light : model.getLights()) {
			mdlxModel.lights.add(toMdlx(light, model));
		}

		for (final Helper helper : model.getHelpers()) {
			mdlxModel.helpers.add(toMdlxHelper(helper, model));
		}

		for (final Attachment attachment : model.getAttachments()) {
			mdlxModel.attachments.add(toMdlx(attachment, model));
		}

		for (final ParticleEmitter emitter : model.getParticleEmitters()) {
			mdlxModel.particleEmitters.add(toMdlx(emitter, model));
		}

		for (final ParticleEmitter2 emitter : model.getParticleEmitter2s()) {
			mdlxModel.particleEmitters2.add(toMdlx(emitter, model));
		}

		for (final ParticleEmitterPopcorn emitter : model.getPopcornEmitters()) {
			mdlxModel.particleEmittersPopcorn.add(toMdlx(emitter, model));
		}

		for (final RibbonEmitter emitter : model.getRibbonEmitters()) {
			mdlxModel.ribbonEmitters.add(toMdlx(emitter, model));
		}

		for (final EventObject object : model.getEvents()) {
			mdlxModel.eventObjects.add(toMdlx(object, model));
		}

		for (final Camera camera : model.getCameras()) {
			mdlxModel.cameras.add(camera.toMdlx());
		}

		for (final CollisionShape shape : model.getColliders()) {
			mdlxModel.collisionShapes.add(toMdlx(shape, model));
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
		for (final IdObject idObject : model.getAllObjects()) {
			model.addPivotPoint(idObject.getPivotPoint());
		}
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
		rebuildGlobalSeqList(model);
	}

	public static void rebuildTextureList(EditableModel model) {
		rebuildTextureAnimList(model);
		model.clearTextures();
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				if ((layer.getTextureBitmap() != null) && !model.contains(layer.getTextureBitmap()) && (layer.getTextures() == null)) {
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
				layer.updateIds(model);
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
		final List<AnimFlag<?>> animFlags = model.getAllAnimFlags();// laggggg!
//		final List<EventObject> evtObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evtObjs = model.getEvents();
		for (final AnimFlag<?> af : animFlags) {
			if (!model.contains(af.globalSeqLength) && (af.globalSeqLength != null)) {
				model.add(af.globalSeqLength);
			}
			af.updateGlobalSeqId(model);// keep the ids straight
		}
		for (final EventObject af : evtObjs) {
			if (!model.contains(af.getGlobalSeq()) && (af.getGlobalSeq() != null)) {
				model.add(af.getGlobalSeq());
			}
			af.updateGlobalSeqId(model);// keep the ids straight
		}
	}

	public static void updateObjectIds(EditableModel model) {
		model.sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
		model.setBindPoseChunk(null);
		for (final IdObject obj : model.getAllObjects()) {
			final Collection<AnimFlag<?>> animFlags = obj.getAnimFlags();
			final List<AnimFlag<?>> bad = new ArrayList<>();
			for (final AnimFlag<?> flag : animFlags) {
				if (flag.size() <= 0) {
					bad.add(flag);
				}
			}
			for (final AnimFlag<?> badFlag : bad) {
				System.err.println("Gleaning out " + badFlag.getName() + " chunk with size of 0");
				animFlags.remove(badFlag);
			}
		}
		// -- end injected ---

		final List<Bone> bones = model.getBones();
		final List<? extends Bone> helpers = model.getHelpers();
		bones.addAll(helpers);

		List<IdObject> allObjects = model.getAllObjects();
		for (int i = 0; i < allObjects.size(); i++) {
			final IdObject obj = allObjects.get(i);
			obj.setObjectId(model.getObjectId(obj));
			obj.setParentId(model.getObjectId(obj.getParent()));
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
			for (final Matrix matrix : geoset.getMatrix()) {
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

	public static MdlxMaterial toMdlx(Material material) {
		final MdlxMaterial mdlxMaterial = new MdlxMaterial();

		for (final Layer layer : material.getLayers()) {
			mdlxMaterial.layers.add(toMdlx(layer));
		}

		mdlxMaterial.priorityPlane = material.getPriorityPlane();

		if (material.getConstantColor()) {
			mdlxMaterial.flags |= 0x1;
		}

		if (material.getSortPrimsFarZ()) {
			mdlxMaterial.flags |= 0x10;
		}

		if (material.getFullResolution()) {
			mdlxMaterial.flags |= 0x20;
		}

		if (material.getTwoSided()) {
			mdlxMaterial.flags |= 0x2;
		}

		mdlxMaterial.shader = material.getShaderString();

		return mdlxMaterial;
	}

	public static MdlxLayer toMdlx(Layer layer) {
		MdlxLayer mdlxLayer = new MdlxLayer();

		mdlxLayer.filterMode = layer.getFilterMode();

		if (layer.getUnshaded()) {
			mdlxLayer.flags |= 0x1;
		}

		if (layer.getSphereEnvMap()) {
			mdlxLayer.flags |= 0x2;
		}

		if (layer.getTwoSided()) {
			mdlxLayer.flags |= 0x10;
		}

		if (layer.getUnfogged()) {
			mdlxLayer.flags |= 0x20;
		}

		if (layer.getNoDepthTest()) {
			mdlxLayer.flags |= 0x40;
		}

		if (layer.getNoDepthSet()) {
			mdlxLayer.flags |= 0x80;
		}

		if (layer.getUnlit()) {
			mdlxLayer.flags |= 0x100;
		}

		mdlxLayer.textureId = layer.getTextureId();
		mdlxLayer.textureAnimationId = layer.getTVertexAnimId();
		mdlxLayer.coordId = layer.getCoordId();
		mdlxLayer.alpha = (float) layer.getStaticAlpha();

		// > 800
		mdlxLayer.emissiveGain = (float) layer.getEmissive();
		// > 900
		mdlxLayer.fresnelColor = ModelUtils.flipRGBtoBGR(layer.getFresnelColor().toFloatArray());
		mdlxLayer.fresnelOpacity = (float) layer.getFresnelOpacity();
		mdlxLayer.fresnelTeamColor = (float) layer.getFresnelTeamColor();

		layer.timelinesToMdlx(mdlxLayer);

		return mdlxLayer;
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

	public static void doSavePrep(Geoset geoset, final EditableModel mdlr) {
		purifyFaces(geoset);

		// Clearing matrix list
		geoset.getMatrix().clear();
		for (final GeosetVertex geosetVertex : geoset.getVertices()) {
			if (geosetVertex.getSkinBoneBones() != null) {
				if (geoset.getMatrix().isEmpty()) {
					List<Bone> bones = mdlr.getBones();
					for (int j = 0; (j < bones.size()) && (j < 256); j++) {
						List<Bone> singleBoneList = new ArrayList<>();
						singleBoneList.add(bones.get(j));
						Matrix matrix1 = new Matrix(singleBoneList);
						matrix1.updateIds(mdlr);
						geoset.getMatrix().add(matrix1);
					}
				}
				int skinIndex = 0;
				for (Bone bone : geosetVertex.getSkinBoneBones()) {
					if (bone != null) {
						List<Bone> singleBoneList = new ArrayList<>();
						singleBoneList.add(bone);
						Matrix newTemp = new Matrix(singleBoneList);
						int index = -1;
						for (int m = 0; (m < geoset.getMatrix().size()) && (index == -1); m++) {
							if (newTemp.equals(geoset.getMatrix().get(m))) {
								index = m;
							}
						}
						geosetVertex.getSkinBoneIndexes()[skinIndex++] = (byte) index;
					}
				}
				geosetVertex.setVertexGroup(-1);
			} else {
				Matrix newTemp = new Matrix(geosetVertex.getBones());
				boolean newMatrix = true;
				for (int m = 0; (m < geoset.getMatrix().size()) && newMatrix; m++) {
					if (newTemp.equals(geoset.getMatrix().get(m))) {
						newTemp = geoset.getMatrix().get(m);
						newMatrix = false;
					}
				}
				if (!geoset.getMatrix().contains(newTemp)) {
					geoset.getMatrix().add(newTemp);
					newTemp.updateIds(mdlr);
				}
				geosetVertex.setVertexGroup(geoset.getMatrix().indexOf(newTemp));
				geosetVertex.setMatrix(newTemp);
			}
		}
		for (Triangle triangle : geoset.getTriangles()) {
			triangle.updateVertexIds(geoset);
		}
		int boneRefCount = 0;
		for (Matrix matrix : geoset.getMatrix()) {
			boneRefCount += matrix.getBones().size();
		}
		for (Matrix matrix : geoset.getMatrix()) {
			matrix.updateIds(mdlr);
		}
	}

	public static MdlxSequence toMdlx(Animation animation) {
		final MdlxSequence sequence = new MdlxSequence();

		sequence.name = animation.getName();
		sequence.interval[0] = animation.getStart();
		sequence.interval[1] = animation.getEnd();
		sequence.extent = animation.getExtents().toMdlx();
		sequence.moveSpeed = animation.getMoveSpeed();

		if (animation.isNonLooping()) {
			sequence.flags = 1;
		}

		sequence.rarity = animation.getRarity();

		return sequence;
	}

	public static MdlxTexture toMdlx(Bitmap bitmap) {
		MdlxTexture mdlxTexture = new MdlxTexture();

		mdlxTexture.path = bitmap.getPath();
		mdlxTexture.replaceableId = bitmap.getReplaceableId();
		mdlxTexture.wrapMode = bitmap.getWrapMode();

		return mdlxTexture;
	}

	public static MdlxBone toMdlx(Bone bone, EditableModel model) {
		MdlxBone mdlxBone = new MdlxBone();

		objectToMdlx(bone, mdlxBone, model);

		mdlxBone.geosetId = bone.getGeosetId();
		mdlxBone.geosetAnimationId = bone.getGeosetAnimId();

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

		mdlxAttachment.attachmentId = attachment.getAttachmentID();

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
			mdlxShape.boundsRadius = (float) collisionShape.getExtents().getBoundsRadius();
		}

		return mdlxShape;
	}

	public static MdlxEventObject toMdlx(EventObject eventObject, EditableModel model) {
		final MdlxEventObject object = new MdlxEventObject();

		objectToMdlx(eventObject, object, model);

		if (eventObject.isHasGlobalSeq()) {
			object.globalSequenceId = eventObject.getGlobalSeqId();
		}

		final List<Integer> keyframes = new ArrayList<>(eventObject.getEventTrack());

		object.keyFrames = new long[keyframes.size()];

		for (int i = 0; i < keyframes.size(); i++) {
			object.keyFrames[i] = keyframes.get(i).longValue();
		}

		return object;
	}

	public static MdlxLight toMdlx(Light light, EditableModel model) {
		final MdlxLight mdlxLight = new MdlxLight();

		objectToMdlx(light, mdlxLight, model);

		mdlxLight.type = light.getType();
		mdlxLight.attenuation[0] = light.getAttenuationStart();
		mdlxLight.attenuation[1] = light.getAttenuationEnd();
		mdlxLight.color = ModelUtils.flipRGBtoBGR(light.getStaticColor().toFloatArray());
		mdlxLight.intensity = (float) light.getIntensity();
		mdlxLight.ambientColor = ModelUtils.flipRGBtoBGR(light.getStaticAmbColor().toFloatArray());
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

		mdlxEmitter.textureId = particleEmitter2.getTextureID();
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
		mdlxEmitter.color = ModelUtils.flipRGBtoBGR(ribbonEmitter.getStaticColor().toFloatArray());
		mdlxEmitter.lifeSpan = (float) ribbonEmitter.getLifeSpan();
		mdlxEmitter.emissionRate = ribbonEmitter.getEmissionRate();
		mdlxEmitter.rows = ribbonEmitter.getRows();
		mdlxEmitter.columns = ribbonEmitter.getColumns();
		mdlxEmitter.materialId = ribbonEmitter.getMaterialId();
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

		idObject.timelinesToMdlx(mdlxObject);
	}

	public static MdlxTextureAnimation toMdlx(TextureAnim textureAnim) {
		final MdlxTextureAnimation animation = new MdlxTextureAnimation();

		textureAnim.timelinesToMdlx(animation);

		return animation;
	}
}
