/*******************************************************************************************************
 *
 * ISaveDelegate.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import msi.gama.runtime.IScope;
import msi.gaml.expressions.IExpression;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * The Interface ISaveDelegate.
 */
public interface ISaveDelegate {

	/** The empty synonyms. */
	BiMap<String, String> EMPTY_SYNONYMS = ImmutableBiMap.of();

	/**
	 * Save an item to disk.
	 *
	 * @param scope
	 *            the runtime scope
	 * @param item
	 *            the item to save
	 * @param file
	 *            the file to save it to
	 * @param code
	 *            the code (epsg code)
	 * @param addHeader
	 *            wether to add a header or not when it makes sense
	 * @param type
	 *            the type of saved data we target (e.g. 'image', etc.)
	 * @param attributesToSave
	 *            the attributes to save. Can be an instance of Arguments or an expression containing a map<string,
	 *            value> or a list<string>.
	 * @throws IOException
	 */
	void save(IScope scope, IExpression item, File file, String code, boolean addHeader, String type,
			Object attributesToSave) throws IOException;

	/**
	 * The type of the item. Returns the gaml type required for triggering this save delegate. If no type is declared
	 * (by default), then the type of the file to produce (see {@link #getFileTypes()} is used to determine which save
	 * delegate to run and/or the method {@link #handlesDataType()}. If a gaml type is declared, the type of the file
	 * needs also to match (this allows two delegates to save the same objects, but with different file types).
	 *
	 * @return the i type
	 */
	default IType getDataType() { return Types.NO_TYPE; }

	/**
	 * Return whether or not the specified data type is handled by the save delegate. True by default. Can be used to
	 * restrict the usage of this delegate to specific types
	 *
	 * @return the i type
	 */
	default boolean handlesDataType(final IType request) {
		return true;
	}

	/**
	 * Returns the types of file this ISaveDelegate is able to produce (e.g. "image", "shp", etc.). This is used to
	 * determine which ISaveDelegate to choose when 'save' is invoked in addition to the gaml type provided (see
	 * {@link #getDataType()}
	 *
	 * @return the type
	 */
	Set<String> getFileTypes();

	/**
	 * Gets the synonyms. Returns any pairs of synonyms between type names (for instance, "txt" and "text"), otherwise
	 * an empty BiMap that can be obtained from this interface using ISaveDelegate.EMPTY_SYNONYMS;
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the synonyms
	 * @date 13 oct. 2023
	 */
	default BiMap<String, String> getSynonyms() { return EMPTY_SYNONYMS; }

}