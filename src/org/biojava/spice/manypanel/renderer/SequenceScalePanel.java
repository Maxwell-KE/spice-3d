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
 * Created on Nov 8, 2005
 *
 */
package org.biojava.spice.manypanel.renderer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.logging.*;
import javax.swing.JPanel;
import org.biojava.bio.structure.*;
import java.awt.Color;
import java.util.*;


/** a class that draws a Sequence as a rectange, a scale display over it
 * 
 */
public class SequenceScalePanel
extends JPanel{
    
    static final long serialVersionUID = 7893248902423l;
    
    Logger logger = Logger.getLogger("org.biojava.spice");

    static String baseName = "spice";    
    
    public static final int    DEFAULT_X_START          = 10  ;
    public static final int    DEFAULT_X_RIGHT_BORDER   = 40 ;
    public static final int    DEFAULT_Y_START          = 0 ;
    public static final int    DEFAULT_Y_STEP           = 10 ;
    public static final int    DEFAULT_Y_HEIGHT         = 8 ;// thes size of the boxs
    public static final int    DEFAULT_Y_BOTTOM         = 16 ;
    public static final int    LINE_HEIGHT              = 10 ;    
    public static final int    MINIMUM_HEIGHT           = 20;
    public static final Color  SEQUENCE_COLOR           = Color.LIGHT_GRAY;
    public static final Color  SCALE_COLOR              = Color.black;
    public static final Color  TEXT_SCALE_COLOR         = Color.GRAY;
    public static final Color  BACKGROUND_COLOR;
    public static final Font   seqFont ;
    
    // the scale value after which to show the sequence as text    
    private static final int   SEQUENCE_SHOW = 9;
    
    // the height of the panel
    public static final int SIZE = 20;
       
    Chain chain;
    int chainLength;
    float scale;
    Character[] seqArr;    
    
    static {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName);
        String fontName = bundle.getString("org.biojava.spice.manypanel.renderer.SequenceScalePanel.FontName");
        String fontSize = bundle.getString("org.biojava.spice.manypanel.renderer.SequenceScalePanel.FontSize");
        int fsize = Integer.parseInt(fontSize);
        seqFont = new Font(fontName,Font.PLAIN,fsize);
        
        String col1 = bundle.getString("org.biojava.spice.manypanel.renderer.BackgroundColor");
        BACKGROUND_COLOR = Color.decode(col1);
        //TODO: move the other static parameters into the .property file
    }
    
    
    public SequenceScalePanel() {
        super();
        
        this.setBackground(BACKGROUND_COLOR);
        
        chain = new ChainImpl();
        setDoubleBuffered(true);
        
        seqArr = new Character[0];       
        chainLength = 0;
        setPrefSize();
        
    }
    
    private void setPrefSize() {
        // hm if we do not add +2 to the length then there is a size mismatch to the other panels.
        // so I guess there is a +/-1 issue somewhere...        
        int length = chainLength  ; 
        int l = Math.round(length*scale) + DEFAULT_X_START + DEFAULT_X_RIGHT_BORDER ;
        if ( l  < 200){
            l = 200;
        }
        this.setPreferredSize(new Dimension(l,SIZE));
        
    }
     
    public synchronized void setChain(Chain c){
     
        List a = c.getGroups("amino");
        seqArr = new Character[a.size()];
        
        chain = new ChainImpl();
        
        Iterator iter = a.iterator();
        int i = 0;
        while (iter.hasNext()){
            AminoAcid aa = (AminoAcid) iter.next();
            chain.addGroup(aa);
            seqArr[i] = aa.getAminoType();
            i++;
        }

        chainLength = i;
        
        setPrefSize();
        
        this.repaint();  
    }
    
    public synchronized float getScale(){
        return scale;
    }
    
    
    public void setScale(float scale) {
        
        this.scale=scale;
     
        setPrefSize();
     
        this.repaint();
        this.revalidate();
    }
  
    /** set some default rendering hints, like text antialiasing on
     * 
     * @param g2D the graphics object to set the defaults on
     */
    protected void setPaintDefaults(Graphics2D g2D){
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
       
       g2D.setFont(seqFont);
    }
    
    public void paintComponent(Graphics g){
        //super.paintComponent(g);
       
        g.setColor(BACKGROUND_COLOR);
    
        
        Rectangle drawHere = g.getClipBounds();        
        g.fillRect(drawHere.x,drawHere.y, drawHere.width, drawHere.height);
        
        
        Graphics2D g2D =(Graphics2D) g;

        setPaintDefaults(g2D);
       
        int y = 1;
            
        //  1st: draw the scale        
      
        y = drawScale(g2D,1);
        
        // 2nd: sequence
        y = drawSequence(g2D,y);
        
       
    }
    
    /** start counting at 0...
     * 
     * @param panelPos
     * @return the sequence position
     */
    protected int getSeqPos(int panelPos){
        int seqPos = Math.round((panelPos - DEFAULT_X_START) / scale) ;
        if ( seqPos < 0)
            seqPos = 0;
        int length = chainLength;
        if ( seqPos >= length)
            seqPos = length-1;
        return seqPos;
    }
    
    protected int getPanelPos(int seqPos){
        int length = chainLength;
        

        if ( seqPos < 0 )
            seqPos = 0;
        
        if ( seqPos >= length)
            seqPos = length-1;

        int panelPos = Math.round(seqPos * scale) + DEFAULT_X_START;
        return panelPos;
    }

    /** draw the Scale
     * 
     * @param g2D
     * @param y the height on which to draw the scale
     * @return the new y position
     */
    protected int drawScale(Graphics2D g2D, int y){
        
        // only draw within the ranges of the Clip
        Rectangle drawHere = g2D.getClipBounds();        
      
        g2D.setColor(SCALE_COLOR);
        
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1)
            aminosize = 1;
        
       
        
        int startpos = getSeqPos(drawHere.x);       
        int endpos   = getSeqPos(drawHere.x+drawHere.width);
    
        int l = endpos - startpos ;        
        int drawStart = getPanelPos(startpos);
        int drawEnd   = getPanelPos(l);
        
     
        
