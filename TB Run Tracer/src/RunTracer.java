import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class RunTracer {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane p = new JScrollPane(new RunModel(null, frame), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(p);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
