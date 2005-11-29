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
 * Created on Oct 28, 2005
 *
 */
package org.biojava.spice.manypanel.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Iterator;


import org.biojava.services.das.registry.*;
import org.biojava.spice.das.AlignmentThread;
import org.biojava.spice.das.SpiceDasSource;
import org.biojava.spice.manypanel.eventmodel.*;
import org.biojava.spice.manypanel.AlignmentTool;
import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASException;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;


public class AlignmentManager 
implements AlignmentListener {
    
    DasCoordinateSystem coordSys1;
    DasCoordinateSystem coordSys2;
    SpiceDasSource[] refservers1;
    SpiceDasSource[] refservers2;
    SpiceDasSource[] alignmentServers;
    AlignmentSequenceListener object1Listener;
    AlignmentSequenceListener object2Listener;
    List sequence1Listeners;
    List sequence2Listeners;
    
    List object1Listeners;
    List object2Listeners;
    
    Alignment alignment;
    
    String object1Id;
    String object2Id;
    Chain sequence1;
    Chain sequence2;
    Map alignmentMap1;
    Map alignmentMap2;
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    String panelName;
    
    public AlignmentManager(String panelName,DasCoordinateSystem coordSys1, DasCoordinateSystem coordSys2){
        this.panelName = panelName;
        
        this.coordSys1 = coordSys1;
        this.coordSys2 = coordSys2;
        clearDasSources();
        //alignmentServers = new SpiceDasSource[0];
        object1Listener = new AlignmentSequenceListener(this,1);
        object2Listener = new AlignmentSequenceListener(this,2);
        alignment = new Alignment();
        clearSequenceListeners();
        
        clearObjectListeners();
        object1Id = "";
        object2Id = "" ;
        sequence1 = new ChainImpl();
        sequence2 = new ChainImpl();
        
       alignmentMap1= new HashMap();
       alignmentMap2 = new HashMap();
    }
    
    public void clearDasSources(){
        alignmentServers = new SpiceDasSource[0];
    }
    
    public void clearObjectListeners(){
        object1Listeners = new ArrayList();
        object2Listeners = new ArrayList();
    }
    
    public void clearSequenceListeners() {
        sequence1Listeners = new ArrayList();
        sequence2Listeners = new ArrayList();
        
    }
    
    
    public void addSequence1Listener(SequenceListener one) {
        sequence1Listeners.add(one);
    }
    
    public void addSequence2Listener(SequenceListener two){
        sequence2Listeners.add(two);
    }
    
    public void addObject1Listener(ObjectListener li){
        object1Listeners.add(li);
    }
    public void addObject2Listener(ObjectListener li){
        object2Listeners.add(li);
    }
    public void setCoordSys1(DasCoordinateSystem coordSys){
        this.coordSys1 = coordSys;
    }
    public void setCoordSys2(DasCoordinateSystem coordSys) {
        this.coordSys2 = coordSys; 
    }
    
    public void setReferenceServers1(DasSource[] refservers){
        
        refservers1 = new SpiceDasSource[refservers.length];
        for ( int i = 0 ; i< refservers.length; i++){
            refservers1[i] =  SpiceDasSource.fromDasSource(refservers[i]);
        }
        
    }
    public void setReferenceServers2(DasSource[] refservers){
        
        refservers2 = new SpiceDasSource[refservers.length];
        for ( int i = 0 ; i< refservers.length; i++){
            refservers2[i] =  SpiceDasSource.fromDasSource(refservers[i]);
        }
        
    }
    
    //public SpiceDasSource[] getReferenceServers1() {
    //    return refservers1;
    //}
   
    //public SpiceDasSource[] getReferenceServers2(){
    //    return refservers2;
    //}
    
    public SpiceDasSource[] getAlignmentServers(){
        return alignmentServers;
    }
    
    public void setAlignmentServers(SpiceDasSource[] servers){
        logger.info(panelName+ " got "+servers.length+"  alignmentservers");
        alignmentServers = servers;
    }
    public SequenceListener getSeq1Listener(){
        return object1Listener ;
    }
    
    
    public SequenceListener getSeq2Listener(){
        return object2Listener ;
    }
    
    public void newAlignment(AlignmentEvent event){
        
        alignment = event.getAlignment();
        Annotation[] os = alignment.getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            logger.warning(panelName+" got  alignment of wrong # objects...");
            return;
        }
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        
        String ac1 =  (String) a1.getProperty("dbAccessionId");
        String ac2 =  (String) a2.getProperty("dbAccessionId");
        logger.info(panelName+" got new Alignment "+ac1 + " " + ac2+   " currently know:"+object1Id+" " + object2Id);
  
        
        
        
        // we need to find out which of the two objects is object1/object2 ...
        
       
//      triggerObject1Request(ac1);
//      triggerObject2Request(ac2);
    //  see wich object already was there..
       
       
        if ( ac1.equals(object1Id) || ( ac1.substring(0,4).equals(object1Id))){
            // object1 = ac1
           
            triggerObject2Request(ac2);
        } else if ( ac1.equals(object2Id)|| ( ac1.substring(0,4).equals(object2Id))){
            // object2 = ac1
          
            triggerObject1Request(ac2);
        } else if ( ac2.equals(object1Id)|| ( ac2.substring(0,4).equals(object1Id))){
           
            triggerObject2Request(ac1);
        } else if ( ac2.equals(object2Id)|| ( ac2.substring(0,4).equals(object2Id))){
          
            triggerObject1Request(ac1);
        } else {
            logger.info(panelName+" could not detect correct accessionCode");
        }
        
        tryCreateAlignmentChain();
        
        
    }
    
    private void tryCreateAlignmentChain(){
        
        Annotation[] os = alignment.getObjects();
        if ( os.length < 2){
            // something strange is going on here..
            logger.warning(panelName+" got  alignment of wrong # objects..." + os.length);
            return;
        }
        Annotation a1 = os[0];
        Annotation a2 = os[1];
        
        String ac1 =  (String) a1.getProperty("dbAccessionId");
        String ac2 =  (String) a2.getProperty("dbAccessionId");
        
        
        boolean found1 = false;
        boolean found2 = false;
        if ( object1Id.equals(ac1) || object2Id.equals(ac1))
            found1 =true;
        if ( object1Id.equals(ac2) || object2Id.equals(ac2))
            found2 =true;
        
        if ( ! (found1 && found2)) {
            logger.info(panelName + " can not create alignmentChain, yet >" + ac1 + "< >" + ac2 + 
                      "< >" + object1Id + "< >" + object2Id+"<");
            return;
        }
        
        //  create the internal alignment representation that allows to project one position in
        // one alignment onte the other ...
        
        try {
           storeAlignment(alignment);
        } catch (Exception e){
            e.printStackTrace();
            logger.warning(e.getMessage());
        }
        
    }
    
    public void triggerObject1Request(String ac){
        
        Iterator iter = object1Listeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.newObjectRequested(ac);
        }
    }
    
    public void triggerObject2Request(String ac){
        Iterator iter = object2Listeners.iterator();
        while (iter.hasNext()){
            ObjectListener li = (ObjectListener)iter.next();
            li.newObjectRequested(ac);
        }
    }
    
    
    public void newObject1 (Object o) {
        
    }
    
    public void newObject2 (Object o){
        
    }
    
    
    public void newSequence1(SequenceEvent e){
        logger.info(panelName+" alignment : new sequence1:" + e.getAccessionCode() + " currently know:"+object1Id+" " + object2Id);
        
       
        
        if (  object1Id.equals(e.getAccessionCode()) || object2Id.equals(e.getAccessionCode())){
            // we already go this one, ignore...
            logger.info("already loaded, skipping");
            tryCreateAlignmentChain();
            return;
        }
        
        SequenceManager sm = new SequenceManager();
        sequence1 = sm.getChainFromString(e.getSequence());
      
        // a new object, request the data...
        object1Id = e.getAccessionCode();
        
        if ( object2Id == null) {
            // get first alignment for this sequence..
        
            requestAlignment(object1Id);
        } else {
            requestAlignment(object1Id,object2Id);
        }
    }
    
    /** request a particular alignment between two objects */
    private void requestAlignment(String query, String subject){
        logger.info(panelName + " requesting new alignment for " + query + " " + subject);
        AlignmentThread thread = null;
        // TODO: find a cleaner solution for this:
        if ( panelName.equals("UP_ENSP"))
            thread =    new AlignmentThread(panelName,query,subject, alignmentServers,"ensemblpep-human-ncbi35");
        else
            thread = new AlignmentThread(panelName,query,subject, alignmentServers);
        thread.addAlignmentListener(this);
        thread.start();
    }
    
    /** request the first alignment for a particular accession code 
     * 
     * @param code
     */  
    private void requestAlignment(String code){
        logger.info(panelName + " requesting new alignment for " + code);
        AlignmentThread thread = null;
        // TODO: find a cleaner solution for this:
        if ( panelName.equals("UP_ENSP"))
            thread =    new AlignmentThread(panelName,code, alignmentServers,"ensemblpep-human-ncbi35");
        else
            thread = new AlignmentThread(panelName,code, alignmentServers);
        thread.addAlignmentListener(this);
        thread.start();
        
    }
    
    public void newSequence2(SequenceEvent e){
        logger.info(panelName+" alignment : new sequence2:"+e.getAccessionCode() + " currently know:"+object1Id+" " + object2Id);
        if (  object2Id.equals(e.getAccessionCode()) || object1Id.equals(e.getAccessionCode())){
            // we already go this one, ignore...
            logger.info("already loaded, skipping");
            tryCreateAlignmentChain();
            return;
        }

        SequenceManager sm = new SequenceManager();
        sequence2 = sm.getChainFromString(e.getSequence());
      
        object2Id = e.getAccessionCode();
        
        if ( object1Id == null) {
            // get first alignment for this sequence..
        
            requestAlignment(object2Id);
        } else {
            requestAlignment(object2Id,object1Id);
        }
        
        
       
    }
    
    public void requestedObject1(Object o){
        
    }
    
    public void requestedObject2(Object o){
        
    }
    
    // get the position of the second object ...
    private int getPosition2(int pos1){
        int pos2 = -1;
        //TODO add support for insertion codes...
        try {
            //System.out.println(alignmentMap1);
            Integer p = new Integer(pos1);
            if ( alignmentMap1.containsKey(p)){
                //System.out.println(alignmentMap1.get(p));
                Integer spos2 = (Integer) alignmentMap1.get(p);
                pos2 = spos2.intValue();
            }
        } catch (Exception e){ e.printStackTrace();}
            
        return pos2;
        
    }
    
    private int getPosition1(int pos2){
        int pos1 = -1;
        try {
           //System.out.println(alignmentMap2);
            Integer p = new Integer(pos2);
            if ( alignmentMap2.containsKey(p)){
                //System.out.println(alignmentMap2.get(p));
                Integer spos1 = (Integer) alignmentMap2.get(p);
                pos1 = spos1.intValue();
            }
        } catch (Exception e){}
        return pos1;
        
    }
    
    public void selectedSeqPosition1(int pos){
        //logger.info("selected seq pos1: " + pos + " pos2:" + getPosition2(pos));
        int npos = getPosition2(pos);
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(npos);
        }
    }
    
    public void selectedSeqPosition2(int pos){
        //logger.info("selected seq pos1: " + getPosition1(pos) + " pos2:" + pos);
        int npos = getPosition1(pos);
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqPosition(npos);
        }
    }
    
    public void selectedSeqRange1(int start, int end ){
        
        int s = getPosition2(start);
        int e = getPosition2(end);
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(s,e);
        }
        
    }
    
    public void selectedSeqRange2(int start, int end ){
        int s = getPosition1(start);
        int e = getPosition1(end);
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectedSeqRange(s,e);
        }
    }
    
    public void selectionLocked1(boolean flag){
     
        Iterator iter = sequence1Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectionLocked(flag);
        }
        object2Listener.selectionLocked(flag);
    }
    
    public void selectionLocked2(boolean flag){
        Iterator iter = sequence2Listeners.iterator();
        while (iter.hasNext()){
            SequenceListener li = (SequenceListener)iter.next();
            li.selectionLocked(flag);
        }
        object1Listener.selectionLocked(flag);
    }
    
    /** get the Algignment between two accession codes 
     * 
     * @param query1
     * @param query2
     */
    public Alignment getAlignment(String query1, String query2){
        return null;
    }
    
    
    public Alignment[] getAllAlignments(String query){
        return null;
    }
    

    private  void storeAlignment(Alignment ali) 
    throws DASException
    {
        System.out.println("storing alignment");
             
        
        // go over all blocks of alignment and join pdb info ...
        Annotation seq_object   = getAlignmentObject(ali,coordSys1.toString() );
        
        Annotation stru_object  = getAlignmentObject(ali,coordSys2.toString());
        
        String obj1Id = seq_object.getProperty("intObjectId").toString();
        String obj2Id = stru_object.getProperty("intObjectId").toString();
        
        System.out.println("storing alignment " + obj1Id + " " + obj2Id);
        
        //Simple_AminoAcid_Map current_amino = null ;
        List aligMap1 = AlignmentTool.createAlignmentTable(ali,obj1Id);
        List aligMap2 = AlignmentTool.createAlignmentTable(ali,obj2Id);
               
      
        // the two maps MUST have the same size!
        //if ( aligMap1.size() != aligMap2.size()){
        //    logger.warning("can ")
        //    return
        //}
                       
        // resolve the alignment maps
        alignmentMap1 = new HashMap();
        alignmentMap2 = new HashMap();
        
        for ( int pos = 0 ; pos < aligMap1.size(); pos++ ) {
            Map m1 = (Map) aligMap1.get(pos);
            Map m2 = (Map) aligMap2.get(pos);
            //System.out.println("1:"+m1);
            //System.out.println("2:"+m2);
            Object s1 =  m1.get("seqpos");
            Object s2 =  m2.get("seqpos");
            
            if (( s1 == null ) || (s2 == null)) {
                // this sequence position is not aligned ...
                continue;
            }
            //logger.info("aligned : "+ s1 + " " +s2);
            alignmentMap1.put(s1,s2);
            alignmentMap2.put(s2,s1);
            
        }
    }
    
    
    private Annotation getAlignmentObject (Alignment ali,String objecttype) 
    throws DASException
    {
        // go through objects and get sequence one ...
        Annotation[] objects = ali.getObjects();
        //HashMap seq_obj = new HashMap() ;
        
        for (int i =0 ; i<objects.length;i++) {
            Annotation object = objects[i];
            String dbCoordSys = (String)object.getProperty("dbCoordSys");
            
            
            if ( dbCoordSys.equals(objecttype) ) {      
                return object ;
            }
            
            /** TODO: fix this */
            // tmp fix until Alignment server returns the same coordsystems as the registry contains ... :-/
            if ( objecttype.equals("UniProt,Protein Sequence")){
                if ( dbCoordSys.equals("UniProt"))
                    return object;
            }
            if ( objecttype.equals("PDBresnum,Protein Structure")){
                if ( dbCoordSys.equals("PDBresnum"))
                    return object;
            }
            if ( objecttype.equals("Ensembl,Protein Sequence")){
                if ( dbCoordSys.equals("ENSEMBLPEP"))
                    return object;
            }
        }      
        
        throw new DASException("no >" + objecttype + "< object found as dbSource in alignment!");        
    }
    
    
    
    /** map from one segment to the other and store this info in chain  
    private  void mapSegments(
            Annotation block,
            Annotation seq_obj, 
            Annotation stru_obj) 
    throws DASException
    {
        logger.finest("mapSegment");
        logger.finest(block.toString());
        // order segmetns
        // HashMap 0 = refers to seq
        // hashMap 1 = refers to struct
        ArrayList segments = (ArrayList)block.getProperty("segments");
        logger.finest("segments size: "+ segments.size());
        Annotation[] arr = new Annotation[segments.size()] ;
        
        String seq_id  = (String) seq_obj.getProperty("intObjectId");
        String stru_id = (String)stru_obj.getProperty("intObjectId");
        
        if ( segments.size() <2) {
            logger.finest("<2 segments in block. skipping");
            return ;
        }
        
        //if ( ! seq_id.equals(chain.getSwissprotId() )){
        //    logger.fine("chain - swissprot does not match Alignment swissprot! can not map segments");
        //    return;
       // }
        
        boolean seqOK = false;
        boolean strucOK = false;
        for ( int s =0; s< segments.size() ; s++) {
            Annotation segment = (Annotation) segments.get(s) ;
            logger.finest(" testing segment " +segment.toString());
            String obid = (String) segment.getProperty("intObjectId");
            if ( obid.equals(seq_id)) {
                logger.finest("got seq object in block");
                arr[0] = segment;
                seqOK = true;
            }
            if ( obid.equals(stru_id)) {            
                logger.finest("got struc object in block");
                arr[1] = segment;
                strucOK = true;
            }
            
            // if there are other alignments, do not consider them ...
        }
        
        
        if ( ! seqOK ){
            logger.info("problem with alignment sequence " + seq_id + " not found in segments.");
            return;
            
        }
        if ( ! strucOK ){
            logger.info("problem with alignment structure " + stru_id + " not found in segment.");
            return;
        }
        
        // here is the location where the cigar string shouldbe parsed, if there is any
        // for the moment, nope..... !!!
        
        // coordinate system for sequence is always numeric and linear
        // -> phew!
        
        logger.finest(" seq segment: " + arr[0].toString());
        
        //TODO:
         //add either start - end based mapping (pbd - up )
        // or: cigar string based alignment ...
        
        try {
            
        int seq1start = Integer.parseInt((String)arr[0].getProperty("start")); 
        int seq1end   = Integer.parseInt((String)arr[0].getProperty("end"));
        
        int seq2start = Integer.parseInt((String)arr[1].getProperty("start")); 
        int seq2end   = Integer.parseInt((String)arr[1].getProperty("end"));
        
        // size of the segment
        
        int seg1size = seq1end-seq1start + 1 ;
        int seg2size = seq2end-seq2start + 1;
        
        // now build up a segment of aminoacids
        // they should be of same size ...
        AminoAcid[] seq1Segment = new AminoAcid[seg1size]  ;
        AminoAcid[] seq2Segment = new AminoAcid[seg2size]  ;
        
        int chainlength = sequence1.getLength();
        //if ( seqend >= chainlength ){
            //logger.warning("warning: potential version conflict: coordinate of alignment ("+seqend+") and sequence length ("+chain.getLength()+"( does not match!");
            //return;
        //}
        for ( int i = 0 ; i < seg1size ; i++) {

            int pos = i+seq1start-1;
            if ( pos >= chainlength){
                logger.finest("i "+i + "chainlength "+ chainlength + " segsize " + seg1size);
                logger.finest("requesting wrong coordinate - " + pos + " is larger than chainlength " + chainlength);
                break;
            }
            seq1Segment[i] = (AminoAcid) sequence1.getGroup(i+seq1start-1);
            seq2Segment[i] = (AminoAcid) sequence2.getGroup(i+seq2start-1);
        }
        
        //logger.finest("segsize " + segsize);
        for ( int i =0 ; i< seg1size; i++) {
            
            String pos1 = Integer.toString(seq1start + i) ;
            String pos2 = Integer.toString(seq2start + i) ;
            //logger.finest(i + " " + pdbcode);
            
            // the PDBCode field contains the position on the other object ...
            seq1Segment[i].setPDBCode(pos2) ;
            seq2Segment[i].setPDBCode(pos1) ;
            
        }
        
        } catch (Exception e) {
            // if this does not work  there is an insertion code
            
            
            // -> segment must be of size one.
            //  alignment from seq to structure HAS TO BE provided as a one to one mapping
            
            //logger.finest("CAUGHT!!!!! conversion of >"+ (String)arr[1].get("start") + "<") ;
            //e.printStackTrace();
            
            //if ( seg1size != 1 ) {
             //   throw new DASException(" alignment is not a 1:1 mapping! there is an insertion code -> this does not work!");       
            //}
            
            
            
            String start1Pos = (String)arr[0].getProperty("start") ;
            String start2Pos = (String)arr[1].getProperty("start") ;
            if (( start2Pos.equals("-")) || ( start1Pos.equals("-"))) {
                // not mapped ...
                //logger.finest("skipping char - at position " + pdbcode);
                return ;
            }
            
            //e.printStackTrace();
            logger.finest("Insertion Code ! setting new pdbcode "+start2Pos) ;
            //seq1Segment[0].setPDBCode(start2Pos);
            //seq2Segment[0].setPDBCode(start1Pos);
            try {
                AminoAcid a = (AminoAcid) sequence1.getGroup(Integer.parseInt(start1Pos));
                a.setPDBCode(start2Pos);
            } catch (Exception ex){}
            
            
            try {
                AminoAcid a = (AminoAcid) sequence1.getGroup(Integer.parseInt(start2Pos));
                a.setPDBCode(start1Pos);
            } catch (Exception ex){}
            
            return ;
        }
        
      
    }
    */
    
}


