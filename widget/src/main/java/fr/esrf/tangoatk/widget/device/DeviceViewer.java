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
 

    package fr.esrf.tangoatk.widget.device;

import fr.esrf.tangoatk.core.IDevice;

    /**
     *
     * @author  root
     */
    public class DeviceViewer extends javax.swing.JPanel {

        /** Creates new form DevicePanel */
        public DeviceViewer () {
            initComponents();
        }

        public void setModel(IDevice device) {
            status.setModel(device);
            state.setModel(device);
            ((javax.swing.border.TitledBorder)getBorder()).setTitle(device.getName());
        }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        private void initComponents() {//GEN-BEGIN:initComponents
            state = new fr.esrf.tangoatk.widget.device.StateViewer();
            status = new fr.esrf.tangoatk.widget.device.StatusViewer();
            
            setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints2;
            
            setBorder(new javax.swing.border.TitledBorder("Not Connected"));
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.insets = new java.awt.Insets(0, 4, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.weightx = 70.0;
            add(state, gridBagConstraints2);
            
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            add(status, gridBagConstraints2);
            
        }//GEN-END:initComponents

	public void setStateVisible(boolean b) {
	    state.setVisible(b);
	}

	public boolean isStateVisible() {
	    return state.isVisible();
	}

	public void setStatusVisible(boolean b) {
	    status.setVisible(b);
	}

	public boolean isStatusVisible() {
	    return status.isVisible();
	}
		
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private fr.esrf.tangoatk.widget.device.StateViewer state;
        private fr.esrf.tangoatk.widget.device.StatusViewer status;
        // End of variables declaration//GEN-END:variables

}