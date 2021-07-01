package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class EditTexturesPopupPanel extends JPanel {
	private final JTextField pathField;
	private final JPanel imageViewerPanel;
	private final FileDialog fileDialog;

	/**
	 * Create the panel.
	 */
	public EditTexturesPopupPanel(final ModelView modelView) {
		fileDialog = new FileDialog(this);
		setLayout(new MigLayout("fill", "[16%:16%:97][16%:16%:97][16%:16%:97][5%:5%:30][fill, max(200)][16%:16%:97][grow]", "[fill, grow][shrink][shrink]"));

		final JPanel texturesPanel = new JPanel();
		texturesPanel.setBorder(new TitledBorder(null, "Textures", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		texturesPanel.setLayout(new BorderLayout(0, 0));
		add(texturesPanel, "cell 0 0 3 1, w 50%:300:300");

		final JCheckBox chckbxDisplayPath = new JCheckBox("Display Path");

		final DefaultListModel<Bitmap> bitmapListModel = new DefaultListModel<>();
		final JList<Bitmap> list = new JList<>();
		chckbxDisplayPath.addActionListener(e -> list.repaint());

		Bitmap defaultTexture = getBitmaps(modelView, bitmapListModel);

		list.setModel(bitmapListModel);
		list.setCellRenderer(getCellRenderer(chckbxDisplayPath));
		list.addListSelectionListener(e -> ListListener(modelView, list));
		texturesPanel.add(new JScrollPane(list));

		texturesPanel.add(chckbxDisplayPath, BorderLayout.SOUTH);

		imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		add(imageViewerPanel, "cell 3 0 6 1, w 50%:95%:95%, grow");

		loadBitmap(modelView, defaultTexture);

//		final JButton importButton = new JButton("Import");
		final JButton importButton = new JButton("Add");
		importButton.addActionListener(e -> importTexturePopup(modelView, bitmapListModel, list));
		add(importButton, "cell 0 1 1 1, growx");

		final JButton btnAdd = new JButton("Add From Path");
		btnAdd.addActionListener(e -> btnAddTexture(modelView, bitmapListModel));
		add(btnAdd, "cell 1 1 1 1, growx");

		final JButton exportButton = new JButton("Export");
		exportButton.addActionListener(e -> exportTexture(list));
		add(exportButton, "cell 2 1 1 1, growx");

//		final JButton btnReplaceTexture = new JButton("Replace Texture");
		final JButton btnReplaceTexture = new JButton("Replace");
		btnReplaceTexture.addActionListener(e -> btnReplaceTexture(modelView, list));
		add(btnReplaceTexture, "cell 0 2 1 1, growx");

		final JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(e -> btnRemoveTexture(modelView, list, bitmapListModel));
		add(btnRemove, "cell 1 2 1 1, growx");

		pathField = new JTextField();
//		pathField.setEditable(false);
		pathField.setColumns(10);
		pathField.addActionListener(e -> btnEditTexture(list, modelView));
		add(pathField, "cell 4 1 2 1, grow");

		final JButton btnEditTexture = new JButton("Apply Path");
		btnEditTexture.addActionListener(e -> btnEditTexture(list, modelView));
		add(btnEditTexture, "cell 5 2 1 1, growx");

	}

	private void ListListener(ModelView modelView, JList<Bitmap> list) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			pathField.setText(selectedValue.getPath());
			loadBitmap(modelView, list.getSelectedValue());
		}
	}

	private DefaultListCellRenderer getCellRenderer(JCheckBox chckbxDisplayPath) {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			                                              final boolean isSelected, final boolean cellHasFocus) {
				if (value instanceof Bitmap) {
					String path = ((Bitmap) value).getPath();
					if (!chckbxDisplayPath.isSelected()) {
//						String displayName = path.substring(path.lastIndexOf("\\") + 1);
						String[] bits = path.split("[\\\\/]");
						String displayName = bits[bits.length - 1];
						return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
					} else {
						return super.getListCellRendererComponent(list, path, index, isSelected, cellHasFocus);
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		};
	}

	private Bitmap getBitmaps(ModelView modelView, DefaultListModel<Bitmap> bitmapListModel) {
		Bitmap defaultTexture = null;
		for (final Bitmap bitmap : modelView.getModel().getTextures()) {
			if ((bitmap.getPath() != null) && (bitmap.getPath().length() > 0)) {
				bitmapListModel.addElement(bitmap);
				if (defaultTexture == null) {
					defaultTexture = bitmap;
				}
			}
		}
		return defaultTexture;
	}

	private void importTexturePopup(ModelView modelView, DefaultListModel<Bitmap> bitmapListModel, JList<Bitmap> list) {
		final Bitmap newBitmap = fileDialog.importImage();
		if (newBitmap != null) {
			modelView.getModel().add(newBitmap);
			bitmapListModel.addElement(newBitmap);
			list.setSelectedIndex(bitmapListModel.size() - 1);
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
	}

	private void exportTexture(JList<Bitmap> list) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			fileDialog.exportImage(selectedValue);
		}
	}

	private void btnAddTexture(ModelView modelView, DefaultListModel<Bitmap> bitmapListModel) {
		final String path = JOptionPane.showInputDialog(EditTexturesPopupPanel.this, "Enter texture path:",
				"Add Texture", JOptionPane.PLAIN_MESSAGE);
		if (path != null) {
			final Bitmap newBitmap = new Bitmap(path);
			modelView.getModel().add(newBitmap);
			bitmapListModel.addElement(newBitmap);
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
	}

	private void btnEditTexture(JList<Bitmap> list, ModelView modelView) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
//			final String path = (String) JOptionPane.showInputDialog(EditTexturesPopupPanel.this, "Enter new texture path:",
//					"Edit Path", JOptionPane.PLAIN_MESSAGE, null, null, pathField.getText());
//			System.out.println("path: " + path);
//			if (path != null) {
//				final Bitmap newBitmap = new Bitmap(path);
//				modelView.getModel().add(newBitmap);
//				bitmapListModel.addElement(newBitmap);
//				listener.texturesChanged();
//			}
//			pathField.setText(path);
//			selectedValue.setPath(path);
			selectedValue.setPath(pathField.getText());
			list.repaint();
			loadBitmap(modelView, selectedValue);
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
	}

	private void btnEditTexture2(JList<Bitmap> list, ModelView modelView) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			selectedValue.setPath(pathField.getText());
			list.repaint();
			loadBitmap(modelView, selectedValue);
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
	}

	private void btnRemoveTexture(ModelView modelView, JList<Bitmap> list, DefaultListModel<Bitmap> bitmapListModel) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			modelView.getModel().remove(selectedValue);
			bitmapListModel.removeElement(selectedValue);
			ModelStructureChangeListener.changeListener.texturesChanged();
		}
	}

	private void btnReplaceTexture(ModelView modelView, JList<Bitmap> list) {
		final Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			Bitmap newBitmap = fileDialog.importImage();
			if (newBitmap != null) {
				selectedValue.setPath(newBitmap.getPath());
				pathField.setText(selectedValue.getPath());
				list.repaint();
				loadBitmap(modelView, selectedValue);
				ModelStructureChangeListener.changeListener.texturesChanged();
			}
		}
	}

	private void loadBitmap(final ModelView modelView, final Bitmap defaultTexture) {
		if (defaultTexture != null) {
			final DataSource workingDirectory = modelView.getModel().getWrappedDataSource();
			imageViewerPanel.removeAll();
			try {
				imageViewerPanel.add(new ZoomableImagePreviewPanel(BLPHandler.get().getTexture(workingDirectory, defaultTexture.getPath())));
			} catch (final Exception exc) {
				final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
				imageViewerPanel.add(new ZoomableImagePreviewPanel(image));
			}
			imageViewerPanel.revalidate();
		}
	}

	public static void showPanel() {
		EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(ProgramGlobals.getCurrentModelPanel().getModelView());
		textureManager.setSize(new Dimension(800, 650));
		FramePopup.show(textureManager, ProgramGlobals.getMainPanel(), "Edit Textures");
//		final JFrame frame = new JFrame("Edit Textures");
//		frame.setContentPane(textureManager);
//		frame.setSize(textureManager.getSize());
//		frame.setLocationRelativeTo(null);
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		frame.setVisible(true);
	}
}
