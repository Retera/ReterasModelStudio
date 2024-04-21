package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.SetFromOtherParticle2Action;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
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
import com.hiveworkshop.rms.util.*;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParticleEditPanel extends JPanel {
	private final static String SLIDER_CONSTRAINTS = "wrap, growx, spanx";
	private final static String SPINNER_CONSTRAINTS = "wrap, growx, spanx, align 50% 100%";
	private final ModelHandler tempModelHandler;
	private final PerspectiveViewport perspectiveViewport;
	private final ParticleEmitter2 particleEmitter2;
	private ParticleEmitter2 copy;
	Map<String, Function<ParticleEmitter2, Double>> nameToValueGetter = new HashMap<>();
	{
		nameToValueGetter.put(MdlUtils.TOKEN_WIDTH, ParticleEmitter2::getWidth);
		nameToValueGetter.put(MdlUtils.TOKEN_LENGTH, ParticleEmitter2::getLength);
		nameToValueGetter.put(MdlUtils.TOKEN_LATITUDE, ParticleEmitter2::getLatitude);
		nameToValueGetter.put(MdlUtils.TOKEN_VARIATION, ParticleEmitter2::getVariation);
		nameToValueGetter.put(MdlUtils.TOKEN_SPEED, ParticleEmitter2::getSpeed);
		nameToValueGetter.put(MdlUtils.TOKEN_GRAVITY, ParticleEmitter2::getGravity);
		nameToValueGetter.put(MdlUtils.TOKEN_EMISSION_RATE, ParticleEmitter2::getEmissionRate);
		nameToValueGetter.put(MdlUtils.TOKEN_LIFE_SPAN, ParticleEmitter2::getLifeSpan);
	}

	public ParticleEditPanel(ParticleEmitter2 particleEmitter2) {
		super(new MigLayout("", "[]", ""));
		this.particleEmitter2 = particleEmitter2;

		copy = getCopyForEditing(particleEmitter2);
		tempModelHandler = getModelHandler(copy);
		perspectiveViewport = getViewport(tempModelHandler);

		TimeEnvironmentImpl renderEnv = tempModelHandler.getPreviewRenderModel().getTimeEnvironment();
		renderEnv.setRelativeAnimationTime(0);
		renderEnv.setLive(true);

		JPanel viewportPanel = new JPanel(new BorderLayout());
		setLayout(new MigLayout());

		viewportPanel.add(perspectiveViewport, BorderLayout.CENTER);
		add(viewportPanel, "");

		add(getHeadTailGroup(), "");
		add(getFilterModeButtonGroup(), "");
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
		updateSquirtTimer(copy.getSquirt());
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
	private EnumButtonGroup<ParticleEmitter2.HeadTailFlag> getHeadTailGroup() {
		EnumButtonGroup<ParticleEmitter2.HeadTailFlag> bg2 = new EnumButtonGroup<>(ParticleEmitter2.HeadTailFlag.class, "Head/Tail", 1, copy::toggleFlag);
		bg2.addPanelConst("gap 0", "ins 0");

		bg2.setSelected(copy.getHeadTailFlags());
		return bg2;
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

		sliderPanel.add(new SmartNumberSlider("LifeSpan (~ 0.1 s)", copy.getLifeSpan() * 10, 200, (i) -> {copy.setLifeSpan(i / 10d); updateSquirtTimer(copy.getSquirt());}).setMinLowerLimit(0).setMaxUpperLimit(1000), SLIDER_CONSTRAINTS);
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
//		flagPanel.add(getCheckBox("Squirt", copy.getSquirt(), (b) -> copy.setSquirt(b)), "wrap");


		flagPanel.add(getCheckBox("Squirt", copy.getSquirt(), (b) -> {copy.setSquirt(b);updateSquirtTimer(b);}), "wrap");

		return flagPanel;
	}

	Timer[] timer = new Timer[1];
	private void updateSquirtTimer(boolean isSquirt) {
		if (timer[0] != null) {
			timer[0].cancel();
			timer[0] = null;
		}

		if (isSquirt) {
			int delay = Math.max((int)(1300 * copy.getLifeSpan()), 2500);
			timer[0] = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					tempModelHandler.getPreviewRenderModel().refreshFromEditor();
				}
			};
			timer[0].schedule(task, delay, delay);
		}
	}
	private EnumButtonGroup<ParticleEmitter2.P2Flag> getFlagPanel2() {
		// if this gets used, a checkbox for "Squirt" needs to be added separately in a
		// suitable location (under emission rate..?)
		EnumButtonGroup<ParticleEmitter2.P2Flag> p2FlagEnumButtonGroup = new EnumButtonGroup<>(ParticleEmitter2.P2Flag.class, "Flags", EnumButtonGroupGroup.TYPE_CHECK, copy::toggleFlag);
		p2FlagEnumButtonGroup.addPanelConst("ins 0", "gap 0");
		return p2FlagEnumButtonGroup;
	}

	private EnumButtonGroup<MdlxParticleEmitter2.FilterMode> getFilterModeButtonGroup() {
		EnumButtonGroup<MdlxParticleEmitter2.FilterMode> filterModeG = new EnumButtonGroup<>(MdlxParticleEmitter2.FilterMode.class, "Filter Mode", EnumButtonGroupGroup.TYPE_RADIO, copy::setFilterMode);
		filterModeG.addPanelConst("gap 0", "ins 0");
		filterModeG.setSelected(copy.getFilterMode());
		return filterModeG;
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

			for (AnimFlag<?> animFlag : particleEmitter2.getAnimFlags()) {
				if (animFlag != null) {
					emitter2.add(animFlag.deepCopy());
				}
			}

			List<String> affectedAnims = getAffectedAnims(emitter2);
			if (checkShouldScaleAnims(affectedAnims)) {
				for (String s : affectedAnims) {
					AnimFlag<?> animFlag = emitter2.find(s);
					if (animFlag instanceof FloatAnimFlag) {
						double maxValue = nameToValueGetter.get(s).apply(emitter2);
						scaleAnimAmplitude(animFlag, (float) maxValue);
					}
				}
			}
			UndoAction action = new AddNodeAction(modelPanel.getModel(), emitter2, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}

	private void doApply1() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel().contains(particleEmitter2)) {
			UndoAction action1 = new SetFromOtherParticle2Action(particleEmitter2, copy);
			modelPanel.getModelHandler().getUndoManager().pushAction(action1.redo());
		}
	}
	private void doApply() {

		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel().contains(particleEmitter2)) {
			UndoAction action1 = new SetFromOtherParticle2Action(particleEmitter2, copy);
			List<String> affectedAnims = getAffectedAnims(particleEmitter2);
			if (checkShouldScaleAnims(affectedAnims)) {
				List<UndoAction> undoActions = new ArrayList<>();
				undoActions.add(action1);
				for (String s : affectedAnims) {
					UndoAction scaleAmplitudeAction = getScaleAmplitudeAction(particleEmitter2, s, nameToValueGetter.get(s).apply(copy));
					if (scaleAmplitudeAction != null) {
						undoActions.add(scaleAmplitudeAction);
					}
				}
				modelPanel.getModelHandler().getUndoManager().pushAction(new CompoundAction(action1.actionName(), undoActions).redo());
			} else {
				modelPanel.getModelHandler().getUndoManager().pushAction(action1.redo());
			}
		}
	}

	private boolean checkShouldScaleAnims(List<String> affectedAnims) {
		if (!affectedAnims.isEmpty()) {
			StringBuilder sb = new StringBuilder("The following is animated:\n");
			for (String affectedAnim : affectedAnims) {
				sb.append("    ").append(affectedAnim).append("\n");

			}
			sb.append("Scale animated values to match edited values?");
			int opt = JOptionPane.showConfirmDialog(this, sb.toString(), "Values Animated", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			return opt == JOptionPane.YES_OPTION;
		}
		return false;
	}

	private JPanel getTexturePanel() {
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
		textureChooser.selectOrFirst(copy.getTexture());
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

	private void changeTexture(Bitmap texture, JLabel imageLabel) {
		copy.setTexture(texture);
		updateImageLabel(imageLabel);
	}

	private void updateImageParts(int rows, int cols, JLabel imageLabel) {
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
		float colSpace = w / (float)cols;
		float rowSpace = h / (float)rows;
		for (float i = colSpace; i < w; i+= colSpace) {
			graphics.drawLine((int) i, 0, (int) i, h);
		}
		for (float i = rowSpace; i < h; i+= rowSpace) {
			graphics.drawLine(0, (int) i, w, (int) i);
		}
		graphics.dispose();

		imageLabel.setIcon(new ImageIcon(iconImage));
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
		Animation animation = new Animation("stand", 100, 5100);
		animation.setNonLooping(false);
		tempModel.add(animation);

//		Bone bone = new Bone("noBone");
//		tempModel.add(bone);

		copy.setParent(null);
		if (copy.getVisibilityFlag() != null) {
			FloatAnimFlag flag = new FloatAnimFlag(MdlUtils.TOKEN_VISIBILITY);
			flag.addEntry(0, 1f, animation);
			flag.addEntry(animation.getLength(), 1f, animation);
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

	private UndoAction getScaleAmplitudeAction(ParticleEmitter2 emitter, String flagName, double newMaxValue) {
		AnimFlag<?> animFlag = emitter.find(flagName);
		if (animFlag instanceof FloatAnimFlag) {
			AnimFlag<?> newAnimFlag = animFlag.deepCopy();
			scaleAnimAmplitude(newAnimFlag, (float) newMaxValue);
			return new AddAnimFlagAction<>(emitter, newAnimFlag, null);
		}
		return null;
	}

	private void scaleAnimAmplitude(AnimFlag<?> animFlag, float newMaxValue) {
		if (animFlag instanceof FloatAnimFlag floatAnimFlag) {
			float staticValue = getStaticValue(floatAnimFlag, newMaxValue);
			if (staticValue != 0 && staticValue != newMaxValue) {
				float scaleFactor = newMaxValue/staticValue;
				for (TreeMap<Integer, Entry<Float>> seqMap : floatAnimFlag.getAnimMap().values()) {
					for (Entry<Float> entry : seqMap.values()) {
						entry.setValue(entry.getValue() * scaleFactor);
						if (entry.isTangential()) {
							entry.setInTan(entry.getInTan() * scaleFactor);
							entry.setOutTan(entry.getOutTan() * scaleFactor);
						}
					}
				}
			}
		}
	}

	private List<String> getAffectedAnims(ParticleEmitter2 emitter) {
		List<String> animatedStuff = new ArrayList<>();
		String[] flagNames = new String[] {
				MdlUtils.TOKEN_WIDTH,
				MdlUtils.TOKEN_LENGTH,
				MdlUtils.TOKEN_LATITUDE,
				MdlUtils.TOKEN_VARIATION,
				MdlUtils.TOKEN_SPEED,
				MdlUtils.TOKEN_GRAVITY,
				MdlUtils.TOKEN_EMISSION_RATE,
				MdlUtils.TOKEN_LIFE_SPAN};
		for (String flagName : flagNames) {
//			System.out.println("Has \"" + flagName + "\": " + emitter.has("flagName"));
			if (emitter.has(flagName)) {
				animatedStuff.add(flagName);
			}
		}
		return animatedStuff;
	}

	private ParticleEmitter2 getCopyForEditing(ParticleEmitter2 emitter) {
		ParticleEmitter2 copy = emitter.copy();

		setStaticValue(copy, MdlUtils.TOKEN_WIDTH, copy::setWidth);
		setStaticValue(copy, MdlUtils.TOKEN_LENGTH, copy::setLength);
		setStaticValue(copy, MdlUtils.TOKEN_LATITUDE, copy::setLatitude);
		setStaticValue(copy, MdlUtils.TOKEN_VARIATION, copy::setVariation);
		setStaticValue(copy, MdlUtils.TOKEN_SPEED, copy::setSpeed);
		setStaticValue(copy, MdlUtils.TOKEN_GRAVITY, copy::setGravity);
		setStaticValue(copy, MdlUtils.TOKEN_EMISSION_RATE, copy::setEmissionRate);
		setStaticValue(copy, MdlUtils.TOKEN_LIFE_SPAN, copy::setLifeSpan);

		copy.clearAnimFlags();

//		copy.setWidth(getStaticValue(copy, MdlUtils.TOKEN_WIDTH, copy.getWidth()));
//		copy.setLength(getStaticValue(copy, MdlUtils.TOKEN_LENGTH, copy.getLength()));
//		copy.setLatitude(getStaticValue(copy, MdlUtils.TOKEN_LATITUDE, copy.getLatitude()));
//		copy.setVariation(getStaticValue(copy, MdlUtils.TOKEN_VARIATION, copy.getVariation()));
//		copy.setSpeed(getStaticValue(copy, MdlUtils.TOKEN_SPEED, copy.getSpeed()));
//		copy.setGravity(getStaticValue(copy, MdlUtils.TOKEN_GRAVITY, copy.getGravity()));
//		copy.setEmissionRate(getStaticValue(copy, MdlUtils.TOKEN_EMISSION_RATE, copy.getEmissionRate()));
//		copy.setLifeSpan(getStaticValue(copy, MdlUtils.TOKEN_LIFE_SPAN, copy.getLifeSpan()));
//
//
//		copy.setWidth(getStaticValue(copy.find(MdlUtils.TOKEN_WIDTH), copy.getWidth()));
//		copy.setLength(getStaticValue(copy.find(MdlUtils.TOKEN_LENGTH), copy.getLength()));
//		copy.setLatitude(getStaticValue(copy.find(MdlUtils.TOKEN_LATITUDE), copy.getLatitude()));
//		copy.setVariation(getStaticValue(copy.find(MdlUtils.TOKEN_VARIATION), copy.getVariation()));
//		copy.setSpeed(getStaticValue(copy.find(MdlUtils.TOKEN_SPEED), copy.getSpeed()));
//		copy.setGravity(getStaticValue(copy.find(MdlUtils.TOKEN_GRAVITY), copy.getGravity()));
//		copy.setEmissionRate(getStaticValue(copy.find(MdlUtils.TOKEN_EMISSION_RATE), copy.getEmissionRate()));
//		copy.setLifeSpan(getStaticValue(copy.find(MdlUtils.TOKEN_LIFE_SPAN), copy.getLifeSpan()));


		return copy;
	}
	private void setStaticValue(ParticleEmitter2 emitter, String timelineTitle, Consumer<Float> valueConsumer) {
		AnimFlag<?> animFlag = emitter.find(timelineTitle);
		if (animFlag instanceof FloatAnimFlag) {
			Float maxFloat = getStaticValue((FloatAnimFlag) animFlag, null);
			if (maxFloat != null) {
				valueConsumer.accept(maxFloat);
			}
		}
	}

	private Float getStaticValue(FloatAnimFlag animFlag, Float value) {
		Float maxFloat = null;
		for (TreeMap<Integer, Entry<Float>> seqMap : animFlag.getAnimMap().values()) {
			for (Entry<Float> entry : seqMap.values()) {
				maxFloat = maxFloat == null ? entry.getValue() : Math.max(maxFloat, entry.getValue());
			}
		}
		if (maxFloat != null) {
			return maxFloat;
		}
		return value;
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

	private void changeColor(int i, Color color) {
		System.out.println("Setting color " + i + " to: " + color);
		copy.setSegmentColor(i, clampColorVector(color.getComponents(null)));
	}

	private float[] clampColorVector(float[] rowColor) {
		for (int i = 0; i < rowColor.length; i++) {
			rowColor[i] = Math.max(0, Math.min(1, rowColor[i]));
		}
		return rowColor;
	}
}
