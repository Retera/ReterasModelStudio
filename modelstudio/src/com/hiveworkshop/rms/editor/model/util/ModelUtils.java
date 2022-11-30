package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.util.*;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public final class ModelUtils {
	public static String getPortrait(String filepath) {
		int endIndex = filepath.contains(".") ? filepath.lastIndexOf('.') : filepath.length();
		String portrait = filepath.substring(0, endIndex) + "_portrait" + filepath.substring(endIndex);
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
		byte planeDimension = getUnusedXYZ(firstDimension, secondDimension);

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

	public static Mesh createPlane(Mat4 transMat, Vec3 facingVector, double planeHeight, Vec2 p1, Vec2 p2, int numberOfSegmentsX, int numberOfSegmentsY) {
//		byte planeDimension = getUnusedXYZ(firstDimension, secondDimension);

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
				GeosetVertex vertex = new GeosetVertex(planeHeight, x * segSizes.x + p1.x, y * segSizes.y + p1.y, normal);
//				vertex.transform(transMat);

//				vertex.setCoord(planeDimension, planeHeight);
//				vertex.setCoords(firstDimension, secondDimension, Vec2.getProd(segSizes, xy).add(p1));

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

	public static Mesh createPlane2(Mat4 transMat, Vec3 facingVector, double planeHeight, Vec2 p1, Vec2 p2, int subDivX, int subDivY) {
//		byte planeDimension = getUnusedXYZ(firstDimension, secondDimension);

		List<GeosetVertex> vertices = new ArrayList<>();


		GeosetVertex[][] vertexGrid = getVertexGrid(subDivX, subDivY);
		for (int y = 0; y < subDivY + 1; y++) {
			vertices.addAll(Arrays.asList(vertexGrid[y]));
		}

		List<Triangle> triangles = getTriangles(facingVector, vertexGrid);
		return new Mesh(vertices, triangles);
	}

	private static List<Triangle> getTriangles(Vec3 facingVector, GeosetVertex[][] vertexGrid) {
		List<Triangle> triangles = new ArrayList<>();
		for (int y = 0; y < vertexGrid.length; y++) {
			for (int x = 0; x < vertexGrid[0].length; x++) {
				GeosetVertex upperL = vertexGrid[y][x];
				GeosetVertex upperR = vertexGrid[y][x + 1];
				GeosetVertex lowerL = vertexGrid[y + 1][x];
				GeosetVertex lowerR = vertexGrid[y + 1][x + 1];

				Triangle firstFace = new Triangle(upperL, lowerL, upperR);
				triangles.add(firstFace);
				Triangle secondFace = new Triangle(lowerL, lowerR, upperR);
				triangles.add(secondFace);

				boolean flip = firstFace.getNormal().dot(facingVector) < 0;
				if (flip) {
					firstFace.flip(false);
					secondFace.flip(false);
				}
			}
		}
		return triangles;
	}

	public static GeosetVertex[][] getVertexGrid(int subDivX, int subDivY) {
		GeosetVertex[][] vertexGrid = new GeosetVertex[subDivY + 1][subDivX + 1];

		for (int y = 0; y < (subDivY + 1); y++) {
			for (int x = 0; x < (subDivX + 1); x++) {
				GeosetVertex vertex = new GeosetVertex(x / (float) subDivX, y / (float) subDivY, 0, new Vec3(0, 0, 1));
				vertex.addTVertex(new Vec2(x / (float) subDivX, y / (float) subDivY));
				vertexGrid[y][x] = vertex;
			}
		}
		return vertexGrid;
	}


	public static Mesh createPlane3(int subDivX, int subDivY) {
		Set<Triangle> triangles = new HashSet<>();
		List<GeosetVertex> vertices = new ArrayList<>();


		GeosetVertex[][] vertexGrid = getVertexGrid2(subDivX, subDivY);
		for (int y = 0; y < subDivY + 1; y++) {
			vertices.addAll(Arrays.asList(vertexGrid[y]));
		}
		for (GeosetVertex vertex : vertices) {
			triangles.addAll(vertex.getTriangles());
		}

		return new Mesh(vertices, new ArrayList<>(triangles));
	}

	public static GeosetVertex[][] getVertexGrid2(int subDivX, int subDivY) {
		GeosetVertex[][] vertexGrid = new GeosetVertex[subDivY + 1][subDivX + 1];

		for (int y = 0; y < (subDivY + 1); y++) {
			for (int x = 0; x < (subDivX + 1); x++) {
				GeosetVertex vertex = new GeosetVertex(x / (float) subDivX, y / (float) subDivY, 0, new Vec3(Vec3.Z_AXIS));
				vertex.addTVertex(new Vec2(x / (float) subDivX, y / (float) subDivY));
				vertexGrid[y][x] = vertex;
			}
		}

		for (int y = 0; y < subDivY; y++) {
			for (int x = 0; x < subDivX; x++) {
				GeosetVertex upperL = vertexGrid[y][x];
				GeosetVertex upperR = vertexGrid[y][x + 1];
				GeosetVertex lowerL = vertexGrid[y + 1][x];
				GeosetVertex lowerR = vertexGrid[y + 1][x + 1];

				Triangle firstFace = new Triangle(upperL, lowerL, upperR);
				Triangle secondFace = new Triangle(lowerL, lowerR, upperR);

//				float dot = firstFace.getNormal().dot(Vec3.Z_AXIS);
//				boolean flip = dot < 0;
//				System.out.println("flip face? " +  flip + ", (dot: " + dot + ", normal: " + firstFace.getNormal() + ")");
//				if (flip) {
//					firstFace.flip(false);
//					System.out.println("(new normal: " + firstFace.getNormal() + ")");
//					secondFace.flip(false);
//				}
			}
		}

		return vertexGrid;
	}

	public static byte getUnusedXYZ(byte portFirstXYZ, byte portSecondXYZ) {
		if (portFirstXYZ < 0) {
			portFirstXYZ = (byte) (-portFirstXYZ - 1);
		}
		if (portSecondXYZ < 0) {
			portSecondXYZ = (byte) (-portSecondXYZ - 1);
		}
		return (byte) (3 - portFirstXYZ - portSecondXYZ);
	}

	/**
	 * Creates a box ready to add to the dataGeoset, but does not actually modify the geoset itself
	 */
	public static Mesh createBox(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs, Geoset dataGeoset) {
		Mesh box = getBoxMesh(max, min, lengthSegs, widthSegs, heightSegs);
		for (GeosetVertex vertex : box.getVertices()) {
			vertex.setGeoset(dataGeoset);
		}
		for (Triangle triangle : box.getTriangles()) {
			triangle.setGeoset(dataGeoset);
		}
		return box;
	}

	public static Mesh createBox2(int lengthSegs, int widthSegs, int heightSegs, Geoset dataGeoset) {
		Mesh box = getBoxMesh2(lengthSegs, widthSegs, heightSegs);
		for (GeosetVertex vertex : box.getVertices()) {
			vertex.setGeoset(dataGeoset);
		}
		for (Triangle triangle : box.getTriangles()) {
			triangle.setGeoset(dataGeoset);
		}
		return box;
	}

	public static Mesh getBoxMesh(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs) {
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
				box.addVertices(sidedPlane.getVertices());
				box.addTriangles(sidedPlane.getTriangles());
			}
		}
		return box;
	}


	public static Mesh getBoxMesh2(Vec3 max, Vec3 min, int lengthSegs, int widthSegs, int heightSegs) {
		Mesh boxMesh2 = getBoxMesh2(lengthSegs, widthSegs, heightSegs);
		Vec3 size = new Vec3(max).sub(min);
//		Vec3 center = new Vec3(max).add(min).scale(.5f);
		for(GeosetVertex vertex : boxMesh2.getVertices()){
//			vertex.scaleCentered(center, size);
//			vertex.scaleCentered(Vec3.ZERO, size);
			vertex.multiply(size).add(min);
		}
		return boxMesh2;
	}

	public static Mesh getBoxMesh2(int xSegs, int ySegs, int zSegs) {
		Mesh box = new Mesh(new ArrayList<>(), new ArrayList<>());

		Vec3 spinPoint = new Vec3(0.5, 0.5, 0.5);
		Quat rot = new Quat();

		Mesh[] sides = new Mesh[] {
				createPlane3(xSegs, ySegs), createPlane3(xSegs, ySegs),
				createPlane3(xSegs, zSegs), createPlane3(xSegs, zSegs),
				createPlane3(ySegs, zSegs), createPlane3(ySegs, zSegs)
		};

		rotatePlane(sides[0], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI)));

		rotatePlane(sides[2], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI / 2.0)));
		rotatePlane(sides[3], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (-Math.PI / 2.0)));

		rotatePlane(sides[4], spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (Math.PI / 2.0)));
		rotatePlane(sides[5], spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (-Math.PI / 2.0)));

