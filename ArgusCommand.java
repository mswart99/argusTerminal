package argusTerminal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import basicTerminal.BasicUserControlPanel;

public class ArgusCommand extends BasicUserControlPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected ArgusTerminal argusTerminal;
	protected StringBuffer stringBuffer;
	int len=0;
	JTextField[] text = new JTextField[100];
	String cOut = null;
	public static final String commandFile = "argusTerminal/ArgusCommands.argus";

	public static void main(String[] args) {
		JFrame jf = new JFrame("Argus Command");
		jf.pack();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ArgusCommand ac = new ArgusCommand();
		jf.getContentPane().add(ac);
		jf.setVisible(true);
	}

	public ArgusCommand() {
		super(null);
		init();
	}
	
	public ArgusCommand(ArgusTerminal at) {
		super(at);
		argusTerminal = at;
		init();
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
	
	private void init() {
		breakButton.setEnabled(false);  // We don't use it
		stringBuffer = new StringBuffer();
		//Create Buttons
		JPanel bPanel = new JPanel();
		Box con = Box.createVerticalBox();
		try{
			//Reads Commands.txt

			BufferedReader br = new BufferedReader(new FileReader(commandFile));

			//Uses Commands.txt to create new buttons

			String line;
			while((line = br.readLine()) != null){

				Box bRow = Box.createHorizontalBox();
				
				String name = line.substring(0, check(line, "~"));
				JButton send = new JButton("Send"+name);
				send.setMinimumSize(new Dimension(72, 30));
				send.setPreferredSize(new Dimension(72, 30));
				send.setMaximumSize(new Dimension(72, 30));
				send.addActionListener(this);
				bRow.add(send);
				
				JLabel label = new JLabel("     "+name);
				bRow.add(label);
				label.setMinimumSize(new Dimension(190,25));
				label.setPreferredSize(new Dimension(190,25));
				label.setMaximumSize(new Dimension(190,25));
				
				if (check(line, "^") != 0){
				
					text[len] = new JTextField();
					bRow.add(text[len]);
					text[len].setMaximumSize(new Dimension(100, 25));
					
					JLabel parameter = new JLabel();
					bRow.add(parameter);
					parameter.setText("   " + parExp(line));
				}
				
				bRow.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				con.add(bRow);
				
				len++;
			}
			

			br.close();
		} catch (FileNotFoundException fnffe){
			message("Could not find config file: " + commandFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Box fill = Box.createHorizontalBox();
		fill.setMinimumSize(new Dimension(0,10));
		fill.setPreferredSize(new Dimension(0,10));
		fill.setMaximumSize(new Dimension(0,10));
		con.add(fill);
		
		//Creates custom commands field
		
		Box custom = Box.createHorizontalBox();
		JButton send = new JButton("SendCustom");
		send.addActionListener(this);
		send.setMinimumSize(new Dimension(72, 30));
		send.setPreferredSize(new Dimension(72, 30));
		send.setMaximumSize(new Dimension(72, 30));
		custom.add(send);
		JLabel label = new JLabel();
		custom.add(label);
		label.setMinimumSize(new Dimension(190,25));
		label.setPreferredSize(new Dimension(190,25));
		label.setText("     Input New Command: ");
		text[99] = new JTextField();
		custom.add(text[99]);
		custom.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		con.add(custom);
		
		bPanel.setLayout(new BorderLayout());
		bPanel.add(con, BorderLayout.WEST);
		
		bPanel.setVisible(true);
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
		int j;
		
		for(int i=0; i<line.length(); i++){
			if (line.substring(i, i+1).equals("^")){
				if ((j = check(line, "#")) != 0){				
					title = line.substring(i+1, j);
				}else{
					title = line.substring(i+1, line.length());
				}
			}
		}
		return title;
	}

	//Activates when button is pressed

	public void actionPerformed(ActionEvent aEvent) {
		super.actionPerformed(aEvent);
		String name = aEvent.getActionCommand();
		int count = 0, title, par, che;

		//Detects if a default button is pressed
		if(name.substring(4, name.length()).equals("Custom") && !(text[99].getText().equals(""))){
			int response = JOptionPane.showConfirmDialog(null, "Send: "+text[99].getText(), "Confirm", JOptionPane.YES_NO_OPTION);
			if(response == JOptionPane.YES_OPTION){	
				send(text[99].getText());
			}
		}else{
			try {
				BufferedReader commands = new BufferedReader(new FileReader(commandFile));

				//Searches Commands.txt to find selected Title and outputs equivalent Command

				String in;
				while((in = commands.readLine()) != null){
					if ((title = check(in, "~")) != 0){
						if(in.substring(0,title).equals(name.substring(4, name.length()))){
							if((par = check(in, "^")) != 0){
								if ((che = check(in, "#")) != 0){
									int response = JOptionPane.showConfirmDialog(null, "Send: "+in.substring(title+1, par)+text[count].getText(), "Confirm", JOptionPane.YES_NO_OPTION);
									if(response == JOptionPane.YES_OPTION){	
										System.out.println(in.substring(title+1, par)+text[count].getText());
									}
								}else{
									System.out.println(in.substring(title+1, par)+text[count].getText());
								}
								break;
							}else{
								if((che = check(in, "#")) != 0){
									int response = JOptionPane.showConfirmDialog(null, "Send: "+in.substring(title+1, che), "Confirm", JOptionPane.YES_NO_OPTION);
									if(response == JOptionPane.YES_OPTION){	
										System.out.println(in.substring(title+1, che));
									}
								}else{
									System.out.println(in.substring(title+1, in.length()));
								}
								break;
							}
						}
					}
				count++;
				}

				commands.close();
			} catch (FileNotFoundException fnfe){
				message("Could not find config file: " + commandFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
