package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hiveworkshop.wc3.gui.modeledit.ModelComponentListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentAttachmentCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentBindPoseCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentBoneCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentCameraCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentCollisionShapeCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentEventObjectCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentFaceEffectCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentGeosetAnimCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentGeosetCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentHelperCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentLightCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentParticleEmitter2Card;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentParticleEmitterCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentPopcornCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentRibbonEmitterCard;
import com.hiveworkshop.wc3.gui.modeledit.components.cards.ComponentTextureAnimCard;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.gui.modeledit.util.TextureExporter;
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

/**
 * The right hand side of the Model tab: one editor card per component type,
 * switched by the browser tree's selection.
 */
public class ComponentsPanel extends JPanel implements ModelComponentListener {
	private static final String BLANK = "BLANK";
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private static final String BITMAP = "BITMAP";
	private static final String MATERIAL = "MATERIAL";
	private static final String TEXTURE_ANIM = "TEXTURE_ANIM";
	private static final String GEOSET = "GEOSET";
	private static final String GEOSET_ANIM = "GEOSET_ANIM";
	private static final String BONE = "BONE";
	private static final String LIGHT = "LIGHT";
	private static final String HELPER = "HELPER";
	private static final String ATTACHMENT = "ATTACHMENT";
	private static final String PARTICLE = "PARTICLE";
	private static final String PARTICLE2 = "PARTICLE2";
	private static final String POPCORN = "POPCORN";
	private static final String RIBBON = "RIBBON";
	private static final String EVENT = "EVENT";
	private static final String COLLISION = "COLLISION";
	private static final String CAMERA = "CAMERA";
	private static final String FACEFX = "FACEFX";
	private static final String BINDPOSE = "BINDPOSE";

	private final CardLayout cardLayout;
	private final JPanel blankPanel;
	private final ComponentHeaderPanel headerPanel;
	private final ComponentCommentPanel commentPanel;
	private final ComponentAnimationPanel animationPanel;
	private final ComponentGlobalSequencePanel globalSeqPanel;
	private final ComponentBitmapPanel bitmapPanel;
	private final ComponentMaterialPanel materialPanel;
	private final ComponentTextureAnimCard textureAnimCard = new ComponentTextureAnimCard();
	private final ComponentGeosetCard geosetCard = new ComponentGeosetCard();
	private final ComponentGeosetAnimCard geosetAnimCard = new ComponentGeosetAnimCard();
	private final ComponentBoneCard boneCard = new ComponentBoneCard();
	private final ComponentLightCard lightCard = new ComponentLightCard();
	private final ComponentHelperCard helperCard = new ComponentHelperCard();
	private final ComponentAttachmentCard attachmentCard = new ComponentAttachmentCard();
	private final ComponentParticleEmitterCard particleCard = new ComponentParticleEmitterCard();
	private final ComponentParticleEmitter2Card particle2Card = new ComponentParticleEmitter2Card();
	private final ComponentPopcornCard popcornCard = new ComponentPopcornCard();
	private final ComponentRibbonEmitterCard ribbonCard = new ComponentRibbonEmitterCard();
	private final ComponentEventObjectCard eventCard = new ComponentEventObjectCard();
	private final ComponentCollisionShapeCard collisionCard = new ComponentCollisionShapeCard();
	private final ComponentCameraCard cameraCard = new ComponentCameraCard();
	private final ComponentFaceEffectCard faceEffectCard = new ComponentFaceEffectCard();
	private final ComponentBindPoseCard bindPoseCard = new ComponentBindPoseCard();
	private final ComponentCard<?>[] cards = { textureAnimCard, geosetCard, geosetAnimCard, boneCard, lightCard,
			helperCard, attachmentCard, particleCard, particle2Card, popcornCard, ribbonCard, eventCard,
			collisionCard, cameraCard, faceEffectCard, bindPoseCard };
	private ComponentPanel currentPanel;
	private ComponentCard<?> currentCard;

	public ComponentsPanel(final TextureExporter textureExporter) {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		blankPanel = new JPanel();
		blankPanel.add(new JLabel("Select a model component to get started..."));
		add(blankPanel, BLANK);
		headerPanel = new ComponentHeaderPanel();
		add(headerPanel, HEADER);
		commentPanel = new ComponentCommentPanel();
		add(commentPanel, COMMENT);
		animationPanel = new ComponentAnimationPanel();
		add(animationPanel, ANIMATION);
		globalSeqPanel = new ComponentGlobalSequencePanel();
		add(globalSeqPanel, GLOBALSEQ);
		bitmapPanel = new ComponentBitmapPanel(textureExporter);
		add(bitmapPanel, BITMAP);
		materialPanel = new ComponentMaterialPanel();
		add(scrolling(materialPanel), MATERIAL);
		add(scrolling(textureAnimCard), TEXTURE_ANIM);
		add(scrolling(geosetCard), GEOSET);
		add(scrolling(geosetAnimCard), GEOSET_ANIM);
		add(scrolling(boneCard), BONE);
		add(scrolling(lightCard), LIGHT);
		add(scrolling(helperCard), HELPER);
		add(scrolling(attachmentCard), ATTACHMENT);
		add(scrolling(particleCard), PARTICLE);
		add(scrolling(particle2Card), PARTICLE2);
		add(scrolling(popcornCard), POPCORN);
		add(scrolling(ribbonCard), RIBBON);
		add(scrolling(eventCard), EVENT);
		add(scrolling(collisionCard), COLLISION);
		add(scrolling(cameraCard), CAMERA);
		add(scrolling(faceEffectCard), FACEFX);
		add(scrolling(bindPoseCard), BINDPOSE);
		cardLayout.show(this, BLANK);
	}

