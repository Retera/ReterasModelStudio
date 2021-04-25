package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ExportTextureDialog {

    //ToDo figure out why these throw errors sometimes (might have to do with non-existing texture files)
    static void exportMaterialAsTextures(MainPanel mainPanel) {
        exportMaterialAsTextures(mainPanel, mainPanel.currentMDL());
    }

    static void exportMaterialAsTextures(JComponent mainPanel, EditableModel model) {
        IterableListModel<Material> materials = new IterableListModel<>();
        for (Material mat : model.getMaterials()) {
            materials.addElement(mat);
        }
        for (ParticleEmitter2 emitter2 : model.getParticleEmitter2s()) {
            Material dummyMaterial = new Material(new Layer("Blend", model.getTexture(emitter2.getTextureID())));
        }

        JList<Material> materialsList = new JList<>(materials);
        materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
        materialsList.setCellRenderer(new MaterialListRenderer(model));

        JScrollPane texturePane = new JScrollPane(materialsList);
        JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
        panel.add(texturePane, "wrap");

        FileDialog fileDialog = new FileDialog(mainPanel);
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportChosenMaterial(model, materialsList, fileDialog));
        panel.add(exportButton);

        JOptionPane.showOptionDialog(
                mainPanel, panel,
                "Export Material as Texture",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[] {"Close"}, "Close");
//        }
    }

    private static void exportChosenMaterial(EditableModel model, JList<Material> materialsList, FileDialog fileDialog) {
        if (materialsList.getSelectedValue() != null) {
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
        IterableListModel<Bitmap> bitmaps = new IterableListModel<>();
        for (Bitmap texture : model.getTextures()) {
            bitmaps.addElement(texture);
        }

        JList<Bitmap> bitmapJList = new JList<>(bitmaps);
        bitmapJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
        bitmapJList.setCellRenderer(new TextureListRenderer(model));


        JScrollPane texturePane = new JScrollPane(bitmapJList);
        JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
        panel.add(texturePane, "growx, wrap");

        FileDialog fileDialog = new FileDialog(mainPanel);
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportChosenTexture(model, bitmapJList, fileDialog));
        panel.add(exportButton);

        JOptionPane.showOptionDialog(
                mainPanel, panel,
                "Export Texture",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[] {"Close"}, "Close");
    }

    private static void exportChosenTexture(EditableModel model, JList<Bitmap> bitmapJList, FileDialog fileDialog) {
        if (bitmapJList.getSelectedValue() != null) {
            BufferedImage texture = BLPHandler.getImage(bitmapJList.getSelectedValue(), model.getWrappedDataSource());

            String name = bitmapJList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
            System.out.println(name);
//
            fileDialog.exportTexture(texture, name);
        }
    }
}
