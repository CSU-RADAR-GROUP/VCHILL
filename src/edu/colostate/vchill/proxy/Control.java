package edu.colostate.vchill.proxy;

import java.io.IOException;

/**
 * Interface allowing for shutting down a proxy connection
 *
 * @author Jochen Deyke
 * @version 2004-12-10
 */
interface Control {
    /**
     * Removes the connection associated with <code>pdt</code> from the active
     * list and closes the connection to the client, which will cause all other
     * ProxyDataThreads associated with that connection to terminate.
     *
     * @param pdt the ProxyDataThread whose connection is to be terminated
     * @throws IOException if a problem is encountered
     */
    public void killConnection(final ProxyDataThread pdt) throws IOException;

    /**
     * Is the Proxy this Control is associated with supposed to calculate
     * the composite data types?
     *
     * @return true if the proxy should calculate data types
     * or false if it should request everything from the server
     */
    public boolean getCalcFlag();
}
