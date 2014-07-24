package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import TNCterminal.TNCoutputDisplay;
import basicTerminal.BareBonesUserControlPanel;
import mas.utils.Utils;

public class ArgusOutputPanel extends TNCoutputDisplay { // implements ActionListener { // implements ItemListener {

	private static final long serialVersionUID = 1L;
	private static String[] unit = new String[100];
	private JLabel[] value = new JLabel[100];
	private static double[][] thresholds = new double[100][4];
	public static final int THRESHOLD_RED_HIGH = 3;
	public static final int THRESHOLD_YELLOW_HIGH = 2;
	public static final int THRESHOLD_YELLOW_LOW = 1;
	public static final int THRESHOLD_RED_LOW = 0;
	private static double[][] array = new double[100][15];
	private static int find;
//	private boolean auto;
	private Point p;

//	private String beaconText;	// A complete beacon text string
	public static final String[] BEACON_TEST_STRING = 
		{"247 316 39D 39F 314 39C 39B 317 233 314 39B 32C 318 1FF 325 317 0 " + 
					"FFFF _UNKNOWN77 000000260 3F:7F:7F.00 3F/1F/20FF 031",
		"240 310 390 390 310 390 390 310 238 310 390 30C 308 1FF 310 307 0 " + 
					"FFFF _UNKNOWN77 000000270 3F:7F:7F.00 3F/1F/20FF 031",
		"237 306 38D 38F 304 38C 37B 347 263 214 36B 3AC 3A8 1FF 300 3F7 0 " + 
					"FFFF _UNKNOWN77 000000280 3F:7F:7F.00 3F/1F/20FF 031",
		};
	public static final int VANDY_SPOT = 16;
	public static final int CLOCK_SPOT = 19;
	public static final int RSSI_SPOT = 18;
	public static final int VERSION_SPOT = 22;
//	private Random r = new Random();

