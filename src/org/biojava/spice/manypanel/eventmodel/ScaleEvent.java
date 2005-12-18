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
 * Created on Dec 18, 2005
 *
 */
package org.biojava.spice.manypanel.eventmodel;

public class ScaleEvent {
    float scale;
    public ScaleEvent(float scale) {
        super();
        this.scale = scale;

    }
    
    public float getScale(){
        return scale;
    }
    
    

}