	private static JScrollPane scrolling(final JComponent component) {
		final JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	public void setNavigationListener(final ModelComponentNavigationListener navigationListener) {
		for (final ComponentCard<?> card : cards) {
			card.setNavigationListener(navigationListener);
		}
		materialPanel.setNavigationListener(navigationListener);
	}

	/**
	 * Re-reads the visible card from the model; called after undo/redo and
	 * after any component property change.
	 */
	public void reloadCurrentCard() {
		if (currentCard != null) {
			currentCard.reload();
		}
	}

	public Object getCurrentItem() {
		return currentCard == null ? null : currentCard.getItem();
	}

	private <T> void show(final ComponentCard<T> card, final String key, final T item,
			final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		card.setItem(item, modelViewManager, undoActionListener, modelStructureChangeListener);
		cardLayout.show(this, key);
		currentPanel = null;
		currentCard = card;
	}

	@Override
	public void selected(final EditableModel model) {
	}

	@Override
	public void selectedHeaderData(final EditableModel model, final ModelViewManager modelViewManager,
			final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener) {
		headerPanel.setActiveModel(modelViewManager, undoListener, modelStructureChangeListener);
		cardLayout.show(this, HEADER);
		currentPanel = headerPanel;
		currentCard = null;
	}

	@Override
	public void selectedHeaderComment(final Iterable<String> comment) {
		commentPanel.setCommentContents(comment);
		cardLayout.show(this, COMMENT);
		currentPanel = commentPanel;
		currentCard = null;
	}

	@Override
	public void selected(final Animation animation, final UndoActionListener undoListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		animationPanel.setAnimation(animation, undoListener, modelStructureChangeListener);
		cardLayout.show(this, ANIMATION);
		currentPanel = null;
		currentCard = null;
	}

	@Override
	public void selected(final EditableModel model, final Integer globalSequence, final int globalSequenceId,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoActionListener,
				modelStructureChangeListener);
		cardLayout.show(this, GLOBALSEQ);
		currentPanel = null;
		currentCard = null;
	}

	@Override
	public void selected(final Bitmap texture, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		bitmapPanel.setBitmap(texture, modelViewManager, undoActionListener, modelStructureChangeListener);
		cardLayout.show(this, BITMAP);
		currentPanel = bitmapPanel;
		currentCard = null;
	}

	@Override
	public void selected(final Material material, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		materialPanel.setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
		cardLayout.show(this, MATERIAL);
		currentPanel = materialPanel;
		currentCard = null;
	}

	@Override
	public void selected(final Layer layer, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		// NOTE: for now this should never get called, and is only used in Tracks view
	}

	@Override
	public void selected(final TextureAnim textureAnim, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(textureAnimCard, TEXTURE_ANIM, textureAnim, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final Geoset geoset, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(geosetCard, GEOSET, geoset, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final GeosetAnim geosetAnim, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(geosetAnimCard, GEOSET_ANIM, geosetAnim, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final Bone object, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(boneCard, BONE, object, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final Light light, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(lightCard, LIGHT, light, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final Helper object, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(helperCard, HELPER, object, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final Attachment attachment, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(attachmentCard, ATTACHMENT, attachment, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final ParticleEmitter particleEmitter, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(particleCard, PARTICLE, particleEmitter, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final ParticleEmitter2 particleEmitter, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(particle2Card, PARTICLE2, particleEmitter, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final ParticleEmitterPopcorn popcornFxEmitter, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(popcornCard, POPCORN, popcornFxEmitter, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final RibbonEmitter particleEmitter, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(ribbonCard, RIBBON, particleEmitter, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final EventObject eventObject, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(eventCard, EVENT, eventObject, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final CollisionShape collisionShape, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(collisionCard, COLLISION, collisionShape, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final Camera camera, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(cameraCard, CAMERA, camera, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void selected(final FaceEffect faceEffectsChunk, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(faceEffectCard, FACEFX, faceEffectsChunk, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selected(final BindPoseChunk bindPoseChunk, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		show(bindPoseCard, BINDPOSE, bindPoseChunk, modelViewManager, undoActionListener,
				modelStructureChangeListener);
	}

	@Override
	public void selectedBlank() {
		cardLayout.show(this, BLANK);
		currentPanel = null;
		currentCard = null;
	}
}
