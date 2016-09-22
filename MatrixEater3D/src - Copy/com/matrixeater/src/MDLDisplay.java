package com.matrixeater.src;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
/**
 * A wrapper for an MDL to control how it is displayed onscreen,
 * between all viewports.
 * 
 * Eric Theller
 * 6/7/2012
 */
public class MDLDisplay
{
    MDL model;
    ArrayList<Vertex> selection = new ArrayList<Vertex>();
    ArrayList<TVertex> uvselection = new ArrayList<TVertex>();
    ArrayList<Geoset> visibleGeosets = new ArrayList<Geoset>();
    ArrayList<Geoset> editableGeosets = new ArrayList<Geoset>();
    Geoset highlight;
    public static Color selectColor = Color.red;
    ModelPanel mpanel;
    
    ArrayList<UndoAction> actionStack = new ArrayList<UndoAction>();
    ArrayList<UndoAction> redoStack = new ArrayList<UndoAction>();
    
    int actionType = -1;
    UndoAction currentAction;
    int actionTypeUV = -1;
    UndoAction currentUVAction;
    
    boolean beenSaved = true;
    boolean lockdown = false;//lockdown editability
    boolean dispPivots = false;
    boolean dispChildren = false;
    boolean dispPivotNames = false;
    boolean dispCameras = false;
    boolean dispCameraNames = false;
    
