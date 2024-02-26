package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.parsers.twiImageStuff.DDS.DDSFile;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import de.wc3data.image.TgaFile;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExportTexture extends ActionFunction {
	public ExportTexture(){
		super(TextKey.EXPORT_TEXTURE, ExportTexture::exportTextures2);
	}

	public static void exportTextures2(ModelHandler modelHandler){
		if (modelHandler != null) {
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.setMinimumSize(new Dimension(200, 200));

			JPanel texturePanel = getExportTexturePanel(modelHandler.getModel());
			tabbedPane.addTab("Export Texture", texturePanel);

			JPanel materialPanel = getExportMaterialPanel(modelHandler.getModel());
			tabbedPane.addTab("Export Material as Texture", materialPanel);

			showDialog(tabbedPane, "Export Texture");
		}
	}

    //ToDo figure out why these throw errors sometimes (might have to do with non-existing texture files)

	private static JPanel getExportMaterialPanel(EditableModel model) {
		TwiList<Material> materials = new TwiList<>();
		materials.addAll(model.getMaterials());
		for (ParticleEmitter2 emitter2 : model.getParticleEmitter2s()) {
			Material dummyMaterial = new Material(new Layer(getRealFilterMode(emitter2.getFilterMode()), emitter2.getTexture()));
			materials.add(dummyMaterial);
		}

		materials.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
		materials.setCellRenderer(new MaterialListRenderer(model));

		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		panel.add(materials.getScrollableList(), "growx, wrap");

		FileDialog fileDialog = new FileDialog();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> exportChosenMaterial(model, materials.getSelectedValue(), fileDialog));
		panel.add(exportButton);
		return panel;
	}

	private static FilterMode getRealFilterMode(MdlxParticleEmitter2.FilterMode fm){
		return switch (fm) {
			case ALPHAKEY -> FilterMode.TRANSPARENT;
			case BLEND -> FilterMode.BLEND;
			case ADDITIVE -> FilterMode.ADDITIVE;
			case MODULATE -> FilterMode.MODULATE;
			case MODULATE2X -> FilterMode.MODULATE2X;
		};
	}

	private static void exportChosenMaterial(EditableModel model, Material material, FileDialog fileDialog) {
		if (material != null) {
	        BufferedImage bufferedImage = ImageCreator.getBufferedImage(material, model.getWrappedDataSource());
	        String name = material.getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
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
		TwiList<Bitmap> bitmaps = new TwiList<>();
		bitmaps.addAll(model.getTextures());

		bitmaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //TODO would be nice to be able to batch save
		bitmaps.setCellRenderer(new TextureListRenderer(model));


		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		panel.add(bitmaps.getScrollableList(), "growx, wrap");

		FileDialog fileDialog = new FileDialog();
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> exportChosenTexture(model, bitmaps.getSelectedValue(), fileDialog));
		panel.add(exportButton);
		return panel;
	}

	private static void exportChosenTexture(EditableModel model, Bitmap selectedValue, FileDialog fileDialog) {
		if (selectedValue != null) {
            BufferedImage texture = BLPHandler.getImage(selectedValue, model.getWrappedDataSource());

            String name = selectedValue.getName().replaceAll("[^\\w\\[\\]()#\\. ]", "").replaceAll(" +", "_");
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
				SaveProfileNew.get().addRecent(modelFile.getPath());
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
