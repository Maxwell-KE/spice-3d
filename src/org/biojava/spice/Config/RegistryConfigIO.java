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
 * Created on 20.09.2004
 * @author Andreas Prlic
 *
 */


package org.biojava.spice ;

// to get config file via http
import java.net.HttpURLConnection ;
import java.net.URL;
import java.io.IOException ;

import java.util.HashMap   ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.List ;
// for DAS registration server:
import org.biojava.services.das.registry.*;


// for GUI;

import java.awt.Frame ;
import java.awt.event.*    ;
import javax.swing.Box ;
import javax.swing.border.TitledBorder ;
import javax.swing.JTextField  ;
import javax.swing.JButton ;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/** a class to contact and retreive the configuration from a DAS
 * registry server.
 * also provide GUI window to active / deactive DAS servers.
 */
public class RegistryConfigIO 
    extends Thread
    
{



    URL REGISTRY  ;
	
    RegistryConfiguration config    ;
    boolean done ;

    JProgressBar progressBar ;
    JFrame progressFrame      ;
    SPICEFrame spice  ;
    public RegistryConfigIO ( SPICEFrame parent , URL registryurl) {
	spice = parent ;
	REGISTRY = registryurl ;
	done = false ;
    }
    public boolean isDone(){
	return done ;
    }

    public void run(){
	try {
	    getData();
	} catch ( ConfigurationException e) {
	    e.printStackTrace();
	}
    } 


    /** contact the das registry service and retreive new Data */
    private synchronized void getData()
	throws ConfigurationException
    {
	// show dialog
	showProgressBar();
	
	System.out.println("DAS Registry server config thread loadData");
	done = false ;
	System.out.println("contacting DAS registry server at: " +REGISTRY);
	DasRegistryAxisClient rclient = new DasRegistryAxisClient(REGISTRY);
	
	DasSource[] sources = rclient.listServices();
	
	if ( sources==null) {
	    done = true ; 
	    throw new ConfigurationException("Could not connect to registration service at " + REGISTRY);
	}
	System.out.println("found "+sources.length+" servers"); 
	//config = getDasServers(sources); 
	config = new RegistryConfiguration();
	ArrayList servers = new ArrayList();
	
	for (int i = 0 ; i < sources.length; i++) {
	    DasSource s = sources[i];	    
	    SpiceDasSource sds = new SpiceDasSource();
	    sds.fromDasSource(s);
	    config.addServer(sds,true);
	}

	done = true ; 
	disposeProgressBar();
	notifyAll(); 


	
    }
    

    private void showProgressBar(){
	progressFrame = new JFrame("contacting DAS registration service");
	progressFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	/*progressFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent evt) {
		    Frame frame = (Frame) evt.getSource();
		    frame.setVisible(false);
		    frame.dispose();
		}
	    });
	*/
	ImageIcon icon = createImageIcon("spice.jpg");
	progressFrame.setIconImage(icon.getImage());

	progressFrame.setDefaultLookAndFeelDecorated(false);
	//progressFrame.setUndecorated(true);

	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	Box vbox = Box.createVerticalBox();
	JLabel txt = new JLabel("detecting available DAS servers", JLabel.RIGHT);	
	vbox.add(txt);

	progressBar = new JProgressBar();
	progressBar.setStringPainted(true); //get space for the string
	progressBar.setString("");          //but don't paint it
	progressBar.setIndeterminate(true);
	
	//progressBar.setMaximum(100);
	//progressBar.setValue(50);
	
	vbox.add(progressBar);

	JLabel server = new JLabel("contacting "+REGISTRY, JLabel.RIGHT);	
	vbox.add(server);
	panel.add(vbox);
	progressFrame.getContentPane().add(panel);
	progressFrame.pack();
	progressFrame.setVisible(true);
	
    }
    private void disposeProgressBar(){
	progressFrame.dispose();
    }

    /** write back the config to the SPICE application */
    public void saveConfiguration() {
	spice.setConfiguration(config);
    }

    /** returns the Config for SPICE */
    public RegistryConfiguration getConfiguration() {
	while (! isDone()) {	  
	    try {
		wait(30);
	    } catch (InterruptedException e) {
		e.printStackTrace();
		done = true ;
	    }
	}
	return config ; 
    }


    
    
   

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = SpiceApplication.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public void showConfigFrame(){

        //Create and set up the window.
        JFrame frame = new JFrame("SPICE configuration window");
	frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	//Make sure we have the standard desktop window decorations.
        JFrame.setDefaultLookAndFeelDecorated(false);
	ImageIcon icon = createImageIcon("spice.jpg");
	frame.setIconImage(icon.getImage());
	
        //Create and set up the content pane.
        //JComponent newContentPane = new TabbedPaneDemo(config);
        //newContentPane.setOpaque(true); //content panes must be opaque

        //frame.getContentPane().add(new TabbedPaneDemo(config),
	//                       BorderLayout.CENTER);
	TabbedPaneDemo tpd = new TabbedPaneDemo(this,config);
	//frame.getContentPane().add(tpd);

	Box vbox = Box.createVerticalBox();
	vbox.add(tpd);

		
	JButton saveb   = new JButton("Save");
	JButton cancelb = new JButton("Close");
	
	saveb.addActionListener(   new ButtonListener(frame, tpd) );
	cancelb.addActionListener( new ButtonListener(frame, tpd) );

	//frame.getContentPane().add(saveb);
	//frame.getContentPane().add(cancelb);
	Box hbox = Box.createHorizontalBox();
	hbox.add(saveb);
	hbox.add(cancelb);

	vbox.add(hbox);
	
        //Display the window.
	frame.getContentPane().add(vbox);
        frame.pack();
        frame.setVisible(true);
    }
    
    /** set status of server */
    public void setServerStatus(String url, Boolean flag) {
	// browse through config and set status of server
	List servers =  config.getServers();
	for (int i = 0 ; i < servers.size(); i++) {
	    Map s = (Map)servers.get(i) ;
	    SpiceDasSource ds = (SpiceDasSource) s.get("server");
	    String surl = ds.getUrl();
	    if ( surl.equals(url) ) {
		boolean f = flag.booleanValue();
		config.setStatus(i,f);
	    }	    
	}
    }
}

