import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jLOAF.action.Action;
import org.jLOAF.action.AtomicAction;
import org.jLOAF.action.ComplexAction;
import org.jLOAF.agent.RunAgent;
import org.jLOAF.casebase.AtomicCase;
import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseBase;
import org.jLOAF.casebase.ComplexCase;
import org.jLOAF.inputs.AtomicInput;
import org.jLOAF.inputs.ComplexInput;
import org.jLOAF.inputs.Feature;
import org.jLOAF.inputs.Input;
import org.jLOAF.reasoning.BacktrackingReasoning;
import org.jLOAF.reasoning.BestRunReasoning;
import org.jLOAF.reasoning.EditDistanceReasoning;
import org.jLOAF.reasoning.JaccardDistanceReasoning;
import org.jLOAF.reasoning.SequentialReasoning;
import org.jLOAF.sim.atomic.ActionEquality;
import org.jLOAF.sim.complex.ActionMean;
import org.jLOAF.sim.complex.InputMean;
import org.jLOAF.tools.LeaveOneOut;
import org.jLOAF.tools.TestingTrainingPair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sandbox.BoxObstacle;
import sandbox.Direction;
import sandbox.Environment;
import sandbox.MovementAction;
import sandbox.sensor.Sensor;
import agent.AbstractSandboxAgent;
import agent.SandboxAction;
import agent.backtracking.SandboxFeatureInput;
import agent.backtracking.SandboxSimilarity;
import agent.lfo.DirtBasedAgentSenseConfig;
import agent.lfo.expert.SmartStraightLineExpert;
import agent.lfo.expert.ZigZagExpert;


public class DiscreteRandomAgentRunTool {
	
	enum Command{
		ALL,
		SUMMARY,
		FIND,
		TEST,
		TRACK,
		GUESS,
		ACTION,
		LINE,
		WALL,
		SET,
		EXIT,
		HELP,
		;
		public static Command stringToCommand(String str){
			for (Command c : Command.values()){
				if (str.equals(c.name().toLowerCase())){
					return c;
				}
			}
			return null;
		}
	}
	
	enum Reasoning{
		KNN,
		BEST,
		SEQ,
		JACCARD,
		EDIT,
		;
		
		public static Reasoning stringToReasoning(String str){
			for (Reasoning c : Reasoning.values()){
				if (str.equals(c.name().toLowerCase())){
					return c;
				}
			}
			return null;
		}
	}
	
	public static final String MAP_LOCATION[] = {
		RunTool.MAP_DIR + "discreet-8x8.xml",
		RunTool.MAP_DIR + "discreet-8x8-2.xml",
		RunTool.MAP_DIR + "discreet-8x8-3.xml",
		RunTool.MAP_DIR + "discreet-8x8-4.xml",
		RunTool.MAP_DIR + "discreet-8x8-5.xml",
		RunTool.MAP_DIR + "discreet-32x32.xml",
		RunTool.MAP_DIR + "discreet-32x32-2.xml"};
	
	public static final double DEFAULT_THRESHOLD = 0.99;
	public static final double DEFAULT_EQUAL_THRESHOLD = 0.99;
	
	public static final String DEFAULT_WILDCARD = "*";

	private String traceFolder;
	private int foldNumber;
	private int runSize;
	private List<TestingTrainingPair> pair;
	
	private InputActionHashMap inputActionMap;
	
	private InputActionHashMap testingActionMapList[];
	
	private FailPointList failList;
	
	private int total;
	
	private boolean useMap;
	private AbstractSandboxAgent expert;
	
