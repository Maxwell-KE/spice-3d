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
 * Created on May 8, 2006
 *
 */
package org.biojava.spice.gui;

import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.StructureAlignment;
import org.biojava.spice.manypanel.eventmodel.StructureAlignmentListener;
import org.biojava.spice.panel.StructurePanel;

/** a class that eithe provides a JList or a JCheckbox,
 * depending if spice is running in structure alignment mode or
 * in single protein mode
 * 
 * @author Andreas Prlic
 * @since 2:02:15 PM
 * @version %I% %G%
 */
public class SelectionPanel
extends JPanel
implements StructureListener
{
    static final long serialVersionUID = 927593656266584l;

    public static Logger logger =  Logger.getLogger("org.biojava.spice");
    
    JList chainList;
    JCheckBox checkBox ;
    SpiceChainDisplay chainDisplay;
    boolean alignmentMode;
    
    Box hBox;
    //StructureAlignment structureAlignment;
    StructureAlignmentChooser alignmentChooser;
    
    JSplitPane splitPanel;
    
    public SelectionPanel() {
        super();
        
        DefaultListModel model = new DefaultListModel();        
        model.add(0,"");
        
        chainList	= new JList(model);        
        checkBox 	= new JCheckBox();
        
        chainDisplay = new SpiceChainDisplay(chainList);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        hBox = Box.createVerticalBox(); 
        this.add(hBox);
        
        chainList.addListSelectionListener(chainDisplay);
        
        // default we are in single protein mode ...
        alignmentMode = false;                
        hBox.add(chainList);
        

        alignmentChooser = new StructureAlignmentChooser();
        setOpaque(false);
    }

    public void clearListeners(){
        chainDisplay.clearStructureListeners();
        chainList.removeListSelectionListener(chainDisplay);
        
        alignmentChooser.clearListeners();
    }
    
    public StructureAlignmentChooser getAlignmentChooser(){
        return alignmentChooser;
    }
    
    public SpiceChainDisplay getChainDisplay(){
        return chainDisplay;
    }
    
    public StructureAlignmentListener getStructureAlignmentListener(){
        return alignmentChooser;
    }
    
    public void addPDBSequenceListener(SequenceListener li){
        alignmentChooser.addPDBSequenceListener(li);
      
    }
    
    
    public int getCurrentChainNumber(){
        return chainDisplay.getCurrentChainNumber();
    }
    
    public Chain getChain(int chainnumber){
        return chainDisplay.getChain(chainnumber);
    }
    
    
    public void setStructurePanel(StructurePanel panel){
        alignmentChooser.setStructurePanel(panel);
    }
    
    public void setStructureAlignment(StructureAlignment strucAli){
        //structureAlignment = strucAli;
    	
        alignmentChooser.setStructureAlignment(strucAli);
        alignmentMode = true;
        hBox.removeAll();
        
        JScrollPane scroll = new JScrollPane(alignmentChooser);
        hBox.add(scroll);
      
        alignmentChooser.setScroller(scroll);
        
        Box horBox = Box.createHorizontalBox();
        JTextField searchBox = alignmentChooser.getSearchBox();
        horBox.add(searchBox);
        
        /*Action action = new AbstractAction("X") {
            
            private static final long serialVersionUID = 1L;

            // This method is called when the button is pressed
            public void actionPerformed(ActionEvent evt) {
                JTextField searchBox = alignmentChooser.getSearchBox();
                searchBox.setText("");
            }
        };
        
        JButton b = new JButton(action);
        horBox.add(b);
        */
        hBox.add(horBox);
      
        
        Dimension dim = new Dimension(100,200);
        logger.finest("setting selection panel size" + dim);
        this.setPreferredSize(dim);
        this.setSize(dim);
        
        
        this.repaint();
        if ( splitPanel != null) {
            
            splitPanel.setDividerLocation(150);
            splitPanel.repaint();
            
        }
    
        
        alignmentChooser.repaint();
        hBox.repaint();
        
        StructureEvent event = new StructureEvent(new StructureImpl());      
        chainDisplay.newStructure(event);
        
        repaint();
        revalidate();
       
       // logger.info("set SelectionPanel to structure alignment mode...");
    }
    
    public void setSplitPanel(JSplitPane panel){
    	splitPanel = panel;
    	
    }
    
    public void addStructureListener(StructureListener li){
        alignmentChooser.addStructureListener(li);
        chainDisplay.addStructureListener(li);
        
    }
    
    public void newStructure(StructureEvent event){
        // set into single structure mode
        //logger.info("selectionPanel got new structure");
        
        // make sure this is not the first active structure in the structure alignment ...
        StructureAlignment strucAli = alignmentChooser.getStructureAlignment();
        if ( strucAli == null)
            strucAli = new StructureAlignment(null);
        
        int pos = strucAli.getLastSelectedPos();
        if ( pos > -1){
            try {
                Structure selected = strucAli.getStructure(pos);
                if ( selected.getPDBCode().equalsIgnoreCase(event.getPDBCode())){
                    return;
                }
            } catch (StructureException e){
                
            }
            
        }
        
        hBox.removeAll();
        hBox.add(chainList);
        chainDisplay.newStructure(event);
        alignmentMode = false;
        alignmentChooser.setStructureAlignment(null);
        
        Dimension dim = new Dimension(30,30);
        logger.finest("setting SelectionPanel dimension to " + dim);
        this.setPreferredSize(dim);
        if ( splitPanel != null) {
        	splitPanel.repaint();
        	splitPanel.setDividerLocation(30);
        }
        
        hBox.repaint();
        repaint();
        revalidate();
        
    }
    
    public void repaint(){
        super.repaint();
        if ( chainList != null)
            chainList.repaint();        
    }  
    
    
    public void selectedChain(StructureEvent e){}

    public void newObjectRequested(String accessionCode) {
        
    }

    public void noObjectFound(String accessionCode) {
  
        
    }
    
}