class ButtonListener
    implements ActionListener
	       
{
    JFrame parent ;
    TabbedPaneDemo configpane ;

    public ButtonListener( JFrame parent_,TabbedPaneDemo tpd) {
	parent = parent_ ;
	configpane = tpd ;
    }

    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	System.out.println("button pressed:" + cmd);
	if ( cmd.equals("Close")) {
	    //System.out.println("closing..");
	    parent.dispose();
	} else  if (cmd.equals("Save")) {
	    
	    configpane.saveConfiguration();

	    
	} 

    
    }

   
}


class TabbedPaneDemo extends JPanel {
    static String[] colNames= new String [] {"url","coordinateSystems","adminemail","capabilities","description"};

    RegistryConfiguration config ;
    RegistryConfigIO registryIO  ;
    JTabbedPane tabbedPane       ;
    List        entryFormFields  ;

    public TabbedPaneDemo(RegistryConfigIO registryparent, RegistryConfiguration config_) {
        super(new GridLayout(1, 1));
	registryIO = registryparent ;
	config = config_;

	tabbedPane = new JTabbedPane();
        ImageIcon icon = createImageIcon("spice.jpg");

	TitledBorder dasborder1;
	dasborder1 = BorderFactory.createTitledBorder("available DAS sources");

	// Make sequence and structure Panel
	JPanel seqstrucpanel = new JPanel();
	seqstrucpanel.setLayout(new BoxLayout(seqstrucpanel, BoxLayout.Y_AXIS));	
	List sequenceservers = config.getServers() ;
	
	
	String seqdata[][] = getTabData();

	//System.out.println(seqdata);
	//JTable table= new JTable(seqdata,colNames);
	MyTableModel mtm = new MyTableModel(this,seqdata,colNames);
	//mtm.getModel().addTableModelListener(this);

	JTable table  = new JTable(mtm);

	

	// Configure some of JTable's paramters
	table.setShowHorizontalLines( false );
	table.setRowSelectionAllowed( true );
	table.setColumnSelectionAllowed( true );
		
	// Add the table to a scrolling pane
	JScrollPane seqscrollPane = table.createScrollPaneForTable( table );

	seqscrollPane.setBorder(dasborder1);

	seqstrucpanel.add( seqscrollPane, BorderLayout.CENTER );

	

	tabbedPane.addTab("list sources", icon, seqstrucpanel,
                          "configure sequence and structure servers");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
	
	


	/// add a local DAS source Panel 
	JPanel addLocalPanel = new JPanel();
	addLocalPanel.setLayout(new BoxLayout(addLocalPanel, BoxLayout.LINE_AXIS));

	TitledBorder dasborder2;
	dasborder2 = BorderFactory.createTitledBorder("add local DAS source");

	JPanel entryForm = new JPanel();
	entryForm.setBorder(dasborder2);
	entryForm.setLayout(new BoxLayout(entryForm, BoxLayout.LINE_AXIS));

	Box vBoxRight =  Box.createVerticalBox();
	Box vBoxLeft =  Box.createVerticalBox();

	entryFormFields = new ArrayList();

	for ( int i = 0 ; i < colNames.length; i++) {
	    String col = colNames[i];
	   
	    JTextField txt1 = new JTextField(col);
	    txt1.setEditable(false);
	    txt1.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	    vBoxLeft.add(txt1);

	    JTextField txt2 = new JTextField("    ");
	    txt2.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
	    vBoxRight.add(txt2);
	    entryFormFields.add(txt2);
	}


	entryForm.add(vBoxLeft);
	entryForm.add(vBoxRight);
	addLocalPanel.add(entryForm);
	


	tabbedPane.addTab("Add local source", icon, addLocalPanel,"add a local DAS source");

	
        //Add the tabbed pane to this panel.
        add(tabbedPane);
        
        //Uncomment the following line to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    private Map convertSource2Map(SpiceDasSource source) {
	HashMap server = new HashMap();
	server.put("name",source.getUrl()); // for backwards compability
	server.put("url",source.getUrl());
	server.put("coordinateSystems",source.getCoordinateSystem());
	server.put("description",source.getDescription());
	server.put("adminemail",source.getAdminemail());
	server.put("capabilities",source.getCapabilities());
	return server ;
    }


    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TabbedPaneDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    public String[][] getTabData() {
	List servers = config.getServers();

	String[][] data = new String[servers.size()][colNames.length+1];

	for ( int i =0; i< servers.size(); i++ ) {
	    SpiceDasSource ds = (SpiceDasSource) servers.get(i);
	    Map server = convertSource2Map(ds);
	    for ( int j =0;j<colNames.length;j++){
		String colname = colNames[j];
		//System.out.println(colname);
		String s = "" ;
		if (( colname.equals("coordinateSystems")) || 
		    (colname.equals("capabilities"))) {
		    String[] stmp = (String[])server.get(colname);
		    s = "" ;
		    for ( int u = 0; u<stmp.length;u++){
			s += stmp[u]+" ";
		    }
		} else {
		    s = (String)server.get(colname);
		}
		data[i][j] = s;
		//data[i][colnames.length] = config.getStatus(i);
	    }
	}
	return data ;
    }

