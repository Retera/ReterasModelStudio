package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;

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

    //ToDo figure out why these throw errors sometimes (might have to do with non-existing texture files)
    static void exportMaterialAsTextures(MainPanel mainPanel) {
        exportMaterialAsTextures(mainPanel, mainPanel.currentMDL());
    }

    static void exportMaterialAsTextures(JComponent mainPanel, EditableModel model) {
        final DefaultListModel<Material> materials = new DefaultListModel<>();
        for (int i = 0; i < model.getMaterials().size(); i++) {
            final Material mat = model.getMaterials().get(i);
            materials.addElement(mat);
        }
        for (final ParticleEmitter2 emitter2 : model.sortedIdObjects(ParticleEmitter2.class)) {
            final Material dummyMaterial = new Material(new Layer("Blend", model.getTexture(emitter2.getTextureID())));
        }

        final JList<Material> materialsList = new JList<>(materials);
        materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
        materialsList.setCellRenderer(new MaterialListRenderer(model));
//        JOptionPane.showMessageDialog(mainPanel, new JScrollPane(materialsList));
        int option = JOptionPane.showConfirmDialog(mainPanel, new JScrollPane(materialsList), "Export Texture", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            FileDialog fileDialog = new FileDialog(mainPanel);

            BufferedImage bufferedImage = materialsList.getSelectedValue().getBufferedImage(model.getWrappedDataSource());
            String name = materialsList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
            System.out.println(name);

            fileDialog.exportTexture(bufferedImage, name);
        }
    }

    static void exportTextures(MainPanel mainPanel) {
        exportTextures(mainPanel, mainPanel.currentMDL());
    }

    static void exportTextures(JComponent mainPanel, EditableModel model) {
        final DefaultListModel<Bitmap> bitmaps = new DefaultListModel<>();
        for (int i = 0; i < model.getTextures().size(); i++) {
            final Bitmap texture = model.getTextures().get(i);
            bitmaps.addElement(texture);
        }
//        for (final ParticleEmitter2 emitter2 : mainPanel.currentMDL().sortedIdObjects(ParticleEmitter2.class)) {
//            final Material dummyMaterial = new Material(new Layer("Blend", model.getTexture(emitter2.getTextureID())));
//        }

        final JList<Bitmap> bitmapJList = new JList<>(bitmaps);
        bitmapJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
        bitmapJList.setCellRenderer(new TextureListRenderer(model));
//        JOptionPane.showMessageDialog(mainPanel, new JScrollPane(bitmapJList));
        int option = JOptionPane.showConfirmDialog(mainPanel, new JScrollPane(bitmapJList), "Export Texture", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            FileDialog fileDialog = new FileDialog(mainPanel);

            final BufferedImage texture = BLPHandler.getImage(bitmapJList.getSelectedValue(), model.getWrappedDataSource());
//        BufferedImage bufferedImage = bitmapJList.getSelectedValue().getBufferedImage(mainPanel.currentMDL().getWrappedDataSource());
//
            String name = bitmapJList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
            System.out.println(name);
//
            fileDialog.exportTexture(texture, name);
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
//            setCurrentDirectory(suggestedName);
            showWarningDialog(fileHandler, parent, false);
        }

        @Override
        public void exportTexture(final String suggestedName, final TextureExporterClickListener fileHandler,
                                  final Component parent) {
//            setCurrentDirectory(suggestedName);
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

//        private void setCurrentDirectory(String suggestedName) {
//            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//                final EditableModel current = mainPanel.currentMDL();
//                if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
//                    mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
//                } else if (FileDialog.getPath() != null) {
//                    mainPanel.fc.setCurrentDirectory(new File(FileDialog.getPath()));
//                }
//            }
//            if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//                mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator + suggestedName));
//            }
//        }

    }
}
