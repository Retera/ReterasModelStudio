package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ExportTexture;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListRenderer;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec2;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

public class CrudeSelectionUVMask extends JPanel {
	Bitmap prototypeBitmap = new Bitmap("PrototypeBitmapForCombobox", 0);
	Geoset prototypeGeoset = new Geoset().setLevelOfDetailName("PrototypeGeosetForCombobox");
	Material prototypeMaterial = new Material(new Layer(prototypeBitmap));

	TwiComboBox<?>[] comboBoxes;
	SmartButtonGroup options;
	SmartButtonGroup filterGroup;

	IntEditorJSpinner withSpinner = new IntEditorJSpinner(1024, 2, 8192, 64, null);
	IntEditorJSpinner heightSpinner = new IntEditorJSpinner(1024, 2, 8192, 64, null);

	Color lineColor;
	ModelHandler modelHandler;

//	TwiComboBox<Bitmap>   bitmapTwiComboBox;
//	TwiComboBox<Material> materialTwiComboBox;
//	TwiComboBox<Geoset>   geosetTwiComboBox;

	public CrudeSelectionUVMask(ModelHandler modelHandler){
		super(new MigLayout("fill", "[grow]", ""));
		this.modelHandler = modelHandler;
		EditableModel model = modelHandler.getModel();

		add(getSourcePanel(model), "wrap");

		this.filterGroup = getFilterGroup().setSelectedIndex(3);
		add(filterGroup.getButtonPanel(), "growx, wrap");

		add(getSpinners(), "grow, split");

		JCheckBox linesCheckbox = new JCheckBox("Draw outlines");
		linesCheckbox.addActionListener(e -> lineColor = linesCheckbox.isSelected() ? Color.CYAN : null);
		add(linesCheckbox, "growx, wrap, right");
	}

	public void popup(JComponent parent){
		int opt = JOptionPane.showConfirmDialog(parent, this, "Export UV map as image", JOptionPane.OK_CANCEL_OPTION);
		if(opt == JOptionPane.OK_OPTION){
			createAndSaveImage(modelHandler);
		}
	}

	private void createAndSaveImage(ModelHandler modelHandler) {
		EditableModel model = modelHandler.getModel();
		String fileName = model.getName() + "_uvMask";
		int width = withSpinner.getIntValue();
		int height = heightSpinner.getIntValue();

		ModelView modelView = modelHandler.getModelView();
		int selectedIndex = filterGroup.getSelectedIndex();

		Set<Triangle> triangles = switch (options.getSelectedIndex()) {
			case 0 -> getTexTriangles(Collections.singleton((Bitmap) comboBoxes[0].getSelected()), modelView, selectedIndex);
			case 1 -> getMatTriangles(Collections.singleton((Material) comboBoxes[1].getSelected()), modelView, selectedIndex);
			case 2 -> getGeoTriangles(Collections.singleton((Geoset) comboBoxes[2].getSelected()), modelView, selectedIndex);
			case 3 -> getGeoTriangles(model.getGeosets(), modelView, selectedIndex);
			default -> Collections.emptySet();
		};
		if(!triangles.isEmpty()){
			BufferedImage maskImage = getBufferedImage2(triangles, width, height, lineColor);
			ExportTexture.onClickSaveAs(maskImage, fileName, FileDialog.SAVE_TEXTURE, new FileDialog(), ProgramGlobals.getMainPanel());
		}
	}

	private JPanel getSpinners() {
		JPanel spinners = new JPanel(new MigLayout("gap 0", "[][]"));
		spinners.add(new JLabel("Width "));
		spinners.add(withSpinner, "wrap");
		spinners.add(new JLabel("Height "));
		spinners.add(heightSpinner, "wrap");
		return spinners;
	}

