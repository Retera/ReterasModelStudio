package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.FolderDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.MpqDataSourceDescriptor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddDataSources {
	private final JFileChooser fileChooser = new JFileChooser();


	public List<DataSourceDescriptor> addWarcraft3Installation(final Path installPathPath, final boolean allowPopup) {
		List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
		if (Files.exists(installPathPath.resolve("Data/indices"))) {
			CascDataSourceDescriptor dataSourceDesc = new CascDataSourceDescriptor(installPathPath.toString(), new ArrayList<>());
			dataSourceDescriptors.add(dataSourceDesc);
			List<String> prefixes = CascPrefixChooser.addDefaultCASCPrefixes(installPathPath, allowPopup);
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




	public List<DataSourceDescriptor> addWar3InstallDirectory() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY);
		if (selectedFile != null) {
			// Is it a CASC war3
			Path installPathPath = selectedFile.toPath();
			return addWarcraft3Installation(installPathPath, true);
		}
		return Collections.emptyList();
	}

	public FolderDataSourceDescriptor addFolder() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY);
		if (selectedFile != null) {
			return new FolderDataSourceDescriptor(selectedFile.getPath());
		}
		return null;
	}

	public MpqDataSourceDescriptor addMPQ() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"MPQ Archive File (*.mpq;*.w3x;*.w3m)", "mpq", "w3x", "w3m");
		File selectedFile = getFile(filter, JFileChooser.FILES_ONLY);
		if (selectedFile != null) {
			return new MpqDataSourceDescriptor(selectedFile.getPath());
		}
		return null;
	}

	public CascDataSourceDescriptor addCASC() {
		File selectedFile = getFile(null, JFileChooser.DIRECTORIES_ONLY);
		if (selectedFile != null) {
			return new CascDataSourceDescriptor(selectedFile.getPath(), new ArrayList<>());
		}
		return null;
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
