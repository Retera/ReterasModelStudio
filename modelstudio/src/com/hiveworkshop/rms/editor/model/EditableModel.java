package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.*;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSource;
import com.hiveworkshop.rms.parsers.mdlx.*;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import jassimp.AiMaterial;
import jassimp.AiMesh;
import jassimp.AiScene;

import javax.swing.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file format).
 * <p>
 * Eric Theller 11/5/2011
 */
public class EditableModel implements Named {
	public static boolean RETERA_FORMAT_BPOS_MATRICES = false;
	public static boolean DISABLE_BONE_GEO_ID_VALIDATOR = false;

	private File fileRef;
	private String name = "UnnamedModel";
	private int blendTime = 0;
	private ExtLog extents;
	private int formatVersion = 800;
	protected ArrayList<String> header = new ArrayList<>();
	protected List<Animation> anims = new ArrayList<>();
	protected List<Integer> globalSeqs = new ArrayList<>();
	protected List<Bitmap> textures = new ArrayList<>();
	protected List<SoundFile> sounds = new ArrayList<>();
	protected List<Material> materials = new ArrayList<>();
	protected List<TextureAnim> texAnims = new ArrayList<>();
	protected List<Geoset> geosets = new ArrayList<>();
	protected List<GeosetAnim> geosetAnims = new ArrayList<>();
	//	protected List<IdObject> idObjects = new ArrayList<>();
	protected List<Vec3> pivots = new ArrayList<>();
	protected List<Camera> cameras = new ArrayList<>();
	private final List<FaceEffect> faceEffects = new ArrayList<>();
	private BindPose bindPose;
	private boolean temporary;
	private DataSource wrappedDataSource = GameDataFileSystem.getDefault();

	private ModelIdObjects modelIdObjects;

	public EditableModel() {

		modelIdObjects = new ModelIdObjects();
	}

	public EditableModel(final String newName) {
		name = newName;

		modelIdObjects = new ModelIdObjects();
	}

	public EditableModel(final EditableModel other) {
		System.out.println("new model from model");
		setFileRef(other.fileRef);
		name = other.name;
		blendTime = other.blendTime;
		extents = new ExtLog(other.extents);
		formatVersion = other.formatVersion;
		header = new ArrayList<>(other.header);
		anims = new ArrayList<>(other.anims);
		globalSeqs = new ArrayList<>(other.globalSeqs);
		textures = new ArrayList<>(other.textures);
		materials = new ArrayList<>(other.materials);
		texAnims = new ArrayList<>(other.texAnims);
		geosets = new ArrayList<>(other.geosets);
		geosetAnims = new ArrayList<>(other.geosetAnims);
//		idObjects = new ArrayList<>(other.idObjects);
		pivots = new ArrayList<>(other.pivots);
		cameras = new ArrayList<>(other.cameras);
		modelIdObjects = new ModelIdObjects(); // todo copy from other
		for (IdObject idObject : other.modelIdObjects.allObjects) {
			modelIdObjects.addIdObject(idObject);
		}
	}

	public EditableModel(final MdlxModel model) {
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the MDL class
		Map<IdObject, Integer> parentMap = new HashMap<>();
		Map<Integer, IdObject> idMap = new HashMap<>();
		Map<IdObject, Integer> objIdMap = new HashMap<>();

		modelIdObjects = new ModelIdObjects();
		setFormatVersion(model.version);
		setName(model.name);
		setBlendTime((int) model.blendTime);
		setExtents(new ExtLog(model.extent));

		// Step 2: Convert the Sequences
		for (final MdlxSequence sequence : model.sequences) {
			add(new Animation(sequence));
		}

		// Step 3: Convert any global sequences
		for (final long sequence : model.globalSequences) {
			add((int) sequence);
		}

		// Step 4: Convert Texture refs
		for (final MdlxTexture texture : model.textures) {
			add(new Bitmap(texture));
		}

		// Step 6: Convert TVertexAnims
		for (final MdlxTextureAnimation animation : model.textureAnimations) {
			add(new TextureAnim(animation));
		}

		// Step 5: Convert Material refs
		for (final MdlxMaterial material : model.materials) {
			add(new Material(material, this));
		}
		
		// Step 7: Geoset
		for (final MdlxGeoset geoset : model.geosets) {
			add(new Geoset(geoset, this));
		}

		// Step 8: GeosetAnims
		for (final MdlxGeosetAnimation animation : model.geosetAnimations) {
			if (animation.geosetId != -1) {
				final GeosetAnim geosetAnim = new GeosetAnim(animation, this);

				add(geosetAnim);

				if (geosetAnim.geoset != null) {
					geosetAnim.geoset.geosetAnim = geosetAnim;
				}
			}
		}

		// Step 9:
		// convert "IdObjects" as I called them in my high school mdl code (nodes)

		// Bones
		for (final MdlxBone bone : model.bones) {
//			System.out.println("MdlxBone, id: " + bone.objectId + " name: " + bone.name);
			Bone x = new Bone(bone);
			objIdMap.put(x, bone.objectId);
			idMap.put(bone.objectId, x);
			parentMap.put(x, bone.parentId);
			add(x);
		}
		
		// Lights
		for (final MdlxLight light : model.lights) {
			Light x = new Light(light);
			objIdMap.put(x, light.objectId);
			idMap.put(light.objectId, x);
			parentMap.put(x, light.parentId);
			add(x);
		}

		// Helpers
		for (final MdlxHelper helper : model.helpers) {
//			System.out.println("MdlxHelper");
			Helper x = new Helper(helper);
			objIdMap.put(x, helper.objectId);
			idMap.put(helper.objectId, x);
			parentMap.put(x, helper.parentId);
			add(x);
		}

		// Attachment
		for (final MdlxAttachment attachment : model.attachments) {
			Attachment x = new Attachment(attachment);
			objIdMap.put(x, attachment.objectId);
			idMap.put(attachment.objectId, x);
			parentMap.put(x, attachment.parentId);
			add(x);
		}

		// ParticleEmitter (number 1 kind)
		for (final MdlxParticleEmitter emitter : model.particleEmitters) {
			ParticleEmitter x = new ParticleEmitter(emitter);
			objIdMap.put(x, emitter.objectId);
			idMap.put(emitter.objectId, x);
			parentMap.put(x, emitter.parentId);
			add(x);
		}

		// ParticleEmitter2
		for (final MdlxParticleEmitter2 emitter : model.particleEmitters2) {
			ParticleEmitter2 x = new ParticleEmitter2(emitter);
			objIdMap.put(x, emitter.objectId);
			idMap.put(emitter.objectId, x);
			parentMap.put(x, emitter.parentId);
			add(x);
		}

		// PopcornFxEmitter
		for (final MdlxParticleEmitterPopcorn emitter : model.particleEmittersPopcorn) {
			ParticleEmitterPopcorn x = new ParticleEmitterPopcorn(emitter);
			objIdMap.put(x, emitter.objectId);
			idMap.put(emitter.objectId, x);
			parentMap.put(x, emitter.parentId);
			add(x);
		}

		// RibbonEmitter
		for (final MdlxRibbonEmitter emitter : model.ribbonEmitters) {
			RibbonEmitter x = new RibbonEmitter(emitter);
			objIdMap.put(x, emitter.objectId);
			idMap.put(emitter.objectId, x);
			parentMap.put(x, emitter.parentId);
			add(x);
		}

		// EventObject
		for (final MdlxEventObject object : model.eventObjects) {
			EventObject x = new EventObject(object);
			objIdMap.put(x, object.objectId);
			idMap.put(object.objectId, x);
			parentMap.put(x, object.parentId);
			add(x);
		}

		for (final MdlxCamera camera : model.cameras) {
			add(new Camera(camera));
		}

		// CollisionShape
		for (final MdlxCollisionShape shape : model.collisionShapes) {
			CollisionShape x = new CollisionShape(shape);
			objIdMap.put(x, shape.objectId);
			idMap.put(shape.objectId, x);
			parentMap.put(x, shape.parentId);
			add(x);
		}

		for (final float[] point : model.pivotPoints) {
			addPivotPoint(new Vec3(point));
		}

		for (final MdlxFaceEffect effect : model.faceEffects) {
			addFaceEffect(new FaceEffect(effect.type, effect.path));
		}

		if (model.bindPose.size() > 0) {
			bindPose = new BindPose(model.bindPose);
		}

		for (IdObject idObject : parentMap.keySet()) {
			int parentId = parentMap.get(idObject);
			if (parentId != -1 && idMap.containsKey(parentId)) {
				idObject.setParent(idMap.get(parentId));
//				if(bindPose.size()>objIdMap.get(idObject)){
//					idObject.setPivotPoint(pivots.get(objIdMap.get(idObject)));
//				}
			}
			Integer objId = objIdMap.get(idObject);
			if (pivots.size() > objId && objId > -1) {
//				System.out.println("set pivot to: " + pivots.get(objIdMap.get(idObject)));
				idObject.setPivotPoint(pivots.get(objId));
			} else {
				System.out.println("set {0, 0, 0} pivot");
				idObject.setPivotPoint(new Vec3(0, 0, 0));

			}
			if (bindPose != null && objId > -1) {
				idObject.setBindPose(bindPose.bindPose[objId]);
			}
//
		}

		doPostRead(); // fixes all the things
	}

