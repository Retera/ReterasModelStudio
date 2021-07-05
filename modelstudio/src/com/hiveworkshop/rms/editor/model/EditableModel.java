package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSource;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file format).
 * <p>
 * Eric Theller 11/5/2011
 */
public class EditableModel implements Named {
	public static boolean RETERA_FORMAT_BPOS_MATRICES = false;

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
	protected List<Vec3> pivots = new ArrayList<>();
	protected List<Camera> cameras = new ArrayList<>();
	private final List<FaceEffect> faceEffects = new ArrayList<>();
	private BindPose bindPose;
	private boolean temporary;
	private DataSource wrappedDataSource = GameDataFileSystem.getDefault();

	private ModelIdObjects modelIdObjects = new ModelIdObjects();

	public EditableModel() {
	}

	public EditableModel(final String newName) {
		name = newName;
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
			extents = other.extents.deepCopy();
		}
		formatVersion = other.formatVersion;
		header = new ArrayList<>(other.header);
		name = other.name;
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

	public void clearTexAnims() {
		texAnims.clear();
//		if (texAnims != null) {
//			texAnims.clear();
//		} else {
//			texAnims = new ArrayList<>();
//		}
	}

	public <T extends IdObject> List<? extends IdObject> sortedIdObjects(final Class<T> objectClass) {
		return modelIdObjects.getListByClass(objectClass);
	}

	public List<AnimFlag<?>> getAllAnimFlags() {
		// Probably will cause a bunch of lag, be wary
		final List<AnimFlag<?>> allFlags = Collections.synchronizedList(new ArrayList<>());
		for (final Material m : getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				allFlags.addAll(lay.getAnimFlags());
			}
		}
		if (getTexAnims() != null) {
			for (final TextureAnim texa : getTexAnims()) {
				if (texa != null) {
					allFlags.addAll(texa.getAnimFlags());
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (getGeosetAnims() != null) {
			for (final GeosetAnim ga : getGeosetAnims()) {
				if (ga != null) {
					allFlags.addAll(ga.getAnimFlags());
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}
//		for (final IdObject idObject : idObjects) {
		for (final IdObject idObject : getAllObjects()) {
			allFlags.addAll(idObject.getAnimFlags());
		}
		if (getCameras() != null) {
			for (final Camera x : getCameras()) {
				allFlags.addAll(x.getSourceNode().getAnimFlags());
				allFlags.addAll(x.getTargetNode().getAnimFlags());
			}
		}

		return allFlags;
	}

	public List<VisibilitySource> getAllVis() {
		// Probably will cause a bunch of lag, be wary
		final List<VisibilitySource> allVis = Collections.synchronizedList(new ArrayList<>());
		for (final Material m : getMaterials()) {
			for (final Layer lay : m.getLayers()) {
//				allVis.add(lay.getVisibilitySource());
				VisibilitySource vs = lay.getVisibilitySource();
				if (vs != null) {
					allVis.add(vs);
				}
			}
		}
		if (getTexAnims() != null) {
			for (final TextureAnim texa : getTexAnims()) {
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
		if (getGeosetAnims() != null) {
			for (final GeosetAnim ga : getGeosetAnims()) {
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
		for (final IdObject idObject : getAllObjects()) {
			VisibilitySource vs = idObject.getVisibilitySource();
			if (vs != null) {
				allVis.add(vs);
			}
		}
		if (getCameras() != null) {
			for (final Camera x : getCameras()) {
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

	public int animTrackEnd() {
		int highestEnd = 0;
		for (final Animation a : getAnims()) {
			if (a.getStart() > highestEnd) {
				highestEnd = a.getStart();
			}
			if (a.getEnd() > highestEnd) {
				highestEnd = a.getEnd();
			}
		}
		return highestEnd;
	}


	public void removeAllTimelinesForGlobalSeq(final Integer selectedValue) {
		for (final Material m : getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				lay.removeAllTimelinesForGlobalSeq(selectedValue);
			}
		}
		if (getTexAnims() != null) {
			for (final TextureAnim texa : getTexAnims()) {
				if (texa != null) {
					texa.removeAllTimelinesForGlobalSeq(selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
				}
			}
		}
		if (getGeosetAnims() != null) {
			for (final GeosetAnim ga : getGeosetAnims()) {
				if (ga != null) {
					ga.removeAllTimelinesForGlobalSeq(selectedValue);
				} else {
					JOptionPane.showMessageDialog(null,
							"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
				}
			}
		}

		for (final IdObject object : getAllObjects()) {
			object.removeAllTimelinesForGlobalSeq(selectedValue);
		}

		if (getCameras() != null) {
			for (final Camera x : getCameras()) {
				x.getSourceNode().removeAllTimelinesForGlobalSeq(selectedValue);
				x.getTargetNode().removeAllTimelinesForGlobalSeq(selectedValue);
			}
		}
	}


	public void setGlobalSequenceLength(final int globalSequenceId, final Integer newLength) {
		if (globalSequenceId < globalSeqs.size()) {
			final Integer prevLength = globalSeqs.get(globalSequenceId);
			final List<AnimFlag<?>> allAnimFlags = getAllAnimFlags();
			for (final AnimFlag<?> af : allAnimFlags) {
				if ((af.getGlobalSeqLength() != null) && af.hasGlobalSeq()) {// TODO eliminate redundant structure
					if (af.getGlobalSeqLength().equals(prevLength)) {
						af.setGlobalSeqLength(newLength);
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
			if (!contains(x.getGeoset())) {
				add(x.getGeoset());
			}
			x.getGeoset().add(x);
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

	public void clearGlobalSeqs() {
		globalSeqs.clear();
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

	public void clearMaterials() {
		materials.clear();
	}

	public List<TextureAnim> getTexAnims() {
		return texAnims;
	}

	public List<Geoset> getGeosets() {
		return geosets;
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

	public void clearPivots() {
		pivots.clear();
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

	public void clearTextures() {
		textures.clear();
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
		Map<String, IdObject> nameToIdObjectMap;


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
			nameToIdObjectMap = null;
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
			nameToIdObjectMap = null;
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
			nameToIdObjectMap = null;
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
//			for (final IdObject obj : allObjects) {
//				if (obj.name.equalsIgnoreCase(name)) {
//					return obj;
//				}
//			}
//			return null;
			return getNameToIdObjectMap().get(name);
		}

		Map<String, IdObject> getNameToIdObjectMap() {
			if (nameToIdObjectMap == null) {
				nameToIdObjectMap = new HashMap<>();
				for (IdObject idObject : allObjects) {
					nameToIdObjectMap.put(idObject.getName(), idObject);
				}
			}
			return nameToIdObjectMap;
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
