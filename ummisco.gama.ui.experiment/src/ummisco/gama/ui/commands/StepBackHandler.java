/*******************************************************************************************************
 *
 * StepBackHandler.java, in ummisco.gama.ui.experiment, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package ummisco.gama.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import msi.gama.runtime.GAMA;

/**
 * The Class StepBackHandler.
 */
public class StepBackHandler extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		GAMA.stepBackFrontmostExperiment();
		return this;
	}

}