	public EditableModel(final AiScene scene) {
		System.out.println("IMPLEMENT EditableModel(AiScene)");

		modelIdObjects = new ModelIdObjects();
		final Map<Material, Vec3> materialColors = new HashMap<>();

		for (final AiMaterial material : scene.getMaterials()) {
			final Material editableMaterial = new Material(material, this);

			add(editableMaterial);

			final AiMaterial.Property prop = material.getProperty("$raw.Diffuse");
			if (prop != null) {
				final ByteBuffer buffer = (ByteBuffer) prop.getData();
				final float r = buffer.getFloat();
				final float g = buffer.getFloat();
				final float b = buffer.getFloat();

				if (r != 1.0f || g != 1.0f || b != 1.0f) {
					// Alpha?
					materialColors.put(editableMaterial, new Vec3(r, g, b));
				}
			}
		}

		for (final AiMesh mesh : scene.getMeshes()) {
			// For now only handle triangular meshes.
			// Note that this doesn't mean polygons are not supported.
			// This is because the meshes are triangularized by Assimp.
			// Rather, this stops line meshes from being imported.
			if (mesh.isPureTriangle()) {
				final Geoset geoset = new Geoset(mesh, this);

				add(geoset);

				// If the material used by this geoset had a diffuse color, add a geoset animation with that color.
				final Material material = geoset.getMaterial();

				if (materialColors.containsKey(material)) {
					final GeosetAnim geosetAnim = new GeosetAnim(geoset);
					geosetAnim.setStaticColor(materialColors.get(material));
					add(geosetAnim);
					geoset.geosetAnim = geosetAnim;
				}
			}
		}

		doPostRead();
	}

	public static EditableModel clone(final EditableModel what, final String newName) {
		final EditableModel newModel = new EditableModel(what);

		newModel.setName(newName);

		return newModel;
	}

	public static EditableModel deepClone(final EditableModel what, final String newName) {
		// Need to do a real save, because of strings being passed by reference.
		// Maybe other objects I didn't think about (or the code does by mistake).
		final EditableModel newModel = new EditableModel(new MdlxModel(what.toMdlx().saveMdx()));

		newModel.setName(newName);
		newModel.setFileRef(what.getFile());

		return newModel;
	}

	public File getFile() {
		return fileRef;
	}

	public File getWorkingDirectory() {
		if (fileRef != null) {
			return fileRef.getParentFile();
		}

		return null;
	}

	public DataSource getWrappedDataSource() {
		return wrappedDataSource;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.hiveworkshop.wc3.mdl.Named#getName()
	 */
	@Override
	public String getName() {
		if (fileRef == null) {
			return getHeaderName();
		}
		return fileRef.getName().split("\\.")[0];
	}

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String toString() {
		return getName() + " (\"" + getHeaderName() + "\")";
	}

	/**
	 * IMPORTANT: This is the only way to retrieve the true header name from the top
	 * of the "model chunk", the same one set by {@link #setName(String)} function.
	 */
	public String getHeaderName() {
		return name;
	}

	public void copyHeaders(final EditableModel other) {
		setFileRef(other.fileRef);
		blendTime = other.blendTime;
		if (other.extents != null) {
			extents = new ExtLog(other.extents);
		}
		formatVersion = other.formatVersion;
		header = new ArrayList<>(other.header);
		name = other.name;
	}

	public MdlxModel toMdlx() {
		doSavePreps(); // restores all GeosetID, ObjectID, TextureID,
		// MaterialID stuff all based on object references in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())" without a problem, or even
		// "mdl.add(otherMdl.getGeoset(5))" and have the geoset's textures and
		// materials all be carried over with it via object references in java

		// also this re-creates all matrices, which are consumed by the
		// MatrixEater at runtime in doPostRead() in favor of each vertex
		// having its own attachments list, no vertex groups)

		final MdlxModel model = new MdlxModel();

		model.version = getFormatVersion();
		model.name = getName();
		model.blendTime = getBlendTime();
		model.extent = getExtents().toMdlx();

		for (final Animation sequence : getAnims()) {
			model.sequences.add(sequence.toMdlx());
		}

		for (final Integer sequence : globalSeqs) {
			model.globalSequences.add(sequence.longValue());
		}

		for (final Bitmap texture : textures) {
			model.textures.add(texture.toMdlx());
		}

		for (final TextureAnim animation : texAnims) {
			model.textureAnimations.add(animation.toMdlx());
		}

		for (final Material material : materials) {
			model.materials.add(material.toMdlx());
		}

		for (final Geoset geoset : geosets) {
			model.geosets.add(geoset.toMdlx(this));
		}

		for (final GeosetAnim animation : geosetAnims) {
			model.geosetAnimations.add(animation.toMdlx(this));
		}

		for (final Bone bone : getBones()) {
			model.bones.add(bone.toMdlx(this));
		}

		for (final Light light : getLights()) {
			model.lights.add(light.toMdlx(this));
		}

		for (final Helper helper : getHelpers()) {
			model.helpers.add(helper.toMdlxHelper(this));
		}

		for (final Attachment attachment : getAttachments()) {
			model.attachments.add(attachment.toMdlx(this));
		}

		for (final ParticleEmitter emitter : getParticleEmitters()) {
			model.particleEmitters.add(emitter.toMdlx(this));
		}

		for (final ParticleEmitter2 emitter : getParticleEmitter2s()) {
			model.particleEmitters2.add(emitter.toMdlx(this));
		}

		for (final ParticleEmitterPopcorn emitter : getPopcornEmitters()) {
			model.particleEmittersPopcorn.add(emitter.toMdlx(this));
		}

		for (final RibbonEmitter emitter : getRibbonEmitters()) {
			model.ribbonEmitters.add(emitter.toMdlx(this));
		}

		for (final EventObject object : getEvents()) {
			model.eventObjects.add(object.toMdlx(this));
		}

		for (final Camera camera : getCameras()) {
			model.cameras.add(camera.toMdlx());
		}

		for (final CollisionShape shape : getColliders()) {
			model.collisionShapes.add(shape.toMdlx(this));
		}

		for (final Vec3 point : getPivots()) {
			model.pivotPoints.add(point.toFloatArray());
		}

		for (final FaceEffect effect : faceEffects) {
			model.faceEffects.add(effect.toMdlx());
		}

		if (bindPose != null) {
			model.bindPose = bindPose.toMdlx();
		}

		return model;
	}

	public void clearToHeader() {
		anims.clear();
		globalSeqs.clear();
		textures.clear();
		materials.clear();
		texAnims.clear();
		geosets.clear();
		geosetAnims.clear();
//		idObjects.clear();
		pivots.clear();
		cameras.clear();
		modelIdObjects.clearAll();
	}

	/**
	 * Deletes all the animation in the model from the time track.
	 *
	 * Might leave behind nice things like global sequences if the code works out.
	 */
	public void deleteAllAnimation(final boolean clearUnusedNodes) {
		if (clearUnusedNodes) {
			// check the emitters
			final List<ParticleEmitter> particleEmitters = getParticleEmitters();
			final List<ParticleEmitter2> particleEmitters2 = getParticleEmitter2s();
			final List<RibbonEmitter> ribbonEmitters = getRibbonEmitters();
			final List<ParticleEmitterPopcorn> popcornEmitters = getPopcornEmitters();
			final List<IdObject> emitters = new ArrayList<>();
			emitters.addAll(particleEmitters2);
			emitters.addAll(particleEmitters);
			emitters.addAll(ribbonEmitters);
			emitters.addAll(popcornEmitters);

			for (final IdObject emitter : emitters) {
				int talliesFor = 0;
				int talliesAgainst = 0;
//				final AnimFlag<?> visibility = ((VisibilitySource) emitter).getVisibilityFlag();
				final AnimFlag<?> visibility = emitter.getVisibilityFlag();
				for (final Animation anim : anims) {
					final Integer animStartTime = anim.getStart();
					final Number visible = (Number) visibility.valueAt(animStartTime);
					if ((visible == null) || (visible.floatValue() > 0)) {
						talliesFor++;
					} else {
						talliesAgainst++;
					}
				}
				if (talliesAgainst > talliesFor) {
					remove(emitter);
				}
			}
		}
		final List<AnimFlag<?>> flags = getAllAnimFlags();
//		final List<EventObject> evts = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evts = modelIdObjects.events;
		for (final Animation anim : anims) {
			anim.clearData(flags, evts);
		}
		if (clearUnusedNodes) {
			for (final EventObject e : evts) {
				if (e.size() <= 0) {
					modelIdObjects.removeIdObject(e);
//					idObjects.remove(e);
				}
			}
		}
		clearAnimations();
	}

	public List<Animation> addAnimationsFrom(EditableModel other, final List<Animation> anims) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = EditableModel.deepClone(other, "animation source file");

		final List<AnimFlag<?>> flags = getAllAnimFlags();
//		final List<EventObject> eventObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> eventObjs = getEvents();

		final List<AnimFlag<?>> othersFlags = other.getAllAnimFlags();
//		final List<EventObject> othersEventObjs = (List<EventObject>) other.sortedIdObjects(EventObject.class);
		final List<EventObject> othersEventObjs = other.getEvents();

		final List<Animation> newAnimations = new ArrayList<>();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to
		// the information specific to each node about how it will
		// move if it gets translated into or onto the current model

		final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		for (final AnimFlag<?> af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(AnimFlag.createFromAnimFlag(af));
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with the exact same data, but shifted forward
		// relative to wherever the current model's last animation starts
		for (final Animation anim : anims) {
			final int animTrackEnd = animTrackEnd();
			final int newStart = animTrackEnd + 300;
			final int newEnd = newStart + anim.length();
			final Animation newAnim = new Animation(anim);
			// clone the animation from the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			add(newAnim); // add the new animation to this model
			newAnimations.add(newAnim);
		}

		// destroy the other model's animations, filling them in with the new stuff
		for (final AnimFlag<?> af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current model
		final List<Bone> leftBehind = new ArrayList<>();
		// the bones that don't find matches in current model
//		for (final IdObject object : other.idObjects) {
		for (final IdObject object : other.getBones()) {
			if (object instanceof Bone) {
				// the bone from the other model
				final Bone bone = (Bone) object;
				// the object in this model of similar name
				final Object localObject = getObject(bone.getName());
				if ((localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone);
					// if it's a match, take the data
				} else {
					leftBehind.add(bone);
				}
			}
		}
		for (final Bone bone : leftBehind) {
			if (bone.animates()) {
				add(bone);
			}
		}

		return newAnimations;
		// i think we're done????
	}

	public void copyVisibility(final Animation visibilitySource, final Animation target) {
//		final List<VisibilitySource> allVisibilitySources = getAllVisibilitySources();
		final List<VisibilitySource> allVisibilitySources = getAllVis();
		for (final VisibilitySource source : allVisibilitySources) {
			final AnimFlag<?> visibilityFlag = source.getVisibilityFlag();
			final AnimFlag<?> copyFlag = AnimFlag.createFromAnimFlag(visibilityFlag);
			visibilityFlag.deleteAnim(target);
			visibilityFlag.copyFrom(copyFlag, visibilitySource.getStart(), visibilitySource.getEnd(), target.getStart(), target.getEnd());
		}
	}

	public void doPostRead() {
		System.out.println("doPostRead for model: " + name);
		updateIdObjectReferences();
		for (final Geoset geo : geosets) {
			geo.updateToObjects(this);
		}
		final List<GeosetAnim> badAnims = new ArrayList<>();
		for (final GeosetAnim geoAnim : geosetAnims) {
			if (geoAnim.geoset == null) {
				badAnims.add(geoAnim);
			}
		}
		if (badAnims.size() > 0) {
			JOptionPane.showMessageDialog(null,
					"We discovered GeosetAnim data pointing to an invalid GeosetID! Bad data will be deleted. Please backup your model file.");
		}
		for (final GeosetAnim bad : badAnims) {
			geosetAnims.remove(bad);
		}
		final List<AnimFlag<?>> animFlags = getAllAnimFlags();// laggggg!
		for (final AnimFlag<?> af : animFlags) {
			af.updateGlobalSeqRef(this);
			if (!af.getName().equals("Scaling") && !af.getName().equals("Translation")
					&& !af.getName().equals("Rotation")) {
			}
		}
//		final List<EventObject> evtObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evtObjs = getEvents();
		for (final EventObject af : evtObjs) {
			af.updateGlobalSeqRef(this);
		}
//		for (final ParticleEmitter2 temp : (List<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class)) {
		for (final ParticleEmitter2 temp : getParticleEmitter2s()) {
			temp.updateTextureRef(textures);
		}
//		for (final RibbonEmitter emitter : (List<RibbonEmitter>)sortedIdObjects(RibbonEmitter.class)) {
		for (final RibbonEmitter emitter : getRibbonEmitters()) {
			emitter.updateMaterialRef(materials);
		}
		for (ParticleEmitterPopcorn popcorn : getPopcornEmitters()) {
			popcorn.initAnimsVisStates(getAnims());
		}
	}

	public void doSavePreps() {
		rebuildLists();
		// If rebuilding the lists is to crash, then we want to crash the thread
		// BEFORE clearing the file

		// Animations

		// Geosets -- delete if empty
		if (geosets != null) {
			if (geosets.size() > 0) {
				for (int i = geosets.size() - 1; i >= 0; i--) {
					if (geosets.get(i).isEmpty()) {
						if (geosets.get(i).geosetAnim != null) {
							geosetAnims.remove(geosets.get(i).geosetAnim);
						}
						geosets.remove(i);
					}
				}
			}
		}

		cureBoneGeoAnimIds();
		updateObjectIds();
		// We want to print out the right ObjectIds!

		// Geosets
		if (geosets != null) {
			if (geosets.size() > 0) {
				for (final Geoset geoset : geosets) {
					geoset.doSavePrep(this);
				}
			}
		}

		// Clearing pivot points
		pivots.clear();
//		for (final IdObject idObject : idObjects) {
		for (final IdObject idObject : modelIdObjects.allObjects) {
			pivots.add(idObject.pivotPoint);
		}
	}

	public void clearTexAnims() {
		if (texAnims != null) {
			texAnims.clear();
		} else {
			texAnims = new ArrayList<>();
		}
	}

	public void rebuildTextureAnimList() {
		clearTexAnims();
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				if ((lay.textureAnim != null) && !texAnims.contains(lay.textureAnim)) {
					texAnims.add(lay.textureAnim);
				}
			}
		}
	}

