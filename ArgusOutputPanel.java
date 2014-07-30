package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import TNCterminal.HeliumConfig;
import TNCterminal.HeliumTelemetry;
import TNCterminal.TNCoutputDisplay;
import mas.swing.NumberTextField;
import mas.utils.Utils;

public class ArgusOutputPanel extends TNCoutputDisplay { // implements ActionListener { // implements ItemListener {

	private static final long serialVersionUID = 1L;
	private static final int MAX_NUMFIELDS = 100;
	private static String[] unit = new String[MAX_NUMFIELDS];
	private static String[] names = new String[MAX_NUMFIELDS];
	private NumberTextField[] value = new NumberTextField[MAX_NUMFIELDS];
	private double[][] thresholds = new double[MAX_NUMFIELDS][4];
	public static final int THRESHOLD_RED_HIGH = 3;
	public static final int THRESHOLD_YELLOW_HIGH = 2;
	public static final int THRESHOLD_YELLOW_LOW = 1;
	public static final int THRESHOLD_RED_LOW = 0;
	public static final double[] DEFAULT_THRESHOLD = {-1, 0, 1000, 10000};
	public static final DecimalFormat FORMAT_MILLIAMPS = new DecimalFormat("#0");
	public static final DecimalFormat FORMAT_CELSIUS = new DecimalFormat("#0.0");
	public static final DecimalFormat FORMAT_VOLTS = new DecimalFormat("#0.00");
	public static final DecimalFormat FORMAT_DEFAULT = new DecimalFormat("#0");
	public static final String UNIT_CELSIUS = "C";
	public static final String UNIT_MILLIAMPS = "mA";
	public static final String UNIT_VOLTS = "V";
	
	public static final int MAX_DATAPOINTS = 15;
	private double[][] storedData = new double[MAX_NUMFIELDS][MAX_DATAPOINTS];
//	private static int find;
//	private Point p;
	private JButton fakeButton = new JButton();	// Only exists to trigger actions
	private HeliumTelemetry heliumTelem;
	private HeliumConfig heliumConfig;

	public static final String[] BEACON_TEST_STRING = 
		{"247 316 39D 39F 314 39C 39B 317 233 314 39B 32C 318 1FF 325 317 0 FFFF " + 
				"004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5 " +
				"00FF8EFFFF0C0000765C000000FF901600 " +
				"000000190 3F:7F:7F.00 3F/1F/20FF 031",
//					"_UNKNOWN77 000000260 3F:7F:7F.00 3F/1F/20FF 031",
		"240 310 390 390 310 390 390 310 238 310 390 30C 308 1FF 310 307 1 FFFF " + 
		"004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5 " +
		"00FF8EFFFF0C0000765C000000FF901600 " +
		"000000200 3F:7F:7F.00 3F/1F/20FF 031",
//					"_UNKNOWN77 000000270 3F:7F:7F.00 3F/1F/20FF 031",
		"237 306 38D 38F 304 38C 37B 347 263 214 36B 3AC 3A8 1FF 300 3F7 0 FFFF " + 
		"004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5 " +
		"00FF8EFFFF0C0000765C000000FF901600 " +
		"000000210 3F:7F:7F.00 3F/1F/20FF 031",
//					"_UNKNOWN77 000000280 3F:7F:7F.00 3F/1F/20FF 031",
		"23E 314 3A1 3A4 312 3A1 3A0 315 230 311 3A3 324 315 1FC 31E 315 0 FFFF " + 
				"004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5 " +
				"00FF8EFFFF0C0000765C000000FF901600 " +
				"000000220 3F:7F:7F.00 3F/1F/20FF 031"
		};
	public static final int VANDY_SPOT = 16;
	public static final int HLMCONFIG_SPOT = 18;
	public static final int HLMTLM_SPOT = 19;
	public static final int CLOCK_SPOT = 20;
//	public static final int RSSI_SPOT = 18;
	public static final int VERSION_SPOT = 23;
	protected StringBuffer stringBuffer;
	public static final String[] outputFile = {
		"argusTerminal/ArgusOutputs.argus",
		"ArgusOutputs.argus"
	};

	public static void main(String[] args) {
		JFrame jf = new JFrame("Argus Output");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ArgusOutputPanel ao = new ArgusOutputPanel(null);
		jf.getContentPane().add(ao.getPoppedContents());
		jf.setVisible(true);
		jf.pack();
		// Start a Timer to parse the fake beacon every second
		ao.runTestTimer();
	}
	
