package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;

import javax.swing.*;

import mas.swing.ClosingJFrame;
import mas.utils.Utils;

/** Loads a contact session text file and extracts the data portions. Saves those portions in a separate
 *  file - RAW data, not calibrated.
 * @author mas
 *
 */
public class ArgusLogfileArchiver extends ClosingJFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String BEACON_EXTENSION = "argbraw";
	public static String[] LOGFILE_EXTENSION = {"txt"};
	public static String BEACON_START = "BEABEG";
	public static String BEACON_END = "BEAEND";
	protected JTextField logField, beacField;
	protected JTextArea beacData;
	public static Font jtaFont = new Font(Utils.typewriterFont(), Font.PLAIN, 12);

	public ArgusLogfileArchiver() {
		super("Load and Parse Argus Sessions");
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel jp = new JPanel();
		JButton jb = new JButton("Load");
		jb.addActionListener(this);
		jp.add(jb);
		JPanel namePanel = new JPanel(new GridLayout(2,1));
		JPanel logPanel = new JPanel();
		logPanel.add(new JLabel("Log file:"));
		logField = new JTextField(30);
		logField.setFont(jtaFont);
		logPanel.add(logField);
		namePanel.add(logPanel);
		JPanel beacPanel = new JPanel();
		beacPanel.add(new JLabel("Beacon file:"));
		beacField = new JTextField(30);
		beacField.setFont(jtaFont);
		beacPanel.add(beacField);
		namePanel.add(beacPanel);
		jp.add(namePanel);
		cp.add(jp, BorderLayout.NORTH);
		beacData = new JTextArea(50, 80);
		beacData.setFont(jtaFont);
		cp.add(new JScrollPane(beacData), BorderLayout.CENTER);
		setVisible(true);
		pack();
	}

	public static void main(String[] args) {
		new ArgusLogfileArchiver();
	}
	
	public void loadAndRun() {
		File logfile = Utils.openFile(this, "Choose Log File", LOGFILE_EXTENSION, "Argus Log Files");
		if (logfile == null) {
			return;
		}
		String[] logName = logfile.getName().split(LOGFILE_EXTENSION[0]);
		File beacFile = new File(logfile.getParentFile(), logName[0] + BEACON_EXTENSION);
		logField.setText(logfile.getName());
		beacField.setText(beacFile.getName());
		try {
			BufferedReader br = new BufferedReader(new FileReader(logfile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(beacFile));
			// Let's go
			while (br.ready()) {
				String line = br.readLine();
				System.out.println(line);
				int bstart = line.indexOf(BEACON_START);
				if (bstart >= 0) {
					int bend = line.indexOf(BEACON_END);
					while (bend < 0) {
						line = line + br.readLine();
						bend = line.indexOf(BEACON_END);
					}
					String writeText = line.substring(bstart + BEACON_START.length(), bend) + "\n";
					bw.write(writeText);
					beacData.append(writeText);
				}
			}
			// All done
			br.close();
			bw.close();
		} catch (IOException ioe) {
			System.err.println("IOException " + ioe.getMessage());
		}

	}

	@Override
	public void windowClosing(WindowEvent e) {
		dispose();
	}

	public void actionPerformed(ActionEvent aEvent) {
		String actionC = aEvent.getActionCommand();
		if (actionC.equals("Load")) {
			loadAndRun();
		}
	}

}