	public void rebuildMaterialList() {
		materials.clear();
		for (final Geoset g : geosets) {
			if ((g.material != null) && !materials.contains(g.material)) {
				materials.add(g.material);
			}
		}
		final List<RibbonEmitter> ribbons = getRibbonEmitters();
		for (final RibbonEmitter r : ribbons) {
			if ((r.material != null) && !materials.contains(r.material)) {
				materials.add(r.material);
			} else {
				// JOptionPane.showMessageDialog(null,"Null material found for
				// ribbon at temporary object id: "+m_idobjects.indexOf(r));
			}
			r.setMaterialId(materials.indexOf(r.material)); // -1 if null
		}
	}

	public void rebuildLists() {
		rebuildMaterialList();
		rebuildTextureList();// texture anims handled inside textures
		rebuildGlobalSeqList();
	}

	public void rebuildTextureList() {
		rebuildTextureAnimList();
		textures.clear();
		for (final Material m : materials) {
			for (final Layer layer : m.layers) {
				if ((layer.texture != null) && !textures.contains(layer.texture) && (layer.textures == null)) {
					textures.add(layer.texture);

//					boolean good = true; // ToDo check that this really is as stupid as it looks...
//					for (final Bitmap btm : textures) {
//						if (layer.texture.equals(btm)) {
//							good = false;
//							break;
//						}
//					}
//					if (good) {
//						textures.add(layer.texture);
//					}
				} else {
					final AnimFlag<?> af = layer.find("TextureID");
					if (af != null) {
						for (final Bitmap temp : layer.textures) {
							if (!textures.contains(temp)) {
								textures.add(temp);
							}
//							boolean good = true;
//							for (final Bitmap btm : textures) {
//								if (temp.equals(btm)) {
//									good = false;
//									break;
//								}
//							}
//							if (good) {
//								textures.add(temp);
//							}
						}
					}
				}
				layer.updateIds(this);
				// keep those Ids straight, will be -1 if null
			}
		}
		final List<ParticleEmitter2> particles = getParticleEmitter2s();
		for (final ParticleEmitter2 pe : particles) {
			boolean good = true;
			if ((pe.texture != null) && !textures.contains(pe.texture)) {
				textures.add(pe.texture);
//				for (final Bitmap btm : textures) {
//					if (pe.texture.equals(btm)) {
//						good = false;
//						break;
//					}
//				}
//				if (good) {
//					textures.add(pe.texture);
//				}
			}
			pe.setTextureId(getTextureId(pe.texture));
			// will be -1 if null
		}
	}

	public void rebuildGlobalSeqList() {
		globalSeqs.clear();
		final List<AnimFlag<?>> animFlags = getAllAnimFlags();// laggggg!
//		final List<EventObject> evtObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evtObjs = getEvents();
		for (final AnimFlag<?> af : animFlags) {
			if (!globalSeqs.contains(af.globalSeq) && (af.globalSeq != null)) {
				globalSeqs.add(af.globalSeq);
			}
			af.updateGlobalSeqId(this);// keep the ids straight
		}
		for (final EventObject af : evtObjs) {
			if (!globalSeqs.contains(af.globalSeq) && (af.globalSeq != null)) {
				globalSeqs.add(af.globalSeq);
			}
			af.updateGlobalSeqId(this);// keep the ids straight
		}
	}



