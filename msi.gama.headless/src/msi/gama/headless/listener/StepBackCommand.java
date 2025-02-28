/*******************************************************************************************************
 *
 * StepBackCommand.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.headless.listener;

import org.java_websocket.WebSocket;

import msi.gama.headless.server.GamaServerExperimentController;
import msi.gama.runtime.server.CommandResponse;
import msi.gama.runtime.server.GamaServerMessage;
import msi.gama.runtime.server.GamaWebSocketServer;
import msi.gama.runtime.server.ISocketCommand;
import msi.gama.util.IMap;
import ummisco.gama.dev.utils.DEBUG;

/**
 * The Class StepBackCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class StepBackCommand implements ISocketCommand {

	@Override
	public CommandResponse execute(final WebSocket socket, final IMap<String, Object> map) {

		final String exp_id = map.get(EXP_ID) != null ? map.get(EXP_ID).toString() : "";
		final int nb_step = map.get(NB_STEP) != null ? Integer.parseInt(map.get(NB_STEP).toString()) : 1;
		final String socket_id = map.get(SOCKET_ID) != null ? map.get(SOCKET_ID).toString() : "" + socket.hashCode();
		final boolean sync = map.get(SYNC) != null ? Boolean.parseBoolean("" + map.get(SYNC)) : false;
		final GamaWebSocketServer gamaWebSocketServer = (GamaWebSocketServer) map.get(SERVER);
		DEBUG.OUT("stepBack");
		DEBUG.OUT(exp_id);

		if (exp_id == "") return new CommandResponse(GamaServerMessage.Type.MalformedRequest,
				"For 'stepBack', mandatory parameter is: 'exp_id' ", map, false);

		var gama_exp = gamaWebSocketServer.getExperiment(socket_id, exp_id);
		if (gama_exp == null || gama_exp.getCurrentSimulation() == null)
			return new CommandResponse(GamaServerMessage.Type.UnableToExecuteRequest,
					"Unable to find the experiment or simulation", map, false);
	

		((GamaServerExperimentController)gama_exp.getController()).pause();

		if (sync) {
			while (!gama_exp.getController().isPaused()) {
			}
		}
		for (int i = 0; i < nb_step; i++) {
			try {
				if (sync) {
					((GamaServerExperimentController)gama_exp.getController())._job.doBackStep();
//					gama_exp.getController().back();
				} else {
					gama_exp.getController().userStepBack();
				}
			} catch (RuntimeException e) {
				DEBUG.OUT(e.getStackTrace());
				return new CommandResponse(GamaServerMessage.Type.GamaServerError, e, map, false);
			}

		}
		
		return new CommandResponse(GamaServerMessage.Type.CommandExecutedSuccessfully, "", map, false);
	}
}
