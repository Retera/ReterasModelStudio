package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Supplier;

public class MdxLoadSave {
	// Below, these can't call a function on a string to make their value because
	// switch/case statements require the value to be compile-time defined in order
	// to be legal, and it appears to only allow basic binary operators for that.
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

	public static void loadMdx(MdlxModel mdlxModel, final ByteBuffer buffer) {
		final BinaryReader reader = new BinaryReader(buffer);

		if (reader.readTag() != MDLX) {
			throw new IllegalStateException("WrongMagicNumber");
		}
		try {
			while (reader.remaining() > 0) {
				final int tag = reader.readTag();
				final int size = reader.readInt32();

				switch (tag) {
					case VERS -> loadVersionChunk(mdlxModel, reader);
					case MODL -> loadModelChunk(mdlxModel, reader);
					case SEQS -> loadStaticObjects(mdlxModel.version, mdlxModel.sequences, MdlxSequence::new, reader, size / 132);
					case GLBS -> loadGlobalSequenceChunk(mdlxModel.globalSequences, reader, size);
					case MTLS -> loadDynamicObjects(mdlxModel.version, mdlxModel.materials, MdlxMaterial::new, reader, size);
					case TEXS -> loadStaticObjects(mdlxModel.version, mdlxModel.textures, MdlxTexture::new, reader, size / 268);
					case TXAN -> loadDynamicObjects(mdlxModel.version, mdlxModel.textureAnimations, MdlxTextureAnimation::new, reader, size);
					case GEOS -> loadDynamicObjects(mdlxModel.version, mdlxModel.geosets, MdlxGeoset::new, reader, size);
					case GEOA -> loadDynamicObjects(mdlxModel.version, mdlxModel.geosetAnimations, MdlxGeosetAnimation::new, reader, size);
					case BONE -> loadDynamicObjects(mdlxModel.version, mdlxModel.bones, MdlxBone::new, reader, size);
					case LITE -> loadDynamicObjects(mdlxModel.version, mdlxModel.lights, MdlxLight::new, reader, size);
					case HELP -> loadDynamicObjects(mdlxModel.version, mdlxModel.helpers, MdlxHelper::new, reader, size);
					case ATCH -> loadDynamicObjects(mdlxModel.version, mdlxModel.attachments, MdlxAttachment::new, reader, size);
					case PIVT -> loadPivotPointChunk(mdlxModel.pivotPoints, reader, size);
					case PREM -> loadDynamicObjects(mdlxModel.version, mdlxModel.particleEmitters, MdlxParticleEmitter::new, reader, size);
					case PRE2 -> loadDynamicObjects(mdlxModel.version, mdlxModel.particleEmitters2, MdlxParticleEmitter2::new, reader, size);
					case CORN -> loadDynamicObjects(mdlxModel.version, mdlxModel.particleEmittersPopcorn, MdlxParticleEmitterPopcorn::new, reader, size);
					case RIBB -> loadDynamicObjects(mdlxModel.version, mdlxModel.ribbonEmitters, MdlxRibbonEmitter::new, reader, size);
					case CAMS -> loadDynamicObjects(mdlxModel.version, mdlxModel.cameras, MdlxCamera::new, reader, size);
					case EVTS -> loadDynamicObjects(mdlxModel.version, mdlxModel.eventObjects, MdlxEventObject::new, reader, size);
					case CLID -> loadDynamicObjects(mdlxModel.version, mdlxModel.collisionShapes, MdlxCollisionShape::new, reader, size);
					case FAFX -> loadStaticObjects(mdlxModel.version, mdlxModel.faceEffects, MdlxFaceEffect::new, reader, size / 340);
					case BPOS -> loadBindPoseChunk(mdlxModel.bindPose, reader, size);
					default -> mdlxModel.unknownChunks.add(new MdlxUnknownChunk(reader, size, new War3ID(tag)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
//			throw new RuntimeException(e);
		}
	}

	private static void loadVersionChunk(MdlxModel mdlxModel, final BinaryReader reader) {
		mdlxModel.version = reader.readInt32();
	}

	private static void loadModelChunk(MdlxModel mdlxModel, final BinaryReader reader) {
		mdlxModel.name = reader.read(80);
		mdlxModel.animationFile = reader.read(260);
		mdlxModel.extent.readMdx(reader);
		mdlxModel.blendTime = reader.readInt32();
	}

	private static <E extends MdlxBlock> void loadStaticObjects(int version, List<E> out, Supplier<E> constructor,
	                                                            BinaryReader reader, long count) {
		for (int i = 0; i < count; i++) {
			E object = constructor.get();

			object.readMdx(reader, version);

			out.add(object);
		}
	}

	private static void loadGlobalSequenceChunk(List<Long> globalSequences, BinaryReader reader, long size) {
		for (long i = 0, l = size / 4; i < l; i++) {
			globalSequences.add(reader.readUInt32());
		}
	}

	private static <E extends MdlxBlock & MdlxChunk> void loadDynamicObjects(int version, List<E> out,
	                                                                         Supplier<E> constructor,
	                                                                         BinaryReader reader, long size) {
		long totalSize = 0;
		while (totalSize < size) {
			final E object = constructor.get();

			object.readMdx(reader, version);

			totalSize += object.getByteLength(version);

			out.add(object);
		}
	}

	private static void loadPivotPointChunk(List<float[]> pivotPoints, BinaryReader reader, long size) {
		for (long i = 0, l = size / 12; i < l; i++) {
			pivotPoints.add(reader.readFloat32Array(3));
		}
	}

	private static void loadBindPoseChunk(List<float[]> bindPose, BinaryReader reader, long size) {
		for (int i = 0, l = reader.readInt32(); i < l; i++) {
			bindPose.add(reader.readFloat32Array(12));
		}
	}

	public static ByteBuffer saveMdx(MdlxModel mdlxModel) {
		final BinaryWriter writer = new BinaryWriter(getByteLength(mdlxModel));

		writer.writeTag(MDLX);
		saveVersionChunk(mdlxModel.version, writer);
		saveModelChunk(mdlxModel, writer);
		saveStaticObjectChunk(mdlxModel.version, writer, SEQS, mdlxModel.sequences, 132);
		saveGlobalSequenceChunk(mdlxModel.globalSequences, writer);
		saveDynamicObjectChunk(mdlxModel, writer, MTLS, mdlxModel.materials);
		saveStaticObjectChunk(mdlxModel.version, writer, TEXS, mdlxModel.textures, 268);
		saveDynamicObjectChunk(mdlxModel, writer, TXAN, mdlxModel.textureAnimations);
		saveDynamicObjectChunk(mdlxModel, writer, GEOS, mdlxModel.geosets);
		saveDynamicObjectChunk(mdlxModel, writer, GEOA, mdlxModel.geosetAnimations);
		saveDynamicObjectChunk(mdlxModel, writer, BONE, mdlxModel.bones);
		saveDynamicObjectChunk(mdlxModel, writer, LITE, mdlxModel.lights);
		saveDynamicObjectChunk(mdlxModel, writer, HELP, mdlxModel.helpers);
		saveDynamicObjectChunk(mdlxModel, writer, ATCH, mdlxModel.attachments);
		savePivotPointChunk(mdlxModel.pivotPoints, writer);
		saveDynamicObjectChunk(mdlxModel, writer, PREM, mdlxModel.particleEmitters);
		saveDynamicObjectChunk(mdlxModel, writer, PRE2, mdlxModel.particleEmitters2);

		if (mdlxModel.version > 800) {
			saveDynamicObjectChunk(mdlxModel, writer, CORN, mdlxModel.particleEmittersPopcorn);
		}

		saveDynamicObjectChunk(mdlxModel, writer, RIBB, mdlxModel.ribbonEmitters);
		saveDynamicObjectChunk(mdlxModel, writer, CAMS, mdlxModel.cameras);
		saveDynamicObjectChunk(mdlxModel, writer, EVTS, mdlxModel.eventObjects);
		saveDynamicObjectChunk(mdlxModel, writer, CLID, mdlxModel.collisionShapes);

		if (mdlxModel.version > 800) {
			saveStaticObjectChunk(mdlxModel.version, writer, FAFX, mdlxModel.faceEffects, 340);
			saveBindPoseChunk(mdlxModel.bindPose, writer);
		}

		for (MdlxUnknownChunk chunk : mdlxModel.unknownChunks) {
			chunk.writeMdx(writer, mdlxModel.version);
		}

		return writer.buffer;
	}

	private static void saveVersionChunk(int version, final BinaryWriter writer) {
		writer.writeTag(VERS);
		writer.writeUInt32(4);
		writer.writeUInt32(version);
	}

	private static void saveModelChunk(MdlxModel mdlxModel, final BinaryWriter writer) {
		writer.writeTag(MODL);
		writer.writeUInt32(372);
		writer.writeWithNulls(mdlxModel.name, 80);
		writer.writeWithNulls(mdlxModel.animationFile, 260);
		mdlxModel.extent.writeMdx(writer);
		writer.writeUInt32(mdlxModel.blendTime);
	}

	private static <E extends MdlxBlock> void saveStaticObjectChunk(int version, BinaryWriter writer, int name,
	                                                                List<E> objects, long size) {
		if (!objects.isEmpty()) {
			writer.writeTag(name);
			writer.writeUInt32(objects.size() * size);

			for (final E object : objects) {
				object.writeMdx(writer, version);
			}
		}
	}

	private static void saveGlobalSequenceChunk(List<Long> globalSequences, BinaryWriter writer) {
		if (!globalSequences.isEmpty()) {
			writer.writeTag(GLBS);
			writer.writeUInt32(globalSequences.size() * 4);

			for (Long globalSequence : globalSequences) {
				writer.writeUInt32(globalSequence);
			}
		}
	}

	private static <E extends MdlxBlock & MdlxChunk> void saveDynamicObjectChunk(MdlxModel mdlxModel,
	                                                                             BinaryWriter writer, int name, List<E> objects) {
		if (!objects.isEmpty()) {
			writer.writeTag(name);
			writer.writeUInt32(getObjectsByteLength(mdlxModel.version, objects));

			for (final E object : objects) {
				object.writeMdx(writer, mdlxModel.version);
			}
		}
	}

	private static void savePivotPointChunk(List<float[]> pivotPoints, final BinaryWriter writer) {
		if (pivotPoints.size() > 0) {
			writer.writeTag(PIVT);
			writer.writeUInt32(pivotPoints.size() * 12);

			for (final float[] pivotPoint : pivotPoints) {
				writer.writeFloat32Array(pivotPoint);
			}
		}
	}

	private static void saveBindPoseChunk(List<float[]> bindPose, final BinaryWriter writer) {
		if (bindPose.size() > 0) {
			writer.writeTag(BPOS);
			writer.writeUInt32(4 + bindPose.size() * 48);
			writer.writeUInt32(bindPose.size());

			for (final float[] matrix : bindPose) {
				writer.writeFloat32Array(matrix);
			}
		}
	}

	public static int getByteLength(MdlxModel mdlxModel) {
		int size = 396;

		size += getStaticObjectsChunkByteLength(mdlxModel.sequences, 132);
		size += getStaticObjectsChunkByteLength(mdlxModel.globalSequences, 4);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.materials);
		size += getStaticObjectsChunkByteLength(mdlxModel.textures, 268);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.textureAnimations);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.geosets);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.geosetAnimations);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.bones);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.lights);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.helpers);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.attachments);
		size += getStaticObjectsChunkByteLength(mdlxModel.pivotPoints, 12);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.particleEmitters);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.particleEmitters2);

		if (mdlxModel.version > 800) {
			size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.particleEmittersPopcorn);
		}

		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.ribbonEmitters);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.cameras);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.eventObjects);
		size += getDynamicObjectsChunkByteLength(mdlxModel, mdlxModel.collisionShapes);
		size += getObjectsByteLength(mdlxModel.version, mdlxModel.unknownChunks);

		if (mdlxModel.version > 800) {
			size += getStaticObjectsChunkByteLength(mdlxModel.faceEffects, 340);
			size += getBindPoseChunkByteLength(mdlxModel.bindPose);
		}

		return size;
	}

	private static <E extends MdlxChunk> long getObjectsByteLength(int version, final List<E> objects) {
		long size = 0;
		for (final E object : objects) {
			size += object.getByteLength(version);
		}
		return size;
	}

	private static <E extends MdlxChunk> long getDynamicObjectsChunkByteLength(MdlxModel mdlxModel, final List<E> objects) {
		if (!objects.isEmpty()) {
			return 8 + getObjectsByteLength(mdlxModel.version, objects);
		}

		return 0;
	}

	private static <E> long getStaticObjectsChunkByteLength(final List<E> objects, final long size) {
		if (!objects.isEmpty()) {
			return 8 + (objects.size() * size);
		}

		return 0;
	}

	private static long getBindPoseChunkByteLength(List<float[]> bindPose) {
		if (bindPose.size() > 0) {
			return 12 + bindPose.size() * 48;
		}

		return 0;
	}
}