	// The below commented stuff is now an internal part of list rebuilding
	// public void updateIdsAcrossModel()
	// {
	// //globalSeq ids update dynamically as the list rebuilds
	//
	// //Fixes material/layer TextureID and TVertexAnimIDs, assuming that the
	// // proper lists are already built for use
	// for( Material m: m_materials )
	// {
	// m.updateReferenceIds(this);
	// }
	// }
	public void updateIdObjectReferences() {
		final List<Bone> bones = getBones();
		final List<? extends Bone> helpers = getHelpers();
		bones.addAll(helpers);
		boolean canWarnPivot = true;
		System.out.println("pivots: " + pivots.size());
		System.out.println("idObjects: " + modelIdObjects.getIdObjectsSize());
//		for (int i = 0; i < modelIdObjects.getIdObjectsSize(); i++) {
//			final IdObject obj = modelIdObjects.getIdObject(i);
////			if (obj.parentId != -1) {
////				obj.setParent(modelIdObjects.getIdObject(obj.parentId));
////			}
//			if (i >= pivots.size()) {
//				System.out.println("idOb size: " + modelIdObjects.getIdObjectsSize() + ", pivots: " + pivots.size() + ", i: " + i);
//				if (canWarnPivot) {
//					JOptionPane.showMessageDialog(null,
//							"Error: More objects than PivotPoints were found." +
//									"\nAdditional pivot at {0,0,0} will be added.");
//					System.out.println("Error: More objects than PivotPoints were found." +
//							"\nAdditional pivot at {0,0,0} will be added.");
//					canWarnPivot = false;
//				}
//				pivots.add(new Vec3(0, 0, 0));
//			}
////			obj.setPivotPoint(pivots.get(i));
////			if (bindPose != null) {
////				obj.bindPose = bindPose.bindPose[i];
////			}
//		}
		for (final Bone b : bones) {
			if ((b.geosetId != -1) && (b.geosetId < geosets.size())) {
				b.geoset = geosets.get(b.geosetId);
			}
			if ((b.geosetAnimId != -1) && (b.geosetAnimId < geosetAnims.size())) {
				b.geosetAnim = geosetAnims.get(b.geosetAnimId);
			}
		}
		for (int i = 0; i < cameras.size(); i++) {
			final Camera camera = cameras.get(i);
			if (bindPose != null) {
				camera.setBindPose(bindPose.bindPose[i + modelIdObjects.getIdObjectsSize()]);
			}
		}
	}

	public void updateObjectIds() {
		sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
		bindPose = null;
		for (final IdObject obj : modelIdObjects.allObjects) {
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

		final List<Bone> bones = getBones();
		final List<? extends Bone> helpers = getHelpers();
		bones.addAll(helpers);

		for (int i = 0; i < modelIdObjects.getIdObjectsSize(); i++) {
			final IdObject obj = modelIdObjects.getIdObject(i);
			obj.objectId = modelIdObjects.getObjectId(obj);
			obj.parentId = modelIdObjects.getObjectId(obj.getParent());
			if (obj.getBindPose() != null) {
				if (bindPose == null) {
					bindPose = new BindPose(modelIdObjects.getIdObjectsSize() + cameras.size());
				}
				bindPose.bindPose[i] = obj.getBindPose();
			}
		}
		for (final Bone b : bones) {
			b.geosetId = geosets.indexOf(b.geoset);
			b.geosetAnimId = geosetAnims.indexOf(b.geosetAnim);
		}
		for (int i = 0; i < cameras.size(); i++) {
			final Camera obj = cameras.get(i);
			if (obj.getBindPose() != null) {
				if (bindPose == null) {
					bindPose = new BindPose(modelIdObjects.getIdObjectsSize() + cameras.size());
				}
				bindPose.bindPose[i + modelIdObjects.getIdObjectsSize()] = obj.getBindPose();
			}
		}
	}

	public <T extends IdObject> List<? extends IdObject> sortedIdObjects(final Class<T> objectClass) {
		return modelIdObjects.getListByClass(objectClass);
	}

	public List<AnimFlag<?>> getAllAnimFlags() {
		// Probably will cause a bunch of lag, be wary
		final List<AnimFlag<?>> allFlags = Collections.synchronizedList(new ArrayList<>());
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				allFlags.addAll(lay.animFlags.values());
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa != null) {
					allFlags.addAll(texa.animFlags.values());
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga != null) {
					allFlags.addAll(ga.animFlags.values());
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}
//		for (final IdObject idObject : idObjects) {
		for (final IdObject idObject : modelIdObjects.allObjects) {
			allFlags.addAll(idObject.getAnimFlags());
		}
		if (cameras != null) {
			for (final Camera x : cameras) {
				allFlags.addAll(x.getSourceNode().getAnimFlags());
				allFlags.addAll(x.getTargetNode().getAnimFlags());
			}
		}

		return allFlags;
	}

	public List<VisibilitySource> getAllVis() {
		// Probably will cause a bunch of lag, be wary
		final List<VisibilitySource> allVis = Collections.synchronizedList(new ArrayList<>());
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
//				allVis.add(lay.getVisibilitySource());
				VisibilitySource vs = lay.getVisibilitySource();
				if (vs != null) {
					allVis.add(vs);
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa != null) {
					VisibilitySource vs = texa.getVisibilitySource();
					if (vs != null) {
						allVis.add(vs);
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga != null) {
					VisibilitySource vs = ga.getVisibilitySource();
					if (vs != null) {
						allVis.add(vs);
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}
//		for (final IdObject idObject : idObjects) {
		for (final IdObject idObject : modelIdObjects.allObjects) {
			VisibilitySource vs = idObject.getVisibilitySource();
			if (vs != null) {
				allVis.add(vs);
			}
		}
		if (cameras != null) {
			for (final Camera x : cameras) {
				VisibilitySource vs1 = x.getSourceNode().getVisibilitySource();
				if (vs1 != null) {
					allVis.add(vs1);
				}
				VisibilitySource vs2 = x.getTargetNode().getVisibilitySource();
				if (vs2 != null) {
					allVis.add(vs2);
				}
			}
		}

		return allVis;
	}

	public Object getAnimFlagSource(final AnimFlag<?> animFlag) {
		// Probably will cause a bunch of lag, be wary
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				final AnimFlag<?> timeline = lay.find(animFlag.getName());
				if (timeline != null) {
					return lay;
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim textureAnim : texAnims) {
				final AnimFlag<?> timeline = textureAnim.find(animFlag.getName());
				if (timeline != null) {
					return textureAnim;
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim geosetAnim : geosetAnims) {
				final AnimFlag<?> timeline = geosetAnim.find(animFlag.getName());
				if (timeline != null) {
					return geosetAnim;
				}
			}
		}

//		for (final IdObject object : idObjects) {
		for (final IdObject object : modelIdObjects.allObjects) {
			final AnimFlag<?> timeline = object.find(animFlag.getName());
			if (timeline != null) {
				return object;
			}
		}

		if (cameras != null) {
			for (final Camera x : cameras) {
				AnimFlag<?> timeline = x.getSourceNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}

				timeline = x.getTargetNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}
			}
		}

		return null;
	}

	public void buildGlobSeqFrom(final Animation anim) {
		buildGlobSeqFrom(anim, getAllAnimFlags());
	}

	public void addFlagToParent(final AnimFlag<?> aflg, final AnimFlag<?> added)
	// aflg is the parent
	{
		// ADDS "added" TO THE PARENT OF "aflg"
		for (final Material m : materials) {
			for (final Layer layer : m.layers) {
				if (layer.has(aflg.getName())) {
					layer.add(added);
				}
			}
		}

		if (texAnims != null) {
			for (final TextureAnim textureAnim : texAnims) {
				if (textureAnim.has(aflg.getName())) {
					textureAnim.add(added);
				}
			}
		}

		if (geosetAnims != null) {
			for (final GeosetAnim geosetAnim : geosetAnims) {
				if (geosetAnim.has(aflg.getName())) {
					geosetAnim.add(added);
				}
			}
		}

//		for (final IdObject object : idObjects) {
		for (final IdObject object : modelIdObjects.allObjects) {
			if (object.has(aflg.getName())) {
				object.add(added);
			}
		}

		if (cameras != null) {
			for (final Camera x : cameras) {
				if (x.getSourceNode().has(aflg.getName()) || x.targetAnimFlags.contains(aflg)) {
					x.getSourceNode().add(added);
				}
			}
		}
	}

	public void buildGlobSeqFrom(final Animation anim, final List<AnimFlag<?>> flags) {
		final Integer newSeq = anim.length();
		for (final AnimFlag<?> af : flags) {
			if (!af.hasGlobalSeq) {
				final AnimFlag<?> copy = AnimFlag.createFromAnimFlag(af);
				copy.setGlobSeq(newSeq);
				copy.copyFrom(af, anim.getStart(), anim.getEnd(), 0, anim.length());
				addFlagToParent(af, copy);
			}
		}
	}

