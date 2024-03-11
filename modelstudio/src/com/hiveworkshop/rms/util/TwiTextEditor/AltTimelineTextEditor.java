package com.hiveworkshop.rms.util.TwiTextEditor;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.function.Function;

public class AltTimelineTextEditor<T> extends JPanel {
	private final ModelHandler modelHandler;
	private final TimelineContainer node;
	private final AnimFlag<T> animFlag;
	private final Function<String, T> parseFunction;
	private Sequence currSequence;
	private String valueRegex = "[eE\\d-.]+";
	private String weedingRegex = "[^-\\d,.eE]";

	JFrame frame;
	TitledBorder border;

	private final JPanel mainPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));



	public AltTimelineTextEditor(Sequence sequence, AnimFlag<T> animFlag, Function<String, T> parseFunction, TimelineContainer node, ModelHandler modelHandler){
		super(new MigLayout("fill", "[grow]", "[][grow]"));
		this.animFlag = animFlag;
		this.parseFunction = parseFunction;
		this.modelHandler = modelHandler;
		this.currSequence = sequence;
		this.node = node;
		TwiComboBox<Sequence> sequences = new TwiComboBox<>(new Animation("Prototype prototype", 0, 10000));
		sequences.setNewLinkedModelOf(modelHandler.getModel().getAllSequences());
		sequences.setSelectedItem(sequence);
		sequences.addOnSelectItemListener(this::setSequence);

		sequences.addMouseWheelListener(e -> sequences.incIndex(e.getWheelRotation()));
		sequences.setAllowLastToFirst(true);

		border = new TitledBorder("");
		mainPanel.setBorder(border);
		add(sequences, "growx, wrap");
		add(mainPanel, "growx, growy, spanx");
	}

	public AltTimelineTextEditor<T> setRegexStuff(String valueRegex, String weedingRegex){
		this.valueRegex = valueRegex;
		this.weedingRegex = weedingRegex;
		return this;
	}

	public void setSequence(Sequence sequence){
		currSequence = sequence;
		TimelineTextEditor<T> currEditor = new TimelineTextEditor<>(animFlag, sequence, parseFunction, node, modelHandler);
		mainPanel.removeAll();
		mainPanel.add(currEditor.getEditorPanel(), "growx, growy");
		border.setTitle(sequence + " (" + sequence.getLength() + ")");
		currEditor.setRegexStuff(valueRegex, weedingRegex).setFrameKeybinds(frame);
		frame.revalidate();
	}


	public void showWindow(){
		setPreferredSize(ScreenInfo.getSmallWindow());
		String nameString = node instanceof Named named ? named.getName() : node.getClass().getSimpleName();
		frame = FramePopup.show(this, null, nameString + " - [" + animFlag.getName() + "]");
		setSequence(currSequence);
	}
}