class AlignmentSequenceListener implements SequenceListener{
    int objectNr;
    int oldpos;
    int oldend;
    boolean selectionLocked;
    AlignmentManager parent;
    
    public AlignmentSequenceListener(AlignmentManager parent, int nr){
        this.parent=parent;
        objectNr = nr;
        oldpos = -1;
        oldend = -1;
        selectionLocked = false;
    }
    
    public void newSequence(SequenceEvent e) {
        // TODO Auto-generated method stub
        if ( objectNr == 1)
            parent.newSequence1(e);
        else
            parent.newSequence2(e);
    }
    public void selectedSeqPosition(int position) {
        if ( selectionLocked )
            return;
         
        
        if ( oldpos == position)            
            return;
        oldpos = position;
        oldend = position;
        
        if ( objectNr == 1)
            parent.selectedSeqPosition1(position);
        else
            parent.selectedSeqPosition2(position);
        
    }
    public void selectedSeqRange(int start, int end) {
       if ( selectionLocked )
           return;
        if (( oldpos == start ) && ( oldend == end)){
            return;
        }
        oldpos = start;
        oldend = end;
        
        if ( objectNr == 1)
            parent.selectedSeqRange1(start,end);
        else
            parent.selectedSeqRange2(start,end);
    }
    
    public void selectionLocked(boolean flag) {
        //System.out.println("AlignmentManager got selectionLocked " + flag + " "+selectionLocked);
        if ( selectionLocked == flag) {
            //System.out.println("not reporting further");
            return;
        }
        selectionLocked = flag;
        // selection lock is for all objects...
        parent.selectionLocked1(flag);
        parent.selectionLocked2(flag);
    }
    
    public void newObject(Object object) {
        
        
        if ( objectNr == 1){
            parent.newObject1(object);
        } else {
            parent.newObject2(object);
            
        }
    }
    
    public void newObjectRequested(String accessionCode) {
        // TODO Auto-generated method stub
        
        if ( objectNr == 1){
            parent.requestedObject1(accessionCode);
        } else {
            parent.requestedObject2(accessionCode);
            
        }
    }
    
    
    
    
}





