package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.RemoveBitmapAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


public class EditTexturesPopupPanel extends JPanel {
	private final ModelHandler modelHandler;
	private final UndoManager undoManager;
	private final EditableModel model;
	private final JTextField pathField;
	private final JPanel imageViewerPanel;
	private final FileDialog fileDialog;
	private final IterableListModel<Bitmap> bitmapListModel = new IterableListModel<>();
	JList<Bitmap> bitmapJList = new JList<>(bitmapListModel);

	/**
	 * Create the panel.
	 */
	public EditTexturesPopupPanel(ModelHandler modelHandler) {
		fileDialog = new FileDialog(this);
		setLayout(new MigLayout("fill", "[16%:16%:97][16%:16%:97][16%:16%:97][5%:5%:30][fill, max(200)][16%:16%:97][grow]", "[fill, grow][shrink][shrink]"));
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.model = modelHandler.getModel();

		JPanel texturesPanel = new JPanel();
		texturesPanel.setBorder(new TitledBorder(null, "Textures", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		texturesPanel.setLayout(new BorderLayout(0, 0));
		add(texturesPanel, "cell 0 0 3 1, w 50%:300:300");

		JCheckBox chckbxDisplayPath = new JCheckBox("Display Path");

		chckbxDisplayPath.addActionListener(e -> bitmapJList.repaint());

		Bitmap defaultTexture = getBitmaps();

//		list.setModel(bitmapListModel);
		bitmapJList.setCellRenderer(getCellRenderer(chckbxDisplayPath));
		bitmapJList.addListSelectionListener(e -> ListListener(bitmapJList));
		texturesPanel.add(new JScrollPane(bitmapJList));

		texturesPanel.add(chckbxDisplayPath, BorderLayout.SOUTH);

		imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		add(imageViewerPanel, "cell 3 0 6 1, w 50%:95%:95%, grow");

		loadBitmap(defaultTexture);


		add(getButton("Add", e -> importTexturePopup()), "cell 0 1 1 1, growx");
		add(getButton("Add From Path", e -> btnAddTexture()), "cell 1 1 1 1, growx");
		add(getButton("Export", e -> exportTexture()), "cell 2 1 1 1, growx");
		add(getButton("Replace", e -> btnReplaceTexture()), "cell 0 2 1 1, growx");
		add(getButton("Remove", e -> btnRemoveTexture()), "cell 1 2 1 1, growx");

		pathField = new JTextField();
		pathField.setColumns(10);
		pathField.addActionListener(e -> btnEditTexture());
		add(pathField, "cell 4 1 2 1, grow");

		add(getButton("Apply Path", e -> btnEditTexture()), "cell 5 2 1 1, growx");

	}

	private JButton getButton(String s, ActionListener actionListener) {
		JButton btnAdd = new JButton(s);
		btnAdd.addActionListener(actionListener);
		return btnAdd;
	}

	private void ListListener(JList<Bitmap> list) {
		Bitmap selectedValue = list.getSelectedValue();
		if (selectedValue != null) {
			pathField.setText(selectedValue.getPath());
			loadBitmap(list.getSelectedValue());
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

	private Bitmap getBitmaps() {
		Bitmap defaultTexture = null;
		for (Bitmap bitmap : model.getTextures()) {
			if ((bitmap.getPath() != null) && (bitmap.getPath().length() > 0)) {
				bitmapListModel.addElement(bitmap);
				if (defaultTexture == null) {
					defaultTexture = bitmap;
				}
			}
		}
		return defaultTexture;
	}

	private void updateBitmapList() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		bitmapListModel.clear();
		for (Bitmap bitmap : model.getTextures()) {
			if ((bitmap.getPath() != null) && (bitmap.getPath().length() > 0)) {
				bitmapListModel.addElement(bitmap);
			}
		}
		bitmapJList.setSelectedValue(selectedValue, true);
	}

	private void importTexturePopup() {
		Bitmap newBitmap = fileDialog.importImage();
		if (newBitmap != null) {
			UndoAction action = new AddBitmapAction(newBitmap, model, ModelStructureChangeListener.changeListener);
			undoManager.pushAction(action.redo());
			updateBitmapList();
			bitmapJList.setSelectedIndex(bitmapListModel.size() - 1);
		}
	}

	private void exportTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null) {
			fileDialog.exportImage(selectedValue);
		}
	}

	private void btnAddTexture() {
		String path = JOptionPane.showInputDialog(EditTexturesPopupPanel.this, "Enter texture path:",
				"Add Texture", JOptionPane.PLAIN_MESSAGE);
		if (path != null) {
			Bitmap newBitmap = new Bitmap(path);
			UndoAction action = new AddBitmapAction(newBitmap, model, ModelStructureChangeListener.changeListener);
			undoManager.pushAction(action.redo());
			updateBitmapList();
			bitmapJList.setSelectedIndex(bitmapListModel.size() - 1);
		}
	}

	private void btnEditTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null) {

			SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(selectedValue, pathField.getText(), ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(setBitmapPathAction.redo());
			loadBitmap(selectedValue);
		}
	}

	private void btnRemoveTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		int selectedIndex = bitmapJList.getSelectedIndex()-1;
		if (selectedValue != null) {
			UndoAction action = new RemoveBitmapAction(selectedValue, model, ModelStructureChangeListener.changeListener);
			undoManager.pushAction(action.redo());
			updateBitmapList();
			bitmapJList.setSelectedIndex(selectedIndex);
		}
	}

	private void btnReplaceTexture() {
		final Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null) {
			Bitmap newBitmap = fileDialog.importImage();
			if (newBitmap != null) {
				selectedValue.setPath(newBitmap.getPath());
				pathField.setText(selectedValue.getPath());
				bitmapJList.repaint();
				loadBitmap(selectedValue);
				ModelStructureChangeListener.changeListener.texturesChanged();
			}
		}
	}

	private void loadBitmap(Bitmap defaultTexture) {
		if (defaultTexture != null) {
			final DataSource workingDirectory = model.getWrappedDataSource();
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
		EditTexturesPopupPanel textureManager = new EditTexturesPopupPanel(ProgramGlobals.getCurrentModelPanel().getModelHandler());
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
