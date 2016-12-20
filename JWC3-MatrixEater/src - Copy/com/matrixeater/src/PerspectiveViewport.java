package com.matrixeater.src;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHT1;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LIGHT_MODEL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NORMALIZE;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glLightModel;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class PerspectiveViewport extends AWTGLCanvas implements MouseListener,
		ActionListener, MouseWheelListener {
	MDLDisplay dispMDL;
	Vertex cameraPos = new Vertex(0,0,0);
	double m_zoom = 1;
	Point lastClick;
	Point leftClickStart;
	Point actStart;
	Timer clickTimer = new Timer(16, this);
	boolean mouseInBounds = false;
	JPopupMenu contextMenu;
	JMenuItem reAssignMatrix;
	JMenuItem cogBone;
	boolean enabled = false;
	
	boolean texLoaded = false;

	JCheckBox wireframe;
	HashMap<Bitmap,Integer> textureMap = new HashMap<Bitmap,Integer>();
	public PerspectiveViewport(MDLDisplay dispMDL) throws LWJGLException {
		super();
		// Dimension 1 and Dimension 2, these specify which dimensions to
		// display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z
		// dimensions
		//
		// Viewport border
		// setBorder(BorderFactory.createBevelBorder(1));
		setBackground(new Color(255, 255, 255));
//		setMinimumSize(new Dimension(200, 200));
//		 add(Box.createHorizontalStrut(200));
//		 add(Box.createVerticalStrut(200));
//		 setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		this.dispMDL = dispMDL;
		addMouseListener(this);
		addMouseWheelListener(this);

		contextMenu = new JPopupMenu();
		reAssignMatrix = new JMenuItem("Re-assign Matrix");
		reAssignMatrix.addActionListener(this);
		contextMenu.add(reAssignMatrix);
		cogBone = new JMenuItem("Auto-Center Bone(s)");
		cogBone.addActionListener(this);
		contextMenu.add(cogBone);
	}
	
	public void setWireframeHandler(JCheckBox nwireframe)
	{
		wireframe = nwireframe;
	}

    private float[] whiteDiffuse = { 1f,1f,1f,1f};
    private float[] posSun = {0.0f, 10.0f ,0.0f, 1.0f }; 

    boolean wantReload = false;
    public void reloadTextures()
    {
    	wantReload = true;
    }
    boolean wantReloadAll = false;
    public void reloadAllTextures()
    {
    	wantReloadAll = true;
    }
    public void forceReloadTextures()
    {
    	texLoaded = true;
//    	for( Bitmap tex: textureMap.keySet())
//    	{
//    		//GL11.glDeleteTextures(textureMap.get(tex));
//    	}
//    	initGL();

		for (Geoset geo : dispMDL.getMDL().m_geosets) {//.getMDL().m_geosets
			for( int i = 0; i < geo.material.layers.size(); i++ )
			{
				Bitmap tex = geo.material.layers.get(i).firstTexture();
				if( textureMap.get(tex) == null )
				{
					String path = tex.getPath();
					if( path.length() == 0 )
					{
						if( tex.getReplaceableId() == 1 )
						{
							path = "ReplaceableTextures\\TeamColor\\TeamColor00";
						}
						else if( tex.getReplaceableId() == 2 )
						{
							path = "ReplaceableTextures\\TeamGlow\\TeamGlow00";
						}
					}
					else
						path = path.substring(0,path.length()-4);
					Integer texture = null;
					try {
						texture = loadTexture(BLPHandler.get().getGameTex(path+".blp"));
					}
					catch (Exception exc)
					{
						exc.printStackTrace();
						texture = loadTexture(BLPHandler.get().getCustomTex(dispMDL.getMDL().getFile().getParent()+"\\"+path+".blp"));//TextureLoader.getTexture("TGA", new FileInputStream(new File(dispMDL.getMDL().getFile().getParent()+"\\"+path+".tga"))).getTextureID();

//						try {					} catch (FileNotFoundException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
					if( texture != null )
					{
						textureMap.put(tex, texture);
//						textureMapCID.put(tex, geo.material.layers.get(i).getCoordId());
						//texture.bind();
					}
				}
			}
		}
    }
    
    public void addGeosets(List<Geoset> geosets)
    {
		for (Geoset geo : geosets) {//.getMDL().m_geosets
			for( int i = 0; i < geo.material.layers.size(); i++ )
			{
				Bitmap tex = geo.material.layers.get(i).firstTexture();
				String path = tex.getPath();
				if( path.length() == 0 )
				{
					if( tex.getReplaceableId() == 1 )
					{
						path = "ReplaceableTextures\\TeamColor\\TeamColor00";
					}
					else if( tex.getReplaceableId() == 2 )
					{
						path = "ReplaceableTextures\\TeamGlow\\TeamGlow00";
					}
				}
				else
					path = path.substring(0,path.length()-4);
				Integer texture = null;
				try {
					texture = loadTexture(BLPHandler.get().getGameTex(path+".blp"));
				}
				catch (Exception exc)
				{
					exc.printStackTrace();
					texture = loadTexture(BLPHandler.get().getCustomTex(dispMDL.getMDL().getFile().getParent()+"\\"+path+".blp"));//TextureLoader.getTexture("TGA", new FileInputStream(new File(dispMDL.getMDL().getFile().getParent()+"\\"+path+".tga"))).getTextureID();

//					try {					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
				if( texture != null )
				{
					textureMap.put(tex, texture);
//					textureMapCID.put(tex, geo.material.layers.get(i).getCoordId());
					//texture.bind();
				}
			}
		}
    }
    
    @Override
	public void initGL() {

    	if( MainFrame.panel.textureModels() )
    	{
    		texLoaded = true;
    		for (Geoset geo : dispMDL.getMDL().m_geosets) {//.getMDL().m_geosets
    			for( int i = 0; i < geo.material.layers.size(); i++ )
    			{
    				Bitmap tex = geo.material.layers.get(i).firstTexture();
    				String path = tex.getPath();
    				if( path.length() == 0 )
    				{
    					if( tex.getReplaceableId() == 1 )
    					{
    						path = "ReplaceableTextures\\TeamColor\\TeamColor00";
    					}
    					else if( tex.getReplaceableId() == 2 )
    					{
    						path = "ReplaceableTextures\\TeamGlow\\TeamGlow00";
    					}
    				}
    				else
    					path = path.substring(0,path.length()-4);
    				Integer texture = null;
    				try {
    					texture = loadTexture(BLPHandler.get().getGameTex(path+".blp"));
    				}
    				catch (Exception exc)
    				{
    					exc.printStackTrace();
    					texture = loadTexture(BLPHandler.get().getCustomTex(dispMDL.getMDL().getFile().getParent()+"\\"+path+".blp"));//TextureLoader.getTexture("TGA", new FileInputStream(new File(dispMDL.getMDL().getFile().getParent()+"\\"+path+".tga"))).getTextureID();

//    					try {					} catch (FileNotFoundException e) {
//    						// TODO Auto-generated catch block
//    						e.printStackTrace();
//    					} catch (IOException e) {
//    						// TODO Auto-generated catch block
//    						e.printStackTrace();
//    					}
    				}
    				if( texture != null )
    				{
    					textureMap.put(tex, texture);
//						textureMapCID.put(tex, geo.material.layers.get(i).getCoordId());
    					//texture.bind();
    				}
    			}
    		}
    	}
//		try {
////			Display.setDisplayMode(new DisplayMode(800, 600));
////			Display.create();
//		} catch (LWJGLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		GL11.glMatrixMode(GL11.GL_PROJECTION);
//		GL11.glLoadIdentity();
//		GL11.glOrtho(0, 800, 0, 600, 300, -300);
//		GL11.glMatrixMode(GL11.GL_MODELVIEW);
    	
    	
//        GL11.glShadeModel(GL11.GL_SMOOTH);
//        GL11.glClearColor(0.5f,0.5f,0.5f,0.0f);         // black background
//        GL11.glClearDepth(1.0f);                        // depth of 0 to 1
//        GL11.glEnable(GL11.GL_DEPTH_TEST);              // enable depth testing        
//        GL11.glDepthFunc(GL11.GL_LEQUAL);        
//        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,GL11.GL_NICEST);
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        FloatBuffer buffer = ByteBuffer.allocateDirect(whiteDiffuse.length*8).asFloatBuffer();
//        buffer.put(whiteDiffuse);
//        GL11.glLight(GL11.GL_LIGHT0,GL11.GL_DIFFUSE,buffer);
//        FloatBuffer buffer2 = ByteBuffer.allocateDirect(posSun.length*8).asFloatBuffer();
//        buffer2.put(posSun);
//        GL11.glLight(GL11.GL_LIGHT0,GL11.GL_POSITION,buffer2);
//        GL11.glEnable(GL11.GL_LIGHT0);
//        GL11.glEnable(GL11.GL_LIGHTING);
//        GL11.glViewport(0,0,getWidth(),getHeight());        
//        GL11.glMatrixMode(GL11.GL_PROJECTION);
//        GL11.glLoadIdentity();
//        // a nice 45° perspective
////        GLU.gluPerspective(45.0f,(float)width/(float)height,0.1f,fov);
//        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        GL11.glLoadIdentity();
	}

	public void setPosition(double a, double b) {
		cameraPos.x = a;
		cameraPos.y = b;
	}

	public void translate(double a, double b) {
		cameraPos.x += a;
		cameraPos.y += b;
	}

	public void zoom(double amount) {
		m_zoom *= (1 + amount);
	}

	public double getZoomAmount() {
		return m_zoom;
	}

	public Point2D.Double getDisplayOffset() {
		return new Point2D.Double(cameraPos.x, cameraPos.y);
	}

	// public byte getPortFirstXYZ()
	// {
	// return m_d1;
	// }
	// public byte getPortSecondXYZ()
	// {
	// return m_d2;
	// }
	public BufferedImage getBufferedImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		// paintComponent(image.getGraphics(),5);
		return image;
	}

	boolean initialized = false;
    int current_height; 
    int current_width; 
    private float xangle; 
    private float yangle; 
    
    public boolean renderTextures()
    {
    	return texLoaded && MainFrame.panel.textureModels();
    }
    
    public void paintGL() {
//    	setSize(getParent().getSize());
    	if( wantReloadAll )
    	{
    		wantReloadAll = false;
    		wantReload = false;//If we just reloaded all, no need to reload some.
    		try {
    			initGL();//Re-overwrite textures
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			ExceptionPopup.display("Error loading textures:", e);
    		}
    	}
    	else if( wantReload )
    	{
    		wantReload = false;
    		try {
    			forceReloadTextures();
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			ExceptionPopup.display("Error loading new texture:", e);
    		}
    	}
    	else if( !texLoaded && MainFrame.panel.textureModels() )
    	{
			forceReloadTextures();
			texLoaded = true;
    	}
        try { 
            if (getWidth() != current_width || getHeight() != current_height) { 
                current_width = getWidth(); 
                current_height = getHeight(); 
                glViewport(0, 0, current_width, current_height); 
            }
            if(  MainFrame.panel.viewMode() == 0 )//wireframe.isSelected() )
            	glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
            else if(  MainFrame.panel.viewMode() == 1 )
            	glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
            glViewport(0, 0, getWidth(), getHeight()); 
            glEnable(GL_DEPTH_TEST);
            
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            glEnable(GL_COLOR_MATERIAL);
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glEnable(GL_LIGHT1);
            glEnable(GL_NORMALIZE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
           // System.out.println("max: "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
            if( renderTextures() )
            	glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            glClearColor(0.3137254901960784f, 0.3137254901960784f, 0.3137254901960784f, 1.0f);
//            glClearColor(0f, 0f, 0f, 1.0f);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(45f, (float)current_width/(float)current_height, 1.0f, 600.0f); 
            //GLU.gluOrtho2D(45f, (float)current_width/(float)current_height, 1.0f, 600.0f);
//            glRotatef(angle, 0, 0, 0);
//            glClearColor(1.0f, 1.0f, 0.0f, 1.0f); 
//            gluOrtho2D(0.0f, (float) getWidth(), 0.0f, (float) getHeight()); 
//            GLU. 
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
            glMatrixMode(GL_MODELVIEW); 
            glLoadIdentity();
//            GL11.glShadeModel(GL11.GL_SMOOTH);

            glTranslatef(0f+(float)cameraPos.x*(float)m_zoom,-70f-(float)cameraPos.y*(float)m_zoom,-200f-(float)cameraPos.z*(float)m_zoom);
            glRotatef(yangle,1f,0f,0f);
            glRotatef(xangle,0f,1f,0f);
            glScalef((float)m_zoom,(float)m_zoom,(float)m_zoom);
            
            FloatBuffer ambientColor = BufferUtils.createFloatBuffer(4);
            ambientColor.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
//            float [] ambientColor = {0.2f, 0.2f, 0.2f, 1f};
//            FloatBuffer buffer = ByteBuffer.allocateDirect(ambientColor.length*8).asFloatBuffer();
//            buffer.put(ambientColor).flip();
            glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientColor);


            FloatBuffer lightColor0 = BufferUtils.createFloatBuffer(4);
            lightColor0.put(0.5f).put(0.5f).put(0.5f).put(1f).flip();
            FloatBuffer lightPos0 = BufferUtils.createFloatBuffer(4);
            lightPos0.put(40.0f).put(100.0f).put(80.0f).put(1f).flip();
            glLight(GL_LIGHT0, GL_DIFFUSE, lightColor0);
            glLight(GL_LIGHT0, GL_POSITION, lightPos0);
            

            FloatBuffer lightColor1 = BufferUtils.createFloatBuffer(4);
            lightColor1.put(0.2f).put(0.2f).put(0.2f).put(1f).flip();
            FloatBuffer lightPos1 = BufferUtils.createFloatBuffer(4);
            lightPos1.put(-100.0f).put(100.5f).put(0.5f).put(1f).flip();
            
            glLight(GL_LIGHT1, GL_DIFFUSE, lightColor1);
            glLight(GL_LIGHT1, GL_POSITION, lightPos1);
            

//            glColor3f(1f,1f,0f);
//            glColorMaterial ( GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE ) ;
//            glEnable(GL_COLOR_MATERIAL);
            glColor4f(0.5882352941176471f,0.5882352941176471f,1f,0.3f);
//            glPushMatrix();
//            glTranslatef(getWidth() / 2.0f, getHeight() / 2.0f, 0.0f); 
//            glRotatef(2*angle, 0f, 0f, -1.0f); 
//            glRectf(-50.0f, -50.0f, 50.0f, 50.0f);
			for (Geoset geo : dispMDL.visibleGeosets) {//.getMDL().m_geosets
				if( !dispMDL.editableGeosets.contains(geo) && dispMDL.highlight != geo )
				{
					for( int i = 0; i < geo.material.layers.size(); i++ )
					{
						Layer layer = geo.material.layers.get(i);
						Bitmap tex = layer.firstTexture();
						Integer texture = textureMap.get(tex);
						if( texture != null )
						{
							//texture.bind();
				            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				            if( renderTextures() )
				            GL11.glEnable(GL11.GL_TEXTURE_2D);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
						}
						else
						{
				            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COLOR);
				            GL11.glDisable(GL11.GL_TEXTURE_2D);
						}
						if( layer.getFilterMode().equals("Additive") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
						}
						else if( layer.getFilterMode().equals("AddAlpha") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
						}
						else
						{
							//GL11.glEnable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(true);
				            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}
						glBegin(GL11.GL_TRIANGLES); 
						for (Triangle tri : geo.m_triangle) {
							for (GeosetVertex v : tri.m_verts) {
								GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
								GL11.glTexCoord2f((float) v.tverts.get(v.tverts.size()-1-layer.getCoordId()).x, (float) v.tverts.get(v.tverts.size()-1-layer.getCoordId()).y);
								GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
							}
						}
//						if( texture != null )
//						{
//							texture.release();
//						}
						glEnd(); 
					}
				}
			}
            glColor3f(2f,2f,2f);
			for (Geoset geo : dispMDL.editableGeosets) {//.getMDL().m_geosets
				if( dispMDL.highlight != geo )
				{
					for( int i = 0; i < geo.material.layers.size(); i++ )
					{
						Layer layer = geo.material.layers.get(i);
						Bitmap tex = layer.firstTexture();
						Integer texture = textureMap.get(tex);
						if( texture != null )
						{
							//texture.bind();
							GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
						}
						if( layer.getFilterMode().equals("Additive") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
						}
						else if( layer.getFilterMode().equals("AddAlpha") )
						{
							//GL11.glDisable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(false);
							GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
						}
						else
						{
							//GL11.glEnable(GL11.GL_DEPTH_TEST);
				            GL11.glDepthMask(true);
				            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						}
						glBegin(GL11.GL_TRIANGLES); 
						for (Triangle tri : geo.m_triangle) {
							for (GeosetVertex v : tri.m_verts) {
								GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
								GL11.glTexCoord2f((float) v.tverts.get(v.tverts.size()-1-layer.getCoordId()).x, (float) v.tverts.get(v.tverts.size()-1-layer.getCoordId()).y);
								GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
							}
						}
//						if( texture != null )
//						{
//							texture.release();
//						}
						glEnd(); 
					}
				}
			}
            GL11.glDepthMask(true);
           // System.out.println("max: "+GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
            if( dispMDL.highlight != null)
            {
//				for( int i = 0; i < dispMDL.highlight.material.layers.size(); i++ )
//				{
	            	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COLOR);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
//					Layer layer = dispMDL.highlight.material.layers.get(i);
//					Bitmap tex = layer.firstTexture();
//					Texture texture = textureMap.get(tex);
//					if( texture != null )
//					{
//						texture.bind();
//						//GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture.getTextureID());
//					}
//					if( layer.getFilterMode().equals("Additive") )
//					{
//						//GL11.glDisable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(false);
//						GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
//					}
//					else if( layer.getFilterMode().equals("AddAlpha") )
//					{
//						//GL11.glDisable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(false);
//						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//					}
//					else
//					{
//						//GL11.glEnable(GL11.GL_DEPTH_TEST);
//			            GL11.glDepthMask(true);
//			            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//					}
		            glColor3f(1f, 3f,1f);
					glBegin(GL11.GL_TRIANGLES); 
					for (Triangle tri : dispMDL.highlight.m_triangle) {
						for (GeosetVertex v : tri.m_verts) {
							GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
							GL11.glTexCoord2f((float) v.tverts.get(0).x, (float) v.tverts.get(0).y);
							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
						}
					}
					glEnd();
//				}
            }

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
			if( MainFrame.panel.showNormals() )
			{
				glBegin(GL11.GL_LINES); 
	            glColor3f(1f, 1f, 3f);
//	            if( wireframe.isSelected() )
				for (Geoset geo : dispMDL.visibleGeosets) {//.getMDL().m_geosets
					for (Triangle tri : geo.m_triangle) {
						for (GeosetVertex v : tri.m_verts) {
							GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);

							GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
							GL11.glVertex3f((float) v.y/1.0f+(float)(v.normal.y*6/m_zoom), (float) v.z/1.0f+(float) (v.normal.z*6/m_zoom), (float) v.x/1.0f+(float) (v.normal.x*6/m_zoom));
						}	
					}
				}
	            

//	        	glPolygonMode( GL_FRONT, GL_POINTS );
//				for (Geoset geo : dispMDL.visibleGeosets) {//.getMDL().m_geosets
//					if( !dispMDL.editableGeosets.contains(geo) && dispMDL.highlight != geo )
//					for (Triangle tri : geo.m_triangle) {
//						for (GeosetVertex v : tri.m_verts) {
//							if( dispMDL.selection.contains(v))
//					            glColor3f(1f, 0f, 0f);
//							else
//					            glColor3f(0f, 0f, 0f);
//							GL11.glNormal3f((float) v.normal.y, (float) v.normal.z, (float) v.normal.x);
//							GL11.glVertex3f((float) v.y/1.0f, (float) v.z/1.0f, (float) v.x/1.0f);
//						}
//					}
//				}
	            
				glEnd();
	            //GL11.glDisable(GL11.GL_BLEND);
			}
			
			
//            glPopMatrix();
            swapBuffers();
            repaint(); 
        } catch (LWJGLException e) { 
            throw new RuntimeException(e); 
        } 
    } 
