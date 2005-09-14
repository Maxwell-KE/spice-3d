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
 * Created on 21.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice.Feature ;

import org.biojava.spice.DAS.*		   ;
import java.util.logging.*             ;
import java.util.ArrayList             ;
import java.util.Map;
import java.net.URL                    ;
import org.biojava.spice.Config.*;
//import java.util.Iterator;

/** a thread that connects to a DAS - Feature service
 * and sets results in "parent thread" FeatureFetcher.
 *@author Andreas Prlic
 */

public class SingleFeatureThread
extends Thread 

{
    URL dascommand ;
    FeatureFetcher parentfetcher ;
    int myId ;
    Logger logger        ;
    SpiceDasSource dasSource;
    
    /** contact a single DAS feature server and retreive features 
     @param parent a link to the parent class
     @param urlstring string of server
     @param threadid id for this thread, if job is finished parent
     is told that threadid has finised
     */
    public SingleFeatureThread(FeatureFetcher parent, URL dascmd,int threadid, SpiceDasSource ds) {
        logger = Logger.getLogger("org.biojava.spice");
        logger.finest("init new thread " + threadid + " " + dascmd);
        dascommand = dascmd ;
        parentfetcher = parent ;
        myId = threadid ;
        dasSource = ds;
    }
    /** start thread */
    public void run() {
        doDasConnection();
        
    }
    
    private synchronized void doDasConnection() {
        logger.finer("opening " + dascommand);
        DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(dascommand);
        ArrayList features = ftmp.get_features();
        
        /*Iterator iter =features.iterator();
        	while (iter.hasNext()){
        	    Map f = (Map)iter.next();
        	    logger.info(f+"");
        	}*/
        
        //logger.info("SingleeFeatureThread got " + features.size() + " features") ;
        //new ArrayList();
        //ArrayList tmp = 
        //for (int i=0; i<tmp.size();i++){
        //   HashMap feat = (HashMap)tmp.get(i);			
        //logger.finest("got feature: "+feat);
        //    features.add(feat) ;		
        //} 
        //logger.finest("done "+ dascommand);

        // now with support for stylesheets.
        
        Map[] typeStyle = dasSource.getStylesheet();
        // is null if no stylesheet has been loaded ...
        if ( typeStyle == null){
            dasSource.loadStylesheet();
            typeStyle = dasSource.getStylesheet();
            for ( int m=0; m< typeStyle.length;m++){
                logger.finest("got stylesheet: " + typeStyle[m]);    
            }
        }
        
         
        

        parentfetcher.setFinished(myId,features);
        notifyAll();
    }
    
}
