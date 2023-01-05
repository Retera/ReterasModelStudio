package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetFromOtherParticle2Action;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.TextureListRenderer;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserIconLabel;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Consumer;

public class ParticleEditPanel extends JPanel {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";
	private final static String SPINNER_CONSTRAINTS = "wrap, growx, spanx, align 50% 100%";
	private final ModelHandler tempModelHandler;
	private final PerspectiveViewport perspectiveViewport;
	private final ParticleEmitter2 particleEmitter2;
	private ParticleEmitter2 copy;

	public ParticleEditPanel(ParticleEmitter2 particleEmitter2) {
		super(new MigLayout("", "[]", ""));
		this.particleEmitter2 = particleEmitter2;

		copy = particleEmitter2.copy();
		tempModelHandler = getModelHandler(copy);
		perspectiveViewport = getViewport(tempModelHandler);

		TimeEnvironmentImpl renderEnv = tempModelHandler.getPreviewRenderModel().getTimeEnvironment();
		renderEnv.setRelativeAnimationTime(0);
		renderEnv.setLive(true);

		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "");

		add(getHeadTailGroup().getButtonPanel(), "");
		add(getFilterModeButtonGroup().getButtonPanel(), "");
//		add(getFlagPanel(), "wrap");
		add(getFlagPanel(), "");
		add(getTexturePanel(), "wrap");


		JPanel subPanel = new JPanel(new MigLayout("ins 0", "[]20[]"));

		subPanel.add(getSliderPanel(), "");

		JPanel spinnerPanel = getSpinnerPanel();

