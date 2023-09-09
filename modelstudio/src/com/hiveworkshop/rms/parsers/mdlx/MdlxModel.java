package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A Warcraft 3 model. Supports loading from and saving to both the binary MDX
 * and text MDL file formats.
 */
public class MdlxModel {
	// Below, these can't call a function on a string to make their value
	// because
	// switch/case statements require the value to be compile-time defined in
	// order
	// to be legal, and it appears to only allow basic binary operators for
	// that.
	// I would love a clearer way to just type 'MDLX' in a character constant in
	// Java for this
	private static final int MDLX = ('M' << 24) | ('D' << 16) | ('L' << 8) | ('X');// War3ID.fromString("MDLX").getValue();
	private static final int VERS = ('V' << 24) | ('E' << 16) | ('R' << 8) | ('S');// War3ID.fromString("VERS").getValue();
	private static final int MODL = ('M' << 24) | ('O' << 16) | ('D' << 8) | ('L');// War3ID.fromString("MODL").getValue();
	private static final int SEQS = ('S' << 24) | ('E' << 16) | ('Q' << 8) | ('S');// War3ID.fromString("SEQS").getValue();
	private static final int GLBS = ('G' << 24) | ('L' << 16) | ('B' << 8) | ('S');// War3ID.fromString("GLBS").getValue();
	private static final int MTLS = ('M' << 24) | ('T' << 16) | ('L' << 8) | ('S');// War3ID.fromString("MTLS").getValue();
	private static final int TEXS = ('T' << 24) | ('E' << 16) | ('X' << 8) | ('S');// War3ID.fromString("TEXS").getValue();
	private static final int TXAN = ('T' << 24) | ('X' << 16) | ('A' << 8) | ('N');// War3ID.fromString("TXAN").getValue();
	private static final int GEOS = ('G' << 24) | ('E' << 16) | ('O' << 8) | ('S');// War3ID.fromString("GEOS").getValue();
	private static final int GEOA = ('G' << 24) | ('E' << 16) | ('O' << 8) | ('A');// War3ID.fromString("GEOA").getValue();
	private static final int BONE = ('B' << 24) | ('O' << 16) | ('N' << 8) | ('E');// War3ID.fromString("BONE").getValue();
	private static final int LITE = ('L' << 24) | ('I' << 16) | ('T' << 8) | ('E');// War3ID.fromString("LITE").getValue();
	private static final int HELP = ('H' << 24) | ('E' << 16) | ('L' << 8) | ('P');// War3ID.fromString("HELP").getValue();
	private static final int ATCH = ('A' << 24) | ('T' << 16) | ('C' << 8) | ('H');// War3ID.fromString("ATCH").getValue();
	private static final int PIVT = ('P' << 24) | ('I' << 16) | ('V' << 8) | ('T');// War3ID.fromString("PIVT").getValue();
	private static final int PREM = ('P' << 24) | ('R' << 16) | ('E' << 8) | ('M');// War3ID.fromString("PREM").getValue();
	private static final int PRE2 = ('P' << 24) | ('R' << 16) | ('E' << 8) | ('2');// War3ID.fromString("PRE2").getValue();
	private static final int CORN = ('C' << 24) | ('O' << 16) | ('R' << 8) | ('N');// War3ID.fromString("CORN").getValue();
	private static final int RIBB = ('R' << 24) | ('I' << 16) | ('B' << 8) | ('B');// War3ID.fromString("RIBB").getValue();
	private static final int CAMS = ('C' << 24) | ('A' << 16) | ('M' << 8) | ('S');// War3ID.fromString("CAMS").getValue();
	private static final int EVTS = ('E' << 24) | ('V' << 16) | ('T' << 8) | ('S');// War3ID.fromString("EVTS").getValue();
	private static final int CLID = ('C' << 24) | ('L' << 16) | ('I' << 8) | ('D');// War3ID.fromString("CLID").getValue();
	private static final int FAFX = ('F' << 24) | ('A' << 16) | ('F' << 8) | ('X');// War3ID.fromString("FAFX").getValue();
	private static final int BPOS = ('B' << 24) | ('P' << 16) | ('O' << 8) | ('S');// War3ID.fromString("BPOS").getValue();
	