	public DiscreteRandomAgentRunTool(String traceFolder, int foldNumber, int runSize){
		this.traceFolder = traceFolder;
		this.foldNumber = foldNumber;
		this.runSize = runSize;
		
		this.inputActionMap = new InputActionHashMap();
		this.failList = new FailPointList();
		
		this.useMap = false;
		this.expert = RunTool.expert.getAgent(0, 0, 0, Direction.NORTH);
	}
	public void setup(){
		LeaveOneOut out = LeaveOneOut.loadTrainAndTest(this.traceFolder, runSize, foldNumber);
		pair = out.getTestingAndTrainingSets();
		
//		AtomicAction.setClassStrategy(new ActionEquality());
//		ComplexAction.setClassStrategy(new ActionMean());
//		
//		AtomicInput.setClassStrategy(new InputEquality());;
//		ComplexInput.setClassStrategy(new InputMean());
		
		ComplexInput.setClassStrategy(new InputMean());
		//AtomicInput.setClassStrategy(new InputEquality());
		SandboxFeatureInput.setClassSimilarityMetric(new SandboxSimilarity());
		//SandboxFeatureInput.setClassSimilarityMetric(new SandboxSequenceSimilarity());
		AtomicAction.setClassStrategy(new ActionEquality());
		ComplexAction.setClassStrategy(new ActionMean());
		
		this.testingActionMapList = new InputActionHashMap[pair.size()];
	}
	
