import util.expert.lfo.SmartRandomExpertStrategy;


public class RunTool {
	
	
	public static void main(String args[]){
		String file = "C:/Users/calebchan/Desktop/Stuff/workspace/Test Data/Batch Test 3/TB/Expert/Run 1/SmartRandomAgent";
		SmartRandomExpertStrategy expert = new SmartRandomExpertStrategy();
		expert.parseFile(file, "ToolCasebase");
		DiscreteRandomAgentRunTool tool = new DiscreteRandomAgentRunTool("ToolCasebase.cb", 1000, 7);
		tool.setup();
		tool.runTool();
		tool.displayToolSummary();
	}
}