    public void setServerStatus(String url, Boolean status){
	System.out.print("Setting server status " + url + " " + status);
	boolean flag = status.booleanValue();
	config.setStatus(url,flag);
    }


    public void saveConfiguration() {
	System.out.println("saving config");
	int pos = tabbedPane.getSelectedIndex();
	System.out.println("index: " + pos);
	if ( pos == 0 ) {
	
	    registryIO.saveConfiguration();
	} else if ( pos == 1 ) {
	    // add a new local DAS source ...
	    System.out.println("adding new local DAS source");
	    HashMap formdata = new HashMap();
	    for ( int i = 0 ; i < colNames.length; i++) {
		String col = colNames[i];
		JTextField txt = (JTextField)entryFormFields.get(i);
		String data = txt.getText();
		System.out.println(col + " " + data);
		formdata.put(col,data);
	    }
	    SpiceDasSource sds = new SpiceDasSource();
	    sds.setUrl(              (String) formdata.get("url"));
	    sds.setAdminemail(       (String) formdata.get("adminemail"));
	    sds.setDescription(      (String) formdata.get("description"));
	    sds.setCoordinateSystem( (String) formdata.get("coordinateSystem"));
	    sds.setRegistered(false);
	}
    }
}


/** a table model twhere the last colun is a checkbox to deceide if true or false */
class MyTableModel extends AbstractTableModel {

