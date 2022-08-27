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

	public DataSourceTracker(Component popupParent){
		getWindowsRegistryDirectory();
		this.popupParent = popupParent;
	}

	protected List<DataSourceDescriptor> getDefaults(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		if (dataSourceDescriptorDefaults == null) {
			if (wcDirectory != null) {
				return addWarcraft3Installation(Paths.get(wcDirectory), false);

			}
		} else {
			List<DataSourceDescriptor> defaults = new ArrayList<>();
			for (final DataSourceDescriptor dataSourceDescriptor : dataSourceDescriptorDefaults) {
				defaults.add(dataSourceDescriptor.duplicate());
			}
			return defaults;
		}
		return Collections.emptyList();
	}


	private void getWindowsRegistryDirectory() {
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

	protected String getWcDirectory(){
		return wcDirectory;
	}

	public List<DataSourceDescriptor> addWarcraft3Installation(final Path installPathPath, final boolean allowPopup) {
		List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
		if (Files.exists(installPathPath.resolve("Data/indices"))) {
			// Is it a CASC war3
			CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString(), new ArrayList<>());
			dataSourceDescriptors.add(dataSourceDesc);
			List<String> prefixes = CascPrefixChooser.addDefaultCASCPrefixes(installPathPath, allowPopup, popupParent);
			dataSourceDesc.addPrefixes(prefixes);
		} else {
			// Is it a MPQ war3
			String[] mpqSubPaths = {"War3.mpq", "War3Local.mpq", "War3x.mpq", "War3xlocal.mpq", "war3patch.mpq", "Deprecated.mpq"};
			String[] folderSubPaths = {"war3.w3mod", "war3.w3mod/_locales/enus.w3mod", "war3.w3mod/_deprecated.w3mod", "war3.w3mod/_hd.w3mod", "war3.w3mod/_hd.w3mod/_locales/enus.w3mod"};

			for (String s : mpqSubPaths){
				MpqDataSourceDescriptor descriptor = getFromMPQ(installPathPath, s);
				if(descriptor != null){
					dataSourceDescriptors.add(descriptor);
				}
			}

			for (String s : folderSubPaths){
				FolderDataSourceDescriptor descriptor = getFromFolder(installPathPath, s);
				if(descriptor != null){
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
			return addWarcraft3Installation(installPathPath, true);
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
