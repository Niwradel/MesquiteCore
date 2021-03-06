// DatedTipsClockTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;


/**
 * provides parameter interface to a clock tree with dated tips,
 * following A. Rambaut. 2000. Bioinformatics 16:395-399.
 * (parameters are the minimal node height differences
 * at each internal node and the evolutionary rate)
 *
 * @version $Id: DatedTipsClockTree.java,v 1.12 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class DatedTipsClockTree extends ParameterizedTree
	implements java.io.Serializable {

	//
	// Public stuff
	//


	/**
	 * Constructor without TimeOrderCharacterData.
	 * Dates are extracted from labels.
	 */
	public DatedTipsClockTree(Tree t) {
		this(t,  null, true);
	}

	/**
	 * take any tree and afford it with an interface
	 * suitable for a clock-like tree with dated tips (parameters
	 * are the minimal node height differences at each internal node
	 * and the rate).
	 *
	 * This constructor uses the standard definition of a rate (default value = 0, min value = 0, max value = 1)
	 * and does include the rate as the parameter
	 * <p>
	 * <em>This parameterisation of a clock-tree, ensuring that
	 * all parameters are independent of each other is due to
	 * Andrew Rambaut (personal communication).</em>
	 */
	public DatedTipsClockTree(Tree t, TimeOrderCharacterData tocd, boolean useDefaultParameters)
	{
		this(t,new ParameterizedDouble(0,0,1),tocd,useDefaultParameters,true);
	}

	/**
	 * take any tree and afford it with an interface
	 * suitable for a clock-like tree with dated tips (parameters
	 * are the minimal node height differences at each internal node
	 * and the rate).
	 * This constructor does not include the rate as the parameter
	 * <p>
	 * <em>This parameterisation of a clock-tree, ensuring that
	 * all parameters are independent of each other is due to
				 * Andrew Rambaut (personal communication).</em>
	 */
	public DatedTipsClockTree(Tree t, ParameterizedDouble rate, TimeOrderCharacterData tocd, boolean useDefaultParameters)
	{
		this(t,rate,tocd,useDefaultParameters,false);
	}
	/**
	 * take any tree and afford it with an interface
	 * suitable for a clock-like tree with dated tips (parameters
	 * are the minimal node height differences at each internal node
	 * and the rate).
	 * <p>
	 * <em>This parameterisation of a clock-tree, ensuring that
	 * all parameters are independent of each other is due to
				 * Andrew Rambaut (personal communication).</em>
	 */
	public DatedTipsClockTree(Tree t, ParameterizedDouble rate, TimeOrderCharacterData tocd, boolean useDefaultParameters, boolean includeRateAsParameter)
	{
		this.includeRateAsParameter_ = includeRateAsParameter;
		this.rate = rate;
		setBaseTree(t);
		this.tocd = tocd;

		if (getRoot().getChildCount() < 2)
		{
			throw new IllegalArgumentException(
			"The root node must have at least two childs!");
		}

		parameter = new double[getInternalNodeCount()];

		date = new double[t.getExternalNodeCount()];
		getDates();

		if (useDefaultParameters) {
			for (int i = 0; i < parameter.length; i++) {
				parameter[i] = getDefaultValue(i);
			}
		} else {
			// assumes that the tree heights are already valid
			// DatedTipClockTree heights.
			// extracts rate on this assumption with aid of
			// TimeOrderCharacterData.

			heights2parameters();

			boolean found = false;
			int i = 0;
			while ((i < getExternalNodeCount()) && !found) {

				String name = getExternalNode(i).getIdentifier().getName();

				int index = tocd.getIdGroup().whichIdNumber(name);

				double time = tocd.getTime(index);

				if (time > 0.0) {
					double height = getExternalNode(i).getNodeHeight();

					rate.setValue(height / time);
					System.out.println("rate = " + rate);
					found = true;
				}
				i += 1;
			}
			if (!found) {
				rate.setValue(0.0);
			}
		}


		// init
		parameters2Heights();
		NodeUtils.heights2Lengths(getRoot());
	}
	
	/** make parameters consistent with branch lengths and rate parameter */
	public void update()
	{
		createNodeList();
		getDates(); // order of tips may have changed!
		NodeUtils.lengths2Heights(getRoot());
		heights2parameters();
	}
	
	// interface Parameterized (Rambaut paremeterisation)

	public int getNumParameters()
	{
		return getInternalNodeCount()+(includeRateAsParameter_ ? 1 : 0);
	}

	public void setParameter(double param, int n)
	{
		if (n < getInternalNodeCount())
		{
			parameter[n] = param;
		}
		else
		{
			rate.setValue(param);
		}
		
		parameters2Heights();
		NodeUtils.heights2Lengths(getRoot());
	}

	public double getParameter(int n)
	{
		if (n < getInternalNodeCount())
		{
			return parameter[n];
		}
		else
		{
			return rate.getValue();
		}
	}

	public void setParameterSE(double paramSE, int n)
	{
		// we are only interested in SE of rate
		if (n < getInternalNodeCount())
		{
			// do nothing
		}
		else
		{
			rate.setSE(paramSE);
		}
		return;
	}

	/** return standard error of parameter */
	public double getParameterSE(int n)
	{
		// we are only interested in SE of rate
		if (n < getInternalNodeCount())
		{
			return 0.0;
		}
		else
		{
			return rate.getSE();
		}
	}


	public double getLowerLimit(int n)
	{
		if (n < getInternalNodeCount())
		{
			return BranchLimits.MINARC;
		}
		else
		{
			return rate.getLowerLimit();
		}
	}

	public double getUpperLimit(int n)
	{
		if (n < getInternalNodeCount())
		{
			return BranchLimits.MAXARC;
		}
		else
		{
			return rate.getUpperLimit(); // subst. per unit time
		}
	}

	public double getDefaultValue(int n)
	{
		if (n < getInternalNodeCount())
		{
			return BranchLimits.DEFAULT_LENGTH;
		}
		else
		{
			return rate.getDefaultValue(); // contemporaneous tips
		}
	}
	
	/** 
	 * set rate (and thus node heights and branch lengths of leaves) without
	 * changing all other node heights and branch lengths
	 */
	public void setRate(double r)
	{
		rate.setValue(r);
		for (int i = 0; i < getExternalNodeCount(); i++)
		{
			Node leaf = getExternalNode(i);
			double h = (maxDate-date[i])*rate.getValue();
			leaf.setNodeHeight(Math.abs(h));
			double hp = leaf.getParent().getNodeHeight();
			leaf.setBranchLength(Math.abs(hp-h));
		}
	}

	/** 
	 * get rate 
	 */
	public double getRate()
	{
		return rate.getValue();
	}


	/** 
	 * set rate SE
	 */
	public void setRateSE(double rSE)
	{
		rate.setSE(rSE);
	}


	/**
	 * find max. rate (for setRate) allowed by current node heights
	 */
	public double getMaxRate()
	{
		double maxRate = 0;

		for (int i = 0; i < getExternalNodeCount(); i++)
		{
			Node leaf = getExternalNode(i);
			
			// maximum rate for this leaf
			double maxRateLeaf = (leaf.getParent().getNodeHeight()-BranchLimits.MINARC)/(maxDate-date[i]);
			
			if (i == 0 || maxRateLeaf < maxRate)
			{
				maxRate = maxRateLeaf;
			}
		}
		
		return maxRate;
	}

	/**
	 * Gets the TimeOrderCharacterData
	 */
	public TimeOrderCharacterData getTimeOrderCharacterData() {
		if (tocd != null) {
			return tocd;
		} else {
			//extract from tree.
			//WHAT UNITS TO USE!? Defaulting to EXPECTED_SUBSTITUTIONS
			
			TimeOrderCharacterData tempTOCD = 
				new TimeOrderCharacterData(TreeUtils.getLeafIdGroup(this),
					Units.EXPECTED_SUBSTITUTIONS);
			tempTOCD.setTimes(date, Units.EXPECTED_SUBSTITUTIONS, true);

			return tempTOCD;
		}
	}	

	//
	// Private stuff
	//

	private void parameters2Heights()
	{
		// nodes have been stored by a post-order traversal
		
		for (int i = 0; i < getExternalNodeCount(); i++)
		{
			getExternalNode(i).setNodeHeight(  (maxDate-date[i])*rate.getValue()  );
		}
		
		for (int i = 0; i < getInternalNodeCount(); i++)
		{
			Node node = getInternalNode(i);
			node.setNodeHeight(parameter[i] + NodeUtils.findLargestChild(node));
		}
	}
	
	private void heights2parameters()
	{
		for (int i = 0; i < getInternalNodeCount(); i++)
		{
			Node inode = getInternalNode(i);
			parameter[i] = inode.getNodeHeight()-NodeUtils.findLargestChild(inode);
		}
	}

	private void getDates()
	{

		double maxTime = 0.0;
		if (tocd != null) {
			for (int i = 0; i < date.length; i++) {
				if (tocd.getTime(i) > maxTime) {
					maxTime = tocd.getTime(i);
				}
			}
		}

		for (int i = 0; i < getExternalNodeCount(); i++)
		{
			if (tocd == null) {
				// extract dates from sequence identifiers
				date[i] = extractDate(getExternalNode(i).getIdentifier().getName());
			} else {

				if (tocd.hasTimes()) {
					String name = getExternalNode(i).getIdentifier().getName();	
				
					int index = tocd.getIdGroup().whichIdNumber(name);
					
					//times are measured backwards from zero at most recent tip. 
					// this needs to be converted to forward-time dates starting
					// from time zero at oldest tip.
					
					date[i] = maxTime - tocd.getTime(index);
				} else {
					//no times available 
					throw new IllegalArgumentException("TimeOrderCharacterData does not have any times!");
				}
			}
		} 
		
		// find maximum and minimum date
		minDate = maxDate = date[0];
		for (int i = 1; i < getExternalNodeCount(); i++)
		{
			if (date[i] > maxDate) maxDate = date[i];
			if (date[i] < minDate) minDate = date[i];
		}
		
		// check for equality of dates
		if (minDate == maxDate)
		{
			throw new IllegalArgumentException("Tip dates must not be the same for all tips");
		}	
	}

	private double extractDate(String string)
	{
		StringBuffer buffer = new StringBuffer();
		
		int len = string.length();
		
		boolean readDot = false;
		for (int i = len-1; i > -1; i--)
		{
			char c = string.charAt(i);
			
			if ( !Character.isDigit(c) &&  !(c == '.' && !readDot) ) break;
			buffer.append(c);
			if (c == '.') readDot = true;
			
		}
		buffer.reverse();
		
		String date = buffer.toString();
		
		if (date.length() == 0)
		{
			return 0.0;
		}
		else
		{
			return (Double.valueOf(buffer.toString())).doubleValue();
		}
	}
	
	private double[] parameter;
	private ParameterizedDouble rate;
	private boolean includeRateAsParameter_; /** If true than the rate is included as a parameter of this object */ 
	private double minDate, maxDate;
	private double[] date;
	private TimeOrderCharacterData tocd;
}

