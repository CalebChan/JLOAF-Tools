import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;


public class ConfusionMatrixParser {
	
	private String filename;
	private TreeMap<MatrixInfo, Map<String,Map<String,Integer>>> matrixMap;
	
	private Map<String,Map<String,Integer>> currentMatrix;
	private MatrixInfo currentInfo;
	
	private MatrixInfo globalInfo;
	
	private String[] headers;
	
	public static final String REASONING_TAG = "Reasoning Method : ";
	public static final String WEIGHT_TAG = "Weight Function : ";
	
	public static void main(String args[]){
		ConfusionMatrixParser parser = new ConfusionMatrixParser("J:/RESULTS/RESULT/Version 5/Oct18-Seq-NonRandom-NoSmartRandom.txt");
		//ConfusionMatrixParser parser = new ConfusionMatrixParser("J:/RESULTS/RESULT/Version 5/Oct18-Seq-NonRandom-SmartRandomOnly.txt");
		parser.parseFile();
		parser.trimConfusionMatrix();
		parser.outputResutls();
	}
	
	
	public ConfusionMatrixParser(String filename){
		this.filename = filename;
		Comparator<MatrixInfo> c = new Comparator<MatrixInfo>(){

			@Override
			public int compare(MatrixInfo arg0, MatrixInfo arg1) {
				return arg0.hashCode() - arg1.hashCode();
			}
			
		};
		this.matrixMap = new TreeMap<MatrixInfo, Map<String,Map<String,Integer>>>(c);
	}
	
