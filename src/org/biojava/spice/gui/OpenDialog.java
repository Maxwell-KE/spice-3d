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
 * Created on Dec 1, 2004
 *
 */
package org.biojava.spice.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

import org.biojava.spice.ResourceManager;
import org.biojava.spice.SPICEFrame;
import org.biojava.spice.SpiceApplication;
import org.biojava.spice.config.SpiceDefaults;
import org.biojava.spice.gui.msdkeyword.*;

import javax.swing.JLabel;


/**
 * Enter a new PDB, UniProt, or ENSP code to be loaded in SPICE.
 * 
 * @author Andreas Prlic
 *
 */
public class OpenDialog 
extends JDialog 
{

	private static final long serialVersionUID = 2832023723402743924L;


	static final String[] supportedCoords = { "PDB","UniProt","ENSP", "alignment"};
	//static final String[] supportedCoords = { "PDB","UniProt"};
	static int H_SIZE = 350;
	static int V_SIZE = 150 ;
	SpiceApplication spice       ;
	JTextField getCom      ;
	JComboBox  list        ;
	String     currentType ;
	JTextField kwsearch;     


	/** a dialog responsible for opening new entries.
	 * 
	 * @param parent the parent SpiceApplication instance
	 */
	public OpenDialog(SpiceApplication parent){
		// Calls the parent telling it this
		// dialog is modal(i.e true)


		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				JFrame frame = (JFrame) evt.getSource();
				frame.setVisible(false);
				frame.dispose();
			}
		});

		spice = parent ;

		this.setTitle("enter code") ;

		JPanel p = new JPanel();

		Box vBox  = Box.createVerticalBox();
		Box hBox1 = Box.createHorizontalBox();
		Box hBox2 = Box.createHorizontalBox();
		Box hBox3 = Box.createHorizontalBox();

		int startpos   = 0;
		currentType    = supportedCoords[startpos];

		list = new JComboBox(supportedCoords) ;		
		list.setEditable(false);
		list.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
		list.setSelectedIndex(startpos);

		list.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				//System.out.println("action performed");
				setHelpToolTip();     

			}        
		});



		hBox1.add(list);


		getCom = new JTextField(10);


		getCom.addActionListener(new ActionListener()  {

			public void actionPerformed(ActionEvent e) {

				String code = getCom.getText();
				String type = (String)list.getSelectedItem() ;


				if ( SpiceDefaults.argumentTypes.contains(type) ) {
					spice.load(type,code);
					dispose();			    
				} else {
					System.out.println("unknown type " + type + " code " + code);
				}
			}
		});



		hBox1.add(getCom);


		JButton openB =new JButton(ResourceManager.getString("org.biojava.spice.action.open"));
		openB.addActionListener(new openActionListener(spice,this));

		JButton cancelB = new JButton(ResourceManager.getString("org.biojava.spice.action.cancel"));
		cancelB.addActionListener(new openActionListener(spice,this));

		hBox1.add(openB);
		hBox3.add(cancelB);
		setHelpToolTip();

		kwsearch = new JTextField(10);
		kwsearch.addActionListener(new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				String kw  = kwsearch.getText();            
				new MSDWindow(spice,kw);              
				dispose();			    
			}

		});

		JButton openKw = new JButton(ResourceManager.getString("org.biojava.spice.gui.ConfigPanel.Search"));
		openKw.addActionListener(new openActionListener(spice,this));
		JLabel kwl = new JLabel("keyword:");
		hBox2.add(kwl);
		hBox2.add(kwsearch);
		hBox2.add(openKw);


		vBox.add(hBox1);
		vBox.add(hBox2);
		vBox.add(hBox3);


		p.add(vBox);
		this.getContentPane().add(p);

		this.setSize(H_SIZE, V_SIZE);
	}

	private void setHelpToolTip(){
		String type = (String) list.getSelectedItem();
		if ( type.equals("PDB")){
			getCom.setToolTipText("enter PDB code or PDB code + chain. e.g. 1tim.B");
		} else if ( type.equals("UniProt")){
			getCom.setToolTipText("enter UniProt accession code e.g. P50225");            
		} else if ( type.equals("ENSP")){
			getCom.setToolTipText("enter Ensembl peptide accession code e.g. ENSP00000334713");
		}
		// todo: alignment helper


	}

}

class openActionListener implements ActionListener {
	
	OpenDialog parent;
	SPICEFrame spice;

	public openActionListener(SPICEFrame spice, OpenDialog parent){
		this.spice = spice;
		this.parent = parent ;
	}

	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();
		if(cmd.equals("Cancel"))
		{
			parent.dispose();
			return ;
		}
		else if ( cmd.equals("Open"))
		{ 
			String type = (String)parent.list.getSelectedItem() ;
			String code = parent.getCom.getText();

			if ( SpiceDefaults.argumentTypes.contains(type)){
				spice.load(type,code);
				parent.dispose();			    
			}
			return ;	
		}
		else if ( cmd.equals("Search")){
			String kw  = parent.kwsearch.getText();

			new MSDWindow(spice,kw);    
			parent.dispose();
		}  
	} 


}

