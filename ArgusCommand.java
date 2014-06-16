package argusTerminal;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import basicTerminal.BasicUserControlPanel;

public class ArgusCommand extends BasicUserControlPanel {

	private static final long serialVersionUID = 1L;
	protected ArgusTerminal argusTerminal;
	protected StringBuffer stringBuffer;
	public int length=5;
	JTextField text = new JTextField("");
	JLabel label = new JLabel();
	public static final String commandFile = "argusTerminal/ArgusCommands.argus";

	public static void main(String[] args) {
		JFrame jf = new JFrame("Argus Command");
		jf.setSize(250, 728);
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
		try{
			//Reads Commands.txt

			BufferedReader br = new BufferedReader(new FileReader(commandFile));

			//Uses Commands.txt to create new buttons

			String line;
			while((line = br.readLine()) != null){

				String name = Title(line);
				JButton button = new JButton(name);
				button.addActionListener(this);	
				bPanel.add(button);
				length++;
			}

			//Creates default menu items

			bPanel.add(text);
			bPanel.add(label);

			JButton enter = new JButton("Enter");
			enter.addActionListener(this);	
			bPanel.add(enter);

			JButton send = new JButton("Send");
			send.addActionListener(this);	
			bPanel.add(send);

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(this);	
			bPanel.add(cancel);

			br.close();
		} catch (FileNotFoundException fnffe){
			message("Could not find config file: " + commandFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Size of buttons determined by number of commands
		bPanel.setLayout(new GridLayout(length, 1));
		bPanel.setVisible(true);
		add(bPanel, BorderLayout.CENTER);
	}

	//Parses line to find Command Title (Displays on button)

	static String Title(String line){

		String title = null;

		for(int i=0; i<line.length(); i++){
			if (line.substring(i, i+1).equals("~")){
				title = line.substring(0,i);
			}
		}
		return title;
	}

	//Activates when button is pressed

	public void actionPerformed(ActionEvent arg0) {
		String name = arg0.getActionCommand();

		//Detects if a default button is pressed

		if(name.equals("Enter")){
			String type = text.getText();
			label.setText(type);
		}else if(name.equals("Send")){
			if(!label.getText().equals("")){

				//Replace "System.out..." with the output to the radio
				send(label.getText());
				label.setText("");
			}
		}else if(name.equals("Cancel")){
			text.setText("");
			label.setText("");
		}else{

			//Reads Commands.txt

			try {
				BufferedReader commands = new BufferedReader(new FileReader(commandFile));

				//Searches Commands.txt to find selected Title and outputs equivalent Command

				String in;
				while((in = commands.readLine()) != null){

					for(int i=0; i<in.length(); i++){
						if (in.substring(i, i+1).equals("~")){
							if(in.substring(0,i).equals(name)){
								text.setText(in.substring(i+1, in.length()));
							}
						}
					}
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