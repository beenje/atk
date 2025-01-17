/*
 *  Copyright (C) :     2002,2003,2004,2005,2006,2007,2008,2009
 *                      European Synchrotron Radiation Facility
 *                      BP 220, Grenoble 38043
 *                      FRANCE
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
package fr.esrf.tangoatk.widget.util.jgl3dchart;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import java.awt.*;
import java.util.ArrayList;

/**
 * Class which handles chart axis.
 * @author JL Pons
 */
public class JGL3DAxis {

  public static final int XAXIS = 0;
  public static final int YAXIS = 1;
  public static final int ZAXIS = 2;

  /** Use default compiler format to display double */
  public static final int AUTO_FORMAT = 0;
  /** Display value using exponential representation (x.xxEyy) */
  public static final int SCIENTIFIC_FORMAT = 1;
  /** Display number of second as HH:MM:SS */
  public static final int TIME_FORMAT = 2;
  /** Display integer using decimal format */
  public static final int DECINT_FORMAT = 3;
  /** Display integer using haxadecimal format */
  public static final int HEXINT_FORMAT = 4;
  /** Display integer using binary format */
  public static final int BININT_FORMAT = 5;
  /** Display value using exponential representation (xEyy) */
  public static final int SCIENTIFICINT_FORMAT = 6;

  /** Use linear scale for this axis  */
  public static final int LINEAR_SCALE = 0;
  /** Use logarithmic scale for this axis  */
  public static final int LOG_SCALE = 1;

  static final String labelFomats[] = {"%g", "", "%02d:%02d:%02d", "%d", "%X", "%b"};

  VERTEX3D p1;
  VERTEX3D p2;
  VERTEX3D normal;

  private JGL3DView parent;
  private Color     labelColor;
  private Font      labelFont;
  private Color     nameColor;
  private Font      nameFont;
  private int       labelFormat;
  private int       scale;
  ArrayList<LabelInfo> labelInfo;
  LabelInfo         nameInfo;
  private int       type;
  private String    name;
  private boolean   drawAble;
  private boolean   visible;
  private double    min;
  private double    max;
  private double    minimum;
  private double    maximum;
  private boolean   autoScale;
  private int       tickSpacing;
  private double    offLabel;
  private double    atOffset;
  private double    atGain;

  public JGL3DAxis(JGL3DView parent,int type) {

    this.type = type;
    this.parent = parent;
    labelFont = new Font("Dialog", Font.PLAIN, 11);
    nameFont = new Font("Dialog", Font.BOLD, 11);
    labelColor = Color.black;
    labelFormat = AUTO_FORMAT;
    scale = LINEAR_SCALE;
    labelInfo = new ArrayList<LabelInfo>();
    name = "";
    drawAble = true;
    visible = true;
    min=0.0;
    max=100.0;
    minimum=0.0;
    maximum=100.0;
    autoScale=true;
    tickSpacing =30;
    offLabel = 4.0;
    atOffset = 0.0;
    atGain = 1.0;

  }

  /**
   * Sets the axis label format.
   * @param l Format of values displayed on axis and in tooltips.
   * @see  JGL3DAxis#AUTO_FORMAT
   * @see  JGL3DAxis#SCIENTIFIC_FORMAT
   * @see  JGL3DAxis#TIME_FORMAT
   * @see  JGL3DAxis#DECINT_FORMAT
   * @see  JGL3DAxis#HEXINT_FORMAT
   * @see  JGL3DAxis#BININT_FORMAT
   * @see  JGL3DAxis#SCIENTIFICINT_FORMAT
   * @see  JGL3DAxis#getLabelFormat
   */
  public void setLabelFormat(int l) {
    labelFormat = l;
  }

  /**
   * Returns the axis label format.
   * @return Axis value format
   * @see  JGL3DAxis#setLabelFormat
   */
  public int getLabelFormat() {
    return labelFormat;
  }

  /**
   * Set the label font
   * @param lFont Label font
   */
  public void setLabelFont(Font lFont) {
    labelFont = lFont;
  }

