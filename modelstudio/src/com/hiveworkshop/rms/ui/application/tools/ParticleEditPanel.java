package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetFromOtherParticle2Action;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.util.function.Consumer;

public class ParticleEditPanel extends JPanel {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";
	private final static String SPINNER_CONSTRAINTS = "wrap, growx, spanx, align 50% 100%";
	private final ModelHandler tempModelHandler;
	private final PerspectiveViewport perspectiveViewport;
	private final ParticleEmitter2 particleEmitter2;
	private ParticleEmitter2 copy;

	private final JColorChooser colorChooser = new JColorChooser();
	private final JPopupMenu chooseColor = new JPopupMenu("Choose Color");
	private final Vec3 selectedColor = new Vec3(1, 1, 1);
	int currentColorIndex = 0;
	JButton[] colorButtons;

	public ParticleEditPanel(ParticleEmitter2 particleEmitter2) {
		super(new MigLayout("", "[]", ""));
		this.particleEmitter2 = particleEmitter2;
		tempModelHandler = getModelHandler(particleEmitter2);
		perspectiveViewport = getViewport(tempModelHandler);

		colorChooser.getSelectionModel().addChangeListener(e -> {
			Color color = colorChooser.getColor();
			selectedColor.set(color.getComponents(null));
			if (colorButtons != null) {
				colorButtons[currentColorIndex].setIcon(new ImageIcon(IconUtils.createBlank(color, 32, 32)));
			}
			changeColor();
		});
		chooseColor.add(colorChooser);

//		for (AnimFlag<?> animFlag : particleEmitter2.getAnimFlags()) {
//			System.out.println("got flag: " + animFlag.getName() + " (" + animFlag.getEntryMap().size() + ")");
//		}


		TimeEnvironmentImpl renderEnv = tempModelHandler.getPreviewTimeEnv();
		renderEnv.setRelativeAnimationTime(0);
		renderEnv.setLive(true);

		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "");

