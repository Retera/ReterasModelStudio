package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.RemoveBitmapAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.OpenImages;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.OverviewPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelTextureThings;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;


public class EditTexturesPanel extends OverviewPanel {
	private final UndoManager undoManager;
	private final EditableModel model;
	private final JTextField pathField = new JTextField(24);
	private final JPanel imageViewerPanel;
	private final FileDialog fileDialog;
	private final TwiList<Bitmap> bitmapJList;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	private ZoomableImagePreviewPanel comp;

	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;
	private Bitmap selectedImage;

	public EditTexturesPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill", "[][grow]", "[grow][]"));
		this.undoManager = modelHandler.getUndoManager();
		this.model = modelHandler.getModel();
		fileDialog = new FileDialog(this);
		bitmapJList = new TwiList<>(modelHandler.getModel().getTextures());

		add(getTexturesListPanel(), "growy, growx 90");

		imageViewerPanel = getImageViewerPanel();
		add(imageViewerPanel, "growy, growx, wrap");

//		updateBitmapList();

		add(getLowerButtonPanel(), "");
		add(getPathFieldPanel(), "split");

		TwiComboBox<ImageUtils.ColorMode> colorModeBox = getColorModeBox();
		add(colorModeBox);

		if(!bitmapJList.isEmpty()){
			System.out.println("select 0");
			bitmapJList.setSelectedIndex(0);
			selectedImage = bitmapJList.getSelectedValue();
			loadBitmap(selectedImage);
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
		loadBitmap(selectedImage);
	}


	private JPanel getImageViewerPanel() {
		JPanel imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
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
		texturesListPanel.setPreferredSize(this.getSize());

		TextureListRenderer textureListRenderer = ModelTextureThings.getTextureListRenderer();
		textureListRenderer.setTextSize(12);
		textureListRenderer.setImageSize(16);
		JCheckBox displayPath = new JCheckBox("Display Path");
		displayPath.addActionListener(e -> {
			textureListRenderer.setShowPath(displayPath.isSelected());
			bitmapJList.repaint();
		});

		bitmapJList.setCellRenderer(textureListRenderer);
//		bitmapJList.setCellRenderer(getCellRenderer(displayPath::isSelected));
		bitmapJList.addSelectionListener1(this::onListSelection);
		texturesListPanel.add(new JScrollPane(bitmapJList), "growx, growy, wrap");

		texturesListPanel.add(displayPath, "");
		return texturesListPanel;
	}

	private JButton getButton(String s, ActionListener actionListener) {
		JButton btnAdd = new JButton(s);
		btnAdd.addActionListener(actionListener);
		return btnAdd;
	}

	private void onListSelection(Bitmap bitmap) {
		selectedImage = bitmap;
		System.out.println("onListSelection");
		if (bitmap != null) {
			pathField.setText(bitmap.getPath());
			loadBitmap(bitmap);
		}
	}

	private DefaultListCellRenderer getCellRenderer(Supplier<Boolean> displayPaths) {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			                                              final boolean isSelected, final boolean cellHasFocus) {
				if (value instanceof Bitmap) {
					String path = ((Bitmap) value).getRenderableTexturePath();

					if (displayPaths.get()) {
						return super.getListCellRendererComponent(list, path, index, isSelected, cellHasFocus);
					} else {
						String[] bits = path.split("[\\\\/]");
						String displayName = bits[bits.length - 1];
						return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		};
	}


	private void importTexturePopup() {
		Bitmap newBitmap = OpenImages.importImage(model, this);
		if (newBitmap != null) {
			UndoAction action = new AddBitmapAction(newBitmap, model, changeListener);
			undoManager.pushAction(action.redo());
			bitmapJList.setSelectedIndex(bitmapJList.listSize() - 1);
//			SwingUtilities.invokeLater(() -> bitmapJList.setSelectedIndex(bitmapJList.listSize() - 1));
		}
	}

	private void exportTexture() {
		Bitmap selectedValue = bitmapJList.getSelectedValue();
		if (selectedValue != null) {
			ExportInternal.exportInternalFile3(selectedValue.getRenderableTexturePath(), "Texture", this);
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
			bitmapJList.setSelectedIndex(bitmapJList.listSize() - 1);
		}
	}

	private void btnEditTexture() {
		if (selectedImage != null && !selectedImage.getPath().equals(pathField.getText())) {
			undoManager.pushAction(new SetBitmapPathAction(selectedImage, pathField.getText(), changeListener).redo());
			loadBitmap(selectedImage);
		}
	}

	private void btnRemoveTexture() {
		int selectedIndex = bitmapJList.getSelectedIndex()-1;
		if (selectedImage != null) {
			undoManager.pushAction(new RemoveBitmapAction(selectedImage, model, changeListener).redo());
//			updateBitmapList();
			bitmapJList.setSelectedIndex(Math.max(0, selectedIndex));
		}
	}

	private void btnReplaceTexture() {
		if (selectedImage != null) {
			Bitmap newBitmap = OpenImages.importImage(model, this);
			if (newBitmap != null && !newBitmap.getPath().equals(selectedImage.getPath())) {
				undoManager.pushAction(new SetBitmapPathAction(selectedImage, newBitmap.getPath(), changeListener).redo());
				loadBitmap(selectedImage);
			}
		}
	}

	private void loadBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			final DataSource workingDirectory = model.getWrappedDataSource();
			comp.setImage(getImage(bitmap, workingDirectory));
			comp.resetZoom();
			imageViewerPanel.revalidate();
			imageViewerPanel.repaint();
		}
	}

	private BufferedImage getImage(Bitmap bitmap, DataSource workingDirectory){
		BufferedImage texture = BLPHandler.getImage(bitmap, workingDirectory);
		if(texture != null){
			if(colorMode == ImageUtils.ColorMode.RGBA){
				return texture;
			} else {
				return ImageUtils.getBufferedImageIsolateChannel(texture, colorMode);
			}
		} else {
			int imageSize = 128;
			final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.BLACK);
			int size = imageSize-6;
			GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, size);
			int dist1 = (imageSize - size)/2;
			int dist2 = imageSize-dist1;
			GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return image;
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

	@Override
	public void update() {

	}
}
