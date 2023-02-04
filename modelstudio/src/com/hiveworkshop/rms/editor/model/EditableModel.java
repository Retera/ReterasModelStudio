package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSource;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * A java object to represent and store an MDL 3d model (Warcraft III file format).
 * <p>
 * Eric Theller 11/5/2011
 */
public class EditableModel implements Named {

	private File fileRef;
	private String name = "UnnamedModel";
	private int blendTime = 0;
	private final ExtLog extents = new ExtLog();
	private int formatVersion = 800;
	private final ArrayList<String> header = new ArrayList<>();
	private final ArrayList<String> comments = new ArrayList<>();
	private final List<Animation> anims = new ArrayList<>();
	private final List<GlobalSeq> globalSeqs = new ArrayList<>();
	private final List<Bitmap> textures = new ArrayList<>();
	private final List<Material> materials = new ArrayList<>();
	private final List<TextureAnim> texAnims = new ArrayList<>();
	private final List<Geoset> geosets = new ArrayList<>();
	private final List<Camera> cameras = new ArrayList<>();
	private final List<FaceEffect> faceEffects = new ArrayList<>();
	private boolean useBindPose;
	private boolean temporary;
	private DataSource wrappedDataSource = GameDataFileSystem.getDefault();

	private final ModelIdObjects modelIdObjects = new ModelIdObjects();

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

	@Override
	public String getName() {
		if (fileRef != null && (name.equals("") || name.equals("UnnamedModel"))) {
			return fileRef.getName().split("\\.")[0];
		}
		return getHeaderName();
	}

	public void setName(final String text) {
		name = text;
	}

	@Override
	public String toString() {
		return getName() + " (\"" + getHeaderName() + "\")";
	}

	public String getHeaderName() {
		return name;
	}

	public void copyHeaders(final EditableModel other) {
		setFileRef(other.fileRef);
		blendTime = other.blendTime;
		extents.set(other.extents);
		formatVersion = other.formatVersion;
		header.addAll(other.header);
		name = other.name;
	}

	public void clearToHeader() {
		anims.clear();
		globalSeqs.clear();
		textures.clear();
		materials.clear();
		texAnims.clear();
		geosets.clear();
		cameras.clear();
		modelIdObjects.clearAll();
	}

	public <T extends IdObject> List<? extends IdObject> listForIdObjects(final Class<T> objectClass) {
		return modelIdObjects.getListByClass(objectClass);
	}

	public File getFileRef() {
		return fileRef;
	}

	public void setFilePath(final String path) {
		if(path == null){
			setFileRef(null);
		} else {
			setFileRef(new File(path));
		}
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
		this.extents.set(extents);
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

	public ArrayList<String> getComments() {
		return comments;
	}

	public void addToHeader(final ArrayList<String> header) {
		this.header.addAll(header);
	}


	public boolean isTemp() {
		return temporary;
	}

	public void setTemp(final boolean flag) {
		temporary = flag;
	}

	public boolean isUseBindPose() {
		return useBindPose;
	}

	public void setUseBindPose(final boolean useBindPose) {
		this.useBindPose = useBindPose;
	}

	public int getAnimsSize() {
		return anims.size();
	}

	public int getGeosetsSize() {
		return geosets.size();
	}

	public int getIdObjectsSize() {
		return modelIdObjects.getIdObjectsSize();
	}

	public void sortIdObjects() {
		modelIdObjects.sort();
	}


	public void add(final Animation x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Anim component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			anims.add(x);
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

	public void add(final Bitmap x, int index) {
		if (x != null) {
			if(0 <= index && index !=textures.size()){
				textures.add(index, x);
			} else {
				textures.add(x);
			}
		}
	}

	public void add(final Camera x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Camera component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			cameras.add(x);
			if (ModelUtils.isBindPoseSupported(formatVersion) && useBindPose && x.getBindPose() == null) {
				x.setBindPose(new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0});
			}
		}
	}

