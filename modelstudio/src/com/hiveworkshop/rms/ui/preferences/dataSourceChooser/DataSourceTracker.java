package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.MpqDataSourceDescriptor;
import com.hiveworkshop.rms.util.WindowsRegistry;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataSourceTracker {
	private String wcDirectory;
	private final JFileChooser fileChooser = new JFileChooser();
	private final Component popupParent;

	public DataSourceTracker(Component popupParent) {
		wcDirectory = getWindowsRegistryDirectory();
		if (wcDirectory != null) fileChooser.setCurrentDirectory(new File(wcDirectory));
		else if (new File("C:\\Program Files").exists()) fileChooser.setCurrentDirectory(new File("C:\\Program Files\\Warcraft III"));

		this.popupParent = popupParent;
	}

	protected List<DataSourceDescriptor> getInitialDescriptors(final List<DataSourceDescriptor> currentDescriptors) {
		if (currentDescriptors == null) {
			return getDefaults();
		} else {
			List<DataSourceDescriptor> defaults = new ArrayList<>();
			for (DataSourceDescriptor descriptor : currentDescriptors) {
				defaults.add(descriptor.duplicate());
			}
			return defaults;
		}
	}

	protected List<DataSourceDescriptor> getDefaults() {
		if (wcDirectory != null) {
			if (isCASC(wcDirectory)) {
				List<String> prefixes = CascPrefixChooser.getDefaultCASCPrefixes(wcDirectory, false, popupParent);
				return getCascDataSourceDescriptors(Paths.get(wcDirectory), prefixes);
			} else {
				return getMPQDataSourceDescriptors(Paths.get(wcDirectory));
			}
		} else {
			return Collections.emptyList();
		}
	}

	private String getWindowsRegistryDirectory() {
		String[] keys = {"InstallPathX", "InstallPath", "InstallLocation"};
		String[] locations = {
				"HKEY_CURRENT_USER\\SOFTWARE\\Blizzard Entertainment\\Warcraft III",
				"HKEY_CURRENT_USER\\SOFTWARE\\Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\Blizzard Entertainment\\Warcraft III",
				"HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Warcraft III",
		};

		for (String location : locations) {
			for (String key : keys) {
				String wcDirectory = WindowsRegistry.readRegistry(location, key);
				if (wcDirectory != null) {
					return wcDirectory.trim();
				}
			}
		}
		return null;
	}

	protected String getWcDirectory() {
		return wcDirectory;
	}

	public List<DataSourceDescriptor> getCascDataSourceDescriptors(final Path installPathPath, List<String> prefixes){
		if (prefixes != null) {
			CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString());
			dataSourceDesc.addPrefixes(prefixes);
			return Collections.singletonList(dataSourceDesc);
		}
		return Collections.emptyList();
	}

	public List<DataSourceDescriptor> getMPQDataSourceDescriptors(final Path installPathPath){
		List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
		dataSourceDescriptors.addAll(getMPQDescriptors(installPathPath));
		dataSourceDescriptors.addAll(getFolderDescriptors(installPathPath));
		return dataSourceDescriptors;
	}

	private boolean isCASC(final String installPath) {
		return Files.exists(Paths.get(installPath).resolve("Data/indices"));
	}

	private List<DataSourceDescriptor> getFolderDescriptors(Path installPathPath) {
		List<DataSourceDescriptor> folderDescriptors = new ArrayList<>();
		String[] folderSubPaths = {"war3.w3mod", "war3.w3mod/_locales/enus.w3mod", "war3.w3mod/_deprecated.w3mod", "war3.w3mod/_hd.w3mod", "war3.w3mod/_hd.w3mod/_locales/enus.w3mod"};
		for (String s : folderSubPaths) {
			if (Files.exists(installPathPath.resolve(s))) {
				folderDescriptors.add(new FolderDataSourceDescriptor(installPathPath.resolve(s).toString()));
			}
		}
		return folderDescriptors;
	}

	private List<DataSourceDescriptor> getMPQDescriptors(Path installPathPath) {
		List<DataSourceDescriptor> mpqDescriptors = new ArrayList<>();
		String[] mpqSubPaths = {"War3.mpq", "War3Local.mpq", "War3x.mpq", "War3xlocal.mpq", "war3patch.mpq", "Deprecated.mpq"};
		for (String s : mpqSubPaths) {
			if (Files.exists(installPathPath.resolve(s))) {
				mpqDescriptors.add(new MpqDataSourceDescriptor(installPathPath.resolve(s).toString()));
			}
		}
		return mpqDescriptors;
	}

	public List<DataSourceDescriptor> getWar3InstallDirectory() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			Path installPathPath = selectedFile.toPath();
			List<DataSourceDescriptor> descriptors;
			if (isCASC(selectedFile.getPath())) {
				List<String> prefixes = CascPrefixChooser.getDefaultCASCPrefixes(selectedFile.getPath(), true, popupParent);
				descriptors = getCascDataSourceDescriptors(installPathPath, prefixes);
			} else {
				descriptors = getMPQDescriptors(installPathPath);
			}
			if (descriptors.isEmpty()) {
				String message = "Did not find any installation on path \"" + installPathPath + "\"";
				System.err.println(message);
				JOptionPane.showMessageDialog(popupParent, message, "Error", JOptionPane.ERROR_MESSAGE);
			}
			return descriptors;
		}
		return Collections.emptyList();
	}

	public List<DataSourceDescriptor> getFolder() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			return Collections.singletonList(new FolderDataSourceDescriptor(selectedFile.getPath()));
		}
		return Collections.emptyList();
	}

	public List<DataSourceDescriptor> getMPQ() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MPQ Archive File (*.mpq;*.w3x;*.w3m)", "mpq", "w3x", "w3m");
		File selectedFile = getFile(filter, JFileChooser.FILES_ONLY, popupParent);
		if (selectedFile != null) {
			return Collections.singletonList(new MpqDataSourceDescriptor(selectedFile.getPath()));
		}
		return Collections.emptyList();
	}

	public List<DataSourceDescriptor> getCASC() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			return Collections.singletonList(new CascDataSourceDescriptor(selectedFile.getPath(), new ArrayList<>()));
		}
		return Collections.emptyList();
	}

	public File getFile(FileFilter filter, int mode) {
		return getFile(filter, mode, null);
	}
	public File getFile(FileFilter filter, int mode, Component parent) {
		fileChooser.setFileFilter(filter);
		fileChooser.setFileSelectionMode(mode);
		int result = fileChooser.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}
}
