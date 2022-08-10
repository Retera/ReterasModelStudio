package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapReplaceableIdAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapWrapHeightAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapWrapWidthAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ComponentBitmapPanel extends ComponentPanel<Bitmap> {

	private Bitmap bitmap;
	private final ComponentEditorTextField texturePathField;
	private final IntEditorJSpinner replaceableIdSpinner;
	private final JLabel sizeLabel;
	private final JCheckBox wrapWidthBox;
	private final JCheckBox wrapHeightBox;
	private final JPanel previewPanel;
	private final ZoomableImagePreviewPanel imagePreviewPanel;
	private final FileDialog fileDialog;
	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;

	public ComponentBitmapPanel(ModelHandler modelHandler) {
		super(modelHandler);
		texturePathField = new ComponentEditorTextField(24, this::texturePathField);
		fileDialog = new FileDialog(this);

		replaceableIdSpinner = new IntEditorJSpinner(-1, -1, this::replaceableIdSpinner);

		sizeLabel = new JLabel();

		wrapWidthBox = new JCheckBox("Wrap Width");
		wrapWidthBox.addActionListener(e -> wrapWidthBox(wrapWidthBox.isSelected()));

		wrapHeightBox = new JCheckBox("Wrap Height");
		wrapHeightBox.addActionListener(e -> wrapHeightBox(wrapHeightBox.isSelected()));

		imagePreviewPanel = new ZoomableImagePreviewPanel(null);
		previewPanel = new JPanel();
		previewPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(imagePreviewPanel);

		JLabel pathLab = new JLabel("Path: ");
		JLabel replaceableIdLab = new JLabel("ReplaceableId: ");
		JButton exportButton = getButton("Export", e -> exportTextureImageFile());

		setLayout(new MigLayout("fillx", "[][grow][][]", "[][][][][grow]"));
//		add(pathLab,                "cell 0 0");
//		add(texturePathField,       "cell 1 0 3, growx");
//		add(replaceableIdLab,        "cell 0 1");
//		add(replaceableIdSpinner,   "cell 1 1");
//		add(sizeLabel,              "cell 3 1");
//		add(wrapWidthBox,           "cell 0 2");
//		add(wrapHeightBox,          "cell 0 3");
//		add(getColorModeBox(),      "cell 2 3, pushx");
//		add(exportButton,           "cell 3 3");
//
//
//		add(previewPanel, "cell 0 4 4, growx, growy");

		add(pathLab,                "");
		add(texturePathField,       "spanx, growx, wrap");
		add(replaceableIdLab,       "");
		add(replaceableIdSpinner,   "spanx 2");
		add(sizeLabel,              "wrap");
		add(wrapWidthBox,           "wrap");
		add(wrapHeightBox,          "");
		add(getColorModeBox(),      "spanx 2, right");
		add(exportButton,           "right, wrap");


		add(previewPanel, "spanx, growx, growy");
	}

	private void exportTextureImageFile() {
		DataSource workingDirectory = modelHandler.getModel().getWrappedDataSource();
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
		String suggestedName = texturePathField.getText();
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("\\") + 1);
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("/") + 1);
		ExportTexture.onClickSaveAs(texture, suggestedName, FileDialog.SAVE_TEXTURE, fileDialog, ProgramGlobals.getMainPanel());
	}

	private void wrapHeightBox(boolean b) {
		if(bitmap.isWrapHeight() != b){
			undoManager.pushAction(new SetBitmapWrapHeightAction(bitmap, b, changeListener).redo());
		}
	}

	private void wrapWidthBox(boolean b) {
		if(bitmap.isWrapWidth() != b){
			undoManager.pushAction(new SetBitmapWrapWidthAction(bitmap, b, changeListener).redo());
		}
	}

	private void replaceableIdSpinner(int newValue) {
		if(bitmap.getReplaceableId() != newValue){
			undoManager.pushAction(new SetBitmapReplaceableIdAction(bitmap, newValue, changeListener).redo());
		}
	}

	private void texturePathField(String newPath) {
		if(!bitmap.getPath().equals(newPath)){
			undoManager.pushAction(new SetBitmapPathAction(bitmap, newPath, changeListener).redo());
		}
	}

	@Override
	public ComponentPanel<Bitmap> setSelectedItem(Bitmap bitmap) {
		this.bitmap = bitmap;
		sizeLabel.setText("");
		texturePathField.reloadNewValue(bitmap.getPath());
		replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
		wrapWidthBox.setSelected(bitmap.isWrapWidth());
		wrapHeightBox.setSelected(bitmap.isWrapHeight());

		loadBitmapPreview(bitmap);
		return this;
	}

	private void loadBitmapPreview(Bitmap bitmap) {
		if (bitmap != null) {
			DataSource workingDirectory = modelHandler.getModel().getWrappedDataSource();
			imagePreviewPanel.setImage(getImage(bitmap, workingDirectory));
			previewPanel.repaint();
		}
	}

	private TwiComboBox<ImageUtils.ColorMode> getColorModeBox() {
		TwiComboBox<ImageUtils.ColorMode> colorModeGroup = new TwiComboBox<>(ImageUtils.ColorMode.values(), ImageUtils.ColorMode.GREEN_GREEN);
		colorModeGroup.addOnSelectItemListener(this::setColorMode);
		colorModeGroup.selectOrFirst(ImageUtils.ColorMode.RGBA);
		return colorModeGroup;
	}

	private void setColorMode(ImageUtils.ColorMode colorMode){
		this.colorMode = colorMode;
		loadBitmapPreview(bitmap);
	}

	private BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory){
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
		if(texture != null){
			sizeLabel.setText(texture.getWidth() + " px * " + texture.getHeight() + " px");
			if(colorMode == ImageUtils.ColorMode.RGBA){
				return texture;
			} else {
				return ImageUtils.getBufferedImageIsolateChannel(texture, colorMode);
			}
		} else {
			int imageSize = 256;
			final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.RED);
			int size = 220;
			GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, size);
			int dist1 = (imageSize - size)/2;
			int dist2 = imageSize-dist1;
			GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return image;
		}
	}
}
