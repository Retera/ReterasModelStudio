package com.hiveworkshop.rms.parsers.mdlx.timeline;

import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

public abstract class MdlxTimeline<TYPE> {
	public War3ID name;
	public InterpolationType interpolationType;
	public int globalSequenceId = -1;

	public long[] frames;
	public TYPE[] values;
	public TYPE[] inTans;
	public TYPE[] outTans;

	/**
	 * Restricts us to only be able to parse models on one thread at a time, in
	 * return for high performance.
	 */
	private static final StringBuffer STRING_BUFFER_HEAP = new StringBuffer();

	public MdlxTimeline() {
		
	}

	public void readMdx(final BinaryReader reader, final War3ID name) {
		this.name = name;

		final long keyFrameCount = reader.readUInt32();

		interpolationType = InterpolationType.getType(reader.readInt32());
		globalSequenceId = reader.readInt32();

		frames = new long[(int) keyFrameCount];
		values = (TYPE[]) new Object[(int) keyFrameCount];
		if (interpolationType.tangential()) {
			inTans = (TYPE[]) new Object[(int) keyFrameCount];
			outTans = (TYPE[]) new Object[(int) keyFrameCount];
		}

		for (int i = 0; i < keyFrameCount; i++) {
			frames[i] = reader.readInt32();
			values[i] = (readMdxValue(reader));

			if (interpolationType.tangential()) {
				inTans[i] = (readMdxValue(reader));
				outTans[i] = (readMdxValue(reader));
			}
		}
	}

	public void writeMdx(final BinaryWriter writer) {
		writer.writeTag(name.getValue());

		final int keyframeCount = frames.length;

		writer.writeInt32(keyframeCount);
		writer.writeInt32(interpolationType.ordinal());
		writer.writeInt32(globalSequenceId);

		for (int i = 0; i < keyframeCount; i++) {
			writer.writeInt32((int) frames[i]);
			writeMdxValue(writer, values[i]);

			if (interpolationType.tangential()) {
				writeMdxValue(writer, inTans[i]);
				writeMdxValue(writer, outTans[i]);
			}
		}
	}

	public void readMdl(final MdlTokenInputStream stream, final War3ID name) {
		this.name = name;

		final int keyFrameCount = stream.readInt();

		stream.read(); // {

		final String token = stream.read();
		final InterpolationType interpolationType = switch (token) {
			case MdlUtils.TOKEN_DONT_INTERP -> InterpolationType.DONT_INTERP;
			case MdlUtils.TOKEN_LINEAR -> InterpolationType.LINEAR;
			case MdlUtils.TOKEN_HERMITE -> InterpolationType.HERMITE;
			case MdlUtils.TOKEN_BEZIER -> InterpolationType.BEZIER;
			default -> InterpolationType.DONT_INTERP;
		};

		this.interpolationType = interpolationType;

		if (stream.peek().equals(MdlUtils.TOKEN_GLOBAL_SEQ_ID)) {
			stream.read();
			globalSequenceId = stream.readInt();
		} else {
			globalSequenceId = -1;
		}

		frames = new long[keyFrameCount];
		values = (TYPE[]) new Object[keyFrameCount];
		if (this.interpolationType.tangential()) {
			inTans = (TYPE[]) new Object[keyFrameCount];
			outTans = (TYPE[]) new Object[keyFrameCount];
		}
		for (int i = 0; i < keyFrameCount; i++) {
			frames[i] = (stream.readInt());
			values[i] = (readMdlValue(stream));
			if (interpolationType.tangential()) {
				stream.read(); // InTan
				inTans[i] = (readMdlValue(stream));
				stream.read(); // OutTan
				outTans[i] = (readMdlValue(stream));
			}
		}

		stream.read(); // }
	}

	public void writeMdl(final MdlTokenOutputStream stream) {
		final int tracksCount = frames.length;
		stream.startBlock(AnimationMap.ID_TO_TAG.get(name).getMdlToken(), tracksCount);

		stream.writeFlag(interpolationType.toString());

		if (globalSequenceId != -1) {
			stream.writeAttrib(MdlUtils.TOKEN_GLOBAL_SEQ_ID, globalSequenceId);
		}

		for (int i = 0; i < tracksCount; i++) {
			STRING_BUFFER_HEAP.setLength(0);
			STRING_BUFFER_HEAP.append(frames[i]);
			STRING_BUFFER_HEAP.append(':');
			writeMdlValue(stream, STRING_BUFFER_HEAP.toString(), values[i]);
			if (interpolationType.tangential()) {
				stream.indent();
				writeMdlValue(stream, "InTan", inTans[i]);
				writeMdlValue(stream, "OutTan", outTans[i]);
				stream.unindent();
			}
		}

		stream.endBlock();
	}

	public long getByteLength() {
		final int tracksCount = frames.length;
		int size = 16;

		if (tracksCount > 0) {
			final int bytesPerValue = size() * 4;
			int valuesPerTrack = 1;
			if (interpolationType.tangential()) {
				valuesPerTrack = 3;
			}

			size += (4 + (valuesPerTrack * bytesPerValue)) * tracksCount;
		}
		return size;
	}

	protected abstract int size();

	protected abstract TYPE readMdxValue(BinaryReader reader);

	protected abstract TYPE readMdlValue(MdlTokenInputStream stream);

	protected abstract void writeMdxValue(BinaryWriter writer, TYPE value);

	protected abstract void writeMdlValue(MdlTokenOutputStream stream, String prefix, TYPE value);
}
