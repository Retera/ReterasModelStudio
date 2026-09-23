package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxExtent {
	public float boundsRadius = 0;
	public float[] min = new float[3];
	public float[] max = new float[3];

	public void readMdx(final BinaryReader reader) {
		boundsRadius = reader.readFloat32();
		reader.readFloat32Array(min);
		reader.readFloat32Array(max);
	}

	public void writeMdx(final BinaryWriter writer) {
		writer.writeFloat32(boundsRadius);
		writer.writeFloat32Array(min);
		writer.writeFloat32Array(max);
	}

	public void writeMdl(final MdlTokenOutputStream stream) {
		if ((min[0] != 0) || (min[1] != 0) || (min[2] != 0)) {
			stream.writeFloatArrayAttrib(MdlUtils.TOKEN_MINIMUM_EXTENT, min);
		}
		
		if ((max[0] != 0) || (max[1] != 0) || (max[2] != 0)) {
			stream.writeFloatArrayAttrib(MdlUtils.TOKEN_MAXIMUM_EXTENT, max);
		}

		if (boundsRadius != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_BOUNDSRADIUS, boundsRadius);
		}
	}
}
