/*
 * ImageController.java
 *
 * Created on May 31, 2002, 2:43 PM
 */

package fr.esrf.tangoatk.widget.image;
import fr.esrf.tangoatk.widget.util.*;
import java.awt.*;
import javax.swing.event.*;
/**
 *
 * @author  root
 */
import javax.swing.*;

public class ImageController extends javax.swing.JPanel {

    /** Creates new form ImageController */
    public ImageController() {
        initComponents();

	jTabbedPane1.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent evt) {
		    tabChanged(evt);
		}
	    });

    }

    void tabChanged(ChangeEvent evt) {
	Component comp = jTabbedPane1.getSelectedComponent();
	buttonBar1.setControlee((IControlee)comp);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        colorButtonGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        buttonBar1 = new fr.esrf.tangoatk.widget.util.ButtonBar();
        
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(10, 10, 0, 10);
        gridBagConstraints1.weightx = 0.1;
        gridBagConstraints1.weighty = 0.1;
        add(jTabbedPane1, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(buttonBar1, gridBagConstraints1);
        
    }//GEN-END:initComponents

    public void setModel(IImageViewer viewer) {
        model = viewer;
    }    

    public void addToPanel(IImagePanel panel) {
	jTabbedPane1.add(panel.getComponent(), panel.getName());
	buttonBar1.setControlee((IControlee)jTabbedPane1.getSelectedComponent());
    }
    
    protected IImageViewer model;    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup colorButtonGroup;
    private javax.swing.JTabbedPane jTabbedPane1;
    private fr.esrf.tangoatk.widget.util.ButtonBar buttonBar1;
    // End of variables declaration//GEN-END:variables

}