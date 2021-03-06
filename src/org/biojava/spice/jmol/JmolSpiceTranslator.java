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
 * Created on Jan 25, 2006
 *
 */
package org.biojava.spice.jmol;


import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.dasobert.eventmodel.SequenceListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.popup.JmolPopup;
import org.jmol.viewer.JmolConstants;

public class JmolSpiceTranslator 

implements JmolStatusListener, StructureListener
{

	static Logger    logger      = Logger.getLogger("org.biojava.spice");

	static  char emptyChain = ' ';

	JmolViewer  viewer;    
	JmolPopup jmolpopup ;

	Structure structure;
	int currentChainNumber;
	List<SequenceListener> pdbSequenceListener;

	public JmolSpiceTranslator() {
		super();
		structure = new StructureImpl();
		currentChainNumber = -1;
		pdbSequenceListener = new ArrayList<SequenceListener>();
	}

	public void setJmolViewer(JmolViewer viewer){
		this.viewer = viewer;
	}

	public void setJmolPopup(JmolPopup popup){
		this.jmolpopup = popup;
	}

	public synchronized void notifyFileLoaded(String fullPathName, String fileName,
			String modelName, Object clientFile,
			String errorMessage){
		//logger.info("JmolSpiceTranslator notifyFileLoaded " + fileName + " " + modelName + " " + errorMessage);
		if (errorMessage != null){
			logger.log(Level.SEVERE,errorMessage);
		}        
	}

	public void notifyFileNotLoaded(String fullPathName, String errorMsg){}

	public void setStatusMessage(String statusMessage){
		logger.log(Level.INFO,statusMessage);
	}

	public void scriptEcho(String strEcho){
		if (  strEcho.equals("no structure found"))
			return;
		logger.log(Level.INFO, "jmol scriptEcho: " + strEcho);
	}

	public void scriptStatus(String strStatus){
		logger.log(Level.FINE,"jmol scriptStatus: " +strStatus);
	}

	public void notifyScriptTermination(String statusMessage, int msWalltime){

		//logger.fine("Script finished in " + msWalltime + "ms");
	}

	public void showUrl(String urlString) {
		logger.finest("showUrl: " +urlString);
	}

	public void showConsole(boolean showConsole){
		logger.finest("jmol: showConsole "+showConsole);
	}

	public void handlePopupMenu(int x, int y){
		//logger.finest("handlePopupMenu");
		//viewer.popupMenu(e.getX(),e.getY());
		if ( jmolpopup != null) {
			jmolpopup.show(x,y);
		}

	}



	public void notifyAtomPicked(int atomIndex, String strInfo){
		logger.info("Atom picked "  + atomIndex + " " + strInfo);

		if ( viewer != null ) {
			//int mod = viewer.getAtomModelIndex(atomIndex);
			//viewer.get
			String info = viewer.getAtomInfo(atomIndex);  
			AtomInfo ai = AtomInfoParser.parse(info);
			logger.info(info);
			logger.info(ai+"");

			//int modelNr = viewer.getAtomModelIndex(atomIndex);
			String pdbcode = ai.getResidueNumber();

			String chainId = ai.getChainId();
			int modelNr = ai.getModelNumber();
			if ( modelNr > 1) {
				logger.info("you selected an atom from model "+modelNr+" which is currently not active");
				logger.info(strInfo);
				return;
			}
			//logger.info(">"+chainId + "< >" + pdbcode +"<");
			highlitePdbPosition(pdbcode,""+chainId);
		}
	}

	private void highlitePdbPosition(String pdbresnum,String chainId){
		// notify that a particulat position has been selected

		Chain currentChain = structure.getChain(currentChainNumber);

		//logger.info("current chain is >" + currentChain.getName() + "< selected is >" + chainId + "< " + chainId.length());

		if ( (chainId == null) || (chainId.equals("")))
			chainId = " ";

		if ( currentChain.getName().equals(chainId)){
			int seqPos = getSeqPosFromPdb(pdbresnum, currentChain);
			//logger.info("is spice seq. position " + seqPos);
			if ( seqPos >=0){
				triggerSelectedSeqPos(seqPos);
			} 
		}  else {
			logger.info("selected residue " + pdbresnum + " chain >" + chainId + "< (chain currently not active in sequence dispay)");

		}
		// set the selection in Jmol...
		String cmd ;
		if (! chainId.equals(" ")) 
			cmd = "select "+pdbresnum+":"+chainId+"/1; set display selected";
		else 
			cmd = "select "+pdbresnum+"/1; set display selected";
		if ( viewer != null){
			viewer.evalString(cmd);
		}
	}

	private int getSeqPosFromPdb(String pdbresnum, Chain currentChain){
		List<Group> groups = currentChain.getAtomGroups();
		try {
			Group g = currentChain.getGroupByPDB(pdbresnum);
			return groups.indexOf(g);

		} catch (StructureException e) {
			return -1;
		}

	}



	public void notifyMeasurementsChanged(){
		logger.finest("nofiyMeasurementsChanged");
	}

	public void notifyFrameChanged(int frameNo){}


	// now the Spice Structure events ...

	public void newStructure(StructureEvent event) {
		//logger.info("JmolSpiceTranslator got new structure " + event.getPDBCode() + " " + structure.getPDBCode());
		String p = event.getPDBCode();

		if ( ( p != null ) && ( p.equalsIgnoreCase(structure.getPDBCode()))) {
			// already known
			return;
		}
		this.structure = event.getStructure();
		this.currentChainNumber = event.getCurrentChainNumber();
	}

	public void selectedChain(StructureEvent event) {
		//logger.info("JmolSpiceTranslator selected Chain" + event.getCurrentChainNumber());
		this.structure = event.getStructure();
		this.currentChainNumber = event.getCurrentChainNumber();

	}

	public void newObjectRequested(String accessionCode) {
		this.structure = new StructureImpl();
		this.currentChainNumber = -1;

	}

	public void noObjectFound(String accessionCode) {
		// TODO Auto-generated method stub

	}

	public void addPDBSequenceListener(SequenceListener li){
		pdbSequenceListener.add(li);
	}

	public void clearListeners(){
		structure = new  StructureImpl();
		pdbSequenceListener.clear();
	}

	private void triggerSelectedSeqPos(int position){
		Iterator<SequenceListener> iter = pdbSequenceListener.iterator();
		while (iter.hasNext()){
			SequenceListener li = iter.next();
			li.selectedSeqPosition(position);

		}

	}

	public void notifyNewDefaultModeMeasurement(int count, String strInfo) {
		// TODO Auto-generated method stub

	}

	public void notifyNewPickingModeMeasurement(int iatom, String strMeasure) {
		logger.info("notifyPickingModeMeasurement: " + iatom + " " + strMeasure);

	}

	public void notifyScriptStart(String statusMessage, String additionalInfo) {
		// TODO Auto-generated method stub

	}

	public void sendConsoleEcho(String strEcho) {
		// TODO Auto-generated method stub
		logger.info(strEcho);

	}

	public void sendConsoleMessage(String strStatus) {
		logger.info(strStatus);

	}

	public void sendSyncScript(String script, String appletName) {
		// TODO Auto-generated method stub
		logger.info("sendSyncScript" + script);

	}

	public void notifyAtomHovered(int atomIndex, String strInfo) {
		logger.info("over Atom " + strInfo);

	}

	public void setCallbackFunction(String callbackType, String callbackFunction) {
		System.out.println("setCallbackFunction " +callbackType + " " + callbackFunction);

	}
	
	public String eval(String strEval) {
		System.out.println("strEval called" + strEval);
		return null;
	}

	public void notifyFrameChanged(int frameNo, int fileNo, int modelNo, int firstNo, int LastNo) {
		//System.out.println("notifyFrameChanged " + frameNo);

	}

	public void notifyResized(int newWidth, int newHeight) {
		// TODO Auto-generated method stub

	}

	public float[][] functionXY(String functionName, int x, int y) {
	
		return null;
	}




	public void notifyCallback(int type, Object[] data) {
		//System.out.println("notifyCallback " + type );
		
		  switch (type) {
		    case JmolConstants.CALLBACK_ANIMFRAME:
		      break;
		    case JmolConstants.CALLBACK_ECHO:
		      sendConsoleEcho((String) data[1]);
		      break;
		    case JmolConstants.CALLBACK_HOVER:
		      break;
		    case JmolConstants.CALLBACK_LOADSTRUCT:
		      String strInfo = (String) data[1];
		      System.out.println(strInfo);
		      logger.info(strInfo);
		      break;
		    case JmolConstants.CALLBACK_MEASURE:
		      break;
		    case JmolConstants.CALLBACK_MESSAGE:
		      sendConsoleMessage((String) data[1]);
		      break;
		    case JmolConstants.CALLBACK_MINIMIZATION:
		      break;
		    case JmolConstants.CALLBACK_PICK:
		      //for example:
		      notifyAtomPicked(((Integer) data[2]).intValue(), (String) data[1]);
		      break;
		    case JmolConstants.CALLBACK_RESIZE:
		      break;
		    case JmolConstants.CALLBACK_SCRIPT:
		    	logger.info("callback script");
		      break;
		    case JmolConstants.CALLBACK_SYNC:
		      break;
		    }

	}

	public boolean notifyEnabled(int callback_pick) {
		System.out.println("notifyEnabled " + callback_pick);
		return false;
	}


	public String createImage(String file, String type, Object text_or_bytes,
			int quality) {
		logger.info("iamge export not yet supported...");
		return "";
		
	}

	private String createPdfDocument(File file) {
		logger.info("pdf not yet supported...");
		return "";
	}


	int qualityJPG = -1;
	int qualityPNG = -1;
	String imageType;
	final static String[] imageChoices = { "JPEG", "PNG", "GIF", "PPM", "PDF" };
	final static String[] imageExtensions = { "jpg", "png", "gif", "ppm", "pdf" };


	public String dialogAsk(String type, String fileName) {

		return null;
	}

	public Hashtable getRegistryInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
