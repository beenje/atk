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

import java.awt.geom.Point2D;
import java.awt.*;
import java.io.IOException;
import java.util.Vector;

/** JDraw Slider graphic object.
 */
public class JDSlider extends JDRectangular {

  /** Horizontal bar , min at feft */
  public final static int SLIDER_HORIZONTAL_LEFT  = 0;
  /** Horizontal bar , min at right */
  public final static int SLIDER_HORIZONTAL_RIGHT = 1;
  /** Vertical bar , min at top */
  public final static int SLIDER_VERTICAL_TOP = 2;
  /** Vertical bar , min at bottom */
  public final static int SLIDER_VERTICAL_BOTTOM = 3;

  // Default Properties
  static final double minDefault             = 0.0;
  static final double maxDefault             = 1.0;
  static final double valueDefault           = 0.5;
  static final int orientationDefault        = SLIDER_VERTICAL_BOTTOM;
  static final JDObject cursorDefault        = new JDRectangle("DefaultSliderCursor",0,0,16,16);

  // Vars
  double   min;
  double   max;
  double   value;
  int      orientation;
  JDObject cursor;

  /**
   * Contructs a JDSlider.
   * @param objectName Object name
   * @param x Up left corner x coordinate
   * @param y Up left corner y coordinate
   * @param w Rectangle width
   * @param h Rectangle height
   *
   */
  public JDSlider(String objectName, int x, int y, int w, int h) {

    initDefault();
    setOrigin(new Point2D.Double(x, y));
    summit = new Point2D.Double[8];
    name = objectName;
    createSummit();
    computeSummitCoordinates(x, y, w, h);
    updateShape();

  }

  JDSlider(JDSlider e,int x,int y) {

    cloneObject(e,x,y);
    min = e.min;
    max = e.max;
    value = e.value;
    orientation = e.orientation;
    // Clone child
    cursor = e.cursor.copy(x, y);
    updateShape();

  }

  JDSlider(JDFileLoader f) throws IOException {

    initDefault();
    f.startBlock();
    summit = f.parseRectangularSummitArray();

    while (!f.isEndBlock()) {
      String propName = f.parseProperyName();
      if( propName.equals("minSlider") ) {
        min = f.parseDouble();
      } else if( propName.equals("maxSlider") ) {
        max = f.parseDouble();
      } else if( propName.equals("valueSlider") ) {
        value = f.parseDouble();
      } else if( propName.equals("orientation") ) {
        orientation = (int)f.parseDouble();
      } else if( propName.equals("cursor") ) {
        cursor = f.parseObject();
      } else
        loadDefaultPropery(f, propName);
    }

    f.endBlock();
    updateShape();

  }

  public JDObject copy(int x,int y) {
    return new JDSlider(this,x,y);
  }

  // -----------------------------------------------------------
  // Overrides
  // -----------------------------------------------------------

  void initDefault() {

    super.initDefault();
    min = minDefault;
    max = maxDefault;
    value = valueDefault;
    orientation = orientationDefault;
    cursor = cursorDefault.copy(0,0);

  }

  public boolean isInsideObject(int x, int y) {

    // Check cursor
    if(cursor.isInsideObject(x,y))
      return true;

    if(!super.isInsideObject(x,y)) return false;

    if (fillStyle != FILL_STYLE_NONE)
      return boundRect.contains(x, y);
    else {
      int x1 = boundRect.x;
      int x2 = boundRect.x + boundRect.width;
      int y1 = boundRect.y;
      int y2 = boundRect.y + boundRect.height;

      return isPointOnLine(x, y, x1, y1, x2, y1) ||
          isPointOnLine(x, y, x2, y1, x2, y2) ||
          isPointOnLine(x, y, x2, y2, x1, y2) ||
          isPointOnLine(x, y, x1, y2, x1, y1);
    }
  }

