import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jLOAF.tools.LeaveOneOut;
import org.jLOAF.tools.TestingTrainingPair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.expert.lfo.SmartRandomExpertStrategy;

public class RunTracer {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setTitle("Title");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		SmartRandomExpertStrategy s = new SmartRandomExpertStrategy();
		s.parseFile("C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1\\SmartRandomAgent", "TestCasebase");
		
		LeaveOneOut l = LeaveOneOut.loadTrainAndTest("TestCasebase.cb", 1000, 7);
		List<TestingTrainingPair> loo = l.getTestingAndTrainingSets();
		
		JScrollPane p = new JScrollPane(new RunModel(loo.get(0).getTesting()), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(p);
		
		RunTracer r = new RunTracer("C:/Users/calebchan/Desktop/Stuff/git/jLOAF-Sandbox-Agent/testSuiteJSON.json",p);
		r.parseJSON();

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private String filename;
	private JScrollPane p;
	
	public RunTracer(String filename, JScrollPane p){
		this.filename = filename;
		this.p = p;
	}
	
	public void parseJSON(){
		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));
			JSONObject oo = new JSONObject(r.readLine());
			int lowest = 99999;
			for (String s : JSONObject.getNames(oo)){
				System.out.println("Names : " + s);
				lowest = Math.min(lowest, Integer.parseInt(s));
			}
			System.out.println("Lowest : " + lowest);
			for (String s : JSONObject.getNames(oo.getJSONObject("" + lowest))){
				System.out.println("SubNames : " + s);
			}
			JSONArray a = oo.getJSONObject("" + lowest).getJSONArray("History");
			for (int i = 0; i < a.length(); i++){
				System.out.println("SubSubNames : " + a.getJSONObject(i).toString());
			}
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
