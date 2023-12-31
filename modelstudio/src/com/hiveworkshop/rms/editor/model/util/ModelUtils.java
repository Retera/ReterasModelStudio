package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.util.*;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ModelUtils {
	public static String getPortrait(String filepath) {
		int endIndex = filepath.contains(".") ? filepath.lastIndexOf('.') : filepath.length();
		return filepath.substring(0, endIndex) + "_portrait" + filepath.substring(endIndex);
	}

	public static Mesh getPlaneMesh2(Vec3 max, Vec3 min, int lengthSegs, int widthSegs) {
		Mesh planeMesh = getPlane(lengthSegs, widthSegs);
		Vec3 size = new Vec3(max).sub(min);
		System.out.println("max: " + max + ", min: " + min);
		for(GeosetVertex vertex : planeMesh.getVertices()){
			vertex.multiply(size).add(min);
		}
		return planeMesh;
	}
	public static Mesh getPlane(int subDivX, int subDivY) {
		GeosetVertex[][] vertexGrid = new GeosetVertex[subDivY + 1][subDivX + 1];
		Mesh mesh = new Mesh();

		for (int y = 0; y < (subDivY + 1); y++) {
			for (int x = 0; x < (subDivX + 1); x++) {
				GeosetVertex vertex = new GeosetVertex(x / (float) subDivX, y / (float) subDivY, 0, new Vec3(Vec3.Z_AXIS));
				vertex.addTVertex(new Vec2(y / (float) subDivY, x / (float) subDivX));
				vertexGrid[y][x] = vertex;
				mesh.add(vertex);
			}
		}

		for (int y = 0; y < subDivY; y++) {
			for (int x = 0; x < subDivX; x++) {
				GeosetVertex upperL = vertexGrid[y][x];
				GeosetVertex upperR = vertexGrid[y][x + 1];
				GeosetVertex lowerL = vertexGrid[y + 1][x];
				GeosetVertex lowerR = vertexGrid[y + 1][x + 1];

				mesh.add(new Triangle(lowerL, upperL, upperR).addToVerts());
				mesh.add(new Triangle(lowerR, lowerL, upperR).addToVerts());
			}
		}

		return mesh;
	}

	public static Mesh getBoxMesh2(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs) {
		Mesh boxMesh2 = getBoxMesh2(lengthSegs, widthSegs, heightSegs);
		Vec3 size = new Vec3(max).sub(min);
		for(GeosetVertex vertex : boxMesh2.getVertices()){
			vertex.multiply(size).add(min);
		}
		return boxMesh2;
	}

	public static Mesh getBoxMesh2(int xSegs, int ySegs, int zSegs) {
		Mesh box = new Mesh(new ArrayList<>(), new ArrayList<>());

		Vec3 spinPoint = new Vec3(0.5, 0.5, 0.5);
		Quat rot = new Quat();

		Mesh[] sides = new Mesh[] {
				getPlane(xSegs, ySegs).translate(Vec3.Z_AXIS), getPlane(xSegs, ySegs).translate(Vec3.Z_AXIS),
				getPlane(xSegs, zSegs).translate(Vec3.Z_AXIS), getPlane(xSegs, zSegs).translate(Vec3.Z_AXIS),
				getPlane(ySegs, zSegs).translate(Vec3.Z_AXIS), getPlane(ySegs, zSegs).translate(Vec3.Z_AXIS)
		};


		sides[0].rotate(spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI)));
		sides[2].rotate(spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI / 2.0)));
		sides[3].rotate(spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (-Math.PI / 2.0)));
		sides[4].rotate(spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (Math.PI / 2.0)));
		sides[5].rotate(spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (-Math.PI / 2.0)));

		for (Mesh side : sides) {
			box.addVertices(side.getVertices());
			box.addTriangles(side.getTriangles());
		}
		return box;
	}


	public static Material getWhiteMaterial(EditableModel model) {
//		Material material = new Material(new Layer(new Bitmap("Textures\\White.blp")));
		Material material = new Material(new Layer(new Bitmap("Textures\\BTNtempW.blp")));
		if (model.getMaterials().contains(material)) {
			int i = model.getMaterials().indexOf(material);
			return model.getMaterial(i);
		}
		return material;
	}

	public static float[] flipRGBtoBGR(float[] rgb) {
		float[] bgr = new float[3];
		for (int i = 0; i < 3; i++) {
			bgr[i] = rgb[2 - i];
		}
		return bgr;
	}

	public static boolean isLevelOfDetailSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	public static boolean isShaderStringSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	public static boolean isTangentAndSkinSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	public static boolean isTangentAndSkinSupported(EditableModel model) {
		return is900OrAbove(model.getFormatVersion());
	}

	public static boolean isBindPoseSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	public static boolean isEmissiveLayerSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	public static boolean isFresnelColorLayerSupported(int formatVersion) {
		return 1000 <= formatVersion;
	}

	public static boolean isCornSupported(int formatVersion) {
		return is900OrAbove(formatVersion);
	}

	private static boolean is900OrAbove(int formatVersion) {
//		return (formatVersion == 900) || (formatVersion == 1000);
		return 900 <= formatVersion;
	}

	public static List<TimelineContainer> getAllVis(EditableModel model) {
		// Probably will cause a bunch of lag, be wary
		List<TimelineContainer> allVis = Collections.synchronizedList(new ArrayList<>());
		for (Material m : model.getMaterials()) {
			for (Layer lay : m.getLayers()) {
				TimelineContainer vs = lay.getVisibilitySource();
				if (vs != null) {
					allVis.add(vs);
				}
			}
		}
		for (TextureAnim texa : model.getTexAnims()) {
			if (texa != null) {
				TimelineContainer vs = texa.getVisibilitySource();
				if (vs != null) {
					allVis.add(vs);
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
			}
		}
		for (Geoset geoset : model.getGeosets()) {
			if (geoset != null) {
				TimelineContainer vs = geoset.getVisibilitySource();
				if (vs != null) {
					allVis.add(vs);
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
			}
		}
		for (IdObject idObject : model.getIdObjects()) {
			TimelineContainer vs = idObject.getVisibilitySource();
			if (vs != null) {
				allVis.add(vs);
			}
		}
		for (Camera x : model.getCameras()) {
			TimelineContainer vs1 = x.getSourceNode().getVisibilitySource();
			if (vs1 != null) {
				allVis.add(vs1);
			}
			TimelineContainer vs2 = x.getTargetNode().getVisibilitySource();
			if (vs2 != null) {
				allVis.add(vs2);
			}
		}

		return allVis;
	}

	public static int animTrackEnd(EditableModel model) {
		int highestEnd = 0;
		for (final Animation a : model.getAnims()) {
			if (a.getStart() > highestEnd) {
				highestEnd = a.getStart();
			}
			if (a.getEnd() > highestEnd) {
				highestEnd = a.getEnd();
			}
		}
		return highestEnd;
	}

	public static List<AnimFlag<?>> getAllAnimFlags(EditableModel model) {
		ModelUtils.doForAnimFlags(model, animFlag -> {
		});
		// Probably will cause a bunch of lag, be wary
		List<AnimFlag<?>> allFlags = Collections.synchronizedList(new ArrayList<>());
		for (Material m : model.getMaterials()) {
			for (Layer lay : m.getLayers()) {
				allFlags.addAll(lay.getAnimFlags());
			}
		}
		for (TextureAnim texa : model.getTexAnims()) {
			if (texa != null) {
				allFlags.addAll(texa.getAnimFlags());
			} else {
				JOptionPane.showMessageDialog(null,
						"WARNING: Error with processing time-scale from TextureAnims! Program will attempt to proceed.");
			}
		}
		for (Geoset geoset : model.getGeosets()) {
			if (geoset != null) {
				allFlags.addAll(geoset.getAnimFlags());
			} else {
				JOptionPane.showMessageDialog(null,
						"WARNING: Error with processing time-scale from GeosetAnims! Program will attempt to proceed.");
			}
		}
		for (IdObject idObject : model.getIdObjects()) {
			allFlags.addAll(idObject.getAnimFlags());
		}
		for (Camera x : model.getCameras()) {
			allFlags.addAll(x.getSourceNode().getAnimFlags());
			allFlags.addAll(x.getTargetNode().getAnimFlags());
		}

		return allFlags;
	}

	public static void doForAnimFlags(EditableModel model, Consumer<AnimFlag<?>> flagConsumer) {
		for (Material m : model.getMaterials()) {
			for (Layer lay : m.getLayers()) {
				lay.getAnimFlags().forEach(flagConsumer);
				for (Layer.Texture texture: lay.getTextureSlots()) {
					texture.getAnimFlags().forEach(flagConsumer);
				}
			}
		}
		for (TextureAnim texa : model.getTexAnims()) {
			texa.getAnimFlags().forEach(flagConsumer);
		}
		for (Geoset geoset : model.getGeosets()) {
			geoset.getAnimFlags().forEach(flagConsumer);
		}
		for (IdObject idObject : model.getIdObjects()) {
			idObject.getAnimFlags().forEach(flagConsumer);
		}
		for (Camera x : model.getCameras()) {
			x.getSourceNode().getAnimFlags().forEach(flagConsumer);
			x.getTargetNode().getAnimFlags().forEach(flagConsumer);
		}

	}

	/**
	 * Finds the outer edges of the meshes contained in the provided collection.
	 * This will go through the triangles of the vertices in the collection and
	 * for triangles whose vertices all is contained in the provided collection
	 * find the edges(vertex-pairs) that only appears once
	 *
	 * @return a set of unique GeosetVertex pairs making up the outer edges
	 */
	public static Set<Pair<GeosetVertex, GeosetVertex>> getEdges(Collection<GeosetVertex> vertices) {
		Map<Pair<GeosetVertex, GeosetVertex>, Integer> edgeCounter = new HashMap<>();
		Set<Triangle> uniqueTriangles = new HashSet<>();
		for (GeosetVertex geosetVertex : vertices) {
			uniqueTriangles.addAll(geosetVertex.getTriangles());
		}

		for (Triangle triangle : uniqueTriangles) {
			if (vertices.containsAll(Arrays.asList(triangle.getVerts()))) {
				for (int i = 0; i < 3; i++) {
					Pair<GeosetVertex, GeosetVertex> edge = new Pair<>(triangle.get(i % 3), triangle.get((i + 1) % 3));
					if (edgeCounter.containsKey(edge)) {
						int count = edgeCounter.get(edge) + 1;
						edgeCounter.put(edge, count);
					} else {
						edgeCounter.put(edge, 1);
					}
				}
			}
		}

		Set<Pair<GeosetVertex, GeosetVertex>> edges = new HashSet<>();
		for (Map.Entry<Pair<GeosetVertex, GeosetVertex>, Integer> entry : edgeCounter.entrySet()) {
			if (entry.getValue() == 1) {
				edges.add(entry.getKey());
			}
		}
		return edges;
	}

	private ModelUtils() {
	}

	public static Mat4 processBones(RenderModel renderModel, GeosetVertex geosetVertex, Geoset geoset) {
		if (isTangentAndSkinSupported(geoset.getParentModel()) && (geoset.getVertex(0).getSkinBoneBones() != null)) {
			return processHdBones(renderModel, geosetVertex.getSkinBones());
		} else {
			return processSdBones(renderModel, geosetVertex.getBones());
		}
	}


	public static Mat4 processHdBones(RenderModel renderModel, SkinBone[] skinBones) {
		boolean processedBones = false;
		Mat4 skinBonesMatrixSumHeap = new Mat4().setZero();

		for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
			Bone bone = skinBones[boneIndex].getBone();
			if (bone == null) {
				continue;
			}
			processedBones = true;
			Mat4 worldMatrix = renderModel.getRenderNode(bone).getWorldMatrix();

			skinBonesMatrixSumHeap.addScaled(worldMatrix,skinBones[boneIndex].getWeightFraction());
		}
		if (!processedBones) {
			skinBonesMatrixSumHeap.setIdentity();
		}
		return skinBonesMatrixSumHeap;
	}

	public static Mat4 processSdBones(RenderModel renderModel, List<Bone> bones) {
		int boneCount = bones.size();
		Mat4 bonesMatrixSumHeap = new Mat4().setZero();
		if (boneCount > 0) {
			for (Bone bone : bones) {
				bonesMatrixSumHeap.add(renderModel.getRenderNode(bone).getWorldMatrix());
			}
			return bonesMatrixSumHeap.uniformScale(1f / boneCount);
		}
		return bonesMatrixSumHeap.setIdentity();
	}


	public static String sdGetMostCommonUniqueBoneName(Set<Bone> bones) {
		List<Bone> nonSharedParentBones = new ArrayList<>();
		if (!bones.isEmpty()) {
			if (bones.size() == 1) {
				return new ArrayList<>(bones).get(0).getName().replaceAll("(?i)bone_*", "");
			}
			for (Bone bone : bones) {
				Bone lp = lastParentIn(bone, bones);

				if (lp != null && !nonSharedParentBones.contains(lp)) {
					nonSharedParentBones.add(lp);
				}
			}
			List<Bone> curatedBones = nonSharedParentBones.stream()
					.filter(bone -> !bone.getName().toLowerCase().startsWith("mesh") && !bone.getName().toLowerCase().startsWith("object"))
					.collect(Collectors.toList());

			if (curatedBones.size() < 3 && curatedBones.size() != nonSharedParentBones.size()) {
				for (int i = 0; i < nonSharedParentBones.size() && curatedBones.size() < 3; i++) {
					if (!curatedBones.contains(nonSharedParentBones.get(i))) {
						curatedBones.add(nonSharedParentBones.get(i));
					}
				}
			}
			List<String> nameParts = new ArrayList<>();
			for (Bone bone : curatedBones) {
				nameParts.add(bone.getName().replaceAll("(?i)bone_*", ""));
			}
			String name = String.join(", ", nameParts);
			if(65 < name.length()){
				return name.substring(0, Math.max(65, name.indexOf(", ", 45)+2)) + "...";
			}
			return name;
		}
		return "";
	}


	public static Bone lastParentIn(Bone bone, Collection<Bone> list) {
		Bone parentBone = bone;
		int infStopper = 0;
		while (list.contains(parentBone) && parentBone != null && infStopper < 1000) {
			if (bone.getParent() instanceof Bone) {
				parentBone = (Bone) bone.getParent();
				if (list.contains(parentBone)) {
					bone = parentBone;
				}
			} else {
				return bone;
			}
			infStopper++;
		}
		return bone;
	}
}
