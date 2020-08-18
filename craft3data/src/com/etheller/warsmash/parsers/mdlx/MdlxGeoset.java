package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class MdlxGeoset implements MdlxBlock, MdlxChunk {
	private static final War3ID VRTX = War3ID.fromString("VRTX");
	private static final War3ID NRMS = War3ID.fromString("NRMS");
	private static final War3ID PTYP = War3ID.fromString("PTYP");
	private static final War3ID PCNT = War3ID.fromString("PCNT");
	private static final War3ID PVTX = War3ID.fromString("PVTX");
	private static final War3ID GNDX = War3ID.fromString("GNDX");
	private static final War3ID MTGC = War3ID.fromString("MTGC");
	private static final War3ID MATS = War3ID.fromString("MATS");
	private static final War3ID TANG = War3ID.fromString("TANG");
	private static final War3ID SKIN = War3ID.fromString("SKIN");
	private static final War3ID UVAS = War3ID.fromString("UVAS");
	private static final War3ID UVBS = War3ID.fromString("UVBS");
	
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
	/** 
	 * @since 900
	 */
	public int lod = 0;
	/** 
	 * @since 900
	 */
	public String lodName = "";
	public MdlxExtent extent = new MdlxExtent();
	public MdlxExtent[] sequenceExtents;
	/** 
	 * @since 900
	 */
	public float[] tangents;
	/** 
	 * @since 900
	 */
	public short[] skin;
	public float[][] uvSets;

	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		final long size = reader.readUInt32();

		reader.readInt32(); // skip VRTX
		this.vertices = reader.readFloat32Array(reader.readInt32() * 3);
		reader.readInt32(); // skip NRMS
		this.normals = reader.readFloat32Array(reader.readInt32() * 3);
		reader.readInt32(); // skip PTYP
		this.faceTypeGroups = reader.readUInt32Array(reader.readInt32());
		reader.readInt32(); // skip PCNT
		this.faceGroups = reader.readUInt32Array(reader.readInt32());
		reader.readInt32(); // skip PVTX
		this.faces = reader.readUInt16Array(reader.readInt32());
		reader.readInt32(); // skip GNDX
		this.vertexGroups = reader.readUInt8Array(reader.readInt32());
		reader.readInt32(); // skip MTGC
		this.matrixGroups = reader.readUInt32Array(reader.readInt32());
		reader.readInt32(); // skip MATS
		this.matrixIndices = reader.readUInt32Array(reader.readInt32());
		this.materialId = reader.readUInt32();
		this.selectionGroup = reader.readUInt32();
		this.selectionFlags = reader.readUInt32();

		if (version > 800) {
			lod = reader.readInt32();
			lodName = reader.read(80);
		}

		this.extent.readMdx(reader);

		final long numExtents = reader.readUInt32();
		this.sequenceExtents = new MdlxExtent[(int) numExtents];
		for (int i = 0; i < numExtents; i++) {
			final MdlxExtent extent = new MdlxExtent();
			extent.readMdx(reader);
			this.sequenceExtents[i] = extent;
		}

		int id = reader.readTag(); // TANG or SKIN or UVAS

		if (version > 800 && id != UVAS.getValue()) {
			if (id == TANG.getValue()) {
				this.tangents = reader.readFloat32Array(reader.readInt32() * 4);

				id = reader.readTag(); // SKIN or UVAS
			}

			if (id == SKIN.getValue()) {
				this.skin = reader.readUInt8Array(reader.readInt32());

				id = reader.readInt32(); // UVAS
			}
		}

		final long numUVLayers = reader.readUInt32();
		this.uvSets = new float[(int) numUVLayers][];
		for (int i = 0; i < numUVLayers; i++) {
			reader.readInt32(); // skip UVBS
			this.uvSets[i] = reader.readFloat32Array(reader.readInt32() * 2);
		}
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, this.getByteLength(version));
		ParseUtils.writeWar3ID(stream, VRTX);
		ParseUtils.writeUInt32(stream, this.vertices.length / 3);
		ParseUtils.writeFloatArray(stream, this.vertices);
		ParseUtils.writeWar3ID(stream, NRMS);
		ParseUtils.writeUInt32(stream, this.normals.length / 3);
		ParseUtils.writeFloatArray(stream, this.normals);
		ParseUtils.writeWar3ID(stream, PTYP);
		ParseUtils.writeUInt32(stream, this.faceTypeGroups.length);
		ParseUtils.writeUInt32Array(stream, this.faceTypeGroups);
		ParseUtils.writeWar3ID(stream, PCNT);
		ParseUtils.writeUInt32(stream, this.faceGroups.length);
		ParseUtils.writeUInt32Array(stream, this.faceGroups);
		ParseUtils.writeWar3ID(stream, PVTX);
		ParseUtils.writeUInt32(stream, this.faces.length);
		ParseUtils.writeUInt16Array(stream, this.faces);
		ParseUtils.writeWar3ID(stream, GNDX);
		ParseUtils.writeUInt32(stream, this.vertexGroups.length);
		ParseUtils.writeUInt8Array(stream, this.vertexGroups);
		ParseUtils.writeWar3ID(stream, MTGC);
		ParseUtils.writeUInt32(stream, this.matrixGroups.length);
		ParseUtils.writeUInt32Array(stream, this.matrixGroups);
		ParseUtils.writeWar3ID(stream, MATS);
		ParseUtils.writeUInt32(stream, this.matrixIndices.length);
		ParseUtils.writeUInt32Array(stream, this.matrixIndices);
		ParseUtils.writeUInt32(stream, this.materialId);
		ParseUtils.writeUInt32(stream, this.selectionGroup);
		ParseUtils.writeUInt32(stream, this.selectionFlags);

		if (version > 800) {
			stream.writeInt(lod);
			ParseUtils.writeString(stream, lodName, 80);
		}

		this.extent.writeMdx(stream);
		ParseUtils.writeUInt32(stream, this.sequenceExtents.length);

		for (final MdlxExtent sequenceExtent : this.sequenceExtents) {
			sequenceExtent.writeMdx(stream);
		}

		if (version > 800) {
			if (tangents != null) {
				ParseUtils.writeWar3ID(stream, TANG);
				ParseUtils.writeUInt32(stream, tangents.length / 4);
				ParseUtils.writeFloatArray(stream, tangents);
			}

			if (skin != null) {
				ParseUtils.writeWar3ID(stream, SKIN);
				ParseUtils.writeUInt32(stream, skin.length);
				ParseUtils.writeUInt8Array(stream, skin);
			}
		}

		ParseUtils.writeWar3ID(stream, UVAS);
		ParseUtils.writeUInt32(stream, this.uvSets.length);
		for (final float[] uvSet : this.uvSets) {
			ParseUtils.writeWar3ID(stream, UVBS);
			ParseUtils.writeUInt32(stream, uvSet.length / 2);
			ParseUtils.writeFloatArray(stream, uvSet);
		}
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		this.uvSets = new float[0][];
		final List<MdlxExtent> sequenceExtents = new ArrayList<>();
		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_VERTICES:
				this.vertices = stream.readVectorArray(new float[stream.readInt() * 3], 3);
				System.out.println(vertices.length);
				break;
			case MdlUtils.TOKEN_NORMALS:
				this.normals = stream.readVectorArray(new float[stream.readInt() * 3], 3);
				break;
			case MdlUtils.TOKEN_TVERTICES:
				this.uvSets = Arrays.copyOf(this.uvSets, this.uvSets.length + 1);
				this.uvSets[this.uvSets.length - 1] = stream.readVectorArray(new float[stream.readInt() * 2], 2);
				break;
			case MdlUtils.TOKEN_VERTEX_GROUP: {
				// Vertex groups are stored in a block with no count, can't allocate the buffer
				// yet.
				final List<Short> vertexGroups = new ArrayList<>();
				for (final String vertexGroup : stream.readBlock()) {
					vertexGroups.add(Short.valueOf(vertexGroup));
				}

				this.vertexGroups = new short[vertexGroups.size()];
				int i = 0;
				for (final Short vertexGroup : vertexGroups) {
					this.vertexGroups[i++] = vertexGroup.shortValue();
				}
			}
				break;
			case "Tangents":
				int tansCount = (int)stream.readUInt32();

				tangents = new float[tansCount * 4];

				stream.readVectorArray(tangents, 4);

				break;
			case "SkinWeights":
				int skinCount = (int)stream.readUInt32();

				skin = new short[skinCount * 8];

				stream.readUInt8Array(skin);

				break;
			case MdlUtils.TOKEN_FACES:
				// For now hardcoded for triangles, until I see a model with something
				// different.
				this.faceTypeGroups = new long[] { 4L };

				stream.readInt(); // number of groups

				final int count = stream.readInt();

				stream.read(); // {
				stream.read(); // Triangles
				stream.read(); // {

				this.faces = stream.readUInt16Array(new int[count]);
				this.faceGroups = new long[] { count };

				stream.read(); // }
				stream.read(); // }
				break;
			case MdlUtils.TOKEN_GROUPS: {
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

				this.matrixIndices = new long[indices.size()];
				int i = 0;
				for (final Integer index : indices) {
					this.matrixIndices[i++] = index.intValue();
				}
				this.matrixGroups = new long[groups.size()];
				i = 0;
				for (final Integer group : groups) {
					this.matrixGroups[i++] = group.intValue();
				}
			}
				break;
			case MdlUtils.TOKEN_MINIMUM_EXTENT:
				stream.readFloatArray(this.extent.min);
				break;
			case MdlUtils.TOKEN_MAXIMUM_EXTENT:
				stream.readFloatArray(this.extent.max);
				break;
			case MdlUtils.TOKEN_BOUNDSRADIUS:
				this.extent.boundsRadius = stream.readFloat();
				break;
			case MdlUtils.TOKEN_ANIM:
				final MdlxExtent extent = new MdlxExtent();

				for (final String subToken : stream.readBlock()) {
					switch (subToken) {
					case MdlUtils.TOKEN_MINIMUM_EXTENT:
						stream.readFloatArray(extent.min);
						break;
					case MdlUtils.TOKEN_MAXIMUM_EXTENT:
						stream.readFloatArray(extent.max);
						break;
					case MdlUtils.TOKEN_BOUNDSRADIUS:
						extent.boundsRadius = stream.readFloat();
						break;
					}
				}

				sequenceExtents.add(extent);
				break;
			case MdlUtils.TOKEN_MATERIAL_ID:
				this.materialId = stream.readInt();
				break;
			case MdlUtils.TOKEN_SELECTION_GROUP:
				this.selectionGroup = stream.readInt();
				break;
			case MdlUtils.TOKEN_UNSELECTABLE:
				this.selectionFlags = 4;
				break;
			case "LevelOfDetail":
				lod = stream.readInt();
				break;
			case "Name":
				lodName = stream.read();
				break;
			default:
				throw new RuntimeException("Unknown token in Geoset: " + token);
			}
		}
		this.sequenceExtents = sequenceExtents.toArray(new MdlxExtent[sequenceExtents.size()]);
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_GEOSET);

		stream.writeVectorArray(MdlUtils.TOKEN_VERTICES, this.vertices, 3);
		stream.writeVectorArray(MdlUtils.TOKEN_NORMALS, this.normals, 3);

		for (final float[] uvSet : this.uvSets) {
			stream.writeVectorArray(MdlUtils.TOKEN_TVERTICES, uvSet, 2);
		}

		stream.startBlock(MdlUtils.TOKEN_VERTEX_GROUP);
		for (int i = 0; i < this.vertexGroups.length; i++) {
			stream.writeLine(this.vertexGroups[i] + ",");
		}
		stream.endBlock();

		if (version > 800) {
			if (tangents != null) {
				stream.startBlock("Tangents", tangents.length / 4);

				for (int i = 0, l = tangents.length; i < l; i += 4) {
					stream.writeFloatArray(Arrays.copyOfRange(tangents, i, i + 4));
				}

				stream.endBlock();
			}

			if (skin != null) {
				stream.startBlock("SkinWeights");

				for (int i = 0, l = skin.length; i < l; i += 8) {
					stream.writeShortArray(Arrays.copyOfRange(skin, i, i + 8));
				}

				stream.endBlock();
			}
		}

		// For now hardcoded for triangles, until I see a model with something
		// different.
		stream.startBlock(MdlUtils.TOKEN_FACES, 1, this.faces.length);
		stream.startBlock(MdlUtils.TOKEN_TRIANGLES);
		final StringBuffer facesBuffer = new StringBuffer();
		for (final int faceValue : this.faces) {
			if (facesBuffer.length() > 0) {
				facesBuffer.append(", ");
			}
			facesBuffer.append(faceValue);
		}
		stream.writeLine("{ " + facesBuffer.toString() + " },");
		stream.endBlock();
		stream.endBlock();

		stream.startBlock(MdlUtils.TOKEN_GROUPS, this.matrixGroups.length, this.matrixIndices.length);
		int index = 0;
		for (final long groupSize : this.matrixGroups) {
			stream.writeLongSubArrayAttrib(MdlUtils.TOKEN_MATRICES, this.matrixIndices, index,
					(int) (index + groupSize));
			index += groupSize;
		}
		stream.endBlock();

		this.extent.writeMdl(stream);

		for (final MdlxExtent sequenceExtent : this.sequenceExtents) {
			stream.startBlock(MdlUtils.TOKEN_ANIM);
			sequenceExtent.writeMdl(stream);
			stream.endBlock();
		}

		stream.writeAttribUInt32("MaterialID", this.materialId);
		stream.writeAttribUInt32("SelectionGroup", this.selectionGroup);
		if (this.selectionFlags == 4) {
			stream.writeFlag("Unselectable");
		}

		if (version > 800) {
			stream.writeAttrib("LevelOfDetail", lod);
	  
			if (lodName.length() > 0) {
			  stream.writeStringAttrib("Name", lodName);
			}
		  }

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = 120 + (this.vertices.length * 4) + (this.normals.length * 4) + (this.faceTypeGroups.length * 4)
				+ (this.faceGroups.length * 4) + (this.faces.length * 2) + this.vertexGroups.length
				+ (this.matrixGroups.length * 4) + (this.matrixIndices.length * 4) + (this.sequenceExtents.length * 28);
		for (final float[] uvSet : this.uvSets) {
			size += 8 + (uvSet.length * 4);
		}

		if (version > 800) {
			size += 84;

			if (tangents != null) {
				size += 8 + tangents.length * 4;
			}

			if (skin != null) {
				size += 8 + skin.length;
			}
		}

		return size;
	}
}
