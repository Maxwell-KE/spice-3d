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
 * Created on May 3, 2006
 *
 */
package org.biojava.spice;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.bio.Annotation;
import org.biojava.bio.program.das.dasalignment.Alignment;
import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.GroupIterator;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.DASStructureClient;
import org.biojava.bio.structure.jama.Matrix;
import org.biojava.dasobert.das.AlignmentParameters;
import org.biojava.dasobert.das.AlignmentThread;
import org.biojava.dasobert.das.SpiceDasSource;
import org.biojava.dasobert.eventmodel.AlignmentEvent;
import org.biojava.dasobert.eventmodel.AlignmentListener;
import org.biojava.dasobert.eventmodel.StructureEvent;
import org.biojava.dasobert.eventmodel.StructureListener;
import org.biojava.spice.Panel.StructurePanel;

/** a class that builds up a structure alignment and creates a StructureAlignment object
 * 
 * @author Andreas Prlic
 * @since 2:36:11 PM
 * @version %I% %G%
 */
public class StructureAlignmentBuilder
implements AlignmentListener {
    
    SpiceDasSource[] alignmentServers;
    SpiceDasSource[] structureServers;
    List structureListeners;
    StructurePanel structurePanel;
    
    public static Logger logger = Logger.getLogger("org.biojava.spice");
    
    public StructureAlignmentBuilder(){
        super();
        alignmentServers = new SpiceDasSource[0];
        structureServers = new SpiceDasSource[0];
        structureListeners = new ArrayList();
        structurePanel = null;
    }
    
    public void setStructurePanel(StructurePanel panel){
        structurePanel = panel;
    }
    public void setAlignmentServers(SpiceDasSource[] alignmentServers) {
        this.alignmentServers = alignmentServers;
    }
    
    public void setStructureServers(SpiceDasSource[] structureServers) {
        this.structureServers = structureServers;
    }
    
    
    public void request(String alignmentCode){
        logger.info("using " + alignmentServers.length + "alignment servers");
        AlignmentParameters params = new AlignmentParameters();
        params.setDasSources(alignmentServers);
        logger.info("requesting alignment for" + alignmentCode);
        if ( alignmentServers.length > 0){
            logger.info(alignmentServers[0].getUrl());
        }
        params.setQuery(alignmentCode);
        
        AlignmentThread thread =    new AlignmentThread(params);
        thread.addAlignmentListener(this);
        
        thread.start();
    }
    
    public void clearAlignment() {
        
        
    }
    
    private Atom[] getCaAtoms(Structure struc) throws StructureException{
        GroupIterator iter = new GroupIterator(struc);
        List atoms = new ArrayList();
        while ( iter.hasNext()){
            Group g = (Group)iter.next();
            if ( ! (g instanceof AminoAcid)) 
                continue;
            Atom ca = g.getAtom("CA");
            atoms.add(ca);
        }
        
        return (Atom[]) atoms.toArray(new Atom[atoms.size()]);
    }
    
    
    public synchronized void newAlignment(AlignmentEvent e) {
        Alignment ali = e.getAlignment();
        logger.info("got new alignment " + ali);
        
        Annotation[] matrices = ali.getMatrices();
        for (int i=0 ; i< matrices.length;i++){
            Annotation a = matrices[i];
            System.out.println(a);
        }
        
        // get the first two structures and superimpose them..
        Annotation[] objects = ali.getObjects();
        Annotation[] vectors = ali.getVectors();
        Annotation[] blockx  = ali.getBlocks();
        Annotation[] blocks = new Annotation[blockx.length];
        // sort blocks!
        
        for (int i = 0; i < blockx.length; i++) {
            boolean found = false;
            for (int bb = 0 ; bb <blockx.length;bb++){
                Annotation blo = blockx[bb];
                String bo = (String) blo.getProperty("blockOrder");
                int boi = Integer.parseInt(bo);
                if (boi == i+1) {
                    blocks[i] = blo;
                    found = true;
                }
            }
            if ( ! found){
                System.out.println("did not find blockOrder "+i);
            }
        }
        
        if (objects.length > 3) {
            
            Annotation o1 = objects[1];
            Annotation o2 = objects[2];
            
            Annotation m1 = matrices[1];
            Annotation m2 = matrices[2];
            
            Annotation v1 = vectors[1];
            Annotation v2 = vectors[2];
            
            
            
            // get pdb code
            String intId1 = (String) o1.getProperty("intObjectId");
            String intId2 = (String) o2.getProperty("intObjectId");
            
            String pdb1 = intId1.substring(0,4);
            String pdb2 = intId2.substring(0,4);
            
            Matrix max1 = getMatrix(m1);
            Matrix max2 = getMatrix(m2);
            
            max1.print(3,3);
            max2.print(3,3);
            
            //max1 = max1.transpose();
            //max2 = max2.transpose();
            
            Atom vec1 = getVector(v1);
            Atom vec2 = getVector(v2);
            
            
            // now request the structures...
            String dasstructurecommand = structureServers[0].getUrl() + "structure?model=1&query=";
            
            try {
                
                Structure s1 = getStructure(dasstructurecommand,pdb1);
                Structure s2 = getStructure(dasstructurecommand,pdb2);
                
                //Structure s2 = dasc.getStructureById(pdb2);
                
                Structure a1 = getRegions(s1,intId1,blocks,"A");
                Structure a2 = getRegions(s2,intId2,blocks,"A");
                
                Atom[] ca1 = getCaAtoms(a1);
                Atom[] ca2 = getCaAtoms(a2);
                
                SVDSuperimposer svd = new SVDSuperimposer(ca1,ca2);
                Matrix max3 = svd.getRotation();
                        
                max3.print(3,3);
                Atom  vec3 = svd.getTranslation();
                
                
                
                Calc.rotate(s1,max1);
                Calc.rotate(s2,max2);
                Calc.shift(s1,vec1);                
                Calc.shift(s2,vec2);
               
                //System.out.println("RMS:" + SVDSuperimposer.getRMS(ca1,ca2));
                
                //Atom vec1 = Calc.getCenterVector(ca1);
                //Atom vec2 = Calc.getCenterVector(ca2);
                
                
                //Atom vec1 = Calc.getCentroid(ca1);
                //Atom vec2 = Calc.getCentroid(ca2);
                
                //Atom nu = new AtomImpl();
                //nu.setX(0); nu.setY(0); nu.setZ(0);
                
                //vec1 = Calc.substract(nu,vec1);
                //vec2 = Calc.substract(nu,vec2);
                
                //System.out.println(vec1);
                //System.out.println(vec2);
                
                
                //Calc.rotate(s1,max1);
                //Calc.rotate(s2,max2);
                
                //Calc.shift(s1,vec1);
                //Calc.shift(s2,vec2); 
                
                Structure newStruc = createNewStructure(s1,s2);
                
                StructureEvent event = new StructureEvent(newStruc);
                
                Iterator iter = structureListeners.iterator();
                while (iter.hasNext()){
                    StructureListener li = (StructureListener)iter.next();
                    li.newStructure(event);
                }
                
                // create color command ...
                String cmd1 = getRasmolScript(blocks, intId1, "A", "red") ;
                String cmd2 = getRasmolScript(blocks, intId2, "B", "blue") ;
                
                
                System.out.println(cmd1+cmd2);
                structurePanel.executeCmd(cmd1+cmd2);
                
            } catch (Exception ex){
                ex.printStackTrace();
            }
            
        }
        
    }
    
    private String getRasmolScript(Annotation[] blocks, String intId, String chainId,String color){
        String cmd = "";
        for (int b=0;b<blocks.length;b++){
            Annotation block = blocks[b];
            
            List segments = (List)block.getProperty("segments");
            Iterator siter = segments.iterator();
            while (siter.hasNext()){
                Annotation seg = (Annotation)siter.next();
                String ii =  (String)seg.getProperty("intObjectId");
                String start = (String) seg.getProperty("start");
                String end   = (String) seg.getProperty("end");
                
                //String chain ="";
                int indx1 = start.indexOf(":");
                if ( indx1 >-1) 
                    start = start.substring(0,indx1);
                
                int indx2 = end.indexOf(":");
                if ( indx2>-1)
                    end = end.substring(0,indx2);
                
                if ( ii.equals(intId)){
                    cmd += "select "+start+"-"+end +":"+chainId+"; color "+color+";";                            
                }
                
                
            }
            
        }
        return cmd;
    }
    
    private Structure getRegions(Structure struc, String intId, Annotation[] blocks, String chainName) 
    throws StructureException {
        Structure newstruc = new StructureImpl();
        Chain n = new ChainImpl();
        n.setName(chainName);
        
        for (int b=0;b<2;b++){
            Annotation block = blocks[b];
            List segments = (List)block.getProperty("segments");
            Iterator siter = segments.iterator();
            while (siter.hasNext()){
                Annotation seg = (Annotation)siter.next();
                String ii =  (String)seg.getProperty("intObjectId");
                if (!( ii.equals(intId))) 
                    continue;
                String start = (String) seg.getProperty("start");
                String end   = (String) seg.getProperty("end");
                
                int indx1 = start.indexOf(":");
                String chainId1 = " ";
                String chainId2 = " ";
                System.out.println(start);
                System.out.println(end);
                
                if (indx1 >-1) {
                    chainId1 = start.substring(indx1+1,start.length());
                    start = start.substring(0,indx1);
                    
                }
                int indx2 = end.indexOf(":");
                if ( indx2>-1) {
                    chainId2 = end.substring(indx2+1,end.length());
                    end = end.substring(0,indx2);                    
                }
                
                if ( chainId1.equals(""))
                    chainId1 = " ";
                Chain c = struc.getChainByPDB(chainId1);
                Group[] groups = c.getGroupsByPDB(start,end);
                for (int g=0;g< groups.length;g++){
                    Group gg =groups[g];
                    n.addGroup(gg);
                }
                
            }
            
        }
        newstruc.addChain(n);
        System.out.println(n.getSequence());
        return newstruc;
    }
    
    private Structure getStructure(String dasstructurecommand,String pdbCode)
    throws IOException {
        DASStructureClient dasc= new DASStructureClient(dasstructurecommand);
        Structure s = dasc.getStructureById(pdbCode);
        return s;
        
    }
    
    public void clearListeners(){
        structureListeners.clear();
    }
    
    public void addStructureListener(StructureListener li){
        structureListeners.add(li);
    }
    
    private Structure createNewStructure(Structure s1, Structure s2) 
    throws StructureException {
        
        Structure n = new StructureImpl();
        
//      List chains1 = s1.getChains(0);
//      
//      n.addModel(chains1);
//      
//      List chains2 = s2.getChains(0);
//      n.addModel(chains2);
//      n.setNmr(true);
//      //System.out.println(n.toPDB());
//      System.out.println(n.size());
//      return n;
        
        
        Chain c1 = new ChainImpl();
        c1.setName("A");
        Chain co1 = s1.getChainByPDB("A");
        Structure n1 = new StructureImpl();
        n1.addChain(co1);
        
        GroupIterator iter1 = new GroupIterator(n1);
        while (iter1.hasNext()){
            Group g = (Group) iter1.next();
            c1.addGroup(g);
        }
        n.addChain(c1);
        
        Chain c2 = new ChainImpl();
        c2.setName("B");
        
        Chain co2 = s2.getChainByPDB("A");
        Structure n2 = new StructureImpl();
        n2.addChain(co2);
        
        GroupIterator iter2= new GroupIterator(n2);
        while (iter2.hasNext()){
            Group g = (Group) iter2.next();
            c2.addGroup(g);
        }
        n.addChain(c2);
        return n;
        
        
    }
    
    private Atom getVector(Annotation anno){
        Atom vec = (Atom) anno.getProperty("vector");
        
        //vec.setX( getDoubleFromAnnotation(anno,"x"));
        //vec.setY( getDoubleFromAnnotation(anno,"y"));
        //vec.setZ( getDoubleFromAnnotation(anno,"z"));
        
        return vec;
        
        
    }
    
//  private double getDoubleFromAnnotation(Annotation anno, String property){
//  System.out.println("get double from: " +  anno);
//  String xs = (String)anno.getProperty(property);
//  double x = Double.parseDouble(xs);
//  return x;
//  
//  }
    
    private Matrix getMatrix(Annotation anno){
        Matrix max = new Matrix(3,3);
        
        for(int x=1;x<4;x++){
            for(int y=1;y<4;y++){
                String m = "mat"+x+y;
                String val = (String)anno.getProperty(m);
                double d = Double.parseDouble(val);
                max.set(x-1,y-1,d);
            }
        }
        
        return max;
    }
    
    public synchronized void noAlignmentFound(AlignmentEvent e) {
        logger.info("no alignment found " + e.getAlignment());
        
    }
    
    
    
}