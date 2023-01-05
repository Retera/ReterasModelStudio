package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MdlxGeoset implements MdlxBlock, MdlxChunk {
	private static final War3ID VRTX = War3ID.fromString("VRTX"); // vertex position
	private static final War3ID NRMS = War3ID.fromString("NRMS"); // vertex normal
	private static final War3ID PTYP = War3ID.fromString("PTYP"); // face type group
	private static final War3ID PCNT = War3ID.fromString("PCNT"); // face group
	private static final War3ID PVTX = War3ID.fromString("PVTX"); // face
	private static final War3ID GNDX = War3ID.fromString("GNDX"); // vertex groups
	private static final War3ID MTGC = War3ID.fromString("MTGC"); // matrix groups
	private static final War3ID MATS = War3ID.fromString("MATS"); // matrix indices
	private static final War3ID TANG = War3ID.fromString("TANG"); // tangents
	private static final War3ID SKIN = War3ID.fromString("SKIN"); // skin
	private static final War3ID UVAS = War3ID.fromString("UVAS"); // UV group
	private static final War3ID UVBS = War3ID.fromString("UVBS"); // vertex UV position
	
	public float[] vertices;
	public float[] normals;
	public long[] faceTypeGroups; // unsigned int[]
	public long[] faceGroups; // unsigned int[]
	public int[] faces; // unsigned short[]
	public short[] vertexGroups; // unsigned byte[]
	public long[] matrixGroups; // unsigned int[]
	public long[] matrixIndices; // unsigned int[]
	public long materialId = 0;
	public long selectionGroup = 0;
	public long selectionFlags = 0;
	public int lod = 0; // @since 900
	public String lodName = ""; // @since 900
	public MdlxExtent extent = new MdlxExtent();
	public List<MdlxExtent> sequenceExtents = new ArrayList<>();
	public float[] tangents; // @since 900
	public short[] skin; // @since 900
	public float[][] uvSets;

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final long size = reader.readUInt32();

		reader.readInt32(); // skip VRTX
		int vertexCount = reader.readInt32();
		vertices = reader.readFloat32Array(vertexCount * 3);
		reader.readInt32(); // skip NRMS
		int normalCount = reader.readInt32();
		normals = reader.readFloat32Array(normalCount * 3);
		reader.readInt32(); // skip PTYP
		int faceTypeGroupsCount = reader.readInt32();
		faceTypeGroups = reader.readUInt32Array(faceTypeGroupsCount);
		int pcnt = reader.readInt32();// skip PCNT
		int faceGroupCount = reader.readInt32();
		faceGroups = reader.readUInt32Array(faceGroupCount);
		int pvtx = reader.readInt32(); // skip PVTX
		int faceCount = reader.readInt32();
		faces = reader.readUInt16Array(faceCount);
		reader.readInt32(); // skip GNDX
		vertexGroups = reader.readUInt8Array(reader.readInt32());

//		reader.readInt32(); // skip VRTX
//		int vertexCount = reader.readInt32();
//		vertices = reader.readFloat32Array(vertexCount * 3);
//		reader.readInt32(); // skip NRMS
//		int normalCount = reader.readInt32();
//		normals = reader.readFloat32Array(normalCount * 3);
//		int ptyp = reader.readInt32();// skip PTYP
//		int faceTypeGroupsCount = reader.readInt32();
//		faceTypeGroups = reader.readUInt32Array(faceTypeGroupsCount);
//		int pcnt = reader.readInt32();// skip PCNT
////		int faceGroupCount = reader.readInt32();
//		int faceGroupCount = (int) reader.readUInt32();
//		faceGroups = reader.readUInt32Array(faceGroupCount);
//		int pvtx = reader.readInt32(); // skip PVTX
//		int faceCount = reader.readInt32();
//		faces = reader.readUInt16Array(faceCount);
//		reader.readInt32(); // skip GNDX
//
//		int geosetVertexGroupsCount = reader.readInt32();
//		System.out.println("Read: vertexCount: " + vertexCount);
//		System.out.println("Read: normalCount: " + normalCount);
//		System.out.println("Read: ptyp: " + Integer.reverseBytes(ptyp));
//		System.out.println("Read: faceTypeGroupsCount: " + faceTypeGroupsCount);
//		System.out.println("Read: faceTypeGroups: " + Arrays.toString(faceTypeGroups));
//		System.out.println("Read: pcnt: " + Integer.reverseBytes(pcnt));
//		System.out.println("Read: faceGroupCount: " + faceGroupCount);
//		System.out.println("Read: faceGroups: " + Arrays.toString(faceGroups));
//		System.out.println("Read: pvtx: " + Integer.reverseBytes(pvtx));
//		System.out.println("Read: faceCount: " + faceCount);
//		System.out.println("Read: faces: " + Arrays.toString(faces));
//		System.out.println("Read: geosetVGCount: " + geosetVertexGroupsCount);
//		vertexGroups = reader.readUInt8Array(geosetVertexGroupsCount);


		reader.readInt32(); // skip MTGC
		matrixGroups = reader.readUInt32Array(reader.readInt32());
		reader.readInt32(); // skip MATS
		matrixIndices = reader.readUInt32Array(reader.readInt32());
		materialId = reader.readUInt32();
		selectionGroup = reader.readUInt32();
		selectionFlags = reader.readUInt32();

//		System.out.println("Read: vertexCount: " + vertexCount);
//		System.out.println("Read: faceGroupCount: " + faceGroupCount);
//		System.out.println("Read: faceCount: " + faceCount);
//		System.out.println("Read: geosetVGCount: " + geosetVertexGroupsCount);

		if (800 < version) {
			lod = reader.readInt32();
			lodName = reader.read(80);
		}

		extent.readMdx(reader);

		final long numExtents = reader.readUInt32();

		for (int i = 0; i < numExtents; i++) {
			final MdlxExtent extent = new MdlxExtent();
			extent.readMdx(reader);
			sequenceExtents.add(extent);
		}

		int id = reader.readTag(); // TANG or SKIN or UVAS

		if (800 < version && id != UVAS.getValue()) {
			if (id == TANG.getValue()) {
				tangents = reader.readFloat32Array(reader.readInt32() * 4);

				id = reader.readTag(); // SKIN or UVAS
			}

			if (id == SKIN.getValue()) {
				skin = reader.readUInt8Array(reader.readInt32());

				id = reader.readInt32(); // UVAS
			}
		}

		final long numUVLayers = reader.readUInt32();
		uvSets = new float[(int) numUVLayers][];
		for (int i = 0; i < numUVLayers; i++) {
			reader.readInt32(); // skip UVBS
			uvSets[i] = reader.readFloat32Array(reader.readInt32() * 2);
		}
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));
		writer.writeTag(VRTX.getValue());
		writer.writeUInt32(vertices.length / 3);
		writer.writeFloat32Array(vertices);
		writer.writeTag(NRMS.getValue());
		writer.writeUInt32(normals.length / 3);
		writer.writeFloat32Array(normals);
		writer.writeTag(PTYP.getValue());
		writer.writeUInt32(faceTypeGroups.length);
		writer.writeUInt32Array(faceTypeGroups);
		writer.writeTag(PCNT.getValue());
		writer.writeUInt32(faceGroups.length);
		writer.writeUInt32Array(faceGroups);
		writer.writeTag(PVTX.getValue());
		writer.writeUInt32(faces.length);
		writer.writeUInt16Array(faces);
		writer.writeTag(GNDX.getValue());
		writer.writeUInt32(vertexGroups.length);
		writer.writeUInt8Array(vertexGroups);
		writer.writeTag(MTGC.getValue());
		writer.writeUInt32(matrixGroups.length);
		writer.writeUInt32Array(matrixGroups);
		writer.writeTag(MATS.getValue());
		writer.writeUInt32(matrixIndices.length);
		writer.writeUInt32Array(matrixIndices);
		writer.writeUInt32(materialId);
		writer.writeUInt32(selectionGroup);
		writer.writeUInt32(selectionFlags);

