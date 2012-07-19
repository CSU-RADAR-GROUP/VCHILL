package edu.colostate.vchill;

import edu.colostate.vchill.ascope.ViewAScopeWindow;
import edu.colostate.vchill.connection.Controller;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillOldExtTrackInfo;
import edu.colostate.vchill.chill.ChillNewExtTrackInfo;
import edu.colostate.vchill.chill.ChillHeader;
import edu.colostate.vchill.chill.ChillHSKHeader;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.chill.ChillProcessorInfo;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.WindowManager;
import edu.colostate.vchill.numdump.NumDumpWindow;
import edu.colostate.vchill.plot.ViewPlotMethod;
import edu.colostate.vchill.plot.ViewPlotWindow;
import java.awt.EventQueue;
import java.util.LinkedHashSet;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * This is the main class that handles the plots and queues up their requests
 * and manages the stop functionality (still not working). The class shares a
 * number of structures with ViewControl, but there doesn't seem to be a way to
 * help this and avoid massive amounts of message passing.
 * 
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @created June 26, 2003
 * @version 2010-07-07
 */
public class ViewControlThread implements Runnable {
	private static final WindowManager wm = WindowManager.getInstance();
	private static final Config config = Config.getInstance();
	private static final ScaleManager sm = ScaleManager.getInstance();
	private static final LocationManager lm = LocationManager.getInstance();

	/** The current message the thread is using */
	private ControlMessage currMessage;

	/** Used for message passing */
	private final ControlSyncQueue<ControlMessage> queue;

	/** The connection to the server */
	private final Controller controller;

	/** The list of open plot windows */
	private final List<ViewPlotWindow> plots;
	/** The list of open ascope windows */
	private final List<ViewAScopeWindow> ascopes;
	/** The list of open numerical windows */
	private final List<NumDumpWindow> numdumps;
	/** The list of open histogram windows */

	/**
	 * This thread class will handle all of the plotting requests that the user
	 * makes. It takes messages in through a shared
	 * <code>ControlSyncQueue</code> and references the Controller class only
	 * for the <code>getRay</code> method.
	 * 
	 * @param queue
	 *            The shared message queue to recieve plotting requests.
	 * @param controller
	 *            This is the controller class that has the connections to the
	 *            data retrieval methods, the only method that should be used is
	 *            getRay()
	 */
	public ViewControlThread(final ControlSyncQueue<ControlMessage> queue,
			final Controller controller) {
		this.ascopes = wm.getAScopeList();
		this.numdumps = wm.getNumDumpList();
		this.plots = wm.getPlotList();
		this.queue = queue;
		this.controller = controller;
	}

