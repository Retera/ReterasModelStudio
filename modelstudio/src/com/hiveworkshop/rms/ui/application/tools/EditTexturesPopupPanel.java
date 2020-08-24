package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter.TextureExporterClickListener;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class EditTexturesPopupPanel extends JPanel {
	private final JTextField pathField;
	private final ModelView modelView;
	private final ModelStructureChangeListener listener;
	private final JPanel panel_1;

	/**
	 * Create the panel.
	 */
	public EditTexturesPopupPanel(final ModelView modelView, final ModelStructureChangeListener listener,
								  final TextureExporter textureExporter) {
		this.modelView = modelView;
		this.listener = listener;
		setLayout(null);

		final JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Textures", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(16, 17, 297, 507);
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		final JCheckBox chckbxDisplayPath = new JCheckBox("Display Path");

		final JList<Bitmap> list = new JList<Bitmap>();
		chckbxDisplayPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				list.repaint();
			}
		});
		panel.add(new JScrollPane(list));
		Bitmap defaultTexture = null;
		final DefaultListModel<Bitmap> bitmapListModel = new DefaultListModel<>();
		for (final Bitmap bitmap : modelView.getModel().getTextures()) {
			if ((bitmap.getPath() != null) && (bitmap.getPath().length() > 0)) {
				bitmapListModel.addElement(bitmap);
				if (defaultTexture == null) {
					defaultTexture = bitmap;
				}
			}
		}
		list.setModel(bitmapListModel);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
					final boolean isSelected, final boolean cellHasFocus) {
				if (value instanceof Bitmap) {
					final String path = ((Bitmap) value).getPath();
					if (!chckbxDisplayPath.isSelected()) {
						final String displayName = path.substring(path.lastIndexOf("\\") + 1);
						return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
					} else {
						return super.getListCellRendererComponent(list, path, index, isSelected, cellHasFocus);
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					pathField.setText(selectedValue.getPath());
					loadBitmap(modelView, list.getSelectedValue());
				}
			}
		});
		panel.add(chckbxDisplayPath, BorderLayout.SOUTH);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(323, 17, 439, 507);
		panel_1.setLayout(new BorderLayout());
		add(panel_1);

		loadBitmap(modelView, defaultTexture);

		final JButton importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				textureExporter.showOpenDialog("", new TextureExporterClickListener() {

					@Override
					public void onClickOK(final File file, final FileFilter filter) {
						final Bitmap newBitmap = new Bitmap(file.getName());
						modelView.getModel().add(newBitmap);
						bitmapListModel.addElement(newBitmap);
						listener.texturesChanged();
					}
				}, EditTexturesPopupPanel.this);
			}
		});
		importButton.setBounds(26, 535, 89, 23);
		add(importButton);

		final JButton exportButton = new JButton("Export");
		exportButton.setBounds(125, 535, 89, 23);
		add(exportButton);
		exportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					String selectedPath = selectedValue.getPath();
					selectedPath = selectedPath.substring(selectedPath.lastIndexOf("\\") + 1);
					textureExporter.exportTexture(selectedPath, new TextureExporterClickListener() {

						@Override
						public void onClickOK(final File file, final FileFilter filter) {
							BLPHandler.exportBitmapTextureFile(EditTexturesPopupPanel.this, modelView, selectedValue, file);
						}

					}, EditTexturesPopupPanel.this);
				}
			}
		});

		final JButton btnReplaceTexture = new JButton("Replace Texture");
		btnReplaceTexture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					textureExporter.showOpenDialog("", new TextureExporterClickListener() {
						@Override
						public void onClickOK(final File file, final FileFilter filter) {
							selectedValue.setPath(file.getName());
							list.repaint();
							loadBitmap(modelView, selectedValue);
							listener.texturesChanged();
						}
					}, EditTexturesPopupPanel.this);
				}
			}
		});
		btnReplaceTexture.setBounds(25, 569, 185, 23);
		add(btnReplaceTexture);

		final JButton btnRemove = new JButton("Remove");
		btnRemove.setBounds(224, 535, 89, 23);
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					modelView.getModel().remove(selectedValue);
					bitmapListModel.removeElement(selectedValue);
					listener.texturesChanged();
				}
			}
		});
		add(btnRemove);

		final JButton btnEditTexture = new JButton("Edit Path");
		btnEditTexture.setBounds(415, 535, 88, 23);
		add(btnEditTexture);
		btnEditTexture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					selectedValue.setPath(pathField.getText());
					list.repaint();
					loadBitmap(modelView, selectedValue);
					listener.texturesChanged();
				}
			}
		});

		pathField = new JTextField();
		pathField.setBounds(513, 535, 249, 20);
		add(pathField);
		pathField.setColumns(10);

		final JButton btnAdd = new JButton("Add Path");
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String path = JOptionPane.showInputDialog(EditTexturesPopupPanel.this, "Enter texture path:",
						"Add Texture", JOptionPane.PLAIN_MESSAGE);
				if (path != null) {
					final Bitmap newBitmap = new Bitmap(path);
					modelView.getModel().add(newBitmap);
					bitmapListModel.addElement(newBitmap);
					listener.texturesChanged();
				}
			}
		});
		btnAdd.setBounds(415, 569, 89, 23);
		add(btnAdd);
		pathField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final Bitmap selectedValue = list.getSelectedValue();
				if (selectedValue != null) {
					selectedValue.setPath(pathField.getText());
					list.repaint();
					loadBitmap(modelView, selectedValue);
					listener.texturesChanged();
				}
			}
		});

	}

	private void loadBitmap(final ModelView modelView, final Bitmap defaultTexture) {
		if (defaultTexture != null) {
			final DataSource workingDirectory = modelView.getModel().getWrappedDataSource();
			panel_1.removeAll();
			try {
				panel_1.add(new ZoomableImagePreviewPanel(BLPHandler.get().getTexture(workingDirectory, defaultTexture.getPath())));
			} catch (final Exception exc) {
				final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
				panel_1.add(new ZoomableImagePreviewPanel(image));
			}
			panel_1.revalidate();
		}
	}
}
