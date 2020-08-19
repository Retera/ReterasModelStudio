package com.hiveworkshop.wc3.mdx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.hiveworkshop.wc3.mdl.EditableModel;

public class MdxUtils {
	public static MdlxModel loadMdlx(final InputStream in) throws IOException {
		return new MdlxModel(ByteBuffer.wrap(in.readAllBytes()));
	}

	public static void saveMdx(final MdlxModel model, final OutputStream out) throws IOException {
		out.write(model.saveMdx().array());
	}

	public static void saveMdl(final MdlxModel model, final OutputStream out) throws IOException {
		out.write(model.saveMdl().array());
	}

	public static void saveMdl(final MdlxModel model, final File out) throws IOException {
		saveMdl(model, new FileOutputStream(out));
	}

	public static EditableModel loadEditable(final InputStream in) throws IOException {
		return new EditableModel(loadMdlx(in));
	}

	public static EditableModel loadEditable(final ByteBuffer buffer) {
		return new EditableModel(new MdlxModel(buffer));
	}

	public static EditableModel loadEditable(final File in) throws IOException {
		return new EditableModel(loadMdlx(new FileInputStream(in)));
	}

	public static void saveMdx(final EditableModel editableModel, final OutputStream out) throws IOException {
		saveMdx(editableModel.toMdlx(), out);
	}

	public static void saveMdx(final EditableModel editableModel, final File out) throws IOException {
		saveMdx(editableModel, new FileOutputStream(out));
	}
}
