package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataOutputStream;
import de.wc3data.stream.SquishingBlizzardDataOutputStream;

public final class SquishWar3 {
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
		texRoot = new File(target.getParent() + "/" + "CompressorStep2");
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
			if (target.getName().toLowerCase().endsWith(".mdx")) {
				try {
					// final String relativePath =
					// target.getAbsolutePath().substring(root.getAbsolutePath().length());
					// final File copiedTarget = new File(texRoot +
					// relativePath);
					// copiedTarget.getParentFile().mkdirs();
					final EditableModel model = MdxUtils.loadEditableModel(target);
					for (final AnimFlag flag : model.getAllAnimFlags()) {
						flag.linearize();
					}
					model.simplifyKeyframes();
					// model.printTo(target);
					try (BlizzardDataOutputStream out = new SquishingBlizzardDataOutputStream(target)) {
						model.toMdlx().saveMdx(out);
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
					passed.add(target.getPath());
				} catch (final Exception e) {
					failed.add(target.getPath());
					throw new RuntimeException(target.getPath(), e);
				}
			}
		}
	}

}
