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
 * Created on 19.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.spice.DAS;


import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes            ;
import java.util.logging.*                ;

/**
 * a class that parses the XML response of a DAS - sequence command.
 * @author Andreas Prlic
 *
 */
public class DAS_Sequence_Handler extends DefaultHandler {

	String sequence ;
	int length ;
	boolean dna_flag; 
	/**
	 * 
	 */
	public DAS_Sequence_Handler() {
		super();
		// TODO Auto-generated constructor stub
		sequence = "" ;
		length = 0;
		dna_flag = false ;
	}

	public void startElement (String uri, String name, String qName, Attributes atts){

	    if ( qName.equals("SEQUENCE")){
		//System.out.println("new element >" + name + "< >" + qName+"<");
		// was : length
		String lenstr 	= atts.getValue("stop");
		length = Integer.parseInt(lenstr);
		dna_flag = true ;
	    }
		
	}
	
	public void characters (char ch[], int start, int length){
	    //System.out.print("Characters:    \"");
		if (dna_flag) 
		 for (int i = start; i < start + length; i++) {
		 	switch (ch[i]) {
		 	case '\\':
		 		//System.out.print("\\\\");
		 		break;
		 	case '"':
		 		//System.out.print("\\\"");
		 		break;
		 	case '\n':
		 		//System.out.print("\\n");
		 		break;
		 	case '\r':
		 		//System.out.print("\\r");
		 		break;
		 	case '\t':
		 		//System.out.print("\\t");
		 		break;
		 	case ' ':
		 		break;
		 	default:
		 		sequence = sequence + ch[i];
		 		//System.out.print(ch[i]);
		 break;
		 }
		 }
		 //System.out.print("\"\n");
		 
	}
	
	public String get_sequence() {
		if ( length != sequence.length()) {	
		    Logger logger  = Logger.getLogger("org.biojava.spice");
		    logger.warning("Sequence does not match specified length!");
			
		}
		
		return sequence;
	}
		
	
	
}