	public void runTestTimer() {
		Timer timer = new Timer(1000, new ActionListener() {
			int i=0; 
			
			public void actionPerformed(ActionEvent e) {
				receive(BEACON_TEST_STRING[i]);
				i++;
				if (i == BEACON_TEST_STRING.length) { i = 0; }
			}
		});
		timer.setRepeats(true);
		timer.setCoalesce(true);
		timer.start();
	}

	public ArgusOutputPanel(ArgusTerminal at){
		super();
		initDisplay();
	}

	private static BufferedReader tryToOpenOutputFile() {
		BufferedReader br;
		for (int i=0; i < outputFile.length; i++) {
			try {
				br = new BufferedReader(new FileReader(outputFile[i]));
				return(br);
			} catch (FileNotFoundException fnfe) {
				// Do nothing. Try the next one
			}
		}
		// The file is not where it should be
		File f = new File("");
		System.err.println("Could not find any config file: " + 
				Utils.arrayToStringBuffer(outputFile));
		System.err.println("Did you move the config file to this directory " + f.getAbsolutePath());
		return(null);
	}

	private void initDisplay(){
		stringBuffer = new StringBuffer();
		JPanel displayPanel = new JPanel(new GridLayout(0, 4));
		Color bgColor = Color.darkGray;
		displayPanel.setBackground(bgColor);
		add(displayPanel, BorderLayout.CENTER);
		// The Helium stuff goes off to the right
		JPanel heliumPanel = new JPanel(new BorderLayout());
		heliumConfig = new HeliumConfig();
		heliumTelem = new HeliumTelemetry();
		heliumPanel.add(heliumConfig, BorderLayout.CENTER);
		heliumPanel.add(heliumTelem, BorderLayout.NORTH);
		add(heliumPanel, BorderLayout.EAST);
		try {
			BufferedReader br = tryToOpenOutputFile();

			String line;
			for(int i=0; (line = br.readLine())!= null; i++) {
				// Split by tabs
				String[] paramSplit = line.split("\t");
//				JPanel panel1 = new JPanel();
				names[i] = new String(paramSplit[0]);
				JLabel name = new JLabel(names[i]);
				name.setHorizontalAlignment(JLabel.RIGHT);
//				panel1.add(name);
				displayPanel.add(name);
				name.setForeground(Color.white);

//				JPanel panel = new JPanel();
				value[i] = new NumberTextField();
				value[i].setForeground(Color.white);
				value[i].setFont(new Font(Utils.typewriterFont(), Font.BOLD, 13));
				value[i].setBackground(bgColor);
//				panel.add(value[i]);
				displayPanel.add(value[i]);

//				JPanel panel2 = new JPanel();
				unit[i] = paramSplit[1];
				JLabel label = new JLabel(unit[i]);
				label.setHorizontalAlignment(JLabel.LEFT);
//				panel2.add(label);
				displayPanel.add(label);
				label.setForeground(Color.white);
				
				setNumberFormat(value[i], paramSplit[1]);

//				JPanel jp = new JPanel();
				JButton graph = new JButton("Graph");
				graph.addActionListener(this);
				graph.setActionCommand("Graph" + i);
//				jp.add(graph);
				displayPanel.add(graph);

//				panel1.setBackground(Color.DARK_GRAY);
//				panel2.setBackground(Color.DARK_GRAY);
//				Border loweredetched;
//				loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
				value[i].setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

				//Color Thresholds
				for (int j=0; j < 4; j++) {
					if (paramSplit.length > j+2) {
						thresholds[i][j]=Double.valueOf(paramSplit[j+2]);
					} else {
						thresholds[i][j]=DEFAULT_THRESHOLD[j];
					}
				}
			}
			br.close();
			value[0].setForeground(Color.white);
		} catch (FileNotFoundException fnfe){
			System.err.println("ArgusOutputPanel cannot find the file: " + fnfe.getMessage());
		} catch (IOException ioe) {
			System.err.println("IOException in ArgusOutputPanel: " + ioe.getMessage());
		}

		for(int i=0;i<3;i++){
			JPanel nullPanel = new JPanel();
			displayPanel.add(nullPanel);
			nullPanel.setBackground(Color.darkGray);
		}
	}

	public static void setNumberFormat(NumberTextField ntf, String unit) {
		DecimalFormat df = FORMAT_DEFAULT;
		if (unit.equals(UNIT_MILLIAMPS)) {
			df = FORMAT_MILLIAMPS;
		} else if (unit.equals(UNIT_CELSIUS)) {
			df = FORMAT_CELSIUS;
		} else if (unit.equals(UNIT_VOLTS)) {
			df = FORMAT_VOLTS;
		}
		ntf.setFormat(df);
	}
	
