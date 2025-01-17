/*
 *  Copyright (C) :	2002,2003,2004,2005,2006,2007,2008,2009
 *			European Synchrotron Radiation Facility
 *			BP 220, Grenoble 38043
 *			FRANCE
 * 
 *  This file is part of Tango.
 * 
 *  Tango is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Tango is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Tango.  If not, see <http://www.gnu.org/licenses/>.
 */
// $Id$
//
// Description:

package fr.esrf.tangoatk.core;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevSource;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;

/**
 * An object that maps to a Tango Device. It is able to produce the attributes
 * and the commands belonging to this Device.
 * <p>
 * Normally a Device object is obtained through the
 * {@link  fr.esrf.tangoatk.core.DeviceFactory}to make sure that we do not
 * create two instances of the same physical device. The device sends out three
 * events, a Status-change event, a state-change event, and an error event.
 * Objects interested in receiving such events must register as listeners.
 */
public class Device extends DeviceProxy implements IDevice, Serializable {

    public static final String OPEN_CLOSE_PROP = "OpenCloseInverted";
    public static final String INSERT_EXTRACT_PROP = "InsertExtractInverted";
    
    private String name;
    private boolean supportsEvents;
    //protected String alias;
    protected EventSupport propChanges;
    transient protected DeviceProxy proxy;
    private long refreshCount = 0;
    private int idlVersion = 0;
    private boolean        connected = true;
    protected Map<String,DeviceProperty>  propertyMap;
    private boolean openCloseInverted=false;
    private boolean insertExtractInverted=false;
    protected boolean devPropertiesLoaded=false;
    
    private static Map<String, DevState> stringStateMap;

    static
    {
       stringStateMap = new HashMap<String, DevState> ();
       stringStateMap.put(IDevice.ON, DevState.from_int(DevState._ON));
       stringStateMap.put(IDevice.OFF, DevState.from_int(DevState._OFF));
       stringStateMap.put(IDevice.CLOSE, DevState.from_int(DevState._CLOSE));
       stringStateMap.put(IDevice.OPEN, DevState.from_int(DevState._OPEN));
       stringStateMap.put(IDevice.INSERT, DevState.from_int(DevState._INSERT));
       stringStateMap.put(IDevice.EXTRACT, DevState.from_int(DevState._EXTRACT));
       stringStateMap.put(IDevice.MOVING, DevState.from_int(DevState._MOVING));
       stringStateMap.put(IDevice.STANDBY, DevState.from_int(DevState._STANDBY));
       stringStateMap.put(IDevice.FAULT, DevState.from_int(DevState._FAULT));
       stringStateMap.put(IDevice.INIT, DevState.from_int(DevState._INIT));
       stringStateMap.put(IDevice.RUNNING, DevState.from_int(DevState._RUNNING));
       stringStateMap.put(IDevice.ALARM, DevState.from_int(DevState._ALARM));
       stringStateMap.put(IDevice.DISABLE, DevState.from_int(DevState._DISABLE));
       stringStateMap.put(IDevice.UNKNOWN, DevState.from_int(DevState._UNKNOWN));
    }
    
    
    
    /**
     * Creates a new <code>Device</code> instance.
     * 
     * @param name
     *            a <code>String</code> containing the name of the device to
     *            connect to.
     * @exception DevFailed
     *                if an error occurs
     * @see fr.esrf.TangoApi.DeviceProxy#DeviceProxy
     */
    public Device(String name) throws DevFailed {
        super(name);
        init(name);
    }

    public Device(String name, String host, String port) throws DevFailed {
        super(name, host, port);
        init(name);
    }

    public Device(String name, boolean connectionLess) throws DevFailed
    {
        super(name);
        long t0 = System.currentTimeMillis();
        if (connectionLess == false)
            init(name);
        else
        {
            try
            {
                init(name);
            }
            catch (DevFailed df)
            {

                trace(DeviceFactory.TRACE_FAIL, "Device.get_idl_version(" + name
                        + ") failed", t0);
                setConnected(false);
            }
        }
    }

