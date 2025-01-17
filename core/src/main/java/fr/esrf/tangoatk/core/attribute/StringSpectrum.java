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
 
// File:          StringSpectrum.java
// Created:       2003-12-11 18:00:00, poncet
// By:            <poncet@esrf.fr>
//
// $Id$
//
// Description:

package fr.esrf.tangoatk.core.attribute;

import fr.esrf.tangoatk.core.*;

import fr.esrf.Tango.*;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoApi.events.*;

public class StringSpectrum extends AAttribute
  implements IStringSpectrum {

  StringSpectrumHelper   stringSpectHelper;
  String[]               spectrumValue = null;
  String[]               spectrumSetPointValue = null;

  public StringSpectrum() {
    stringSpectHelper = new StringSpectrumHelper(this);
  }


  public void setStringSpectrumValue(String[] s) {
    try {
      DeviceAttribute da = new DeviceAttribute(getNameSansDevice());
      da.insert(s);
      writeAtt(da);
      refresh();
    } catch (DevFailed df) {
      setAttError("Couldn't set value", new AttributeSetException(df));
    }
  }


  public String[] getStringSpectrumValue() {
    return spectrumValue;
  }
  
  // getStringSpectrumSetPoint returns the attribute's setpoint value
  public String[] getStringSpectrumSetPoint()
  {
      return spectrumSetPointValue;
  }


  public void addListener(IStringSpectrumListener l) {
    stringSpectHelper.addStringSpectrumListener(l);
    addStateListener(l);
  }

  public void removeListener(IStringSpectrumListener l) {
    stringSpectHelper.removeStringSpectrumListener(l);
    removeStateListener(l);
  }


  public void refresh()
  {
      DeviceAttribute da = null;

//    if (skippingRefresh) return;
      refreshCount++;
      try
      {
          try
          {
              // Retreive the value from the device
              // Read the attribute from device cache (readValueFromNetwork)
              da = readValueFromNetwork();
              // Retreive the read value for the attribute
              spectrumValue = stringSpectHelper.getStringSpectrumValue(da);
              // Retreive the setPoint value for the attribute
              spectrumSetPointValue = stringSpectHelper.getStringSpectrumSetPoint(da);

              // Fire valueChanged
              fireValueChanged(spectrumValue);
          }
          catch (DevFailed e)
          {
              // Tango error
              spectrumValue = null;
              spectrumSetPointValue = null;

              // Fire error event
              readAttError(e.getMessage(), new AttributeReadException(e));
          }
          catch (java.lang.Error err)
          {
              spectrumValue = null;
              spectrumSetPointValue = null;

              // Fire error event
              readAttError(err.getMessage(), new AttributeReadException(err));
          }
      }
      catch (Throwable th)
      {
         // Code failure
          spectrumValue = null;
          spectrumSetPointValue = null;
          
          System.out.println("StringSpectrum.refresh() Throwable caught ------------------------------");
          th.printStackTrace();
          System.out.println("StringSpectrum.refresh()------------------------------------------------");
      }
  }

  public void dispatch(DeviceAttribute attValue) {

//    if (skippingRefresh) return;
    refreshCount++;
    try {

      try {
        // symetric with refresh
        if (attValue == null) return;
        attribute = attValue;

        setState(attValue);
        timeStamp = attValue.getTimeValMillisSec();

        // Retreive the read value for the attribute
        spectrumValue = stringSpectHelper.getStringSpectrumValue(attValue);
        // Retreive the setPoint value for the attribute
        spectrumSetPointValue = stringSpectHelper.getStringSpectrumSetPoint(attValue);

        // Fire valueChanged
        fireValueChanged(spectrumValue);

      } 
      catch (DevFailed e)
      {
        dispatchError(e);
      }

    } catch (Exception e) {

      // Code failure
      spectrumValue = null;
      spectrumSetPointValue = null;

      System.out.println("StringSpectrum.dispatch() Exception caught ------------------------------");
      e.printStackTrace();
      System.out.println("StringSpectrum.dispatch()------------------------------------------------");

    }

  }

  public void dispatchError(DevFailed e) {

    // Tango error
    spectrumValue = null;
    spectrumSetPointValue = null;

    // Fire error event
    readAttError(e.getMessage(), new AttributeReadException(e));

  }

  // Implement the method of ITangoPeriodicListener
  public void periodic (TangoPeriodicEvent evt) 
  {
      periodicCount++;
      if(evt.isZmqEvent()) eventType=2; else eventType=1;
      DeviceAttribute     da=null;
      long t0 = System.currentTimeMillis();

      trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodic method called for " + getName(), t0);

      try
      {
          da = evt.getValue();
          trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodicEvt.getValue(" + getName() + ") success", t0);
      }
      catch (DevFailed  dfe)
      {
          trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodicEvt.getValue(" + getName() + ") failed, caught DevFailed", t0);
          if (dfe.errors[0].reason.equals("API_EventTimeout")) //heartbeat error
	  {
             trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodicEvt.getValue(" + getName() + ") failed, got heartbeat error", t0);
             // Tango error
             spectrumValue = null;
             spectrumSetPointValue = null;

             // Fire error event
             readAttError(dfe.getMessage(), new AttributeReadException(dfe));
	  }
	  else // For the moment the behaviour for all DevFailed is the same
	  {
             trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodicEvt.getValue(" + getName() + ") failed, got other error", t0);
             // Tango error
             spectrumValue = null;
             spectrumSetPointValue = null;

             // Fire error event
             readAttError(dfe.getMessage(), new AttributeReadException(dfe));
	  }
          return;
      }
      catch (Exception e) // Code failure
      {
          trace(DeviceFactory.TRACE_PERIODIC_EVENT, "StringSpectrum.periodicEvt.getValue(" + getName() + ") failed, caught Exception, code failure", t0);
	  spectrumValue = null;
          spectrumSetPointValue = null;

	  System.out.println("StringSpectrum.periodic.getValue() Exception caught ------------------------------");
	  e.printStackTrace();
	  System.out.println("StringSpectrum.periodic.getValue()------------------------------------------------");
          return;
      } // end of catch


      // read the attribute value from the received event!      
      if (da != null)
      {
            try 
            {
               setState(da); // To set the quality factor and fire AttributeState event
               attribute = da;
               timeStamp = da.getTimeValMillisSec();
               // Retreive the read value for the attribute
               spectrumValue = stringSpectHelper.getStringSpectrumValue(da);
               // Retreive the setPoint value for the attribute
               spectrumSetPointValue = stringSpectHelper.getStringSpectrumSetPoint(da);
               // Fire valueChanged
               fireValueChanged(spectrumValue);
            }
            catch (DevFailed dfe)
            {
               // Tango error
               spectrumValue = null;
               spectrumSetPointValue = null;

               // Fire error event
               readAttError(dfe.getMessage(), new AttributeReadException(dfe));
            } 
            catch (Exception e) // Code failure
            {
               spectrumValue = null;
               spectrumSetPointValue = null;

               System.out.println("StringSpectrum.periodic.extractStringArray() Exception caught ------------------------------");
               e.printStackTrace();
               System.out.println("StringSpectrum.periodic.extractStringArray()------------------------------------------------");
            } // end of catch
      }
      
  }
 
  
  
  
  // Implement the method of ITangoChangeListener
  public void change (TangoChangeEvent evt) 
  {
      changeCount++;
      if(evt.isZmqEvent()) eventType=2; else eventType=1;
      DeviceAttribute     da=null;
      long t0 = System.currentTimeMillis();

      trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.change method called for " + getName(), t0);

      try
      {
          da = evt.getValue();
          trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.changeEvt.getValue(" + getName() + ") success", t0);
      }
      catch (DevFailed  dfe)
      {
          trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.changeEvt.getValue(" + getName() + ") failed, caught DevFailed", t0);
          if (dfe.errors[0].reason.equals("API_EventTimeout")) //heartbeat error
	  {
             trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.changeEvt.getValue(" + getName() + ") failed, got heartbeat error", t0);
             // Tango error
             spectrumValue = null;
             spectrumSetPointValue = null;

             // Fire error event
             readAttError(dfe.getMessage(), new AttributeReadException(dfe));
	  }
	  else // For the moment the behaviour for all DevFailed is the same
	  {
             trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.changeEvt.getValue(" + getName() + ") failed, got other error", t0);
             // Tango error
             spectrumValue = null;
             spectrumSetPointValue = null;

             // Fire error event
             readAttError(dfe.getMessage(), new AttributeReadException(dfe));
	  }
          return;
      }
      catch (Exception e) // Code failure
      {
          trace(DeviceFactory.TRACE_CHANGE_EVENT, "StringSpectrum.changeEvt.getValue(" + getName() + ") failed, caught Exception, code failure", t0);
	  spectrumValue = null;
          spectrumSetPointValue = null;

	  System.out.println("StringSpectrum.change.getValue() Exception caught ------------------------------");
	  e.printStackTrace();
	  System.out.println("StringSpectrum.change.getValue()------------------------------------------------");
          return;
      } // end of catch


      // read the attribute value from the received event!      
      if (da != null)
      {
            try 
            {
               setState(da); // To set the quality factor and fire AttributeState event
               attribute = da;
               timeStamp = da.getTimeValMillisSec();
               // Retreive the read value for the attribute
               spectrumValue = stringSpectHelper.getStringSpectrumValue(da);
               // Retreive the setPoint value for the attribute
               spectrumSetPointValue = stringSpectHelper.getStringSpectrumSetPoint(da);
               // Fire valueChanged
               fireValueChanged(spectrumValue);
            } 
            catch (DevFailed dfe) 
            {
               // Tango error
               spectrumValue = null;
               spectrumSetPointValue = null;

               // Fire error event
               readAttError(dfe.getMessage(), new AttributeReadException(dfe));
            } 
            catch (Exception e) // Code failure
            {
               spectrumValue = null;
               spectrumSetPointValue = null;

               System.out.println("StringSpectrum.change.extractStringArray() Exception caught ------------------------------");
               e.printStackTrace();
               System.out.println("StringSpectrum.change.extractStringArray()------------------------------------------------");
            } // end of catch
      }
      
  }


  private void trace(int level,String msg,long time)
  {
    DeviceFactory.getInstance().trace(level,msg,time);
  }
  

  public boolean isWritable() {
    return super.isWritable();
  }


  protected void fireValueChanged(String[] newValue) {
    propChanges.fireStringSpectrumEvent(this, newValue, timeStamp);
  }


  public String getVersion() {
    return "$Id$";
  }

  private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
    System.out.print("Loading attribute ");
    in.defaultReadObject();
    serializeInit();
  }

  public boolean isScalar() {
    return false;
  }

  public boolean isSpectrum(){
    return true;
  }

  public boolean isImage(){
    return false;
  }


}
