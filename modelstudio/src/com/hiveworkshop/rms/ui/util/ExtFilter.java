package com.hiveworkshop.rms.ui.util;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.util.stream.Collectors;

public class ExtFilter {
	private final List<FileNameExtensionFilter> openFilesExtensions;
	private final List<FileNameExtensionFilter> openModelExtensions;
	private final List<FileNameExtensionFilter> saveModelExtensions;
	private final List<FileNameExtensionFilter> savableExtensions;
	private final List<FileNameExtensionFilter> textureExtensions;
	private final Set<String> savableModelExtensions = new HashSet<>();
	private final Set<String> savableTextureExtensions = new HashSet<>();
	private final Set<String> supModelExtensions = new HashSet<>();
	private final Set<String> supTextureExtensions = new HashSet<>();
	List<ExtInfo> extInfos = Arrays.asList(
			new ExtInfo("Warcraft III Binary Model", FileType.MODEL, true, true, "mdx"),
			new ExtInfo("Warcraft III Text Model", FileType.MODEL, true, true, "mdl"),
			new ExtInfo("Warcraft III BLP Image", FileType.IMAGE, true, true, "blp"),
			new ExtInfo("DDS Image", FileType.IMAGE, true, true, "dds"),
			new ExtInfo("TGA Image", FileType.IMAGE, true, true, "tga"),
			new ExtInfo("Autodesk FBX Model", FileType.MODEL, false, false, "fbx"),
			new ExtInfo("Wavefront OBJ Model", FileType.MODEL, false, false, "obj"),
			new ExtInfo("PNG Image", FileType.IMAGE, true, false, "png"),
			new ExtInfo("JPG Image", FileType.IMAGE, true, false, "jpg", "jpeg"),
			new ExtInfo("BMP Image", FileType.IMAGE, true, false, "bmp"),
			new ExtInfo("TIF Image", FileType.IMAGE, true, false, "tif")
	);

	public ExtFilter() {
		openFilesExtensions = getFilterList(Arrays.asList(FileType.MODEL, FileType.IMAGE), false, false);
		openModelExtensions = getFilterList(Collections.singletonList(FileType.MODEL), false, false);
		saveModelExtensions = getFilterList(Collections.singletonList(FileType.MODEL), true, false);
		savableExtensions = getFilterList(Arrays.asList(FileType.MODEL, FileType.IMAGE), true, false);
		textureExtensions = getFilterList(Collections.singletonList(FileType.IMAGE), false, false);

		for (FileNameExtensionFilter filter : saveModelExtensions) {
			savableModelExtensions.addAll(Arrays.asList(filter.getExtensions()));
		}
		for (FileNameExtensionFilter filter : textureExtensions) {
			savableTextureExtensions.addAll(Arrays.asList(filter.getExtensions()));
		}
		for (FileNameExtensionFilter filter : openModelExtensions) {
			supModelExtensions.addAll(Arrays.asList(filter.getExtensions()));
		}
		for (FileNameExtensionFilter filter : textureExtensions) {
			supTextureExtensions.addAll(Arrays.asList(filter.getExtensions()));
		}
	}

	List<FileNameExtensionFilter> getFilterList(List<FileType> fileTypes, boolean onlySavable, boolean onlyWC3) {
		List<FileNameExtensionFilter> extensionFilters = new ArrayList<>();

		extensionFilters.add(getComboFilter(fileTypes, onlySavable, onlyWC3));

		if (fileTypes.size() > 1) {
			for (FileType fileType : fileTypes) {
				extensionFilters.add(getComboFilter(Collections.singletonList(fileType), onlySavable, onlyWC3));
			}
		}
		if (!onlyWC3) {
			extensionFilters.add(getComboFilter(fileTypes, onlySavable, true));
			if (fileTypes.size() > 1) {
				for (FileType fileType : fileTypes) {
					extensionFilters.add(getComboFilter(Collections.singletonList(fileType), onlySavable, true));
				}
			}
		}

		List<ExtInfo> filteredExtInfos = getFilteredExtInfos(fileTypes, onlySavable, onlyWC3);
		for (ExtInfo extInfo : filteredExtInfos) {
			extensionFilters.add(new FileNameExtensionFilter(extInfo.getFilterDescription(), extInfo.getExtVarients()));
		}

		return extensionFilters;
	}