	public int version = 800;
	public String name = "";
	/**
	 * (Comment copied from Ghostwolf JS) To the best of my knowledge, this should
	 * always be left empty. This is probably a leftover from the Warcraft 3 beta.
	 * (WS game note: No, I never saw any animation files in the RoC 2001-2002 Beta.
	 * So it must be from the Alpha)
	 *
	 * @member {string}
	 */
	public String animationFile = "";
	public MdlxExtent extent = new MdlxExtent();
	public long blendTime = 0;
	public byte flags = 0;
	public List<MdlxSequence> sequences = new ArrayList<>();
	public List<Long /* UInt32 */> globalSequences = new ArrayList<>();
	public List<MdlxMaterial> materials = new ArrayList<>();
	public List<MdlxTexture> textures = new ArrayList<>();
	public List<MdlxTextureAnimation> textureAnimations = new ArrayList<>();
	public List<MdlxGeoset> geosets = new ArrayList<>();
	public List<MdlxGeosetAnimation> geosetAnimations = new ArrayList<>();
	public List<MdlxBone> bones = new ArrayList<>();
	public List<MdlxLight> lights = new ArrayList<>();
	public List<MdlxHelper> helpers = new ArrayList<>();
	public List<MdlxAttachment> attachments = new ArrayList<>();
	public List<float[]> pivotPoints = new ArrayList<>();
	public List<MdlxParticleEmitter> particleEmitters = new ArrayList<>();
	public List<MdlxParticleEmitter2> particleEmitters2 = new ArrayList<>();
	public List<MdlxParticleEmitterPopcorn> particleEmittersPopcorn = new ArrayList<>();
	public List<MdlxRibbonEmitter> ribbonEmitters = new ArrayList<>();
	public List<MdlxCamera> cameras = new ArrayList<>();
	public List<MdlxEventObject> eventObjects = new ArrayList<>();
	public List<MdlxCollisionShape> collisionShapes = new ArrayList<>();
	/**
	 * @since 900
	 */
	public List<MdlxFaceEffect> faceEffects = new ArrayList<>();
	/**
	 * @since 900
	 */
	public List<float[]> bindPose = new ArrayList<>();
	public List<MdlxUnknownChunk> unknownChunks = new ArrayList<>();

	public MdlxModel() {

	}

	public MdlxModel(final ByteBuffer buffer) {
		load(buffer);
	}

	public void load(final ByteBuffer buffer) {
		// MDX files start with "MDLX".
		if (buffer.get(0) == 77 && buffer.get(1) == 68 && buffer.get(2) == 76 && buffer.get(3) == 88) {
			loadMdx(buffer);
		} else {
			loadMdl(buffer);
		}
	}

	public static String convertInt(int tag) {
		return "" + (char)((tag>>24)&0xFF) + (char)((tag>>16)&0xFF) + (char)((tag>>8)&0xFF) + (char)((tag>>0)&0xFF);
	}

	public static String convertInt2(int tag) {
		return "" + (char)((tag>>0)&0xFF) + (char)((tag>>8)&0xFF) + (char)((tag>>16)&0xFF) + (char)((tag>>24)&0xFF);
	}

