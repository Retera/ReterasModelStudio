package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;

public interface IdObjectVisitor {

	void visitIdObject(IdObject idObject);

	void camera(Camera camera);
}
