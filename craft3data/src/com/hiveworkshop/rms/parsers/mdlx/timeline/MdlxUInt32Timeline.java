package com.hiveworkshop.rms.parsers.mdlx.timeline;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

public final class MdlxUInt32Timeline extends MdlxTimeline<long[]> {
	public MdlxUInt32Timeline(War3ID name){
		super(name);
	}
	@Override
	protected int valueSize() {
		return 1;
	}

	@Override
	protected long[] readMdxValue(final BinaryReader reader) {
		return new long[]{reader.readUInt32()};
	}

	@Override
	protected long[] readMdlValue(final MdlTokenInputStream stream) {
		return new long[]{stream.readUInt32()};
	}

	@Override
	protected void writeMdxValue(final BinaryWriter writer, final long[] uint32) {
		writer.writeUInt32(uint32[0]);
	}

	@Override
	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final long[] uint32) {
		stream.writeKeyframe(prefix, uint32[0]);
	}

	@Override
	public void initLists(int size){
		frames = new long[size];
		values = new long[size][];
		if(interpolationType.tangential()){
			inTans = new long[size][];
			outTans = new long[size][];
		}
	}

	public long[][] getEntryAt(int i){
		long[][] entry = new long[3][];
		entry[0] = values[i];
		if(interpolationType.tangential()){
			entry[1] = inTans[i];
			entry[2] = outTans[i];
		} else {
			entry[1] = new long[valueSize()];
			entry[2] = new long[valueSize()];
		}
		return entry;
	}
}
