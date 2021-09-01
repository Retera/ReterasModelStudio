package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapReplaceableIdAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapWrapHeightAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapWrapWidthAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ComponentBitmapPanel extends ComponentPanel<Bitmap> {

	private Bitmap bitmap;
	private final ComponentEditorTextField texturePathField;
	private final IntEditorJSpinner replaceableIdSpinner;
	private final JCheckBox wrapWidthBox;
	private final JCheckBox wrapHeightBox;
	private final JPanel previewPanel;
	private final FileDialog fileDialog;

	public ComponentBitmapPanel(ModelHandler modelHandler) {
		super(modelHandler);
		texturePathField = new ComponentEditorTextField(24);
		texturePathField.addEditingStoppedListener(this::texturePathField);
		fileDialog = new FileDialog(this);

		replaceableIdSpinner = new IntEditorJSpinner(-1, -1, this::replaceableIdSpinner);

		wrapWidthBox = new JCheckBox("Wrap Width");
		wrapWidthBox.addActionListener(e -> wrapWidthBox());

		wrapHeightBox = new JCheckBox("Wrap Height");
		wrapHeightBox.addActionListener(e -> wrapHeightBox());

		previewPanel = new JPanel();
		previewPanel.setBorder(new TitledBorder(null, "Previewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		previewPanel.setLayout(new BorderLayout());

		setLayout(new MigLayout("fillx", "[][grow][]", "[][][][][grow]"));
		add(new JLabel("Path: "), "cell 0 0");
		add(texturePathField, "cell 1 0 2, growx");
		add(new JLabel("ReplaceableId: "), "cell 0 1");
		add(replaceableIdSpinner, "cell 1 1 2");
		add(wrapWidthBox, "cell 0 2 3");
		add(wrapHeightBox, "cell 0 3");


		add(getButton("Export Texture Image File", e -> exportTextureImageFile()), "cell 2 3, pushx");
		add(previewPanel, "cell 0 4 3, growx, growy");
	}

	private void exportTextureImageFile() {
		DataSource workingDirectory = modelHandler.getModel().getWrappedDataSource();
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
		String suggestedName = texturePathField.getText();
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("\\") + 1);
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("/") + 1);
		fileDialog.exportTexture(texture, suggestedName);
	}

	private void wrapHeightBox() {
		undoManager.pushAction(new SetBitmapWrapHeightAction(bitmap, wrapHeightBox.isSelected(), changeListener).redo());
	}

	private void wrapWidthBox() {
		undoManager.pushAction(new SetBitmapWrapWidthAction(bitmap, wrapWidthBox.isSelected(), changeListener).redo());
	}

	private void replaceableIdSpinner(int newValue) {
		undoManager.pushAction(new SetBitmapReplaceableIdAction(bitmap, newValue, changeListener).redo());
	}

	private void texturePathField() {
		undoManager.pushAction(new SetBitmapPathAction(bitmap, texturePathField.getText(), changeListener).redo());
	}

	@Override
	public void setSelectedItem(Bitmap bitmap) {
		this.bitmap = bitmap;
		texturePathField.reloadNewValue(bitmap.getPath());
		replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
		wrapWidthBox.setSelected(bitmap.isWrapWidth());
		wrapHeightBox.setSelected(bitmap.isWrapHeight());

		loadBitmapPreview(bitmap);
	}

	private void loadBitmapPreview(Bitmap defaultTexture) {
		if (defaultTexture != null) {
			DataSource workingDirectory = modelHandler.getModel().getWrappedDataSource();
			previewPanel.removeAll();
			previewPanel.add(getZoomableImagePreviewPanel(defaultTexture, workingDirectory));
			previewPanel.revalidate();
		}
	}

	private ZoomableImagePreviewPanel getZoomableImagePreviewPanel(Bitmap defaultTexture, DataSource workingDirectory) {
		ZoomableImagePreviewPanel comp;
		try {
			comp = new ZoomableImagePreviewPanel(BLPHandler.getImage(defaultTexture, workingDirectory));
		} catch (final Exception exc) {
			BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.RED);
			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			comp = new ZoomableImagePreviewPanel(image);
		}
		return comp;
	}
}
