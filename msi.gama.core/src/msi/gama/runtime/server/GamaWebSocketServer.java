/*******************************************************************************************************
 *
 * GamaWebSocketServer.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.runtime.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.SSLParametersWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IMap;
import msi.gama.util.file.json.Jsoner;
import ummisco.gama.dev.utils.DEBUG;

/**
 * The Class GamaWebSocketServer.
 */
public class GamaWebSocketServer extends WebSocketServer {

	/** The Constant DEFAULT_PING_INTERVAL. */
	public static final int DEFAULT_PING_INTERVAL = 10000;

	/**
	 * Start for headless with SSL security on
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to which to listen to
	 * @param runner
	 *            the runner a ThreadPoolExecutor to launch concurrent experiments
	 * @param ssl
	 *            the ssl wether to use ssl or no
	 * @param jksPath
	 *            the jks path the store path
	 * @param spwd
	 *            the spwd the store password
	 * @param kpwd
	 *            the kpwd the key password
	 * @param pingInterval
	 *            the ping interval
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaWebSocketServer StartForSecureHeadless(final int port, final ThreadPoolExecutor runner,
			final boolean ssl, final String jksPath, final String spwd, final String kpwd, final int pingInterval) {
		GamaWebSocketServer server = new GamaWebSocketServer(port, runner, ssl, jksPath, spwd, kpwd, pingInterval);
		try {
			server.start();
			return server;
		} finally {
			server.infiniteLoop();
		}
	}

	/**
	 * Start for headless without SSL
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to listen to
	 * @param runner
	 *            the runner
	 * @param pingInterval
	 *            the ping interval
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaWebSocketServer StartForHeadless(final int port, final ThreadPoolExecutor runner,
			final int pingInterval) {
		GamaWebSocketServer server = new GamaWebSocketServer(port, runner, false, "", "", "", pingInterval);
		try {
			server.start();
			return server;
		} finally {
			server.infiniteLoop();
		}
	}

	/**
	 * Start for GUI. No SSL and a default ping interval
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaWebSocketServer StartForGUI(final int port) {
		return StartForGUI(port, DEFAULT_PING_INTERVAL);
	}

	/**
	 * Start for GUI.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to which to listen to
	 * @param ssl
	 *            the ssl wether to use ssl or no
	 * @param jksPath
	 *            the jks path the store path
	 * @param spwd
	 *            the spwd the store password
	 * @param kpwd
	 *            the kpwd the key password
	 * @param pingInterval
	 *            the ping interval
	 * @return the gama web socket server
	 * @date 16 oct. 2023
	 */
	public static GamaWebSocketServer StartForGUI(final int port, final int pingInterval) {
		GamaWebSocketServer server = new GamaWebSocketServer(port, null, false, "", "", "", pingInterval);
		server.start();
		return server;
	}

	/** The executor. */
	private final ThreadPoolExecutor executor;

	/** The experiments. Only used in the headless version */
	private final Map<String, Map<String, IExperimentPlan>> launchedExperiments = new ConcurrentHashMap<>();

	/** The cmd helper. */
	private final CommandExecutor cmdHelper = new CommandExecutor();

	/** The can ping. false if pingInterval is negative */
	public final boolean canPing;

	/** The ping interval. the time interval between two ping requests in ms */
	public final int pingInterval;

	/** The ping timers. map of all connected clients and their associated timers running ping requests */
	protected final Map<WebSocket, Timer> pingTimers = new HashMap<>();

	/**
	 * Instantiates a new gama web socket server.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param port
	 *            the port to listen to
	 * @param runner
	 *            the runner
	 * @param ssl
	 *            the ssl
	 * @param jksPath
	 *            the jks path
	 * @param spwd
	 *            the spwd
	 * @param kpwd
	 *            the kpwd
	 * @param interval
	 *            the interval
	 * @date 16 oct. 2023
	 */
	private GamaWebSocketServer(final int port, final ThreadPoolExecutor runner, final boolean ssl,
			final String jksPath, final String spwd, final String kpwd, final int interval) {
		super(new InetSocketAddress(port));
		executor = runner;
		canPing = interval >= 0;
		pingInterval = interval;
		if (ssl) { configureWebSocketFactoryWithSSL(jksPath, spwd, kpwd); }
		configureErrorStream();
	}

	/**
	 * Configure error stream so as to broadcast errors
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 oct. 2023
	 */
	private void configureErrorStream() {
		PrintStream errorStream = new PrintStream(System.err) {

			@Override
			public void println(final String x) {
				super.println(x);
				broadcast(Jsoner.serialize(new GamaServerMessage(GamaServerMessage.Type.GamaServerError, x)));
			}
		};
		System.setErr(errorStream);
	}

