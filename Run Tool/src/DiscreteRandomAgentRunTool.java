import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jLOAF.action.Action;
import org.jLOAF.action.AtomicAction;
import org.jLOAF.action.ComplexAction;
import org.jLOAF.agent.RunAgent;
import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseBase;
import org.jLOAF.casebase.CaseRun;
import org.jLOAF.inputs.AtomicInput;
import org.jLOAF.inputs.ComplexInput;
import org.jLOAF.inputs.Input;
import org.jLOAF.reasoning.BacktrackingReasoning;
import org.jLOAF.reasoning.BestRunReasoning;
import org.jLOAF.reasoning.KNNBacktracking;
import org.jLOAF.reasoning.SequentialReasoning;
import org.jLOAF.retrieve.kNNUtil;
import org.jLOAF.retrieve.sequence.weight.LinearWeightFunction;
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
	
	private InputActionHashMap testingActionMapList[];
	
	private FailPointList failList;
	
	private static final String EXIT_STRING = "exit";
	private static final String HELP_STRING = "help";
	
	
	private int total;
	
	public DiscreteRandomAgentRunTool(String traceFolder, int foldNumber, int runSize){
		this.traceFolder = traceFolder;
		this.foldNumber = foldNumber;
		this.runSize = runSize;
		
		this.inputActionMap = new InputActionHashMap();
		this.failList = new FailPointList();
	}
	
	public void setup(){
		LeaveOneOut out = LeaveOneOut.loadTrainAndTest(this.traceFolder, runSize, foldNumber);
		pair = out.getTestingAndTrainingSets();
		
		AtomicAction.setClassStrategy(new ActionEquality());
		ComplexAction.setClassStrategy(new ActionMean());
		
		AtomicInput.setClassStrategy(new InputEquality());;
		ComplexInput.setClassStrategy(new InputMean());
		
		kNNUtil.setWeightFunction(new LinearWeightFunction(0.5));
		
		this.testingActionMapList = new InputActionHashMap[pair.size()];
	}
	
	public void runTool(){
		int index = 0;
		for (TestingTrainingPair ttp : pair){
			CaseRun r = ttp.getTesting();
			for (int i = 0; i < r.getRunLength(); i++){
				Case c = r.getCasePastOffset(i);
				this.inputActionMap.put(c.getInput(), c.getAction());
			}
			buildTestingList(index);
			index++;
		}
		
		for (Input ahm : this.inputActionMap.keySet()){
			total += this.inputActionMap.getInputSummaryTotal(ahm);
		}
	}
	
	private void buildTestingList(int runNum){
		CaseRun run = pair.get(runNum).getTesting();
		InputActionHashMap map = new InputActionHashMap();
		for (int i = 0; i < run.getRunLength(); i++){
			Case c = run.getCasePastOffset(i);
			map.put(c.getInput(), c.getAction());
		}
		this.testingActionMapList[runNum] = map;
	}
	
	public void displayToolSummary(){
		String msg = "";
		Scanner s = new Scanner(System.in);
		helpFunction();
		while(!msg.equals(EXIT_STRING)){
			try{
				msg = s.nextLine();
				String tokens[] = msg.split(" ");
				switch(tokens[0]){
				case EXIT_STRING:
					break;
				case HELP_STRING:
					helpFunction();
					break;
				case "summary":
				{
					int index = 0;
					if (tokens.length > 2){
						index = Integer.parseInt(tokens[2]);
					}
					InputActionHashMap map = (tokens[1].equals("train")) ? inputActionMap : testingActionMapList[index];
					int length = (tokens[1].equals("train")) ? total : pair.get(index).getTesting().getRunLength();
					summary(map, length);
					break;
				}
				case "all":
				{
					int index = 0;
					if (tokens.length > 2){
						index = Integer.parseInt(tokens[2]);
					}
					InputActionHashMap map = (tokens[1].equals("train")) ? inputActionMap : testingActionMapList[index];
					int length = (tokens[1].equals("train")) ? total : pair.get(index).getTesting().getRunLength();
					all(map, length);
					break;
				}
				case "find":
				{
					String searchStr = "";
					for (int i = 1; i < tokens.length; i++){
						searchStr += tokens[i] + " ";
					}
					find(searchStr.trim());
					break;
				}
				case "test":
				{
					int k = Integer.parseInt(tokens[2]);
					testAll(tokens[1], k);
					break;
				}
				case "track":
				{
					int failPoint = 7;
					boolean exact = false;
					if (tokens.length == 3){
						if (tokens[2].equals("e")){
							exact = true;
						}
						failPoint = Integer.parseInt(tokens[1]);
					}
					trackFailPoint(failPoint, exact);
					break;
				}
				case "guess":
				{
					randomGuess();
					break;
				}
				default:
					helpFunction();
					break;
				}
			}catch(Exception e){
				helpFunction();
			}
		}
		s.close();
	}
	private void helpFunction(){
		System.out.println("Help Commands : ");
		System.out.println("\t all       (train | test testNo)");
		System.out.println("\t summary   (train | test testNo)");
		System.out.println("\t find      searchString");
		System.out.println("\t test      (best | knn | seq) kValue");
		System.out.println("\t track     failPoint exact");
		System.out.println("\t guess");
	}
	
	public void randomGuess(){
		
		int overallTotal = 0;
		int randomTotal = 0;
		for (TestingTrainingPair ttp : pair){
			InputActionHashMap tmpMap = new InputActionHashMap();
			CaseBase r = ttp.getTraining();
			for (Case c : r.getCases()){
				tmpMap.put(c.getInput(), c.getAction());
				overallTotal++;
			}
			for (Input ahm : tmpMap.keySet()){
				int subTotal = 0;
				for (Action action : tmpMap.get(ahm).keySet()){
					if (tmpMap.get(ahm).get(action) > subTotal){
						subTotal = tmpMap.get(ahm).get(action);
					}
				}
				randomTotal += subTotal;
			}
		}
		System.out.println("Random % : " + (randomTotal * 1.0 / overallTotal));
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
	
	private void summary(InputActionHashMap map, int total){
		System.out.println("Total : " + total);
		for (Input ahm : map.keySet()){
			int sum = map.getInputSummaryTotal(ahm);
			int depth = map.get(ahm).size();
			System.out.println(ahm.getSimpleString() + " -> " + String.format("%5d", sum)+ "\t" + depth + "\t % " + String.format("%2.4f", sum / (1.0 * total) * 100));
		}
	}
	
	private void all(InputActionHashMap map, int total){
		for (Input ahm : map.keySet()){
			for (Action action : map.get(ahm).keySet()){
				int sum = map.get(ahm).get(action);
				
				System.out.println(ahm.getSimpleString() + "= " + action.getSimpleString() + " -> " + String.format("%5d", sum) + "\t % " + String.format("%2.2f", sum / (1.0 * total) * 100));
			}
		}
	}
	
	private void trackFailPoint(int failPoint, boolean exact){
		ArrayList<Integer> points = new ArrayList<Integer>(this.failList.keySet());
		Collections.sort(points);
		
		for(int i : points){
			if (!exact && this.failList.get(i).size() < failPoint){
				continue;
			}else if (exact && this.failList.get(i).size() != failPoint){
				continue;
			}
			ArrayList<ActionPair> actions = this.failList.get(i);
			System.out.print("" + i + "\t");
			for (ActionPair a : actions){
				System.out.print("" + a + " ");
			}
			System.out.println();
		}
	}

	private HashMap<Integer, Integer> testReasoning(String reason, int test, int k){
		BacktrackingReasoning reasoning = null;
		if (reason.equals("knn")){
			reasoning = new KNNBacktracking(pair.get(test).getTraining(), null, k, true, true);
		}else if (reason.equals("best")){
			reasoning = new BestRunReasoning(pair.get(test).getTraining(), k, true);
		}else if (reason.equals("seq")){
			reasoning = new SequentialReasoning(pair.get(test).getTraining(), null, k, true);
		}else{
			System.out.println("Failed to test");
			return new HashMap<Integer, Integer>();
		}
		HashMap<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
		
		RunAgent agent = new RunAgent(reasoning, pair.get(test).getTraining());
		reasoning.setCurrentRun(agent.getCurrentRun());
		
		CaseRun testRun = pair.get(test).getTesting();
		for (int i = testRun.getRunLength() - 1; i >= 0; i--){
			Case c = testRun.getCasePastOffset(i);
			Action a = agent.senseEnvironment(c.getInput());
			if (!a.equals(c.getAction())){
				this.failList.traceFailPoint(agent.getCurrentRun().getRunLength(), a, c.getAction());
				if (errorMap.containsKey(agent.getCurrentRun().getRunLength())){
					errorMap.put(agent.getCurrentRun().getRunLength(), errorMap.get(agent.getCurrentRun().getRunLength()) + 1);
				}else{
					errorMap.put(agent.getCurrentRun().getRunLength(), 1);
				}
				Case amendCase = new Case(c.getInput(), c.getAction(), null);
				agent.learn(amendCase);
			}
			
		}
		return errorMap;
	}
	
	private void testAll(String reason, int k){
		HashMap<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
		this.failList = new FailPointList();
		for (int test = 0; test < pair.size(); test++){
			HashMap<Integer, Integer> tmpMap = testReasoning(reason, test, k);
			for (Integer i : tmpMap.keySet()){
				if (errorMap.containsKey(i)){
					errorMap.put(i, tmpMap.get(i) + errorMap.get(i));
				}else{
					errorMap.put(i, tmpMap.get(i));
				}
			}
		}
		int totalErrors = 0;
		int totalRight = 0;
		for (int i = 0; i < 1000; i++){
			int errors = 0;
			if (errorMap.containsKey(i)){
				errors = errorMap.get(i).intValue();
				totalRight += pair.size() - errors;
			}else{
				totalRight += pair.size();
			}
			System.out.println("" + i + " --- " + errors + " ---> " + String.format("%3.5f", errors * 1.0 / 7));
			totalErrors += errors;
		}
		System.out.println("Errors : " + String.format("%3.5f", totalErrors * 1.0 / total) + " " + totalErrors);
		System.out.println("Right : " + totalRight + " Pairs : " + pair.size());
	}
	
		
	class ActionPair{
		private Action guessAction;
		private Action actualAction;
		public Action getGuessAction() {
			return guessAction;
		}
		public void setGuessAction(Action guessAction) {
			this.guessAction = guessAction;
		}
		public Action getActualAction() {
			return actualAction;
		}
		public void setActualAction(Action actualAction) {
			this.actualAction = actualAction;
		}
		
		public String toString(){
			return guessAction.getSimpleString() + "->" + actualAction.getSimpleString();
		}
	}
	class FailPointList extends HashMap<Integer, ArrayList<ActionPair>>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void traceFailPoint(int index, Action guessAction, Action actualAction){
			if (!this.containsKey(index)){
				this.put(index, new ArrayList<ActionPair>());
			}
			ActionPair pair = new ActionPair();
			pair.setActualAction(actualAction);
			pair.setGuessAction(guessAction);
			this.get(index).add(pair);
		}
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
