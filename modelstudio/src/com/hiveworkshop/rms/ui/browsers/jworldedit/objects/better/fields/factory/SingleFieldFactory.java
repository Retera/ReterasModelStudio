package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.util.War3ID;

public interface SingleFieldFactory {
	EditableOnscreenObjectField create(MutableGameObject gameObject, ObjectData metaData, War3ID metaKey, int level,
	                                   WorldEditorDataType worldEditorDataType, boolean hasMoreThanOneLevel);
}
