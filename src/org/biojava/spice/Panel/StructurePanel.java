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
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

// for Jmol stuff
import org.jmol.api.JmolAdapter;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.viewer.JmolViewer;
import org.jmol.viewer.JmolStatusListener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// biojava structure stuff
import org.biojava.bio.structure.Structure ;

// logging
import java.util.logging.*;

/** a Panel that provides a wrapper around the Jmol viewer. Code heavily
 * inspired by
 * http://cvs.sourceforge.net/viewcvs.py/jmol/Jmol/examples/Integration.java?view=markup
 * - the Jmol example of how to integrate Jmol into an application.
 * 
 */
class StructurePanel extends JPanel {
    
    final  Dimension currentSize = new Dimension();
    static Logger    logger      = Logger.getLogger("org.biojava.spice");

    JmolViewer  viewer;
    JmolAdapter adapter;
    TextField   strucommand  ; 


    StructurePanel() {
	adapter = new SmarterJmolAdapter(null);
	viewer = new JmolViewer(this, adapter);
    }
    
    /** returns the JmolViewer */
    public JmolViewer getViewer() {
	return viewer;
    }

    /** paint Jmol */
    public void paint(Graphics g) {

	viewer.setScreenDimension(getSize(currentSize));
	Rectangle rectClip = new Rectangle();
	g.getClipBounds(rectClip);
	viewer.renderScreenImage(g, currentSize, rectClip);
    }

    /** send a RASMOL like command to Jmol
     * @param command - a String containing a RASMOL like command. e.g. "select protein; cartoon on;"
     */
    public void executeCmd(String command) {
	
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest("sending Jmol command: "+command);
	}

	
	viewer.evalString(command);
    }

    /** display a new PDB structure in Jmol 
     * @param structure a Biojava structure object    
     *
     */
    public  void setStructure(Structure structure) {
	
	String pdbstr = structure.toPDB(); 
	
	synchronized ( viewer) {
	    viewer.openStringInline(pdbstr);
	}
	
	String cmd ="select all; cpk off; wireframe off;"  ;
	executeCmd(cmd);
	    
	
	String strError = viewer.getOpenFileError();
	if (strError != null) {
	    if (logger.isLoggable(Level.WARNING)) {
		logger.severe("could not open PDB file in viewer "+ strError);
	    }
	}
    }
    


}
