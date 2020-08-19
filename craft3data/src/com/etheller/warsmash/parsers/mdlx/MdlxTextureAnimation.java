package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public class MdlxTextureAnimation extends MdlxAnimatedObject {
	public void readMdx(final BinaryReader reader, final int version) {
		final long size = reader.readUInt32();

		this.readTimelines(reader, size - 4);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(this.getByteLength(version));

		this.writeTimelines(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
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
				throw new RuntimeException("Unknown token in TextureAnimation: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
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