  /**
   * @return the label font
   */
  public Font getLabelFont() {
    return labelFont;
  }

  /**
   * Sets the label color
   * @param color Label color
   */
  public void setLabelColor(Color color) {
    this.labelColor = color;
  }

  /**
   * @return label color
   */
  public Color getLabelColor() {
    return labelColor;
  }

  /**
   * Set the axis name font
   * @param lFont Axis name font
   */
  public void setNameFont(Font lFont) {
    nameFont = lFont;
  }

  /**
   * @return the axis name font
   */
  public Font getNameFont() {
    return nameFont;
  }

  /**
   * Sets the axis name color
   * @param color Axis name color
   */
  public void setNameColor(Color color) {
    this.nameColor = color;
  }

  /**
   * @return axis name color
   */
  public Color getNameColor() {
    return nameColor;
  }

  /**
   * Sets the axis name
   * @param name Axis name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the axis name
   */
  public String getName() {
    return name;
  }

  /**
   * @return true if the axis is visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Make the axis visible or not
   * @param visible Visible flag
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Sets the axis maximum
   * @param max Maximum value
   */
  public void setMaximum(double max) {
    maximum = max;
    parent.computeScale();
  }

  /**
   * @return maximum value of this axis
   */
  public double getMaximum() {
    return maximum;
  }

  /**
   * Sets the axis minimum
   * @param min Minimum value
   */
  public void setMinimum(double min) {
    minimum = min;
    parent.computeScale();
  }

  /**
   * Sets linear or log scale
   * @see #LINEAR_SCALE
   * @see #LOG_SCALE
   * @param scale Scale
   */
  public void setScale(int scale) {
    this.scale = scale;
    parent.computeScale();
  }

  /**
   * @return current scale
   * @see #LINEAR_SCALE
   * @see #LOG_SCALE
   * @return Scale
   */
  public int getScale() {
    return scale;
  }

  /**
   * @return minimum value of this axis
   */
  public double getMinimum() {
    return minimum;
  }

  /**
   * Sets or unset the axis in autoscale mode.
   * @param enable Auto scale enable
   */
  public void setAutoScale(boolean enable) {
    autoScale = enable;
    parent.computeScale();
  }

  /**
   * @return true wether this axis is auto scale
\   */
  public boolean isAutoScale() {
    return autoScale;
  }

  /**
   * Set the minimum length between 2 labels
   * @param tickSpacing minimum length (pixel)
   */
  public void setTickSpacing(int tickSpacing) {
    this.tickSpacing = tickSpacing;
  }

  /**
   * @return the minimum length between 2 labels
   */
  public int getTickSpacing() {
    return tickSpacing;
  }

  /**
   * Sets the space between the axis name and the axis (to avoid label and name overlap)
   * @param offset Offset value
   */
  public void setTitleOffset(double offset) {
    offLabel = offset;
  }

  /**
   * @return the space between the axis name and the axis
   */
  public double getTitleOffset() {
    return offLabel;
  }

  /**
   * Sets the offset for axis coordinates
   * @param offset Offset value
   */
  public void setOffsetTransform(double offset) {
    atOffset = offset;
    parent.computeScale();
  }

  /**
   * @return the offset for axis coordinates
   */
  public double getOffsetTransform() {
    return atOffset;
  }

  /**
   * Sets the gain for axis coordinates
   * @param gain Gain value
   */
  public void setGainTransform(double gain) {
    atGain = gain;
    parent.computeScale();
  }

  /**
   * @return the gain for axis coordinates
   */
  public double getGainTransform() {
    return atGain;
  }

  // -----------------------------------------------------------------------------
  // Private stuff
  // -----------------------------------------------------------------------------

  boolean isDrawAble() {
    return drawAble;
  }

  double getMin() {
    return min * atGain;
  }

  double getMax() {
    return max*atGain;
  }

  void setMin(double min) {
    this.min = min;
  }

  void setMax(double max) {
    this.max = max;
  }

  void setPosition(VERTEX3D p1,VERTEX3D p2) {
    this.p1 = p1;
    this.p2 = p2;
  }

