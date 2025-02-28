/*******************************************************************************************************
 *
 * GamaList.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.operators.Cast;
import msi.gaml.types.GamaType;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 21 nov. 2008
 *
 * @todo Description
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaList<E> extends ArrayList<E> implements IList<E> {

	/** The type. */
	private IContainerType type;

	@Override
	public IContainerType<?> getGamlType() { return type; }

	@Override
	public StreamEx<E> stream(final IScope scope) {
		// Building an explicit copy for preventing concurrent modifications.
		// Performances need to be tested as it may create bottlenecks (large population of agents being copied over and
		// over again...). See #3626.
//		E[] array = (E[]) this.toArray();
//		return StreamEx.<E> of(array);
		return StreamEx.<E> of (this);
	}

	@Override
	public boolean equals(final Object other) {
		if (other == this) return true;
		if (!(other instanceof IList)) return false;
		return GamaListFactory.equals(this, (IList) other);
	}

	/**
	 * Instantiates a new gama list.
	 */
	protected GamaList() {
		this(0, Types.NO_TYPE);
	}

	/**
	 * Instantiates a new gama list.
	 *
	 * @param capacity
	 *            the capacity
	 * @param contentType
	 *            the content type
	 */
	protected GamaList(final int capacity, final IType contentType) {
		super(capacity);
		this.type = Types.LIST.of(contentType);
	}

	@Override
	public IList<E> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		if (!GamaType.requiresCasting(contentsType, getGamlType().getContentType())) {
			if (copy) return this.cloneWithContentType(contentsType);
			return this;
		}
		final GamaList clone = this.cloneWithContentType(contentsType);
		final int n = size();
		for (int i = 0; i < n; i++) { clone.setValueAtIndex(scope, i, get(i)); }
		return clone;
	}

	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		for (final Iterator iterator = iterator(); iterator.hasNext();) {
			if (Objects.equals(iterator.next(), value)) { iterator.remove(); }
		}
	}

	/**
	 * Clone with content type.
	 *
	 * @param contentType
	 *            the content type
	 * @return the gama list
	 */
	private GamaList cloneWithContentType(final IType contentType) {
		final GamaList clone = (GamaList) super.clone();
		clone.type = Types.LIST.of(contentType);
		return clone;
	}

	@Override
	public IList<E> copy(final IScope scope) {
		return cloneWithContentType(type.getContentType());
	}

	@Override
	public E getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, Cast.asInt(scope, indices.get(0)));
		// We do not consider the case where multiple indices are used. Maybe
		// could be used in the
		// future to return a list of values ?
	}

}
