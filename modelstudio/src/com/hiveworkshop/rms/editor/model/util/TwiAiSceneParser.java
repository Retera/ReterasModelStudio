package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import jassimp.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

// Should probably get a better name.. Just thought "AiSceneParser" sounded a bit too much like it were
// from the jassimp lib...
public class TwiAiSceneParser {
	AiScene scene;
	String name;
	Map<AiMaterial, Vec3> materialColors = new HashMap<>();
//	Map<Integer, List<AiBone>> vertToBone = new HashMap<>();
//	Map<Integer, Map<Bone, Float>> vertToBone = new HashMap<>();

	EditableModel editableModel;
	Map<String, Bone> nameBoneMap = new HashMap<>();
	Map<Bone, Mat4> boneToTransMatMap = new HashMap<>();
	Map<Bone, Mat4> boneToTransMulOffMap = new HashMap<>();
	Map<Bone, Mat4> boneToTransMatMapWOP = new HashMap<>();
	Map<Bone, Mat4> boneToOffsMatMap = new HashMap<>();
	Map<Bone, Mat4> boneToOffsMatMapWP = new HashMap<>();
	Map<AiNode, Mat4> boneToBindPoseMat = new HashMap<>();
	Map<String, Bone> nameBoneMap2 = new HashMap<>();
	Map<String, AiBone> nameAiBoneMap = new HashMap<>();
	Map<String, AiNode> nameAiNodeMap = new HashMap<>();
	Map<String, AiNodeAnim> nameAiNodeAnimMap = new HashMap<>();
	Map<String, AiNodeAnim> nameAiNodeAnimWKFsMap = new HashMap<>();
	Set<AiNodeAnim> aiNodeAnimsWithKFs = new HashSet<>();
	AiBuiltInWrapperProvider aiBuiltInWrapperProvider = new AiBuiltInWrapperProvider();
	Map<AiAnimation, Animation> animationMap = new HashMap<>();


	public TwiAiSceneParser(AiScene scene) {
		System.out.println("AiMeshHandler got the scene");
		this.scene = scene;
		editableModel = new EditableModel();
		editableModel.setFormatVersion(1000);
		readMaterials();

//		AiNode sceneRoot = scene.getSceneRoot(aiBuiltInWrapperProvider);
//		collectAllBones(sceneRoot);
//		setCorrectPivotsAndParents(sceneRoot);


		for (AiMesh am : scene.getMeshes()) {
			makeGeoset(am);
//			List<AiBone> bones = am.getBones();
//			AiMatrix4f offsetMatrix = bones.get(0).getOffsetMatrix(aiBuiltInWrapperProvider);

		}

		AiNode sceneRoot = scene.getSceneRoot(aiBuiltInWrapperProvider);
//		printMetadata(sceneRoot);


		AiMatrix4f transform = sceneRoot.getTransform(aiBuiltInWrapperProvider);
		System.out.println("sceneRoot: " + sceneRoot.getName() + ", \n\t" + transform);
//		AiMatrix4f offsetMatrix = nameAiBoneMap.get(sceneRoot.getName()).getOffsetMatrix(aiBuiltInWrapperProvider);
//		if(offsetMatrix != null){
////					pivotPoint = new Vec3(-offsetMatrix.get(0, 3), -offsetMatrix.get(1, 3), -offsetMatrix.get(2, 3));
//			Mat4 offMat = new Mat4(getAsArray(offsetMatrix));
//			if(nameBoneMap.containsKey(sceneRoot.getName())){
//				boneToMatMap.put(nameBoneMap.get(sceneRoot.getName()), offMat);
//			}
//		}

		fetchAnims();



		checkAllNodes(sceneRoot);

		for(AiAnimation aiAnimation : animationMap.keySet()){
			fetchNodeTransformations(aiAnimation, animationMap.get(aiAnimation));
		}

//		sceneRoot.getMetadata().get("").
//		collectAndAddGeosetBones();

		System.out.println("\n");

		System.out.println("sceneRoot: " + sceneRoot);
//		reduceIdenticalAnims();

		new RecalculateExtentsAction(editableModel, editableModel.getGeosets()).redo();

		if(editableModel.getBones().isEmpty() && !editableModel.getGeosets().isEmpty()){
			Bone noBones = new Bone("Found no Bones");
			editableModel.add(noBones);
			for(Geoset geoset : editableModel.getGeosets()){
				for(GeosetVertex vertex : geoset.getVertices()){
					vertex.setSkinBone(noBones, (short) 255, 0);
				}
			}
		}

	}

	private void printMetadata(AiNode node) {
		Map<String, AiMetadataEntry> metadata = node.getMetadata();
		if(!metadata.isEmpty()){
			System.out.println("~~~~ metadata for " + node.getName() + " ~~~~");
			for(String s : metadata.keySet()){
				AiMetadataEntry aiMetadataEntry = metadata.get(s);
//			aiMetadataEntry.getMetaDataType();
				System.out.println(s + " (" + aiMetadataEntry.getMetaDataType() + ")" + ": " + aiMetadataEntry.getData());
			}
			System.out.println("~~~~ ~~~~~~~~ ~~~~");
		}
	}
	private void printMetadata(Map<String, AiMetadataEntry> metadata) {
		if(!metadata.isEmpty()){
			System.out.println("~~~~ metadata for ~~~~");
			for(String s : metadata.keySet()){
				AiMetadataEntry aiMetadataEntry = metadata.get(s);
//			aiMetadataEntry.getMetaDataType();
				System.out.println(s + " (" + aiMetadataEntry.getMetaDataType() + ")" + ": " + aiMetadataEntry.getData());
			}
			System.out.println("~~~~ ~~~~~~~~ ~~~~");
		}
	}