//		System.out.println("Save: vertexCount: " + vertices.length / 3);
//		System.out.println("Save: normalCount: " + normals.length / 3);
//		System.out.println("Save: PTYP.getValue(): " + PTYP.getValue());
//		System.out.println("Save: faceTypeGroupsCount: " + faceTypeGroups.length);
//		System.out.println("Save: faceTypeGroups: " + Arrays.toString(faceTypeGroups));
//		System.out.println("Save: PCNT.getValue(): " + PCNT.getValue());
//		System.out.println("Save: faceGroupsCount: " + faceGroups.length);
//		System.out.println("Save: faceGroupsCount: " + Arrays.toString(faceGroups));
//		System.out.println("Save: PVTX.getValue(): " + PVTX.getValue());
//		System.out.println("Save: faceCount: " + faces.length);
//		System.out.println("Save: faceCount: " + (int)((long)faces.length));
//		System.out.println("Save: faces: " + Arrays.toString(faces));
//
//		System.out.println("Save: VG len: " + vertexGroups.length);

		if (800 < version) {
			writer.writeInt32(lod);
			writer.writeWithNulls(lodName, 80);
		}

		extent.writeMdx(writer);
		writer.writeUInt32(sequenceExtents.size());

		for (final MdlxExtent sequenceExtent : sequenceExtents) {
			sequenceExtent.writeMdx(writer);
		}

		if (800 < version) {
			if (tangents != null) {
				writer.writeTag(TANG.getValue());
				writer.writeUInt32(tangents.length / 4);
				writer.writeFloat32Array(tangents);
			}

			if (skin != null) {
				writer.writeTag(SKIN.getValue());
				writer.writeUInt32(skin.length);
				writer.writeUInt8Array(skin);
			}
		}

		writer.writeTag(UVAS.getValue());
		writer.writeUInt32(uvSets.length);
		
		for (final float[] uvSet : uvSets) {
			writer.writeTag(UVBS.getValue());
			writer.writeUInt32(uvSet.length / 2);
			writer.writeFloat32Array(uvSet);
		}
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		uvSets = new float[0][];

		for (final String token : stream.readBlock()) {
			// For now hardcoded for triangles, until I see a model with something different.
//			System.out.println("Token: " + token);
			switch (token) {
				case MdlUtils.TOKEN_VERTICES -> vertices = stream.readVectorArray(new float[stream.readInt() * 3], 3);
				case MdlUtils.TOKEN_NORMALS -> normals = stream.readVectorArray(new float[stream.readInt() * 3], 3);
				case MdlUtils.TOKEN_TVERTICES -> {
					uvSets = Arrays.copyOf(uvSets, uvSets.length + 1);
					uvSets[uvSets.length - 1] = stream.readVectorArray(new float[stream.readInt() * 2], 2);
				}
				case MdlUtils.TOKEN_VERTEX_GROUP -> {
					// Vertex groups are stored in a block with no count, can't allocate the buffer yet.
					final List<Short> vertexGroups = new ArrayList<>();
					for (final String vertexGroup : stream.readBlock()) {
						vertexGroups.add(Short.valueOf(vertexGroup));
					}

					this.vertexGroups = new short[vertexGroups.size()];
					int i = 0;
					for (final Short vertexGroup : vertexGroups) {
						this.vertexGroups[i++] = vertexGroup;
					}
				}
				case MdlUtils.TOKEN_TANGENTS -> {
					final int tansCount = (int) stream.readUInt32();
					tangents = new float[tansCount * 4];
					stream.readVectorArray(tangents, 4);
				}
				case MdlUtils.TOKEN_SKINWEIGHTS -> {
					final int skinCount = (int) stream.readUInt32();
					skin = new short[skinCount * 8];
					stream.readUInt8Array(skin, 8);
				}
				case MdlUtils.TOKEN_FACES -> {
					faceTypeGroups = new long[] {4L};
					stream.readInt(); // number of groups
					final int count = stream.readInt();
					stream.read(); // {
					stream.read(); // Triangles
					if(0 < count){
						faces = stream.readUInt16Array(new int[count], 3);
						faceGroups = new long[] {count};
					} else {
						faces = new int[]{};
						faceGroups = new long[]{};
						if (stream.read().equals("{")){
							if (stream.read().equals("{")){
								stream.read(); // }
							}
							stream.read(); // }
						}
					}
					stream.read(); // }
				}
				case MdlUtils.TOKEN_GROUPS -> {
					final List<Integer> indices = new ArrayList<>();
					final List<Integer> groups = new ArrayList<>();

					stream.readInt(); // matrices count
					stream.readInt(); // total indices

					// eslint-disable-next-line no-unused-vars
					for (final String matrix : stream.readBlock()) {
						int size = 0;

						for (final String index : stream.readBlock()) {
							indices.add(Integer.valueOf(index));
							size += 1;
						}
						groups.add(size);
					}

					matrixIndices = new long[indices.size()];
					int i = 0;
					for (final Integer index : indices) {
						matrixIndices[i++] = index;
					}
					matrixGroups = new long[groups.size()];
					i = 0;
					for (final Integer group : groups) {
						matrixGroups[i++] = group;
					}
				}
				case MdlUtils.TOKEN_MINIMUM_EXTENT -> stream.readFloatArray(extent.min);
				case MdlUtils.TOKEN_MAXIMUM_EXTENT -> stream.readFloatArray(extent.max);
				case MdlUtils.TOKEN_BOUNDSRADIUS -> extent.boundsRadius = stream.readFloat();
				case MdlUtils.TOKEN_ANIM -> {
					final MdlxExtent extent = new MdlxExtent();
					for (final String subToken : stream.readBlock()) {
						switch (subToken) {
							case MdlUtils.TOKEN_MINIMUM_EXTENT -> stream.readFloatArray(extent.min);
							case MdlUtils.TOKEN_MAXIMUM_EXTENT -> stream.readFloatArray(extent.max);
							case MdlUtils.TOKEN_BOUNDSRADIUS -> extent.boundsRadius = stream.readFloat();
						}
					}
					sequenceExtents.add(extent);
				}
				case MdlUtils.TOKEN_MATERIAL_ID -> materialId = stream.readInt();
				case MdlUtils.TOKEN_SELECTION_GROUP -> selectionGroup = stream.readInt();
				case MdlUtils.TOKEN_UNSELECTABLE -> selectionFlags = 4;
				case MdlUtils.TOKEN_LEVELOFDETAIL -> lod = stream.readInt();
				case MdlUtils.TOKEN_NAME -> lodName = stream.read();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Geoset: " + token);
			}
		}
		if(vertexGroups == null){
			vertexGroups = new short[0];
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_GEOSET);

		stream.writeVectorArray(MdlUtils.TOKEN_VERTICES, vertices, 3);
		stream.writeVectorArray(MdlUtils.TOKEN_NORMALS, normals, 3);

		for (final float[] uvSet : uvSets) {
			stream.writeVectorArray(MdlUtils.TOKEN_TVERTICES, uvSet, 2);
		}

		if (version <= 800) {
			stream.startBlock(MdlUtils.TOKEN_VERTEX_GROUP);
			for (short vertexGroup : vertexGroups) {
				stream.writeLine(vertexGroup + ",");
			}
			stream.endBlock();
		}

		if (800 < version) {

			stream.startBlock(MdlUtils.TOKEN_VERTEX_GROUP);
			if (skin == null) {
				for (short vertexGroup : vertexGroups) {
					stream.writeLine(vertexGroup + ",");
				}
			}
			stream.endBlock();


			if (tangents != null) {
				stream.startBlock(MdlUtils.TOKEN_TANGENTS, tangents.length / 4);

				for (int i = 0, l = tangents.length; i < l; i += 4) {
					stream.writeFloatArray(Arrays.copyOfRange(tangents, i, i + 4));
				}

				stream.endBlock();
			}

			if (skin != null) {
				stream.startBlock(MdlUtils.TOKEN_SKINWEIGHTS, skin.length / 8);

				for (int i = 0, l = skin.length; i < l; i += 8) {
					stream.writeShortArrayRaw(Arrays.copyOfRange(skin, i, i + 8));
				}

				stream.endBlock();
			}
		}

		// For now hardcoded for triangles, until I see a model with something different.
		stream.startBlock(MdlUtils.TOKEN_FACES, 1, faces.length);
		stream.startBlock(MdlUtils.TOKEN_TRIANGLES);
		final StringBuilder facesBuffer = new StringBuilder();
		for (final int faceValue : faces) {
			if (0 < facesBuffer.length()) {
				facesBuffer.append(", ");
			}
			facesBuffer.append(faceValue);
		}
		stream.writeLine("{ " + facesBuffer.toString() + " },");
		stream.endBlock();
		stream.endBlock();

		stream.startBlock(MdlUtils.TOKEN_GROUPS, matrixGroups.length, matrixIndices.length);
		int index = 0;
		for (final long groupSize : matrixGroups) {
			stream.writeLongSubArrayAttrib(MdlUtils.TOKEN_MATRICES, matrixIndices, index, (int) (index + groupSize));
			index += groupSize;
		}
		stream.endBlock();

		extent.writeMdl(stream);

		for (final MdlxExtent sequenceExtent : sequenceExtents) {
			stream.startBlock(MdlUtils.TOKEN_ANIM);
			sequenceExtent.writeMdl(stream);
			stream.endBlock();
		}

		stream.writeAttribUInt32(MdlUtils.TOKEN_MATERIAL_ID, materialId);
		stream.writeAttribUInt32(MdlUtils.TOKEN_SELECTION_GROUP, selectionGroup);
		if (selectionFlags == 4) {
			stream.writeFlag(MdlUtils.TOKEN_UNSELECTABLE);
		}

		if (800 < version) {
			stream.writeAttrib(MdlUtils.TOKEN_LEVELOFDETAIL, lod);
	  
			if (lodName.length() > 0) {
			  stream.writeStringAttrib(MdlUtils.TOKEN_NAME, lodName);
			}
		  }

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = 120
				+ (vertices.length * 4L)
				+ (normals.length * 4L)
				+ (faceTypeGroups.length * 4L)
				+ (faceGroups.length * 4L)
				+ (faces.length * 2L)
				+ vertexGroups.length
				+ (matrixGroups.length * 4L)
				+ (matrixIndices.length * 4L)
				+ (sequenceExtents.size() * 28L);
		for (final float[] uvSet : uvSets) {
			size += 8 + (uvSet.length * 4L);
		}

		if (800 < version) {
			size += 84;

			if (tangents != null) {
				size += 8 + tangents.length * 4L;
			}

			if (skin != null) {
				size += 8 + skin.length;
			}
		}

		return size;
	}
}
