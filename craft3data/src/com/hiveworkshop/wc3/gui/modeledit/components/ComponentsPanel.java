package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.ModelComponentBrowserTree;
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
import com.hiveworkshop.wc3.mdl.PopcornFxEmitter;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdx.BindPoseChunk;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk;

public class ComponentsPanel extends JPanel implements ModelComponentBrowserTree.ModelComponentListener {
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private final CardLayout cardLayout;
	private final ComponentHeaderPanel headerPanel;
	private final ComponentCommentPanel commentPanel;
	private final ComponentAnimationPanel animationPanel;
	private final ComponentGlobalSequencePanel globalSeqPanel;

	public ComponentsPanel() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		headerPanel = new ComponentHeaderPanel();
		add(headerPanel, HEADER);
		commentPanel = new ComponentCommentPanel();
		add(commentPanel, COMMENT);
		animationPanel = new ComponentAnimationPanel();
		add(animationPanel, ANIMATION);
		globalSeqPanel = new ComponentGlobalSequencePanel();
		add(globalSeqPanel, GLOBALSEQ);
		cardLayout.show(this, HEADER);
	}

	@Override
	public void selected(final MDL model) {
	}

	@Override
	public void selectedHeaderData(final MDL model) {
		headerPanel.setModelHeader(model);
		cardLayout.show(this, HEADER);
	}

	@Override
	public void selectedHeaderComment(final Iterable<String> comment) {
		commentPanel.setCommentContents(comment);
		cardLayout.show(this, COMMENT);
	}

	@Override
	public void selected(final Animation animation) {
		animationPanel.setAnimation(animation);
		cardLayout.show(this, ANIMATION);
	}

	@Override
	public void selected(final Integer globalSequence, final int globalSequenceId) {
		globalSeqPanel.setGlobalSequence(globalSequence, globalSequenceId);
		cardLayout.show(this, GLOBALSEQ);
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
	public void selected(final PopcornFxEmitter popcornFxEmitter) {
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
	public void selected(final FaceEffectsChunk faceEffectsChunk) {
	}

	@Override
	public void selected(final BindPoseChunk bindPoseChunk) {
	}
}
