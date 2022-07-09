package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.util.Vec2;

public class NodeBufferFiller {

	private RenderModel renderModel;
	private ModelView modelView;


	public NodeBufferFiller(){
	}


	public NodeBufferFiller setModel(RenderModel renderModel, ModelView modelView){
		this.renderModel = renderModel;
		this.modelView = modelView;
		return this;
	}

	public void fillBuffer(ShaderPipeline pipeline) {
		pipeline.prepare();
		Vec2 selStat = new Vec2();

		for (final IdObject idObject : modelView.getVisibleIdObjects()) {
			RenderNode2 renderNode = renderModel.getRenderNode(idObject);
			if (renderNode != null && modelView.shouldRender(idObject)) {
//				float nodeSize = (float) (1 * idObject.getClickRadius() / 2f);
				selStat.x = getSelectionStatus(idObject);
				selStat.y = getSelectionStatus(idObject.getParent());


				pipeline.addVert(renderNode.getPivot(), renderNode.getParentPivot(), renderNode.getWorldRotation(), selStat, null, renderNode.getWorldScale(), 0);

			}
		}
	}


	private int getSelectionStatus(IdObject idObject){
		if(modelView.getHighlightedNode() != null && modelView.getHighlightedNode() == idObject) {
			return 0;
		} else if(idObject != null && modelView.isEditable(idObject)){
			if (modelView.isSelected(idObject)) {
				return 1;
			} else {
				return 2;
			}
		}
//		else if (!modelView.isHidden(idObject)){
//		}
		return 3;
	}

}
