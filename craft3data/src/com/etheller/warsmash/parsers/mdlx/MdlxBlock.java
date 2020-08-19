package com.etheller.warsmash.parsers.mdlx;

import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;

public interface MdlxBlock {
	void readMdx(final BinaryReader reader, final int version);

	void writeMdx(final BinaryWriter writer, final int version);

	void readMdl(final MdlTokenInputStream stream, final int version);

	void writeMdl(final MdlTokenOutputStream stream, final int version);
}