//		Mesh[] xySides = new Mesh[]{createPlane3(xSegs, ySegs), createPlane3(xSegs, ySegs)};
//		Mesh[] xzSides = new Mesh[]{createPlane3(xSegs, zSegs), createPlane3(xSegs, zSegs)};
//		Mesh[] yzSides = new Mesh[]{createPlane3(ySegs, zSegs), createPlane3(ySegs, zSegs)};
//
//		rotatePlane(xySides[0], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI)));
//
//		rotatePlane(xzSides[0], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (Math.PI/2.0)));
//		rotatePlane(xzSides[1], spinPoint, rot.setFromAxisAngle(Vec3.Y_AXIS, (float) (-Math.PI/2.0)));
//
//		rotatePlane(yzSides[0], spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (Math.PI/2.0)));
//		rotatePlane(yzSides[1], spinPoint, rot.setFromAxisAngle(Vec3.X_AXIS, (float) (-Math.PI/2.0)));


//		for (byte side = (byte) 0; side < 2; side++) {
//			for (byte dimension = (byte) 0; dimension < 3; dimension++) {
//				Vec3 sideMaxima = switch (side) {
//					case 0 -> min;
//					case 1 -> max;
//					default -> throw new IllegalStateException();
//				};
//				double coordinateAtSide = sideMaxima.getCoord(dimension);
//
//
//				byte[] dimensions = getBytesDimensions(dimension);
//
//				int segsX = dimensions[0] == 0 ? xSegs : ySegs;
//				int segsY = dimensions[1] == 2 ? zSegs : ySegs;
//
//				Vec2 minP = min.getProjected(dimensions[0], dimensions[1]);
//				Vec2 maxP = max.getProjected(dimensions[0], dimensions[1]);
//
//				Mesh sidedPlane = createPlane(dimension, side != 1, coordinateAtSide, minP, maxP, segsX, segsY);
//				box.addVertices(sidedPlane.getVertices());
//				box.addTriangles(sidedPlane.getTriangles());
//			}
//		}
		for (Mesh side : sides) {
			box.addVertices(side.getVertices());
			box.addTriangles(side.getTriangles());
		}
		return box;
	}

	private static Mesh rotatePlane(Mesh mesh, Vec3 spinPoint, Quat rot) {
		for (GeosetVertex vertex : mesh.getVertices()) {
			vertex.rotate(spinPoint, rot);
			vertex.getNormal().transform(rot);
		}
		return mesh;
	}

	private static Mesh flipPlane(Mesh mesh) {
		for (Triangle triangle : mesh.getTriangles()) {
			triangle.flip(true);
		}
		return mesh;
	}

	private static byte[] getBytesDimensions(byte dimension) {
		return switch (dimension) {
			case 0 -> new byte[] {1, 2};
			case 1 -> new byte[] {0, 2};
			case 2 -> new byte[] {0, 1};
			default -> throw new IllegalStateException();
		};
	}

	public static Material getWhiteMaterial(EditableModel model) {
		Material material = new Material(new Layer(new Bitmap("Textures\\White.blp")));
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
