import javax.swing.JFrame;

public class RunTracer {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(new TracePanel());

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