  void setNormal(VERTEX3D normal) {
    this.normal = normal;
  }

  /** Return a scientific (exponential) representation of the double.
   * @param d double to convert
   * @return A string continaing a scientific representation of the given double.
   */
  private String toScientific(double d) {

    double a = Math.abs(d);
    int e = 0;
    String f = "%.2fe%d";

    if (a != 0) {
      if (a < 1) {
        while (a < 1) {
          a = a * 10;
          e--;
        }
      } else {
        while (a >= 10) {
          a = a / 10;
          e++;
        }
      }
    }

    if (a >= 9.999999999) {
      a = a / 10;
      e++;
    }

    if (d < 0) a = -a;

    return String.format(f,a,e);
  }

  private String toScientificInt(double d) {

    double a = Math.abs(d);
    int e = 0;
    String f = "%de%d";

    if (a != 0) {
      if (a < 1) {
        while (a < 1) {
          a = a * 10;
          e--;
        }
      } else {
        while (a >= 10) {
          a = a / 10;
          e++;
        }
      }
    }

    if (a >= 9.999999999) {
      a = a / 10;
      e++;
    }

    if (d < 0) a = -a;

    return String.format(f,(int)Math.rint(a),e);

  }

  /**
   * Suppress last non significative zeros
   * @param n String representing a floating number
   */
  private String suppressZero(String n) {

    if(n.indexOf('e')>=0 || n.indexOf('E')>=0)
      return n;

    boolean hasDecimal = n.indexOf('.') != -1;

    if(hasDecimal) {

      StringBuffer str = new StringBuffer(n);
      int i = str.length() - 1;
      while( str.charAt(i)=='0' ) {
        str.deleteCharAt(i);
        i--;
      }
      if(str.charAt(i)=='.') {
        // Remove unwanted decimal
        str.deleteCharAt(i);
      }

      return str.toString();

    } else {
      return n;
    }

  }

  /**
   * Returns a representation of the double acording to the format
   * @param vt double to convert
   * @param prec Desired precision (Pass 0 to not perform prec rounding).
   * @return A string continaing a formated representation of the given double.
   */
  private String formatValue(double vt, double prec) {

    if(Double.isNaN(vt))
      return "NaN";

    if(scale==LOG_SCALE)
      vt = Math.pow(10.0, vt);

    // Round value to nearest multiple of prec
    // TODO: rounding in LOG_SCALE
    if (prec != 0 && scale == LINEAR_SCALE) {

      boolean isNegative = (vt < 0.0);
      if(isNegative) vt = -vt;

      // Find multiple
      double i = Math.floor(vt/prec + 0.5d);
      vt = i * prec;

      if(isNegative) vt = -vt;

    }

    switch (labelFormat) {
      case SCIENTIFIC_FORMAT:
        return toScientific(vt);

      case SCIENTIFICINT_FORMAT:
        return toScientificInt(vt);

      case DECINT_FORMAT:
      case HEXINT_FORMAT:
      case BININT_FORMAT:
        if (vt < 0.0)
          return "-" + String.format(labelFomats[labelFormat],(int) (Math.abs(vt)+0.5) );
        else
          return String.format(labelFomats[labelFormat], (int) (Math.abs(vt)+0.5));

      case TIME_FORMAT:

        int sec = (int) (Math.abs(vt));
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;

        if (vt < 0.0)
          return "-" + String.format(labelFomats[labelFormat], h,m,s);
        else
          return String.format(labelFomats[labelFormat], h,m,s);

      default:

        // Auto format
        if(vt==0.0) return "0";

        if(Math.abs(vt)<=1.0E-4) {

          return toScientific(vt);

        } else {

          int nbDigit = -(int)Math.floor(Math.log10(prec));
          if( nbDigit<=0 ) {
            return suppressZero(Double.toString(vt));
          } else {
            String dFormat = "%." + nbDigit + "f";
            return suppressZero(String.format(dFormat,vt));
          }

        }

    }

  }

