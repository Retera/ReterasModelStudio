package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExportTextureDialog {
    static void createExportTextureDialog(MainPanel mainPanel) {
        mainPanel.exportTextureDialog = new JFileChooser();
        mainPanel.exportTextureDialog.setDialogTitle("Export Texture");
        final String[] imageTypes = ImageIO.getWriterFileSuffixes();
        for (final String suffix : imageTypes) {
            mainPanel.exportTextureDialog.addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
        }
    }

    static void createFileChooser(MainPanel mainPanel) {
        mainPanel.fc = new JFileChooser();
        mainPanel.fc.setAcceptAllFileFilterUsed(false);
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Supported Files (*.mdx;*.mdl;*.blp;*.dds;*.tga;*.png;*.obj,*.fbx)", "mdx", "mdl", "blp", "dds", "tga", "png", "obj", "fbx"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Files (*.mdx;*.mdl;*.blp;*.dds;*.tga)", "mdx", "mdl", "blp", "dds", "tga"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Binary Model (*.mdx)", "mdx"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III Text Model (*.mdl)", "mdl"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Warcraft III BLP Image (*.blp)", "blp"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("DDS Image (*.dds)", "dds"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("TGA Image (*.tga)", "tga"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Wavefront OBJ Model (*.obj)", "obj"));
        mainPanel.fc.addChoosableFileFilter(new FileNameExtensionFilter("Autodesk FBX Model (*.fbx)", "fbx"));
    }

    static void exportTextures(MainPanel mainPanel) {
        final DefaultListModel<Material> materials = new DefaultListModel<>();
        for (int i = 0; i < mainPanel.currentMDL().getMaterials().size(); i++) {
            final Material mat = mainPanel.currentMDL().getMaterials().get(i);
            materials.addElement(mat);
        }
        for (final ParticleEmitter2 emitter2 : mainPanel.currentMDL().sortedIdObjects(ParticleEmitter2.class)) {
            final Material dummyMaterial = new Material(new Layer("Blend", mainPanel.currentMDL().getTexture(emitter2.getTextureID())));
        }

        final JList<Material> materialsList = new JList<>(materials);
        materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialsList.setCellRenderer(new MaterialListRenderer(mainPanel.currentMDL()));
        JOptionPane.showMessageDialog(mainPanel, new JScrollPane(materialsList));

        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            final EditableModel current = mainPanel.currentMDL();
            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
            } else if (mainPanel.profile.getPath() != null) {
                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
            }
        }
        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
            mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory()
                    + File.separator + materialsList.getSelectedValue().getName()));
        }

        final int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
        if (x == JFileChooser.APPROVE_OPTION) {
            final File file = mainPanel.exportTextureDialog.getSelectedFile();
            if (file != null) {
                try {
                    if (file.getName().lastIndexOf('.') >= 0) {
                        BufferedImage bufferedImage = materialsList.getSelectedValue()
                                .getBufferedImage(mainPanel.currentMDL().getWrappedDataSource());
                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1).toUpperCase();
                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG") || fileExtension.equals("JPEG")) {
                            JOptionPane.showMessageDialog(mainPanel,
                                    "Warning: Alpha channel was converted to black. Some data will be lost" +
                                            "\nif you convert this texture back to Warcraft BLP.");
                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
                        }
                        if (fileExtension.equals("BLP")) {
                            fileExtension = "blp";
                        }
                        final boolean write = ImageIO.write(bufferedImage, fileExtension, file);
                        if (!write) {
                            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
                    }
                } catch (final Exception e1) {
                    ExceptionPopup.display(e1);
                    e1.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
            }
        }
    }

    public static class TextureExporterImpl implements TextureExporter {
        private final MainPanel mainPanel;

        public TextureExporterImpl(MainPanel mainPanel) {
            this.mainPanel = mainPanel;
        }

        public JFileChooser getFileChooser() {
            return mainPanel.exportTextureDialog;
        }

        @Override
        public void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
                                   final Component parent) {
            setCurrentDirectory(suggestedName);
            showWarningDialog(fileHandler, parent, false);
        }

        @Override
        public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
                                  final Component parent) {
            setCurrentDirectory(suggestedName);
            showWarningDialog(fileHandler, parent, true);
        }

        private void showWarningDialog(TextureExporterClickListener fileHandler, Component parent, boolean checkForFileExtention) {
            final int showOpenDialog = mainPanel.exportTextureDialog.showOpenDialog(parent);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    if (!checkForFileExtention || file.getName().lastIndexOf('.') >= 0) {
                        fileHandler.onClickOK(file, mainPanel.exportTextureDialog.getFileFilter());
                    } else {
                        JOptionPane.showMessageDialog(parent, "No file type was specified");
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "No file was specified");
//                    JOptionPane.showMessageDialog(parent, "No import file was specified");
//                    JOptionPane.showMessageDialog(parent, "No output file was specified");
                }
            }
        }

        private void setCurrentDirectory(String suggestedName) {
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }
        }

    }
}
