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

//
// $Id: Long64ScalarHelper.java 15105 2010-09-24 13:11:54Z poncet $
//
// Description:


package fr.esrf.tangoatk.core.attribute;


import fr.esrf.tangoatk.core.*;
import fr.esrf.TangoApi.*;
import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.DevFailed;

import java.util.List;
import java.util.Vector;

public class Long64ScalarHelper extends ANumberScalarHelper {

  public Long64ScalarHelper(AAttribute attribute) {
    init(attribute);
  }

  @Override
  void init(AAttribute attribute) {
    super.init(attribute);
  }

  void insert(DeviceAttribute da,double d) {
    double dUnitFactor = 1.0;

    dUnitFactor = this.attribute.getDisplayUnitFactor();

    if (dUnitFactor == 1.0) {
      da.insert((long) d);
    } else {
      long di = (long) (d / dUnitFactor);
      da.insert(di);
    }
  }


  double getNumberScalarValue(DeviceAttribute devAtt) {
    long[] long_arr = null;

    try {
      long_arr = devAtt.extractLong64Array();
    } catch (DevFailed e) {
      return Double.NaN;
    }

    if (long_arr == null)
      return Double.NaN;

    if (long_arr.length < 1)
      return Double.NaN;

    return long_arr[0];
  }

  double getNumberScalarSetPoint(DeviceAttribute devAtt) {
    long[] long_arr = null;

    try {
      long_arr = devAtt.extractLong64Array();
    } catch (DevFailed e) {
      return Double.NaN;
    }

    if (long_arr == null)
      return Double.NaN;

    if (long_arr.length < 1)
      return Double.NaN;

    if (long_arr.length > 1)
      return long_arr[1];
    else // The attributes WRITE (WRITE ONLY) return their setPoint in the first element
      return long_arr[0];
  }

  double getNumberScalarDisplayValue(DeviceAttribute devAtt) {
    long[] long_arr = null;
    double dUnitFactor;

    dUnitFactor = this.attribute.getDisplayUnitFactor();

    try {
      long_arr = devAtt.extractLong64Array();
    } catch (DevFailed e) {
      return Double.NaN;
    }

    if (long_arr == null)
      return Double.NaN;

    if (long_arr.length < 1)
      return Double.NaN;

    return (long_arr[0] * dUnitFactor);
  }

  double getNumberScalarDisplaySetPoint(DeviceAttribute devAtt) {

    long[] long_arr = null;
    double dUnitFactor;

    dUnitFactor = this.attribute.getDisplayUnitFactor();

    try {
      long_arr = devAtt.extractLong64Array();
    } catch (DevFailed e) {
      return Double.NaN;
    }

    if (long_arr == null)
      return Double.NaN;

    if (long_arr.length < 1)
      return Double.NaN;

    if (long_arr.length > 1)
      return (long_arr[1] * dUnitFactor);
    else // The attributes WRITE (WRITE ONLY) return their setPoint in the first element
      return (long_arr[0] * dUnitFactor);
  }


  protected INumberScalarHistory[] getNumberScalarAttHistory(DeviceDataHistory[] attPollHist) {

    List<NumberScalarHistory> hist;
    NumberScalarHistory histElem;
    fr.esrf.Tango.AttrQuality attq;
    int i;
    double dUnitFactor = 1.0;

    if (attPollHist.length <= 0)
      return null;

    dUnitFactor = this.attribute.getDisplayUnitFactor();
    if (dUnitFactor <= 0)
      dUnitFactor = 1.0;

    hist = new Vector<NumberScalarHistory>();

    for (i = 0; i < attPollHist.length; i++) {
      histElem = new NumberScalarHistory();


      try {
        histElem.setTimestamp(attPollHist[i].getTime());
      } catch (Exception ex) {
        histElem.setTimestamp(0);
      }


      try {
        attq = attPollHist[i].getAttrQuality();

        if (AttrQuality._ATTR_VALID == attq.value()) {
          histElem.setState(IAttribute.VALID);
        } else {
          if (AttrQuality._ATTR_INVALID == attq.value()) {
            histElem.setState(IAttribute.INVALID);
          } else {
            if (AttrQuality._ATTR_ALARM == attq.value()) {
              histElem.setState(IAttribute.ALARM);
            } else {
              if (AttrQuality._ATTR_WARNING == attq.value()) {
                histElem.setState(IAttribute.WARNING);
              } else {
                if (AttrQuality._ATTR_CHANGING == attq.value()) {
                  histElem.setState(IAttribute.CHANGING);
                } else
                  histElem.setState(IAttribute.UNKNOWN);
              }
            }
          }
        }

      } catch (Exception ex) {
        histElem.setState(IAttribute.UNKNOWN);
      }


      try {
        long intVal;
        double dblVal;
        intVal = attPollHist[i].extractLong64();
        dblVal = ((double) intVal) * dUnitFactor;
        histElem.setValue(dblVal);
      } catch (Exception ex) {
        histElem.setValue(Double.NaN);
      }

      hist.add(i, histElem);
    }

    NumberScalarHistory[] histArray;

    //histArray = (NumberScalarHistory[]) hist.toArray(new NumberScalarHistory[0]);
    histArray = hist.toArray(new NumberScalarHistory[0]);

    return histArray;
  }


