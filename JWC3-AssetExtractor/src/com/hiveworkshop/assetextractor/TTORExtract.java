package com.hiveworkshop.assetextractor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

public class TTORExtract {

	public static void main(final String[] args) {
		final Set<String> possibleFiles = new HashSet<>();
		final Path ttorPath = Paths.get("C:\\Users\\Eric\\Documents\\Warcraft\\Modding\\ttor\\ttor.mpq");
		// final MapAssetExtractor mapAssetExtractor = new
		// MapAssetExtractor(ttorPath);
		// final WarcraftData unitData = mapAssetExtractor.getUnitData();
		// for (final String key : unitData.keySet()) {
		// final GameObject element = unitData.get(key);
		// processElement(possibleFiles, element);
		// }
		final DataTable buffs = DataTable.getBuffs();
		final DataTable mainTable = DataTable.get();
		final DataTable items = DataTable.getItems();
		final DataTable doodads = DataTable.getDoodads();
		for (final String key : mainTable.keySet()) {
			final Element element = mainTable.get(key);
			processElement(possibleFiles, element);
			// final String modelFile = asMdxExtension(unit.getField("file"));
			// final String specialArt =
			// asMdxExtension(unit.getField("Specialart"));
			// final String[] missileArts =
			// unit.getField("Missileart").split(",");
			// final String icon = unit.getField("Art");
			// final String disabledIcon = IconUtils.getDisabledIcon(icon);
			// final String scoreScreenIcon = unit.getField("ScoreScreenIcon");
			// final String casterUpgradeIcon =
			// unit.getField("Casterupgradeart");

		}
		for (final String key : buffs.keySet()) {
			final Element element = buffs.get(key);
			processElement(possibleFiles, element);

		}
		for (final String key : items.keySet()) {
			final Element element = items.get(key);
			processElement(possibleFiles, element);
		}
		for (final String key : doodads.keySet()) {
			final Element element = doodads.get(key);
			processElement(possibleFiles, element);
		}
		for (final String str : possibleFiles) {
			System.out.println(str);
		}

	}

	private static void processElement(final Set<String> possibleFiles, final GameObject element) {
		final String icon = asExtension(element.getField("Art"), ".blp");
		possibleFiles.add(icon);
		possibleFiles.add(IconUtils.getDisabledIcon(icon));
		final String modelFile = element.getField("file");
		possibleFiles.add(asExtension(modelFile, ".mdx"));
		try {
			possibleFiles.add(asExtension(ModelUtils.getPortrait(modelFile), ".mdx"));
		} catch (final Exception e) {

		}
		possibleFiles.add(asExtension(element.getField("ScoreScreenIcon"), ".blp"));
		possibleFiles.add(asExtension(element.getField("Specialart"), "..mdx"));
		possibleFiles.add(asExtension(element.getField("Casterupgradeart"), ".blp"));
		final String[] strings = element.getField("Missileart").split(",");
		for (final String s : strings) {
			possibleFiles.add(asExtension(s, ".blp"));
		}
	}

	public static String asExtension(String modelFile, final String extension) {
		if (modelFile.contains(".")) {
			modelFile = modelFile.substring(0, modelFile.lastIndexOf('.')) + extension;
		}
		if (!modelFile.toLowerCase().endsWith(extension)) {
			modelFile += extension;
		}
		return modelFile;
	}

}
