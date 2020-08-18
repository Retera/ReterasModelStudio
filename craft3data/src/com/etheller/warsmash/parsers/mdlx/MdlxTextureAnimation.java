package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public class MdlxTextureAnimation extends MdlxAnimatedObject {
	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		final long size = reader.readUInt32();

		this.readTimelines(reader, size - 4);
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, this.getByteLength(version));
		this.writeTimelines(stream);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_TRANSLATION:
				this.readTimeline(stream, AnimationMap.KTAT);
				break;
			case MdlUtils.TOKEN_ROTATION:
				this.readTimeline(stream, AnimationMap.KTAR);
				break;
			case MdlUtils.TOKEN_SCALING:
				this.readTimeline(stream, AnimationMap.KTAS);
				break;
			default:
				throw new IllegalStateException("Unknown token in TextureAnimation: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startBlock(MdlUtils.TOKEN_TVERTEX_ANIM_SPACE);
		this.writeTimeline(stream, AnimationMap.KTAT);
		this.writeTimeline(stream, AnimationMap.KTAR);
		this.writeTimeline(stream, AnimationMap.KTAS);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 4 + super.getByteLength(version);
	}
}
