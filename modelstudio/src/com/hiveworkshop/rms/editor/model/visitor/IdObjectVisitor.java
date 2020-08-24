package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;

public interface IdObjectVisitor {

	void bone(Bone object);

	void light(Light light);

	void helper(Helper object);

	void attachment(Attachment attachment);

	void particleEmitter(ParticleEmitter particleEmitter);

	void particleEmitter2(ParticleEmitter2 particleEmitter);

	void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter);

	void ribbonEmitter(RibbonEmitter particleEmitter);

	void eventObject(EventObject eventObject);

	void collisionShape(CollisionShape collisionShape);

	void camera(Camera camera);
}
