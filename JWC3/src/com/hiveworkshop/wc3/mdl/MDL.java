package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.AnimFlag.Entry;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.TriangleVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.VertexVisitor;
import com.hiveworkshop.wc3.mdx.AttachmentChunk;
import com.hiveworkshop.wc3.mdx.BoneChunk;
import com.hiveworkshop.wc3.mdx.CameraChunk;
import com.hiveworkshop.wc3.mdx.CollisionShapeChunk;
import com.hiveworkshop.wc3.mdx.EventObjectChunk;
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
import com.hiveworkshop.wc3.util.MathUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file format).
 *
 * Eric Theller 11/5/2011
 */
public class MDL implements Named {
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
	 * IMPORTANT: This is the only way to retrieve the true header name from the top of the "model chunk", the same one
	 * set by {@link #setName(String)} function.
	 *
	 * @return
	 */
	public String getHeaderName() {
		return name;
	}

	public void setFile(final File file) {
		fileRef = file;
	}

	public boolean isTemp() {
		return temporary;
	}

	public void setTemp(final boolean flag) {
		temporary = flag;
	}

	public void copyHeaders(final MDL other) {
		fileRef = other.fileRef;
		BlendTime = other.BlendTime;
		if (other.extents != null) {
			extents = new ExtLog(other.extents);
		}
		formatVersion = other.formatVersion;
		header = new ArrayList<>(other.header);
		name = other.name;
	}

	public static MDL clone(final MDL what, final String newName) {
		final MDL newModel = new MDL(what);
		newModel.setName(newName);
		return newModel;
	}

	public static MDL deepClone(final MDL what, final String newName) {
		File temp;
		try {
			temp = File.createTempFile("model_clone", "mdl");
			what.printTo(temp);
			final MDL newModel = MDL.read(temp);

			newModel.setName(newName);
			newModel.setFile(what.getFile());
			temp.deleteOnExit();

			return newModel;
		} catch (final IOException e) {
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

	public MDL() {
		name = "UnnamedModel";
		formatVersion = 800;
	}

	public MDL(final String newName) {
		name = newName;
		formatVersion = 800;
	}

	public MDL(final MDL other) {
		fileRef = other.fileRef;
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
	 * @param flags
	 *            An int representing a set of booleans with its bits
	 * @param mask
	 *            A bit to check against
	 * @return Whether the specified bit was "true" (=1)
	 */
	public static boolean hasFlag(final int flags, final int mask) {
		return (flags & mask) != 0;
	}

	public MDL(final MdxModel mdx) {
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
					JOptionPane.showMessageDialog(null,
							"--- " + this.getName()
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
			loading = false;
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

	public void addGeosetAnim(final GeosetAnim x) {
		geosetAnims.add(x);
	}

	public GeosetAnim getGeosetAnim(final int index) {
		return geosetAnims.get(index);
	}

	public void addCamera(final Camera x) {
		cameras.add(x);
	}

	public void addIdObject(final IdObject x) {
		idObjects.add(x);
	}

	public IdObject getIdObject(final int index) {
		return idObjects.get(index);
	}

	public Bone getBone(final int index) {
		try {
			if (index < idObjects.size()) {
				final IdObject temp = idObjects.get(index);
				if (temp.getClass() == (Bone.class)) {
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
			final List<IdObject> emitters = new ArrayList<>();
			emitters.addAll(particleEmitters2);
			emitters.addAll(particleEmitters);
			emitters.addAll(ribbonEmitters);

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
	 * Copies the animations from another model into this model. Specifically, copies all motion from similarly named
	 * bones and copies in the "Anim" blocks at the top of the MDL for the newly added sections.
	 *
	 * In addition, any bones with significant amounts of motion that were not found to correlate with the contents of
	 * this model get added to this model's list of bones.
	 *
	 * @param other
	 */
	public void addAnimationsFrom(MDL other) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = MDL.deepClone(other, "animation source file");

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

	public List<Animation> addAnimationsFrom(MDL other, final List<Animation> anims) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = MDL.deepClone(other, "animation source file");

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

	public static MDL read(final File f) {
		if (f.getPath().toLowerCase().endsWith(".mdx")) {
			// f = MDXHandler.convert(f);
			try (BlizzardDataInputStream in = new BlizzardDataInputStream(new FileInputStream(f))) {
				final MDL mdl = new MDL(MdxUtils.loadModel(in));
				mdl.fileRef = f;
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
			final MDL mdlObject = read(fos);
			mdlObject.fileRef = f;
			return mdlObject;
		} catch (final FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "The file chosen was not found: " + e.getMessage());
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, "The file chosen could not be read: " + e.getMessage());
		}
		return null;
	}

	public static MDL read(final InputStream f) {
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
			final MDL mdlr = new MDL();
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
			if (mdlr.formatVersion != 800) {
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
			while ((line).contains("Geoset ")) {
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
			while ((line).length() > 1 && !line.equals("COMPLETED PARSING")) {
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

	public void doPostRead() {
		updateIdObjectReferences();
		for (final Geoset geo : geosets) {
			geo.updateToObjects(this);
		}
		for (final GeosetAnim geoAnim : this.geosetAnims) {
			if (geoAnim.geosetId != -1) {
				geoAnim.geoset = this.getGeoset(geoAnim.geosetId);
				geoAnim.geoset.geosetAnim = geoAnim;// YEAH THIS MAKES SENSE
			}
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
		writer.println("// Saved by Retera's MDL Toolkit on " + (new Date(System.currentTimeMillis())).toString());
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
		sz = countIdObjectsOfClass(RibbonEmitter.class);
		if (sz > 0) {
			writer.println("\tNumRibbonEmitters " + sz + ",");
		}
		sz = countIdObjectsOfClass(EventObject.class);
		if (sz > 0) {
			writer.println("\tNumEvents " + sz + ",");
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
					materials.get(i).printTo(writer, 1);
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
					|| obj.getClass() == RibbonEmitter.class || obj.getClass() == EventObject.class
					|| obj.getClass() == CollisionShape.class)) {
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
				if (lay.texture != null && !textures.contains(lay.texture)
						&& (lay.textures == null /*
													 * || lay.textures.size() == 0
													 */)) {
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
				obj.parent = idObjects.get(obj.parentId);
			}
			if (i > pivots.size()) {
				JOptionPane.showMessageDialog(null, "Error: More objects than PivotPoints were found.");
			}
			obj.setPivotPoint(pivots.get(i));
		}
		for (final Bone b : bones) {
			if (b.geosetId != -1 && b.geosetId < geosets.size()) {
				b.geoset = geosets.get(b.geosetId);
			}
			if (b.geosetAnimId != -1 && b.geosetAnimId < geosetAnims.size()) {
				b.geosetAnim = geosetAnims.get(b.geosetAnimId);
			}
		}
	}

	public void updateObjectIds() {
		sortIdObjects();

		// -- Injected in save prep --
		// Delete empty rotation/translation/scaling
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
			obj.parentId = idObjects.indexOf(obj.parent);
		}
		for (final Bone b : bones) {
			b.geosetId = geosets.indexOf(b.geoset);
			b.geosetAnimId = geosetAnims.indexOf(b.geosetAnim);
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
		final ArrayList<RibbonEmitter> ribbonEmitters = sortedIdObjects(RibbonEmitter.class);
		final ArrayList<EventObject> events = sortedIdObjects(EventObject.class);
		final ArrayList<CollisionShape> colliders = sortedIdObjects(CollisionShape.class);

		allObjects.addAll(bones);
		allObjects.addAll(lights);
		allObjects.addAll(helpers);
		allObjects.addAll(attachments);
		allObjects.addAll(particleEmitters);
		allObjects.addAll(particleEmitter2s);
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
		final ArrayList<Bone> bones = sortedIdObjects(Bone.class);
		bones.addAll(sortedIdObjects(Helper.class));// Hey, look at that!
		for (final Bone b : bones) {
			allFlags.addAll(b.animFlags);
		}
		final ArrayList<Light> lights = sortedIdObjects(Light.class);
		for (final Light l : lights) {
			allFlags.addAll(l.animFlags);
		}
		final ArrayList<Attachment> atcs = sortedIdObjects(Attachment.class);
		for (final Attachment x : atcs) {
			allFlags.addAll(x.animFlags);
		}
		final ArrayList<ParticleEmitter2> pes = sortedIdObjects(ParticleEmitter2.class);
		for (final ParticleEmitter2 x : pes) {
			allFlags.addAll(x.animFlags);
		}
		final ArrayList<ParticleEmitter> xpes = sortedIdObjects(ParticleEmitter.class);
		for (final ParticleEmitter x : xpes) {
			allFlags.addAll(x.animFlags);
		}
		final ArrayList<RibbonEmitter> res = sortedIdObjects(RibbonEmitter.class);
		for (final RibbonEmitter x : res) {
			allFlags.addAll(x.animFlags);
		}
		final ArrayList<CollisionShape> cs = sortedIdObjects(CollisionShape.class);
		for (final CollisionShape x : cs) {
			allFlags.addAll(x.animFlags);
		}
		final ArrayList<EventObject> evt = sortedIdObjects(EventObject.class);
		for (final EventObject x : evt) {
			allFlags.addAll(x.animFlags);
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
							}
							b.geoset = g;
						} else if (b.geoset != g) {
							// The bone has only been found by ONE matrix
							b.multiGeoId = true;
							b.geoset = null;
							if (ga != null) {
								b.geosetAnim = ga.getMostVisible(b.geosetAnim);
							}

						}
					} else if (ga != null && ga != b.geosetAnim) {
						b.geosetAnim = ga.getMostVisible(b.geosetAnim);
					}
					IdObject bp = b.parent;
					while (bp != null) {
						if (bp.getClass() == Bone.class) {
							final Bone b2 = ((Bone) bp);
							if (!b2.multiGeoId) {
								if (b2.geoset == null) {
									// The bone has been found by no prior
									// matrices
									if (ga != null) {
										b2.geosetAnim = ga;
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
						bp = bp.parent;
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
	}

	public void add(final Camera x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Added null Camera component to model, which is really bad. Tell Retera you saw this once you have errors.");
		}
		cameras.add(x);
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

	public void setFileRef(final File fileRef) {
		this.fileRef = fileRef;
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

	public void render(final ModelVisitor renderer) {
		int geosetId = 0;
		for (final Geoset geoset : geosets) {
			final GeosetVisitor geosetRenderer = renderer.beginGeoset(geosetId++, geoset.getMaterial(),
					geoset.getGeosetAnim());
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
			geosetRenderer.geosetFinished();
		}
		for (final IdObject object : idObjects) {
			object.apply(renderer);
		}
		for (final Camera camera : cameras) {
			renderer.camera(camera);
		}
	}

	public void simplifyKeyframes() {
		final MDL currentMDL = this;
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
}