	private void reduceIdenticalAnims(){
		List<IdObject> rootObjects = editableModel.getIdObjects().stream().filter(idObject -> idObject.getParent() == null).collect(Collectors.toList());

		List<Animation> anims = editableModel.getAnims();
		List<Animation> potIdenticalAnims = new ArrayList<>();

		Map<Animation, Set<Animation>> animToIdenticalAnims = new HashMap<>();
		Map<Animation, Set<Animation>> animToNotIdenticalAnims = new HashMap<>();

//		int identicalAnims1 = 0;
//		for (int i = 0; i < anims.size()-1; i++) {
//			Animation animToCheck = anims.get(i);
//			for (int j = i; j < anims.size(); j++) {
//				Animation potIdenticalAnim = anims.get(j);
//				if(animToCheck.getLength() == potIdenticalAnim.getLength()){
//					if(animToIdenticalAnims.computeIfAbsent(animToCheck, k -> new HashSet<>()).add(potIdenticalAnim)) identicalAnims1++;
//				}
//			}
//		}

		Set<Animation> tempAnims = new HashSet<>();
		int identicalAnims = 0;
		for(IdObject idObject : rootObjects){
			for(AnimFlag<?> animFlag: idObject.getAnimFlags()){
				for (int i = 0; i < anims.size()-1; i++) {
					Animation animToCheck = anims.get(i);
					String nameStart = animToCheck.getName().substring(0,17);
					for (int j = i+1; j < anims.size(); j++) {
						Animation potIdenticalAnim = anims.get(j);
						String potName = potIdenticalAnim.getName().substring(0,17);
						if(animToCheck.getLength() == potIdenticalAnim.getLength() && (animToNotIdenticalAnims.get(potIdenticalAnim) == null || !animToNotIdenticalAnims.get(potIdenticalAnim).contains(animToCheck))) {
							TreeMap<Integer, ? extends Entry<?>> entryMap1 = animFlag.getEntryMap(animToCheck);
							TreeMap<Integer, ? extends Entry<?>> entryMap2 = animFlag.getEntryMap(potIdenticalAnim);
							if (entryMap1.equals(entryMap2)) {
//							animToIdenticalAnims.computeIfAbsent(anims.get(i), k -> new HashSet<>()).add(anims.get(j));
								if(animToIdenticalAnims.computeIfAbsent(animToCheck, k -> new HashSet<>()).add(potIdenticalAnim)) identicalAnims++;
							} else if (mapEquals(entryMap1, entryMap2)) {
								if(nameStart.equals(potName)){
									System.out.println("custom equals o.O");
								}
								if(animToIdenticalAnims.computeIfAbsent(animToCheck, k -> new HashSet<>()).add(potIdenticalAnim)) identicalAnims++;
							} else {
//								TreeMap<Integer, ? extends Entry<?>> entryMap1 = entryMap1;
//								TreeMap<Integer, ? extends Entry<?>> entryMap2 = entryMap2;
								boolean equals = entryMap1.equals(entryMap2);

								if(nameStart.equals(potName)){
									System.out.println(equals + " - marking " + potIdenticalAnim.getName() + " as not similar to " + animToCheck.getName() + " (" + idObject.getName() + ")" + entryMap1.lastEntry().getValue().equals(entryMap2.lastEntry().getValue()));
									System.out.println("\tEM1: " + entryMap1.size() + ", first: " + entryMap1.firstEntry() + ", last: " + entryMap1.lastEntry());
									System.out.println("\tEM2: " + entryMap2.size() + ", first: " + entryMap2.firstEntry() + ", last: " + entryMap2.lastEntry());
								}
								animToNotIdenticalAnims.computeIfAbsent(potIdenticalAnim, k -> new HashSet<>()).add(animToCheck);
								animToNotIdenticalAnims.computeIfAbsent(animToCheck, k -> new HashSet<>()).add(potIdenticalAnim);
								if (animToIdenticalAnims.containsKey(animToCheck)) {
//								animToIdenticalAnims.get(animToCheck).remove(potIdenticalAnim);
									if(animToIdenticalAnims.get(animToCheck).remove(potIdenticalAnim)) identicalAnims--;
								}
							}
						}
					}
				}
			}
		}


		for(Animation animation : animToIdenticalAnims.keySet()) {
			if(animToIdenticalAnims.get(animation) == null || animToIdenticalAnims.get(animation).isEmpty()){
				tempAnims.add(animation);
			}
		}
		tempAnims.forEach(animToIdenticalAnims::remove);
		tempAnims.clear();
//		for(animToIdenticalAnims)

		for(Animation animation : animToIdenticalAnims.keySet()) {
			System.out.println(animation.getName() + " matchin: " + animToIdenticalAnims.get(animation).size() + " animatins");
			tempAnims.addAll(animToIdenticalAnims.get(animation));
		}

		System.out.println("potIdenticalAnims: " + identicalAnims + "/" + tempAnims.size() + " of " + anims.size());

		tempAnims.clear();

		for (Animation animation : animToIdenticalAnims.keySet()){

			for(IdObject idObject : editableModel.getIdObjects()){
				for(AnimFlag<?> animFlag: idObject.getAnimFlags()){
					TreeMap<Integer, ? extends Entry<?>> entryMap1 = animFlag.getEntryMap(animation);
					for(Animation animToCheck : animToIdenticalAnims.get(animation)) {
						if(!tempAnims.contains(animToCheck) && !Objects.equals(entryMap1, animFlag.getEntryMap(animToCheck))){
							tempAnims.add(animToCheck);
						}
					}
					animToIdenticalAnims.get(animation).removeAll(tempAnims);
					tempAnims.clear();
				}
			}
		}

		Set<Animation> keysToRemove = new HashSet<>();
		for(Animation animation : animToIdenticalAnims.keySet()) {
			if(animToIdenticalAnims.get(animation) == null || animToIdenticalAnims.get(animation).isEmpty()){
				tempAnims.add(animation);
			} else if(!keysToRemove.contains(animation)){
				keysToRemove.addAll(animToIdenticalAnims.get(animation));
			}
		}
		tempAnims.forEach(animToIdenticalAnims::remove);
		keysToRemove.removeAll(tempAnims);
		keysToRemove.forEach(animToIdenticalAnims::remove);
		tempAnims.clear();

		for(Animation animation : animToIdenticalAnims.keySet()) {
//			System.out.println(animation.getName() + " matchin: " + animToIdenticalAnims.get(animation).size() + " animatins");
			tempAnims.addAll(animToIdenticalAnims.get(animation));
		}

		Set<String> realAnims = new HashSet<>();
		for (Animation animation : anims){
			realAnims.add(animation.getName().replaceAll(" grunt\\.?\\d*$", ""));
		}

		System.out.println("\t culled identical anims: " + animToIdenticalAnims.size() + " mapped to tot " + tempAnims.size() + " of " + anims.size() + " (" + realAnims.size() + ")");
	}

