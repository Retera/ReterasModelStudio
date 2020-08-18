package com.hiveworkshop.wc3.mdl;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import com.etheller.warsmash.parsers.mdlx.MdlxAttachment;
import com.etheller.warsmash.parsers.mdlx.MdlxBone;
import com.etheller.warsmash.parsers.mdlx.MdlxCamera;
import com.etheller.warsmash.parsers.mdlx.MdlxCollisionShape;
import com.etheller.warsmash.parsers.mdlx.MdlxEventObject;
import com.etheller.warsmash.parsers.mdlx.MdlxFaceEffect;
import com.etheller.warsmash.parsers.mdlx.MdlxGeoset;
import com.etheller.warsmash.parsers.mdlx.MdlxGeosetAnimation;
import com.etheller.warsmash.parsers.mdlx.MdlxHelper;
import com.etheller.warsmash.parsers.mdlx.MdlxLight;
import com.etheller.warsmash.parsers.mdlx.MdlxMaterial;
import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter;
import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter2;
import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitterPopcorn;
import com.etheller.warsmash.parsers.mdlx.MdlxRibbonEmitter;
import com.etheller.warsmash.parsers.mdlx.MdlxSequence;
import com.etheller.warsmash.parsers.mdlx.MdlxTexture;
import com.etheller.warsmash.parsers.mdlx.MdlxTextureAnimation;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.datachooser.CompoundDataSource;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.datachooser.FolderDataSource;
import com.hiveworkshop.wc3.mdl.AnimFlag.Entry;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.TriangleVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.VertexVisitor;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.util.MathUtils;
import com.hiveworkshop.wc3.util.ModelUtils;
import com.hiveworkshop.wc3.util.ModelUtils.Mesh;

import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file
 * format).
 *
 * Eric Theller 11/5/2011
 */
public class EditableModel implements Named {
	public static boolean RETERA_FORMAT_BPOS_MATRICES = false;
	// private static String [] tags = {"Model ","Sequences ","GlobalSequences
	// ","Bitmap ","Material ","Geoset ",};

	private File fileRef;
	private String name;
	private int BlendTime;
	private ExtLog extents;
	private int formatVersion = 800;
	protected ArrayList<String> header = new ArrayList<>();
	protected ArrayList<Animation> anims = new ArrayList<>();
	protected ArrayList<Integer> globalSeqs = new ArrayList<>();
	protected ArrayList<Bitmap> textures = new ArrayList<>();
	protected ArrayList<SoundFile> sounds = new ArrayList<>();
	protected ArrayList<Material> materials = new ArrayList<>();
	protected ArrayList<TextureAnim> texAnims = new ArrayList<>();
	protected ArrayList<Geoset> geosets = new ArrayList<>();
	protected ArrayList<GeosetAnim> geosetAnims = new ArrayList<>();
	protected ArrayList<IdObject> idObjects = new ArrayList<>();
	protected ArrayList<Vertex> pivots = new ArrayList<>();
	protected ArrayList<Camera> cameras = new ArrayList<>();

	protected ArrayList m_junkCode = new ArrayList();// A series of
														// UnrecognizedElements

	protected ArrayList m_allParts = new ArrayList();// A compilation of array
														// list components in
														// the model, to contain
														// all parts
	private int c;
	private boolean loading;
	private boolean temporary;

	private final List<FaceEffect> faceEffects = new ArrayList<>();
	private BindPose bindPoseChunk;

	private DataSource wrappedDataSource = MpqCodebase.get();

	public static boolean DISABLE_BONE_GEO_ID_VALIDATOR = false;

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
	 *
	 * @return
	 */
	public String getHeaderName() {
		return name;
	}

