package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ModelUtils {
	public static String getPortrait(String filepath) {
		String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait" + filepath.substring(filepath.lastIndexOf('.'));
		return portrait;
	}

	public static Mesh createPlane(byte planeDimension, boolean outward, double planeHeight, Vec2 min, Vec2 max, int numberOfSegments) {
		return createPlane(planeDimension, outward, planeHeight, min, max, numberOfSegments, numberOfSegments);
	}

	public static Mesh createPlane(byte planeDimension, boolean outward, double planeHeight, Vec2 min, Vec2 max, int numberOfSegmentsX, int numberOfSegmentsY) {
		byte[] dimensions = getBytesDimensions(planeDimension);

		byte firstDimension = dimensions[0];
		byte secondDimension = dimensions[1];

		Vec3 normal = new Vec3(0, 0, 0);
		normal.setCoord(planeDimension, outward ? 1 : -1);
		return createPlane(firstDimension, secondDimension, normal, planeHeight, min, max, numberOfSegmentsX, numberOfSegmentsY);
	}

	public static Mesh createPlane(byte firstDimension, byte secondDimension, Vec3 facingVector, double planeHeight, Vec2 p1, Vec2 p2, int numberOfSegmentsX, int numberOfSegmentsY) {
		byte planeDimension = CoordinateSystem.Util.getUnusedXYZ(firstDimension, secondDimension);

		List<GeosetVertex> vertices = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();

		Vec2 segSizes = Vec2.getDif(p2, p1).div(new Vec2(numberOfSegmentsX, numberOfSegmentsY));

		Vec2 uvSegW = new Vec2(1.0 / numberOfSegmentsX, 1.0 / numberOfSegmentsY);
		GeosetVertex[][] vertexGrid = new GeosetVertex[numberOfSegmentsY + 1][numberOfSegmentsX + 1];

		Vec2 xy = new Vec2();
		for (int y = 0; y < (numberOfSegmentsY + 1); y++) {
			for (int x = 0; x < (numberOfSegmentsX + 1); x++) {
				xy.set(x, y);

				Vec3 normal = new Vec3(facingVector.x, facingVector.y, facingVector.z);
				GeosetVertex vertex = new GeosetVertex(0, 0, 0, normal);

				vertex.setCoord(planeDimension, planeHeight);
				vertex.setCoords(firstDimension, secondDimension, Vec2.getProd(segSizes, xy).add(p1));

				vertex.addTVertex(Vec2.getProd(uvSegW, xy));

				vertexGrid[y][x] = vertex;
				vertices.add(vertex);
			}
		}

		for (int y = 0; y < (numberOfSegmentsY); y++) {
			for (int x = 0; x < (numberOfSegmentsX); x++) {
				GeosetVertex upperL = vertexGrid[y][x];
				GeosetVertex upperR = vertexGrid[y][x + 1];
				GeosetVertex lowerL = vertexGrid[y + 1][x];
				GeosetVertex lowerR = vertexGrid[y + 1][x + 1];

				Triangle firstFace = new Triangle(upperR, upperL, lowerL);
				triangles.add(firstFace);
				Triangle secondFace = new Triangle(upperR, lowerL, lowerR);
				triangles.add(secondFace);

				boolean flip = firstFace.getNormal().dot(facingVector) < 0;
				if (flip) {
					firstFace.flip(false);
					secondFace.flip(false);
				}
			}
		}
		return new Mesh(vertices, triangles);
	}

	public static void createBox(EditableModel model, Vec3 min, Vec3 max, int segments) {
		Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		Mesh box = getBoxMesh(min, max, segments, segments, segments);


		for (GeosetVertex vertex : box.vertices) {
			vertex.setGeoset(geoset);
			geoset.add(vertex);
		}
		for (Triangle triangle : box.triangles) {
			geoset.add(triangle);
			triangle.setGeoset(geoset);
//			for (GeosetVertex vertex : triangle.getVerts()) {
//				vertex.addTriangle(triangle);
//			}
		}

		geoset.setName("Box");

		model.add(geoset);
	}

	/**
	 * Creates a box ready to add to the dataGeoset, but does not actually modify
	 * the geoset itself
	 */
	public static Mesh createBox(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs, Geoset dataGeoset) {
		Mesh box = getBoxMesh(max, min, lengthSegs, widthSegs, heightSegs);
		for (GeosetVertex vertex : box.getVertices()) {
			vertex.setGeoset(dataGeoset);
		}
		for (Triangle triangle : box.getTriangles()) {
			triangle.setGeoset(dataGeoset);
//			for (GeosetVertex vertex : triangle.getVerts()) {
//				vertex.addTriangle(triangle);
//			}
		}
		return box;
	}

	private static Mesh getBoxMesh(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs) {
		Mesh box = new Mesh(new ArrayList<>(), new ArrayList<>());
		for (byte side = (byte) 0; side < 2; side++) {
			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
				Vec3 sideMaxima = switch (side) {
					case 0 -> min;
					case 1 -> max;
					default -> throw new IllegalStateException();
				};
				double coordinateAtSide = sideMaxima.getCoord(dimension);


				byte[] dimensions = getBytesDimensions(dimension);

				int segsX = dimensions[0] == 0 ? lengthSegs : widthSegs;
				int segsY = dimensions[1] == 2 ? heightSegs : widthSegs;

				Vec2 minP = min.getProjected(dimensions[0], dimensions[1]);
				Vec2 maxP = max.getProjected(dimensions[0], dimensions[1]);

				Mesh sidedPlane = createPlane(dimension, side != 1, coordinateAtSide, minP, maxP, segsX, segsY);
				box.vertices.addAll(sidedPlane.vertices);
				box.triangles.addAll(sidedPlane.triangles);
			}
		}
		return box;
	}

	private static byte[] getBytesDimensions(byte dimension) {
		return switch (dimension) {
			case 0 -> new byte[] {1, 2};
			case 1 -> new byte[] {0, 2};
			case 2 -> new byte[] {0, 1};
			default -> throw new IllegalStateException();
		};
	}

	public static void createGroundPlane(EditableModel model, Vec3 max, Vec3 min, int segments) {
		Geoset geoset = new Geoset();
		geoset.setMaterial(new Material(new Layer("None", new Bitmap("textures\\white.blp"))));

		Vec2 minP = min.getProjected((byte) 0, (byte) 1);
		Vec2 maxP = max.getProjected((byte) 0, (byte) 1);

		Mesh sidedPlane = createPlane((byte) 2, true, 0, minP, maxP, segments);
		for (GeosetVertex vertex : sidedPlane.vertices) {
			vertex.setGeoset(geoset);
			geoset.add(vertex);
		}
		for (Triangle triangle : sidedPlane.triangles) {
			triangle.setGeoset(geoset);
			geoset.add(triangle);
//			for (GeosetVertex vertex : triangle.getVerts()) {
//				vertex.addTriangle(triangle);
//			}
		}
		model.add(geoset);
	}

	public static float[] flipRGBtoBGR(float[] rgb) {
		float[] bgr = new float[3];
		for (int i = 0; i < 3; i++) {
			bgr[i] = rgb[2 - i];
		}
		return bgr;
	}

	public static boolean isLevelOfDetailSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isShaderStringSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isTangentAndSkinSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isBindPoseSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isEmissiveLayerSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static boolean isFresnelColorLayerSupported(int formatVersion) {
		return formatVersion == 1000;
	}

	public static boolean isCornSupported(int formatVersion) {
		return (formatVersion == 900) || (formatVersion == 1000);
	}

	public static final class Mesh {
		private final List<GeosetVertex> vertices;
		private final List<Triangle> triangles;

		private Mesh(List<GeosetVertex> vertices, List<Triangle> triangles) {
			this.vertices = vertices;
			this.triangles = triangles;
		}

		public List<GeosetVertex> getVertices() {
			return vertices;
		}

		public List<Triangle> getTriangles() {
			return triangles;
		}

	}

	private ModelUtils() {
	}

	public static Mat4 processHdBones(RenderModel renderModel, Bone[] skinBones, short[] skinBoneWeights) {
		boolean processedBones = false;
		Mat4 skinBonesMatrixSumHeap = new Mat4().setZero();

		for (int boneIndex = 0; boneIndex < 4; boneIndex++) {
			Bone skinBone = skinBones[boneIndex];
			if (skinBone == null) {
				continue;
			}
			processedBones = true;
			Mat4 worldMatrix = renderModel.getRenderNode(skinBone).getWorldMatrix();

			float skinBoneWeight = skinBoneWeights[boneIndex] / 255f;
			skinBonesMatrixSumHeap.add(worldMatrix.getUniformlyScaled(skinBoneWeight));
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
}