		SmartButtonGroup headTail = new SmartButtonGroup("Head/Tail");
		headTail.addPanelConst("gap 0");
		headTail.addJRadioButton("Head", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.HEAD));
		headTail.addJRadioButton("Tail", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.TAIL));
		headTail.addJRadioButton("Both", e -> copy.setHeadOrTail(MdlxParticleEmitter2.HeadOrTail.BOTH));
		headTail.setSelectedIndex(copy.getHeadOrTail().ordinal());
		add(headTail.getButtonPanel(), "");

		SmartButtonGroup filterMode = new SmartButtonGroup("Filter Mode");
		filterMode.addPanelConst("gap 0");
		filterMode.addJRadioButton("Blend", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.BLEND));
		filterMode.addJRadioButton("Additive", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.ADDITIVE));
		filterMode.addJRadioButton("Modulate", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.MODULATE));
		filterMode.addJRadioButton("Modulate2x", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.MODULATE2X));
		filterMode.addJRadioButton("AlphaKey", e -> copy.setFilterMode(MdlxParticleEmitter2.FilterMode.ALPHAKEY));
		filterMode.setSelectedIndex(copy.getFilterMode().ordinal());
		add(filterMode.getButtonPanel(), "");

		JPanel flagPanel = new JPanel(new MigLayout("ins 0, gap 0"));
		flagPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(flagPanel, "wrap");

		flagPanel.add(getCheckBox("Unshaded", copy.getUnshaded(), (b) -> copy.setUnshaded(b)), "wrap");
		flagPanel.add(getCheckBox("Unfogged", copy.getUnfogged(), (b) -> copy.setUnfogged(b)), "wrap");
		flagPanel.add(getCheckBox("LineEmitter", copy.getLineEmitter(), (b) -> copy.setLineEmitter(b)), "wrap");
		flagPanel.add(getCheckBox("SortPrimsFarZ", copy.getSortPrimsFarZ(), (b) -> copy.setSortPrimsFarZ(b)), "wrap");
		flagPanel.add(getCheckBox("ModelSpace", copy.getModelSpace(), (b) -> copy.setModelSpace(b)), "wrap");
		flagPanel.add(getCheckBox("XYQuad", copy.getXYQuad(), (b) -> copy.setXYQuad(b)), "wrap");
		flagPanel.add(getCheckBox("Squirt", copy.getSquirt(), (b) -> copy.setSquirt(b)), "wrap");


		JPanel subPanel = new JPanel(new MigLayout("ins 0", "[]20[]"));

		JPanel sliderPanel = new JPanel(new MigLayout("ins 0"));

		sliderPanel.add(new SmartNumberSlider("Speed", copy.getSpeed(), 500, (i) -> copy.setSpeed(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Variation (%)", copy.getVariation() * 100, 500, (i) -> copy.setVariation(i / 100d)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("EmissionRate", copy.getEmissionRate(), 100, (i) -> copy.setEmissionRate(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Latitude", copy.getLatitude(), 180, (i) -> copy.setLatitude(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Gravity", copy.getGravity(), -100, 100, (i) -> copy.setGravity(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Width", copy.getWidth(), 400, (i) -> copy.setWidth(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Length", copy.getLength(), 400, (i) -> copy.setLength(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("LifeSpan (~ 0.1 s)", copy.getLifeSpan() * 10, 200, (i) -> copy.setLifeSpan(i / 10d)).setMinLowerLimit(0).setMaxUpperLimit(1000), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("TailLength", copy.getTailLength(), 100, (i) -> copy.setTailLength(i)), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Time (%)", copy.getTime() * 100, 100, (i) -> copy.setTime(i / 100d)).setMinLowerLimit(0).setMaxUpperLimit(100).setExpandMax(false).setExpandMin(false), SLIDER_CONSTRAINTS);

		sliderPanel.add(new JLabel("Sub-Textures:"), "wrap");
		sliderPanel.add(new SmartNumberSlider("Rows", copy.getRows(), 16, (i) -> copy.setRows(i)).setMinLowerLimit(1).setMaxUpperLimit(100), SLIDER_CONSTRAINTS);
		sliderPanel.add(new SmartNumberSlider("Columns", copy.getColumns(), 16, (i) -> copy.setColumns(i)).setMinLowerLimit(1).setMaxUpperLimit(100), SLIDER_CONSTRAINTS);
		subPanel.add(sliderPanel, "");

		JPanel spinnerPanel = new JPanel(new MigLayout("ins 0"));


		spinnerPanel.add(getColorPanel(), SPINNER_CONSTRAINTS);

		JPanel alphaPanel = new Vec3SpinnerArray(copy.getAlpha(), 0, .1f)
				.setVec3Consumer((v) -> copy.setAlpha(v))
				.spinnerPanel();
		alphaPanel.setBorder(BorderFactory.createTitledBorder("Alpha"));
		spinnerPanel.add(alphaPanel, SPINNER_CONSTRAINTS);

		JPanel scalingPanel = new Vec3SpinnerArray(copy.getParticleScaling())
				.setVec3Consumer((v) -> copy.setParticleScaling(v))
				.spinnerPanel();
		scalingPanel.setBorder(BorderFactory.createTitledBorder("Particle Scaling"));
		spinnerPanel.add(scalingPanel, SPINNER_CONSTRAINTS);

		JPanel headPanel = new JPanel(new MigLayout("ins 0"));
		headPanel.setBorder(BorderFactory.createTitledBorder("Head UV (sub-texture index)"));
		headPanel.add(new JLabel("Life Span"));
		headPanel.add(new Vec3SpinnerArray(copy.getHeadUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setHeadUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		headPanel.add(new JLabel("Decay"));
		headPanel.add(new Vec3SpinnerArray(copy.getHeadDecayUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setHeadDecayUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		spinnerPanel.add(headPanel, SPINNER_CONSTRAINTS);

		JPanel tailPanel = new JPanel(new MigLayout("ins 0"));
		tailPanel.setBorder(BorderFactory.createTitledBorder("Tail UV (sub-texture index)"));
		tailPanel.add(new JLabel("Life Span"));
		tailPanel.add(new Vec3SpinnerArray(copy.getTailUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setTailUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		tailPanel.add(new JLabel("Decay"));
		tailPanel.add(new Vec3SpinnerArray(copy.getTailDecayUVAnim(), "start", "end", "repeat").setVec3Consumer((v) -> copy.setTailDecayUVAnim(v)).spinnerPanel(), SPINNER_CONSTRAINTS);
		spinnerPanel.add(tailPanel, SPINNER_CONSTRAINTS);

//		add(new SmartNumberSlider("Textureid", copy.getTextureID(), 50, (i) -> copy.setTextureID(i)), sliderConstraints);
//		add(new SmartNumberSlider("Replaceableid", copy.getReplaceableId(), 50, (i) -> copy.setReplaceableId(i)), sliderConstraints);
//		add(new SmartNumberSlider("Priorityplane", copy.getPriorityPlane(), 50, (i) -> copy.setPriorityPlane(i)), sliderConstraints);

		subPanel.add(spinnerPanel, "wrap");
		add(subPanel, "spanx, wrap");

		JButton apply = new JButton("Apply");
		apply.addActionListener(e -> doApply());
		add(apply, "wrap");

		JButton addAsNew = new JButton("Add as new Emitter");
		addAsNew.addActionListener(e -> addAsNew());
		add(addAsNew, "wrap");
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

//	private void doApply(boolean ugg){
//		particleEmitter2.setSpeed(copy.getSpeed());
//		particleEmitter2.setVariation(copy.getVariation());
//		particleEmitter2.setEmissionRate(copy.getEmissionRate());
//		particleEmitter2.setLatitude(copy.getLatitude());
//		particleEmitter2.setGravity(copy.getGravity());
//		particleEmitter2.setWidth(copy.getWidth());
//		particleEmitter2.setLength(copy.getLength());
//		particleEmitter2.setLifeSpan(copy.getLifeSpan());
//		particleEmitter2.setTailLength(copy.getTailLength());
//		particleEmitter2.setTime(copy.getTime());
//		particleEmitter2.setRows(copy.getRows());
//		particleEmitter2.setColumns(copy.getColumns());
//
//		particleEmitter2.setAlpha(copy.getAlpha());
//		particleEmitter2.setParticleScaling(copy.getParticleScaling());
//		particleEmitter2.setHeadUVAnim(copy.getHeadUVAnim());
//		particleEmitter2.setHeadDecayUVAnim(copy.getHeadDecayUVAnim());
//		particleEmitter2.setTailUVAnim(copy.getTailUVAnim());
//		particleEmitter2.setTailDecayUVAnim(copy.getTailDecayUVAnim());
//
//		particleEmitter2.setSegmentColor(0, copy.getSegmentColor(0));
//		particleEmitter2.setSegmentColor(1, copy.getSegmentColor(1));
//		particleEmitter2.setSegmentColor(2, copy.getSegmentColor(2));
//	}

	private void setColor(Vec3 color) {
		Color color1 = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(color.toFloatArray()), 1.0f);
		colorChooser.setColor(color1);
	}

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < rowColor.length; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}

	private void changeColor() {
		copy.setSegmentColor(currentColorIndex, selectedColor);
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

	private ModelHandler getModelHandler(ParticleEmitter2 particleEmitter2) {
		EditableModel tempModel = new EditableModel();

		tempModel.setExtents(new ExtLog(100));
		Animation animation = new Animation("stand", 100, 1100);
		animation.setNonLooping(false);
		tempModel.add(animation);

//		Bone bone = new Bone("noBone");
//		tempModel.add(bone);

		copy = particleEmitter2.copy();
//		ParticleEmitter2 copy = particleEmitter2.copy();
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
		modelHandler.getPreviewTimeEnv().setSequence(animation);
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
		colorButtons = new JButton[3];
		colorButtons[0] = getColorButton(0);
		colorButtons[1] = getColorButton(1);
		colorButtons[2] = getColorButton(2);

		panel.add(colorButtons[0]);
		panel.add(colorButtons[1]);
		panel.add(colorButtons[2]);

		return panel;
	}

	private JButton getColorButton(int i) {
		Color color = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), clampColorVector(copy.getSegmentColor(i).toFloatArray()), 1.0f);
//		JButton button = new JButton("Color " + (i+1), new ImageIcon(IconUtils.createBlank(color, 32, 32)));
		JButton button = new JButton("", new ImageIcon(IconUtils.createBlank(color, 32, 32)));
		button.addActionListener(e12 -> {
			currentColorIndex = i;
			setColor(copy.getSegmentColor(i));
			chooseColor.show(button, 0, 0);
		});

		return button;
	}

	private void makeColorButtons(ParticleEmitter2 particle, JButton[] colorButtons, Color[] colors) {
		for (int i = 0; i < colorButtons.length; i++) {
			final Vec3 colorValues = particle.getSegmentColor(i);
			final Color color = new Color((int) (colorValues.x * 255), (int) (colorValues.y * 255), (int) (colorValues.z * 255));

			final JButton button = new JButton("Color " + (i + 1), new ImageIcon(IconUtils.createBlank(color, 32, 32)));
			colors[i] = color;
			final int index = i;
			button.addActionListener(e12 -> {
				final Color colorChoice = JColorChooser.showDialog(this, "Chooser Color", colors[index]);
				if (colorChoice != null) {
					colors[index] = colorChoice;
					button.setIcon(new ImageIcon(IconUtils.createBlank(colors[index], 32, 32)));
				}
			});
			colorButtons[i] = button;
		}
	}
}
