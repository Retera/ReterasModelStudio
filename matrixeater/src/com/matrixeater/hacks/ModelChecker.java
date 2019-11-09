package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.MDL;

public class ModelChecker {
	private static List<String> matchingModels = new ArrayList<>();

	public static void main(final String[] args) {
		traverse(new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Altered Melee\\HFGen\\(2)HFAlteracIsle_FileDump.w3x"));
		for (final String modelName : matchingModels) {
			System.out.println(modelName);
		}
	}

	public static void traverse(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else {
			final String lowerPath = file.getPath().toLowerCase();
			if (lowerPath.endsWith(".mdx") || lowerPath.endsWith(".mdl")) {
				boolean usesSecondLayer = false;
				try {
					final MDL model = MDL.read(file);
					for (final Geoset geoset : model.getGeosets()) {
						if (geoset.getUVLayers().size() >= 2) {
							usesSecondLayer = true;
						}
					}
					if (usesSecondLayer) {
						matchingModels.add(file.getPath());
					}
				} catch (final Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}
}
