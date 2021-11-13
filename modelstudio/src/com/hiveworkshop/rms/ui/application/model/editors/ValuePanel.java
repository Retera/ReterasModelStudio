package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.*;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.CollapsablePanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

public abstract class ValuePanel<T> extends JPanel {
	protected final JComponent staticComponent;
	protected final JComboBox<InterpolationType> interpTypeBox;
	protected final UndoManager undoManager;
	protected final ModelHandler modelHandler;
	protected final ModelStructureChangeListener changeListener;
	protected T staticValue;
	protected TimelineContainer timelineContainer;
	protected String flagName;
	protected AnimFlag<T> animFlag;
	protected TimelineKeyNamer timelineKeyNamer;
	JPanel dynStatPanel;
	JPanel dynamicPanel;
	JPanel dynamicContentPanel;
	private final CardLayout dynStatLayout;
	protected String title;
	protected TitledBorder titledBorder;

	protected TreeMap<Sequence, KeyframePanel<T>> keyframePanelMap = new TreeMap<>();
	KeyframePanel<T> keyframePanel;

	protected Consumer<T> valueSettingFunction;

	protected CollapsablePanel collapsablePanel;

	public ValuePanel(ModelHandler modelHandler, final String title) {
		this(modelHandler, title, Double.MAX_VALUE, -Double.MAX_VALUE);
	}

	public ValuePanel(ModelHandler modelHandler, final String title, double maxValue, double minValue) {
		this.undoManager = modelHandler.getUndoManager();
		this.modelHandler = modelHandler;
		this.changeListener = ModelStructureChangeListener.changeListener;
		this.title = title;
		setFocusable(true);
		timelineKeyNamer = null;
		animFlag = null;
		timelineContainer = null;
		flagName = "";
//		keyframePanel = new KeyframePanel<>(modelHandler.getModel(), this::addEntry, this::removeEntry, this::changeEntry, this::parseValue);
		keyframePanel = new KeyframePanel<>(modelHandler.getModel(), this::addEntry, this::removeEntry, (time, entry) -> changeEntry(keyframePanel.getSequence(), time, entry), this::parseValue);

		titledBorder = BorderFactory.createTitledBorder(title);
//		setBorder(titledBorder);
//		setLayout(new MigLayout("fill, hidemode 1", "[]", "[][][]0[][]"));
		setLayout(new MigLayout("fill, hidemode 1, ins 0", "[grow]", "[grow]"));

		JPanel collapsablePCont = new JPanel(new MigLayout("fill, hidemode 1", "[]", "[][][]0[][]"));
		collapsablePanel = new CollapsablePanel(title, collapsablePCont);
		add(collapsablePanel, "growx, spanx");

		dynStatLayout = new CardLayout();
		dynStatPanel = new JPanel(dynStatLayout);
		collapsablePCont.add(dynStatPanel, "align center, growx, wrap");

		dynamicPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[][][][]"));
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

		dynamicContentPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[][][][]"));
		dynamicPanel.add(dynamicContentPanel, "spanx, growx, wrap, hidemode 3");

//		dynamicPanel.add(keyframePanel, "spanx, growx, wrap, hidemode 3");

		JPanel staticPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 3", "[grow][][]", "[]"));
		staticPanel.add(new JLabel("Static"), "al 0% 0%");
		JButton makeDynamicButton = new JButton("Make Dynamic");
		makeDynamicButton.addActionListener(e -> makeDynamic());
		staticPanel.add(makeDynamicButton, "al 100% 0%, wrap, hidemode 3");

		staticComponent = this.getStaticComponent();
		staticPanel.add(staticComponent, "spanx, wrap, hidemode 3");

		dynStatPanel.add("static", staticPanel);
	}

	private void makeStatic() {
		toggleStaticDynamicPanel(true);
		RemoveAnimFlagAction removeAnimFlagAction = new RemoveAnimFlagAction(timelineContainer, animFlag, changeListener);
		undoManager.pushAction(removeAnimFlagAction.redo());
		if (valueSettingFunction != null) {
			valueSettingFunction.accept(staticValue);
		}
	}

