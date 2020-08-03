package com.hiveworkshop.wc3.mdl.v2.visitor;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;

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
