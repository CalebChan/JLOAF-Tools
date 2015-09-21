import agent.lfo.SmartExpert;
import util.expert.ExpertStrategy;
import util.expert.lfo.*;


public class RunTool {
	
	
	public static void main(String args[]){
//		String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomAgent";
		//String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/ZigZagAgent";
		String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomExplorerAgent";
		ExpertStrategy expert = new SmartExplorerExpertStrategy();
		expert.parseFile(file, "ToolCasebase");
		DiscreteRandomAgentRunTool tool = new DiscreteRandomAgentRunTool("ToolCasebase.cb", 7, 1000);
		tool.setup();
		tool.runTool();
		tool.displayToolSummary();
	}
}
