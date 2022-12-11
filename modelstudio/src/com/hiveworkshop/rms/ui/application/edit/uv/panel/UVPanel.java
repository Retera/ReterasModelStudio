package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.uv.MirrorTVerticesAction;
import com.hiveworkshop.rms.editor.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.OpenImages;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.AnimatedPerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.UVPanelToolBar;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ManualUVTransformPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec2;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class UVPanel extends JPanel {
	private ModelHandler modelHandler;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private JPanel zoomPanel;
	private JPanel navPanel;

	private TwiComboBox<Bitmap> textureComboBox;
	private final UVPanelToolBar toolbar;

	private final UVViewport uvViewport;

	private final UvPanelMenuBar menuBar;
	private final ManualUVTransformPanel transformPanel;

	public UVPanel() {

		setLayout(new MigLayout("fill, ins 0", "[grow][]", "[][grow][]"));
		toolbar = new UVPanelToolBar();
		transformPanel = new ManualUVTransformPanel();

		setOpaque(true);
		uvViewport = new UVViewport(this::setMouseCoordDisplay);
		add(toolbar, "wrap, spanx");
		add(uvViewport, "growx, growy");
		add(getStuffPanel(), "growy, wrap");
		add(getBottomPanel());
		this.menuBar = new UvPanelMenuBar(uvViewport);
	}

	private JPanel getBottomPanel() {
		zoomPanel = new JPanel(new MigLayout("gap 0", "[]16[]"));
		zoomPanel.add(getButton(20, 20, "Plus.png", e -> zoom(1.15)));
		zoomPanel.add(getButton(20, 20, "Minus.png", e -> zoom(-1.15)));

		navPanel = new JPanel(new MigLayout("gap 0"));
		navPanel.add(getButton(32, 16, "ArrowUp.png",    e ->    moveUpDown( 20)), "cell 1 0");
//		navPanel.add(getButton(32, 16, "ArrowDown.png",  e ->    moveUpDown(-20)), "cell 0 1");
		navPanel.add(getButton(16, 32, "ArrowLeft.png",  e -> moveLeftRight( 20)), "cell 0 1");
		navPanel.add(getButton(16, 32, "ArrowRight.png", e -> moveLeftRight(-20)), "cell 2 1");
		navPanel.add(getButton(32, 16, "ArrowDown.png",  e ->    moveUpDown(-20)), "cell 1 2");


		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}

		JPanel bottomPanel = new JPanel(new MigLayout("gap 0, hidemode 2", "[][]120[]16[]"));
		bottomPanel.add(mouseCoordDisplay[0], "aligny top");
		bottomPanel.add(mouseCoordDisplay[1], "aligny top");
		bottomPanel.add(navPanel);
		bottomPanel.add(zoomPanel);
		return bottomPanel;
	}

	private JPanel getStuffPanel() {

		JLabel[] divider = new JLabel[4];
		for (int i = 0; i < divider.length; i++) {
			divider[i] = new JLabel("----------");
		}

		ModeButton loadImage = new ModeButton("Load Image");
		loadImage.addActionListener(e -> loadExternalImage());

		TwiComboBox<UnwrapDirection> unwrapDirectionBox = new TwiComboBox<>(UnwrapDirection.values(), UnwrapDirection.PERSPECTIVE);

		ModeButton unwrapButton = new ModeButton("Remap UVs");
		unwrapButton.addActionListener(e -> unwrapFromView((UnwrapDirection) unwrapDirectionBox.getSelectedItem()));

		unwrapDirectionBox.setMaximumSize(new Dimension(100, 35));
		unwrapDirectionBox.setMinimumSize(new Dimension(90, 15));

		textureComboBox = new TwiComboBox<>(new Bitmap("", 13333337));
		textureComboBox.addOnSelectItemListener(this::setBitmapAsBackground);

		// ToDo the texture combo box should maybe be limited in size and/or moved to a better spot to allow to view longer strings
		JPanel stuffPanel = new JPanel(new MigLayout("wrap 1, gap 0, ins 0"));
		stuffPanel.add(loadImage);
		stuffPanel.add(textureComboBox);
		stuffPanel.add(divider[0]);
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.SELECT));
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.ADD));
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.DESELECT));
		stuffPanel.add(divider[1]);
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.TRANSLATION));
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.ROTATION));
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.SCALING));
		stuffPanel.add(divider[2]);
		stuffPanel.add(unwrapDirectionBox);
		stuffPanel.add(unwrapButton);

		stuffPanel.add(transformPanel);
		return stuffPanel;
	}

	private void mirror(Vec2 axis, Vec2 center) {
		if (modelHandler != null) {
			if(center == null) {
				center = toolbar.getModelEditorManager().getSelectionView().getUVCenter(0);
			}
			Collection<Vec2> tVertices = getTVertices(modelHandler.getModelView().getSelectedVertices(), 0);
			modelHandler.getUndoManager().pushAction(new MirrorTVerticesAction(tVertices, center, axis, ModelStructureChangeListener.changeListener).redo());
		}
		repaint();
	}

	public static Collection<Vec2> getTVertices(Collection<GeosetVertex> vertexSelection, int uvLayerIndex) {
		List<Vec2> tVertices = new ArrayList<>();
		for (GeosetVertex vertex : vertexSelection) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				tVertices.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		return tVertices;
	}

	private void unwrapFromView(UnwrapDirection unwrapDirection) {
		if (unwrapDirection != null) {
			Mat4 cam;
			if(unwrapDirection == UnwrapDirection.PERSPECTIVE) {
				cam = viewport.getCameraHandler().getViewProjectionMatrix();
			} else {
				cam = new Mat4().setIdentity();
			}
			remap(unwrapDirection.getTransDim(), cam, unwrapDirection.toString());
		} else {
			JOptionPane.showMessageDialog(UVPanel.this,
					"Please select a direction", "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	protected void remap(Mat4 dim, Mat4 cam, String direction) {
		if (modelHandler != null) {
			Set<GeosetVertex> selectedVertices = modelHandler.getModelView().getSelectedVertices();
			modelHandler.getUndoManager().pushAction(new UVRemapAction(selectedVertices, 0, dim, cam, direction, false).redo());
		}
		repaint();
	}

	private JButton getButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(actionListener);
		return button;
	}

	public JMenuBar getMenuBar() {
//		JMenuBar menuBar = new JMenuBar();
//		menuBar.add(getEditMenu());
//		menuBar.add(getDisplayMenu());
		return menuBar;
	}

	public void setControlsVisible(boolean flag) {
		navPanel.setVisible(flag);
		zoomPanel.setVisible(flag);
	}

	public void initViewport() {
		uvViewport.setAspectRatio(1);
		uvViewport.revalidate();
	}

	public void init() {
//		System.out.println("UVPanel: initiating");
		uvViewport.init();
//		System.out.println("Zoom: " + uvViewport.getCoordinateSystem().getZoom());
//		uvViewport.getCoordinateSystem().zoomOut(1.5);

//		uvViewport.getCoordinateSystem().doZoom(.5, .5, false);
//		uvViewport.getCoordinateSystem().doZoom(.5, .5, false);

//		System.out.println("UVPanel: vp initiated, setting controls visibility");

		setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
//		System.out.println("UVPanel: controls visibility set");
	}


	public void setMouseCoordDisplay(double x, double y) {
		mouseCoordDisplay[0].setText(String.format(Locale.US, "%3.4f", x));
		mouseCoordDisplay[1].setText(String.format(Locale.US, "%3.4f", y));
	}

	AnimatedPerspectiveViewport viewport;
	public UVPanel setModel(ModelPanel modelPanel) {
		if (modelPanel != null){
			this.modelHandler = modelPanel.getModelHandler();
			transformPanel.setModel(modelHandler, modelPanel.getUvModelEditorManager());
			toolbar.setModelHandler(modelPanel);
			menuBar.setModel(modelPanel);
			uvViewport.setModel(this.modelHandler, modelPanel.getUVViewportActivityManager());

			textureComboBox.setNewLinkedModelOf(modelHandler.getModel().getTextures());

			textureComboBox.setRenderer(new TextureListRenderer(modelHandler.getModel()));
			if (0 < textureComboBox.getItemCount()) {
				textureComboBox.setSelectedItem(null);
				textureComboBox.setSelectedIndex(0);
			}

			PerspectiveViewUgg modelDependentView = (PerspectiveViewUgg) WindowHandler2.getAllViews().stream().filter(v -> v instanceof PerspectiveViewUgg).findFirst().orElse(null);
			if (modelDependentView != null && modelDependentView.getPerspectiveViewport() != null) {
				viewport = modelDependentView.getPerspectiveViewport();

			}
		} else {
			modelHandler = null;
			transformPanel.setModel(null, null);
			toolbar.setModelHandler(null);
			menuBar.setModel(null);
			uvViewport.setModel(null, null);

			textureComboBox.removeAllItems();
		}

		return this;
	}
//	public UVPanel setModel(ModelHandler modelHandler) {
//		this.modelHandler = modelHandler;
//		toolbar.setModelHandler(modelHandler);
//		menuBar.setModel(modelHandler, toolbar.getModelEditorManager());
//		uvViewport.setModel(this.modelHandler, toolbar.getViewportActivityManager());
//
//		textureComboBox.removeAllItems();
//		textureComboBox.addAll(modelHandler.getModel().getTextures());
//
//		textureComboBox.setRenderer(new TextureListRenderer(modelHandler.getModel()));
//		if (textureComboBox.getItemCount() > 0) {
//			textureComboBox.setSelectedItem(null);
//			textureComboBox.setSelectedIndex(0);
//		}
//
//		PerspectiveViewUgg modelDependentView = (PerspectiveViewUgg) WindowHandler2.getAllViews().stream().filter(v -> v instanceof PerspectiveViewUgg).findFirst().orElse(null);
//		if (modelDependentView != null && modelDependentView.getPerspectiveViewport() != null) {
//			viewport = modelDependentView.getPerspectiveViewport();
//
//		}
//
//		return this;
//	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	private void moveUpDown(int i) {
		uvViewport.getCoordinateSystem().translateZoomed(0, i);
		uvViewport.repaint();
	}

	private void moveLeftRight(int i) {
		uvViewport.getCoordinateSystem().translateZoomed(i, 0);
		uvViewport.repaint();
	}

	private void zoom(double v) {
		if (v > 0) {
			uvViewport.getCoordinateSystem().zoomIn(v);
		} else {
			uvViewport.getCoordinateSystem().zoomOut(-v);

		}
		uvViewport.repaint();
	}

	private void loadExternalImage() {
		Bitmap bitmap = OpenImages.importImage(modelHandler.getModel(), this);
		setBitmapAsBackground(bitmap);
	}

	private void setBitmapAsBackground(Bitmap bitmap) {
		BufferedImage image = null;
		if (bitmap != null) {
			image = BLPHandler.getImage(bitmap, modelHandler.getModel().getWrappedDataSource());
		}
		setTextureAsBackground(image);
	}

	private void setTextureAsBackground(BufferedImage image) {
		uvViewport.clearBackgroundImage();
		if (image != null) {
			uvViewport.addBackgroundImage(image);
		}
		uvViewport.repaint();
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(uvViewport.getBufferedImage());
	}

	public BufferedImage getBufferedImage() {
		return uvViewport.getBufferedImage();
	}

	/**
	 * A method defining the currently selected UV layer.
	 */
	public int currentLayer() {
		int uvLayerIndex = 0;
		return uvLayerIndex;
	}


	public enum UnwrapDirection {
		FRONT("Front", 0,1,0,0,0,-1),
		RIGHT("Right", 1, 0, 0, 0, 0, -1),
		BOTTOM("Bottom", 0, 1, 0, -1, 0, 0),
		PERSPECTIVE("Perspective", 0,1,0,0,0,-1);
//				case FRONT -> remap((byte) Y, (byte) -Z, unwrapDirection);
//				case RIGHT -> remap((byte) X, (byte) -Z, unwrapDirection);
//				case BOTTOM -> remap((byte) Y, (byte) X, unwrapDirection);

//				case BACK -> remap((byte) -Y, (byte) -Z, unwrapDirection);
//				case LEFT -> remap((byte) -X, (byte) -Z, unwrapDirection);
//				case TOP -> remap((byte) Y, (byte) -X, unwrapDirection);

//				case BOTTOM -> remap((byte) 1, (byte) 0, unwrapDirection);
//				case FRONT -> remap((byte) 1, (byte) 2, unwrapDirection);
//				case RIGHT -> remap((byte) 0, (byte) 2, unwrapDirection);
//			case 0 -> centerX;
//			case 1 -> centerY;
//			case 2 -> centerZ;

		private final String displayText;
		private final Mat4 transDim;

		UnwrapDirection(String displayText,
		                float xDimX, float xDimY, float xDimZ,
		                float yDimX, float yDimY, float yDimZ) {
			this.displayText = displayText;
			transDim = new Mat4(
					xDimX, yDimX, 0, 0,
					xDimY, yDimY, 0, 0,
					xDimZ, yDimZ, 0, 0,
					0, 0, 0, 0);
		}

		@Override
		public String toString() {
			return displayText;
		}

		public Mat4 getTransDim(){
			return transDim;
		}
	}

}
