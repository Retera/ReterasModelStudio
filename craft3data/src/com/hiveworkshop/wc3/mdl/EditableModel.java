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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.etheller.collections.Collection;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.datachooser.CompoundDataSource;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.datachooser.FolderDataSource;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.FaceModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.FaceSelectionManager;
import com.hiveworkshop.wc3.mdl.AnimFlag.Entry;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.MeshVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.TriangleVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.VertexVisitor;
import com.hiveworkshop.wc3.mdx.AttachmentChunk;
import com.hiveworkshop.wc3.mdx.BindPoseChunk;
import com.hiveworkshop.wc3.mdx.BoneChunk;
import com.hiveworkshop.wc3.mdx.CameraChunk;
import com.hiveworkshop.wc3.mdx.CollisionShapeChunk;
import com.hiveworkshop.wc3.mdx.CornChunk;
import com.hiveworkshop.wc3.mdx.EventObjectChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;
import com.hiveworkshop.wc3.mdx.GeosetAnimationChunk;
import com.hiveworkshop.wc3.mdx.GeosetChunk;
import com.hiveworkshop.wc3.mdx.HelperChunk;
import com.hiveworkshop.wc3.mdx.LightChunk;
import com.hiveworkshop.wc3.mdx.MaterialChunk;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mdx.ParticleEmitter2Chunk;
import com.hiveworkshop.wc3.mdx.ParticleEmitterChunk;
import com.hiveworkshop.wc3.mdx.RibbonEmitterChunk;
import com.hiveworkshop.wc3.mdx.SequenceChunk.Sequence;
import com.hiveworkshop.wc3.mdx.TextureAnimationChunk.TextureAnimation;
import com.hiveworkshop.wc3.mdx.TextureChunk.Texture;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.MathUtils;
import com.hiveworkshop.wc3.util.ModelUtils;
import com.hiveworkshop.wc3.util.ModelUtils.Mesh;

