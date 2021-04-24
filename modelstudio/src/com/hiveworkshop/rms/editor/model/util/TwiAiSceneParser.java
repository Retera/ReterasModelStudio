package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import jassimp.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Should probably get a better name.. Just thought "AiSceneParser" sounded a bit too much like it were
// form the jassimp lib...
public class TwiAiSceneParser {
	AiScene scene;
	String name;
	Map<AiMaterial, Vec3> materialColors = new HashMap<>();
//	Map<Integer, List<AiBone>> vertToBone = new HashMap<>();
//	Map<Integer, Map<Bone, Float>> vertToBone = new HashMap<>();

	EditableModel editableModel;
	Map<String, Bone> nameBoneMap = new HashMap<>();
	AiBuiltInWrapperProvider aiBuiltInWrapperProvider = new AiBuiltInWrapperProvider();


	public TwiAiSceneParser(AiScene scene) {
		System.out.println("AiMeshHandler got the scene");
		this.scene = scene;
		editableModel = new EditableModel();
		readMaterials();
		for (AiMesh am : scene.getMeshes()) {
			makeGeoset(am);
		}

		AiNode sceneRoot = scene.getSceneRoot(aiBuiltInWrapperProvider);

		checkAllNodes(sceneRoot);
		fetchAnims();

//		for(Bone bone : nameBoneMap.values()){
//			editableModel.add(bone);
//		}

		System.out.println("\n");

		System.out.println("sceneRoot: " + sceneRoot);

	}

	private void fetchAnims() {
		int nextStart = 10;
		List<Animation> animations = new ArrayList<>();
		for (AiAnimation aiAnimation : scene.getAnimations()) {
//			System.out.println("adding aiAnim: " + aiAnimation.getName());
			int duration = (int) aiAnimation.getDuration();
			Animation animation = new Animation(aiAnimation.getName(), nextStart, nextStart + duration);
			nextStart += duration + 20;
			animations.add(animation);
		}
		if (animations.isEmpty()) {
			Animation animation = new Animation("emptyAnim", nextStart, nextStart + 300);
			animations.add(animation);

		}

		for (Animation animation : animations) {
			editableModel.add(animation);
		}
//		if(scene.getAnimations().isEmpty()){
//			Animation animation = new Animation("emptyAnim", nextStart, nextStart + 300);
//			editableModel.add(animation);
//		}
	}

	private void checkAllNodes(AiNode node) {
		for (AiNode childNode : node.getChildren()) {
//			checkAllNodes(childNode);
			if (nameBoneMap.containsKey(childNode.getName())) {
				Bone bone = nameBoneMap.get(childNode.getName());

				AiMatrix4f aiMatrix4f = childNode.getTransform(aiBuiltInWrapperProvider);
				Vec3 pivotPoint = new Vec3(aiMatrix4f.get(0, 3), aiMatrix4f.get(1, 3), aiMatrix4f.get(2, 3));


				if (nameBoneMap.containsKey(node.getName())) {
					Bone parent = nameBoneMap.get(node.getName());
					bone.setParent(parent);
					pivotPoint.add(parent.getPivotPoint());
//					System.out.println(bone.getName() + ": " + bone.getPivotPoint() + ", par: " + bone.getParent().getName());
				} else {
//					System.out.println(bone.getName() + ": " + bone.getPivotPoint());
				}
				bone.setPivotPoint(pivotPoint);
				editableModel.add(bone);
			}
			checkAllNodes(childNode);
		}
	}


	public EditableModel getEditableModel() {
		return editableModel;
	}

	//	public EditableModel(final AiScene scene) {
	public void makeModel(final AiScene scene) {
		System.out.println("IMPLEMENT EditableModel(AiScene)");
		final Map<Material, Vec3> materialColors = new HashMap<>();

		for (final AiMesh mesh : scene.getMeshes()) {
			// For now only handle triangular meshes.
			// Note that this doesn't mean polygons are not supported.
			// This is because the meshes are triangularized by Assimp.
			// Rather, this stops line meshes from being imported.
			if (mesh.isPureTriangle()) {
//				final Geoset geoset = makeGeoset(mesh, this);

				// If the material used by this geoset had a diffuse color, add a geoset animation with that color.
//				final Material material = geoset.getMaterial();

//				if (materialColors.containsKey(material)) {
//					final GeosetAnim geosetAnim = new GeosetAnim(geoset);
//					geosetAnim.setStaticColor(materialColors.get(material));
//					add(geosetAnim);
//					geoset.geosetAnim = geosetAnim;
//				}
			}
		}
	}

