/*******************************************************************************************************
 *
 * DbvtTreeCollider.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

// Dbvt implementation by Nathanael Presson

package com.bulletphysics.collision.broadphase;

/**
 *
 * @author jezek2
 */
public class DbvtTreeCollider extends Dbvt.ICollide {

	/** The pbp. */
	public DbvtBroadphase pbp;

	/**
	 * Instantiates a new dbvt tree collider.
	 *
	 * @param p the p
	 */
	public DbvtTreeCollider(DbvtBroadphase p) {
		this.pbp = p;
	}

	@Override
	public void Process(Dbvt.Node na, Dbvt.Node nb) {
		DbvtProxy pa = (DbvtProxy) na.data;
		DbvtProxy pb = (DbvtProxy) nb.data;
		//#if DBVT_BP_DISCRETPAIRS
		if (DbvtAabbMm.Intersect(pa.aabb, pb.aabb))
		//#endif
		{
			//if(pa>pb) btSwap(pa,pb);
			if (pa.hashCode() > pb.hashCode()) {
				DbvtProxy tmp = pa;
				pa = pb;
				pb = tmp;
			}
			pbp.paircache.addOverlappingPair(pa, pb);
		}
	}

}
