package argusTerminal;

import java.awt.Color;
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

import basicTerminal.BasicUserControlPanel;
import mas.utils.Utils;

public class ArgusOutput extends BasicUserControlPanel implements ActionListener, ItemListener {

	private static final long serialVersionUID = 1L;
	static String[] unit = new String[100];
	JLabel[] value = new JLabel[100];
	static double[][] range = new double[100][4];
	static double[][] array = new double[100][15];
	static int find;
	boolean auto;
	Point p;
	
	String beaconText = "247 316 39D 39F 314 39C 39B 317 233 314 39B 32C 318 1FF 320 317 0 FFFF _UNKNOWN77 000000260 3F:7F:7F.00 3F/1F/20FF";
	Random r = new Random();
	
	private int hour;
    private int min;
    private int second;
    JLabel clockMain;
    
    protected ArgusTerminal argusTerminal;
	protected StringBuffer stringBuffer;
    public static final String[] outputFile = {
		"argusTerminal/ArgusOutputs.argus",
		"ArgusOutputs.argus"
	};

	public static void main(String[] args) {
		JFrame jf = new JFrame("Argus Output");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ArgusOutput ao = new ArgusOutput(null);
		jf.getContentPane().add(ao);
		jf.setVisible(true);
		jf.pack();
	}
	
	public ArgusOutput(ArgusTerminal at){
		super(at);
		argusTerminal = at;
		init();
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
	
	private void init(){
		breakButton.setEnabled(false);  // We don't use it
		stringBuffer = new StringBuffer();
		setLayout(new GridLayout(0,4));
		
		try {
			BufferedReader br = tryToOpenOutputFile();

			String line;
			for(int i=1; (line = br.readLine())!= null; i++){
				JPanel panel1 = new JPanel();
				JLabel name = new JLabel(line.substring(0,search(line,"~")));
				panel1.add(name);
				add(panel1);
				name.setForeground(Color.white);
				
				JPanel panel = new JPanel();
				value[i] = new JLabel();
				panel.add(value[i]);
				add(panel);
				
				JPanel panel2 = new JPanel();
				unit[i] = line.substring(search(line,"~")+1,line.length());
				JLabel label = new JLabel(unit[i]);
				panel2.add(label);
				add(panel2);
				label.setForeground(Color.white);
				
				JButton graph = new JButton("Graph"+i);
				graph.addActionListener(this);
				add(graph);

				panel.setBackground(Color.DARK_GRAY);
				panel1.setBackground(Color.DARK_GRAY);
				panel2.setBackground(Color.DARK_GRAY);
				Border loweredetched;
				loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
				panel.setBorder(loweredetched);
				
				//Color Thresholds
				
				if(!(unit[i].equals(null))){
					if(unit[i].equals("C")){
						range[i][0]=10;
						range[i][1]=15;
						range[i][2]=30;
						range[i][3]=35;
					}else if(unit[i].equals("V")){
						range[i][0]=7.5;
						range[i][1]=7.75;
						range[i][2]=9.25;
						range[i][3]=9.5;
					}else if(unit[i].equals("mA")){
						range[i][0]=75;
						range[i][1]=100;
						range[i][2]=325;
						range[i][3]=350;
					}else{
						range[i][0]=0;
						range[i][1]=0;
						range[i][2]=0;
						range[i][3]=0;
					}
				}
			}
			
			br.close();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		decodeBeacon(beaconText);
		for(int i=0;i<3;i++){
			JPanel nullPanel = new JPanel();
			add(nullPanel);
			nullPanel.setBackground(Color.darkGray);
		}
		JCheckBox refresh = new JCheckBox("Refresh");
		refresh.addItemListener(this);
		add(refresh);
		refresh.setSelected(true);
		clock();
	}
	
	public void clock(){
		JPanel clockM = new JPanel();
		JPanel clockN = new JPanel();
		JPanel clockU = new JPanel();
		clockMain = new JLabel();
        JLabel clockName = new JLabel("Clock");
        JLabel clockUnit = new JLabel("ms");
        clockN.add(clockName);
        clockM.add(clockMain);
        clockU.add(clockUnit);
        add(clockN); add(clockM); add(clockU);
        
        clockMain.setForeground(Color.cyan);
		clockM.setBackground(Color.DARK_GRAY);
		clockName.setForeground(Color.white);
		clockN.setBackground(Color.DARK_GRAY);
		clockUnit.setForeground(Color.white);
		clockU.setBackground(Color.DARK_GRAY);
		Border loweredetched;
		loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		clockM.setBorder(loweredetched);
		
		JButton update = new JButton("Update");
		update.addActionListener(this);
		add(update);
		
        Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Calendar cal = Calendar.getInstance();
            hour=cal.get(Calendar.HOUR_OF_DAY);
            min=cal.get(Calendar.MINUTE);
            second=cal.get(Calendar.SECOND);
            clockMain.setText(hour+":"+min+":"+second);
            
            //Refresh Rate
            
            if (second%5==0 && auto){
            	decodeBeacon(beaconText);
            }
            }
        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
	}
	
	public void decodeBeacon(String parseBeacon){
		for(int i=1; i<17; i++){
			String parse = parseBeacon.substring((i-1)*4,(((i-1)*4)+3));
			double val = 0;
			if(check(parse,"A") ||check(parse,"B") ||check(parse,"C") ||check(parse,"D") ||
				check(parse,"E") ||check(parse,"F")){   
				val = Integer.parseInt(parse,16);
	        }else{
	        	val = Integer.parseInt(parse,16);
	        }
			
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
			}
			
			if(!(value[i].getText().equals("")))
				val=Double.parseDouble(value[i].getText());
			
			if(val<range[i][0]) 
				value[i].setForeground(Color.red);
			if(range[i][0]<=val&&val<range[i][1]) 
				value[i].setForeground(Color.yellow);
			if(range[i][1]<=val&&val<=range[i][2]) 
				value[i].setForeground(Color.green);
			if(range[i][2]<val&&val<=range[i][3]) 
				value[i].setForeground(Color.yellow);
			if(range[i][3]<val)
				value[i].setForeground(Color.red);
			for(int j=14;j>0;j--){
				array[i][j]=array[i][j-1];
			}
			array[i][0] = val;
		}
		
		//Temp
		
		String chara = "";
		String chars = "ABCDEF0123456789";
		for(int i=1;i<17;i++){
			if(unit[i].equals("C")){
				int code = r.nextInt(5);
				chara = chara+"39"+chars.substring(code,code+1);
			}else if(unit[i].equals("mA")){
				int code = r.nextInt(10);
				chara = chara+"3"+(10+code);
			}else if(unit[i].equals("V")){
				int code = r.nextInt(5);
				chara = chara+"32"+chars.substring(code,code+1);
			}else{
				chara = chara+"123";
			}
			chara = chara+" ";
		}
		beaconText = chara;
		
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
		if(j<range[find][0]) return Color.red;
		if(range[find][0]<=j&&j<range[find][1]) return Color.yellow;
		if(range[find][1]<=j&&j<=range[find][2]) return Color.green;
		if(range[find][2]<j&&j<=range[find][3]) return Color.yellow;
		if(range[find][3]<j) return Color.red;
		return Color.white;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String name = arg0.getActionCommand();
		
		if(name.equals("Update")){
			decodeBeacon(beaconText);
		}else{
			p = DrawGraph.close();
			find = Integer.parseInt(name.substring(5,name.length()));
			DrawGraph.newGraph(p);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED){
			auto = true;
		}else{
			auto = false;
		}
	}
}

