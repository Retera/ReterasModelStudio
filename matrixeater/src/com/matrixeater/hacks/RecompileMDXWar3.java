package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.MDXHandler;

public final class RecompileMDXWar3 {
	private static final List<String> failed = new ArrayList<>();
	private static final List<String> passed = new ArrayList<>();

	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: <mutableModelsDirectory>");
			System.exit(-1);
		}
		squish(new File(args[0]));
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
			if (target.getName().toLowerCase().endsWith(".mdx")
					&& (target.getPath().toLowerCase().contains("units")
							|| target.getPath().toLowerCase().contains("buildings"))
					&& !target.getPath().toLowerCase().contains("portrait")) {
				try {
					System.out.println(target.getPath());
					MDXHandler.compile(MDXHandler.convert(target));
					passed.add(target.getPath());
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			}
		}
	}

}
