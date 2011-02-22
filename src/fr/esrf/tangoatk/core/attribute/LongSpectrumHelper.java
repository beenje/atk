// File:          LongSpectrumHelper.java
// Created:       2002-01-24 10:02:46, assum
// By:            <assum@esrf.fr>
// Time-stamp:    <2002-06-24 16:56:32, assum>
//
// $Id$
//
// Description:
package fr.esrf.tangoatk.core.attribute;

import fr.esrf.tangoatk.core.*;


import fr.esrf.Tango.*;
import fr.esrf.TangoApi.*;

import java.beans.*;

public class LongSpectrumHelper extends ANumberSpectrumHelper {

  public LongSpectrumHelper(IAttribute attribute) {
    init(attribute);
  }

  void init(IAttribute attribute) {
    super.init(attribute);
    imageHelper = new LongImageHelper(attribute);
  }

/* Modified to add support for display_unit property
  protected void insert(double[] d) {
    int[] tmp = new int[d.length];
    for (int i = 0; i < tmp.length; i++) {
      tmp[i] = new Double(d[i]).intValue();
    }

    deviceAttribute.insert(tmp,
      ((IAttribute) attribute).getXDimension(),
      ((IAttribute) attribute).getYDimension());
  }
*/

  void insert(double[] d)
  {
      double   dUnitFactor=1.0;
      int[]    tmp = new int[d.length];
      
      dUnitFactor = this.attribute.getDisplayUnitFactor();
      DeviceAttribute da = this.attribute.getAttribute();
      
      for (int i = 0; i < tmp.length; i++)
      {
           tmp[i] = (int) (d[i] / dUnitFactor);
      }

      da.insert(tmp, this.attribute.getXDimension(),
	             this.attribute.getYDimension()  );
  }


  void setMinAlarm(double d) {
    setProperty("min_alarm", new Long((long) d));
  }

  void setMaxAlarm(double d) {
    setProperty("max_alarm", new Long((long) d));
  }

  void setMinValue(double d) {
    setProperty("min_value", new Long((long) d));
  }

  void setMaxValue(double d) {
    setProperty("max_value", new Long((long) d));
  }

  void setMinAlarm(double d, boolean writable) {
    setProperty("min_alarm", new Long((long) d), writable);
  }

  void setMaxAlarm(double d, boolean writable) {
    setProperty("max_alarm", new Long((long) d), writable);
  }

  void setMinValue(double d, boolean writable) {
    setProperty("min_value", new Long((long) d), writable);
  }

  void setMaxValue(double d, boolean writable) {
    setProperty("max_value", new Long((long) d), writable);
  }


  double[] getNumberSpectrumValue(DeviceAttribute deviceAttribute) throws DevFailed {
      int[] tmp = deviceAttribute.extractLongArray();
      double[] retval = new double[tmp.length];
      for (int i = 0; i < tmp.length; i++) {
        retval[i] = (double) tmp[i];
      }
      return retval;
  }

  double[] getNumberSpectrumDisplayValue(DeviceAttribute deviceAttribute) throws DevFailed
  {
     int[]     tmp;
     double    dUnitFactor;
     double[]  retSpectVal;

     tmp = deviceAttribute.extractLongArray();
     dUnitFactor = this.attribute.getDisplayUnitFactor();
     retSpectVal = new double[tmp.length];
     
     for (int i = 0; i < tmp.length; i++)
     {
         retSpectVal[i] = (double) tmp[i] * dUnitFactor; //return the value in the display unit
     }
     return retSpectVal;
  }

  public String getVersion() {
    return "$Id$";
  }
}
