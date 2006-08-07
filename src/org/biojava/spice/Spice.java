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
 * Copyright for this cilode is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 06.10.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.spice ;

import org.biojava.spice.server.SpiceClient;
import org.biojava.spice.server.SpiceServer;
import org.biojava.spice.config.ConfigurationException;
import java.util.ArrayList;
import org.biojava.spice.gui.AboutDialog;
import org.biojava.spice.gui.LoggingPanelManager;
import org.biojava.spice.gui.SpiceTabbedPane;
import java.util.List;
import org.biojava.spice.utils.CliTools;

/** SPICE is a browser for protein structure and sequence annotations built on the Distributed Annotation System.
 * For more information on SPICE please visit the homepage at http://www.efamily.org.uk/software/dasclients/spice/
 * 
 * This is startup class of the SPICE application. It takes care of correctly parsing the arguments and
 * either sends them to an already running instance of SPICE or creates a new instance.
 *  
 * @author Andreas Prlic
 * 
 * */
public class Spice {
    
    private static final long serialVersionUID = 8273923744127087423L;
    
    
    /** the main call of the SPICE.
     * it takes a number of arguments and passes them to the  {@link SpiceApplication}.
     * Please look at the {@link SpiceStartParameters} class to get a complete list of arguments.
     * 
     * some example arguments:
     * <ul>
     *  <li><b>-codetype</b> the <i>type</i> of the provided code. currently supported: <i>PDB</i> and <i>UniProt</i>.</li>
     *  <li><b>-code</b>the <i>Accession code</i>. e.g. 5pti for PDB or P00280 for UniProt.</li>
     *  <li><b>-registry</b> the URL for the DAS - registration web service. Usually will be http://servlet.sanger.ac.uk/dasregistry/services/das_registry</li>
     *  <li><b>-backupRegistry</b> (optional) the URl for a backup registration service. To be used if the primary service provided by <i>-registry</i> fails.</li>
     *  <li><b>-display</b> (optional) a list of DAS - sources (by their unique Id from registry) to be highlited. A ";" separated list of DAS source ids e.g. DS_101;DS_102;DS_110</li>.
     *  <li><b>-displayLabel</b> (optional) Choose all das source belonging to a particular label to be highlited. A ";" separated list of labels e.f. biosapiens;efamily;</li>
     *  <li><b>-rasmolScript</b> (optional) Send a rasmol script to be executed after the (first) structure has been loaded. 
     *  <li><b>-seqSelectStart</b> (optional) Select a region in sequence coordinates (start position).
     *  <li><b>-seqSelectEnd</b> (optional) Select a region in sequence coordinates (end position).
     *  <li><b>-pdbSelectStart</b> (optional) Select a region in PDB resnum coordinates (start position).
     *  <li><b>-pdbSelectEnd</b> (optional) Select a region in PDB resnum coordinates (end position).
     *  <li><b>-displayMessage</b> (optional) display a (html formatted) message when the structure has been loaded.
     *  <li><b>-displayMessageWidth</b> (optional) set the width of the message window.
     *  <li><b>-displayMessageHeight</b> (optional) set the height of the message window. 
     *  <li><b>-localServerURL</b> (optional) add a "local" i.e. not registered DAS server
     * 
     * @param argv
     * @see SpiceStartParameters
     */
    public static void main(String[] argv) {
        
        javax.swing.SwingUtilities.invokeLater(new SpiceRunnable(argv));
        
    }
    
    
    
    /** launch a new  {@link SpiceApplication} 
     * @param params the parameters to use
     * */
    protected void triggerSpice(SpiceStartParameters params){
        
        System.out.println("Welcome to the SPICE - DAS client!");
        System.out.println("SPICE version: " + AboutDialog.VERSION);
        System.out.println("displaying for you: " + params.getCodetype() + " " + params.getCode());
        
        /** test if already one instance of SPICE is running
         * if yes the code should be displayed there...
         * 
         */
        boolean serverFound = testSendToServer(params);
        
        
        if (  serverFound){
            // quit this SPICE instance, 
            // the code is being loaded in SPICE in another instance that is already running ...
            String msg = " sent " + params.getCodetype() + " " + params.getCode() + " to already running spice instance";
            
            System.out.println(msg);
            
            System.exit(0);
        }
        
        //  start a spice instance
        createNewInstance(params);
    }
    
    
    
    private void createNewInstance (SpiceStartParameters params) {
        
        System.out.println("no spice instance has been found - starting new one for "+ 
                params.getCodetype() + " " + params.getCode());
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoggingPanelManager.show();
            }
        });
        
        
        SpiceApplication app = new SpiceApplication(params);
        SpiceServer server = new SpiceServer();
        
        
        
        server.registerInstance(app);
        app.setSpiceServer(server);
        new SpiceTabbedPane(server,app);      
                
        // and display the accession code...
        app.load(params.getCodetype(),params.getCode());
        
    }
    
    
    private boolean testSendToServer(SpiceStartParameters params){
        
        SpiceClient sc = new SpiceClient();
        
        try {
//          contact the port at which SPICE is listening ...
            // if successfull communication, no need to start another SPICE window...
            int status = sc.send(params);
            if  (status == SpiceClient.SPICE_SUBMITTED )
                return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        
        return false;
        
    }
    
}




class SpiceRunnable implements Runnable {
    String[] argv ;
    public SpiceRunnable (String[] argv) {
        this.argv = argv;
    }
    
    public void run() {
        createAndShowGUI(argv);
    }
    
    
    public static void createAndShowGUI(String[] argv){
        Spice app = new Spice();
        SpiceStartParameters params = new SpiceStartParameters();
        // init the configuration
        
        List mandatoryArgs= new ArrayList();
        mandatoryArgs.add("primaryRegistry");
        mandatoryArgs.add("code");
        mandatoryArgs.add("codetype");
        
        
        for (int i = 0 ; i < argv.length; i++){
            String arg   = argv[i];
            String value = argv[i+1];
            i++;
            String[] tmp = {arg,value};
            //System.out.println(arg + " " + value);
            
            try {
                CliTools.configureBean(params, tmp);        
            } catch (ConfigurationException e){
                e.printStackTrace();
                if ( mandatoryArgs.contains(arg) ) {
                    // there must not be a ConfigurationException with mandatory arguments.
                    return;
                } else {
                    // but there can be with optional ...
                }
            }           
        }
        
        app.triggerSpice(params);
        
    } 
    
    
    
}





