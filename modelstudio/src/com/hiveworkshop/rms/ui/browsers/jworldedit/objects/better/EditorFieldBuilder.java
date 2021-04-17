package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better;

import java.util.List;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;

public interface EditorFieldBuilder {
	List<EditableOnscreenObjectField> buildFields(ObjectData metaData, MutableGameObject gameObject);
}
