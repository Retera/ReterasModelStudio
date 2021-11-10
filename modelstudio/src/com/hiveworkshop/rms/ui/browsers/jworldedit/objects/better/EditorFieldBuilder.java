package com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better;

import com.hiveworkshop.rms.parsers.slk.ObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.better.fields.EditableOnscreenObjectField;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;

import java.util.List;

public interface EditorFieldBuilder {
	List<EditableOnscreenObjectField> buildFields(ObjectData metaData, MutableGameObject gameObject);
}
