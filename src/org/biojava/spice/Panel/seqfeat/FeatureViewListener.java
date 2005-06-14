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

/** An interface to listen to events that can occur in a FeatureView
 * @author Andreas Prlic
 *
 */
public interface FeatureViewListener {

    	public void mouseOverFeature(FeatureEvent e);
    	public void mouseOverSegment(FeatureEvent e);
    	public void featureSelected(FeatureEvent e);
    	public void segmentSelected(FeatureEvent e);
    	
    

}
