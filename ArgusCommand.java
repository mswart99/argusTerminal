package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mas.utils.Utils;
import basicTerminal.BasicUserControlPanel;

public class ArgusCommand extends BasicUserControlPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected ArgusTerminal argusTerminal;
	protected StringBuffer stringBuffer;
	JTextField[] text = new JTextField[100];
	public static final String[] commandFile = {
		"argusTerminal/ArgusCommands.argus",
		"ArgusCommands.argus"
	};

	public static void main(String[] args) {
		JFrame jf = new JFrame("Argus Command");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ArgusCommand ac = new ArgusCommand(null);
		jf.getContentPane().add(ac);
		jf.setVisible(true);
		jf.pack();
	}

	public ArgusCommand(ArgusTerminal at) {
		super(at);
		argusTerminal = at;
		init();
	}
	
	public void message(String text, boolean makeBold) {
		if (argusTerminal == null) {
			System.out.println(text);
		} else {
			argusTerminal.message(text, makeBold);
		}
	}
	
	public void message(String text) {
		message(text, false);
	}
	
	/** StringBuffers are used in send() in the foolish attempt to manage
	 *  memory in Java.
	 *  
	 * @param commandString
	 */
	public void send(String commandString) {
		stringBuffer.setLength(0);
		stringBuffer.append(commandString);
		send(stringBuffer);
	}
	
	/** Route the command to the ArgusTerminal, if it exists. Otherwise
	 * just dump to stdout
	 * @param command
	 */
	public void send(StringBuffer command) {
		send(command, true);
	}
	
	/** Route the command to the ArgusTerminal, if it exists. Otherwise
	 * just dump to stdout
	 * @param command
	 */
	public void send(StringBuffer command, boolean endLine) {
		if (argusTerminal == null) {
			System.out.print(command);
			if (endLine) {
				System.out.print("\n");
			}
		} else {
			argusTerminal.send(command, endLine);
		}
	}
	
	private BufferedReader tryToOpenCommandFile() {
		BufferedReader br;
		for (int i=0; i < commandFile.length; i++) {
			try {
				br = new BufferedReader(new FileReader(commandFile[i]));
				return(br);
			} catch (FileNotFoundException fnfe) {
				// Do nothing. Try the next one
			}
		}
		// The file is not where it should be
		File f = new File("");
		System.err.println("Could not find any config file: " + 
				Utils.arrayToStringBuffer(commandFile));
		System.err.println("Did you move the config file to this directory " + f.getAbsolutePath());
		return(null);
	}
	
	private void init() {
		breakButton.setEnabled(false);  // We don't use it
		stringBuffer = new StringBuffer();
		Box con = Box.createVerticalBox();
		try{
			BufferedReader br = tryToOpenCommandFile();

			String line;
			for(int i=0; (line = br.readLine()) != null; i++){

				Box bRow = Box.createHorizontalBox();
				
				String name = line.substring(0, check(line, "~"));
				JButton send = new JButton("Send"+name);
				send.setPreferredSize(new Dimension(72, 30));
				send.setMaximumSize(new Dimension(72, 30));
				send.addActionListener(this);
				bRow.add(send);
				
				JLabel label = new JLabel("     "+name);
				label.setPreferredSize(new Dimension(190,25));
				label.setMaximumSize(new Dimension(190,25));
				bRow.add(label);
				
				if (check(line, "^") != 0){
				
					text[i] = new JTextField();
					bRow.add(text[i]);
					text[i].setMaximumSize(new Dimension(125, 35));
					
					JLabel parameter = new JLabel();
					bRow.add(parameter);
					parameter.setText("   " + parExp(line));
				}
				
				bRow.setAlignmentX(Component.LEFT_ALIGNMENT);
				bRow.setBackground(Color.darkGray);
				con.add(bRow);
			}
			

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		add(con, BorderLayout.CENTER);
	}

	static int check(String line, String sym){
		
		for(int i=0; i<line.length(); i++){
			if (line.substring(i, i+1).equals(sym)){
				return i;
			}
		}
		return 0;
	}
	
	static String parExp(String line){
		
		String title = null;
		int i, j;
		
		if ((i = check(line, "^")) != 0){
			if ((j = check(line, "#")) != 0){				
				title = line.substring(i+1, j);
			}else{
				title = line.substring(i+1, line.length());
			}
		}
		return title;
	}

	//Activates when button is pressed

	public void actionPerformed(ActionEvent aEvent) {
		super.actionPerformed(aEvent);
		String name = aEvent.getActionCommand();
		int title, par, che;
		try {
			BufferedReader commands = tryToOpenCommandFile();

			String in;
			for(int i=0; (in=commands.readLine())!=null; i++){
				if ((title = check(in, "~")) != 0 && in.substring(0,title).equals(name.substring(4,name.length()))){
					if((par = check(in, "^")) != 0){
						if (((che = check(in, "#")) != 0) &&
							JOptionPane.showConfirmDialog(null, "Send: "+in.substring(title+1, par)+text[i].getText(), "Confirm", JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION){	
								send(in.substring(title+1, par)+text[i].getText());
						}else{
							send(in.substring(title+1, par)+text[i].getText());
						}
						break;
					}else{
						if(((che = check(in, "#")) != 0) &&
							JOptionPane.showConfirmDialog(null, "Send: "+in.substring(title+1, che), "Confirm", JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION){	
								send(in.substring(title+1, che));	
						}else{
							send(in.substring(title+1, in.length()));
						}
						break;
					}
				}
			}

			commands.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
