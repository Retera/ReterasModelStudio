package com.hiveworkshop.wc3.jworldedit.objects.better.fields.factory;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

public interface SingleFieldFactory {
	EditableOnscreenObjectField create(MutableGameObject gameObject, ObjectData metaData, War3ID metaKey, int level,
			MutableObjectData.WorldEditorDataType worldEditorDataType, boolean hasMoreThanOneLevel);
}
