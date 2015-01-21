import java.util.ArrayList;

import org.jLOAF.casebase.Case;
import org.jLOAF.casebase.CaseRun;


public class CandidateRunModel extends RunModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<ExtraData> extraInfo;
	
	public CandidateRunModel(CaseRun r, ArrayList<ExtraData> extraInfo) {
		super(r);
		
		if (r.getRunLength() != extraInfo.size()){
			throw new RuntimeException();
		}
		
		this.extraInfo = extraInfo;
	}
	
	class CandidiateCustomDialog extends CustomDialog{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public void setText(Case c){
			super.setText(c);
			
			// Add stuff on top
		}
	}

}