//	public void paintGL() {
//		super.paintGL();
//		try {
//			if( !initialized )
//			{
//				initGL();
//				initialized = true;
//			}
//			System.out.println("printingGL");
////			makeCurrent();
//			GL11.glBegin(GL11.GL_QUADS);
//			GL11.glColor3f(0f,1f,1f);
//			for (Geoset geo : dispMDL.getMDL().m_geosets) {
//				for (Triangle tri : geo.m_triangle) {
//					for (Vertex v : tri.m_verts) {
//						GL11.glVertex3f((float) v.x, (float) v.y, (float) v.z);
//					}
//				}
//			}
//			GL11.glEnd();
////			swapBuffers();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	// public void paintComponent(Graphics g)
	// {
	// paintComponent(g,1);
	// }
	// public void paintComponent(Graphics g, int vertexSize)
	// {
	// super.paintComponent(g);
	// dispMDL.drawGeosets(g,this,vertexSize);
	// dispMDL.drawPivots(g,this,vertexSize);
	// switch((int)m_d1)
	// {
	// case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
	// case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
	// case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
	// }
	// //g.setColor( new Color( 255, 0, 0 ) );
	// g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(5)),(int)Math.round(convertY(0)));
	//
	// switch((int)m_d2)
	// {
	// case 0: g.setColor( new Color( 0, 255, 0 ) ); break;
	// case 1: g.setColor( new Color( 255, 0, 0 ) ); break;
	// case 2: g.setColor( new Color( 0, 0, 255 ) ); break;
	// }
	// //g.setColor( new Color( 255, 0, 0 ) );
	// g.drawLine((int)Math.round(convertX(0)),(int)Math.round(convertY(0)),(int)Math.round(convertX(0)),(int)Math.round(convertY(5)));
	//
	// //Visual effects from user controls
	// int xoff = 0;
	// int yoff = 0;
	// Component temp = this;
	// while( temp != null )
	// {
	// xoff+=temp.getX();
	// yoff+=temp.getY();
	// if( temp.getClass() == ModelPanel.class )
	// {
	// temp = MainFrame.panel;
	// }
	// else
	// {
	// temp = temp.getParent();
	// }
	// }
	//
	// try {
	// double mx =
	// (MouseInfo.getPointerInfo().getLocation().x-xoff);//MainFrame.frame.getX()-8);
	// double my =
	// (MouseInfo.getPointerInfo().getLocation().y-yoff);//MainFrame.frame.getY()-30);
	//
	// //SelectionBox:
	// if( selectStart != null )
	// {
	// Point sEnd = new Point((int)mx,(int)my);
	// Rectangle2D.Double r = pointsToRect(selectStart,sEnd);
	// g.setColor(MDLDisplay.selectColor);
	// ((Graphics2D)g).draw(r);
	// }
	// }
	// catch (Exception exc)
	// {
	// JOptionPane.showMessageDialog(null,"Error retrieving mouse coordinates. (Probably not a major issue. Due to sleep mode?)");
	// }
	// }
	public double convertX(double x) {
		return (x + cameraPos.x) * m_zoom + getWidth() / 2;
	}

	public double convertY(double y) {
		return ((-y + cameraPos.y) * m_zoom) + getHeight() / 2;
	}

	public double geomX(double x) {
		return (x - getWidth() / 2) / m_zoom - cameraPos.x;
	}

	public double geomY(double y) {
		return -((y - getHeight() / 2) / m_zoom - cameraPos.y);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clickTimer) {
			int xoff = 0;
			int yoff = 0;
			Component temp = this;
			while (temp != null) {
				xoff += temp.getX();
				yoff += temp.getY();
				if (temp.getClass() == ModelPanel.class) {
					temp = MainFrame.panel;
				} else {
					temp = temp.getParent();
				}
			}
			double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);// MainFrame.frame.getX()-8);
			double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);// MainFrame.frame.getY()-30);
			// JOptionPane.showMessageDialog(null,mx+","+my+" as mouse, "+lastClick.x+","+lastClick.y+" as last.");
			// System.out.println(xoff+" and "+mx);
			if (lastClick != null) {

				cameraPos.x += ((int) mx - lastClick.x) / m_zoom;
				cameraPos.y += ((int) my - lastClick.y) / m_zoom;
				lastClick.x = (int) mx;
				lastClick.y = (int) my;
			}
			if (leftClickStart != null) {

				xangle += ( mx - leftClickStart.x);
				yangle += ( my - leftClickStart.y);
				leftClickStart.x = (int) mx;
				leftClickStart.y = (int) my;
			}
