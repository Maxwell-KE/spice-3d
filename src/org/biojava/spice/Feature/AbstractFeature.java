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
 * Created on Feb 9, 2005
 *
 */
package org.biojava.spice.Feature;

import java.util.ArrayList;
import java.util.List;

/** An Abstract class representing a Feature as being diplayed in the SeqFeaturePanel 
 * There are different types of features and each implements it's own painting method:
 * e/g/ disulfid type features are different form secondary structure features, which 
 * are different from "default" features
 * 
 * 
 * @author Andreas Prlic
 *
 */
public abstract class AbstractFeature implements Feature {

    String name   ;
    String method ;
    String type   ;
    List   segments ;
    String note   ;
    String link   ;
    String source ;
    String score;

    public AbstractFeature() {
	source = "Unknown";
	method = "Unknown";
	type   = "Unknown";
	note   = "";
	link   = "";
	score  = "";
	
	segments = new ArrayList();
	       
    }

    public String toString() {
	String str = "Feature: method: " + method +" type: " + type + " note: "+note + " link: "+ link;
	
	str += segments ;
	return str ;
    }

    public void setSource(String s) { source = s;}
    public String getSource() { return source; };


    public void setName(String nam) { name = nam; }
    public String getName() { return name; }
    
    public void setMethod(String methd) { method = methd ; }
    public String getMethod() { return method ; }

    public void setType(String typ) { type = typ ; }
    public String getType() { return type ; }
    
    public void setNote(String nte) { note = nte; }
    public String getNote() { return note ; }
    
    public void setLink(String lnk) { link = lnk;}
    public String getLink() { return link;}

    public void setScore(String s){ score = s;}
    public String getScore() { return score;}
    
    /** add a segment to this feature */
    public void addSegment(int start, int end, String name) {
	Segment s = new Segment() ;
	s.setStart(start);
	s.setEnd(end) ;
	s.setName(name);
	s.setParent(this);
	segments.add(s);
    }
    
    public void addSegment( Segment s ){
	s.setParent(this);
	segments.add(s);
    }

    public List getSegments() { return segments ;}
  
    

}
