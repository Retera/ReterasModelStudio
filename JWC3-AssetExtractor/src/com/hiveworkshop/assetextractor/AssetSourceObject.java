package com.hiveworkshop.assetextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.wc3.mdx.AttachmentChunk;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mdx.TextureChunk;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.GameObject;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public final class AssetSourceObject {
	private final String unitId;

	public AssetSourceObject(final String unitId) {
		this.unitId = unitId;
	}

	public void extract(final Codebase mpqs, final ObjectData standardUnits, final Path outputDirectory,
			final AssetExtractorSettings settings) {
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
		final String icon = asExtension(unit.getField("Art"), ".blp");
		final String disabledIcon = asExtension(IconUtils.getDisabledIcon(icon), ".blp");
		final String scoreScreenIcon = asExtension(unit.getField("ScoreScreenIcon"), ".blp");
		final String casterUpgradeIcon = asExtension(unit.getField("Casterupgradeart"), ".blp");
		extract(mpqs, outputDirectory, modelFile, settings, false);
		extract(mpqs, outputDirectory, icon, settings, settings.getFlatten() == 3);
		extract(mpqs, outputDirectory, disabledIcon, settings, settings.getFlatten() == 3);
		extract(mpqs, outputDirectory, scoreScreenIcon, settings, settings.getFlatten() == 3);
		extract(mpqs, outputDirectory, casterUpgradeIcon, settings, settings.getFlatten() == 3);

		try {
			extractModel(mpqs, outputDirectory, modelFile, settings);
			extractModel(mpqs, outputDirectory, specialArt, settings);
			for (final String missileArt : missileArts) {
				extractModel(mpqs, outputDirectory, asMdxExtension(missileArt), settings);
			}
			extractModel(mpqs, outputDirectory, ModelUtils.getPortrait(modelFile), settings);

			// abilities
			extractModel(mpqs, outputDirectory, asMdxExtension(unit.getField("SpecialArt")), settings);
			extractModel(mpqs, outputDirectory, asMdxExtension(unit.getField("TargetArt")), settings);
			extractModel(mpqs, outputDirectory, asMdxExtension(unit.getField("Areaeffectart")), settings);

		} catch (final IOException e) {
			throw new RuntimeException(e);
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

	public static String asMdxExtension(String modelFile) {
		if (modelFile.toLowerCase().endsWith(".mdl")) {
			modelFile = modelFile.substring(0, modelFile.lastIndexOf('.')) + ".mdx";
		}
		if (!modelFile.toLowerCase().endsWith(".mdx")) {
			modelFile += ".mdx";
		}
		return modelFile;
	}

	public static void extractModel(final Codebase codebase, final Path outputDirectory, final String modelFile,
			final AssetExtractorSettings settings) throws IOException {
		extract(codebase, outputDirectory, modelFile, settings, false);
		// Model Textures
		if (codebase.has(modelFile)) {
			final MdxModel mdxModel = MdxUtils
					.loadModel(new BlizzardDataInputStream(codebase.getResourceAsStream(modelFile)));
			// final MDL unitModel = new MDL(
			// mdxModel);
			if (mdxModel.textureChunk != null && mdxModel.textureChunk.texture != null) {
				for (final TextureChunk.Texture texture : mdxModel.textureChunk.texture) {
					if (texture.replaceableId > 0) {
						// it's a team color
					} else {
						final String path = texture.fileName;
						extract(codebase, outputDirectory, path, settings, true);
					}
				}
			}
			// for (final Bitmap texture : unitModel.getTextures()) {
			// if (texture.getReplaceableId() > 0) {
			// // it's a team color
			// } else {
			// final String path = texture.getPath();
			// extract(codebase, outputDirectory, path, settings, true);
			// }
			// }
			if (mdxModel.attachmentChunk != null && mdxModel.attachmentChunk.attachment != null) {
				for (final AttachmentChunk.Attachment attachment : mdxModel.attachmentChunk.attachment) {
					if (attachment.unknownName_modelPath != null) {
						extractModel(codebase, outputDirectory, asMdxExtension(attachment.unknownName_modelPath),
								settings);
					}
				}
				// for (final Attachment attachment :
				// unitModel.sortedIdObjects(Attachment.class)) {
				// if (attachment.getPath() != null) {
				// extractModel(codebase, outputDirectory,
				// asMdxExtension(attachment.getPath()), settings);
				// }
				// }
			}
		}
	}

	public static void extract(final Codebase codebase, final Path outputDirectory, final String path,
			final AssetExtractorSettings settings, final boolean isModelTexture) {
		if (!codebase.has(path) || (!settings.isIncludeInternal() && ((MpqCodebase) codebase).isBaseGameFile(path))) {
			return;
		}
		try {
			final boolean shouldFlatten = (settings.getFlatten() == 2) || ((settings.getFlatten() == 1
					|| settings.getFlatten() == 3)
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
		System.out.println("extracting: " + path);
	}
}
