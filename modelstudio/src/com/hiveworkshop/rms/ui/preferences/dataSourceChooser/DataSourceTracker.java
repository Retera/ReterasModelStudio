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
import java.util.List;

public class DataSourceTracker {
	private final List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
	private String wcDirectory;
	private final JFileChooser fileChooser = new JFileChooser();
	private final Component popupParent;

	public DataSourceTracker(List<DataSourceDescriptor> dataSourceDescriptorDefaults, Component popupParent){
		loadDefaults(dataSourceDescriptorDefaults);
		getWindowsRegistryDirectory();
		this.popupParent = popupParent;
	}


	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceDescriptors;
	}

	protected void loadDefaults(final List<DataSourceDescriptor> dataSourceDescriptorDefaults) {
		dataSourceDescriptors.clear();
		if (dataSourceDescriptorDefaults == null) {
			if (wcDirectory != null) {
				dataSourceDescriptors.addAll(addWarcraft3Installation(Paths.get(wcDirectory), false));
			}
		} else {
			for (final DataSourceDescriptor dataSourceDescriptor : dataSourceDescriptorDefaults) {
				dataSourceDescriptors.add(dataSourceDescriptor.duplicate());
			}
		}
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
	public CascDataSourceDescriptor getCascDataSourceDescriptor() {
		if ((dataSourceDescriptors.size() == 1) && (dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor)) {
			return  (CascDataSourceDescriptor) dataSourceDescriptors.get(0);
		}
		return null;
	}

	protected void addSource(DataSourceDescriptor sourceDescriptor){
		if(sourceDescriptor != null){
			dataSourceDescriptors.add(sourceDescriptor);
		}
	}
	protected void addSource(List<DataSourceDescriptor> sourceDescriptors){
		if(sourceDescriptors != null){
			dataSourceDescriptors.addAll(sourceDescriptors);
		}
	}

	protected String getWcDirectory(){
		return wcDirectory;
	}

	public void clear(){
		dataSourceDescriptors.clear();
	}


	public List<DataSourceDescriptor> addWarcraft3Installation(final Path installPathPath, final boolean allowPopup) {
		List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
		if (Files.exists(installPathPath.resolve("Data/indices"))) {
			CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString(), new ArrayList<>());
			dataSourceDescriptors.add(dataSourceDesc);
			List<String> prefixes = CascPrefixChooser.addDefaultCASCPrefixes(installPathPath, allowPopup, popupParent);
			dataSourceDesc.addPrefixes(prefixes);
		} else {
			String[] mpqSubPaths = {"War3.mpq", "War3Local.mpq", "War3x.mpq", "War3xlocal.mpq", "war3patch.mpq", "Deprecated.mpq"};
			String[] folderSubPaths = {"war3.w3mod", "war3.w3mod/_locales/enus.w3mod", "war3.w3mod/_deprecated.w3mod", "war3.w3mod/_hd.w3mod", "war3.w3mod/_hd.w3mod/_locales/enus.w3mod"};

			for (String s : mpqSubPaths){
				MpqDataSourceDescriptor descriptor = getFromMPQ(installPathPath, s);
				if(descriptor != null){
					dataSourceDescriptors.add(descriptor);
				}
			}

//			String[] posLocals = {"zhCN", "ruRU", "esES", "itIT", "zhTW", "frFR", "enUS", "koKR", "deDE", "plPL"};
//
//			String realLoc = "enUS";
//			if (Files.exists(installPathPath.resolve("war3.w3mod"))) {
//				for(String loc : posLocals){
//					if (Files.exists(installPathPath.resolve("war3.w3mod\\_locales\\" + loc + ".w3mod"))) {
//						realLoc = loc.toLowerCase(Locale.US);
//						break;
//					}
//				}
//			}
//
//			ArrayList<String> folderSubPaths = SupportedCascPatchFormat.PATCH132.getPrefixes(realLoc);

			for (String s : folderSubPaths){
				FolderDataSourceDescriptor descriptor = getFromFolder(installPathPath, s);
				if(descriptor != null){
					dataSourceDescriptors.add(descriptor);
				}
			}
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "War3.mpq"));
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "War3Local.mpq"));
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "War3x.mpq"));
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "War3xlocal.mpq"));
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "war3patch.mpq"));
//			dataSourceDescriptors.add(getFromMPQ(installPathPath, "Deprecated.mpq"));
//			dataSourceDescriptors.add(getFromFolder(installPathPath, "war3.w3mod"));
//			dataSourceDescriptors.add(getFromFolder(installPathPath, "war3.w3mod/_locales/enus.w3mod"));
//			dataSourceDescriptors.add(getFromFolder(installPathPath, "war3.w3mod/_deprecated.w3mod"));
//			dataSourceDescriptors.add(getFromFolder(installPathPath, "war3.w3mod/_hd.w3mod"));
//			dataSourceDescriptors.add(getFromFolder(installPathPath, "war3.w3mod/_hd.w3mod/_locales/enus.w3mod"));
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


	public void addWar3InstallDirectory(Runnable updater) {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			// Is it a CASC war3
			Path installPathPath = selectedFile.toPath();
			List<DataSourceDescriptor> descriptors = addWarcraft3Installation(installPathPath, true);
			dataSourceDescriptors.addAll(descriptors);
			if(updater != null){
				updater.run();
			}
		}
	}

	public void addFolder(Runnable updater) {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			FolderDataSourceDescriptor descriptor = new FolderDataSourceDescriptor(selectedFile.getPath());
			dataSourceDescriptors.add(descriptor);
			if(updater != null){
				updater.run();
			}
		}
	}

	public void addMPQ(Runnable updater) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"MPQ Archive File (*.mpq;*.w3x;*.w3m)", "mpq", "w3x", "w3m");
		File selectedFile = getFile(filter, JFileChooser.FILES_ONLY, popupParent);
		if (selectedFile != null) {
			MpqDataSourceDescriptor descriptor = new MpqDataSourceDescriptor(selectedFile.getPath());
			dataSourceDescriptors.add(descriptor);
			if(updater != null){
				updater.run();
			}
		}
	}

	public void addCASC(Runnable updater) {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY, popupParent);
		if (selectedFile != null) {
			CascDataSourceDescriptor descriptor = new CascDataSourceDescriptor(selectedFile.getPath(), new ArrayList<>());
			dataSourceDescriptors.add(descriptor);
			if(updater != null){
				updater.run();
			}
		}
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


	public void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
