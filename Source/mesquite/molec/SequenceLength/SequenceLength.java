/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.SequenceLength;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class SequenceLength extends NumberForTaxon {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
		"The source of characters is arranged initially");
	}
	MatrixSourceCoord matrixSourceTask;
	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	MesquiteBoolean countExcluded = new MesquiteBoolean(false);
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, MolecularState.class, "Source of character matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		addCheckMenuItem(null, "Count Excluded Characters", makeCommand("toggleCountExcluded",  this), countExcluded);
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("toggleCountExcluded " + countExcluded.toOffOnString());
		return temp;
	}

	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		observedStates = null;
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Sets whether or not excluded characters are considered in the calculation", "[on or off]", commandName, "toggleCountExcluded")) {
			boolean current = countExcluded.getValue();
			countExcluded.toggleValue(parser.getFirstToken(arguments));
			if (current!=countExcluded.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
		matrixSourceTask.initialize(currentTaxa);
		observedStates = matrixSourceTask.getCurrentMatrix(taxa);
	}

	public  void calculateNumber(Taxon taxon, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		clearResultAndLastResult(result);
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
		if (taxa != currentTaxa || observedStates == null ) {
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
			currentTaxa = taxa;
		}
		if (observedStates==null)
			return;
		CharacterData data = observedStates.getParentData();
		CharInclusionSet incl = null;
		if (data !=null)
			incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = observedStates.getNumChars();
		if (numChars != 0) {
			CharacterState cs = null;
			int seqLen = 0;
			for (int ic=0; ic<numChars; ic++) {
				if (countExcluded.getValue() || (incl == null || incl.isBitOn(ic)) && observedStates!=null){  // adjusted 2. 01 to consider inclusion  // added control in 3.0
					cs = observedStates.getCharacterState(cs, ic, it);
					if (!cs.isInapplicable())
						seqLen++;
				}
			}
			result.setValue(seqLen);
		}	
	
		if (resultString!=null)
			resultString.setValue("Length of sequence in matrix "+ observedStates.getName()  + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
		if (currentTaxa != null && observedStates == null)
			observedStates = matrixSourceTask.getCurrentMatrix(currentTaxa);
		if (observedStates != null && getProject().getNumberCharMatricesVisible()>1){
			CharacterData d = observedStates.getParentData();
			if (d != null && d.getName()!= null) {
				String n =  d.getName();
				if (n.length()>12)
					n = n.substring(0, 12); 
				return "Seq.Length (" + n + ")";
			}
		}
		return "Sequence Length";
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public String getParameters() {
		String s="";
		if (countExcluded.getValue())
			s = "Excluded characters are counted.";
		else
			s="Excluded characters not counted.";
		return "Sequence Length in matrix from: " + matrixSourceTask.getParameters() + ". " + s;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the length (total minus gaps) of a molecular sequence in a taxon." ;
	}

}