  // Compute labels
  private void computeTicks(GLU glu,GL gl,double length) {

    double min=0,max=0;
    VERTEX3D axisPos = new VERTEX3D();
    boolean extractLabel = false;
    double[] coord = new double[2];

    switch(type) {
      case XAXIS:
        min = p1.x;
        max = p2.x;
        axisPos.x = 0.0;
        axisPos.y = p1.y;
        axisPos.z = p1.z;
        break;
      case YAXIS:
        min = p1.y;
        max = p2.y;
        axisPos.x = p1.x;
        axisPos.y = 0.0;
        axisPos.z = p1.z;
        break;
      case ZAXIS:
        min = p1.z;
        max = p2.z;
        axisPos.x = p1.x;
        axisPos.y = p1.y;
        axisPos.z = 0.0;
        break;
    }

    max = max / atGain;
    min = min / atGain;

    double prec = computeLowTen(max - min);
    int n = (int) Math.rint((max - min) / prec);
    int nbMaxLab = (int) length / tickSpacing;
    if(nbMaxLab>20) nbMaxLab=20;
    double sz = (max-min);
    int step=10;
    double startx;
    double sx;
    double precDelta = sz / length;

    if (scale == LOG_SCALE) {

      prec = 1;   // Decade

      startx = Math.rint(min);

      n = (int) Math.rint((max - min) / prec);

      while (n > nbMaxLab) {
        prec = prec * 2;
        n = (int) Math.rint((max - min) / prec);
        if (n > nbMaxLab) {
          prec = prec * 5;
          n = (int) Math.rint((max - min) / prec);
        }
      }

    } else {

      if (n <= nbMaxLab) {

        // Look forward
        n = (int) Math.rint((max - min) / (prec / 2.0));

        while (n <= nbMaxLab) {
          prec = prec / 2.0;
          step = 5;
          n = (int) Math.rint((max - min) / (prec / 5.0));
          if (n <= nbMaxLab) {
            prec = prec / 5.0;
            step = 10;
            n = (int) Math.rint((max - min) / (prec / 2.0));
          }
        }

      } else {

        // Look backward
        while (n > nbMaxLab) {
          prec = prec * 5.0;
          step = 5;
          n = (int) Math.rint((max - min) / prec);
          if (n > nbMaxLab) {
            prec = prec * 2.0;
            step = 10;
            n = (int) Math.rint((max - min) / prec);
          }
        }

      }

      // round to multiple of prec (last not visible label)

      long round = (long) Math.floor(min / prec);
      startx = round * prec;

      // Compute real number of label

      sx = startx;
      int nbL = 0;
      while (sx <= (max + precDelta)) {
        if (sx >= (min - precDelta)) {
          nbL++;
        }
        sx += prec;
      }

      if (nbL <= 2) {
        // Only one label
        // Go backward and extract the 2 extremity
        if (step == 10) {
          prec = prec / 2.0;
        } else {
          prec = prec / 5.0;
        }
        extractLabel = true;
      }

    }

    VERTEX3D nV = new VERTEX3D(p2.x-p1.x,p2.y-p1.y,p2.z-p1.z);
    nV.normalize();

    while(startx<=(max + precDelta)) {
      if(startx>(min - precDelta)) {

        LabelInfo l = new LabelInfo();

        sx = startx * atGain;
        l.p1 = new VERTEX3D(sx*nV.x+axisPos.x,sx*nV.y+axisPos.y,sx*nV.z+axisPos.z);
        l.p2 = new VERTEX3D(sx*nV.x+normal.x+axisPos.x,sx*nV.y+normal.y+axisPos.y,sx*nV.z+normal.z+axisPos.z);

        l.value = formatValue(startx,prec);
        l.labelColor = labelColor;
        double offLabel = 1.5;
        Utils.project(glu,gl,sx*nV.x+axisPos.x,
                         sx*nV.y+axisPos.y,
                         sx*nV.z+axisPos.z,
                         coord);
        l.x1 = coord[0];
        l.y1 = coord[1];
        Utils.project(glu,gl,sx*nV.x+axisPos.x+normal.x*offLabel,
                         sx*nV.y+axisPos.y+normal.y*offLabel,
                         sx*nV.z+axisPos.z+normal.z*offLabel,
                         coord);
        l.x2 = coord[0];
        l.y2 = coord[1];
        labelInfo.add(l);

      }
      startx+=prec;
    }

    // Extract 2 bounds when we didn't found a correct match.
    if(extractLabel) {
      if(labelInfo.size()>2) {
        ArrayList<LabelInfo> nLabels = new ArrayList<LabelInfo>();
        LabelInfo lis = labelInfo.get(0);
        LabelInfo lie = labelInfo.get(labelInfo.size()-1);
        nLabels.add(lis);
        nLabels.add(lie);
        labelInfo = nLabels;
      }
    }

    // Axis name
    double middlePos = (max+min)/2.0f;
    nameInfo = new LabelInfo();
    nameInfo.value = name;
    nameInfo.labelColor = nameColor;
    Utils.project(glu,gl,middlePos*nV.x+axisPos.x,
                     middlePos*nV.y+axisPos.y,
                     middlePos*nV.z+axisPos.z,
                     coord);
    nameInfo.x1 = coord[0];
    nameInfo.y1 = coord[1];
    Utils.project(glu,gl,middlePos*nV.x+axisPos.x+normal.x*offLabel,
                     middlePos*nV.y+axisPos.y+normal.y*offLabel,
                     middlePos*nV.z+axisPos.z+normal.z*offLabel,
                     coord);
    nameInfo.x2 = coord[0];
    nameInfo.y2 = coord[1];

  }

