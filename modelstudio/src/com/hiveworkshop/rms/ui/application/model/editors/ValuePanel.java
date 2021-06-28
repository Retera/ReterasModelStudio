package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.*;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public abstract class ValuePanel<T> extends JPanel {
	protected final JComponent staticComponent;
	protected final JComboBox<InterpolationType> interpTypeBox;
	protected final UndoManager undoManager;
	protected final ModelStructureChangeListener modelStructureChangeListener;
	protected T staticValue;
	protected TimelineContainer timelineContainer;
	protected String flagName;
	protected AnimFlag<T> animFlag;
	protected TimelineKeyNamer timelineKeyNamer;
	JPanel dynStatPanel;
	private final CardLayout dynStatLayout;

	KeyframePanel<T> keyframePanel;

	protected Consumer<T> valueSettingFunction;

	public ValuePanel(ModelHandler modelHandler, final String title, UndoManager undoManager) {
		this(modelHandler, title, Double.MAX_VALUE, -Double.MAX_VALUE, undoManager);
	}

	public ValuePanel(ModelHandler modelHandler, final String title, double maxValue, double minValue, UndoManager undoManager) {
		this.undoManager = undoManager;
		this.modelStructureChangeListener = ModelStructureChangeListener.changeListener;
		setFocusable(true);
		timelineKeyNamer = null;
		animFlag = null;
		timelineContainer = null;
		flagName = "";
		keyframePanel = new KeyframePanel<>(modelHandler.getModel(), this::addEntry, this::removeEntry, this::changeEntry, this::parseValue);

		setBorder(BorderFactory.createTitledBorder(title));
		setLayout(new MigLayout("fill, hidemode 1", "[]", "[][][]0[][]"));

		dynStatLayout = new CardLayout();
		dynStatPanel = new JPanel(dynStatLayout);
		add(dynStatPanel, "align center, growx, wrap");

		JPanel dynamicPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[][][][]"));
		dynamicPanel.add(new JLabel("Dynamic"), "al 0% 0%, hidemode 3");
		dynStatPanel.add("dynamic", dynamicPanel);

		JButton makeStaticButton = new JButton("Make Static");
		makeStaticButton.addActionListener(e -> makeStatic());
		makeStaticButton.setVisible(true);
		dynamicPanel.add(makeStaticButton, "al 100% 0%, wrap, hidemode 3");

		JPanel spinInterpPanel = new JPanel(new MigLayout("ins 0, gap 0, hidemode 3", "[]", "[]"));
		dynamicPanel.add(spinInterpPanel, "wrap, hidemode 3");

		spinInterpPanel.add(new JLabel("Interpolation:"));
		interpTypeBox = new JComboBox<>(InterpolationType.values());
		interpTypeBox.addItemListener(this::setInterpolationType);
		spinInterpPanel.add(interpTypeBox, "wrap, hidemode 3");

		dynamicPanel.add(keyframePanel, "spanx, growx, wrap, hidemode 3");

		JPanel staticPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[]"));
		staticPanel.add(new JLabel("Static"), "al 0% 0%");
		JButton makeDynamicButton = new JButton("Make Dynamic");
		makeDynamicButton.addActionListener(e -> makeDynamic());
		staticPanel.add(makeDynamicButton, "al 100% 0%, wrap, hidemode 3");

		staticComponent = this.getStaticComponent();
		staticPanel.add(staticComponent, "wrap, hidemode 3");

		dynStatPanel.add("static", staticPanel);
	}

	private void makeStatic() {
		toggleStaticDynamicPanel(true);
		RemoveAnimFlagAction removeAnimFlagAction = new RemoveAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
		undoManager.pushAction(removeAnimFlagAction.redo());
	}

	private void makeDynamic() {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			if (staticValue == null) {
				staticValue = getZeroValue();
			}
			AnimFlag<T> newAnimFlag = getNewAnimFlag();

			if (newAnimFlag != null) {
				toggleStaticDynamicPanel(false);
				newAnimFlag.addEntry(0, staticValue);
				AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, newAnimFlag, modelStructureChangeListener);
				undoManager.pushAction(addAnimFlagAction.redo());
			}
		} else if (animFlag != null) {
			toggleStaticDynamicPanel(false);
			animFlag.addEntry(0, staticValue);
			AddAnimFlagAction addAnimFlagAction = new AddAnimFlagAction(timelineContainer, animFlag, modelStructureChangeListener);
			undoManager.pushAction(addAnimFlagAction.redo());
		}