//			 MainFrame.panel.setMouseCoordDisplay(m_d1,m_d2,((mx-getWidth()/2)/m_zoom)-m_a,-(((my-getHeight()/2)/m_zoom)-m_b));

			if (actStart != null) {
				Point actEnd = new Point((int) mx, (int) my);
				Point2D.Double convertedStart = new Point2D.Double(
						geomX(actStart.x), geomY(actStart.y));
				Point2D.Double convertedEnd = new Point2D.Double(
						geomX(actEnd.x), geomY(actEnd.y));
//				 dispMDL.updateAction(convertedStart,convertedEnd,m_d1,m_d2);
				actStart = actEnd;
			}
			repaint();
		} else if (e.getSource() == reAssignMatrix) {
			MatrixPopup matrixPopup = new MatrixPopup(dispMDL.getMDL());
			String[] words = { "Accept", "Cancel" };
			int i = JOptionPane.showOptionDialog(MainFrame.panel, matrixPopup,
					"Rebuild Matrix", JOptionPane.PLAIN_MESSAGE,
					JOptionPane.YES_NO_OPTION, null, words, words[1]);
			if (i == 0) {
				// JOptionPane.showMessageDialog(null,"action approved");
				dispMDL.setMatrix(matrixPopup.newRefs);
			}
		} else if (e.getSource() == cogBone) {
			dispMDL.cogBones();
		}
	}

	public void mouseEntered(MouseEvent e) {
		clickTimer.setRepeats(true);
		clickTimer.start();
		mouseInBounds = true;
	}

	public void mouseExited(MouseEvent e) {
		if (leftClickStart == null && actStart == null && lastClick == null)
			clickTimer.stop();
		mouseInBounds = false;
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			lastClick = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			leftClickStart = new Point(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			actStart = new Point(e.getX(), e.getY());
			Point2D.Double convertedStart = new Point2D.Double(
					geomX(actStart.x), geomY(actStart.y));
			// dispMDL.startAction(convertedStart,m_d1,m_d2,MainFrame.panel.currentActionType());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			cameraPos.x += (e.getX() - lastClick.x) / m_zoom;
			cameraPos.y += (e.getY() - lastClick.y) / m_zoom;
			lastClick = null;
		} else if (e.getButton() == MouseEvent.BUTTON1 && leftClickStart != null) {
			Point selectEnd = new Point(e.getX(), e.getY());
			Rectangle2D.Double area = pointsToGeomRect(leftClickStart, selectEnd);
			// System.out.println(area);
			// dispMDL.selectVerteces(area,m_d1,m_d2,MainFrame.panel.currentSelectionType());
			leftClickStart = null;
		} else if (e.getButton() == MouseEvent.BUTTON3 && actStart != null) {
			Point actEnd = new Point(e.getX(), e.getY());
			Point2D.Double convertedStart = new Point2D.Double(
					geomX(actStart.x), geomY(actStart.y));
			Point2D.Double convertedEnd = new Point2D.Double(geomX(actEnd.x),
					geomY(actEnd.y));
			// dispMDL.finishAction(convertedStart,convertedEnd,m_d1,m_d2);
			actStart = null;
		}
		if (!mouseInBounds && leftClickStart == null && actStart == null
				&& lastClick == null) {
			clickTimer.stop();
			repaint();
		}
		MainFrame.panel.refreshUndo();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {

			// if( actEnd.equals(actStart) )
			// {
			// actStart = null;

			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
			contextMenu.show(this, e.getX(), e.getY());
			// }
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int wr = e.getWheelRotation();
		boolean neg = wr < 0;

		// get mouse coords
		int xoff = 0;
		int yoff = 0;
		Component temp = this;
		while (temp != null) {
			xoff += temp.getX();
			yoff += temp.getY();
			if (temp.getClass() == ModelPanel.class) {
				temp = MainFrame.panel;
			} else {
				temp = temp.getParent();
			}
		}
		double mx = (MouseInfo.getPointerInfo().getLocation().x - xoff);
		double my = (MouseInfo.getPointerInfo().getLocation().y - yoff);

		if (neg) {
			wr = -wr;
		}
		for (int i = 0; i < wr; i++) {
			if (neg) {
//				cameraPos.x -= (mx - getWidth() / 2)
//						* (1 / m_zoom - 1 / (m_zoom * 1.15));
//				cameraPos.y -= (my - getHeight() / 2)
//						* (1 / m_zoom - 1 / (m_zoom * 1.15));
//				cameraPos.z -= (getHeight() / 2)
//						* (1 / m_zoom - 1 / (m_zoom * 1.15));
				m_zoom *= 1.15;
			} else {
				m_zoom /= 1.15;
//				cameraPos.x -= (mx - getWidth() / 2)
//						* (1 / (m_zoom * 1.15) - 1 / m_zoom);
//				cameraPos.y -= (my - getHeight() / 2)
//						* (1 / (m_zoom * 1.15) - 1 / m_zoom);
//				cameraPos.z -= (getHeight() / 2)
//						* (1 / (m_zoom * 1.15) - 1 / m_zoom);
			}
		}
	}

	public Rectangle2D.Double pointsToGeomRect(Point a, Point b) {
		Point2D.Double topLeft = new Point2D.Double(Math.min(geomX(a.x),
				geomX(b.x)), Math.min(geomY(a.y), geomY(b.y)));
		Point2D.Double lowRight = new Point2D.Double(Math.max(geomX(a.x),
				geomX(b.x)), Math.max(geomY(a.y), geomY(b.y)));
		Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y,
				(lowRight.x - (topLeft.x)), ((lowRight.y) - (topLeft.y)));
		return temp;
	}

	public Rectangle2D.Double pointsToRect(Point a, Point b) {
		Point2D.Double topLeft = new Point2D.Double(Math.min((a.x), (b.x)),
				Math.min((a.y), (b.y)));
		Point2D.Double lowRight = new Point2D.Double(Math.max((a.x), (b.x)),
				Math.max((a.y), (b.y)));
		Rectangle2D.Double temp = new Rectangle2D.Double(topLeft.x, topLeft.y,
				(lowRight.x - (topLeft.x)), ((lowRight.y) - (topLeft.y)));
		return temp;
	}
	

	   private static final int BYTES_PER_PIXEL = 4;
	   public static int loadTexture(BufferedImage image){
	      
	      int[] pixels = new int[image.getWidth() * image.getHeight()];
	        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

	        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB
	        
	        for(int y = 0; y < image.getHeight(); y++){
	            for(int x = 0; x < image.getWidth(); x++){
	                int pixel = pixels[y * image.getWidth() + x];
	                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
	                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
	                buffer.put((byte) (pixel & 0xFF));               // Blue component
	                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
	            }
	        }

	        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

	        // You now have a ByteBuffer filled with the color data of each pixel.
	        // Now just create a texture ID and bind it. Then you can load it using 
	        // whatever OpenGL method you want, for example:

	      int textureID = GL11.glGenTextures(); //Generate texture ID
	      GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); //Bind texture ID
	        
	        //Setup wrap mode
	      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
	        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

	        //Setup texture scaling filtering
	        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	        
	        //Send texel data to OpenGL
	        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
	      
	        //Return the texture ID so we can bind it later again
	      return textureID;
	   }
}