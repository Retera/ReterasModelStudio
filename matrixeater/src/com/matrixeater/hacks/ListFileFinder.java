package com.matrixeater.hacks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import mpq.MPQException;

public class ListFileFinder {

	public static void main(final String[] args) {
		try {
			final LoadedMPQ mpq = MpqCodebase.get().loadMPQ(Paths.get(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Download\\TemplarNaruto\\Disect.w3x"));

			final Warcraft3MapObjectData data = Warcraft3MapObjectData.load(false);

			final Set<String> usedFileList = new HashSet<>();
			try (PrintWriter writer = new PrintWriter(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Download\\TemplarNaruto\\objectEditorListfile.txt")) {
				for (final MutableObjectData unitData : data.getDatas()) {
					for (final War3ID unitId : unitData.keySet()) {
						final MutableGameObject unit = unitData.get(unitId);
						if (unit == null) {
							System.err.println("no unit for id: " + unitId);
							continue;
						}
						final List<War3ID> idsToCheck = new ArrayList<>();
						final ObjectData sourceSLKMetaData = unitData.getSourceSLKMetaData();
						for (final String metaDataId : sourceSLKMetaData.keySet()) {
							final GameObject metaDataEntry = sourceSLKMetaData.get(metaDataId);
							final String fieldType = metaDataEntry.getField("type");
							if ("model".equals(fieldType) || "modelList".equals(fieldType)
									|| "icon".equals(fieldType)) {
								idsToCheck.add(War3ID.fromString(metaDataId));
							}
						}
						check(usedFileList, mpq, unit, idsToCheck.toArray(new War3ID[idsToCheck.size()]));
					}
				}
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(
						MpqCodebase.get().getResourceAsStream("Scripts\\war3map.j"), Charset.forName("utf-8")))) {
					String line;
					while ((line = reader.readLine()) != null) {
						final String startingLine = line;
						int firstQuote = line.indexOf('"');
						while (firstQuote >= 0) {
							final String afterQuote = line.substring(firstQuote + 1);
							final int nextQuote = afterQuote.indexOf('"');
							if (nextQuote == -1) {
								System.err.println("invalid quoted string: " + startingLine);
								break;
							}
							final String betweenQuotes = afterQuote.substring(nextQuote);
							checkString(usedFileList, mpq, betweenQuotes);
							line = afterQuote.substring(nextQuote + 1);
							firstQuote = line.indexOf('"');
						}
					}
				}
				for (final String str : MpqCodebase.get().getMergedListfile()) {
					if (mpq.has(str)) {
						usedFileList.add(str);
					}
				}
				for (final String file : usedFileList) {
					writer.println(file);
				}
				final File outputDirectory = new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Download\\TemplarNaruto\\JavaArchive");

				for (final String file : usedFileList) {
//					writer.println(file);
					try (InputStream stream = MpqCodebase.get().getResourceAsStream(file)) {
						Files.createDirectories(outputDirectory.toPath().resolve(file).getParent());
						Files.copy(stream, outputDirectory.toPath().resolve(file), StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		} catch (final MPQException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void check(final Set<String> usedFileList, final LoadedMPQ mpq, final MutableGameObject object,
			final War3ID... war3ids) {
		for (final War3ID id : war3ids) {
			final String fieldAsString = object.getFieldAsString(id, 0);
			checkString(usedFileList, mpq, fieldAsString);
		}
	}

	private static void checkString(final Set<String> usedFileList, final LoadedMPQ mpq, final String fieldAsString) {
		if (mpq.has(fieldAsString)) {
			usedFileList.add(fieldAsString);
		} else if (mpq.has(extension(fieldAsString, "mdx"))) {
			usedFileList.add(extension(fieldAsString, "mdx"));
			try {
				final EditableModel modelFile = MdxUtils.loadEditableModel(MpqCodebase.get().getFile(extension(fieldAsString, "mdx")));
				for (final Bitmap tex : modelFile.getTextures()) {
					if ((tex.getPath() != null) && (tex.getPath().length() > 0)) {
						checkString(usedFileList, mpq, tex.getPath());
					}
				}
				for (final Attachment atc : modelFile.sortedIdObjects(Attachment.class)) {
					if ((atc.getPath() != null) && (atc.getPath().length() > 0)) {
						checkString(usedFileList, mpq, atc.getPath());
					}
				}
			} catch (final Exception e) {
				System.err.println("unable to parse model: " + fieldAsString);
				e.printStackTrace();
			}
		} else if (mpq.has(extension(fieldAsString, "blp"))) {
			usedFileList.add(extension(fieldAsString, "blp"));
		} else {
			if (fieldAsString.endsWith(".blp")) {
				final String fileName = fieldAsString.substring(fieldAsString.lastIndexOf('\\') + 1);
				final String disabledName = "ReplaceableTextures\\CommandButtonsDisabled\\DIS" + fileName;
				if (mpq.has(disabledName)) {
					usedFileList.add(disabledName);
				}
			}
		}
	}

	public static String extension(final String input, final String ext) {
		final int dotIndex = input.lastIndexOf('.');
		if (dotIndex == -1) {
			return input;
		} else {
			return input.substring(0, dotIndex) + "." + ext;
		}
	}
}
