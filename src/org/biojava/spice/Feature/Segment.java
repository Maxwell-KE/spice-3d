/*
 *                    BioJava development code
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
 * Created on 22.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice.Feature ;

import java.awt.Color ;
import java.awt.Graphics2D;

/** a class to keep track of location information for a feature */
public class Segment {
    int start   ;
    int end     ;
    String name ;
    Color color ;
    Feature parent ;
    String txtColor ;
    String note;
    
    public Segment() {
	start = 0 ;
	end   = 0 ;
	name  = "Unknown";
	color = Color.white ;
	txtColor = "white" ;
	parent = null ;
    note = "";
    }

    public String toString() {
	String str = "Segment: " +name + " " +start + " " + end ;
    if ( ( note != null ) && ( ! note.equals("null")))
            str += note;
	return str ;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setStart(int strt) {start = strt ; }
    public int  getStart() {return start ;}
    
    public void setEnd(int ed) { end = ed;}
    public int getEnd() { return end;}

    public void setName(String nam) { name = nam;}
    public String getName() { return name ; }

    public void setColor(Color col) { color = col; }
    public Color getColor() { return color ; }

    public void setParent(Feature f) { parent = f;}
    public Feature getParent(){ return parent;}
    
    public void setTxtColor(String str) { txtColor = str; }
    public String getTxtColor() { return txtColor;}
    
    /** tests if two segments are overlapping */
    public boolean overlaps(Segment segment){
        if (! (this.start <= this.end )) 
            throw new IndexOutOfBoundsException("start > end for segment" + this);
        
        if ( ! (segment.getStart() <= segment.getEnd() ))
                throw new IndexOutOfBoundsException("start > end for segment" + segment);
        
        // start must be in region of other
        if ( this.start >= segment.getStart()){
            if ( this.start <= segment.getEnd()){
                return true;
            }
        }
        // or end must be in region of other..
        if ( this.end >= segment.getStart() ) {
            if ( this.end <= segment.getEnd()){
                return true;
            }
        }

	if ( this.start <= segment.getStart() ) {
	    if ( this.end >= segment.getEnd() ) {
		return true;
	    }
	}
        return false;
    }
    
    /** paint this segment on a a graphics panel 
     * */
    public void paint(Graphics2D g, int y){
        
    }
    
}