	public static double hexToUnits(String hexData, String unit) {
		// Grab each hex element and convert to integer
		double val = -1;
		try {
			val = Integer.parseInt(hexData, 16);
		} catch (NumberFormatException nfe) {
			// Do nothing. Drop the bit and move on
		}
		double data = val;
		// Convert
		if (unit.equals(UNIT_MILLIAMPS)){
			data = (3.3*val/1024 - 2.522)*50000/9;
		}else if (unit.equals(UNIT_VOLTS)) {
			data = 3.3*val/1024*15.11/5.11;
		}else if (unit.equals(UNIT_CELSIUS)) {
			data = 330*val/1024 - 273.14;
		}
		return(data);
	}

	public void receive(String parseBeacon) {
		// Clean up
		parseBeacon.trim();
		// Split on the whitespace
		String[] beaconBits = parseBeacon.split(" ");
		int rowCount = 0;
		
		// Try to parse; fail gracefully
		try {
		// Following Justin's original setup
		// Except mission clock is first
		value[rowCount++].setText(beaconBits[CLOCK_SPOT]);
		// The next 16 elements are 2-bit hex
		for(int i=1; i<17; i++){
			// Grab each hex element and convert to integer
			double convertedData = hexToUnits(beaconBits[i-1], unit[i]);
			value[i].set(convertedData);
			value[i].setForeground(sendTLCcolor(convertedData, i));
			// Move the stored values over one column to make room for this data
			for(int j=0; j < MAX_DATAPOINTS-1; j++) {
				storedData[i][j]=storedData[i][j+1];
			}
			storedData[i][MAX_DATAPOINTS-1] = convertedData;
		}
		rowCount = 17;
		// Next is the Vandy state
		if (beaconBits[VANDY_SPOT].equals("1")) {
			value[rowCount].setText("ON");
			value[rowCount].setForeground(Color.green);
		} else {
			value[rowCount].setText("off");
			value[rowCount].setForeground(Color.lightGray);			
		}
		rowCount++;
		// Vandy data
		value[rowCount++].setText(beaconBits[VANDY_SPOT+1]);
		// Software version and state are split
		value[rowCount++].setText(beaconBits[VERSION_SPOT].substring(0, 2));
		value[rowCount++].setText(beaconBits[VERSION_SPOT].substring(2));
		
		// Let the helium panels do their thing
		heliumTelem.parseString(beaconBits[HLMTLM_SPOT]);
		heliumConfig.parseString(beaconBits[HLMCONFIG_SPOT]);
		} catch (StringIndexOutOfBoundsException sioobe) {
			System.err.println("ArgusOutputPanel got stuck at " + rowCount + " from " + parseBeacon);
		}
		// Update all the active graphs
		fakeButton.doClick();
//		DrawGraph.redraw();
	}

	static int search(String line, String sym){
		for(int i=0; i<line.length(); i++){
			if (line.substring(i, i+1).equals(sym)){
				return i;
			}
		}
		return 0;
	}

	static boolean check(String line, String sym){
		for(int i=0; i<line.length(); i++){
			if (line.substring(i, i+1).equals(sym)){
				return true;
			}
		}
		return false;
	}

	public double sendArray(int i, int sensorID){
		return storedData[sensorID][i];
	}

	public static String sendUnit(int sensorID){
		if(!(unit[sensorID].equals(null))){
			return unit[sensorID];
		}
		return "";
	}

	/** Run the threshold limit check and return the appropriate color. This
	 * function assumes that the threshold limits are defined properly. 
	 * 
	 * @param testValue
	 * @return
	 */
	public Color sendTLCcolor(double testValue, int sensorNum){
		// Start low and work your way up
		if (testValue <  thresholds[sensorNum][THRESHOLD_RED_LOW]) return Color.red;
		if (testValue <= thresholds[sensorNum][THRESHOLD_YELLOW_LOW]) return Color.yellow;
		if (testValue <= thresholds[sensorNum][THRESHOLD_YELLOW_HIGH]) return Color.green;
		if (testValue <= thresholds[sensorNum][THRESHOLD_RED_HIGH]) return Color.yellow;
		return Color.red;
	}

	public void actionPerformed(ActionEvent actionE) {
		super.actionPerformed(actionE);
		String actionC = actionE.getActionCommand();
		if (actionC.startsWith("Graph")) {
			int sID = Integer.parseInt(actionC.substring(5));
			Point p = ((Component) actionE.getSource()).getLocation();
			p.x = p.x + 300;
			DrawGraph dg = new DrawGraph(this, sID, p, MAX_DATAPOINTS, 
					names[sID] + " (" + unit[sID] + ")");
			fakeButton.addActionListener(dg);
//			p = DrawGraph.close();
//			DrawGraph.newGraph(p, find);			
		}
	}
}