	private boolean mapEquals(Map<?, ?> map, Object o){
		if (o == map) return true;

		if(o instanceof Map<?, ?>){
			Map<?, ?> m = (Map<?, ?>) o;
			if (m.size() != map.size())
				return false;
			for(Object key : map.keySet()){
				Object value = map.get(key);

				if (!Objects.equals(m.get(key), value) || !m.containsKey(key)) {
					return false;
				}

//				if(value == null){
//					if (!m.containsKey(key) || m.get(key) != null){
//						return false;
//					}
//				} else if (!Objects.equals(m.get(key), value)) {
//					return false;
//				}
			}

//			try {
//				for (Map.Entry<?, ?> e : map.entrySet()) {
//					Object key = e.getKey();
//					Object value = e.getValue();
//					if (value == null) {
//						if (!(m.get(key) == null && m.containsKey(key)))
//							return false;
//					} else {
//						if (!value.equals(m.get(key)))
//							return false;
//					}
//				}
//			} catch (ClassCastException | NullPointerException unused) {
//				return false;
//			}

			return true;
		}
		return false;
	}

	private void collectAndAddGeosetBones() {
		Set<Bone> geosetBones = new HashSet<>();
		for (Geoset geoset : editableModel.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				Bone[] skinBoneBones = vertex.getSkinBoneBones();
				if (skinBoneBones != null) {
					geosetBones.addAll(Arrays.asList(skinBoneBones));
				}
			}
		}
		for (Bone bone : geosetBones) {
			if (bone != null) {
				editableModel.add(bone);
				if (!geosetBones.contains(bone.getParent())) {
					bone.setParent(null);
				}
			}
		}
	}

	public static Material createMaterial(AiMaterial aiMaterial, EditableModel model) {
//		System.out.println("aiMaterial: " + aiMaterial);
////		if(aiMaterial.getTextureFile(AiTextureType.NONE, 0) != null) System.out.println("NONE:              " + aiMaterial.getNumTextures(AiTextureType.NONE));
//		if(aiMaterial.getTextureFile(AiTextureType.DIFFUSE, 0) != null) System.out.println("DIFFUSE:           " + aiMaterial.getNumTextures(AiTextureType.DIFFUSE));
//		if(aiMaterial.getTextureFile(AiTextureType.SPECULAR, 0) != null) System.out.println("SPECULAR:          " + aiMaterial.getNumTextures(AiTextureType.SPECULAR));
//		if(aiMaterial.getTextureFile(AiTextureType.AMBIENT, 0) != null) System.out.println("AMBIENT:           " + aiMaterial.getNumTextures(AiTextureType.AMBIENT));
//		if(aiMaterial.getTextureFile(AiTextureType.EMISSIVE, 0) != null) System.out.println("EMISSIVE:          " + aiMaterial.getNumTextures(AiTextureType.EMISSIVE));
//		if(aiMaterial.getTextureFile(AiTextureType.HEIGHT, 0) != null) System.out.println("HEIGHT:            " + aiMaterial.getNumTextures(AiTextureType.HEIGHT));
//		if(aiMaterial.getTextureFile(AiTextureType.NORMALS, 0) != null) System.out.println("NORMALS:           " + aiMaterial.getNumTextures(AiTextureType.NORMALS));
//		if(aiMaterial.getTextureFile(AiTextureType.SHININESS, 0) != null) System.out.println("SHININESS:         " + aiMaterial.getNumTextures(AiTextureType.SHININESS));
//		if(aiMaterial.getTextureFile(AiTextureType.OPACITY, 0) != null) System.out.println("OPACITY:           " + aiMaterial.getNumTextures(AiTextureType.OPACITY));
//		if(aiMaterial.getTextureFile(AiTextureType.DISPLACEMENT, 0) != null) System.out.println("DISPLACEMENT:      " + aiMaterial.getNumTextures(AiTextureType.DISPLACEMENT));
//		if(aiMaterial.getTextureFile(AiTextureType.LIGHTMAP, 0) != null) System.out.println("LIGHTMAP:          " + aiMaterial.getNumTextures(AiTextureType.LIGHTMAP));
//		if(aiMaterial.getTextureFile(AiTextureType.REFLECTION, 0) != null) System.out.println("REFLECTION:        " + aiMaterial.getNumTextures(AiTextureType.REFLECTION));
//		if(aiMaterial.getTextureFile(AiTextureType.BASE_COLOR, 0) != null) System.out.println("BASE_COLOR:        " + aiMaterial.getNumTextures(AiTextureType.BASE_COLOR));
//		if(aiMaterial.getTextureFile(AiTextureType.NORMAL_CAMERA, 0) != null) System.out.println("NORMAL_CAMERA:     " + aiMaterial.getNumTextures(AiTextureType.NORMAL_CAMERA));
//		if(aiMaterial.getTextureFile(AiTextureType.EMISSION_COLOR, 0) != null) System.out.println("EMISSION_COLOR:    " + aiMaterial.getNumTextures(AiTextureType.EMISSION_COLOR));
//		if(aiMaterial.getTextureFile(AiTextureType.METALNESS, 0) != null) System.out.println("METALNESS:         " + aiMaterial.getNumTextures(AiTextureType.METALNESS));
//		if(aiMaterial.getTextureFile(AiTextureType.DIFFUSE_ROUGHNESS, 0) != null) System.out.println("DIFFUSE_ROUGHNESS: " + aiMaterial.getNumTextures(AiTextureType.DIFFUSE_ROUGHNESS));
//		if(aiMaterial.getTextureFile(AiTextureType.AMBIENT_OCCLUSION, 0) != null) System.out.println("AMBIENT_OCCLUSION: " + aiMaterial.getNumTextures(AiTextureType.AMBIENT_OCCLUSION));
////		if(aiMaterial.getTextureFile(AiTextureType.UNKNOWN, 0) != null) System.out.println("UNKNOWN:           " + aiMaterial.getNumTextures(AiTextureType.UNKNOWN));

		Material material = new Material();
		Layer layer = new Layer();
		for(HD_Material_Layer hd_layer : HD_Material_Layer.values()){
			Bitmap texture = loadTexture(model, getTextureFile(aiMaterial, hd_layer), hd_layer);
			layer.setTexture(hd_layer.ordinal(), texture);
		}
		layer.setStaticAlpha(aiMaterial.getOpacity());
		material.addLayer(layer);
		return material;
	}

	private static String getTextureFile(AiMaterial aiMaterial, HD_Material_Layer hd_layer){
		return switch (hd_layer){
			case DIFFUSE -> aiMaterial.getTextureFile(AiTextureType.DIFFUSE, 0);
			case VERTEX -> aiMaterial.getTextureFile(AiTextureType.NORMALS, 0);
			case ORM -> aiMaterial.getTextureFile(AiTextureType.SPECULAR, 0);
			case EMISSIVE -> aiMaterial.getTextureFile(AiTextureType.EMISSIVE, 0);
			case TEAM_COLOR -> aiMaterial.getTextureFile(AiTextureType.BASE_COLOR, 0);
			case REFLECTIONS -> aiMaterial.getTextureFile(AiTextureType.REFLECTION, 0);
		};
	}

	public static Bitmap loadTexture(EditableModel model, String path, HD_Material_Layer hd_layer) {
		if(path == null || path.isBlank()){
			path = hd_layer.getPlaceholderBitmap().getPath();
		}
		for (Bitmap texture : model.getTextures()) {
			if (texture.getPath().equals(path)) {
				return texture;
			}
		}
		Bitmap texture = new Bitmap(path);
		model.add(texture);
		return texture;
	}

	public static Material createMaterial1(AiMaterial aiMaterial, EditableModel model) {
		Material material = new Material();
		Bitmap texture = loadTexture(model, aiMaterial.getTextureFile(AiTextureType.DIFFUSE, 0));
		final Layer diffuseLayer = new Layer(texture);

//		aiMaterial.getNumTextures(AiTextureType.DIFFUSE);
//		diffuseLayer.setTexture(0, texture);
		diffuseLayer.setStaticAlpha(aiMaterial.getOpacity());

//		String textureFile = aiMaterial.getTextureFile(AiTextureType.NORMALS, 0);
//		if(textureFile != null){
//
//		}

		material.addLayer(diffuseLayer);

		return material;
	}
	public static Bitmap loadTexture(EditableModel model, String path) {
		for (Bitmap texture : model.getTextures()) {
			if (texture.getPath().equals(path)) {
				return texture;
			}
		}
		if(path.isBlank()){
			path = "Textures\\BTNtempW.blp";
		}

		Bitmap texture = new Bitmap(path);
		model.add(texture);
		return texture;
	}


	private void fetchAnims() {
		int nextStart = 10;
//		List<Animation> animations = new ArrayList<>();
		Set<Animation> animations = new TreeSet<>();
		Map<Animation, Animation> animToAddedAnim = new HashMap<>();
		for (AiAnimation aiAnimation : scene.getAnimations()) {

//			System.out.println("adding aiAnim: " + aiAnimation.getName() + " with " + aiAnimation.getNumChannels() + "/" + aiAnimation.getChannels().size() + " channels");
			int duration = (int) (aiAnimation.getDuration()/aiAnimation.getTicksPerSecond() * 1000.0);
			Animation animation = new Animation(aiAnimation.getName(), nextStart, nextStart + duration);
			nextStart += duration + 20;
			if(animations.contains(animation)){
				animationMap.put(aiAnimation, animToAddedAnim.get(animation));
			} else {
				animations.add(animation);
				animToAddedAnim.put(animation, animation);
				animationMap.put(aiAnimation, animation);
			}

			int nodesWOkeys = 0;
			for (AiNodeAnim aiNodeAnim : aiAnimation.getChannels()) {
				nameAiNodeAnimMap.computeIfAbsent(aiNodeAnim.getNodeName(), k -> aiNodeAnim);
				if(aiNodeAnim.getNumPosKeys() != 0){
					nameAiNodeAnimWKFsMap.computeIfAbsent(aiNodeAnim.getNodeName(), k -> aiNodeAnim);
					aiNodeAnimsWithKFs.add(aiNodeAnim);
				} else {
					nodesWOkeys++;
				}
			}
//			System.out.println("\t" + nodesWOkeys + " nodes withoute PosKeys found!");
//			fetchNodeTransformations(aiAnimation, animation);
		}
		if (animations.isEmpty()) {
			Animation animation = new Animation("emptyAnim", nextStart, nextStart + 300);
			animations.add(animation);

		}

		for (Animation animation : animations) {
			editableModel.add(animation);
		}
	}

	private void fetchNodeTransformations(AiAnimation aiAnimation, Animation animation) {
		Mat4 tempMat = new Mat4();
		Mat4 tempMat2 = new Mat4();
		Vec3 temp = new Vec3();
		for (AiNodeAnim aiNodeAnim : aiAnimation.getChannels()) {
//			aiNodeAnim.
			IdObject node = nameBoneMap.get(aiNodeAnim.getNodeName());
			AiBone aiBone = nameAiBoneMap.get(aiNodeAnim.getNodeName());
			AiBone aiBoneParent = null;
			AiNode aiNode = nameAiNodeMap.get(aiNodeAnim.getNodeName());
//			if(aiBone != null){
//				System.out.println("\nfound AiBone \"" + aiBone.getName() + "\":\n" + aiBone.getOffsetMatrix(aiBuiltInWrapperProvider));
//			}

			if(node != null){
				if(node.getParent() != null){
					aiBoneParent = nameAiBoneMap.get(node.getParent().getName());
//					tempMat.set(boneToTransMatMap.get(node.getParent()));
				} else {
//					tempMat.setIdentity();
				}
				//aiNode.getTransform(aiBuiltInWrapperProvider);
				//tempMat.set(boneToTransMatMap.get(node)).invert();
				if(aiBone != null){
					AiMatrix4f offsetMatrix = aiBone.getOffsetMatrix(aiBuiltInWrapperProvider);
					if(offsetMatrix != null) {
						float[] matNums = getAsArray2(offsetMatrix);
//						System.out.println("\nfound AiBone \"" + aiBone.getName() + "\" offs:\n" + aiBone.getOffsetMatrix(aiBuiltInWrapperProvider));
						tempMat.set(matNums).invert();
					}
				}
				if(aiBoneParent != null){
					AiMatrix4f offsetMatrix = aiBoneParent.getOffsetMatrix(aiBuiltInWrapperProvider);
					if(offsetMatrix != null) {
						float[] matNums = getAsArray2(offsetMatrix);
//						System.out.println("found AiBoneParent \"" + aiBoneParent.getName() + "\" offs:\n" + aiBoneParent.getOffsetMatrix(aiBuiltInWrapperProvider));
						tempMat.set(matNums).invert();
					}
				}

				tempMat.setIdentity();
				if(aiNode != null){
					//AiMatrix4f offsetMatrix = aiNode.getParent().getTransform(aiBuiltInWrapperProvider);
					AiMatrix4f offsetMatrix = aiNode.getTransform(aiBuiltInWrapperProvider);
					if(offsetMatrix != null) {
						float[] matNums = getAsArray(offsetMatrix);
//						System.out.println("found AiBone \"" + aiNode.getName() + "\" transf:\n" + aiNode.getTransform(aiBuiltInWrapperProvider));
						tempMat.set(matNums);
					}
				}

				temp.set(tempMat.m30, tempMat.m31, tempMat.m32);
				double timeAdj = 1000.0 / aiAnimation.getTicksPerSecond();
				for (int i = 0; i < aiNodeAnim.getNumPosKeys(); i++){
					double time = aiNodeAnim.getPosKeyTime(i) * timeAdj;
//					tempMat2.setIdentity();
//					tempMat2.m03 = aiNodeAnim.getPosKeyX(i);
//					tempMat2.m13 = aiNodeAnim.getPosKeyY(i);
//					tempMat2.m23 = aiNodeAnim.getPosKeyZ(i);
//					tempMat2.mul(tempMat);
//					AiVector posKeyVector = aiNodeAnim.getPosKeyVector(i, new AiBuiltInWrapperProvider());
					Vec3 loc1 = new Vec3(aiNodeAnim.getPosKeyX(i), aiNodeAnim.getPosKeyY(i) , aiNodeAnim.getPosKeyZ(i));
//					Vec3 loc1 = new Vec3(aiNodeAnim.getPosKeyX(i) - tempMat2.m03, aiNodeAnim.getPosKeyY(i) - tempMat2.m13, aiNodeAnim.getPosKeyZ(i) - tempMat2.m23);
//					Vec3 loc = new Vec3(loc1).transformInverted(tempMat);
					Vec3 loc = new Vec3(loc1);
					if(node.getParent() == null){
						loc.sub(node.getPivotPoint());
					} else {
//						temp.set(tempMat.m03, tempMat.m13, tempMat.m23);
//						temp.set(tempMat.m30, tempMat.m31, tempMat.m32);
						loc.sub(temp);
					}
//					loc.transform(tempMat);
//					loc.add(node.getPivotPoint());
//					tempMat2.set(tempMat).translate(loc);
//					loc.set(tempMat2.m03, tempMat2.m13, tempMat2.m23);
//					Vec3 loc = new Vec3(posKeyVector.getX(), posKeyVector.getY(), posKeyVector.getZ());

//					if(i == 0){
//						System.out.println("loc for \"" + node.getName() + "\": " + loc + ", loc1: "  + loc1 + ", piv: " + node.getPivotPoint() + ", temp: " + temp);
//					}
					AnimFlag<Vec3> translationFlag = node.getTranslationFlag();
					if(translationFlag == null){
						translationFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
						translationFlag.setInterpType(InterpolationType.LINEAR);
						node.add(translationFlag);
					}
					translationFlag.addEntry((int) time, loc, animation);
				}
//				tempMat2.setIdentity().translate(temp).invert();
//				tempMat.mul(tempMat2);
				for (int i = 0; i < aiNodeAnim.getNumScaleKeys(); i++){
					double time = aiNodeAnim.getScaleKeyTime(i) * timeAdj;
					Vec3 scale = new Vec3(aiNodeAnim.getScaleKeyX(i), aiNodeAnim.getScaleKeyY(i), aiNodeAnim.getScaleKeyZ(i));
					AnimFlag<Vec3> scalingFlag = node.getScalingFlag();
					if(scalingFlag == null){
						scalingFlag = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING);
						scalingFlag.setInterpType(InterpolationType.LINEAR);
						node.add(scalingFlag);
					}
					if(scale.length() < 0.999  || 1.001 < scale.length()){
						scalingFlag.addEntry((int) time, scale, animation);
					}
				}
				for (int i = 0; i < aiNodeAnim.getNumRotKeys(); i++){
					double time = aiNodeAnim.getRotKeyTime(i) * timeAdj;
					Quat rot = new Quat(aiNodeAnim.getRotKeyX(i), aiNodeAnim.getRotKeyY(i), aiNodeAnim.getRotKeyZ(i), aiNodeAnim.getRotKeyW(i));
					AnimFlag<Quat> rotationFlag = node.getRotationFlag();
					if(rotationFlag == null){
						rotationFlag = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
						rotationFlag.setInterpType(InterpolationType.LINEAR);
						node.add(rotationFlag);
					}
					rotationFlag.addEntry((int) time, rot, animation);
				}
			} else {
//				System.out.println("Intermediat Node!");
//				if(aiNodeAnim.getNumPosKeys()>0){
//					System.out.println("\tloc: " + aiNodeAnim.getPosKeyX(0) + ", " + aiNodeAnim.getPosKeyY(0) + ", " + aiNodeAnim.getPosKeyZ(0) + ", ");
//				}
//				if(aiNodeAnim.getNumScaleKeys()>0){
//					System.out.println("\tsca: " + aiNodeAnim.getScaleKeyX(0) + ", " + aiNodeAnim.getScaleKeyY(0) + ", " + aiNodeAnim.getScaleKeyZ(0) + ", ");
//				}
//
//				if(aiNodeAnim.getNumRotKeys()>0){
//					System.out.println("\trot: " + aiNodeAnim.getRotKeyX(0) + ", " + aiNodeAnim.getRotKeyY(0) + ", " + aiNodeAnim.getRotKeyZ(0) + ", " + aiNodeAnim.getRotKeyW(0) + ", ");
//				}
			}
		}
	}

	private float[] getAsArray(AiMatrix4f offsetMatrix) {
		float[] matNums = new float[16];
		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++){
				matNums[row*4+col] = offsetMatrix.get(col, row);
			}
		}
		return matNums;
	}
	private float[] getAsArray2(AiMatrix4f offsetMatrix) {
		float[] matNums = new float[16];
		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++){
				matNums[row*4+col] = offsetMatrix.get(row, col);
			}
		}
		return matNums;
	}


	private void checkAllNodes(AiNode node) {
		Mat4 offMat = new Mat4().setIdentity();
//		printMetadata(node);

		Bone parent = nameBoneMap.get(node.getName());
		Mat4 parBPMat = new Mat4().setIdentity();

		if(boneToBindPoseMat.containsKey(node)){
			parBPMat.set(boneToBindPoseMat.get(node));
		}

		for (AiNode childNode : node.getChildren()) {
			if (nameBoneMap.containsKey(childNode.getName())) {
				AiNode aiNode1 = nameAiNodeMap.putIfAbsent(childNode.getName(), childNode);

				if(aiNode1 != null){
					String mat1 = aiNode1.getTransform(aiBuiltInWrapperProvider).toString();
					String mat2 = childNode.getTransform(aiBuiltInWrapperProvider).toString();
					if(!mat1.equals(mat2)){
						System.out.println("weird node!");
					}
				}
				Bone bone = nameBoneMap.get(childNode.getName());

				AiMatrix4f transformMatrix = childNode.getTransform(aiBuiltInWrapperProvider);
				Mat4 childTransMat = new Mat4(getAsArray2(transformMatrix));
				boneToTransMatMapWOP.put(bone, childTransMat);

				AiMatrix4f offsetMatrix = nameAiBoneMap.get(childNode.getName()).getOffsetMatrix(aiBuiltInWrapperProvider);
				Mat4 childOffMat = new Mat4().setIdentity();
				if(offsetMatrix != null){
					childOffMat.set(getAsArray2(offsetMatrix));
					childOffMat.invert();
				}
				offMat.set(childOffMat);

				boneToOffsMatMap.put(bone, new Mat4(offMat));

				boneToTransMatMapWOP.put(bone, childTransMat);

				if(offsetMatrix == null){
					offMat.set(childTransMat).mul(parBPMat);
				} else {
					offMat.set(childOffMat);
				}
				boneToBindPoseMat.put(childNode, offMat);
//				Vec3 pivotPoint = new Vec3(transTotMat.m03, transTotMat.m13, transTotMat.m23);
				Vec3 pivotPoint = new Vec3(offMat.m03, offMat.m13, offMat.m23);
//				Vec3 pivotPoint = new Vec3(childOffMat.m03, childOffMat.m13, childOffMat.m23);

//				System.out.println("pivotPoint: " + pivotPoint);
				bone.setParent(parent);
				bone.setPivotPoint(pivotPoint);
				editableModel.add(bone);
			} else {
//				if(!childNode.getName().endsWith("end")){
//					System.out.println("odd bone!" + childNode.getName() + ", aiNode: " + nameAiBoneMap.get(childNode.getName()));
//					System.out.println(childNode.getTransform(aiBuiltInWrapperProvider));
//				}
				AiMatrix4f transformMatrix = childNode.getTransform(aiBuiltInWrapperProvider);
				offMat.set(getAsArray2(transformMatrix));
				boneToBindPoseMat.put(childNode, offMat);

			}
			checkAllNodes(childNode);
		}
	}

	private void printMatrix(Mat4 transMat, String s) {
		System.out.println(s);
		transMat.printMatrix();
	}



	public EditableModel getEditableModel() {
		return editableModel;
	}


	public void makeGeoset(final AiMesh mesh) {
//		System.out.println("IMPLEMENT Geoset(AiMesh)");
//		System.out.println("reading mesh \""+ mesh.getName() + "\" (" + mesh.getNumVertices() + " vertices)");

		Map<Integer, List<SkinBone>> vertToSkinBone = getVertToSkinBone(mesh);

		List<FloatBuffer> uvSets = getUvSets(mesh);

		FloatBuffer vertices = mesh.getPositionBuffer();
		FloatBuffer normals = mesh.getNormalBuffer();

		List<GeosetVertex> geosetVertices = new ArrayList<>();

		Geoset geoset = new Geoset();
		for (int i = 0; i < mesh.getNumVertices(); i++) {
			GeosetVertex gv = new GeosetVertex(vertices.get(), vertices.get(), vertices.get());
			gv.setGeoset(geoset);

			geosetVertices.add(gv);
			gv.initV900();
			List<SkinBone> skinBoneList = vertToSkinBone.get(i);
			if (skinBoneList != null) {
//				skinBoneList.sort(Comparator.comparingInt(SkinBone::getWeight));
				skinBoneList.sort((a, b) -> b.getWeight()-a.getWeight());
				for(int j = 0; j<4; j++){
					if(j < skinBoneList.size()){
						gv.setSkinBone(skinBoneList.get(j), j);
					} else {
						gv.setSkinBone((short) 0, j);
					}
				}
//				if (skinBoneList.size() > 4) System.out.println("bones size: " + skinBoneList.size());
				gv.normalizeBoneWeights();
			}

			if (normals != null) {
				gv.setNormal(new Vec3(normals.get(), normals.get(), normals.get()));
			}


			addUVs(uvSets, gv);
		}
		geoset.addVerticies(geosetVertices);
		geoset.setLevelOfDetail(0);
		geoset.setLevelOfDetailName(mesh.getName());
		editableModel.add(geoset);

		final IntBuffer indices = mesh.getFaceBuffer();

		for (int i = 0, l = mesh.getNumFaces(); i < l; i++) {
//			geoset.add(new Triangle(indices.get(), indices.get(), indices.get(), geoset));
			geoset.add(new Triangle(geosetVertices.get(indices.get()), geosetVertices.get(indices.get()), geosetVertices.get(indices.get()), geoset).addToVerts());
		}
//
		geoset.setMaterial(editableModel.getMaterial(mesh.getMaterialIndex()));
	}

	private void addUVs(List<FloatBuffer> uvSets, GeosetVertex gv) {
		for (FloatBuffer uvSet : uvSets) {
//			Vec2 coord = new Vec2();
//
//			coord.x = uvSet.get();
//			coord.y = uvSet.get();
			Vec2 coord = new Vec2(uvSet.get(), 1f-uvSet.get());

			gv.addTVertex(coord);
		}
		if(gv.getTverts().isEmpty()){
			gv.addTVertex(new Vec2());
		}
	}

	private List<FloatBuffer> getUvSets(AiMesh mesh) {
		List<FloatBuffer> uvSets = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (mesh.hasTexCoords(i)) {
				uvSets.add(mesh.getTexCoordBuffer(i));
			}
		}
		return uvSets;
	}

	private Map<Integer, Map<Bone, Float>> getVertToBone(AiMesh mesh) {
		Map<Integer, Map<Bone, Float>> vertToBone = new HashMap<>();
		for (AiBone aiBone : mesh.getBones()) {
			Bone bone = nameBoneMap.computeIfAbsent(aiBone.getName(), k -> new Bone(aiBone.getName()));
			nameAiBoneMap.putIfAbsent(aiBone.getName(), aiBone);
			for (AiBoneWeight aiBoneWeight : aiBone.getBoneWeights()) {
				vertToBone.computeIfAbsent(aiBoneWeight.getVertexId(), k -> new HashMap<>()).put(bone, aiBoneWeight.getWeight());
			}
		}
		return vertToBone;
	}
	private Map<Integer, List<SkinBone>> getVertToSkinBone(AiMesh mesh) {
		Map<Integer, List<SkinBone>> vertToBone = new HashMap<>();
		for (AiBone aiBone : mesh.getBones()) {
			Bone bone = nameBoneMap.computeIfAbsent(aiBone.getName(), k -> new Bone(aiBone.getName()));
			AiBone boneInMap = nameAiBoneMap.putIfAbsent(aiBone.getName(), aiBone);
			if(boneInMap != null){
				AiMatrix4f offsetMatrixInMap = boneInMap.getOffsetMatrix(aiBuiltInWrapperProvider);
				AiMatrix4f offsetMatrixNextBone = aiBone.getOffsetMatrix(aiBuiltInWrapperProvider);
				if(offsetMatrixInMap == null && offsetMatrixNextBone == null ){
//					System.out.println("both mats was null");
				} else if(offsetMatrixInMap == null && offsetMatrixNextBone != null) {
//					System.out.println("offMatInMap was null");
					nameAiBoneMap.put(aiBone.getName(), aiBone);
				} else if(offsetMatrixInMap != null && offsetMatrixNextBone == null) {
//					System.out.println("offMatNextBone was null");
				} else {
					String mat1 = offsetMatrixInMap.toString();
					String mat2 = offsetMatrixNextBone.toString();
					if(!mat1.equals(mat2)){
						System.out.println("weird bone!");
					}
				}
			}
			for (AiBoneWeight aiBoneWeight : aiBone.getBoneWeights()) {
				vertToBone.computeIfAbsent(aiBoneWeight.getVertexId(), k -> new ArrayList<>())
						.add(new SkinBone((short) (aiBoneWeight.getWeight() * 255), bone));
			}
		}
		return vertToBone;
	}

	private void readMaterials() {
		for (final AiMaterial aiMaterial : scene.getMaterials()) {
//			System.out.println("Reading material \"" + aiMaterial.getName() + "\"");
			final Material material = createMaterial(aiMaterial, editableModel);
//			materialList.add(aiMaterial);
			editableModel.add(material);

			final AiMaterial.Property prop = aiMaterial.getProperty("$raw.Diffuse");
			if (prop != null) {
				final ByteBuffer buffer = (ByteBuffer) prop.getData();
				final float r = buffer.getFloat();
				final float g = buffer.getFloat();
				final float b = buffer.getFloat();

				if (r != 1.0f || g != 1.0f || b != 1.0f) {
					// Alpha?
					materialColors.put(aiMaterial, new Vec3(r, g, b));
				}
			}
		}
	}
}