    protected void init(String name) throws DevFailed
    {
        long t0 = System.currentTimeMillis();

        propertyMap = new HashMap<String,DeviceProperty>();
        
        //refreshPropertyMap(); Do not refresh in init : performance issues

        propChanges = new EventSupport();
        this.name = name;
        this.supportsEvents = false;
        this.openCloseInverted = false;
        this.insertExtractInverted = false;

        // Check if the device is "event compatible"
        idlVersion = get_idl_version();
        trace(DeviceFactory.TRACE_SUCCESS, "Device.get_idl_version(" + name
                + ") ok", t0);
        if (idlVersion >= 3) // all idl versions >= 3 are event compatible
            this.supportsEvents = true;
    }

    public void reconnect()
    {
        long t0 = System.currentTimeMillis();
        // Check if the device is "event compatible"
        try
        {
            idlVersion = get_idl_version();
            setConnected(true);
            trace(DeviceFactory.TRACE_SUCCESS, "Device.reconnect get_idl_version(" + name
                    + ") ok", t0);
            if (idlVersion >= 3) // all idl versions >= 3 are event compatible
                this.supportsEvents = true;
        }
        catch (DevFailed dfe)
        {
            trace(DeviceFactory.TRACE_FAIL, "Device.reconnect get_idl_version(" + name
                    + ") failed", t0);
        }
    }

    public boolean isConnected()
    {
        return connected;
    }

    private void setConnected(boolean b)
    {
        connected = b;
    }

    public void addErrorListener(IErrorListener l) {
        propChanges.addErrorListener(l);
    }

    public void removeErrorListener(IErrorListener l) {
        propChanges.removeErrorListener(l);
    }

    /**
     * <code>addStatusListener</code> adds a listener to status-events
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     * @see fr.esrf.tangoatk.core.IDeviceListener
     */
    public void addStatusListener(IStatusListener l) {
        propChanges.addStatusListener(l);
    }

    /**
     * <code>removeStatusListener</code> removes a status listener
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     */
    public void removeStatusListener(IStatusListener l) {
        propChanges.removeStatusListener(l);
    }

    /**
     * <code>addStateListener</code> adds a listener to state-events
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     */
    public void addStateListener(IStateListener l) {
        propChanges.addStateListener(l);
    }

    /**
     * <code>removeStateListener</code> removes a listener to a state-events
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     */
    public void removeStateListener(IStateListener l) {
        propChanges.removeStateListener(l);
    }

    /**
     * <code>addListener</code> adds a listener to all device events
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     */
    public void addListener(IDeviceListener l) {
        propChanges.addStateListener(l);
        propChanges.addStatusListener(l);
    }

    /**
     * <code>removeListener</code> removes a listener to this device.
     * 
     * @param l
     *            an <code>IDeviceListener</code> value
     */
    public void removeListener(IDeviceListener l) {
        propChanges.removeStateListener(l);
        propChanges.removeStatusListener(l);
    }

    /**
     * Returns the EventSupport object which manages ATKEvents for this device.
     * @return EventSupport object
     */
    public EventSupport getPropChanges() {
        return propChanges;
    }

