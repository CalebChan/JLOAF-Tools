import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import util.expert.ExpertStrategy;
import util.expert.lfo.*;


public class RunTool {
	
	public static ExpertStrategy expert = null;
	
	
	public static void main(String args[]){
//		String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomAgent";
		//String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/ZigZagAgent";
		String baseFolder = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/";
		String fileName = "";
		File file = new File(baseFolder);
		Scanner scanner = new Scanner(System.in);
		boolean run = true;
		while(run){
			String input = scanner.nextLine();
			String inputs[] = input.split(" ", 2);
			if (inputs[0].equals("exit")){
				return;
			}else if (inputs[0].equals("ls")){
				for (String f : file.list()){
					System.out.println(f);
				}
			}else if (inputs[0].equals("cd")){
				System.out.println("Input : " + inputs[1]);
				File newFile = new File(file, inputs[1]);
				if (newFile.exists() && newFile.isDirectory()){
					file = newFile;
				}else if (!newFile.exists() && !newFile.isDirectory()){
					newFile = new File(inputs[1]);
					if (newFile.exists() && newFile.isDirectory()){
						file = newFile;
					}
				}
			}else if (inputs[0].equals("file")){
				try {
					fileName = file.getCanonicalPath() + "/" + inputs[1].substring(0, inputs[1].lastIndexOf("."));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if (inputs[0].equals("agent")){
				if (inputs[1].equals("random")){
					expert = new SmartRandomExpertStrategy();
				}else if (inputs[1].equals("line")){
					expert = new SmartStraightLineExpertStrategy();
				}else if (inputs[1].equals("zig")){
					expert = new ZigZagExpertStrategy();
				}else if (inputs[1].equals("fixed")){
					expert = new FixedSequenceExpertStrategy();
				}else if (inputs[1].equals("explorer")){
					expert = new SmartExplorerExpertStrategy();
				}
			}else if (inputs[0].equals("clear")){
				fileName = "";
				expert = null;
			}else if (inputs[0].equals("show")){
				System.out.println("File name : " + fileName);
				if (expert == null){
					System.out.println("Expert : null");
				}else{
					System.out.println("Expert : " + expert.getClass().getName());
				}
			}
			if (!fileName.equals("") && expert != null){
				run = false;
			}
		}
		
		
		expert.parseFile(fileName, "ToolCasebase");
		DiscreteRandomAgentRunTool tool = new DiscreteRandomAgentRunTool("ToolCasebase.cb", 7, 1000);
		tool.setup();
		tool.runTool();
		tool.displayToolSummary();
		
		scanner.reset();
		scanner.close();
	}
}
