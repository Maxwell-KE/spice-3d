/*
 *                  BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 * 
 * Created on May 15, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;


import org.biojava.bio.Annotation;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;
import org.biojava.dasobert.eventmodel.SequenceEvent;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.StructureAlignment;
import org.biojava.spice.panel.StructurePanel;
import org.biojava.spice.panel.StructurePanelListener;
import org.jmol.api.JmolViewer;

import javax.vecmath.Matrix3f;

/** a JPanel that contains radio buttons to choose, which structures to show superimposed
 * 
 * @author Andreas Prlic
 * @since 10:14:51 AM
 * @version %I% %G%
 */
public class StructureAlignmentChooser 
extends JPanel 
implements ItemListener, 
StructureAlignmentListener {
    
    static final long serialVersionUID = 65937284545329877l;
    
    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    List checkButtons;
    StructureAlignment structureAlignment;
    Box vBox;
    List structureListeners;
    List pdbSequenceListeners;
    
    int referenceStructure; // the structure at that position is the first one 
    
    JTextField searchBox;
    JScrollPane scroller;
    StructurePanel structurePanel;
    
    public final static float radiansPerDegree = (float) (2 * Math.PI / 360);
    public final static float degreesPerRadian = (float) (360 / (2 * Math.PI));
    
    boolean sortReverse;
    JMenu parent;
    JMenuItem sort;
    
    public StructureAlignmentChooser(JMenu parent) {
        super();
        this.parent = parent;
        structureListeners 		= new ArrayList();
        structureAlignment 		= new StructureAlignment(null);
        checkButtons 			= new ArrayList();
        pdbSequenceListeners 	= new ArrayList();
        
        vBox 					= Box.createVerticalBox();
        
        this.add(vBox);
        referenceStructure = -1;
        
        searchBox = new JTextField();
        searchBox.setEditable(true);
        searchBox.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        searchBox.addKeyListener(new MyKeyListener(this));
        
    }
    
    public void setStructurePanel(StructurePanel panel){
        this.structurePanel = panel;
    }
    
    public void setScroller(JScrollPane scroll){
        this.scroller = scroll;
    }
    
    
    public void setSortReverse(boolean direction){
        sortReverse = direction;
    }
    
    protected JScrollPane getScroller(){
        return scroller;
    }
    
    public JTextField getSearchBox() {
        return searchBox;
    }
    
    public void clearListeners(){
        structureAlignment = new StructureAlignment(null);
        structureListeners.clear();
        pdbSequenceListeners.clear();
        parent = null;
    }
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    public void addPDBSequenceListener(SequenceListener li){
        pdbSequenceListeners.add(li);
    }
    private void clearButtons(){
        Iterator iter = checkButtons.iterator();
        
        while (iter.hasNext()){
            JCheckBox b = (JCheckBox)iter.next();
            b.removeItemListener(this);
            vBox.remove(b);
            
        }
        checkButtons.clear();
        vBox.repaint();
    }
    
    public StructureAlignment getStructureAlignment(){
        return structureAlignment;
    }
    
    
    private void updateMenuItems(){

        if ( parent == null) {
            return;
        }

        if ( sort != null)            
            parent.remove(sort);

        AlignmentSortPopup sorter = new AlignmentSortPopup(structureAlignment, this, false);
        sort = MenuAlignmentListener.getSortMenuFromAlignment(structureAlignment.getAlignment(),sorter);
        logger.info("added new menu item " + sort);
        parent.add(sort);     
        
      
    }
    
    private String getButtonTooltip(Annotation anno){
        String tooltip = "";
        
        List details = new ArrayList();
       // System.out.println(anno);
        try {
            details = (List) anno.getProperty("details");
        } catch (NoSuchElementException e){}
        
        if ( details != null) {
        
            Iterator iter = details.iterator();
            while ( iter.hasNext()) {
                Annotation d = (Annotation) iter.next();
                String prop = (String) d.getProperty("property");
                if ( prop.equals(MenuAlignmentListener.filterProperty))
                    continue;
                String det  = (String) d.getProperty("detail");
                if (! tooltip.equals(""))
                    tooltip += " | ";
                tooltip += prop + " " + det;
            }                
        }
        return tooltip;
    }
    
    /** return true if it should be displayed
     * 
     * @param object
     * @param filterBy
     * @return flag if is visible or not
     */
    private boolean isVisibleAfterFilter(Annotation object, String filterBy){
        boolean show = true;
        if ( filterBy == null)
            return true;
        if ( filterBy.equalsIgnoreCase(MenuAlignmentListener.showAllObjects))
            return true;
        
        List details = (List) object.getProperty("details");
        Iterator iter = details.iterator();
        while ( iter.hasNext()) {
            Annotation d = (Annotation) iter.next();
            String prop = (String) d.getProperty("property");
            if (! prop.equals(MenuAlignmentListener.filterProperty))
                continue;
            String det  = (String) d.getProperty("detail");
            if (! det.equalsIgnoreCase(filterBy)){                
                return false;
            }
        }        
        return show;
    }
    
    public void setStructureAlignment(StructureAlignment ali){
        
        structureAlignment = ali;
        //logger.info("got new structure alignment");
        if ( (ali != null) && ( ali.getIds().length > 0) )
            System.setProperty("SPICE:drawStructureRegion","true");
        
                
        clearButtons();
        if ( ali == null) {
            clearButtons();
            repaint();
            return;
        }
        
        updateMenuItems();
                
        AlignmentSortPopup sorter = new AlignmentSortPopup(ali,this, sortReverse);
        
        Annotation[] objects = structureAlignment.getAlignment().getObjects();
        
        boolean[] selectedArr = ali.getSelection();
      
        String[] ids = ali.getIds();
        Color background = getBackground();
        for ( int i=0; i< ids.length;i++){
            String id = ids[i];
            Color col = structureAlignment.getColor(i);
            if ( ( i == 0 ) || (structureAlignment.isSelected(i))){
                
                UIManager.put("CheckBox.background", col);
                UIManager.put("CheckBox.interiorBackground", col);
                UIManager.put("CheckBox.highlite", col);
                
            } else {
                UIManager.put("CheckBox.background", background);
                UIManager.put("CheckBox.interiorBackground", background);
                UIManager.put("CheckBox.highlite", background);
            }
            JCheckBox b = new JCheckBox(id);
            b.addMouseListener(sorter);
            
            // get tooltip
            String tooltip = getButtonTooltip(objects[i]);
            
            boolean doShow = isVisibleAfterFilter(objects[i],structureAlignment.getFilterBy());
            if ( ! doShow) {
                b.setVisible(false);
            }
            b.setToolTipText(tooltip);
            
            boolean selected = false;
            if (selectedArr[i]) {
                selected = true;
                // always show selected / even if filtered out!
                b.setVisible(true);
            }
            
            if ( i == 0) {
                
                selected = true;
                
                referenceStructure = 0;
                structureAlignment.select(0);
                
                System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                                      
                
                try {
                    structureAlignment.getStructure(i);
                } catch (StructureException e){
                    selected = false;
                };
                
                
            }
            
            b.setSelected(selected);
            vBox.add(b);
            checkButtons.add(b);
            b.addItemListener(this);
        }
        
        //      update the structure alignment in the structure display.
        Structure newStruc = structureAlignment.createArtificalStructure();
        
        // execute Rasmol cmd...
        String cmd = structureAlignment.getRasmolScript();
        
        if ( newStruc.size() < 1)
            return;
        
        StructureEvent event = new StructureEvent(newStruc);
        
        Iterator iter2 = structureListeners.iterator();
        while (iter2.hasNext()){
            StructureListener li = (StructureListener)iter2.next();
            li.newStructure(event);
            if ( li instanceof StructurePanelListener){
                StructurePanelListener pli = (StructurePanelListener)li;
                pli.executeCmd(cmd);
            }
            
        }
        
        
        repaint();
    }
    
    /** recalculate the displayed alignment. e.g. can be called after toggle full structure
     * 
     *
     */
    public void recalcAlignmentDisplay() {
        logger.info("recalculating the alignment display");
        Structure newStruc = structureAlignment.createArtificalStructure(referenceStructure);
        String cmd = structureAlignment.getRasmolScript(referenceStructure);                    
        
        
        
        StructureEvent event = new StructureEvent(newStruc);
        Iterator iter2 = structureListeners.iterator();
        while (iter2.hasNext()){
            StructureListener li = (StructureListener)iter2.next();
            li.newStructure(event);
            if ( li instanceof StructurePanelListener){
                StructurePanelListener pli = (StructurePanelListener)li;
                pli.executeCmd(cmd);
            }
            
        }
    }
    
    protected List getCheckBoxes() {
        return checkButtons;
    }
    
    public void itemStateChanged(ItemEvent e) {
        
        Object source = e.getItemSelectable();
        Iterator iter = checkButtons.iterator();
        int i=-1;
        while (iter.hasNext()){
            i++;
            Object o = iter.next();
            JCheckBox box =(JCheckBox)o;
            if ( o.equals(source)){
                String[] ids = structureAlignment.getIds();
                String id = ids[i];
                //System.out.println("do something with " + id);
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    // remove structure from alignment
                    structureAlignment.deselect(i);
                    box.setBackground(this.getBackground());
                    box.repaint();
                    // display the first one that is selected
                    // set the color to that one 
                    //int j = structureAlignment.getFirstSelectedPos();
                    int j = structureAlignment.getLastSelectedPos();
                    if ( j > -1) {
                        
                        Color col = structureAlignment.getColor(j);
                        System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());
                        
                        
                    }
                    referenceStructure = j;
                    
                } else {
                    structureAlignment.select(i);
                    // add structure to alignment
                    Color col = structureAlignment.getColor(i);
                    
                    System.setProperty("SPICE:StructureRegionColor",new Integer(col.getRGB()).toString());                                      
                    
                    
                    UIManager.put("CheckBox.background", col);
                    UIManager.put("CheckBox.interiorBackground", col);
                    UIManager.put("CheckBox.highlite", col);
                    
                    box.setBackground(col);
                    box.repaint();
                    
                    // check if we can get the structure...
                    Structure struc = null;
                    try {
                        struc = structureAlignment.getStructure(i);
                        referenceStructure = i;
                        if ( struc.size() > 0) {
                            Chain c1 = struc.getChain(0);
                            String sequence = c1.getSequence();
                            String ac = id + "." + c1.getName();
                            
                            SequenceEvent sevent = new SequenceEvent(ac,sequence);
                            //logger.info("*** seqeunce event " + ac);
                            Iterator iter3 = pdbSequenceListeners.iterator();
                            while (iter3.hasNext()){
                                SequenceListener li = (SequenceListener)iter3.next();
                                li.newSequence(sevent);
                            }
                        } else {
                            logger.warning("could not load structure at position " +i );
                            JCheckBox b = (JCheckBox) source;
                            b.setSelected(false);
                        }
                        
                    } catch (StructureException ex){
                        ex.printStackTrace();
                        structureAlignment.deselect(i);
                        //return;
                        JCheckBox b = (JCheckBox) source;
                        b.setSelected(false);
                    }
                }
                
                
                
                Matrix jmolRotation = getJmolRotation();
                
                
                
                Structure newStruc = null; 
//              execute Rasmol cmd...
                String cmd = null;
                
                
                // update the structure alignment in the structure display.
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    newStruc = structureAlignment.createArtificalStructure();
                    cmd = structureAlignment.getRasmolScript();
                    
                }
                else {
                    newStruc = structureAlignment.createArtificalStructure(i);
                    cmd = structureAlignment.getRasmolScript(i);                    
                }
                
//                if ( newStruc != null){
//                    if ( jmolRotation != null){
//                        Structure clonedStruc = (Structure) newStruc.clone();
//                        Calc.rotate(clonedStruc,jmolRotation);
//                        newStruc = clonedStruc;
//                    }
//                }
                StructureEvent event = new StructureEvent(newStruc);
                Iterator iter2 = structureListeners.iterator();
                while (iter2.hasNext()){
                    StructureListener li = (StructureListener)iter2.next();
                    li.newStructure(event);
                    if ( li instanceof StructurePanelListener){
                        StructurePanelListener pli = (StructurePanelListener)li;
                        pli.executeCmd(cmd);
                    }
                    
                }
                
                rotateJmol(jmolRotation);
            }
           
        }
        
    }
    
    public void rotateJmol(Matrix jmolRotation) {
        if ( structurePanel != null){
            if ( jmolRotation != null) {
 
                double[] zyz = getZYZEuler(jmolRotation);
                
                String script = "reset; rotate z "+zyz[0] +"; rotate y " + zyz[1] +"; rotate z"+zyz[2]+";";

                structurePanel.executeCmd(script);
                /*structurePanel.executeCmd("show orientation");
                JmolViewer viewer = structurePanel.getViewer();
                System.out.println("rotating jmol ... " + script);
                viewer.homePosition();
                viewer.rotateToZ(Math.round(zyz[0]));
                viewer.rotateToY(Math.round(zyz[1]));
                viewer.rotateToZ(Math.round(zyz[2]));
                */
            }
        }
    }
    
    /** get the rotation out of Jmol 
     * 
     * @return the jmol rotation matrix
     */
    public Matrix getJmolRotation(){
        Matrix jmolRotation = null;
        if ( structurePanel != null){
            //structurePanel.executeCmd("show orientation;");
            JmolViewer jmol = structurePanel.getViewer();
            Object obj = jmol.getProperty(null,"transformInfo","");
            //System.out.println(obj);
            if ( obj instanceof Matrix3f ) {
                Matrix3f max = (Matrix3f) obj;
                jmolRotation = new Matrix(3,3);
                for (int x=0; x<3;x++) {
                    for (int y=0 ; y<3;y++){
                        float val = max.getElement(x,y);
                        //System.out.println("x " + x + " y " + y + " " + val);
                        jmolRotation.set(x,y,val);
                    }
                }
                
            }                             
        }    
        return jmolRotation;
    }
    
    
    /** Convert a rotation Matrix to Euler angles.
     *   This conversion uses conventions as described on page:
     *   http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
     *   Coordinate System: right hand
     *   Positive angle: right hand
     *   Order of euler angles: heading first, then attitude, then bank
     *   
     * @param m the rotation matrix
     * @return a array of three doubles containing the three euler angles in radians
     */   
    public static double[] getXYZEuler(Matrix m){
        double heading, attitude, bank;
        
        // Assuming the angles are in radians.
        if (m.get(1,0) > 0.998) { // singularity at north pole
            heading = Math.atan2(m.get(0,2),m.get(2,2));
            attitude = Math.PI/2;
            bank = 0;
            
        } else if  (m.get(1,0) < -0.998) { // singularity at south pole
            heading = Math.atan2(m.get(0,2),m.get(2,2));
            attitude = -Math.PI/2;
            bank = 0;
            
        } else {
            heading = Math.atan2(-m.get(2,0),m.get(0,0));
            bank = Math.atan2(-m.get(1,2),m.get(1,1));
            attitude = Math.asin(m.get(1,0));
        }
        return new double[] { heading, attitude, bank };
    }
    
    /** get euler angles for a matrix given in ZYZ convention 
     * 
     * @param m
     * @return the euler values for a rotation around Z, Y, Z in degrees...
     */
    public static double[] getZYZEuler(Matrix m) {
        double m22 = m.get(2,2);
        double rY = (float) Math.acos(m22) * degreesPerRadian;
        double rZ1, rZ2;
        if (m22 > .999d || m22 < -.999d) {
          rZ1 = (double) Math.atan2(m.get(1,0),  m.get(1,1)) * degreesPerRadian;
          rZ2 = 0;
        } else {
          rZ1 = (double) Math.atan2(m.get(2,1), -m.get(2,0)) * degreesPerRadian;
          rZ2 = (double) Math.atan2(m.get(1,2),  m.get(0,2)) * degreesPerRadian;
        }
        return new double[] {rZ1,rY,rZ2};
    }
    
    
    
    /** this conversion uses NASA standard aeroplane conventions as described on page:
    *   http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm
    *   Coordinate System: right hand
    *   Positive angle: right hand
    *   Order of euler angles: heading first, then attitude, then bank
    *   matrix row column ordering:
    *   [m00 m01 m02]
    *   [m10 m11 m12]
    *   [m20 m21 m22]
     * @param heading in radians
     * @param attitude  in radians
     * @param bank  in radians
     * @return the rotation matrix */
    public static Matrix matrixFromEuler(double heading, double attitude, double bank) {
        // Assuming the angles are in radians.
        double ch = Math.cos(heading);
        double sh = Math.sin(heading);
        double ca = Math.cos(attitude);
        double sa = Math.sin(attitude);
        double cb = Math.cos(bank);
        double sb = Math.sin(bank);

        Matrix m = new Matrix(3,3);
        m.set(0,0, ch * ca);
        m.set(0,1, sh*sb - ch*sa*cb);
        m.set(0,2, ch*sa*sb + sh*cb);
        m.set(1,0, sa);
        m.set(1,1, ca*cb);
        m.set(1,2, -ca*sb);
        m.set(2,0, -sh*ca);
        m.set(2,1, sh*sa*cb + ch*sb);
        m.set(2,2, -sh*sa*sb + ch*cb);
        
        return m;
    }
    
    
}

