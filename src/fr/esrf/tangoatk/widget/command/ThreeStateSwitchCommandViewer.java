package fr.esrf.tangoatk.widget.command;

import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.util.ThreeStateSwitch;
import fr.esrf.Tango.DevState;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

/** A ThreeStateSwitchCommandViewer is a 3 states viewer associated with
 * 2 commands (one to switch on the device , one to switch it off) and one
 * scalar attribute which represents the state (Tango::ON or Tanog::OFF)
 * The viewer can also display an undefined state (when different from ON or OFF)
 */
public class ThreeStateSwitchCommandViewer extends ThreeStateSwitch implements ActionListener, INumberScalarListener {


  ICommand onCmd=null;
  ICommand offCmd=null;
  INumberScalar stateAtt=null;

  // ---------------------------------------------------
  // Contruction
  // ---------------------------------------------------
  public ThreeStateSwitchCommandViewer() {

    addActionListener(this);

  }

  public ThreeStateSwitchCommandViewer(String title, Font tFont) {

    super(title, tFont);
    addActionListener(this);

  }

  // ---------------------------------------------------
  // Property stuff
  // ---------------------------------------------------
  public void setModel(INumberScalar stateAttribute,
                       ICommand onCommand,
                       ICommand offCommand) {

    if (stateAtt != null) {
      stateAtt.removeNumberScalarListener(this);
      onCmd.removeErrorListener(this);
      offCmd.removeErrorListener(this);
      stateAtt=null;
    }

    if (stateAttribute != null) {
      stateAtt = stateAttribute;
      onCmd = onCommand;
      offCmd = offCommand;

      stateAtt.addNumberScalarListener(this);
      onCmd.addErrorListener(this);
      offCmd.addErrorListener(this);
    }

  }

  public void clearModel() {
    setModel(null,null,null);
  }

  // ---------------------------------------------------
  // Action listener
  // ---------------------------------------------------
  public void actionPerformed(ActionEvent e) {

    switch(getState()) {
      case ThreeStateSwitch.ON_STATE:
        //Switch on the device
        onCmd.execute();
        break;
      case ThreeStateSwitch.OFF_STATE:
        offCmd.execute();
        break;
      case ThreeStateSwitch.UNKNOWN_STATE:
        //TODO
        break;
    }

  }

  // ---------------------------------------------------
  // Scalar listener
  // ---------------------------------------------------
  public void errorChange(ErrorEvent e) {
    if(e.getSource()==onCmd) {
      JOptionPane.showMessageDialog(this,"Failed to switch on:\n" + e.getError().getMessage());
    } else if(e.getSource() == offCmd) {
      JOptionPane.showMessageDialog(this, "Failed to switch off:\n" + e.getError().getMessage());
    } else if(e.getSource() == stateAtt ) {
      setState(ThreeStateSwitch.UNKNOWN_STATE);
    }
  }

  public void stateChange(AttributeStateEvent e) {
  }

  public void numberScalarChange(NumberScalarEvent e) {

    if (e.getSource() == stateAtt) {
      int s = (int) e.getNumberSource().getNumberScalarValue();
      switch(s) {
        case DevState._ON:
          setState(ThreeStateSwitch.ON_STATE);
          break;
        case DevState._OFF:
          setState(ThreeStateSwitch.OFF_STATE);
          break;
        default:
          setState(ThreeStateSwitch.UNKNOWN_STATE);
          break;
      }
    }

  }

  // ---------------------------------------------------
  // Main test fucntion
  // ---------------------------------------------------
  static public void main(String args[]) {
    JFrame f = new JFrame();
    JPanel jp = new JPanel();
    ThreeStateSwitchCommandViewer stv = new ThreeStateSwitchCommandViewer();
    stv.setBorder(BorderFactory.createEtchedBorder());
    jp.add(stv);
    f.setContentPane(jp);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setVisible(true);
  }

}