	FileNameExtensionFilter getComboFilter(List<FileType> fileTypes, boolean onlySavable, boolean onlyWC3) {
		List<ExtInfo> filteredExtInfos = getFilteredExtInfos(fileTypes, onlySavable, onlyWC3);

		StringBuilder extDes = new StringBuilder();
		List<String> exts = new ArrayList<>();
		for (ExtInfo extInfo : filteredExtInfos) {
			extDes.append(extInfo.getExtDescription());
			exts.addAll(Arrays.asList(extInfo.getExtVarients()));
		}
		extDes.replace(0, 1, "(");
		extDes.append(")");
		String wholeDes = "";

		if (onlyWC3) {
			wholeDes += "Warcraft III ";
		} else {
			wholeDes += "Supported ";
		}

		if (fileTypes.size() == 1) {
			wholeDes += fileTypes.get(0).getType();
		}

		wholeDes += " Files ";

		extDes.insert(0, wholeDes);


		return new FileNameExtensionFilter(extDes.toString(), exts.toArray(exts.toArray(new String[0])));
	}

	private List<ExtInfo> getFilteredExtInfos(List<FileType> fileTypes, boolean onlySavable, boolean onlyWC3) {
		return extInfos.stream()
				.filter(extInfo -> fileTypes.contains(extInfo.getFileType()))
				.filter(extInfo -> !onlySavable || extInfo.isSavable())
				.filter(extInfo -> !onlyWC3 || extInfo.isWC3)
				.collect(Collectors.toList());
	}

	public List<FileNameExtensionFilter> getOpenFilesExtensions() {
		return openFilesExtensions;
	}

	public List<FileNameExtensionFilter> getOpenModelExtensions() {
		return openModelExtensions;
	}

	public List<FileNameExtensionFilter> getSaveModelExtensions() {
		return saveModelExtensions;
	}

	public List<FileNameExtensionFilter> getSavableExtensions() {
		return savableExtensions;
	}

	public List<FileNameExtensionFilter> getTextureExtensions() {
		return textureExtensions;
	}

	public boolean isSavableModelExt(String ext) {
		return savableModelExtensions.contains(ext);
	}

	public boolean isSupModel(String ext) {
		return supModelExtensions.contains(ext);
	}

	public boolean isSavableTextureExt(String ext) {
		return savableTextureExtensions.contains(ext);
	}

	public boolean isSupTexture(String ext) {
		return supTextureExtensions.contains(ext);
	}

	private enum FileType {
		MODEL("Model"), IMAGE("Image"), OTHER("File");
		String type;

		FileType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	private static class ExtInfo {
		String description;
		String[] extVarients;
		FileType fileType;
		boolean isWC3;
		boolean savable;

		ExtInfo(String description, FileType fileType, boolean savable, boolean isWC3, String... extVarients) {
			this.description = description;
			this.extVarients = extVarients;
			this.fileType = fileType;
			this.isWC3 = isWC3;
			this.savable = savable;
		}

		public String getDescription() {
			return description;
		}

		public String[] getExtVarients() {
			return extVarients;
		}

		public FileType getFileType() {
			return fileType;
		}

		public boolean isWC3() {
			return isWC3;
		}

		public boolean isSavable() {
			return savable;
		}

		public String getExtDescription() {
			StringBuilder des = new StringBuilder();
			for (String ext : extVarients) {
				des.append(";*.").append(ext);
			}
			return des.toString();
		}

		public String getFilterDescription() {
			StringBuilder des = new StringBuilder();
			for (String ext : extVarients) {
				des.append(";*.").append(ext);
			}
			des.replace(0, 1, "(");
			des.append(")");
			return description + " " + des.toString();
		}
	}
}
