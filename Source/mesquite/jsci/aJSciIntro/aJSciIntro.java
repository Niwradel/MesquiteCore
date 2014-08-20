/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.jsci.aJSciIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aJSciIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aJSciIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Introduces the JSci library.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "mesquite.jsci Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "JSci";
 	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	/** Returns information about a package of modules*/
 	public String getPackageCitation(){
 		return "Hale, M. & J. van Kooten.  JSci - A science API for Java, version 0.89 (14 April 2003)";
 	}
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
	
	public boolean getHideable(){
		return false;
	}
}

