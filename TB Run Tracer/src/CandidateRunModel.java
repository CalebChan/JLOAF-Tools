import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JDesktopPane;

import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseBase;
import org.jLOAF.casebase.CaseRun;
import org.json.JSONObject;


public class CandidateRunModel extends RunModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, ArrayList<JSONObject>> historyMap;
	
	private String name;
	
	private int currentTime;
	
	private Set<CaseRun> caseRuns;
	
	public CandidateRunModel(CaseRun c, JDesktopPane parent, CaseBase cb) {
		super(c, parent);
		this.name = c.getRunName();
		this.currentTime = 0;
		this.caseRuns = cb.convertCaseBaseToRuns();
		this.historyMap = new HashMap<String, ArrayList<JSONObject>>();
	}
	
	public void increaseTime(){
		currentTime++;
		currentTime = Math.min(999, currentTime);
	}
	
	public int getCurrentTime(){
		return currentTime;
	}
	
	public void decreaseTime(){
		currentTime--;
		currentTime = Math.max(0, currentTime);
	}
	
	public int getHistorySize(){
		return this.historyMap.size();
	}
	
	public void addHistory(String time, JSONObject info){
		if (!info.getString("Name").equals(name)){
			throw new RuntimeException("Incorrect name");
		}
		if (!this.historyMap.containsKey(time)){
			this.historyMap.put(time, new ArrayList<JSONObject>());
		}
		this.historyMap.get(time).add(info);
	}
	
	protected void draw(Graphics2D g2, float yOffset){
		if (this.currentTime == -1 || !this.historyMap.containsKey("" + this.currentTime)){
			return;
		}
		
		ArrayList<JSONObject> a = this.historyMap.get("" + this.currentTime);
		
//		System.out.println("Run : " + this.name + " Length : " + a.length());
//		System.out.println("C : " + this.currentTime + " A : " + a.length() + " T : " + (this.currentTime - a.length() - 1));
		int runSize = getRunSize(a);
//		System.out.println("Current Time : " + this.currentTime + " Size : " + runSize);
		for (int i = 0; i < runSize; i++){
			int timeIndex = (this.currentTime - runSize + 1 + i) ;
			int x = (timeIndex) * 30;
			if (hasState(a, runSize - 1 - i)){
				g2.setColor(Color.BLACK);
				if (index == timeIndex){
					g2.setColor(Color.GREEN);
				}
				g2.fillOval(12 + x, 12, 14, 14);
			}
			g2.drawOval(12 + x, 12, 14, 14);
			
			g2.setColor(Color.BLACK);
			if (hasAction(a, runSize - 1 - i)){
				if (index - 0.5 == timeIndex){
					g2.setColor(Color.CYAN);
					g2.fillRect(30 + x, 16, 6, 6);
				}else {
					g2.drawRect(30 + x, 16, 6, 6);
				}
			}
			
			g2.drawRect(30 + x, 16, 6, 6);
		}
	}
	
	public boolean hasState(ArrayList<JSONObject> a, int index){
		if (index == 0){
			return true;
		}
		
		for (JSONObject o : a){
			if (o.getInt("Time") == index && o.getString("R Type").equals("S")){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAction(ArrayList<JSONObject> a, int index){
		if (index == 0){
			return true;
		}
		for (JSONObject o : a){
			if (o.getInt("Time") == index && o.getString("R Type").equals("A")){
				return true;
			}
		}
		return false;
	}
	
	public int getRunSize(ArrayList<JSONObject> a){
		int highest = 0;
		for (JSONObject o : a){
			if (o.has("Time")){
				highest = Math.max(highest, o.getInt("Time"));
			}
		}
		return highest + 1;
	}
	
	protected CustomDialog createDialog(){
		CandidiateCustomDialog d = new CandidiateCustomDialog();
		d.pack();
		return d;
	}
	
	public Case findCase(int location, boolean isState){
		ArrayList<JSONObject> a = this.historyMap.get("" + this.currentTime);
		String type = (isState) ? "S" : "A";
		for (JSONObject o : a){
			if ((o.getInt("Time") == location && o.getString("R Type").equals(type)) || ((o.getInt("Time") == 0 && location == 0 && !isState))){
				String runName = o.getString("P Name");
				int caseNo = o.getInt("Case No");
				for (CaseRun r : caseRuns){
					if (r.getRunName().equals(runName)){
						System.out.println("Case No : " + caseNo + " Run Name : " + runName + " Location : " + location);
						return r.getCase(caseNo);
					}
				}
			}
		}
		return null;
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && index != -1){
			Case c = null;
			int intIndex = (int)Math.floor(index);
			System.out.println("Current : " + this.currentTime + " index : " + index + " intIndex : " + intIndex + " Offset : " + (this.currentTime - intIndex));
			if (this.run != null){
//				c = this.run.getCase((int) Math.floor(index));
				c = findCase(this.currentTime - intIndex, intIndex == index);
			}
			
			if (c == null){
				System.out.println("Case is Null");
			}
			if (this.currentTime - intIndex >= 0 && intIndex <= this.currentTime){
				((CandidiateCustomDialog)d).setText(c, this.historyMap.get("" + (this.currentTime)), this.currentTime - intIndex, intIndex == index);
				d.setLocation(e.getX(), e.getY());
				d.setVisible(true);
				repaint();
			}
			
		}
	}
	
	class CandidiateCustomDialog extends CustomDialog{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public void setText(Case c, ArrayList<JSONObject> a, int offset, boolean isState){
			super.setText(c);
			
			if (a == null){
				System.out.println("Offset : " + offset + " isState : " + isState);
				return;
			}
			String s = this.getText() + "\n";
			for (JSONObject o : a){
				if (isState && o.getInt("Time") == offset && o.getString("R Type").equals("S")){
					s += "Sim State : " + o.getDouble("Sim");
				}else if (!isState && o.getInt("Time") == offset && o.getString("R Type").equals("A")){
					s += "Sim Action : " + o.getDouble("Sim");
				}
			}
			super.setText(s);
			// Add stuff on top
		}
	}

}
