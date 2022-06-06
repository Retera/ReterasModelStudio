package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class IdObjectChooserButton extends JButton {
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private final IdObjectChooser idObjectChooser;
	private final JComponent parent;
	private String buttonText = "Choose Bone Chain Start";
	private IdObject chosenIdObject = null;
	private Function<IdObject, ImageIcon> iconFunction;
	private Runnable updateFunction;

	public IdObjectChooserButton(EditableModel model, JComponent parent){
		super();
		setText(buttonText);
		this.parent = parent;
		idObjectChooser = new IdObjectChooser(model);
		HashSet<Class<?>> classSet = new HashSet<>();
		classSet.add(Bone.class);
		classSet.add(Helper.class);
		idObjectChooser.setClassSet(classSet);
		if(model != null){
			iconFunction = o -> iconHandler.getImageIcon(o, model);
			setIcon(iconHandler.getImageIcon(model));
		}
		addActionListener(e -> chooseIdObject());
	}

	public IdObjectChooserButton(EditableModel model, HashSet<Class<?>> classSet, JComponent parent){
		super();
		setText(buttonText);
		this.parent = parent;
		idObjectChooser = new IdObjectChooser(model);
		idObjectChooser.setClassSet(classSet);
		if(model != null){
			iconFunction = o -> iconHandler.getImageIcon(o, model);
			setIcon(iconHandler.getImageIcon(model));
		}
		addActionListener(e -> chooseIdObject());
	}


	protected void chooseIdObject() {
		chosenIdObject = idObjectChooser.chooseObject(chosenIdObject, parent);
		if (chosenIdObject != null) {
			setText(chosenIdObject.getName());
		} else {
			setText(buttonText);
		}
		if(iconFunction != null){
			setIcon(iconFunction.apply(chosenIdObject));
		}
		if(updateFunction != null){
			updateFunction.run();
		}
	}

	public IdObjectChooserButton setButtonText(String text) {
		this.buttonText = text;
		this.setText(text);
		return this;
	}

	public Bone getChosenBone() {
		if(chosenIdObject instanceof Bone){

			return (Bone) chosenIdObject;
		}
		return null;
	}
	public IdObject getChosenIdObject() {
		return chosenIdObject;
	}

	public IdObjectChooserButton setUpdateFunction(Runnable updateFunction) {
		this.updateFunction = updateFunction;
		return this;
	}

	public IdObjectChooserButton setClassSet(Set<Class<?>> classSet) {
		idObjectChooser.setClassSet(classSet);
		return this;
	}

	public Runnable getUpdateFunction() {
		return updateFunction;
	}
}
