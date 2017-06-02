package com.hiveworkshop.assetripper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.StandardObjectData.WarcraftData;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public final class AssetSourceObject {
	private final String unitId;

	public AssetSourceObject(final String unitId) {
		this.unitId = unitId;
	}

	public void rip(final Codebase mpqs, final WarcraftData standardUnits, final Path outputDirectory,
			final AssetRipperSettings settings) {
		if (!Files.exists(outputDirectory)) {
			throw new IllegalArgumentException("The folder does not exist: " + outputDirectory.toString());
		}
		if (!Files.isDirectory(outputDirectory)) {
			throw new IllegalArgumentException("The target is not a folder: " + outputDirectory.toString());
		}
		final GameObject unit = standardUnits.get(unitId);
		final String modelFile = asMdxExtension(unit.getField("file"));
		final String specialArt = asMdxExtension(unit.getField("Specialart"));
		final String[] missileArts = unit.getField("Missileart").split(",");
		final String icon = unit.getField("Art");
		final String disabledIcon = IconUtils.getDisabledIcon(icon);
		final String scoreScreenIcon = unit.getField("ScoreScreenIcon");
		final String casterUpgradeIcon = unit.getField("Casterupgradeart");
		rip(mpqs, outputDirectory, modelFile, settings, false);
		rip(mpqs, outputDirectory, icon, settings, false);
		rip(mpqs, outputDirectory, disabledIcon, settings, false);
		rip(mpqs, outputDirectory, scoreScreenIcon, settings, false);
		rip(mpqs, outputDirectory, casterUpgradeIcon, settings, false);

		try {
			ripModel(mpqs, outputDirectory, modelFile, settings);
			ripModel(mpqs, outputDirectory, specialArt, settings);
			for (final String missileArt : missileArts) {
				ripModel(mpqs, outputDirectory, asMdxExtension(missileArt), settings);
			}
			ripModel(mpqs, outputDirectory, ModelUtils.getPortrait(modelFile), settings);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String asMdxExtension(String modelFile) {
		if (modelFile.toLowerCase().endsWith(".mdl")) {
			modelFile = modelFile.substring(0, modelFile.lastIndexOf('.')) + ".mdx";
		}
		if (!modelFile.toLowerCase().endsWith(".mdx")) {
			modelFile += ".mdx";
		}
		return modelFile;
	}

	private void ripModel(final Codebase codebase, final Path outputDirectory, final String modelFile,
			final AssetRipperSettings settings) throws IOException {
		rip(codebase, outputDirectory, modelFile, settings, false);
		// Model Textures
		if (codebase.has(modelFile)) {
			final MDL unitModel = new MDL(
					MdxUtils.loadModel(new BlizzardDataInputStream(codebase.getResourceAsStream(modelFile))));
			for (final Bitmap texture : unitModel.getTextures()) {
				if (texture.getReplaceableId() > 0) {
					// it's a team color
				} else {
					final String path = texture.getPath();
					rip(codebase, outputDirectory, path, settings, true);
				}
			}
			for (final Attachment attachment : unitModel.sortedIdObjects(Attachment.class)) {
				if (attachment.getPath() != null) {
					ripModel(codebase, outputDirectory, asMdxExtension(attachment.getPath()), settings);
				}
			}
		}
	}

	private void rip(final Codebase codebase, final Path outputDirectory, final String path,
			final AssetRipperSettings settings, final boolean isModelTexture) {
		if (!codebase.has(path) || (!settings.isIncludeInternal() && ((MpqCodebase) codebase).isBaseGameFile(path))) {
			return;
		}
		try {
			final boolean shouldFlatten = (settings.getFlatten() == 2) || ((settings.getFlatten() == 1)
					&& !isModelTexture/* !path.toLowerCase().endsWith(".blp") */);
			final String finalOutPath = shouldFlatten ? path.substring(path.lastIndexOf('\\') + 1) : path;
			final Path destination = Paths.get(outputDirectory.toString(), finalOutPath);
			final Path parentFolder = destination.getParent();
			if (!Files.exists(parentFolder)) {
				Files.createDirectories(parentFolder);
			}
			Files.copy(codebase.getResourceAsStream(path), destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("ripping: " + path);
	}
}
