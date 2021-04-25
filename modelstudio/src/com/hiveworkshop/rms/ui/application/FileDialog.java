package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ExtFilter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileDialog {
    public static final int OPEN_FILE = 0;
    public static final int OPEN_MODEL = 1;
    public static final int OPEN_WC_MODEL = 2;
    public static final int SAVE_MODEL = 3;
    public static final int OPEN_TEXTURE = 4;
    public static final int SAVE_TEXTURE = 5;
    public static final int SAVE = 6;

    private static File currentFile;
    private static SaveProfile profile = SaveProfile.get();

    private static MainPanel mainPanel;
    private ModelPanel modelPanel;
    private JComponent parent;
    private final JFileChooser fileChooser;
    private final ExtFilter extFilter;

    public FileDialog(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.fileChooser = getFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        extFilter = new ExtFilter();
    }

    public FileDialog(ModelPanel modelPanel) {
        this.modelPanel = modelPanel;
        this.fileChooser = getFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        extFilter = new ExtFilter();
    }

    public FileDialog(JComponent parent) {
        this.parent = parent;
        this.fileChooser = getFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        extFilter = new ExtFilter();
    }

    public static void setCurrentPath(File modelFile) {
//        mainPanel.profile.setPath(modelFile.getParent());
        profile.setPath(modelFile.getParent());
    }

//    public void saveFileDialog() {
//        setFilter(saveModelExtensions);
//        int saveOption = fileChooser.showSaveDialog(mainPanel);
//
//        if (saveOption == JFileChooser.APPROVE_OPTION) {
//            final File selectedFile = fileChooser.getSelectedFile();
//            if (selectedFile != null) {
//                if (!selectedFile.exists()) {
//                    System.out.println("Should Save!");
////                    try {
//////                        Files.copy(gameDataFileSystem.getResourceAsStream(clickedNode.getPath()), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
////                    } catch (final IOException e1) {
////                        ExceptionPopup.display(e1);
////                        e1.printStackTrace();
////                    }
//                }
//            }
//        }
//    }


    private void setFilter(int type) {
        switch (type) {
            case OPEN_FILE -> setFilter(extFilter.getOpenFilesExtensions());
            case OPEN_MODEL -> setFilter(extFilter.getOpenModelExtensions());
            case SAVE_MODEL, OPEN_WC_MODEL -> setFilter(extFilter.getSaveModelExtensions());
            case OPEN_TEXTURE, SAVE_TEXTURE -> setFilter(extFilter.getTextureExtensions());
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

    public static File getCurrentFile() {
        return currentFile;
    }

    public static void setCurrentFile(File file) {
        currentFile = file;
    }

    // Creates an overwrite-prompt without closing the fileChooser dialog,
    // letting the user change the name to save as if choosing "Cancel"
    private JFileChooser getFileChooser() {
        return new JFileChooser() {
            @Override
            public void approveSelection() {
                File selectedFile = this.getSelectedFile();
                String ext = getExtension(selectedFile);
                if (!selectedFile.getName().endsWith(ext)) {
                    selectedFile = new File(selectedFile.getPath() + "." + ext);
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
        };
    }

    void onClickSaveAs() {
        final EditableModel model = getModel();
        onClickSaveAs(model, SAVE, true);
    }

    public void exportTexture(final BufferedImage bufferedImage, String fileName) {
        setCurrentDirectory(getModel());
        setFilter(SAVE_TEXTURE);
        File selectedFile = new File(fileChooser.getCurrentDirectory(), fileName);
        fileChooser.setSelectedFile(selectedFile);
        onClickSaveAs(null, bufferedImage, SAVE_TEXTURE, false);
    }

    private String getExtension(File modelFile) {
        final String name = modelFile.getName();
        if (name.lastIndexOf('.') != -1) {
            return name.substring(name.lastIndexOf('.') + 1);
        } else {
            return ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
        }
    }

    void onClickSaveAs(final EditableModel model, int operationType, boolean updateCurrent) {
        BufferedImage bufferedImage = null;
        if (operationType == SAVE_TEXTURE || operationType == SAVE) {
            if (model != null && model.getMaterial(0) != null) {
                bufferedImage = model.getMaterial(0).getBufferedImage(getModel().getWrappedDataSource());
            }
        }
        onClickSaveAs(model, bufferedImage, operationType, updateCurrent);
    }

    void onClickSaveAs(final EditableModel model, BufferedImage bufferedImage, int operationType, boolean updateCurrent) {
        fileChooser.setDialogTitle("Save as");
        setFilter(operationType);
        setCurrentDirectory(model);
        if (model != null) {
            File selectedFile = model.getFile();
            if (selectedFile == null) {
                System.out.println("model.getName(): " + model.getName());
                selectedFile = new File(fileChooser.getCurrentDirectory(), model.getName());
            } else if (model.isTemp()) {
                System.out.println("selectedFile.getName(): " + selectedFile.getName());
                selectedFile = new File(fileChooser.getCurrentDirectory(), selectedFile.getName());
            }
            fileChooser.setSelectedFile(selectedFile);
        }
        try {
            final int returnValue = fileChooser.showSaveDialog(getParent());
            File file = fileChooser.getSelectedFile();
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (file != null) {
                    String ext = getExtension(file).toLowerCase();
                    String absolutePath = file.getAbsolutePath();
                    if (absolutePath.lastIndexOf('.') == -1) {
                        absolutePath += "." + ext;
                        file = new File(absolutePath);
                    }
                    setCurrentPath(file);
                    if (extFilter.isSavableModelExt(ext)) {
                        saveModel(model, file, ext, updateCurrent);
                    } else if (extFilter.isSavableTextureExt(ext) && bufferedImage != null) {
                        saveTexture(bufferedImage, file, ext);
                    }
                } else {
                    JOptionPane.showMessageDialog(getParent(),
                            "You tried to save, but you somehow didn't select a file.\nThat is bad.");
                }
            }
            fileChooser.setSelectedFile(null);
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
            exc.printStackTrace();
        }
    }

    public void exportInternalFile(String internalPath) {
        setCurrentDirectory(getModel());
        String fileName = internalPath.replaceAll(".+[\\\\/](?=.+)", "");
        File tempFile = new File(fileChooser.getCurrentDirectory(), fileName);
        fileChooser.setSelectedFile(tempFile);
        final int returnValue = fileChooser.showSaveDialog(getParent());
        File selectedFile = fileChooser.getSelectedFile();
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                Files.copy(GameDataFileSystem.getDefault().getResourceAsStream(internalPath), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
    }

    public void exportImage(Bitmap bitmap) {
        setFilter(FileDialog.OPEN_TEXTURE);
        setCurrentDirectory(getModel());
        fileChooser.setSelectedFile(new File(getPath(), bitmap.getName()));
        final int returnValue = fileChooser.showSaveDialog(getParent());
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) {
            String ext = getExtension(selectedFile).toLowerCase();
            String absolutePath = selectedFile.getAbsolutePath();
            if (absolutePath.lastIndexOf('.') == -1) {
                absolutePath += "." + ext;
                selectedFile = new File(absolutePath);
            }
            setCurrentPath(selectedFile);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                BufferedImage bufferedImage = BLPHandler.getImage(bitmap, null);
                if (bufferedImage != null) {
                    try {
                        saveTexture(bufferedImage, selectedFile, ext);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    public Bitmap importImage() {
        setFilter(FileDialog.OPEN_TEXTURE);
        setCurrentDirectory(getModel());
        final int returnValue = fileChooser.showOpenDialog(getParent());
        File selectedFile = fileChooser.getSelectedFile();
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (getModel().getFile() != null) {
                File modelDirectory = getModel().getFile().getParentFile();
                return new Bitmap(modelDirectory.toPath().relativize(selectedFile.toPath()).toString());
            }
            return new Bitmap(selectedFile.toPath().toString());
        }
        return null;
    }

    public void exportAnimatedFramePNG() {
        if (getModelPanel() != null) {
            final BufferedImage fBufferedImage = getModelPanel().getPerspArea().getViewport().getBufferedImage();
            if (fBufferedImage != null) {
                onClickSaveAs(null, fBufferedImage, SAVE_TEXTURE, false);
            }
        }
    }

    private void saveModel(EditableModel model, File modelFile, String ext, boolean updateCurrent) throws IOException {

        final MdlxModel mdlx = model.toMdlx();
        final FileOutputStream stream = new FileOutputStream(modelFile);

        if (ext.equals("mdl")) {
            MdxUtils.saveMdl(mdlx, stream);
        } else {
            MdxUtils.saveMdx(mdlx, stream);
        }
        model.setFileRef(modelFile);

        if (updateCurrent) {
            setCurrentModelFile(modelFile);
            // currentMDLDisp().resetBeenSaved();
            // TODO reset been saved
        }
        SaveProfile.get().addRecent(modelFile.getPath());
        MenuBar.updateRecent();
    }

    void onClickOpen() {
        onClickOpen(OPEN_FILE);
    }

    private void setCurrentModelFile(File modelFile) {
        setCurrentFile(modelFile);
        getModelPanel().getMenuItem().setName(getCurrentFile().getName().split("\\.")[0]);
        getModelPanel().getMenuItem().setToolTipText(getCurrentFile().getPath());
    }

    private void saveTexture(BufferedImage bufferedImage, File modelFile, String ext) throws IOException {
        String fileExtension = ext.toLowerCase();
        if (fileExtension.equals("bmp") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            JOptionPane.showMessageDialog(getParent(),
                    "Warning: Alpha channel was converted to black. Some data will be lost" +
                            "\nif you convert this texture back to Warcraft BLP.");
            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
        }
        final boolean write = ImageIO.write(bufferedImage, fileExtension, modelFile);
        SaveProfile.get().addRecent(modelFile.getPath());
        if (!write) {
            JOptionPane.showMessageDialog(getParent(), "File type unknown or unavailable");
        }
    }

    private void setCurrentDirectory(EditableModel model) {
        if ((model != null) && !model.isTemp() && (model.getFile() != null)) {
            fileChooser.setCurrentDirectory(model.getFile().getParentFile());
        } else if (getPath() != null) {
            fileChooser.setCurrentDirectory(new File(getPath()));
        }
    }

    void onClickSave() {
        System.out.println("saving");
        try {
            EditableModel model = getModel();
            if (model != null && !model.isTemp()) {
                File modelFile = model.getFile();
                if (modelFile != null) {
                    String extension = getExtension(modelFile);
                    saveModel(model, modelFile, extension, true);
                }
                // TODO reset been saved
            } else {
                onClickSaveAs();
            }
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
    }

    void onClickOpen(int operationType) {
        fileChooser.setDialogTitle("Open");
        setFilter(operationType);
        final EditableModel model = getModel();
        setCurrentDirectory(model);

        final int returnValue = fileChooser.showOpenDialog(getParent());

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile());
        }

        fileChooser.setSelectedFile(null);
    }

    public EditableModel chooseModelFile(int operationType) {
        fileChooser.setDialogTitle("Open");
        setFilter(operationType);
        setCurrentDirectory(getModel());

        final int returnValue = fileChooser.showOpenDialog(mainPanel);
        File file = fileChooser.getSelectedFile();
        EditableModel model = null;
        ;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (file != null) {
                String ext = getExtension(file).toLowerCase();
                String absolutePath = file.getAbsolutePath();
                if (absolutePath.lastIndexOf('.') == -1) {
                    absolutePath += "." + ext;
                    file = new File(absolutePath);
                }
                setCurrentPath(file);
                if (extFilter.isSavableModelExt(ext)) {
                    try {
                        model = MdxUtils.loadEditable(file);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else if (extFilter.isSavableTextureExt(ext)) {
                    int version = ext.equals("dds") ? 1000 : 800;
                    model = MPQBrowserView.getImagePlaneModel(file, version);
                }
            }
        }
        fileChooser.setSelectedFile(null);
        return model;
    }

    public EditableModel getModel() {
        if (mainPanel != null) {
            return mainPanel.currentMDL();
        } else if (modelPanel != null) {
            return modelPanel.getModel();
        } else {
            return null;
        }
    }

    private ModelPanel getModelPanel() {
        if (mainPanel != null) {
            return mainPanel.currentModelPanel();
        } else if (modelPanel != null) {
            return modelPanel;
        } else {
            return null;
        }
    }

    private JComponent getParent() {
        if (parent != null) {
            return parent;
        } else if (mainPanel != null) {
            return mainPanel;
        } else if (modelPanel != null) {
            return modelPanel.getParent();
        } else {
            return null;
        }
    }

    public void openFile(final File file) {
        if (file != null) {
            setCurrentFile(file);
            setCurrentPath(file);
            // frontArea.clearGeosets();
            // sideArea.clearGeosets();
            // botArea.clearGeosets();
//            mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
            MenuBar.toolsMenu.getAccessibleContext().setAccessibleDescription(
                    "Allows the user to control which parts of the model are displayed for editing.");
//            mainPanel.toolsMenu.setEnabled(true);
            MenuBar.toolsMenu.setEnabled(true);
            SaveProfile.get().addRecent(getCurrentFile().getPath());
            MenuBar.updateRecent();
            MPQBrowserView.loadFile(mainPanel, getCurrentFile());
        }
    }
}
