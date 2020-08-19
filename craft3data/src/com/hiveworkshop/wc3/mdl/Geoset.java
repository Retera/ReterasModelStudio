package com.hiveworkshop.wc3.mdl;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.etheller.warsmash.parsers.mdlx.MdlxExtent;
import com.etheller.warsmash.parsers.mdlx.MdlxGeoset;

import com.hiveworkshop.wc3.util.ModelUtils;

public class Geoset implements Named, VisibilitySource {
	ExtLog extents;
	List<GeosetVertex> vertex;
	List<Normal> normals;
	List<UVLayer> uvlayers;
	List<Triangle> triangles;
	List<Matrix> matrix;
	List<Animation> anims;
	List<String> flags;
	int materialID = 0;
	Material material;
	int selectionGroup = 0;

	EditableModel parentModel;

	GeosetAnim geosetAnim = null;

	int levelOfDetail = -1;
	String levelOfDetailName;
	List<short[]> skin;
	List<float[]> tangents;

	public Geoset() {
		vertex = new ArrayList();
		matrix = new ArrayList();
		triangles = new ArrayList();
		normals = new ArrayList();
		uvlayers = new ArrayList();
		anims = new ArrayList();
		flags = new ArrayList();
	}

	public Geoset(final MdlxGeoset geoset) {
		this();
		setExtLog(new ExtLog(geoset.extent));

		for (final MdlxExtent extent : geoset.sequenceExtents) {
			final ExtLog extents = new ExtLog(extent);
			final Animation anim = new Animation(extents);
			add(anim);
		}

		setMaterialID((int)geoset.materialId);

		float[][] uvSets = geoset.uvSets;

		final List<UVLayer> uv = new ArrayList<>();
		for (int i = 0; i < geoset.uvSets.length; i++) {
			final UVLayer layer = new UVLayer();
			uv.add(layer);
			addUVLayer(layer);
		}

		float[] vertices = geoset.vertices;
		float[] normals = geoset.normals;
		final int nVertices = vertices.length / 3;
		short[] vertexGroups = geoset.vertexGroups;

		for (int k = 0; k < nVertices; k++) {
			final int i = k * 3;
			final int j = k * 2;
			GeosetVertex gv;

			add(gv = new GeosetVertex(vertices[i], vertices[i + 1],vertices[i + 2]));

			if (k >= vertexGroups.length) {
				gv.setVertexGroup(-1);
			} else {
				gv.setVertexGroup((256 + vertexGroups[k]) % 256);
			}
			// this is an unsigned byte, the other guys java code will read as
			// signed
			if (normals.length > 0) {
				addNormal(new Normal(normals[i], normals[i + 1], normals[i + 2]));
			}

			for (int uvId = 0; uvId < uv.size(); uvId++) {
				uv.get(uvId).addTVertex(new TVertex(uvSets[uvId][j], uvSets[uvId][j + 1]));
			}
		}
		// guys I didn't code this to allow experimental
		// non-triangle faces that were suggested to exist
		// on the web (i.e. quads).
		// if you wanted to fix that, you'd want to do it below
		int[] faces = geoset.faces;

		for (int i = 0; i < faces.length; i += 3) {
			final Triangle triangle = new Triangle(
				convertPossiblyBuggedShort((short)faces[i + 0]),
				convertPossiblyBuggedShort((short)faces[i + 1]), 
				convertPossiblyBuggedShort((short)faces[i + 2]),
				this);
			add(triangle);
		}

		if (geoset.selectionFlags == 4) {
			addFlag("Unselectable");
		}

		setSelectionGroup((int)geoset.selectionGroup);
		setLevelOfDetail(geoset.lod);
		setLevelOfDetailName(geoset.lodName);

		int index = 0;
		for (final long size : geoset.matrixGroups) {
			final Matrix m = new Matrix();
			for (int i = 0; i < size; i++) {
				m.addId((int)geoset.matrixIndices[index++]);
			}
			addMatrix(m);
		}

		if (geoset.tangents != null) {
			float[] tangents = geoset.tangents;
			short[] skin = geoset.skin;

			// version 900
			this.skin = new ArrayList<short[]>();
			this.tangents = new ArrayList<float[]>();
			for (int i = 0; i < tangents.length; i += 4) {
				this.tangents.add(new float[] { tangents[i], tangents[i + 1], tangents[i + 2], tangents[i + 3] });
			}
			for (int i = 0; i < skin.length; i += 8) {
				this.skin.add(new short[] { skin[i], skin[i + 1], skin[i + 2], skin[i + 3], skin[i + 4], skin[i + 5], skin[i + 6], skin[i + 7] });
			}
		}
	}

