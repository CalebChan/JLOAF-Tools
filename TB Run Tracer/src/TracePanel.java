import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;


public class TracePanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float scale;
	
	private static final float SCALE_FACTOR = 0.1f;
	
	private RunModel problemModel;
	
	public TracePanel(){
		this.scale = 1;
		setPreferredSize(new Dimension(1024, 300));
		
		this.addMouseWheelListener(new MouseAdapter(){
			public void mouseWheelMoved(MouseWheelEvent e){
				if (e.getWheelRotation() > 0){
					//decreaseScale();
				}else if (e.getWheelRotation() < 0){
					//increaseScale();
				}
			}
		});
		
		this.addMouseMotionListener(new MouseAdapter(){
			public void mouseMoved(MouseEvent e){
				System.out.println("X : " + e.getX() + " Y : " + e.getY() + " Overlap : " + overlap);
				if (e.getX() > 228 - 14 && e.getX() < 228 + 14 && e.getY() > 24 - 14 && e.getY() < 24 + 14){
					overlap = true;
					repaint();
				}else{
					overlap = false;
					repaint();
				}
			}
		});
	}
	
	private boolean overlap = false;
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		drawScale(g2, 0);
	}
	
	public void increaseScale(){
		scale += SCALE_FACTOR;
		repaint();
	}
	public void decreaseScale(){
		scale -= SCALE_FACTOR;
		scale = Math.max(1, scale);
		repaint();
	}
	
	private void drawScale(Graphics2D g2, float yOffset){
		AffineTransform at = g2.getTransform();
		
		g2.scale(scale, scale);
		
		String txt = "A-->                                                B";
		g2.scale(scale, scale);
		g2.setColor(Color.DARK_GRAY);
		g2.drawRect(10, 10 + (int)yOffset, 234, 20);
		g2.drawString(txt, 14, 24 + yOffset);
		if (overlap){
			g2.setColor(Color.GREEN);
		}else{
			g2.setColor(Color.red);
		}
		g2.drawOval(228, 12 + (int)yOffset, 14, 14);
		
		
		g2.setTransform(at);
	}
}
