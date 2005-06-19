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
 * Created on Jun 14, 2005
 *
 */
package org.biojava.spice.Panel.seqfeat;

/**
 * @author Andreas Prlic
 *
 */
public class FeatureEvent {

    FeatureView parent;
    Object source;
    /**
     * 
     */
    public FeatureEvent(FeatureView parent, Object source) {
        super();
        this.parent = parent;
        this.source = source;
        // TODO Auto-generated constructor stub
    }
    
    /** the paretn FeatureView in which this event occured */
    public FeatureView getParent(){ return parent ;}
    
    /** a Feature or aSegment object, that has triggered this event */
    public Object getSource(){ return source; }
}