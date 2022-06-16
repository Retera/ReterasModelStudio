package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;

import javax.swing.*;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class IdObjectChooserButton extends JButton {
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private final IdObjectChooser idObjectChooser;
	private final JComponent parent;
	private String buttonText = "Choose Bone Chain Start";
	private IdObject chosenIdObject = null;
	private Function<IdObject, ImageIcon> iconFunction;
	private Runnable updateFunction;
	private Consumer<IdObject> idObjectConsumer;

	public IdObjectChooserButton(EditableModel model, JComponent parent){
		super();
		setText(buttonText);
		this.parent = parent;
		idObjectChooser = new IdObjectChooser(model);
		if(model != null){
			iconFunction = o -> iconHandler.getImageIcon(o, model);
			setIcon(iconHandler.getImageIcon(model));
		}
		addActionListener(e -> chooseIdObject());
	}

	public IdObjectChooserButton(EditableModel model, Set<Class<?>> classSet, JComponent parent){
		this(model, parent);
		idObjectChooser.setClassSet(classSet);
	}


	protected void chooseIdObject() {
		setChosenIdObject(idObjectChooser.chooseObject(chosenIdObject, parent));
		if(idObjectConsumer != null){
			idObjectConsumer.accept(chosenIdObject);
		}
		if(updateFunction != null){
			updateFunction.run();
		}
	}

	public IdObjectChooserButton setChosenIdObject(IdObject chosenIdObject) {
		this.chosenIdObject = chosenIdObject;
		if (chosenIdObject != null) {
			setText(chosenIdObject.getName());
		} else {
			setText(buttonText);
		}
		if(iconFunction != null){
			setIcon(iconFunction.apply(chosenIdObject));
		}
		return this;
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

	public IdObjectChooserButton setClasses(Class<?>... clazzes){
		idObjectChooser.setClasses(clazzes);
		return this;
	}

	public Runnable getUpdateFunction() {
		return updateFunction;
	}

	public IdObjectChooserButton setIdObjectConsumer(Consumer<IdObject> idObjectConsumer) {
		this.idObjectConsumer = idObjectConsumer;
		return this;
	}
}