  protected INumberScalarHistory[] getNumberScalarDeviceAttHistory(DeviceDataHistory[] attPollHist) {

    List<NumberScalarHistory> hist;
    NumberScalarHistory histElem;
    fr.esrf.Tango.AttrQuality attq;
    int i;

    if (attPollHist.length <= 0)
      return null;

    hist = new Vector<NumberScalarHistory>();

    for (i = 0; i < attPollHist.length; i++) {
      histElem = new NumberScalarHistory();


      try {
        histElem.setTimestamp(attPollHist[i].getTime());
      } catch (Exception ex) {
        histElem.setTimestamp(0);
      }


      try {
        attq = attPollHist[i].getAttrQuality();

        if (AttrQuality._ATTR_VALID == attq.value()) {
          histElem.setState(IAttribute.VALID);
        } else {
          if (AttrQuality._ATTR_INVALID == attq.value()) {
            histElem.setState(IAttribute.INVALID);
          } else {
            if (AttrQuality._ATTR_ALARM == attq.value()) {
              histElem.setState(IAttribute.ALARM);
            } else {
              if (AttrQuality._ATTR_WARNING == attq.value()) {
                histElem.setState(IAttribute.WARNING);
              } else {
                if (AttrQuality._ATTR_CHANGING == attq.value()) {
                  histElem.setState(IAttribute.CHANGING);
                } else
                  histElem.setState(IAttribute.UNKNOWN);
              }
            }
          }
        }

      } catch (Exception ex) {
        histElem.setState(IAttribute.UNKNOWN);
      }


      try {
        long intVal;
        double dblVal;
        intVal = attPollHist[i].extractLong64();
        dblVal = (double) intVal;
        histElem.setValue(dblVal);
      } catch (Exception ex) {
        histElem.setValue(Double.NaN);
      }

      hist.add(i, histElem);
    }

    NumberScalarHistory[] histArray;

    //histArray = (NumberScalarHistory[]) hist.toArray(new NumberScalarHistory[0]);
    histArray = hist.toArray(new NumberScalarHistory[0]);

    return histArray;
  }

  protected IAttributeScalarHistory[] getScalarDeviceAttHistory(DeviceDataHistory[] attPollHist) {
    return (getNumberScalarDeviceAttHistory(attPollHist));
  }

  protected IAttributeScalarHistory[] getScalarAttHistory(DeviceDataHistory[] attPollHist) {
    return (getNumberScalarAttHistory(attPollHist));
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

  void setMinWarning(double d) {
    setProperty("min_warning", new Long((long) d));
  }

  void setMaxWarning(double d) {
    setProperty("max_warning", new Long((long) d));
  }

  void setDeltaT(double d) {
    setProperty("delta_t", new Long((long) d));
  }

  void setDeltaVal(double d) {
    setProperty("delta_val", new Long((long) d));
  }

  void setMinWarning(double d, boolean writable) {
    setProperty("min_warning", new Long((long) d), writable);
  }

  void setMaxWarning(double d, boolean writable) {
    setProperty("max_warning", new Long((long) d), writable);
  }

  void setDeltaT(double d, boolean writable) {
    setProperty("delta_t", new Long((long) d), writable);
  }

  void setDeltaVal(double d, boolean writable) {
    setProperty("delta_val", new Long((long) d), writable);
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


  public String getVersion() {
    return "$Id: Long64ScalarHelper.java 15105 2010-09-24 13:11:54Z poncet $";
  }
}
