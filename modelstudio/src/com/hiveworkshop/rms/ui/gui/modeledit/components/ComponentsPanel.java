package com.hiveworkshop.rms.ui.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelComponentBrowserTree;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TextureExporter;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.editor.model.BindPose;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.FaceEffect;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

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
