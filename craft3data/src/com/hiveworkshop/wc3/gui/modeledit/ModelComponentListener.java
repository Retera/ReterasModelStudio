package com.hiveworkshop.wc3.gui.modeledit;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.BindPoseChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;

public interface ModelComponentListener {

	void selectedBlank();

	void selected(EditableModel model);

	void selectedHeaderData(EditableModel model, ModelViewManager modelViewManager, UndoActionListener undoListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selectedHeaderComment(Iterable<String> comment);

	void selected(Animation animation, UndoActionListener undoListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(EditableModel model, Integer globalSequence, int globalSequenceId,
			UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener);

	void selected(Bitmap texture, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Material material, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Layer layer, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(TextureAnim textureAnim);

	void selected(Geoset geoset);

	void selected(GeosetAnim geosetAnim);

	void selected(Bone object);

	void selected(Light light);

	void selected(Helper object);

	void selected(Attachment attachment);

	void selected(ParticleEmitter particleEmitter);

	void selected(ParticleEmitter2 particleEmitter);

	void selected(ParticleEmitterPopcorn popcornFxEmitter);

	void selected(RibbonEmitter particleEmitter);

	void selected(EventObject eventObject);

	void selected(CollisionShape collisionShape);

	void selected(Camera camera);

	void selected(FaceEffect faceEffectsChunk);

	void selected(BindPoseChunk bindPoseChunk);
}