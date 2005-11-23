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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;



import org.biojava.bio.structure.ChainImpl;
import org.biojava.spice.manypanel.drawable.*;


public class SequenceRenderer 

extends AbstractChainRenderer
{

   
    public SequenceRenderer() {
        super();
        
        logger.info("init sequenceRenderer");
        
        sequence = new DrawableSequence(new ChainImpl());
        featurePanel = new FeaturePanel();
        cursorPanel  = new CursorPanel();
        initPanels();
        
    }

    
    
    public void setDrawableSequence(DrawableSequence sequence) {
        logger.info("setting drawable sequence ");
        this.sequence=sequence;
        featurePanel.setChain(sequence.getSequence());
        cursorPanel.setChain(sequence.getSequence());
        calcScale(100);
       
    }
    
    
    
  

  

   

}