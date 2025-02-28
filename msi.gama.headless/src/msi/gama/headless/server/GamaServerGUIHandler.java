/*******************************************************************************************************
 *
 * GamaServerGUIHandler.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.headless.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

import msi.gama.common.interfaces.IConsoleDisplayer;
import msi.gama.common.interfaces.IStatusDisplayer;
import msi.gama.headless.listener.ExpressionCommand;
import msi.gama.headless.listener.LoadCommand;
import msi.gama.headless.listener.PauseCommand;
import msi.gama.headless.listener.PlayCommand;
import msi.gama.headless.listener.ReloadCommand;
import msi.gama.headless.listener.StepBackCommand;
import msi.gama.headless.listener.StepCommand;
import msi.gama.headless.listener.StopCommand;
import msi.gama.kernel.experiment.IExperimentAgent;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.NullGuiHandler;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.server.DefaultServerCommands;
import msi.gama.runtime.server.GamaServerMessage;
import msi.gama.runtime.server.ISocketCommand;
import msi.gama.util.GamaColor;
import msi.gama.util.file.json.DeserializationException;
import msi.gama.util.file.json.Jsoner;
import ummisco.gama.dev.utils.DEBUG;

/**
 * Implements the behaviours to trigger when GUI events happen in a simulation run in GamaServer
 *
 */
public class GamaServerGUIHandler extends NullGuiHandler {

	/** The status. */
	IStatusDisplayer status;

