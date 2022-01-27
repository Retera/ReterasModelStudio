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


public class EditTexturesPanel extends JPanel {
	private final ModelHandler modelHandler;
	private final UndoManager undoManager;
	private final EditableModel model;
	private final JTextField pathField = new JTextField(24);
	private final JPanel imageViewerPanel;
	private final FileDialog fileDialog;
	private final IterableListModel<Bitmap> bitmapListModel = new IterableListModel<>();
	private final JList<Bitmap> bitmapJList = new JList<>(bitmapListModel);
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	public EditTexturesPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill", "[][grow]", "[grow][]"));
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.model = modelHandler.getModel();
		fileDialog = new FileDialog(this);

		add(getTexturesListPanel(), "growy, growx 90");

		imageViewerPanel = getImageViewerPanel();
		add(imageViewerPanel, "growy, growx, wrap");

		updateBitmapList();

		add(getLowerButtonPanel(), "");
		add(getPathFieldPanel(), "");

		if(!bitmapListModel.isEmpty()){
			bitmapJList.setSelectedIndex(0);
		}
	}

	private JPanel getImageViewerPanel() {
		JPanel imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
//		add(imageViewerPanel, "w 50%:95%:95%, growy, wrap");
		return imageViewerPanel;
	}

	private JPanel getPathFieldPanel() {
		JPanel pathFieldPanel = new JPanel(new MigLayout("fill, ins 0", "[]"));
		pathField.addActionListener(e -> btnEditTexture());
		pathFieldPanel.add(pathField, "grow, wrap");

		pathFieldPanel.add(getButton("Apply Path", e -> btnEditTexture()), "right");
		return pathFieldPanel;
	}

	private JPanel getLowerButtonPanel() {
		JPanel lowerButtonPanel = new JPanel(new MigLayout("ins 0, gapx 0, wrap 3", "[sg 1]"));
		lowerButtonPanel.add(getButton("Add", e -> importTexturePopup()), "growx");
		lowerButtonPanel.add(getButton("Add From Path", e -> btnAddTexture()), "growx");
		lowerButtonPanel.add(getButton("Export", e -> exportTexture()), "growx");
		lowerButtonPanel.add(getButton("Replace", e -> btnReplaceTexture()), "growx");
		lowerButtonPanel.add(getButton("Remove", e -> btnRemoveTexture()), "growx");
		return lowerButtonPanel;
	}

	private JPanel getTexturesListPanel() {
		JPanel texturesListPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[grow][]"));
		texturesListPanel.setBorder(BorderFactory.createTitledBorder("Textures"));

		JCheckBox chckbxDisplayPath = new JCheckBox("Display Path");
		chckbxDisplayPath.addActionListener(e -> bitmapJList.repaint());

		bitmapJList.setCellRenderer(getCellRenderer(chckbxDisplayPath));
		bitmapJList.addListSelectionListener(e -> ListListener(bitmapJList));
		texturesListPanel.add(new JScrollPane(bitmapJList), "growx, growy, wrap");

		texturesListPanel.add(chckbxDisplayPath, "");
		return texturesListPanel;
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
					String path = ((Bitmap) value).getRenderableTexturePath();

					if (!chckbxDisplayPath.isSelected()) {
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

	private void updateBitmapList() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		bitmapListModel.clear();
		bitmapListModel.addAll(model.getTextures());
		bitmapJList.setSelectedValue(selectedValue, true);
	}

	private void importTexturePopup() {
		Bitmap newBitmap = fileDialog.importImage();
		if (newBitmap != null) {
			UndoAction action = new AddBitmapAction(newBitmap, model, changeListener);
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
		String path = JOptionPane.showInputDialog(
				EditTexturesPanel.this,
				"Enter texture path:",
				"Add Texture",
				JOptionPane.PLAIN_MESSAGE);
		if (path != null) {
			undoManager.pushAction(new AddBitmapAction(new Bitmap(path), model, changeListener).redo());
			updateBitmapList();
			bitmapJList.setSelectedIndex(bitmapListModel.size() - 1);
		}
	}

	private void btnEditTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null && !selectedValue.getPath().equals(pathField.getText())) {
			undoManager.pushAction(new SetBitmapPathAction(selectedValue, pathField.getText(), changeListener).redo());
			loadBitmap(selectedValue);
		}
	}

	private void btnRemoveTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		int selectedIndex = bitmapJList.getSelectedIndex()-1;
		if (selectedValue != null) {
			undoManager.pushAction(new RemoveBitmapAction(selectedValue, model, changeListener).redo());
			updateBitmapList();
			bitmapJList.setSelectedIndex(selectedIndex);
		}
	}

	private void btnReplaceTexture() {
		final Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null) {
			Bitmap newBitmap = fileDialog.importImage();
			if (newBitmap != null && !newBitmap.getPath().equals(selectedValue.getPath())) {
				undoManager.pushAction(new SetBitmapPathAction(selectedValue, newBitmap.getPath(), changeListener).redo());
				loadBitmap(selectedValue);
			}
		}
	}

	private void loadBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			final DataSource workingDirectory = model.getWrappedDataSource();
			imageViewerPanel.removeAll();
			ZoomableImagePreviewPanel comp = getZoomableImagePreviewPanel(bitmap, workingDirectory);
			imageViewerPanel.add(comp);
			imageViewerPanel.revalidate();
		}
		if(bitmapListModel.size() != model.getTextures().size()){
			updateBitmapList();
		}
	}

	private ZoomableImagePreviewPanel getZoomableImagePreviewPanel(Bitmap bitmap, DataSource workingDirectory) {
		try {
			BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
			return new ZoomableImagePreviewPanel(texture);
		} catch (final Exception exc) {
			final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.RED);
			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return new ZoomableImagePreviewPanel(image);
		}
	}

	public static void showPanel() {
		EditTexturesPanel textureManager = new EditTexturesPanel(ProgramGlobals.getCurrentModelPanel().getModelHandler());
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
