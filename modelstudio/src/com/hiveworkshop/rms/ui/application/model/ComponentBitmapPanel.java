package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
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
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class ComponentBitmapPanel extends JPanel implements ComponentPanel {

	private Bitmap bitmap;
	private final ComponentEditorTextField texturePathField;
	private final ComponentEditorJSpinner replaceableIdSpinner;
	private final JCheckBox wrapWidthBox;
	private final JCheckBox wrapHeightBox;
	private final JPanel previewPanel;
	private UndoActionListener undoListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private ModelViewManager modelViewManager;

	public ComponentBitmapPanel(final TextureExporter textureExporter) {
		texturePathField = new ComponentEditorTextField(24);
		texturePathField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(bitmap, bitmap.getPath(),
						texturePathField.getText(), modelStructureChangeListener);
				setBitmapPathAction.redo();
				undoListener.pushAction(setBitmapPathAction);
			}
		});
		replaceableIdSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		replaceableIdSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetBitmapReplaceableIdAction setBitmapReplaceableIdAction = new SetBitmapReplaceableIdAction(
						bitmap, bitmap.getReplaceableId(), ((Number) replaceableIdSpinner.getValue()).intValue(),
						modelStructureChangeListener);
				setBitmapReplaceableIdAction.redo();
				undoListener.pushAction(setBitmapReplaceableIdAction);
			}
		});
		wrapWidthBox = new JCheckBox("Wrap Width");
		wrapWidthBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SetBitmapWrapWidthAction setBitmapWrapWidthAction = new SetBitmapWrapWidthAction(bitmap,
						bitmap.isWrapWidth(), wrapWidthBox.isSelected(), modelStructureChangeListener);
				setBitmapWrapWidthAction.redo();
				undoListener.pushAction(setBitmapWrapWidthAction);
			}
		});
		wrapHeightBox = new JCheckBox("Wrap Height");
		wrapHeightBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final SetBitmapWrapHeightAction setBitmapWrapHeightAction = new SetBitmapWrapHeightAction(bitmap,
						bitmap.isWrapHeight(), wrapHeightBox.isSelected(), modelStructureChangeListener);
				setBitmapWrapHeightAction.redo();
				undoListener.pushAction(setBitmapWrapHeightAction);
			}
		});
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
		exportTextureImageFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String suggestedName = texturePathField.getText();
				suggestedName = suggestedName.substring(suggestedName.lastIndexOf("\\") + 1);
				suggestedName = suggestedName.substring(suggestedName.lastIndexOf("/") + 1);
				textureExporter.exportTexture(suggestedName, new TextureExporter.TextureExporterClickListener() {

					@Override
					public void onClickOK(final File file, final FileFilter filter) {
						BLPHandler.exportBitmapTextureFile(ComponentBitmapPanel.this, modelViewManager, bitmap, file);
					}
				}, ComponentBitmapPanel.this);
			}
		});
		add(exportTextureImageFile, "cell 2 3, pushx");
		add(previewPanel, "cell 0 4 3, growx, growy");
	}

	public void setBitmap(final Bitmap bitmap, final ModelViewManager modelViewManager,
			final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener) {
		this.bitmap = bitmap;
		this.modelViewManager = modelViewManager;
		this.undoListener = undoListener;
		this.modelStructureChangeListener = modelStructureChangeListener;

		texturePathField.reloadNewValue(bitmap.getPath());
		replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
		wrapWidthBox.setSelected(bitmap.isWrapWidth());
		wrapHeightBox.setSelected(bitmap.isWrapHeight());

		loadBitmapPreview(modelViewManager, bitmap);
	}

	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
                     final ModelStructureChangeListener changeListener) {

	}

	private void loadBitmapPreview(final ModelView modelView, final Bitmap defaultTexture) {
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
