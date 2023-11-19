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
			return addWarcraft3Installation(Paths.get(wcDirectory), false);
		} else {
			return Collections.emptyList();
		}
	}


	private void getWindowsRegistryDirectory1() {
		String usrSw = "HKEY_CURRENT_USER\\Software\\";
		String beW3 = "Blizzard Entertainment\\Warcraft III";
		wcDirectory = WindowsRegistry.readRegistry(usrSw + beW3, "InstallPathX");
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(usrSw + beW3, "InstallPathX");
		}
		if (wcDirectory == null) {
			wcDirectory = WindowsRegistry.readRegistry(
					usrSw + "Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\" + beW3,
					"InstallPath");
		}
		if (wcDirectory != null) {
			wcDirectory = wcDirectory.trim();
			fileChooser.setCurrentDirectory(new File(wcDirectory));
		}
	}

	private void getWindowsRegistryDirectory2() {
		String usrSw = "HKEY_CURRENT_USER\\Software\\";
		String beW3 = "Blizzard Entertainment\\Warcraft III";

		String[][] locationKeyPairs = {
				{usrSw + beW3, "InstallPathX"},
				{usrSw + beW3, "InstallPathX"},
				{usrSw + "Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\" + beW3, "InstallPath"}};

		for (String[] loc_key : locationKeyPairs) {
			wcDirectory = WindowsRegistry.readRegistry(loc_key[0], loc_key[1]);
			if (wcDirectory != null) {
				wcDirectory = wcDirectory.trim();
				fileChooser.setCurrentDirectory(new File(wcDirectory));
				break;
			}
		}
	}

	public static void main(String[] args) {
		System.out.println(getUgg());
	}
	private static String getUgg() {
//		String usrSw = "HKEY_CURRENT_USER\\Software\\";
		String usrSw = "HKEY_CURRENT_USER\\SOFTWARE\\";
		String beW3 = "Blizzard Entertainment\\Warcraft III";



		String[][] locationKeyPairs = {
				{usrSw + beW3, "InstallPathX"},
				{usrSw + beW3, "InstallPath"},
				{usrSw + "Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\" + beW3, "InstallPath"}};

		for (String[] loc_key : locationKeyPairs) {
			String wcDirectory = WindowsRegistry.readRegistry(loc_key[0], loc_key[1]);
			if (wcDirectory != null) {
				return wcDirectory.trim();
			}
		}
		return null;
	}
	private String getWindowsRegistryDirectory() {
//		String usrSw = "HKEY_CURRENT_USER\\Software\\";
		String usrSw = "HKEY_CURRENT_USER\\SOFTWARE\\";
		String beW3 = "Blizzard Entertainment\\Warcraft III";



		String[][] locationKeyPairs = {
				{usrSw + beW3, "InstallPathX"},
				{usrSw + beW3, "InstallPathX"},
				{usrSw + "Classes\\VirtualStore\\MACHINE\\SOFTWARE\\Wow6432Node\\" + beW3, "InstallPath"}};

		for (String[] loc_key : locationKeyPairs) {
			String wcDirectory = WindowsRegistry.readRegistry(loc_key[0], loc_key[1]);
			if (wcDirectory != null) {
				return wcDirectory.trim();
			}
		}
		return null;
	}

	protected String getWcDirectory() {
		return wcDirectory;
	}

	public List<DataSourceDescriptor> addWarcraft3Installation(final Path installPathPath, final boolean allowPopup) {
		List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
		if (Files.exists(installPathPath.resolve("Data/indices"))) {
			// Is it a CASC war3
			List<String> prefixes = CascPrefixChooser.addDefaultCASCPrefixes(installPathPath, allowPopup, popupParent);
			if (!allowPopup || !prefixes.isEmpty()) {
				CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString(), new ArrayList<>());
				dataSourceDescriptors.add(dataSourceDesc);
				dataSourceDesc.addPrefixes(prefixes);
			}
		} else {
			// Is it a MPQ war3
			String[] mpqSubPaths = {"War3.mpq", "War3Local.mpq", "War3x.mpq", "War3xlocal.mpq", "war3patch.mpq", "Deprecated.mpq"};
			String[] folderSubPaths = {"war3.w3mod", "war3.w3mod/_locales/enus.w3mod", "war3.w3mod/_deprecated.w3mod", "war3.w3mod/_hd.w3mod", "war3.w3mod/_hd.w3mod/_locales/enus.w3mod"};

			for (String s : mpqSubPaths) {
				MpqDataSourceDescriptor descriptor = getFromMPQ(installPathPath, s);
				if (descriptor != null) {
					dataSourceDescriptors.add(descriptor);
				}
			}

			for (String s : folderSubPaths) {
				FolderDataSourceDescriptor descriptor = getFromFolder(installPathPath, s);
				if (descriptor != null) {
					dataSourceDescriptors.add(descriptor);
				}
			}
		}
		return dataSourceDescriptors;
	}

	private FolderDataSourceDescriptor getFromFolder(Path installPathPath, String s) {
		if (Files.exists(installPathPath.resolve(s))) {
			return new FolderDataSourceDescriptor(installPathPath.resolve(s).toString());
		}
		return null;
	}

	private MpqDataSourceDescriptor getFromMPQ(Path installPathPath, String s) {
		if (Files.exists(installPathPath.resolve(s))) {
			return new MpqDataSourceDescriptor(installPathPath.resolve(s).toString());
		}
		return null;
	}

	public List<DataSourceDescriptor> getWar3InstallDirectory() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			Path installPathPath = selectedFile.toPath();
			List<DataSourceDescriptor> descriptors = addWarcraft3Installation(installPathPath, true);
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
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"MPQ Archive File (*.mpq;*.w3x;*.w3m)", "mpq", "w3x", "w3m");
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
