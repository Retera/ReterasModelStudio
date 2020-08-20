package com.matrixeater.hacks.converts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.OldBLPHandler;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public final class RecompileModelsDirectory {
	private static final List<String> failed = new ArrayList<>();
	private static final List<String> passed = new ArrayList<>();

	public static void main(final String[] args) {
		OldBLPHandler.WANT_DESTROY_SAVED_TGAS = false;
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
			if (target.getName().toLowerCase().endsWith(".tga")) {
				try {
					System.out.println(target.getPath());
					final File blpFile = new File(
							target.getPath().substring(0, target.getPath().lastIndexOf('.')) + ".blp");
					blpFile.delete();
					OldBLPHandler.get().convertTGAtoBLP(target, blpFile);
					passed.add(target.getPath());
					target.delete();
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			} else if (target.getName().toLowerCase().endsWith(".mdl")) {
				try {
					System.out.println(target.getPath());
					// MDXHandler.compile(target);
					final File mdxFile = new File(
							target.getPath().substring(0, target.getPath().lastIndexOf('.')) + ".mdx");
					MdxUtils.saveMdx(MdxUtils.loadEditable(target), mdxFile);
					passed.add(target.getPath());
					target.delete();
				} catch (final Exception e) {
					failed.add(target.getPath());
				}
			}
		}
	}

}