		subPanel.add(spinnerPanel, "wrap");
		add(subPanel, "spanx, wrap");

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> doApply());
		add(apply, "wrap");

		JButton addAsNew = new JButton("Add as new Emitter");
		addAsNew.addActionListener(e -> addAsNew());
		add(addAsNew, "wrap");
	}

	private JPanel getSpinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("ins 0"));


		spinnerPanel.add(getColorPanel(), SPINNER_CONSTRAINTS);

		JPanel alphaPanel = new Vec3SpinnerArray(copy.getAlpha(), 0, .1f)
				.setVec3Consumer((v) -> copy.setAlpha(v))
				.spinnerPanel("Alpha");
		spinnerPanel.add(alphaPanel, SPINNER_CONSTRAINTS);

		JPanel scalingPanel = new Vec3SpinnerArray(copy.getParticleScaling())
				.setVec3Consumer((v) -> copy.setParticleScaling(v))
				.spinnerPanel("Particle Scaling");
		spinnerPanel.add(scalingPanel, SPINNER_CONSTRAINTS);

		spinnerPanel.add(getHeadPanel(), SPINNER_CONSTRAINTS);
		spinnerPanel.add(getTailPanel(), SPINNER_CONSTRAINTS);
		return spinnerPanel;
	}

	private SmartButtonGroup getHeadTailGroup() {
		SmartButtonGroup headTail = new SmartButtonGroup("Head/Tail");
		headTail.addPanelConst("gap 0");
		headTail.addJRadioButton("Head", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.HEAD));
		headTail.addJRadioButton("Tail", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.TAIL));
		headTail.addJRadioButton("Both", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.BOTH));
		headTail.setSelectedIndex(copy.getHeadOrTail().ordinal());
		return headTail;
	}

	private JPanel getTailPanel() {
		JPanel tailPanel = new JPanel(new MigLayout("ins 0"));
		tailPanel.setBorder(BorderFactory.createTitledBorder("Tail UV (sub-texture index)"));
		tailPanel.add(new JLabel("Life Span"));
		tailPanel.add(new Vec3SpinnerArray(copy.getTailUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setTailUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		tailPanel.add(new JLabel("Decay"));
		tailPanel.add(new Vec3SpinnerArray(copy.getTailDecayUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setTailDecayUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		return tailPanel;
	}

	private JPanel getHeadPanel() {
		JPanel headPanel = new JPanel(new MigLayout("ins 0"));
		headPanel.setBorder(BorderFactory.createTitledBorder("Head UV (sub-texture index)"));
		headPanel.add(new JLabel("Life Span"));
		headPanel.add(new Vec3SpinnerArray(copy.getHeadUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setHeadUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		headPanel.add(new JLabel("Decay"));

		headPanel.add(new Vec3SpinnerArray(copy.getHeadDecayUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setHeadDecayUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		return headPanel;
	}

	private JPanel getSliderPanel() {
		JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));

		sliderPanel.add(new SmartNumberSlider("EmissionRate", copy.getEmissionRate(), 100, (i) -> copy.setEmissionRate(i)).addLabelTooltip("Particles emitted per second"), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("Speed", copy.getSpeed(), 500, (i) -> copy.setSpeed(i)), SLIDER_CONSTRAINTS + ", gaptop 5");
		sliderPanel.add(new SmartNumberSlider("Variation (%)", copy.getVariation() * 100, 500, (i) -> copy.setVariation(i / 100d)).addLabelTooltip("Variation in speed"), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("Latitude", copy.getLatitude(), 180, (i) -> copy.setLatitude(i)).addLabelTooltip("Angular spread"), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Gravity", copy.getGravity(), -100, 100, (i) -> copy.setGravity(i)), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("Width", copy.getWidth(), 400, (i) -> copy.setWidth(i)).addLabelTooltip("Width of the spawn plane"), SLIDER_CONSTRAINTS + ", gaptop 5");
		sliderPanel.add(new SmartNumberSlider("Length", copy.getLength(), 400, (i) -> copy.setLength(i)).addLabelTooltip("Length of the spawn plane"), SLIDER_CONSTRAINTS);

		sliderPanel.add(new SmartNumberSlider("LifeSpan (~ 0.1 s)", copy.getLifeSpan() * 10, 200, (i) -> copy.setLifeSpan(i / 10d)).setMinLowerLimit(0).setMaxUpperLimit(1000), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("TailLength", copy.getTailLength(), 100, (i) -> copy.setTailLength(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Time (%)", copy.getTime() * 100, 0, 100, (i) -> copy.setTime(i / 100d), false, false).addLabelTooltip("Fraction of lifespan at which to start decaying"), SLIDER_CONSTRAINTS);

		return sliderPanel;
	}

	private JPanel getFlagPanel() {
		JPanel flagPanel = new JPanel(new MigLayout("ins 0, gap 0"));
		flagPanel.setBorder(BorderFactory.createTitledBorder("Flags"));

		flagPanel.add(getCheckBox("Unshaded", copy.getUnshaded(), (b) -> copy.setUnshaded(b)), "wrap");
		flagPanel.add(getCheckBox("Unfogged", copy.getUnfogged(), (b) -> copy.setUnfogged(b)), "wrap");
		flagPanel.add(getCheckBox("LineEmitter", copy.getLineEmitter(), (b) -> copy.setLineEmitter(b)), "wrap");
		flagPanel.add(getCheckBox("SortPrimsFarZ", copy.getSortPrimsFarZ(), (b) -> copy.setSortPrimsFarZ(b)), "wrap");
		flagPanel.add(getCheckBox("ModelSpace", copy.getModelSpace(), (b) -> copy.setModelSpace(b)), "wrap");
		flagPanel.add(getCheckBox("XYQuad", copy.getXYQuad(), (b) -> copy.setXYQuad(b)), "wrap");
		flagPanel.add(getCheckBox("Squirt", copy.getSquirt(), (b) -> copy.setSquirt(b)), "wrap");
		return flagPanel;
	}

	private SmartButtonGroup getFilterModeButtonGroup() {
		SmartButtonGroup filterMode = new SmartButtonGroup("Filter Mode");
		filterMode.addPanelConst("gap 0");
		filterMode.addJRadioButton("Blend", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.BLEND));
		filterMode.addJRadioButton("Additive", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.ADDITIVE));
		filterMode.addJRadioButton("Modulate", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.MODULATE));
		filterMode.addJRadioButton("Modulate2x", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.MODULATE2X));
		filterMode.addJRadioButton("AlphaKey", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.ALPHAKEY));
		filterMode.setSelectedIndex(copy.getFilterMode().ordinal());
		return filterMode;
	}

	public JCheckBox getCheckBox(String flagName, boolean selected, Consumer<Boolean> checkboxConsumer) {
		JCheckBox checkBox = new JCheckBox(flagName);
		checkBox.setSelected(selected);
		checkBox.addActionListener(e -> checkboxConsumer.accept(checkBox.isSelected()));
		return checkBox;
	}

	private void addAsNew() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ParticleEmitter2 emitter2 = this.copy.copy();
			emitter2.setParent(null);
			UndoAction action = new AddNodeAction(modelPanel.getModel(), emitter2, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}

	private void doApply() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel().contains(particleEmitter2)) {
			UndoAction action1 = new SetFromOtherParticle2Action(particleEmitter2, copy);
			modelPanel.getModelHandler().getUndoManager().pushAction(action1.redo());
		}
	}

	private JPanel getTexturePanel(){
		JPanel panel = new JPanel(new MigLayout("fill"));
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		ModelHandler modelHandler;
		if (modelPanel != null) {
			modelHandler = modelPanel.getModelHandler();
		} else {
			modelHandler = tempModelHandler;
		}
		JLabel imageLabel = new JLabel();
		updateImageLabel(imageLabel);

		EditableModel model = modelHandler.getModel();
		TwiComboBox<Bitmap> textureChooser = new TwiComboBox<>(model.getTextures(), new Bitmap("", 1));
		textureChooser.setRenderer(new TextureListRenderer(model).setImageSize(16));
//		textureChooser.addOnSelectItemListener(copy::setTexture);
		textureChooser.addOnSelectItemListener(bitmap -> changeTexture(bitmap, imageLabel));
		panel.add(textureChooser, "spanx, growx, wrap");

		panel.add(imageLabel, "growx");

//		panel.add(new SmartNumberSlider("Rows", copy.getRows(), 1, 16, (i) -> copy.setRows(i)).setMinLowerLimit(1).setMaxUpperLimit(100), "");
//		panel.add(new SmartNumberSlider("Columns", copy.getColumns(), 1, 16, (i) -> copy.setColumns(i)).setMinLowerLimit(1).setMaxUpperLimit(100), "");
		panel.add(new JLabel("Rows"), "split");
		panel.add(new IntEditorJSpinner(copy.getRows(), 1, 1024, i -> updateImageParts(i, copy.getCols(), imageLabel)), ", wrap");
		panel.add(new JLabel("Columns"), "split");
		panel.add(new IntEditorJSpinner(copy.getColumns(), 1, 1024, i -> updateImageParts(copy.getRows(), i, imageLabel)));
		return panel;
	}

	private void changeTexture(Bitmap texture, JLabel imageLabel){
		copy.setTexture(texture);
		updateImageLabel(imageLabel);
	}

	private void updateImageParts(int rows, int cols, JLabel imageLabel){
		copy.setRows(rows);
		copy.setColumns(cols);
		updateImageLabel(imageLabel);
	}

	private void updateImageLabel(JLabel imageLabel) {
		BufferedImage bufferedImage = BLPHandler.getImage(copy.getTexture(), GameDataFileSystem.getDefault());
		int cols = copy.getCols();
		int rows = copy.getRows();
		int iconWidth = 128;
		int iconHeight = 128 * bufferedImage.getHeight()/bufferedImage.getWidth();

		Image scaledInstance = bufferedImage.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);

		int w = scaledInstance.getWidth(null);
		int h = scaledInstance.getHeight(null);
		BufferedImage iconImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[w*h];
		Arrays.fill(pixels, 0);
		iconImage.setRGB(0,0, w, h, pixels, 0, w);

		Graphics graphics = iconImage.getGraphics();
		graphics.drawImage(scaledInstance, 0,0,null);

		graphics.setColor(new Color(128,128,128,128));
		float colSpace = w/(float)cols;
		float rowSpace = h/(float)rows;
		for(float i = colSpace; i < w; i+= colSpace){
			graphics.drawLine((int) i, 0, (int) i, h);
		}
		for(float i = rowSpace; i < h; i+= rowSpace){
			graphics.drawLine(0, (int) i, w, (int) i);
		}
		graphics.dispose();

		imageLabel.setIcon(new ImageIcon(iconImage));
	}

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < rowColor.length; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}

//	private void changeColor() {
//		copy.setSegmentColor(currentColorIndex, selectedColor);
//	}
	private void changeColor(int i, Color color) {
		System.out.println("Setting color " + i + " to: " + color);
		copy.setSegmentColor(i, clampColorVector(color.getComponents(null)));
	}


	private void setParticleSpeed(JSlider speedSlider) {
		copy.setSpeed(speedSlider.getValue());
	}


	private BasicComboBoxRenderer getBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
			                                              boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value == null
						? "(Unanimated)" : value, index, isSelected, cellHasFocus);
			}
		};
	}

	private ModelHandler getModelHandler(ParticleEmitter2 copy) {
		EditableModel tempModel = new EditableModel();

		tempModel.setExtents(new ExtLog(100));
		Animation animation = new Animation("stand", 100, 1100);
		animation.setNonLooping(false);
		tempModel.add(animation);

//		Bone bone = new Bone("noBone");
//		tempModel.add(bone);

		copy.setParent(null);
		if (copy.getVisibilityFlag() != null) {
			FloatAnimFlag flag = new FloatAnimFlag(MdlUtils.TOKEN_VISIBILITY);
			flag.addEntry(100, 1f, animation);
			flag.addEntry(1100, 1f, animation);
			copy.setVisibilityFlag(flag);
		}
		System.out.println("copy name: " + copy.getName());
//		for (AnimFlag<?> animFlag : copy.getAnimFlags()) {
//			System.out.println("copy got flag: " + animFlag.getName() + " (" + animFlag.getEntryMap().size() + ")");
//		}
		tempModel.add(copy.getTexture());
		tempModel.add(copy);

		ModelHandler modelHandler = new ModelHandler(tempModel);
		modelHandler.getPreviewRenderModel().getTimeEnvironment().setSequence(animation);
		modelHandler.getPreviewRenderModel().setVetoOverrideParticles(true);

		return modelHandler;
	}

	private PerspectiveViewport getViewport(ModelHandler modelHandler) {
		try {
			modelHandler.getPreviewRenderModel().updateNodes(true);
			PerspectiveViewport perspectiveViewport = new PerspectiveViewport().setModel(modelHandler.getModelView(), modelHandler.getPreviewRenderModel(), true);
			perspectiveViewport.setMinimumSize(new Dimension(200, 200));
			return perspectiveViewport;
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTitle(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		perspectiveViewport.paint(perspectiveViewport.getGraphics());
	}

	public void reloadAllTextures() {
		perspectiveViewport.reloadAllTextures();
	}

	private JPanel getColorPanel() {
		JPanel panel = new JPanel(new MigLayout("ins 0"));
		ColorChooserIconLabel button0 = new ColorChooserIconLabel(copy.getSegmentColor(0).asFloatColor(), c -> changeColor(0, c));
		ColorChooserIconLabel button1 = new ColorChooserIconLabel(copy.getSegmentColor(1).asFloatColor(), c -> changeColor(1, c));
		ColorChooserIconLabel button2 = new ColorChooserIconLabel(copy.getSegmentColor(2).asFloatColor(), c -> changeColor(2, c));

		panel.add(button0);
		panel.add(button1);
		panel.add(button2);

		return panel;
	}
}
