package com.etheller.warsmash.parsers.mdlx;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

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
	public List<MdlxSequence> sequences = new ArrayList<MdlxSequence>();
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
	public String faceEffectTarget = "";
	/**
	 * @since 900
	 */
	public String faceEffect = "";
	/**
	 * @since 900
	 */
	public List<float[]> bindPose = new ArrayList<>();
	public List<MdlxUnknownChunk> unknownChunks = new ArrayList<>();

	public MdlxModel() {

	}

	public MdlxModel(final InputStream buffer) throws IOException {
		loadMdx(buffer);
	}

	// public MdlxModel(final InputStream buffer) throws IOException {
	// 	if (buffer != null) {
	// 		// In ghostwolf JS, this function called load()
	// 		// which decided whether the buffer was an MDL.
	// 		loadMdx(buffer);
	// 	}
	// }

	public void loadMdx(final InputStream buffer) throws IOException {
		final LittleEndianDataInputStream stream = new LittleEndianDataInputStream(buffer);
		if (Integer.reverseBytes(stream.readInt()) != MDLX) {
			throw new IllegalStateException("WrongMagicNumber");
		}

		while (stream.available() > 0) {
			final int tag = Integer.reverseBytes(stream.readInt());
			final long size = ParseUtils.readUInt32(stream);

			switch (tag) {
			case VERS:
				loadVersionChunk(stream);
				break;
			case MODL:
				loadModelChunk(stream);
				break;
			case SEQS:
				loadStaticObjects(this.sequences, MdlxBlockDescriptor.SEQUENCE, stream, size / 132);
				break;
			case GLBS:
				loadGlobalSequenceChunk(stream, size);
				break;
			case MTLS:
				loadDynamicObjects(this.materials, MdlxBlockDescriptor.MATERIAL, stream, size);
				break;
			case TEXS:
				loadStaticObjects(this.textures, MdlxBlockDescriptor.TEXTURE, stream, size / 268);
				break;
			case TXAN:
				loadDynamicObjects(this.textureAnimations, MdlxBlockDescriptor.TEXTURE_ANIMATION, stream, size);
				break;
			case GEOS:
				loadDynamicObjects(this.geosets, MdlxBlockDescriptor.GEOSET, stream, size);
				break;
			case GEOA:
				loadDynamicObjects(this.geosetAnimations, MdlxBlockDescriptor.GEOSET_ANIMATION, stream, size);
				break;
			case BONE:
				loadDynamicObjects(this.bones, MdlxBlockDescriptor.BONE, stream, size);
				break;
			case LITE:
				loadDynamicObjects(this.lights, MdlxBlockDescriptor.LIGHT, stream, size);
				break;
			case HELP:
				loadDynamicObjects(this.helpers, MdlxBlockDescriptor.HELPER, stream, size);
				break;
			case ATCH:
				loadDynamicObjects(this.attachments, MdlxBlockDescriptor.ATTACHMENT, stream, size);
				break;
			case PIVT:
				loadPivotPointChunk(stream, size);
				break;
			case PREM:
				loadDynamicObjects(this.particleEmitters, MdlxBlockDescriptor.PARTICLE_EMITTER, stream, size);
				break;
			case PRE2:
				loadDynamicObjects(this.particleEmitters2, MdlxBlockDescriptor.PARTICLE_EMITTER2, stream, size);
				break;
			case CORN:
				loadDynamicObjects(this.particleEmittersPopcorn, MdlxBlockDescriptor.PARTICLE_EMITTER_POPCORN, stream, size);
				break;
			case RIBB:
				loadDynamicObjects(this.ribbonEmitters, MdlxBlockDescriptor.RIBBON_EMITTER, stream, size);
				break;
			case CAMS:
				loadDynamicObjects(this.cameras, MdlxBlockDescriptor.CAMERA, stream, size);
				break;
			case EVTS:
				loadDynamicObjects(this.eventObjects, MdlxBlockDescriptor.EVENT_OBJECT, stream, size);
				break;
			case CLID:
				loadDynamicObjects(this.collisionShapes, MdlxBlockDescriptor.COLLISION_SHAPE, stream, size);
				break;
			case FAFX:
				loadFaceEffectChunk(stream);
				break;
			case BPOS:
				loadBindPoseChunk(stream, size);
				break;
			default:
				this.unknownChunks.add(new MdlxUnknownChunk(stream, size, new War3ID(tag)));
			}
		}
	}

	private void loadVersionChunk(final LittleEndianDataInputStream stream) throws IOException {
		this.version = (int) ParseUtils.readUInt32(stream);
	}

	/**
	 * Restricts us to only be able to parse models on one thread at a time, in
	 * return for high performance.
	 */
	private static final byte[] NAME_BYTES_HEAP = new byte[80];
	private static final byte[] ANIMATION_FILE_BYTES_HEAP = new byte[260];

	private void loadModelChunk(final LittleEndianDataInputStream stream) throws IOException {
		this.name = ParseUtils.readString(stream, NAME_BYTES_HEAP);
		this.animationFile = ParseUtils.readString(stream, ANIMATION_FILE_BYTES_HEAP);
		this.extent.readMdx(stream);
		this.blendTime = ParseUtils.readUInt32(stream);
	}

	private <E extends MdlxBlock> void loadStaticObjects(final List<E> out, final MdlxBlockDescriptor<E> constructor,
			final LittleEndianDataInputStream stream, final long count) throws IOException {
		for (int i = 0; i < count; i++) {
			final E object = constructor.create();

			object.readMdx(stream, version);

			out.add(object);
		}
	}

	private void loadGlobalSequenceChunk(final LittleEndianDataInputStream stream, final long size) throws IOException {
		for (long i = 0, l = size / 4; i < l; i++) {
			this.globalSequences.add(ParseUtils.readUInt32(stream));
		}
	}

	private <E extends MdlxBlock & MdlxChunk> void loadDynamicObjects(final List<E> out,
			final MdlxBlockDescriptor<E> constructor, final LittleEndianDataInputStream stream, final long size)
			throws IOException {
		long totalSize = 0;
		while (totalSize < size) {
			final E object = constructor.create();

			object.readMdx(stream, version);

			totalSize += object.getByteLength(version);

			out.add(object);
		}
	}

	private void loadPivotPointChunk(final LittleEndianDataInputStream stream, final long size) throws IOException {
		for (long i = 0, l = size / 12; i < l; i++) {
			this.pivotPoints.add(ParseUtils.readFloatArray(stream, 3));
		}
	}

	private void loadFaceEffectChunk(final LittleEndianDataInputStream stream) throws IOException {
		faceEffectTarget = ParseUtils.readString(stream, NAME_BYTES_HEAP);
		faceEffect = ParseUtils.readString(stream, ANIMATION_FILE_BYTES_HEAP);
	}

	private void loadBindPoseChunk(final LittleEndianDataInputStream stream, final long size) throws IOException {
		for (int i = 0, l = stream.readInt(); i < l; i++) {
			bindPose.add(ParseUtils.readFloatArray(stream, 12));
		}
	}

	public void saveMdx(final OutputStream outputStream) throws IOException {
		final LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(outputStream);
		stream.writeInt(Integer.reverseBytes(MDLX));
		this.saveVersionChunk(stream);
		this.saveModelChunk(stream);
		this.saveStaticObjectChunk(stream, SEQS, this.sequences, 132);
		this.saveGlobalSequenceChunk(stream);
		this.saveDynamicObjectChunk(stream, MTLS, this.materials);
		this.saveStaticObjectChunk(stream, TEXS, this.textures, 268);
		this.saveDynamicObjectChunk(stream, TXAN, this.textureAnimations);
		this.saveDynamicObjectChunk(stream, GEOS, this.geosets);
		this.saveDynamicObjectChunk(stream, GEOA, this.geosetAnimations);
		this.saveDynamicObjectChunk(stream, BONE, this.bones);
		this.saveDynamicObjectChunk(stream, LITE, this.lights);
		this.saveDynamicObjectChunk(stream, HELP, this.helpers);
		this.saveDynamicObjectChunk(stream, ATCH, this.attachments);
		this.savePivotPointChunk(stream);
		this.saveDynamicObjectChunk(stream, PREM, this.particleEmitters);
		this.saveDynamicObjectChunk(stream, PRE2, this.particleEmitters2);

		if (version > 800) {
			this.saveDynamicObjectChunk(stream, CORN, this.particleEmittersPopcorn);
		}

		this.saveDynamicObjectChunk(stream, RIBB, this.ribbonEmitters);
		this.saveDynamicObjectChunk(stream, CAMS, this.cameras);
		this.saveDynamicObjectChunk(stream, EVTS, this.eventObjects);
		this.saveDynamicObjectChunk(stream, CLID, this.collisionShapes);

		if (version > 800) {
			saveFaceEffectChunk(stream);
			saveBindPoseChunk(stream);
		}

		for (final MdlxUnknownChunk chunk : this.unknownChunks) {
			chunk.writeMdx(stream, version);
		}
	}

	private void saveVersionChunk(final LittleEndianDataOutputStream stream) throws IOException {
		stream.writeInt(Integer.reverseBytes(VERS));
		ParseUtils.writeUInt32(stream, 4);
		ParseUtils.writeUInt32(stream, this.version);
	}

	private void saveModelChunk(final LittleEndianDataOutputStream stream) throws IOException {
		stream.writeInt(Integer.reverseBytes(MODL));
		ParseUtils.writeUInt32(stream, 372);
		final byte[] bytes = this.name.getBytes(ParseUtils.UTF8);
		stream.write(bytes);
		for (int i = 0; i < (NAME_BYTES_HEAP.length - bytes.length); i++) {
			stream.write((byte) 0);
		}
		final byte[] animationFileBytes = this.animationFile.getBytes(ParseUtils.UTF8);
		stream.write(animationFileBytes);
		for (int i = 0; i < (ANIMATION_FILE_BYTES_HEAP.length - animationFileBytes.length); i++) {
			stream.write((byte) 0);
		}
		this.extent.writeMdx(stream);
		ParseUtils.writeUInt32(stream, this.blendTime);
	}

	private <E extends MdlxBlock> void saveStaticObjectChunk(final LittleEndianDataOutputStream stream, final int name,
			final List<E> objects, final long size) throws IOException {
		if (!objects.isEmpty()) {
			stream.writeInt(Integer.reverseBytes(name));
			ParseUtils.writeUInt32(stream, objects.size() * size);

			for (final E object : objects) {
				object.writeMdx(stream, version);
			}
		}
	}

	private void saveGlobalSequenceChunk(final LittleEndianDataOutputStream stream) throws IOException {
		if (!this.globalSequences.isEmpty()) {
			stream.writeInt(Integer.reverseBytes(GLBS));
			ParseUtils.writeUInt32(stream, this.globalSequences.size() * 4);

			for (final Long globalSequence : this.globalSequences) {
				ParseUtils.writeUInt32(stream, globalSequence);
			}
		}
	}

	private <E extends MdlxBlock & MdlxChunk> void saveDynamicObjectChunk(final LittleEndianDataOutputStream stream,
			final int name, final List<E> objects) throws IOException {
		if (!objects.isEmpty()) {
			stream.writeInt(Integer.reverseBytes(name));
			ParseUtils.writeUInt32(stream, getObjectsByteLength(objects));

			for (final E object : objects) {
				object.writeMdx(stream, version);
			}
		}
	}

	private void savePivotPointChunk(final LittleEndianDataOutputStream stream) throws IOException {
		if (this.pivotPoints.size() > 0) {
			stream.writeInt(Integer.reverseBytes(PIVT));
			ParseUtils.writeUInt32(stream, this.pivotPoints.size() * 12);

			for (final float[] pivotPoint : this.pivotPoints) {
				ParseUtils.writeFloatArray(stream, pivotPoint);
			}
		}
	}

	private void saveFaceEffectChunk(final LittleEndianDataOutputStream stream) throws IOException {
		if (faceEffectTarget.length() > 0 || faceEffect.length() > 0) {
			stream.writeInt(Integer.reverseBytes(FAFX));
			ParseUtils.writeUInt32(stream, 340);
			ParseUtils.writeString(stream, faceEffectTarget, 80);
			ParseUtils.writeString(stream, faceEffect, 260);
		}
	}


	private void saveBindPoseChunk(final LittleEndianDataOutputStream stream) throws IOException {
		if (bindPose.size() > 0) {
			stream.writeInt(Integer.reverseBytes(BPOS));
			ParseUtils.writeUInt32(stream, 4 + bindPose.size() * 48);
			ParseUtils.writeUInt32(stream, bindPose.size());

			for (final float[] matrix : bindPose) {
				ParseUtils.writeFloatArray(stream, matrix);
			}
		}
	}


	public void loadMdl(final InputStream inputStream) throws IOException {
		// TODO change this to use LibGDX StreamUtils once LibGDX is on the classpath
		// again!
		final byte[] array = PortedFromLibGDXStreamUtils.copyStreamToByteArray(inputStream);
		loadMdl(ByteBuffer.wrap(array));
	}

	public void loadMdl(final ByteBuffer inputStream) throws IOException {
		String token;
		final MdlTokenInputStream stream = new MdlTokenInputStream(inputStream);

		while ((token = stream.read()) != null) {
			switch (token) {
			case MdlUtils.TOKEN_VERSION:
				this.loadVersionBlock(stream);
				break;
			case MdlUtils.TOKEN_MODEL:
				this.loadModelBlock(stream);
				break;
			case MdlUtils.TOKEN_SEQUENCES:
				this.loadNumberedObjectBlock(this.sequences, MdlxBlockDescriptor.SEQUENCE, MdlUtils.TOKEN_ANIM, stream);
				break;
			case MdlUtils.TOKEN_GLOBAL_SEQUENCES:
				this.loadGlobalSequenceBlock(stream);
				break;
			case MdlUtils.TOKEN_TEXTURES:
				this.loadNumberedObjectBlock(this.textures, MdlxBlockDescriptor.TEXTURE, MdlUtils.TOKEN_BITMAP, stream);
				break;
			case MdlUtils.TOKEN_MATERIALS:
				this.loadNumberedObjectBlock(this.materials, MdlxBlockDescriptor.MATERIAL, MdlUtils.TOKEN_MATERIAL,
						stream);
				break;
			case MdlUtils.TOKEN_TEXTURE_ANIMS:
				this.loadNumberedObjectBlock(this.textureAnimations, MdlxBlockDescriptor.TEXTURE_ANIMATION,
						MdlUtils.TOKEN_TEXTURE_ANIM, stream);
				break;
			case MdlUtils.TOKEN_GEOSET:
				this.loadObject(this.geosets, MdlxBlockDescriptor.GEOSET, stream);
				break;
			case MdlUtils.TOKEN_GEOSETANIM:
				this.loadObject(this.geosetAnimations, MdlxBlockDescriptor.GEOSET_ANIMATION, stream);
				break;
			case MdlUtils.TOKEN_BONE:
				this.loadObject(this.bones, MdlxBlockDescriptor.BONE, stream);
				break;
			case MdlUtils.TOKEN_LIGHT:
				this.loadObject(this.lights, MdlxBlockDescriptor.LIGHT, stream);
				break;
			case MdlUtils.TOKEN_HELPER:
				this.loadObject(this.helpers, MdlxBlockDescriptor.HELPER, stream);
				break;
			case MdlUtils.TOKEN_ATTACHMENT:
				this.loadObject(this.attachments, MdlxBlockDescriptor.ATTACHMENT, stream);
				break;
			case MdlUtils.TOKEN_PIVOT_POINTS:
				this.loadPivotPointBlock(stream);
				break;
			case MdlUtils.TOKEN_PARTICLE_EMITTER:
				this.loadObject(this.particleEmitters, MdlxBlockDescriptor.PARTICLE_EMITTER, stream);
				break;
			case MdlUtils.TOKEN_PARTICLE_EMITTER2:
				this.loadObject(this.particleEmitters2, MdlxBlockDescriptor.PARTICLE_EMITTER2, stream);
				break;
			case "ParticleEmitterPopcorn":
				this.loadObject(this.particleEmittersPopcorn, MdlxBlockDescriptor.PARTICLE_EMITTER_POPCORN, stream);
				break;
			case MdlUtils.TOKEN_RIBBON_EMITTER:
				this.loadObject(this.ribbonEmitters, MdlxBlockDescriptor.RIBBON_EMITTER, stream);
				break;
			case MdlUtils.TOKEN_CAMERA:
				this.loadObject(this.cameras, MdlxBlockDescriptor.CAMERA, stream);
				break;
			case MdlUtils.TOKEN_EVENT_OBJECT:
				this.loadObject(this.eventObjects, MdlxBlockDescriptor.EVENT_OBJECT, stream);
				break;
			case MdlUtils.TOKEN_COLLISION_SHAPE:
				this.loadObject(this.collisionShapes, MdlxBlockDescriptor.COLLISION_SHAPE, stream);
				break;
			case "FaceFX":
				loadFaceEffectBlock(stream);
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
				this.version = stream.readInt();
				break;
			default:
				throw new IllegalStateException("Unknown token in Version: " + token);
			}
		}
	}

	private void loadModelBlock(final MdlTokenInputStream stream) {
		this.name = stream.read();
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
					this.blendTime = stream.readUInt32();
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
				default:
					throw new IllegalStateException("Unknown token in Model: " + token);
				}
			}
		}
	}

	private <E extends MdlxBlock> void loadNumberedObjectBlock(final List<E> out,
			final MdlxBlockDescriptor<E> constructor, final String name, final MdlTokenInputStream stream)
			throws IOException {
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
				this.globalSequences.add(stream.readUInt32());
			} else {
				throw new IllegalStateException("Unknown token in GlobalSequences: " + token);
			}
		}
	}

	private <E extends MdlxBlock> void loadObject(final List<E> out, final MdlxBlockDescriptor<E> descriptor,
			final MdlTokenInputStream stream) throws IOException {
		final E object = descriptor.create();

		object.readMdl(stream, version);

		out.add(object);
	}

	private void loadPivotPointBlock(final MdlTokenInputStream stream) {
		final int count = stream.readInt();

		stream.read(); // {

		for (int i = 0; i < count; i++) {
			this.pivotPoints.add(stream.readFloatArray(new float[3]));
		}

		stream.read(); // }
	}

	private void loadFaceEffectBlock(MdlTokenInputStream stream) {
		this.faceEffectTarget = stream.read();
	
		for (final String token : stream.readBlock()) {
			if (token.equals("Path")) {
				this.faceEffect = stream.read();
			} else {
				throw new IllegalStateException("Unknown token in GlobalSequences: " + token);
			}
		}
	  }
	
	private void loadBindPoseBlock(MdlTokenInputStream stream) {
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

	public void saveMdl(final OutputStream outputStream) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
			final MdlTokenOutputStream stream = new MdlTokenOutputStream(writer);
			this.saveVersionBlock(stream);
			this.saveModelBlock(stream);
			this.saveStaticObjectsBlock(stream, MdlUtils.TOKEN_SEQUENCES, this.sequences);
			this.saveGlobalSequenceBlock(stream);
			this.saveStaticObjectsBlock(stream, MdlUtils.TOKEN_TEXTURES, this.textures);
			this.saveStaticObjectsBlock(stream, MdlUtils.TOKEN_MATERIALS, this.materials);
			this.saveStaticObjectsBlock(stream, MdlUtils.TOKEN_TEXTURE_ANIMS, this.textureAnimations);
			this.saveObjects(stream, this.geosets);
			this.saveObjects(stream, this.geosetAnimations);
			this.saveObjects(stream, this.bones);
			this.saveObjects(stream, this.lights);
			this.saveObjects(stream, this.helpers);
			this.saveObjects(stream, this.attachments);
			this.savePivotPointBlock(stream);
			this.saveObjects(stream, this.particleEmitters);
			this.saveObjects(stream, this.particleEmitters2);

			if (version > 800) {
				saveObjects(stream, particleEmittersPopcorn);
			}

			this.saveObjects(stream, this.ribbonEmitters);
			this.saveObjects(stream, this.cameras);
			this.saveObjects(stream, this.eventObjects);
			this.saveObjects(stream, this.collisionShapes);

			if (version > 800) {
				saveFaceEffectBlock(stream);
				saveBindPoseBlock(stream);
			}
		}
	}

	private void saveVersionBlock(final MdlTokenOutputStream stream) {
		stream.startBlock(MdlUtils.TOKEN_VERSION);
		stream.writeAttrib(MdlUtils.TOKEN_FORMAT_VERSION, this.version);
		stream.endBlock();
	}

	private void saveModelBlock(final MdlTokenOutputStream stream) {
		stream.startObjectBlock(MdlUtils.TOKEN_MODEL, this.name);
		stream.writeAttribUInt32(MdlUtils.TOKEN_BLEND_TIME, this.blendTime);
		this.extent.writeMdl(stream);
		stream.endBlock();
	}

	private void saveStaticObjectsBlock(final MdlTokenOutputStream stream, final String name,
			final List<? extends MdlxBlock> objects) throws IOException {
		if (!objects.isEmpty()) {
			stream.startBlock(name, objects.size());

			for (final MdlxBlock object : objects) {
				object.writeMdl(stream, version);
			}

			stream.endBlock();
		}
	}

	private void saveGlobalSequenceBlock(final MdlTokenOutputStream stream) {
		if (!this.globalSequences.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_GLOBAL_SEQUENCES, this.globalSequences.size());

			for (final Long globalSequence : this.globalSequences) {
				stream.writeAttribUInt32(MdlUtils.TOKEN_DURATION, globalSequence);
			}

			stream.endBlock();
		}
	}

	private void saveObjects(final MdlTokenOutputStream stream, final List<? extends MdlxBlock> objects)
			throws IOException {
		for (final MdlxBlock object : objects) {
			object.writeMdl(stream, version);
		}
	}

	private void savePivotPointBlock(final MdlTokenOutputStream stream) {
		if (!this.pivotPoints.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_PIVOT_POINTS, this.pivotPoints.size());

			for (final float[] pivotPoint : this.pivotPoints) {
				stream.writeFloatArray(pivotPoint);
			}

			stream.endBlock();
		}
	}

	private void saveFaceEffectBlock(final MdlTokenOutputStream stream) {
		if (faceEffectTarget.length() > 0 && faceEffect.length() > 0) {
			stream.startObjectBlock("FaceFX", faceEffectTarget);

			stream.writeStringAttrib("Path", faceEffect);

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

	private long getByteLength() {
		long size = 396;

		size += getStaticObjectsChunkByteLength(this.sequences, 132);
		size += this.getStaticObjectsChunkByteLength(this.globalSequences, 4);
		size += this.getDynamicObjectsChunkByteLength(this.materials);
		size += this.getStaticObjectsChunkByteLength(this.textures, 268);
		size += this.getDynamicObjectsChunkByteLength(this.textureAnimations);
		size += this.getDynamicObjectsChunkByteLength(this.geosets);
		size += this.getDynamicObjectsChunkByteLength(this.geosetAnimations);
		size += this.getDynamicObjectsChunkByteLength(this.bones);
		size += this.getDynamicObjectsChunkByteLength(this.lights);
		size += this.getDynamicObjectsChunkByteLength(this.helpers);
		size += this.getDynamicObjectsChunkByteLength(this.attachments);
		size += this.getStaticObjectsChunkByteLength(this.pivotPoints, 12);
		size += this.getDynamicObjectsChunkByteLength(this.particleEmitters);
		size += this.getDynamicObjectsChunkByteLength(this.particleEmitters2);

		if (version > 800) {
			size += this.getDynamicObjectsChunkByteLength(this.particleEmittersPopcorn);
		}

		size += this.getDynamicObjectsChunkByteLength(this.ribbonEmitters);
		size += this.getDynamicObjectsChunkByteLength(this.cameras);
		size += this.getDynamicObjectsChunkByteLength(this.eventObjects);
		size += this.getDynamicObjectsChunkByteLength(this.collisionShapes);
		size += this.getObjectsByteLength(this.unknownChunks);

		if (version > 800) {
			size += this.getFaceEffectChunkByteLength();
			size += this.getBindPoseChunkByteLength();
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
			return 8 + this.getObjectsByteLength(objects);
		}

		return 0;
	}

	private <E> long getStaticObjectsChunkByteLength(final List<E> objects, final long size) {
		if (!objects.isEmpty()) {
			return 8 + (objects.size() * size);
		}

		return 0;
	}

	private long getFaceEffectChunkByteLength() {
		if (faceEffectTarget.length() > 0 || faceEffect.length() > 0) {
			return 348;
		}
	
		return 0;
	}
	
	private long getBindPoseChunkByteLength() {
		if (bindPose.size() > 0) {
		  	return 12 + bindPose.size() * 48;
		}
	
		return 0;
	}

	private static final class PortedFromLibGDXStreamUtils {
		public static final int DEFAULT_BUFFER_SIZE = 4096;
		public static final byte[] EMPTY_BYTES = new byte[0];

		/**
		 * Allocates a {@value #DEFAULT_BUFFER_SIZE} byte[] for use as a temporary
		 * buffer and calls {@link #copyStream(InputStream, OutputStream, byte[])}.
		 */
		public static void copyStream(final InputStream input, final OutputStream output) throws IOException {
			copyStream(input, output, new byte[DEFAULT_BUFFER_SIZE]);
		}

		/**
		 * Copy the data from an {@link InputStream} to an {@link OutputStream}, using
		 * the specified byte[] as a temporary buffer. The stream is not closed.
		 */
		public static void copyStream(final InputStream input, final OutputStream output, final byte[] buffer)
				throws IOException {
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		}

		/**
		 * Copy the data from an {@link InputStream} to a byte array. The stream is not
		 * closed.
		 */
		public static byte[] copyStreamToByteArray(final InputStream input) throws IOException {
			return copyStreamToByteArray(input, input.available());
		}

		/**
		 * Copy the data from an {@link InputStream} to a byte array. The stream is not
		 * closed.
		 *
		 * @param estimatedSize Used to allocate the output byte[] to possibly avoid an
		 *                      array copy.
		 */
		public static byte[] copyStreamToByteArray(final InputStream input, final int estimatedSize)
				throws IOException {
			final ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(Math.max(0, estimatedSize));
			copyStream(input, baos);
			return baos.toByteArray();
		}

		/**
		 * A ByteArrayOutputStream which avoids copying of the byte array if possible.
		 */
		static public class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
			public OptimizedByteArrayOutputStream(final int initialSize) {
				super(initialSize);
			}

			@Override
			public synchronized byte[] toByteArray() {
				if (count == buf.length) {
					return buf;
				}
				return super.toByteArray();
			}

			public byte[] getBuffer() {
				return buf;
			}
		}
	}
}
