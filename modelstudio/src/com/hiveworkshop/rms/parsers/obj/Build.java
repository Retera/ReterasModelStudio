package com.hiveworkshop.rms.parsers.obj;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.UVLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.TargaReader;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.owens.oobjloader.builder.Face;
import com.owens.oobjloader.builder.FaceVertex;
import com.owens.oobjloader.builder.ReflectivityTransmiss;
import com.owens.oobjloader.builder.TrollyLoadbar;
import com.owens.oobjloader.builder.VertexGeometric;
import com.owens.oobjloader.builder.VertexNormal;
import com.owens.oobjloader.builder.VertexTexture;
import com.owens.oobjloader.parser.BuilderInterface;

import de.wc3data.image.BlpFile;

public class Build implements BuilderInterface {

	private final Logger log = Logger.getLogger(Build.class.getName());

	public String objFilename = null;
	// these accumulate each type of vertex as they are parsed, so they can then
	// be referenced via index.
	public ArrayList<VertexGeometric> verticesG = new ArrayList<>();
	public ArrayList<VertexTexture> verticesT = new ArrayList<>();
	public ArrayList<VertexNormal> verticesN = new ArrayList<>();
	// we use this map to consolidate redundant face vertices. Since a face is
	// defined as a list of index
	// triplets, each index referring to a vertex within ONE of the three
	// arraylists verticesG, verticesT
	// or verticesN, two faces might end up specifying the same combination.
	// Clearly (@TODO: really?) this
	// combination should be shared between both faces.
	HashMap<String, FaceVertex> faceVerticeMap = new HashMap<>();
	// Each face vertex as it is parsed, minus the redundant face vertices.
	// @TODO: Not used anywhere yet, maybe get rid of this.
	public ArrayList<FaceVertex> faceVerticeList = new ArrayList<>();
	public ArrayList<Face> faces = new ArrayList<>();
	public HashMap<Integer, ArrayList<Face>> smoothingGroups = new HashMap<>();
	private int currentSmoothingGroupNumber = NO_SMOOTHING_GROUP;
	private ArrayList<Face> currentSmoothingGroup = null;
	public HashMap<String, ArrayList<Face>> groups = new HashMap<>();
	private final List<String> currentGroups = new ArrayList<>();
	private final List<ArrayList<Face>> currentGroupFaceLists = new ArrayList<>();
	public String objectName = null;
	private com.owens.oobjloader.builder.Material currentMaterial = null;
	private com.owens.oobjloader.builder.Material currentMap = null;
	public HashMap<String, com.owens.oobjloader.builder.Material> materialLib = new HashMap<>();
	private com.owens.oobjloader.builder.Material currentMaterialBeingParsed = null;
	public HashMap<String, com.owens.oobjloader.builder.Material> mapLib = new HashMap<>();
	private final com.owens.oobjloader.builder.Material currentMapBeingParsed = null;
	public int faceTriCount = 0;
	public int faceQuadCount = 0;
	public int facePolyCount = 0;
	public int faceErrorCount = 0;

	private final TrollyLoadbar loadbar = new TrollyLoadbar();

	public Build() {
	}

	@Override
	public void setObjFilename(final String filename) {
		objFilename = filename;
	}

	@Override
	public void addVertexGeometric(final float x, final float y, final float z) {
		verticesG.add(new VertexGeometric(x, y, z));
		// log.log(INFO,"Added geometric vertex " + verticesG.size() + " = " +
		// verticesG.get(verticesG.size() - 1));
	}

	@Override
	public void addVertexTexture(final float u, final float v) {
		verticesT.add(new VertexTexture(u, v));
		// log.log(INFO,"Added texture vertex " + verticesT.size() + " = " +
		// verticesT.get(verticesT.size() - 1));
	}

	@Override
	public void addVertexNormal(final float x, final float y, final float z) {
		verticesN.add(new VertexNormal(x, y, z));
	}

	@Override
	public void addPoints(final int[] values) {
		log.log(INFO, "@TODO: Got " + values.length + " points in builder, ignoring");
	}

	@Override
	public void addLine(final int[] values) {
		log.log(INFO, "@TODO: Got a line of " + values.length + " segments in builder, ignoring");
	}

	@Override
	public void addFace(final int[] vertexIndices) {
		final Face face = new Face();

		face.material = currentMaterial;
		face.map = currentMap;

		int loopi = 0;
		// @TODO: add better error checking - make sure values is not empty and
		// that it is a multiple of 3
		while (loopi < vertexIndices.length) {
			// > v is the vertex reference number for a point element. Each
			// point
			// > element requires one vertex. Positive values indicate absolute
			// > vertex numbers. Negative values indicate relative vertex
			// numbers.

			FaceVertex fv = new FaceVertex();
			// log.log(INFO,"Adding vertex g=" + vertexIndices[loopi] + " t=" +
			// vertexIndices[loopi + 1] + " n=" + vertexIndices[loopi + 2]);
			int vertexIndex;
			vertexIndex = vertexIndices[loopi++];
			// Note that we can use negative references to denote vertices in
			// manner relative to the current point in the file, i.e.
			// rather than "the 5th vertice in the file" we can say "the 5th
			// vertice before now"
			if (vertexIndex < 0) {
				vertexIndex = vertexIndex + verticesG.size();
			}
			if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesG.size())) {
				// Note: vertex indices are 1-indexed, i.e. they start at
				// one, so we offset by -1 for the 0-indexed array lists.
				fv.v = verticesG.get(vertexIndex - 1);
			} else {
				log.log(SEVERE,
						"Index for geometric vertex=" + vertexIndex
								+ " is out of the current range of geometric vertex values 1 to " + verticesG.size()
								+ ", ignoring");
			}

