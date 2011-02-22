/*
 * NumberScalarViewer.java
 *
 */

package fr.esrf.tangoatk.widget.attribute;

import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.properties.*;
import fr.esrf.tangoatk.widget.util.*;

import java.beans.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/**
 *
 * @author  pons
 */

public class NumberScalarWheelEditor extends WheelSwitch
    implements INumberScalarListener, IWheelSwitchListener, PropertyChangeListener {

  INumberScalar model;
  long lastTimeSet = 0;

  // General constructor
  public NumberScalarWheelEditor() {
    model = null;
    addWheelSwitchListener(this);
  }

  public IAttribute getModel() {
    return model;
  }

  // Listen on "setpoint" change
  // this is not clean yet as there is no setpointChangeListener
  // Listen on valueChange and readSetpoint
  public void numberScalarChange(NumberScalarEvent evt) {
    long now = System.currentTimeMillis();

    // Dont update if the user has just click on the wheel
    if ((now - lastTimeSet) > 1000) {
      double set = model.getNumberScalarSetpoint();
      if (getValue() != set) setValue(set);
    }
  }

  public void errorChange(ErrorEvent e) {
    setValue(Double.NaN);
  }

  public void stateChange(AttributeStateEvent e) {
  }

  // Listen change on the WheelSwitch
  public void valueChange(WheelSwitchEvent e) {
    if (model != null) model.setValue(e.getValue());
    lastTimeSet = System.currentTimeMillis();
  }

  public void setModel(INumberScalar m) {

    // Remove old registered listener
    if (model != null) {
      model.removeNumberScalarListener(this);
      model.getProperty("format").removePresentationListener(this);
      model = null;
    }

    if( m==null ) return;

    if (!m.isWritable())
      throw new IllegalArgumentException("NumberScalarWheelEditor: Only accept writeable attribute.");


    model = m;

    // Register new listener
    model.addNumberScalarListener(this);
    model.getProperty("format").addPresentationListener(this);

    setFormat(model.getProperty("format").getPresentation());
    model.refresh();
    double d = model.getNumberScalarValue();
    if (!Double.isNaN(d)) setValue(model.getNumberScalarValue());
  }

  public void propertyChange(PropertyChangeEvent evt) {

    Property src = (Property) evt.getSource();
    if (model != null) {
      if (src.getName().equalsIgnoreCase("format")) {
        setFormat(src.getValue().toString());
        model.refresh();
      }
    }

  }

  public static void main(String[] args) {

    fr.esrf.tangoatk.core.AttributeList attributeList = new
        fr.esrf.tangoatk.core.AttributeList();
    java.awt.GridBagConstraints gridBagConstraints;

    final NumberScalarWheelEditor nsv = new NumberScalarWheelEditor();
    final NumberScalarWheelEditor nsv2 = new NumberScalarWheelEditor();

    try {

      nsv.setFont(new java.awt.Font("Lucida Bright", java.awt.Font.BOLD, 22));
      final INumberScalar attr = (INumberScalar) attributeList.add("jlp/test/1/att_trois");
      nsv.setModel(attr);
      nsv2.setModel(attr);
      attributeList.startRefresher();

    } catch (Exception e) {
      System.out.println(e);
    } // end of try-catch


    javax.swing.JFrame f = new javax.swing.JFrame();

    f.getContentPane().setLayout(new java.awt.GridBagLayout());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    f.getContentPane().add(nsv, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    f.getContentPane().add(nsv2, gridBagConstraints);

    f.pack();
    f.show();
  }


}