	private JPanel getSourcePanel(EditableModel model) {
		JPanel sourcePanel = new JPanel(new MigLayout("fill", "[][grow]", "[]"));
		sourcePanel.setBorder(BorderFactory.createTitledBorder("Triangles From"));
		options = new SmartButtonGroup();
		comboBoxes = new TwiComboBox[] {
				new TwiComboBox<>(model.getTextures(),  prototypeBitmap).twiSetEnabled(false).twiSetRenderer(new TextureListRenderer(model)),
				new TwiComboBox<>(model.getMaterials(), prototypeMaterial).twiSetEnabled(false).twiSetRenderer(new MaterialListRenderer(model)),
				new TwiComboBox<>(model.getGeosets(),   prototypeGeoset).twiSetEnabled(false).twiSetRenderer(new GeosetListRenderer(model, 64))
		};

		Consumer<Integer> integerConsumer = integer -> {
			comboBoxes[0].setEnabled(integer == 0);
			comboBoxes[1].setEnabled(integer == 1);
			comboBoxes[2].setEnabled(integer == 2);
		};

		JRadioButton[] radioButtons = new JRadioButton[]{
				options.addJRadioButton("Texture", e -> integerConsumer.accept(options.getSelectedIndex())),
				options.addJRadioButton("Material", e -> integerConsumer.accept(options.getSelectedIndex())),
				options.addJRadioButton("Geoset", e -> integerConsumer.accept(options.getSelectedIndex())),
				options.addJRadioButton("All", e -> integerConsumer.accept(options.getSelectedIndex())),
		};

		options.setSelectedIndex(3);


		sourcePanel.add(radioButtons[0]);
		sourcePanel.add(comboBoxes[0], "growx, wrap");

		sourcePanel.add(radioButtons[1]);
		sourcePanel.add(comboBoxes[1], "growx, wrap");

		sourcePanel.add(radioButtons[2]);
		sourcePanel.add(comboBoxes[2], "growx, wrap");

		sourcePanel.add(radioButtons[3], "wrap");
		return sourcePanel;
	}

	private SmartButtonGroup getFilterGroup() {
		SmartButtonGroup filterGroup = new SmartButtonGroup("Use");
		filterGroup.addJRadioButton("Only selected", null);
		filterGroup.addJRadioButton("Editable", null);
		filterGroup.addJRadioButton("Visible", null);
		filterGroup.addJRadioButton("All", null);
		filterGroup.setButtonConst("");
		return filterGroup;
	}

	enum source_type {
		TEXTURE("Texture", true),
		MATERIAL("Material", true),
		GEOSET("Geoset", true),
		ALL("All", false);