  private double computeHighTen(double d) {
    int p = (int)Math.log10(d);
    return Math.pow(10.0, p + 1);
  }

  private double computeLowTen(double d) {
    int p = (int)Math.log10(d);
    return Math.pow(10.0, p);
  }

  void measureAxis(GLU glu,GL gl) {

    labelInfo.clear();

    double[] coord = new double[2];
    Utils.project(glu,gl,p1.x, p1.y, p1.z,coord);
    double x1 = coord[0];
    double y1 = coord[1];

    Utils.project(glu,gl,p2.x, p2.y, p2.z,coord);
    double x2 = coord[0];
    double y2 = coord[1];

    double axisLenght = Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    if( axisLenght<10.0 ) drawAble = false;
    else                  drawAble = true;

    if(drawAble) computeTicks(glu,gl,axisLenght);

  }

  void paintAxisLabel(GL gl,int screenWidth,int screenHeight) {

    if (isVisible() && isDrawAble()) {
      for (int i = 0; i < labelInfo.size(); i++) {
        LabelInfo l = labelInfo.get(i);
        l.paint(gl,screenWidth,screenHeight);
      }
      nameInfo.paint(gl,screenWidth,screenHeight);
    }

  }

  void paintAxis(GL gl) {

    if(!drawAble || !visible)
      return;

    GL2 gl2 = (GL2)gl;

    // Axis
    gl2.glDisable(GL2.GL_LIGHTING);
    gl2.glDisable(GL.GL_TEXTURE_2D);
    gl2.glDisable(GL.GL_DEPTH_TEST);
    gl2.glDisable(GL2.GL_LINE_STIPPLE);
    gl2.glLineWidth(2.0f);
    gl2.glColor3f((float)labelColor.getRed()/255.0f,
                 (float)labelColor.getGreen()/255.0f,
                 (float)labelColor.getBlue()/255.0f);
    gl2.glBegin(GL.GL_LINES);
    gl2.glVertex3f((float) p1.x, (float) p1.y, (float) p1.z);
    gl2.glVertex3f((float) p2.x, (float) p2.y, (float) p2.z);
    gl2.glEnd();

    // Tick
    gl2.glLineWidth(1.0f);
    gl2.glBegin(GL.GL_LINES);
    for(int i=0;i<labelInfo.size();i++) {
      LabelInfo li = labelInfo.get(i);
      gl2.glVertex3f((float) li.p1.x, (float) li.p1.y, (float) li.p1.z);
      gl2.glVertex3f((float) li.p2.x, (float) li.p2.y, (float) li.p2.z);
    }
    gl2.glEnd();


  }


}