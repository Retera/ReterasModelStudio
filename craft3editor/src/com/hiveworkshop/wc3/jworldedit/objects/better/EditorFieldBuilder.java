package com.hiveworkshop.wc3.jworldedit.objects.better;

import java.util.List;

import com.hiveworkshop.wc3.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.wc3.units.ObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;

public interface EditorFieldBuilder {
	List<EditableOnscreenObjectField> buildFields(ObjectData metaData, MutableGameObject gameObject);
}
