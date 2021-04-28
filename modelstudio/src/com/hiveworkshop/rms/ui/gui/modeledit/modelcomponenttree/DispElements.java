package com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import java.util.ArrayList;

public class DispElements {
	static ChooseableDummyItem getDummyItem(ModelView modelViewManager, String sequences) {
		return new ChooseableDummyItem(modelViewManager, sequences);
	}

	public static ChooseableDisplayElement<?> getIdObjectElement(ModelView modelViewManager, IdObject idObject) {
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

		public ChooseableModelRoot(ModelView modelViewManager, EditableModel item) {
			super(MODEL_ROOT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(EditableModel item, ModelView modelViewManager) {
			return "Model \"" + item.getHeaderName() + "\"";
		}
	}

	static final class ChooseableModelComment extends ChooseableDisplayElement<ArrayList<String>> {
		private static final ImageIcon COMMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("comment.png"));

		public ChooseableModelComment(ModelView modelViewManager, ArrayList<String> item) {
			super(COMMENT_ICON, modelViewManager, item);
//			System.out.println("(ModelComponentBrowserTree) model comment: " + item);
		}

		@Override
		protected String getName(ArrayList<String> item, ModelView modelViewManager) {
			return "Comment";
		}
	}

	static final class ChooseableModelHeader extends ChooseableDisplayElement<EditableModel> {
		private static final ImageIcon DATA_ICON = new ImageIcon(RMSIcons.loadNodeImage("model.png"));

		public ChooseableModelHeader(ModelView modelViewManager, EditableModel item) {
			super(DATA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(EditableModel item, ModelView modelViewManager) {
			return "Header";
		}
	}

	static final class ChooseableAnimationItem extends ChooseableDisplayElement<Animation> {

		private static final ImageIcon ANIMATION_ICON = new ImageIcon(RMSIcons.loadNodeImage("animation.png"));

		public ChooseableAnimationItem(ModelView modelViewManager, Animation item) {
			super(ANIMATION_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Animation item, ModelView modelViewManager) {
			return "Anim \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableGlobalSequenceItem extends ChooseableDisplayElement<Integer> {
		private static final ImageIcon GLOBAL_SEQ_ICON = new ImageIcon(RMSIcons.loadNodeImage("globalseq.png"));

		private final int globalSeqId;

		public ChooseableGlobalSequenceItem(ModelView modelViewManager, Integer item, int globalSeqId) {
			super(GLOBAL_SEQ_ICON, modelViewManager, globalSeqId);
//			super(GLOBAL_SEQ_ICON, modelViewManager, item);
			this.globalSeqId = globalSeqId;
		}

		@Override
		protected String getName(Integer item, ModelView modelViewManager) {
			return "GlobalSequence " + globalSeqId + ": Duration " + item;
		}

		@Override
		public boolean hasSameItem(ChooseableDisplayElement<?> other) {
			return (other != null) && (other.getClass() == getClass())
					&& (globalSeqId == ((ChooseableGlobalSequenceItem) other).globalSeqId);
		}
	}

	static final class ChooseableBitmapItem extends ChooseableDisplayElement<Bitmap> {

		private static final ImageIcon TEXTURE_ICON = new ImageIcon(RMSIcons.loadNodeImage("bitmap.png"));

		public ChooseableBitmapItem(ModelView modelViewManager, Bitmap item, int id) {
			super(TEXTURE_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(Bitmap item, ModelView modelViewManager) {
			return "Bitmap \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableMaterialItem extends ChooseableDisplayElement<Material> {
		private static int num = 0;
		private int thisNum;

		private static final ImageIcon MATERIAL_ICON = new ImageIcon(RMSIcons.loadNodeImage("material.png"));

		public ChooseableMaterialItem(ModelView modelViewManager, Material item, int id) {
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
		protected String getName(Material item, ModelView modelViewManager) {
			//\u2116 №
//			return "Material \u2116" + (thisNum);
//			return "\u2116 " + (thisNum) + " " + item.getName();
			return "# " + (thisNum) + " " + item.getName();
		}
	}

	static final class ChooseableTextureAnimItem extends ChooseableDisplayElement<TextureAnim> {

		private static final ImageIcon TEXTURE_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("textureanim.png"));

		public ChooseableTextureAnimItem(ModelView modelViewManager, TextureAnim item, int id) {
			super(TEXTURE_ANIM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(TextureAnim item, ModelView modelViewManager) {
			return "TextureAnim " + modelViewManager.getModel().getTexAnims().indexOf(item);
		}
	}

	private static final class ChooseableBoneItem extends ChooseableDisplayElement<Bone> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("bone.png"));

		public ChooseableBoneItem(ModelView modelViewManager, Bone item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Bone item, ModelView modelViewManager) {
			return "Bone \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableHelperItem extends ChooseableDisplayElement<Helper> {
		private static final ImageIcon BONE_ICON = new ImageIcon(RMSIcons.loadNodeImage("helperhand.png"));

		public ChooseableHelperItem(ModelView modelViewManager, Helper item) {
			super(BONE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Helper item, ModelView modelViewManager) {
			return "Helper \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableLightItem extends ChooseableDisplayElement<Light> {
		private static final ImageIcon LIGHT_ICON = new ImageIcon(RMSIcons.loadNodeImage("light.png"));

		public ChooseableLightItem(ModelView modelViewManager, Light item) {
			super(LIGHT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Light item, ModelView modelViewManager) {
			return "Light \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableAttachmentItem extends ChooseableDisplayElement<Attachment> {
		private static final ImageIcon ATTACHMENT_ICON = new ImageIcon(RMSIcons.loadNodeImage("attachment.png"));

		public ChooseableAttachmentItem(ModelView modelViewManager, Attachment item) {
			super(ATTACHMENT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Attachment item, ModelView modelViewManager) {
			return "Attachment \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterItem extends ChooseableDisplayElement<ParticleEmitter> {
		private static final ImageIcon PARTICLE_ICON = new ImageIcon(RMSIcons.loadNodeImage("particle1.png"));

		public ChooseableParticleEmitterItem(ModelView modelViewManager, ParticleEmitter item) {
			super(PARTICLE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(ParticleEmitter item, ModelView modelViewManager) {
			return "ParticleEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitter2Item extends ChooseableDisplayElement<ParticleEmitter2> {
		private static final ImageIcon PARTICLE2_ICON = new ImageIcon(RMSIcons.loadNodeImage("particle2.png"));

		public ChooseableParticleEmitter2Item(ModelView modelViewManager, ParticleEmitter2 item) {
			super(PARTICLE2_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(ParticleEmitter2 item, ModelView modelViewManager) {
			return "ParticleEmitter2 \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableParticleEmitterPopcornItem extends ChooseableDisplayElement<ParticleEmitterPopcorn> {
		private static final ImageIcon POPCORN_ICON = new ImageIcon(RMSIcons.loadNodeImage("popcorn.png"));

		public ChooseableParticleEmitterPopcornItem(ModelView modelViewManager, ParticleEmitterPopcorn item) {
			super(POPCORN_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(ParticleEmitterPopcorn item, ModelView modelViewManager) {
			return "ParticleEmitterPopcorn \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableRibbonEmitterItem extends ChooseableDisplayElement<RibbonEmitter> {
		private static final ImageIcon RIBBON_ICON = new ImageIcon(RMSIcons.loadNodeImage("ribbon.png"));

		public ChooseableRibbonEmitterItem(ModelView modelViewManager, RibbonEmitter item) {
			super(RIBBON_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(RibbonEmitter item, ModelView modelViewManager) {
			return "RibbonEmitter \"" + item.getName() + "\"";
		}
	}

	private static final class ChooseableEventObjectItem extends ChooseableDisplayElement<EventObject> {
		private static final ImageIcon EVENT_OBJECT_ICON = new ImageIcon(RMSIcons.loadNodeImage("event.png"));

		public ChooseableEventObjectItem(ModelView modelViewManager, EventObject item) {
			super(EVENT_OBJECT_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(EventObject item, ModelView modelViewManager) {
			return item.getDispString();
		}
	}

	private static final class ChooseableCollisionShapeItem extends ChooseableDisplayElement<CollisionShape> {
		private static final ImageIcon COLLISION_SHAPE_ICON = new ImageIcon(RMSIcons.loadNodeImage("collision.png"));

		public ChooseableCollisionShapeItem(ModelView modelViewManager, CollisionShape item) {
			super(COLLISION_SHAPE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(CollisionShape item, ModelView modelViewManager) {
			return "CollisionShape \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableGeosetAnimItem extends ChooseableDisplayElement<GeosetAnim> {
		private static final ImageIcon GEOSET_ANIM_ICON = new ImageIcon(RMSIcons.loadNodeImage("geoanim.png"));

		public ChooseableGeosetAnimItem(ModelView modelViewManager, GeosetAnim item, int id) {
			super(GEOSET_ANIM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(GeosetAnim item, ModelView modelViewManager) {
			return "GeosetAnim " + modelViewManager.getModel().getGeosetAnims().indexOf(item);
		}
	}

	static final class ChooseableCameraItem extends ChooseableDisplayElement<Camera> {
		private static final ImageIcon CAMERA_ICON = new ImageIcon(RMSIcons.loadNodeImage("camera.png"));

		public ChooseableCameraItem(ModelView modelViewManager, Camera item) {
			super(CAMERA_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(Camera item, ModelView modelViewManager) {
			return "Camera \"" + item.getName() + "\"";
		}
	}

	static final class ChooseableFaceEffectsChunkItem extends ChooseableDisplayElement<FaceEffect> {
		private static final ImageIcon FACEFX_ICON = new ImageIcon(RMSIcons.loadNodeImage("fafx.png"));

		public ChooseableFaceEffectsChunkItem(ModelView modelViewManager, FaceEffect item, int id) {
			super(FACEFX_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(FaceEffect item, ModelView modelViewManager) {
			return "FaceFX \"" + item.getFaceEffectTarget() + "\"";
		}
	}

	static final class ChooseableBindPoseChunkItem extends ChooseableDisplayElement<BindPose> {
		private static final ImageIcon BINDPOSE_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(RMSIcons.loadNodeImage("bindpos.png")));

		public ChooseableBindPoseChunkItem(ModelView modelViewManager, BindPose item) {
			super(BINDPOSE_ICON, modelViewManager, item);
		}

		@Override
		protected String getName(BindPose item, ModelView modelViewManager) {
			return "BindPose";
		}
	}

	static final class ChooseableDummyItem extends ChooseableDisplayElement<Void> {
		private static final ImageIcon GROUP_ICON = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp")));
		private static final ImageIcon GROUP_ICON_EXPANDED = new ImageIcon(IconUtils.worldEditStyleIcon(
				BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp")));
		private final String name2;

		public ChooseableDummyItem(ModelView modelViewManager, String name) {
			super(GROUP_ICON, modelViewManager, null);
			name2 = name;
		}

		@Override
		protected String getName(Void item, ModelView modelViewManager) {
			return name2;
		}

		@Override
		public ImageIcon getIcon(boolean expanded) {
			return expanded ? GROUP_ICON_EXPANDED : GROUP_ICON;
		}

		@Override
		public boolean hasSameItem(ChooseableDisplayElement<?> other) {
			return (other instanceof ChooseableDummyItem) && ((ChooseableDummyItem) other).name2.equals(name2);
		}
	}

	static final class ChooseableGeosetItem extends ChooseableDisplayElement<Geoset> {
		private static final ImageIcon GEOSET_ITEM_ICON = new ImageIcon(RMSIcons.loadNodeImage("geoset.png"));

		public ChooseableGeosetItem(ModelView modelViewManager, Geoset item, int id) {
			super(GEOSET_ITEM_ICON, modelViewManager, item, id);
		}

		@Override
		protected String getName(Geoset item, ModelView modelViewManager) {
//			String numberName = item.getName();
			String numberName = "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
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
		private final ModelView modelViewManager;
		private ChooseableDisplayElement<?> element;
		private final UndoActionListener undoActionListener;
		private final ModelStructureChangeListener modelStructureChangeListener;

		public IdObjectToChooseableElementWrappingConverter(ModelView modelViewManager,
		                                                    UndoActionListener undoActionListener,
		                                                    ModelStructureChangeListener modelStructureChangeListener) {
			this.modelViewManager = modelViewManager;
			this.undoActionListener = undoActionListener;
			this.modelStructureChangeListener = modelStructureChangeListener;
		}

		public ChooseableDisplayElement<?> getElement() {
			return element;
		}

		@Override
		public void visitIdObject(IdObject object) {
			if (object instanceof Light) {
				element = new ChooseableLightItem(modelViewManager, (Light) object);
			} else if (object instanceof Helper) {
				element = new ChooseableHelperItem(modelViewManager, (Helper) object);
			} else if (object instanceof Bone) {
				element = new ChooseableBoneItem(modelViewManager, (Bone) object);
			} else if (object instanceof Attachment) {
				element = new ChooseableAttachmentItem(modelViewManager, (Attachment) object);
			} else if (object instanceof ParticleEmitter) {
				element = new ChooseableParticleEmitterItem(modelViewManager, (ParticleEmitter) object);
			} else if (object instanceof ParticleEmitter2) {
				element = new ChooseableParticleEmitter2Item(modelViewManager, (ParticleEmitter2) object);
			} else if (object instanceof ParticleEmitterPopcorn) {
				element = new ChooseableParticleEmitterPopcornItem(modelViewManager, (ParticleEmitterPopcorn) object);
			} else if (object instanceof RibbonEmitter) {
				element = new ChooseableRibbonEmitterItem(modelViewManager, (RibbonEmitter) object);
			} else if (object instanceof EventObject) {
				element = new ChooseableEventObjectItem(modelViewManager, (EventObject) object);
			} else if (object instanceof CollisionShape) {
				element = new ChooseableCollisionShapeItem(modelViewManager, (CollisionShape) object);
			}
		}

		@Override
		public void camera(Camera camera) {
			element = new ChooseableCameraItem(modelViewManager, camera);
		}
	}
}