  void updateShape() {

    computeBoundRect();

    // Update rectangle coordinates

    ptsx = new int[4];
    ptsy = new int[4];
    ptsx[0] = (int)(summit[0].x+0.5);
    ptsy[0] = (int)(summit[0].y+0.5);
    ptsx[1] = (int)(summit[2].x+0.5);
    ptsy[1] = (int)(summit[2].y+0.5);
    ptsx[2] = (int)(summit[4].x+0.5);
    ptsy[2] = (int)(summit[4].y+0.5);
    ptsx[3] = (int)(summit[6].x+0.5);
    ptsy[3] = (int)(summit[6].y+0.5);

    // Cursor placement

    if (!Double.isNaN(value)) {

      double ratio = 0.0;
      if (min != max) ratio = (value - min) / (max - min);
      if (ratio < 0.0) ratio = 0.0;
      if (ratio > 1.0) ratio = 1.0;

      // Compute slider pos
      // Cursor Center is taken as reference.
      cursor.centerOrigin();
      double xOrg = (summit[0].x + summit[4].x) / 2.0;
      double yOrg = (summit[0].y + summit[4].y) / 2.0;
      double w = (double) (boundRect.width - 1);
      double h = (double) (boundRect.height - 1);

      switch (orientation) {

        case SLIDER_HORIZONTAL_LEFT:
          {
            double tH = w * (ratio - 0.5);
            if (tH < -(w / 2.0)) tH = -w / 2.0;
            if (tH > (w / 2.0)) tH = w / 2.0;
            double trX = xOrg - cursor.origin.x + tH;
            double trY = yOrg - cursor.origin.y;
            if (Math.abs(trX) > 0.5 || Math.abs(trY) > 0.5)
              cursor.translate(trX, trY);
          }
          break;

        case SLIDER_HORIZONTAL_RIGHT:
          {
            double tH = w * ((1.0 - ratio) - 0.5);
            if (tH < -(w / 2.0)) tH = -w / 2.0;
            if (tH > (w / 2.0)) tH = w / 2.0;
            double trX = xOrg - cursor.origin.x + tH;
            double trY = yOrg - cursor.origin.y;
            if (Math.abs(trX) > 0.5 || Math.abs(trY) > 0.5)
              cursor.translate(trX, trY);
          }
          break;

        case SLIDER_VERTICAL_TOP:
          {
            double tV = h * (ratio - 0.5);
            if (tV < -(h / 2.0)) tV = -h / 2.0;
            if (tV > (h / 2.0)) tV = h / 2.0;
            double trX = xOrg - cursor.origin.x;
            double trY = yOrg - cursor.origin.y + tV;
            if (Math.abs(trX) > 0.5 || Math.abs(trY) > 0.5)
              cursor.translate(trX, trY);
          }
          break;

        case SLIDER_VERTICAL_BOTTOM:
          {
            double tV = h * ((1.0 - ratio) - 0.5);
            if (tV < -(h / 2.0)) tV = -h / 2.0;
            if (tV > (h / 2.0)) tV = h / 2.0;
            double trX = xOrg - cursor.origin.x;
            double trY = yOrg - cursor.origin.y + tV;
            if (Math.abs(trX) > 0.5 || Math.abs(trY) > 0.5)
              cursor.translate(trX, trY);
          }
          break;

      }

    }

    // Update shadow coordinates
    if( hasShadow() ) {
      computeShadow(true);
      computeShadowColors();
    }

  }

  public void paint(JDrawEditor parent,Graphics g) {

    if(!visible) return;
    prepareRendering((Graphics2D)g);
    super.paint(parent,g);

    // Draw the curosor
    if (!Double.isNaN(value))
      cursor.paint(parent,g);

  }

  Rectangle getRepaintRect() {

    // Compute repaint rectangle
    Rectangle r = super.getRepaintRect();
    if(Double.isNaN(value))
      return r;
    else
      return r.union(cursor.getRepaintRect());

  }