	public MdlxGeoset toMdlx() {
		MdlxGeoset geoset = new MdlxGeoset();

		if (getExtents() != null) {
			geoset.extent = getExtents().toMdlx();
		}

		geoset.sequenceExtents = new MdlxExtent[getAnims().size()];

		for (int i = 0, l = getAnims().size(); i < l; i++) {
			geoset.sequenceExtents[i] = getAnim(i).getExtents().toMdlx();
		}


		geoset.materialId = getMaterialID();

		final int numVertices = getVertices().size();
		final int nrOfTextureVertexGroups = getUVLayers().size();

		geoset.vertices = new float[numVertices * 3];

		final boolean hasNormals = getNormals().size() > 0;
		if (hasNormals) {
			geoset.normals = new float[numVertices * 3];
		} else {
			geoset.normals = new float[0];
		}

		geoset.vertexGroups = new short[numVertices];
		geoset.uvSets = new float[nrOfTextureVertexGroups][numVertices * 2];

		for (int vId = 0; vId < numVertices; vId++) {
			final GeosetVertex vertex = getVertex(vId);
			geoset.vertices[(vId * 3) + 0] = (float) vertex.getX();
			geoset.vertices[(vId * 3) + 1] = (float) vertex.getY();
			geoset.vertices[(vId * 3) + 2] = (float) vertex.getZ();
			if (hasNormals) {
				geoset.normals[(vId * 3) + 0] = (float) vertex.getNormal().getX();
				geoset.normals[(vId * 3) + 1] = (float) vertex.getNormal().getY();
				geoset.normals[(vId * 3) + 2] = (float) vertex.getNormal().getZ();
			}
			for (int uvLayerIndex = 0; uvLayerIndex < nrOfTextureVertexGroups; uvLayerIndex++) {
				geoset.uvSets[uvLayerIndex][(vId * 2) + 0] = (float) vertex.getTVertex(uvLayerIndex).getX();
				geoset.uvSets[uvLayerIndex][(vId * 2) + 1] = (float) vertex.getTVertex(uvLayerIndex).getY();
			}
			geoset.vertexGroups[vId] = (byte) vertex.getVertexGroup();
		}

		// Again, the current implementation of my mdl code is that it only
		// handles triangle facetypes
		// (there's another note about this in the MDX -> MDL geoset code)
		geoset.faceGroups = new long[] { getTriangles().size() * 3 };
		geoset.faceTypeGroups = new long[] { 4 }; // triangles!
		geoset.faces = new int[getTriangles().size() * 3]; // triangles!

		int i = 0;
		for (final Triangle tri : getTriangles()) {
			for (int v = 0; v < /* tri.size() */3; v++) {
				geoset.faces[i++] = tri.getId(v);
			}
		}

		if (getFlags().contains("Unselectable")) {
			geoset.selectionFlags = 4;
		}

		geoset.selectionGroup = getSelectionGroup();

		int matrixIndexsSize = 0;
		for (final Matrix matrix : getMatrix()) {
			int size = matrix.size();
			if (size == -1) {
				size = 1;
			}
			matrixIndexsSize += size;
		}

		geoset.matrixGroups = new long[getMatrix().size()];
		if (matrixIndexsSize == -1) {
			matrixIndexsSize = 1;
		}

		geoset.matrixIndices = new long[matrixIndexsSize];
		i = 0;
		int groupIndex = 0;
		for (final Matrix matrix : getMatrix()) {
			for (int index = 0; index < matrix.size(); index++) {
				geoset.matrixIndices[i++] = matrix.getBoneId(index);
			}
			if (matrix.size() <= 0) {
				geoset.matrixIndices[i++] = -1;
			}
			int size = matrix.size();
			if (size == -1) {
				size = 1;
			}
			geoset.matrixGroups[groupIndex++] = size;
		}

		geoset.lod = getLevelOfDetail();
		geoset.lodName = getLevelOfDetailName();

		if ((numVertices > 0) && (getVertex(0).getSkinBones() != null)) {
			// v900
			geoset.skin = new short[8 * numVertices];
			geoset.tangents = new float[4 * numVertices];

			for (i = 0; i < numVertices; i++) {
				for (int j = 0; j < 4; j++) {
					final GeosetVertex vertex = getVertex(i);
					geoset.skin[(i * 8) + j] = vertex.getSkinBoneIndexes()[j];
					geoset.skin[(i * 8) + j + 4] = (byte) (vertex.getSkinBoneWeights()[j]);
					geoset.tangents[(i * 4) + j] = vertex.getTangent()[j];
				}
			}
		}

		return geoset;
	}

