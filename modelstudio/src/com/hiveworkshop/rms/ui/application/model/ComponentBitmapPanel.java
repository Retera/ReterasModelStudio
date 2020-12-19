package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapReplaceableIdAction;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapWrapHeightAction;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapWrapWidthAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ComponentBitmapPanel extends JPanel implements ComponentPanel<Bitmap> {

	private Bitmap bitmap;
	private final ComponentEditorTextField texturePathField;
	private final ComponentEditorJSpinner replaceableIdSpinner;
	private final JCheckBox wrapWidthBox;
	private final JCheckBox wrapHeightBox;
	private final JPanel previewPanel;
	private final UndoActionListener undoListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final ModelViewManager modelViewManager;

	public ComponentBitmapPanel(final ModelViewManager modelViewManager,
	                            final UndoActionListener undoListener,
	                            final ModelStructureChangeListener modelStructureChangeListener,
	                            final TextureExporter textureExporter) {
		this.modelViewManager = modelViewManager;
		this.undoListener = undoListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		texturePathField = new ComponentEditorTextField(24);
		texturePathField.addActionListener(e -> texturePathField());

		replaceableIdSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		replaceableIdSpinner.addActionListener(this::replaceableIdSpinner);

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

		final JButton exportTextureImageFile = new JButton("Export Texture Image File");
		exportTextureImageFile.addActionListener(e -> exportTextureImageFile(textureExporter));
		add(exportTextureImageFile, "cell 2 3, pushx");
		add(previewPanel, "cell 0 4 3, growx, growy");
	}

	private void exportTextureImageFile(TextureExporter textureExporter) {
		String suggestedName = texturePathField.getText();
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("\\") + 1);
		suggestedName = suggestedName.substring(suggestedName.lastIndexOf("/") + 1);
		textureExporter.exportTexture(suggestedName, (file, filter) -> BLPHandler.exportBitmapTextureFile(ComponentBitmapPanel.this, modelViewManager, bitmap, file), ComponentBitmapPanel.this);
	}

	private void wrapHeightBox() {
		final SetBitmapWrapHeightAction setBitmapWrapHeightAction = new SetBitmapWrapHeightAction(bitmap,
				bitmap.isWrapHeight(), wrapHeightBox.isSelected(), modelStructureChangeListener);
		setBitmapWrapHeightAction.redo();
		undoListener.pushAction(setBitmapWrapHeightAction);
	}

	private void wrapWidthBox() {
		final SetBitmapWrapWidthAction setBitmapWrapWidthAction = new SetBitmapWrapWidthAction(bitmap,
				bitmap.isWrapWidth(), wrapWidthBox.isSelected(), modelStructureChangeListener);
		setBitmapWrapWidthAction.redo();
		undoListener.pushAction(setBitmapWrapWidthAction);
	}

	private void replaceableIdSpinner() {
		final SetBitmapReplaceableIdAction setBitmapReplaceableIdAction = new SetBitmapReplaceableIdAction(
				bitmap, bitmap.getReplaceableId(), ((Number) replaceableIdSpinner.getValue()).intValue(),
				modelStructureChangeListener);
		setBitmapReplaceableIdAction.redo();
		undoListener.pushAction(setBitmapReplaceableIdAction);
	}

	private void texturePathField() {
		final SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(bitmap, bitmap.getPath(),
				texturePathField.getText(), modelStructureChangeListener);
		setBitmapPathAction.redo();
		undoListener.pushAction(setBitmapPathAction);
	}

	@Override
	public void setSelectedItem(final Bitmap bitmap) {
		this.bitmap = bitmap;
		texturePathField.reloadNewValue(bitmap.getPath());
		replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
		wrapWidthBox.setSelected(bitmap.isWrapWidth());
		wrapHeightBox.setSelected(bitmap.isWrapHeight());

		loadBitmapPreview(bitmap);
	}

	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {

	}

	private void loadBitmapPreview(final Bitmap defaultTexture) {
		if (defaultTexture != null) {
			final DataSource workingDirectory = modelViewManager.getModel().getWrappedDataSource();
			previewPanel.removeAll();
			try {
				final BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
				previewPanel.add(new ZoomableImagePreviewPanel(texture));
			} catch (final Exception exc) {
				final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
				previewPanel.add(new ZoomableImagePreviewPanel(image));
			}
			previewPanel.revalidate();
		}
	}
}