	public List<VisibilitySource> getAllVisibilitySources() {
		final List<AnimFlag<?>> animFlags = getAllAnimFlags();// laggggg!
		final List<VisibilitySource> out = new ArrayList<>();
		for (final AnimFlag<?> af : animFlags) {
			if (af.getName().equals("Visibility") || af.getName().equals("Alpha")) {
				out.add((VisibilitySource) getAnimFlagSource(af));
			}
		}
		return out;
	}

	public int animTrackEnd() {
		int highestEnd = 0;
		for (final Animation a : anims) {
			if (a.getStart() > highestEnd) {
				highestEnd = a.getStart();
			}
			if (a.getEnd() > highestEnd) {
				highestEnd = a.getEnd();
			}
		}
		return highestEnd;
	}

	public GeosetAnim getGeosetAnimOfGeoset(final Geoset g) {
		if (g.geosetAnim == null) {
			boolean noIds = true;
			for (final GeosetAnim ga : geosetAnims) {
				if (ga.geoset != null) {
					noIds = false;
					break;
				}
			}
			if (noIds) {
				if (geosetAnims.size() > geosets.indexOf(g)) {
					g.geosetAnim = geosetAnims.get(geosets.indexOf(g));
				} else {
					return null;
				}
			} else {
				GeosetAnim temp = null;
				for (final GeosetAnim ga : geosetAnims) {
					if (ga.geoset == g) {
						temp = ga;
						break;
					}
				}
				g.geosetAnim = temp;
			}
		}
		return g.geosetAnim;
	}

	public void cureBoneGeoAnimIds() {
		if (DISABLE_BONE_GEO_ID_VALIDATOR) {
			return;
		}
//		final List<Bone> bones = (List<Bone>) sortedIdObjects(Bone.class);
		final List<Bone> bones = getBones();
		for (final Bone b : bones) {
			b.multiGeoId = false;
			b.geoset = null;
			b.geosetAnim = null;
		}
		for (final Geoset g : geosets) {
			final GeosetAnim ga = getGeosetAnimOfGeoset(g);
			for (final Matrix m : g.matrix) {
				for (final Bone bone : m.bones) {
					if (!bone.multiGeoId) {
						if (bone.geoset == null) {
							// The bone has been found by no prior matrices
							bone.geosetAnim = ga;
							bone.geoset = g;
						} else if (bone.geoset != g) {
							// The bone has only been found by ONE matrix
							bone.multiGeoId = true;
							bone.geoset = null;
							if (ga != null) {
								bone.geosetAnim = ga.getMostVisible(bone.geosetAnim);
							} else {
								bone.geosetAnim = null;
							}

						}
					} else if (ga != null) {
						if (ga != bone.geosetAnim) {
							bone.geosetAnim = ga.getMostVisible(bone.geosetAnim);
						}
					} else {
						bone.geosetAnim = null;
					}
					IdObject boneParent = bone.getParent();
					while (boneParent != null) {
						if (boneParent.getClass() == Bone.class) {
							final Bone b2 = (Bone) boneParent;
							if (!b2.multiGeoId) {
								if (b2.geoset == null) {
									// The bone has been found by no prior matrices
									b2.geosetAnim = ga;
									b2.geoset = g;
								} else if (b2.geoset != g) {
									// The bone has only been found by ONE matrix
									b2.multiGeoId = true;
									b2.geoset = null;
									if (ga != null) {
										b2.geosetAnim = ga.getMostVisible(b2.geosetAnim);
										if (b2.geosetAnim != null) {
											b2.geoset = b2.geosetAnim.geoset;
											b2.multiGeoId = false;
										}
									}

								}
							} else if ((ga != null) && (ga != b2.geosetAnim)) {
								b2.geosetAnim = ga.getMostVisible(b2.geosetAnim);
							}
						}
						boneParent = boneParent.getParent();
					}
				}
			}
		}
	}

