package com.hiveworkshop.rms.parsers.mdlx.util;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;

import java.io.*;
import java.nio.ByteBuffer;

public class MdxUtils {
	public static MdlxModel loadMdlx(final InputStream inputStream) throws IOException {
		return new MdlxModel(ByteBuffer.wrap(inputStream.readAllBytes()));
	}

	public static void saveMdx(final MdlxModel model, final OutputStream outputStream) throws IOException {
		outputStream.write(model.saveMdx().array());
	}

	public static void saveMdl(final MdlxModel model, final OutputStream outputStream) throws IOException {
		outputStream.write(model.saveMdl().array());
	}

	public static void saveMdl(final MdlxModel model, final File file) throws IOException {
		saveMdl(model, new FileOutputStream(file));
	}

	public static void saveMdl(final EditableModel model, final File file) throws IOException {
		saveMdl(TempSaveModelStuff.toMdlx(model), new FileOutputStream(file));
	}

	public static EditableModel loadEditable(final InputStream inputStream) throws IOException {
		return TempOpenModelStuff.createEditableModel(loadMdlx(inputStream));
	}

	public static EditableModel loadEditable(final ByteBuffer buffer) {
		return TempOpenModelStuff.createEditableModel(new MdlxModel(buffer));
	}

	public static EditableModel loadEditable(final File in) throws IOException {
		return TempOpenModelStuff.createEditableModel(loadMdlx(new FileInputStream(in)));
	}

	public static void saveMdx(final EditableModel editableModel, final OutputStream outputStream) throws IOException {
		saveMdx(TempSaveModelStuff.toMdlx(editableModel), outputStream);
	}

	public static void saveMdx(final EditableModel editableModel, final File file) throws IOException {
		saveMdx(editableModel, new FileOutputStream(file));
	}
}
