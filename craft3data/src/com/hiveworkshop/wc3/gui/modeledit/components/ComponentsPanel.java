package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.ModelComponentBrowserTree;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.mdx.BindPoseChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;

public class ComponentsPanel extends JPanel implements ModelComponentBrowserTree.ModelComponentListener {
	private static final String BLANK = "BLANK";
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private final CardLayout cardLayout;
	private final JPanel blankPanel;
	private final ComponentHeaderPanel headerPanel;
	private final ComponentCommentPanel commentPanel;
	private final ComponentAnimationPanel animationPanel;
	private final ComponentGlobalSequencePanel globalSeqPanel;
	private ComponentPanel currentPanel;

	public ComponentsPanel() {
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
		cardLayout.show(this, BLANK);
	}

	@Override
	public void selected(final MDL model) {
	}

	@Override
	public void selectedHeaderData(final MDL model, final ModelViewManager modelViewManager,
			final UndoActionListener undoListener, final ModelStructureChangeListener modelStructureChangeListener) {
		headerPanel.setActiveModel(modelViewManager, undoListener, modelStructureChangeListener);
		cardLayout.show(this, HEADER);
		currentPanel = headerPanel;
	}

	@Override
	public void selectedHeaderComment(final Iterable<String> comment) {
		commentPanel.setCommentContents(comment);
		cardLayout.show(this, COMMENT);
		currentPanel = headerPanel;
	}

	@Override
	public void selected(final Animation animation, final UndoActionListener undoListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		animationPanel.setAnimation(animation, undoListener, modelStructureChangeListener);
		cardLayout.show(this, ANIMATION);
		currentPanel = headerPanel;
	}

	@Override
	public void selected(final MDL model, final Integer globalSequence, final int globalSequenceId,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoActionListener,
				modelStructureChangeListener);
		cardLayout.show(this, GLOBALSEQ);
		currentPanel = headerPanel;
	}

	@Override
	public void selected(final Bitmap texture) {
	}

	@Override
	public void selected(final Material material) {
	}

	@Override
	public void selected(final TextureAnim textureAnim) {
	}

	@Override
	public void selected(final Geoset geoset) {
	}

	@Override
	public void selected(final GeosetAnim geosetAnim) {
	}

	@Override
	public void selected(final Bone object) {
	}

	@Override
	public void selected(final Light light) {
	}

	@Override
	public void selected(final Helper object) {
	}

	@Override
	public void selected(final Attachment attachment) {
	}

	@Override
	public void selected(final ParticleEmitter particleEmitter) {
	}

	@Override
	public void selected(final ParticleEmitter2 particleEmitter) {
	}

	@Override
	public void selected(final ParticleEmitterPopcorn popcornFxEmitter) {
	}

	@Override
	public void selected(final RibbonEmitter particleEmitter) {
	}

	@Override
	public void selected(final EventObject eventObject) {
	}

	@Override
	public void selected(final CollisionShape collisionShape) {
	}

	@Override
	public void selected(final Camera camera) {
	}

	@Override
	public void selected(final FaceEffect faceEffectsChunk) {
	}

	@Override
	public void selected(final BindPoseChunk bindPoseChunk) {
	}
}
