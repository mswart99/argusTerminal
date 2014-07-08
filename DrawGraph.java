package argusTerminal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class DrawGraph extends JPanel {
   private static final int PREF_W = 400;
   private static final int PREF_H = 325;
   private static final int BORDER_GAP = 25;
   private static final Color GRAPH_COLOR = Color.blue;
   private static final Stroke GRAPH_STROKE = new BasicStroke(1.5f);
   private static final int GRAPH_POINT_WIDTH = 6;
   private static final int Y_HATCH_CNT = 12;
   private List<Integer> scores;
   static DrawGraph mainPanel;
   

   public DrawGraph(List<Integer> scores) {
      this.scores = scores;
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      setLayout(null);

      List<Point> graphPoints = new ArrayList<Point>();
      double offset=0, unit=1;
	  if(ArgusOutput.sendUnit().equals("C"))
		  unit = 8;
	  if(ArgusOutput.sendUnit().equals("mA"))
		  unit = .4;
	  if(ArgusOutput.sendUnit().equals("V")){
		  unit = 20;
	  }
      for(int i=13;i>=0;i--){
     	 int y1 = (int) (((double) getHeight())-ArgusOutput.sendArray(i)*unit-100+offset);
    	 int x1 = i*25 + BORDER_GAP;
    	 graphPoints.add(new Point(x1, y1));
      }
      // create x and y axes 
      g2.drawLine(BORDER_GAP+25, getHeight() - BORDER_GAP, BORDER_GAP+25, BORDER_GAP);
      g2.drawLine(BORDER_GAP+25, getHeight() - 4*BORDER_GAP, getWidth() - BORDER_GAP, getHeight() - 4*BORDER_GAP);
      
      g2.drawString(Integer.toString(12),5,30);
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
      for (int i = 0; i < scores.size() - 2; i++) {
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
         int y = (int) (((double)getHeight())-ArgusOutput.sendArray(i)*unit-100+offset-((double)GRAPH_POINT_WIDTH)/2);
         int ovalW = GRAPH_POINT_WIDTH;
         int ovalH = GRAPH_POINT_WIDTH;
   	  	 g2.setColor(ArgusOutput.sendColor(ArgusOutput.sendArray(i)));
         g2.fillOval(x, y, ovalW, ovalH);
      }
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }

   private static void createAndShowGui() {
      List<Integer> scores = new ArrayList<Integer>();
      int maxDataPoints = 16;
      for (int i = 0; i < maxDataPoints ; i++) {
         scores.add(i);
      }
      mainPanel = new DrawGraph(scores);

      JFrame frame = new JFrame("DrawGraph");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.getContentPane().add(mainPanel);
      frame.setResizable(false);
      frame.pack();
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            createAndShowGui();
         }
      });
   }
   
   public static void redraw(){
   	if(mainPanel!=null) mainPanel.repaint();
   }
   
   public static void close(){
	if(mainPanel!=null) frame.dispose();
   }
}