	public void runTool(){
		int index = 0;
		for (TestingTrainingPair ttp : pair){
			ComplexCase r = ttp.getTesting();
			List<Case> cases = r.toArrayList();
			for (int i = 0; i < cases.size(); i++){
				Case c = cases.get(i);
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
		ComplexCase run = pair.get(runNum).getTesting();
		InputActionHashMap map = new InputActionHashMap();
		List<Case> cases = run.toArrayList();
		for (int i = 0; i < cases.size(); i++){
			Case c = cases.get(i);
			map.put(c.getInput(), c.getAction());
		}
		this.testingActionMapList[runNum] = map;
	}
	
	public void displayToolSummary(){
		String msg = "";
		Scanner s = new Scanner(System.in);
		helpFunction();
		while(!msg.equals(Command.EXIT.name().toLowerCase())){
			try{
				msg = s.nextLine();
				String tokens[] = msg.split(" ");
				switch(Command.stringToCommand(tokens[0])){
				case EXIT:
					break;
				case HELP:
					helpFunction();
					break;
				case SUMMARY:
				{
					int index = 0;
					if (tokens.length > 2){
						index = Integer.parseInt(tokens[2]);
					}
					InputActionHashMap map = (tokens[1].equals("train")) ? inputActionMap : testingActionMapList[index];
					int length = (tokens[1].equals("train")) ? total : pair.get(index).getTesting().getComplexCaseSize();
					summary(map, length);
					break;
				}
				case ALL:
				{
					int index = 0;
					if (tokens.length > 2){
						index = Integer.parseInt(tokens[2]);
					}
					InputActionHashMap map = (tokens[1].equals("train")) ? inputActionMap : testingActionMapList[index];
					int length = (tokens[1].equals("train")) ? total : pair.get(index).getTesting().getComplexCaseSize();
					all(map, length);
					break;
				}
				case FIND:
				{
					String searchStr = "";
					for (int i = 1; i < tokens.length; i++){
						searchStr += tokens[i] + " ";
					}
					find(searchStr.trim());
					break;
				}
				case ACTION:
				{
					String searchStr = "";
					for (int i = 1; i < tokens.length; i++){
						searchStr += tokens[i] + " ";
					}
					findAction(searchStr.trim());
					break;
				}
				case TEST:
				{
					double threshold = -1;
					if (tokens.length >= 3){
						threshold = Double.parseDouble(tokens[2]);
					}
					testAll(tokens[1], threshold);
					break;
				}
				case TRACK:
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
				case SET:
				{
					if (tokens.length != 3){
						break;
					}
					if (tokens[1].equals("map")){
						this.useMap = Boolean.parseBoolean(tokens[2]);
					}
					break;
				}
				case LINE:
				{
					countLine();
					break;
				}
				case WALL:
				{
					countWall();
					break;
				}
				case GUESS:
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
		s.reset();
	}

	private void countLine(){
		int total = 0;
		int count = 0;
		double currentAction = -1;
		for (TestingTrainingPair p : pair){
			for (ComplexCase c : p.getTraining().getRuns()){
				currentAction = -1;
				ArrayList<Case> run = new ArrayList<Case>();
				run.add(c.getCurrentCase());
				run.addAll(c.getPastCases());
				for (int i = run.size() - 1; i >= 0; i--){
					total++;
					double action = ((SandboxAction)run.get(i).getAction()).getFeature().getValue();
					if (currentAction == -1){
						currentAction = action;
					}else if (currentAction != -1 && currentAction != action){
						count++;
						currentAction = action;
					}
				}
				count++;
			}
		}
		System.out.println("Average length of line : " + (total * 1.0 / (count * 1.0)));	
	}
	
	private void countWall(){
		int total = 0;
		for (TestingTrainingPair p : pair){
			for (Case c : p.getTraining().getCases()){
				if (c.getInput() instanceof ComplexInput){
					ComplexInput cIn = (ComplexInput)c.getInput();
					for (Direction d : Direction.values()){
						AtomicInput ait = (AtomicInput)cIn.get(d.name() + DirtBasedAgentSenseConfig.TYPE_SUFFIX);
						AtomicInput aid = (AtomicInput)cIn.get(d.name() + DirtBasedAgentSenseConfig.DISTANCE_SUFFIX);
						if (ait.getFeature().getValue() == Environment.WALL && aid.getFeature().getValue() == Environment.CLOSE){
							total++;
						}
					}
				}
			}
		}
		System.out.println("Average walls per test set : " + (total * 1.0 / (pair.size() * 1.0)));	
	}
	
	private void helpFunction(){
		System.out.println("Help Commands : ");
		System.out.println("\t all       (train | test testNo)");
		System.out.println("\t summary   (train | test testNo)");
		System.out.println("\t find      searchString");
		System.out.println("\t test      (" + getReasoningString() + ") kValue");
		System.out.println("\t track     failPoint exact");
		System.out.println("\t guess");
	}
	
	private String getReasoningString(){
		String s = "";
		for (Reasoning r : Reasoning.values()){
			s += r.name().toLowerCase() + " | ";
		}
		return s.trim().substring(0, s.length() - 1);
	}
	
	public void randomGuess(){
		
		int overallTotal = 0;
		int randomTotal = 0;
		int testingTotal = 0;
		Random random = new Random(0);
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
			
			
			ComplexCase run = ttp.getTesting();
			List<Case> cases = run.toArrayList();
			for (int i = cases.size() - 1; i >= 0; i--){
				Case c = cases.get(i);
				if (tmpMap.containsKey(c.getInput())){
					int highest = -1;
					Action a = null;
					for (Action action : tmpMap.get(c.getInput()).keySet()){
						if (tmpMap.get(c.getInput()).get(action) > highest){
							highest = tmpMap.get(c.getInput()).get(action);
							a = action;
						}
					}
					if (c.getAction().similarity(a) == 1){
						testingTotal++;
					}
				}else{
					if (random.nextInt(4) == 1){
						testingTotal++;
					}
				}
			}
		}
		System.out.println("Best Action % : " + (randomTotal * 1.0 / overallTotal));
		System.out.println("Random Action % : " + (testingTotal * 1.0 / overallTotal));
	}
	
	private void findAction(String action){
		System.out.println("Finding : " + action);
		for (Input ahm : this.inputActionMap.keySet()){
			for (Action a : this.inputActionMap.get(ahm).keySet()){
				if (a.getSimpleString().trim().equals(action)){
					System.out.println(ahm.getSimpleString() + "= " + a.getSimpleString());
				}
			}
		}
	}
	
	private void find(String input){
		System.out.println("Finding : " + input);
		for (Input ahm : this.inputActionMap.keySet()){
			if (compareRelativeString(ahm.getSimpleString().trim(), input.trim())){
				for (Action action : this.inputActionMap.get(ahm).keySet()){
					int sum = this.inputActionMap.get(ahm).get(action);
					System.out.println(ahm.getSimpleString() + "= " + action.getSimpleString() + " -> " + String.format("%5d", sum) + "\t % " + String.format("%2.4f", sum / (1.0 * total) * 100));
				}
			}
		}
	}
	
	private boolean compareRelativeString(String base, String test){
		if(!test.contains(DEFAULT_WILDCARD)){
			return base.equals(test);
		}
		String baseArray[] = base.split(" ");
		String testArray[] = test.split(" ");
		if (baseArray.length != testArray.length){
			return false;
		}
		for (int i = 0; i < baseArray.length; i++){
			if (!testArray[i].equals(DEFAULT_WILDCARD)){
				if (!baseArray[i].equals(testArray[i])){
					return false;
				}
			}
		}
		return true;
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

	private HashMap<Integer, Integer> testReasoning(String reason, int test, double threshold){
		double reasoningThreshold = DEFAULT_THRESHOLD;
		if (threshold >= -1){
			reasoningThreshold = threshold;
		}
		BacktrackingReasoning reasoning = null;
		switch (Reasoning.stringToReasoning(reason)){
		case SEQ:
			reasoning = new SequentialReasoning(pair.get(test).getTraining(), reasoningThreshold, null);
			break;	
		case BEST:
			reasoning = new BestRunReasoning(pair.get(test).getTraining(), reasoningThreshold);
			break;
		case JACCARD:
			reasoning = new JaccardDistanceReasoning(pair.get(test).getTraining(), reasoningThreshold, DEFAULT_EQUAL_THRESHOLD);
			break;
		case EDIT:
			reasoning = new EditDistanceReasoning(pair.get(test).getTraining(), reasoningThreshold);
			break;
		default:
			System.out.println("Failed to test");
			return new HashMap<Integer, Integer>();
		}
		HashMap<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
		
		RunAgent agent = new RunAgent(reasoning, pair.get(test).getTraining());
		reasoning.setCurrentRun(agent.getCurrentRun());
		
		Environment e = buildEnvironment(MAP_LOCATION[test], this.expert);
		
		ComplexCase testRun = pair.get(test).getTesting();
		List<Case> cases = testRun.toArrayList();
		for (int i = cases.size() - 1; i >= 0; i--){
			Case c = null;
			if (!this.useMap){
				c = cases.get(i);
			}else{
				e.updateSensor(this.expert.getCreature());
				Sensor s = this.expert.getCreature().getSensor();
				Input input = convertSensorToInput(s);
				MovementAction correctMovementAction = this.expert.testAction(this.expert.getCreature());
				Action correctAction = new SandboxAction(correctMovementAction);
				c = new AtomicCase(input, correctAction);
			}
			
			Action a = agent.senseEnvironment(c.getInput());
//			if (!a.equals(c.getAction())){
			
			if (a.similarity(c.getAction()) != 1){
				this.failList.traceFailPoint(agent.getCurrentRun().getComplexCaseSize(), a, c.getAction());
				if (errorMap.containsKey(agent.getCurrentRun().getComplexCaseSize())){
					errorMap.put(agent.getCurrentRun().getComplexCaseSize(), errorMap.get(agent.getCurrentRun().getComplexCaseSize()) + 1);
				}else{
					errorMap.put(agent.getCurrentRun().getComplexCaseSize(), 1);
				}
				Case amendCase = new AtomicCase(c.getInput(), c.getAction());
				if (!this.useMap){
					agent.learn(amendCase);
				}else{
					SandboxAction sandboxAction = (SandboxAction)a;
					e.makeMove(MovementAction.values()[(int) sandboxAction.getFeature().getValue()], this.expert.getCreature());
					if (this.expert instanceof SmartStraightLineExpert){
						SmartStraightLineExpert ee = (SmartStraightLineExpert)this.expert;
						ee.resetDirection(Direction.convertActToDir(MovementAction.values()[(int) sandboxAction.getFeature().getValue()]));
					}else if (this.expert instanceof ZigZagExpert){
						ZigZagExpert ee = (ZigZagExpert)this.expert;
						ee.resetDirection(Direction.convertActToDir(MovementAction.values()[(int) sandboxAction.getFeature().getValue()]));
					}
				}
				
			}
			
		}
		return errorMap;
	}
	
	private Input convertSensorToInput(Sensor s){
		if (s.getSenseKeys().size() == 1){
			Input input = null;
			for (String key : s.getSenseKeys()){
				int value = (int) s.getSense(key).getValue();
				input = new SandboxFeatureInput(key, new Feature(value * 1.0));
			}
			return input;
		}else{
			ComplexInput input = new ComplexInput(common.Config.COMPLEX_INPUT_NAME);
			for (String key : s.getSenseKeys()){
				int value = (int) s.getSense(key).getValue();
				input.add(new SandboxFeatureInput(key, new Feature(value * 1.0)));
			}
			return input;
		}
	}
	
	private void testAll(String reason, double threshold){
		HashMap<Integer, Integer> errorMap = new HashMap<Integer, Integer>();
		this.failList = new FailPointList();
		for (int test = 0; test < pair.size(); test++){
			HashMap<Integer, Integer> tmpMap = testReasoning(reason, test, threshold);
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
	
	private Environment buildSandbox(Element element, AbstractSandboxAgent agent){
		int x = Integer.parseInt(element.getAttribute("dx"));
		int y = Integer.parseInt(element.getAttribute("dy"));
		Environment sandbox = new Environment(x, y);
		agent.setEnvironment(sandbox);
		return sandbox;
	}
	
	private void buildObjects(Element element, Environment sandbox, AbstractSandboxAgent agent){
		NodeList nl = element.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++){
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element obj = (Element) nl.item(i);
				String objType = obj.getNodeName();
				int x = (int) Math.floor(Double.parseDouble(obj.getElementsByTagName("x").item(0).getTextContent()));
				int y = (int) Math.floor(Double.parseDouble(obj.getElementsByTagName("y").item(0).getTextContent()));
				if (objType.equals("dirt")){
					sandbox.addDirt(x, y);
					continue;
				}else if (objType.equals("vacuum")){
					agent.getCreature().moveCreature(x, y, Direction.NORTH);
					continue;
				}
				NodeList shapeList = obj.getElementsByTagName("shape");
				for (int j = 0; j < shapeList.getLength(); j++){
					if (shapeList.item(j).getNodeType() == Node.ELEMENT_NODE){
						Element shape = (Element) shapeList.item(j).getChildNodes().item(0);
						int dx = (int) Math.floor(Double.parseDouble(shape.getAttribute("dx"))) / 2;
						int dy = (int) Math.floor(Double.parseDouble(shape.getAttribute("dx"))) / 2;
						sandbox.addObstacle(new BoxObstacle(x, y, dx, dy));
					}
				}
			}
		}
	}
	
	public Environment buildEnvironment(String filename, AbstractSandboxAgent agent){
//		System.out.println("File name : " + filename);
		Environment environment = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = (Document) dBuilder.parse(new File(filename));
			doc.getDocumentElement().normalize();
			NodeList e = doc.getChildNodes().item(0).getChildNodes();
			for (int i = 0; i < e.getLength(); i++){
				if (e.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) e.item(i);
					switch (element.getNodeName()){
					case "map":
						environment = buildSandbox(element, agent);
						break;
					case "objects":
						buildObjects(element, environment, agent);
						break;
					default:
						break;
					}
					
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return environment;
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
