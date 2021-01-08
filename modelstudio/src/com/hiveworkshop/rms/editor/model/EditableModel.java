package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.AnimFlag.Entry;
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
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file
 * format).
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
	protected List<IdObject> idObjects = new ArrayList<>();
	protected List<Vec3> pivots = new ArrayList<>();
	protected List<Camera> cameras = new ArrayList<>();
	private final List<FaceEffect> faceEffects = new ArrayList<>();
	private BindPose bindPose;
	private boolean temporary;
	private DataSource wrappedDataSource = GameDataFileSystem.getDefault();

	public EditableModel() {

	}

	public EditableModel(final String newName) {
		name = newName;
	}

	public EditableModel(final EditableModel other) {
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
		idObjects = new ArrayList<>(other.idObjects);
		pivots = new ArrayList<>(other.pivots);
		cameras = new ArrayList<>(other.cameras);
	}

	public EditableModel(final MdlxModel model) {
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the
		// MDL class
		setFormatVersion(model.version);
		setName(model.name);
		setBlendTime((int)model.blendTime);
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
		// convert "IdObjects" as I called them in my high school mdl code
		// (nodes)

		// Bones
		for (final MdlxBone bone : model.bones) {
			add(new Bone(bone));
		}
		
		// Lights
		for (final MdlxLight light : model.lights) {
			add(new Light(light));
		}

		// Helpers
		for (final MdlxHelper helper : model.helpers) {
			add(new Helper(helper));
		}

		// Attachment
		for (final MdlxAttachment attachment : model.attachments) {
			add(new Attachment(attachment));
		}

		// ParticleEmitter (number 1 kind)
		for (final MdlxParticleEmitter emitter : model.particleEmitters) {
			add(new ParticleEmitter(emitter));
		}

		// ParticleEmitter2
		for (final MdlxParticleEmitter2 emitter : model.particleEmitters2) {
			add(new ParticleEmitter2(emitter));
		}

		// PopcornFxEmitter
		for (final MdlxParticleEmitterPopcorn emitter : model.particleEmittersPopcorn) {
			add(new ParticleEmitterPopcorn(emitter));
		}

		// RibbonEmitter
		for (final MdlxRibbonEmitter emitter : model.ribbonEmitters) {
			add(new RibbonEmitter(emitter));
		}

		// EventObject
		for (final MdlxEventObject object : model.eventObjects) {
			add(new EventObject(object));
		}

		for (final MdlxCamera camera : model.cameras) {
			add(new Camera(camera));
		}

		// CollisionShape
		for (final MdlxCollisionShape shape : model.collisionShapes) {
			add(new CollisionShape(shape));
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

		doPostRead(); // fixes all the things
	}

	public EditableModel(final AiScene scene) {
		System.out.println("IMPLEMENT EditableModel(AiScene)");

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

	public MdlxModel toMdlx() {
		doSavePreps(); // restores all GeosetID, ObjectID, TextureID,
		// MaterialID stuff all based on object references in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())" without a problem, or even
		// "mdl.add(otherMdl.getGeoset(5))" and have the geoset's textures and
		// materials  all be carried over with it via object references in java

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

		for (final Bone bone : sortedIdObjects(Bone.class)) {
			model.bones.add(bone.toMdlx());
		}

		for (final Light light : sortedIdObjects(Light.class)) {
			model.lights.add(light.toMdlx());
		}

		for (final Helper helper : sortedIdObjects(Helper.class)) {
			model.helpers.add(helper.toMdlxHelper());
		}

		for (final Attachment attachment : sortedIdObjects(Attachment.class)) {
			model.attachments.add(attachment.toMdlx());
		}

		for (final ParticleEmitter emitter : sortedIdObjects(ParticleEmitter.class)) {
			model.particleEmitters.add(emitter.toMdlx());
		}

		for (final ParticleEmitter2 emitter : sortedIdObjects(ParticleEmitter2.class)) {
			model.particleEmitters2.add(emitter.toMdlx());
		}

		for (final ParticleEmitterPopcorn emitter : sortedIdObjects(ParticleEmitterPopcorn.class)) {
			model.particleEmittersPopcorn.add(emitter.toMdlx());
		}

		for (final RibbonEmitter emitter : sortedIdObjects(RibbonEmitter.class)) {
			model.ribbonEmitters.add(emitter.toMdlx());
		}

		for (final EventObject object : sortedIdObjects(EventObject.class)) {
			model.eventObjects.add(object.toMdlx());
		}

		for (final Camera camera : getCameras()) {
			model.cameras.add(camera.toMdlx());
		}

		for (final CollisionShape shape : sortedIdObjects(CollisionShape.class)) {
			model.collisionShapes.add(shape.toMdlx());
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

	public void setFileRef(final File file) {
		fileRef = file;
		if (fileRef != null) {
			wrappedDataSource = new CompoundDataSource(
					Arrays.asList(GameDataFileSystem.getDefault(), new FolderDataSource(file.getParentFile().toPath())));
		} else {
			wrappedDataSource = GameDataFileSystem.getDefault();
		}
	}

	public boolean isTemp() {
		return temporary;
	}

	public void setTemp(final boolean flag) {
		temporary = flag;
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

	public void clearToHeader() {
		anims.clear();
		globalSeqs.clear();
		textures.clear();
		materials.clear();
		texAnims.clear();
		geosets.clear();
		geosetAnims.clear();
		idObjects.clear();
		pivots.clear();
		cameras.clear();
	}

	// INTERNAL PARTS CODING
	public void setName(final String text) {
		name = text;
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

	private void addIdObject(final IdObject x) {
		idObjects.add(x);
	}

	public void addFaceEffect(final FaceEffect faceEffect) {
		faceEffects.add(faceEffect);
	}

	public IdObject getIdObject(final int index) {
		return idObjects.get(index);
	}

	public Bone getBone(final int index) {
		try {
			if (index < idObjects.size()) {
				final IdObject temp = idObjects.get(index);
				if (temp.getClass() == Bone.class) {
					return (Bone) temp;
				}
			}
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Bone reference broken or invalid!");
		}
		return null;
	}

	public IdObject getObject(final String name) {
		for (final IdObject obj : idObjects) {
			if (obj.name.equalsIgnoreCase(name)) {
				return obj;
			}
		}
		return null;
	}

	public int getObjectId(final IdObject what) {
		return idObjects.indexOf(what);
	}

	public void addPivotPoint(final Vec3 x) {
		pivots.add(x);
	}

	public void addGeoset(final Geoset g) {
		geosets.add(g);
	}

	public Geoset getGeoset(final int index) {
		return geosets.get(index);
	}

	public int getGeosetId(final Geoset g) {
		return geosets.indexOf(g);
	}

	// public void setGeosetVisible(int index, boolean flag)
	// {
	// Geoset geo = (Geoset)m_geosets.get(index);
	// geo.setVisible(flag);
	// }
	// public void setGeosetHighlight(int index, boolean flag)
	// {
	// Geoset geo = (Geoset)m_geosets.get(index);
	// geo.setHighlight(flag);
	// }
	public void clearGeosets() {
		geosets.clear();
	}

	public void clearAnimations() {
		anims.clear();
	}

	/**
	 * Deletes all the animation in the model from the time track.
	 *
	 * Might leave behind nice things like global sequences if the code works out.
	 */
	public void deleteAllAnimation(final boolean clearUnusedNodes) {
		if (clearUnusedNodes) {
			// check the emitters
			final List<ParticleEmitter> particleEmitters = sortedIdObjects(ParticleEmitter.class);
			final List<ParticleEmitter2> particleEmitters2 = sortedIdObjects(ParticleEmitter2.class);
			final List<RibbonEmitter> ribbonEmitters = sortedIdObjects(RibbonEmitter.class);
			final List<ParticleEmitterPopcorn> popcornEmitters = sortedIdObjects(ParticleEmitterPopcorn.class);
			final List<IdObject> emitters = new ArrayList<>();
			emitters.addAll(particleEmitters2);
			emitters.addAll(particleEmitters);
			emitters.addAll(ribbonEmitters);
			emitters.addAll(popcornEmitters);

			for (final IdObject emitter : emitters) {
				int talliesFor = 0;
				int talliesAgainst = 0;
				final AnimFlag visibility = ((VisibilitySource) emitter).getVisibilityFlag();
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
		final List<AnimFlag> flags = getAllAnimFlags();
		final List<EventObject> evts = sortedIdObjects(EventObject.class);
		for (final Animation anim : anims) {
			anim.clearData(flags, evts);
		}
		if (clearUnusedNodes) {
			for (final EventObject e : evts) {
				if (e.size() <= 0) {
					idObjects.remove(e);
				}
			}
		}
		clearAnimations();
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
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = EditableModel.deepClone(other, "animation source file");

		final List<AnimFlag> flags = getAllAnimFlags();
		final List<EventObject> eventObjs = sortedIdObjects(EventObject.class);

		final List<AnimFlag> othersFlags = other.getAllAnimFlags();
		final List<EventObject> othersEventObjs = other.sortedIdObjects(EventObject.class);

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to
		// the information specific to each node about how it will
		// move if it gets translated into or onto the current model

		final List<AnimFlag> newImpFlags = new ArrayList<>();
		for (final AnimFlag af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(new AnimFlag(af));
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with
		// the exact same data, but shifted forward relative to wherever the
		// current model's last animation starts
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
		for (final AnimFlag af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current
		// model
		final List<Bone> leftBehind = new ArrayList<>();
		// the bones that don't find matches in current model
		for (final IdObject object : other.idObjects) {
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

	public List<Animation> addAnimationsFrom(EditableModel other, final List<Animation> anims) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = EditableModel.deepClone(other, "animation source file");

		final List<AnimFlag> flags = getAllAnimFlags();
		final List<EventObject> eventObjs = sortedIdObjects(EventObject.class);

		final List<AnimFlag> othersFlags = other.getAllAnimFlags();
		final List<EventObject> othersEventObjs = other.sortedIdObjects(EventObject.class);

		final List<Animation> newAnimations = new ArrayList<>();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to
		// the information specific to each node about how it will
		// move if it gets translated into or onto the current model

		final List<AnimFlag> newImpFlags = new ArrayList<>();
		for (final AnimFlag af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(new AnimFlag(af));
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with
		// the exact same data, but shifted forward relative to wherever the
		// current model's last animation starts
		for (final Animation anim : anims) {
			final int animTrackEnd = animTrackEnd();
			final int newStart = animTrackEnd + 300;
			final int newEnd = newStart + anim.length();
			final Animation newAnim = new Animation(anim); // clone the
															// animation from
															// the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			add(newAnim); // add the new animation to this model
			newAnimations.add(newAnim);
		}

		// destroy the other model's animations, filling them in with the new
		// stuff
		for (final AnimFlag af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current
		// model
		final List<Bone> leftBehind = new ArrayList<>(); // the bones that
															// don't find
															// matches in
															// current model
		for (final IdObject object : other.idObjects) {
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

		return newAnimations;
		// i think we're done????
	}

	public void copyVisibility(final Animation visibilitySource, final Animation target) {
		final List<VisibilitySource> allVisibilitySources = getAllVisibilitySources();
		for (final VisibilitySource source : allVisibilitySources) {
			final AnimFlag visibilityFlag = source.getVisibilityFlag();
			final AnimFlag copyFlag = new AnimFlag(visibilityFlag);
			visibilityFlag.deleteAnim(target);
			visibilityFlag.copyFrom(copyFlag, visibilitySource.getStart(), visibilitySource.getEnd(), target.getStart(),
					target.getEnd());
		}
	}

	public int getGeosetsSize() {
		return geosets.size();
	}

	public int getIdObjectsSize() {
		return idObjects.size();
	}

	public int getAnimsSize() {
		return anims.size();
	}

	public void doPostRead() {
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
		for (final ParticleEmitter2 temp : sortedIdObjects(ParticleEmitter2.class)) {
			temp.updateTextureRef(textures);
		}
		final List<AnimFlag> animFlags = getAllAnimFlags();// laggggg!
		for (final AnimFlag af : animFlags) {
			af.updateGlobalSeqRef(this);
			if (!af.getName().equals("Scaling") && !af.getName().equals("Translation")
					&& !af.getName().equals("Rotation")) {
			}
		}
		final List<EventObject> evtObjs = sortedIdObjects(EventObject.class);
		for (final EventObject af : evtObjs) {
			af.updateGlobalSeqRef(this);
		}
		for (final RibbonEmitter emitter : sortedIdObjects(RibbonEmitter.class)) {
			emitter.updateMaterialRef(materials);
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
		for (final IdObject idObject : idObjects) {
			pivots.add(idObject.pivotPoint);
		}
	}

	public int countIdObjectsOfClass(final Class what) {
		int idoCount = 0;
		for (final IdObject obj : idObjects) {
			if (obj.getClass() == what) {
				idoCount++;
			}
		}
		return idoCount;
	}

	public void rebuildMaterialList() {
		materials.clear();
		for (final Geoset g : geosets) {
			if ((g.material != null) && !materials.contains(g.material)) {
				materials.add(g.material);
			}
		}
		final List<RibbonEmitter> ribbons = sortedIdObjects(RibbonEmitter.class);
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

	public void rebuildTextureList() {
		rebuildTextureAnimList();
		textures.clear();
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				if ((lay.texture != null) && !textures.contains(lay.texture) && (lay.textures == null)) {
					boolean good = true;
					for (final Bitmap btm : textures) {
						if (lay.texture.equals(btm)) {
							good = false;
							break;
						}
					}
					if (good) {
						textures.add(lay.texture);
					}
				} else {
					final AnimFlag af = lay.find("TextureID");
					if (af != null) {
						for (final Bitmap temp : lay.textures) {
							boolean good = true;
							for (final Bitmap btm : textures) {
								if (temp.equals(btm)) {
									good = false;
									break;
								}
							}
							if (good) {
								textures.add(temp);
							}
						}
					}
				}
				lay.updateIds(this);// keep those Ids straight, will be -1 if
									// null
			}
		}
		final List<ParticleEmitter2> particles = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 pe : particles) {
			boolean good = true;
			if ((pe.texture != null) && !textures.contains(pe.texture)) {
				for (final Bitmap btm : textures) {
					if (pe.texture.equals(btm)) {
						good = false;
						break;
					}
				}
				if (good) {
					textures.add(pe.texture);
				}
			}
			pe.setTextureId(getTextureId(pe.texture));// will be -1 if null
		}
	}

	public void rebuildGlobalSeqList() {
		globalSeqs.clear();
		final List<AnimFlag> animFlags = getAllAnimFlags();// laggggg!
		final List<EventObject> evtObjs = sortedIdObjects(EventObject.class);
		for (final AnimFlag af : animFlags) {
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

	public void rebuildLists() {
		rebuildMaterialList();
		rebuildTextureList();// texture anims handled inside textures
		rebuildGlobalSeqList();
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
		final List<Bone> bones = sortedIdObjects(Bone.class);
		final List<? extends Bone> helpers = sortedIdObjects(Helper.class);
		bones.addAll(helpers);

		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject obj = idObjects.get(i);
			if (obj.parentId != -1) {
				obj.setParent(idObjects.get(obj.parentId));
			}
			if (i >= pivots.size()) {
				JOptionPane.showMessageDialog(null,
						"Error: More objects than PivotPoints were found." +
								"\nAdditional pivot at {0,0,0} will be added.");
				pivots.add(new Vec3(0, 0, 0));
			}
			obj.setPivotPoint(pivots.get(i));
			if (bindPose != null) {
				obj.bindPose = bindPose.bindPose[i];
			}
		}
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
				camera.setBindPose(bindPose.bindPose[i + idObjects.size()]);
			}
		}
	}

	public void updateObjectIds() {
		sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
		bindPose = null;
		for (final IdObject obj : idObjects) {
			final Collection<AnimFlag> animFlags = obj.getAnimFlags();
			final List<AnimFlag> bad = new ArrayList<>();
			for (final AnimFlag flag : animFlags) {
				if (flag.size() <= 0) {
					bad.add(flag);
				}
			}
			for (final AnimFlag badFlag : bad) {
				System.err.println("Gleaning out " + badFlag.getName() + " chunk with size of 0");
				animFlags.remove(badFlag);
			}
		}
		// -- end injected ---

		final List<Bone> bones = sortedIdObjects(Bone.class);
		final List<? extends Bone> helpers = sortedIdObjects(Helper.class);
		bones.addAll(helpers);

		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject obj = idObjects.get(i);
			obj.objectId = idObjects.indexOf(obj);
			obj.parentId = idObjects.indexOf(obj.getParent());
			if (obj.getBindPose() != null) {
				if (bindPose == null) {
					bindPose = new BindPose(idObjects.size() + cameras.size());
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
					bindPose = new BindPose(idObjects.size() + cameras.size());
				}
				bindPose.bindPose[i + idObjects.size()] = obj.getBindPose();
			}
		}
	}

	public void sortIdObjects() {
		final List<IdObject> allObjects = new ArrayList<>();
		final List<Bone> bones = sortedIdObjects(Bone.class);
		final List<Light> lights = sortedIdObjects(Light.class);
		final List<Helper> helpers = sortedIdObjects(Helper.class);
		final List<Attachment> attachments = sortedIdObjects(Attachment.class);
		final List<ParticleEmitter> particleEmitters = sortedIdObjects(ParticleEmitter.class);
		final List<ParticleEmitter2> particleEmitter2s = sortedIdObjects(ParticleEmitter2.class);
		final List<ParticleEmitterPopcorn> popcornEmitters = sortedIdObjects(ParticleEmitterPopcorn.class);
		final List<RibbonEmitter> ribbonEmitters = sortedIdObjects(RibbonEmitter.class);
		final List<EventObject> events = sortedIdObjects(EventObject.class);
		final List<CollisionShape> colliders = sortedIdObjects(CollisionShape.class);

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

		idObjects = allObjects;
	}

	public <T> List<T> sortedIdObjects(final Class<T> objectClass) {
		final List<T> objects = new ArrayList<>();
		for (final IdObject obj : idObjects) {
			if (obj.getClass() == objectClass) {
				objects.add((T) obj);
			}
		}
		return objects;
	}

	public List<AnimFlag> getAllAnimFlags() {
		// Probably will cause a bunch of lag, be wary
		final List<AnimFlag> allFlags = Collections.synchronizedList(new ArrayList<>());
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
		for (final IdObject idObject : idObjects) {
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

	public Object getAnimFlagSource(final AnimFlag aflg) {
		// Probably will cause a bunch of lag, be wary
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				final AnimFlag timeline = lay.find(aflg.getName());
				if (timeline != null) {
					return lay;
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				final AnimFlag timeline = texa.find(aflg.getName());
				if (timeline != null) {
					return texa;
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				final AnimFlag timeline = ga.find(aflg.getName());
				if (timeline != null) {
					return ga;
				}
			}
		}

		for (final IdObject object : idObjects) {
			final AnimFlag timeline = object.find(aflg.getName());
			if (timeline != null) {
				return object;
			}
		}

		if (cameras != null) {
			for (final Camera x : cameras) {
				AnimFlag timeline = x.getSourceNode().find(aflg.getName());
				if (timeline != null) {
					return x;
				}

				timeline = x.getTargetNode().find(aflg.getName());
				if (timeline != null) {
					return x;
				}
			}
		}

		return null;
	}

	public void addFlagToParent(final AnimFlag aflg, final AnimFlag added)
	// aflg is the parent
	{
		// ADDS "added" TO THE PARENT OF "aflg"
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				if (lay.has(aflg.getName())) {
					lay.add(added);
				}
			}
		}

		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa.has(aflg.getName())) {
					texa.add(added);
				}
			}
		}

		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga.has(aflg.getName())) {
					ga.add(added);
				}
			}
		}

		for (final IdObject object : idObjects) {
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

	public void buildGlobSeqFrom(final Animation anim, final List<AnimFlag> flags) {
		final Integer newSeq = anim.length();
		for (final AnimFlag af : flags) {
			if (!af.hasGlobalSeq) {
				final AnimFlag copy = new AnimFlag(af);
				copy.setGlobSeq(newSeq);
				copy.copyFrom(af, anim.getStart(), anim.getEnd(), 0, anim.length());
				addFlagToParent(af, copy);
			}
		}
	}

	public void buildGlobSeqFrom(final Animation anim) {
		buildGlobSeqFrom(anim, getAllAnimFlags());
	}

	public GeosetAnim getGeosetAnimOfGeoset(final Geoset g) {
		if (g.geosetAnim == null) {
			boolean noIds = true;
			for (int i = 0; (i < geosetAnims.size()) && noIds; i++) {
				final GeosetAnim ga = geosetAnims.get(i);
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
		final List<Bone> bones = sortedIdObjects(Bone.class);
		for (final Bone b : bones) {
			b.multiGeoId = false;
			b.geoset = null;
			b.geosetAnim = null;
		}
		for (final Geoset g : geosets) {
			final GeosetAnim ga = getGeosetAnimOfGeoset(g);
			for (final Matrix m : g.matrix) {
				for (final Bone b : m.bones) {
					if (!b.multiGeoId) {
						if (b.geoset == null) {
							// The bone has been found by no prior matrices
							b.geosetAnim = ga;
							b.geoset = g;
						} else if (b.geoset != g) {
							// The bone has only been found by ONE matrix
							b.multiGeoId = true;
							b.geoset = null;
							if (ga != null) {
								b.geosetAnim = ga.getMostVisible(b.geosetAnim);
							} else {
								b.geosetAnim = null;
							}

						}
					} else if (ga != null) {
						if (ga != b.geosetAnim) {
							b.geosetAnim = ga.getMostVisible(b.geosetAnim);
						}
					} else {
						b.geosetAnim = null;
					}
					IdObject bp = b.getParent();
					while (bp != null) {
						if (bp.getClass() == Bone.class) {
							final Bone b2 = (Bone) bp;
							if (!b2.multiGeoId) {
								if (b2.geoset == null) {
									// The bone has been found by no prior
									// matrices
									b2.geosetAnim = ga;
									b2.geoset = g;
								} else if (b2.geoset != g) {
									// The bone has only been found by ONE
									// matrix
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
						bp = bp.getParent();
					}
				}
			}
		}
	}

	public List<VisibilitySource> getAllVisibilitySources() {
		final List<AnimFlag> animFlags = getAllAnimFlags();// laggggg!
		final List<VisibilitySource> out = new ArrayList<>();
		for (final AnimFlag af : animFlags) {
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

	// Ultimate List functions
	public void add(final Animation x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Anim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		anims.add(x);
	}

	public void add(final Integer x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null GlobalSeq component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		globalSeqs.add(x);
	}

	public void add(final Bitmap x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Bitmap component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		textures.add(x);
	}

	public void add(final Material x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Material component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		materials.add(x);
	}

	public void add(final TextureAnim x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null TextureAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		texAnims.add(x);
	}

	public void add(final Geoset x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Geoset component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		x.parentModel = this;
		geosets.add(x);
	}

	public void add(final GeosetVertex x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null GeosetVertex component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		if (!contains(x.geoset)) {
			add(x.geoset);
			x.geoset.add(x);
		} else {
			x.geoset.add(x);
		}
	}

	public void add(final Triangle x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Triangle component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		if (!contains(x.geoset)) {
			add(x.geoset);
			x.geoset.add(x);
		} else {
			x.geoset.add(x);
		}
	}

	public void add(final GeosetAnim x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null GeosetAnim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		geosetAnims.add(x);
	}

	public void add(final IdObject x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null IdObject component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		idObjects.add(x);
		if ((x.pivotPoint != null) && !pivots.contains(x.pivotPoint)) {
			pivots.add(x.pivotPoint);
		}
		if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPose != null)) {
			if (x.getBindPose() == null) {
				x.setBindPose(new float[] { 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 });
			}
		}
	}

	public void add(final Camera x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Camera component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		cameras.add(x);
		if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPose != null)) {
			if (x.getBindPose() == null) {
				x.setBindPose(new float[] { 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 });
			}
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

	public boolean contains(final IdObject x) {
		return idObjects.contains(x);
	}

	public boolean contains(final Camera x) {
		return cameras.contains(x);
	}

	public void remove(final IdObject o) {
		idObjects.remove(o);
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

	public List<IdObject> getIdObjects() {
		return idObjects;
	}

	public void setIdObjects(final List<IdObject> idObjects) {
		this.idObjects = idObjects;
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

	public void visit(final MeshVisitor renderer) {
		int geosetId = 0;
		for (final Geoset geoset : geosets) {
			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(),
					geoset.getGeosetAnim());
			if ((ModelUtils.isTangentAndSkinSupported(formatVersion)) && (geoset.getVertices().size() > 0)
					&& (geoset.getVertex(0).getSkinBones() != null)) {
				for (final Triangle triangle : geoset.getTriangles()) {
					final TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
					for (final GeosetVertex vertex : triangle.getVerts()) {
						final VertexVisitor vertexRenderer;
						// TODO redesign for nullable normals
						if (vertex.getNormal() != null) {
							vertexRenderer = triangleRenderer.hdVertex(vertex.x, vertex.y, vertex.z,
									vertex.getNormal().x, vertex.getNormal().y, vertex.getNormal().z,
									vertex.getSkinBones(), vertex.getSkinBoneWeights());
						} else {
							vertexRenderer = triangleRenderer.hdVertex(vertex.x, vertex.y, vertex.z, 0, 0, 0,
									vertex.getSkinBones(), vertex.getSkinBoneWeights());
						}
						for (final Vec2 tvert : vertex.getTverts()) {
							vertexRenderer.textureCoords(tvert.x, tvert.y);
						}
						vertexRenderer.vertexFinished();
					}
					triangleRenderer.triangleFinished();
				}
			} else {
				for (final Triangle triangle : geoset.getTriangles()) {
					final TriangleVisitor triangleRenderer = geosetRenderer.beginTriangle();
					for (final GeosetVertex vertex : triangle.getVerts()) {
						final VertexVisitor vertexRenderer;
						// TODO redesign for nullable normals
						if (vertex.getNormal() != null) {
							vertexRenderer = triangleRenderer.vertex(vertex.x, vertex.y, vertex.z, vertex.getNormal().x,
									vertex.getNormal().y, vertex.getNormal().z, vertex.getBoneAttachments());
						} else {
							vertexRenderer = triangleRenderer.vertex(vertex.x, vertex.y, vertex.z, 0, 0, 0,
									vertex.getBoneAttachments());
						}
						for (final Vec2 tvert : vertex.getTverts()) {
							vertexRenderer.textureCoords(tvert.x, tvert.y);
						}
						vertexRenderer.vertexFinished();
					}
					triangleRenderer.triangleFinished();
				}
			}
			geosetRenderer.geosetFinished();
		}
	}

	public void render(final ModelVisitor renderer) {
		visit(renderer);
		for (final IdObject object : idObjects) {
			object.apply(renderer);
		}
		for (final Camera camera : cameras) {
			renderer.camera(camera);
		}
	}

	public void simplifyKeyframes() {
		final EditableModel currentMDL = this;
		final List<AnimFlag> allAnimFlags = currentMDL.getAllAnimFlags();
		final List<Animation> anims = currentMDL.getAnims();

		for (final AnimFlag flag : allAnimFlags) {
			final List<Integer> indicesForDeletion = new ArrayList<>();
			Entry lastEntry = null;
			for (int i = 0; i < flag.size(); i++) {
				final Entry entry = flag.getEntry(i);
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
			for (final AnimFlag flag : allAnimFlags) {
				if (!flag.hasGlobalSeq()) {
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
						if ((entry.time >= anim.getStart()) && (entry.time <= anim.getEnd())) {
							if (entry.value instanceof Float) {
								final Float d = (Float) entry.value;
								final Float older = (Float) olderKeyframe;
								final Float old = (Float) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older, old, d)) {
									indicesForDeletion.add(i - 1);
								}
							} else if (entry.value instanceof Vec3) {
								final Vec3 current = (Vec3) entry.value;
								final Vec3 older = (Vec3) olderKeyframe;
								final Vec3 old = (Vec3) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older.x, old.x, current.x)
										&& MathUtils.isBetween(older.y, old.y, current.y)
										&& MathUtils.isBetween(older.z, old.z, current.z)) {
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
									if (MathUtils.isBetween(olderEuler.x, oldEuler.x, euler.x)
											&& MathUtils.isBetween(olderEuler.y, oldEuler.y, euler.y)
											&& MathUtils.isBetween(olderEuler.z, oldEuler.z, euler.z)) {
										// if
										// (MathUtils.isBetween(older.a,
										// old.a, current.a)
										// &&
										// MathUtils.isBetween(older.b,
										// old.b, current.b)
										// &&
										// MathUtils.isBetween(older.c,
										// old.c, current.c)
										// &&
										// MathUtils.isBetween(older.d,
										// old.d, current.d)) {
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
			for (final AnimFlag flag : allAnimFlags) {
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
							if ((older != null) && (old != null) && MathUtils.isBetween(older, old, d)) {
								indicesForDeletion.add(i - 1);
							}
						} else if (entry.value instanceof Vec3) {
							final Vec3 current = (Vec3) entry.value;
							final Vec3 older = (Vec3) olderKeyframe;
							final Vec3 old = (Vec3) oldKeyframe;
							if ((older != null) && (old != null) && MathUtils.isBetween(older.x, old.x, current.x)
									&& MathUtils.isBetween(older.y, old.y, current.y)
									&& MathUtils.isBetween(older.z, old.z, current.z)) {
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
								if (MathUtils.isBetween(olderEuler.x, oldEuler.x, euler.x)
										&& MathUtils.isBetween(olderEuler.y, oldEuler.y, euler.y)
										&& MathUtils.isBetween(olderEuler.z, oldEuler.z, euler.z)) {
									// if (MathUtils.isBetween(older.a,
									// old.a, current.a)
									// && MathUtils.isBetween(older.b,
									// old.b, current.b)
									// && MathUtils.isBetween(older.c,
									// old.c, current.c)
									// && MathUtils.isBetween(older.d,
									// old.d, current.d)) {
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

		for (final IdObject object : idObjects) {
			object.removeAllTimelinesForGlobalSeq(selectedValue);
		}
	
		if (cameras != null) {
			for (final Camera x : cameras) {
				x.getSourceNode().removeAllTimelinesForGlobalSeq(selectedValue);
				x.getTargetNode().removeAllTimelinesForGlobalSeq(selectedValue);
			}
		}
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
		for (final ParticleEmitter2 emitter : sortedIdObjects(ParticleEmitter2.class)) {
			if (emitter.getTexture().equals(texture)) {
				emitter.setTexture(replacement);
			}
		}
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

	/**
	 * Please, for the love of Pete, don't actually do this.
	 */
	public static void convertToV800(final int targetLevelOfDetail, final EditableModel model) {
		// Things to fix:
		// 1.) format version
		model.setFormatVersion(800);
		// 2.) materials: only diffuse
		for (final Bitmap tex : model.getTextures()) {
			String path = tex.getPath();
			if ((path != null) && !path.isEmpty()) {
				final int dotIndex = path.lastIndexOf('.');
				if ((dotIndex != -1) && !path.endsWith(".blp")) {
					path = (path.substring(0, dotIndex));
				}
				if (!path.endsWith(".blp")) {
					path += ".blp";
				}
				tex.setPath(path);
			}
		}
		for (final Material material : model.getMaterials()) {
			if (material.getShaderString() != null) {
				material.setShaderString(null);
				final Layer layerZero = material.getLayers().get(0);
				material.getLayers().clear();
				material.getLayers().add(layerZero);
				if (material.getTwoSided()) {
					material.setTwoSided(false);
					layerZero.setTwoSided(true);
				}
			}
			for (final Layer layer : material.getLayers()) {
				if (!Double.isNaN(layer.getEmissive())) {
					layer.setEmissive(Double.NaN);
				}
				final AnimFlag flag = layer.find("Emissive");
				if (flag != null) {
					layer.remove(flag);
				}
			}
		}
		// 3.) geosets:
		// - Convert skin to matrices & vertex groups
		final List<Geoset> wrongLOD = new ArrayList<>();
		for (final Geoset geo : model.getGeosets()) {
			for (final GeosetVertex vertex : geo.getVertices()) {
				vertex.un900Heuristic();
			}
			if (geo.getLevelOfDetail() != targetLevelOfDetail) {
				// wrong lod
				wrongLOD.add(geo);
			}
		}
		// - Probably overwrite normals with tangents, maybe, or maybe not
		// - Eradicate anything that isn't LOD==X
		if (model.getGeosets().size() > wrongLOD.size()) {
			for (final Geoset wrongLODGeo : wrongLOD) {
				model.remove(wrongLODGeo);
				final GeosetAnim geosetAnim = wrongLODGeo.getGeosetAnim();
				if (geosetAnim != null) {
					model.remove(geosetAnim);
				}
			}
		}
		// 4.) remove popcorn
		// - add hero glow from popcorn if necessary
		final List<IdObject> incompatibleObjects = new ArrayList<>();
		for (int idObjIdx = 0; idObjIdx < model.getIdObjectsSize(); idObjIdx++) {
			final IdObject idObject = model.getIdObject(idObjIdx);
			if (idObject instanceof ParticleEmitterPopcorn) {
				incompatibleObjects.add(idObject);
				if (((ParticleEmitterPopcorn) idObject).getPath().toLowerCase().contains("hero_glow")) {
					System.out.println("HERO HERO HERO");
					final Bone dummyHeroGlowNode = new Bone("hero_reforged");
					// this model needs hero glow
					final Geoset heroGlow = new Geoset();
					final ModelUtils.Mesh heroGlowPlane = ModelUtils.createPlane((byte) 0, (byte) 1, new Vec3(0, 0, 1), 0, -64,
							-64, 64, 64, 1);
					heroGlow.getVertices().addAll(heroGlowPlane.getVertices());
					for (final GeosetVertex gv : heroGlow.getVertices()) {
						gv.setGeoset(heroGlow);
						gv.getBones().clear();
						gv.getBones().add(dummyHeroGlowNode);
					}
					heroGlow.getTriangles().addAll(heroGlowPlane.getTriangles());
					heroGlow.setUnselectable(true);
					final Bitmap heroGlowBitmap = new Bitmap("");
					heroGlowBitmap.setReplaceableId(2);
					final Layer layer = new Layer("Additive", heroGlowBitmap);
					layer.setUnshaded(true);
					layer.setUnfogged(true);
					heroGlow.setMaterial(new Material(layer));
					model.add(dummyHeroGlowNode);
					model.add(heroGlow);

				}
			}
		}
		for (final IdObject incompat : incompatibleObjects) {
			model.remove(incompat);
		}
		// 5.) remove other unsupported stuff
		for (final IdObject obj : model.getIdObjects()) {
			obj.setBindPose(null);
		}
		for (final Camera camera : model.getCameras()) {
			camera.setBindPose(null);
		}
		// 6.) fix dump bug with paths:
		for (final Bitmap tex : model.getTextures()) {
			final String path = tex.getPath();
			if (path != null) {
				tex.setPath(path.replace('/', '\\'));
			}
		}
		for (final ParticleEmitter emitter : model.sortedIdObjects(ParticleEmitter.class)) {
			final String path = emitter.getPath();
			if (path != null) {
				emitter.setPath(path.replace('/', '\\'));
			}
		}
		for (final Attachment emitter : model.sortedIdObjects(Attachment.class)) {
			final String path = emitter.getPath();
			if (path != null) {
				emitter.setPath(path.replace('/', '\\'));
			}
		}

		model.setBindPoseChunk(null);
		model.faceEffects.clear();
	}

	public static void makeItHD(final EditableModel model) {
		for (final Geoset geo : model.getGeosets()) {
			final List<GeosetVertex> vertices = geo.getVertices();
			for (final GeosetVertex gv : vertices) {
				final Vec3 normal = gv.getNormal();
				if (normal != null) {
					gv.initV900();
					final float[] tangent = gv.getTangent();
					for (int i = 0; i < 3; i++) {
						tangent[i] = normal.getCoord((byte) i);
					}
					tangent[3] = 1;
				}
				final int bones = Math.min(4, gv.getBoneAttachments().size());
				final short weight = (short) (255 / bones);
				final short offsetWeight = (short) (255 - (weight * bones));
				for (int i = 0; (i < bones) && (i < 4); i++) {
					gv.getSkinBones()[i] = gv.getBoneAttachments().get(i);
					gv.getSkinBoneWeights()[i] = weight;
					if (i == 0) {
						gv.getSkinBoneWeights()[i] += offsetWeight;
					}
				}
			}
		}
		for (final Material m : model.getMaterials()) {
			m.setShaderString("Shader_HD_DefaultUnit");
			if (m.getLayers().size() > 1) {
				m.getLayers().add(m.getLayers().remove(0));
			}
			final Bitmap normTex = new Bitmap("ReplaceableTextures\\TeamColor\\TeamColor09.dds");
			normTex.setWrapHeight(true);
			normTex.setWrapWidth(true);
			final Bitmap ormTex = new Bitmap("ReplaceableTextures\\TeamColor\\TeamColor18.dds");
			ormTex.setWrapHeight(true);
			ormTex.setWrapWidth(true);
			m.getLayers().add(1, new Layer("None", normTex));
			m.getLayers().add(2, new Layer("None", ormTex));
			final Bitmap black32 = new Bitmap("Textures\\Black32.dds");
			black32.setWrapHeight(true);
			black32.setWrapWidth(true);
			m.getLayers().add(3, new Layer("None", black32));
			final Bitmap texture2 = new Bitmap("ReplaceableTextures\\EnvironmentMap.dds");
			texture2.setWrapHeight(true);
			texture2.setWrapWidth(true);
			m.getLayers().add(4, new Layer("None", m.getLayers().get(0).getTextureBitmap()));
			m.getLayers().add(5, new Layer("None", texture2));
			for (final Layer l : m.getLayers()) {
				l.setEmissive(1.0);
			}
		}
	}

	public static void recalculateTangents(final EditableModel currentMDL, final Component parent) {
		// copied from
		// https://github.com/TaylorMouse/MaxScripts/blob/master/Warcraft%203%20Reforged/GriffonStudios/GriffonStudios_Warcraft_3_Reforged_Export.ms#L169
		currentMDL.doSavePreps(); // I wanted to use VertexId on the triangle
		for (final Geoset theMesh : currentMDL.getGeosets()) {
			final double[][] tan1 = new double[theMesh.getVertices().size()][];
			final double[][] tan2 = new double[theMesh.getVertices().size()][];
			for (int nFace = 0; nFace < theMesh.getTriangles().size(); nFace++) {
				final Triangle face = theMesh.getTriangle(nFace);

				final GeosetVertex v1 = face.getVerts()[0];
				final GeosetVertex v2 = face.getVerts()[1];
				final GeosetVertex v3 = face.getVerts()[2];

				final Vec2 w1 = v1.getTVertex(0);
				final Vec2 w2 = v2.getTVertex(0);
				final Vec2 w3 = v3.getTVertex(0);

				final double x1 = v2.x - v1.x;
				final double x2 = v3.x - v1.x;
				final double y1 = v2.y - v1.y;
				final double y2 = v3.y - v1.y;
				final double z1 = v2.z - v1.z;
				final double z2 = v3.z - v1.z;

				final double s1 = w2.x - w1.x;
				final double s2 = w3.x - w1.x;
				final double t1 = w2.y - w1.y;
				final double t2 = w3.y - w1.y;

				final double r = 1.0 / ((s1 * t2) - (s2 * t1));

				final double[] sdir = { ((t2 * x1) - (t1 * x2)) * r, ((t2 * y1) - (t1 * y2)) * r,
						((t2 * z1) - (t1 * z2)) * r };
				final double[] tdir = { ((s1 * x2) - (s2 * x1)) * r, ((s1 * y2) - (s2 * y1)) * r,
						((s1 * z2) - (s2 * z1)) * r };

				tan1[face.getId(0)] = sdir;
				tan1[face.getId(1)] = sdir;
				tan1[face.getId(2)] = sdir;

				tan2[face.getId(0)] = tdir;
				tan2[face.getId(1)] = tdir;
				tan2[face.getId(2)] = tdir;
			}
			for (int vertexId = 0; vertexId < theMesh.getVertices().size(); vertexId++) {
				final GeosetVertex gv = theMesh.getVertex(vertexId);
				final Vec3 n = gv.getNormal();
				final Vec3 t = new Vec3(tan1[vertexId]);

				final Vec3 v = new Vec3(t).sub(n).scale(n.dot(t)).normalize();
				final Vec3 cross = new Vec3();

				n.cross(t, cross);

				final Vec3 tanAsVert = new Vec3(tan2[vertexId]);

				double w = cross.dot(tanAsVert);

				if (w < 0.0) {
					w = -1.0;
				} else {
					w = 1.0;
				}
				gv.setTangent(new float[] {v.x, v.y, v.z, (float) w });
			}
		}
		int goodTangents = 0;
		int badTangents = 0;
		for (final Geoset theMesh : currentMDL.getGeosets()) {
			for (final GeosetVertex gv : theMesh.getVertices()) {
				final double dotProduct = gv.getNormal().dot(new Vec3(gv.getTangent()));
				if (Math.abs(dotProduct) <= 0.000001) {
					goodTangents += 1;
				} else {
					System.out.println(dotProduct);
					badTangents += 1;
				}
			}
		}
		if (parent != null) {
			JOptionPane.showMessageDialog(parent,
					"Tangent generation completed.\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents);
		} else {
			System.out.println(
					"Tangent generation completed.\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents);
		}
	}

	public static void recalculateTangentsOld(final EditableModel currentMDL) {
		for (final Geoset theMesh : currentMDL.getGeosets()) {
			for (int nFace = 0; nFace < theMesh.getTriangles().size(); nFace++) {
				final Triangle face = theMesh.getTriangle(nFace);

				final GeosetVertex v1 = face.getVerts()[0];
				final GeosetVertex v2 = face.getVerts()[0];
				final GeosetVertex v3 = face.getVerts()[0];

				final Vec2 uv1 = v1.getTVertex(0);
				final Vec2 uv2 = v2.getTVertex(0);
				final Vec2 uv3 = v3.getTVertex(0);

				final Vec3 dV1 = new Vec3(v1).sub(v2);
				final Vec3 dV2 = new Vec3(v1).sub(v3);

				final Vec2 dUV1 = new Vec2(uv1).sub(uv2);
				final Vec2 dUV2 = new Vec2(uv1).sub(uv3);
				final double area = (dUV1.x * dUV2.y) - (dUV1.y * dUV2.x);
				final int sign = (area < 0) ? -1 : 1;
				final Vec3 tangent = new Vec3(1, 0, 0);

				tangent.x = (dV1.x * dUV2.y) - (dUV1.y * dV2.x);
				tangent.y = (dV1.y * dUV2.y) - (dUV1.y * dV2.y);
				tangent.z = (dV1.z * dUV2.y) - (dUV1.y * dV2.z);

				tangent.normalize();
				tangent.scale(sign);

				final Vec3 faceNormal = new Vec3(v1.getNormal());
				faceNormal.add(v2.getNormal());
				faceNormal.add(v3.getNormal());
				faceNormal.normalize();
			}
		}
	}

	public void setGlobalSequenceLength(final int globalSequenceId, final Integer newLength) {
		if (globalSequenceId < globalSeqs.size()) {
			final Integer prevLength = globalSeqs.get(globalSequenceId);
			final List<AnimFlag> allAnimFlags = getAllAnimFlags();
			for (final AnimFlag af : allAnimFlags) {
				if ((af.getGlobalSeq() != null) && af.hasGlobalSeq()) {// TODO eliminate redundant structure
					if (af.getGlobalSeq().equals(prevLength)) {
						af.setGlobalSeq(newLength);
					}
				}
			}
			final List<EventObject> sortedEventObjects = sortedIdObjects(EventObject.class);
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

	public int computeMaterialID(final Material material) {
		return materials.indexOf(material);
	}

	public int computeGeosetID(final Geoset geoset) {
		return geosets.indexOf(geoset);
	}
}
