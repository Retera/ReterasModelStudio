package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.RMSFileChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class FileDialog {
    public static final int OPEN_FILE = 0;
    public static final int OPEN_MODEL = 1;
    public static final int OPEN_WC_MODEL = 2;
    public static final int SAVE_MODEL = 3;
    public static final int OPEN_TEXTURE = 4;
    public static final int SAVE_TEXTURE = 5;
    public static final int SAVE = 6;

    private static SaveProfile profile = SaveProfile.get();

    private ModelPanel modelPanel;
    private Component parent;
    private final RMSFileChooser fileChooser;
    private static final ExtFilter extFilter = new ExtFilter();

    public FileDialog() {
        this.fileChooser = new RMSFileChooser(profile);
//        fileChooser.setAccessory(getAccessoryPanel("Ugg!"));
        this.fileChooser.setAcceptAllFileFilterUsed(false);
    }

    public JPanel getAccessoryPanel(String s) {
        JPanel ugg = new JPanel(new MigLayout("ins 0, gap 0"));
        ugg.add(new JButton(s));
        return ugg;
    }

    public FileDialog(ModelPanel modelPanel) {
        this();
        this.modelPanel = modelPanel;
    }

    public FileDialog(Component parent) {
        this();
        this.parent = parent;
    }

    public FileDialog setParent(Component parent) {
        this.parent = parent;
        return this;
    }

    public static void setCurrentPath(File file) {
        profile.setPath(file.getParent());
    }

    private List<FileNameExtensionFilter> getFilter(int type) {
        return switch (type) {
            case OPEN_FILE -> extFilter.getOpenFilesExtensions();
            case OPEN_MODEL -> extFilter.getOpenModelExtensions();
            case SAVE_MODEL, OPEN_WC_MODEL -> extFilter.getSaveModelExtensions();
            case OPEN_TEXTURE -> extFilter.getOpenTextureExtensions();
            case SAVE_TEXTURE -> extFilter.getSaveTextureExtensions();
            case SAVE -> extFilter.getSavableExtensions();
            default -> Collections.EMPTY_LIST;
        };
    }

    private void setFilter(int type) {
        switch (type) {
            case OPEN_FILE -> setFilter(extFilter.getOpenFilesExtensions());
            case OPEN_MODEL -> setFilter(extFilter.getOpenModelExtensions());
            case SAVE_MODEL, OPEN_WC_MODEL -> setFilter(extFilter.getSaveModelExtensions());
            case OPEN_TEXTURE -> setFilter(extFilter.getOpenTextureExtensions());
            case SAVE_TEXTURE -> setFilter(extFilter.getSaveTextureExtensions());
            case SAVE -> setFilter(extFilter.getSavableExtensions());
        }
    }

    private void setFilter(List<FileNameExtensionFilter> filters) {
        fileChooser.resetChoosableFileFilters();
        for (FileNameExtensionFilter filter : filters) {
            fileChooser.addChoosableFileFilter(filter);
        }
    }

    public static String getPath() {
        return profile.getPath();
    }


    public String getExtension(File file) {
        return getExtension(file.getName());
    }

    public String getExtension(String name) {
        if (name.lastIndexOf('.') != -1) {
            return name.substring(name.lastIndexOf('.') + 1);
        } else {
            return ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
        }
    }

    public String getExtensionOrNull(File file){
        return getExtensionOrNull(file.getName());
    }

    public String getExtensionOrNull(String name) {
        if (name.lastIndexOf('.') != -1) {
            return name.substring(name.lastIndexOf('.') + 1);
        }
        return null;
    }

    public RMSFileChooser getFileChooser() {
        return fileChooser;
    }

    public File getSaveFile(int operationType, String fileName){
        List<FileNameExtensionFilter> filter = getFilter(operationType);
        return getSaveFile(fileName, filter);
    }

    public File getSaveFile(String fileName, List<FileNameExtensionFilter> filter) {
        fileChooser.setDialogTitle("Save as");
        setFilter(filter);
        File selectedFile = new File(getCurrentDirectory(), fileName);
        fileChooser.setSelectedFile(selectedFile);

        final int returnValue = fileChooser.showSaveDialog(getParent());
        File file = fileChooser.getSelectedFile();
        fileChooser.setSelectedFile(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (file != null) {
                setCurrentPath(file);
                return file;
            } else {
                JOptionPane.showMessageDialog(getParent(),
                        "You tried to save, but you somehow didn't select a file.\nThat is bad.");
            }
        }
        return null;
    }



    public File[] openFiles(int operationType) {
        fileChooser.setDialogTitle("Open");
        fileChooser.setMultiSelectionEnabled(true);
        setFilter(operationType);
        fileChooser.setCurrentDirectory(getCurrentDirectory());

        final int returnValue = fileChooser.showOpenDialog(getParent());
        File[] files = fileChooser.getSelectedFiles();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectedFile(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (files != null && 0 < files.length) {
                setCurrentPath(files[0]);
            }
            return files;
        }
        return null;
    }
    public File openFile(int operationType) {
        fileChooser.setDialogTitle("Open");
        setFilter(operationType);
        fileChooser.setCurrentDirectory(getCurrentDirectory());

        final int returnValue = fileChooser.showOpenDialog(getParent());
        File file = fileChooser.getSelectedFile();
        fileChooser.setSelectedFile(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (file != null) {
                setCurrentPath(file);
                return file;
            }
        }
        return null;
    }
    public File chooseDir(int operationType) {
        fileChooser.setDialogTitle("Open");
//        setFilter(operationType);
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new FileFilter(){
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Folder";
            }
        });
//        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Folder", ""));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(getCurrentDirectory());

        final int returnValue = fileChooser.showOpenDialog(getParent());
        File file = fileChooser.getSelectedFile();
        fileChooser.setSelectedFile(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (file != null) {
                setCurrentPath(file);
                return file;
            }
        }
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return null;
    }
    private File getCurrentDirectory() {
        EditableModel model = getModel();
        if (model != null && !model.isTemp() && model.getFile() != null) {
            return model.getFile().getParentFile();
        } else if (getPath() != null) {
            return new File(getPath());
        }
        return fileChooser.getCurrentDirectory();
    }
    private EditableModel getModel() {
        if (modelPanel != null && modelPanel.getModel() != null) {
	        return modelPanel.getModel();
        } else if (ProgramGlobals.getCurrentModelPanel() != null) {
	        return ProgramGlobals.getCurrentModelPanel().getModel();
        } else {
	        return null;
        }
    }

    private Component getParent() {
        if (parent != null) {
            return parent;
        } else {
            return ProgramGlobals.getMainPanel();
        }
    }

    public void openFile(final File file) {
        if (file != null) {
            setCurrentPath(file);

            SaveProfile.get().addRecent(file.getPath());
            ProgramGlobals.getMenuBar().updateRecent();
            ModelLoader.loadFile(file);
        }
    }

    public boolean isSavableModelExt(File file){
        String ext = getExtension(file).toLowerCase();
        return isSavableModelExt(ext);
    }
    public boolean isSavableModelExt(String ext){
        return extFilter.isSavableModelExt(ext);
    }
    public boolean isSupModel(String ext){
        return extFilter.isSupModel(ext);
    }
    public boolean isSavableTextureExt(File file){
        String ext = getExtension(file).toLowerCase();
        return isSavableTextureExt(ext);
    }

    public boolean isSavableTextureExt(String ext) {
        return extFilter.isSavableTextureExt(ext);
    }
    public boolean isSupTexture(String ext) {
        return extFilter.isSupTexture(ext);
    }
}
