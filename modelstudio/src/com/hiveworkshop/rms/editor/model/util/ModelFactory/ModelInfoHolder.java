package com.hiveworkshop.rms.editor.model.util.ModelFactory;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxBone;
import com.hiveworkshop.rms.parsers.mdlx.MdlxGenericObject;
import com.hiveworkshop.rms.util.BiMap;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelInfoHolder {
	public List<Material> materials = new ArrayList<>();
	public ArrayList<String> header = new ArrayList<>();
	public List<Animation> anims = new ArrayList<>();
	public List<Integer> globalSeqs = new ArrayList<>();
	public List<Bitmap> textures = new ArrayList<>();
	public List<SoundFile> sounds = new ArrayList<>();
	public List<TextureAnim> texAnims = new ArrayList<>();
	public List<Geoset> geosets = new ArrayList<>();
	public List<Geoset> animatedGeosets = new ArrayList<>();
	public List<Camera> cameras = new ArrayList<>();
	public List<FaceEffect> faceEffects = new ArrayList<>();
	int formatVersion = 800;
	Map<IdObject, Integer> objToParentIdMap = new HashMap<>();
	Map<Bone, Integer> boneToGeoset = new HashMap<>();
	Map<Bone, Integer> boneToGeosetAnim = new HashMap<>();
	BiMap<Integer, IdObject> idObjMap = new BiMap<>();
	List<Vec3> pivots = new ArrayList<>();
	BindPose bindPose;

	public ModelInfoHolder(int formatVersion) {
		this.formatVersion = formatVersion;
	}

	public ModelInfoHolder add() {
		return this;
	}

	public ModelInfoHolder add(Material material) {
		materials.add(material);
		return this;
	}
	public ModelInfoHolder add(Bitmap bitmap) {
		textures.add(bitmap);
		return this;
	}

	public ModelInfoHolder addPivot(Vec3 vec3) {
		pivots.add(vec3);
		return this;
	}

	public ModelInfoHolder add(MdlxGenericObject mdlxObj, IdObject idObject) {
		idObjMap.put(mdlxObj.objectId, idObject);
		objToParentIdMap.put(idObject, mdlxObj.parentId);

		idObject.setPivotPoint(getPivot(mdlxObj.objectId));

		if (bindPose != null && -1 < mdlxObj.objectId && mdlxObj.objectId < bindPose.getSize()) {
			idObject.setBindPoseM4(bindPose.getBindPose(mdlxObj.objectId));
		} else {
			idObject.getBindPoseM4().translate(idObject.getPivotPoint());
		}

		if(idObject instanceof Bone && mdlxObj instanceof MdlxBone){
			boneToGeoset.put((Bone) idObject, ((MdlxBone)mdlxObj).geosetId);
			boneToGeosetAnim.put((Bone) idObject, ((MdlxBone)mdlxObj).geosetAnimationId);
		}

		return this;
	}

	public ModelInfoHolder add(Camera camera) {
		cameras.add(camera);
		if (bindPose != null && cameras.size() + idObjMap.size() <= bindPose.getSize()) {
			camera.setBindPoseM4(bindPose.getBindPose(cameras.size() - 1 + idObjMap.size()));
		} else {
			camera.getBindPoseM4().translate(camera.getPosition());
		}
		return this;
	}


	public ModelInfoHolder add(Geoset geoset) {
		geosets.add(geoset);
		return this;
	}


	public ModelInfoHolder fixIdObjectParents() {
		for (IdObject idObject : objToParentIdMap.keySet()) {
			int parentId = objToParentIdMap.get(idObject);
			if (parentId != -1 && idObjMap.containsKey(parentId)) {
				idObject.setParent(idObjMap.get(parentId));
			}
		}
		return this;
	}

	public Vec3 getPivot(int objId) {
		if (pivots.size() > objId && objId > -1) {
			return pivots.get(objId);
		}
		System.out.println("set {0, 0, 0} pivot");
		return new Vec3(0, 0, 0);
	}

	public ModelInfoHolder setBindPose(List<float[]> bindPose) {
		this.bindPose = new BindPose(bindPose);
		return this;
	}

	public boolean isTangentAndSkinSupported() {
		return is900orAbove();
	}

	private boolean is900orAbove() {
//		return (formatVersion == 900) || (formatVersion == 1000);
		return 900 <= formatVersion;
	}
}
