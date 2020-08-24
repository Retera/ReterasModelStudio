package com.hiveworkshop.rms.parsers.mdlx.util;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;

import java.io.*;
import java.nio.ByteBuffer;

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

	public static void saveMdl(final EditableModel model, final File out) throws IOException {
		saveMdl(model.toMdlx(), new FileOutputStream(out));
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
