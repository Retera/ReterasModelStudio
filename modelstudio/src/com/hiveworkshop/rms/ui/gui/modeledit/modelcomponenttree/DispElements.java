package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import java.util.ArrayList;

public class DispElements {
	static ChooseableDummyItem getDummyItem(ModelViewManager modelViewManager, String sequences) {
		return new ChooseableDummyItem(modelViewManager, sequences);
	}

	public static ChooseableDisplayElement<?> getIdObjectElement(ModelViewManager modelViewManager, IdObject idObject) {
		return switch (idObject.getClass().getSimpleName()) {
			case "Bone" -> new ChooseableBoneItem(modelViewManager, (Bone) idObject);
			case "Light" -> new ChooseableLightItem(modelViewManager, (Light) idObject);
			case "Helper" -> new ChooseableHelperItem(modelViewManager, (Helper) idObject);
			case "Attachment" -> new ChooseableAttachmentItem(modelViewManager, (Attachment) idObject);
			case "ParticleEmitter" -> new ChooseableParticleEmitterItem(modelViewManager, (ParticleEmitter) idObject);
			case "ParticleEmitter2" -> new ChooseableParticleEmitter2Item(modelViewManager, (ParticleEmitter2) idObject);
			case "ParticleEmitterPopcorn" -> new ChooseableParticleEmitterPopcornItem(modelViewManager, (ParticleEmitterPopcorn) idObject);
			case "RibbonEmitter" -> new ChooseableRibbonEmitterItem(modelViewManager, (RibbonEmitter) idObject);
			case "EventObject" -> new ChooseableEventObjectItem(modelViewManager, (EventObject) idObject);
			case "CollisionShape" -> new ChooseableCollisionShapeItem(modelViewManager, (CollisionShape) idObject);
			default -> null;
		};

	}

	static final class ChooseableModelRoot extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon MODEL_ROOT_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-Trigger.blp")));

		public ChooseableModelRoot(final ModelViewManager modelViewManager, final EditableModel item) {
			super(MODEL_ROOT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Model \"" + item.getHeaderName() + "\"";
		}
	}

	static final class ChooseableModelComment extends ChooseableDisplayElement<ArrayList<String>> {
		private static final ImageIcon COMMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("comment.png"));

		public ChooseableModelComment(final ModelViewManager modelViewManager, final ArrayList<String> item) {
			super(COMMENT_ICON, modelViewManager, item);
			System.out.println("(ModelComponentBrowserTree) model comment: " + item);
		}