		final String name;
		final boolean doComboBox;
		source_type(String name, boolean doComboBox) {
			this.name = name;
			this.doComboBox = doComboBox;
		}
	}
	public void withOptions2(ModelHandler modelHandler){
		// for textures
		// for materials
		// for geosets
		// only selected
		EditableModel model = modelHandler.getModel();

		JPanel sourcePanel = new JPanel(new MigLayout("fill", "[][grow]", "[]"));
		sourcePanel.setBorder(BorderFactory.createTitledBorder("Triangles From"));
		TwiComboBox<Bitmap>   bitmapTwiComboBox   = new TwiComboBox<>(modelHandler.getModel().getTextures(), prototypeBitmap).twiSetEnabled(false).twiSetRenderer(new TextureListRenderer(model));
		TwiComboBox<Material> materialTwiComboBox = new TwiComboBox<>(modelHandler.getModel().getMaterials(), prototypeMaterial).twiSetEnabled(false).twiSetRenderer(new MaterialListRenderer(model));
		TwiComboBox<Geoset>   geosetTwiComboBox   = new TwiComboBox<>(modelHandler.getModel().getGeosets(), prototypeGeoset).twiSetEnabled(false).twiSetRenderer(new GeosetListRenderer(model, 64));


		Map<source_type, TwiComboBox<?>> comboBoxMap = new HashMap<>();
		comboBoxMap.put(source_type.TEXTURE, bitmapTwiComboBox);
		comboBoxMap.put(source_type.MATERIAL, materialTwiComboBox);
		comboBoxMap.put(source_type.GEOSET, geosetTwiComboBox);
		comboBoxMap.put(source_type.ALL, new TwiComboBox<>(new String[] {"ugg"}, "ugg"));


		Consumer<source_type> enumConsumer = srcType -> {
			comboBoxMap.get(source_type.TEXTURE).setEnabled(srcType == source_type.TEXTURE);
			comboBoxMap.get(source_type.MATERIAL).setEnabled(srcType == source_type.MATERIAL);
			comboBoxMap.get(source_type.GEOSET).setEnabled(srcType == source_type.GEOSET);
			comboBoxMap.get(source_type.ALL).setEnabled(srcType == source_type.ALL);
		};

		SmartButtonGroup options = new SmartButtonGroup();
		for (source_type st : source_type.values()) {
			if(st.doComboBox){
				sourcePanel.add(options.addJRadioButton(st.name, e -> enumConsumer.accept(st)));
				sourcePanel.add(comboBoxMap.get(st), "growx, wrap");
			} else {
				sourcePanel.add(options.addJRadioButton(st.name, e -> enumConsumer.accept(st)), "wrap");
			}

		}
		options.setSelectedIndex(3);


		JPanel panel = new JPanel(new MigLayout("fill", "[grow]", ""));
		panel.add(sourcePanel, "wrap");


		SmartButtonGroup filterGroup = new SmartButtonGroup("Use");
		filterGroup.addJRadioButton("Only selected", null);
		filterGroup.addJRadioButton("Editable", null);
		filterGroup.addJRadioButton("Visible", null);
		filterGroup.addJRadioButton("All", null);
		filterGroup.setSelectedIndex(3);
		filterGroup.setButtonConst("");
		panel.add(filterGroup.getButtonPanel(), "growx, wrap");

		JPanel spinners = new JPanel(new MigLayout("gap 0", "[][]"));
		IntEditorJSpinner withSpinner = new IntEditorJSpinner(1024, 2, 8192, 64, null);
		IntEditorJSpinner heightSpinner = new IntEditorJSpinner(1024, 2, 8192, 64, null);
		spinners.add(new JLabel("Width "));
		spinners.add(withSpinner, "wrap");
		spinners.add(new JLabel("Height "));
		spinners.add(heightSpinner, "wrap");
		panel.add(spinners, "grow, split");

		JCheckBox linesCheckbox = new JCheckBox("Draw outlines");
		panel.add(linesCheckbox, "growx, wrap, right");


		int opt = JOptionPane.showConfirmDialog(null, panel, "Export UV map as image", JOptionPane.OK_CANCEL_OPTION);
		if(opt == JOptionPane.OK_OPTION){
			String fileName = model.getName() + "_uvMask";
			Color lineColor = linesCheckbox.isSelected() ? Color.CYAN : null;
			int width = withSpinner.getIntValue();
			int height = heightSpinner.getIntValue();

			final ModelView modelView = modelHandler.getModelView();
			final int selectedIndex = filterGroup.getSelectedIndex();

			Set<Triangle> triangles = switch (source_type.values()[options.getSelectedIndex()]) {
				case TEXTURE -> getTexTriangles(Collections.singleton((Bitmap) comboBoxMap.get(source_type.TEXTURE).getSelected()), modelView, selectedIndex);
				case MATERIAL -> getMatTriangles(Collections.singleton((Material) comboBoxMap.get(source_type.MATERIAL).getSelected()), modelView, selectedIndex);
				case GEOSET -> getGeoTriangles(Collections.singleton((Geoset) comboBoxMap.get(source_type.GEOSET).getSelected()), modelView, selectedIndex);
				case ALL -> getGeoTriangles(model.getGeosets(), modelView, selectedIndex);
			};
			if(!triangles.isEmpty()){
				BufferedImage maskImage = getBufferedImage2(triangles, width, height, lineColor);
				ExportTexture.onClickSaveAs(maskImage, fileName, FileDialog.SAVE_TEXTURE, new FileDialog(), ProgramGlobals.getMainPanel());
			}
		}
	}


	private Set<Triangle> getTexTriangles(Set<Bitmap> textures, ModelView modelView, int filter) {
		Set<Material> materials = new HashSet<>();

		for (Material material : modelView.getModel().getMaterials()){
			for (Layer layer : material.getLayers()){
				if(layer.getTextureSlots().stream().anyMatch(ts -> usesTexture(ts, textures))) {
					materials.add(material);
					break;
				}
			}
		}
		return getMatTriangles(materials, modelView, filter);
	}

