import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jLOAF.tools.LeaveOneOut;
import org.jLOAF.tools.TestingTrainingPair;

import util.expert.lfo.SmartRandomExpertStrategy;

public class RunTracer {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SmartRandomExpertStrategy s = new SmartRandomExpertStrategy();
		s.parseFile("C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1\\SmartRandomAgent", "TestCasebase.cb");
		
		LeaveOneOut l = LeaveOneOut.loadTrainAndTest("TestCasebase.cb", 1000, 7);
		List<TestingTrainingPair> loo = l.getTestingAndTrainingSets();
		
		JScrollPane p = new JScrollPane(new RunModel(loo.get(0).getTesting()), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(p);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
