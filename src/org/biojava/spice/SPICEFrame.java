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
 * Created on 05.06.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.Chain;

import java.awt.*;
import java.util.Map ;

/** an interface that defined methods that are required by sub frames
 * of SPICE to communicate with the master application 
 * @author Andreas Prlic
 */
public interface SPICEFrame  {

    /** returns a flag if data is being loaded using DAS. This is
     * needed for the differnt sub-frames to prevent them from having
     * problems with the loading threads  */    
    public boolean isLoading() ;

    /** starts a new thread that retreives protein structure using the
	DAS structure command from some other server this thread will
	call the setStructure method to set the protein structure.
    */
    public void getStructure(String pdbcode) ;

    /** show status notification in defaul color */
    public void   showStatus( String status ) ;
    /** show status notification in specified color */
    public void   showStatus( String status,Color c ) ;
    
    /** set a structure to be displayed and sends a script command to
     * color structure 
     * @param structre_ a Biojava structure object
     * @param selectcmd a rasmol like select command ( all commands in one line, spearated by ";"
     */
    public void   setStructure(Structure structure, String selectcmd );

    /** set a structure to be displayed. Use a default select command
     * to color structure
     * @param structre_ a Biojava structure object
     */
    public void   setStructure(Structure structure );

    /** set the chain nr chainnr as the active one */
    public void setCurrentChain(int chainnr);

    /** retrieve configuration for DAS servers to use */    
    public Map getConfiguration();
  

    /** highighting of range of residues */
    public void highlite(int chainNumber, int start, int end, String colour);

    /** highighting of range of residues */
    public void highlite(int chainNumber, int start, int end);

    /** highighting of single residue */
    public void highlite(int chainNumber, int seqpos, String colour);

    /** highighting of single residue */    
    public void highlite(int chainNumber, int seqpos);

    /** select range of residues */
    public void select(int chainNumber, int start, int end);
    
    /** select single residue */
    public void select(int chainNumber, int seqpos);

    /** colour range of residues */
    public void colour(int chainNumber, int start, int end, String colour);
    
    /** colour single residue */
    public void colour (int chainNumber, int seqpos, String colour);

    /** return command that can be send to executeCmd*/
    public String getSelectStr(int chain_number, int start, int end);
    /** return command that can be send to executeCmd*/
    public String getSelectStr(int chain_number,int seqpos) ;

    /** return the pdbcode + chainid to select a single residue. This
     * can be used to create longer select statements for individual
     * amino acids. */
    public String getSelectStrSingle(int chain_number, int seqpos) ;
    
    /** send a command to the Jmol panel*/
    public void executeCmd(String cmd);
    
    // color management
    public void setOldColor(Color c) ;

    // rescale the windo size 
    public void scale() ;
    
    /** retreive info regarding structure */
    public Chain getChain(int chainnumber);

    /** returns a string to display in the status window */
    public String getToolString(int chainnumber, int seqpos);

    /** display status in status window */
    public void showSeqPos(int chainnumber, int seqpos);



    
}