    UVPanel uvpanel = null;
    public MDLDisplay(MDL mdlr, ModelPanel mpanel)
    {
        model = mdlr;
        visibleGeosets.addAll(mdlr.m_geosets);
        editableGeosets.addAll(mdlr.m_geosets);
        this.mpanel = mpanel;
    }
    public void setUVPanel(UVPanel panel)
    {
    	uvpanel = panel;
    }
    public MDL getMDL()
    {
        return model;
    }
    public void setDispPivots(boolean flag)
    {
        dispPivots = flag;
        if( !flag )
        {
            for( IdObject o: model.m_idobjects )
            {
                Vertex ver = o.pivotPoint;
                selection.remove(ver);
            }
        }
    }
    public void setDispPivotNames(boolean flag)
    {
        dispPivotNames = flag;
    }
    public void setDispChildren(boolean flag)
    {
        dispChildren = flag;
        

    	ArrayList<IdObject> geoParents = null;
    	ArrayList<IdObject> geoSubParents = null;
    	if( dispChildren )
    	{
    		geoParents = new ArrayList<IdObject>();
    		geoSubParents = new ArrayList<IdObject>();
            for( Geoset geo: editableGeosets )
            {
            	for( GeosetVertex ver: geo.m_vertex )
            	{
            		for( Bone b: ver.bones )
            		{
            			if( !geoParents.contains(b))
            				geoParents.add(b);
            		}
            	}
            }
//    		childMap = new HashMap<IdObject,ArrayList<IdObject>>();
            for( IdObject obj: model.m_idobjects)
            {
            	if( !geoParents.contains(obj) )
            	{
                	boolean valid = false;
                	for( int i = 0; !valid && i < geoParents.size(); i++ )
                	{
                		valid = geoParents.get(i).childOf(obj);
                	}
                	if( valid )
                	{
                		geoSubParents.add(obj);
                	}
//                	if( obj.parent != null )
//                  	{
//                      	ArrayList<IdObject> children = childMap.get(obj.parent);
//                      	if( children == null )
//                      	{
//                      		children = new ArrayList<IdObject>();
//                      		childMap.put(obj.parent, children);
//                      	}
//                      	children.add(obj);
//                  	}
            	}
            }
            //System.out.println(geoSubParents);
    	}
        if( dispChildren )
            for( IdObject o: model.m_idobjects )
            {
//            	boolean hasRef = false;//highlight != null && highlight.containsReference(o);
//            	if( dispChildren )
//            	{
//            		for( int i = 0; !hasRef && i < editableGeosets.size(); i++ )
//            		{
//            			hasRef = editableGeosets.get(i).containsReference(o);
//            		}
//            	}
            	if( !(geoParents.contains(o) || geoSubParents.contains(o)) )//!dispChildren || hasRef )
            	{
                    Vertex ver = o.pivotPoint;
                    if( selection.contains(ver)){
                    	selection.remove(ver);
                    }
            	}
            }
    }
    public void setDispCameras(boolean flag)
    {
        dispCameras = flag;
        if( !flag )
        {
            for( Camera cam: model.m_cameras )
            {
                Vertex ver = cam.Position;
                Vertex targ = cam.targetPosition;
                selection.remove(ver);
                selection.remove(targ);
            }
        }
    }
    public void setDispCameraNames(boolean flag)
    {
        dispCameraNames = flag;
    }
    public boolean isGeosetVisible(int i)
    {
        return visibleGeosets.contains(model.getGeoset(i));
    }
    public boolean isGeosetEditable(int i)
    {
        return editableGeosets.contains(model.getGeoset(i));
    }
    public boolean isGeosetHighlighted(int i)
    {
        return highlight == (model.getGeoset(i));
    }
    public void setMatrix( DefaultListModel<BoneShell> bones )
    {
        if( !lockdown )
        {
            Matrix mx = new Matrix();
            mx.bones = new ArrayList<Bone>();
            for( int i = 0; i < bones.size(); i++ )
            {
                mx.add(bones.get(i).bone);
            }
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex vert = selection.get(i);
                if( vert.getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)vert;
                    gv.clearBoneAttachments();
                    gv.addBoneAttachments(mx.bones);
                }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null,"Action refused.");
        }
    }
    public void cogBones()
    {
        ArrayList<IdObject> selBones = new ArrayList<IdObject>();
        for( IdObject b: model.m_idobjects )
        {
            if( selection.contains(b.pivotPoint) && !selBones.contains(b) )
            {
                selBones.add(b);
            }
        }
//      HashMap<IdObject,ArrayList<IdObject>> childMap = new HashMap<IdObject,ArrayList<IdObject>>();
//      
//      for( IdObject obj: model.m_idobjects)
//      {
//      	if( obj.parent != null )
//      	{
//          	ArrayList<IdObject> children = childMap.get(obj.parent);
//          	if( children == null )
//          	{
//          		children = new ArrayList<IdObject>();
//          		childMap.put(obj.parent, children);
//          	}
//          	children.add(obj);
//      	}
//      }
        
        for( IdObject obj: selBones )
        {
        	if( Bone.class.isAssignableFrom(obj.getClass()))
        	{
        		Bone bone = (Bone)obj;
        		ArrayList<GeosetVertex> childVerts = new ArrayList<GeosetVertex>();
        		for( Geoset geo: model.m_geosets )
        		{
        			childVerts.addAll(geo.getChildrenOf(bone));
//                	if( obj.parent != null )
//                	{
//                    	ArrayList<IdObject> children = childMap.get(obj.parent);
//                    	if( children == null )
//                    	{
//                    		children = new ArrayList<IdObject>();
//                    		childMap.put(obj.parent, children);
//                    	}
//                    	children.add(obj);
//                	}
        		}
        		if( childVerts.size() > 0 )
        		bone.pivotPoint.setTo(Vertex.centerOfGroup(childVerts));
        	}
        }
    }
    public void highlightGeoset( Geoset g, boolean flag )
    {
        if( flag )
        {
            highlight = g;
        }
        else
        {
            if( g == highlight )
            {
                highlight = null;
            }
        }
    }
    public void makeGeosetVisible( Geoset g, boolean flag )
    {
        if( flag )
        {
            if( !visibleGeosets.contains(g) )
            {
                visibleGeosets.add(g);
            }
        }
        else
        {
            visibleGeosets.remove(g);
        }
    }
    public void makeGeosetEditable( Geoset g, boolean flag )
    {
        if( flag )
        {
            if( !editableGeosets.contains(g) )
            {
                editableGeosets.add(g);
            }
        }
        else
        {
//             int n = g.numVerteces();
//             for( int i = 0; i < n; i++ )
//             {
//                 selection.remove(g.getVertex(i));
//             }
            selection.removeAll(g.m_vertex);
            editableGeosets.remove(g);
        }
    }
    public void highlightGeoset( int i, boolean flag )
    {
        Geoset g = model.getGeoset(i);
        highlightGeoset(g,flag);
    }
    public void makeGeosetVisible( int i, boolean flag )
    {
        Geoset g = model.getGeoset(i);
        makeGeosetVisible(g,flag);
    }
    public void makeGeosetEditable( int i, boolean flag )
    {
        Geoset g = model.getGeoset(i);
        makeGeosetEditable(g,flag);
    }
    public void drawGeosets(Graphics g, Viewport vp, int vertexSize)
    {
        for( Geoset geo: model.m_geosets )
        {
            if( visibleGeosets.contains(geo) && !editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 150, 150, 255 ) );
                geo.drawTriangles(g,vp);
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 190, 190, 190 ) );
                geo.drawTriangles(g,vp);
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 0, 0, 0 ) );
                geo.drawVerteces(g,vp,vertexSize,selection);
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( geo == highlight )
            {
                g.setColor( new Color( 255, 255, 0 ) );
                geo.drawTriangles(g,vp);
                g.setColor( new Color( 0, 255, 0 ) );
                geo.drawVerteces(g,vp,vertexSize);
            }
        }
    }
    public void drawGeosets(Graphics g, UVViewport vp, int vertexSize)
    {
        for( Geoset geo: model.m_geosets )
        {
            if( visibleGeosets.contains(geo) && !editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 150, 150, 255 ) );
                geo.drawTriangles(g,vp,uvpanel.currentLayer());
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 190, 190, 190 ) );
                geo.drawTriangles(g,vp,uvpanel.currentLayer());
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( editableGeosets.contains(geo) && geo != highlight )
            {
                g.setColor( new Color( 0, 0, 255 ) );
                geo.drawTVerteces(g,vp,vertexSize,uvselection,uvpanel.currentLayer());
            }
        }
        for( Geoset geo: model.m_geosets )
        {
            if( geo == highlight )
            {
                g.setColor( new Color( 255, 255, 0 ) );
                geo.drawTriangles(g,vp,uvpanel.currentLayer());
                g.setColor( new Color( 0, 255, 0 ) );
                geo.drawVerteces(g,vp,vertexSize,uvpanel.currentLayer());
            }
        }
    }
    public void drawPivots(Graphics g, Viewport vp, int vertexSize)
    {
        if( dispPivots )
        {
//        	HashMap<IdObject,ArrayList<IdObject>> childMap = null;
        	ArrayList<IdObject> geoParents = null;
        	ArrayList<IdObject> geoSubParents = null;
        	if( dispChildren )
        	{
        		geoParents = new ArrayList<IdObject>();
        		geoSubParents = new ArrayList<IdObject>();
                for( Geoset geo: editableGeosets )
                {
                	for( GeosetVertex ver: geo.m_vertex )
                	{
                		for( Bone b: ver.bones )
                		{
                			if( !geoParents.contains(b))
                				geoParents.add(b);
                		}
                	}
                }
//        		childMap = new HashMap<IdObject,ArrayList<IdObject>>();
                for( IdObject obj: model.m_idobjects)
                {
                	if( !geoParents.contains(obj) )
                	{
                    	boolean valid = false;
                    	for( int i = 0; !valid && i < geoParents.size(); i++ )
                    	{
                    		valid = geoParents.get(i).childOf(obj);
                    	}
                    	if( valid )
                    	{
                    		geoSubParents.add(obj);
                    	}
//                    	if( obj.parent != null )
//                      	{
//                          	ArrayList<IdObject> children = childMap.get(obj.parent);
//                          	if( children == null )
//                          	{
//                          		children = new ArrayList<IdObject>();
//                          		childMap.put(obj.parent, children);
//                          	}
//                          	children.add(obj);
//                      	}
                	}
                }
                //System.out.println(geoSubParents);
        	}
            g.setColor(Color.magenta.darker());
            g.setFont(new Font("Arial",Font.BOLD,12));
            if( !dispChildren )
                for( IdObject o: model.m_idobjects )
                {
                    Vertex ver = o.pivotPoint;
                    if( selection.contains(ver) )
                    {
                        g.setColor(Color.red.darker());
                    }
                    if( dispPivotNames )
                    g.drawString(o.getName(),(int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
                    g.fillRect((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())))-vertexSize,1+vertexSize*2,1+vertexSize*2);
                    if( selection.contains(ver) )
                    {
                        g.setColor(Color.magenta.darker());
                    }
                }
            else
                for( IdObject o: model.m_idobjects )
                {
//                	boolean hasRef = false;//highlight != null && highlight.containsReference(o);
//                	if( dispChildren )
//                	{
//                		for( int i = 0; !hasRef && i < editableGeosets.size(); i++ )
//                		{
//                			hasRef = editableGeosets.get(i).containsReference(o);
//                		}
//                	}
                	if( geoParents.contains(o) || geoSubParents.contains(o) )//!dispChildren || hasRef )
                	{
                        Vertex ver = o.pivotPoint;
                        if( selection.contains(ver) )
                        {
                            g.setColor(Color.red.darker());
                        }
                        if( dispPivotNames )
                        g.drawString(o.getName(),(int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
                        g.fillRect((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ())))-vertexSize,(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ())))-vertexSize,1+vertexSize*2,1+vertexSize*2);
                        if( selection.contains(ver) )
                        {
                            g.setColor(Color.magenta.darker());
                        }
                	}
                }
        }
    }
    public void drawCameras(Graphics g, Viewport vp, int vertexSize)
    {
        if( dispCameras )
        {
            g.setColor(Color.green.darker());
            g.setFont(new Font("Arial",Font.BOLD,12));
            for( Camera cam: model.m_cameras )
            {
            	Graphics2D g2 = ((Graphics2D)g.create());
                Vertex ver = cam.Position;
                Vertex targ = cam.targetPosition;
                boolean verSel = selection.contains(ver);
                boolean tarSel = selection.contains(targ);
                Point start = new Point((int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
                Point end = new Point((int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
                if( dispCameraNames )
                {
                	boolean changedCol = false;
                	
                	if( verSel )
                	{
                        g2.setColor(Color.orange.darker());
                        changedCol = true;
                	}
                	g2.drawString(cam.getName(),(int)Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
                	if( tarSel )
                	{
                        g2.setColor(Color.orange.darker());
                        changedCol = true;
                	}
                	else if( verSel )
                	{
                        g2.setColor(Color.green.darker());
                        changedCol = false;
                	}
                	g2.drawString(cam.getName()+"_target",(int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),(int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
                	if( changedCol )
                        g2.setColor(Color.green.darker());
                }
                
                g2.translate(end.x, end.y);
                g2.rotate(-(Math.PI/2+Math.atan2(end.x-start.x, end.y-start.y)));
                int size = (int)(20 * vp.getZoomAmount());
                double dist = start.distance(end);

                if( verSel )
                {
                    g2.setColor(Color.orange.darker());
                }
                //Cam
                g2.fillRect((int)dist-vertexSize,0-vertexSize,1+vertexSize*2,1+vertexSize*2);
                g2.drawRect((int)dist-size, -size,size*2,size*2);

                if( tarSel )
                {
                    g2.setColor(Color.orange.darker());
                }
                else if( verSel )
                {
                    g2.setColor(Color.green.darker());
                }
                //Target
                g2.fillRect(0-vertexSize,0-vertexSize,1+vertexSize*2,1+vertexSize*2);
                g2.drawLine(0, 0, size, size );//(int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)), (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
                g2.drawLine(0, 0, size, -size );//(int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)), (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));

                if( !verSel && tarSel )
                {
                    g2.setColor(Color.green.darker());
                }                
                g2.drawLine(0, 0, (int)dist, 0);
            }
        }
    }
    
    public void selectVerteces(Rectangle2D.Double area, byte dim1, byte dim2, int selectionType)
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<Vertex> oldSelection = new ArrayList<Vertex>(selection);
            switch( selectionType )
            {
                case 0:
                    selection.clear();
                    for( Geoset geo: editableGeosets )
                    {
                        for( Vertex v: geo.getVertecesInArea(area,dim1,dim2) )
                        {
                            if( !selection.contains(v) )
                            {
                                selection.add(v);
                            }
                        }
                    }
                    if( dispPivots )
                    {
                    	ArrayList<IdObject> geoParents = null;
                    	ArrayList<IdObject> geoSubParents = null;
                    	if( dispChildren )
                    	{
                    		geoParents = new ArrayList<IdObject>();
                    		geoSubParents = new ArrayList<IdObject>();
                            for( Geoset geo: editableGeosets )
                            {
                            	for( GeosetVertex ver: geo.m_vertex )
                            	{
                            		for( Bone b: ver.bones )
                            		{
                            			if( !geoParents.contains(b))
                            				geoParents.add(b);
                            		}
                            	}
                            }
//                    		childMap = new HashMap<IdObject,ArrayList<IdObject>>();
                            for( IdObject obj: model.m_idobjects)
                            {
                            	if( !geoParents.contains(obj) )
                            	{
                                	boolean valid = false;
                                	for( int i = 0; !valid && i < geoParents.size(); i++ )
                                	{
                                		valid = geoParents.get(i).childOf(obj);
                                	}
                                	if( valid )
                                	{
                                		geoSubParents.add(obj);
                                	}
//                                	if( obj.parent != null )
//                                  	{
//                                      	ArrayList<IdObject> children = childMap.get(obj.parent);
//                                      	if( children == null )
//                                      	{
//                                      		children = new ArrayList<IdObject>();
//                                      		childMap.put(obj.parent, children);
//                                      	}
//                                      	children.add(obj);
//                                  	}
                            	}
                            }
                            //System.out.println(geoSubParents);
                    	}

                        if( !dispChildren )
                            for( Vertex ver: model.m_pivots )
                            {
                                if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                                {
                                    selection.add(ver);
                                }
                            }
                        else
                            for( IdObject o: model.m_idobjects )
                            {
//                            	boolean hasRef = false;//highlight != null && highlight.containsReference(o);
//                            	if( dispChildren )
//                            	{
//                            		for( int i = 0; !hasRef && i < editableGeosets.size(); i++ )
//                            		{
//                            			hasRef = editableGeosets.get(i).containsReference(o);
//                            		}
//                            	}
                            	if( geoParents.contains(o) || geoSubParents.contains(o) )//!dispChildren || hasRef )
                            	{
                                    Vertex ver = o.pivotPoint;
                                    if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                                    {
                                        selection.add(ver);
                                    }
                            	}
                            }
                    }
                    if( dispCameras )
                    for( Camera cam: model.m_cameras )
                    {
                    	Vertex ver = cam.Position;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.add(ver);
                        }
                        ver = cam.targetPosition;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.add(ver);
                        }
                    }
                    break;
                case 1:
                    for( Geoset geo: editableGeosets )
                    {
                        for( Vertex v: geo.getVertecesInArea(area,dim1,dim2) )
                        {
                            if( !selection.contains(v) )
                            {
                                selection.add(v);
                            }
                        }
                    }
                    if( dispPivots )
                    for( Vertex ver: model.m_pivots )
                    {
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.add(ver);
                        }
                    }
                    if( dispCameras )
                    for( Camera cam: model.m_cameras )
                    {
                    	Vertex ver = cam.Position;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.add(ver);
                        }
                        ver = cam.targetPosition;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.add(ver);
                        }
                    }
                    break;
                case 2:
                    for( Geoset geo: editableGeosets )
                    {
                        selection.removeAll(geo.getVertecesInArea(area,dim1,dim2));
                    }
                    if( dispPivots )
                    for( Vertex ver: model.m_pivots )
                    {
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) )
                        {
                            selection.remove(ver);
                        }
                    }
                    if( dispCameras )
                    for( Camera cam: model.m_cameras )
                    {
                    	Vertex ver = cam.Position;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.remove(ver);
                        }
                        ver = cam.targetPosition;
                        if( area.contains(ver.getCoord(dim1),ver.getCoord(dim2)) && !selection.contains(ver) )
                        {
                            selection.remove(ver);
                        }
                    }
                    break;
            }
            redoStack.clear();
            actionStack.add(new SelectAction(oldSelection,selection,this,selectionType));
        }
    }
    public void selectTVerteces(Rectangle2D.Double area, int selectionType)
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<TVertex> oldSelection = new ArrayList<TVertex>(uvselection);
            switch( selectionType )
            {
                case 0:
                    uvselection.clear();
                    for( Geoset geo: editableGeosets )
                    {
                        for( TVertex v: geo.getTVertecesInArea(area,uvpanel.currentLayer()) )
                        {
                            if( !uvselection.contains(v) )
                            {
                                uvselection.add(v);
                            }
                        }
                    }
                    break;
                case 1:
                    for( Geoset geo: editableGeosets )
                    {
                        for( TVertex v: geo.getTVertecesInArea(area,uvpanel.currentLayer()) )
                        {
                            if( !uvselection.contains(v) )
                            {
                                uvselection.add(v);
                            }
                        }
                    }
                    break;
                case 2:
                    for( Geoset geo: editableGeosets )
                    {
                        uvselection.removeAll(geo.getTVertecesInArea(area,uvpanel.currentLayer()));
                    }
                    break;
            }
            redoStack.clear();
            actionStack.add(new UVSelectAction(oldSelection,uvselection,this,selectionType));
        }
    }
    public void selectVerteces(ArrayList<Vertex> newSelection, int selectionType)
    {
        if( !lockdown )
        {
            beenSaved = false;
            switch( selectionType )
            {
                case 0:
                    selection.clear();
                    for( Vertex v: newSelection )
                    {
                        if( !selection.contains(v) )
                        {
                            selection.add(v);
                        }
                    }
                    break;
                case 1:
                    for( Vertex v: newSelection )
                    {
                        if( !selection.contains(v) )
                        {
                            selection.add(v);
                        }
                    }
                    break;
                case 2:
                    selection.removeAll(newSelection);
                    break;
            }
        }
    }
    public void updateAction(Point2D.Double mouseStart, Point2D.Double mouseStop, byte dim1, byte dim2)
    {
        //Points need to be in geometry/model space
        if( !lockdown )
        {
            beenSaved = false;
            Vertex v = null;
            switch( actionType )
            {
                case 3://Move
                    double deltaX = mouseStop.x - mouseStart.x;
                    double deltaY = mouseStop.y - mouseStart.y;
                    for( Vertex ver: selection )
                    {
                    	if( getDimEditable(dim1) )
                        ver.translateCoord(dim1,deltaX);
                    	if( getDimEditable(dim2) )
                        ver.translateCoord(dim2,deltaY);
                    }
                	if( getDimEditable(dim1) )
                    ((MoveAction)currentAction).moveVector.translateCoord(dim1,deltaX);
                	if( getDimEditable(dim2) )
                    ((MoveAction)currentAction).moveVector.translateCoord(dim2,deltaY);
                    break;
                case 4://Rotate
            		ArrayList<Normal> normals = ((RotateAction)currentAction).normals;
                    v = Vertex.centerOfGroup(selection);
                    double cx = v.getCoord(dim1);
                    double cy = v.getCoord(dim2);
                    double dx = mouseStart.x-cx;
                    double dy = mouseStart.y-cy;
                    double r = Math.sqrt(dx*dx+dy*dy);
                    double ang = Math.acos(dx/r);
                    if( dy < 0 )
                    {
                        ang = -ang;
                    }
                    
                    dx = mouseStop.x-cx;
                    dy = mouseStop.y-cy;
                    r = Math.sqrt(dx*dx+dy*dy);
                    double ang2 = Math.acos(dx/r);
                    if( dy < 0 )
                    {
                        ang2 = -ang2;
                    }
                    double deltaAng = ang2-ang;
                	if( selection.size() > 1 )
                	{
                        for( Vertex ver: selection )
                        {
                            double x1 = ver.getCoord(dim1);
                            double y1 = ver.getCoord(dim2);
                            dx = x1-cx;
                            dy = y1-cy;
                            r = Math.sqrt(dx*dx+dy*dy);
                            double verAng = Math.acos(dx/r);
                            if( dy < 0 )
                            {
                                verAng = -verAng;
                            }
//                        	if( getDimEditable(dim1) )
                            double nextDim = Math.cos(verAng+deltaAng)*r+cx;
                            if( !Double.isNaN(nextDim) )
                            ver.setCoord(dim1,Math.cos(verAng+deltaAng)*r+cx);
//                        	if( getDimEditable(dim2) )
                            nextDim = Math.sin(verAng+deltaAng)*r+cy;
                            if( !Double.isNaN(nextDim) )
                            ver.setCoord(dim2,Math.sin(verAng+deltaAng)*r+cy);
//                        	if( getDimEditable(dim1) )
                            ((MoveAction)currentAction).moveVectors.get(selection.indexOf(ver)).translateCoord(dim1,ver.getCoord(dim1)-x1);
//                        	if( getDimEditable(dim2) )
                            ((MoveAction)currentAction).moveVectors.get(selection.indexOf(ver)).translateCoord(dim2,ver.getCoord(dim2)-y1);
                        }
                        
                	}
                    cx = 0;
                    cy = 0;
                    for( Vertex ver: normals )
                    {
                        double x1 = ver.getCoord(dim1);
                        double y1 = ver.getCoord(dim2);
                        dx = x1-cx;
                        dy = y1-cy;
                        r = Math.sqrt(dx*dx+dy*dy);
                        double verAng = Math.acos(dx/r);
                        if( dy < 0 )
                        {
                            verAng = -verAng;
                        }
//                    	if( getDimEditable(dim1) )
                        double nextDim = Math.cos(verAng+deltaAng)*r+cx;
                        if( !Double.isNaN(nextDim) )
                        ver.setCoord(dim1,Math.cos(verAng+deltaAng)*r+cx);
//                    	if( getDimEditable(dim2) )
                        nextDim = Math.sin(verAng+deltaAng)*r+cy;
                        if( !Double.isNaN(nextDim) )
                        ver.setCoord(dim2,Math.sin(verAng+deltaAng)*r+cy);
//                    	if( getDimEditable(dim1) )
                        ((RotateAction)currentAction).normalMoveVectors.get(normals.indexOf(ver)).translateCoord(dim1,ver.getCoord(dim1)-x1);
//                    	if( getDimEditable(dim2) )
                        ((RotateAction)currentAction).normalMoveVectors.get(normals.indexOf(ver)).translateCoord(dim2,ver.getCoord(dim2)-y1);
                    }
                    break;
                case 5:
                    v = Vertex.centerOfGroup(selection);
                    double cxs = v.getCoord(dim1);
                    double cys = v.getCoord(dim2);
                    double czs = 0;
                    double dxs = mouseStart.x-cxs;
                    double dys = mouseStart.y-cys;
                    double dzs = 0;
                    double startDist = Math.sqrt(dxs*dxs+dys*dys);
                    dxs = mouseStop.x-cxs;
                    dys = mouseStop.y-cys;
                    double endDist = Math.sqrt(dxs*dxs+dys*dys);
                    double distRatio = endDist/startDist;
                    cxs = v.getCoord((byte)0);
                    cys = v.getCoord((byte)1);
                    czs = v.getCoord((byte)2);
                    for( Vertex ver: selection )
                    {
                        dxs = ver.getCoord((byte)0)-cxs;
                        dys = ver.getCoord((byte)1)-cys;
                        dzs = ver.getCoord((byte)2)-czs;
                        //startDist is now the distance to vertex from center,
                        // endDist is now the change in distance of mouse
                    	if( getDimEditable(0) )
                        ver.translateCoord((byte)0,dxs*(distRatio-1));
                    	if( getDimEditable(1) )
                        ver.translateCoord((byte)1,dys*(distRatio-1));
                    	if( getDimEditable(2) )
                        ver.translateCoord((byte)2,dzs*(distRatio-1));
                    	if( getDimEditable(0) )
                        ((MoveAction)currentAction).moveVectors.get(selection.indexOf(ver)).translateCoord((byte)0,dxs*(distRatio-1));
                    	if( getDimEditable(1) )
                        ((MoveAction)currentAction).moveVectors.get(selection.indexOf(ver)).translateCoord((byte)1,dys*(distRatio-1));
                    	if( getDimEditable(2) )
                        ((MoveAction)currentAction).moveVectors.get(selection.indexOf(ver)).translateCoord((byte)2,dzs*(distRatio-1));
                    }
                    break;
                case 6:
                    deltaX = mouseStop.x - mouseStart.x;
                    deltaY = mouseStop.y - mouseStart.y;
                    for( Vertex ver: selection )
                    {
                    	if( getDimEditable(dim1) )
                        ver.translateCoord(dim1,deltaX);
                    	if( getDimEditable(dim2) )
                        ver.translateCoord(dim2,deltaY);
                    }
                	if( getDimEditable(dim1) )
                    ((ExtrudeAction)currentAction).baseMovement.moveVector.translateCoord(dim1,deltaX);
                	if( getDimEditable(dim2) )
                    ((ExtrudeAction)currentAction).baseMovement.moveVector.translateCoord(dim2,deltaY);
    //                 extrudeSelection(mouseStart,mouseStop,dim1,dim2);
    
                    break;
                case 7:
                    deltaX = mouseStop.x - mouseStart.x;
                    deltaY = mouseStop.y - mouseStart.y;
                    for( Vertex ver: selection )
                    {
                    	if( getDimEditable(dim1) )
                            ver.translateCoord(dim1,deltaX);
                        	if( getDimEditable(dim2) )
                            ver.translateCoord(dim2,deltaY);
                    }
                	if( getDimEditable(dim1) )
                    ((ExtrudeAction)currentAction).baseMovement.moveVector.translateCoord(dim1,deltaX);
                	if( getDimEditable(dim2) )
                    ((ExtrudeAction)currentAction).baseMovement.moveVector.translateCoord(dim2,deltaY);
    //                 double deltaXe = mouseStop.x - mouseStart.x;
    //                 double deltaYe = mouseStop.y - mouseStart.y;
    //                 
    //                 ArrayList<Triangle> edges = new ArrayList<Triangle>();
    //                 ArrayList<Triangle> brokenFaces = new ArrayList<Triangle>();
    //                 
    //                 ArrayList<GeosetVertex> copies = new ArrayList<GeosetVertex>();
    //                 ArrayList<Triangle> selTris = new ArrayList<Triangle>();
    //                 for( int i = 0; i < selection.size(); i++ )
    //                 {
    //                     Vertex vert = selection.get(i);
    //                     if( vert.getClass() == GeosetVertex.class )
    //                     {
    //                         GeosetVertex gv = (GeosetVertex)vert;
    // //                         copies.add(new GeosetVertex(gv));
    //                         
    // //                         selTris.addAll(gv.triangles);
    //                         for( int ti = 0; ti < gv.triangles.size(); ti++ )
    //                         {
    //                             Triangle temp = gv.triangles.get(ti);
    //                             if( !selTris.contains(temp) )
    //                             {
    //                                 selTris.add(temp);
    //                             }
    //                         }
    //                     }
    //                     else
    //                     {
    // //                         copies.add(null);
    //                         System.out.println("GeosetVertex "+i+" was not found.");
    //                     }
    //                 }
    //                 System.out.println(selection.size()+" verteces cloned into "+copies.size()+ " more.");
    //                 for( Triangle tri: selTris )
    //                 {
    //                     if( !selection.contains(tri.get(0))
    //                         ||!selection.contains(tri.get(1))
    //                         ||!selection.contains(tri.get(2)) )
    //                     {
    //                         int selVerts = 0;
    //                         GeosetVertex gv = null;
    //                         GeosetVertex gvTemp = null;
    //                         GeosetVertex gvCopy = null;//copies.get(selection.indexOf(gv));
    //                         GeosetVertex gvTempCopy = null;//copies.get(selection.indexOf(gvTemp));
    //                         for( int i = 0; i < 3; i++ )
    //                         {
    //                             GeosetVertex a = tri.get(i);
    //                             if( selection.contains(a) )
    //                             {
    //                                 selVerts++;
    // //                                 GeosetVertex b = copies.get(selection.indexOf(a));
    //                                 GeosetVertex b = new GeosetVertex(a);
    //                                 copies.add(b);
    //                                 tri.set(i,b);
    //                                 a.triangles.remove(tri);
    //                                 b.triangles.add(tri);
    //                                 if( gv == null )
    //                                 {
    //                                     gv = a;
    //                                     gvCopy = b;
    //                                     
    //                                 }
    //                                 else if( gvTemp == null )
    //                                 {
    //                                     gvTemp = a;
    //                                     gvTempCopy = b;
    //                                 }
    //                             }
    //                         }
    //                         if( selVerts == 2 )
    //                         {
    //                             if( gvCopy == null )
    //                             {
    //                                 System.out.println("Vertex (gvCopy) copy found as null!");
    //                             }
    //                             if( gvTempCopy == null )
    //                             {
    //                                 System.out.println("Vertex (gvTempCopy) copy found as null!");
    //                             }
    //                             Triangle newFace = new Triangle(null,null,null,gv.geoset);
    //                             
    //                             int indexA = tri.indexOf(gvTempCopy);
    //                             int indexB = tri.indexOf(gvCopy);
    //                             int indexC = -1;
    //                             
    //                             for( int i = 0; i < 3 && indexC == -1; i++ )
    //                             {
    //                                 if( i != indexA && i != indexB )
    //                                 {
    //                                     indexC = i;
    //                                 }
    //                             }
    //                             
    //                             System.out.println(" Indeces: "+indexA+","+indexB+","+indexC);
    //                             
    //                             newFace.set(indexA,gv);
    //                             newFace.set(indexB,gvTemp);
    //                             newFace.set(indexC,gvCopy);
    //                             //Make sure it's included later
    //                             gvTemp.triangles.add(newFace);
    //                             gv.triangles.add(newFace);
    //                             gvCopy.triangles.add(newFace);
    //                             gv.geoset.addTriangle(newFace);
    //                             
    //                             System.out.println("New Face: ");
    //                             System.out.println(newFace.get(0));
    //                             System.out.println(newFace.get(1));
    //                             System.out.println(newFace.get(2));
    //                             
    //                             newFace = new Triangle(null,null,null,gv.geoset);
    //                             
    //                             newFace.set(indexA,gvCopy);
    //                             newFace.set(indexB,gvTemp);
    //                             newFace.set(indexC,gvTempCopy);
    //                             //Make sure it's included later
    //                             gvCopy.triangles.add(newFace);
    //                             gvTemp.triangles.add(newFace);
    //                             gvTempCopy.triangles.add(newFace);
    //                             gv.geoset.addTriangle(newFace);
    //                             
    //                             System.out.println("New Alternate Face: ");
    //                             System.out.println(newFace.get(0));
    //                             System.out.println(newFace.get(1));
    //                             System.out.println(newFace.get(2));
    //                         }
    //                     }
    //                 }
    //                 for( Vertex vert: selection )
    //                 {
    //                     
    //                     vert.translateCoord(dim1,deltaXe);
    //                     vert.translateCoord(dim2,deltaYe);
    //                 }
    // 
    //                 for( GeosetVertex cgv: copies )
    //                 {
    //                     if( cgv != null )
    //                     {
    //                         cgv.geoset.addVertex(cgv);
    //                     }
    //                 }
                    break;
            }
        }
    }

    public void updateUVAction(Point2D.Double mouseStart, Point2D.Double mouseStop)
    {
        //Points need to be in geometry/model space
        if( !lockdown )
        {
        	byte dim1 = 0;
        	byte dim2 = 1;
            beenSaved = false;
            TVertex v = null;
            switch( actionTypeUV )
            {
                case 3://Move
                    double deltaX = mouseStop.x - mouseStart.x;
                    double deltaY = mouseStop.y - mouseStart.y;
                    for( TVertex ver: uvselection )
                    {
                    	if( !uvpanel.getDimLock(dim1) )
                        ver.translateCoord(dim1,deltaX);
                    	if( !uvpanel.getDimLock(dim2) )
                        ver.translateCoord(dim2,deltaY);
                    }
                	if( !uvpanel.getDimLock(dim1) )
                    ((UVMoveAction)currentUVAction).moveVector.translateCoord(dim1,deltaX);
                	if( !uvpanel.getDimLock(dim2) )
                    ((UVMoveAction)currentUVAction).moveVector.translateCoord(dim2,deltaY);
                    break;
                case 4://Rotate
                    v = TVertex.centerOfGroup(uvselection);
                    double cx = v.getCoord(dim1);
                    double cy = v.getCoord(dim2);
                    double dx = mouseStart.x-cx;
                    double dy = mouseStart.y-cy;
                    double r = Math.sqrt(dx*dx+dy*dy);
                    double ang = Math.acos(dx/r);
                    if( dy < 0 )
                    {
                        ang = -ang;
                    }
                    
                    dx = mouseStop.x-cx;
                    dy = mouseStop.y-cy;
                    r = Math.sqrt(dx*dx+dy*dy);
                    double ang2 = Math.acos(dx/r);
                    if( dy < 0 )
                    {
                        ang2 = -ang2;
                    }
                    double deltaAng = ang2-ang;
                	if( uvselection.size() > 1 )
                	{
                        for( TVertex ver: uvselection )
                        {
                            double x1 = ver.getCoord(dim1);
                            double y1 = ver.getCoord(dim2);
                            dx = x1-cx;
                            dy = y1-cy;
                            r = Math.sqrt(dx*dx+dy*dy);
                            double verAng = Math.acos(dx/r);
                            if( dy < 0 )
                            {
                                verAng = -verAng;
                            }
//                        	if( getDimEditable(dim1) )
                            double nextDim = Math.cos(verAng+deltaAng)*r+cx;
                            if( !Double.isNaN(nextDim) )
                            ver.setCoord(dim1,Math.cos(verAng+deltaAng)*r+cx);
//                        	if( getDimEditable(dim2) )
                            nextDim = Math.sin(verAng+deltaAng)*r+cy;
                            if( !Double.isNaN(nextDim) )
                            ver.setCoord(dim2,Math.sin(verAng+deltaAng)*r+cy);
//                        	if( getDimEditable(dim1) )
                            ((UVMoveAction)currentUVAction).moveVectors.get(uvselection.indexOf(ver)).translateCoord(dim1,ver.getCoord(dim1)-x1);
//                        	if( getDimEditable(dim2) )
                            ((UVMoveAction)currentUVAction).moveVectors.get(uvselection.indexOf(ver)).translateCoord(dim2,ver.getCoord(dim2)-y1);
                        }
                        
                	}
                    break;
                case 5:
                    v = TVertex.centerOfGroup(uvselection);
                    double cxs = v.getCoord(dim1);
                    double cys = v.getCoord(dim2);
                    double dxs = mouseStart.x-cxs;
                    double dys = mouseStart.y-cys;
                    double startDist = Math.sqrt(dxs*dxs+dys*dys);
                    dxs = mouseStop.x-cxs;
                    dys = mouseStop.y-cys;
                    double endDist = Math.sqrt(dxs*dxs+dys*dys);
                    double distRatio = endDist/startDist;
                    cxs = v.getCoord((byte)0);
                    cys = v.getCoord((byte)1);
                    for( TVertex ver: uvselection )
                    {
                        dxs = ver.getCoord((byte)0)-cxs;
                        dys = ver.getCoord((byte)1)-cys;
                        //startDist is now the distance to vertex from center,
                        // endDist is now the change in distance of mouse
                    	if( !uvpanel.getDimLock(0) )
                        ver.translateCoord((byte)0,dxs*(distRatio-1));
                    	if( !uvpanel.getDimLock(1) )
                        ver.translateCoord((byte)1,dys*(distRatio-1));
                    	if( !uvpanel.getDimLock(0) )
                        ((UVMoveAction)currentUVAction).moveVectors.get(uvselection.indexOf(ver)).translateCoord((byte)0,dxs*(distRatio-1));
                    	if( !uvpanel.getDimLock(1) )
                        ((UVMoveAction)currentUVAction).moveVectors.get(uvselection.indexOf(ver)).translateCoord((byte)1,dys*(distRatio-1));
                    }
                    break;
                case 6:// NO EXTRUSIONS IN UV MODE
    
                    break;
                case 7:// NO EXTRUSIONS IN UV MODE
                	
                    break;
            }
        }
    }
    public void extrudeSelection(Point2D.Double mouseStart, Point2D.Double mouseStop, byte dim1, byte dim2)
    {
        if( !lockdown )
        {
            beenSaved = false;
            double deltaXe = mouseStop.x - mouseStart.x;
            double deltaYe = mouseStop.y - mouseStart.y;
            
            ArrayList<GeosetVertex> copies = new ArrayList<GeosetVertex>();
            ArrayList<Triangle> selTris = new ArrayList<Triangle>();
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex vert = selection.get(i);
                if( vert.getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)vert;
                    copies.add(new GeosetVertex(gv));
                    
                    for( int ti = 0; ti < gv.triangles.size(); ti++ )
                    {
                        Triangle temp = gv.triangles.get(ti);
                        if( !selTris.contains(temp) )
                        {
                            selTris.add(temp);
                        }
                    }
                }
                else
                {
                    copies.add(null);
                    System.out.println("GeosetVertex "+i+" was not found.");
                }
                vert.translateCoord(dim1,deltaXe);
                vert.translateCoord(dim2,deltaYe);
            }
            for( Triangle tri: selTris )
            {
                if( !selection.contains(tri.get(0))
                    ||!selection.contains(tri.get(1))
                    ||!selection.contains(tri.get(2)) )
                {
                    for( int i = 0; i < 3; i++ )
                    {
                        GeosetVertex a = tri.get(i);
                        if( selection.contains(a) )
                        {
                            GeosetVertex b = copies.get(selection.indexOf(a));
                            tri.set(i,b);
                            a.triangles.remove(tri);
                            b.triangles.add(tri);
                        }
                    }
                }
            }
            System.out.println(selection.size()+" verteces cloned into "+copies.size()+ " more.");
            ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
            for( int k = 0; k < selection.size(); k++ )
            {
                Vertex vert = selection.get(k);
                if( vert.getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)vert;
                    ArrayList<Triangle> gvTriangles = new ArrayList<Triangle>(gv.triangles);
    //                 for( Triangle tri: gv.geoset.m_triangle )
    //                 {
    //                     if( tri.contains(gv) )
    //                     {
    // //                         boolean good = true;
    // //                         for(  Vertex vTemp: tri.getAll() )
    // //                         {
    // //                             if( !selection.contains( vTemp ) )
    // //                             {
    // //                                 good = false;
    // //                                 break;
    // //                             }
    // //                         }
    // //                         if( good )
    //                         gvTriangles.add(tri);
    //                     }
    //                 }
                    for( Triangle tri: gvTriangles )
                    {
                        for( int gvI = 0; gvI < tri.getAll().length; gvI++ )
                        {
                            GeosetVertex gvTemp = tri.get(gvI);
                            if( !gvTemp.equalLocs(gv) )
                            {
                                int ctCount = 0;
                                Triangle temp = null;
                                boolean okay = false;
                                for( Triangle triTest: gvTriangles )
                                {
                                    if( triTest.contains(gvTemp) )
                                    {
                                        ctCount++;
                                        temp = triTest;
                                        if( temp.containsRef(gvTemp) && temp.containsRef(gv) )
                                        {
                                            okay = true;
                                        }
                                    }
                                }
                                if( okay && ctCount == 1 && selection.contains(gvTemp) )
                                {
                                    GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
                                    GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));
                                    if( gvCopy == null )
                                    {
                                        System.out.println("Vertex (gvCopy) copy found as null!");
                                    }
                                    if( gvTempCopy == null )
                                    {
                                        System.out.println("Vertex (gvTempCopy) copy found as null!");
                                    }
                                    Triangle newFace = new Triangle(null,null,null,gv.geoset);
                                    
                                    int indexA = temp.indexOf(gv);
                                    int indexB = temp.indexOf(gvTemp);
                                    int indexC = -1;
                                    
                                    for( int i = 0; i < 3 && indexC == -1; i++ )
                                    {
                                        if( i != indexA && i != indexB )
                                        {
                                            indexC = i;
                                        }
                                    }
                                    
                                    System.out.println(" Indeces: "+indexA+","+indexB+","+indexC);
                                    
                                    newFace.set(indexA,gv);
                                    newFace.set(indexB,gvTemp);
                                    newFace.set(indexC,gvCopy);
                                    //Make sure it's included later
    //                                 gvTemp.triangles.add(newFace);
    //                                 gv.triangles.add(newFace);
    //                                 gvCopy.triangles.add(newFace);
    //                                 gv.geoset.addTriangle(newFace);
                                    boolean bad = false;
                                    for( Triangle t: newTriangles )
                                    {
    //                                     if( t.equals(newFace) )
    //                                     {
    //                                         bad = true;
    //                                         break;
    //                                     }
                                        if( t.contains(gv) && t.contains(gvTemp) )
                                        {
                                            bad = true;
                                            break;
                                        }
                                    }
                                    if( !bad )
                                    {
                                        newTriangles.add(newFace);
                                        
                                        System.out.println("New Face: ");
                                        System.out.println(newFace.get(0));
                                        System.out.println(newFace.get(1));
                                        System.out.println(newFace.get(2));
                                        
                                        newFace = new Triangle(null,null,null,gv.geoset);
                                        
                                        newFace.set(indexA,gvCopy);
                                        newFace.set(indexB,gvTemp);
                                        newFace.set(indexC,gvTempCopy);
                                        //Make sure it's included later
                                        newTriangles.add(newFace);
                                        
                                        System.out.println("New Alternate Face: ");
                                        System.out.println(newFace.get(0));
                                        System.out.println(newFace.get(1));
                                        System.out.println(newFace.get(2));
                                        
                                    }
                                }
                            }
                        }
                    }
                }
            }
           
    
            for( Triangle t: newTriangles )
            {
                for( GeosetVertex gv: t.getAll() )
                {
                    if( !gv.triangles.contains(t) )
                    {
                        gv.triangles.add(t);
                    }
                    if( !gv.geoset.contains(t) )
                    {
                        gv.geoset.addTriangle(t);
                    }
                }
            }
            for( GeosetVertex cgv: copies )
            {
                if( cgv != null )
                {
                    boolean inGeoset = false;
                    for( Triangle t: cgv.geoset.m_triangle )
                    {
                        if( t.containsRef(cgv) )
                        {
                            inGeoset = true;
                            break;
                        }
                    }
                    if( inGeoset )
                    cgv.geoset.addVertex(cgv);
                }
            }
        }
    }
    public void clone(ArrayList<Vertex> source, boolean selectCopies)
    {
        ArrayList<Vertex> oldSelection = new ArrayList<Vertex>(selection);
        
        ArrayList<GeosetVertex> vertCopies = new ArrayList<GeosetVertex>();
        ArrayList<Triangle> selTris = new ArrayList<Triangle>();
        ArrayList<IdObject> selBones = new ArrayList<IdObject>();
        ArrayList<IdObject> newBones = new ArrayList<IdObject>();
        for( int i = 0; i < source.size(); i++ )
        {
            Vertex vert = source.get(i);
            if( vert.getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)vert;
                vertCopies.add(new GeosetVertex(gv));
                
                
//                 for( int ti = 0; ti < gv.triangles.size(); ti++ )
//                 {
//                     Triangle temptr = gv.triangles.get(ti);
//                     if( !selTris.contains(temptr) )
//                     {
//                         selTris.add(temptr);
//                     }
//                 }
            }
            else
            {
                vertCopies.add(null);
            }
        }
        for( IdObject b: model.m_idobjects )
        {
            if( source.contains(b.pivotPoint) && !selBones.contains(b) )
            {
                selBones.add(b);
                newBones.add(b.copy());
            }
        }
        ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
        for( int k = 0; k < source.size(); k++ )
        {
            Vertex vert = source.get(k);
            if( vert.getClass() == GeosetVertex.class )
            {
                GeosetVertex gv = (GeosetVertex)vert;
                ArrayList<Triangle> gvTriangles = new ArrayList<Triangle>();//gv.triangles);
                //WHY IS GV.TRIANGLES WRONG????
                for( Triangle tri: gv.geoset.m_triangle )
                {
                    if( tri.contains(gv) )
                    {
                        boolean good = true;
                        for(  Vertex vTemp: tri.getAll() )
                        {
                            if( !source.contains( vTemp ) )
                            {
                                good = false;
                                break;
                            }
                        }
                        if( good )
                        {
                            gvTriangles.add(tri);
                            if( !selTris.contains(tri) )
                            {
                                selTris.add(tri);
                            }
                        }
                    }
                }
            }
        }
        for( Triangle tri: selTris )
        {
            GeosetVertex a = vertCopies.get(source.indexOf(tri.get(0)));
            GeosetVertex b = vertCopies.get(source.indexOf(tri.get(1)));
            GeosetVertex c = vertCopies.get(source.indexOf(tri.get(2)));
            newTriangles.add(new Triangle(a,b,c,a.geoset));
        }
        for( GeosetVertex gv: vertCopies )
        {
            if( gv != null )
            model.add(gv);
        }
        for( Triangle tri: newTriangles )
        {
            if( tri != null )
            model.add(tri);
            tri.forceVertsUpdate();
        }
        for( IdObject b: newBones )
        {
            if( b != null )
            model.add(b);
        }
        if( selectCopies )
        {
            selection.clear();
            for( Vertex ver: vertCopies )
            {
                if( ver != null )
                {
                    selection.add(ver);
                    if( ver.getClass() == GeosetVertex.class )
                    {
                        GeosetVertex gv = (GeosetVertex)ver;
                        for( int i = 0; i < gv.bones.size(); i++ )
                        {
                            Bone b = gv.bones.get(i);
                            if( selBones.contains(b) )
                            {
                                gv.bones.set(i,(Bone)newBones.get(selBones.indexOf(b)));
                            }
                        }
                    }
                }
            }
//             for( IdObject b: newBones )
//             {
//                 if( b != null )
//                 {
//                     selection.add(b.pivotPoint);
//                 }
//             }
            for( IdObject b: newBones )
            {
                selection.add(b.pivotPoint);
                if( selBones.contains(b.parent) )
                {
                    b.parent = newBones.get(selBones.indexOf(b.parent));
                }
            }
        }
    }
    public void startAction(Point2D.Double mouseStart, byte dim1, byte dim2, int actionType)
    {
        //Points need to be in geometry/model space
        if( ! lockdown )
        {
            beenSaved = false;
            if( this.actionType == -1 )
            {
                this.actionType = actionType;
                if( MainFrame.panel.cloneOn )
                {
                    clone(selection,true);
                    MainFrame.panel.setCloneOn(false);
                }
                switch( actionType )
                {
                    case 3: 
                        MoveAction temp = new MoveAction();
                        temp.storeSelection(selection);
                        temp.createEmptyMoveVector();
                        temp.actType = actionType;
                        currentAction = temp;
                        break;
                    case 4:
                        temp = new RotateAction();
                        temp.storeSelection(selection);
                        temp.createEmptyMoveVectors();
                        temp.actType = actionType;
                        currentAction = temp;
                        break;
                    case 5:
                        temp = new MoveAction();
                        temp.storeSelection(selection);
                        temp.createEmptyMoveVectors();
                        temp.actType = actionType;
                        currentAction = temp;
                        break;
                    case 6:
                        ArrayList<GeosetVertex> copies = new ArrayList<GeosetVertex>();
                        ArrayList<Triangle> selTris = new ArrayList<Triangle>();
                        for( int i = 0; i < selection.size(); i++ )
                        {
                            Vertex vert = selection.get(i);
                            if( vert.getClass() == GeosetVertex.class )
                            {
                                GeosetVertex gv = (GeosetVertex)vert;
                                copies.add(new GeosetVertex(gv));
                                
                                for( int ti = 0; ti < gv.triangles.size(); ti++ )
                                {
                                    Triangle temptr = gv.triangles.get(ti);
                                    if( !selTris.contains(temptr) )
                                    {
                                        selTris.add(temptr);
                                    }
                                }
                            }
                            else
                            {
                                copies.add(null);
                                System.out.println("GeosetVertex "+i+" was not found.");
                            }
                        }
                        for( Triangle tri: selTris )
                        {
                            if( !selection.contains(tri.get(0))
                                ||!selection.contains(tri.get(1))
                                ||!selection.contains(tri.get(2)) )
                            {
                                for( int i = 0; i < 3; i++ )
                                {
                                    GeosetVertex a = tri.get(i);
                                    if( selection.contains(a) )
                                    {
                                        GeosetVertex b = copies.get(selection.indexOf(a));
                                        tri.set(i,b);
                                        a.triangles.remove(tri);
                                        if( a.triangles.contains(tri) )
                                        {
                                            System.out.println("It's a bloody war!");
                                        }
                                        b.triangles.add(tri);
                                    }
                                }
                            }
                        }
                        System.out.println(selection.size()+" verteces cloned into "+copies.size()+ " more.");
                        ArrayList<Triangle> newTriangles = new ArrayList<Triangle>();
                        for( int k = 0; k < selection.size(); k++ )
                        {
                            Vertex vert = selection.get(k);
                            if( vert.getClass() == GeosetVertex.class )
                            {
                                GeosetVertex gv = (GeosetVertex)vert;
                                ArrayList<Triangle> gvTriangles = new ArrayList<Triangle>();//gv.triangles);
                                //WHY IS GV.TRIANGLES WRONG????
                                for( Triangle tri: gv.geoset.m_triangle )
                                {
                                    if( tri.contains(gv) )
                                    {
                                        boolean good = true;
                                        for(  Vertex vTemp: tri.getAll() )
                                        {
                                            if( !selection.contains( vTemp ) )
                                            {
                                                good = false;
                                                break;
                                            }
                                        }
                                        if( good )
                                        gvTriangles.add(tri);
                                    }
                                }
                                for( Triangle tri: gvTriangles )
                                {
                                    for( GeosetVertex copyVer: copies )
                                    {
                                        if( copyVer != null )
                                        {
                                            if( tri.containsRef(copyVer) )
                                            {
                                                System.out.println("holy brejeezers!");
                                            }
                                        }
                                    }
                                    for( int gvI = 0; gvI < tri.getAll().length; gvI++ )
                                    {
                                        GeosetVertex gvTemp = tri.get(gvI);
                                        if( !gvTemp.equalLocs(gv) && gvTemp.geoset == gv.geoset )
                                        {
                                            int ctCount = 0;
                                            Triangle temptr = null;
                                            boolean okay = false;
                                            for( Triangle triTest: gvTriangles )
                                            {
                                                if( triTest.contains(gvTemp) )
                                                {
                                                    ctCount++;
                                                    temptr = triTest;
                                                    if( temptr.containsRef(gvTemp) && temptr.containsRef(gv) )
                                                    {
                                                        okay = true;
                                                    }
                                                }
                                            }
                                            if( okay && ctCount == 1 && selection.contains(gvTemp) )
                                            {
                                                GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
                                                GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));
                                                if( gvCopy == null )
                                                {
                                                    System.out.println("Vertex (gvCopy) copy found as null!");
                                                }
                                                if( gvTempCopy == null )
                                                {
                                                    System.out.println("Vertex (gvTempCopy) copy found as null!");
                                                }
                                                Triangle newFace = new Triangle(null,null,null,gv.geoset);
                                                
                                                int indexA = temptr.indexOf(gv);
                                                int indexB = temptr.indexOf(gvTemp);
                                                int indexC = -1;
                                                
                                                for( int i = 0; i < 3 && indexC == -1; i++ )
                                                {
                                                    if( i != indexA && i != indexB )
                                                    {
                                                        indexC = i;
                                                    }
                                                }
                                                
                                                System.out.println(" Indeces: "+indexA+","+indexB+","+indexC);
                                                
                                                newFace.set(indexA,gv);
                                                newFace.set(indexB,gvTemp);
                                                newFace.set(indexC,gvCopy);
                                                //Make sure it's included later
                //                                 gvTemp.triangles.add(newFace);
                //                                 gv.triangles.add(newFace);
                //                                 gvCopy.triangles.add(newFace);
                //                                 gv.geoset.addTriangle(newFace);
                                                boolean bad = false;
                                                for( Triangle t: newTriangles )
                                                {
                //                                     if( t.equals(newFace) )
                //                                     {
                //                                         bad = true;
                //                                         break;
                //                                     }
                                                    if( t.contains(gv) && t.contains(gvTemp) )
                                                    {
                                                        bad = true;
                                                        break;
                                                    }
                                                }
                                                if( !bad )
                                                {
                                                    newTriangles.add(newFace);
                                                    
                                                    System.out.println("New Face: ");
                                                    System.out.println(newFace.get(0));
                                                    System.out.println(newFace.get(1));
                                                    System.out.println(newFace.get(2));
                                                    
                                                    newFace = new Triangle(null,null,null,gv.geoset);
                                                    
                                                    newFace.set(indexA,gvCopy);
                                                    newFace.set(indexB,gvTemp);
                                                    newFace.set(indexC,gvTempCopy);
                                                    //Make sure it's included later
                                                    newTriangles.add(newFace);
                                                    
                                                    System.out.println("New Alternate Face: ");
                                                    System.out.println(newFace.get(0));
                                                    System.out.println(newFace.get(1));
                                                    System.out.println(newFace.get(2));
                                                    
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                       
                
                        for( Triangle t: newTriangles )
                        {
                            for( GeosetVertex gv: t.getAll() )
                            {
                                if( !gv.triangles.contains(t) )
                                {
                                    gv.triangles.add(t);
                                }
                                if( !gv.geoset.contains(t) )
                                {
                                    gv.geoset.addTriangle(t);
                                }
                            }
                        }
                        for( GeosetVertex cgv: copies )
                        {
                            if( cgv != null )
                            {
                                boolean inGeoset = false;
                                for( Triangle t: cgv.geoset.m_triangle )
                                {
                                    if( t.containsRef(cgv) )
                                    {
                                        inGeoset = true;
                                        break;
                                    }
                                }
                                if( inGeoset )
                                cgv.geoset.addVertex(cgv);
                            }
                        }
                        int probs = 0;
                        for( int k = 0; k < selection.size(); k++ )
                        {
                            Vertex vert = selection.get(k);
                            if( vert.getClass() == GeosetVertex.class )
                            {
                                GeosetVertex gv = (GeosetVertex)vert;
                                for( Triangle t: gv.triangles )
                                {
                                    System.out.println("SHOULD be one: "+Collections.frequency(gv.triangles,t));
                                    if( !t.containsRef(gv) )
                                    {
                                        probs++;
                                    }
                                }
                            }
                        }
                        System.out.println("Extrude finished with "+probs+ " inexplicable errors.");
                        ExtrudeAction tempe = new ExtrudeAction();
                        tempe.storeSelection(selection);
                        tempe.type = true;
                        tempe.storeBaseMovement(new Vertex(0,0,0));
                        tempe.addedTriangles = newTriangles;
                        tempe.addedVerts = copies;
                        currentAction = tempe;
                        break;
                    case 7:
                        
                        ArrayList<Triangle> edges = new ArrayList<Triangle>();
                        ArrayList<Triangle> brokenFaces = new ArrayList<Triangle>();
                        
                        copies = new ArrayList<GeosetVertex>();
                        selTris = new ArrayList<Triangle>();
                        for( int i = 0; i < selection.size(); i++ )
                        {
                            Vertex vert = selection.get(i);
                            if( vert.getClass() == GeosetVertex.class )
                            {
                                GeosetVertex gv = (GeosetVertex)vert;
        //                         copies.add(new GeosetVertex(gv));
                                
        //                         selTris.addAll(gv.triangles);
                                for( int ti = 0; ti < gv.triangles.size(); ti++ )
                                {
                                    Triangle temptr = gv.triangles.get(ti);
                                    if( !selTris.contains(temptr) )
                                    {
                                        selTris.add(temptr);
                                    }
                                }
                            }
                            else
                            {
        //                         copies.add(null);
                                System.out.println("GeosetVertex "+i+" was not found.");
                            }
                        }
                        System.out.println(selection.size()+" verteces cloned into "+copies.size()+ " more.");
                        newTriangles = new ArrayList<Triangle>();
                        ArrayList<GeosetVertex> copiedGroup = new ArrayList<GeosetVertex>();
                        for( Triangle tri: selTris )
                        {
                            if( !selection.contains(tri.get(0))
                                ||!selection.contains(tri.get(1))
                                ||!selection.contains(tri.get(2)) )
                            {
                                int selVerts = 0;
                                GeosetVertex gv = null;
                                GeosetVertex gvTemp = null;
                                GeosetVertex gvCopy = null;//copies.get(selection.indexOf(gv));
                                GeosetVertex gvTempCopy = null;//copies.get(selection.indexOf(gvTemp));
                                for( int i = 0; i < 3; i++ )
                                {
                                    GeosetVertex a = tri.get(i);
                                    if( selection.contains(a) )
                                    {
                                        selVerts++;
                                        GeosetVertex b = new GeosetVertex(a);
                                        copies.add(b);
                                        copiedGroup.add(a);
                                        tri.set(i,b);
                                        a.triangles.remove(tri);
                                        b.triangles.add(tri);
                                        if( gv == null )
                                        {
                                            gv = a;
                                            gvCopy = b;
                                        }
                                        else if( gvTemp == null )
                                        {
                                            gvTemp = a;
                                            gvTempCopy = b;
                                        }
                                    }
                                }
                                if( selVerts == 2 )
                                {
                                    if( gvCopy == null )
                                    {
                                        System.out.println("Vertex (gvCopy) copy found as null!");
                                    }
                                    if( gvTempCopy == null )
                                    {
                                        System.out.println("Vertex (gvTempCopy) copy found as null!");
                                    }
                                    Triangle newFace = new Triangle(null,null,null,gv.geoset);
                                    
                                    int indexA = tri.indexOf(gvCopy);
                                    int indexB = tri.indexOf(gvTempCopy);
                                    int indexC = -1;
                                    
                                    for( int i = 0; i < 3 && indexC == -1; i++ )
                                    {
                                        if( i != indexA && i != indexB )
                                        {
                                            indexC = i;
                                        }
                                    }
                                    
                                    System.out.println(" Indeces: "+indexA+","+indexB+","+indexC);
                                    
                                    newFace.set(indexA,gv);
                                    newFace.set(indexB,gvTemp);
                                    newFace.set(indexC,gvCopy);
                                    //Make sure it's included later
                                    gvTemp.triangles.add(newFace);
                                    gv.triangles.add(newFace);
                                    gvCopy.triangles.add(newFace);
                                    gv.geoset.addTriangle(newFace);
                                    newTriangles.add(newFace);
                                    
                                    System.out.println("New Face: ");
                                    System.out.println(newFace.get(0));
                                    System.out.println(newFace.get(1));
                                    System.out.println(newFace.get(2));
                                    
                                    newFace = new Triangle(null,null,null,gv.geoset);
                                    
                                    newFace.set(indexA,gvCopy);
                                    newFace.set(indexB,gvTemp);
                                    newFace.set(indexC,gvTempCopy);
                                    //Make sure it's included later
                                    gvCopy.triangles.add(newFace);
                                    gvTemp.triangles.add(newFace);
                                    gvTempCopy.triangles.add(newFace);
                                    gv.geoset.addTriangle(newFace);
                                    newTriangles.add(newFace);
                                    
                                    System.out.println("New Alternate Face: ");
                                    System.out.println(newFace.get(0));
                                    System.out.println(newFace.get(1));
                                    System.out.println(newFace.get(2));
                                }
                            }
                        }
        
                        for( GeosetVertex cgv: copies )
                        {
                            if( cgv != null )
                            {
                                cgv.geoset.addVertex(cgv);
                            }
                        }
                        
                        tempe = new ExtrudeAction();
                        tempe.storeSelection(selection);
                        tempe.type = false;
                        tempe.storeBaseMovement(new Vertex(0,0,0));
                        tempe.addedTriangles = newTriangles;
                        tempe.addedVerts = copies;
                        tempe.copiedGroup = copiedGroup;
                        currentAction = tempe;
                        break;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null,"UI Error: Cannot perform two actions at once.");
            }
        }
    }

    public void startUVAction(Point2D.Double mouseStart, int actionTypeUV)
    {
        //Points need to be in geometry/model space
        if( ! lockdown )
        {
        	byte dim1 = 0;
        	byte dim2 = 1;
            beenSaved = false;
            if( this.actionTypeUV == -1 )
            {
                this.actionTypeUV = actionTypeUV;
                switch( actionTypeUV )
                {
                    case 3: 
                        UVMoveAction temp = new UVMoveAction();
                        temp.storeSelection(uvselection);
                        temp.createEmptyMoveVector();
                        temp.actType = actionTypeUV;
                        currentUVAction = temp;
                        break;
                    case 4:
                        temp = new UVMoveAction();
                        temp.storeSelection(uvselection);
                        temp.createEmptyMoveVectors();
                        temp.actType = actionTypeUV;
                        currentUVAction = temp;
                        break;
                    case 5:
                        temp = new UVMoveAction();
                        temp.storeSelection(uvselection);
                        temp.createEmptyMoveVectors();
                        temp.actType = actionTypeUV;
                        currentUVAction = temp;
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(null,"UI Error: Cannot perform two actions at once.");
            }
        }
    }
    public void finishUVAction(Point2D.Double mouseStart, Point2D.Double mouseStop)
    {
        if( !lockdown )
        {
            beenSaved = false;
            //Points need to be in geometry/model space
            
            
    //         switch( actionType )
    //         {
    //             case 3:
    //                 break;
    //             case 4:
    //                 break;
    //             case 5:
    //                 break;
    //             case 6:
    //                 break;
    //             case 7:
    //                 break;
    //         }
            updateUVAction(mouseStart,mouseStop);
            redoStack.clear();
            actionStack.add(currentUVAction);
            currentUVAction = null;
            actionTypeUV = -1;
        }
    }
    public void finishAction(Point2D.Double mouseStart, Point2D.Double mouseStop, byte dim1, byte dim2)
    {
        if( !lockdown )
        {
            beenSaved = false;
            //Points need to be in geometry/model space
            
            
    //         switch( actionType )
    //         {
    //             case 3:
    //                 break;
    //             case 4:
    //                 break;
    //             case 5:
    //                 break;
    //             case 6:
    //                 break;
    //             case 7:
    //                 break;
    //         }
            updateAction(mouseStart,mouseStop,dim1,dim2);
            redoStack.clear();
            actionStack.add(currentAction);
            currentAction = null;
            actionType = -1;
        }
    }
    
    public void snap()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<Vertex> oldLocations = new ArrayList<Vertex>();
            Vertex cog = Vertex.centerOfGroup(selection);
            for( int i = 0; i < selection.size(); i++ )
            {
                oldLocations.add(new Vertex(selection.get(i)));
            }
            SnapAction temp = new SnapAction(selection,oldLocations,cog);
            temp.redo();//a handy way to do the snapping!
            actionStack.add(temp);
        }
    }
    
    public void snapUVs()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<TVertex> oldLocations = new ArrayList<TVertex>();
            TVertex cog = TVertex.centerOfGroup(uvselection);
            for( int i = 0; i < uvselection.size(); i++ )
            {
                oldLocations.add(new TVertex(uvselection.get(i)));
            }
            UVSnapAction temp = new UVSnapAction(uvselection,oldLocations,cog);
            temp.redo();//a handy way to do the snapping!
            actionStack.add(temp);
        }
    }
    
    public void delete()
    {
        if( !lockdown )
        {
        	ArrayList<Geoset> remGeosets = new ArrayList<Geoset>();//model.m_geosets
            beenSaved = false;
            ArrayList<Triangle> deletedTris = new ArrayList<Triangle>();
            for( int i = 0; i < selection.size(); i++ )
            {
                if( selection.get(i).getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)selection.get(i);
                    for( Triangle t: gv.triangles )
                    {
                        t.m_geoRef.removeTriangle(t);
                        if( !deletedTris.contains(t) )
                        deletedTris.add(t);
                    }
                    gv.geoset.remove(gv);
                }
            }
            for( int i = model.m_geosets.size()-1; i >= 0; i-- )
            {
            	if( model.m_geosets.get(i).isEmpty() )
            	{
            		Geoset g = model.getGeoset(i);
            		remGeosets.add(g);
            		model.remove(g);
            		if( g.geosetAnim != null )
            		{
            			model.remove(g.geosetAnim);
            		}
            	}
            }
            if( remGeosets.size() <= 0 )
            {
                DeleteAction temp = new DeleteAction(selection,deletedTris);
                actionStack.add(temp);
            }
            else
            {
                SpecialDeleteAction temp = new SpecialDeleteAction(selection,deletedTris,remGeosets,model);
                actionStack.add(temp);
            }
        }
    }
    
    public void selectAll()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<Vertex> oldSelection = new ArrayList<Vertex>(selection);
            selection.clear();
            for( Geoset geo: editableGeosets )
            {
                for( GeosetVertex v: geo.m_vertex )
                {
                    if( !selection.contains(v) )
                    selection.add(v);
                }
            }
            for( IdObject o: model.m_idobjects )
            {
                Vertex v = o.pivotPoint;
                if( !selection.contains(v) )
                selection.add(v);
            }
            SelectAction temp = new SelectAction(oldSelection,selection,this,3);
            actionStack.add(temp);
        }
    }
    
    public void selectAllUV()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<TVertex> oldSelection = new ArrayList<TVertex>(uvselection);
            uvselection.clear();
            for( Geoset geo: editableGeosets )
            {
            	for( int i = 0; i < geo.m_vertex.size(); i++ )
            	{
            		TVertex v = geo.m_vertex.get(i).getTVertex(uvpanel.currentLayer());
                    if( !uvselection.contains(v) )
                    uvselection.add(v);
            	}
            }
            UVSelectAction temp = new UVSelectAction(oldSelection,uvselection,this,3);
            actionStack.add(temp);
        }
    }
    
    public void viewMatrices()
    {
    	ArrayList<Bone> boneRefs = new ArrayList<Bone>();
    	for( Vertex ver: selection )
    	{
    		if( ver instanceof GeosetVertex )
    		{
    			GeosetVertex gv = (GeosetVertex)ver;
    			for( Bone b: gv.bones )
    			{
    				if( !boneRefs.contains(b) )
    				boneRefs.add(b);
    			}
    		}
    	}
    	String boneList = "";
    	for( int i = 0; i < boneRefs.size(); i++ )
    	{
    		if( i == boneRefs.size() - 2)
    		{
        		boneList = boneList + boneRefs.get(i).getName() + " and ";
    		}
    		else if( i == boneRefs.size() - 1)
    		{
        		boneList = boneList + boneRefs.get(i).getName();
    		}
    		else
    		{
        		boneList = boneList + boneRefs.get(i).getName() + ", ";
    		}
    	}
    	if( boneRefs.size() == 0 )
    	{
    		boneList = "Nothing was selected that was attached to any bones.";
    	}
    	JTextArea tpane = new JTextArea(boneList);
    	tpane.setLineWrap(true);
    	tpane.setWrapStyleWord(true);
    	tpane.setEditable(false);
    	tpane.setSize(230, 400);
    	
    	JScrollPane jspane = new JScrollPane(tpane);
    	jspane.setPreferredSize(new Dimension(270, 230));
    	
        JOptionPane.showMessageDialog(null,jspane);
        //for( IdObject obj: selBones )
        //{
        	
        //}
    }
    
    public void insideOut()
    {
        //Called both by a menu button and by the mirroring function
        if( !lockdown )
        {
            ArrayList<Triangle> selTris = new ArrayList<Triangle>();
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex vert = selection.get(i);
                if( vert.getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)vert;
                    
                    for( int ti = 0; ti < gv.triangles.size(); ti++ )
                    {
                        Triangle temptr = gv.triangles.get(ti);
                        if( !selTris.contains(temptr) )
                        {
                            selTris.add(temptr);
                        }
                    }
                }
                else
                {
                    System.out.println("GeosetVertex "+i+" was not found for \"insideOut\" function.");
                }
            }
            
            for( int i = selTris.size()-1; i>= 0; i-- )
            {
            	boolean goodTri = true;
            	for(Vertex v: selTris.get(i).getAll())
            	{
            		if( !selection.contains(v) )
            		{
            			goodTri = false;
            		}
            	}
            	if( !goodTri )
            	{
            		selTris.remove(i);
            	}
            }
            
            for( Triangle tri: selTris )
            {
                tri.flip();
            }
        }
    }
    
    public void mirror(byte dim, boolean flipModel)
    {
        if( !lockdown )
        {
            byte mirrorDim = dim;
            Vertex center = Vertex.centerOfGroup(selection);//Calc center of mass
            for( int i = 0; i < selection.size(); i++ )
            {
                Vertex vert = selection.get(i);
                vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
                if( vert.getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)vert;
                    Normal normal = gv.normal;
                    //Flip normals, preserve lighting!
                    normal.setCoord(mirrorDim,-normal.getCoord(mirrorDim));
                }
            }
            ArrayList<IdObject> selBones = new ArrayList<IdObject>();
            for( IdObject b: model.m_idobjects )
            {
                if( selection.contains(b.pivotPoint) && !selBones.contains(b) )
                {
                    selBones.add(b);
                }
            }
            for( IdObject obj: selBones )
            {
                obj.flipOver(dim);
            }
            if( flipModel )
            {
                insideOut();
            }
        }
    }
    
    public void mirrorUV(byte dim)
    {
        if( !lockdown )
        {
            byte mirrorDim = dim;
            TVertex center = TVertex.centerOfGroup(uvselection);//Calc center of mass
            for( int i = 0; i < uvselection.size(); i++ )
            {
                TVertex vert = uvselection.get(i);
                vert.setCoord(mirrorDim, 2 * center.getCoord(mirrorDim) - vert.getCoord(mirrorDim));
            }
        }
    }
    
    public void expandSelection()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<Vertex> oldSelection = new ArrayList<Vertex>(selection);
            ArrayList<Triangle> oldTris = new ArrayList<Triangle>();
            for( Vertex v: oldSelection )
            {
                if( v instanceof GeosetVertex )
                {
                    GeosetVertex gv = (GeosetVertex)v;
                    for( Triangle triangle: gv.triangles )
                    {
                        if( !oldTris.contains( triangle ) )
                        {
                            oldTris.add(triangle);
                        }
                    }
                }
            }
