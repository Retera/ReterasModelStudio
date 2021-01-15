package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class FileDialog {
    public static final int OPEN_FILE = 0;
    public static final int OPEN_MODEL = 1;
    public static final int OPEN_WC_MODEL = 2;
    public static final int SAVE_MODEL = 3;
    public static final int OPEN_TEXTURE = 4;
    public static final int SAVE_TEXTURE = 5;
    public static final int SAVE = 6;

    private final MainPanel mainPanel;
    private final JFileChooser fileChooser;
    private List<FileNameExtensionFilter> openFilesExtensions;
    private List<FileNameExtensionFilter> openModelExtensions;
    private List<FileNameExtensionFilter> saveModelExtensions;
    private List<FileNameExtensionFilter> savableExtensions;
    private List<FileNameExtensionFilter> textureExtensions;
    //    private Set<String> acceptedExtensions;
    private Set<String> savableModelExtensions;
    private Set<String> savableTextureExtensions;
    private String defaultModelExtension = ".mdx";
    private String defaultTextureExtension = ".png";

    public FileDialog(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.fileChooser = getFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        fillExtensionLists();
    }

    void fillExtensionLists() {
        FileNameExtensionFilter OpenFiles = new FileNameExtensionFilter("Supported Files (*.mdx;*.mdl;*.blp;*.dds;*.tga;*.png;*.bmp;*.jpg;*.jpeg;*.obj,*.fbx)", "mdx", "mdl", "blp", "dds", "tga", "png", "bmp", "jpg", "jpeg", "obj", "fbx");
        FileNameExtensionFilter SaveFiles = new FileNameExtensionFilter("Supported Files (*.mdx;*.mdl;*.blp;*.dds;*.tga;*.png)", "mdx", "mdl", "blp", "dds", "tga", "png");
        FileNameExtensionFilter Image = new FileNameExtensionFilter("Supported Image Files (*.blp;*.dds;*.tga;*.png)", "blp", "dds", "tga", "png");
        FileNameExtensionFilter OpenModel = new FileNameExtensionFilter("Supported Model Files (*.mdx;*.mdl;*.obj,*.fbx)", "mdx", "mdl", "obj", "fbx");
        FileNameExtensionFilter WcModel = new FileNameExtensionFilter("Warcraft III Models (*.mdx;*.mdl)", "mdx", "mdl");
        FileNameExtensionFilter WcFiles = new FileNameExtensionFilter("Warcraft III Files (*.mdx;*.mdl;*.blp;*.dds;*.tga)", "mdx", "mdl", "blp", "dds", "tga");
        FileNameExtensionFilter BLP = new FileNameExtensionFilter("Warcraft III BLP Image (*.blp)", "blp");
        FileNameExtensionFilter DDS = new FileNameExtensionFilter("DDS Image (*.dds)", "dds");
        FileNameExtensionFilter TGA = new FileNameExtensionFilter("TGA Image (*.tga)", "tga");
        FileNameExtensionFilter PNG = new FileNameExtensionFilter("PNG Image (*.png)", "png");
        FileNameExtensionFilter JPG = new FileNameExtensionFilter("JPG Image (*.jpg;*.jpeg)", "jpg", "jpeg");
        FileNameExtensionFilter BMP = new FileNameExtensionFilter("BMP Image (*.bmp)", "bmp");
        FileNameExtensionFilter MDL = new FileNameExtensionFilter("Warcraft III Text Model (*.mdl)", "mdl");
        FileNameExtensionFilter MDX = new FileNameExtensionFilter("Warcraft III Binary Model (*.mdx)", "mdx");
        FileNameExtensionFilter OBJ = new FileNameExtensionFilter("Wavefront OBJ Model (*.obj)", "obj");
        FileNameExtensionFilter FBX = new FileNameExtensionFilter("Autodesk FBX Model (*.fbx)", "fbx");


        textureExtensions = new ArrayList<>(Arrays.asList(Image, BLP, DDS, TGA, PNG, JPG, BMP));

        saveModelExtensions = new ArrayList<>(Arrays.asList(WcModel, MDX, MDL));

        savableExtensions = new ArrayList<>(Arrays.asList(SaveFiles, WcModel, Image, MDX, MDL, BLP, DDS, TGA, PNG, JPG, BMP));

        openModelExtensions = new ArrayList<>(Arrays.asList(OpenModel, WcFiles, WcModel, MDX, MDL, OBJ, FBX));

        openFilesExtensions = new ArrayList<>(Arrays.asList(OpenFiles, WcFiles, WcModel, MDX, MDL, BLP, Image, DDS, TGA, PNG, OBJ, FBX));

//        fillExtensionSets();
        savableModelExtensions = new HashSet<>();
        savableTextureExtensions = new HashSet<>();
        for (FileNameExtensionFilter filter : saveModelExtensions) {
            savableModelExtensions.addAll(Arrays.asList(filter.getExtensions()));
        }
        for (FileNameExtensionFilter filter : textureExtensions) {
            savableTextureExtensions.addAll(Arrays.asList(filter.getExtensions()));
        }
    }

    public void saveFileDialog() {
        setFilter(saveModelExtensions);
        int saveOption = fileChooser.showSaveDialog(mainPanel);

        if (saveOption == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                if (!selectedFile.exists()) {
                    System.out.println("Should Save!");
//                    try {
////                        Files.copy(gameDataFileSystem.getResourceAsStream(clickedNode.getPath()), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                    } catch (final IOException e1) {
//                        ExceptionPopup.display(e1);
//                        e1.printStackTrace();
//                    }
                }
            }
        }
    }


    private void setFilter(int type) {
        switch (type) {
            case OPEN_FILE -> setFilter(openFilesExtensions);
            case OPEN_MODEL -> setFilter(openModelExtensions);
            case SAVE_MODEL, OPEN_WC_MODEL -> setFilter(saveModelExtensions);
            case OPEN_TEXTURE, SAVE_TEXTURE -> setFilter(textureExtensions);
            case SAVE -> setFilter(savableExtensions);
        }
    }

    private void setFilter(List<FileNameExtensionFilter> filters) {
        fileChooser.resetChoosableFileFilters();
//        acceptedExtensions = new HashSet<>();
        for (FileNameExtensionFilter filter : filters) {
            fileChooser.addChoosableFileFilter(filter);
//            acceptedExtensions.addAll(Arrays.asList(filter.getExtensions()));
        }
    }

    private void fillExtensionSets() {
        savableModelExtensions = new HashSet<>();
        savableTextureExtensions = new HashSet<>();
        for (FileNameExtensionFilter filter : saveModelExtensions) {
            savableModelExtensions.addAll(Arrays.asList(filter.getExtensions()));
        }
        for (FileNameExtensionFilter filter : textureExtensions) {
            savableTextureExtensions.addAll(Arrays.asList(filter.getExtensions()));
        }
    }

    // Creates an overwrite-prompt without closing the fileChooser dialog,
    // letting the user change the name to save as if choosing "Cancel"
    private JFileChooser getFileChooser() {
        return new JFileChooser() {
            @Override
            public void approveSelection() {
                final File selectedFile = this.getSelectedFile();
                System.out.println("filechooser this: " + this);
                System.out.println("dialog type: " + this.getDialogType());
                if (selectedFile.exists() && this.getDialogType() == JFileChooser.SAVE_DIALOG) {
                    int confirmOverwriteFile = JOptionPane.showConfirmDialog(
                            mainPanel,
                            "File \"" + selectedFile.getName() + "\" already exists. Overwrite anyway?",
                            "Export File",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (confirmOverwriteFile == JOptionPane.OK_OPTION) {
                        selectedFile.delete();
                    } else {
                        return;
                    }
                }
                super.approveSelection();
            }
        };
    }

    void onClickSaveAs() {
        final EditableModel model = mainPanel.currentMDL();
        onClickSaveAs(model, SAVE, true);
    }

    void onClickSaveAs(final EditableModel model, int operationType, boolean updateCurrent) {
        BufferedImage bufferedImage = null;
        if (operationType == SAVE_TEXTURE || operationType == SAVE) {
            if (model != null && model.getMaterial(0) != null) {
                bufferedImage = model.getMaterial(0).getBufferedImage(mainPanel.currentMDL().getWrappedDataSource());
            }
        }
//        new ArrayList<>(mainPanel.currentModelPanel().getModelEditorManager().getSelectionView().getSelectedFaces()).get(0).getGeoset().getMaterial().getBufferedImage(mainPanel.currentMDL().getWrappedDataSource())
        onClickSaveAs(model, bufferedImage, operationType, updateCurrent);
    }

    void onClickSaveAs(final EditableModel model, BufferedImage bufferedImage, int operationType, boolean updateCurrent) {
        try {
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

            final int returnValue = fileChooser.showSaveDialog(mainPanel);
            File modelFile = fileChooser.getSelectedFile();
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (modelFile != null) {
                    String ext = getExtension(modelFile).toLowerCase();
                    String absolutePath = modelFile.getAbsolutePath();
                    if (absolutePath.lastIndexOf('.') == -1) {
                        absolutePath += "." + ext;
                        modelFile = new File(absolutePath);
                    }
                    mainPanel.profile.setPath(modelFile.getParent());

                    if (savableModelExtensions.contains(ext)) {
                        saveModel(model, modelFile, ext, updateCurrent);
                    } else if (savableTextureExtensions.contains(ext) && bufferedImage != null) {
                        saveTexture(bufferedImage, modelFile, ext);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel,
                            "You tried to save, but you somehow didn't select a file.\nThat is bad.");
                }
            }
            fileChooser.setSelectedFile(null);
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
            exc.printStackTrace();
        }
    }

    public void exportAnimatedFramePNG() {
        if (mainPanel.currentModelPanel() != null) {
            final BufferedImage fBufferedImage = mainPanel.currentModelPanel().getPerspArea().getViewport().getBufferedImage();
//            final BufferedImage fBufferedImage = mainPanel.currentModelPanel().getAnimationViewer().getBufferedImage();
            if (fBufferedImage != null) {
                onClickSaveAs(null, fBufferedImage, SAVE_TEXTURE, false);
            }
        }
    }

    private String getExtension(File modelFile) {
        final String name = modelFile.getName();
        if (name.lastIndexOf('.') != -1) {
            return name.substring(name.lastIndexOf('.') + 1);
        } else {
            return ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
        }
    }

    private void saveModel(EditableModel model, File modelFile, String ext, boolean updateCurrent) throws IOException {

        final MdlxModel mdlx = model.toMdlx();
        final FileOutputStream stream = new FileOutputStream(modelFile);

        if (ext.equals(".mdl")) {
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
        MenuBar.updateRecent(mainPanel);
    }

    private void setCurrentModelFile(File modelFile) {
        mainPanel.currentFile = modelFile;
        mainPanel.currentModelPanel().getMenuItem().setName(mainPanel.currentFile.getName().split("\\.")[0]);
        mainPanel.currentModelPanel().getMenuItem().setToolTipText(mainPanel.currentFile.getPath());
    }

    private void saveTexture(BufferedImage bufferedImage, File modelFile, String ext) throws IOException {
        String fileExtension = ext.toLowerCase();
        if (fileExtension.equals("bmp") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Warning: Alpha channel was converted to black. Some data will be lost" +
                            "\nif you convert this texture back to Warcraft BLP.");
            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
        }
        final boolean write = ImageIO.write(bufferedImage, fileExtension, modelFile);
        SaveProfile.get().addRecent(modelFile.getPath());
        if (!write) {
            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
        }

    }

    private void setCurrentDirectory(EditableModel model) {
        if ((model != null) && !model.isTemp() && (model.getFile() != null)) {
            fileChooser.setCurrentDirectory(model.getFile().getParentFile());
        } else if (mainPanel.profile.getPath() != null) {
            fileChooser.setCurrentDirectory(new File(mainPanel.profile.getPath()));
        }
    }

    void onClickSave() {
        System.out.println("saving");
        try {
            EditableModel model = mainPanel.currentMDL();
            if (model != null && !model.isTemp()) {
                File modelFile = model.getFile();
                if (modelFile != null) {
                    String extension = getExtension(modelFile);
                    saveModel(model, modelFile, extension, true);
                }
//                String extention =  model.getFile().getName().split(".+(?=\\..+)")[1];
//
//                MdxUtils.saveMdx(model, modelFile);
//                mainPanel.profile.setPath(modelFile.getParent());
                // currentMDLDisp().resetBeenSaved();
//                SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
                // TODO reset been saved
            } else {
                onClickSaveAs();
            }
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
    }

    void onClickOpen() {
        onClickOpen(OPEN_FILE);
    }

    void onClickOpen(int operationType) {
        fileChooser.setDialogTitle("Open");
        setFilter(operationType);
        final EditableModel model = mainPanel.currentMDL();
        setCurrentDirectory(model);

        final int returnValue = fileChooser.showOpenDialog(mainPanel);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile());
        }

        fileChooser.setSelectedFile(null);
    }

    public EditableModel chooseModelFile(int operationType) {
        fileChooser.setDialogTitle("Open");
        setFilter(operationType);
        setCurrentDirectory(mainPanel.currentMDL());

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
                mainPanel.profile.setPath(file.getParent());

                if (savableModelExtensions.contains(ext)) {
                    try {
                        model = MdxUtils.loadEditable(file);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else if (savableTextureExtensions.contains(ext)) {
                    int version = ext.equals("dds") ? 1000 : 800;
                    model = MPQBrowserView.getImagePlaneModel(file, version);
                }
            }
        }
        fileChooser.setSelectedFile(null);
        return model;
    }

    public void openFile(final File file) {
        if (file != null) {
            mainPanel.currentFile = file;
            mainPanel.profile.setPath(file.getParent());
            // frontArea.clearGeosets();
            // sideArea.clearGeosets();
            // botArea.clearGeosets();
            mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                    "Allows the user to control which parts of the model are displayed for editing.");
            mainPanel.toolsMenu.setEnabled(true);
            SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
            MenuBar.updateRecent(mainPanel);
            MPQBrowserView.loadFile(mainPanel, mainPanel.currentFile);
        }
    }
}
