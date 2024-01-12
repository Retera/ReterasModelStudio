package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.uv.UVRemapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.OpenImages;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.WindowHandler2;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.PerspectiveViewUgg;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.AnimatedPerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.UVPanelToolBar;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ManualUVTransformPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class UVPanel extends JPanel {
	private ModelHandler modelHandler;
	private final JTextField[] mouseCoordDisplay = new JTextField[2];
	private JPanel zoomPanel;
	private JPanel navPanel;
	private TwiComboBox<Integer> uvLayerChooser;
	private int uvLayer;

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

		uvLayerChooser = new TwiComboBox<>(1000);
		uvLayerChooser.add(0);
		uvLayerChooser.add(1);
		uvLayerChooser.add(2);
		uvLayerChooser.addOnSelectItemListener(this::setUvLayer);


		JPanel bottomPanel = new JPanel(new MigLayout("gap 0, hidemode 2", "[][]120[]16[]"));
		bottomPanel.add(mouseCoordDisplay[0], "aligny top");
		bottomPanel.add(mouseCoordDisplay[1], "aligny top");
		bottomPanel.add(navPanel);
		bottomPanel.add(zoomPanel);
		bottomPanel.add(uvLayerChooser);
		return bottomPanel;
	}

	private JPanel getStuffPanel() {
		JButton loadImage = Button.create("Load External Image", e -> loadExternalImage());

		JPanel unwrapPanel = new JPanel(new MigLayout("ins 0"));
		unwrapPanel.setBorder(BorderFactory.createTitledBorder("Project UVs"));

		TwiComboBox<UnwrapDirection> unwrapDirectionBox = new TwiComboBox<>(UnwrapDirection.values(), UnwrapDirection.PERSPECTIVE);
		unwrapDirectionBox.setMaximumSize(new Dimension(100, 35));
		unwrapDirectionBox.setMinimumSize(new Dimension(90, 15));

		JButton unwrapButton = Button.create("Remap UVs", e -> unwrapFromView(unwrapDirectionBox.getSelected()));
		Button.setTooltip(unwrapButton, "Remap selected UVs as a projection from the chosen view");

		unwrapPanel.add(unwrapDirectionBox);
		unwrapPanel.add(unwrapButton, "");

		textureComboBox = new TwiComboBox<>(new Bitmap("", 13333337));
		textureComboBox.addOnSelectItemListener(this::setBitmapAsBackground);

		// ToDo the texture combo box should maybe be limited in size and/or moved to a better spot to allow to view longer strings
		JPanel stuffPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		stuffPanel.add(loadImage, "spanx, wrap");
		stuffPanel.add(textureComboBox, "spanx, wrap");
		stuffPanel.add(unwrapPanel, "spanx, wrap");
		stuffPanel.add(new JLabel(" "), "wrap");
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.SELECT), "spanx, wrap");
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.ADD), "spanx, wrap");
		stuffPanel.add(toolbar.getSelectionModeGroup().getModeButton(SelectionMode.DESELECT), "spanx, wrap");
		stuffPanel.add(new JLabel(" "), "wrap");
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.TRANSLATION), "");
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.ROTATION), "");
		stuffPanel.add(toolbar.getActionTypeGroup().getModeButton(ModelEditorActionType3.SCALING), "wrap");

		stuffPanel.add(transformPanel, "spanx, wrap");
		return stuffPanel;
	}

	private void unwrapFromView(UnwrapDirection unwrapDirection) {
		if (unwrapDirection != null) {
			Mat4 cam;
			if (unwrapDirection == UnwrapDirection.PERSPECTIVE) {
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
			modelHandler.getUndoManager().pushAction(new UVRemapAction(selectedVertices, uvLayer, dim, cam, direction, false).redo());
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
		uvViewport.init();
		setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
	}


	public void setMouseCoordDisplay(double x, double y) {
		mouseCoordDisplay[0].setText(String.format(Locale.US, "%3.4f", x));
		mouseCoordDisplay[1].setText(String.format(Locale.US, "%3.4f", y));
	}

	AnimatedPerspectiveViewport viewport;
	TVertexEditorManager uvModelEditorManager;
	public UVPanel setModel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			uvModelEditorManager = modelPanel.getUvModelEditorManager();
			transformPanel.setModel(modelHandler, uvModelEditorManager);
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
			uvModelEditorManager = null;
			transformPanel.setModel(null, null);
			toolbar.setModelHandler(null);
			menuBar.setModel(null);
			uvViewport.setModel(null, null);

			textureComboBox.setNewLinkedModelOf(new ArrayList<>());
		}

		return this;
	}

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
		if (modelHandler != null && bitmap != null) {
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

	public int currentLayer() {
		return uvLayer;
	}

	public void setUvLayer(int layer) {
		if (uvModelEditorManager != null) {
			uvLayer = layer;
			uvModelEditorManager.setUVLayer(layer);
			uvViewport.setUvLayer(layer);
		}
	}

	public enum UnwrapDirection {
		FRONT("Front", 0,1,0,0,0,-1),
		RIGHT("Right", 1, 0, 0, 0, 0, -1),
		BOTTOM("Bottom", 0, 1, 0, -1, 0, 0),
		PERSPECTIVE("Perspective", 0,1,0,0,0,-1);

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

		public Mat4 getTransDim() {
			return transDim;
		}
	}

}