class MyKeyListener extends KeyAdapter {
    
    StructureAlignmentChooser chooser;
    
    public MyKeyListener(StructureAlignmentChooser chooser){
        this.chooser = chooser;
    }
    
    public void keyPressed(KeyEvent evt) {

        JTextField txt = (JTextField) evt.getSource();
        char ch = evt.getKeyChar();
        
        String search = txt.getText();
        
        // If a printable character add to search text
        if (ch != KeyEvent.CHAR_UNDEFINED) {
            if ( ch != KeyEvent.VK_ENTER )
                if ( ch != KeyEvent.VK_HOME )
                    search += ch;
        }
         
        //System.out.println("search text: " + search);
        StructureAlignment ali = chooser.getStructureAlignment();
        String[] ids = ali.getIds();
        if ( search.equals("")){
            search = "no search provided";
        }
        
        Border b = BorderFactory.createMatteBorder(3,3,3,3,Color.blue);
        
        List checkBoxes = chooser.getCheckBoxes();
        boolean firstFound = false;
        
        JScrollPane scroller = chooser.getScroller();
        int  h = 0;
        for (int i=0; i <ids.length;i++){
            JCheckBox box = (JCheckBox) checkBoxes.get(i);
            
            if ( ids[i].indexOf(search) > -1) {
                // this is the selected label
                //System.out.println("selected label " + ids[i]);
                box.setBorder(b);
                box.setBorderPainted(true);
                
                if ( ! firstFound ) {
                    // scroll to this position
                    if ( scroller != null){
                        scroller.getViewport().setViewPosition(new Point (0,h));
                    }
                }
                firstFound = true;
                
            } else {
                // clear checkbutton
                box.setBorderPainted(false);
            }
            box.repaint();
            h+= box.getHeight();
        }
        if (! firstFound) {
            if ( ( search.length() > 0) && (ch != KeyEvent.CHAR_UNDEFINED) ) {
                if (! (ch == KeyEvent.VK_BACK_SPACE)) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
               
    }
    
   
}


