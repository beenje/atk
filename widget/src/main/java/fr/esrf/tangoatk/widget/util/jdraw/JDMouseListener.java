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
 
package fr.esrf.tangoatk.widget.util.jdraw;

import java.util.EventListener;

/**
 * The listener interface for receiving "interesting" mouse events
 * (press, release, click, enter, and exit) on a JDObject.
 */
public interface JDMouseListener extends EventListener {

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a JDObject.
     * @param e Mouse event
     */
    public void mouseClicked(JDMouseEvent e);

    /**
     * Invoked when a mouse button has been pressed on a JDObject.
     * @param e Mouse event
     */
    public void mousePressed(JDMouseEvent e);

    /**
     * Invoked when a mouse button has been released on a JDObject.
     * @param e Mouse event
     */
    public void mouseReleased(JDMouseEvent e);

    /**
     * Invoked when the mouse enters a JDObject.
     * @param e Mouse event
     */
    public void mouseEntered(JDMouseEvent e);

    /**
     * Invoked when the mouse exits a JDObject.
     * @param e Mouse event
     */
    public void mouseExited(JDMouseEvent e);
}
