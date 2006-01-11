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
package org.biojava.spice.das;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.program.das.dasalignment.DASAlignmentCall;
import org.biojava.bio.program.ssbind.AnnotationFactory;
import org.biojava.services.das.registry.DasCoordinateSystem;
import org.biojava.spice.manypanel.BrowserPane;
import org.biojava.spice.manypanel.eventmodel.*;


/** A thread that gets the alignment from a das server
 * 
 * @author Andreas Prlic
 *
 */
public class AlignmentThread 
extends Thread{
    
    
    static Logger logger = Logger.getLogger("org.biojava.spice");
    List alignmentListeners;
    DASAlignmentCall dasalignmentCall;   
    String logname;
    
    AlignmentParameters parameters;
    
    String PDB_COORD_SYS = BrowserPane.DEFAULT_PDBCOORDSYS ;
    
    public AlignmentThread(AlignmentParameters params) {
        super();
        this.parameters = params;
        logname = "";
        
        // if this is a PDB code, check for empty chain.      
        
        clearAlignmentListeners();
        
        dasalignmentCall= new DASAlignmentCall();
        
    }
    
    
    
    
    public void clearAlignmentListeners(){
        alignmentListeners = new ArrayList();
    }
    
    public void addAlignmentListener(AlignmentListener ali){
        alignmentListeners.add(ali);
    }
    
    
    private Alignment handleSubjectRequest(String query, String subject, AlignmentParameters parameters, Alignment[] aligs){

        Alignment finalAlig = aligs[0];
        
       // logger.info("subject " + subject);
//        if ( parameters.getQueryPDBChainId() != null) {
//            query = query +"." + parameters.getQueryPDBChainId();
//            logger.info("query with chain " + query);
//        }
        if ( parameters.getSubjectPDBChainId() != null)
            subject = subject + "." + parameters.getSubjectCoordinateSystem();
      
        //logger.info("searching for " + query + " " + subject);
        boolean found = false;
        for ( int i=0; i< aligs.length;i++ ){
            Alignment a = aligs[i];
            //logger.info("checking alignment " + a.toString());
            try {
                AlignmentTools.getObject(query,a);
                AlignmentTools.getObject(subject,a);
                //logger.info("found alignment for "+query + " " + subject);
                finalAlig = a;
                found = true;
                break;
            } catch (NoSuchElementException e){
                //logger.info(" no such element " + e.getMessage());
                continue;
            }
        }
        
        if ( ! found){
            // hum somebdy requested a particular query & subject, but we do not find this.
            // give him the first alignment for query..
             if ( parameters.getQueryPDBChainId() != null)
                query = query.substring(0,4);
            finalAlig = getAlignmentFromAligs(aligs,query);
        }
        return finalAlig;
    }
    
    public void run() {
        
        DasCoordinateSystem queryCoordSys = parameters.getQueryCoordinateSystem();
        String query = parameters.getQuery();
        String subject = parameters.getSubject();
        
        if ( queryCoordSys != null ){
            String qcs = queryCoordSys.toString();
            //logger.info("found queryCS " + qcs + " query " + query + " subject " + subject);
            
            if ( qcs.equals (PDB_COORD_SYS) ) {
                //logger.info("looks like a PDB " + qcs + " " + PDB_COORD_SYS);
                query = query.substring(0,4);
            }
        }
        //logger.info("requesting for query " + query);
        Alignment[] aligs = getAlignments(query);
        if ( aligs.length == 0) {
            triggerNoAlignmentFound(query,subject);
            return;
        }
        
        Alignment finalAlig =  aligs[0];
        
        
        // take the right alignment
        if (  subject != null) {
            finalAlig = handleSubjectRequest(query,subject,parameters,aligs);
            
        } else {
//           
            finalAlig = getAlignmentFromAligs(aligs,query);
            
        }
        
        if ( parameters.getQueryPDBChainId() != null) {
            aligs = filterWrongChainAligs(aligs,query+"."+parameters.getQueryPDBChainId());
        }
        
        if ( aligs.length == 0)
           triggerNoAlignmentFound(query,subject);
        
        if (finalAlig == null){        
            finalAlig = aligs[0];
        }
        
        
        
        AlignmentEvent event = new AlignmentEvent(finalAlig,aligs); 
        Iterator iter = alignmentListeners.iterator();
        while (iter.hasNext()){
            AlignmentListener li = (AlignmentListener ) iter.next();
            li.newAlignment(event) ;
        }
        
    }
    
    private Alignment[] filterWrongChainAligs(Alignment[] aligs, String query){
  
        List retlst = new ArrayList();
        
        for ( int i=0; i< aligs.length;i++ ){
            Alignment a = aligs[i];
            //logger.info("checking alignment " + a.toString());
            try {
                
                //logger.info("searching for " + query );
                AlignmentTools.getObject(query,a);
                
                //logger.info("found alignment for "+query );
                //finalAlig = a;
                retlst.add(a);
            } catch (NoSuchElementException e){
                //logger.info(" no such element " + e.getMessage());
                continue;
            }
        }
        if ( retlst.size() == 0 )
            return aligs;
        return (Alignment[])retlst.toArray(new Alignment[retlst.size()]);
    }
    
    private Alignment getAlignmentFromAligs(Alignment[] aligs,String query){

        //logger.info("subject is null");
        // check if no subject, but query has a chain id ...
        if ( parameters.getQueryPDBChainId() != null) {
            //logger.info("got a pdb chain request");
            query = query +"." + parameters.getQueryPDBChainId();
            //logger.info("get query " + query);
            
            for ( int i=0; i< aligs.length;i++ ){
                Alignment a = aligs[i];
                //logger.info("checking alignment " + a.toString());
                try {
                    
                    //logger.info("searching for " + query );
                    AlignmentTools.getObject(query,a);
                    
                    //logger.info("found alignment for "+query );
                    //finalAlig = a;
                    return a;
                } catch (NoSuchElementException e){
                    //logger.info(" no such element " + e.getMessage());
                    continue;
                }
            }
        }
        return null;
    }
    
    /** get alignments for a particular uniprot or pdb code */
    private  Alignment[] getAlignments(String code) {
        //logger.finest(logname + "searching for alignments of "+code+" ");
        Alignment[] alignments = new Alignment[0] ;
        SpiceDasSource[] dasSources = parameters.getDasSources();
        //List aligservers = config.getServers("alignment");
        logger.finest(logname + "found " + dasSources.length + " alignment servers");
        
        String  dasalignmentcommand = null  ;
        
        String subject = parameters.getSubject();
        DasCoordinateSystem subjectCoordSys = parameters.getSubjectCoordinateSystem();
        
        // loop over all available alignment servers 
        for ( int i =0 ; i < dasSources.length ; i++ ) {
            SpiceDasSource sds= dasSources[i];
            
            //logger.finest(logname + " investigating " + i + " url" + sds.getUrl());
            //System.out.println("investigating" + sds.getUrl());
            // only consider those serving uniprot and PDB alignments
            
            
            
            String url = sds.getUrl() ;
            char lastChar = url.charAt(url.length()-1);      
            if ( ! (lastChar == '/') ) 
                url +="/" ;
            dasalignmentcommand  =  "alignment?query="+code ;
            
            if ( subject != null){
                dasalignmentcommand += "&subject="+subject;
            }
            
            if (subjectCoordSys != null ){
                // TODO find a nicer solution for this ..
                String scs = subjectCoordSys.toString();
                if ( scs.substring(scs.length()-1).equals(",")){
                    scs = scs.substring(0,scs.length()-1);
                }
                
                dasalignmentcommand +="&subjectcoordsys="+scs;
            }
            
//          protect the command
            try {
                dasalignmentcommand = url +  URLEncoder.encode(dasalignmentcommand,"UTF-8");
            } catch (Exception e){
            
            }
            
            //logger.info(logname + " contacing alignment server " + dasalignmentcommand);
            //System.out.println("contacing alignment server " + dasalignmentcommand);
            
            
            
            
            try{
                //alignments = dasc.getAlignments(code);
                alignments= retreiveAlignments(dasalignmentcommand);
                
                //logger.finest(logname + " DASAlignmentHandler: got "+ alignments.length +" alignment(s):");
                if ( alignments.length == 0 ) {
                    // check next alignment server ...
                    continue ;
                }
                return alignments ;
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        
        
        
        // logger.log(Level.SEVERE,logname +" no  alignment found!");
        
        
        
        return new Alignment[0] ;
    }
    
    private Alignment[] retreiveAlignments(String url)
    throws IOException
    {
        
        logger.info("requesting alignment " + url);
        /* now connect to DAS server */
        
        URL dasUrl = null ;
        try {
            dasUrl = new URL(url);
        } catch (Exception e) {
            throw new IOException("error during creation of URL " + e.getMessage());
        }
        
        InputStream inStream = connectDASServer(dasUrl);
        
        
        Alignment[] ali = null;
        try{
            ali =  dasalignmentCall.parseDASResponse(inStream) ;
        } catch (Exception e) {
            throw new IOException("error during creation of URL " + e.getMessage());
        }
        return ali;
        
    }
    
    /** connect to DAS server and return result as an InputStream.
     *
     */    
    private InputStream connectDASServer(URL url) 
    throws IOException
    {
        InputStream inStream = null ;
        
        //System.out.println("opening connection to "+url);
        HttpURLConnection huc = org.biojava.spice.SpiceApplication.openHttpURLConnection(url);  
        
        
        //System.out.println("temporarily disabled: accepting gzip encoding ");
        // should make communication much faster!
        huc.setRequestProperty("Accept-Encoding", "gzip");
        
        //System.out.println("response code " +huc.getResponseCode());
        String contentEncoding = huc.getContentEncoding();
        //System.out.println("getting InputStream");
        inStream = huc.getInputStream();
        if (contentEncoding != null) {
            if (contentEncoding.indexOf("gzip") != -1) {
                // we have gzip encoding
                inStream = new GZIPInputStream(inStream);
                //System.out.println("using gzip encoding!");
            }
        }
        //System.out.println("got InputStream from  DAS Alignment server");
        //System.out.println("encoding: " + contentEncoding);
        
        return inStream;
        
    }
    
    private void triggerNoAlignmentFound(String q, String s){
        Alignment a = new Alignment();
        
        String objectVersion    = "";
        String intObjectId      = "";
       
        String dbSource         = "";
        String dbVersion        = "";
        String dbCoordSys       = "";
        
        HashMap object1 = new HashMap() ;
        object1.put("dbAccessionId" ,q);
        object1.put("objectVersion" ,objectVersion);
        object1.put("intObjectId"   ,intObjectId);
        ArrayList details = new ArrayList();
        object1.put("details",details);
        object1.put("dbVersion"     ,dbVersion) ;
        object1.put("dbSource"      ,dbSource) ;
        object1.put("dbCoordSys"    , dbCoordSys);
        
        HashMap object2 = new HashMap() ;
        object2.put("dbAccessionId" ,s);
        object2.put("objectVersion" ,objectVersion);
        object2.put("intObjectId"   ,intObjectId);
        object2.put("dbCoordSys"    , dbCoordSys);
        
        object2.put("details",details);
        object2.put("dbVersion"     ,dbVersion) ;
        object2.put("dbSource"      ,dbSource) ;
        
         
        Annotation ob1 = AnnotationFactory.makeAnnotation(object1) ;
        Annotation ob2 = AnnotationFactory.makeAnnotation(object2) ;
        try {
            a.addObject(ob1);
            a.addObject(ob2);
        } catch (Exception e) {
            
        }
        
        AlignmentEvent event = new AlignmentEvent(a,new Alignment[0]); 
        Iterator iter = alignmentListeners.iterator();
        while (iter.hasNext()){
            AlignmentListener li = (AlignmentListener ) iter.next();
            li.noAlignmentFound(event) ;
        }
    }
}
