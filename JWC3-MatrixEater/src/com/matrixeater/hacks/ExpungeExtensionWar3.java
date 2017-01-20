package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ExpungeExtensionWar3 {
	private static final List<String> failed = new ArrayList<>();
	private static final List<String> passed = new ArrayList<>();
	private static File root;

	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: <mutableModelsDirectory>");
			System.exit(-1);
		}
		final File target = new File(args[0]);
		root = target;
		squish(target);
		System.out.println("Passed: " + passed.size());
		System.out.println("Failed: " + failed.size());
		System.out.println("Fail list:");
		for (final String failedName : failed) {
			System.out.println(failedName);
		}
	}

	private static void squish(final File target) {
		if (target.isDirectory()) {
			for (final File file : target.listFiles()) {
				squish(file);
			}
		} else {
			if (target.getName().toLowerCase().endsWith(".mp3") || target.getName().toLowerCase().endsWith(".wav")) {
				try {
					target.delete();
					passed.add(target.getPath());
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			}
		}
	}

}