//             selection.clear();
            for( Geoset geo: editableGeosets )
            {
                for( GeosetVertex v: geo.m_vertex )
                {
                    for( Triangle tri: oldTris )
                    {
                        if( tri.containsRef(v) )
                        {
                            if( !selection.contains(v) )
                            selection.add(v);
                        }
                    }
                }
            }
            SelectAction temp = new SelectAction(oldSelection,selection,this,5);
            actionStack.add(temp);
        }
    }
    public void expandSelectionUV()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<TVertex> oldSelection = new ArrayList<TVertex>(uvselection);
            
            //*** update parenting system
            for( Geoset geo: editableGeosets )
            {
            	for( int i = 0; i < geo.m_vertex.size(); i++ )
            	{
            		TVertex v = geo.m_vertex.get(i).getTVertex(uvpanel.currentLayer());
            		v.setParent(geo.m_vertex.get(i));
            	}
            }
            		
            		
            ArrayList<Triangle> oldTris = new ArrayList<Triangle>();
            for( TVertex v: oldSelection )
            {
            	GeosetVertex gv = v.getParent();
                for( Triangle triangle: gv.triangles )
                {
                    if( !oldTris.contains( triangle ) )
                    {
                        oldTris.add(triangle);
                    }
                }
            }
//             selection.clear();
            for( Geoset geo: editableGeosets )
            {
                for( GeosetVertex v: geo.m_vertex )
                {
                    for( Triangle tri: oldTris )
                    {
                        if( tri.containsRef(v) )
                        {
                            if( !uvselection.contains(v.getTVertex(uvpanel.currentLayer())) )
                            uvselection.add(v.getTVertex(uvpanel.currentLayer()));
                        }
                    }
                }
            }
            UVSelectAction temp = new UVSelectAction(oldSelection,uvselection,this,5);
            actionStack.add(temp);
        }
    }
    
    public void invertSelection()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<Vertex> oldSelection = new ArrayList<Vertex>(selection);
            for( Geoset geo: editableGeosets )
            {
                for( GeosetVertex v: geo.m_vertex )
                {
                    if( selection.contains(v) )
                    {
                        selection.remove(v);
                    }
                    else
                    {
                        selection.add(v);
                    }
                }
            }
            for( IdObject o: model.m_idobjects )
            {
                Vertex v = o.pivotPoint;
                if( selection.contains(v) )
                	selection.remove(v);
                else
                	selection.add(v);
            }
            SelectAction temp = new SelectAction(oldSelection,selection,this,4);
            actionStack.add(temp);
        }
    }
    
    public void invertSelectionUV()
    {
        if( !lockdown )
        {
            beenSaved = false;
            ArrayList<TVertex> oldSelection = new ArrayList<TVertex>(uvselection);
            for( Geoset geo: editableGeosets )
            {
            	for( int i = 0; i < geo.m_vertex.size(); i++ )
            	{
            		TVertex v = geo.m_vertex.get(i).getTVertex(uvpanel.currentLayer());
                    if( uvselection.contains(v) )
                    {
                        uvselection.remove(v);
                    }
                    else
                    {
                        uvselection.add(v);
                    }
                }
            }
            UVSelectAction temp = new UVSelectAction(oldSelection,uvselection,this,4);
            actionStack.add(temp);
        }
    }

    public void selFromMain()
    {
        if( !lockdown )
        {
            beenSaved = false;
            
            ArrayList<TVertex> oldSelection = new ArrayList<TVertex>(uvselection);
            
            uvselection.clear();
            for( Vertex ver: selection )
            {
            	if( ver instanceof GeosetVertex)
            	{
            		GeosetVertex gv = (GeosetVertex)ver;
            		TVertex myT = gv.tverts.get(uvpanel.currentLayer());
            		if( !uvselection.contains(myT) )
            		{
            			uvselection.add(myT);
            		}
            	}
            }
            UVSelectAction temp = new UVSelectAction(oldSelection,uvselection,this,6);
            actionStack.add(temp);
        }
    }
    
    public void cureSelection()
    {
        //this probably conflicts with the undo feature
        if( !lockdown )
        {
            for( int i = 0; i < selection.size(); i++ )
            {
                if( selection.get(i).getClass() == GeosetVertex.class )
                {
                    GeosetVertex gv = (GeosetVertex)selection.get(i);
                    
                    if( !editableGeosets.contains(gv.geoset) )
                    {
                        selection.remove(i);
                    }
                }
            }
        	ArrayList<IdObject> geoParents = null;
        	ArrayList<IdObject> geoSubParents = null;
        	if( dispChildren )
        	{
        		geoParents = new ArrayList<IdObject>();
        		geoSubParents = new ArrayList<IdObject>();
                for( Geoset geo: editableGeosets )
                {
                	for( GeosetVertex ver: geo.m_vertex )
                	{
                		for( Bone b: ver.bones )
                		{
                			if( !geoParents.contains(b))
                				geoParents.add(b);
                		}
                	}
                }
//        		childMap = new HashMap<IdObject,ArrayList<IdObject>>();
                for( IdObject obj: model.m_idobjects)
                {
                	if( !geoParents.contains(obj) )
                	{
                    	boolean valid = false;
                    	for( int i = 0; !valid && i < geoParents.size(); i++ )
                    	{
                    		valid = geoParents.get(i).childOf(obj);
                    	}
                    	if( valid )
                    	{
                    		geoSubParents.add(obj);
                    	}
//                    	if( obj.parent != null )
//                      	{
//                          	ArrayList<IdObject> children = childMap.get(obj.parent);
//                          	if( children == null )
//                          	{
//                          		children = new ArrayList<IdObject>();
//                          		childMap.put(obj.parent, children);
//                          	}
//                          	children.add(obj);
//                      	}
                	}
                }
                //System.out.println(geoSubParents);
        	}
            if( dispChildren )
                for( IdObject o: model.m_idobjects )
                {
//                	boolean hasRef = false;//highlight != null && highlight.containsReference(o);
//                	if( dispChildren )
//                	{
//                		for( int i = 0; !hasRef && i < editableGeosets.size(); i++ )
//                		{
//                			hasRef = editableGeosets.get(i).containsReference(o);
//                		}
//                	}
                	if( !(geoParents.contains(o) || geoSubParents.contains(o)) )//!dispChildren || hasRef )
                	{
                        Vertex ver = o.pivotPoint;
                        if( selection.contains(ver)){
                        	selection.remove(ver);
                        }
                	}
                }
            for( Geoset geo: model.m_geosets )
            {
            	if( !editableGeosets.contains(geo))
            	{
                	for( int i = 0; i < geo.m_vertex.size(); i++ )
                	{
                		TVertex v = geo.m_vertex.get(i).getTVertex(uvpanel.currentLayer());
                		if( uvselection.contains(v) )
                		{
                			uvselection.remove(v);
                		}
                	}
            	}
            }
        }
    }
    
    public void undo()
    {
        if( !lockdown )
        {
            beenSaved = false;
            if( actionStack.size() > 0 )
            {
                UndoAction temp = actionStack.get(actionStack.size()-1);
                actionStack.remove(temp);
                temp.undo();
                redoStack.add(temp);
                
                cureSelection();
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Nothing to undo!");
            }
        }
    }
    public boolean canUndo()
    {
        return ( actionStack.size() > 0 );
    }
    public boolean canRedo()
    {
        return ( redoStack.size() > 0 );
    }
    public void redo()
    {
        if( !lockdown )
        {
            beenSaved = false;
            if( redoStack.size() > 0 )
            {
                UndoAction temp = redoStack.get(redoStack.size()-1);
                redoStack.remove(temp);
                temp.redo();
                actionStack.add(temp);
                
                cureSelection();
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Nothing to redo!");
            }
        }
    }
    public String undoText()
    {
        if( canUndo() )
        {
            UndoAction temp = actionStack.get(actionStack.size()-1);
            return temp.actionName();
        }
        else
        {
            return "Can't undo";
        }
    }
    public String redoText()
    {
        if( canRedo() )
        {
            UndoAction temp = redoStack.get(redoStack.size()-1);
            return temp.actionName();
        }
        else
        {
            return "Can't redo";
        }
    }
    public void lockdown()
    {
        lockdown = true;
    }
    public void unlock()
    {
        lockdown = false;
        cureSelection();//keep it happening but not on lockdown!
    }
    public boolean beenSaved()
    {
        return beenSaved;
    }
    public void resetBeenSaved()
    {
        beenSaved = true;
    }
    
    public boolean getDimEditable(int dim)
    {
    	return !MainFrame.panel.getDimLock(dim);
    }
}
