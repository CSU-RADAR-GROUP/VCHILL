package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillMomentFieldScale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

/**
 * Manages scaling information for the various data types.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-11-10
 */
public class ScaleManager extends Observable {
    private static ScaleManager sm = new ScaleManager();

    /**
     * Maps server urls to server scale managers.
     */
    private HashMap<String, ServerScaleManager> urlMap;

    private ScaleManager() {
        this.urlMap = new HashMap<String, ServerScaleManager>();
    }

    public static ScaleManager getInstance() {
        return sm;
    }

    /**
     * Removes ALL types from the list of known types
     * for a particular server.
     */
    public synchronized void clear() {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Clearing fields for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager != null) {
            serverScaleManager.clear();
            setChanged();
            notifyObservers();
        }
    }

    public synchronized void switchServer() {
        setChanged();
        notifyObservers();
    }

    /**
     * Removes all types whose fieldNumber does not appear in the given mask
     *
     * @param availableMask the bitmask of available data types
     */
    public synchronized void setAvailable(final long availableMask) {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Setting available for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager == null) {
            serverScaleManager = new ServerScaleManager(serverURL);
            this.urlMap.put(serverURL, serverScaleManager);
        }

        serverScaleManager.setAvailableMask(availableMask);
    }

    /**
     * Adds a <code>ChillMomentFieldScale</code> to the list of known types.
     * If the type already exists (as determined by a duplicate fieldName or fieldNumber),
     * first the new type is updated with the old types current max and min, then the old is replaced.
     *
     * @param scale the <code>ChillMomentFieldScale</code> to add
     */
    public synchronized void putScale(final ChillMomentFieldScale scale) {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Putting scale " + scale.fieldName + " for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager == null) {
            serverScaleManager = new ServerScaleManager(serverURL);
            this.urlMap.put(serverURL, serverScaleManager);
        }

        ChillMomentFieldScale oldScale = serverScaleManager.getScale(scale.fieldName);
        if (oldScale != null) { //already have a field with that name
            ConfigUtil.put(oldScale.fieldName + " Scale Max", oldScale.getMax());
            ConfigUtil.put(oldScale.fieldName + " Scale Min", oldScale.getMin());
            scale.setMin(oldScale.getMin());
            scale.setMax(oldScale.getMax());
        } else { //new data type
            scale.setMax(ConfigUtil.getDouble(scale.fieldName + " Scale Max", scale.maxValue / (double) scale.factor));
            scale.setMin(ConfigUtil.getDouble(scale.fieldName + " Scale Min", scale.minValue / (double) scale.factor));
            setChanged();
        }

        serverScaleManager.putScale(scale);
        /* Only notify observers if this field is actually available. */
        if (((1l << scale.fieldNumber) & serverScaleManager.getAvailableMask()) != 0)
            notifyObservers();
    }

    /**
     * Attempts to remove a type from the list of known types.
     * If that type does not exist, nothing happens.
     */
    public synchronized void removeScale(final ChillMomentFieldScale scale) {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Removing scale " + scale.fieldName + " for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager != null) {
            //only set changed if a scale was actually removed
            if (serverScaleManager.removeScale(scale) != null)
                setChanged();
        }

        notifyObservers();
    }

    public synchronized ChillMomentFieldScale getScale(final String name) {
        if (name == null)
            return null;

        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Getting scale " + name + " for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager != null) {
            return serverScaleManager.getScale(name);
        }
        return null;
    }

    public synchronized ChillMomentFieldScale getScale(final int number) {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Getting scale " + number + " for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager != null) {
            return serverScaleManager.getScale(number);
        }
        return null;
    }

    public synchronized ArrayList<String> getTypes() {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Getting types for " + serverURL);
        ServerScaleManager serverScaleManager = this.urlMap.get(serverURL);
        if (serverScaleManager != null) {
            return serverScaleManager.getTypes();
        }
        return new ArrayList<String>();
    }

    /**
     * Saves all currently known types' max and min to the VCHILL <code>Preferences</code> node
     */
    public synchronized void savePreferences() {
        ViewControl vc = ViewControl.getInstance();
        String serverURL = vc.getControlMessage().getURL();
        //System.out.println("Saving preferences for " + serverURL);
        //try saving the preferences for the last connected server
        ServerScaleManager lastServerScaleManager = this.urlMap.get(serverURL);
        if (lastServerScaleManager != null) {
            for (ChillMomentFieldScale scale : lastServerScaleManager.getScales()) {
                ConfigUtil.put(scale.fieldName + " Scale Max", scale.getMax());
                ConfigUtil.put(scale.fieldName + " Scale Min", scale.getMin());
            }
        } else {
            //there will probably be multiple scales with the same name but
            //they should have the same settings and so this code should be safe
            for (ServerScaleManager serverScaleManager : this.urlMap.values()) {
                for (ChillMomentFieldScale scale : serverScaleManager.getScales()) {
                    ConfigUtil.put(scale.fieldName + " Scale Max", scale.getMax());
                    ConfigUtil.put(scale.fieldName + " Scale Min", scale.getMin());
                }
            }
        }
    }
}
