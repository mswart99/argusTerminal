/** Connects the state generator to the socket for external
 * control.
 *
 * satTerminal.java	Created 07/20/2005	M. Swartwout (from TNCserver 3)
 * satTerminal.java	Updated 04/21/2011	M. Swartwout
 *
 *	Version 1.0 -- Compatible with BasicServer 3.0
 *  version 2.0 -- Talks with Matlab and gutted the interpreter path
 *  version 5.0 -- Overhauled to handle a better GUI and a Java-side state propagator.
 *  version 6.0 -- Made a generic SatTerminal class
 */

package argusTerminal;

import java.awt.*;
import java.io.*;

import javax.swing.JMenuItem;

import TNCterminal.TNCcontrolPanel;
import TNCterminal.TNCinputDisplay;
import TNCterminal.TNCinterpreter;
import TNCterminal.TNCoutputDisplay;
import TNCterminal.TNCterminal;
import basicTerminal.serialport.*;

public class ArgusTerminal extends TNCterminal {
	private static final long serialVersionUID = 1L;
	public static final String version = "ArgusTerminal 1.0 06/03/2014";
	public static String helpMessage = 
		"Usage:  ArgusTerminal [-s serialPortNum]";
	public static final String UNPROTO_ARGUS = "ARGUS1";
//	protected TNCcore tncCore;
	
	public static void main(String[] args) throws IOException {
		BasicArgParserSerialServer bap = new BasicArgParserSerialServer(args);
		bap.parseAllArgs();
		// Launch the server
		ArgusTerminal edt = new ArgusTerminal(bap.connectOnStart, bap.socketNum, SELF_LAUNCH,
				bap.portID);
		edt.setSize(new Dimension(750, 700));
	}
	
	public ArgusTerminal(boolean startSocket, int launcher) {
		super(startSocket, launcher);
		init();
	}

	public ArgusTerminal(boolean startSocket, int newSocket, int launcher, 
			String openWithThisPort) {
		super(startSocket, newSocket, launcher, openWithThisPort);
		init();
	}
	
	/** Adds menu items 
	 */
	protected void addMenus() {
		super.addMenus();
		versionMenu.add(new JMenuItem(version));
	}
	
	private void init() {
		setTitle(version);
		message(version);
		pack();
	}
	
	protected TNCinputDisplay initInputDisplay() {
		TNCinputDisplay tin = super.initInputDisplay();
		tin.setEnabled(true);
		tin.add(new ArgusCommand(this), BorderLayout.CENTER);
		return(tin);
	}
	
	protected TNCoutputDisplay initOutputDisplay() {
//		TNCoutputDisplay tout = super.initOutputDisplay();
//		tout.setEnabled(true);
//		tout.add(new ArgusOutputPanel(this), BorderLayout.CENTER);
//		return(tout);
		return(new ArgusOutputPanel(this));
	}
	
	/** Updates the default command settings for Argus.
	 * 
	 * @return
	 */
	protected String[] getTNCCommandDefaults() {
		String[] commandDefaults = TNCcontrolPanel.BASE_TNCCOMMANDDEFAULT;
		/* Obviously, this is predicated on knowing which value in the String array
		 * we need to change. We could write a snazzy function to check the list in
		 * BASE_TNCCOMMANDLIST to find our match, but we're going to assume that nobody
		 * is monkeying with the order of that list.
		 */
		commandDefaults[1] = UNPROTO_ARGUS;
		return(commandDefaults);
	}
	
	protected TNCinterpreter getInterpreter(TNCterminal tnct) {
		return(new ArgusInterpreter(tnct));
	}
	
//	protected TNCcontrolPanel initInsetPanel(BasicTerminal bt) {
//		TNCcontrolPanel tncControl = super.initInsetPanel(bt);
////		JPanel argus
//		return(tncControl);
//	}
	
//	/** Turns on or off control panels depending on the value of set.
//	 */ 
//	protected void activateControls(boolean set) {
//		super.activateControls(set);
//		if (tncCore != null) {
//			tncCore.activateControls(set);
//		}
//	}
}