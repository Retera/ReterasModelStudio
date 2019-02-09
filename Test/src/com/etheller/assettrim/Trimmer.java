package com.etheller.assettrim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Trimmer {
	public static String sourceCopyPrefix;
	public static String destCopyPrefix;

	public static void main(String[] args) {
		File sourceFile = new File("C:/Users/micro/source/repos/HeavensFallWarcraftMod/Assets-bin/");
		File destFile = new File("C:/Users/micro/source/repos/HeavensFallWarcraftMod/AssetsTrimmed-bin/");
		sourceCopyPrefix = sourceFile.getAbsolutePath();
		destCopyPrefix = destFile.getAbsolutePath();
		for(File subFile: sourceFile.listFiles()) {
			process(subFile);
		}
	}
	
	public static void process(File file) {
		if(file.isDirectory()) {
			for(File subFile: file.listFiles()) {
				process(subFile);
			}
		} else {
			if(file.getAbsolutePath().startsWith(sourceCopyPrefix) && !file.getPath().toLowerCase().endsWith(".mdx") && !file.getPath().toLowerCase().endsWith(".blp") && !file.getPath().toLowerCase().endsWith(".mp3")) {
				String subPath = file.getAbsolutePath().substring(sourceCopyPrefix.length());
				String destPath = destCopyPrefix  + subPath;
				try {
					File newDestFile = new File(destPath);
					newDestFile.getParentFile().mkdirs();
					Files.copy(file.toPath(), new FileOutputStream(newDestFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
