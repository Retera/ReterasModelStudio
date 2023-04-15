package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.model.bitmap.*;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.OpenImages;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.OverviewPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class EditTexturesPanel extends OverviewPanel {
	private final UndoManager undoManager;
	private final EditableModel model;
	private final TwiTextField pathField;
	private final IntEditorJSpinner replaceableIdSpinner = new IntEditorJSpinner(-1, -1, 1000, this::setReplaceableId);
	private final JCheckBox wrapWidthBox = new JCheckBox("\ud83e\udc58");
	private final JCheckBox wrapHeightBox = new JCheckBox("\ud83e\udc59");

	private final JPanel imageViewerPanel;
	private final TwiList<Bitmap> bitmapJList;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;

	private ZoomableImagePreviewPanel comp;

	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;
	private Bitmap selectedImage;
	private final JPopupMenu texturePopupMenu;

	public EditTexturesPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill", "[][grow]", "[grow][]"));
		this.undoManager = modelHandler.getUndoManager();
		this.model = modelHandler.getModel();
		bitmapJList = new TwiList<>(modelHandler.getModel().getTextures());
		bitmapJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pathField = new TwiTextField(24, this::editTexturePath);
		texturePopupMenu = getTexturePopupMenu();

		add(getTexturesListPanel(), "growy, growx 90");

		imageViewerPanel = getImageViewerPanel();
		add(imageViewerPanel, "growy, growx, wrap");

		if(!bitmapJList.isEmpty()){
			System.out.println("select 0");
			bitmapJList.setSelectedIndex(0);
			selectedImage = bitmapJList.getSelectedValue();
			loadBitmap(selectedImage);
		}
	}


	private JPanel getImageViewerPanel() {
		JPanel texturePanel = new JPanel(new MigLayout("fill", "[][]", "[][grow][]"));
		texturePanel.setBorder(new TitledBorder(null, "Texture", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		texturePanel.add(getPathFieldPanel(), "spanx, wrap");

		JPanel imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
		texturePanel.add(imageViewerPanel, "spanx, growx, growy, wrap");

		JPanel lowerButtonPanel = new JPanel(new MigLayout("ins 0, gapx 0, fill", "[sg 1][sg 1][sg 1][grow]"));
		lowerButtonPanel.add(Button.create("Export", e -> exportTexture()), "growx");
		lowerButtonPanel.add(Button.create("Replace", e -> replaceTexture()), "growx");
		lowerButtonPanel.add(Button.create("Remove", e -> removeTexture()), "growx");
		TwiComboBox<ImageUtils.ColorMode> colorModeBox = getColorModeBox();
		lowerButtonPanel.add(colorModeBox, "right");
		texturePanel.add(lowerButtonPanel, "spanx, growx");
		return texturePanel;
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

	private void showTexturePopup(ActionEvent event){
		if(event.getSource() instanceof Component){
			texturePopupMenu.show((Component) event.getSource(), 0,((Component) event.getSource()).getHeight());
		}

	}

	private JPopupMenu getTexturePopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem("Export")).addActionListener(e -> exportTexture());
		popupMenu.add(new JMenuItem("Replace")).addActionListener(e -> replaceTexture());
		popupMenu.add(new JMenuItem("Remove")).addActionListener(e -> removeTexture());
		popupMenu.add(new JMenuItem("Remove and Replace Uses")).addActionListener(e -> removeAndReplace());
		return popupMenu;
	}


	private JPanel getPathFieldPanel() {
		JPanel pathFieldPanel = new JPanel(new MigLayout("fill, ins 0", "[]"));
		pathFieldPanel.add(pathField, "grow");

		JButton button = new JButton("...");
		button.addActionListener(this::showTexturePopup);
		pathFieldPanel.add(button, "right");
		wrapWidthBox.addActionListener(e -> setWrap(Bitmap.WrapFlag.WIDTH, wrapWidthBox.isSelected()));
		wrapWidthBox.setHorizontalTextPosition(SwingConstants.LEADING);
		wrapWidthBox.setToolTipText("Wrap Width");
		pathFieldPanel.add(wrapWidthBox, "right");
		wrapHeightBox.addActionListener(e -> setWrap(Bitmap.WrapFlag.HEIGHT, wrapWidthBox.isSelected()));
		wrapHeightBox.setHorizontalTextPosition(SwingConstants.LEADING);
		wrapHeightBox.setToolTipText("Wrap Height");
		pathFieldPanel.add(wrapHeightBox, "right");
		JLabel id = new JLabel("\ud83c\udd94");
		id.setToolTipText("Replaceable ID");

		pathFieldPanel.add(id, "right");
		pathFieldPanel.add(replaceableIdSpinner, "right");

		JButton deleteButton = Button.create("X", e -> removeTexture(), Color.RED, Color.WHITE);
		deleteButton.setToolTipText("Remove Texture");
		pathFieldPanel.add(deleteButton, "right");


		return pathFieldPanel;
	}

	JPopupMenu popupMenu;
	private JPopupMenu getImageOptionsPopup() {
//		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem("Export")).addActionListener(e -> exportTexture());
		popupMenu.add(new JMenuItem("Replace")).addActionListener(e -> replaceTexture());
		popupMenu.add(new JMenuItem("Remove")).addActionListener(e -> removeTexture());
		popupMenu.add(new JMenuItem("Remove and Replace Uses")).addActionListener(e -> removeAndReplace());

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				System.out.println("popupMenuWillBecomeVisible: " + e);
				System.out.println("\tsource: " + e.getSource());
				if (e.getSource() instanceof PopupMenu) {
					System.out.println(((PopupMenu) e.getSource()).paramString());
				}
				if (e.getSource() instanceof JPopupMenu) {
					JPopupMenu source = (JPopupMenu) e.getSource();
//					System.out.println("popup location: " + source.getPopupLocation(e));
					System.out.println("popup location: " + source.getLocation());
					System.out.println("popup invoker: " + source.getInvoker());
					System.out.println("popup prefSize: " + source.getPreferredSize());
					System.out.println("X: " + source.getX() + " Y: " + source.getY() + " ");
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				System.out.println("popupMenuWillBecomeInvisible: " + e);

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				System.out.println("popupMenuCanceled: " + e);

			}
		});
		return popupMenu;
	}

	private JPanel getTexturesListPanel() {
		JPanel texturesListPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]", "[][grow][]"));
		texturesListPanel.setBorder(BorderFactory.createTitledBorder("Textures"));
		texturesListPanel.setPreferredSize(this.getSize());

		TextureListRenderer textureListRenderer = new TextureListRenderer(model);
		textureListRenderer.setTextSize(12);
		textureListRenderer.setImageSize(16);
		JCheckBox displayPath = new JCheckBox("Display Path");
		displayPath.addActionListener(e -> {
			textureListRenderer.setShowPath(displayPath.isSelected());
			bitmapJList.repaint();
		});

		bitmapJList.setCellRenderer(textureListRenderer);
		bitmapJList.setComponentPopupMenu(getImageOptionsPopup());

		bitmapJList.addSelectionListener1(this::onListSelection);
		bitmapJList.addMouseListener(getMA());

		texturesListPanel.add(displayPath, "wrap");
		texturesListPanel.add(new JScrollPane(bitmapJList), "growx, growy, wrap");

		JPanel lowerButtonPanel = new JPanel(new MigLayout("ins 0, gapx 0", "[sg 1]"));
		lowerButtonPanel.add(Button.create("Add From File", e -> addTexture()), "growx");
		lowerButtonPanel.add(Button.create("Add From Path", e -> addTexture(getPath())), "growx");
		texturesListPanel.add(lowerButtonPanel, "growx");

		return texturesListPanel;
	}

	private void onListSelection(Bitmap bitmap) {
		selectedImage = bitmap;
		System.out.println("onListSelection");
		if (bitmap != null) {
			pathField.setText(bitmap.getPath());
			replaceableIdSpinner.reloadNewValue(bitmap.getReplaceableId());
			wrapHeightBox.setSelected(bitmap.isWrapHeight());
			wrapWidthBox.setSelected(bitmap.isWrapWidth());
			loadBitmap(bitmap);
		} else {
			replaceableIdSpinner.reloadNewValue(-1);
			wrapHeightBox.setSelected(false);
			wrapWidthBox.setSelected(false);
			pathField.setText("");
		}
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
				eventInfo(e);
			}

			private void eventInfo(KeyEvent e) {
				System.out.println("keyTyped: " + e);
				System.out.println("MenuShortcut mask: " + Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
				System.out.println("getModifiersEx: " + e.getModifiersEx());
				System.out.println("getExtendedKeyCode: " + e.getExtendedKeyCode());
				System.out.println("getKeyCode: " + e.getKeyCode());
				System.out.println("getID: " + e.getID());
				System.out.println("correct mask: " + (e.getModifiersEx() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			}

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				eventInfo(e);
//				System.out.println("keyPressed: " + e);
//				System.out.println("MenuShortcut mask: " + Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
//				System.out.println("correct mask: " + (e.getModifiersEx() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			}

			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				eventInfo(e);
//				System.out.println("keyReleased: " + e);
//				System.out.println("MenuShortcut mask: " + Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
//				System.out.println("correct mask: " + (e.getModifiersEx() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			}
		};
	}


	private MouseAdapter getMA(){
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
//				System.out.println("mouseClicked: " + e);
//				System.out.println("\tisPopupTrigger: " + e.isPopupTrigger());
//				System.out.println("\tsource: " + e.getSource());
//				System.out.println("popup visible: " + popupMenu.isVisible());
//				System.out.println("popup location: " + popupMenu.getLocation());
				int clickedIndex = bitmapJList.locationToIndex(e.getPoint());
//				System.out.println("clicked index: " + clickedIndex);
				if (!bitmapJList.isSelectedIndex(clickedIndex)){
					bitmapJList.setSelectedIndex(clickedIndex);
				}
				System.out.println();
//				if(e.getSource() instanceof TwiList){
//
//					TwiList<?> source = (TwiList<?>) e.getSource();
//					System.out.println("\tcompAt: " + source.getComponentAt(e.getX(), e.getY()));
//					int index = source.locationToIndex(e.getPoint());
//					System.out.println("\tcompAt: " + index);
//					System.out.println("\tbounds: " + source.getCellBounds(index, index));
//					System.out.println("\tboundsContains: " + source.getCellBounds(index, index).contains(e.getPoint()));
//					if(index < bitmapJList.getComponentCount()){
//
//					}
////					System.out.println("\tcompAt: " + ((TwiList<?>) e.getSource()).(e.getPoint()));
//				}
//				showPopup(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
			}


			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				super.mouseWheelMoved(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
			}
		};
	}

	private Bitmap getReplaceBitmap() {
		List<Bitmap> textures = new ArrayList<>(modelHandler.getModel().getTextures());
		textures.remove(selectedImage);
		TwiComboBox<Bitmap> textureChooser = new TwiComboBox<>(textures, new Bitmap("", 1));
		textureChooser.setRenderer(new TextureListRenderer(model));
		if (0 < textureChooser.getItemCount()) {
			textureChooser.setSelectedIndex(0);
		}
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Replace uses with:"), "wrap");
		panel.add(textureChooser);

		int option = JOptionPane.showConfirmDialog(this, panel, "Replace Texture Uses", JOptionPane.OK_CANCEL_OPTION);
		Bitmap replacement;
		if(option == JOptionPane.OK_OPTION){
			replacement = textureChooser.getSelected();
		} else {
			replacement = null;
		}
		return replacement;
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
	}

	@Override
	public void update() {
		onListSelection(selectedImage);
	}


	private void setReplaceableId(int newId){
		if (selectedImage != null && newId != selectedImage.getReplaceableId()) {
			undoManager.pushAction(new SetBitmapReplaceableIdAction(selectedImage, newId, changeListener).redo());
		}
	}

	private void setWrap(Bitmap.WrapFlag flag, boolean set){
		if (selectedImage != null && selectedImage.isFlagSet(flag) != set) {
			undoManager.pushAction(new SetBitmapWrapModeAction(selectedImage, flag, set, changeListener).redo());
		}
	}

	private void addTexture() {
		Bitmap newBitmap = OpenImages.importImage(model, this);
		if (newBitmap != null) {
			undoManager.pushAction(new AddBitmapAction(newBitmap, model, changeListener).redo());
			bitmapJList.setSelectedValue(newBitmap, true);
		}
	}

	private void exportTexture() {
		if (selectedImage != null) {
			ExportInternal.exportInternalFile3(selectedImage.getRenderableTexturePath(), "Texture", this);
		}
	}

	private String getPath() {
		return JOptionPane.showInputDialog(
				this,
				"Enter texture path:",
				"Add Texture",
				JOptionPane.PLAIN_MESSAGE);
	}

	private void editTexturePath(String text) {
		if (selectedImage != null) {
			if (!selectedImage.getPath().equals(text)) {
				undoManager.pushAction(new SetBitmapPathAction(selectedImage, text, changeListener).redo());
				loadBitmap(selectedImage);
			}
		}
	}

	private void addTexture(String path) {
		if (path != null) {
			undoManager.pushAction(new AddBitmapAction(new Bitmap(path), model, changeListener).redo());
			bitmapJList.setSelectedIndex(bitmapJList.listSize() - 1);
		}
	}

	private void replaceTexture() {
		if (selectedImage != null) {
			Bitmap newBitmap = OpenImages.importImage(model, this);
			if (newBitmap != null && !newBitmap.getPath().equals(selectedImage.getPath())) {
				undoManager.pushAction(new SetBitmapPathAction(selectedImage, newBitmap.getPath(), changeListener).redo());
				loadBitmap(selectedImage);
			}
		}
	}

	private void removeTexture() {
		int selectedIndex = bitmapJList.getSelectedIndex()-1;
		if (selectedImage != null) {
			Bitmap replacement = bitmapJList.getListModel().getElementAt(Math.max(0, selectedIndex));
			undoManager.pushAction(new RemoveBitmapAction(selectedImage, replacement, model, changeListener).redo());
			bitmapJList.setSelectedValue(replacement, true);
		}
	}

	private void removeAndReplace() {
		if (selectedImage != null) {
			Bitmap replacement = getReplaceBitmap();
			if (replacement != null && !replacement.getPath().equals(selectedImage.getPath())) {
				undoManager.pushAction(new RemoveBitmapAction(selectedImage, replacement, model, changeListener).redo());
				bitmapJList.setSelectedValue(replacement, true);
			}
		}
	}
}