	public void loadMdx(final ByteBuffer buffer) {
		final BinaryReader reader = new BinaryReader(buffer);

		if (reader.readTag() != MDLX) {
			throw new IllegalStateException("WrongMagicNumber");
		}

		while (reader.remaining() > 0) {
			final int tag = reader.readTag();
			final int size = reader.readInt32();
			System.out.println("about to load tag : " + convertInt(tag));
			System.out.println("size: " + size);
			switch (tag) {
			case VERS:
				loadVersionChunk(reader);
				break;
			case MODL:
				loadModelChunk(reader);
				break;
			case SEQS: {
				int elementCount = version == 1300 ? (int) reader.readUInt32() : (size / 132);
				loadStaticObjects(sequences, MdlxBlockDescriptor.SEQUENCE, reader, elementCount);
				break;
			}
			case GLBS:
				loadGlobalSequenceChunk(reader, size);
				break;
			case MTLS: {
				if(version == 1300) {
					long numMaterials = reader.readUInt32();
					System.out.println("MTLS num sequences is: " + numMaterials);
					System.out.println("MTLS extra data is: " + reader.readUInt32());
					loadNDynamicObjects(materials, MdlxBlockDescriptor.MATERIAL, reader, numMaterials);
				} else {
					loadDynamicObjects(materials, MdlxBlockDescriptor.MATERIAL, reader, size);
				}
				break;
			}
			case TEXS:
				loadStaticObjects(textures, MdlxBlockDescriptor.TEXTURE, reader, size / 268);
				break;
			case TXAN:
				loadDynamicObjects(textureAnimations, MdlxBlockDescriptor.TEXTURE_ANIMATION, reader, size);
				break;
			case GEOS:
				if(version == 1300) {
					long numGeos = reader.readUInt32();
					System.out.println("GEOS num is: " + numGeos);
					loadNDynamicObjects(geosets, MdlxBlockDescriptor.GEOSET, reader, numGeos);
				} else {
					loadDynamicObjects(geosets, MdlxBlockDescriptor.GEOSET, reader, size);
				}
				break;
			case GEOA:
				if(version == 1300) {
					long numGeoAs = reader.readUInt32();
					loadNDynamicObjects(geosetAnimations, MdlxBlockDescriptor.GEOSET_ANIMATION, reader, numGeoAs);
				} else {
					loadDynamicObjects(geosetAnimations, MdlxBlockDescriptor.GEOSET_ANIMATION, reader, size);
				}
				break;
			case BONE:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("BONE count is: " + count);
					loadNDynamicObjects(bones, MdlxBlockDescriptor.BONE, reader, count);
				} else {
					loadDynamicObjects(bones, MdlxBlockDescriptor.BONE, reader, size);
				}
				break;
			case LITE:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("BONE count is: " + count);
					loadNDynamicObjects(lights, MdlxBlockDescriptor.LIGHT, reader, count);
				} else {
					loadDynamicObjects(lights, MdlxBlockDescriptor.LIGHT, reader, size);
				}
				break;
			case HELP:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("HELP count is: " + count);
					loadNDynamicObjects(helpers, MdlxBlockDescriptor.HELPER, reader, count);
				} else {
					loadDynamicObjects(helpers, MdlxBlockDescriptor.HELPER, reader, size);
				}
				break;
			case ATCH:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("ATCH count is: " + count);
					long unused = reader.readUInt32();
					System.out.println("ATCH unused is: " + unused);
					loadNDynamicObjects(attachments, MdlxBlockDescriptor.ATTACHMENT, reader, count);
				} else {
					loadDynamicObjects(attachments, MdlxBlockDescriptor.ATTACHMENT, reader, size);
				}
				break;
			case PIVT:
				loadPivotPointChunk(reader, size);
				break;
			case PREM:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("PREM count is: " + count);
					loadNDynamicObjects(particleEmitters, MdlxBlockDescriptor.PARTICLE_EMITTER, reader, count);
				} else {
					loadDynamicObjects(particleEmitters, MdlxBlockDescriptor.PARTICLE_EMITTER, reader, size);
				}
				break;
			case PRE2:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("PRE2 count is: " + count);
					loadNDynamicObjects(particleEmitters2, MdlxBlockDescriptor.PARTICLE_EMITTER2, reader, count);
				} else {
					loadDynamicObjects(particleEmitters2, MdlxBlockDescriptor.PARTICLE_EMITTER2, reader, size);
				}
				break;
			case CORN:
				loadDynamicObjects(particleEmittersPopcorn, MdlxBlockDescriptor.PARTICLE_EMITTER_POPCORN, reader, size);
				break;
			case RIBB:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("RIBB count is: " + count);
					loadNDynamicObjects(ribbonEmitters, MdlxBlockDescriptor.RIBBON_EMITTER, reader, count);
				} else {
					loadDynamicObjects(ribbonEmitters, MdlxBlockDescriptor.RIBBON_EMITTER, reader, size);
				}
				break;
			case CAMS:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("CAMS count is: " + count);
					loadNDynamicObjects(cameras, MdlxBlockDescriptor.CAMERA, reader, count);
				} else {
					loadDynamicObjects(cameras, MdlxBlockDescriptor.CAMERA, reader, size);
				}
				break;
			case EVTS:
				if(version == 1300) {
					long count = reader.readUInt32();
					System.out.println("EVTS count is: " + count);
					loadNDynamicObjects(eventObjects, MdlxBlockDescriptor.EVENT_OBJECT, reader, count);
				} else {
					loadDynamicObjects(eventObjects, MdlxBlockDescriptor.EVENT_OBJECT, reader, size);
				}
				break;
			case CLID:
				if(version == 1300) {
					//long count = reader.readUInt32();
					//System.out.println("CLID count is: " + count);
					//loadNDynamicObjects(collisionShapes, MdlxBlockDescriptor.COLLISION_SHAPE, reader, count);
					unknownChunks.add(new MdlxUnknownChunk(reader, size, new War3ID(tag)));
				} else {
					loadDynamicObjects(collisionShapes, MdlxBlockDescriptor.COLLISION_SHAPE, reader, size);
				}
				break;
			case FAFX:
				loadStaticObjects(faceEffects, MdlxBlockDescriptor.FACE_EFFECT, reader, size / 340);
				break;
			case BPOS:
				loadBindPoseChunk(reader, size);
				break;
			default:
				unknownChunks.add(new MdlxUnknownChunk(reader, size, new War3ID(tag)));
			}
		}
	}

	private void loadVersionChunk(final BinaryReader reader) {
		version = reader.readInt32();
		System.out.println("version: " + version);
	}

	private void loadModelChunk(final BinaryReader reader) {
		name = reader.read(80);
		animationFile = reader.read(260);
		extent.readMdx(reader);
		blendTime = reader.readInt32();
		if(version==1300) {
			flags = reader.readInt8();
		}
		System.out.println("name: " + name);
		System.out.println("animationFile: " + animationFile);
		System.out.println("extent: " + extent);
		System.out.println("blendTime: " + blendTime);
		System.out.println("flags: " + flags);
	}

	private <E extends MdlxBlock> void loadStaticObjects(final List<E> out, final MdlxBlockDescriptor<E> constructor,
			final BinaryReader reader, final long count) {
		for (int i = 0; i < count; i++) {
			final E object = constructor.create();

			object.readMdx(reader, version);

			out.add(object);
		}
	}

	private void loadGlobalSequenceChunk(final BinaryReader reader, final long size) {
		for (long i = 0, l = size / 4; i < l; i++) {
			globalSequences.add(reader.readUInt32());
		}
	}

	private <E extends MdlxBlock & MdlxChunk> void loadDynamicObjects(final List<E> out,
																	  final MdlxBlockDescriptor<E> constructor, final BinaryReader reader, final long size) {
		long totalSize = 0;
		while (totalSize < size) {
			final E object = constructor.create();

			object.readMdx(reader, version);

			totalSize += object.getByteLength(version);

			out.add(object);
		}
	}

	private <E extends MdlxBlock & MdlxChunk> void loadNDynamicObjects(final List<E> out,
																	  final MdlxBlockDescriptor<E> constructor, final BinaryReader reader, final long count) {

		while (out.size() < count) {
			final E object = constructor.create();

			object.readMdx(reader, version);

			out.add(object);
		}
	}

	private void loadPivotPointChunk(final BinaryReader reader, final long size) {
		for (long i = 0, l = size / 12; i < l; i++) {
			pivotPoints.add(reader.readFloat32Array(3));
		}
	}

	private void loadBindPoseChunk(final BinaryReader reader, final long size) {
		for (int i = 0, l = reader.readInt32(); i < l; i++) {
			bindPose.add(reader.readFloat32Array(12));
		}
	}

	public ByteBuffer saveMdx() {
		final BinaryWriter writer = new BinaryWriter(getByteLength());

		writer.writeTag(MDLX);
		saveVersionChunk(writer);
		saveModelChunk(writer);
		saveStaticObjectChunk(writer, SEQS, sequences, 132);
		saveGlobalSequenceChunk(writer);
		saveDynamicObjectChunk(writer, MTLS, materials);
		saveStaticObjectChunk(writer, TEXS, textures, 268);
		saveDynamicObjectChunk(writer, TXAN, textureAnimations);
		saveDynamicObjectChunk(writer, GEOS, geosets);
		saveDynamicObjectChunk(writer, GEOA, geosetAnimations);
		saveDynamicObjectChunk(writer, BONE, bones);
		saveDynamicObjectChunk(writer, LITE, lights);
		saveDynamicObjectChunk(writer, HELP, helpers);
		saveDynamicObjectChunk(writer, ATCH, attachments);
		savePivotPointChunk(writer);
		saveDynamicObjectChunk(writer, PREM, particleEmitters);
		saveDynamicObjectChunk(writer, PRE2, particleEmitters2);

		if (version > 800) {
			saveDynamicObjectChunk(writer, CORN, particleEmittersPopcorn);
		}

		saveDynamicObjectChunk(writer, RIBB, ribbonEmitters);
		saveDynamicObjectChunk(writer, CAMS, cameras);
		saveDynamicObjectChunk(writer, EVTS, eventObjects);
		saveDynamicObjectChunk(writer, CLID, collisionShapes);

		if (version > 800) {
			saveStaticObjectChunk(writer, FAFX, faceEffects, 340);
			saveBindPoseChunk(writer);
		}

		for (final MdlxUnknownChunk chunk : unknownChunks) {
			chunk.writeMdx(writer, version);
		}

		return writer.buffer;
	}

	private void saveVersionChunk(final BinaryWriter writer) {
		writer.writeTag(VERS);
		writer.writeUInt32(4);
		writer.writeUInt32(version);
	}

	private void saveModelChunk(final BinaryWriter writer) {
		writer.writeTag(MODL);
		writer.writeUInt32(372);
		writer.writeWithNulls(name, 80);
		writer.writeWithNulls(animationFile, 260);
		extent.writeMdx(writer);
		writer.writeUInt32(blendTime);
	}

	private <E extends MdlxBlock> void saveStaticObjectChunk(final BinaryWriter writer, final int name,
			final List<E> objects, final long size) {
		if (!objects.isEmpty()) {
			writer.writeTag(name);
			writer.writeUInt32(objects.size() * size);

			for (final E object : objects) {
				object.writeMdx(writer, version);
			}
		}
	}

	private void saveGlobalSequenceChunk(final BinaryWriter writer) {
		if (!globalSequences.isEmpty()) {
			writer.writeTag(GLBS);
			writer.writeUInt32(globalSequences.size() * 4);

			for (final Long globalSequence : globalSequences) {
				writer.writeUInt32(globalSequence);
			}
		}
	}

	private <E extends MdlxBlock & MdlxChunk> void saveDynamicObjectChunk(final BinaryWriter writer,
			final int name, final List<E> objects) {
		if (!objects.isEmpty()) {
			writer.writeTag(name);
			writer.writeUInt32(getObjectsByteLength(objects));

			for (final E object : objects) {
				object.writeMdx(writer, version);
			}
		}
	}

	private void savePivotPointChunk(final BinaryWriter writer) {
		if (pivotPoints.size() > 0) {
			writer.writeTag(PIVT);
			writer.writeUInt32(pivotPoints.size() * 12);

			for (final float[] pivotPoint : pivotPoints) {
				writer.writeFloat32Array(pivotPoint);
			}
		}
	}

	private void saveBindPoseChunk(final BinaryWriter writer) {
		if (bindPose.size() > 0) {
			writer.writeTag(BPOS);
			writer.writeUInt32(4 + bindPose.size() * 48);
			writer.writeUInt32(bindPose.size());

			for (final float[] matrix : bindPose) {
				writer.writeFloat32Array(matrix);
			}
		}
	}

	public void loadMdl(final ByteBuffer buffer) {
		String token;
		final MdlTokenInputStream stream = new MdlTokenInputStream(buffer);

		while ((token = stream.read()) != null) {
			switch (token) {
			case MdlUtils.TOKEN_VERSION:
				loadVersionBlock(stream);
				break;
			case MdlUtils.TOKEN_MODEL:
				loadModelBlock(stream);
				break;
			case MdlUtils.TOKEN_SEQUENCES:
				loadNumberedObjectBlock(sequences, MdlxBlockDescriptor.SEQUENCE, MdlUtils.TOKEN_ANIM, stream);
				break;
			case MdlUtils.TOKEN_GLOBAL_SEQUENCES:
				loadGlobalSequenceBlock(stream);
				break;
			case MdlUtils.TOKEN_TEXTURES:
				loadNumberedObjectBlock(textures, MdlxBlockDescriptor.TEXTURE, MdlUtils.TOKEN_BITMAP, stream);
				break;
			case MdlUtils.TOKEN_MATERIALS:
				loadNumberedObjectBlock(materials, MdlxBlockDescriptor.MATERIAL, MdlUtils.TOKEN_MATERIAL,
						stream);
				break;
			case MdlUtils.TOKEN_TEXTURE_ANIMS:
				loadNumberedObjectBlock(textureAnimations, MdlxBlockDescriptor.TEXTURE_ANIMATION,
						MdlUtils.TOKEN_TEXTURE_ANIM, stream);
				break;
			case MdlUtils.TOKEN_GEOSET:
				loadObject(geosets, MdlxBlockDescriptor.GEOSET, stream);
				break;
			case MdlUtils.TOKEN_GEOSETANIM:
				loadObject(geosetAnimations, MdlxBlockDescriptor.GEOSET_ANIMATION, stream);
				break;
			case MdlUtils.TOKEN_BONE:
				loadObject(bones, MdlxBlockDescriptor.BONE, stream);
				break;
			case MdlUtils.TOKEN_LIGHT:
				loadObject(lights, MdlxBlockDescriptor.LIGHT, stream);
				break;
			case MdlUtils.TOKEN_HELPER:
				loadObject(helpers, MdlxBlockDescriptor.HELPER, stream);
				break;
			case MdlUtils.TOKEN_ATTACHMENT:
				loadObject(attachments, MdlxBlockDescriptor.ATTACHMENT, stream);
				break;
			case MdlUtils.TOKEN_PIVOT_POINTS:
				loadPivotPointBlock(stream);
				break;
			case MdlUtils.TOKEN_PARTICLE_EMITTER:
				loadObject(particleEmitters, MdlxBlockDescriptor.PARTICLE_EMITTER, stream);
				break;
			case MdlUtils.TOKEN_PARTICLE_EMITTER2:
				loadObject(particleEmitters2, MdlxBlockDescriptor.PARTICLE_EMITTER2, stream);
				break;
			case "ParticleEmitterPopcorn":
				loadObject(particleEmittersPopcorn, MdlxBlockDescriptor.PARTICLE_EMITTER_POPCORN, stream);
				break;
			case MdlUtils.TOKEN_RIBBON_EMITTER:
				loadObject(ribbonEmitters, MdlxBlockDescriptor.RIBBON_EMITTER, stream);
				break;
			case MdlUtils.TOKEN_CAMERA:
				loadObject(cameras, MdlxBlockDescriptor.CAMERA, stream);
				break;
			case MdlUtils.TOKEN_EVENT_OBJECT:
				loadObject(eventObjects, MdlxBlockDescriptor.EVENT_OBJECT, stream);
				break;
			case MdlUtils.TOKEN_COLLISION_SHAPE:
				loadObject(collisionShapes, MdlxBlockDescriptor.COLLISION_SHAPE, stream);
				break;
			case "FaceFX":
				loadObject(faceEffects, MdlxBlockDescriptor.FACE_EFFECT, stream);
				break;
			case "BindPose":
				loadBindPoseBlock(stream);
				break;
			default:
				throw new IllegalStateException("Unsupported block: " + token);
			}
		}
	}

	private void loadVersionBlock(final MdlTokenInputStream stream) {
		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_FORMAT_VERSION:
				version = stream.readInt();
				break;
			default:
				throw new IllegalStateException("Unknown token in Version: " + token);
			}
		}
	}

	private void loadModelBlock(final MdlTokenInputStream stream) {
		name = stream.read();
		for (final String token : stream.readBlock()) {
			if (token.startsWith("Num")) {
				/*-
				 * Don't care about the number of things, the arrays will grow as they wish.
				 * This includes:
				 *      NumGeosets
				 *      NumGeosetAnims
				 *      NumHelpers
				 *      NumLights
				 *      NumBones
				 *      NumAttachments
				 *      NumParticleEmitters
				 *      NumParticleEmitters2
				 *      NumRibbonEmitters
				 *      NumEvents
				 */
				stream.read();
			} else {
				switch (token) {
				case MdlUtils.TOKEN_BLEND_TIME:
					blendTime = stream.readUInt32();
					break;
				case MdlUtils.TOKEN_MINIMUM_EXTENT:
					stream.readFloatArray(extent.min);
					break;
				case MdlUtils.TOKEN_MAXIMUM_EXTENT:
					stream.readFloatArray(extent.max);
					break;
				case MdlUtils.TOKEN_BOUNDSRADIUS:
					extent.boundsRadius = stream.readFloat();
					break;
				default:
					throw new IllegalStateException("Unknown token in Model: " + token);
				}
			}
		}
	}

	private <E extends MdlxBlock> void loadNumberedObjectBlock(final List<E> out,
			final MdlxBlockDescriptor<E> constructor, final String name, final MdlTokenInputStream stream) {
		stream.read(); // Don't care about the number, the array will grow

		for (final String token : stream.readBlock()) {
			if (token.equals(name)) {
				final E object = constructor.create();

				object.readMdl(stream, version);

				out.add(object);
			} else {
				throw new IllegalStateException("Unknown token in " + name + ": " + token);
			}
		}
	}

	private void loadGlobalSequenceBlock(final MdlTokenInputStream stream) {
		stream.read(); // Don't care about the number, the array will grow

		for (final String token : stream.readBlock()) {
			if (token.equals(MdlUtils.TOKEN_DURATION)) {
				globalSequences.add(stream.readUInt32());
			} else {
				throw new IllegalStateException("Unknown token in GlobalSequences: " + token);
			}
		}
	}

	private <E extends MdlxBlock> void loadObject(final List<E> out, final MdlxBlockDescriptor<E> descriptor,
			final MdlTokenInputStream stream) {
		final E object = descriptor.create();

		object.readMdl(stream, version);

		out.add(object);
	}

	private void loadPivotPointBlock(final MdlTokenInputStream stream) {
		final int count = stream.readInt();

		stream.read(); // {

		for (int i = 0; i < count; i++) {
			pivotPoints.add(stream.readFloatArray(new float[3]));
		}

		stream.read(); // }
	}
	
	private void loadBindPoseBlock(final MdlTokenInputStream stream) {
		for (final String token : stream.readBlock()) {
			if (token.equals("Matrices")) {
				final int matrices = stream.readInt();

				stream.read(); // {

				for (int i = 0; i < matrices; i++) {
					bindPose.add(stream.readFloatArray(new float[12]));
				}

				stream.read(); // }
			} else {
				throw new IllegalStateException("Unknown token in BindPose: " + token);
			}
		}
	}

	public ByteBuffer saveMdl() {
		final MdlTokenOutputStream stream = new MdlTokenOutputStream();

		saveVersionBlock(stream);
		saveModelBlock(stream);
		saveStaticObjectsBlock(stream, MdlUtils.TOKEN_SEQUENCES, sequences);
		saveGlobalSequenceBlock(stream);
		saveStaticObjectsBlock(stream, MdlUtils.TOKEN_TEXTURES, textures);
		saveStaticObjectsBlock(stream, MdlUtils.TOKEN_MATERIALS, materials);
		saveStaticObjectsBlock(stream, MdlUtils.TOKEN_TEXTURE_ANIMS, textureAnimations);
		saveObjects(stream, geosets);
		saveObjects(stream, geosetAnimations);
		saveObjects(stream, bones);
		saveObjects(stream, lights);
		saveObjects(stream, helpers);
		saveObjects(stream, attachments);
		savePivotPointBlock(stream);
		saveObjects(stream, particleEmitters);
		saveObjects(stream, particleEmitters2);

		if (version > 800) {
			saveObjects(stream, particleEmittersPopcorn);
		}

		saveObjects(stream, ribbonEmitters);
		saveObjects(stream, cameras);
		saveObjects(stream, eventObjects);
		saveObjects(stream, collisionShapes);

		if (version > 800) {
			saveObjects(stream, faceEffects);
			saveBindPoseBlock(stream);
		}

		return ByteBuffer.wrap(stream.buffer.toString().getBytes());
	}

	private void saveVersionBlock(final MdlTokenOutputStream stream) {
		stream.startBlock(MdlUtils.TOKEN_VERSION);
		stream.writeAttrib(MdlUtils.TOKEN_FORMAT_VERSION, version);
		stream.endBlock();
	}

	private void saveModelBlock(final MdlTokenOutputStream stream) {
		stream.startObjectBlock(MdlUtils.TOKEN_MODEL, name);
		stream.writeAttribUInt32(MdlUtils.TOKEN_BLEND_TIME, blendTime);
		extent.writeMdl(stream);
		stream.endBlock();
	}

	private void saveStaticObjectsBlock(final MdlTokenOutputStream stream, final String name,
			final List<? extends MdlxBlock> objects) {
		if (!objects.isEmpty()) {
			stream.startBlock(name, objects.size());

			for (final MdlxBlock object : objects) {
				object.writeMdl(stream, version);
			}

			stream.endBlock();
		}
	}

	private void saveGlobalSequenceBlock(final MdlTokenOutputStream stream) {
		if (!globalSequences.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_GLOBAL_SEQUENCES, globalSequences.size());

			for (final Long globalSequence : globalSequences) {
				stream.writeAttribUInt32(MdlUtils.TOKEN_DURATION, globalSequence);
			}

			stream.endBlock();
		}
	}

	private void saveObjects(final MdlTokenOutputStream stream, final List<? extends MdlxBlock> objects) {
		for (final MdlxBlock object : objects) {
			object.writeMdl(stream, version);
		}
	}

	private void savePivotPointBlock(final MdlTokenOutputStream stream) {
		if (!pivotPoints.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_PIVOT_POINTS, pivotPoints.size());

			for (final float[] pivotPoint : pivotPoints) {
				stream.writeFloatArray(pivotPoint);
			}

			stream.endBlock();
		}
	}

	private void saveBindPoseBlock(final MdlTokenOutputStream stream) {
		if (!bindPose.isEmpty()) {
			stream.startBlock("BindPose");

			stream.startBlock("Matrices", bindPose.size());

			for (final float[] matrix : bindPose) {
				stream.writeFloatArray(matrix);
			}

			stream.endBlock();

			stream.endBlock();
		}
	}

	public int getByteLength() {
		int size = 396;

		size += getStaticObjectsChunkByteLength(sequences, 132);
		size += getStaticObjectsChunkByteLength(globalSequences, 4);
		size += getDynamicObjectsChunkByteLength(materials);
		size += getStaticObjectsChunkByteLength(textures, 268);
		size += getDynamicObjectsChunkByteLength(textureAnimations);
		size += getDynamicObjectsChunkByteLength(geosets);
		size += getDynamicObjectsChunkByteLength(geosetAnimations);
		size += getDynamicObjectsChunkByteLength(bones);
		size += getDynamicObjectsChunkByteLength(lights);
		size += getDynamicObjectsChunkByteLength(helpers);
		size += getDynamicObjectsChunkByteLength(attachments);
		size += getStaticObjectsChunkByteLength(pivotPoints, 12);
		size += getDynamicObjectsChunkByteLength(particleEmitters);
		size += getDynamicObjectsChunkByteLength(particleEmitters2);

		if (version > 800) {
			size += getDynamicObjectsChunkByteLength(particleEmittersPopcorn);
		}

		size += getDynamicObjectsChunkByteLength(ribbonEmitters);
		size += getDynamicObjectsChunkByteLength(cameras);
		size += getDynamicObjectsChunkByteLength(eventObjects);
		size += getDynamicObjectsChunkByteLength(collisionShapes);
		size += getObjectsByteLength(unknownChunks);

		if (version > 800) {
			size += getStaticObjectsChunkByteLength(faceEffects, 340);
			size += getBindPoseChunkByteLength();
		}

		return size;
	}

	private <E extends MdlxChunk> long getObjectsByteLength(final List<E> objects) {
		long size = 0;
		for (final E object : objects) {
			size += object.getByteLength(version);
		}
		return size;
	}

	private <E extends MdlxChunk> long getDynamicObjectsChunkByteLength(final List<E> objects) {
		if (!objects.isEmpty()) {
			return 8 + getObjectsByteLength(objects);
		}

		return 0;
	}

	private <E> long getStaticObjectsChunkByteLength(final List<E> objects, final long size) {
		if (!objects.isEmpty()) {
			return 8 + (objects.size() * size);
		}

		return 0;
	}
	
	private long getBindPoseChunkByteLength() {
		if (bindPose.size() > 0) {
		  	return 12 + bindPose.size() * 48;
		}
	
		return 0;
	}
}