//      the frame around the sequence box
        if ( scale < SEQUENCE_SHOW){
            g2D.setColor(SEQUENCE_COLOR);
            Rectangle seqline = new Rectangle(drawStart, y, drawEnd, LINE_HEIGHT);
            
            //g2D=  (Graphics2D)g;
            g2D.fill(seqline);   
            //g2D.setColor(Color.blue);
            //g2D.draw(seqline);
        }
        
        // the top line for the scale
        g2D.setColor(SCALE_COLOR);
        Rectangle baseline = new Rectangle(drawStart, y, drawEnd, 2);        
        g2D.fill(baseline);
     
        
        // draw the vertical ticks
        for (int i =startpos ; i<= endpos ; i++){
            int xpos = getPanelPos(i) ;
            
            int lineH = 11;
            if ( scale <= 3)
                lineH = 8;
            
            if ( ((i+1)%100) == 0 ) {
                
                if ( scale> 0.1) {
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos, y+2, aminosize, y+lineH);
                    g2D.setColor(SCALE_COLOR);
                    if ( scale < SEQUENCE_SHOW)
                        g2D.drawString(""+(i+1),xpos,y+DEFAULT_Y_STEP);
                }
                
            }else if  ( ((i+1)%50) == 0 ) {
                if ( scale>1.4) {                    
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos,y+2, aminosize, y+lineH);  
                    g2D.setColor(SCALE_COLOR);
                    if ( scale < SEQUENCE_SHOW)
                        g2D.drawString(""+(i+1),xpos,y+DEFAULT_Y_STEP);
                   
                }
                
            } else if  ( ((i+1)%10) == 0 ) {                
                if ( scale> 3) {
                    g2D.setColor(TEXT_SCALE_COLOR);
                    g2D.fillRect(xpos, y+2, aminosize, y+lineH);
                    g2D.setColor(SCALE_COLOR);
                    if ( scale < SEQUENCE_SHOW)
                        g2D.drawString(""+(i+1),xpos,y+DEFAULT_Y_STEP);
                    
                }
            } 
        }
        
        
        int length = chainLength;       
        if ( endpos >= length-1) {
            
            int endPanel = getPanelPos(endpos);
            g2D.drawString(""+length,endPanel+10,y+DEFAULT_Y_STEP);
        }
        
        return y ;
        
    }
    
    
    
    /** draw the sequence
     * 
     * @param g2D
     * @param y .. height of line to draw the sequence onto
     * @return the new y value
     */
    protected int drawSequence(Graphics2D g2D,  int y){
        //g2D.drawString(panelName,10,10);
        
        g2D.setColor(SEQUENCE_COLOR);
        int aminosize = Math.round(1*scale);
        if ( aminosize < 1)
            aminosize = 1;
        
        // only draw within the ranges of the Clip
        Rectangle drawHere = g2D.getClipBounds();        
        int startpos = getSeqPos(drawHere.x);       
        int endpos   = getSeqPos(drawHere.x+drawHere.width+1);
               
        
        Composite oldComp = g2D.getComposite();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));  
        //logger.info("paint l " + l + " length " + length );
        
        if ( startpos < 0)
            startpos = 999;
        
        if ( scale > SEQUENCE_SHOW){
            g2D.setColor(Color.black);
                  
         
            //g2D.setColor(SCALE_COLOR);
            
            // display the actual sequence!;
            for ( int i = startpos ; ((i <= endpos) && ( i < seqArr.length)) ;i++){
                int xpos =getPanelPos(i);
                                
                // TODO:
                // color amino acids by hydrophobicity
                
                g2D.drawString(seqArr[i].toString(),xpos+1,y+2+DEFAULT_Y_STEP);
            }     
            
//          in full sequence mode we need abit more space to look nice
            
            y+=2;  
        }
        g2D.setComposite(oldComp);
        y+= DEFAULT_Y_STEP + 2;
        return y;
    }
    
    
}
