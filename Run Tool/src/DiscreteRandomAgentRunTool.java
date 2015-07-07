import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jLOAF.action.Action;
import org.jLOAF.action.AtomicAction;
import org.jLOAF.action.ComplexAction;
import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseRun;
import org.jLOAF.inputs.AtomicInput;
import org.jLOAF.inputs.ComplexInput;
import org.jLOAF.inputs.Input;
import org.jLOAF.sim.atomic.ActionEquality;
import org.jLOAF.sim.atomic.InputEquality;
import org.jLOAF.sim.complex.ActionMean;
import org.jLOAF.sim.complex.InputMean;
import org.jLOAF.tools.LeaveOneOut;
import org.jLOAF.tools.TestingTrainingPair;


public class DiscreteRandomAgentRunTool {

	private String traceFolder;
	private int foldNumber;
	private int runSize;
	private List<TestingTrainingPair> pair;
	
	private InputActionHashMap inputActionMap;
	
	private static final String EXIT_STRING = "exit";
	private static final String HELP_STRING = "help";
	
	private int total;
	
	public DiscreteRandomAgentRunTool(String traceFolder, int foldNumber, int runSize){
		this.traceFolder = traceFolder;
		this.foldNumber = foldNumber;
		this.runSize = runSize;
		
		this.inputActionMap = new InputActionHashMap();
	}
	
	public void setup(){
		LeaveOneOut out = LeaveOneOut.loadTrainAndTest(this.traceFolder, runSize, foldNumber);
		pair = out.getTestingAndTrainingSets();
		
		AtomicAction.setClassStrategy(new ActionEquality());
		ComplexAction.setClassStrategy(new ActionMean());
		
		AtomicInput.setClassStrategy(new InputEquality());;
		ComplexInput.setClassStrategy(new InputMean());
	}
	
	public void runTool(){
		for (TestingTrainingPair ttp : pair){
			CaseRun r = ttp.getTesting();
			for (int i = 0; i < r.getRunLength(); i++){
				Case c = r.getCasePastOffset(i);
				this.inputActionMap.put(c.getInput(), c.getAction());
			}
		}
		
		for (Input ahm : this.inputActionMap.keySet()){
			total += this.inputActionMap.getInputSummaryTotal(ahm);
		}
	}
	
	public void displayToolSummary(){
		String msg = "";
		Scanner s = new Scanner(System.in);
		helpFunction();
		while(!msg.equals(EXIT_STRING)){
			msg = s.nextLine();
			String tokens[] = msg.split("-");
			switch(tokens[0]){
			case EXIT_STRING:
				break;
			case HELP_STRING:
				helpFunction();
				break;
			case "summary":
				summary();
				break;
			case "all":
				all();
				break;
			case "find":
				find(tokens[1]);
				break;
			default:
				helpFunction();
				break;
			}
		}
		s.close();
	}
	
	private void find(String input){
		System.out.println("Finding : " + input);
		for (Input ahm : this.inputActionMap.keySet()){
			if (ahm.getSimpleString().trim().equals(input.trim())){
				for (Action action : this.inputActionMap.get(ahm).keySet()){
					int sum = this.inputActionMap.get(ahm).get(action);
					System.out.println(ahm.getSimpleString() + "= " + action.getSimpleString() + " -> " + String.format("%5d", sum) + "\t % " + String.format("%2.4f", sum / (1.0 * total) * 100));
				}
			}
		}
	}
	
	private void summary(){
		System.out.println("Entry : " + this.inputActionMap.size());
		for (Input ahm : this.inputActionMap.keySet()){
			int sum = this.inputActionMap.getInputSummaryTotal(ahm);
			int depth = this.inputActionMap.get(ahm).size();
			System.out.println(ahm.getSimpleString() + " -> " + String.format("%5d", sum)+ "\t" + depth + "\t % " + String.format("%2.4f", sum / (1.0 * total) * 100));
		}
	}
	
	private void all(){
		for (Input ahm : this.inputActionMap.keySet()){
			for (Action action : this.inputActionMap.get(ahm).keySet()){
				int sum = this.inputActionMap.get(ahm).get(action);
				
				System.out.println(ahm.getSimpleString() + "= " + action.getSimpleString() + " -> " + String.format("%5d", sum) + "\t % " + String.format("%2.2f", sum / (1.0 * total) * 100));
			}
		}
	}
	
	private void helpFunction(){
		System.out.println("Help");
	}
	
	class ActionHashMap extends HashMap<Action, Integer>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Integer put(Action a){
			for (Action action : this.keySet()){
				if (action.similarity(a) == 1){	
					return this.put(action, this.get(action) + 1);
				}
			}
			return this.put(a, 1);
		}
	}
	
	class InputActionHashMap extends HashMap<Input, ActionHashMap>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ActionHashMap put(Input i, Action a){
			for (Input input : this.keySet()){
				if (input.similarity(i) == 1){
					this.get(input).put(a);
					return this.get(input);
				}
			}
			ActionHashMap map = new ActionHashMap();
			map.put(a);
			return this.put(i, map);
		}
		
		public int getInputSummaryTotal(Input i){
			ActionHashMap map = this.get(i);
			if (map == null){
				return -1;
			}
			int total = 0;
			for (Action a : map.keySet()){
				total += map.get(a);
			}
			return total;
		}
	}
}