	public void add(final GlobalSeq x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null GlobalSeq component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			globalSeqs.add(x);
		}
	}

	public void add(final Geoset x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null Geoset component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			x.setParentModel(this);
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
			if (!contains(x.getGeoset())) {
				add(x.getGeoset());
			}
			x.getGeoset().add(x);
		}
	}

	public void add(final IdObject x) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null IdObject component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			modelIdObjects.addIdObject(x);
			if (ModelUtils.isBindPoseSupported(formatVersion) && useBindPose && x.getBindPose() == null) {
				float xBP = x.pivotPoint.x;
				float yBP = x.pivotPoint.y;
				float zBP = x.pivotPoint.z;
				x.setBindPose(new float[] {
						1, 0, 0,
						0, 1, 0,
						0, 0, 1,
						xBP, yBP, zBP});
			}
		}
	}
	public void add(final IdObject x, int pos) {
		if (x == null) {
			JOptionPane.showMessageDialog(null,
					"Tried to add null IdObject component to model, which is really bad. Tell Retera you saw this once you have errors.");
		} else {
			modelIdObjects.addIdObject(x, pos);
			if (ModelUtils.isBindPoseSupported(formatVersion) && useBindPose && x.getBindPose() == null) {
				float xBP = x.pivotPoint.x;
				float yBP = x.pivotPoint.y;
				float zBP = x.pivotPoint.z;
				x.setBindPose(new float[] {
						1, 0, 0,
						0, 1, 0,
						0, 0, 1,
						xBP, yBP, zBP});
			}
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

	public void addFaceEffect(final FaceEffect faceEffect) {
		faceEffects.add(faceEffect);
	}

	public void addToHeader(final String comment) {
		header.add(comment);
	}

	public void addComment(final String comment) {
		comments.add(comment);
	}

	public boolean contains(final Animation x) {
		return anims.contains(x);
	}
	public boolean contains(final Sequence x) {
		return anims.contains(x) || globalSeqs.contains(x);
	}

	public boolean contains(final Bitmap x) {
		return textures.contains(x);
	}

	public boolean contains(final Camera x) {
		return cameras.contains(x);
	}

	public boolean contains(final Geoset x) {
		return geosets.contains(x);
	}

	public boolean contains(final GlobalSeq x) {
		return globalSeqs.contains(x);
	}

	public boolean contains(final IdObject x) {
		return modelIdObjects.contains(x);
	}

	public boolean contains(final Material x) {
		return materials.contains(x);
	}

	public boolean contains(final TextureAnim x) {
		return texAnims.contains(x);
	}

	public boolean contains(final FaceEffect x) {
		return faceEffects.contains(x);
	}

	public void remove(final Animation a) {
		anims.remove(a);
	}

	public void remove(final Bitmap texture) {
		textures.remove(texture);
	}

	public void remove(final Camera camera) {
		cameras.remove(camera);
	}

	public void remove(final Geoset g) {
		geosets.remove(g);
	}

	public void remove(final TextureAnim o) {
		texAnims.remove(o);
	}

	public void remove(final GlobalSeq g) {
		globalSeqs.remove(g);
	}

	public void remove(final IdObject o) {
		modelIdObjects.removeIdObject(o);
	}

	public void remove(final Material material) {
		materials.remove(material);
	}

	public void remove(final FaceEffect faceEffect) {
		faceEffects.remove(faceEffect);
	}

	public void clearHeader() {
		header.clear();
	}

	public void clearComments() {
		comments.clear();
	}

	public void clearAnimations() {
		anims.clear();
	}

	public void clearGeosets() {
		geosets.clear();
	}

	public void clearTextures() {
		textures.clear();
	}

	public void clearTexAnims() {
		texAnims.clear();
	}

	public void clearGlobalSeqs() {
		globalSeqs.clear();
	}

	public void clearMaterials() {
		materials.clear();
	}

	public void clearAllIdObjects() {
		modelIdObjects.clearAll();
	}

	public IdObject getObject(final String name) {
		return modelIdObjects.getObject(name);
	}

	public Animation getAnim(final int i) {
		return (okIndex(i, anims)) ? anims.get(i) : null;
	}

	public GlobalSeq getGlobalSeq(final int i) {
		return (okIndex(i, globalSeqs)) ? globalSeqs.get(i) : null;
	}
	public GlobalSeq getGlobalSeqByLength(final int length) {
		for(GlobalSeq globalSeq : globalSeqs){
			if(globalSeq.getLength() == length){
				return globalSeq;
			}
		}
		return null;
	}

	public Bitmap getTexture(final int i) {
		return (okIndex(i, textures)) ? textures.get(i) : null;
	}
	public TextureAnim getTexAnim(final int i) {
		return (okIndex(i, texAnims)) ? texAnims.get(i) : null;
	}

	public Material getMaterial(final int i) {
		return (okIndex(i, materials)) ? materials.get(i) : null;
	}

	public IdObject getIdObject(final int index) {
		return modelIdObjects.getIdObject(index);
	}

	public Geoset getGeoset(final int i) {
		return (okIndex(i, geosets)) ? geosets.get(i) : null;
	}

	private boolean okIndex(int i, List<?> list) {
		return 0 <= i && i < list.size();
	}

	public int computeMaterialID(final Material material) {
		return materials.indexOf(material);
	}

	public int getGeosetId(final Geoset g) {
		return geosets.indexOf(g);
	}

	public int getObjectId(final IdObject idObject) {
		return modelIdObjects.getObjectId(idObject);
	}

	public int getGlobalSeqId(final GlobalSeq inte) {
		return globalSeqs.indexOf(inte);
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

	public int getId(final Object object) {
		if (object instanceof GlobalSeq){
			return globalSeqs.indexOf(object);
		} else if (object instanceof Animation){
			return anims.indexOf(object);
		} else if (object instanceof TextureAnim){
			return texAnims.indexOf(object);
		} else if (object instanceof Bitmap){
			return textures.indexOf(object);
		} else if (object instanceof Material){
			return materials.indexOf(object);
		} else if (object instanceof Geoset){
			return geosets.indexOf(object);
		} else if (object instanceof IdObject){
			return modelIdObjects.getObjectId((IdObject) object);
		} else if (object instanceof Camera){
			return cameras.indexOf(object);
		}
		return -1;
	}

	public List<Animation> getAnims() {
		return anims;
	}

	public List<GlobalSeq> getGlobalSeqs() {
		return globalSeqs;
	}

	public List<Sequence> getAllSequences() {
		ArrayList<Sequence> sequences = new ArrayList<>(globalSeqs);
		sequences.addAll(anims);
		return sequences;
	}

	public List<Camera> getCameras() {
		return cameras;
	}

	public List<Geoset> getGeosets() {
		return geosets;
	}

	public List<Material> getMaterials() {
		return materials;
	}

	public List<Bitmap> getTextures() {
		return textures;
	}

	public List<TextureAnim> getTexAnims() {
		return texAnims;
	}

	public List<FaceEffect> getFaceEffects() {
		return faceEffects;
	}

	public List<IdObject> getIdObjects() {
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
//				System.out.println("adding Bone: " + idObject.getName());
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

		void addIdObject(IdObject idObject, int pos) {
			if (pos == -1) {
				addIdObject(idObject);
			} else {
				if (idObject instanceof Light) {
					lights.add(Math.min(pos, lights.size()), (Light) idObject);
				} else if (idObject instanceof Helper) {
					helpers.add(Math.min(pos, helpers.size()), (Helper) idObject);
				} else if (idObject instanceof Bone) {
//				System.out.println("adding Bone: " + idObject.getName());
					bones.add(Math.min(pos, bones.size()), (Bone) idObject);
				} else if (idObject instanceof Attachment) {
					attachments.add(Math.min(pos, attachments.size()), (Attachment) idObject);
				} else if (idObject instanceof ParticleEmitter) {
					particleEmitters.add(Math.min(pos, particleEmitters.size()), (ParticleEmitter) idObject);
				} else if (idObject instanceof ParticleEmitter2) {
					particleEmitter2s.add(Math.min(pos, particleEmitter2s.size()), (ParticleEmitter2) idObject);
				} else if (idObject instanceof ParticleEmitterPopcorn) {
					popcornEmitters.add(Math.min(pos, popcornEmitters.size()), (ParticleEmitterPopcorn) idObject);
				} else if (idObject instanceof RibbonEmitter) {
					ribbonEmitters.add(Math.min(pos, ribbonEmitters.size()), (RibbonEmitter) idObject);
				} else if (idObject instanceof EventObject) {
					events.add(Math.min(pos, events.size()), (EventObject) idObject);
				} else if (idObject instanceof CollisionShape) {
					colliders.add(Math.min(pos, colliders.size()), (CollisionShape) idObject);
				}
				allObjects.add(idObject);
				idToIdObjectMap = null;
				idObjectToIdMap = null;
				nameToIdObjectMap = null;
			}
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

		IdObject getIdObject(int index) {
			return getIdToIdObjectMap().get(index);
		}

		IdObject getObject(final String name) {
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
