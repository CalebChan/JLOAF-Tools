import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JDesktopPane;

import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseRun;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CandidateRunModel extends RunModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, JSONArray> historyMap;
	
	private String name;
	
	private int currentTime;
	
	public CandidateRunModel(CaseRun c, JDesktopPane parent) {
		super(c, parent);
		this.name = c.getRunName();
		this.currentTime = 0;
		
		this.historyMap = new HashMap<String, JSONArray>();
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
			this.historyMap.put(time, new JSONArray());
		}
		if (info.getString("Name").equals("1") && time.equals("7")){
			System.out.println("Info : " + info.toString());
		}
		this.historyMap.get(time).put(info);
	}
	
	protected void draw(Graphics2D g2, float yOffset){
		if (this.currentTime == -1 || !this.historyMap.containsKey("" + this.currentTime)){
			return;
		}
		
		JSONArray a = this.historyMap.get("" + this.currentTime);
		
//		System.out.println("Run : " + this.name + " Length : " + a.length());
//		System.out.println("C : " + this.currentTime + " A : " + a.length() + " T : " + (this.currentTime - a.length() - 1));
		int runSize = getRunSize(a);
		for (int i = 0; i < runSize; i++){
			int timeIndex = (this.currentTime - runSize + i) ;
			int x = timeIndex * 30;
			if (hasState(a, runSize - 1 - i)){
				g2.setColor(Color.BLACK);
				if (index == timeIndex){
					g2.setColor(Color.GREEN);
				}
				g2.fillOval(12 + x, 12, 14, 14);
			}
			g2.setColor(Color.BLACK);
			if (hasAction(a, runSize - 1 - i)){
				if (index - 0.5 == timeIndex){
					g2.setColor(Color.CYAN);
					g2.fillRect(30 + x, 16, 6, 6);
				}else {
					g2.drawRect(30 + x, 16, 6, 6);
				}
			}
		}
	}
	
	public boolean hasState(JSONArray a, int index){
		if (index == 0){
			return true;
		}
		for (int i = 0; i < a.length(); i++){
			if (a.getJSONObject(i).getInt("Time") == index && a.getJSONObject(i).getString("R Type").equals("S")){
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAction(JSONArray a, int index){
		if (index == 0){
			return true;
		}
		for (int i = 0; i < a.length(); i++){
			if (a.getJSONObject(i).getInt("Time") == index && a.getJSONObject(i).getString("R Type").equals("A")){
				return true;
			}
		}
		return false;
	}
	
	public int getRunSize(JSONArray a){
		int highest = 0;
		for (int i = 0; i < a.length(); i++){
			if (a.getJSONObject(i).has("Time")){
				highest = Math.max(highest, a.getJSONObject(i).getInt("Time"));
			}
		}
		
		return highest + 1;
	}
	
	protected CustomDialog createDialog(){
		CandidiateCustomDialog d = new CandidiateCustomDialog();
		d.pack();
		return d;
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && index != -1){
			Case c = null;
			int intIndex = (int)Math.floor(index);
			System.out.println("Current : " + this.currentTime + " index : " + index + " intIndex : " + intIndex + " Offset : " + (this.currentTime - intIndex - 1));
			if (this.run != null){
				c = this.run.getCase((int) Math.floor(index));
			}
			if (this.currentTime - intIndex >= 0 && intIndex < this.currentTime){
				((CandidiateCustomDialog)d).setText(c, this.historyMap.get("" + (this.currentTime)), this.currentTime - intIndex - 1, intIndex == index);
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
		
		public void setText(Case c, JSONArray a, int offset, boolean isState){
			super.setText(c);
			
			if (a == null){
				System.out.println("Offset : " + offset + " isState : " + isState);
				return;
			}
			String s = this.getText() + "\n";
			for (int i = 0; i < a.length(); i++){
				try{
					if (isState && a.getJSONObject(i).getInt("Time") == offset && a.getJSONObject(i).getString("R Type").equals("S")){
						s += "Sim : " + a.getJSONObject(i).getDouble("Sim");
					}else if (!isState && a.getJSONObject(i).getInt("Time") == offset && a.getJSONObject(i).getString("R Type").equals("A")){
						s += "Sim : " + a.getJSONObject(i).getDouble("Sim");	
					}
				}catch(JSONException e){
					System.out.println("JSON Error : " + a.getJSONObject(i).toString() + " i : " + i + " e : " + e.getLocalizedMessage());
				}
			}
			super.setText(s);
			// Add stuff on top
		}
	}

}