  // -----------------------------------------------------------
  // Property
  // -----------------------------------------------------------
  /**
   * Sets the slider's maximum value . It is used to handle
   * the bar position calculation.
   * @param m Maximum value
   */
  public void setMaximum(double m) {
    max = m;
    updateShape();
  }

  /**
   * @return the slider's maximum value.
   * @see #setMaximum
   */
  public double getMaximum() {
    return max;
  }

  /**
   * Sets the slider's minimum value . It is used to handle
   * the slider position calculation.
   * @param m Miniimum value
   */
  public void setMinimum(double m) {
    min = m;
    updateShape();
  }

  /**
   * @return the slider's maximum value.
   * @see #setMinimum
   */
  public double getMinimum() {
    return min;
  }

  /**
   * Sets the slider value.
   * @param v slider value.
   */
  public void setSliderValue(double v) {
    value = v;
    updateShape();
  }

  /**
   * @return the current slider value.
   */
  public double getSliderValue() {
    return value;
  }

  /**
   * Sets the bar orientation.
   * @param o Orientation value
   * @see #SLIDER_HORIZONTAL_LEFT
   * @see #SLIDER_HORIZONTAL_RIGHT
   * @see #SLIDER_VERTICAL_TOP
   * @see #SLIDER_VERTICAL_BOTTOM
   */
  public void setOrientation(int o) {
    orientation = o;
    updateShape();
  }

  /**
   * @return the current bar orientation.
   * @see #setOrientation
   */
  public int getOrientation() {
    return orientation;
  }

  /**
   * @return the current cursor object.
   * @see #setCursor
   */
  public JDObject getCursor() {
    return cursor;
  }

  /**
   * Sets the cursor object.
   * @param nCursor Cursor object
   */
  public void setCursor(JDObject nCursor) {
    cursor = nCursor;
    updateShape();
  }

  // -----------------------------------------------------------
  // Configuration management
  // -----------------------------------------------------------
  void recordObject(StringBuffer to_write,int level) {

    StringBuffer decal = recordObjectHeader(to_write,level);

    if(min!=minDefault) {
      to_write.append(decal).append("minSlider:").append(min).append("\n");
    }

    if(max!=maxDefault) {
      to_write.append(decal).append("maxSlider:").append(max).append("\n");
    }

    if(value!=valueDefault) {
      to_write.append(decal).append("valueSlider:").append(value).append("\n");
    }

    if(orientation!=orientationDefault) {
      to_write.append(decal).append("orientation:").append(orientation).append("\n");
    }

    to_write.append(decal).append("cursor:\n");
    cursor.recordObject(to_write, level+2);

    closeObjectHeader(to_write,level);

  }

  // -----------------------------------------------------------
  // Undo buffer
  // -----------------------------------------------------------
  UndoPattern getUndoPattern() {

    UndoPattern u = new UndoPattern(UndoPattern._JDSlider);
    fillUndoPattern(u);
    u.min = min;
    u.max = max;
    u.value = value;
    u.textOrientation = orientation;
    u.gChildren = new Vector();
    u.gChildren.add(cursor.getUndoPattern());
    return u;

  }

  JDSlider(UndoPattern e) {

     initDefault();
     applyUndoPattern(e);
     min = e.min;
     max = e.max;
     value = e.value;
     orientation = e.textOrientation;
     Vector tmp = new Vector();
     UndoBuffer.rebuildObject((UndoPattern)e.gChildren.get(0),tmp);
     cursor = (JDObject)tmp.get(0);
     updateShape();

   }

  // -----------------------------------------------------------
  // Private stuff
  // -----------------------------------------------------------

  // Compute summit coordinates from width, height
  // 0 1 2
  // 7   3
  // 6 5 4
  private void computeSummitCoordinates(int x,int y,int width, int height) {

    // Compute summit

    summit[0].x = x;
    summit[0].y = y;

    summit[2].x = x + width;
    summit[2].y = y;

    summit[4].x = x + width;
    summit[4].y = y + height;

    summit[6].x = x;
    summit[6].y = y + height;

    centerSummit();

  }

}