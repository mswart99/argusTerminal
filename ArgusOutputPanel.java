package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
	public static final String[] BINARYTEXT_ONOFF = {"off", "ON"};
	public static final String[] BINARYTEXT_BINSEM = {"low", "HIGH"};
	public static final int NUMBINSEMS = 7;

	public static final int MAX_DATAPOINTS = 15;
	private double[][] storedData = new double[MAX_NUMFIELDS][MAX_DATAPOINTS];
	//	private static int find;
	//	private Point p;
	private JButton fakeButton = new JButton();	// Only exists to trigger actions
	private HeliumTelemetry heliumTelem;
	private HeliumConfig heliumConfig;
	
	public static final String[] BEACON_TEST_STRING = 
		{"02080502 000000144 004B010100006836020000AC0600415247555331534C55474E440500000000000000 " 
				+ "004BFF87 6C008EFF1200007578000000F017",
		 "02080502 000001450 004B010100006836020000AC0600415247555331534C55474E440500000000000000 "
				+ "004BFF87 FFC7118EFFB90000776E030000047F",
		 "02080502 000002308 004B010100006836020000AC0600415247555331534C55474E440500000000000000 " 
				+ "004BFF87 17138EFF090100746E03000038E6",
		 "02080502 000000430 004B010100006836020000AC0600415247555331534C55474E440500000000000000 " 
				+ "004BFF87 7B028EFF51000072220500001481",
		 "02080502 000000716 004B010100006836020000AC0600415247555331534C55474E440500000000000000 " 
				+ "004BFF87 23038EFF6C0000762205000020A4"
		};

	public static final String[] BEACON_TEST_STRING2 = 
		{"02070100 000000190 247 316 39D 39F 314 39C 39B 317 233 314 39B 32C 318 1FF 325 317 " + 
				"000050001004A000045A0 3F:7F:7F.00 3F/1F/20FF",
				//					"_UNKNOWN77 000000260 3F:7F:7F.00 3F/1F/20FF 031",
		"02070300 000000200 240 310 390 390 310 390 390 310 238 310 390 30C 308 1FF 310 307 " + 
			"100070005040A000045AA 3F:7F:7F.00 3F/1F/20FF ",
		"02070501 000000205 32C 004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5" +
				"AABBCCDDEEFF0011223344556677889900",
				//					"_UNKNOWN77 000000270 3F:7F:7F.00 3F/1F/20FF 031",
		"02070500 000000210 237 306 38D 38F 304 38C 37B 347 263 214 36B 3AC 3A8 1FF 300 3F7 " + 
				"000060002004A000045B4 3F:7F:7F.00 3F/1F/20FF ",
		"02070102 000000215 32C 004B010100006836020000FFAC0600415247555331534C55474E440500000040400101FFE5 " +
				"008EFF0C0000765C000000901600 ",
				//					"_UNKNOWN77 000000280 3F:7F:7F.00 3F/1F/20FF 031",
		"02070500 000000220 23E 314 3A1 3A4 312 3A1 3A0 315 230 311 3A3 324 315 1FC 31E 315 " + 
				"100060002004A000045BE 3F:7F:7F.00 3F/1F/20FF"
		};
	public static final int NUM_ADC_CHANNELS = 16;
	public static final int SC_HEADER_SPOT = 0;
	public static final int VANDY_SPOT = NUM_ADC_CHANNELS + 2;
	public static final int BATV_SPOT_IN_ADC = 11;	// Location of the battery data in the ADC array
	public static final int RTC_SPOT = VANDY_SPOT + 1;
	public static final int HLMCONFIG_SPOT = 2;
	public static final int HLMTLM_SPOT = HLMCONFIG_SPOT + 2;
	public static final int CLOCK_SPOT = 1;
	public static final int ADC_START_SPOT = 2;
	//	public static final int RSSI_SPOT = 18;
	// public static final int VERSION_SPOT = 23;
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

	/** Convenience method for setting the format for telemetry fields.
	 * 
	 * @param ntf
	 * @param unit
	 */
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

	/** Receive the given beacon string and parse it into telemetry elements.
	 * 
	 */
	public void receive(String parseBeacon) {
		// Clean up
		parseBeacon.trim();
		// Split on the whitespace
		String[] beaconBits = parseBeacon.split(" ");
		int rowCount = 0;

		// Try to parse; fail gracefully
		try {
			// Mission clock
			value[rowCount++].setText(beaconBits[CLOCK_SPOT]);
			/* The spacecraft header is first, and is two-byte hex:
			 *  [SC ID][VERSION][STATUS][FRAME ID]
			 */
			value[rowCount++].setText(beaconBits[SC_HEADER_SPOT].substring(0, 2));
			value[rowCount++].setText(beaconBits[SC_HEADER_SPOT].substring(2, 4));
			// Status is spread among several elements
			int binsemStates = Integer.parseInt(beaconBits[SC_HEADER_SPOT].substring(4, 6), 16);
			String binsemStateString = Integer.toBinaryString(binsemStates);
			while (binsemStateString.length() < NUMBINSEMS) {
				binsemStateString = "0" + binsemStateString;
			}
			// Place them in the next sets
			for (int i=0; i < NUMBINSEMS; i++) {
				formatBinaryPanel(value[rowCount++], binsemStateString.substring(i, i+1), BINARYTEXT_BINSEM);				
			}
			// And the frame
			value[rowCount++].setText(beaconBits[SC_HEADER_SPOT].substring(6, 8));
			int frameID = Integer.parseInt(beaconBits[SC_HEADER_SPOT].substring(6,8), 16);
			
			// The rest of what we do depends on the frame
			switch (frameID) {
			case 0: 
				// The next NUM_ADC_CHANNELS elements are 2-bit hex
				for(int i=0; i<NUM_ADC_CHANNELS; i++){
					setADCdisplay(beaconBits[i+ADC_START_SPOT], i+rowCount);
//					// Grab each hex element and convert to integer
//					double convertedData = hexToUnits(beaconBits[i+ADC_START_SPOT], unit[i+rowCount]);
//					value[i+rowCount].set(convertedData);
//					value[i+rowCount].setForeground(sendTLCcolor(convertedData, i+rowCount));
//					// Move the stored values over one column to make room for this data
//					for(int j=0; j < MAX_DATAPOINTS-1; j++) {
//						storedData[i+rowCount][j]=storedData[i+rowCount][j+1];
//					}
//					storedData[i+rowCount][MAX_DATAPOINTS-1] = convertedData;
				}
				rowCount = rowCount + NUM_ADC_CHANNELS;
				/* Next is the Vandy status information, with number of bytes
				 */
				formatBinaryPanel(value[rowCount++], beaconBits[VANDY_SPOT].substring(0, 1), 
						BINARYTEXT_ONOFF);
				value[rowCount++].setText(beaconBits[VANDY_SPOT].substring(1, 5));
				value[rowCount++].setText(beaconBits[VANDY_SPOT].substring(5, 9));
				// Convert the reset count to an integer
				value[rowCount++].set(Integer.parseInt(beaconBits[VANDY_SPOT].substring(9, 13), 16));
				// Ditto for the clock
				value[rowCount++].set(Integer.parseInt(beaconBits[VANDY_SPOT].substring(13, 21), 16));
				// And the real-time clock rounds out frame 0. It's two elements
				value[rowCount++].setText(beaconBits[RTC_SPOT] + " " + beaconBits[RTC_SPOT+1]);
				break;	// Nothing else is in frame 0
			case 1:		// VUC data
				// The battery data is here
				setADCdisplay(beaconBits[2], 4+NUMBINSEMS+BATV_SPOT_IN_ADC);
				// VUC is just telemetry, which we don't know what to do with, yet.
				break;
			case 2: 	// This is helium data
//				setADCdisplay(beaconBits[2], 4+NUMBINSEMS+BATV_SPOT_IN_ADC);
				String fix = beaconBits[HLMCONFIG_SPOT]; //.substring(0, 22) +
				System.out.println(fix);
				//		beaconBits[HLMCONFIG_SPOT].substring(24);
				heliumConfig.parseString(fix);
				if (beaconBits[HLMTLM_SPOT].substring(0, 2).equals("FF")) {
					heliumTelem.parseString(beaconBits[HLMTLM_SPOT].substring(2));
					break;
				}
//				fix = "";
//				for (int i=0; i < beaconBits[HLMTLM_SPOT].length()/2; i++) {
//					String bit = beaconBits[HLMTLM_SPOT].substring(2*i, 2*i+2);
//					if (!bit.equals("FF")) {
//						fix = fix + bit;					
//					}
//				}
				heliumTelem.parseString(beaconBits[HLMTLM_SPOT]);
				break;
			}

		} catch (StringIndexOutOfBoundsException sioobe) {
			System.err.println("ArgusOutputPanel got a StringIndex Error at row " + rowCount + " from " + parseBeacon);
		} catch (NumberFormatException nfe) {
			System.err.println("ArgusOutputPanel got a NumberFormatException at row " + rowCount + " from " + parseBeacon);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			System.err.println("ArgusOutputPanel got an ArrayIndexOutOfBoundsException at row " + rowCount + " from " + parseBeacon);
		}
		// Update all the active graphs
		fakeButton.doClick();
		//		DrawGraph.redraw();
	}

	/** Convenience method for setting the formats for binary telemetry values.
	 * 
	 * @param ntf
	 * @param newValue
	 * @param textOptions
	 */
	public static void formatBinaryPanel(NumberTextField ntf, String newValue, String[] textOptions ) {
		int val = 0;
		if (newValue.equals("1")) {
			val = 1;
			ntf.setForeground(Color.yellow);
			ntf.setBackground(Color.green.darker());
		} else {
			ntf.setForeground(Color.lightGray);
			ntf.setBackground(Color.black);
		}
		ntf.setText(textOptions[val]);
	}

	//	private static int search(String line, String sym){
	//		for(int i=0; i<line.length(); i++){
	//			if (line.substring(i, i+1).equals(sym)){
	//				return i;
	//			}
	//		}
	//		return 0;
	//	}

	//	static boolean check(String line, String sym){
	//		for(int i=0; i<line.length(); i++){
	//			if (line.substring(i, i+1).equals(sym)){
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

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
	
	public void setADCdisplay(String adcText, int rowNum) {
		double convertedData = hexToUnits(adcText, unit[rowNum]);
		value[rowNum].set(convertedData);
		value[rowNum].setForeground(sendTLCcolor(convertedData, rowNum));
		// Move the stored values over one column to make room for this data
		for(int j=0; j < MAX_DATAPOINTS-1; j++) {
			storedData[rowNum][j]=storedData[rowNum][j+1];
		}
		storedData[rowNum][MAX_DATAPOINTS-1] = convertedData;
	}
}