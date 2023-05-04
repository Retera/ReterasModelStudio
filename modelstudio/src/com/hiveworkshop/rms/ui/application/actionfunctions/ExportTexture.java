package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.parsers.twiImageStuff.DDS.DDSFile;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.IterableListModel;
import de.wc3data.image.TgaFile;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExportTexture extends ActionFunction {
	public ExportTexture(){
		super(TextKey.EXPORT_TEXTURE, () -> exportTextures2());
	}

	public static void exportTextures2(){
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel() != null) {
//			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.setMinimumSize(new Dimension(200, 200));

			JPanel texturePanel = getExportTexturePanel(modelPanel.getModel());
			tabbedPane.addTab("Export Texture", texturePanel);

			JPanel materialPanel = getExportMaterialPanel(modelPanel.getModel());
			tabbedPane.addTab("Export Material as Texture", materialPanel);

			showDialog(tabbedPane, "Export Texture");
		}
	}

    //ToDo figure out why these throw errors sometimes (might have to do with non-existing texture files)

	private static JPanel getExportMaterialPanel(EditableModel model) {
		IterableListModel<Material> materials = new IterableListModel<>();
		for (Material mat : model.getMaterials()) {
			materials.addElement(mat);
		}
		for (ParticleEmitter2 emitter2 : model.getParticleEmitter2s()) {
			Material dummyMaterial = new Material(new Layer(FilterMode.BLEND, emitter2.getTexture()));
		}

		JList<Material> materialsList = new JList<>(materials);
		materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
		materialsList.setCellRenderer(new MaterialListRenderer(model));

		JScrollPane texturePane = new JScrollPane(materialsList);
		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		panel.add(texturePane, "growx, wrap");

		FileDialog fileDialog = new FileDialog();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> exportChosenMaterial(model, materialsList, fileDialog));
		panel.add(exportButton);
		return panel;
	}

	private static void exportChosenMaterial(EditableModel model, JList<Material> materialsList, FileDialog fileDialog) {
        if (materialsList.getSelectedValue() != null) {
	        BufferedImage bufferedImage = ImageCreator.getBufferedImage(materialsList.getSelectedValue(), model.getWrappedDataSource());
	        String name = materialsList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
            System.out.println("ExportTexture, material name: " + name);

            onClickSaveAs(bufferedImage, name, FileDialog.SAVE_TEXTURE, fileDialog, ProgramGlobals.getMainPanel());
        }
    }

	private static void showDialog(JComponent panel, String title) {
		JOptionPane.showOptionDialog(
				ProgramGlobals.getMainPanel(), panel,
				title,
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, new String[] {"Close"}, "Close");
	}

	private static JPanel getExportTexturePanel(EditableModel model) {
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

		FileDialog fileDialog = new FileDialog();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> exportChosenTexture(model, bitmapJList, fileDialog));
		panel.add(exportButton);
		return panel;
	}

	private static void exportChosenTexture(EditableModel model, JList<Bitmap> bitmapJList, FileDialog fileDialog) {
        if (bitmapJList.getSelectedValue() != null) {
            BufferedImage texture = BLPHandler.getImage(bitmapJList.getSelectedValue(), model.getWrappedDataSource());

            String name = bitmapJList.getSelectedValue().getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
	        System.out.println("ExportTexture, texture name: " + name);
//
	        onClickSaveAs(texture, name, FileDialog.SAVE_TEXTURE, fileDialog, ProgramGlobals.getMainPanel());
        }
    }

	public static boolean onClickSaveAs(BufferedImage bufferedImage, String suggestedName, int operationType, Component parent) {
		return onClickSaveAs(bufferedImage, suggestedName, operationType, new FileDialog(parent), parent);
	}
	public static boolean onClickSaveAs(BufferedImage bufferedImage, String suggestedName, int operationType, FileDialog fileDialog, Component parent) {
		File file = fileDialog.getSaveFile(operationType, suggestedName);
		if (file != null) {
			if (fileDialog.isSavableTextureExt(file) && bufferedImage != null) {
				try {
					return saveTexture(bufferedImage, file, fileDialog.getExtension(file), parent);
				} catch (final Exception exc) {
					ExceptionPopup.display(exc);
					exc.printStackTrace();
				}
			}
		}
		return false;
	}
	public static boolean saveTexture(BufferedImage bufferedImage, File modelFile, String ext, Component parent) {
		String fileExtension = ext.toLowerCase();
		if (fileExtension.equals("bmp") || fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
			JOptionPane.showMessageDialog(parent,
					"Warning: Alpha channel was converted to black. Some data will be lost" +
							"\nif you convert this texture back to Warcraft BLP.");
			bufferedImage = ImageUtils.removeAlphaChannel(bufferedImage);
		}

		try {
			if(fileExtension.equals("tga")){
				TgaFile.writeTGA(bufferedImage, modelFile);
				return true;
			} else if(fileExtension.equals("dds")){
				System.out.println("writing dds! " + "\"" + modelFile.getAbsolutePath() + "\"");
				DDSFile.writeDDS(bufferedImage, modelFile);
				return true;
			} else {
				System.out.println("writing " + fileExtension);
				final boolean write = ImageIO.write(bufferedImage, fileExtension, modelFile);
				SaveProfile.get().addRecent(modelFile.getPath());
				if (!write) {
					JOptionPane.showMessageDialog(parent, "Could not write file.\nFile type unknown or unavailable");
				}
				return write;
			}
		} catch (final Exception exc) {
			ExceptionPopup.display(exc);
			exc.printStackTrace();
		}
		return false;
	}
}
