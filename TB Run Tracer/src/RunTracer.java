import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jLOAF.casebase.CaseBase;
import org.jLOAF.casebase.CaseRun;
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
		s.parseFile("F:/Batch Test 3/TB/Expert/Run 1\\SmartRandomAgent", "TestCasebase");
		
		LeaveOneOut l = LeaveOneOut.loadTrainAndTest("TestCasebase.cb", 1000, 7);
		List<TestingTrainingPair> loo = l.getTestingAndTrainingSets();
		
		JDesktopPane label = new JDesktopPane();
		label.setLayout(new BoxLayout(label, BoxLayout.Y_AXIS));
		label.add(new RunModel(loo.get(0).getTesting(), label));
		
		JScrollPane p = new JScrollPane(label, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.add(p);
		
		RunTracer r = new RunTracer("C:/Users/Caleb/git/jLOAF-Sandbox-Agent/testSuiteJSON.json",label);
		r.parseJSON(loo.get(0).getTraining(), frame);
		
		label.updateUI();

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	class KA extends KeyAdapter{
		private ArrayList<CandidateRunModel> model;
		private JFrame f;
		public KA(ArrayList<CandidateRunModel> m, JFrame f){
			this.model = m;
			this.f = f;
		}
		
		public void keyPressed(KeyEvent e){
			for (CandidateRunModel c : model){
				if (e.getKeyCode() == KeyEvent.VK_LEFT){
					c.decreaseTime();
					f.repaint();
				}else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
					c.increaseTime();
					f.repaint();
				}
			}
//			System.out.println("Time : " + model.get(0).getCurrentTime());
		}
	}
	
	private String filename;
	private JDesktopPane p;
	
	private ArrayList<CandidateRunModel> runModel;
	
	public RunTracer(String filename, JDesktopPane p){
		this.filename = filename;
		this.p = p;
	}
	
	public void parseJSON(CaseBase cb, JFrame f){
		runModel = new ArrayList<CandidateRunModel>();
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));
			JSONObject oo = new JSONObject(r.readLine());
			HashMap<String, CandidateRunModel> models = new HashMap<String, CandidateRunModel>();
			for (CaseRun cr : cb.convertCaseBaseToRuns()){
				CandidateRunModel m = new CandidateRunModel(cr, p, cb);
				runModel.add(m);
				models.put(cr.getRunName(), m);
			}
			
			f.addKeyListener(new KA(runModel, f));
			
			for (String s : JSONObject.getNames(oo)){
//				System.out.println("Names : " + s);
				JSONArray history = oo.getJSONObject(s).getJSONArray("History");
				for (int i = 0; i < history.length(); i++){
					String candidiateName = history.getJSONObject(i).getString("Name");
					if (candidiateName.equals("Current Run")){
						System.out.println("Current Run at : " + s + " i : " + i);
						continue;
					}
					try{
						models.get(candidiateName).addHistory(s, history.getJSONObject(i));
					}catch(NullPointerException e){
						System.out.println("Error : " + candidiateName + " " + history.getJSONObject(i).toString());
					}
				}
			}
			for (String s : models.keySet()){
				if (models.get(s).getHistorySize() > 0){
					p.add(models.get(s));
				}
			}
			
			r.close();
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
