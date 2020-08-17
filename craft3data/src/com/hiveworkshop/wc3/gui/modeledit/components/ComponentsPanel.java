package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hiveworkshop.wc3.gui.modeledit.ModelComponentBrowserTree;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.BindPose;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.FaceEffect;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class ComponentsPanel extends JPanel implements ModelComponentBrowserTree.ModelComponentListener {
	private static final String BLANK = "BLANK";
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private static final String GLOBALSEQ = "GLOBALSEQ";
	private static final String BITMAP = "BITMAP";
	private static final String MATERIAL = "MATERIAL";
	private final CardLayout cardLayout;
	private final JPanel blankPanel;
	private final ComponentHeaderPanel headerPanel;
	private final ComponentCommentPanel commentPanel;
	private final ComponentAnimationPanel animationPanel;
	private final ComponentGlobalSequencePanel globalSeqPanel;
	private final ComponentBitmapPanel bitmapPanel;
	private final ComponentMaterialPanel materialPanel;
	private ComponentPanel currentPanel;

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
		add(new JScrollPane(materialPanel), MATERIAL);
		cardLayout.show(this, BLANK);
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
	public void selected(final EditableModel model, final Integer globalSequence, final int globalSequenceId,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		globalSeqPanel.setGlobalSequence(model, globalSequence, globalSequenceId, undoActionListener,
				modelStructureChangeListener);
		cardLayout.show(this, GLOBALSEQ);
		currentPanel = headerPanel;
	}

	@Override
	public void selected(final Bitmap texture, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		bitmapPanel.setBitmap(texture, modelViewManager, undoActionListener, modelStructureChangeListener);
		cardLayout.show(this, BITMAP);
		currentPanel = headerPanel;
	}

	@Override
	public void selected(final Material material, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		materialPanel.setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
		cardLayout.show(this, MATERIAL);
		currentPanel = materialPanel;
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
	public void selected(final BindPose bindPoseChunk) {
	}

	@Override
	public void selectedBlank() {
		cardLayout.show(this, BLANK);
		currentPanel = null;
	}
}