	/**
	 * Configure web socket factory.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param keyStore
	 *            the jks path
	 * @param spwd
	 *            the spwd
	 * @param kpwd
	 *            the kpwd
	 * @date 16 oct. 2023
	 */
	private void configureWebSocketFactoryWithSSL(final String keyStore, final String storePassword,
			final String keyPassword) {
		// load up the key store
		KeyStore ks;
		try (FileInputStream fis = new FileInputStream(new File(keyStore))) {
			ks = KeyStore.getInstance("JKS");
			ks.load(fis, storePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keyPassword.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(false);
			this.setWebSocketFactory(new SSLParametersWebSocketServerFactory(sslContext, sslParameters));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Infinite loop.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 oct. 2023
	 */
	public void infiniteLoop() {
		try {
			// empty loop to keep alive the server and catch exceptions
			while (true) {}
		} catch (Exception ex) {
			ex.printStackTrace(); // will be broadcasted to every client
		}
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		// DEBUG.OUT(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
		conn.send(Jsoner
				.serialize(new GamaServerMessage(GamaServerMessage.Type.ConnectionSuccessful, "" + conn.hashCode())));
		if (canPing) {
			var timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (conn.isOpen()) { conn.sendPing(); }
				}
			}, 0, pingInterval);
			pingTimers.put(conn, timer);
		}
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		var timer = pingTimers.remove(conn);
		if (timer != null) { timer.cancel(); }
		if (getLaunched_experiments().get("" + conn.hashCode()) != null) {
			for (IExperimentPlan e : getLaunched_experiments().get("" + conn.hashCode()).values()) {
				e.getController().directPause();
				e.getController().dispose();
			}
			getLaunched_experiments().get("" + conn.hashCode()).clear();
		}
		DEBUG.OUT(conn + " has left the room!");
	}

	/**
	 * Extract param.
	 *
	 * @param socket
	 *            the socket
	 * @param message
	 *            the message
	 * @return the i map
	 */
	@SuppressWarnings ("unchecked")
	public IMap<String, Object> extractParam(final WebSocket socket, final String message) {
		IMap<String, Object> map = null;
		try {
			// DEBUG.OUT(socket + ": " + Jsoner.deserialize(message));
			final Object o = Jsoner.deserialize(message);
			if (o instanceof IMap) {
				map = (IMap<String, Object>) o;
			} else {
				map = GamaMapFactory.create();
				map.put(IKeyword.CONTENTS, o);
			}
		} catch (Exception e1) {
			DEBUG.OUT(e1.toString());
			socket.send(Jsoner.serialize(new GamaServerMessage(GamaServerMessage.Type.MalformedRequest, e1)));
		}
		return map;
	}

	@Override
	public void onMessage(final WebSocket socket, final String message) {
		// DEBUG.OUT(socket + ": " + message);
		try {
			IMap<String, Object> map = extractParam(socket, message);
			map.put("server", this);
			DEBUG.OUT(map.get("type"));
			DEBUG.OUT(map.get("expr"));
			final String exp_id = map.get("exp_id") != null ? map.get("exp_id").toString() : "";
			final String socket_id =
					map.get("socket_id") != null ? map.get("socket_id").toString() : "" + socket.hashCode();
			IExperimentPlan exp = getExperiment(socket_id, exp_id);
			SimulationAgent sim = exp != null && exp.getAgent() != null ? exp.getAgent().getSimulation() : null;
			if (sim != null && exp != null && !exp.getController().isPaused()) {
				sim.postOneShotAction(scope1 -> {
					cmdHelper.pushCommand(socket, map);
					return null;
				});
			} else {
				cmdHelper.pushCommand(socket, map);
			}

		} catch (Exception e1) {
			DEBUG.OUT(e1);
			socket.send(Jsoner.serialize(new GamaServerMessage(GamaServerMessage.Type.GamaServerError, e1)));

		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart() {
		DEBUG.LOG("Gama server started on port: " + getPort());
	}

	/**
	 * Gets the all experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the all experiments
	 * @date 15 oct. 2023
	 */
	public Map<String, Map<String, IExperimentPlan>> getAllExperiments() { return launchedExperiments; }

	/**
	 * Gets the experiments of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @return the experiments of
	 * @date 15 oct. 2023
	 */
	public Map<String, IExperimentPlan> getExperimentsOf(final String socket) {
		return launchedExperiments.get(socket);
	}

	/**
	 * Gets the experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param socket
	 *            the socket
	 * @param expid
	 *            the expid
	 * @return the experiment
	 * @date 15 oct. 2023
	 */
	public IExperimentPlan getExperiment(final String socket, final String expid) {
		if (launchedExperiments.get(socket) == null) return null;
		return launchedExperiments.get(socket).get(expid);
	}

	/**
	 * Execute.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executionThread
	 *            the t
	 * @date 15 oct. 2023
	 */
	public void execute(final Runnable command) {
		if (executor == null) { command.run(); }
		executor.execute(command);
	}

	/**
	 * Gets the simulations.
	 *
	 * @return the simulations
	 */
	public Map<String, Map<String, IExperimentPlan>> getLaunched_experiments() { return launchedExperiments; }

}
