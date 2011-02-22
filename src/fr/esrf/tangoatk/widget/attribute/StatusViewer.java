// File:          StatusViewer.java
// Created:       2005-02-14 18:15:00, poncet
// By:            <poncet@esrf.fr>
//
// $Id$
//
// Description:

package fr.esrf.tangoatk.widget.attribute;

import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.core.attribute.*;
import fr.esrf.tangoatk.core.ErrorEvent;
import fr.esrf.tangoatk.widget.util.ATKConstant;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author  root
 */
public class StatusViewer extends javax.swing.JPanel implements IStringScalarListener
{
    private javax.swing.JScrollPane   jScrollPane1;
    private javax.swing.JTextArea     status;
    private IStringScalar             model;
    private boolean                   useDeviceAlias = true;

    /** Creates new form StatusViewer */

    public StatusViewer()
    {
      model = null;
      initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
	jScrollPane1 = new javax.swing.JScrollPane();
	status = new javax.swing.JTextArea();

	setLayout(new java.awt.BorderLayout());

	setBorder(new javax.swing.border.TitledBorder("Status"));
	jScrollPane1.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	status.setLineWrap(true);
	status.setEditable(false);
	status.setColumns(50);
	status.setRows(4);
	status.setText("Unknown");
	status.setBackground(new java.awt.Color(204, 204, 204));
	jScrollPane1.setViewportView(status);

	add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }

    public void setModel(IStringScalar statusAtt)
    {
	if (model != null)
	{
	    model.removeStringScalarListener(this);
	    setBorder(new javax.swing.border.TitledBorder("Status"));
	}
                
	status.setText("Unknown");
        
        model = statusAtt;
	if( model!=null )
	{
	    model.addStringScalarListener(this);
	    //model.refresh();
	    setStatus(model.getStringDeviceValue());
            if (useDeviceAlias)
	    {
	       if (model.getDevice().getAlias() != null)
		  setBorder(new javax.swing.border.TitledBorder(model.getDevice().getAlias()));
	       else
		  setBorder(new javax.swing.border.TitledBorder(model.getDevice().getName()));
	    }
	    else
	       setBorder(new javax.swing.border.TitledBorder(model.getDevice().getName()));
	}
    }

    /**
     * <code>getModel</code> gets the model of this statusViewer.
     *
     * @return a <code>IStringScalar</code> value
     */
    public IStringScalar getModel()
    {
        return model;
    }


    public int getRows()
    {
        return status.getRows();
    }

    public void setRows(int rows)
    {
        status.setRows(rows);
    }

    public int getColumns()
    {
        return status.getColumns();
    }

    public void setColumns(int columns)
    {
        status.setColumns(columns);
    }

    /**
     * <code>getUseDeviceAlias</code> returns true if the device alias is displayed instead of device name
     *
     * @return a <code>boolean</code> value
     */
    public boolean getUseDeviceAlias() {
      return useDeviceAlias;
    }

    /**
     * <code>setUseDeviceAlias</code> use or not use device alias
     *
     * @param b True to enable device alias usage.
     */
    public void setUseDeviceAlias(boolean b) {
      useDeviceAlias=b;
    }


    public JTextArea getText()
    {
        return status;
    }
    
    public void stringScalarChange(StringScalarEvent evt)
    {
        if (!status.isEnabled())
           status.setEnabled(true);
	   
	setStatus(evt.getValue());
    }

    public void errorChange(ErrorEvent evt)
    {
        setStatus(IDevice.UNKNOWN);
	status.setEnabled(false);
    }
    
    public void stateChange(AttributeStateEvent attributeStateEvent)
    {
    	attStateChange(attributeStateEvent.getState());    
    }



  /* javax.swing.JTextArea:setText(String) method has a memory
  leak on SUN Solaris JVM (seems to be OK on windows)
  The statusChange method is called each time the status is read by
  the refresher even if it has not changed. This will be changed in the
  future when the Tango Events will be used instead of ATK refreshers.
  For the time being a test has been added to limit the memory leak.
  Modified by F. Poncet  on 22/septembre/2003

      public void setStatus(String s) {
          status.setText(s);
      }

      */

    private void setStatus(String s)
    {
      if (s == null)
      {
        if ( "".equals(status.getText()) || status.getText() == null )
          return;
        else
          status.setText("");
      }
      else if (s.equals(status.getText())) 
        return;
      else
        status.setText(s);
    }
	
    private void attStateChange(String state)
    {
	if ("VALID".equals(state))
	{
	    status.setBackground(getBackground());
	    return;
	}
	status.setBackground(ATKConstant.getColor4Quality(state));
    }
    

    public static void main(String[] args)
    {
       final fr.esrf.tangoatk.core.AttributeList  attList = new fr.esrf.tangoatk.core.AttributeList();
       IStringScalar                              attStatus;
       JFrame                                     mainFrame;
       
       StatusViewer                                stsv = new StatusViewer();


       try
       {
          attStatus = (IStringScalar) attList.add("dev/test/10/Status");
	  stsv.setModel(attStatus);
       }
       catch (Exception ex)
       {
          System.out.println("caught exception : "+ ex.getMessage());
	  System.exit(-1);
       }
       
       mainFrame = new JFrame();
       
       mainFrame.addWindowListener(
	       new java.awt.event.WindowAdapter()
			  {
			      public void windowActivated(java.awt.event.WindowEvent evt)
			      {
				 // To be sure that the refresher (an independente thread)
				 // will begin when the the layout manager has finished
				 // to size and position all the components of the window
				 attList.startRefresher();
			      }
			  }
                                     );
				     

       mainFrame.setContentPane(stsv);
       mainFrame.pack();

       mainFrame.show();

    } // end of main ()

}
