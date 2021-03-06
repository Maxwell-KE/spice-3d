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
 * Created on Nov 20, 2005
 *
 */
package org.biojava.spice.manypanel.managers;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.io.PDBParseException;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.dasobert.das.SequenceThread;
import org.biojava.dasobert.eventmodel.*;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.manypanel.drawable.DrawableSequence;
import org.biojava.spice.manypanel.renderer.*;
import org.biojava.spice.utils.UniProtAccessionCodeTools;

import java.util.*;

public class SequenceManager 
extends AbstractChainManager
    implements ObjectManager, SequenceListener {
      
        List<SequenceRenderer> seqRenderers;
        
        static Logger logger = Logger.getLogger("org.biojava.spice");
//      for conversion 3code 1code
        SymbolTokenization threeLetter ;
        SymbolTokenization oneLetter ;
    
    public SequenceManager() {
        super();
        
        seqRenderers = new ArrayList<SequenceRenderer>();
//      some utils for conversion of 3code to 1code
        Alphabet alpha_prot = ProteinTools.getAlphabet();
        
        try {
            threeLetter = alpha_prot.getTokenization("name");
            oneLetter  = alpha_prot.getTokenization("token");
        } catch (Exception e) {
            e.printStackTrace() ;
        }

    }

    
   
    
    public void clearDasSources(){
        super.clearDasSources();
        //logger.info("clear sequence manager das sources");
        if ( seqRenderers == null){
            return;
        }
        Iterator iter = seqRenderers.iterator();
        while (iter.hasNext()){
            SequenceRenderer rend = (SequenceRenderer)iter.next();
            rend.clearDasSources();
        }
    }
    
    public void addSequenceRenderer(SequenceRenderer r) {
        seqRenderers.add(r);
    }


    public void newObject(Object object) {
        
       
  
        
    
    }

    public Chain getChainFromString(String sequence){
        Chain chain =  new ChainImpl();
        //chain.setSwissprotId(id);
      
        for ( int pos = 0 ; pos < sequence.length() ; pos ++ ){
            AminoAcidImpl s_amino = new AminoAcidImpl();
            
            Character c = new Character(sequence.charAt(pos)) ;
            s_amino.setAminoType(c) ;
            String code1 = c.toString() ;
            String code3 = "" ;
            try {
                code3 = convert_1code_3code(code1);
            } catch (IllegalSymbolException e) {
                //e.printStackTrace();
                code3 = "XXX" ;     
            }
            try {
                s_amino.setPDBName(code3) ;     
            } catch ( PDBParseException e ) {
                e.printStackTrace() ;
                
            }
            chain.addGroup(s_amino) ;       
            
        }
        //logger.info("created new chain " + sequence);
        return chain;
    }
    
    public DrawableSequence fromString(String ac, String sequence){
        Chain chain = getChainFromString(sequence);
        chain.setName(ac);
        return new DrawableSequence(ac, chain);
    }
    
    
    
    
    
    /** convert one character amino acid codes into three character
     *  e.g. convert CYS to C
     */
    
    private String convert_1code_3code(String code1) 
    throws IllegalSymbolException
    {
        Symbol sym   =  oneLetter.parseToken(code1) ;
        String code3 =  threeLetter.tokenizeSymbol(sym);
        
        return code3;
        
    }
    
    public  DrawableSequence getDrawableSequence(String ac, String sequence){
        Chain chain =  new ChainImpl();
        //chain.setSwissprotId(id);
        
        for ( int pos = 0 ; pos < sequence.length() ; pos ++ ){
            AminoAcidImpl s_amino = new AminoAcidImpl();
            
            Character c = new Character(sequence.charAt(pos)) ;
            s_amino.setAminoType(c) ;
            String code1 = c.toString() ;
            String code3 = "" ;
            try {
                code3 = convert_1code_3code(code1);
            } catch (IllegalSymbolException e) {
                //e.printStackTrace();
                code3 = "XXX" ;     
            }
            try {
                s_amino.setPDBName(code3) ;     
            } catch ( PDBParseException e ) {
                e.printStackTrace() ;
                
            }
            chain.addGroup(s_amino) ;       
            
            
        }
        
        return new DrawableSequence(ac,chain);
    
    }
    
    
    /** trigger DAS Sequence request */
    public void newObjectRequested(String accessionCode) {
    	if (logger.isLoggable(Level.FINEST)){
    		logger.finest("SequenceManager new Object Requested");
    	}
        
        if  ( coordinateSystem.toString().equals(SpiceDefaults.UNIPROTCOORDSYS)){
            if ( UniProtAccessionCodeTools.isEntryName(accessionCode)){
                String aac = UniProtAccessionCodeTools.translateName2Accession(accessionCode);
                if ( aac != null && (! aac.equals(""))){
                    accessionCode = aac;
                }
            }
        }
        
        //SpiceDasSource[] sds = toSpiceDasSource(dasSources);
        SequenceThread thr = new SequenceThread(accessionCode,dasSources );
        
        thr.addSequenceListener(this);
        //thr.addSequenceListener(featureManager);
        thr.start();
        
        Iterator iter = seqRenderers.iterator();
        while (iter.hasNext()){
            SequenceRenderer re = (SequenceRenderer)iter.next();
            re.getStatusPanel().setLoading(true);
        }                
    }
    
    public void noObjectFound(String accessionCode){
        // clear the display...
    	if ( logger.isLoggable(Level.FINEST)){
    		logger.finest("SequenceManager noObjectFound " + accessionCode);
    	}
        setAccessionCode("");
        DrawableSequence ds = fromString("","");
        Iterator iter = seqRenderers.iterator();
        
        while (iter.hasNext()){
            SequenceRenderer renderer = (SequenceRenderer)iter.next();
            
            renderer.setDrawableSequence(ds);          
            renderer.clearDisplay();
            
        }
    }


    public void newSequence(SequenceEvent e) {
    	if (logger.isLoggable(Level.FINEST)){
    		logger.finest("SequenceManager newSequence " + e.getAccessionCode());
    	}
        String sequence = e.getSequence();
        
        DrawableSequence ds = fromString(e.getAccessionCode(),sequence);
        
        Iterator iter = seqRenderers.iterator();
        
        while (iter.hasNext()){
            SequenceRenderer renderer = (SequenceRenderer)iter.next();
            
            renderer.setDrawableSequence(ds);          
        }
    	//TODO: add support for versioning of reference objects
    	String version = null;
        SequenceEvent sevent = new SequenceEvent(e.getAccessionCode(),sequence,version);
        iter = sequenceListeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
         
            li.newSequence(sevent);
        }
        
        setAccessionCode(e.getAccessionCode());
    }


    public void selectedSeqPosition(int position) {
        // TODO Auto-generated method stub
        
    }


    public void selectedSeqRange(int start, int end) {
        // TODO Auto-generated method stub
        
    }


    public void selectionLocked(boolean flag) {
        // TODO Auto-generated method stub
        
    }

    public void clearSelection(){};
}