	private int convertPossiblyBuggedShort(final short x) {
		if (x < 0) {
			return x - Short.MIN_VALUE;
		}
		return x;
	}

	@Override
	public String getName() {
		return "Geoset " + (parentModel.getGeosetId(this) + 1);// parentModel.getName()
																// + "
	}

	public void addVertex(final GeosetVertex v) {
		add(v);
	}

	public void add(final GeosetVertex v) {
		vertex.add(v);
	}

	public GeosetVertex getVertex(final int vertId) {
		return vertex.get(vertId);
	}

	public int getVertexId(final GeosetVertex v) {
		// int x = 0;
		// for(int i = 0; i < m_vertex.size(); i++ )
		// {
		// if( m_vertex.get(i) == v )
		// {
		// x = i;
		// break;
		// }
		// }
		// return x;
		return vertex.indexOf(v);
	}

	public void remove(final GeosetVertex v) {
		vertex.remove(v);
	}

	public boolean containsReference(final IdObject obj) {
		// boolean does = false;
		for (int i = 0; i < vertex.size(); i++) {
			if (vertex.get(i).bones.contains(obj)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(final Triangle t) {
		return triangles.contains(t);
	}

	public boolean contains(final Vertex v) {
		return vertex.contains(v);
	}

	public int numVerteces() {
		return vertex.size();
	}

	public void addNormal(final Normal n) {
		normals.add(n);
	}

	public Normal getNormal(final int vertId) {
		return normals.get(vertId);
	}

	public int numNormals() {
		return normals.size();
	}

	public void addUVLayer(final UVLayer v) {
		uvlayers.add(v);
	}

	public UVLayer getUVLayer(final int id) {
		return uvlayers.get(id);
	}

	public int numUVLayers() {
		return uvlayers.size();
	}

	public void setTriangles(final List<Triangle> list) {
		triangles = list;
	}

	public void addTriangle(final Triangle p) {
		// Left for compat
		add(p);
	}

	public void add(final Triangle p) {
		if (!triangles.contains(p)) {
			triangles.add(p);
		} else {
			System.out.println("2x triangles");
		}
	}

	public Triangle getTriangle(final int triId) {
		return triangles.get(triId);
	}

	public Triangle[] getTrianglesAll() {
		return (Triangle[]) triangles.toArray();
	}

	/**
	 * Returns all vertices that directly inherit motion from the specified Bone, or
	 * an empty list if no vertices reference the object.
	 *
	 * @param parent
	 * @return
	 */
	public List<GeosetVertex> getChildrenOf(final Bone parent) {
		final List<GeosetVertex> children = new ArrayList<>();
		for (final GeosetVertex gv : vertex) {
			if (gv.bones.contains(parent)) {
				children.add(gv);
			}
		}
		return children;
	}

	public int numTriangles() {
		return triangles.size();
	}

	public void removeTriangle(final Triangle t) {
		triangles.remove(t);
	}

	public void addMatrix(final Matrix v) {
		matrix.add(v);
	}

	public Matrix getMatrix(final int vertId) {
		if ((vertId < 0) && (vertId >= -128)) {
			return getMatrix(256 + vertId);
		}
		if (vertId >= matrix.size()) {
			return null;
		}
		return matrix.get(vertId);
	}

	public int numMatrices() {
		return matrix.size();
	}

	public void setMaterialId(final int i) {
		materialID = i;
	}

	public void setMaterial(final Material m) {
		material = m;
	}

	public Material getMaterial() {
		return material;
	}

	public void setExtLog(final ExtLog e) {
		extents = e;
	}

	public ExtLog getExtLog() {
		return extents;
	}

	public void add(final Animation a) {
		anims.add(a);
	}

	public Animation getAnim(final int id) {
		return anims.get(id);
	}

	public int numAnims() {
		return anims.size();
	}

	public void addFlag(final String a) {
		flags.add(a);
	}

	public String getFlag(final int id) {
		return flags.get(id);
	}

	public int numFlags() {
		return flags.size();
	}

	public List<TVertex> getTVertecesInArea(final Rectangle2D.Double area, final int layerId) {
		final List<TVertex> temp = new ArrayList<>();
		for (int i = 0; i < vertex.size(); i++) {
			final TVertex ver = vertex.get(i).getTVertex(layerId);
			// Point2D.Double p = new
			// Point(ver.getCoords(dim1),ver.getCoords(dim2))
			if (area.contains(ver.getX(), ver.getY())) {
				temp.add(ver);
			}
		}
		return temp;
	}

	public List<Vertex> getVertecesInArea(final Rectangle2D.Double area, final byte dim1, final byte dim2) {
		final List<Vertex> temp = new ArrayList<>();
		for (final Vertex ver : vertex) {
			// Point2D.Double p = new
			// Point(ver.getCoords(dim1),ver.getCoords(dim2))
			if (area.contains(ver.getCoord(dim1), ver.getCoord(dim2))) {
				temp.add(ver);
			}
		}
		return temp;
	}

	public void updateToObjects(final EditableModel mdlr) {
		// upload the temporary UVLayer and Matrix objects into the vertices
		// themselves
		final int sz = numVerteces();
		for (final Matrix m : matrix) {
			m.updateBones(mdlr);
		}
		for (int i = 0; i < sz; i++) {
			final GeosetVertex gv = vertex.get(i);
			if ((ModelUtils.isTangentAndSkinSupported(mdlr.getFormatVersion())) && (tangents != null)) {
				gv.initV900();
				for (int j = 0; j < 4; j++) {
					short boneLookupId = skin.get(i)[j];
					if (boneLookupId < 0) {
						boneLookupId += 256;
					}
					short boneWeight = skin.get(i)[j + 4];
					if (boneWeight < 0) {
						boneWeight += 256;
					}
					Bone bone;
					if ((boneLookupId >= mdlr.getIdObjectsSize()) || (boneLookupId < 0)) {
						bone = null;
					} else {
						final IdObject idObject = mdlr.getIdObject(boneLookupId);
						if (idObject instanceof Bone) {
							bone = (Bone) idObject;
						} else {
							bone = null;
						}
					}
					gv.getSkinBones()[j] = bone;
					gv.getSkinBoneWeights()[j] = boneWeight;
					gv.getTangent()[j] = tangents.get(i)[j];
				}
			}
			gv.clearTVerts();
			final int szuv = uvlayers.size();
			for (int l = 0; l < szuv; l++) {
				try {
					gv.addTVertex(uvlayers.get(l).getTVertex(i));
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(null,
							"Error: Length of TVertices and Vertices chunk differ (Or some other unknown error has occurred)!");
				}
			}
			Matrix mx;
			if ((gv.getVertexGroup() == -1) && (ModelUtils.isTangentAndSkinSupported(mdlr.getFormatVersion()))) {
				mx = null;
			} else {
				mx = getMatrix(gv.getVertexGroup());
			}
			if (mx != null) {
				final int szmx = mx.size();
				gv.clearBoneAttachments();
				for (int m = 0; m < szmx; m++) {
					final int boneId = mx.getBoneId(m);
					if ((boneId >= 0) && (boneId < mdlr.getIdObjectsSize())) {
						gv.addBoneAttachment((Bone) mdlr.getIdObject(boneId));
					}
				}
			}
			if ((normals != null) && (normals.size() > 0)) {
				gv.setNormal(normals.get(i));
			}
			for (final Triangle t : triangles) {
				if (t.containsRef(gv)) {
					gv.triangles.add(t);
				}
				t.geoset = this;
			}
			gv.geoset = this;

			// gv.addBoneAttachment(null);//Why was this here?
		}
		try {
			material = mdlr.getMaterial(materialID);
		} catch (final ArrayIndexOutOfBoundsException e) {
			JOptionPane.showMessageDialog(null, "Error: Material index out of bounds for geoset!");
		}
		parentModel = mdlr;
	}

	public void applyMatricesToVertices(final EditableModel mdlr) {
		final int sz = numVerteces();
		for (int i = 0; i < sz; i++) {
			final GeosetVertex gv = vertex.get(i);
			gv.clearBoneAttachments();
			final Matrix mx = getMatrix(gv.getVertexGroup());
			if (((gv.getVertexGroup() == -1) || (mx == null))) {
				if (!ModelUtils.isTangentAndSkinSupported(mdlr.getFormatVersion())) {
					throw new IllegalStateException(
							"You have empty vertex groupings but FormatVersion is 800. Did you load HD mesh into an SD model?");
				}
			} else {
				mx.updateIds(mdlr);
				final int szmx = mx.size();
				for (int m = 0; m < szmx; m++) {
					gv.addBoneAttachment((Bone) mdlr.getIdObject(mx.getBoneId(m)));
				}
			}
		}
	}

	public void applyVerticesToMatrices(final EditableModel mdlr) {
		matrix.clear();
		for (int i = 0; i < vertex.size(); i++) {
			Matrix newTemp = new Matrix(vertex.get(i).bones);
			boolean newMatrix = true;
			for (int m = 0; (m < matrix.size()) && newMatrix; m++) {
				if (newTemp.equals(matrix.get(m))) {
					newTemp = matrix.get(m);
					newMatrix = false;
				}
			}
			if (newMatrix) {
				matrix.add(newTemp);
				newTemp.updateIds(mdlr);
			}
			vertex.get(i).VertexGroup = matrix.indexOf(newTemp);
			vertex.get(i).setMatrix(newTemp);
		}
	}

	public void purifyFaces() {
		for (int i = triangles.size() - 1; i >= 0; i--) {
			final Triangle tri = triangles.get(i);
			for (int ix = 0; ix < triangles.size(); ix++) {
				final Triangle trix = triangles.get(ix);
				if (trix != tri) {
					if (trix.equalRefsNoIds(tri))// Changed this from
													// "sameVerts" -- this means
													// that
													// triangles with the same
													// vertices but in a
													// different order will no
													// longer be purged
													// automatically.
					{
						triangles.remove(tri);
						break;
					}
				}
			}
		}
	}

	public boolean isEmpty() {
		return vertex.size() <= 0;
	}

	public void doSavePrep(final EditableModel mdlr) {
		purifyFaces();

		// Normals cleared here, in case that becomes a problem later.
		normals.clear();
		// UV Layers cleared here
		uvlayers.clear();
		int bigNum = 0;
		int littleNum = -1;
		for (int i = 0; i < vertex.size(); i++) {
			final int temp = vertex.get(i).tverts.size();
			if (temp > bigNum) {
				bigNum = temp;
			}
			if ((littleNum == -1) || (temp < littleNum)) {
				littleNum = temp;
			}
		}
		if (littleNum != bigNum) {
			JOptionPane.showMessageDialog(null,
					"Error: Attempting to save a Geoset with Verteces that have differing numbers of TVertices! Empty TVertices will be autogenerated.");
		}
		for (int i = 0; i < bigNum; i++) {
			uvlayers.add(new UVLayer());
		}
		for (int i = 0; i < vertex.size(); i++) {
			if (vertex.get(i).getNormal() != null) {
				normals.add(vertex.get(i).getNormal());
			}
			for (int uv = 0; uv < bigNum; uv++) {
				final TVertex temp = vertex.get(i).getTVertex(uv);
				if (temp != null) {
					uvlayers.get(uv).addTVertex(temp);
				} else {
					uvlayers.get(uv).addTVertex(new TVertex(0, 0));
				}
			}
		}
		// Clearing matrix list
		matrix.clear();
		for (int i = 0; i < vertex.size(); i++) {
			final GeosetVertex geosetVertex = vertex.get(i);
			if (geosetVertex.getSkinBones() != null) {
				if (matrix.isEmpty()) {
					final List<Bone> bones = mdlr.sortedIdObjects(Bone.class);
					for (int j = 0; (j < bones.size()) && (j < 256); j++) {
						final List<Bone> singleBoneList = new ArrayList<Bone>();
						singleBoneList.add(bones.get(j));
						final Matrix e = new Matrix(singleBoneList);
						e.updateIds(mdlr);
						matrix.add(e);
					}
				}
				int skinIndex = 0;
				for (final Bone bone : geosetVertex.getSkinBones()) {
					if (bone != null) {
						final List<Bone> singleBoneList = new ArrayList<Bone>();
						singleBoneList.add(bone);
						final Matrix newTemp = new Matrix(singleBoneList);
						int index = -1;
						for (int m = 0; (m < matrix.size()) && (index == -1); m++) {
							if (newTemp.equals(matrix.get(m))) {
								index = m;
							}
						}
						geosetVertex.getSkinBoneIndexes()[skinIndex++] = (byte) index;
					}
				}
				geosetVertex.VertexGroup = -1;
			} else {
				Matrix newTemp = new Matrix(vertex.get(i).bones);
				boolean newMatrix = true;
				for (int m = 0; (m < matrix.size()) && newMatrix; m++) {
					if (newTemp.equals(matrix.get(m))) {
						newTemp = matrix.get(m);
						newMatrix = false;
					}
				}
				if (newMatrix) {
					matrix.add(newTemp);
					newTemp.updateIds(mdlr);
				}
				vertex.get(i).VertexGroup = matrix.indexOf(newTemp);
				vertex.get(i).setMatrix(newTemp);
			}
		}
		for (int i = 0; i < triangles.size(); i++) {
			triangles.get(i).updateVertexIds(this);
		}
		int boneRefCount = 0;
		for (int i = 0; i < matrix.size(); i++) {
			boneRefCount += matrix.get(i).bones.size();
		}
		for (int i = 0; i < matrix.size(); i++) {
			matrix.get(i).updateIds(mdlr);
		}
	}

	public GeosetAnim forceGetGeosetAnim() {
		if (geosetAnim == null) {
			geosetAnim = new GeosetAnim(this);
			parentModel.add(geosetAnim);
		}
		return geosetAnim;
	}

	@Override
	public void setVisibilityFlag(final AnimFlag a) {
		if (a != null) {
			forceGetGeosetAnim().setVisibilityFlag(a);
		}
	}

	@Override
	public AnimFlag getVisibilityFlag() {
		if (geosetAnim != null) {
			return geosetAnim.getVisibilityFlag();
		}
		return null;
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public List<GeosetVertex> getVertices() {
		return vertex;
	}

	public void setVertex(final List<GeosetVertex> vertex) {
		this.vertex = vertex;
	}

	public List<Normal> getNormals() {
		return normals;
	}

	public void setNormals(final List<Normal> normals) {
		this.normals = normals;
	}

	public List<UVLayer> getUVLayers() {
		return uvlayers;
	}

	public void setUvlayers(final List<UVLayer> uvlayers) {
		this.uvlayers = uvlayers;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public void setTriangle(final List<Triangle> triangle) {
		this.triangles = triangle;
	}

	public List<Matrix> getMatrix() {
		return matrix;
	}

	public void setMatrix(final List<Matrix> matrix) {
		this.matrix = matrix;
	}

	public List<Animation> getAnims() {
		return anims;
	}

	public void setAnims(final List<Animation> anims) {
		this.anims = anims;
	}

	public List<String> getFlags() {
		return flags;
	}

	public void setFlags(final List<String> flags) {
		this.flags = flags;
	}

	public int getMaterialID() {
		return materialID;
	}

	public void setMaterialID(final int materialID) {
		this.materialID = materialID;
	}

	public int getSelectionGroup() {
		return selectionGroup;
	}

	public void setSelectionGroup(final int selectionGroup) {
		this.selectionGroup = selectionGroup;
	}

	public void setLevelOfDetail(final int levelOfDetail) {
		this.levelOfDetail = levelOfDetail;
	}

	public void setLevelOfDetailName(final String levelOfDetailName) {
		this.levelOfDetailName = levelOfDetailName;
	}

	public int getLevelOfDetail() {
		return levelOfDetail;
	}

	public String getLevelOfDetailName() {
		return levelOfDetailName;
	}

	public EditableModel getParentModel() {
		return parentModel;
	}

	public void setParentModel(final EditableModel parentModel) {
		this.parentModel = parentModel;
	}

	public GeosetAnim getGeosetAnim() {
		return geosetAnim;
	}

	public void setGeosetAnim(final GeosetAnim geosetAnim) {
		this.geosetAnim = geosetAnim;
	}

	public void remove(final Triangle tri) {
		this.triangles.remove(tri);
	}

	public ExtLog calculateExtent() {
		double maximumDistanceFromCenter = 0;
		double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
		for (final GeosetVertex geosetVertex : vertex) {
			if (geosetVertex.x > maxX) {
				maxX = geosetVertex.x;
			}
			if (geosetVertex.y > maxY) {
				maxY = geosetVertex.y;
			}
			if (geosetVertex.z > maxZ) {
				maxZ = geosetVertex.z;
			}
			if (geosetVertex.x < minX) {
				minX = geosetVertex.x;
			}
			if (geosetVertex.y < minY) {
				minY = geosetVertex.y;
			}
			if (geosetVertex.z < minZ) {
				minZ = geosetVertex.z;
			}
			final double distanceFromCenter = Math.sqrt((geosetVertex.x * geosetVertex.x)
					+ (geosetVertex.y * geosetVertex.y) + (geosetVertex.z * geosetVertex.z));
			if (distanceFromCenter > maximumDistanceFromCenter) {
				maximumDistanceFromCenter = distanceFromCenter;
			}
		}
		return new ExtLog(new Vertex(minX, minY, minZ), new Vertex(maxX, maxY, maxZ), maximumDistanceFromCenter);
	}
}
