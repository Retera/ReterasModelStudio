package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.util.IterableListModel;

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
        IterableListModel<Bitmap> bitmaps = new IterableListModel<>();
        for (Bitmap texture : model.getTextures()) {
            bitmaps.addElement(texture);
        }

        JList<Bitmap> bitmapJList = new JList<>(bitmaps);
        bitmapJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
        bitmapJList.setCellRenderer(new TextureListRenderer(model));

        int option = JOptionPane.showConfirmDialog(mainPanel, new JScrollPane(bitmapJList), "Export Texture", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            FileDialog fileDialog = new FileDialog(mainPanel);

            BufferedImage texture = BLPHandler.getImage(bitmapJList.getSelectedValue(), model.getWrappedDataSource());

            String name = bitmapJList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
            System.out.println(name);
//
            fileDialog.exportTexture(texture, name);
        }
    }
}