	public void visit(final MeshVisitor renderer) {
		int geosetId = 0;
		for (final Geoset geoset : geosets) {
			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(), geoset.getGeosetAnim());
			visitVert(geoset, geosetRenderer, isHd(geoset));
			geosetRenderer.geosetFinished();
		}
	}

	public boolean isHd(Geoset geoset) {
		return (ModelUtils.isTangentAndSkinSupported(formatVersion))
				&& (geoset.getVertices().size() > 0)
				&& (geoset.getVertex(0).getSkinBoneBones() != null);
	}

	private void visitVert(Geoset geoset, GeosetVisitor geosetRenderer, boolean isHD) {
		for (final Triangle triangle : geoset.getTriangles()) {
			final TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
			for (final GeosetVertex vertex : triangle.getVerts()) {
				final VertexVisitor vertexRenderer;
				// TODO redesign for nullable normals
				Vec3 normal = vertex.getNormal() == null ? new Vec3(0, 0, 0) : vertex.getNormal();
				if (isHD) {
					vertexRenderer = triangleRenderer.hdVertex(vertex, normal, vertex.getSkinBoneBones(), vertex.getSkinBoneWeights());
				} else {
					vertexRenderer = triangleRenderer.vertex(vertex, normal, vertex.getBoneAttachments());
				}
				for (final Vec2 tvert : vertex.getTverts()) {
					vertexRenderer.textureCoords(tvert.x, tvert.y);
				}
				vertexRenderer.vertexFinished();
			}
			triangleRenderer.triangleFinished();
		}
	}

	public void render(final ModelVisitor renderer) {
		visit(renderer);
//		for (final IdObject object : idObjects) {
		for (final IdObject object : modelIdObjects.allObjects) {
			object.apply(renderer);
		}
		for (final Camera camera : cameras) {
			renderer.camera(camera);
		}
	}

	public void simplifyKeyframes() {
		final EditableModel currentMDL = this;
		final List<AnimFlag<?>> allAnimFlags = currentMDL.getAllAnimFlags();
		final List<Animation> anims = currentMDL.getAnims();

		for (final AnimFlag<?> flag : allAnimFlags) {
			final List<Integer> indicesForDeletion = new ArrayList<>();
			Entry<?> lastEntry = null;
			for (int i = 0; i < flag.size(); i++) {
				final Entry<?> entry = flag.getEntry(i);
				if ((lastEntry != null) && (lastEntry.time.equals(entry.time))) {
					indicesForDeletion.add(i);
				}
				lastEntry = entry;
			}
			for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
				flag.deleteAt(indicesForDeletion.get(i));
			}
		}
		for (final Animation anim : anims) {
			for (final AnimFlag<?> flag : allAnimFlags) {
				if (!flag.hasGlobalSeq()) {
					Object olderKeyframe = null;
					Object oldKeyframe = null;
					final List<Integer> indicesForDeletion = new ArrayList<>();
					for (int i = 0; i < flag.size(); i++) {
						final Entry<?> entry = flag.getEntry(i);
						//
						// //Types of AnimFlags:
						// // 0 Alpha
						// public static final int ALPHA = 0;
						// // 1 Scaling
						// public static final int SCALING = 1;
						// // 2 Rotation
						// public static final int ROTATION = 2;
						// // 3 Translation
						// public static final int TRANSLATION = 3;
						// // 4 Color
						// public static final int COLOR = 4;
						// // 5 TextureID
						// public static final int TEXTUREID = 5;
						if ((entry.time >= anim.getStart()) && (entry.time <= anim.getEnd())) {
							if (entry.value instanceof Float) {
								final Float d = (Float) entry.value;
								final Float older = (Float) olderKeyframe;
								final Float old = (Float) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older, d, old)) {
									indicesForDeletion.add(i - 1);
								}
							} else if (entry.value instanceof Vec3) {
								final Vec3 current = (Vec3) entry.value;
								final Vec3 older = (Vec3) olderKeyframe;
								final Vec3 old = (Vec3) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older.x, current.x, old.x)
										&& MathUtils.isBetween(older.y, current.y, old.y)
										&& MathUtils.isBetween(older.z, current.z, old.z)) {
									indicesForDeletion.add(i - 1);
								}
							} else if (entry.value instanceof Quat) {
								final Quat current = (Quat) entry.value;
								final Quat older = (Quat) olderKeyframe;
								final Quat old = (Quat) oldKeyframe;
								final Vec3 euler = current.toEuler();
								if ((older != null) && (old != null)) {
									final Vec3 olderEuler = older.toEuler();
									final Vec3 oldEuler = old.toEuler();
									if (MathUtils.isBetween(olderEuler.x, euler.x, oldEuler.x)
											&& MathUtils.isBetween(olderEuler.y, euler.y, oldEuler.y)
											&& MathUtils.isBetween(olderEuler.z, euler.z, oldEuler.z)) {
										indicesForDeletion.add(i - 1);
									}
								}
							}
							olderKeyframe = oldKeyframe;
							oldKeyframe = entry.value;
						}
					}
					for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
						flag.deleteAt(indicesForDeletion.get(i));
					}
				}
			}
		}
		for (final Integer globalSeq : currentMDL.getGlobalSeqs()) {
			for (final AnimFlag<?> flag : allAnimFlags) {
				if (flag.hasGlobalSeq() && flag.getGlobalSeq().equals(globalSeq)) {
					Object olderKeyframe = null;
					Object oldKeyframe = null;
					final List<Integer> indicesForDeletion = new ArrayList<>();
					for (int i = 0; i < flag.size(); i++) {
						final Entry entry = flag.getEntry(i);
						//
						// //Types of AnimFlags:
						// // 0 Alpha
						// public static final int ALPHA = 0;
						// // 1 Scaling
						// public static final int SCALING = 1;
						// // 2 Rotation
						// public static final int ROTATION = 2;
						// // 3 Translation
						// public static final int TRANSLATION = 3;
						// // 4 Color
						// public static final int COLOR = 4;
						// // 5 TextureID
						// public static final int TEXTUREID = 5;
						if (entry.value instanceof Float) {
							final Float d = (Float) entry.value;
							final Float older = (Float) olderKeyframe;
							final Float old = (Float) oldKeyframe;
							if ((older != null) && (old != null) && MathUtils.isBetween(older, d, old)) {
								indicesForDeletion.add(i - 1);
							}
						} else if (entry.value instanceof Vec3) {
							final Vec3 current = (Vec3) entry.value;
							final Vec3 older = (Vec3) olderKeyframe;
							final Vec3 old = (Vec3) oldKeyframe;
							if ((older != null) && (old != null) && MathUtils.isBetween(older.x, current.x, old.x)
									&& MathUtils.isBetween(older.y, current.y, old.y)
									&& MathUtils.isBetween(older.z, current.z, old.z)) {
								indicesForDeletion.add(i - 1);
							}
						} else if (entry.value instanceof Quat) {
							final Quat current = (Quat) entry.value;
							final Quat older = (Quat) olderKeyframe;
							final Quat old = (Quat) oldKeyframe;
							final Vec3 euler = current.toEuler();
							if ((older != null) && (old != null)) {
								final Vec3 olderEuler = older.toEuler();
								final Vec3 oldEuler = old.toEuler();
								if (MathUtils.isBetween(olderEuler.x, euler.x, oldEuler.x)
										&& MathUtils.isBetween(olderEuler.y, euler.y, oldEuler.y)
										&& MathUtils.isBetween(olderEuler.z, euler.z, oldEuler.z)) {
									indicesForDeletion.add(i - 1);
								}
							}
						}
						olderKeyframe = oldKeyframe;
						oldKeyframe = entry.value;
					}
					for (int i = indicesForDeletion.size() - 1; i >= 0; i--) {
						flag.deleteAt(indicesForDeletion.get(i));
					}
				}
			}
		}
	}

	public void removeAllTimelinesForGlobalSeq(final Integer selectedValue) {
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				lay.removeAllTimelinesForGlobalSeq(selectedValue);
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa != null) {
					texa.removeAllTimelinesForGlobalSeq(selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga != null) {
					ga.removeAllTimelinesForGlobalSeq(selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}

//		for (final IdObject object : idObjects) {
		for (final IdObject object : modelIdObjects.allObjects) {
			object.removeAllTimelinesForGlobalSeq(selectedValue);
		}

		if (cameras != null) {
			for (final Camera x : cameras) {
				x.getSourceNode().removeAllTimelinesForGlobalSeq(selectedValue);
				x.getTargetNode().removeAllTimelinesForGlobalSeq(selectedValue);
			}
		}
	}

	/**
	 * Copies the animations from another model into this model. Specifically,
	 * copies all motion from similarly named bones and copies in the "Anim" blocks
	 * at the top of the MDL for the newly added sections.
	 *
	 * In addition, any bones with significant amounts of motion that were not found
	 * to correlate with the contents of this model get added to this model's list
	 * of bones.
	 */
	public void addAnimationsFrom(EditableModel other) {
		// this process destroys the "other" model inside memory, so destroy a copy instead
		other = EditableModel.deepClone(other, "animation source file");

		final List<AnimFlag<?>> flags = getAllAnimFlags();
//		final List<EventObject> eventObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> eventObjs = getEvents();

		final List<AnimFlag<?>> othersFlags = other.getAllAnimFlags();
//		final List<EventObject> othersEventObjs = (List<EventObject>) other.sortedIdObjects(EventObject.class);
		final List<EventObject> othersEventObjs = other.getEvents();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to the information specific to
		// each node about how it will move if it gets translated into or onto the current model

		final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		for (final AnimFlag<?> af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(AnimFlag.createFromAnimFlag(af));
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with the exact same data, but shifted
		// forward relative to wherever the current model's last animation starts
		for (final Animation anim : other.anims) {
			final int animTrackEnd = animTrackEnd();
			final int newStart = animTrackEnd + 300;
			final int newEnd = newStart + anim.length();
			final Animation newAnim = new Animation(anim);
			// clone the animation from the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			add(newAnim); // add the new animation to this model
		}

		// destroy the other model's animations, filling them in with the new stuff
		for (final AnimFlag<?> af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current
		// model
		final List<Bone> leftBehind = new ArrayList<>();
		// the bones that don't find matches in current model
//		for (final IdObject object : other.idObjects) {
		for (final IdObject object : other.modelIdObjects.allObjects) {
			if (object instanceof Bone) {
				// the bone from the other model
				final Bone bone = (Bone) object;
				// the object in this model of similar name
				final Object localObject = getObject(bone.getName());
				if ((localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone); // if it's a match, take the data
				} else {
					leftBehind.add(bone);
				}
			}
		}
		for (final Bone bone : leftBehind) {
			if (bone.animates()) {
				add(bone);
			}
		}

		// i think we're done????
	}


	public void setGlobalSequenceLength(final int globalSequenceId, final Integer newLength) {
		if (globalSequenceId < globalSeqs.size()) {
			final Integer prevLength = globalSeqs.get(globalSequenceId);
			final List<AnimFlag<?>> allAnimFlags = getAllAnimFlags();
			for (final AnimFlag<?> af : allAnimFlags) {
				if ((af.getGlobalSeq() != null) && af.hasGlobalSeq()) {// TODO eliminate redundant structure
					if (af.getGlobalSeq().equals(prevLength)) {
						af.setGlobalSeq(newLength);
					}
				}
			}
//			final List<EventObject> sortedEventObjects = (List<EventObject>) sortedIdObjects(EventObject.class);
			final List<EventObject> sortedEventObjects = getEvents();
			for (final EventObject eventObject : sortedEventObjects) {
				// TODO eliminate redundant structure
				if (eventObject.isHasGlobalSeq() && (eventObject.getGlobalSeq() != null)) {
					if (eventObject.getGlobalSeq().equals(prevLength)) {
						eventObject.setGlobalSeq(newLength);
					}
				}
			}
			globalSeqs.set(globalSequenceId, newLength);
		}
	}


	public Bone getBone(final int index) {
		try {
			if (index < modelIdObjects.getIdObjectsSize()) {
				final IdObject temp = modelIdObjects.getIdObject(index);
				if (temp.getClass() == Bone.class) {
					return (Bone) temp;
				}
			}
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Bone reference broken or invalid!");
		}
		return null;
	}


	public boolean isTemp() {
		return temporary;
	}

	public void setTemp(final boolean flag) {
		temporary = flag;
	}

	public IdObject getIdObject(final int index) {
		return modelIdObjects.getIdObject(index);
	}

	public Geoset getGeoset(final int index) {
		return geosets.get(index);
	}

	public int getGeosetId(final Geoset g) {
		return geosets.indexOf(g);
	}

	public int getGeosetsSize() {
		return geosets.size();
	}

	public int getAnimsSize() {
		return anims.size();
	}

	public int getIdObjectsSize() {
		return modelIdObjects.getIdObjectsSize();
	}


	public void remove(final Bitmap texture) {
		// remove a texture, replacing with "Textures\\white.blp" if necessary.
		final Bitmap replacement = new Bitmap("Textures\\white.blp");
		textures.remove(texture);
		for (final Material material : materials) {
			for (final Layer layer : material.getLayers()) {
				if (layer.getTextureBitmap().equals(texture)) {
					layer.setTexture(replacement);
				} else {
					if ((layer.getTextures() != null) && layer.getTextures().contains(texture)) {
						for (int i = 0; i < layer.getTextures().size(); i++) {
							if (layer.getTextures().get(i).equals(texture)) {
								layer.getTextures().set(i, replacement);
							}
						}
					}
				}
			}
		}
//		for (final ParticleEmitter2 emitter : (List<ParticleEmitter2>)sortedIdObjects(ParticleEmitter2.class)) {
		for (final ParticleEmitter2 emitter : getParticleEmitter2s()) {
			if (emitter.getTexture().equals(texture)) {
				emitter.setTexture(replacement);
			}
		}
	}

	public void sortIdObjects() {
		modelIdObjects.sort();
	}


	public void add(final Camera x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Camera component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			cameras.add(x);
			if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPose != null)) {
				if (x.getBindPose() == null) {
					x.setBindPose(new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
				}
			}
		}
	}

	public void add(final Animation x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Anim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			anims.add(x);
		}
	}

	public void add(final Integer x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null GlobalSeq component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			globalSeqs.add(x);
		}
	}

	public void add(final Bitmap x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Bitmap component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			textures.add(x);
		}
	}

	public void add(final Material x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Material component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			materials.add(x);
		}
	}

	public void add(final TextureAnim x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null TextureAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			texAnims.add(x);
		}
	}

	public void add(final Geoset x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Geoset component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			x.parentModel = this;
			geosets.add(x);
		}
	}

	public void add(final GeosetVertex x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null GeosetVertex component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			if (!contains(x.geoset)) {
				add(x.geoset);
			}
			x.geoset.add(x);
		}
	}

	public void add(final Triangle x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Triangle component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			if (!contains(x.geoset)) {
				add(x.geoset);
			}
			x.geoset.add(x);
		}
	}

	public void add(final IdObject x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null IdObject component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			modelIdObjects.addIdObject(x);
//			idObjects.add(x);
			if ((x.pivotPoint != null) && !pivots.contains(x.pivotPoint)) {
				pivots.add(x.pivotPoint);
			}
			if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPose != null)) {
				if (x.getBindPose() == null) {
					x.setBindPose(new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
				}
			}
		}
	}

	public void add(final GeosetAnim x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null GeosetAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			geosetAnims.add(x);
		}
	}

	public boolean contains(final Animation x) {
		return anims.contains(x);
	}

	public boolean contains(final Integer x) {
		return globalSeqs.contains(x);
	}

	public boolean contains(final Bitmap x) {
		return textures.contains(x);
	}

	public boolean contains(final Material x) {
		return materials.contains(x);
	}

	public boolean contains(final TextureAnim x) {
		return texAnims.contains(x);
	}

	public boolean contains(final Geoset x) {
		return geosets.contains(x);
	}

	public boolean contains(final GeosetAnim x) {
		return geosetAnims.contains(x);
	}

	public boolean contains(final Camera x) {
		return cameras.contains(x);
	}

	public void remove(final Camera camera) {
		cameras.remove(camera);
	}

	public void remove(final Geoset g) {
		geosets.remove(g);
	}

	public void remove(final GeosetAnim g) {
		geosetAnims.remove(g);
	}

	public void remove(final Animation a) {
		anims.remove(a);
	}

	public File getFileRef() {
		return fileRef;
	}

	public void setFileRef(final File file) {
		fileRef = file;
		if (fileRef != null) {
			wrappedDataSource = new CompoundDataSource(
					Arrays.asList(GameDataFileSystem.getDefault(), new FolderDataSource(file.getParentFile().toPath())));
		} else {
			wrappedDataSource = GameDataFileSystem.getDefault();
		}
	}

	public int getBlendTime() {
		return blendTime;
	}

	public void setBlendTime(final int blendTime) {
		this.blendTime = blendTime;
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public int getFormatVersion() {
		return formatVersion;
	}

	public void setFormatVersion(final int formatVersion) {
		this.formatVersion = formatVersion;
	}

	public ArrayList<String> getHeader() {
		return header;
	}

	public void setHeader(final ArrayList<String> header) {
		this.header = header;
	}

	public List<Animation> getAnims() {
		return anims;
	}

	public void setAnims(final List<Animation> anims) {
		this.anims = anims;
	}

	public List<Integer> getGlobalSeqs() {
		return globalSeqs;
	}

	public void setGlobalSeqs(final List<Integer> globalSeqs) {
		this.globalSeqs = globalSeqs;
	}

	public List<Bitmap> getTextures() {
		return textures;
	}

	public void setTextures(final List<Bitmap> textures) {
		this.textures = textures;
	}

	public List<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(final List<Material> materials) {
		this.materials = materials;
	}

	public List<TextureAnim> getTexAnims() {
		return texAnims;
	}

	public void setTexAnims(final List<TextureAnim> texAnims) {
		this.texAnims = texAnims;
	}

	public List<Geoset> getGeosets() {
		return geosets;
	}

	public void setGeosets(final List<Geoset> geosets) {
		this.geosets = geosets;
	}

	public List<GeosetAnim> getGeosetAnims() {
		return geosetAnims;
	}

	public void setGeosetAnims(final List<GeosetAnim> geosetAnims) {
		this.geosetAnims = geosetAnims;
	}

	public boolean contains(final IdObject x) {
		return modelIdObjects.contains(x);
	}

	public void remove(final IdObject o) {
		modelIdObjects.removeIdObject(o);
	}

	public List<Vec3> getPivots() {
		return pivots;
	}

	public void setPivots(final List<Vec3> pivots) {
		this.pivots = pivots;
	}

	public List<Camera> getCameras() {
		return cameras;
	}

	public void setCameras(final List<Camera> cameras) {
		this.cameras = cameras;
	}

	public List<IdObject> getIdObjects() {
		return new ArrayList<>(modelIdObjects.allObjects);
	}

	public List<FaceEffect> getFaceEffects() {
		return faceEffects;
	}

	public BindPose getBindPoseChunk() {
		return bindPose;
	}

	public void setBindPoseChunk(final BindPose bindPoseChunk) {
		bindPose = bindPoseChunk;
	}

	public void clearAnimations() {
		anims.clear();
	}

	public IdObject getObject(final String name) {
		return modelIdObjects.getObject(name);
	}

	public void clearGeosets() {
		geosets.clear();
	}

	public int getObjectId(final IdObject idObject) {
		return modelIdObjects.getObjectId(idObject);
	}

	public void addToHeader(final String comment) {
		header.add(comment);
	}

	public void addAnimation(final Animation a) {
		anims.add(a);
	}

	public Animation getAnim(final int index) {
		return anims.get(index);
	}

	public Animation findAnimByName(final String name) {
		for (final Animation anim : anims) {
			if (anim.getName().toLowerCase().contains(name.toLowerCase())) {
				return anim;
			}
		}
		return null;
	}

	public void addGlobalSeq(final int i) {
		globalSeqs.add(i);
	}

	public int getGlobalSeqId(final Integer inte) {
		return globalSeqs.indexOf(inte);
	}

	public Integer getGlobalSeq(final int id) {
		return globalSeqs.get(id);
	}

	public void addTexture(final Bitmap b) {
		textures.add(b);
	}

	public Bitmap getTexture(final int index) {
		return textures.get(index);
	}

	public Bitmap getTexture(final String path) {
		for (final Bitmap texture : textures) {
			if (texture.getPath().equals(path)) {
				return texture;
			}
		}
		return null;
	}

	public Bitmap loadTexture(final String path) {
		Bitmap texture = getTexture(path);

		if (texture == null) {
			texture = new Bitmap(path);
			add(texture);
		}

		return texture;
	}

	public int getTextureId(final Bitmap b) {
		if (b == null) {
			return -1;
		}
		return textures.indexOf(b);
	}

	public int getTextureAnimId(final TextureAnim texa) {
		return texAnims.indexOf(texa);
	}

	public void addMaterial(final Material x) {
		materials.add(x);
	}

	public Material getMaterial(final int i) {
		if (i >= 0 && i < materials.size()) {
			return materials.get(i);
		}

		return null;
	}

	public void addSound(final SoundFile sound) {
		sounds.add(sound);
	}

	public SoundFile getSound(final int index) {
		return sounds.get(index);
	}

	public void addGeosetAnim(final GeosetAnim x) {
		geosetAnims.add(x);
	}

	public GeosetAnim getGeosetAnim(final int index) {
		return geosetAnims.get(index);
	}

	public void addCamera(final Camera x) {
		cameras.add(x);
	}

	public void addFaceEffect(final FaceEffect faceEffect) {
		faceEffects.add(faceEffect);
	}

	private void addIdObject(final IdObject x) {
		modelIdObjects.addIdObject(x);
//		idObjects.add(x);
	}

	public void addPivotPoint(final Vec3 x) {
		pivots.add(x);
	}

	public void addGeoset(final Geoset g) {
		geosets.add(g);
	}

	public int computeMaterialID(final Material material) {
		return materials.indexOf(material);
	}

	public int computeGeosetID(final Geoset geoset) {
		return geosets.indexOf(geoset);
	}

	public List<IdObject> getAllObjects() {
		return new ArrayList<>(modelIdObjects.allObjects);
	}

	public List<Bone> getBones() {
		return new ArrayList<>(modelIdObjects.bones);
	}

	public List<Light> getLights() {
		return new ArrayList<>(modelIdObjects.lights);
	}

	public List<Helper> getHelpers() {
		return new ArrayList<>(modelIdObjects.helpers);
	}

	public List<Attachment> getAttachments() {
		return new ArrayList<>(modelIdObjects.attachments);
	}

	public List<ParticleEmitter> getParticleEmitters() {
		return new ArrayList<>(modelIdObjects.particleEmitters);
	}

	public List<ParticleEmitter2> getParticleEmitter2s() {
		return new ArrayList<>(modelIdObjects.particleEmitter2s);
	}

	public List<ParticleEmitterPopcorn> getPopcornEmitters() {
		return new ArrayList<>(modelIdObjects.popcornEmitters);
	}

	public List<RibbonEmitter> getRibbonEmitters() {
		return new ArrayList<>(modelIdObjects.ribbonEmitters);
	}

	public List<EventObject> getEvents() {
		return new ArrayList<>(modelIdObjects.events);
	}

	public List<CollisionShape> getColliders() {
		return new ArrayList<>(modelIdObjects.colliders);
	}

	Map<Integer, IdObject> getIdToIdObjectMap() {
		return modelIdObjects.getIdToIdObjectMap();
	}

	Map<IdObject, Integer> getIdObjectToIdMap() {
		return modelIdObjects.getIdObjectToIdMap();
	}

	public void clearAllIdObjects() {
		modelIdObjects.clearAll();
	}

	private static class ModelIdObjects {
		final List<IdObject> allObjects;
		final List<Bone> bones;
		final List<Light> lights;
		final List<Helper> helpers;
		final List<Attachment> attachments;
		final List<ParticleEmitter> particleEmitters;
		final List<ParticleEmitter2> particleEmitter2s;
		final List<ParticleEmitterPopcorn> popcornEmitters;
		final List<RibbonEmitter> ribbonEmitters;
		final List<EventObject> events;
		final List<CollisionShape> colliders;

		Map<Integer, IdObject> idToIdObjectMap;
		Map<IdObject, Integer> idObjectToIdMap;


		ModelIdObjects() {
			allObjects = new ArrayList<>();
			bones = new ArrayList<>();
			lights = new ArrayList<>();
			helpers = new ArrayList<>();
			attachments = new ArrayList<>();
			particleEmitters = new ArrayList<>();
			particleEmitter2s = new ArrayList<>();
			popcornEmitters = new ArrayList<>();
			ribbonEmitters = new ArrayList<>();
			events = new ArrayList<>();
			colliders = new ArrayList<>();
		}

		void addIdObject(IdObject idObject) {
			if (idObject instanceof Light) {
				lights.add((Light) idObject);
			} else if (idObject instanceof Helper) {
				helpers.add((Helper) idObject);
			} else if (idObject instanceof Bone) {
//				System.out.println("adding Bone");
				bones.add((Bone) idObject);
			} else if (idObject instanceof Attachment) {
				attachments.add((Attachment) idObject);
			} else if (idObject instanceof ParticleEmitter) {
				particleEmitters.add((ParticleEmitter) idObject);
			} else if (idObject instanceof ParticleEmitter2) {
				particleEmitter2s.add((ParticleEmitter2) idObject);
			} else if (idObject instanceof ParticleEmitterPopcorn) {
				popcornEmitters.add((ParticleEmitterPopcorn) idObject);
			} else if (idObject instanceof RibbonEmitter) {
				ribbonEmitters.add((RibbonEmitter) idObject);
			} else if (idObject instanceof EventObject) {
				events.add((EventObject) idObject);
			} else if (idObject instanceof CollisionShape) {
				colliders.add((CollisionShape) idObject);
			}
			allObjects.add(idObject);
			idToIdObjectMap = null;
			idObjectToIdMap = null;
		}

		void removeIdObject(IdObject idObject) {
			if (idObject instanceof Light) {
				lights.remove(idObject);
			} else if (idObject instanceof Helper) {
				helpers.remove(idObject);
			} else if (idObject instanceof Bone) {
				bones.remove(idObject);
			} else if (idObject instanceof Attachment) {
				attachments.remove(idObject);
			} else if (idObject instanceof ParticleEmitter) {
				particleEmitters.remove(idObject);
			} else if (idObject instanceof ParticleEmitter2) {
				particleEmitter2s.remove(idObject);
			} else if (idObject instanceof ParticleEmitterPopcorn) {
				popcornEmitters.remove(idObject);
			} else if (idObject instanceof RibbonEmitter) {
				ribbonEmitters.remove(idObject);
			} else if (idObject instanceof EventObject) {
				events.remove(idObject);
			} else if (idObject instanceof CollisionShape) {
				colliders.remove(idObject);
			}
			allObjects.remove(idObject);
			idToIdObjectMap = null;
			idObjectToIdMap = null;
		}

		private void sort() {
			allObjects.clear();
			allObjects.addAll(bones);
			allObjects.addAll(lights);
			allObjects.addAll(helpers);
			allObjects.addAll(attachments);
			allObjects.addAll(particleEmitters);
			allObjects.addAll(particleEmitter2s);
			allObjects.addAll(popcornEmitters);
			allObjects.addAll(ribbonEmitters);
			allObjects.addAll(events);
			allObjects.addAll(colliders);
		}

		void clearAll() {
			allObjects.clear();
			bones.clear();
			lights.clear();
			helpers.clear();
			attachments.clear();
			particleEmitters.clear();
			particleEmitter2s.clear();
			popcornEmitters.clear();
			ribbonEmitters.clear();
			events.clear();
			colliders.clear();

			idToIdObjectMap = null;
			idObjectToIdMap = null;
		}

		List<IdObject> getSorted() {
			sort();
			return allObjects;
		}

		//		IdObject getIdObject(int index) {
//			return allObjects.get(index);
//		}
		IdObject getIdObject(int index) {
			return getIdToIdObjectMap().get(index);
		}

		IdObject getObject(final String name) {
			for (final IdObject obj : allObjects) {
				if (obj.name.equalsIgnoreCase(name)) {
					return obj;
				}
			}
			return null;
		}

		int getObjectId(final IdObject idObject) {
//			System.out.println("allObjects.size(): " + allObjects.size());
			return getIdObjectToIdMap().getOrDefault(idObject, -1);
		}

		Map<Integer, IdObject> getIdToIdObjectMap() {
			if (idToIdObjectMap == null) {
				sort();
				idToIdObjectMap = new HashMap<>();
				for (int i = 0; i < allObjects.size(); i++) {
					idToIdObjectMap.put(i, allObjects.get(i));
				}
			}
			return idToIdObjectMap;
		}

		Map<IdObject, Integer> getIdObjectToIdMap() {
			if (idObjectToIdMap == null) {
				sort();
				idObjectToIdMap = new HashMap<>();
				for (int i = 0; i < allObjects.size(); i++) {
					idObjectToIdMap.put(allObjects.get(i), i);
				}
			}
			return idObjectToIdMap;
		}

		int getIdObjectsSize() {
			return allObjects.size();
		}

		boolean contains(final IdObject idObject) {
			if (idObjectToIdMap != null) {
				return idObjectToIdMap.containsKey(idObject);
			}
			if (idObject instanceof Light) {
				return lights.contains(idObject);
			} else if (idObject instanceof Helper) {
				return helpers.contains(idObject);
			} else if (idObject instanceof Bone) {
				return bones.contains(idObject);
			} else if (idObject instanceof Attachment) {
				return attachments.contains(idObject);
			} else if (idObject instanceof ParticleEmitter) {
				return particleEmitters.contains(idObject);
			} else if (idObject instanceof ParticleEmitter2) {
				return particleEmitter2s.contains(idObject);
			} else if (idObject instanceof ParticleEmitterPopcorn) {
				return popcornEmitters.contains(idObject);
			} else if (idObject instanceof RibbonEmitter) {
				return ribbonEmitters.contains(idObject);
			} else if (idObject instanceof EventObject) {
				return events.contains(idObject);
			} else if (idObject instanceof CollisionShape) {
				return colliders.contains(idObject);
			}
			return false;
		}

		public <T extends IdObject> List<? extends IdObject> getListByClass(final Class<T> objectClass) {
			if (objectClass.equals(Light.class)) {
				return new ArrayList<>(lights);
			} else if (objectClass.equals(Helper.class)) {
				return new ArrayList<>(helpers);
			} else if (objectClass.equals(Bone.class)) {
				return new ArrayList<>(bones);
			} else if (objectClass.equals(Attachment.class)) {
				return new ArrayList<>(attachments);
			} else if (objectClass.equals(ParticleEmitter.class)) {
				return new ArrayList<>(particleEmitters);
			} else if (objectClass.equals(ParticleEmitter2.class)) {
				return new ArrayList<>(particleEmitter2s);
			} else if (objectClass.equals(ParticleEmitterPopcorn.class)) {
				return new ArrayList<>(popcornEmitters);
			} else if (objectClass.equals(RibbonEmitter.class)) {
				return new ArrayList<>(ribbonEmitters);
			} else if (objectClass.equals(EventObject.class)) {
				return new ArrayList<>(events);
			} else if (objectClass.equals(CollisionShape.class)) {
				return new ArrayList<>(colliders);
			}
			return null;
		}
	}
}
