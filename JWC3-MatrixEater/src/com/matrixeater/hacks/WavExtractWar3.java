package com.matrixeater.hacks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class WavExtractWar3 {
	private static final List<String> failed = new ArrayList<>();
	private static final List<String> passed = new ArrayList<>();
	private static File root;
	private static File texRoot;

	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: <mutableModelsDirectory>");
			System.exit(-1);
		}
		final File target = new File(args[0]);
		root = target;
		texRoot = new File(target.getParent() + "/" + "wavbase");
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
			if (target.getName().toLowerCase().endsWith(".wav")) {
				try {
					final String relativePath = target.getAbsolutePath().substring(root.getAbsolutePath().length());
					final File textureTarget = new File(texRoot + relativePath);
					textureTarget.getParentFile().mkdirs();
					Files.copy(target.toPath(), textureTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
					passed.add(target.getPath());
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			}
		}
	}

}
