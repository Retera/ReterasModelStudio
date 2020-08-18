package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public class MdlxEventObject extends MdlxGenericObject {
	private static final War3ID KEVT = War3ID.fromString("KEVT");

	public int globalSequenceId = -1;
	public long[] keyFrames = { 1 };

	public MdlxEventObject() {
		super(0x400);
	}

	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		super.readMdx(reader, version);

		reader.readInt32(); // KEVT skipped

		final long count = reader.readUInt32();

		this.globalSequenceId = reader.readInt32();

		this.keyFrames = new long[(int) count];

		for (int i = 0; i < count; i++) {
			this.keyFrames[i] = reader.readInt32();
		}
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		super.writeMdx(stream, version);
		ParseUtils.writeWar3ID(stream, KEVT);
		ParseUtils.writeUInt32(stream, this.keyFrames.length);
		stream.writeInt(this.globalSequenceId);
		for (int i = 0; i < this.keyFrames.length; i++) {
			ParseUtils.writeUInt32(stream, this.keyFrames[i]);
		}
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			if (MdlUtils.TOKEN_EVENT_TRACK.equals(token)) {
				this.keyFrames = new long[stream.readInt()];
				stream.readIntArray(this.keyFrames);
			}
			else {
				throw new RuntimeException("Unknown token in EventObject " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startObjectBlock(MdlUtils.TOKEN_EVENT_OBJECT, this.name);
		writeGenericHeader(stream);
		stream.startBlock(MdlUtils.TOKEN_EVENT_TRACK, this.keyFrames.length);

		for (final long keyFrame : this.keyFrames) {
			stream.writeFlagUInt32(keyFrame);
		}

		stream.endBlock();

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 12 + (this.keyFrames.length * 4) + super.getByteLength(version);
	}
}
