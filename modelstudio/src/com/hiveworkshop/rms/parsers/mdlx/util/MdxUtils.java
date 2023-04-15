package com.hiveworkshop.rms.parsers.mdlx.util;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.mdlx.MdlLoadSave;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.MdxLoadSave;

import java.io.*;
import java.nio.ByteBuffer;

public class MdxUtils {
	public static EditableModel loadEditable(final String path, DataSource dataSource) throws IOException {
		return TempOpenModelStuff.createEditableModel(loadMdlx(path, dataSource));
	}

	public static EditableModel loadEditable(final File in) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(in)){
			return TempOpenModelStuff.createEditableModel(loadMdlx(inputStream));
		}
	}
	public static MdlxModel loadMdlx(final File in) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(in)){
			return loadMdlx(inputStream);
		}
	}

	public static MdlxModel loadMdlx(final String path, DataSource dataSource) throws IOException {
		if(dataSource == null) dataSource = GameDataFileSystem.getDefault();
		try (InputStream reader = dataSource.getResourceAsStream(path)) {
			return loadMdlx(reader);
		}
	}
	private static MdlxModel loadMdlx(final InputStream inputStream) throws IOException {
		return modelFrom(ByteBuffer.wrap(inputStream.readAllBytes()));
	}

	public static EditableModel loadEditable(final ByteBuffer buffer) {
		return TempOpenModelStuff.createEditableModel(modelFrom(buffer));
	}

	public static MdlxModel modelFrom(final ByteBuffer buffer) {
		// MDX files start with "MDLX".
		MdlxModel mdlxModel = new MdlxModel();
		if (buffer.get(0) == 'M' && buffer.get(1) == 'D' && buffer.get(2) == 'L' && buffer.get(3) == 'X') {
			MdxLoadSave.loadMdx(mdlxModel, buffer);
		} else {
			MdlLoadSave.loadMdl(mdlxModel, buffer);
		}
		return mdlxModel;
	}

	public static void saveMdl(final MdlxModel model, final File file) throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(MdlLoadSave.saveMdl(model).array());
		}
	}

	public static void saveMdl(final EditableModel model, final File file) throws IOException {
		saveMdl(TempSaveModelStuff.toMdlx(model), file);
	}

	public static void saveMdx(final MdlxModel model, final File file) throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(MdxLoadSave.saveMdx(model).array());
		}
	}

	public static void saveMdx(final EditableModel editableModel, final File file) throws IOException {
		saveMdx(TempSaveModelStuff.toMdlx(editableModel), file);
	}
}
