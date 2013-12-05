/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TreeListPolyAssumption;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TreeListPolyAssumption extends TreeListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Polytomy Assumption";
	}
	public String getExplanation() {
		return "Indicates whether the tree assumes that any polytomies, if present, are hard or soft.  If not, the default assumption is used." ;
	}
	/*.................................................................................................................*/
	TreeVector treesBlock;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public void setTableAndTreeBlock(MesquiteTable table, TreeVector trees){
		treesBlock = trees;
	}
	public String getTitle() {
		return "Polytomy Assumption";
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == treesBlock)
			treesBlock=null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public String getStringForTree(int ic){
		if (treesBlock==null)
			return "";
		Tree tree = treesBlock.getTree(ic);
		if (tree ==null)
			return "";
		if (tree.getPolytomiesAssumption()== 0)
			return "Hard";
		else if (tree.getPolytomiesAssumption()== 1)
			return "Soft";
		else
			return "Default";
	}
	public String getWidestString(){
		return " Polytomy Assumption ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
}

