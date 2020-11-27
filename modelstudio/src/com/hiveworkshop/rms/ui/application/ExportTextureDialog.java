package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ExportTextureDialog {
    static void createExportTextureDialog(MainPanel mainPanel) {
        mainPanel.exportTextureDialog = new JFileChooser();
        mainPanel.exportTextureDialog.setDialogTitle("Export Texture");
        final String[] imageTypes = ImageIO.getWriterFileSuffixes();
        for (final String suffix : imageTypes) {
            mainPanel.exportTextureDialog
                    .addChoosableFileFilter(new FileNameExtensionFilter(suffix.toUpperCase() + " Image File", suffix));
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

    public static class TextureExporterImpl implements TextureExporter {
        private MainPanel mainPanel;

        public TextureExporterImpl(MainPanel mainPanel) {
            this.mainPanel = mainPanel;
        }

        public JFileChooser getFileChooser() {
            return mainPanel.exportTextureDialog;
        }

        @Override
        public void showOpenDialog(final String suggestedName, final TextureExporterClickListener fileHandler,
                                   final Component parent) {
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(new File(
                        mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }
            final int showOpenDialog = mainPanel.exportTextureDialog.showOpenDialog(parent);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    fileHandler.onClickOK(file, mainPanel.exportTextureDialog.getFileFilter());
                } else {
                    JOptionPane.showMessageDialog(parent, "No import file was specified");
                }
            }
        }

        @Override
        public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
                                  final Component parent) {

            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                final EditableModel current = mainPanel.currentMDL();
                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
                } else if (mainPanel.profile.getPath() != null) {
                    mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
                }
            }
            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
                mainPanel.exportTextureDialog.setSelectedFile(new File(
                        mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
            }

            final int x = mainPanel.exportTextureDialog.showSaveDialog(parent);
            if (x == JFileChooser.APPROVE_OPTION) {
                final File file = mainPanel.exportTextureDialog.getSelectedFile();
                if (file != null) {
                    try {
                        if (file.getName().lastIndexOf('.') >= 0) {
                            fileHandler.onClickOK(file, mainPanel.exportTextureDialog.getFileFilter());
                        } else {
                            JOptionPane.showMessageDialog(parent, "No file type was specified");
                        }
                    } catch (final Exception e2) {
                        ExceptionPopup.display(e2);
                        e2.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, "No output file was specified");
                }
            }
        }

    }
}
