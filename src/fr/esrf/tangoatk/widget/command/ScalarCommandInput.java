/*
 * ScalarCommandInput.java
 *
 * Created on January 16, 2002, 4:48 PM
 */

package fr.esrf.tangoatk.widget.command;
import fr.esrf.tangoatk.core.ICommand;
/**
 *
 * @author  root
 */
public class ScalarCommandInput extends javax.swing.JPanel
    implements IInput {
    javax.swing.JComponent parent;
    /** Creates new form ScalarCommandInput */

    public ScalarCommandInput(ICommand command) {
	this();
	setModel(command);
    }
    
    public ScalarCommandInput() {
        initComponents();
    }

    public String getInput() {
	return jTextField1.getText();
    }

    public void setInput(String input) {
	jTextField1.setText(input);
    }
	

    public void setModel(ICommand command) {

    }

    public ICommand getModel() {

	return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        executeButton = new javax.swing.JButton();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        jLabel1.setText("Input");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints1);
        
        jTextField1.setPreferredSize(new java.awt.Dimension(74, 17));
        jTextField1.setMinimumSize(new java.awt.Dimension(74, 17));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 0.3;
        add(jTextField1, gridBagConstraints1);
        
        executeButton.setText("Execute");
        executeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                executeButtonMouseClicked(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
        add(executeButton, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        // Add your handling code here:
        if (java.awt.event.KeyEvent.VK_ENTER == evt.getKeyCode()) {
            firePropertyChange("execute", null, jTextField1.getText());
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void executeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_executeButtonMouseClicked
        // Add your handling code here:
        firePropertyChange("execute", null, jTextField1.getText());
    }//GEN-LAST:event_executeButtonMouseClicked

    
    public void setInputEnabled(boolean b) {
	jTextField1.setEnabled(b);
    }

    public boolean isInputEnabled() {
	return jTextField1.isEnabled();
    }

    private void serializeInit() {
	executeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                executeButtonMouseClicked(evt);
            }
        });
	jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
        });
    }
    private void readObject(java.io.ObjectInputStream in)
 	throws java.io.IOException, ClassNotFoundException {
 	in.defaultReadObject();
	serializeInit();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton executeButton;
    // End of variables declaration//GEN-END:variables

}
