import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class ConfusionMatrixParser {
	
	private String filename;
	private HashMap<MatrixInfo, Map<String,Map<String,Integer>>> matrixMap;
	
	private Map<String,Map<String,Integer>> currentMatrix;
	private MatrixInfo currentInfo;
	
	private MatrixInfo globalInfo;
	
	private String[] headers;
	
	public static final String REASONING_TAG = "Reasoning Method : ";
	public static final String WEIGHT_TAG = "Weight Function : ";
	
	public ConfusionMatrixParser(String filename){
		this.filename = filename;
		this.matrixMap = new HashMap<MatrixInfo, Map<String,Map<String,Integer>>>();
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
	
	public void outputResutls(){
		for (MatrixInfo info : this.matrixMap.keySet()){
			if (info.getWeigth().contains("Gaussian") || info.getK() != 10){
				continue;
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
	
	public static void main(String args[]){
		ConfusionMatrixParser parser = new ConfusionMatrixParser("J:/RESULTS/RESULT/Version 5/kNNBestConfusion.txt");
		parser.parseFile();
		parser.outputResutls();
	}
	
	class MatrixInfo{
		private String method;
		private String behaviour;
		private boolean isRandom;
		private int k;
		private String weight;
		
		public MatrixInfo(){}
		
		public MatrixInfo(MatrixInfo info){
			this.method = info.method;
			this.behaviour = info.behaviour;
			this.isRandom = info.isRandom;
			this.k = info.k;
			this.weight = info.weight;
		}
		
		public String getWeigth(){
			return this.weight;
		}
		
		public void setWeight(String weight){
			this.weight = weight;
		}
		
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
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
		}
		@Override
		public int hashCode(){
			String s = method + isRandom + k + behaviour + weight;
			return s.hashCode();
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
