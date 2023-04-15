package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;

public enum ShaderEditorType {
	SELECTION(ShaderManager.PipelineType.SELECTION, "Selection Shader Editor", "SelectionBox.vert", "SelectionBox.glsl", "SelectionBox.frag"),
	MESH(ShaderManager.PipelineType.MESH, "Mesh Shader Editor", "HDDiffuseVertColor.vert", "HDDiffuseVertColor.frag"),
	BONE(ShaderManager.PipelineType.BONE, "Node Shader Editor", "Bone.vert", "Bone.glsl", "Bone.frag"),
	VERT(ShaderManager.PipelineType.VERT, "Vertex Shader Editor", "VertexBoxesVC.vert", "VertexBoxes.glsl", "VertexBoxes.frag"),
	NORM(ShaderManager.PipelineType.NORM, "Normal Shader Editor", "NormalLines.vert", "NormalLines.glsl", "NormalLines.frag"),
	GRID(ShaderManager.PipelineType.GRID, "Grid Shader Editor", "Grid.vert", "Grid.glsl", "Grid.frag"),
	COLLISION(ShaderManager.PipelineType.COLLISION, "Collision Shader Editor", "ShapeOutline.vert", "ShapeOutline.glsl", "ShapeOutline.frag"),
	CAMERA(ShaderManager.PipelineType.CAMERA, "Camera Shader Editor", "Camera.vert", "Camera.frag"),
	PARTICLE2(ShaderManager.PipelineType.PARTICLE2, "Particle Shader Editor", "Particle.vert", "Particle.glsl", "Particle.frag"),
	RIBBON(ShaderManager.PipelineType.RIBBON, "Ribbon Shader Editor", "HDDiffuseVertColor.vert", "HDDiffuseVertColor.frag"),
	;
	ShaderManager.PipelineType pipelineType;
	String title;
	String[] shaders;

	ShaderEditorType(ShaderManager.PipelineType pipelineType, String title, String... shaders) {
		this.pipelineType = pipelineType;
		this.title = title;
		this.shaders = shaders;
	}

	public String getTitle() {
		return title;
	}

	public ShaderManager.PipelineType getPipelineType() {
		return pipelineType;
	}

	public String[] getShaders() {
		return shaders;
	}
}