			vertexIndex = vertexIndices[loopi++];
			if (vertexIndex != EMPTY_VERTEX_VALUE) {
				if (vertexIndex < 0) {
					// Note that we can use negative references to denote
					// vertices in manner relative to the current point in the
					// file, i.e.
					// rather than "the 5th vertice in the file" we can say "the
					// 5th vertice before now"
					vertexIndex = vertexIndex + verticesT.size();
				}
				if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesT.size())) {
					// Note: vertex indices are 1-indexed, i.e. they start at
					// one, so we offset by -1 for the 0-indexed array lists.
					fv.t = verticesT.get(vertexIndex - 1);
				} else {
					log.log(SEVERE,
							"Index for texture vertex=" + vertexIndex
									+ " is out of the current range of texture vertex values 1 to " + verticesT.size()
									+ ", ignoring");
				}
			}

			vertexIndex = vertexIndices[loopi++];
			if (vertexIndex != EMPTY_VERTEX_VALUE) {
				if (vertexIndex < 0) {
					// Note that we can use negative references to denote
					// vertices in manner relative to the current point in the
					// file, i.e.
					// rather than "the 5th vertice in the file" we can say "the
					// 5th vertice before now"
					vertexIndex = vertexIndex + verticesN.size();
				}
				if (((vertexIndex - 1) >= 0) && ((vertexIndex - 1) < verticesN.size())) {
					// Note: vertex indices are 1-indexed, i.e. they start at
					// one, so we offset by -1 for the 0-indexed array lists.
					fv.n = verticesN.get(vertexIndex - 1);
				} else {
					log.log(SEVERE,
							"Index for vertex normal=" + vertexIndex
									+ " is out of the current range of vertex normal values 1 to " + verticesN.size()
									+ ", ignoring");
				}
			}

			if (fv.v == null) {
				log.log(SEVERE, "Can't add vertex to face with missing vertex!  Throwing away face.");
				faceErrorCount++;
				return;
			}

			// Make sure we don't end up with redundant vertice
			// combinations - i.e. any specific combination of g,v and
			// t is only stored once and is reused instead.
			final String key = fv.toString();
			final FaceVertex fv2 = faceVerticeMap.get(key);
			if (null == fv2) {
				faceVerticeMap.put(key, fv);
				fv.index = faceVerticeList.size();
				faceVerticeList.add(fv);
			} else {
				fv = fv2;
			}

			face.add(fv);
		}
		// log.log(INFO,"Parsed face=" + face);
		if (currentSmoothingGroup != null) {
			currentSmoothingGroup.add(face);
		}

		if (currentGroupFaceLists.size() > 0) {
			for (loopi = 0; loopi < currentGroupFaceLists.size(); loopi++) {
				currentGroupFaceLists.get(loopi).add(face);
			}
		}

		faces.add(face);

		// collect some stats for laughs
		if (face.vertices.size() == 3) {
			faceTriCount++;
		} else if (face.vertices.size() == 4) {
			faceQuadCount++;
		} else {
			facePolyCount++;
		}
	}

	// @TODO: http://local.wasp.uwa.edu.au/~pbourke/dataformats/obj/
	//
	// > Grouping
	// >
	// > There are four statements in the .obj file to help you manipulate
	// groups
	// > of elements:
	// >
	// > o Gropu name statements are used to organize collections of
	// > elements and simplify data manipulation for operations in
	// > Model.
	// ...
	// > o Object name statements let you assign a name to an entire object
	// > in a single file.
	// >
	// > All grouping statements are state-setting. This means that once a
	// > group statement is set, it alpplies to all elements that follow
	// > until the next group statement.
	// >
	// > This portion of a sample file shows a single element which belongs to
	// > three groups. The smoothing group is turned off.
	// >
	// > g square thing all
	// > s off
	// > f 1 2 3 4
	// >
	// > This example shows two surfaces in merging group 1 with a merge
	// > resolution of 0.5.
	// >
	// > mg 1 .5
	// > surf 0.0 1.0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
	// > surf 0.0 1.0 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32
	// >
	// > Syntax
	// >
	// > g group_name1 group_name2 . . .
	// >
	// > Polygonal and free-form geometry statement.
	// >
	// > Specifies the group name for the elements that follow it. You can
	// > have multiple group names. If there are multiple groups on one
	// > line, the data that follows belong to all groups. Group information
	// > is optional.
	// >
	// > group_name is the name for the group. Letters, numbers, and
	// > combinations of letters and numbers are accepted for group names.
	// > The default group name is default.
	// ...
	// > 1. Cube with group names
	// >
	// > The following example is a cube with each of its faces placed in a
	// > separate group. In addition, all elements belong to the group cube.
	// >
	// > v 0.000000 2.000000 2.000000
	// > v 0.000000 0.000000 2.000000
	// > v 2.000000 0.000000 2.000000
	// > v 2.000000 2.000000 2.000000
	// > v 0.000000 2.000000 0.000000
	// > v 0.000000 0.000000 0.000000
	// > v 2.000000 0.000000 0.000000
	// > v 2.000000 2.000000 0.000000
	// > # 8 vertices
	// >
	// > g front cube
	// > f 1 2 3 4
	// > g back cube
	// > f 8 7 6 5
	// > g right cube
	// > f 4 3 7 8
	// > g top cube
	// > f 5 1 4 8
	// > g left cube
	// > f 5 6 2 1
	// > g bottom cube
	// > f 2 6 7 3
	// > # 6 elements
	@Override
	public void setCurrentGroupNames(final String[] names) {
		currentGroups.clear();
		currentGroupFaceLists.clear();
		if (null == names) {
			// Set current group to 'none' - so since we've already
			// cleared the currentGroups lists, just return.
			return;
		}
		for (String name : names) {
			final String group = name.trim();
			currentGroups.add(group);
			groups.computeIfAbsent(group, k -> new ArrayList<Face>());
			currentGroupFaceLists.add(groups.get(group));
		}
	}

	@Override
	public void addObjectName(final String name) {
		objectName = name;
	}

	@Override
	public void setCurrentSmoothingGroup(final int groupNumber) {
		currentSmoothingGroupNumber = groupNumber;
		if (currentSmoothingGroupNumber == NO_SMOOTHING_GROUP) {
			return;
		}
		if (null == smoothingGroups.get(currentSmoothingGroupNumber)) {
			currentSmoothingGroup = new ArrayList<>();
			smoothingGroups.put(currentSmoothingGroupNumber, currentSmoothingGroup);
		}
	}

	// @TODO:
	//
	// > maplib filename1 filename2 . . .
	// >
	// > This is a rendering identifier that specifies the map library file
	// > for the texture map definitions set with the usemap identifier. You
	// > can specify multiple filenames with maplib. If multiple filenames
	// > are specified, the first file listed is searched first for the map
	// > definition, the second file is searched next, and so on.
	// >
	// > When you assign a map library using the Model program, Model allows
	// > only one map library per .obj file. You can assign multiple
	// > libraries using a text editor.
	// >
	// > filename is the name of the library file where the texture maps are
	// > defined. There is no default.
	@Override
	public void addMapLib(final String[] names) {
		if (null == names) {
			log.log(INFO,
					"@TODO: ERROR! Got a maplib line with null names array - blank group line? (i.e. \"g\\n\" ?)");
			return;
		}
		if (names.length == 1) {
			log.log(INFO, "@TODO: Got a maplib line with one name=|" + names[0] + "|");
			return;
		}
		log.log(INFO, "@TODO: Got a maplib line;");
		for (int loopi = 0; loopi < names.length; loopi++) {
			log.log(INFO, "        names[" + loopi + "] = |" + names[loopi] + "|");
		}
	}

	// @TODO:
	//
	// > usemap map_name/off
	// >
	// > This is a rendering identifier that specifies the texture map name
	// > for the element following it. To turn off texture mapping, specify
	// > off instead of the map name.
	// >
	// > If you specify texture mapping for a face without texture vertices,
	// > the texture map will be ignored.
	// >
	// > map_name is the name of the texture map.
	// >
	// > off turns off texture mapping. The default is off.
	@Override
	public void setCurrentUseMap(final String name) {
		currentMap = mapLib.get(name);
	}

	// > usemtl material_name
	// >
	// > Polygonal and free-form geometry statement.
	// >
	// > Specifies the material name for the element following it. Once a
	// > material is assigned, it cannot be turned off; it can only be
	// > changed.
	// >
	// > material_name is the name of the material. If a material name is
	// > not specified, a white material is used.
	@Override
	public void setCurrentUseMaterial(final String name) {
		currentMaterial = materialLib.get(name);
	}

	// > mtllib filename1 filename2 . . .
	// >
	// > Polygonal and free-form geometry statement.
	// >
	// > Specifies the material library file for the material definitions
	// > set with the usemtl statement. You can specify multiple filenames
	// > with mtllib. If multiple filenames are specified, the first file
	// > listed is searched first for the material definition, the second
	// > file is searched next, and so on.
	// >
	// > When you assign a material library using the Model program, only
	// > one map library per .obj file is allowed. You can assign multiple
	// > libraries using a text editor.
	// >
	// > filename is the name of the library file that defines the
	// > materials. There is no default.
	// @TODO: I think I need to just delete this... because we now parse
	// material lib files in Parse.java in processMaterialLib()
	// public void addMaterialLib(String[] names) {
	// if (null == names) {
	// log.log(INFO,"@TODO: Got a mtllib line with null names array - blank
	// group line? (i.e. \"g\\n\" ?)");
	// return;
	// }
	// if (names.length == 1) {
	// log.log(INFO,"@TODO: Got a mtllib line with one name=|" + names[0] +
	// "|");
	// return;
	// }
	// log.log(INFO,"@TODO: Got a mtllib line;");
	// for (int loopi = 0; loopi < names.length; loopi++) {
	// log.log(INFO," names[" + loopi + "] = |" + names[loopi] + "|");
	// }
	// }
	@Override
	public void newMtl(final String name) {
		currentMaterialBeingParsed = new com.owens.oobjloader.builder.Material(name);
		currentMaterialBeingParsed.dFactor = 1.0; // NOTE TODO MatrixEater here
													// - default StaticAlpha on
													// layer is 1.0
		materialLib.put(name, currentMaterialBeingParsed);
	}

	@Override
	public void setXYZ(final int type, final float x, final float y, final float z) {
		ReflectivityTransmiss rt = currentMaterialBeingParsed.ka;
		if (type == MTL_KD) {
			rt = currentMaterialBeingParsed.kd;
		} else if (type == MTL_KS) {
			rt = currentMaterialBeingParsed.ks;
		} else if (type == MTL_TF) {
			rt = currentMaterialBeingParsed.tf;
		}

		rt.rx = x;
		rt.gy = y;
		rt.bz = z;
		rt.isXYZ = true;
		rt.isRGB = false;
	}

	@Override
	public void setRGB(final int type, final float r, final float g, final float b) {
		ReflectivityTransmiss rt = currentMaterialBeingParsed.ka;
		if (type == MTL_KD) {
			rt = currentMaterialBeingParsed.kd;
		} else if (type == MTL_KS) {
			rt = currentMaterialBeingParsed.ks;
		} else if (type == MTL_TF) {
			rt = currentMaterialBeingParsed.tf;
		}

		rt.rx = r;
		rt.gy = g;
		rt.bz = b;
		rt.isRGB = true;
		rt.isXYZ = false;
	}

	@Override
	public void setIllum(final int illumModel) {
		currentMaterialBeingParsed.illumModel = illumModel;
	}

	@Override
	public void setD(final boolean halo, final float factor) {
		currentMaterialBeingParsed.dHalo = halo;
		currentMaterialBeingParsed.dFactor = factor;
		log.log(INFO, "@TODO: got a setD call!");
	}

	@Override
	public void setNs(final float exponent) {
		currentMaterialBeingParsed.nsExponent = exponent;
		log.log(INFO, "@TODO: got a setNs call!");
	}

	@Override
	public void setSharpness(final float value) {
		currentMaterialBeingParsed.sharpnessValue = value;
	}

	@Override
	public void setNi(final float opticalDensity) {
		currentMaterialBeingParsed.niOpticalDensity = opticalDensity;
	}

	@Override
	public void setMapDecalDispBump(final int type, final String filename) {
		if (type == MTL_MAP_KA) {
			currentMaterialBeingParsed.mapKaFilename = filename;
		} else if (type == MTL_MAP_KD) {
			currentMaterialBeingParsed.mapKdFilename = filename;
		} else if (type == MTL_MAP_KS) {
			currentMaterialBeingParsed.mapKsFilename = filename;
		} else if (type == MTL_MAP_NS) {
			currentMaterialBeingParsed.mapNsFilename = filename;
		} else if (type == MTL_MAP_D) {
			currentMaterialBeingParsed.mapDFilename = filename;
		} else if (type == MTL_DECAL) {
			currentMaterialBeingParsed.decalFilename = filename;
		} else if (type == MTL_DISP) {
			currentMaterialBeingParsed.dispFilename = filename;
		} else if (type == MTL_BUMP) {
			currentMaterialBeingParsed.bumpFilename = filename;
		}
	}

	@Override
	public void setRefl(final int type, final String filename) {
		currentMaterialBeingParsed.reflType = type;
		currentMaterialBeingParsed.reflFilename = filename;
	}

	@Override
	public void doneParsingMaterial() {
		// if we finish a .mtl file, and then we parse another mtllib (.mtl)
		// file AND that other
		// file is malformed, and missing the newmtl line, then any statements
		// would quietly
		// overwrite whatever is being pointed to by currentMaterialBeingParsed.
		// Hence we set
		// it to null now. (Now any such malformed .mtl file will cause an
		// exception but that's
		// better than quiet bugs.) This method ( doneParsingMaterial() ) is
		// called by Parse when
		// it finished parsing a .mtl file.
		//
		// @TODO: We can make this not throw an exception if we simply add a
		// check for a null
		// currentMaterialBeingParsed at the start of each material setter
		// method in Build... but that
		// still assumes we'll always have a newmtl line FIRST THING for each
		// material, to create the
		// currentMaterialBeingParsed object. Is that a reasonable assumption?
		currentMaterialBeingParsed = null;
	}

	@Override
	public void doneParsingObj(final String filename) {
		log.log(INFO,
				"Loaded filename '" + filename + "' with " + verticesG.size() + " verticesG, " + verticesT.size()
						+ " verticesT, " + verticesN.size() + " verticesN and " + faces.size() + " faces, of which "
						+ faceTriCount + " triangles, " + faceQuadCount + " quads, and " + facePolyCount
						+ " with more than 4 points, and faces with errors " + faceErrorCount);
	}

	/**
	 * The code for this part looks ridiculous, but the point is that we want to
	 * make sure when we convert to MDL that regardless of how data was stored
	 * in the OBJ, the MDL will have the existence of two vertices at the same
	 * world position only when they have different normals or different texture
	 * coordinates.
	 *
	 * So, I am accomplishing that by hashing based on a set of the 8 values
	 * below as the key. Two faces that reference a vertex in exactly the same
	 * place with exactly the same additional data will end up hashing to the
	 * same key and therefore linking to the same physical vertex ID inside the
	 * MDL.
	 *
	 * @author Eric "Retera"
	 *
	 */
	private final class VertexKey {
		private final float posX, posY, posZ;
		private final float normX, normY, normZ;
		private final float uvU, uvV;

		public VertexKey(final VertexGeometric vertex, final VertexNormal normal, final VertexTexture uv) {
			posX = vertex.z;
			posY = vertex.x;
			posZ = vertex.y;
			if (normal != null) {
				normX = normal.z;
				normY = normal.x;
				normZ = normal.y;
			} else {
				normX = 0.0f;
				normY = 0.0f;
				normZ = 1.0f;
				// TODO should add normal gens
				// log.log(INFO, "@TODO: Got mesh without normal, should later
				// add normal generation");
			}
			if (uv != null) {
				uvU = uv.u;
				uvV = 1.0f - uv.v;
			} else {
				uvU = 0.01f;
				uvV = 0.01f;
			}
		}

		public VertexKey(final FaceVertex fv) {
			this(fv.v, fv.n, fv.t);
		}

		public GeosetVertex createVertex() {
			return new GeosetVertex(posX, posY, posZ, new Vec3(normX, normY, normZ));
		}

		public Vec2 createTVertex() {
			return new Vec2(uvU, uvV);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(normX);
			result = prime * result + Float.floatToIntBits(normY);
			result = prime * result + Float.floatToIntBits(normZ);
			result = prime * result + Float.floatToIntBits(posX);
			result = prime * result + Float.floatToIntBits(posY);
			result = prime * result + Float.floatToIntBits(posZ);
			result = prime * result + Float.floatToIntBits(uvU);
			result = prime * result + Float.floatToIntBits(uvV);
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final VertexKey other = (VertexKey) obj;
			if (Float.floatToIntBits(normX) != Float.floatToIntBits(other.normX)) {
				return false;
			}
			if (Float.floatToIntBits(normY) != Float.floatToIntBits(other.normY)) {
				return false;
			}
			if (Float.floatToIntBits(normZ) != Float.floatToIntBits(other.normZ)) {
				return false;
			}
			if (Float.floatToIntBits(posX) != Float.floatToIntBits(other.posX)) {
				return false;
			}
			if (Float.floatToIntBits(posY) != Float.floatToIntBits(other.posY)) {
				return false;
			}
			if (Float.floatToIntBits(posZ) != Float.floatToIntBits(other.posZ)) {
				return false;
			}
			if (Float.floatToIntBits(uvU) != Float.floatToIntBits(other.uvU)) {
				return false;
			}
			return Float.floatToIntBits(uvV) == Float.floatToIntBits(other.uvV);
		}
	}

	private static final class Subgroup {
		private final Map<VertexKey, Integer> vertexKeys;
		private final Geoset geo;
		private int keyCounter = 0;

		public Subgroup(final Map<VertexKey, Integer> vertexKeys, final Geoset geo) {
			this.vertexKeys = vertexKeys;
			this.geo = geo;
		}

		public Map<VertexKey, Integer> getVertexKeys() {
			return vertexKeys;
		}

		public Geoset getGeo() {
			return geo;
		}

		public int nextKey() {
			return keyCounter++;
		}
	}

	public EditableModel createMDL() throws IOException {
		try {
			final EditableModel mdl = new EditableModel(new File(objFilename).getName());
			if (faces.size() >= 10000 || verticesG.size() >= 10000) {
				loadbar.show();
			}
			final Set<Face> processedFaces = new HashSet<>();
			for (final Map.Entry<String, ArrayList<Face>> entry : groups.entrySet()) {
				// we want to split group by material
				// for each material, we have
				// - list of vertices
				// - geoset
				final Map<com.owens.oobjloader.builder.Material, Subgroup> materialToSubgroup = new HashMap<>();
				// final Map<VertexKey,GeosetVertex> builderVertexToMdlVertex =
				// new HashMap<VertexKey,GeosetVertex>();
				// Map
				final List<Face> faceList = entry.getValue();
				if (faceList.size() >= 10000) {
					loadbar.show();
				}
				convertMesh(mdl, processedFaces, entry.getKey(), materialToSubgroup, faceList);
			}
			// ==================== second run for "face" global
			// ============================

			final Map<com.owens.oobjloader.builder.Material, Subgroup> materialToSubgroup = new HashMap<>();
			convertMesh(mdl, processedFaces, objectName, materialToSubgroup, faces);

			// ===================== end second run
			// =========================================
			if (loadbar.isVisible()) {
				loadbar.setPercent(0f);
				loadbar.setText("Collapsing MatrixEater MDL representation");
			}
			mdl.doSavePreps();
			if (loadbar.isVisible()) {
				loadbar.setPercent(0.5f);
				loadbar.setText("Preparing model for editing...");
			}
			mdl.doPostRead();
			if (loadbar.isVisible()) {
				loadbar.setPercent(1.0f);
				loadbar.setText("Adding \"Stand\" animation...");
			}
			mdl.add(new Animation("Stand", 333, 1333));
			boolean allLessThan2 = true;
			final int sizeLimit = 10;
			if (loadbar.isVisible()) {
				loadbar.setPercent(0.0f);
				loadbar.setText("Scanning for WoW sizing...");
			}
			for (final Geoset geo : mdl.getGeosets()) {
				for (final GeosetVertex gv : geo.getVertices()) {
					if (Math.abs(gv.x) > sizeLimit || Math.abs(gv.y) > sizeLimit || Math.abs(gv.z) > sizeLimit) {
						allLessThan2 = false;
						break;
					}
				}
			}
			for (final Vec3 pivot : mdl.getPivots()) {
				if (Math.abs(pivot.x) > sizeLimit || Math.abs(pivot.y) > sizeLimit || Math.abs(pivot.z) > sizeLimit) {
					allLessThan2 = false;
					break;
				}
			}
			if (allLessThan2) {
				final String[] options = { "x32", "x64", "x128", "No" };
				final int option = JOptionPane.showOptionDialog(null,
						"This model might be a WoW model, or peculiarly small. Would you like to increase its size?",
						"WoW Scaling", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[1]);
				// final int result = JOptionPane.showConfirmDialog(null, "This
				// model might be a WoW model, or peculiarly small. Would you
				// like to increase its size?","WoW
				// Scaling",JOptionPane.YES_NO_OPTION);
				if (option != JOptionPane.CLOSED_OPTION && option != 3) {
					final int factor = (int) (32 * Math.pow(2, option));
					for (final Geoset geo : mdl.getGeosets()) {
						for (final GeosetVertex gv : geo.getVertices()) {
							gv.x *= factor;
							gv.y *= factor;
							gv.z *= factor;
						}
					}
					for (final Vec3 pivot : mdl.getPivots()) {
						pivot.x *= factor;
						pivot.y *= factor;
						pivot.z *= factor;
					}
				}
			}
			if (loadbar.isVisible()) {
				loadbar.setPercent(0.0f);
				loadbar.setText("Scanning for convertable textures...");
			}
			boolean hasPNGs = false;
			int index = 0;
			int nLayers = 0;
			for (final com.hiveworkshop.rms.editor.model.Material material : mdl.getMaterials()) {
				for (final Layer layer : material.getLayers()) {
					final String name = layer.getTextureBitmap().getPath();
					if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".tga")
							|| name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".bmp")) {
						hasPNGs = true;
						nLayers++;
					}
				}
				if (loadbar.isVisible()) {
					loadbar.setPercent(index / (float) mdl.getMaterials().size());
					loadbar.setText("Scanning for convertable textures...");
					index++;
				}
			}
			boolean userWantsSwapToBLP = false;
			if (hasPNGs) {
				final int result = JOptionPane.showConfirmDialog(null,
						"This OBJ model contains references to non-BLP files in its materials. Automatically create corresponding BLP files?\n\nIf you choose YES, the MDL format of this OBJ will also be generated to support Matrix Eater 3D viewing.",
						"Convert Textures to BLPs", JOptionPane.YES_NO_OPTION);
				userWantsSwapToBLP = result == JOptionPane.YES_OPTION;
			}
			final File objFile = new File(objFilename);
			final File objFolder = objFile.getParentFile();
			final File mdlFile = new File(objFolder.getPath() + "/"
					+ objFile.getName().substring(0, objFile.getName().length() - 4) + ".mdl");
			if (userWantsSwapToBLP) {
				mdl.setFileRef(mdlFile);
			}
			if (loadbar.isVisible()) {
				loadbar.setPercent(0.0f);
				loadbar.setText("Converting textures...");
				index = 0;
			}
			for (final com.hiveworkshop.rms.editor.model.Material material : mdl.getMaterials()) {
				for (final Layer layer : material.getLayers()) {
					String name = layer.getTextureBitmap().getPath();
					if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".tga")
							|| name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".bmp")) {
						if (userWantsSwapToBLP) {
							File imageFilePNG = new File(objFolder.getPath() + "/" + name);
							try {
								if (!imageFilePNG.exists() && name.contains("_")) {
									imageFilePNG = new File(
											objFolder.getPath() + "/" + name.substring(name.indexOf("_") + 1));
								}
								if (!imageFilePNG.exists()) {
									imageFilePNG = new File(objFolder.getPath() + "/textures/" + name);
								}
								if (!imageFilePNG.exists() && name.contains("_")) {
									imageFilePNG = new File(
											objFolder.getPath() + "/textures/" + name.substring(name.indexOf("_") + 1));
								}
								BufferedImage imageData = ImageIO.read(imageFilePNG);
								if (imageData == null && imageFilePNG.getPath().toLowerCase().endsWith(".tga")) {
									imageData = TargaReader.getImage(imageFilePNG.getPath());
								}
								if (imageData == null) {
									throw new RuntimeException("Java/MatrixEater failed to read image data: "
											+ imageFilePNG.getAbsolutePath());
								}
								final File imageFileBLP = new File(
										objFolder.getPath() + "/" + name.substring(0, name.lastIndexOf('.')) + ".blp"); // name.replace(".png",
																														// ".blp").replace(".PNG",
																														// ".BLP"));
								BlpFile.writeJpgBLP(imageData, imageFileBLP, true, 0.90f);
								// BlpFile.writePalettedBLP(imageData,
								// imageFileBLP, true, true, false);
							} catch (final Exception e) {
								ExceptionPopup.display("Unable to convert PNG to BLP: " + imageFilePNG.toString(), e);
							}
						}
						name = name.substring(0, name.lastIndexOf('.')) + ".blp";
						layer.getTextureBitmap().setPath(name);
					}
					if (loadbar.isVisible()) {
						loadbar.setPercent(index / (float) nLayers);
						loadbar.setText("Converting textures..");
						index++;
					}
				}
			}
			if (userWantsSwapToBLP) {
				if (loadbar.isVisible()) {
					loadbar.setPercent(0);
					loadbar.setText("Saving file...");
				}
				MdxUtils.saveMdx(mdl, mdl.getFile());
			}
			return mdl;
		} catch (final IOException e) {
			throw e;
		} catch (final Exception e) {
			throw e;
		} finally {
			loadbar.hide();
		}
	}

	private void convertMesh(final EditableModel mdl, final Set<Face> processedFaces, final String groupName,
							 final Map<com.owens.oobjloader.builder.Material, Subgroup> materialToSubgroup, final List<Face> faceList) {
		if (loadbar.isVisible()) {
			loadbar.setText("Converting " + groupName + " ...");
		}
		FaceIteration: for (final Face face : faceList) {
			if (processedFaces.contains(face)) {
				continue;
			}
			processedFaces.add(face);
			for (int subTriangleIndex = 0; subTriangleIndex < face.vertices.size() - 2; subTriangleIndex++) {
				Subgroup subgroup = materialToSubgroup.get(face.material);
				if (subgroup == null) {
					subgroup = new Subgroup(new HashMap<>(), new Geoset());
					materialToSubgroup.put(face.material, subgroup);
				}
				final Geoset geo = subgroup.getGeo();
				final Map<VertexKey, Integer> vertexKeys = subgroup.getVertexKeys();
				// final GeosetVertex[] geoVertices = new GeosetVertex[3];
				final int[] vertexIndices = new int[3];
				for (int i = 0; i < 3; i++) {
					final FaceVertex faceVertex = face.vertices.get(i == 0 ? 0 : (subTriangleIndex + i));
					if (faceVertex == null) {
						continue FaceIteration;
					}
					final VertexKey key = new VertexKey(faceVertex);
					Integer index = vertexKeys.get(key);
					if (index == null) {
						index = subgroup.nextKey();// vertexKeys.size();
						vertexKeys.put(key, index);
					}
					// GeosetVertex gv = builderVertexToMdlVertex.get(key);
					// if( gv == null ) {
					// builderVertexToMdlVertex.put(key, gv =
					// key.createVertex());
					// }
					// geoVertices[i] = gv;
					vertexIndices[i] = index;
				}
				final Triangle triangle = new Triangle(vertexIndices[0], vertexIndices[1], vertexIndices[2]);
				geo.add(triangle);
			}
			final int facesProcessedCount = processedFaces.size();
			if (loadbar.isVisible() && facesProcessedCount % 100 == 0) {
				loadbar.setPercent((float) facesProcessedCount / (float) faces.size());
			}
		}
		final Bone groupBone = new Bone(groupName);
		mdl.add(groupBone);
		final List<Vec3> attachedVertices = new ArrayList<>();
		for (final Map.Entry<com.owens.oobjloader.builder.Material, Subgroup> subgroup : materialToSubgroup.entrySet()) {
			final Geoset geo = subgroup.getValue().getGeo();
			geo.setParentModel(mdl);
			final Map<VertexKey, Integer> vertexKeysToIndices = subgroup.getValue().getVertexKeys();

			boolean noteForMatrixEaterAboutWrapHeights = false;
			final UVLayer uvLayer = new UVLayer();
			geo.addUVLayer(uvLayer);
			final List<VertexKey> vertexKeys = new ArrayList<>(vertexKeysToIndices.keySet());
			vertexKeys.sort((a, b) -> vertexKeysToIndices.get(a).compareTo(vertexKeysToIndices.get(b)));
			// for(VertexKey key: vertexKeysToIndices.keySet()) {
			// vertexKeys.add(e)
			// }

			for (final VertexKey key : vertexKeys) {
				final List<Vec2> tverts = new ArrayList<>();
				final Vec2 createdTVertex = key.createTVertex();

				if (createdTVertex.x > 1.0 || createdTVertex.x < 0 || createdTVertex.y > 1.0
						|| createdTVertex.y < 0) {
					noteForMatrixEaterAboutWrapHeights = true;
				}
				tverts.add(createdTVertex);
				uvLayer.addTVertex(createdTVertex);
				final GeosetVertex createdVertex = key.createVertex();
				createdVertex.addBoneAttachment(groupBone);
				createdVertex.setTverts(tverts);
				geo.add(createdVertex);
				attachedVertices.add(createdVertex);
			}
			for (final Triangle triangle : geo.getTriangles()) {
				triangle.setGeoset(geo);
				triangle.updateVertexRefs();
			}

			final List<Layer> layers = new ArrayList<>();
			final com.owens.oobjloader.builder.Material material = subgroup.getKey();// materialLib.get(groupName);
			final com.hiveworkshop.rms.editor.model.Material mdlMaterial = convertMaterial(geo, layers, material);
			if (noteForMatrixEaterAboutWrapHeights) {
				for (final Layer layer : layers) {
					layer.getTextureBitmap().setWrapHeight(true);
					layer.getTextureBitmap().setWrapWidth(true);
					// JOptionPane.showMessageDialog(null, "One or more meshes
					// were imported with texture coordinates stretching outside
					// the texture.\n\nThese will not render correctly in the
					// Matrix Eater viewport, but their\ncorresponding textures
					// will be flagged to WrapWidth and WrapHeight and
					// render\ncorrectly in the Warcraft III game and Magos's
					// viewer.");
				}
			}
			geo.setMaterial(mdlMaterial);
			if (!geo.getVertices().isEmpty()) {
				mdl.add(geo);
				geo.applyVerticesToMatrices(mdl);
			}
		}
		if (!attachedVertices.isEmpty()) {
			groupBone.setPivotPoint(Vec3.centerOfGroup(attachedVertices));
		} else {
			mdl.remove(groupBone);
		}

		// for(final GeosetVertex vertex: builderVertexToMdlVertex.values()) {
		// geo.add(vertex);
		// uvLayer.addTVertex(v);
		// }
	}

	private com.hiveworkshop.rms.editor.model.Material convertMaterial(final Geoset geo, final List<Layer> layers,
																	   com.owens.oobjloader.builder.Material material) {
		if (material == null) {
			material = new com.owens.oobjloader.builder.Material("defaultmat");
			material.dFactor = 1.0f;
			material.mapKdFilename = "Textures\\white.blp";
		}
		boolean transparent = false;
		boolean colorOn = true;
		switch (material.illumModel) {
		case 0:
			colorOn = true;
			break;
		// 0. Color on and Ambient off
		case 1:
			colorOn = true;
			break;
		// 1. Color on and Ambient on
		case 2:
			break;
		// 2. Highlight on
		case 3:
			break;
		// 3. Reflection on and Ray trace on
		case 4:
			transparent = true;
			break;
		// 4. Transparency: Glass on, Reflection: Ray trace on
		case 5:
			break;
		// 5. Reflection: Fresnel on and Ray trace on
		case 6:
			transparent = true;
			break;
		// 6. Transparency: Refraction on, Reflection: Fresnel off and Ray trace
		// on
		case 7:
			transparent = true;
			break;
		// 7. Transparency: Refraction on, Reflection: Fresnel on and Ray trace
		// on
		case 8:
			break;
		// 8. Reflection on and Ray trace off
		case 9:
			transparent = true;
			break;
		// 9. Transparency: Glass on, Reflection: Ray trace off
		case 10:
			ExceptionPopup.display("Casting shadows not supported in WC3", new Exception("Not supported"));
			break;
		// 10. Casts shadows onto invisible surfaces
		}

		addLayerByName(layers, material.bumpFilename, null);
		addLayerByName(layers, material.decalFilename, null);
		addLayerByName(layers, material.dispFilename, null);
		addLayerByName(layers, material.mapDFilename, null);
		addLayerByName(layers, material.mapKaFilename, material.ka);
		addLayerByName(layers, material.mapKdFilename, material.kd);
		addLayerByName(layers, material.mapKsFilename, material.ks, "Additive");
		addLayerByName(layers, material.mapNsFilename, null);
		addLayerByName(layers, material.reflFilename, null);
		if (layers.isEmpty()) {
			final Layer layer = new Layer("None", new Bitmap("Textures\\white.blp", -1));
			layers.add(layer);
		}
		for (final Layer layer : layers) {
			if (transparent) {
				if (layer.getFilterMode() == FilterMode.ADDITIVE) {
					layer.setFilterMode(FilterMode.ADDALPHA);
				} else {
					layer.setFilterMode(FilterMode.TRANSPARENT);
				}
			}
			final double staticAlpha = material.dFactor;
			if (staticAlpha != 1.0 && staticAlpha != 0.0) {
				layer.setStaticAlpha(staticAlpha);
			}
		}
		final com.hiveworkshop.rms.editor.model.Material mdlMaterial = new com.hiveworkshop.rms.editor.model.Material(layers);
		if (colorOn) {
			mdlMaterial.setConstantColor(true);
			final List<ReflectivityTransmiss> transmisses = new ArrayList<>();
			ReflectivityTransmiss max = null;
			for (final ReflectivityTransmiss transmiss : transmisses) {
				if (transmiss.bz + transmiss.gy + transmiss.rx > max.bz + max.rx + max.gy) {
					max = transmiss;
				}
			}
			final ReflectivityTransmiss color = max;
			if (color != null) {
				geo.forceGetGeosetAnim().setStaticColor(new Vec3(color.bz, color.gy, color.rx));// pretty
																									// sure
																									// geoset
																									// anim
																									// is
																									// BGR
																									// off
																									// the
																									// top
																									// my
																									// head
			}
		}
		return mdlMaterial;
	}

	private void addLayerByName(final List<Layer> layers, final String name, final ReflectivityTransmiss rt) {
		addLayerByName(layers, name, rt, "None");
	}

	private void addLayerByName(final List<Layer> layers, final String name, final ReflectivityTransmiss rt,
			final String filterMode) {
		if (name != null && !name.equals("")) {
			final Bitmap bitmap = new Bitmap(name, -1);
			final Layer layer = new Layer(filterMode, bitmap);
			layers.add(layer);
		}
	}
}
