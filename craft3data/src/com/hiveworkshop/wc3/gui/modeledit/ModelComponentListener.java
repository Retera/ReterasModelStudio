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

	void selected(TextureAnim textureAnim, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Geoset geoset, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(GeosetAnim geosetAnim, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Bone object, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Light light, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Helper object, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Attachment attachment, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(ParticleEmitter particleEmitter, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(ParticleEmitter2 particleEmitter, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(ParticleEmitterPopcorn popcornFxEmitter, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(RibbonEmitter particleEmitter, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(EventObject eventObject, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(CollisionShape collisionShape, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(Camera camera, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(FaceEffect faceEffectsChunk, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);

	void selected(BindPoseChunk bindPoseChunk, ModelViewManager modelViewManager, UndoActionListener undoActionListener,
			ModelStructureChangeListener modelStructureChangeListener);
}