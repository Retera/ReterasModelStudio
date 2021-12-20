package com.hiveworkshop.rms.ui.browsers.jworldedit;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.List;

public class RMSFileChooser extends JFileChooser {

	public RMSFileChooser(){
//		this.getUI().get
//		setUpLookAndFeel();
//		getUI().
//		setUI();
//		FileChooserUI fileChooserUI = new FileChooserUI();
	}

	@Override
	public void approveSelection() {
		File selectedFile = this.getSelectedFile();
		String ext = getExtension(selectedFile);
		if (!selectedFile.getName().endsWith(ext)) {
			selectedFile = new File(selectedFile.getPath() + ext);
			this.setSelectedFile(selectedFile);
		}
		System.out.println("filechooser this: " + this);
		System.out.println("dialog type: " + this.getDialogType());
		if (selectedFile.exists() && this.getDialogType() == JFileChooser.SAVE_DIALOG) {
			int confirmOverwriteFile = JOptionPane.showConfirmDialog(
					getParent(),
					"File \"" + selectedFile.getName() + "\" already exists. Overwrite anyway?",
					"Export File",
					JOptionPane.OK_CANCEL_OPTION);
			if (confirmOverwriteFile == JOptionPane.OK_OPTION) {
				//selectedFile.delete();
			} else {
				return;
			}
		}
		super.approveSelection();
	}

	private String getExtension(File modelFile) {
		final String name = modelFile.getName();
		if (name.lastIndexOf('.') != -1) {
			return name.substring(name.lastIndexOf('.'));
		} else {
			String[] extensions = ((FileNameExtensionFilter) getFileFilter()).getExtensions();
			if(0 < extensions.length){
				return "." + extensions[0];
			}
			return "";
		}
	}

	private void setFilter(List<FileNameExtensionFilter> filters) {
		resetChoosableFileFilters();
		for (FileNameExtensionFilter filter : filters) {
			addChoosableFileFilter(filter);
		}
	}

	private static void setUpLookAndFeel() {
//		try {
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
//		} catch (final Exception exc) {
//			try {
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
//				e.printStackTrace();
//			}
//		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
	}
}
