/*******************************************************************************************************
 *
 * IExperimentController.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.kernel.experiment;

/**
 * Class IExperimentController.
 *
 * @author drogoul
 * @since 6 déc. 2015
 *
 */
public interface IExperimentController {

	/** The open. */
	int _OPEN = 0;

	/** The start. */
	int _START = 1;

	/** The step. */
	int _STEP = 2;

	/** The pause. */
	int _PAUSE = 3;

	/** The reload. */
	int _RELOAD = 6;

	/** The back. */
	int _BACK = 8;

	/** The close. */
	int _CLOSE = -1;

	/**
	 * @return
	 */
	IExperimentPlan getExperiment();

	/**
	 *
	 */
	default void userStep() {}

	/**
	 *
	 */
	default void userStepBack() {}

	/**
	 *
	 */
	default void startPause() {}

	/**
	 *
	 */
	default void close() {}

	/**
	 *
	 */
	default void userStart() {}

	/**
	 *
	 */
	default void directPause() {}

	/**
	 * Back.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 15 oct. 2023
	 */
	default void back() {}

	/**
	 *
	 */
	default void dispose() {}

	/**
	 *
	 */
	default void directOpenExperiment() {}

	/**
	 *
	 */
	default void userReload() {}

	/**
	 *
	 */
	default void userPause() {}

	/**
	 *
	 */
	default void userOpen() {}

	/**
	 * Checks if is disposing.
	 *
	 * @return true, if is disposing
	 */
	default boolean isDisposing() { return false; }

	/**
	 * Checks if is paused.
	 *
	 * @return true, if is paused
	 */
	default boolean isPaused() { return false; }

	/**
	 * Schedule.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	default void schedule(final ExperimentAgent agent) {}

	/**
	 * Direct step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 15 oct. 2023
	 */
	default void directStep() {}

}