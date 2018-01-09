package com.matrixeater.hacks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.BLPHandler;

public final class BLPExtractorCompressorWar3 {
	private static final List<String> failed = new ArrayList<>();
	private static final List<String> passed = new ArrayList<>();
	private static File root;
	private static File texRoot;
	private static File compressRoot;
	private static int count = 0;

	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: <mutableModelsDirectory>");
			System.exit(-1);
		}
		final File target = new File(args[0]);
		root = target;
		texRoot = new File(target.getParent() + "/Compressor");
		compressRoot = new File(target.getParent() + "/CompressorOut");
		operate(target);
		System.out.println("Passed: " + passed.size());
		System.out.println("Failed: " + failed.size());
		System.out.println("Fail list:");
		for (final String failedName : failed) {
			System.out.println(failedName);
		}
	}

	private static void operate(final File target) {
		if (target.isDirectory()) {
			for (final File file : target.listFiles()) {
				operate(file);
			}
		} else {
			if (target.getName().toLowerCase().endsWith(".blp")) {
				count++;
				if (count % 300 == 0) {
					System.out.println(count);
				}
				try {
					final String relativePath = target.getAbsolutePath().substring(root.getAbsolutePath().length());
					final File textureTarget = new File(texRoot + relativePath);
					final File compressTarget = new File(compressRoot + relativePath);
					textureTarget.getParentFile().mkdirs();
					compressTarget.getParentFile().mkdirs();
					Files.copy(target.toPath(), textureTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
					// final BufferedImage blpImage =
					// BlpFile.read(textureTarget);
					final boolean generateMipMaps = relativePath.toLowerCase().contains("\\units")
							|| relativePath.toLowerCase().contains("\\abilities")
							|| relativePath.toLowerCase().contains("\\buildings")
							|| relativePath.toLowerCase().contains("\\textures")
							|| relativePath.toLowerCase().contains("\\environment")
							|| relativePath.toLowerCase().contains("\\doodads")
							|| relativePath.toLowerCase().contains("\\sharedmodels")
							|| relativePath.toLowerCase().contains("\\objects\\inventoryitems")
							|| relativePath.toLowerCase().contains("\\replaceabletextures\\splats")
							|| relativePath.toLowerCase().startsWith("\\ReplaceableTextures\\Splats")
							|| !relativePath.toLowerCase().substring(1).contains("\\");// ReplaceableTextures\Splats
					// BlpFile.writePalettedBLP(blpImage, compressTarget,
					// blpImage.getColorModel().hasAlpha(),
					// generateMipMaps, false);
					BLPHandler.get().compressBLPHopefullyALot(textureTarget, compressTarget, generateMipMaps);
					passed.add(target.getPath());
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			}
		}
	}

}