	public void parseFile(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String line = reader.readLine();
			String method = null;
			boolean hasReadConfusion = false;
			while (line != null){
				
				if (line.contains(REASONING_TAG)){
					method = line.substring(REASONING_TAG.length(), line.length());
				}else if (line.startsWith("Random") || line.startsWith("Non Random")){
					globalInfo = new MatrixInfo();
					globalInfo.setMethod(method);
					String randomness = line.split(",")[0];
					globalInfo.setRandom((randomness.equals("Random") ? true : false));
					String iterK = line.split(",")[1];
					String k = iterK.split(":")[1].split("Iter")[0].trim();
					globalInfo.setK(Integer.parseInt(k));
				}else if (line.contains(WEIGHT_TAG)){
					String weight = line.split(":")[1].trim();
					globalInfo.setWeight(weight);
				}else if (line.startsWith("|")){
					hasReadConfusion = true;
					parseLine(line);
				}else{
					if (hasReadConfusion){
						
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getConfusionMatrixString(String agentName, Map<String,Map<String,Integer>> confusionMatrix){
		String s = "";
		String header = "|          |";
		boolean firstIndex = true; 
		HashSet<String> titles = new HashSet<String>();
		titles.addAll(confusionMatrix.keySet());
		for (String s1 : confusionMatrix.keySet()){
			titles.addAll(confusionMatrix.get(s1).keySet());
		}
		for (String s1 : titles){
			s += "|" + String.format("%-10s", s1) + "|";
			for (String s2 : titles){
				if (firstIndex){
					header += String.format("%-10s", s2) + "|";
				}
				if (!confusionMatrix.containsKey(s1) || !confusionMatrix.get(s1).containsKey(s2)){
					s += String.format("%-10d", 0) + "|";
				}else{
					s += String.format("%-10d", confusionMatrix.get(s1).get(s2)) + "|";
				}
			}
			s += "\n";
			firstIndex = false;
		}
		s = s.substring(0, s.length() - 1);
		String label = "|" + String.format("%-" + (header.length() - 2) + "s", agentName + " Confusion Matrix") + "|";
		return label + "\n" + header + "\n" + s;
	}
	
	public void trimConfusionMatrix(){
		for (MatrixInfo info : this.matrixMap.keySet()){
			Map<String,Map<String,Integer>> currentMatrix = this.matrixMap.get(info);
			HashSet<String> keys = new HashSet<String>();
			for (String s : currentMatrix.keySet()){
				int total = 0;
				for (String m : currentMatrix.get(s).keySet()){
					total += currentMatrix.get(s).get(m).intValue();
				}
				if (total == 0){
					keys.add(s);
				}
			}
			
			for (String s : keys){
				currentMatrix.remove(s);
			}
		}
	}
	
	public void outputResutls(){
		for (MatrixInfo info : this.matrixMap.keySet()){
			if (info.getWeigth().contains("Gaussian") || info.getK() != 4){
				//continue;
			}
			System.out.println(info);
			StatisticsWrapper wrapper = new StatisticsWrapper(this.matrixMap.get(info));
			System.out.println(wrapper.getStatisticString());
			System.out.println(getConfusionMatrixString(info.getBehaviour(), this.matrixMap.get(info)));
			System.out.println("");
		}
	}
	
	public void parseLine(String line){
		String parsed[] = line.substring(1, line.length()).split("\\|");
		if (parsed.length == 1){
			String behaviour = parsed[0].substring(0, parsed[0].indexOf("Confusion Matrix") - 1);
			currentInfo = new MatrixInfo(globalInfo);
			currentInfo.setBehaviour(behaviour.trim());
			if (!this.matrixMap.containsKey(currentInfo)){
				this.matrixMap.put(currentInfo, new HashMap<String,Map<String,Integer>>());
			}
			this.currentMatrix = this.matrixMap.get(currentInfo);
			
		}else if (parsed[0].trim().isEmpty()){
			headers = new String[parsed.length - 1];
			for (int i = 1; i < parsed.length; i++){
				headers[i - 1] = parsed[i].trim();
				if (!this.currentMatrix.containsKey(parsed[i].trim())){
					this.currentMatrix.put(parsed[i].trim(), new HashMap<String, Integer>());
				}
			}
		}else{
			for (int i = 1; i < parsed.length; i++){
				if (!this.currentMatrix.get(parsed[0].trim()).containsKey(headers[i - 1])){
					this.currentMatrix.get(parsed[0].trim()).put(headers[i - 1], 0);
				}
				int value = this.currentMatrix.get(parsed[0].trim()).get(headers[i - 1]);
				this.currentMatrix.get(parsed[0].trim()).put(headers[i - 1], Integer.parseInt(parsed[i].trim()) + value);
			}
		}
	}
	
	enum Method{
		SEQ,
		KNN,
		BEST,
		EDIT,
		JACCARD,
		;
		public static Method strToMethod(String str){
			for (Method m : Method.values()){
				if (str.toLowerCase().equals(m.name().toLowerCase())){
					return m;
				}
			}
			return null;
		}
	}
	
	enum Behaviour{
		SmartRandom("Smart Random"), 
		SmartStraightLine("Straight Line"), 
		ZigZag("Zig Zag"), 
		FixedSequence("Fixed Sequence"), 
		SmartExplorer("Smart Explorer"),
		;
		String name;
		
		Behaviour(String name){
			this.name = name;
		}
		
		public static Behaviour strToBehaviour(String str){
			for (Behaviour b : Behaviour.values()){
				if (str.equals(b.name)){
					return b;
				}
			}
			return null;
		}
	}
	
	enum Weight{
		Linear_05("Linear", "-0.5"),
		Linear_02("Linear", "-0.2"),
		Linear_01("Linear", "-0.1"),
		Linear_005("Linear", "0.05"),
		
		Decay_10("Decay", "-10.0"),
		Decay_1("Linear", "-1.0"),
		Decay_01("Decay", "-0.1"),
		Decay_001("Decay", "-0.01"),
		
		Gaussian_00("Gaussian", "-0.0-0.15"),
		Gaussian_10("Gaussian", "-1.0-0.15"),
		Gaussian_20("Gaussian", "-2.0-0.15"),
		Gaussian_50("Gaussian", "-5.0-0.15"),
		Gaussian_100("Gaussian", "-10.0-0.15"),
		;
		
		String name;
		String value;
		Weight(String name, String value){
			this.name = name;
			this.value = value;
		}
		
		public static Weight strToWeight(String str){
			for (Weight w : Weight.values()){
				if (str.contains(w.name) && str.endsWith(w.value)){
					return w;
				}
			}
			return null;
		}
	}
	
	class MatrixInfo{
		private String method;
		private String behaviour;
		private boolean isRandom;
		private int k;
		private String weight;
		
		private Method m;
		private Behaviour b;
		private Weight w;
		
		public MatrixInfo(){
			weight = "";
		}
		
		public MatrixInfo(MatrixInfo info){
			this.method = info.method;
			this.behaviour = info.behaviour;
			this.isRandom = info.isRandom;
			this.k = info.k;
			this.weight = info.weight;
			
			this.w = info.w;
			this.m = info.m;
			this.b = info.b;
		}
		
		public String getWeigth(){
			return this.weight;
		}
		
		public void setWeight(String weight){
			this.weight = weight;
			
			this.w = Weight.strToWeight(weight);
		}
		
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
			
			this.m = Method.strToMethod(method);
		}
		public boolean isRandom() {
			return isRandom;
		}
		public void setRandom(boolean isRandom) {
			this.isRandom = isRandom;
		}
		public int getK() {
			return k;
		}
		public void setK(int k) {
			this.k = k;
		}
		public String getBehaviour() {
			return behaviour;
		}
		public void setBehaviour(String behaviour) {
			this.behaviour = behaviour;
			
			this.b = Behaviour.strToBehaviour(behaviour);
		}
		@Override
		public int hashCode(){
			int s = this.m.ordinal();
			s *= 10;
			
			s += this.b.ordinal();
			s *= 10;
			
			if (this.w != null){
				s += this.w.ordinal();
			}
			s *= 100;
			
			s += this.k;
			s *= 10;
			
			s += (this.isRandom) ? 1 : 0;
			return s;
		}
		
		@Override
		public boolean equals(Object o){
			if (!(o instanceof MatrixInfo)){
				return false;
			}
			MatrixInfo m = (MatrixInfo)o;
			return this.method.equals(m.method) && this.isRandom == m.isRandom && this.k == m.k && this.behaviour.equals(m.behaviour) && this.weight.equals(m.weight);
		}
		
		@Override
		public String toString(){
			return method + " " + behaviour + " " + ((isRandom) ? "Random" : "Non Random") + " " + k + " " + weight; 
		}
	}
}