	private void makeDynamic() {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			if (staticValue == null) {
				staticValue = getZeroValue();
			}
			AnimFlag<T> newAnimFlag = getNewAnimFlag();

			if (newAnimFlag != null) {
				toggleStaticDynamicPanel(false);
				AddAnimFlagAction<T> addAnimFlagAction = new AddAnimFlagAction<>(timelineContainer, newAnimFlag, changeListener);
				undoManager.pushAction(addAnimFlagAction.redo());
			}
		} else if (animFlag != null) {
			toggleStaticDynamicPanel(false);
			AddAnimFlagAction<T> addAnimFlagAction = new AddAnimFlagAction<>(timelineContainer, animFlag, changeListener);
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

	protected abstract AnimFlag<T> getNewAnimFlag();

	private void toggleStaticDynamicPanel(boolean isStatic) {
		boolean isDynamic = !isStatic;
		if (isDynamic) {
			dynStatLayout.show(dynStatPanel, "dynamic");
		} else {
			dynStatLayout.show(dynStatPanel, "static");
		}
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

//		dynamicPanel.removeAll();
		dynamicContentPanel.removeAll();
		if (animFlag == null) {
			toggleStaticDynamicPanel(true);
			for (KeyframePanel<T> kfp : keyframePanelMap.values()) {
				kfp.updateFlag(null, null);
			}
			if (valueSettingFunction == null) {
				staticComponent.setVisible(false);
			} else {
				staticComponent.setVisible(true);
			}
			collapsablePanel.setTitle(title);
			titledBorder.setTitle(title);
//			keyframePanel.updateFlag(null, null);
		} else {
			TreeSet<Sequence> tempSet = new TreeSet<>(modelHandler.getModel().getAnims());
			tempSet.addAll(modelHandler.getModel().getGlobalSeqs());
			keyframePanelMap.keySet().removeIf(anim -> !tempSet.contains(anim));
			for(Sequence sequence : tempSet) {
//				KeyframePanel<T> keyframePanel1 = keyframePanelMap.computeIfAbsent(sequence, k -> new KeyframePanel<>(modelHandler.getModel(), this::addEntry, this::removeEntry, (time, entry) -> changeEntry(sequence, time, entry), this::parseValue));
				KeyframePanel<T> keyframePanel = keyframePanelMap.computeIfAbsent(sequence, k -> getKeyframePanel(sequence));
				keyframePanel.updateFlag(animFlag, sequence);

				int size = animFlag.size(sequence);
				CollapsablePanel collapsablePanel = new CollapsablePanel(sequence + " " + size + " keyframes", keyframePanel);
				collapsablePanel.setCollapsed(size == 0);
//				dynamicPanel.add(collapsablePanel, "spanx, growx, wrap, hidemode 3");
				dynamicContentPanel.add(collapsablePanel, "spanx, growx, wrap, hidemode 3");
			}
			if (animFlag.hasGlobalSeq()) {
				titledBorder.setTitle(title + " (GlobalSeq: " + animFlag.getGlobalSeq() + ")");
				collapsablePanel.setTitle(title + " (GlobalSeq: " + animFlag.getGlobalSeq() + ")");
			} else {
				collapsablePanel.setTitle(title);
				titledBorder.setTitle(title);
			}
			toggleStaticDynamicPanel(false);
			interpTypeBox.setSelectedItem(animFlag.getInterpolationType());
		}

		reloadStaticValue(value);
		revalidate();
		repaint();
	}

	private KeyframePanel<T> getKeyframePanel(Sequence sequence) {
		return new KeyframePanel<>(modelHandler.getModel(),
				this::addEntry,
				this::removeEntry,
				(time, entry) -> changeEntry(sequence, time, entry),
				this::parseValue);
	}

	abstract JComponent getStaticComponent();

	abstract void reloadStaticValue(T value);

	private void setInterpolationType(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED
				&& e.getItem() instanceof InterpolationType
				&& e.getItem() != animFlag.getInterpolationType()) {
			UndoAction undoAction = new ChangeInterpTypeAction<>(animFlag, (InterpolationType) e.getItem(), changeListener);
			undoManager.pushAction(undoAction.redo());

		}
	}

	private void addEntry(Sequence sequence, int row) {
		if (animFlag == null && timelineContainer != null && !flagName.equals("")) {
			AnimFlag<T> flag = getNewAnimFlag();
			if (flag != null) {
				UndoAction undoAction = new AddAnimFlagAction<>(timelineContainer, flag, changeListener);
				undoManager.pushAction(undoAction.redo());
			}
		} else if (animFlag != null) {
			Entry<T> newEntry;
			T zeroValue = getZeroValue();
			if (staticValue == null) {
				staticValue = zeroValue;
			}
			if (animFlag.getEntryMap(sequence) == null || animFlag.getEntryMap(sequence).isEmpty()) {
				if (animFlag.getInterpolationType().tangential()) {
					newEntry = new Entry<>(0, staticValue, zeroValue, zeroValue);
				} else {
					newEntry = new Entry<>(0, staticValue);
				}
			} else {
				int time = animFlag.getTimeFromIndex(sequence, row);
				newEntry = animFlag.getEntryAt(sequence, time).deepCopy();

				while (animFlag.hasEntryAt(sequence, newEntry.getTime())) {
					newEntry.setTime(newEntry.getTime() + 1);
				}
			}

			UndoAction undoAction = new AddFlagEntryAction<>(animFlag, newEntry, sequence, changeListener);
			undoManager.pushAction(undoAction.redo());

		}
	}

	abstract T getZeroValue();

	abstract T parseValue(String valueString);

	private void removeEntry(Sequence sequence, int orgTime) {
		if (animFlag.hasEntryAt(sequence, orgTime)) {
			UndoAction undoAction = new RemoveFlagEntryAction<>(animFlag, orgTime, sequence, changeListener);
			undoManager.pushAction(undoAction.redo());
		}
	}

	protected void changeEntry(Sequence sequence, int row, String field, String val) {
//		int orgTime = (int) keyframePanel.getFloatTrackTableModel().getValueAt(row, 0);
		int orgTime = (int) keyframePanelMap.get(sequence).getFloatTrackTableModel().getValueAt(row, 0);
		Entry<T> newEntry = animFlag.getEntryAt(sequence, orgTime).deepCopy();
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

		UndoAction undoAction = new ChangeFlagEntryAction<>(animFlag, newEntry, orgTime, sequence, changeListener);
		undoManager.pushAction(undoAction.redo());
	}

	protected void changeEntry(Sequence sequence, Integer orgTime, Entry<T> newEntry) {
		UndoAction undoAction = new ChangeFlagEntryAction<>(animFlag, newEntry, orgTime, sequence, changeListener);
		undoManager.pushAction(undoAction.redo());
	}

	private int parseTime(String val) {
		String intString = val.replaceAll("[^\\d]", "");//.split("\\.")[0];
		intString = intString.substring(0, Math.min(intString.length(), 10));
		return Integer.parseInt(intString);
	}

}
