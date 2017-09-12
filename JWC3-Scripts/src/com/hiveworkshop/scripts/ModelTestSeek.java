package com.hiveworkshop.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;

import mpq.MPQException;

public class ModelTestSeek {

	public static void main(final String[] args) {
		final List<String> modelPaths = new ArrayList<>();
		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			final LoadedMPQ loadMPQ = mpqCodebase.loadMPQ(Paths.get("input/Demons and Wizards.w3x"));
			final InputStream resourceAsStream = mpqCodebase.getResourceAsStream("(listfile)");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.toLowerCase().endsWith(".mdx")) {
						System.out.println("PARSING - " + line);
						final MDL model;
						try {
							model = MDL.read(mpqCodebase.getFile(line));
							if (model.getGeosetAnims().size() > model.getGeosets().size()) {
								modelPaths.add(line);
								Files.createDirectories(Paths.get("output/" + line).getParent(), new FileAttribute[0]);
								Files.copy(mpqCodebase.getResourceAsStream(line), Paths.get("output/" + line),
										StandardCopyOption.REPLACE_EXISTING);
							}
						} catch (final Exception exc) {
							ExceptionPopup.display(line, exc);
							Files.createDirectories(Paths.get("output/" + line).getParent(), new FileAttribute[0]);
							Files.copy(mpqCodebase.getResourceAsStream(line), Paths.get("output/" + line),
									StandardCopyOption.REPLACE_EXISTING);
						}
					}
				}
			}
		} catch (final MPQException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		for (final String path : modelPaths) {
			System.out.println(path);
		}
	}

}
