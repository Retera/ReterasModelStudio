package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;
import com.hiveworkshop.rms.util.War3ID;

public final class MutableObjectDataChangeNotifier extends SubscriberSetNotifier<MutableObjectDataChangeListener>
		implements MutableObjectDataChangeListener {

	@Override
	public void textChanged(War3ID changedObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.textChanged(changedObject);
		}
	}

	@Override
	public void categoriesChanged(War3ID changedObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.categoriesChanged(changedObject);
		}
	}

	@Override
	public void iconsChanged(War3ID changedObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.iconsChanged(changedObject);
		}
	}

	@Override
	public void fieldsChanged(War3ID changedObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.fieldsChanged(changedObject);
		}
	}

	@Override
	public void modelChanged(War3ID changedObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.modelChanged(changedObject);
		}
	}

	@Override
	public void objectCreated(War3ID newObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.objectCreated(newObject);
		}
	}

	@Override
	public void objectsCreated(War3ID[] newObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.objectsCreated(newObject);
		}
	}

	@Override
	public void objectRemoved(War3ID newObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.objectRemoved(newObject);
		}
	}

	@Override
	public void objectsRemoved(War3ID[] newObject) {
		for (MutableObjectDataChangeListener listener : listenerSet) {
			listener.objectsRemoved(newObject);
		}
	}

}
