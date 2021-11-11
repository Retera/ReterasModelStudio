package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorDataChangeListener;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;
import com.hiveworkshop.rms.util.War3ID;

public final class MutableObjectDataChangeNotifier extends SubscriberSetNotifier<UnitEditorDataChangeListener> {

	public void textChanged(War3ID changedObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.textChanged(changedObject);
		}
	}

	public void categoriesChanged(War3ID changedObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.categoriesChanged(changedObject);
		}
	}

	public void iconsChanged(War3ID changedObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.iconsChanged(changedObject);
		}
	}

	public void fieldsChanged(War3ID changedObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.fieldsChanged(changedObject);
		}
	}

	public void modelChanged(War3ID changedObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.modelChanged(changedObject);
		}
	}

	public void objectCreated(War3ID newObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.objectCreated(newObject);
		}
	}

	public void objectsCreated(War3ID[] newObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.objectsCreated(newObject);
		}
	}

	public void objectRemoved(War3ID newObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.objectRemoved(newObject);
		}
	}

	public void objectsRemoved(War3ID[] newObject) {
		for (UnitEditorDataChangeListener listener : listenerSet) {
			listener.objectsRemoved(newObject);
		}
	}

}
