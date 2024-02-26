package com.hiveworkshop.rms.ui.preferences;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

public class FileListTracker {
	private final LinkedHashSet<File> files;

	public FileListTracker() {
		this.files = new LinkedHashSet<>();
	}

	public boolean contains(File file) {
		return files.contains(file);
	}

	public boolean add(File file) {
		return files.add(file);
	}
	public boolean addAll(Collection<File> files) {
		return this.files.addAll(files);
	}

	public boolean remove(File file) {
		return files.remove(file);
	}

	public boolean add(String path) {
		return add(new File(path));
	}
	public boolean addAllPaths(Collection<String> paths) {
		int size = files.size();
		paths.forEach(p -> files.add(new File(p)));
		return size != files.size();
	}
	public boolean addAll3(Collection<?> paths) {
		boolean anyChange = false;
		for (Object path : paths) {
			if (path instanceof File file) {
				anyChange |= files.add(file);
			} else if (path instanceof String string) {
				anyChange |= files.add(new File(string));
			}
		}
		return anyChange;
	}

	public boolean remove(String path) {
		return remove(new File(path));
	}

	public boolean removeFirst() {
		File file = files.stream().findFirst().orElse(null);
		return remove(file);
	}

	public boolean remove(int i) {
		File file = files.stream().skip(i).findFirst().orElse(null);
		return remove(file);
	}

	public Collection<File> getFiles() {
		return files;
	}

	public FileListTracker clear() {
		files.clear();
		return this;
	}

	public int size() {
		return files.size();
	}

	public FileListTracker sort() {
		TreeSet<File> sorted = new TreeSet<>(files);
		files.clear();
		files.addAll(sorted);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for (File f : files) {
			sb.append("\t\"").append(f).append("\",\n");
		}
		sb.append("]");
		return sb.toString();
	}

	public  FileListTracker fromString(String string) {
		System.out.println("FLT - Full String:\n" + string);
		System.out.println("\nFLT - parsedBits:\n");

		String[] split = string.split("((\\[)|(\",))\\n((\\s*\")|(]))");
		int i = 0;
		for (String s : split) {
			if(!s.isBlank()) {
				System.out.println(i++ + ": \"" + s + "\"");
				add(s.strip());
			}
		}
		System.out.println("FLT end \n");
		return this;
	}
}
