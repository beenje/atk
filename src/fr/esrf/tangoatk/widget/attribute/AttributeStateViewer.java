/*
 * AttributeStateViewer.java
 *
 * Created on December 5, 2001, 11:08 AM
 */

package fr.esrf.tangoatk.widget.attribute;
import fr.esrf.tangoatk.core.IAttribute;
import fr.esrf.tangoatk.core.IStateListener;
import fr.esrf.tangoatk.core.*;
import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author  root
 */
public class AttributeStateViewer extends javax.swing.JPanel
    implements IAttributeStateListener {
    IAttribute model;
    JButton label;

    static Map stateMap;

    static {
	stateMap = new HashMap();
	stateMap.put("INVALID", Color.red);
	stateMap.put("ALARM",   Color.orange);
	stateMap.put("VALID",   Color.green);
	stateMap.put("UNKNOWN", Color.gray);
    }

    public static Color getColor4State(String state) {
	return (Color)stateMap.get(state);
    }
						  
    /** Creates new form AttributeStateViewer */
    public AttributeStateViewer() {
	initComponents();
	label = new JButton();
	add(label);
	setVisible(true);
	setState("INVALID");
	label.setVisible(true);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        
        setLayout(new java.awt.BorderLayout());
        
    }//GEN-END:initComponents


    public void setPreferredSize(java.awt.Dimension dimension) {
        label.setPreferredSize(dimension);
    }
    
    public java.awt.Dimension getPreferredSize() {
        return label.getPreferredSize();
    }
    
    public void setBorder(javax.swing.border.Border border) {
	if (label == null) return;
	
        label.setBorder(border);
    }
    
    public javax.swing.border.Border getBorder() {
	if (label == null) 
	    return null;
	
        return label.getBorder();
    }
    
    public void setModel(IAttribute m) {
	model = m;
	setState(m.getState());
	m.addStateListener(this);
    }

    public IAttribute getModel() {
	return model;
    }
    
	
    protected void setState(String s) {
	try {
	    label.setBackground((Color)stateMap.get(s));	     
	} catch (Exception e) {
	    label.setBackground((Color)stateMap.get("INVALID"));
	} // end of try-catch
	

    }

    public void stateChange(AttributeStateEvent e) {
	setState(e.getState());
    }

    public void errorChange(ErrorEvent e) {
	label.setBackground(Color.gray);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