    /**
     * <code>refresh</code> sends out status and state events. This forces a
     * synchronous device state and status reading.
     */
    public void refresh()
    {
        if (!isConnected())
            reconnect();
        if (isConnected())
        {
            refreshCount++;
            DevState s = null;
            String newStatus;
            long t0 = System.currentTimeMillis();
            try
            {
                try // Get the state
                {

                    s = state(ApiDefs.FROM_CMD);
                    trace(DeviceFactory.TRACE_STATE_REFRESHER,
                            "Device.refresh(State," + name + ") success", t0);
                    propChanges.fireStateEvent(this, toString(s));
                }
                catch (DevFailed ex)
                {

                    trace(DeviceFactory.TRACE_STATE_REFRESHER,
                            "Device.refresh(State," + name + ") failed", t0);
                    ConnectionException e = new ConnectionException(ex);
                    deviceError("Couldn't read state: ", e);
                    newStatus = getName() + ":\n" + e.getDescription();
                    propChanges.fireStateEvent(this, IDevice.UNKNOWN);
                    propChanges.fireStatusEvent(this, newStatus);
                    return;
                }

                t0 = System.currentTimeMillis();

                // Get the status
                try
                {

                    newStatus = status(ApiDefs.FROM_CMD);
                    trace(DeviceFactory.TRACE_STATE_REFRESHER,
                            "Device.refresh(Status," + name + ") success", t0);
                    propChanges.fireStatusEvent(this, newStatus);

                }
                catch (DevFailed ex)
                {

                    trace(DeviceFactory.TRACE_STATE_REFRESHER,
                            "Device.refresh(Status," + name + ") failed", t0);
                    ConnectionException e = new ConnectionException(ex);
                    newStatus = getName() + ":\n" + e.getDescription();
                    propChanges.fireStatusEvent(this, newStatus);
                    return;

                }
            }
            catch (Exception ex)
            {
                // Code failure
                System.out.println("-- Device.refresh() : Unexpected exception -----------------------");
                ex.printStackTrace();

                // Try to fire a deviceError event if execption has
                // happened if JavaApi.
                try
                {
                    ConnectionException e = new ConnectionException(ex);
                    propChanges.fireStateEvent(this, IDevice.UNKNOWN);
                    propChanges.fireStatusEvent(this, IDevice.UNKNOWN);
                    deviceError("Couldn't read state: ", e);
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    /**
     * <code>isAlive</code> checks to see if this device is reachable
     * 
     * @return a <code>boolean</code> value which is true if the device is
     *         reachable, false otherwise
     * @see fr.esrf.TangoApi.Connection#ping
     */
    public boolean isAlive() {

        long t0 = System.currentTimeMillis();

        try {
            ping();
            trace(DeviceFactory.TRACE_SUCCESS, "Device.ping(" + name
                    + ") success", t0);
            return true;
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.ping(" + name + ") failed",
                    t0);
            return false;
        }

    }

    /**
     * <code>getCommandList</code> returns the list of commands known to this
     * device
     * 
     * @return a <code>DevCmdInfo[]</code> value
     * @exception DevFailed
     *                if an error occurs
     */
    public CommandInfo[] getCommandList() throws DevFailed {

        long t0 = System.currentTimeMillis();
        CommandInfo[] ret;

        try {
            ret = command_list_query();
            trace(DeviceFactory.TRACE_SUCCESS, "Device.command_list_query("
                    + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.command_list_query(" + name
                    + ") failed", t0);
            throw e;
        }

        return ret;
    }

    /**
     * <code>getCommand</code> returns a CommandInfo corresponding to the
     * given name.
     * 
     * @param name
     *            Command name
     * @return CommandInfo
     * @throws DevFailed
     *             in case of failure.
     */
    public CommandInfo getCommand(String name) throws DevFailed {
        CommandInfo ret;
        long t0 = System.currentTimeMillis();

        try {
            ret = command_query(name);
            trace(DeviceFactory.TRACE_SUCCESS, "Device.command_query("
                    + getName() + "/" + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.command_query(" + getName()
                    + "/" + name + ") failed", t0);
            throw e;
        }

        return ret;
    }

    /**
     * <code>getAttributeInfo</code> returns the attribute configs for the
     * attributes named in the parameter.
     * 
     * @param name
     *            a <code>String[]</code> value containing the names of the
     *            attributes we want the configs of.
     * @return an <code>AttributeConfig[]</code> value
     * @exception DevFailed
     *                if an error occurs
     */
    public AttributeInfoEx[] getAttributeInfo(String[] name) throws DevFailed {

        long t0 = System.currentTimeMillis();
        AttributeInfoEx[] ret;

        try {
            if( name==null )
              ret = get_attribute_info_ex();
            else
              ret = get_attribute_info_ex(name);
            trace(DeviceFactory.TRACE_SUCCESS, "Device.get_attribute_info_ex("
                    + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.get_attribute_info_ex(" + name
                    + ") failed", t0);
            throw e;
        }

        return ret;
    }

    /**
     * <code>getAttributeInfo</code> gets the attribute config of an attribute
     * 
     * @param name
     *            a <code>String</code> value containing the name of the
     *            attribute we want the config of.
     * @return an <code>AttributeConfig</code> value
     * @exception DevFailed
     *                if an error occurs
     * @see #getAttributeInfo(String[] name)
     */
    public AttributeInfoEx getAttributeInfo(String name) throws DevFailed {
        AttributeInfoEx ret;
        long t0 = System.currentTimeMillis();

        try {
            ret = get_attribute_info_ex(AEntityFactory.extractEntityName(name));
            trace(DeviceFactory.TRACE_SUCCESS, "Device.get_attribute_config("
                    + getName() + "/" + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.get_attribute_info("
                    + getName() + "/" + name + ") failed", t0);
            throw e;
        }

        return ret;

    }

    /**
     * <code>getName</code> returns the name of this device.
     * 
     * @return a <code>String</code> value containing the name of the device.
     */
    public String getName() {
        return name;
    }

    /**
     * <code>getAlias</code> returns the alias of this device.
     * 
     * @return a <code>String</code> value containing the alias of the device.
     */
    public String getAlias() {
        String al;
        try {
            al = get_alias();
            return al;
        } catch (Exception ex) {
            return null;
        }
    }

    /*
     * public void setAlias(String alias) { this.alias = alias; }
     */

    /**
     * <code>deviceError</code> fires of an error event from this device
     * 
     * @param s
     *            a <code>String</code> value containing an error message.
     * @param t
     *            a <code>Throwable</code> value containing the exception
     *            which caused this error message to be called.
     */
    protected void deviceError(String s, Throwable t) {
        propChanges.fireReadErrorEvent(this, t);
    }

    /**
     * <code>getState</code> returns the state of the device as a string.
     * 
     * @return a <code>String</code> value containing the states.
     * @see fr.esrf.Tango.DevState for information on which states can bee
     *      returned.
     */
    public String getState() {

        DevState ret = null;
        long t0 = System.currentTimeMillis();

        try {
            ret = state(ApiDefs.FROM_CMD);
            trace(DeviceFactory.TRACE_SUCCESS, "Device.state(" + name
                    + ") success", t0);
        } catch (Exception e) {
            trace(DeviceFactory.TRACE_FAIL,
                    "Device.state(" + name + ") failed", t0);
            deviceError("Couldn't read state: ", new ConnectionException(e));
        }

        return toString(ret);
    }

    public static String toString(DevState st) {

        if (st == null)
            return IDevice.UNKNOWN;

        switch (st.value()) {
        case DevState._ON:
            return IDevice.ON;
        case DevState._OFF:
            return IDevice.OFF;
        case DevState._CLOSE:
            return IDevice.CLOSE;
        case DevState._OPEN:
            return IDevice.OPEN;
        case DevState._INSERT:
            return IDevice.INSERT;
        case DevState._EXTRACT:
            return IDevice.EXTRACT;
        case DevState._MOVING:
            return IDevice.MOVING;
        case DevState._STANDBY:
            return IDevice.STANDBY;
        case DevState._FAULT:
            return IDevice.FAULT;
        case DevState._INIT:
            return IDevice.INIT;
        case DevState._RUNNING:
            return IDevice.RUNNING;
        case DevState._ALARM:
            return IDevice.ALARM;
        case DevState._DISABLE:
            return IDevice.DISABLE;
        default:
            return IDevice.UNKNOWN;
        } // end of switch ()

    }

    public static DevState getStateFromString(String st)
    {
        if (st == null)
            return DevState.from_int(DevState._UNKNOWN);

        if (stringStateMap.containsKey(st))
            return stringStateMap.get(st);
        else
            return DevState.from_int(DevState._UNKNOWN);
    }
    
    /**
     * <code>getStatus</code> returns the status of the device. returns
     * UNKNOWN and sets an error if a DevFailed is thrown while obtaining the
     * status.
     * 
     * @return a <code>String</code> value containing the status.
     */
    public String getStatus() {

        String ret = IDevice.UNKNOWN;
        long t0 = System.currentTimeMillis();

        try {
            ret = status(ApiDefs.FROM_CMD);
            trace(DeviceFactory.TRACE_SUCCESS, "Device.status(" + name
                    + ") success", t0);
        } catch (Exception e) {
            trace(DeviceFactory.TRACE_FAIL, "Device.status(" + name
                    + ") failed", t0);
            deviceError("Couldn't read status: ", new ConnectionException(e));
        }
        return ret;
    }

    /**
     * <code>readAttribute</code> reads the value of an attribute. It is made
     * final to be a bit quicker.
     * 
     * @param name
     *            a <code>String</code> value
     * @return a <code>DeviceAttribute</code> value
     * @exception DevFailed
     *                if an error occurs
     */
    public final DeviceAttribute readAttribute(String name) throws DevFailed {
        DeviceAttribute da;
        long t0 = System.currentTimeMillis();

        try {
            da = read_attribute(AEntityFactory.extractEntityName(name));
            trace(DeviceFactory.TRACE_ATTREFRESHER, "Device.read_attribute("
                    + getName() + "/" + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_ATTREFRESHER, "Device.read_attribute("
                    + getName() + "/" + name + ") failed", t0);
            throw e;
        }
        return da;
    }

    /**
     * Returns the number of call to refresh()
     * @return Refresh count
     */
    public long getRefreshCount() {
        return refreshCount;
    }

    /**
     * Returns the IDL version number of this device.
     */
    public int getIdlVersion() {
        return idlVersion;
    }

    /**
     * <code>readAttributeFromDevice</code> reads the value of an attribute.
     * Force the reading from the tango device (ignore polling buffer).
     * 
     * @param name
     *            a <code>String</code> value
     * @return a <code>DeviceAttribute</code> value
     * @exception DevFailed
     *                if an error occurs
     */
    public final DeviceAttribute readAttributeFromDevice(String name)
            throws DevFailed {
        DeviceAttribute da;
        long t0 = System.currentTimeMillis();

        set_source(DevSource.DEV);

        try {
            da = read_attribute(AEntityFactory.extractEntityName(name));
            trace(DeviceFactory.TRACE_ATTREFRESHER,
                    "Device.read_attribute_from_device(" + getName() + "/"
                            + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_ATTREFRESHER,
                    "Device.read_attribute_from_device(" + getName() + "/"
                            + name + ") failed", t0);
            // Reset to default
            set_source(DevSource.CACHE_DEV);
            throw e;
        }

        // Reset to default
        set_source(DevSource.CACHE_DEV);
        return da;
    }

    /**
     * <code>writeAttribute</code> writes an attribute.
     * 
     * @param a
     *            a <code>DeviceAttribute</code> value to write.
     * @exception DevFailed
     *                if an error occurs
     */
    public void writeAttribute(DeviceAttribute a) throws DevFailed {

        long t0 = System.currentTimeMillis();

        try {
            write_attribute(a);
            trace(DeviceFactory.TRACE_ATTREFRESHER, "Device.write_attribute("
                    + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_ATTREFRESHER, "Device.write_attribute("
                    + name + ") failed", t0);
            throw e;
        }

        refresh();

    }

    /**
     * <code>executeCommand</code>
     * 
     * @param command
     *            a <code>String</code> value containing the name of the
     *            command, obtained by getCommandList();
     * @param argin
     *            a <code>DeviceData</code> value holding the in argument
     * @return a <code>DeviceData</code> value containing the result
     * @exception DevFailed
     *                if an error occurs
     */
    public DeviceData executeCommand(String command, DeviceData argin)
            throws DevFailed {

        DeviceData data = null;
        long t0 = System.currentTimeMillis();

        try {
            data = command_inout(AEntityFactory.extractEntityName(command),
                    argin);
            trace(DeviceFactory.TRACE_COMMAND, "Device.command_inout("
                    + getName() + "/" + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_COMMAND, "Device.command_inout("
                    + getName() + "/" + name + ") failed", t0);
            throw e;
        }

        // refresh(); removed to avoid overloading each command_inout, with
        // two other command_inout one for State another for Status
        return data;
    }

    /**
     * <code>storeInfo</code>
     *
     * @param c an <code>AttributeInfo</code> value to store
     * @exception DevFailed if an error occurs
     */
    public void storeInfo(AttributeInfoEx c) throws DevFailed {
        AttributeInfoEx[] ac = { c };
        long t0 = System.currentTimeMillis();

        try {
            set_attribute_info(ac);
            trace(DeviceFactory.TRACE_ATTREFRESHER,
                    "Device.set_attribute_info(" + name + ") success", t0);
        } catch (DevFailed e) {
            trace(DeviceFactory.TRACE_ATTREFRESHER,
                    "Device.set_attribute_info(" + name + ") failed", t0);
            throw e;
        }
    }

    public String toString() {
        return getName();
    }

    /**
     * Returns true if this device supports event.
     */
    public boolean doesEvent() {
        return supportsEvents;
    }

    public String getVersion() {
        return "$Id$";
    }

    private void trace(int level, String msg, long time) {
        DeviceFactory.getInstance().trace(level, msg, time);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        System.out.print("Storing device " + name + "...");
        out.defaultWriteObject();
        System.out.println("Done");
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        System.out.print("Loading device ");
        in.defaultReadObject();
        System.out.print(name + "...");
        try {
            proxy = new DeviceProxy(name);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } // end of try-catch
        System.out.println("Done");

    }

    /* (non-Javadoc)
     * @see fr.esrf.tangoatk.core.IDevice#getProperty(String propertyName)
     */
    public DeviceProperty getProperty(String propertyName) {
        if (propertyMap!=null){
            //return (DeviceProperty) propertyMap.get(propertyName);
            return propertyMap.get(propertyName);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see fr.esrf.tangoatk.core.IDevice#getPropertyMap()
     */
    public Map getPropertyMap() {
        return propertyMap;
    }

    /**
     * Stores a property in database
     * @param propertyName the name of the property to store
     */
    public void storeProperty(String propertyName) {
        //DeviceProperty property = (DeviceProperty) propertyMap.get(propertyName);
        DeviceProperty property = propertyMap.get(propertyName);
        if (property != null) {
            try {
                DbDatum dbProperty = get_property(property.getName());
                dbProperty.insert(property.getValue());
                put_property(dbProperty);
            }
            catch (DevFailed e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Could not update property " + property.getName(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        else {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not update property " + property.getName()
                     + " : \nThis property is not registered",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * You can use this method if you added or removed a property for this
     * device by using jive. This forces the <code>Device</code> to refresh
     * its <code>Map</code> of properties (<code>DeviceProperty</code>).
     */
    public void refreshPropertyMap() {
        String[] propnames = null;
        DbDatum[] data = new DbDatum[0];
        try {
            propnames = get_property_list("*");
            if (propnames != null) {
                if (propnames.length > 0) {
                    data = get_property(propnames);
                    if (data == null){
                        data = new DbDatum[0];
                    }
                }
            }

            /*
             * Adds the new properties, updates the existing ones
             */
            //HashSet nameSet = new HashSet();
            HashSet<String> nameSet = new HashSet<String> ();
            for (int i=0; i< data.length; i++) {
                nameSet.add(data[i].name);
                //DeviceProperty property = (DeviceProperty) propertyMap.get(data[i].name);
                DeviceProperty property = propertyMap.get(data[i].name);
                if (property == null) {
                    property = new DeviceProperty(this, data[i].name, data[i].extractStringArray());
                    propertyMap.put(data[i].name,property);
                }
                else {
                    property.setValue(data[i].extractStringArray());
                }
            }// for (int i=0; i< data.length; i++)

            /*
             * Removes the no more existing properties
             */
            //Set keySet = propertyMap.keySet();
            Set<String> keySet = propertyMap.keySet();
            if (keySet != null) {
                //Iterator it = keySet.iterator();
                Iterator<String> it = keySet.iterator();
                while (it.hasNext()) {
                    //String name = (String) it.next();
                    String name = it.next();
                    if (!nameSet.contains(name)) {
                        propertyMap.remove(name);
                    }// end if (!nameSet.contains(name))
                }// end while (it.hasNext())
            }// end if (keySet != null)
        }// end try
        catch (DevFailed df) {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to update property map" ,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }// end catch (DevFailed df)
    }// end refreshPropertyMap()


    /**
     * This method is provided to ATK applications so that they don't need to
     * import fr.esrf.TangoApi.DeviceProxy to get the timeout for the
     * connection to the device.
     * @return Device timeout
     * @throws ConnectionException In case of failure
     */
    public int getDevTimeout() throws ConnectionException
    {
       long t0 = System.currentTimeMillis();
       
       try
       {
	  int    tmout_ms = get_timeout_millis();
	  trace(DeviceFactory.TRACE_SUCCESS, "Device.getDevTimeout(" + name+ ") success", t0);
          return  tmout_ms;
       }
       catch (DevFailed de)
       {
	  trace(DeviceFactory.TRACE_FAIL, "Device.getDevTimeout(" + name+ ") failed", t0);
	  ConnectionException  ce = new ConnectionException(de);
	  deviceError("Couldn't read timeout value : ", ce);
	  throw ce;
       }
       catch (Exception ex)
       {
	  trace(DeviceFactory.TRACE_FAIL, "Device.getDevTimeout(" + name+ ") failed", t0);
	  ConnectionException  ce = new ConnectionException(ex);
	  deviceError("Couldn't read timeout value : ", ce);
	  throw ce;
       }
    }




    /**
     * This method is provided to ATK applications so that they don't need to
     * import fr.esrf.TangoApi.DeviceProxy to set the timeout for the
     * connection to the device.
     * @param tmout_ms Timeout in millisecond
     * @throws ConnectionException In case of failure
     */
    synchronized public void setDevTimeout(int  tmout_ms) throws ConnectionException
    {
       long t0 = System.currentTimeMillis();
       
       try
       {
	  set_timeout_millis(tmout_ms);
	  trace(DeviceFactory.TRACE_SUCCESS, "Device.setDevTimeout(" + name+ ") success", t0);
       }
       catch (DevFailed de)
       {
	  trace(DeviceFactory.TRACE_FAIL, "Device.setDevTimeout(" + name+ ") failed", t0);
	  ConnectionException  ce = new ConnectionException(de);
	  deviceError("Couldn't set timeout value : ", ce);
	  throw ce;
       }
       catch (Exception ex)
       {
	  trace(DeviceFactory.TRACE_FAIL, "Device.setDevTimeout(" + name+ ") failed", t0);
	  ConnectionException  ce = new ConnectionException(ex);
	  deviceError("Couldn't set timeout value : ", ce);
	  throw ce;
       }
    }


    public AtkEventListenerList getListenerList() {
      if (propChanges == null) return null;
      else return propChanges.getListenerList();
    }
    
    
    public boolean getInvertedOpenClose()
    {
       return openCloseInverted;
    }
  
    public boolean getInvertedInsertExtract()
    {
       return insertExtractInverted;
    }

    
    void setInvertedOpenClose(boolean b)
    {
       openCloseInverted = b;
    }
  
    void setInvertedInsertExtract(boolean b)
    {
       insertExtractInverted = b;
    }
    
    public boolean areDevPropertiesLoaded()
    {
        return devPropertiesLoaded;
    }
    
    public void loadDevProperties()
    {
        long         t0 = System.currentTimeMillis();
        String[]     propList = {OPEN_CLOSE_PROP, INSERT_EXTRACT_PROP};
        DbDatum[]    propDbDatums=null;
        
       
        // Get the open/close inverted and insert/extract inverted properties from the Tango DB
        try
        {
            devPropertiesLoaded = true;
            propDbDatums = this.get_property(propList);
        } 
        catch (DevFailed dfe) 
        {
            trace(DeviceFactory.TRACE_FAIL, "Device.get_property(" + OPEN_CLOSE_PROP + ", " + INSERT_EXTRACT_PROP
                    + ") failed", t0);
        }

        if (propDbDatums == null) return;
        
        if (propDbDatums.length != 2) return;
        
        // Check if the colors should be inverted for open and close states
        if (!propDbDatums[0].is_empty())
           openCloseInverted = propDbDatums[0].extractBoolean();

        // Check if the colors should be inverted for insert and extract states 
        if (!propDbDatums[1].is_empty())
           insertExtractInverted = propDbDatums[1].extractBoolean();
        
    }
    
}




