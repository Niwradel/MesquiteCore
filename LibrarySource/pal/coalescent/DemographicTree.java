// DemographicTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

// - partial likelihoods need a lot of memory storage
//   memory usage could be opimized by working in a single site


package pal.coalescent;

/**
 * interface defining a parameterized tree that
 * includes demographic information.
 *
 * @author Alexei Drummond
 */
public interface DemographicTree {

	double computeDemoLogLikelihood();

	DemographicModel getDemographicModel();
}


