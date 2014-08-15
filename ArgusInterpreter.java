package argusTerminal;

import TNCterminal.TNCinterpreter;
import TNCterminal.TNCterminal;


public class ArgusInterpreter extends TNCinterpreter {
	private StringBuffer beaconBuffer = new StringBuffer();
	private StringBuffer textBuffer = new StringBuffer();
	private boolean foundBeacon = false;
	public static final String BEACON_START = "SLUBCN";
	public static final String BEACON_END = "BEAEND";
	

	public ArgusInterpreter(TNCterminal tnct) {
		super(tnct);
	}

	/** We are looking for beacon data.
	 * 
	 */
	protected String decipherThis(String text) {
		// Let the superclass do its thing
		text = super.decipherThis(text);
//		System.out.println("Parse: " + text);
		/* Parse for beacon data
		 * We assume that text could be of arbitrary length, and could include
		 * multiple beacons
		 */
		int bstart, bend;
		textBuffer.setLength(0);
		textBuffer.append(text);
		while (textBuffer.length() > 0) {
			// Find the next instances of the start and end beacon bits
			bstart = textBuffer.indexOf(BEACON_START);
			bend = textBuffer.indexOf(BEACON_END);
//			System.out.println("\tDeciphering: " + textBuffer);
			// If we are not in the middle of a beacon, look for the start
			if (!foundBeacon) {
				// Look for the start
				if (bstart >= 0) {
//					System.out.println("BEGIN");
					foundBeacon = true;
					/* Grab everything between BEACON_START and the next BEACON_END
					 * If there is no BEACON_END, grab the rest of the string.
					 */
					if (bend < 0) {
						bend = textBuffer.length();
					}
					beaconBuffer.setLength(0);
					beaconBuffer.append(textBuffer.substring(bstart + BEACON_START.length() , bend));
					/* Eliminate the text string up to the location of bend -- we'll deal with
					 * BEACON_END on the next go-round
					 */
//					System.out.println("\t\tBeacon is now: " + beaconBuffer);
					textBuffer.delete(0, bend);
				} else {
					// If there's no start, we clear the string
					textBuffer.setLength(0);
				}
			} else {
				// Look for the end
				if (bend >= 0) {
					// Grab everything up to BEACON_END
					beaconBuffer.append(textBuffer.substring(0, bend));
					// Send
//					System.out.println("Sending: " + beaconBuffer);
					sendToDisplay(beaconBuffer.toString());
					// Reset
					foundBeacon = false;
					// Pull that text out of textBuffer
					textBuffer.delete(0, bend);
				} else {
					// Add it all and keep going
					beaconBuffer.append(textBuffer);
					textBuffer.setLength(0);
				}				
			}
			
		}		
		return(text);
	}
}