   TabbedPaneDemo parent ;

    private Object[][] data ;
    private String[]   columnNames  ;
				    
    public MyTableModel(TabbedPaneDemo parent_,String[][]seqdata, String[] columnNames_){
	super();
	parent = parent_ ;
	columnNames = columnNames_;

	setData(seqdata);

    }

				      
    /*private String[]   columnNames = {"URL",
      "capabilities",
      "coordinate system",
      "admin email",
      "description",				   
      "active"
      };
    */
   


    private void setData(String[][]seqdata) {
	Object[][] o = new Object[seqdata.length][columnNames.length];
	for ( int i = 0 ; i < seqdata.length; i++){
	    for ( int j =0 ; j < columnNames.length-1; j++){
		o[i][j] = seqdata[i][j];
	    }
	    o[i][columnNames.length-1] = new Boolean(true);
	}
	data = o ;
    }

    public int getColumnCount() {
	return columnNames.length;
    }
    
    public int getRowCount() {
	return data.length;
    }
    
    public String getColumnName(int col) {
	return columnNames[col];
    }
    
    public Object getValueAt(int row, int col) {
	//System.out.println("getValueAt");
	if ((row > data.length) || ( col > columnNames.length))
	    {
		System.out.println("out of range");
		return null ;
	    }
	return data[row][col];
    }
    
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
	//System.out.println("getColumnClass " + c);
	return getValueAt(0, c).getClass();
    }
    

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
	//Note that the data/cell address is constant,
	//no matter where the cell appears onscreen.
	if (col < columnNames.length - 1 ) {
	    return false;
	} else {
	    return true;
	}
    }
    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
	
	System.out.println("Setting value at " + row + "," + col
			   + " to " + value
			   + " (an instance of "
			   + value.getClass() + ")");
	
	
	data[row][col] = value;
	fireTableCellUpdated(row, col);

	if ( col == ( columnNames.length - 1 )) {
	   	
	    //String url = (String)model.getValueAt(row,0);
	    // Do something with the data...
	    //Boolean status = (Boolean) model.getValueAt(row, column);
	    String url = (String)getValueAt(row,0);
	    parent.setServerStatus(url,(Boolean)value) ;
	}
    }

    public void tableChanged(TableModelEvent e) {
	//System.out.println("tableChanged");
        int row = e.getFirstRow();
        int column = e.getColumn();
        MyTableModel model = (MyTableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object cell = model.getValueAt(row, column);

	if ( column == ( columnNames.length - 1 )) {
	   	
	    String url = (String)model.getValueAt(row,0);
	    // Do something with the data...
	    Boolean status = (Boolean) model.getValueAt(row, column);
	    parent.setServerStatus(url,status) ;
	}
    }
    


}