import de.wc3data.stream.BlizzardDataInputStream;
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

	private int c;
	private boolean temporary;

	private final List<FaceEffectsChunk.FaceEffect> faceEffects = new ArrayList<>();
	private BindPoseChunk bindPoseChunk;

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
			what.printTo(byteArrayOutputStream);
			try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
				final EditableModel newModel = EditableModel.read(bais);
				newModel.setName(newName);
				newModel.setFileRef(what.getFile());
				return newModel;
			}

		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}
		// Write some legit deep clone code later

		// MDL newModel = new MDL(what);
		//
		// newModel.m_anims.clear();
		// for( Animation anim: what.m_anims )
		// {
		// newModel.add(new Animation(anim));
		// }
		// newModel.m_textures.clear();
		// for( Bitmap tex: what.m_textures )
		// {
		// newModel.add(new Bitmap(tex));
		// }
		// newModel.m_materials.clear();
		// for(Material mat: what.m_materials)
		// {
		// newModel.add(new Material(mat));
		// }
		// m_geosets = new ArrayList(other.m_geosets);
		// m_geosetanims = new ArrayList(other.m_geosetanims);
		// m_idobjects = new ArrayList(other.m_idobjects);
		// m_pivots = new ArrayList(other.m_pivots);
		// m_cameras = new ArrayList(other.m_cameras);

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

	public EditableModel(final MdxModel mdx) {
		this();
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the
		// MDL class
		setName(mdx.modelChunk.name);
		addToHeader("//This model was converted from MDX by ogre-lord's Java MDX API and Retera's Java MDL API");
		setBlendTime(mdx.modelChunk.blendTime);
		setExtents(new ExtLog(mdx.modelChunk.minimumExtent, mdx.modelChunk.maximumExtent, mdx.modelChunk.boundsRadius));
		setFormatVersion(mdx.versionChunk.version);

		// Step 2: Convert the Sequences
		if (mdx.sequenceChunk != null) {
			for (final Sequence seq : mdx.sequenceChunk.sequence) {
				add(new Animation(seq));
			}
		}

		// Step 3: Convert any global sequences
		if (mdx.globalSequenceChunk != null) {
			for (int i = 0; i < mdx.globalSequenceChunk.globalSequences.length; i++) {
				add(new Integer(mdx.globalSequenceChunk.globalSequences[i]));
			}
		}

		// Step 4: Convert Texture refs
		if (mdx.textureChunk != null) {
			for (final Texture tex : mdx.textureChunk.texture) {
				add(new Bitmap(tex));
			}
		}

		// Step 6: Convert TVertexAnims
		if (mdx.textureAnimationChunk != null) {
			for (final TextureAnimation txa : mdx.textureAnimationChunk.textureAnimation) {
				add(new TextureAnim(txa));
			}
		}

		// Step 5: Convert Material refs
		if (mdx.materialChunk != null) {
			for (final MaterialChunk.Material mat : mdx.materialChunk.material) {
				add(new Material(mat, this));
			}
		}

		// Step 7: Geoset
		if (mdx.geosetChunk != null) {
			for (final GeosetChunk.Geoset mdxGeo : mdx.geosetChunk.geoset) {
				add(new Geoset(mdxGeo));
			}
		}

		// Step 8: GeosetAnims
		if (mdx.geosetAnimationChunk != null) {
			for (final GeosetAnimationChunk.GeosetAnimation geosetAnim : mdx.geosetAnimationChunk.geosetAnimation) {
				add(new GeosetAnim(geosetAnim));
			}
		}

		// Step 9:
		// convert "IdObjects" as I called them in my high school mdl code
		// (nodes)

		// Bones
		if (mdx.boneChunk != null) {
			for (final BoneChunk.Bone bone : mdx.boneChunk.bone) {
				add(new Bone(bone));
			}
		}
		// Lights
		if (mdx.lightChunk != null) {
			for (final LightChunk.Light light : mdx.lightChunk.light) {
				add(new Light(light));
			}
		}
		// Helpers
		if (mdx.helperChunk != null) {
			for (final HelperChunk.Helper helper : mdx.helperChunk.helper) {
				add(new Helper(helper));
			}
		}
		// Attachment
		if (mdx.attachmentChunk != null) {
			for (final AttachmentChunk.Attachment attachment : mdx.attachmentChunk.attachment) {
				add(new Attachment(attachment));
			}
		}
		// ParticleEmitter (number 1 kind)
		if (mdx.particleEmitterChunk != null) {
			for (final ParticleEmitterChunk.ParticleEmitter emitter : mdx.particleEmitterChunk.particleEmitter) {
				add(new ParticleEmitter(emitter));
			}
		}
		// ParticleEmitter2
		if (mdx.particleEmitter2Chunk != null) {
			for (final ParticleEmitter2Chunk.ParticleEmitter2 emitter : mdx.particleEmitter2Chunk.particleEmitter2) {
				add(new ParticleEmitter2(emitter));
			}
		}
		// PopcornFxEmitter
		if (mdx.cornChunk != null) {
			for (final CornChunk.ParticleEmitterPopcorn emitter : mdx.cornChunk.corns) {
				add(new ParticleEmitterPopcorn(emitter));
			}
		}
		// RibbonEmitter
		if (mdx.ribbonEmitterChunk != null) {
			for (final RibbonEmitterChunk.RibbonEmitter emitter : mdx.ribbonEmitterChunk.ribbonEmitter) {
				add(new RibbonEmitter(emitter));
			}
		}
		// EventObject
		if (mdx.eventObjectChunk != null) {
			for (final EventObjectChunk.EventObject evtobj : mdx.eventObjectChunk.eventObject) {
				final EventObject mdlEvtObj = new EventObject(evtobj);
				add(mdlEvtObj);
			}
		}
		boolean corruptedCameraWarningGiven = false;
		// Camera
		if (mdx.cameraChunk != null) {
			for (final CameraChunk.Camera cam : mdx.cameraChunk.camera) {
				final Camera mdlCam = new Camera(cam);
				if (!corruptedCameraWarningGiven && (mdlCam.getName().contains("????????")
						|| mdlCam.getName().length() > 20 || mdlCam.getName().length() <= 0)) {
					corruptedCameraWarningGiven = true;
					JOptionPane.showMessageDialog(null, "--- " + this.getName()
							+ " ---\nWARNING: Java Warcraft Libraries thinks we are loading a camera with corrupted data due to bug in Native MDX Parser.\nPlease DISABLE \"View > Use Native MDX Parser\" if you want to correctly edit \""
							+ getName()
							+ "\".\nYou may continue to work, but portions of the model's data have been lost, and will be missing if you save.",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				add(mdlCam);
			}
		}
		// CollisionShape
		if (mdx.collisionShapeChunk != null) {
			for (final CollisionShapeChunk.CollisionShape collision : mdx.collisionShapeChunk.collisionShape) {
				final CollisionShape mdlCollision = new CollisionShape(collision);
				add(mdlCollision);
			}
		}

		if (mdx.pivotPointChunk != null) {
			for (int objId = 0; objId < mdx.pivotPointChunk.pivotPoints.length / 3; objId++) {
				addPivotPoint(new Vertex(mdx.pivotPointChunk.pivotPoints[objId * 3 + 0],
						mdx.pivotPointChunk.pivotPoints[objId * 3 + 1],
						mdx.pivotPointChunk.pivotPoints[objId * 3 + 2]));
			}
		}

		if (mdx.faceEffectsChunk != null && ModelUtils.isBindPoseSupported(formatVersion)) {
			for (final FaceEffect facefx : mdx.faceEffectsChunk.faceEffects) {
				addFaceEffect(facefx);
			}
			bindPoseChunk = mdx.bindPoseChunk;
		}

		doPostRead(); // fixes all the things
	}

	public void parseVertex(final String input, final Geoset geoset) {
		final String[] entries = input.split(",");
		try {
			geoset.addVertex(new GeosetVertex(Double.parseDouble(entries[0].substring(4, entries[0].length())),
					Double.parseDouble(entries[1]),
					Double.parseDouble(entries[2].substring(0, entries[2].length() - 1))));
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error (on line " + c + "): Vertex coordinates could not be interpreted.");
		}
	}

	public void parseTriangles(final String input, final Geoset g) {
		// Loading triangles to a geoset requires verteces to be loaded first
		final String[] s = input.split(",");
		s[0] = s[0].substring(4, s[0].length());
		final int s_size = countContainsString(input, ",");
		s[s_size - 1] = s[s_size - 1].substring(0, s[s_size - 1].length() - 2);
		for (int t = 0; t < s_size - 1; t += 3)// s[t+3].equals("")||
		{
			for (int i = 0; i < 3; i++) {
				s[t + i] = s[t + i].substring(1);
			}
			try {
				g.addTriangle(new Triangle(Integer.parseInt(s[t]), Integer.parseInt(s[t + 1]),
						Integer.parseInt(s[t + 2]), g));
			} catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						"Error: Unable to interpret information in Triangles: " + s[t] + ", " + s[t + 1] + ", or "
								+ s[t + 2]);
			}
		}
	}

	public String nextLine(final BufferedReader reader) {
		String output = "";
		try {
			output = reader.readLine();
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(), "Error reading file.");
		}
		c++;
		if (output == null) {
			output = "COMPLETED PARSING";
		}
		return output;
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
					if (visible == null || visible.floatValue() > 0) {
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
				if (localObject != null && localObject instanceof Bone) {
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
				if (localObject != null && localObject instanceof Bone) {
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

	public static EditableModel read(final File f) {
		if (f.getPath().toLowerCase().endsWith(".mdx")) {
			// f = MDXHandler.convert(f);
			try (BlizzardDataInputStream in = new BlizzardDataInputStream(new FileInputStream(f))) {
				final EditableModel mdl = new EditableModel(MdxUtils.loadModel(in));
				mdl.setFileRef(f);
				return mdl;
			} catch (final FileNotFoundException e) {
				throw new RuntimeException(e);
				// e.printStackTrace();
				// f = MDXHandler.convert(f);
				// // return null;
			} catch (final IOException e) {
				throw new RuntimeException(e);
				// e.printStackTrace();
				// f = MDXHandler.convert(f);
				// // return null;
			}
		}
		try (final FileInputStream fos = new FileInputStream(f)) {
			final EditableModel mdlObject = read(fos);
			mdlObject.setFileRef(f);
			return mdlObject;
		} catch (final FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "The file chosen was not found: " + e.getMessage());
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, "The file chosen could not be read: " + e.getMessage());
		}
		return null;
	}

	public static EditableModel read(final InputStream f) {
		try {
			MDLReader.clearLineId();
			BufferedReader mdl;
			// try
			// {
			mdl = new BufferedReader(new InputStreamReader(f));
			// }
			// catch (final IOException e)
			// {
			// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Attempted
			// to read file, but file was not found.");
			// return null;
			// }
			final EditableModel mdlr = new EditableModel();
			String line = "";
			while ((line = MDLReader.nextLineSpecial(mdl)).startsWith("//")) {
				if (!line.contains("// Saved by Retera's MDL Toolkit on ")) {
					mdlr.addToHeader(line);
				}
			}
			if (!line.contains("Version")) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(), "The file version is missing!");
			}
			line = MDLReader.nextLine(mdl);
			mdlr.formatVersion = MDLReader.readInt(line);
			if (mdlr.formatVersion != 800 && mdlr.formatVersion != 900 && mdlr.formatVersion != 1000
					&& mdlr.formatVersion != 1100) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(), "The format version was confusing!");
			}
			line = MDLReader.nextLine(mdl);// this is "}" for format version
			if (!line.startsWith("}")) // now I'll prove it
			{ // gotta have that sense of humor, right?
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						"Model could not be understood. Program does not understand this type of file.");
			}
			line = MDLReader.nextLine(mdl);
			mdlr.setName(MDLReader.readName(line));
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
				if (line.contains("BlendTime")) {
					mdlr.BlendTime = MDLReader.readInt(line);
				} else if (line.contains("Extent")) {
					MDLReader.reset(mdl);
					mdlr.extents = ExtLog.read(mdl);
				}
				MDLReader.mark(mdl);
			}
			MDLReader.mark(mdl);
			mdlr.anims = Sequences.read(mdl);

			// GlobalSequences
			if (mdlr.anims.size() < 1) {
				MDLReader.reset(mdl);
			}
			MDLReader.mark(mdl);
			if ((line = MDLReader.nextLine(mdl)).contains("GlobalSequences")) {
				while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
					if (line.contains("Duration")) {
						mdlr.globalSeqs.add(new Integer(MDLReader.readInt(line)));
					}
				}
			} else {
				MDLReader.reset(mdl);
			}
			mdlr.textures = Bitmap.readAll(mdl);
			mdlr.materials = Material.readAll(mdl, mdlr);
			mdlr.texAnims = TextureAnim.readAll(mdl);
			if (mdlr.materials != null) {
				final int sz = mdlr.materials.size();
				for (int i = 0; i < sz; i++) {
					mdlr.materials.get(i).updateTextureAnims(mdlr.texAnims);
				}
			}
			MDLReader.mark(mdl);
			boolean hadGeosets = false;
			line = MDLReader.nextLine(mdl);
			while (line.contains("Geoset ")) {
				hadGeosets = true;
				MDLReader.reset(mdl);
				mdlr.addGeoset(Geoset.read(mdl));
				MDLReader.mark(mdl);
				line = MDLReader.nextLine(mdl);
			}
			// if( hadGeosets )
			MDLReader.reset(mdl);
			MDLReader.mark(mdl);
			boolean hadGeosetAnims = false;
			while ((line = MDLReader.nextLine(mdl)).contains("GeosetAnim ")) {
				hadGeosetAnims = true;
				MDLReader.reset(mdl);
				mdlr.addGeosetAnim(GeosetAnim.read(mdl));
				MDLReader.mark(mdl);
			}
			// if( hadGeosetAnims )
			MDLReader.reset(mdl);
			line = MDLReader.nextLine(mdl);
			while (line.length() > 1 && !line.equals("COMPLETED PARSING")) {
				if (line.startsWith("Bone ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(Bone.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("Light ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(Light.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("Helper ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(Helper.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("Attachment ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(Attachment.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("ParticleEmitter ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(ParticleEmitter.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("ParticleEmitter2 ")) {
					MDLReader.reset(mdl);
					final ParticleEmitter2 temp = ParticleEmitter2.read(mdl);
					mdlr.addIdObject(temp);
					temp.updateTextureRef(mdlr.textures);
					MDLReader.mark(mdl);
				} else if (line.contains("RibbonEmitter ")) {
					MDLReader.reset(mdl);
					final RibbonEmitter temp = RibbonEmitter.read(mdl);
					mdlr.addIdObject(temp);
					temp.updateMaterialRef(mdlr.materials);
					MDLReader.mark(mdl);
				} else if (line.contains("PopcornFxEmitter ") || line.contains("ParticleEmitterPopcorn ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(ParticleEmitterPopcorn.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("Camera ")) {
					MDLReader.reset(mdl);
					mdlr.addCamera(Camera.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("EventObject ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(EventObject.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("CollisionShape ")) {
					MDLReader.reset(mdl);
					mdlr.addIdObject(CollisionShape.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("PivotPoints ")) {
					while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
						mdlr.addPivotPoint(Vertex.parseText(line));
					}
					MDLReader.mark(mdl);
				} else if (line.contains("FaceEffects ")) {
					// This "FaceEffects " branch is for 2019-2020 RMS MDL format that was not
					// consistent with Blizzard's format, and was invented to give us a way to edit
					// models as text prior to obtaining the official version.
					final FaceEffectsChunk.FaceEffect faceEffect = new FaceEffectsChunk.FaceEffect();
					mdlr.faceEffects.add(faceEffect);
					while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
						final String trimmedLine = line.trim();
						if (trimmedLine.startsWith("Target")) {
							faceEffect.faceEffectTarget = MDLReader.readName(line);
						} else if (trimmedLine.startsWith("Path")) {
							faceEffect.faceEffect = MDLReader.readName(line);
						}
					}
					MDLReader.mark(mdl);
				} else if (line.contains("FaceFX ")) {
					MDLReader.reset(mdl);
					mdlr.addFaceEffect(FaceEffect.read(mdl));
					MDLReader.mark(mdl);
				} else if (line.contains("BindPose ")) {
					mdlr.bindPoseChunk = new BindPoseChunk();
					final List<float[]> bindPoseElements = new ArrayList<>();
					while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
						final String trimmedLine = line.trim();
						if (trimmedLine.startsWith("Matrix")) {
							final float[] matrix = new float[12];
							for (int i = 0; i < 3; i++) {
								parse4FloatBPos(MDLReader.nextLine(mdl), matrix, i);
							}
							MDLReader.nextLine(mdl);
							bindPoseElements.add(matrix);
						} else if (trimmedLine.startsWith("Matrices")) {
							while (!(line = MDLReader.nextLine(mdl)).trim().startsWith("}")) {
								final float[] matrix = new float[12];
								parse12FloatBPos(line, matrix);
								bindPoseElements.add(matrix);
							}
						} else {
							throw new IllegalStateException("Bad tokens in BindPose chunk: " + line);
						}
					}
					mdlr.bindPoseChunk.bindPose = new float[bindPoseElements.size()][];
					for (int i = 0; i < bindPoseElements.size(); i++) {
						mdlr.bindPoseChunk.bindPose[i] = bindPoseElements.get(i);
					}
					MDLReader.mark(mdl);
				}
				line = MDLReader.nextLine(mdl);
			}
			mdlr.updateIdObjectReferences();
			for (final Geoset geo : mdlr.geosets) {
				geo.updateToObjects(mdlr);
			}
			for (final GeosetAnim geoAnim : mdlr.geosetAnims) {
				if (geoAnim.geosetId != -1) {
					geoAnim.geoset = mdlr.getGeoset(geoAnim.geosetId);
					geoAnim.geoset.geosetAnim = geoAnim;// YEAH THIS MAKES SENSE
				}
			}
			final List<AnimFlag> animFlags = mdlr.getAllAnimFlags();// laggggg!
			for (final AnimFlag af : animFlags) {
				af.updateGlobalSeqRef(mdlr);
				if (!af.getName().equals("Scaling") && !af.getName().equals("Translation")
						&& !af.getName().equals("Rotation")) {
				}
			}
			final List<EventObject> evtObjs = mdlr.sortedIdObjects(EventObject.class);
			for (final EventObject af : evtObjs) {
				af.updateGlobalSeqRef(mdlr);
			}
			try {
				mdl.close();
			} catch (final Exception e) {

			}
			return mdlr;
		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			// pane.getStyledDocument().
			// JOptionPane.showMessageDialog(null,newJTextPane(e));
		}
		return null;
	}

	public static void parse4FloatBPos(final String input, final float[] output, final int offset) {
		final String[] entries = input.split(",");
		try {
			output[offset] = Float.parseFloat(entries[0].split("\\{")[1].trim());
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: BindPose Matrix could not be interpreted.");
		}
		for (int i = 1; i < 3; i++) {
			try {
				output[offset + i * 3] = Float.parseFloat(entries[i].trim());
			} catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						"Error {" + input + "}: BindPose Matrix could not be interpreted.");
			}
		}
		try {
			output[offset + 3 * 3] = Float.parseFloat(entries[3].split("}")[0].trim());
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: BindPose Matrix could not be interpreted.");
		}
	}

	public static void parse12FloatBPos(final String input, final float[] output) {
		final String[] entries = input.split(",");
		try {
			output[0] = Float.parseFloat(entries[0].split("\\{")[1].trim());
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: BindPose Matrix could not be interpreted.");
		}
		for (int i = 1; i < 11; i++) {
			try {
				output[i] = Float.parseFloat(entries[i].trim());
			} catch (final NumberFormatException e) {
				JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
						"Error {" + input + "}: BindPose Matrix could not be interpreted.");
			}
		}
		try {
			output[11] = Float.parseFloat(entries[11].split("}")[0].trim());
		} catch (final NumberFormatException e) {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Error {" + input + "}: BindPose Matrix could not be interpreted.");
		}
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

	public void saveFile() {
		printTo(fileRef);
	}

	public void printTo(final File baseFile) {
		File f = baseFile;
		baseFile.getParentFile().mkdirs();
		boolean mdx = false;
		if (f.getPath().toLowerCase().endsWith(".mdx")) {
			// String fp = baseFile.getPath();
			// f = new File(fp.substring(0,fp.length()-1) + "l");
			// mdx = true;
			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(baseFile)) {
				new MdxModel(this).save(out);
				return;
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				final String fp = baseFile.getPath();
				f = new File(fp.substring(0, fp.length() - 1) + "l");
				mdx = true;
			} catch (final IOException e) {
				e.printStackTrace();
				final String fp = baseFile.getPath();
				f = new File(fp.substring(0, fp.length() - 1) + "l");
				mdx = true;
			}
		}
		try {
			printTo(new FileOutputStream(baseFile));
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		if (mdx) {
			MDXHandler.compile(f);
		}
	}

	public void printTo(final OutputStream outputStream) {
		rebuildLists();
		// If rebuilding the lists is to crash, then we want to crash the thread
		// BEFORE clearing the file

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputStream);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to save MDL to file.");
		}

		for (final String s : header) {
			writer.println(s);
		}
		writer.println("// Saved by Retera's MDL Toolkit on " + new Date(System.currentTimeMillis()).toString());
		writer.println("Version {");
		writer.println("\tFormatVersion " + formatVersion + ",");
		writer.println("}");
		writer.println("Model \"" + name + "\" {");
		int sz = geosets.size();
		if (sz > 0) {
			writer.println("\tNumGeosets " + sz + ",");
		}
		sz = geosetAnims.size();
		if (sz > 0) {
			writer.println("\tNumGeosetAnims " + sz + ",");
		}
		sz = countIdObjectsOfClass(Helper.class);
		if (sz > 0) {
			writer.println("\tNumHelpers " + sz + ",");
		}
		sz = countIdObjectsOfClass(Light.class);
		if (sz > 0) {
			writer.println("\tNumLights " + sz + ",");
		}
		sz = countIdObjectsOfClass(Bone.class);
		if (sz > 0) {
			writer.println("\tNumBones " + sz + ",");
		}
		sz = countIdObjectsOfClass(Attachment.class);
		if (sz > 0) {
			writer.println("\tNumAttachments " + sz + ",");
		}
		sz = countIdObjectsOfClass(ParticleEmitter.class);
		if (sz > 0) {
			writer.println("\tNumParticleEmitters " + sz + ",");
		}
		sz = countIdObjectsOfClass(ParticleEmitter2.class);
		if (sz > 0) {
			writer.println("\tNumParticleEmitters2 " + sz + ",");
		}
		sz = countIdObjectsOfClass(ParticleEmitterPopcorn.class);
		if (sz > 0) {
			writer.println("\tNumParticleEmittersPopcorn " + sz + ",");
		}
		sz = countIdObjectsOfClass(RibbonEmitter.class);
		if (sz > 0) {
			writer.println("\tNumRibbonEmitters " + sz + ",");
		}
		sz = countIdObjectsOfClass(EventObject.class);
		if (sz > 0) {
			writer.println("\tNumEvents " + sz + ",");
		}
		sz = faceEffects.size();
		if (sz > 0) {
			writer.println("\tNumFaceFX " + sz + ",");
		}
		writer.println("\tBlendTime " + BlendTime + ",");
		if (extents != null) {
			extents.printTo(writer, 1);
		}
		writer.println("}");

		// Animations
		if (anims != null) {
			if (anims.size() > 0) {
				writer.println("Sequences " + anims.size() + " {");
				for (int i = 0; i < anims.size(); i++) {
					anims.get(i).printTo(writer, 1);
				}
				writer.println("}");
			}
		}

		// Global Sequences
		if (globalSeqs != null) {
			if (globalSeqs.size() > 0) {
				writer.println("GlobalSequences " + globalSeqs.size() + " {");
				for (int i = 0; i < globalSeqs.size(); i++) {
					writer.println("\tDuration " + globalSeqs.get(i).toString() + ",");
				}
				writer.println("}");
			}
		}

		// Textures
		if (textures != null) {
			if (textures.size() > 0) {
				writer.println("Textures " + textures.size() + " {");
				for (int i = 0; i < textures.size(); i++) {
					textures.get(i).printTo(writer, 1);
				}
				writer.println("}");
			}
		}

		// Materials
		if (materials != null) {
			if (materials.size() > 0) {
				writer.println("Materials " + materials.size() + " {");
				for (int i = 0; i < materials.size(); i++) {
					materials.get(i).printTo(writer, 1, formatVersion);
				}
				writer.println("}");
			}
		}

		// TextureAnims
		if (texAnims != null) {
			if (texAnims.size() > 0) {
				writer.println("TextureAnims " + texAnims.size() + " {");
				for (int i = 0; i < texAnims.size(); i++) {
					texAnims.get(i).printTo(writer, 1);
				}
				writer.println("}");
			}
		}

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
		if (geosets != null) {
			if (geosets.size() > 0) {
				for (int i = 0; i < geosets.size(); i++) {
					geosets.get(i).printTo(writer, this, true);
				}
			}
		}

		// GeosetAnims
		for (final GeosetAnim geoAnim : geosetAnims) {
			geoAnim.geosetId = geosets.indexOf(geoAnim.geoset);
		}
		if (geosetAnims != null) {
			if (geosetAnims.size() > 0) {
				for (int i = 0; i < geosetAnims.size(); i++) {
					geosetAnims.get(i).printTo(writer, 0);
				}
			}
		}

		// Clearing pivot points
		pivots.clear();
		for (int i = 0; i < idObjects.size(); i++) {
			pivots.add(idObjects.get(i).pivotPoint);
		}

		boolean pivotsPrinted = false;
		if (pivots.size() == 0) {
			pivotsPrinted = true;
		}
		boolean camerasPrinted = false;
		if (cameras.size() == 0) {
			camerasPrinted = true;
		}

		for (int i = 0; i < idObjects.size(); i++) {
			final IdObject obj = idObjects.get(i);
			if (!pivotsPrinted && (obj.getClass() == ParticleEmitter.class || obj.getClass() == ParticleEmitter2.class
					|| obj.getClass() == ParticleEmitterPopcorn.class || obj.getClass() == RibbonEmitter.class
					|| obj.getClass() == EventObject.class || obj.getClass() == CollisionShape.class)) {
				writer.println("PivotPoints " + pivots.size() + " {");
				for (int p = 0; p < pivots.size(); p++) {
					writer.println("\t" + pivots.get(p).toString() + ",");
				}
				writer.println("}");
				pivotsPrinted = true;
			}
			if (!camerasPrinted && (obj.getClass() == EventObject.class || obj.getClass() == CollisionShape.class)) {
				camerasPrinted = true;
				for (int c = 0; c < cameras.size(); c++) {
					cameras.get(c).printTo(writer);
				}
			}
			obj.printTo(writer);
		}

		if (!pivotsPrinted) {
			writer.println("PivotPoints " + pivots.size() + " {");
			for (int p = 0; p < pivots.size(); p++) {
				writer.println("\t" + pivots.get(p).toString() + ",");
			}
			writer.println("}");
		}

		if (!camerasPrinted) {
			for (int i = 0; i < cameras.size(); i++) {
				cameras.get(i).printTo(writer);
			}
		}

		if (ModelUtils.isBindPoseSupported(formatVersion)) {
			for (int i = 0; i < faceEffects.size(); i++) {
				final FaceEffect faceEffect = faceEffects.get(i);
				writer.println("FaceFX \"" + faceEffect.faceEffectTarget + "\" {");
				writer.println("\tPath \"" + faceEffect.faceEffect + "\",");
				writer.println("}");
			}
		}

		if (bindPoseChunk != null && ModelUtils.isBindPoseSupported(formatVersion)) {
			if (RETERA_FORMAT_BPOS_MATRICES) {
				writer.println("BindPose " + bindPoseChunk.bindPose.length + " {");
				final StringBuilder matrixStringBuilder = new StringBuilder();
				for (int i = 0; i < bindPoseChunk.bindPose.length; i++) {
					Named matrixPredictedParent = null;
					if (i < idObjects.size()) {
						matrixPredictedParent = idObjects.get(i);
					} else if (i < idObjects.size() + cameras.size()) {
						matrixPredictedParent = cameras.get(i - idObjects.size());
					}
					if (matrixPredictedParent != null) {
						writer.println("\tMatrix { // for \"" + matrixPredictedParent.getName() + "\"");
					} else {
						writer.println("\tMatrix {");
					}
					final float[] matrix = bindPoseChunk.bindPose[i];
					for (int j = 0; j < 3; j++) {
						matrixStringBuilder.setLength(0);
						matrixStringBuilder.append("{ ");
						for (int k = 0; k < 4; k++) {
							if (k > 0) {
								matrixStringBuilder.append(", ");
							}
							matrixStringBuilder.append(MDLReader.doubleToString(matrix[k * 3 + j]));
						}
						matrixStringBuilder.append(" },");
						writer.println("\t\t" + matrixStringBuilder.toString());
					}
					writer.println("\t}");
				}
				writer.println("}");
			} else {
				writer.println("BindPose {");
				writer.println("\tMatrices " + bindPoseChunk.bindPose.length + " {");
				final StringBuilder matrixStringBuilder = new StringBuilder();
				for (int i = 0; i < bindPoseChunk.bindPose.length; i++) {
					final float[] matrix = bindPoseChunk.bindPose[i];
					matrixStringBuilder.setLength(0);
					matrixStringBuilder.append("{ ");
					for (int k = 0; k < matrix.length; k++) {
						if (k > 0) {
							matrixStringBuilder.append(", ");
						}
						matrixStringBuilder.append(MDLReader.doubleToString(matrix[k]));
					}
					matrixStringBuilder.append(" },");
//					matrixStringBuilder.append(" // ");
//					matrixStringBuilder.append(i);
					writer.println("\t\t" + matrixStringBuilder.toString());
				}
				writer.println("\t}");
				writer.println("}");
			}
		}

		try {
			writer.close();
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to close MDL writer -- did you run out of hard drive space?");
			ExceptionPopup.display(e);
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
			if (g.material != null && !materials.contains(g.material)) {
				materials.add(g.material);
			}
			g.setMaterialId(materials.indexOf(g.material)); // -1 if null
		}
		final ArrayList<RibbonEmitter> ribbons = sortedIdObjects(RibbonEmitter.class);
		for (final RibbonEmitter r : ribbons) {
			if (r.material != null && !materials.contains(r.material)) {
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
				if (lay.textureAnim != null && !texAnims.contains(lay.textureAnim)) {
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
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final Bitmap texture = lay.getShaderTextures().get(shaderTextureTypeHD);
					if (texture != null) {
						boolean good = true;
						for (final Bitmap btm : textures) {
							if (texture.equals(btm)) {
								good = false;
							}
						}
						if (good) {
							textures.add(texture);
						}
					}
					String flagKey = "TextureID";
					if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
						flagKey = shaderTextureTypeHD.name() + flagKey;
					}
					final AnimFlag af = lay.getFlag(flagKey);
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
			if (pe.texture != null && !textures.contains(pe.texture)) {
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
			if (!globalSeqs.contains(af.globalSeq) && af.globalSeq != null) {
				globalSeqs.add(af.globalSeq);
			}
			af.updateGlobalSeqId(this);// keep the ids straight
		}
		for (final EventObject af : evtObjs) {
			if (!globalSeqs.contains(af.globalSeq) && af.globalSeq != null) {
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
			if (b.geosetId != -1 && b.geosetId < geosets.size()) {
				b.geoset = geosets.get(b.geosetId);
			}
			if (b.geosetAnimId != -1 && b.geosetAnimId < geosetAnims.size()) {
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
					bindPoseChunk = new BindPoseChunk();
					bindPoseChunk.bindPose = new float[idObjects.size() + cameras.size()][];
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
					bindPoseChunk = new BindPoseChunk();
					bindPoseChunk.bindPose = new float[idObjects.size() + cameras.size()][];
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
				allFlags.addAll(lay.anims);
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
				if (lay.anims.contains(aflg)) {
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
				if (lay.anims.contains(aflg)) {
					lay.anims.add(added);
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
			for (int i = 0; i < geosetAnims.size() && noIds; i++) {
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
							} else if (ga != null && ga != b2.geosetAnim) {
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
		if (x.pivotPoint != null && !pivots.contains(x.pivotPoint)) {
			pivots.add(x.pivotPoint);
		}
		if (ModelUtils.isBindPoseSupported(formatVersion) && bindPoseChunk != null) {
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
		if (ModelUtils.isBindPoseSupported(formatVersion) && bindPoseChunk != null) {
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
			if (ModelUtils.isTangentAndSkinSupported(formatVersion) && geoset.getVertices().size() > 0
					&& geoset.getVertex(0).getSkinBones() != null) {
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
				if (lastEntry != null && lastEntry.time == entry.time) {
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
						if (entry.time >= anim.getStart() && entry.time <= anim.getEnd()) {
							if (entry.value instanceof Double) {
								final Double d = (Double) entry.value;
								final Double older = (Double) olderKeyframe;
								final Double old = (Double) oldKeyframe;
								if (older != null && old != null && MathUtils.isBetween(older, old, d)) {
									indicesForDeletion.add(new Integer(i - 1));
								}
							} else if (entry.value instanceof Vertex) {
								final Vertex current = (Vertex) entry.value;
								final Vertex older = (Vertex) olderKeyframe;
								final Vertex old = (Vertex) oldKeyframe;
								if (older != null && old != null && MathUtils.isBetween(older.x, old.x, current.x)
										&& MathUtils.isBetween(older.y, old.y, current.y)
										&& MathUtils.isBetween(older.z, old.z, current.z)) {
									indicesForDeletion.add(new Integer(i - 1));
								}
							} else if (entry.value instanceof QuaternionRotation) {
								final QuaternionRotation current = (QuaternionRotation) entry.value;
								final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
								final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
								final Vertex euler = current.toEuler();
								if (older != null && old != null) {
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
							if (older != null && old != null && MathUtils.isBetween(older, old, d)) {
								indicesForDeletion.add(new Integer(i - 1));
							}
						} else if (entry.value instanceof Vertex) {
							final Vertex current = (Vertex) entry.value;
							final Vertex older = (Vertex) olderKeyframe;
							final Vertex old = (Vertex) oldKeyframe;
							if (older != null && old != null && MathUtils.isBetween(older.x, old.x, current.x)
									&& MathUtils.isBetween(older.y, old.y, current.y)
									&& MathUtils.isBetween(older.z, old.z, current.z)) {
								indicesForDeletion.add(new Integer(i - 1));
							}
						} else if (entry.value instanceof QuaternionRotation) {
							final QuaternionRotation current = (QuaternionRotation) entry.value;
							final QuaternionRotation older = (QuaternionRotation) olderKeyframe;
							final QuaternionRotation old = (QuaternionRotation) oldKeyframe;
							final Vertex euler = current.toEuler();
							if (older != null && old != null) {
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
				final Iterator<AnimFlag> iterator = lay.anims.iterator();
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
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = layer.getShaderTextures();
					final Bitmap bitmap = shaderTextures.get(shaderTextureTypeHD);
					if (bitmap != null && bitmap.equals(texture)) {
						layer.getShaderTextures().put(shaderTextureTypeHD, replacement);
					}
				}
				if (layer.getTextures() != null && layer.getTextures().contains(texture)) {
					for (int i = 0; i < layer.getTextures().size(); i++) {
						if (layer.getTextures().get(i).equals(texture)) {
							layer.getTextures().set(i, replacement);
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

	public List<FaceEffectsChunk.FaceEffect> getFaceEffects() {
		return faceEffects;
	}

	public BindPoseChunk getBindPoseChunk() {
		return bindPoseChunk;
	}

	public void setBindPoseChunk(final BindPoseChunk bindPoseChunk) {
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
			if (path != null && !path.isEmpty()) {
				final int dotIndex = path.lastIndexOf('.');
				if (dotIndex != -1 && !path.endsWith(".blp")) {
					path = path.substring(0, dotIndex);
				}
				if (!path.endsWith(".blp")) {
					path += ".blp";
				}
				tex.setPath(path);
			}
		}
		for (final Material material : model.getMaterials()) {
			for (final Layer layer : material.getLayers()) {
				if (layer.getLayerShader() == LayerShader.HD) {
					layer.setLayerShader(LayerShader.SD);
					final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = layer.getShaderTextures();
					final Bitmap diffuseBitmap = shaderTextures.get(ShaderTextureTypeHD.Diffuse);
					shaderTextures.clear();
					shaderTextures.put(ShaderTextureTypeHD.Diffuse, diffuseBitmap);
					if (material.getFlags().contains("TwoSided")) {
						material.getFlags().remove("TwoSided");
						layer.add("TwoSided");
					}
				}
			}
			for (final Layer layer : material.getLayers()) {
				if (!Double.isNaN(layer.getEmissive())) {
					layer.setEmissive(Double.NaN);
				}
				final AnimFlag flag = layer.getFlag("Emissive");
				if (flag != null) {
					layer.getAnims().remove(flag);
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

	public static void convertToV800BakingTextures(final int targetLevelOfDetail, final EditableModel model,
			final File outputDirectory) {
		// Things to fix:
		// 1.) format version
		model.setFormatVersion(800);
		// 2.) materials: bake to only diffuse
		class BakingInfo {
			String bakedTexturePath;
			AnimFlag alphaFlag;
		}
		final Map<Triangle, Integer> triangleToTeamColorPixelCount = new HashMap<>();
		final Map<Layer, BakingInfo> layerToBakingInfo = new HashMap<>();
		for (final Material material : new HashSet<>(model.getMaterials())) {
			for (final Layer layer : material.getLayers()) {
				if (layer.getLayerShader() == LayerShader.HD) {
					final BakingInfo bakingInfo = new BakingInfo();
					bakingInfo.bakedTexturePath = material.getBakedHDNonEmissiveBufferedImage(
							model.getWrappedDataSource(), outputDirectory, model, targetLevelOfDetail,
							triangleToTeamColorPixelCount);
					layerToBakingInfo.put(layer, bakingInfo);
				}
			}
		}
		for (final Material material : model.getMaterials()) {
			final com.etheller.collections.List<Layer> additionalLayers = new com.etheller.collections.ArrayList<>();
			for (final Layer layer : material.getLayers()) {
				final BakingInfo bakingInfo = layerToBakingInfo.get(layer);
				bakingInfo.alphaFlag = layer.getFlag("Alpha");
				if (layer.getLayerShader() == LayerShader.HD) {
					layer.getAnims().remove(bakingInfo.alphaFlag);
					layer.setLayerShader(LayerShader.SD);
					final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = layer.getShaderTextures();
					final Bitmap diffuseBitmap = shaderTextures.get(ShaderTextureTypeHD.Diffuse);

					final Bitmap emissive = shaderTextures.get(ShaderTextureTypeHD.Emissive);
					if (emissive != null && emissive.getPath().toLowerCase().contains("black32")) {
						final Layer layerThree = new Layer(FilterMode.ADDITIVE, LayerShader.SD);
						layerThree.getShaderTextures().put(ShaderTextureTypeHD.Diffuse, emissive);
						layerThree.setFilterMode(FilterMode.ADDITIVE);
						layerThree.add("Unshaded");
						layerThree.add("Unfogged");
						additionalLayers.add(layer);
					}
					if (bakingInfo.bakedTexturePath != null) {
						shaderTextures.clear();
						final Bitmap newBitmap = new Bitmap(diffuseBitmap);
						shaderTextures.put(ShaderTextureTypeHD.Diffuse, newBitmap);
						newBitmap.setPath(bakingInfo.bakedTexturePath);
						if (material.getFlags().contains("TwoSided")) {
							material.getFlags().remove("TwoSided");
							layer.add("TwoSided");
						}
					}
				}
				if (!Double.isNaN(layer.getEmissive())) {
					layer.setEmissive(Double.NaN);
				}
				final AnimFlag flag = layer.getFlag("Emissive");
				if (flag != null) {
					layer.getAnims().remove(flag);
				}
			}
			Collection.Util.addAll(material.getLayers(), additionalLayers);
		}
		for (final Bitmap tex : model.getTextures()) {
			String path = tex.getPath();
			if (path != null && !path.isEmpty() && !path.toLowerCase().contains("_bake")) {
				final int dotIndex = path.lastIndexOf('.');
				if (dotIndex != -1 && !path.endsWith(".blp")) {
					path = path.substring(0, dotIndex);
				}
				if (!path.endsWith(".blp")) {
					path = "_HD.w3mod:" + path;
					path += ".blp";
				}
				tex.setPath(path);
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
		// - Push the layer alpha onto GeosetAnim alpha because that makes more sense
		// and matches 2002 game structure
		for (final Geoset geoset : model.getGeosets()) {
			final Material material = geoset.getMaterial();
			for (final Layer layer : material.getLayers()) {
				final BakingInfo bakingInfo = layerToBakingInfo.get(layer);
				if (bakingInfo != null) {
					final GeosetAnim geosetAnim = geoset.forceGetGeosetAnim();
					final AnimFlag visibilityFlag = geosetAnim.getVisibilityFlag();
					if (visibilityFlag == null) {
						geosetAnim.addAnimFlag(bakingInfo.alphaFlag);
					} else {
						visibilityFlag.copyFrom(bakingInfo.alphaFlag);
						visibilityFlag.sort();
					}
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

		model.doSavePreps();

		// now stick team color under surfaces that need it
		final ModelViewManager modelViewManager = new ModelViewManager(model);
		final FaceSelectionManager faceSelectionManager = new FaceSelectionManager();
		final FaceModelEditor faceModelEditor = new FaceModelEditor(modelViewManager,
				SaveProfile.get().getPreferences(), faceSelectionManager, ModelStructureChangeListener.DO_NOTHING);
		for (final Geoset geoset : model.getGeosets()) {
			modelViewManager.makeGeosetEditable(geoset);
		}
		final Set<Triangle> selectedTriangles = new HashSet<>();
		for (final Map.Entry<Triangle, Integer> entry : triangleToTeamColorPixelCount.entrySet()) {
			if (entry.getValue() > 0) {
				selectedTriangles.add(entry.getKey());
			}
		}
		faceSelectionManager.setSelection(selectedTriangles);
		final int numberOfGeosetsWithoutTeamColor = model.getGeosetsSize();
		faceModelEditor.addTeamColor();
		for (int i = numberOfGeosetsWithoutTeamColor; i < model.getGeosetsSize(); i++) {
			final Material material = model.getGeoset(i).getMaterial();
			if (material.getLayers().size() > 1) {
				final Layer teamColorLayer = material.getLayers().get(0);
				if (teamColorLayer.isUnshaded()) {
					teamColorLayer.getFlags().remove("Unshaded");
				}
				if (teamColorLayer.isUnfogged()) {
					teamColorLayer.getFlags().remove("Unfogged");
				}
				final Layer diffuseLayer = material.getLayers().get(1);
				if (diffuseLayer.getFilterMode() == FilterMode.TRANSPARENT) {
					diffuseLayer.setFilterMode(FilterMode.BLEND);
				}
			}
		}

		// now split any geosets that are bloated and would bust
		selectedTriangles.clear();
		for (final Geoset geoset : model.getGeosets()) {
			final int matrixSize = geoset.getMatrix().size();
			final int halfMatrixSize = matrixSize / 2;
			if (matrixSize > 256) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					if (vertex.VertexGroup > halfMatrixSize) {
						selectedTriangles.addAll(vertex.triangles);
					}
				}
			}
		}
		if (!selectedTriangles.isEmpty()) {
			// do the split geoset algorithm on some stuff that we picked that probably will
			// get us < 256 matrices chunks hopefully
			faceSelectionManager.setSelection(selectedTriangles);
			faceModelEditor.splitGeoset();
		}
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
				final short offsetWeight = (short) (255 - weight * bones);
				for (int i = 0; i < bones && i < 4; i++) {
					gv.getSkinBones()[i] = gv.getBoneAttachments().get(i);
					gv.getSkinBoneWeights()[i] = weight;
					if (i == 0) {
						gv.getSkinBoneWeights()[i] += offsetWeight;
					}
				}
			}
		}
		for (final Material m : model.getMaterials()) {
			for (final Layer layer : m.getLayers()) {
				layer.setLayerShader(LayerShader.HD);

				final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = layer.getShaderTextures();

				final Bitmap normTex = new Bitmap("Textures\\Normal.dds");
				normTex.setWrapHeight(true);
				normTex.setWrapWidth(true);
				shaderTextures.put(ShaderTextureTypeHD.Normal, normTex);

				final Bitmap ormTex = new Bitmap("Textures\\ORM.dds");
				ormTex.setWrapHeight(true);
				ormTex.setWrapWidth(true);
				shaderTextures.put(ShaderTextureTypeHD.ORM, ormTex);

				final Bitmap black32 = new Bitmap("Textures\\Black32.dds");
				black32.setWrapHeight(true);
				black32.setWrapWidth(true);
				shaderTextures.put(ShaderTextureTypeHD.Emissive, black32);

				final Bitmap teamColor = new Bitmap("");
				teamColor.setReplaceableId(1);
				shaderTextures.put(ShaderTextureTypeHD.TeamColor, teamColor);

				final Bitmap texture2 = new Bitmap("ReplaceableTextures\\EnvironmentMap.dds");
				texture2.setWrapHeight(true);
				texture2.setWrapWidth(true);
				shaderTextures.put(ShaderTextureTypeHD.Reflections, black32);

				layer.setEmissive(1.0);
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

				final double r = 1.0 / (s1 * t2 - s2 * t1);

				final double[] sdir = { (t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r, (t2 * z1 - t1 * z2) * r };
				final double[] tdir = { (s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r, (s1 * z2 - s2 * z1) * r };

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
				final double area = dUV1.x * dUV2.y - dUV1.y * dUV2.x;
				final int sign = area < 0 ? -1 : 1;
				final Vertex tangent = new Vertex(1, 0, 0);

				tangent.x = dV1.x * dUV2.y - dUV1.y * dV2.x;
				tangent.y = dV1.y * dUV2.y - dUV1.y * dV2.y;
				tangent.z = dV1.z * dUV2.y - dUV1.y * dV2.z;

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
				if (af.getGlobalSeq() != null && af.hasGlobalSeq()) {// TODO eliminate redundant structure
					if (af.getGlobalSeq().equals(prevLength)) {
						af.setGlobalSeq(newLength);
					}
				}
			}
			final ArrayList<EventObject> sortedEventObjects = sortedIdObjects(EventObject.class);
			for (final EventObject eventObject : sortedEventObjects) {
				// TODO eliminate redundant structure
				if (eventObject.isHasGlobalSeq() && eventObject.getGlobalSeq() != null) {
					if (eventObject.getGlobalSeq().equals(prevLength)) {
						eventObject.setGlobalSeq(newLength);
					}
				}
			}
			globalSeqs.set(globalSequenceId, newLength);
		}
	}

	public void removeMesh() {
		geosets.clear();
	}
}