	//	public Geoset makeGeoset(final AiMesh mesh, final EditableModel model) {
	public void makeGeoset(final AiMesh mesh) {
//		System.out.println("IMPLEMENT Geoset(AiMesh)");
//		System.out.println("reading mesh \""+ mesh.getName() + "\" (" + mesh.getNumVertices() + " vertices)");

		Map<Integer, Map<Bone, Float>> vertToBone = new HashMap<>();
		if (mesh.hasBones()) {
			for (AiBone aiBone : mesh.getBones()) {
				Bone bone = new Bone(aiBone.getName());
				nameBoneMap.put(aiBone.getName(), bone);
//				editableModel.add(bone);
				if (aiBone.getNumWeights() != 0) {
					for (AiBoneWeight aiBoneWeight : aiBone.getBoneWeights()) {
//						System.out.println(aiBoneWeight);
						if (!vertToBone.containsKey(aiBoneWeight.getVertexId())) {
//							System.out.println("weights (" + aiBone.getName() + ") :" + aiBoneWeight.getVertexId());
							List<AiBone> boneList = new ArrayList<>();
							Map<Bone, Float> boneWeightMap = new HashMap<>();
							boneWeightMap.put(bone, aiBoneWeight.getWeight());
							vertToBone.put(aiBoneWeight.getVertexId(), boneWeightMap);
						} else {
							vertToBone.get(aiBoneWeight.getVertexId()).put(bone, aiBoneWeight.getWeight());
						}
//						if(mesh.getName().equals("grunt.001")){
//							System.out.println("weights (" + aiBone.getName() + ") :" + aiBoneWeight.getVertexId());
//						}
					}
				}
			}
		}


////		this.name = mesh.getName();

		List<FloatBuffer> uvSets = new ArrayList<>();

		for (int i = 0; i < 8; i++) {
			if (mesh.hasTexCoords(i)) {
				uvSets.add(mesh.getTexCoordBuffer(i));
			}
		}

		int uvSetCount = Math.max(uvSets.size(), 1);
		boolean hasUVs = uvSets.size() > 0;

		FloatBuffer vertices = mesh.getPositionBuffer();
		FloatBuffer normals = mesh.getNormalBuffer();

		List<GeosetVertex> geosetVertices = new ArrayList<>();
		Map<GeosetVertex, List<GeosetVertex.SkinBone>> geosetVertexSkinBoneMap = new HashMap<>();

		for (int i = 0, l = mesh.getNumVertices(); i < l; i++) {
			GeosetVertex gv = new GeosetVertex(vertices.get(), vertices.get(), vertices.get());

			gv.setVertexGroup(-1);
			geosetVertices.add(gv);
			gv.initV900();
			gv.magicSkinBones();
			List<GeosetVertex.SkinBone> skinBoneList = new ArrayList<>();
			Map<Bone, Float> boneWeightMap = vertToBone.get(i);
//			short[] weights = new short[boneWeightMap.keySet().size()];
			int j = 0;
			for (Bone bone : boneWeightMap.keySet()) {
				gv.setSkinBone(bone, (short) (boneWeightMap.get(bone) * 255), j);
				j++;
//				skinBoneList.add(new GeosetVertex.SkinBone((short) (boneWeightMap.get(bone) * 255), bone));
			}
//			System.out.println("skinBoneList.size(): " + skinBoneList.size());
//			geosetVertexSkinBoneMap.put(gv, skinBoneList);
//			gv.setSkinBones(boneWeightMap.keySet().toArray(Bone[]::new));

			if (normals != null) {
				gv.setNormal(new Vec3(normals.get(), normals.get(), normals.get()));
			}

			for (int uvId = 0; uvId < uvSetCount; uvId++) {
				Vec2 coord = new Vec2();

				if (hasUVs) {
					coord.x = uvSets.get(uvId).get();
					coord.y = uvSets.get(uvId).get();
				}

				gv.addTVertex(coord);
			}
		}

		Geoset geoset = new Geoset();
		geoset.addVerticies(geosetVertices);
		editableModel.add(geoset);

		final IntBuffer indices = mesh.getFaceBuffer();

		for (int i = 0, l = mesh.getNumFaces(); i < l; i++) {
			geoset.add(new Triangle(indices.get(), indices.get(), indices.get(), geoset));
		}
//
		geoset.setMaterial(editableModel.getMaterial(mesh.getMaterialIndex()));
	}

	private void readMaterials() {
		for (final AiMaterial aiMaterial : scene.getMaterials()) {
//			System.out.println("Reading material \"" + aiMaterial.getName() + "\"");
			final Material material = new Material(aiMaterial, editableModel);
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