//		System.out.println("ftt: "+ floatTrackTableModel.getColumnCount());
//		keyframeTable.setModel(floatTrackTableModel);
//		System.out.println("kft_b: " + keyframeTable.getColumnCount());
//		System.out.println("kftM_b: " + keyframeTable.getModel().getColumnCount());
//		keyframeTable.revalidate();
//		System.out.println("kft_a: " + keyframeTable.getColumnCount());
//		System.out.println("kftM_a: " + keyframeTable.getModel().getColumnCount());
	}

	private AnimFlag<T> getNewAnimFlag() {
		if (staticValue instanceof Integer) {
			return (AnimFlag<T>) new IntAnimFlag(flagName);
		} else if (staticValue instanceof Float) {
			return (AnimFlag<T>) new FloatAnimFlag(flagName);
		} else if (staticValue instanceof Vec3) {
			return (AnimFlag<T>) new Vec3AnimFlag(flagName);
		} else if (staticValue instanceof Quat) {
			return (AnimFlag<T>) new QuatAnimFlag(flagName);
		} else {
			return null;
		}
	}

	private void toggleStaticDynamicPanel(boolean isStatic) {
		boolean isDynamic = !isStatic;
		if (isDynamic) {
			dynStatLayout.show(dynStatPanel, "dynamic");
		} else {
			dynStatLayout.show(dynStatPanel, "static");
		}
	}

	public void setKeyframeHelper(TimelineKeyNamer timelineKeyNamer) {
		this.timelineKeyNamer = timelineKeyNamer;
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag) {
		reloadNewValue(value, animFlag, null, "");
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag, final TimelineContainer timelineContainer, final String flagName, Consumer<T> valueSettingFunction) {
		this.valueSettingFunction = valueSettingFunction;
		reloadNewValue(value, animFlag, timelineContainer, flagName);
	}

	public void reloadNewValue(final T value, final AnimFlag<T> animFlag, final TimelineContainer timelineContainer, final String flagName) {
		this.animFlag = animFlag;
		this.timelineContainer = timelineContainer;
		this.flagName = flagName;
		staticValue = value;

		if (animFlag == null) {
			toggleStaticDynamicPanel(true);
			keyframePanel.updateFlag(null);
		} else {
			keyframePanel.updateFlag(animFlag);
			toggleStaticDynamicPanel(false);
			interpTypeBox.setSelectedItem(animFlag.getInterpolationType());
		}

		reloadStaticValue(value);
		revalidate();
		repaint();
	}

	abstract JComponent getStaticComponent();

	abstract void reloadStaticValue(T value);

	private void setInterpolationType(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED
				&& e.getItem() instanceof InterpolationType
				&& e.getItem() != animFlag.getInterpolationType()) {
			UndoAction undoAction = new ChangeInterpTypeAction<>(animFlag, (InterpolationType) e.getItem(), modelStructureChangeListener);
			undoManager.pushAction(undoAction.redo());

		}
	}

	private void addEntry(int row) {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			AnimFlag<T> flag = getNewAnimFlag();
			if (flag != null) {
				Entry<T> entry = new Entry<>(0, staticValue);
				flag.setOrAddEntryT(0, entry);
				UndoAction undoAction = new AddAnimFlagAction(timelineContainer, flag, modelStructureChangeListener);
				undoManager.pushAction(undoAction.redo());
			}
		} else if (animFlag != null) {
			Entry<T> newEntry;
			T zeroValue = getZeroValue();
			if (staticValue == null) {
				staticValue = zeroValue;
			}
			if (animFlag.getEntryMap().isEmpty()) {
				if (animFlag.getInterpolationType().tangential()) {
					newEntry = new Entry<>(0, staticValue, zeroValue, zeroValue);
				} else {
					newEntry = new Entry<>(0, staticValue);
				}
			} else {
				int time = animFlag.getTimeFromIndex(row);
				newEntry = animFlag.getEntryAt(time).deepCopy();

				while (animFlag.hasEntryAt(newEntry.getTime())) {
					newEntry.setTime(newEntry.getTime() + 1);
				}
			}

			UndoAction undoAction = new AddFlagEntryAction(animFlag, newEntry, timelineContainer, modelStructureChangeListener);
			undoManager.pushAction(undoAction.redo());

		}
	}

	abstract T getZeroValue();

	abstract T parseValue(String valueString);

	private void removeEntry(int orgTime) {
		if (animFlag.hasEntryAt(orgTime)) {
			UndoAction undoAction = new RemoveFlagEntryAction(animFlag, orgTime, timelineContainer, modelStructureChangeListener);
			undoManager.pushAction(undoAction.redo());
		}
	}

	protected void changeEntry(int row, String field, String val) {
		int orgTime = (int) keyframePanel.getFloatTrackTableModel().getValueAt(row, 0);
		Entry<T> newEntry = animFlag.getEntryAt(orgTime).deepCopy();
		T tValue = parseValue(val);
		System.out.println(val);
		System.out.println(tValue);

		switch (field) {
			case "Keyframe" -> newEntry.setTime(parseTime(val));
			case "Value" -> newEntry.setValue(tValue);
			case "InTan" -> newEntry.setInTan(tValue);
			case "OutTan" -> newEntry.setOutTan(tValue);
		}
		System.out.println(newEntry.getTime());

		UndoAction undoAction = new ChangeFlagEntryAction<>(animFlag, newEntry, orgTime, modelStructureChangeListener);
		undoManager.pushAction(undoAction.redo());
	}

	protected void changeEntry(Integer orgTime, Entry<T> newEntry) {
		UndoAction undoAction = new ChangeFlagEntryAction<>(animFlag, newEntry, orgTime, modelStructureChangeListener);
		undoManager.pushAction(undoAction.redo());
	}

	private int parseTime(String val) {
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));
		return Integer.parseInt(intString);
	}

}
