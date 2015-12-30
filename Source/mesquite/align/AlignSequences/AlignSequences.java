/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.AlignSequences;
/*~~  */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;

/* ======================================================================== */
public class AlignSequences extends MolecDataEditorInit implements SeparateThreadStorage {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",
		"The sequence aligner is chosen in the Align Multiple Sequences submenu of the Matrix menu");
	}
	boolean separateThread = AlignMultipleSequencesMachine.separateThread;
	MolecularData data ;
	MultipleSequenceAligner aligner;
	
	AlignMultipleSequencesMachine alignmentMachine;

	MesquiteTable table;

	MesquiteSubmenuSpec mss= null;
	
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		mss = addSubmenu(null, "Align Multiple Sequences", makeCommand("doAlign",  this));
		mss.setList(MultipleSequenceAligner.class);
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (!(data instanceof MolecularData)){
			mss.setEnabled(false);
			resetContainingMenuBar();
			return;
		}
		this.table = table;
		this.data = (MolecularData)data;
		mss.setCompatibilityCheck(data.getStateClass());
		resetContainingMenuBar();

	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires module to align sequences", "[name of module]", commandName, "doAlign")) {
			if (table!=null && data !=null){
				if (data.getEditorInhibition()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				aligner= (MultipleSequenceAligner)hireNamedEmployee(MultipleSequenceAligner.class, arguments);
				if (aligner!=null) {
					boolean a = alterData(data, table);
					if (a) {
						table.repaintAll();
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
						data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
					}
					if (!separateThread) {
						fireEmployee(aligner);
					}
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void setSeparateThread(boolean separateThread){
		this.separateThread=separateThread;
	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alterData(CharacterData data, MesquiteTable table){
		alignmentMachine = new AlignMultipleSequencesMachine(this,this, aligner);
		return alignmentMachine.alignData(data,table);
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Align Sequences";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sends the selected sequence to be aligned." ;
	}


}

/*

class AlignThread extends Thread {
	AlignSequences ownerModule;
	MultipleSequenceAligner aligner;
	MolecularData data;
	MesquiteTable table;
	boolean separateThread = false;
	public AlignThread(AlignSequences ownerModule, MultipleSequenceAligner aligner, MolecularData data, MesquiteTable table){
		this.aligner = aligner;
		this.ownerModule = ownerModule;
		this.data = data;
		this.table = table;
	}

	public void run() {

		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		boolean entireColumnsSelected = false;
		int oldNumChars = data.getNumChars();
		if (!table.singleCellBlockSelected(firstRow, lastRow,  firstColumn, lastColumn)) {
			firstRow.setValue(0);
			lastRow.setValue(data.getNumTaxa()-1);
			firstColumn.setValue(0);
			lastColumn.setValue(data.getNumChars()-1);
		}
		else 				
			entireColumnsSelected =  table.isColumnSelected(firstColumn.getValue());
		//NOTE: at present this deals only with whole character selecting, and with all taxa
		long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue());
		ownerModule.integrateAlignment(m, data,  firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue());
		if (entireColumnsSelected) {
			for (int ic = 0; ic<data.getNumChars(); ic++) 
				data.setSelected(ic,ic>=firstColumn.getValue() && ic<=lastColumn.getValue()- (oldNumChars - data.getNumChars()));
			table.selectColumns(firstColumn.getValue(),lastColumn.getValue()- (oldNumChars - data.getNumChars()));
		}
		if (separateThread)
			data.notifyListeners(ownerModule, new Notification(MesquiteListener.DATA_CHANGED));
		table.repaintAll();

	}
}

*/