	//	private int hour;
	//    private int min;
	//    private int second;
	//    JLabel clockMain;

//	protected ArgusTerminal argusTerminal;
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
//		argusTerminal = at;
		initDisplay();
	}

	private BufferedReader tryToOpenOutputFile() {
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
		//		breakButton.setEnabled(false);  // We don't use it
		stringBuffer = new StringBuffer();
		JPanel displayPanel = new JPanel(new GridLayout(0, 4));
		add(displayPanel, BorderLayout.CENTER);
		
		try {
			BufferedReader br = tryToOpenOutputFile();

			String line;
			for(int i=0; (line = br.readLine())!= null; i++) {
				// Split by tabs
				String[] paramSplit = line.split("\t");
				JPanel panel1 = new JPanel();
				JLabel name = new JLabel(paramSplit[0]);
				panel1.add(name);
				displayPanel.add(panel1);
				name.setForeground(Color.white);

				JPanel panel = new JPanel();
				value[i] = new JLabel();
				value[i].setForeground(Color.white);
				value[i].setFont(new Font(Utils.typewriterFont(), Font.BOLD, 13));
				panel.add(value[i]);
				displayPanel.add(panel);

				JPanel panel2 = new JPanel();
				unit[i] = paramSplit[1];
				JLabel label = new JLabel(unit[i]);
				panel2.add(label);
				displayPanel.add(panel2);
				label.setForeground(Color.white);

				JButton graph = new JButton("Graph");
				graph.addActionListener(this);
				graph.setActionCommand("Graph" + i);
				displayPanel.add(graph);

				panel.setBackground(Color.DARK_GRAY);
				panel1.setBackground(Color.DARK_GRAY);
				panel2.setBackground(Color.DARK_GRAY);
				Border loweredetched;
				loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
				panel.setBorder(loweredetched);

				//Color Thresholds
				for (int j=0; j < 4; j++) {
					if (paramSplit.length > j+2) {
						thresholds[i][j]=Double.valueOf(paramSplit[j+2]);
					} else {
						thresholds[i][j]=0;
					}
				}

//				if(!(unit[i].equals(null))){
//					if(unit[i].equals("C")){
//						thresholds[i][THRESHOLD_RED_LOW]=10;
//						thresholds[i][THRESHOLD_YELLOW_LOW]=15;
//						thresholds[i][THRESHOLD_YELLOW_HIGH]=30;
//						thresholds[i][THRESHOLD_RED_HIGH]=35;
//					}else if(unit[i].equals("V")){
//						thresholds[i][THRESHOLD_RED_LOW]=7.5;
//						thresholds[i][THRESHOLD_YELLOW_LOW]=7.75;
//						thresholds[i][THRESHOLD_YELLOW_HIGH]=9.25;
//						thresholds[i][THRESHOLD_RED_HIGH]=9.5;
//					}else if(unit[i].equals("mA")){
//						thresholds[i][THRESHOLD_RED_LOW]=75;
//						thresholds[i][THRESHOLD_YELLOW_LOW]=100;
//						thresholds[i][THRESHOLD_YELLOW_HIGH]=325;
//						thresholds[i][THRESHOLD_RED_HIGH]=350;
//					}else{
//						thresholds[i][THRESHOLD_RED_LOW]=0;
//						thresholds[i][THRESHOLD_YELLOW_LOW]=0;
//						thresholds[i][THRESHOLD_YELLOW_HIGH]=0;
//						thresholds[i][THRESHOLD_RED_HIGH]=0;
//					}
//				}
			}

			br.close();
			value[0].setForeground(Color.white);
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		decodeBeacon(beaconText);
		for(int i=0;i<3;i++){
			JPanel nullPanel = new JPanel();
			displayPanel.add(nullPanel);
			nullPanel.setBackground(Color.darkGray);
		}
//		JCheckBox refresh = new JCheckBox("Refresh");
//		refresh.addItemListener(this);
//		add(refresh);
//		refresh.setSelected(true);
//		clock();
	}

	//	public void clock(){
	//		JPanel clockM = new JPanel();
	//		JPanel clockN = new JPanel();
	//		JPanel clockU = new JPanel();
	//		clockMain = new JLabel();
	//        JLabel clockName = new JLabel("Clock");
	//        JLabel clockUnit = new JLabel("ms");
	//        clockN.add(clockName);
	//        clockM.add(clockMain);
	//        clockU.add(clockUnit);
	//        add(clockN); add(clockM); add(clockU);
	//        
	//        clockMain.setForeground(Color.cyan);
	//		clockM.setBackground(Color.DARK_GRAY);
	//		clockName.setForeground(Color.white);
	//		clockN.setBackground(Color.DARK_GRAY);
	//		clockUnit.setForeground(Color.white);
	//		clockU.setBackground(Color.DARK_GRAY);
	//		Border loweredetched;
	//		loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	//		clockM.setBorder(loweredetched);
	//		
	//		JButton update = new JButton("Update");
	//		update.addActionListener(this);
	//		add(update);
	//		update.setSelected(false);

	//        Timer timer = new Timer(1000, new ActionListener() {
	////        @Override
	//        public void actionPerformed(ActionEvent e) {
	////            Calendar cal = Calendar.getInstance();
	////            hour=cal.get(Calendar.HOUR_OF_DAY);
	////            min=cal.get(Calendar.MINUTE);
	////            second=cal.get(Calendar.SECOND);
	////            clockMain.setText(hour+":"+min+":"+second);
	//            
	//            //Refresh Rate
	//            
	//            if (second%5==0 && auto){
	//            	decodeBeacon(beaconText);
	//            }
	//            }
	//        });
	//        timer.setRepeats(true);
	//        timer.setCoalesce(true);
	//        timer.start();
	//	}

	public void receive(String parseBeacon) {
		// Clean up
		parseBeacon.trim();
		// Split on the whitespace
		String[] beaconBits = parseBeacon.split(" ");
		int rowCount = 0;
		
		// Following Justin's original setup
		// Except mission clock is first
		value[rowCount++].setText(beaconBits[CLOCK_SPOT]);
		// The next 16 elements are 2-bit hex
		for(int i=1; i<17; i++){
			// Grab each hex element and convert to integer
//			String parse = parseBeacon.substring((i-1)*4,(((i-1)*4)+3));
			double val = -1;
			try {
				val = Integer.parseInt(beaconBits[i-1], 16);
			} catch (NumberFormatException nfe) {
				// Do nothing. Drop the bit and move on
			}
//			double val = 0;
//			if(check(parse,"A") ||check(parse,"B") ||check(parse,"C") ||check(parse,"D") ||
//					check(parse,"E") ||check(parse,"F")){   
//				val = Integer.parseInt(parse,16);
//			}else{
//				val = Integer.parseInt(parse,16);
//			}
//			System.out.println(parse + ": " + val1 + " vs " + val);

			//Conversions

			if(unit[i].equals("mA")){
				String data = (double)((3.3*val/1024) - 2.522)*50000/9+"";
				value[i].setText(data.substring(0,search(data,".")));
			}else if(unit[i].equals("V")){
				String data =(double)(3.3*(val)/1024*15.11/5.11)+"";
				value[i].setText(data.substring(0,(search(data,".")+3)));
			}else if(unit[i].equals("C")){
				String data =(double)(330*(val)/1024 - 273.14)+"";
				value[i].setText(data.substring(0,(search(data,".")+2)));
			} else {
				value[i].setText((int) val + "");
			}

			if(!(value[i].getText().equals("")))
				val=Double.parseDouble(value[i].getText());

			if (val<thresholds[i][THRESHOLD_RED_LOW]) {
				value[i].setForeground(Color.red);
			} else if (val<thresholds[i][THRESHOLD_YELLOW_LOW]) {
				value[i].setForeground(Color.yellow);
			} else if (val<=thresholds[i][THRESHOLD_YELLOW_HIGH]) { 
				value[i].setForeground(Color.green);
			} else if (val > thresholds[i][THRESHOLD_RED_HIGH]) {
				value[i].setForeground(Color.red);				
			} else {
				value[i].setForeground(Color.yellow);
			}
			if (thresholds[i][THRESHOLD_RED_HIGH]<val)
			for(int j=14;j>0;j--){
				array[i][j]=array[i][j-1];
			}
			array[i][0] = val;
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
		// RSSI
		value[rowCount++].setText(beaconBits[RSSI_SPOT].substring(8));
		// Software version and state are split
		value[rowCount++].setText(beaconBits[VERSION_SPOT].substring(0, 2));
		value[rowCount++].setText(beaconBits[VERSION_SPOT].substring(2));
		
		//Temp

//		String chara = "";
//		String chars = "ABCDEF0123456789";
//		for(int i=1;i<17;i++){
//			if(unit[i].equals("C")){
//				int code = r.nextInt(5);
//				chara = chara+"39"+chars.substring(code,code+1);
//			}else if(unit[i].equals("mA")){
//				int code = r.nextInt(10);
//				chara = chara+"31"+chars.substring(code,code+1);
//			}else if(unit[i].equals("V")){
//				int code = r.nextInt(5);
//				chara = chara+"32"+chars.substring(code,code+1);
//			}else{
//				chara = chara+"123";
//			}
//			chara = chara+" ";
//		}
////		beaconText = chara;

		//End Temp

		DrawGraph.redraw();
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

	public static double sendArray(int i){
		return array[find][i];
	}

	public static String sendUnit(){
		if(!(unit[find].equals(null))){
			return unit[find];
		}
		return "";
	}

	public static Color sendColor(double j){
		if(j<thresholds[find][0]) return Color.red;
		if(thresholds[find][0]<=j&&j<thresholds[find][1]) return Color.yellow;
		if(thresholds[find][1]<=j&&j<=thresholds[find][2]) return Color.green;
		if(thresholds[find][2]<j&&j<=thresholds[find][3]) return Color.yellow;
		if(thresholds[find][3]<j) return Color.red;
		return Color.white;
	}

	public void actionPerformed(ActionEvent actionE) {
		super.actionPerformed(actionE);
		String actionC = actionE.getActionCommand();
		if (actionC.startsWith("Graph")) {
			find = Integer.parseInt(actionC.substring(5));
			p = DrawGraph.close();
			DrawGraph.newGraph(p);			
		}
//
//		if(name.equals("Update")){
//			decodeBeacon(BEACON_TEST_STRING[0]);
//		}else{
//			p = DrawGraph.close();
//			find = Integer.parseInt(name);
//			DrawGraph.newGraph(p);
//		}
	}

//	//	@Override
//	public void itemStateChanged(ItemEvent arg0) {
//		if (arg0.getStateChange() == ItemEvent.SELECTED){
//			auto = true;
//		}else{
//			auto = false;
//		}
//	}
}
