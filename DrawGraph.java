package argusTerminal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

//@SuppressWarnings("serial")
public class DrawGraph extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int PREF_W = 400, PREF_H = 325;
	private static final int BORDER_GAP = 25;
	private static final Color GRAPH_COLOR = Color.blue;
	private static final Stroke GRAPH_STROKE = new BasicStroke(1.5f);
	private static final int GRAPH_POINT_WIDTH = 6;
	private static final int Y_HATCH_CNT = 12;
//	private Vector<Double> scores;
//	static Point p;
//	static JPanel mainPanel;
	private JFrame frame;
	private int sensorID;
	private ArgusOutputPanel aop;
	private int numPts;

	public DrawGraph(ArgusOutputPanel aout, int sID, Point p, int pointsToPlot,
			String title) {
		aop = aout;
		numPts = pointsToPlot;
		sensorID = sID;
		// Start the Graph
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocation(p);
		frame.setVisible(true);
	}
	
	public static String[] AXES_MILLIAMPS = {"400", "200", "-400"};
	public static String[] AXES_VOLTS = {"0", "5", "10"};
	public static String[] AXES_CELSIUS = {"-15", "20", "40"};
	public static double UNIT_MILLIAMPS = 0.5;
	public static double UNIT_CELSIUS = 5;
	public static double UNIT_VOLTS = 20;
	
//	public void startTimer() {
//		Timer timer = new Timer(500, new ActionListener() {
////			int i=0; 
//			
//			public void actionPerformed(ActionEvent e) {
//				repaint();
//			}
//		});
//		timer.setRepeats(true);
//		timer.setCoalesce(true);
//		timer.start();
//	}

	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		setLayout(null);

		List<Point> graphPoints = new ArrayList<Point>();
		double offset=0, unit=1;
		String[] axes = AXES_CELSIUS;
		if(ArgusOutputPanel.sendUnit(sensorID).equals(ArgusOutputPanel.UNIT_CELSIUS)) {
			unit = UNIT_CELSIUS;
			axes = AXES_CELSIUS;
		} else if(ArgusOutputPanel.sendUnit(sensorID).equals(ArgusOutputPanel.UNIT_MILLIAMPS)) {
			unit = UNIT_MILLIAMPS;
			axes = AXES_MILLIAMPS;
		} else if (ArgusOutputPanel.sendUnit(sensorID).equals(ArgusOutputPanel.UNIT_VOLTS)){
			unit = UNIT_VOLTS;
			axes = AXES_VOLTS;
		}

		for(int i=numPts-1;i>=0;i--){
			int y1 = (int) (((double) getHeight())-aop.sendArray(i, sensorID)*unit-100+offset);
			int x1 = i*25 + BORDER_GAP;
			graphPoints.add(new Point(x1, y1));
		}
		// create x and y axes 
		g2.drawLine(BORDER_GAP+25, getHeight() - BORDER_GAP, BORDER_GAP+25, BORDER_GAP);
		g2.drawLine(BORDER_GAP+25, getHeight() - 4*BORDER_GAP, 
				getWidth() - BORDER_GAP, getHeight() - 4*BORDER_GAP);

		g2.drawString(axes[0],10,30);
		g2.drawString(axes[1],10,130);
		g2.drawString(axes[2],8,305);
		g2.drawString("0", 18, getHeight() - 4*BORDER_GAP+5);

		// create hatch marks for y axis. 
		for (int i = 0; i < Y_HATCH_CNT; i++) {
			int x0 = BORDER_GAP+15;
			int x1 = x0+20;
			int y0 = 25*i + getHeight() - 300;
			int y1 = y0;
			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		for (int i = 0; i < numPts - 2; i++) {
			int x0 = 25*i + getWidth() - 350;
			int x1 = x0;
			int y0 = getHeight() - 4*BORDER_GAP +10;
			int y1 = y0 - 20;
			g2.drawLine(x0, y0, x1, y1);
		}

		Stroke oldStroke = g2.getStroke();
		g2.setColor(GRAPH_COLOR);
		g2.setStroke(GRAPH_STROKE);
		for (int i = 0; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x+25;
			int y1 = graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x+25;
			int y2 = graphPoints.get(i + 1).y;
			g2.drawLine(x1, y1, x2, y2);         
		}

		g2.setStroke(oldStroke);      

		for (int i = 0; i < graphPoints.size(); i++) {
			int x = i*25 + BORDER_GAP - (GRAPH_POINT_WIDTH/2) +(1/2)+25;
			int y = (int) (((double)getHeight())-aop.sendArray(i, sensorID)*unit
					-100+offset-((double)GRAPH_POINT_WIDTH)/2);
			int ovalW = GRAPH_POINT_WIDTH;
			int ovalH = GRAPH_POINT_WIDTH;
			g2.setColor(aop.sendTLCcolor(aop.sendArray(i, sensorID), sensorID));
			g2.fillOval(x, y, ovalW, ovalH);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}

//	private static void createAndShowGui(int sensorID) {
//		List<Integer> scores = new ArrayList<Integer>();
//		int maxDataPoints = 16;
//		for (int i = 0; i < maxDataPoints ; i++) {
//			scores.add(i);
//		}
//		mainPanel = new DrawGraph(scores);
//
//	}


//	public static void main(String[] args) {
//		int sID = 0;
//		if (args != null) {
//			sID = Integer.valueOf(args[0]);
//		}
//		SwingUtilities.invokeLater(new GuiRun(sID));
//	}

//	private class GuiRun implements Runnable {
//		private int sensorID;
//		public GuiRun(int sID) {
//			sensorID = sID;
//		}
//		public void run() {
//			createAndShowGui(sensorID);
//		}
//	}

//	public void redraw(){
//		repaint();
//	}


	/** Only listens for actions that would indicate a redraw is needed.
	 * 
	 * @param arg0
	 */
	public void actionPerformed(ActionEvent arg0) {
		repaint();		
	}

//	public static Point close(){
//		if(mainPanel!=null){
//			p = frame.getLocationOnScreen();
//			frame.dispose();
//		}
//		return p;
//	}

//	static void newGraph(Point in, int sensorID){
//		if(mainPanel==null){
//			p = new Point(500,125);
//		}else{
//			p = in;
//		}
//		String[] args = {new Integer(sensorID).toString()};
//		main(args);
//	}
}