	private boolean usesTexture(Layer.Texture ts, Set<Bitmap> textures) {
		if (ts.getFlipbookTexture() != null) {
			Map<Sequence, TreeMap<Integer, Entry<Bitmap>>> animMap = ts.getFlipbookTexture().getAnimMap();
			for (TreeMap<Integer, Entry<Bitmap>> treeMap : animMap.values()){
				for (Entry<Bitmap> entry : treeMap.values()){
					if (textures.contains(entry.getValue())
							|| textures.contains(entry.getInTan())
							|| textures.contains(entry.getOutTan())){
						return true;
					}
				}
			}
		} else {
			return textures.contains(ts.getTexture());
		}
		return false;
	}

	private Set<Triangle> getMatTriangles(Set<Material> materials, ModelView modelView, int filter) {
		Set<Geoset> geosets = new HashSet<>();
		for (Geoset geoset : modelView.getModel().getGeosets()){
			if(materials.contains(geoset.getMaterial())){
				geosets.add(geoset);
			}
		}
		return getGeoTriangles(geosets, modelView, filter);
	}

	private Set<Triangle> getGeoTriangles(Collection<Geoset> geosets, ModelView modelView, int filter){
		Set<Triangle> triangles = new HashSet<>();
		for (Geoset geoset : geosets) {
			if (modelView.isEditable(geoset) ||
					filter == 2 && modelView.isVisible(geoset)
					|| filter == 3){
				for (Triangle triangle : geoset.getTriangles()) {
					if(filter == 0 && modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2))
							|| filter == 1 && modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2))
							|| filter == 2 && !(modelView.isHidden(triangle.get(0)) || modelView.isHidden(triangle.get(1)) || modelView.isHidden(triangle.get(2)))
							|| filter == 3){
						triangles.add(triangle);
					}
				}
			}
		}

		return triangles;
	}

	public BufferedImage getBufferedImage2(Set<Triangle> triangles, int width, int height, Color lineColor) {

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fill3DRect(0, 0, width, height, false);
		graphics.setColor(Color.WHITE);

		BufferedImage lineImage = null;
		Graphics lineGraphics = null;
		if(lineColor != null) {
			lineImage = getLineImage(width, height);
			lineGraphics = lineImage.getGraphics();
			lineGraphics.setColor(lineColor);
		}


		for (Triangle triangle : triangles) {
			int[][] uvPoints = getTriUVPoints2(triangle, 0, new float[] {width, height});
			graphics.fillPolygon(uvPoints[0], uvPoints[1], 4);
			if (lineGraphics != null) {
				lineGraphics.drawPolyline(uvPoints[0], uvPoints[1], 4);
			}
		}

		if (lineImage != null) {
			lineGraphics.dispose();
			graphics.drawImage(lineImage, 0, 0, null);
		}

		graphics.dispose();
		return image;
	}

	private BufferedImage getLineImage(int width, int height) {
		BufferedImage lineImage;
		lineImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		int[] pixels = new int[width * height];
		Arrays.fill(pixels, 0);
		lineImage.setRGB(0,0, lineImage.getWidth(), lineImage.getHeight(), pixels, 0, lineImage.getWidth());
		return lineImage;
	}

	private int[][] getTriUVPoints2(Triangle t, int uvLayer, float[] scale){
		int[][] output = new int[2][4];
		for (int i = 0; i < 3; i++) {
			output[0][i] = Math.round(t.getTVert(i, uvLayer).dot(Vec2.X_AXIS) * scale[0]);
			output[1][i] = Math.round(t.getTVert(i, uvLayer).dot(Vec2.Y_AXIS) * scale[1]);
		}
		output[0][3] = output[0][0];
		output[1][3] = output[1][0];
		return output;
	}

	public static void showPanel(JComponent parent, ModelHandler modelHandler) {
		CrudeSelectionUVMask selectionUVMask = new CrudeSelectionUVMask(modelHandler);
////		selectionUVMask.setSize(new Dimension(800, 650));
//		selectionUVMask.setPreferredSize(new Dimension(800, 650));
//		selectionUVMask.revalidate();
//		FramePopup.show(selectionUVMask, parent, "Export UV Mask");

		selectionUVMask.popup(parent);
	}
}