		@Override
		protected String getName(final ArrayList<String> item, final ModelViewManager modelViewManager) {
			return "Comment";
		}
	}

	static final class ChooseableModelHeader extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon DATA_ICON = new ImageIcon(RMSIcons.loadNodeImage("model.png"));

		public ChooseableModelHeader(final ModelViewManager modelViewManager, final EditableModel item) {
			super(DATA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final EditableModel item, final ModelViewManager modelViewManager) {
			return "Header";
		}
	}

	static final class ChooseableAnimationItem extends ChooseableDisplayElement<Animation> {

		private static final ImageIcon ANIMATION_ICON = new ImageIcon(RMSIcons.loadNodeImage("animation.png"));

		public ChooseableAnimationItem(final ModelViewManager modelViewManager, final Animation item) {
			super(ANIMATION_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Animation item, final ModelViewManager modelViewManager) {
			return "Anim \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableGlobalSequenceItem extends ChooseableDisplayElement<Integer> {
		private static final ImageIcon GLOBAL_SEQ_ICON = new ImageIcon(RMSIcons.loadNodeImage("globalseq.png"));

		private final int globalSeqId;

		public ChooseableGlobalSequenceItem(final ModelViewManager modelViewManager, final Integer item, final int globalSeqId) {
			super(GLOBAL_SEQ_ICON, modelViewManager, globalSeqId);
//			super(GLOBAL_SEQ_ICON, modelViewManager, item);
			this.globalSeqId = globalSeqId;
		}

		@Override
		protected String getName(final Integer item, final ModelViewManager modelViewManager) {
			return "GlobalSequence " + globalSeqId + ": Duration " + item;
		}

		@Override
		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (other != null) && (other.getClass() == getClass())
					&& (globalSeqId == ((ChooseableGlobalSequenceItem) other).globalSeqId);
		}
	}

	static final class ChooseableBitmapItem extends ChooseableDisplayElement<Bitmap> {

		private static final ImageIcon TEXTURE_ICON = new ImageIcon(RMSIcons.loadNodeImage("bitmap.png"));

		public ChooseableBitmapItem(final ModelViewManager modelViewManager, final Bitmap item, int id) {
			super(TEXTURE_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(final Bitmap item, final ModelViewManager modelViewManager) {
			return "Bitmap \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableMaterialItem extends ChooseableDisplayElement<Material> {
		private static int num = 0;
		private int thisNum;

		private static final ImageIcon MATERIAL_ICON = new ImageIcon(RMSIcons.loadNodeImage("material.png"));

		public ChooseableMaterialItem(final ModelViewManager modelViewManager, final Material item, int id) {
			super(MATERIAL_ICON, modelViewManager, item, id);
			thisNum = id;
//			thisNum = num;
//			num++;
//			int matNum = modelViewManager.getModel().getMaterials().size();
//			if(num>= matNum){
//				num=0;
//			}
		}

		@Override
		protected String getName(final Material item, final ModelViewManager modelViewManager) {
			//\u2116 №
//			return "Material \u2116" + (thisNum);
//			return "\u2116 " + (thisNum) + " " + item.getName();
			return "# " + (thisNum) + " " + item.getName();
		}
	}

	static final class ChooseableTextureAnimItem extends ChooseableDisplayElement<TextureAnim> {

		private static final ImageIcon TEXTURE_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("textureanim.png"));

		public ChooseableTextureAnimItem(final ModelViewManager modelViewManager, final TextureAnim item, int id) {
			super(TEXTURE_ANIM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(final TextureAnim item, final ModelViewManager modelViewManager) {
			return "TextureAnim " + modelViewManager.getModel().getTexAnims().indexOf(item);
		}
	}

	private static final class ChooseableBoneItem extends ChooseableDisplayElement<Bone> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("bone.png"));

		public ChooseableBoneItem(final ModelViewManager modelViewManager, final Bone item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Bone item, final ModelViewManager modelViewManager) {
			return "Bone \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableHelperItem extends ChooseableDisplayElement<Helper> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("helperhand.png"));

		public ChooseableHelperItem(final ModelViewManager modelViewManager, final Helper item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Helper item, final ModelViewManager modelViewManager) {
			return "Helper \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableLightItem extends ChooseableDisplayElement<Light> {
		private static final ImageIcon LIGHT_ICON = new ImageIcon(RMSIcons.loadNodeImage("light.png"));

		public ChooseableLightItem(final ModelViewManager modelViewManager, final Light item) {
			super(LIGHT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Light item, final ModelViewManager modelViewManager) {
			return "Light \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableAttachmentItem extends ChooseableDisplayElement<Attachment> {
		private static final ImageIcon ATTACHMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("attachment.png"));

		public ChooseableAttachmentItem(final ModelViewManager modelViewManager, final Attachment item) {
			super(ATTACHMENT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Attachment item, final ModelViewManager modelViewManager) {
			return "Attachment \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterItem extends ChooseableDisplayElement<ParticleEmitter> {
		private static final ImageIcon PARTICLE_ICON = new ImageIcon(RMSIcons.loadNodeImage("particle1.png"));

		public ChooseableParticleEmitterItem(final ModelViewManager modelViewManager, final ParticleEmitter item) {
			super(PARTICLE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitter item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitter2Item extends ChooseableDisplayElement<ParticleEmitter2> {
		private static final ImageIcon PARTICLE2_ICON = new ImageIcon(RMSIcons.loadNodeImage("particle2.png"));

		public ChooseableParticleEmitter2Item(final ModelViewManager modelViewManager, final ParticleEmitter2 item) {
			super(PARTICLE2_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitter2 item, final ModelViewManager modelViewManager) {
			return "ParticleEmitter2 \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterPopcornItem extends ChooseableDisplayElement<ParticleEmitterPopcorn> {
		private static final ImageIcon POPCORN_ICON = new ImageIcon(RMSIcons.loadNodeImage("popcorn.png"));

		public ChooseableParticleEmitterPopcornItem(final ModelViewManager modelViewManager, final ParticleEmitterPopcorn item) {
			super(POPCORN_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final ParticleEmitterPopcorn item, final ModelViewManager modelViewManager) {
			return "ParticleEmitterPopcorn \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableRibbonEmitterItem extends ChooseableDisplayElement<RibbonEmitter> {
		private static final ImageIcon RIBBON_ICON = new ImageIcon(RMSIcons.loadNodeImage("ribbon.png"));

		public ChooseableRibbonEmitterItem(final ModelViewManager modelViewManager, final RibbonEmitter item) {
			super(RIBBON_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final RibbonEmitter item, final ModelViewManager modelViewManager) {
			return "RibbonEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableEventObjectItem extends ChooseableDisplayElement<EventObject> {
		private static final ImageIcon EVENT_OBJECT_ICON = new ImageIcon(RMSIcons.loadNodeImage("event.png"));

		public ChooseableEventObjectItem(final ModelViewManager modelViewManager, final EventObject item) {
			super(EVENT_OBJECT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(EventObject item, ModelViewManager modelViewManager) {
			return item.getDispString();
		}
	}

	private static final class ChooseableCollisionShapeItem extends ChooseableDisplayElement<CollisionShape> {
		private static final ImageIcon COLLISION_SHAPE_ICON = new ImageIcon(RMSIcons.loadNodeImage("collision.png"));

		public ChooseableCollisionShapeItem(final ModelViewManager modelViewManager, final CollisionShape item) {
			super(COLLISION_SHAPE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final CollisionShape item, final ModelViewManager modelViewManager) {
			return "CollisionShape \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableGeosetAnimItem extends ChooseableDisplayElement<GeosetAnim> {
		private static final ImageIcon GEOSET_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("geoanim.png"));

		public ChooseableGeosetAnimItem(final ModelViewManager modelViewManager, final GeosetAnim item, int id) {
			super(GEOSET_ANIM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(final GeosetAnim item, final ModelViewManager modelViewManager) {
			return "GeosetAnim " + modelViewManager.getModel().getGeosetAnims().indexOf(item);
		}
	}

	static final class ChooseableCameraItem extends ChooseableDisplayElement<Camera> {
		private static final ImageIcon CAMERA_ICON = new ImageIcon(RMSIcons.loadNodeImage("camera.png"));

		public ChooseableCameraItem(final ModelViewManager modelViewManager, final Camera item) {
			super(CAMERA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final Camera item, final ModelViewManager modelViewManager) {
			return "Camera \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableFaceEffectsChunkItem extends ChooseableDisplayElement<FaceEffect> {
		private static final ImageIcon FACEFX_ICON = new ImageIcon(RMSIcons.loadNodeImage("fafx.png"));

		public ChooseableFaceEffectsChunkItem(final ModelViewManager modelViewManager, final FaceEffect item, int id) {
			super(FACEFX_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(final FaceEffect item, final ModelViewManager modelViewManager) {
			return "FaceFX \"" + item.getFaceEffectTarget() + "\"";
		}
	}

	static final class ChooseableBindPoseChunkItem extends ChooseableDisplayElement<BindPose> {
		private static final ImageIcon BINDPOSE_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadNodeImage("bindpos.png")));

		public ChooseableBindPoseChunkItem(final ModelViewManager modelViewManager, final BindPose item) {
			super(BINDPOSE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(final BindPose item, final ModelViewManager modelViewManager) {
			return "BindPose";
		}
	}

	static final class ChooseableDummyItem extends ChooseableDisplayElement<Void> {
		private static final ImageIcon GROUP_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
		private static final ImageIcon GROUP_ICON_EXPANDED = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
		private final String name2;

		public ChooseableDummyItem(final ModelViewManager modelViewManager, final String name) {
			super(GROUP_ICON, modelViewManager, null);
			name2 = name;
		}

		@Override
		protected String getName(final Void item, final ModelViewManager modelViewManager) {
			return name2;
		}

		@Override
		public ImageIcon getIcon(final boolean expanded) {
			return expanded ? GROUP_ICON_EXPANDED : GROUP_ICON;
		}

		@Override
		public boolean hasSameItem(final ChooseableDisplayElement<?> other) {
			return (other instanceof ChooseableDummyItem) && ((ChooseableDummyItem) other).name2.equals(name2);
		}
	}

	static final class ChooseableGeosetItem extends ChooseableDisplayElement<Geoset> {
		private static final ImageIcon GEOSET_ITEM_ICON = new ImageIcon(RMSIcons.loadNodeImage("geoset.png"));

		public ChooseableGeosetItem(final ModelViewManager modelViewManager, final Geoset item, int id) {
			super(GEOSET_ITEM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(final Geoset item, final ModelViewManager modelViewManager) {
//			final String numberName = item.getName();
			final String numberName = "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
			if ((item.getLevelOfDetailName() != null) && (item.getLevelOfDetailName().length() > 0)) {
				return numberName + ": " + item.getLevelOfDetailName();
			}
			return numberName;
		}

		@Override
		public void mouseEntered() {
			modelViewManager.highlightGeoset(item);
		}

		@Override
		public void mouseExited() {
			modelViewManager.unhighlightGeoset(item);
		}
	}

	static final class IdObjectToChooseableElementWrappingConverter implements IdObjectVisitor {
		private final ModelViewManager modelViewManager;
		private ChooseableDisplayElement<?> element;
		private final UndoActionListener undoActionListener;
		private final ModelStructureChangeListener modelStructureChangeListener;

		public IdObjectToChooseableElementWrappingConverter(final ModelViewManager modelViewManager,
		                                                    final UndoActionListener undoActionListener,
		                                                    final ModelStructureChangeListener modelStructureChangeListener) {
			this.modelViewManager = modelViewManager;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
		}

		public ChooseableDisplayElement<?> getElement() {
			return element;
		}

		@Override
		public void bone(final Bone object) {
			element = new ChooseableBoneItem(modelViewManager, object);
		}

		@Override
		public void light(final Light light) {
			element = new ChooseableLightItem(modelViewManager, light);
		}

		@Override
		public void helper(final Helper object) {
			element = new ChooseableHelperItem(modelViewManager, object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			element = new ChooseableAttachmentItem(modelViewManager, attachment);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			element = new ChooseableParticleEmitterItem(modelViewManager, particleEmitter);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			element = new ChooseableParticleEmitter2Item(modelViewManager, particleEmitter);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			element = new ChooseableParticleEmitterPopcornItem(modelViewManager, popcornFxEmitter);
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			element = new ChooseableRibbonEmitterItem(modelViewManager, particleEmitter);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			element = new ChooseableEventObjectItem(modelViewManager, eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			element = new ChooseableCollisionShapeItem(modelViewManager, collisionShape);
		}

		@Override
		public void camera(final Camera camera) {
			element = new ChooseableCameraItem(modelViewManager, camera);
		}
	}
}
