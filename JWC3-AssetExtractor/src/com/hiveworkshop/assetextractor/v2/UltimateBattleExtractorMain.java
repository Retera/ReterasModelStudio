package com.hiveworkshop.assetextractor.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase.LoadedMPQ;
import com.hiveworkshop.wc3.units.StandardObjectData;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.WTSFile;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

import de.wc3data.stream.BlizzardDataInputStream;
import mpq.MPQException;

public class UltimateBattleExtractorMain {
	private static final String[] neededFiles = { "war3map.w3u", "war3map.w3t", "war3map.w3b", "war3map.w3d",
			"war3map.w3a", "war3map.w3h", "war3map.w3q", "war3mapMisc.txt", "war3mapSkin.txt", "Scripts\\war3map.j",
			"war3mapPreview.tga" };

	private static final String[] ubJassModelRefs = { "FreezingRing.mdx", "EnergyBondCaster.mdx", "Impact.mdx",
			"BlueHolyBoltSpecialArt.mdx" };

	public static void main(final String[] args) {

		try {
			final MpqCodebase mpqCodebase = MpqCodebase.get();
			final LoadedMPQ ubMpq = mpqCodebase.loadMPQ(Paths.get(args[0]));
			// for (final String path : neededFiles) {
			// final InputStream resourceAsStream = mpqCodebase.getResourceAsStream(path);
			// if (resourceAsStream == null) {
			// System.err.println("Skipping " + path + " because map does not have it!");
			// continue;
			// }
			// if (path.endsWith(".j")) {
			// final File scriptsFolder = new File(args[1] + File.separatorChar + "Scripts");
			// if (!scriptsFolder.exists()) {
			// scriptsFolder.mkdirs();
			// }
			// try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
			// PrintWriter writer = new PrintWriter(args[1] + File.separatorChar + path)) {
			// String line;
			// while ((line = reader.readLine()) != null) {
			// writer.println(line);
			// final boolean hasMdx = line.contains(".mdx\"");
			// final boolean hasMdl = line.contains(".mdl\"");
			// if (hasMdx || line.contains(".ai\"") || hasMdl) {
			// final String extension = hasMdl ? "mdl" : hasMdx ? "mdx" : "ai";
			// String embeddedPath = line.substring(0, line.lastIndexOf("." + extension + "\""));
			// embeddedPath = embeddedPath.substring(embeddedPath.lastIndexOf('"') + 1).replace("\\\\",
			// "\\") + "." + (hasMdl ? "mdx" : extension);
			// InputStream embeddedResourceAsStream = mpqCodebase.getResourceAsStream(embeddedPath);
			// if (!ubMpq.has(embeddedPath) || embeddedResourceAsStream == null) {
			// if (embeddedPath.endsWith(".ai")) {
			// embeddedPath = "Scripts\\" + embeddedPath;
			// embeddedResourceAsStream = mpqCodebase.getResourceAsStream(embeddedPath);
			// }
			// if (embeddedPath.startsWith("war3mapImported\\")) {
			// System.out.println("Fixing path \"" + embeddedPath
			// + "\" which was apparently from a previous version");
			// embeddedPath = embeddedPath.substring("war3mapImported\\".length());
			// embeddedResourceAsStream = mpqCodebase.getResourceAsStream(embeddedPath);
			// }
			// if (!ubMpq.has(embeddedPath) || embeddedResourceAsStream == null) {
			// System.err
			// .println("Skipping " + embeddedPath + " because map does not have it!");
			// continue;
			// }
			// }
			// final Path targetPath = Paths.get(args[1]).resolve(embeddedPath);
			// if (!Files.exists(targetPath.getParent())) {
			// Files.createDirectories(targetPath.getParent());
			// }
			// Files.copy(embeddedResourceAsStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			// }
			// }
			// }
			// } else {
			// Files.copy(resourceAsStream, Paths.get(args[1]).resolve(path), StandardCopyOption.REPLACE_EXISTING);
			// }
			// }
			final WTSFile stringsFile = new WTSFile(mpqCodebase.getResourceAsStream("war3map.wts"));

			final InputStream unitDataStream = mpqCodebase.getResourceAsStream("war3map.w3u");
			final War3ObjectDataChangeset unitDataChangeset = new War3ObjectDataChangeset('u');
			unitDataChangeset.load(new BlizzardDataInputStream(unitDataStream), stringsFile, true);
			final MutableObjectData unitData = new MutableObjectData(WorldEditorDataType.UNITS,
					StandardObjectData.getStandardUnits(), StandardObjectData.getStandardUnitMeta(), unitDataChangeset);

			final InputStream itemDataStream = mpqCodebase.getResourceAsStream("war3map.w3t");
			final War3ObjectDataChangeset itemDataChangeset = new War3ObjectDataChangeset('t');
			itemDataChangeset.load(new BlizzardDataInputStream(itemDataStream), stringsFile, true);
			final MutableObjectData itemData = new MutableObjectData(WorldEditorDataType.ITEM,
					StandardObjectData.getStandardItems(), StandardObjectData.getStandardUnitMeta(), itemDataChangeset);

			final InputStream abilityDataStream = mpqCodebase.getResourceAsStream("war3map.w3a");
			final War3ObjectDataChangeset abilityDataChangeset = new War3ObjectDataChangeset('a');
			abilityDataChangeset.load(new BlizzardDataInputStream(abilityDataStream), stringsFile, true);
			final MutableObjectData abilityData = new MutableObjectData(WorldEditorDataType.ABILITIES,
					StandardObjectData.getStandardAbilities(), StandardObjectData.getStandardAbilityMeta(),
					abilityDataChangeset);

			final InputStream buffDataStream = mpqCodebase.getResourceAsStream("war3map.w3h");
			final War3ObjectDataChangeset buffDataChangeset = new War3ObjectDataChangeset('h');
			buffDataChangeset.load(new BlizzardDataInputStream(buffDataStream), stringsFile, true);
			final MutableObjectData buffData = new MutableObjectData(WorldEditorDataType.BUFFS_EFFECTS,
					StandardObjectData.getStandardAbilityBuffs(), StandardObjectData.getStandardAbilityBuffMeta(),
					buffDataChangeset);

			final InputStream upgradeDataStream = mpqCodebase.getResourceAsStream("war3map.w3q");
			final War3ObjectDataChangeset upgradeDataChangeset = new War3ObjectDataChangeset('q');
			upgradeDataChangeset.load(new BlizzardDataInputStream(upgradeDataStream), stringsFile, true);
			final MutableObjectData upgradeData = new MutableObjectData(WorldEditorDataType.UPGRADES,
					StandardObjectData.getStandardUpgrades(), StandardObjectData.getStandardUpgradeMeta(),
					upgradeDataChangeset);

			final Warcraft3MapObjectData warcraft3MapObjectData = new Warcraft3MapObjectData(unitData, itemData, null,
					null, abilityData, buffData, upgradeData);
			final GenericMapObjectDataExtractor extractor = new GenericMapObjectDataExtractor(mpqCodebase,
					warcraft3MapObjectData, Paths.get(args[1]));
			extractor.extract();

		} catch (final MPQException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		// try (MapAssetExtractor extractor = new MapAssetExtractor(Paths.get(args[0]))) {
		// UnitOptionPanel.dropRaceCache();
		// DataTable.dropCache();
		// ModelOptionPanel.dropCache();
		// WEString.dropCache();
		// Resources.dropCache();
		// BLPHandler.get().dropCache();
		// final WarcraftData unitData = extractor.getUnitData();
		// for (final String key : unitData.keySet()) {
		// extractor.extractObject(key, Paths.get(args[1]),
		// new AssetExtractorSettings(false, AssetExtractorSettings.FLATTEN_BY_RETAIN_ALL_PATHS));
		// }
		// extractor.close();
		// } catch (final Throwable exc) {
		// exc.printStackTrace();
		// }
	}

}