	/**
	 * Send message.
	 *
	 * @param exp
	 *            the exp
	 * @param m
	 *            the m
	 * @param type
	 *            the type
	 */
	private static void sendMessage(final IExperimentAgent exp, final Object m, final GamaServerMessage.Type type) {

		try {

			if (exp == null) {
				DEBUG.OUT("No experiment, unable to send message: " + m);
				return;
			}

			var scope = exp.getScope();
			if (scope == null) {
				DEBUG.OUT("No scope, unable to send message: " + m);
				return;
			}
			var socket = (WebSocket) scope.getData("socket");
			if (socket == null) {
				DEBUG.OUT("No socket found, maybe the client is already disconnected. Unable to send message: " + m);
				return;
			}
			socket.send(Jsoner.serialize(new GamaServerMessage(type, m, (String) scope.getData("exp_id"))));

		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + m);
			DEBUG.OUT(ex.toString());
		}
	}

	/**
	 * Can send dialog messages.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	private boolean canSendDialogMessages(final IScope scope) {
		if (scope != null && scope.getExperiment() != null && scope.getExperiment().getScope() != null)
			return scope.getExperiment().getScope().getData("dialog") != null
					? (boolean) scope.getExperiment().getScope().getData("dialog") : true;
		return true;
	}

	/**
	 * Can send runtime errors.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @date 14 août 2023
	 */
	private boolean canSendRuntimeErrors(final IScope scope) {
		if (scope != null && scope.getExperiment() != null && scope.getExperiment().getScope() != null)
			return scope.getExperiment().getScope().getData("runtime") != null
					? (boolean) scope.getExperiment().getScope().getData("runtime") : true;
		return true;
	}

	@Override
	public void openMessageDialog(final IScope scope, final String message) {
		DEBUG.OUT(message);
		if (!canSendDialogMessages(scope)) return;
		sendMessage(scope.getExperiment(), message, GamaServerMessage.Type.SimulationDialog);
	}

	@Override
	public void openErrorDialog(final IScope scope, final String error) {
		DEBUG.OUT(error);
		if (!canSendDialogMessages(scope)) return;
		sendMessage(scope.getExperiment(), error, GamaServerMessage.Type.SimulationErrorDialog);
	}

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		DEBUG.OUT(g);
		// removed to fix #3758
		// if (!canSendDialogMessages(scope)) return;
		if (!canSendRuntimeErrors(scope)) return;
		sendMessage(scope.getExperiment(), g, GamaServerMessage.Type.SimulationError);
	}

	@Override
	public IStatusDisplayer getStatus() {

		if (status == null) {
			status = new IStatusDisplayer() {

				private boolean canSendMessage(final IExperimentAgent exp) {
					if (exp == null) return false;
					var scope = exp.getScope();
					return scope != null && scope.getData("status") != null ? (boolean) scope.getData("status") : true;
				}

				@Override
				public void informStatus(final IScope scope, final String string) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + string + "\"" + "}"),
								GamaServerMessage.Type.SimulationStatusInform);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + string + "\"" + "}",
								GamaServerMessage.Type.SimulationStatusInform);
					}

				}

				@Override
				public void errorStatus(final IScope scope, final String message) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + message + "\"" + "}"),
								GamaServerMessage.Type.SimulationStatusError);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + message + "\"" + "}",
								GamaServerMessage.Type.SimulationStatusError);
					}

				}

				@Override
				public void setStatus(final IScope scope, final String msg, final GamaColor color) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(
								scope.getExperiment(), Jsoner.deserialize("{" + "\"message\": \"" + msg + "\","
										+ "\"color\": " + Jsoner.serialize(color) + "" + "}"),
								GamaServerMessage.Type.SimulationStatus);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + msg + "\"," + "\"color\": " + Jsoner.serialize(color) + "}",
								GamaServerMessage.Type.SimulationStatus);
					}
				}

				@Override
				public void informStatus(final IScope scope, final String message, final String icon) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize(
										"{" + "\"message\": \"" + message + "\"," + "\"icon\": \"" + icon + "\"" + "}"),
								GamaServerMessage.Type.SimulationStatusInform);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + message + "\"," + "\"icon\": \"" + icon + "\"" + "}",
								GamaServerMessage.Type.SimulationStatusInform);
					}

				}

				@Override
				public void setStatus(final IScope scope, final String msg, final String icon) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize(
										"{" + "\"message\": \"" + msg + "\"," + "\"icon\":\"" + icon + "\"" + "}"),
								GamaServerMessage.Type.SimulationStatus);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + msg + "\"," + "\"icon\": \"" + icon + "\"" + "}",
								GamaServerMessage.Type.SimulationStatus);
					}
				}

				@Override
				public void neutralStatus(final IScope scope, final String string) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + string + "\"" + "}"),
								GamaServerMessage.Type.SimulationStatusNeutral);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + string + "\"" + "}",
								GamaServerMessage.Type.SimulationStatusNeutral);
					}

				}

			};
		}
		return status;
	}

	@Override
	public IConsoleDisplayer getConsole() {
		if (console == null) {

			console = new IConsoleDisplayer() {

				private boolean canSendMessage(final IExperimentAgent exp) {
					var scope = exp.getScope();
					return scope != null && scope.getData("console") != null ? (boolean) scope.getData("console")
							: true;
				}

				@Override
				public void informConsole(final String s, final ITopLevelAgent root, final GamaColor color) {
					System.out.println(s);
					if (!canSendMessage(root.getExperiment())) return;

					try {
						sendMessage(
								root.getExperiment(), Jsoner.deserialize("{" + "\"message\": \"" + s + "\","
										+ "\"color\":" + Jsoner.serialize(color) + "}"),
								GamaServerMessage.Type.SimulationOutput);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(root.getExperiment(),
								"{" + "\"message\": \"" + s + "\"," + "\"color\":" + Jsoner.serialize(color) + "}",
								GamaServerMessage.Type.SimulationOutput);
					}
				}

				@Override
				public void debugConsole(final int cycle, final String s, final ITopLevelAgent root,
						final GamaColor color) {
					if (!canSendMessage(root.getExperiment())) return;
					try {
						sendMessage(root.getExperiment(),
								Jsoner.deserialize("{" + "\"cycle\":" + cycle + "," + "\"message\": \""
										+ Jsoner.escape(s) + "\"," + "\"color\":" + Jsoner.serialize(color) + "}"),
								GamaServerMessage.Type.SimulationDebug);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(root.getExperiment(),
								"{" + "\"cycle\":" + cycle + "," + "\"message\": \"" + Jsoner.escape(s) + "\","
										+ "\"color\":" + Jsoner.serialize(color) + "}",
								GamaServerMessage.Type.SimulationDebug);
					}
				}
			};
		}
		return console;

	}

	@Override
	public Map<String, ISocketCommand> getServerCommands() {
		final Map<String, ISocketCommand> cmds = new HashMap<>();
		cmds.put(ISocketCommand.LOAD, new LoadCommand());
		cmds.put(ISocketCommand.PLAY, new PlayCommand());
		cmds.put(ISocketCommand.STEP, new StepCommand());
		cmds.put(ISocketCommand.STEPBACK, new StepBackCommand());
		cmds.put(ISocketCommand.BACK, new StepBackCommand());
		cmds.put(ISocketCommand.PAUSE, new PauseCommand());
		cmds.put(ISocketCommand.STOP, new StopCommand());
		cmds.put(ISocketCommand.RELOAD, new ReloadCommand());
		cmds.put(ISocketCommand.EXPRESSION, new ExpressionCommand());
		cmds.put(ISocketCommand.EVALUATE, new ExpressionCommand());
		cmds.put(ISocketCommand.EXIT, DefaultServerCommands::EXIT);
		cmds.put(ISocketCommand.DOWNLOAD, DefaultServerCommands::DOWNLOAD);
		cmds.put(ISocketCommand.UPLOAD, DefaultServerCommands::UPLOAD);
		return Collections.unmodifiableMap(cmds);
	}

}