	/**
	 * This is the main loop where the thread attempts to get the first message
	 * that is on the InputData class. If not found it will wait until something
	 * is added. It then goes into the plotting method. NOTE: There may need to
	 * be some form of file checking to leave sweeps behind instead of clearing
	 * the buffers.
	 */
	public void run() {

		Boolean firstRunThrough = true;

		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			currMessage = queue.get();
			if (currMessage == null) {
				continue; // can't do anything without a request
			}

			if (currMessage.getURL() == null || currMessage.getDir() == null
					|| currMessage.getFile() == null
					|| currMessage.getSweep() == null) {
				continue; // can't plot without all fields set
			} else if (ChillDefines.REALTIME_DIR.equals(currMessage.getDir())
					|| ChillDefines.REALTIME_FILE.equals(currMessage.getFile())
					|| ChillDefines.REALTIME_SWEEP.equals(currMessage
							.getSweep())) {
				continue; // plotted as it comes in; don't need to do anything
			} else { // good to go?
				String filter = config.getScanFilter();
				int sweepNum = 0;
				try {
					sweepNum = Integer.parseInt(currMessage.getSweep().split(
							" ")[1]);
				} catch (Exception e) {
					System.out.println("Bad filter");
				} // ignore, can be caused by "Nothing to display"
				int sweepFilter = config.getAutosaveTilt();
				if ((filter == null || currMessage.getFile().contains(filter))
						&& // filename filter
						(sweepFilter == 0 || sweepFilter == sweepNum)) { // sweep
																			// filter
					newPlot();
				} else { // filter enabled, and scan doesn't match
					System.out.println("Skipping sweep");
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							ViewControl.getInstance().sweepDone();
						}
					});
					continue;
				}
			}
		}
	}

	public void handleAircraft(ChillHeader header) {
		final ViewPlotWindow[] plotWins = this.plots
				.toArray(new ViewPlotWindow[this.plots.size()]);

		switch (header.header.recordType) {
		case ChillDefines.TRACK_DATA: // aircraft info
			ChillTrackInfo loc = (ChillTrackInfo) header;
			for (int i = 0; i < plotWins.length; ++i) {
				plotWins[i].plotAircraft(loc);
			}
			break;
		case ChillDefines.OLD_EXT_TRACK_DATA: // extended aircraft info
			ChillOldExtTrackInfo coeti = (ChillOldExtTrackInfo) header;
			for (int i = 0; i < plotWins.length; ++i) {
				plotWins[i].plotAircraft(coeti);
			}
			break;
		case ChillDefines.NEW_EXT_TRACK_DATA: // extended aircraft info
			ChillNewExtTrackInfo cneti = (ChillNewExtTrackInfo) header;
			for (int i = 0; i < plotWins.length; ++i) {
				plotWins[i].plotAircraft(cneti);
			}
			break;
		}
	}

	/**
	 * This method has three major parts. They would be broken up but for all of
	 * the data passing that would be required. First it finds the list of all
	 * the active windows. It then tries to get a ray; if not found it will
	 * sleep and try a few more times. Once the ray is found, it asks for the
	 * threshold field for the sake of interleaving the fields. Finally it sends
	 * the data to the window for actual plotting. Note: This is also where stop
	 * is implemented; the main while loop looks for a stop action in the
	 * ControlSyncQueue queue and if one is found it breaks out of the while
	 * loops.
	 * 
	 * NOTE: Needs to have a better flow diagram.
	 */
	private void newPlot() {
		final ViewPlotWindow[] plotWins = this.plots
				.toArray(new ViewPlotWindow[this.plots.size()]);
		
		System.out.println("NEW PLOT");
		{ // request data & ensure a window is open

			// get threshold field if any window is open
			LinkedHashSet<String> allTypes = new LinkedHashSet<String>();
			String thresh = config.getThresholdType();
			if (thresh != null)
				allTypes.add(thresh);

			// This is used to do the precaching, and data interleaving. The
			// requests queue in the buffer, and the lower levels determine
			// what request to send based on a quick check on the types
			// requested.
			int num_windows = 0;

			// get data needed for plot windows
			for (ViewPlotWindow win : this.plots) {
				++num_windows;
				allTypes.add(win.getType());
			}

			
			// get data needed for ascope windows
			for (ViewAScopeWindow win : this.ascopes) {
				++num_windows;
				allTypes.add(win.getType());
				allTypes.add(win.getSecondary());
			}

			// get data needed for numerical windows
			for (NumDumpWindow win : this.numdumps) {
				++num_windows;
				allTypes.add(win.getType());
			}

			// sm.clear(); //clear data type list; will be recreated from
			// metadata stream
			// System.out.println("Requesting " +
			// currMessage.setType(allTypes));
			System.out.println("Requesting sweep");
			controller.requestSweep(currMessage, allTypes); // always request to
															// ensure we get
															// datatype list
			if (num_windows == 0)
				return; // nothing to display
		}
		System.out.println("Waiting for sweep to finish");
		while (!controller.isSweepDone(currMessage)) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			} // ensure fetchthread gets a chance to see request
		}

		{// now that all the types are coming in, start plotting
			System.out.println("Starting plotting");
			wm.setPlotting(true);
			// final ControlMessage meta =
			// currMessage.setType(ChillDefines.META_TYPE);
			for (int metaNum = 0; /* check in loop */; ++metaNum) {
				if (queue.stopping())
					break;
				ChillHeader header = (ChillHeader) controller.getRayWait(
						currMessage, ChillDefines.META_TYPE, metaNum);
				if (header == null)
					break;
				if (header.header.recordType == ChillDefines.GEN_MOM_DATA) {
					ChillDataHeader dh = (ChillDataHeader) header;
					sm.setAvailable(dh.availableData);
					// System.out.println("requested 0x" +
					// Long.toHexString(dh.requestedData));
					// System.out.println("available 0x" +
					// Long.toHexString(dh.availableData));
					// System.out.println("not avail 0x" +
					// Long.toHexString(dh.requestedData & ~dh.availableData));
					break;
				}
			}
			System.out.println("Checking availability");
			final String[] plotTypes = new String[plotWins.length];
			for (int i = 0; i < plotWins.length; ++i) {
				plotTypes[i] = this.checkAvailability(plotWins[i].getType());
			}

			final ViewAScopeWindow[] ascopeWins = this.ascopes
					.toArray(new ViewAScopeWindow[this.ascopes.size()]);
			final String[][] ascopeTypes = new String[ascopeWins.length][2];
			for (int i = 0; i < ascopeWins.length; ++i) {
				ascopeTypes[i][0] = this.checkAvailability(ascopeWins[i]
						.getType());
				ascopeTypes[i][1] = this.checkAvailability(ascopeWins[i]
						.getSecondary());
			}

			final NumDumpWindow[] numdumpWins = this.numdumps
					.toArray(new NumDumpWindow[this.numdumps.size()]);
			final String[] numdumpTypes = new String[numdumpWins.length];
			for (int i = 0; i < numdumpWins.length; ++i) {
				numdumpTypes[i] = this.checkAvailability(numdumpWins[i]
						.getType());
			}


			
			
		
			
			String threshold = this
					.checkAvailability(config.getThresholdType());

			Ray[] plotPrevRays = new Ray[plotWins.length];
			Ray[] plotCurrRays = new Ray[plotWins.length];
			Ray[] plotNextRays = new Ray[plotWins.length];
			Ray[][] ascopeRays = new Ray[ascopeWins.length][2];
			Ray[] numdumpRays = new Ray[numdumpWins.length];
//			Ray[] histogramRays = new Ray[histogramWins.length];
			Ray threshRay;

			for (ViewPlotWindow win : plotWins) {
				win.clearScreen();
				// clear any existing aircraft info from previous sweeps
				win.clearAircraftInfo();
			}			
			
			int rayNum = 0;
			int metaNum = 0;
			boolean rangeSet = false;
			while (true) {
				if (queue.stopping())
					break;

				ChillHeader header = (ChillHeader) controller.getRayWait(
						currMessage, ChillDefines.META_TYPE, metaNum);
				if (header == null)
					break;

				switch (header.header.recordType) {
				case ChillDefines.GEN_MOM_DATA:
					// System.out.println("0x" +
					// Long.toHexString(((ChillDataHeader)header).requestedData));
					// System.out.println("0x" +
					// Long.toHexString(((ChillDataHeader)header).availableData));

					threshRay = threshold == null ? null : getRayWait(
							currMessage, threshold, rayNum);
					for (int i = 0; i < plotWins.length; ++i) { // get rays /
																// detect end
						if (plotTypes[i] == null)
							continue; // seems to be unavailable
						plotPrevRays[i] = plotCurrRays[i];
						plotCurrRays[i] = getRayWait(currMessage, plotTypes[i],
								rayNum);

						if (plotCurrRays[i] != null) { // null => not available
														// => sweep done?
							if (!rangeSet) {
								config.setMaxPlotRange(plotCurrRays[i]
										.getEndRange() * 1e-6);
								rangeSet = true;
							}
							plotNextRays[i] = getRayWait(currMessage,
									plotTypes[i], rayNum + 1);
							plotWins[i].plot(plotPrevRays[i], plotCurrRays[i],
									plotNextRays[i], threshRay);
						}
					}

					
					for (int i = 0; i < ascopeWins.length; ++i) { // get rays /
																	// detect
																	// end
						if (ascopeTypes[i][0] != null)
							ascopeRays[i][0] = getRayWait(currMessage,
									ascopeTypes[i][0], rayNum);
						if (ascopeRays[i][0] != null) { // null => not available
														// => sweep done?
							if (!rangeSet) {
								config.setMaxPlotRange(ascopeRays[i][0]
										.getEndRange());
								rangeSet = true;
							}
							if (ascopeTypes[i][1] != null)
								ascopeRays[i][1] = getRayWait(currMessage,
										ascopeTypes[i][1], rayNum);
						}
					}
					for (int i = 0; i < numdumpWins.length; ++i) { // get rays /
																	// detect
																	// end
						if (numdumpTypes[i] == null)
							continue; // seems to be unavailable
						numdumpRays[i] = getRayWait(currMessage,
								numdumpTypes[i], rayNum);
						if (numdumpRays[i] != null) { // null => not available
														// => sweep done?
							if (!rangeSet) {
								config.setMaxPlotRange(numdumpRays[i]
										.getEndRange());
								rangeSet = true;
							}
							numdumpWins[i].plot(numdumpRays[i], threshRay);
						}
					}

					
					
/*					for (int i = 0; i < ascopeWins.length; ++i) 
					{ // get rays /
											// detect
											// end
					if (ascopeTypes[i][0] != null)
						ascopeRays[i][0] = getRayWait(currMessage, ascopeTypes[i][0], rayNum);
					if (ascopeRays[i][0] != null) 
					{ // null => not available
								// => sweep done?
						if (!rangeSet) 
						{
							config.setMaxPlotRange(ascopeRays[i][0]
									.getEndRange());
							rangeSet = true;
						}
						if (ascopeTypes[i][1] != null)
							ascopeRays[i][1] = getRayWait(currMessage, ascopeTypes[i][1], rayNum);
					}
					}					
					
*/					

					
					
					
					if (rayNum % config.getRayStep() == 0) {
						for (ViewPlotWindow win : plotWins)
							win.rePlotDrawingArea();
						for (int i = 0; i < ascopeWins.length; ++i) {
							ascopeWins[i].plot(ascopeRays[i][0],
									ascopeRays[i][1]);
							ascopeWins[i].repaint(ascopeWins[i]
									.getVisibleRect());
						}
						Thread.yield();
					}

					++rayNum;
					break;
				case ChillDefines.BRIEF_HSK_DATA: // housekeeping header
					ChillHSKHeader hskh = (ChillHSKHeader) header;
					lm.setLatitude(hskh.radarLatitude * 1e-6);
					lm.setLongitude(hskh.radarLongitude * 1e-6);
					wm.replotOverlay();
					break;
				case ChillDefines.FIELD_SCALE_DATA:
					sm.putScale((ChillMomentFieldScale) header);
					break;
				case ChillDefines.TRACK_DATA: // aircraft info
					// System.out.println("Aircraft data found");
					ChillTrackInfo loc = (ChillTrackInfo) header;
					for (int i = 0; i < plotWins.length; ++i) {
						plotWins[i].plotAircraft(loc);
					}
					break;
				case ChillDefines.OLD_EXT_TRACK_DATA: // extended aircraft info
					ChillOldExtTrackInfo coeti = (ChillOldExtTrackInfo) header;
					for (int i = 0; i < plotWins.length; ++i) {
						plotWins[i].plotAircraft(coeti);
					}
					break;
				case ChillDefines.NEW_EXT_TRACK_DATA: // extended aircraft info
					ChillNewExtTrackInfo cneti = (ChillNewExtTrackInfo) header;
					for (int i = 0; i < plotWins.length; ++i) {
						plotWins[i].plotAircraft(cneti);
					}
					break;
				case ChillDefines.HSK_ID_PROCESSOR_INFO: // processor
															// information
					ChillProcessorInfo procinf = (ChillProcessorInfo) header;
					config.setMaxPlotRange(procinf.range_stop_km);
					rangeSet = true; // don't bother calculating from ray
										// headers
					break;
				case ChillDefines.HSK_ID_END_NOTICE: // end of sweep/volume -
														// archive doesn't send?
					System.out.println("viewcontrolthread: end notice");
					break;
				default: // unkown/unimportant header
					break;
				}

				++metaNum;
			}

			// finish up & handle autosave/export
			for (int i = 0; i < plotWins.length; ++i) {
				final ViewPlotWindow win = plotWins[i];
				win.rePlotDrawingArea();
				Ray ray = plotPrevRays[i];
				if (ray != null) { // saving possible
					if (config.isSaveAllEnabled() || // always save
							ray.getTiltNum() == ray.getSaveTilt()) { // webtilt
						if (config.isImageAutosaveEnabled()) { // image save
																// enabled
							try {
								EventQueue.invokeAndWait(new Runnable() {
									public void run() {
										wm.saveImage(win, currMessage);
									}
								});
							} catch (Exception e) {
								throw new Error(e);
							}
						}
						if (config.isImageAutoExportEnabled()) { // export
																	// enabled
							try {
								EventQueue.invokeAndWait(new Runnable() {
									public void run() {
										wm.export(win, currMessage);
									}
								});
							} catch (Exception e) {
								throw new Error(e);
							}
						}
					}
				}
			}

			// done plotting
			wm.setPlotting(false);
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					System.out.println("Sweep done!");
					ViewControl.getInstance().sweepDone();
				}
			});
		}
		System.out.println("END NEW PLOT");
	}

	/**
	 * Check the availability of a certain data type
	 * 
	 * @param type
	 *            the type to check
	 * @return currMessage.setType(type) or null if unavailable
	 */
	private String checkAvailability(final String type) {
		if (type == null)
			return null;
		System.out.print("Checking availability of " + type + " ...");
		if (controller.getRayWait(currMessage, type, 0, 1000) == null) {
			System.out.println(" not available");
			return null;
		} else {
			System.out.println(" OK");
			return type;
		}
	}

	/**
	 * This method makes the request from the Control object for the actual ray
	 * data.
	 * 
	 * @param msg
	 *            The describing ControlMessage of data that is being requested.
	 * @param rayIndex
	 *            The index of the ray being requested.
	 * @return A Ray of data, or null to signify termination or end of the
	 *         requested volume.
	 */
	public Ray getRay(final ControlMessage msg, final String type,
			final int rayIndex) {
		try {
			return msg == null ? null : (Ray) controller.getRay(msg, type,
					rayIndex);
		} catch (Exception e) {
			System.err.println("Exception in ViewControlThread.getRay: " + e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This is the same as the getRay method with one major difference. If the
	 * ray is not yet available, it will wait for it to come in or for the sweep
	 * to be marked complete.
	 * 
	 * @param msg
	 *            ControlMessage describing the ray that is being requested.
	 * @param rayIndex
	 *            The index of the requested ray.
	 * @return The ray of data that was requested.
	 */
	public Ray getRayWait(final ControlMessage msg, final String type,
			final int rayIndex) {
		return msg == null ? null : (Ray) controller.getRayWait(msg, type,
				rayIndex);
	}

	/**
	 * @param msg
	 *            ControlMessage describing the ray that is being requested.
	 * @param rayIndex
	 *            The index of the requested ray.
	 * @param timeout
	 *            the time (in ms) to wait for the ray to come in if it is not
	 *            yet available
	 * @return The ray of data that was requested.
	 */
	public Ray getRayWait(final ControlMessage msg, final String type,
			final int rayIndex, final long timeout) {
		return msg == null ? null : (Ray) controller.getRayWait(msg, type,
				rayIndex, timeout);
	}
}
