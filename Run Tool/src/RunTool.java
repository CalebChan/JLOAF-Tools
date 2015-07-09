import util.expert.lfo.SmartRandomExpertStrategy;


public class RunTool {
	
	
	public static void main(String args[]){
		String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomAgent";
		//String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomExplorerAgent";
		SmartRandomExpertStrategy expert = new SmartRandomExpertStrategy();
		expert.parseFile(file, "ToolCasebase");
		DiscreteRandomAgentRunTool tool = new DiscreteRandomAgentRunTool("ToolCasebase.cb", 7, 1000);
		tool.setup();
		tool.runTool();
		tool.displayToolSummary();
	}
}