	public void setFileRef(final File file) {
		fileRef = file;
		if (fileRef != null) {
			wrappedDataSource = new CompoundDataSource(
					Arrays.asList(MpqCodebase.get(), new FolderDataSource(file.getParentFile().toPath())));
		} else {
			wrappedDataSource = MpqCodebase.get();
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
		BlendTime = other.BlendTime;
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
		final File temp;
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			what.toMdlx().saveMdx(byteArrayOutputStream);

			try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
				final EditableModel newModel = MdxUtils.loadEditableModel(bais);
				newModel.setName(newName);
				newModel.setFileRef(what.getFile());
				return newModel;
			}

		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		return null;
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

	public EditableModel() {
		name = "UnnamedModel";
		formatVersion = 800;
	}

	public EditableModel(final String newName) {
		name = newName;
		formatVersion = 800;
	}

	public EditableModel(final EditableModel other) {
		setFileRef(other.fileRef);
		name = other.name;
		BlendTime = other.BlendTime;
		extents = new ExtLog(other.extents);
		formatVersion = other.formatVersion;
		header = new ArrayList<>(other.header);
		anims = new ArrayList(other.anims);
		globalSeqs = new ArrayList(other.globalSeqs);
		textures = new ArrayList(other.textures);
		materials = new ArrayList(other.materials);
		texAnims = new ArrayList(other.texAnims);
		geosets = new ArrayList(other.geosets);
		geosetAnims = new ArrayList(other.geosetAnims);
		idObjects = new ArrayList(other.idObjects);
		pivots = new ArrayList(other.pivots);
		cameras = new ArrayList(other.cameras);
	}

	/**
	 * Used for checking MDX conversions.
	 *
	 * @param flags An int representing a set of booleans with its bits
	 * @param mask  A bit to check against
	 * @return Whether the specified bit was "true" (=1)
	 */
	public static boolean hasFlag(final int flags, final int mask) {
		return (flags & mask) != 0;
	}

	public EditableModel(final MdlxModel model) {
		this();

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
			add(Integer.valueOf((int)sequence));
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
			add(new Geoset(geoset));
		}

		// Step 8: GeosetAnims
		for (final MdlxGeosetAnimation animation : model.geosetAnimations) {
			add(new GeosetAnim(animation));
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

		boolean corruptedCameraWarningGiven = false;
		for (final MdlxCamera camera : model.cameras) {
			final Camera mdlCam = new Camera(camera);

			if (!corruptedCameraWarningGiven && (mdlCam.getName().contains("????????")
					|| (mdlCam.getName().length() > 20) || (mdlCam.getName().length() <= 0))) {
				corruptedCameraWarningGiven = true;
				JOptionPane.showMessageDialog(null, "--- " + this.getName()
						+ " ---\nWARNING: Java Warcraft Libraries thinks we are loading a camera with corrupted data due to bug in Native MDX Parser.\nPlease DISABLE \"View > Use Native MDX Parser\" if you want to correctly edit \""
						+ getName()
						+ "\".\nYou may continue to work, but portions of the model's data have been lost, and will be missing if you save.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}

			add(mdlCam);
		}

		// CollisionShape
		for (final MdlxCollisionShape shape : model.collisionShapes) {
			add(new CollisionShape(shape));
		}

		for (final float[] point : model.pivotPoints) {
			addPivotPoint(new Vertex(point));
		}

		for (final MdlxFaceEffect effect : model.faceEffects) {
			addFaceEffect(new FaceEffect(effect.type, effect.path));
		}

		if (model.bindPose.size() > 0) {
			bindPoseChunk = new BindPose(model.bindPose);
		}

		doPostRead(); // fixes all the things
	}

	public MdlxModel toMdlx() {
		doSavePreps(); // restores all GeosetID, ObjectID, TextureID,
		// MaterialID stuff all based on object references
		// in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())" without
		// a problem, or even "mdl.add(otherMdl.getGeoset(5))" and have the
		// geoset's textures and materials
		// all be carried over with it via object references in java

		// also this re-creates all matrices, which are consumed by the
		// MatrixEater at runtime in doPostRead()
		// in favor of each vertex having its own attachments list, no vertex
		// groups)

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
			model.geosets.add(geoset.toMdlx());
		}

		for (final GeosetAnim animation : geosetAnims) {
			model.geosetAnimations.add(animation.toMdlx());
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

		for (final Camera camera : sortedIdObjects(Camera.class)) {
			model.cameras.add(camera.toMdlx());
		}

		for (final CollisionShape shape : sortedIdObjects(CollisionShape.class)) {
			model.collisionShapes.add(shape.toMdlx());
		}

		for (final Vertex point : getPivots()) {
			model.pivotPoints.add(point.toFloatArray());
		}

		for (final FaceEffect effect : faceEffects) {
			model.faceEffects.add(effect.toMdlx());
		}

		if (bindPoseChunk != null) {
			model.bindPose = bindPoseChunk.toMdlx();
		}

		return model;
	}

	public boolean doesContainString(final String a, final String b)// see if a
																	// contains
																	// b
	{
		final int l = a.length();
		for (int i = 0; i < l; i++) {
			if (a.startsWith(b, i)) {
				return true;
			}
		}
		return false;
	}

	public int countContainsString(final String a, final String b)// see if a
																	// contains
																	// b
	{
		final int l = a.length();
		int x = 0;
		for (int i = 0; i < l; i++) {
			if (a.startsWith(b, i)) {
				x++;
			}
		}
		return x;
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

	public int getTextureId(final Bitmap b) {
		if (b == null) {
			return -1;
		}
		for (final Bitmap btm : textures) {
			if (b.equals(btm)) {
				return textures.indexOf(btm);
			}
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
		return materials.get(i);
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

	public void addPivotPoint(final Vertex x) {
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
		for (final Animation anim : this.anims) {
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
	 *
	 * @param other
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

		final ArrayList<AnimFlag> newImpFlags = new ArrayList<>();
		for (final AnimFlag af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(new AnimFlag(af));
			}
		}
		final ArrayList<EventObject> newImpEventObjs = new ArrayList<>();
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
			final Animation newAnim = new Animation(anim); // clone the
															// animation from
															// the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			add(newAnim); // add the new animation to this model
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
				if ((localObject != null) && (localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone); // if it's a match, take the
													// data
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

		final ArrayList<AnimFlag> newImpFlags = new ArrayList<>();
		for (final AnimFlag af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(AnimFlag.buildEmptyFrom(af));
			} else {
				newImpFlags.add(new AnimFlag(af));
			}
		}
		final ArrayList<EventObject> newImpEventObjs = new ArrayList<>();
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
				if ((localObject != null) && (localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone); // if it's a match, take the
													// data
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
		final ArrayList<VisibilitySource> allVisibilitySources = getAllVisibilitySources();
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
		for (final GeosetAnim geoAnim : this.geosetAnims) {
			if (geoAnim.geosetId != -1) {
				if (geoAnim.geosetId >= this.geosets.size()) {
					badAnims.add(geoAnim);
				} else {

					geoAnim.geoset = this.getGeoset(geoAnim.geosetId);
					geoAnim.geoset.geosetAnim = geoAnim;// YEAH THIS MAKES SENSE
				}
			}
		}
		if (badAnims.size() > 0) {
			JOptionPane.showMessageDialog(null,
					"We discovered GeosetAnim data pointing to an invalid GeosetID! Bad data will be deleted. Please backup your model file.");
		}
		for (final GeosetAnim bad : badAnims) {
			this.geosetAnims.remove(bad);
		}
		for (final ParticleEmitter2 temp : sortedIdObjects(ParticleEmitter2.class)) {
			temp.updateTextureRef(textures);
		}
		final List<AnimFlag> animFlags = this.getAllAnimFlags();// laggggg!
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
				for (int i = 0; i < geosets.size(); i++) {
					geosets.get(i).doSavePrep(this);
				}
			}
		}

		// GeosetAnims
		for (final GeosetAnim geoAnim : geosetAnims) {
			geoAnim.geosetId = geosets.indexOf(geoAnim.geoset);
		}

		// Clearing pivot points
		pivots.clear();
		for (int i = 0; i < idObjects.size(); i++) {
			pivots.add(idObjects.get(i).pivotPoint);
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
			g.setMaterialId(materials.indexOf(g.material)); // -1 if null
		}
		final ArrayList<RibbonEmitter> ribbons = sortedIdObjects(RibbonEmitter.class);
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
						}
					}
					if (good) {
						textures.add(lay.texture);
					}
				} else {
					final AnimFlag af = lay.getFlag("TextureID");
					if (af != null) {
						for (final Bitmap temp : lay.textures) {
							boolean good = true;
							for (final Bitmap btm : textures) {
								if (temp.equals(btm)) {
									good = false;
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
		final ArrayList<ParticleEmitter2> particles = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 pe : particles) {
			boolean good = true;
			if ((pe.texture != null) && !textures.contains(pe.texture)) {
				for (final Bitmap btm : textures) {
					if (pe.texture.equals(btm)) {
						good = false;
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
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		final ArrayList<? extends Bone> helpers = sortedIdObjects(Helper.class);
		bones.addAll(helpers);

		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject obj = idObjects.get(i);
			if (obj.parentId != -1) {
				obj.setParent(idObjects.get(obj.parentId));
			}
			if (i >= pivots.size()) {
				JOptionPane.showMessageDialog(null,
						"Error: More objects than PivotPoints were found.\nAdditional pivot at {0,0,0} will be added.");
				pivots.add(new Vertex(0, 0, 0));
			}
			obj.setPivotPoint(pivots.get(i));
			if (bindPoseChunk != null) {
				obj.bindPose = bindPoseChunk.bindPose[i];
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
			if (bindPoseChunk != null) {
				camera.setBindPose(bindPoseChunk.bindPose[i + idObjects.size()]);
			}
		}
	}

	public void updateObjectIds() {
		sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
		bindPoseChunk = null;
		for (final IdObject obj : idObjects) {
			final List<AnimFlag> animFlags = obj.getAnimFlags();
			final List<AnimFlag> bad = new ArrayList<>();
			for (final AnimFlag flag : animFlags) {
				if (flag.length() <= 0) {
					bad.add(flag);
				}
			}
			for (final AnimFlag badFlag : bad) {
				System.err.println("Gleaning out " + badFlag.getName() + " chunk with size of 0");
				animFlags.remove(badFlag);
			}
		}
		// -- end injected ---

		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		final ArrayList<? extends Bone> helpers = sortedIdObjects(Helper.class);
		bones.addAll(helpers);

		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject obj = idObjects.get(i);
			obj.objectId = idObjects.indexOf(obj);
			obj.parentId = idObjects.indexOf(obj.getParent());
			if (obj.getBindPose() != null) {
				if (bindPoseChunk == null) {
					bindPoseChunk = new BindPose(idObjects.size() + cameras.size());
				}
				bindPoseChunk.bindPose[i] = obj.getBindPose();
			}
		}
		for (final Bone b : bones) {
			b.geosetId = geosets.indexOf(b.geoset);
			b.geosetAnimId = geosetAnims.indexOf(b.geosetAnim);
		}
		for (int i = 0; i < cameras.size(); i++) {
			final Camera obj = cameras.get(i);
			if (obj.getBindPose() != null) {
				if (bindPoseChunk == null) {
					bindPoseChunk = new BindPose(idObjects.size() + cameras.size());
				}
				bindPoseChunk.bindPose[i + idObjects.size()] = obj.getBindPose();
			}
		}
	}

	public void sortIdObjects() {
		final ArrayList<IdObject> allObjects = new ArrayList<>();
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		final ArrayList<Light> lights = sortedIdObjects(Light.class);
		final ArrayList<Helper> helpers = sortedIdObjects(Helper.class);
		final ArrayList<Attachment> attachments = sortedIdObjects(Attachment.class);
		final ArrayList<ParticleEmitter> particleEmitters = sortedIdObjects(ParticleEmitter.class);
		final ArrayList<ParticleEmitter2> particleEmitter2s = sortedIdObjects(ParticleEmitter2.class);
		final ArrayList<ParticleEmitterPopcorn> popcornEmitters = sortedIdObjects(ParticleEmitterPopcorn.class);
		final ArrayList<RibbonEmitter> ribbonEmitters = sortedIdObjects(RibbonEmitter.class);
		final ArrayList<EventObject> events = sortedIdObjects(EventObject.class);
		final ArrayList<CollisionShape> colliders = sortedIdObjects(CollisionShape.class);

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

	public <T> ArrayList<T> sortedIdObjects(final Class<T> objectClass) {
		final ArrayList<T> objects = new ArrayList<>();
		for (final IdObject obj : idObjects) {
			if (obj.getClass() == objectClass) {
				objects.add((T) obj);
			}
		}
		return objects;
	}

	// public ArrayList sortedIdObjects(final Class kind)
	// {
	// final ArrayList objects = new ArrayList();
	// for( final IdObject obj: idObjects )
	// {
	// if( obj.getClass() == kind )
	// {
	// objects.add(obj);
	// }
	// }
	// return objects;
	// }
	public List<AnimFlag> getAllAnimFlags() {
		// Probably will cause a bunch of lag, be wary
		final List<AnimFlag> allFlags = Collections.synchronizedList(new ArrayList<AnimFlag>());
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				allFlags.addAll(lay.animFlags);
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa != null) {
					allFlags.addAll(texa.animFlags);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga != null) {
					allFlags.addAll(ga.animFlags);
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
				allFlags.addAll(x.animFlags);
				allFlags.addAll(x.targetAnimFlags);
			}
		}

		return allFlags;
	}

	public Object getAnimFlagSource(final AnimFlag aflg) {
		// Probably will cause a bunch of lag, be wary
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				if (lay.animFlags.contains(aflg)) {
					return lay;
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa.animFlags.contains(aflg)) {
					return texa;
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga.animFlags.contains(aflg)) {
					return ga;
				}
			}
		}
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		bones.addAll(sortedIdObjects(Helper.class));// Hey, look at that!
		for (final Bone b : bones) {
			if (b.animFlags.contains(aflg)) {
				return b;
			}
		}
		final ArrayList<Light> lights = sortedIdObjects(Light.class);
		for (final Light l : lights) {
			if (l.animFlags.contains(aflg)) {
				return l;
			}
		}
		final ArrayList<Attachment> atcs = sortedIdObjects(Attachment.class);
		for (final Attachment x : atcs) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		final ArrayList<ParticleEmitter2> pes = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 x : pes) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		final ArrayList<ParticleEmitter> xpes = sortedIdObjects(ParticleEmitter.class);
		for (final ParticleEmitter x : xpes) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		final ArrayList<ParticleEmitterPopcorn> pfes = sortedIdObjects(ParticleEmitterPopcorn.class);
		for (final ParticleEmitterPopcorn x : pfes) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		final ArrayList<RibbonEmitter> res = sortedIdObjects(RibbonEmitter.class);
		for (final RibbonEmitter x : res) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		final ArrayList<CollisionShape> cs = sortedIdObjects(CollisionShape.class);
		for (final CollisionShape x : cs) {
			if (x.animFlags.contains(aflg)) {
				return x;
			}
		}
		if (cameras != null) {
			for (final Camera x : cameras) {
				if (x.animFlags.contains(aflg) || x.targetAnimFlags.contains(aflg)) {
					return x;
				}
			}
		}

		return null;
	}

	public void addFlagToParent(final AnimFlag aflg, final AnimFlag added)// aflg
																			// is
																			// the
																			// parent
	{
		// ADDS "added" TO THE PARENT OF "aflg"
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				if (lay.animFlags.contains(aflg)) {
					lay.animFlags.add(added);
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa.animFlags.contains(aflg)) {
					texa.animFlags.add(added);
				}
			}
		}
		if (geosetAnims != null) {
			for (final GeosetAnim ga : geosetAnims) {
				if (ga.animFlags.contains(aflg)) {
					ga.animFlags.add(added);
				}
			}
		}
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		bones.addAll(sortedIdObjects(Helper.class));// Hey, look at that!
		for (final Bone b : bones) {
			if (b.animFlags.contains(aflg)) {
				b.animFlags.add(added);
			}
		}
		final ArrayList<Light> lights = sortedIdObjects(Light.class);
		for (final Light l : lights) {
			if (l.animFlags.contains(aflg)) {
				l.animFlags.add(added);
			}
		}
		final ArrayList<Attachment> atcs = sortedIdObjects(Attachment.class);
		for (final Attachment x : atcs) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		final ArrayList<ParticleEmitter2> pes = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 x : pes) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		final ArrayList<ParticleEmitter> xpes = sortedIdObjects(ParticleEmitter.class);
		for (final ParticleEmitter x : xpes) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		final ArrayList<ParticleEmitterPopcorn> pfes = sortedIdObjects(ParticleEmitterPopcorn.class);
		for (final ParticleEmitterPopcorn x : pfes) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		final ArrayList<RibbonEmitter> res = sortedIdObjects(RibbonEmitter.class);
		for (final RibbonEmitter x : res) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		final ArrayList<CollisionShape> cs = sortedIdObjects(CollisionShape.class);
		for (final CollisionShape x : cs) {
			if (x.animFlags.contains(aflg)) {
				x.animFlags.add(added);
			}
		}
		if (cameras != null) {
			for (final Camera x : cameras) {
				if (x.animFlags.contains(aflg) || x.targetAnimFlags.contains(aflg)) {
					x.animFlags.add(added);
				}
			}
		}
	}

	public void buildGlobSeqFrom(final Animation anim, final List<AnimFlag> flags) {
		final Integer newSeq = new Integer(anim.length());
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
				if (ga.geosetId != -1) {
					noIds = false;
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
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
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
							if (ga != null) {
								b.geosetAnim = ga;
							} else {
								b.geosetAnim = null;
							}
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
									if (ga != null) {
										b2.geosetAnim = ga;
									} else {
										b2.geosetAnim = null;
									}
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

	public ArrayList<VisibilitySource> getAllVisibilitySources() {
		final List<AnimFlag> animFlags = getAllAnimFlags();// laggggg!
		final ArrayList<VisibilitySource> out = new ArrayList<>();
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
		if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPoseChunk != null)) {
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
		if (ModelUtils.isBindPoseSupported(formatVersion) && (bindPoseChunk != null)) {
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
		return BlendTime;
	}

	public void setBlendTime(final int blendTime) {
		BlendTime = blendTime;
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

	public ArrayList<Animation> getAnims() {
		return anims;
	}

	public void setAnims(final ArrayList<Animation> anims) {
		this.anims = anims;
	}

	public ArrayList<Integer> getGlobalSeqs() {
		return globalSeqs;
	}

	public void setGlobalSeqs(final ArrayList<Integer> globalSeqs) {
		this.globalSeqs = globalSeqs;
	}

	public ArrayList<Bitmap> getTextures() {
		return textures;
	}

	public void setTextures(final ArrayList<Bitmap> textures) {
		this.textures = textures;
	}

	public ArrayList<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(final ArrayList<Material> materials) {
		this.materials = materials;
	}

	public ArrayList<TextureAnim> getTexAnims() {
		return texAnims;
	}

	public void setTexAnims(final ArrayList<TextureAnim> texAnims) {
		this.texAnims = texAnims;
	}

	public ArrayList<Geoset> getGeosets() {
		return geosets;
	}

	public void setGeosets(final ArrayList<Geoset> geosets) {
		this.geosets = geosets;
	}

	public ArrayList<GeosetAnim> getGeosetAnims() {
		return geosetAnims;
	}

	public void setGeosetAnims(final ArrayList<GeosetAnim> geosetAnims) {
		this.geosetAnims = geosetAnims;
	}

	public ArrayList<IdObject> getIdObjects() {
		return idObjects;
	}

	public void setIdObjects(final ArrayList<IdObject> idObjects) {
		this.idObjects = idObjects;
	}

	public ArrayList<Vertex> getPivots() {
		return pivots;
	}

	public void setPivots(final ArrayList<Vertex> pivots) {
		this.pivots = pivots;
	}

	public ArrayList<Camera> getCameras() {
		return cameras;
	}

	public void setCameras(final ArrayList<Camera> cameras) {
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
						for (final TVertex tvert : vertex.getTverts()) {
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
						for (final TVertex tvert : vertex.getTverts()) {
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
			for (int i = 0; i < flag.length(); i++) {
				final Entry entry = flag.getEntry(i);
				if ((lastEntry != null) && (lastEntry.time == entry.time)) {
					indicesForDeletion.add(new Integer(i));
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
					for (int i = 0; i < flag.length(); i++) {
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
							if (entry.value instanceof Double) {
								final Double d = (Double) entry.value;
								final Double older = (Double) olderKeyframe;
								final Double old = (Double) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older, old, d)) {
									indicesForDeletion.add(new Integer(i - 1));
								}
							} else if (entry.value instanceof Vertex) {
								final Vertex current = (Vertex) entry.value;
								final Vertex older = (Vertex) olderKeyframe;
								final Vertex old = (Vertex) oldKeyframe;
								if ((older != null) && (old != null) && MathUtils.isBetween(older.x, old.x, current.x)
										&& MathUtils.isBetween(older.y, old.y, current.y)
										&& MathUtils.isBetween(older.z, old.z, current.z)) {
									indicesForDeletion.add(new Integer(i - 1));
								}
							} else if (entry.value instanceof QuaternionRotation) {
								final QuaternionRotation current = (QuaternionRotation) entry.value;
								final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
								final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
								final Vertex euler = current.toEuler();
								if ((older != null) && (old != null)) {
									final Vertex olderEuler = older.toEuler();
									final Vertex oldEuler = old.toEuler();
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
										indicesForDeletion.add(new Integer(i - 1));
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
					for (int i = 0; i < flag.length(); i++) {
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
						if (entry.value instanceof Double) {
							final Double d = (Double) entry.value;
							final Double older = (Double) olderKeyframe;
							final Double old = (Double) oldKeyframe;
							if ((older != null) && (old != null) && MathUtils.isBetween(older, old, d)) {
								indicesForDeletion.add(new Integer(i - 1));
							}
						} else if (entry.value instanceof Vertex) {
							final Vertex current = (Vertex) entry.value;
							final Vertex older = (Vertex) olderKeyframe;
							final Vertex old = (Vertex) oldKeyframe;
							if ((older != null) && (old != null) && MathUtils.isBetween(older.x, old.x, current.x)
									&& MathUtils.isBetween(older.y, old.y, current.y)
									&& MathUtils.isBetween(older.z, old.z, current.z)) {
								indicesForDeletion.add(new Integer(i - 1));
							}
						} else if (entry.value instanceof QuaternionRotation) {
							final QuaternionRotation current = (QuaternionRotation) entry.value;
							final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
							final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
							final Vertex euler = current.toEuler();
							if ((older != null) && (old != null)) {
								final Vertex olderEuler = older.toEuler();
								final Vertex oldEuler = old.toEuler();
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
									indicesForDeletion.add(new Integer(i - 1));
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
	// public void destroy()
	// {
	// try {
	// this.finalize();
	// } catch (Throwable e) {
	// e.printStackTrace();
	// }
	// }

	public void removeAllTimelinesForGlobalSeq(final Integer selectedValue) {
		for (final Material m : materials) {
			for (final Layer lay : m.layers) {
				final Iterator<AnimFlag> iterator = lay.animFlags.iterator();
				while (iterator.hasNext()) {
					final AnimFlag animFlag = iterator.next();
					if (selectedValue.equals(animFlag.getGlobalSeq())) {
						iterator.remove();
					}
				}
			}
		}
		if (texAnims != null) {
			for (final TextureAnim texa : texAnims) {
				if (texa != null) {
					final Iterator<AnimFlag> iterator = texa.animFlags.iterator();
					while (iterator.hasNext()) {
						final AnimFlag animFlag = iterator.next();
						if (selectedValue.equals(animFlag.getGlobalSeq())) {
							iterator.remove();
						}
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
					final Iterator<AnimFlag> iterator = ga.animFlags.iterator();
					while (iterator.hasNext()) {
						final AnimFlag animFlag = iterator.next();
						if (selectedValue.equals(animFlag.getGlobalSeq())) {
							iterator.remove();
						}
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		bones.addAll(sortedIdObjects(Helper.class));// Hey, look at that!
		for (final Bone b : bones) {
			final Iterator<AnimFlag> iterator = b.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<Light> lights = sortedIdObjects(Light.class);
		for (final Light l : lights) {
			final Iterator<AnimFlag> iterator = l.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<Attachment> atcs = sortedIdObjects(Attachment.class);
		for (final Attachment x : atcs) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<ParticleEmitter2> pes = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 x : pes) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<ParticleEmitter> xpes = sortedIdObjects(ParticleEmitter.class);
		for (final ParticleEmitter x : xpes) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<ParticleEmitterPopcorn> pfes = sortedIdObjects(ParticleEmitterPopcorn.class);
		for (final ParticleEmitterPopcorn x : pfes) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<RibbonEmitter> res = sortedIdObjects(RibbonEmitter.class);
		for (final RibbonEmitter x : res) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<CollisionShape> cs = sortedIdObjects(CollisionShape.class);
		for (final CollisionShape x : cs) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		final ArrayList<EventObject> evt = sortedIdObjects(EventObject.class);
		for (final EventObject x : evt) {
			final Iterator<AnimFlag> iterator = x.animFlags.iterator();
			while (iterator.hasNext()) {
				final AnimFlag animFlag = iterator.next();
				if (selectedValue.equals(animFlag.getGlobalSeq())) {
					iterator.remove();
				}
			}
		}
		if (cameras != null) {
			for (final Camera x : cameras) {
				Iterator<AnimFlag> iterator = x.animFlags.iterator();
				while (iterator.hasNext()) {
					final AnimFlag animFlag = iterator.next();
					if (selectedValue.equals(animFlag.getGlobalSeq())) {
						iterator.remove();
					}
				}
				iterator = x.targetAnimFlags.iterator();
				while (iterator.hasNext()) {
					final AnimFlag animFlag = iterator.next();
					if (selectedValue.equals(animFlag.getGlobalSeq())) {
						iterator.remove();
					}
				}
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
		return bindPoseChunk;
	}

	public void setBindPoseChunk(final BindPose bindPoseChunk) {
		this.bindPoseChunk = bindPoseChunk;
	}

	/**
	 * Please, for the love of Pete, don't actually do this.
	 *
	 * @param targetLevelOfDetail
	 * @param model
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
				if (material.getFlags().contains("TwoSided")) {
					material.getFlags().remove("TwoSided");
					layerZero.add("TwoSided");
				}
			}
			for (final Layer layer : material.getLayers()) {
				if (!Double.isNaN(layer.getEmissive())) {
					layer.setEmissive(Double.NaN);
				}
				final AnimFlag flag = layer.getFlag("Emissive");
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
					final Mesh heroGlowPlane = ModelUtils.createPlane((byte) 0, (byte) 1, new Vertex(0, 0, 1), 0, -64,
							-64, 64, 64, 1);
					heroGlow.getVertices().addAll(heroGlowPlane.getVertices());
					for (final GeosetVertex gv : heroGlow.getVertices()) {
						gv.setGeoset(heroGlow);
						gv.getBones().clear();
						gv.getBones().add(dummyHeroGlowNode);
					}
					heroGlow.getTriangles().addAll(heroGlowPlane.getTriangles());
					heroGlow.addFlag("Unselectable");
					final Bitmap heroGlowBitmap = new Bitmap("");
					heroGlowBitmap.setReplaceableId(2);
					final Layer layer = new Layer("Additive", heroGlowBitmap);
					layer.add("Unshaded");
					layer.add("Unfogged");
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
			final ArrayList<GeosetVertex> vertices = geo.getVertices();
			for (final GeosetVertex gv : vertices) {
				final Normal normal = gv.getNormal();
				if (normal != null) {
					gv.initV900();
					final float[] tangent = gv.getTangent();
					for (int i = 0; i < 3; i++) {
						tangent[i] = (float) normal.getCoord((byte) i);
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

				final TVertex w1 = v1.getTVertex(0);
				final TVertex w2 = v2.getTVertex(0);
				final TVertex w3 = v3.getTVertex(0);

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
				final Normal n = gv.getNormal();
				final Vertex t = new Vertex(tan1[vertexId]);

				final Vertex v = new Vertex(t).subtract(n).scale(n.dotProduct(t)).normalize();
				double w = n.crossProduct(t).dotProduct(new Vertex(tan2[vertexId]));

				if (w < 0.0) {
					w = -1.0;
				} else {
					w = 1.0;
				}
				gv.setTangent(new float[] { (float) v.x, (float) v.y, (float) v.z, (float) w });
			}
		}
		int goodTangents = 0;
		int badTangents = 0;
		for (final Geoset theMesh : currentMDL.getGeosets()) {
			for (final GeosetVertex gv : theMesh.getVertices()) {
				final double dotProduct = gv.getNormal().dotProduct(new Vertex(gv.getTangent()));
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

				final TVertex uv1 = v1.getTVertex(0);
				final TVertex uv2 = v2.getTVertex(0);
				final TVertex uv3 = v3.getTVertex(0);

				final Vertex dV1 = new Vertex(v1).subtract(v2);
				final Vertex dV2 = new Vertex(v1).subtract(v3);

				final TVertex dUV1 = new TVertex(uv1).subtract(uv2);
				final TVertex dUV2 = new TVertex(uv1).subtract(uv3);
				final double area = (dUV1.x * dUV2.y) - (dUV1.y * dUV2.x);
				final int sign = (area < 0) ? -1 : 1;
				final Vertex tangent = new Vertex(1, 0, 0);

				tangent.x = (dV1.x * dUV2.y) - (dUV1.y * dV2.x);
				tangent.y = (dV1.y * dUV2.y) - (dUV1.y * dV2.y);
				tangent.z = (dV1.z * dUV2.y) - (dUV1.y * dV2.z);

				tangent.normalize();
				tangent.scale(sign);

				final Vertex faceNormal = new Vertex(v1.getNormal());
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
			final ArrayList<EventObject> sortedEventObjects = sortedIdObjects(EventObject.class);
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
}
