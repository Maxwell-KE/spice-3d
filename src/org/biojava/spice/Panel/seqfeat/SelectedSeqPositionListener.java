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
 * Created on Jun 10, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

/**
 * @author Andreas Prlic
 *
 */
public interface SelectedSeqPositionListener {
    
    /* select a certain sequence position */
    public void selectedSeqPosition(int position);
    
    /** select a certain range of a sequence */
    public void selectedSeqRange(int start, int end);
    
    /** the current selecetion is locked and can not be changed */
    public void selectionLocked(boolean flag);
}
