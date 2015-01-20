import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import org.jLOAF.casebase.CaseRun;


public class RunModel extends JDesktopPane implements MouseMotionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CaseRun run;
	
	private boolean isEntered;
	
	private double index;
	
	private CustomDialog d;
	
	public RunModel(CaseRun r, JFrame frame){
		setPreferredSize(new Dimension(30 + 30 * getRunSize() + 30, 50));
		this.isEntered = false;
		
		this.index = -1;
		
		d = new CustomDialog();
		d.pack();
		this.add(d);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		draw(g2, 0);
	}

	private int getRunSize(){
		return 1000;
	}
	
	private void draw(Graphics2D g2, float yOffset){
		for (int i = 0; i < getRunSize(); i++){
			g2.setColor(Color.BLACK);
			if (index == i){
				g2.setColor(Color.GREEN);
			}
			g2.fillOval(12 + (30 * i), 12, 14, 14);
			g2.setColor(Color.BLACK);
			if (index - 0.5 == i){
				g2.setColor(Color.CYAN);
				g2.fillRect(30 + (30 * i), 16, 6, 6);
			}else {
				g2.drawRect(30 + (30 * i), 16, 6, 6);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		index = -1;
		if (this.isEntered){
			int x = arg0.getX();
			int y = arg0.getY();
			int action = 30;
			int input = 12;
			for (int i = 0; i < getRunSize(); i++){
				if (y >= 12 && y < 12 + 14){
					if (x >= input && x < input + 14){
						index = i;
						break;
					}else if (x >= action && x < action + 6){
						index = i + 0.5;
						break;
					}
				}
				
				input += (30);
				action += (30);
			}
			if (index != -1){
				repaint();
			}
//			System.out.println("Index : " + index);
		}
	}
	
	class CustomDialog extends JInternalFrame{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private JLabel l;
		
		public CustomDialog(){
			super("Hello World");
			l = new JLabel("Hello World");
			this.add(l);
			this.setSize(200, 200);
		}
		
		public void setText(String text){
			this.l.setText(text);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		this.isEntered = true;
	}
	@Override
	public void mouseExited(MouseEvent e) {
		this.isEntered = false;
		repaint();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && index != -1){
			d.setLocation(e.getX(), e.getY());
			d.setVisible(true);
			repaint();
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && index != -1){
			d.setVisible(false);
			repaint();
		}
	}

	
}
