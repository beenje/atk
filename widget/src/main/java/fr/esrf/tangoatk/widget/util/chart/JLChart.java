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
 
//
// JLChart.java
// Description: A Class to handle 2D graphics plot.
//
// JL Pons (c)ESRF 2002


package fr.esrf.tangoatk.widget.util.chart;


import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.JTableRow;
import fr.esrf.tangoatk.widget.util.MultiExtFileFilter;

class LabelRect {
  Rectangle rect;
  JLDataView view;

  LabelRect(int x, int y, int w, int h, JLDataView v) {
    rect = new Rectangle(x, y, w, h);
    view = v;
  }
}

class TabbedLine {

  JLDataView[] dv;
  DataList[] dl;
  double[] lastValue;
  int anno;
  int sIndex;
  int precision = 0;
  String noValueString = "";
  boolean correlated;

  TabbedLine(int nb) {
    dv = new JLDataView[nb];
    dl = new DataList[nb];
    lastValue = new double[nb];
  }

  void setCorrelated(boolean correlated) {
    this.correlated = correlated;
  }

  void setPrecision(int milliseconds) {
    precision = milliseconds;
  }

  void setNoValueString (String noValueString) {
      this.noValueString = noValueString;
  }

  void add(int id, JLDataView v) {
    dv[id] = v;
    dl[id] = v.getData();
    if(v.getData()!=null)
      lastValue[id] = v.getData().y;
    else
      lastValue[id] = Double.NaN;
  }

  double getMinTime() {
    double r = Double.MAX_VALUE;
    for (int i = 0; i < dl.length; i++) {
      if (dl[i] != null) {
        if (dl[i].x < r) r = dl[i].x;
      }
    }
    return r;
  }

  String getFirstLine(int annotation) {

    StringBuffer ret = new StringBuffer();
    anno = annotation;

    if (annotation == JLAxis.TIME_ANNO) {
      ret.append("Time (s)\t");
    } else {
      ret.append("Index\t");
    }

    for (int i = 0; i < dv.length; i++)
      ret.append(dv[i].getName() + "\t");

    ret.append("\n");

    return ret.toString();

  }

  String getNextLine() {

    double t0 = getMinTime();

    // Test end of data
    if (t0 == Double.MAX_VALUE)
      return null;

    StringBuffer ret = new StringBuffer();

    if (anno == JLAxis.TIME_ANNO) {
      long t = (long) t0;
      long ts = t / 1000;
      long ms = t % 1000;

      if (ms == 0)
        ret.append(ts + "\t");
      else if (ms < 10)
        ret.append(ts + ".00" + ms + "\t");
      else if (ms < 100)
        ret.append(ts + ".0" + ms + "\t");
      else
        ret.append(ts + "." + ms + "\t");

    } else {
      ret.append(Double.toString(t0) + "\t");
    }

    if (correlated) {
      for (int i = 0; i < dl.length; i++) {
        if (dl[i] != null) {
          if (dl[i].x == t0) {
            ret.append(Double.toString(dl[i].y) + "\t");
            lastValue[i] = dl[i].y;
            dl[i] = dl[i].next;
          } else {
            ret.append(Double.toString(lastValue[i]) + "\t");
          }
        } else {
          ret.append(Double.toString(lastValue[i]) + "\t");
        }
      }
    } else {
      for (int i = 0; i < dl.length; i++) {
        if (dl[i] != null) {
          if ((dl[i].x >= t0 - precision) && (dl[i].x <= t0 + precision)) {
            ret.append(Double.toString(dl[i].y) + "\t");
            dl[i] = dl[i].next;
          } else {
            ret.append(noValueString + "\t");
          }
        } else {
          ret.append(noValueString + "\t");
        }
      }
    }


    ret.append("\n");
    return ret.toString();

  }

  String[] getFirstFields(int annotation, boolean showIndex) {

    anno = annotation;
    sIndex = (showIndex)?1:0;
    String[] ret = new String[dv.length + sIndex];

    if (sIndex>0) {
      if (annotation == JLAxis.TIME_ANNO) {
        ret[0] = "Time (s)";
      } else {
        ret[0] = "Index";
      }
    }

    for (int i = 0; i < dv.length; i++)
      ret[i + sIndex] = dv[i].getName();

    return ret;

  }

  String[] getNextFields() {

    double t0 = getMinTime();

    // Test end of data
    if (t0 == Double.MAX_VALUE)
      return null;

    String[] ret = new String[dv.length + sIndex];

    if (sIndex > 0) {
      if (anno == JLAxis.TIME_ANNO) {
        ret[0] = JLAxis.formatTimeValue(t0);
      } else {
        ret[0] = Double.toString(t0);
      }
    }

    for (int i = 0; i < dl.length; i++) {
      if (dl[i] != null) {
//      if (dl[i].x == t0) {
        if ( (dl[i].x >= t0 - precision) && (dl[i].x <= t0 + precision) ) {
          ret[i + sIndex] = dv[i].formatValue(dl[i].y);
          dl[i] = dl[i].next;
        } else {
          ret[i + sIndex] = "";
        }
      } else {
        ret[i + sIndex] = "";
      }
    }


    return ret;

  }

}

/**
 * A Class to handle 2D graphics plot.
 * @author JL Pons
 */

public class JLChart extends JComponent implements MouseWheelListener, MouseListener, MouseMotionListener, ActionListener {

  // constant
  /** Place label at the bottom of the chart */
  public static final int LABEL_DOWN = 0;
  /** Place label at the top of the chart */
  public static final int LABEL_UP = 1;
  /** Place label at the right of the chart */
  public static final int LABEL_RIGHT = 2;
  /** Place label at the left of the chart */
  public static final int LABEL_LEFT = 3;
  /** Place label at the bottom of the chart and try to arrange them in rows */
  public static final int LABEL_ROW = 4;

  /* Chart properties menu item */
  public static final int MENU_CHARTPROP = 0;
  /* Data view properties menu */
  public static final int MENU_DVPROP    = 1;
  /* Show table menu */
  public static final int MENU_TABLE     = 2;
  /* Save data file menu item */
  public static final int MENU_DATASAVE  = 3;
  /* print graph menu item */
  public static final int MENU_PRINT     = 4;
  /* Statistics menu item */
  public static final int MENU_STAT      = 5;
  /* Load data file menu item */
  public static final int MENU_DATALOAD  = 6;

  /* Date Format recognized by loadDataFile() */
  public static final String US_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  /* Date Format recognized by loadDataFile() */
  public static final String FR_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.SSS";

  // Global graph options
  private String header = null;
  private boolean headerVisible = false;
  private Font headerFont;
  private Color headerColor;

  private boolean labelVisible = true;
  private int labelMode = LABEL_ROW;
  private Font labelFont;
  protected Vector<LabelRect> labelRect;

  private boolean ipanelVisible = false;
  private boolean paintAxisFirst = true;
  private Color chartBackground;

  private double displayDuration;
  private double oldDisplayDuration = Double.NaN;
  protected double maxDisplayDuration;

  protected JPopupMenu chartMenu;
  private JMenuItem optionMenuItem;
  private JMenuItem saveFileMenuItem;
  private JMenuItem loadFileMenuItem;
  private JMenuItem zoomBackMenuItem;
  private JMenuItem printMenuItem;
  private JSeparator sepMenuItem;

  private JMenu tableMenu;
  private JMenuItem tableAllMenuItem;
  private JMenuItem[] tableSingleY1MenuItem = new JMenuItem[0];
  private JMenuItem[] tableSingleY2MenuItem = new JMenuItem[0];

  private JMenu dvMenu;
  private JMenuItem[] dvY1MenuItem = new JMenuItem[0];
  private JMenuItem[] dvY2MenuItem = new JMenuItem[0];

  private JMenu statMenu;
  private JMenuItem statAllMenuItem;
  private JMenuItem[] statSingleY1MenuItem = new JMenuItem[0];
  private JMenuItem[] statSingleY2MenuItem = new JMenuItem[0];

  private JMenuItem[] userActionMenuItem;
  private String[] userAction;

  private boolean zoomDrag;
  private boolean translateDragStart;
  private boolean translateDrag;
  private boolean zoomDragAllowed;
  private int zoomX;
  private int zoomY;
  private JButton zoomButton;

  private int lastX;
  private int lastY;
  private SearchInfo lastSearch;

  // Measurements stuff
  private Rectangle headerR;
  private Rectangle labelR;
  private Rectangle viewR;
  private Dimension margin;
  private int labelHeight;
  private int labelWidth;
  private int labelPerLine;
  private int nbLabel;
  private int headerWidth;
  private int axisHeight;
  private int axisWidth;
  private int y1AxisThickness;
  private int y2AxisThickness;
  private int xAxisThickness;
  private int xAxisUpMargin;

  // Axis
  private JLAxis xAxis;
  private JLAxis y1Axis;
  private JLAxis y2Axis;

  // Listeners
  private IJLChartListener listener;  // JLChart listener

  // Table
  private JLTable theTable = null;

  // Timestamp precision to build the table. Positive int. The lower, the more precise
  private int timePrecision = 0;

  // Menu item to allow user to set time precision
  private JMenuItem precisionMenuItem;

  // Menu item to save a snapshot of this chart
  private JMenuItem saveSnapshotMenuItem;

  protected boolean preferDialog = false, modalDialog = false;
  protected JDialog tableDialog = null;
  protected Window dialogParent;
  protected JFrame parentFrame = null;

  // Used to open the file chooser dialog on the last saved snapshot location
  protected String lastSnapshotLocation = ".";

  // Used to open the file chooser dialog on the last saved data file location
  protected String lastDataFileLocation = ".";

  // Used to open the file chooser dialog with the last used file filter
  protected FileFilter lastFileFilter = null;

  protected String noValueString = "";

  /**
   * Graph constructor.
   */
  public JLChart() {
    Color defColor = Color.WHITE;
    setBackground(defColor);
    setChartBackground(defColor);
    setForeground(Color.black);
    setOpaque(true);
    setFont(new Font("Dialog", Font.PLAIN, 12));
    headerFont = getFont();
    headerColor = getForeground();
    labelFont = getFont();

    margin = new Dimension(5,5);
    headerR = new Rectangle(0,0,0,0);
    viewR = new Rectangle(0,0,0,0);
    labelR = new Rectangle(0,0,0,0);

    xAxis = new JLAxis(this, JLAxis.HORIZONTAL_DOWN);
    xAxis.setAnnotation(JLAxis.TIME_ANNO);
    xAxis.setAutoScale(true);
    xAxis.setAxeName("(X)");
    y1Axis = new JLAxis(this, JLAxis.VERTICAL_LEFT);
    y1Axis.setAxeName("(Y1)");
    y2Axis = new JLAxis(this, JLAxis.VERTICAL_RIGHT);
    y2Axis.setAxeName("(Y2)");
    displayDuration = Double.POSITIVE_INFINITY;
    maxDisplayDuration = Double.POSITIVE_INFINITY;

    labelRect = new Vector<LabelRect>();
    zoomDrag = false;
    translateDragStart = false;
    translateDrag = false;
    zoomDragAllowed = false;

    chartMenu = new JPopupMenu();

    optionMenuItem = new JMenuItem("Chart properties");
    optionMenuItem.addActionListener(this);

    saveFileMenuItem = new JMenuItem("Save data File");
    saveFileMenuItem.addActionListener(this);

    loadFileMenuItem = new JMenuItem("Load data File");
    loadFileMenuItem.addActionListener(this);

    tableMenu = new JMenu("Show table");
    tableAllMenuItem = new JMenuItem("All");
    tableAllMenuItem.addActionListener(this);

    zoomBackMenuItem = new JMenuItem("Zoom back");
    zoomBackMenuItem.addActionListener(this);

    printMenuItem = new JMenuItem("Print Chart");
    printMenuItem.addActionListener(this);

    precisionMenuItem = new JMenuItem("Abscisse error margin");
    precisionMenuItem.addActionListener(this);

    saveSnapshotMenuItem = new JMenuItem("Save a snapshot of this chart");
    saveSnapshotMenuItem.addActionListener(this);

    dvMenu = new JMenu("Data View properties");

    statMenu = new JMenu("Show statistics");
    statAllMenuItem = new JMenuItem("All");
    statAllMenuItem.addActionListener(this);

    /*
    infoMenuItem = new JMenuItem("Chart menu");
    infoMenuItem.setEnabled(false);
    chartMenu.add(infoMenuItem);
    chartMenu.add(new JSeparator());
    */

    chartMenu.add(zoomBackMenuItem);
    chartMenu.add(new JSeparator());
    chartMenu.add(optionMenuItem);
    chartMenu.add(dvMenu);
    chartMenu.add(tableMenu);
    chartMenu.add(statMenu);
    chartMenu.add(new JSeparator());
    chartMenu.add(saveFileMenuItem);
    chartMenu.add(loadFileMenuItem);
    chartMenu.add(printMenuItem);
    chartMenu.add(saveSnapshotMenuItem);
    chartMenu.add(precisionMenuItem);

    sepMenuItem = new JSeparator();
    chartMenu.add(sepMenuItem);

    userActionMenuItem = new JMenuItem[0];
    userAction = new String[0];
    sepMenuItem.setVisible(false);

    //Set up listeners
    addMouseListener(this);
    addMouseWheelListener(this);
    addMouseMotionListener(this);

    listener = null;
    listenerList = new EventListenerList();

    zoomButton = new JButton("Zoom back");
    zoomButton.setFont(labelFont);
    zoomButton.setMargin(new Insets(2,2,1,1));
    zoomButton.setVisible(false);
    zoomButton.addActionListener(this);
    add(zoomButton);
  }

  private void saveSnapshot()
  {
        chartMenu.setVisible(false);
        int ok = JOptionPane.YES_OPTION;
        FileFilter jpgFilter = new FileFilter() {
            public boolean accept (File f)
            {
                if ( f.isDirectory() )
                {
                    return true;
                }
                String extension = MultiExtFileFilter.getExtension( f );
                if ( extension != null && extension.equals( "jpg" ) ) return true;
                return false;
            }

            public String getDescription ()
            {
                return "jpg - JPEG pictures";
            }

            public boolean equals (Object obj) {
                if ( obj == null ) {
                    return false;
                }
                else if ( obj instanceof FileFilter ) {
                    return getDescription().equals(
                            ( (FileFilter) obj ).getDescription() );
                }
                else {
                    return false;
                }
            }
        };
        FileFilter pngFilter = new FileFilter() {
            public boolean accept (File f)
            {
                if ( f.isDirectory() )
                {
                    return true;
                }
                String extension = MultiExtFileFilter.getExtension( f );
                if ( extension != null && extension.equals( "png" ) ) return true;
                return false;
            }

            public String getDescription ()
            {
                return "png - PNG pictures";
            }

            public boolean equals (Object obj) {
                if ( obj == null ) {
                    return false;
                }
                else if ( obj instanceof FileFilter ) {
                    return getDescription().equals(
                            ( (FileFilter) obj ).getDescription() );
                }
                else {
                    return false;
                }
            }
        };
        FileFilter[] filters = new FileFilter[2];
        filters[0] = jpgFilter;
        filters[1] = pngFilter;
        JFileChooser chooser = new JFileChooser( lastSnapshotLocation );
        for (int i = 0; i < filters.length; i++) {
          if ( filters[i].equals(lastFileFilter) ) {
            continue;
          }
          chooser.addChoosableFileFilter(filters[i]);
        }
        if ( lastFileFilter != null ) {
          chooser.addChoosableFileFilter(lastFileFilter);
        }
        chooser.setDialogTitle( "Save snapshot" );
        int returnVal = chooser.showSaveDialog( this );
        String extension = "";
        File f = null;
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
            f = chooser.getSelectedFile();
            if ( f != null )
            {
                lastSnapshotLocation = f.getParentFile().getAbsolutePath();
                FileFilter filter = chooser.getFileFilter();
                if ( filter == jpgFilter )
                {
                    if ( MultiExtFileFilter.getExtension( f ) == null
                            || !MultiExtFileFilter.getExtension( f ).equalsIgnoreCase( "jpg" ) )
                    {
                        f = new File( f.getAbsolutePath() + ".jpg" );
                    }
                    lastFileFilter = filter;
                }
                else if ( filter == pngFilter )
                {
                    if ( MultiExtFileFilter.getExtension( f ) == null
                            || !MultiExtFileFilter.getExtension( f ).equalsIgnoreCase( "png" ) )
                    {
                        f = new File( f.getAbsolutePath() + ".png" );
                    }
                    lastFileFilter = filter;
                }
                if ( f.exists() ) ok = JOptionPane.showConfirmDialog( this,
                        "Do you want to overwrite " + f.getName() + " ?",
                        "Confirm overwrite", JOptionPane.YES_NO_OPTION );
                if ( ok == JOptionPane.YES_OPTION )
                {
                    this.repaint();
                    if ( MultiExtFileFilter.getExtension( f ) == null )
                    {
                        JOptionPane.showMessageDialog( this,
                                "Unknown file type", "Error",
                                JOptionPane.ERROR_MESSAGE );
                    }
                    else if ( MultiExtFileFilter.getExtension( f ).equalsIgnoreCase( "jpg" ) )
                    {
                        extension = "jpg";
                    }
                    else if ( MultiExtFileFilter.getExtension( f ).equalsIgnoreCase( "png" ) )
                    {
                        extension = "png";
                    }
                    else
                    {
                        extension = "";
                    } // end tests for file extension
                } // end if (ok == JOptionPane.YES_OPTION)
                else
                {
                    return;
                }
            } // end if (f != null)
        } // end if (returnVal == JFileChooser.APPROVE_OPTION)
        else
        {
            return;
        }
        if (!"".equals(extension.trim()))
        {
            this.revalidate();
            this.repaint();
            BufferedImage img = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_RGB);
            this.paint(img.getGraphics());
            try
            {
                ImageIO.write( img, extension, f );
            }
            catch (IOException ioe)
            {
                JOptionPane.showMessageDialog( this,
                        "File could not be saved", "Error",
                        JOptionPane.ERROR_MESSAGE );
            }
            img.flush();
        }
        else
        {
            JOptionPane.showMessageDialog( this,
                    "Unknown file type", "Error",
                    JOptionPane.ERROR_MESSAGE );
        }
        jpgFilter = null;
        pngFilter = null;
        for (int i = 0; i < filters.length; i++) {
            filters[i] = null;
        }
        filters = null;
  }

  /**
   * Return a handle to the x axis
   * @return Axis handle
   */
  public JLAxis getXAxis() {
    return xAxis;
  }

  /**
   * Return a handle to the left y axis
   * @return Axis handle
   */
  public JLAxis getY1Axis() {
    return y1Axis;
  }

  /**
   * Return a handle to the right y axis
   * @return Axis handle
   */
  public JLAxis getY2Axis() {
    return y2Axis;
  }

  /**
   * Sets  weather x Axis is on bottom of screen or not
   * @param b boolean to know weather x Axis is on bottom of screen or not
   */
  public void setXAxisOnBottom(boolean b){
      if (b) {
          getXAxis().setPosition(JLAxis.HORIZONTAL_DOWN);
      }
      else {
          getXAxis().setPosition(JLAxis.HORIZONTAL_ORG1);
      }
  }

  /**
   * tells  weather x Axis is on bottom of screen or not
   * @return [code]true[/code] if x Axis is on bottom of screen, [code]false[/code] otherwise
   */
  public boolean isXAxisOnBottom(){
      return getXAxis().getPosition() == JLAxis.HORIZONTAL_DOWN;
  }

  /**
   * Sets header font
   * @param f Header font
   * @see JLChart#getHeaderFont
   */
  public void setHeaderFont(Font f) {
    headerFont = f;
  }

  /**
   * Gets the header font
   * @return Header font
   * @see JLChart#setHeaderFont
   */
  public Font getHeaderFont() {
    return headerFont;
  }

  /**
   * Sets component margin
   * @param d Margin
   * @see JLChart#getMargin
   */
  public void setMargin(Dimension d) {
    margin  = d;
  }

  /**
   * Gets the current margin
   * @return Margin
   * @see JLChart#setMargin
   */
  public Dimension getMargin() {
    return margin;
  }

  public void setBackground(Color c) {
    super.setBackground(c);
    //setChartBackground(c);
  }

  /**
   * Sets the chart background (curve area)
   * @param c Background color
   */
  public void setChartBackground(Color c) {
    chartBackground = c;
  }

  /**
   *
   * Gets the chart background (curve area)
   * @return Background color
   */
  public Color getChartBackground() {
    return chartBackground;
  }

  /**
   * Paints axis under curve when true
   * @param b Painting order
   */
  public void setPaintAxisFirst(boolean b) {
    //paintAxisFirst = true;
    paintAxisFirst = b;
  }

  /**
   * Return painting order between axis and curve
   * @return true if axis are painted under curve
   */
  public boolean isPaintAxisFirst() {
    return paintAxisFirst;
  }

  /**
   * Displays or hides header.
   * @param b true if the header is visible, false otherwise
   * @see JLChart#setHeader
   */
  public void setHeaderVisible(boolean b) {
    headerVisible = b;
  }

  /**
   * @return true if the header is visible, false otherwise
 a  */
  public boolean isHeaderVisible() {
    return headerVisible;
  }

  /**
   * Sets the header and displays it.
   * @param s Graph header
   * @see JLChart#getHeader
   */
  public void setHeader(String s) {
    header = s;
    if (s != null)
      if (s.length() == 0)
        header = null;
    setHeaderVisible(header != null);
    if(parentFrame!=null && header!=null) {
      parentFrame.setTitle(header);
    }
  }

  /**
   * Gets the current header
   * @return Graph header
   * @see JLChart#setHeader
   */
  public String getHeader() {
    return header;
  }

  /**
   * Sets the JFrame that will receive the header as title.
   * @param parent JFrame parent
   */
  public void setFrameParent(JFrame parent) {
    parentFrame = parent;
  }

  /**
   * Sets the display duration.This will garbage old data in all displayed data views.
   * Garbaging occurs when addData is called.
   * @param v Displauy duration (millisec). Pass Double.POSITIVE_INFINITY to disable.
   * @see JLChart#addData
   */
  public void setDisplayDuration(double v) {
    if (v <= maxDisplayDuration)
    {
      // accept displayDuration
      displayDuration = v;
      getXAxis().setAxisDuration(v);
    }
    else
    {
      // refuse displayDuration
      StringBuffer result = new StringBuffer("Duration refused : can not be greater than ");
      if ( JLAxis.TIME_ANNO == getXAxis().getAnnotation()
           || JLAxis.TIME_FORMAT == getXAxis().getLabelFormat()
         )
      {
        // in case of time, convert maxDisplayDuration to readable time
        double days, hours, minutes, seconds, milliseconds;
        int dayTime = 1000 * 60 * 60 * 24;
        int hourTime = 1000 * 60 * 60;
        int minuteTime = 1000* 60;
        int secondTime = 1000;
        double totalTime = maxDisplayDuration;

        days = totalTime / dayTime;
        totalTime -= days * dayTime;

        hours = totalTime / hourTime;
        totalTime -= hours * hourTime;

        minutes = totalTime / minuteTime;
        totalTime -= minutes * minuteTime;

        seconds = totalTime / secondTime;

        totalTime -= seconds * secondTime;
        milliseconds = totalTime;

        if (days > 0)
        {
          String dayString = Double.toString(days);
          if (dayString.endsWith(".0")) dayString = dayString.substring(0, dayString.indexOf("."));
          result.append(dayString).append("day(s) ");
          dayString = null;
        }

        if (hours > 0)
        {
          String hourString = Double.toString(hours);
          if (hourString.endsWith(".0")) hourString = hourString.substring(0, hourString.indexOf("."));
          result.append(hours).append("hr ");
          hourString = null;
        }

        if (minutes > 0)
        {
          String minuteString = Double.toString(minutes);
          if (minuteString.endsWith(".0")) minuteString = minuteString.substring(0, minuteString.indexOf("."));
          result.append(minuteString).append("mn ");
          minuteString = null;
        }

        if (seconds > 0)
        {
          String secondString = Double.toString(seconds);
          if (secondString.endsWith(".0")) secondString = secondString.substring(0, secondString.indexOf("."));
          result.append(secondString).append("s ");
          secondString = null;
        }

        if (milliseconds > 0)
        {
          String millisecondString = Double.toString(milliseconds);
          if (millisecondString.endsWith(".0")) millisecondString = millisecondString.substring(0, millisecondString.indexOf("."));
          result.append(millisecondString).append("ms ");
          millisecondString = null;
        }
        else if (days == 0 && hours == 0 && minutes == 0 && seconds == 0 && milliseconds == 0)
        {
          String millisecondString = Double.toString(milliseconds);
          if (millisecondString.endsWith(".0")) millisecondString = millisecondString.substring(0, millisecondString.indexOf("."));
          result.append(millisecondString).append("ms ");
          millisecondString = null;
        }
      }
      else
      {
          // otherwise, maxDisplayDuration does not have to be converted to readable time
          String maxString = Double.toString(maxDisplayDuration);
          if (maxString.endsWith(".0")) maxString = maxString.substring(0, maxString.indexOf("."));
          result.append(maxString);
          maxString = null;
      }
      JOptionPane.showMessageDialog(this, result.toString(), "Warning !", JOptionPane.WARNING_MESSAGE);
      result = null;
    }
  }

  /**
   * Gets the display duration.
   * @return Display duration
   * @see JLChart#setDisplayDuration
   */
  public double getDisplayDuration() {
    return displayDuration;
  }

  /**
   * Gets the maximum allowed for a display duration
   * @return Maximum allowed for a display duration
   * @see JLChart#setMaxDisplayDuration
   * @see JLChart#setDisplayDuration
   */
  public double getMaxDisplayDuration ()
  {
      return maxDisplayDuration;
  }

  /**
   * Sets the maximum allowed for a display duration
   * @param maxDisplayDuration The maximum allowed for a display duration
   * @see JLChart#getMaxDisplayDuration
   * @see JLChart#getDisplayDuration
   * @see JLChart#setDisplayDuration
   */
  public void setMaxDisplayDuration (double maxDisplayDuration)
  {
      this.maxDisplayDuration = maxDisplayDuration;
  }

  /**
   * Sets the header color
   * @param c Header color
   */
  public void setHeaderColor(Color c) {
    headerColor = c;
    setHeaderVisible(true);
  }

  /**
   * @return the header color
   */
  public Color getHeaderColor() {
    return headerColor;
  }

  /**
   * Displays or hide labels.
   * @param b true if labels are visible, false otherwise
   * @see JLChart#isLabelVisible
   */
  public void setLabelVisible(boolean b) {
    labelVisible = b;
  }

  /**
   * Determines wether labels are visivle or not.
   * @return true if labels are visible, false otherwise
   */
  public boolean isLabelVisible() {
    return labelVisible;
  }

  /**
   * Set the label placement.
   * @param p Placement
   * @see JLChart#LABEL_UP
   * @see JLChart#LABEL_DOWN
   * @see JLChart#LABEL_ROW
   * @see JLChart#LABEL_LEFT
   * @see JLChart#LABEL_RIGHT
   */
  public void setLabelPlacement(int p) {
    labelMode = p;
  }

  /**
   * Returns the current label placement.
   * @return Label placement
   * @see JLChart#setLabelPlacement
   */
  public int getLabelPlacement() {
    return labelMode;
  }

  /**
   * Sets the label font
   * @param f Label font
   */
  public void setLabelFont(Font f) {
    labelFont = f;
  }

  /**
   * @return the label font
   * @see #setLabelFont
   */
  public Font getLabelFont() {
    return labelFont;
  }

  /**
   * Display the global graph option dialog.
   */
  public void showOptionDialog() {

    Object dlgParent = getRootPane().getParent();
    JLChartOption optionDlg;

    if (dlgParent instanceof JDialog) {
      optionDlg = new JLChartOption((JDialog) dlgParent, this);
    } else if (dlgParent instanceof JFrame) {
      optionDlg = new JLChartOption((JFrame) dlgParent, this);
    } else {
      optionDlg = new JLChartOption((JFrame) null, this);
    }

    ATKGraphicsUtils.centerDialog(optionDlg);
    optionDlg.setVisible(true);

  }

  /**
   * Display the data view option dialog.
   * @param v Dataview
   */
  public void showDataOptionDialog(JLDataView v) {

    Object dlgParent = getRootPane().getParent();
    JLDataViewOption optionDlg;

    if (dlgParent instanceof JDialog) {
      optionDlg = new JLDataViewOption((JDialog) dlgParent, this, v);
    } else if (dlgParent instanceof JFrame) {
      optionDlg = new JLDataViewOption((JFrame) dlgParent, this, v);
    } else {
      optionDlg = new JLDataViewOption((JFrame) null, this, v);
    }

    ATKGraphicsUtils.centerDialog(optionDlg);
    optionDlg.setVisible(true);

  }

  /**
   * Determines wether the graph is zoomed.
   * @return true if the , false otherwise
   */
  public boolean isZoomed() {
    return xAxis.isZoomed() || y1Axis.isZoomed() || y2Axis.isZoomed();
  }

  /**
   * Enter zoom mode. This happens when you hold the left mouse button down
   * and drag the mouse.
   */
  public void enterZoom() {
    if (!zoomDragAllowed) {
      zoomDragAllowed = true;
      setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }
  }

  /**
   * Set the specified JLChart Listener
   * @param l JLChart listener. If set to null the listener will be removed.
   */
  public void setJLChartListener(IJLChartListener l) {
    listener = l;
  }

  /**
   * Adds a user action. It will be available from the contextual
   * chart menu. All JLChartActionListener are triggered when
   * a user action is executed.
   * Hint: If the action name starts with 'chk' , it will be
   * displayed as check box menu item. Each time the chart menu
   * is shown, a getActionState() is executed on all listener,
   * if several listener handle the same action, the result will be a
   * logical and of all results.
   * @param name Action name
   */
  public void addUserAction(String name) {
    int i;

    String[] action = new String[userAction.length + 1];
    for (i = 0; i < userAction.length; i++) action[i] = userAction[i];
    action[i] = name;

    // Build the menu
    for (i = 0; i < userActionMenuItem.length; i++) {
      chartMenu.remove(userActionMenuItem[i]);
      userActionMenuItem[i].removeActionListener(this);
      userActionMenuItem[i] = null;
    }

    JMenuItem[] actionMenu = new JMenuItem[action.length];
    for (i = 0; i < action.length; i++) {
      if (action[i].startsWith("chk")) {
        actionMenu[i] = new JCheckBoxMenuItem(action[i].substring(3));
      } else {
        actionMenu[i] = new JMenuItem(action[i]);
      }
      actionMenu[i].addActionListener(this);
      chartMenu.add(actionMenu[i]);
    }

    userActionMenuItem = actionMenu;
    userAction = action;
    sepMenuItem.setVisible(true);

  }

  /**
   * Removes a user action from chart menu.
   * @param name Action name
   */
  public void removeUserAction(String name) {
    int i;

    int correspondingIndex = -1;

    for (i = 0; i < userAction.length; i++) {
        if ( userAction[i].equals(name) ) {
            correspondingIndex = i;
            break;
        }
    }
    if (correspondingIndex != -1) {
      String[] action = new String[userAction.length - 1];
      for (i = 0; i < correspondingIndex; i++) {
        action[i] = userAction[i];
      }
      for (i = correspondingIndex + 1;  i < userAction.length; i++) {
        action[i-1] = userAction[i];
      }

      // Build the menu
      for (i = 0; i < userActionMenuItem.length; i++) {
        chartMenu.remove(userActionMenuItem[i]);
        userActionMenuItem[i].removeActionListener(this);
        userActionMenuItem[i] = null;
      }

      JMenuItem[] actionMenu = new JMenuItem[action.length];
      for (i = 0; i < action.length; i++) {
        if (action[i].startsWith("chk")) {
          actionMenu[i] = new JCheckBoxMenuItem(action[i].substring(3));
        }
        else {
          actionMenu[i] = new JMenuItem(action[i]);
        }
        actionMenu[i].addActionListener(this);
        chartMenu.add(actionMenu[i]);
      }

      userActionMenuItem = actionMenu;
      userAction = action;
      sepMenuItem.setVisible(true);
    }

  }

  /**
   * Add the specified JLChartAction listener to the list
   * @param l Listener to add
   */
  public void addJLChartActionListener(IJLChartActionListener l) {
    listenerList.add(IJLChartActionListener.class,  l);
  }

  /**
   * Exit zoom mode.
   */
  public void exitZoom() {
    if( !Double.isNaN(oldDisplayDuration) ) {
      setDisplayDuration(oldDisplayDuration);
      oldDisplayDuration = Double.NaN;
    } else {
      xAxis.unzoom();
    }
    y1Axis.unzoom();
    y2Axis.unzoom();
    zoomDragAllowed = false;
    setCursor(Cursor.getDefaultCursor());
    repaint();
  }

  /**
   * Method to remove item of the contextual menu.
   * @param menu Item to remove
   * @see #MENU_CHARTPROP
   * @see #MENU_DVPROP
   * @see #MENU_TABLE
   * @see #MENU_DATASAVE
   * @see #MENU_PRINT
   * @see #MENU_STAT
   * @see #MENU_DATALOAD
   */
  public void removeMenuItem(int menu) {

    switch(menu) {
      /* Chart properties menu item */
      case MENU_CHARTPROP:
        chartMenu.remove(optionMenuItem);
        break;
      /* Data view properties menu item */
      case MENU_DVPROP:
        chartMenu.remove(dvMenu);
        break;
      /* Show table menu item */
      case MENU_TABLE:
        chartMenu.remove(tableMenu);
        break;
      /* Save data file menu item */
      case MENU_DATASAVE:
        chartMenu.remove(saveFileMenuItem);
        break;
      /* print graph menu item */
     case MENU_PRINT:
        chartMenu.remove(printMenuItem);
        break;
       /* Statistics menu */
     case MENU_STAT:
       chartMenu.remove(statMenu);
       break;
     case MENU_DATALOAD:
       chartMenu.remove(loadFileMenuItem);
       break;
    }
  }

  /**
   * Method to add item to the contextual menu.
   * @param menu MenuItem to add
   */
  public void addMenuItem(JMenuItem menu) {
    chartMenu.add(menu);
  }

  /**
   * Method to add a separator to the contextual menu.
   */
  public void addSeparator() {
    chartMenu.addSeparator();
  }

  /**
   * Remove the specified JLChartAction Listener
   * @param l Listener to remove
   */
  public void removeJLChartActionListener(IJLChartActionListener l) {
    listenerList.remove(IJLChartActionListener.class, l);
  }

  /**
   * Apply graph configuration. This includes all global settings.
   * The CfFileReader object must have been filled by the caller.
   * @param f Handle to CfFileReader object that contains global graph param
   * @see CfFileReader#parseText
   * @see CfFileReader#readFile
   * @see JLAxis#applyConfiguration
   * @see JLDataView#applyConfiguration
   */
  public void applyConfiguration(CfFileReader f) {

    Vector p;

    // General settings
    p = f.getParam("graph_title");
    if (p != null) setHeader(OFormat.getName(p.get(0).toString()));
    p = f.getParam("label_visible");
    if (p != null) setLabelVisible(OFormat.getBoolean(p.get(0).toString()));
    p = f.getParam("label_placement");
    if (p != null) setLabelPlacement(OFormat.getInt(p.get(0).toString()));
    p = f.getParam("label_font");
    if (p != null) setLabelFont(OFormat.getFont(p));
    p = f.getParam("graph_background");
    if (p != null) setBackground(OFormat.getColor(p));
    p = f.getParam("chart_background");
    if (p != null) setChartBackground(OFormat.getColor(p));
    p = f.getParam("title_font");
    if (p != null) setHeaderFont(OFormat.getFont(p));
    p = f.getParam("display_duration");
    if (p != null) setDisplayDuration(OFormat.getDouble(p.get(0).toString()));
    p = f.getParam("precision");
    if (p != null) setTimePrecision(OFormat.getInt(p.get(0).toString()));

  }

  /**
   * Build a configuration string that can be write into a file and is compatible
   * with CfFileReader.
   * @return A string containing param.
   * @see JLChart#applyConfiguration
   * @see JLDataView#getConfiguration
   * @see JLAxis#getConfiguration
   */
  public String getConfiguration() {

    String to_write = "";

    to_write += "graph_title:\'" + getHeader() + "\'\n";
    to_write += "label_visible:" + isLabelVisible() + "\n";
    to_write += "label_placement:" + getLabelPlacement() + "\n";
    to_write += "label_font:" + OFormat.font(getLabelFont()) + "\n";
    to_write += "graph_background:" + OFormat.color(getBackground()) + "\n";
    to_write += "chart_background:" + OFormat.color(getChartBackground()) + "\n";
    to_write += "title_font:" + OFormat.font(getHeaderFont()) + "\n";
    to_write += "display_duration:" + getDisplayDuration() + "\n";
    to_write += "precision:" + getTimePrecision() + "\n";

    return to_write;
  }

  /**
   * @return a string containing the configuration file help.
   */
  public String getHelpString() {

    return "-- Global chart settings --\n\n" +
           "graph_title:'title'   Chart title ('null' to disable)\n" +
           "label_visible:true or false  Show legend\n" +
           "label_placement:value   (0 Down,1 Up,2 Right, 3 Left)\n" +
           "label_font:name,style(0 Plain,1 Bold,2 italic),size \n" +
           "graph_background:r,g,b   Component background \n" +
           "chart_background:r,g,b   Graph area background \n" +
           "title_font:name,style(0 Plain,1 Bold,2 italic),size\n" +
           "display_duration:milliSec   X axis duration (time monitoring)\n\n" +
           JLAxis.getHelpString() + "\n" +
           JLDataView.getHelpString();

  }

  /**
   * Remove all dataview from the graph.
   */
  public void unselectAll() {
    getY1Axis().clearDataView();
    getY2Axis().clearDataView();
    getXAxis().clearDataView();
  }

  /**
   * Prints out this graph.
   */
  public void printGraph() {

    ATKGraphicsUtils.printComponent(this,"Print Graph",true,0);

  }

  // -----------------------------------------------------

  // Fire JLChartActionEvent to all registered IJLChartActionListener
  private void fireActionPerfromed(String name, boolean state) {
    IJLChartActionListener[] list = (IJLChartActionListener[]) (listenerList.getListeners(IJLChartActionListener.class));
    JLChartActionEvent w = new JLChartActionEvent(this, name, state);
    for (int i = 0; i < list.length; i++) list[i].actionPerformed(w);
  }

  // Fire JLChartActionEvent to all registered IJLChartActionListener
  private boolean fireGetActionState(String name) {
    IJLChartActionListener[] list = (IJLChartActionListener[]) (listenerList.getListeners(IJLChartActionListener.class));
    JLChartActionEvent w = new JLChartActionEvent(this, name);
    boolean ret = true;
    for (int i = 0; i < list.length; i++)
      ret = list[i].getActionState(w) && ret;

    return ret;
  }

  // Make a snapshot of data in a TAB seperated field
  private void saveDataFile(String fileName,boolean correlated,boolean decimate,String decimatePeriod) {

    try {

      double decPeriod = Double.parseDouble(decimatePeriod)/1000.0;
      FileWriter fw = new FileWriter(fileName);
      TabbedLine tl;
      String s;

      Vector<JLDataView>  views = new Vector<JLDataView> ();
      if (xAxis.isXY()) views.addAll(xAxis.getViews());
      views.addAll(y1Axis.getViews());
      views.addAll(y2Axis.getViews());

      tl = new TabbedLine(views.size());
      tl.setPrecision(timePrecision);
      tl.setCorrelated(correlated);
      tl.setNoValueString( noValueString );

      for (int v = 0; v < views.size(); v++) tl.add(v, views.get(v));

      s = tl.getFirstLine(xAxis.getAnnotation());
      fw.write(s);
      double lastT = 0.0;

      while(s!=null) {

        s = tl.getNextLine();
        if (s != null) {

          double t = tl.getMinTime();
          if (decimate) {
            if (t - lastT > decPeriod) {
              fw.write(s);
              lastT = t;
            }
          } else {
            fw.write(s);
          }

        }

      }

      fw.close();

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error during saving file.\n" + e.getMessage());
    }

  }

  /**
   * Loads a data file and add the corresponding data to Y1 axis
   * @param fileName the full path of the data file
   */

  public void loadDataFile(String fileName) {

    try {

      int curLine = 0;
      File dataFile = new File(fileName);
      BufferedReader reader = new BufferedReader(new FileReader(dataFile));
      String line = "";
      line = reader.readLine();curLine++;
      if ("".equals(line.trim())) throw new Exception("Empty file");
      String[] parsedLine = line.split("\t");
      int annotationType = -1;
      if ("Time (s)".equals(parsedLine[0].trim())) {
        annotationType = JLAxis.TIME_ANNO;
      } else if ("Index".equals(parsedLine[0].trim())) {
        annotationType = JLAxis.VALUE_ANNO;
      } else {
        reader.close();
        reader = null;
        dataFile = null;
        line = null;
        parsedLine = null;
        throw new Exception("Failed to read X Axis annotation type");
      }

      Vector<JLDataView> existingViews = new Vector<JLDataView>();
      if (xAxis.isXY()) existingViews.addAll(xAxis.getViews());
      existingViews.addAll(y1Axis.getViews());
      existingViews.addAll(y2Axis.getViews());
      if (existingViews.size() != 0 && annotationType != getXAxis().getAnnotation()) {
        existingViews.clear();
        existingViews = null;
        String warning = "Loading this file will change X Axis annotation type.\n"
                + "Your component may not work any more.\n"
                + "Are you sure to load this file ?";
        int choice = JOptionPane.showConfirmDialog(
                this,
                warning,
                "Risk of breaking component",
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.OK_OPTION) {
          reader.close();
          reader = null;
          dataFile = null;
          line = null;
          parsedLine = null;
          return;
        }
      }

      lastDataFileLocation = dataFile.getParentFile().getAbsolutePath();
      int viewCount = parsedLine.length - 1;
      if (viewCount < 0) throw new Exception();
      JLDataView[] views = new JLDataView[viewCount];
      Color[] defaultColor = {
              Color.red,
              Color.blue,
              Color.cyan,
              Color.green,
              Color.magenta,
              Color.orange,
              Color.pink,
              Color.yellow,
              Color.black
      };
      for (int i = 0; i < views.length; i++) {
        views[i] = new JLDataView();
        views[i].setName(parsedLine[i + 1].trim());
        views[i].setLineWidth(1);
        views[i].setColor(defaultColor[i % defaultColor.length]);
        views[i].setStyle(JLDataView.STYLE_SOLID);
        views[i].setViewType(JLDataView.TYPE_LINE);
        views[i].setMarkerSize(5);
        views[i].setMarker(JLDataView.MARKER_BOX);
        views[i].setMarkerColor(views[i].getColor());
      }

      double time = 0;
      double minTime = Double.MAX_VALUE, maxTime = -Double.MAX_VALUE;

      SimpleDateFormat genFormatFR = new SimpleDateFormat(FR_DATE_FORMAT);
      SimpleDateFormat genFormatUS = new SimpleDateFormat(US_DATE_FORMAT);
      while (true) {
        line = reader.readLine();curLine++;
        if (line == null) {
          break;
        }
        parsedLine = line.split("\t",-1);
        if (parsedLine.length - 1 < viewCount) throw new Exception("Unexpected number of field at line " + curLine);
        if (annotationType == JLAxis.TIME_ANNO) {
          try {
            time = Double.parseDouble(parsedLine[0]) * 1000;
          } catch (NumberFormatException e) {
            if (parsedLine[0].indexOf(".") == -1) {
              parsedLine[0] = parsedLine[0] + (".000");
            }
            if (parsedLine[0].indexOf("-") != 4) {
              genFormatFR.parse(parsedLine[0]);
              time = genFormatFR.getCalendar().getTimeInMillis();
            } else {
              genFormatUS.parse(parsedLine[0]);
              time = genFormatUS.getCalendar().getTimeInMillis();
            }
          }
        } else {
          try {
            time = Double.parseDouble(parsedLine[0]);
          } catch (NumberFormatException nfe) {
            continue; // error on this line, try to read the other ones
          }
        }
        if (time > maxTime) maxTime = time;
        if (time < minTime) minTime = time;
        for (int i = 0; i < views.length; i++) {
          try {
            views[i].add(time, Double.parseDouble(parsedLine[i + 1]));
          } catch (NumberFormatException nfe) {
            if ("null".equalsIgnoreCase(parsedLine[i + 1].trim())) {
              // case null
              views[i].add(time, JLDataView.NAN_FOR_NULL);
            } else continue;// no data at this time
          }
        }
      }
      reader.close();
      reader = null;
      dataFile = null;
      line = null;
      parsedLine = null;
      genFormatFR = null;
      genFormatUS = null;

      reset(false);
      setDisplayDuration(maxTime-minTime);

      for (int i = 0; i < views.length; i++) {
        getY1Axis().addDataView(views[i]);
      }

      getXAxis().setAnnotation(annotationType);


      repaint();
      defaultColor = null;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Failed to load file: " + fileName + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

  }

  // Refresh a JTable containing data of a single dataView when the tableDialog is visible
  public void refreshTableSingle(JLDataView v) {

    if (preferDialog) {
      if (tableDialog == null) return;
      if (!tableDialog.isVisible()) return;
    } else {
      if (theTable == null) return;
      if (!theTable.isVisible()) return;
    }
    updateTableDataSingle(v);

  }

  private void updateTableDataSingle (JLDataView v) {
        TabbedLine tl;
        tl = new TabbedLine( 1 );
        tl.add( 0, v );
        // Build data
        Vector<Object[]> data = new Vector<Object[]>();
        String[] cols = tl
                .getFirstFields( xAxis.getAnnotation(), !xAxis.isXY() );
        String[] s = tl.getNextFields();
        while (s != null) {
            data.add( s );
            s = tl.getNextFields();
        }
        int y = data.size();
        int x = cols.length;
        Object[][] dv = new Object[y][x];
        for (int j = 0; j < y; j++) {
            Object[] ln = data.get( j );
            for (int i = 0; i < x; i++) {
                dv[j][i] = ln[i];
            }
        }
        if ( preferDialog && tableDialog != null ) {
            JTableRow row = null;
            try {
                row = (JTableRow) tableDialog.getContentPane();
            }
            catch (ClassCastException cce) {
                if ( theTable != null ) {
                    row = (JTableRow) theTable.getContentPane();
                }
                else {
                    // nothing can be done
                    return;
                }
            }
            row.setData( dv, cols );
            row = null;
        }
        else {
            theTable.setData( dv, cols );
        }
    }

  // Display a JTable containing data of a single dataView
  private void showTableSingle(JLDataView v) {


    if (theTable == null)
      theTable = new JLTable();

    if (tableDialog == null)
    {
      if (dialogParent != null)
      {
        if (dialogParent instanceof Frame)
        {
          tableDialog = new JDialog((Frame)dialogParent, theTable.getTitle(), modalDialog);
        }
        else if (dialogParent instanceof Dialog)
        {
          tableDialog = new JDialog((Dialog)dialogParent, theTable.getTitle(), modalDialog);
        }
        else
        {
          tableDialog = new JDialog((Frame)null, theTable.getTitle(), modalDialog);
        }
      }
      else
      {
        tableDialog = new JDialog((Frame)null, theTable.getTitle(), modalDialog);
      }
    }

    updateTableDataSingle(v);

    if (tableDialog != null && tableDialog.getContentPane() instanceof JTableRow)
    {
        theTable.setContentPane( tableDialog.getContentPane() );
    }

    if (!theTable.isVisible())
      theTable.centerWindow();

    if (preferDialog)
    {
      tableDialog.setContentPane( theTable.getContentPane() );
      tableDialog.setBounds( theTable.getBounds() );
      tableDialog.setResizable(theTable.isResizable());
      theTable.setVisible(false);
      theTable = null;
      tableDialog.setVisible(true);
    }
    else
    {
      tableDialog.setVisible(false);
      tableDialog = null;
      theTable.setVisible(true);
    }

    if (preferDialog)
    {
        tableDialog.repaint();
    }
    else
    {
        theTable.repaint();
    }

  }

  // Displays a dialog in which user can set precision (for table construction)
  private void displayPrecisionDialog() {
      final JDialog precisionDialog = new JDialog((Frame)null,"Set error margin",true);
      precisionDialog.setLocationRelativeTo(this);
      JLabel precisionLabel = new JLabel("Set error margin in x units.");
      JLabel detailLabel = new JLabel("If x represents time, unit is ms.");
      final char[] allowed = {'0','1','2','3','4','5','6','7','8','9',KeyEvent.VK_BACK_SPACE,KeyEvent.VK_DELETE,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_KP_LEFT,KeyEvent.VK_KP_RIGHT,KeyEvent.VK_ENTER,KeyEvent.VK_TAB,KeyEvent.VK_SHIFT};
      JTextField textField = new JTextField();
      //allow positive numbers only
      textField.addKeyListener(new KeyListener(){
        public void keyPressed(KeyEvent arg0) {
            if ( !validateChar(arg0.getKeyChar()) ) {
                arg0.consume();
            }
        }
        public void keyReleased(KeyEvent arg0) {
            if ( !validateChar(arg0.getKeyChar()) ) {
                arg0.consume();
            }
        }
        public void keyTyped(KeyEvent arg0) {
            if ( !validateChar(arg0.getKeyChar()) ) {
                arg0.consume();
            }
        }
        private boolean validateChar(char c) {
            for (int j=0; j < allowed.length; j++) {
                if (c == allowed[j]) {
                    return true;
                }
            }
            return false;
        }
      });
      textField.setColumns(5);
      textField.setText(""+this.timePrecision);
      JButton ok = new JButton("ok");
      ok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            precisionDialog.setVisible(false);
        }
      });
      JPanel panel = new JPanel();
      Box box = new Box(BoxLayout.Y_AXIS);
      box.add(precisionLabel);
      box.add(textField);
      box.add(detailLabel);
      box.add(ok);
      panel.add(box);
      precisionDialog.setContentPane(panel);
      precisionDialog.setSize(230,120);
      precisionDialog.setResizable(false);
      precisionDialog.setVisible( true );
      int prec = timePrecision;
      try {
          this.timePrecision = Integer.parseInt(textField.getText());
      }
      catch (Exception e) {
          timePrecision = prec;
      }
  }

  //Extract the date to display in order to overwrite it in extends class
  protected void showTableAll(Vector<JLDataView>  views){
      TabbedLine tl;
      
      tl = new TabbedLine(views.size());
      //-------precision-------//
      tl.setPrecision(timePrecision);
      //-------precision-------//
      for (int v = 0; v < views.size(); v++) tl.add(v, views.get(v));

      if (theTable == null)
        theTable = new JLTable();

      if (tableDialog == null)
      {
        if (dialogParent != null)
        {
          if (dialogParent instanceof Frame)
          {
            tableDialog = new JDialog((Frame)dialogParent, theTable.getTitle(), modalDialog);
          }
          else if (dialogParent instanceof Dialog)
          {
            tableDialog = new JDialog((Dialog)dialogParent, theTable.getTitle(), modalDialog);
          }
          else
          {
            tableDialog = new JDialog((Frame)null, theTable.getTitle(), modalDialog);
          }
        }
        else
        {
          tableDialog = new JDialog((Frame)null, theTable.getTitle(), modalDialog);
        }
      }

      // Build data
      Vector<Object[]> data = new Vector<Object[]>();
      String[] cols = tl.getFirstFields(xAxis.getAnnotation(),!xAxis.isXY());
      String[] s = tl.getNextFields();
      while (s != null) {
        data.add(s);
        s = tl.getNextFields();
      }

      int y = data.size();
      int x = cols.length;
      Object[][] dv = new Object[y][x];
      for (int j = 0; j < y; j++) {
        Object[] ln = data.get(j);
        for (int i = 0; i < x; i++) {
          dv[j][i] = ln[i];
        }
      }

      if (x == 1 && y == 0) {
        // no data
        dv = null;
        cols = null;
      }
      theTable.setData(dv, cols);

      if (!theTable.isVisible())
        theTable.centerWindow();

      if (preferDialog) {
        tableDialog.setContentPane(theTable.getContentPane());
        tableDialog.setBounds(theTable.getBounds());
        tableDialog.setResizable(theTable.isResizable());
        theTable.setVisible(false);
        theTable = null;
        tableDialog.setVisible(true);
      }
      else {
        tableDialog.setVisible(false);
        tableDialog = null;
        theTable.setVisible(true);
      }
  }
  
  // Display a JTable containing all data of the chart
  protected void showTableAll() {

    Vector<JLDataView>  views = new Vector<JLDataView> ();
    if (xAxis.isXY()) views.addAll(xAxis.getViews());
    views.addAll(y1Axis.getViews());
    views.addAll(y2Axis.getViews());    
    showTableAll(views);
  }


  // Display a Dialog containing all statistics of the chart
  protected void showStatAll() {
    Vector<JLDataView>  views = new Vector<JLDataView> ();
    views.addAll(y1Axis.getViews());
    views.addAll(y2Axis.getViews());
    StatFrame fr = new StatFrame(this,views);
    fr.setVisible(true);
  }

  // Display a Dialog containing The dataView statistics
  private void showStatSingle(JLDataView v) {
    Vector<JLDataView>  views = new Vector<JLDataView> ();
    views.add(v);
    StatFrame fr = new StatFrame(this,views);
    fr.setVisible(true);
  }

  // -----------------------------------------------------
  // Action listener
  // -----------------------------------------------------
  public void actionPerformed(ActionEvent evt) {

    Object src= evt.getSource();

    if (src == optionMenuItem) {
      showOptionDialog();
    } else if (src == zoomBackMenuItem || src == zoomButton) {
      exitZoom();
    } else if (src == printMenuItem ) {
      printGraph();
    } else if (src == saveSnapshotMenuItem) {
      saveSnapshot();
    } else if (src == precisionMenuItem) {
      displayPrecisionDialog();
    } else if (src == tableAllMenuItem) {
      showTableAll();
    } else if (src == statAllMenuItem) {
       showStatAll();
    } else if (src == loadFileMenuItem) {
      readFile();
    } else if (src == saveFileMenuItem) {

      int ok = JOptionPane.YES_OPTION;

      // Acessory panel
      javax.swing.JPanel aPanel;
      javax.swing.JCheckBox correlatedCheck;
      javax.swing.JPanel correlationPanel;
      javax.swing.JCheckBox decimateCheck;
      javax.swing.JTextField decimateText;
      javax.swing.JPanel decimationPanel;
      javax.swing.JLabel periodLabel;
      GridBagConstraints gridBagConstraints;

      aPanel = new javax.swing.JPanel();
      correlationPanel = new javax.swing.JPanel();
      correlatedCheck = new javax.swing.JCheckBox();
      decimationPanel = new javax.swing.JPanel();
      decimateText = new javax.swing.JTextField();
      periodLabel = new javax.swing.JLabel();
      decimateCheck = new javax.swing.JCheckBox();

      aPanel.setLayout(new java.awt.GridBagLayout());

      correlationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Correlated Data"));
      correlationPanel.setLayout(new java.awt.BorderLayout());

      correlatedCheck.setText("Enable");
      correlationPanel.add(correlatedCheck, java.awt.BorderLayout.CENTER);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      aPanel.add(correlationPanel, gridBagConstraints);

      decimationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Decimation"));
      decimationPanel.setLayout(new java.awt.GridBagLayout());

      decimateText.setText("1000");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.ipadx = 20;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      decimationPanel.add(decimateText, gridBagConstraints);

      periodLabel.setText("Period (ms)");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.ipadx = 10;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      decimationPanel.add(periodLabel, gridBagConstraints);

      decimateCheck.setText("Enable");
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      decimationPanel.add(decimateCheck, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      aPanel.add(decimationPanel, gridBagConstraints);


      JFileChooser chooser = new JFileChooser(lastDataFileLocation);
      chooser.addChoosableFileFilter(new MultiExtFileFilter("Text files", "txt"));
      chooser.setDialogTitle("Save Graph Data (Text file with TAB separated fields)");
      chooser.setAccessory(aPanel);

      int returnVal = chooser.showSaveDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();
        if (f != null) {
          if (MultiExtFileFilter.getExtension(f) == null) {
            f = new File(f.getAbsolutePath() + ".txt");
          }
          if (f.exists()) {
            ok = JOptionPane.showConfirmDialog(
                this,
                "Do you want to overwrite " + f.getName() + " ?",
                "Confirm overwrite",
                JOptionPane.YES_NO_OPTION
            );
          }
          if (ok == JOptionPane.YES_OPTION) {
            lastDataFileLocation = f.getParentFile().getAbsolutePath();
            saveDataFile(f.getAbsolutePath(), correlatedCheck.isSelected(), decimateCheck.isSelected(), decimateText.getText());
          }
        }
      }
    } else {

      // Search in user action
      boolean found = false;
      int i = 0;
      while (i < userActionMenuItem.length && !found) {
        found = userActionMenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        if (userActionMenuItem[i] instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem c = (JCheckBoxMenuItem) userActionMenuItem[i];
          fireActionPerfromed(c.getText(), c.getState());
        } else {
          fireActionPerfromed(userActionMenuItem[i].getText(), false);
        }
        return;
      }

      // Search in show Data View option menu
      i = 0;
      while (i < dvY1MenuItem.length && !found) {
        found = dvY1MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showDataOptionDialog(y1Axis.getDataView(i));
        return;
      }

      i = 0;
      while (i < dvY2MenuItem.length && !found) {
        found = dvY2MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showDataOptionDialog(y2Axis.getDataView(i));
        return;
      }

      // Search in show table single menu item
      i = 0;
      while (i < tableSingleY1MenuItem.length && !found) {
        found = tableSingleY1MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showTableSingle(y1Axis.getDataView(i));
        return;
      }

      i = 0;
      while (i < tableSingleY2MenuItem.length && !found) {
        found = tableSingleY2MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showTableSingle(y2Axis.getDataView(i));
        return;
      }

      // Search in show stat single menu item
      i = 0;
      while (i < statSingleY1MenuItem.length && !found) {
        found = statSingleY1MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showStatSingle(y1Axis.getDataView(i));
        return;
      }

      i = 0;
      while (i < statSingleY2MenuItem.length && !found) {
        found = statSingleY2MenuItem[i] == evt.getSource();
        if (!found) i++;
      }

      if (found) {
        showStatSingle(y2Axis.getDataView(i));
        return;
      }

    }

  }

  private void paintLabel(Graphics2D g,JLDataView v,JLAxis axis,int x,int y,int w,int a) {

    int xm   = x + (w - labelWidth) / 2 + 7;
    int ym   = y + labelHeight/2 + 2;
    g.setColor(v.getColor());
    JLAxis.drawSampleLine(g, xm, ym - 2, v);
    g.setColor(v.getLabelColor());
    g.drawString(v.getExtendedName() + " " + axis.getAxeName(), xm + 44, ym + labelHeight - a);
    labelRect.add(new LabelRect(xm, ym - a, labelWidth, labelHeight, v));

  }

  // paint Label and header
  private void paintLabelAndHeader(Graphics2D g) {

    int nbv1 = y1Axis.getViews().size();
    int nbv2 = y2Axis.getViews().size();

    // Draw header
    if (headerR.width>0) {
      g.setFont(headerFont);
      int xpos = ((headerR.width - headerWidth) / 2);
      g.setColor(headerColor);
      g.drawString(header, xpos, headerR.y + g.getFontMetrics(headerFont).getAscent() - 1);
    }

    // Draw labels
    labelRect.clear();
    if (labelR.width>0) {

      g.setFont(labelFont);
      JLDataView v;
      int a = g.getFontMetrics(labelFont).getAscent();
      int i,k = 0;

      if( labelMode==LABEL_ROW && labelPerLine>1 ) {

        int rowWidth = labelR.width / labelPerLine;

        // Draw labels (in row/column)
        for (i = 0; i < nbv1; i++) {
          v = y1Axis.getViews().get(i);
          if (v.isLabelVisible()) {
            int x = (k%labelPerLine) * rowWidth + labelR.x;
            int y = (k/labelPerLine) * labelHeight + labelR.y;
            paintLabel(g,v,y1Axis,x,y,rowWidth,a);
            k++;
          }
        }

        for (i = 0; i < nbv2; i++) {
          v = y2Axis.getViews().get(i);
          if (v.isLabelVisible()) {
            int x = (k%labelPerLine) * rowWidth + labelR.x;
            int y = (k/labelPerLine) * labelHeight + labelR.y;
            paintLabel(g,v,y2Axis,x,y,rowWidth,a);
            k++;
          }
        }

      } else {

        // Draw labels (in column)
        for (i = 0; i < nbv1; i++) {
          v = y1Axis.getViews().get(i);
          if (v.isLabelVisible()) {
            int y = labelR.y + (labelR.height-nbLabel*labelHeight)/2 + labelHeight * k;
            paintLabel(g,v,y1Axis,labelR.x,y,labelR.width,a);
            k++;
          }
        }

        for (i = 0; i < nbv2; i++) {
          v = y2Axis.getViews().get(i);
          if (v.isLabelVisible()) {
            int y = labelR.y + (labelR.height-nbLabel*labelHeight)/2 + labelHeight * k;
            paintLabel(g,v,y2Axis,labelR.x,y,labelR.width,a);
            k++;
          }
        }

      }

    }

  }

  // Compute size of graph items (Axe,label,header,....
  private void measureGraphItems(Graphics2D g, FontRenderContext frc, int w, int h, Vector views) {

    Rectangle2D bounds = null;
    int i;
    int MX = margin.width;
    int MY = margin.height;
    int labelTHeight = 0; // Total label height

    // Reset sizes ------------------------------------------------------
    headerR.setBounds(0,0,0,10);
    viewR.setBounds(0, 0, 0, 0);
    labelR.setBounds(0, 0, 0, 0);
    labelWidth = 0;
    headerWidth = 0;
    axisWidth = 0;
    axisHeight = 0;
    y1AxisThickness = 0;
    y2AxisThickness = 0;

    // Measure header ------------------------------------------------------
    if (headerVisible && (header!=null) && (headerFont!=null)) {
      bounds = headerFont.getStringBounds(header, frc);
      headerWidth = (int) bounds.getWidth();
      headerR.setBounds(MX , MY , w-2*MX , (int)bounds.getHeight() + 5);
    }

    // Compute label number ------------------------------------------------------
    nbLabel=0;
    for (i = 0; i < y1Axis.getViews().size(); i++)
      if( y1Axis.getViews().get(i).isLabelVisible() )
        nbLabel++;
    for (i = 0; i < y2Axis.getViews().size(); i++)
      if( y2Axis.getViews().get(i).isLabelVisible() )
        nbLabel++;

    // Measure labels ------------------------------------------------------
    if (labelVisible && (nbLabel>0) && (labelFont!=null)) {

      JLDataView v;
      i = 0;

      double maxLength = 0;
      for (i = 0; i < y1Axis.getViews().size(); i++) {
        v = y1Axis.getViews().get(i);
        if (v.isLabelVisible()) {
          bounds = labelFont.getStringBounds(v.getExtendedName() + " " + y1Axis.getAxeName(), frc);
          if (bounds.getWidth() > maxLength)
            maxLength = bounds.getWidth();
        }
      }
      for (i = 0; i < y2Axis.getViews().size(); i++) {
        v = y2Axis.getViews().get(i);
        if (v.isLabelVisible()) {
          bounds = labelFont.getStringBounds(v.getExtendedName() + " " + y2Axis.getAxeName(), frc);
          if (bounds.getWidth() > maxLength)
            maxLength = bounds.getWidth();
        }
      }

      labelHeight = (int) bounds.getHeight() + 2;
      labelTHeight = (labelHeight * nbLabel) + 10;
      labelWidth = (int) (maxLength + 55); // sample line width & margin

      switch( labelMode ) {
        case LABEL_UP:
          labelR.setBounds(MX ,MY + headerR.height ,w-2*MX ,labelTHeight);
          break;
        case LABEL_DOWN:
          labelR.setBounds(MX ,h-MY-labelTHeight, w-2*MX, labelTHeight);
          break;
        case LABEL_RIGHT:
          labelR.setBounds(w-MX-labelWidth, MY + headerR.height, labelWidth, h-2*MY-headerR.height);
          break;
        case LABEL_LEFT:
          labelR.setBounds(MX, MY + headerR.height, labelWidth, h - 2 * MY - headerR.height);
          break;
        case LABEL_ROW:
          labelPerLine = w/labelWidth;
          if(labelPerLine>nbLabel) labelPerLine = nbLabel;
          if(labelPerLine<=1) {
            // Revert to classic LABEL_DOWN
            labelR.setBounds(MX ,h-MY-labelTHeight, w-2*MX, labelTHeight);
          } else {
            labelTHeight = labelHeight*(nbLabel/labelPerLine);
            if(nbLabel%labelPerLine!=0) labelTHeight += labelHeight;
            labelR.setBounds(MX ,h-MY-labelTHeight, w-2*MX, labelTHeight);
          }
          break;
      }

    }

    // Measure view Rectangle --------------------------------------------
    switch (labelMode) {
      case LABEL_UP:
        viewR.setBounds(MX, MY + headerR.height + labelR.height , w - 2 * MX, h - 2*MY - headerR.height - labelR.height);
        break;
      case LABEL_DOWN:
      case LABEL_ROW:
        viewR.setBounds(MX, MY + headerR.height , w - 2 * MX, h - 2 * MY - headerR.height - labelR.height);
        break;
      case LABEL_RIGHT:
        viewR.setBounds(MX, MY + headerR.height , w - 2 * MX - labelR.width , h - 2 * MY - headerR.height);
        break;
      case LABEL_LEFT:
        viewR.setBounds(MX + labelR.width, MY + headerR.height, w - 2 * MX - labelR.width, h - 2 * MY - headerR.height);
        break;
    }

    // Measure Axis ------------------------------------------------------
    xAxisThickness =  xAxis.getLabelFontDimension(frc);
    if(xAxis.getOrientation()==JLAxis.HORIZONTAL_UP) {
      xAxisUpMargin = xAxisThickness/2;
    } else {
      xAxisUpMargin = 0;
    }

    axisHeight = viewR.height - xAxisThickness;

    xAxis.computeXScale(views);
    y1Axis.measureAxis(frc, 0, axisHeight);
    y2Axis.measureAxis(frc, 0, axisHeight);
    y1AxisThickness = y1Axis.getThickness();
    if(y1AxisThickness==0) y1AxisThickness = 5;
    y2AxisThickness = y2Axis.getThickness();
    if(y2AxisThickness==0) y2AxisThickness = 5;

    axisWidth = viewR.width - (y1AxisThickness+y2AxisThickness);

    xAxis.measureAxis(frc, axisWidth, 0);

 }

  // Paint the zoom mode label
  private void paintZoomButton(int x,int y) {

    if( isZoomed() ) {
      int w = zoomButton.getPreferredSize().width;
      int h = zoomButton.getPreferredSize().height;
      zoomButton.setBounds(x+7,y+5,w,h);
      zoomButton.setVisible(w<(axisWidth-7) && h<(axisHeight-5));
    } else {
      zoomButton.setVisible(false);
    }

  }

  // Paint the zoom rectangle
  private void paintZoomSelection(Graphics g) {

    if (zoomDrag) {
      g.setColor(Color.black);
      // Draw rectangle
      Rectangle r = buildRect(zoomX, zoomY, lastX, lastY);
      g.drawRect(r.x, r.y, r.width, r.height);
    }

  }

  /**
   * Paint the components. Use the repaint method to repaint the graph.
   * @param g Graphics object.
   */
  public void paint(Graphics g) {

    int w = getWidth();
    int h = getHeight();

    // Create a vector containing all views
    Vector<JLDataView> views = new Vector<JLDataView> (y1Axis.getViews());
    views.addAll(y2Axis.getViews());

    // Avoid a partial repaint that can make bad looking fx
    // if some dataViews has changed without a repaint().
//  Rectangle cr = g.getClipBounds();
//  Rectangle mr = new Rectangle(0, 0, w, h);
//  if (!cr.equals(mr)) {
//     // Ask full repaint
//     repaint();
//     return;
//  }

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    FontRenderContext frc = g2.getFontRenderContext();

    g.setPaintMode();

    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, w, h);
    }

    // Compute bounds of label and graph
    measureGraphItems(g2,frc,w,h,views);

    // Draw label and header
    paintLabelAndHeader(g2);

    // Paint chart background
    int xOrg = viewR.x + y1AxisThickness;
    int yOrg = viewR.y + axisHeight + xAxisUpMargin;

    int xOrgY1 = viewR.x;
    int xOrgY2 = viewR.x + y1AxisThickness + axisWidth;
    int yOrgY  = viewR.y + xAxisUpMargin;

    if (!chartBackground.equals(getBackground()) && axisWidth > 0 && axisHeight > 0) {
      g.setColor(chartBackground);
      g.fillRect(xOrg, yOrg - axisHeight, axisWidth, axisHeight);
    }

    // Paint zoom stuff
    paintZoomSelection(g);
    paintZoomButton(xOrg,yOrgY);

    if (paintAxisFirst) {

      //Draw axes
      if(views.size()==0) {
        // No view, paint only Y1
        y1Axis.paintAxis(g, frc, xOrgY1, yOrgY, xAxis, xOrg, yOrg, getBackground(),true,true);
      } else {
        y1Axis.paintAxis(g, frc, xOrgY1, yOrgY, xAxis, xOrg, yOrg, getBackground(),!y2Axis.isVisible() || y2Axis.getViewNumber()==0,false);
        y2Axis.paintAxis(g, frc, xOrgY2, yOrgY, xAxis, xOrg, yOrg, getBackground(),!y1Axis.isVisible() || y1Axis.getViewNumber()==0,false);
      }
      if( xAxis.getPosition()==JLAxis.HORIZONTAL_ORG2)
        xAxis.paintAxis(g, frc, xOrg, yOrg, y2Axis, 0, 0, getBackground(),true,false);
      else
        xAxis.paintAxis(g, frc, xOrg, yOrg, y1Axis, 0, 0, getBackground(),true,false);

      //Draw data
      Rectangle clipRect = g.getClipBounds();
      y1Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      y2Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      if (clipRect != null) {
        g.setClip(clipRect.x,clipRect.y, clipRect.width, clipRect.height);
      } else {
        g.setClip(null);
      }

    } else {

      //Draw data
      Rectangle clipRect = g.getClipBounds();
      y1Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      y2Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      if (clipRect != null) {
        g.setClip(clipRect.x,clipRect.y, clipRect.width, clipRect.height);
      } else {
        g.setClip(null);
      }

      //Draw axes
      if(views.size()==0) {
        // No view, paint only Y1
        y1Axis.paintAxis(g, frc, xOrgY1, yOrgY, xAxis, xOrg, yOrg, getBackground(),true,true);
      } else {
        y1Axis.paintAxis(g, frc, xOrgY1, yOrgY, xAxis, xOrg, yOrg, getBackground(),!y2Axis.isVisible() || y2Axis.getViewNumber()==0,false);
        y2Axis.paintAxis(g, frc, xOrgY2, yOrgY, xAxis, xOrg, yOrg, getBackground(),!y1Axis.isVisible() || y1Axis.getViewNumber()==0,false);
      }
      if (xAxis.getPosition() == JLAxis.HORIZONTAL_ORG2)
        xAxis.paintAxis(g, frc, xOrg, yOrg, y2Axis, 0, 0, getBackground(),true,false);
      else
        xAxis.paintAxis(g, frc, xOrg, yOrg, y1Axis, 0, 0, getBackground(),true,false);

    }

    redrawPanel(g);

    // Paint swing stuff
    paintComponents(g);
    paintBorder(g);
  }

  // Build a valid rectangle with the given coordinates
  private Rectangle buildRect(int x1, int y1, int x2, int y2) {

    Rectangle r = new Rectangle();

    if (x1 < x2) {
      if (y1 < y2) {
        r.setRect(x1, y1, x2 - x1, y2 - y1);
      } else {
        r.setRect(x1, y2, x2 - x1, y1 - y2);
      }
    } else {
      if (y1 < y2) {
        r.setRect(x2, y1, x1 - x2, y2 - y1);
      } else {
        r.setRect(x2, y2, x1 - x2, y1 - y2);
      }
    }

    return r;
  }

  // ************************************************************************
  // Mouse Listener

  public void mouseWheelMoved(MouseWheelEvent evt) {

    int nbStep = evt.getWheelRotation();
    if( !zoomDrag ) {

      double ratio = Math.pow(0.9,(double)-nbStep);
      xAxis.zoom(ratio);
      if(! (evt.isControlDown() || evt.isShiftDown()) ) {
        y1Axis.zoom(ratio);
        y2Axis.zoom(ratio);
      }

      if( zoomDragAllowed ) {
        zoomDragAllowed = false;
        setCursor(Cursor.getDefaultCursor());
      }

      repaint();

    }

  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {

    if (zoomDrag) {

      // Clear old rectangle
      Rectangle r = buildRect(zoomX, zoomY, lastX, lastY);
      r.width+=1;
      r.height+=1;
      repaint(r);

      // Draw new one
      lastX = e.getX();
      lastY = e.getY();
      r = buildRect(zoomX, zoomY, lastX, lastY);
      r.width+=1;
      r.height+=1;
      repaint(r);

    }

    if( translateDragStart ) {

      int tx = e.getX() - lastX;
      int ty = e.getY() - lastY;
      int dist = tx*tx + ty*ty;
      if( dist>100 ) {
        // We have move a bit, start translation
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        translateDrag = true;
        translateDragStart = false;
      }

    }

    if( translateDrag ) {

      int tx = e.getX() - lastX;
      int ty = e.getY() - lastY;
      xAxis.translate(-tx);
      y1Axis.translate(ty);
      y2Axis.translate(ty);
      lastX = e.getX();
      lastY = e.getY();
      repaint();

    }


  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {

    if (zoomDrag) {

      Rectangle r = buildRect(zoomX, zoomY, e.getX(), e.getY());
      zoomDrag = false;

      if( xAxis.canApplyTimeSpan(r.x, r.x + r.width) ) {
        Rectangle br = xAxis.getBoundRect();
        double ratio = (double)r.width/(double)br.width;
        if(ratio<1.0) {
          if(Double.isNaN(oldDisplayDuration)) oldDisplayDuration = displayDuration;
          double newDuration = ratio * (xAxis.getMax()-xAxis.getMin());
          setDisplayDuration(newDuration);
        }
      } else {
        xAxis.zoom(r.x, r.x + r.width);
      }
      y1Axis.zoom(r.y, r.y + r.height);
      y2Axis.zoom(r.y, r.y + r.height);
      if( zoomDragAllowed ) {
        zoomDragAllowed = false;
        setCursor(Cursor.getDefaultCursor());
      }

    }

    if( translateDrag ) {
      translateDrag = false;
      setCursor(Cursor.getDefaultCursor());
    }

    translateDragStart = false;
    ipanelVisible = false;
    repaint();
  }

  public void mousePressed(MouseEvent e) {

    // Left button click
    if (e.getButton() == MouseEvent.BUTTON1) {

      lastX = e.getX();
      lastY = e.getY();

      // Zoom management
      if (e.isControlDown() || zoomDragAllowed) {
        zoomDrag = true;
        zoomX = e.getX();
        zoomY = e.getY();
        return;
      }

      translateDragStart = true;

      SearchInfo si;
      SearchInfo msi = null;

      // Look for the nearest value on each dataView
      msi = y1Axis.searchNearest(e.getX(), e.getY(), xAxis);
      si = y2Axis.searchNearest(e.getX(), e.getY(), xAxis);
      if (si.found && si.dist < msi.dist) msi = si;

      if (msi.found) {
        Graphics g = getGraphics();
        if(g!=null) {
          showPanel(g, msi, e);
          g.dispose();
        }
        return;
      }

      // Click on label
      int i = 0;
      boolean found = false;
      while (i < labelRect.size() && !found) {
        LabelRect r = labelRect.get(i);
        found = r.rect.contains(e.getX(), e.getY());
        if (found) {
          //Display the Dataview options
          showDataOptionDialog(r.view);
        }
        i++;
      }

    }

    // Right button click
    if (e.getButton() == MouseEvent.BUTTON3) {
      int i;

        zoomBackMenuItem.setEnabled(isZoomed());
        // Gets user action state
        for (i = 0; i < userActionMenuItem.length; i++) {
          if (userActionMenuItem[i] instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem b = (JCheckBoxMenuItem) userActionMenuItem[i];
            b.setSelected(fireGetActionState(b.getText()));
          }
        }

        // Add dataView table item
        tableMenu.removeAll();
        tableMenu.add(tableAllMenuItem);

        // --------
        if (y1Axis.getViewNumber() > 0) tableMenu.add(new JSeparator());
        for (i = 0; i < tableSingleY1MenuItem.length; i++)
          tableSingleY1MenuItem[i].removeActionListener(this);
        tableSingleY1MenuItem = new JMenuItem[y1Axis.getViewNumber()];
        for (i = 0; i < y1Axis.getViewNumber(); i++) {
          tableSingleY1MenuItem[i] = new JMenuItem(y1Axis.getDataView(i).getName());
          tableSingleY1MenuItem[i].addActionListener(this);
          tableMenu.add(tableSingleY1MenuItem[i]);
        }

        // --------
        if (y1Axis.getViewNumber() > 0 && y2Axis.getViewNumber() > 0) tableMenu.add(new JSeparator());
        for (i = 0; i < tableSingleY2MenuItem.length; i++)
          tableSingleY2MenuItem[i].removeActionListener(this);
        tableSingleY2MenuItem = new JMenuItem[y2Axis.getViewNumber()];
        for (i = 0; i < y2Axis.getViewNumber(); i++) {
          tableSingleY2MenuItem[i] = new JMenuItem(y2Axis.getDataView(i).getName());
          tableSingleY2MenuItem[i].addActionListener(this);
          tableMenu.add(tableSingleY2MenuItem[i]);
        }

        // Add dataView option menu
        dvMenu.removeAll();
        for (i = 0; i < dvY1MenuItem.length; i++)
          dvY1MenuItem[i].removeActionListener(this);
        for (i = 0; i < dvY2MenuItem.length; i++)
          dvY2MenuItem[i].removeActionListener(this);

        dvY1MenuItem = new JMenuItem[y1Axis.getViewNumber()];
        dvY2MenuItem = new JMenuItem[y2Axis.getViewNumber()];

        int unamedDv = 1;

        for(i = 0; i<y1Axis.getViewNumber(); i++ ) {
          String dvName = y1Axis.getDataView(i).getName();
          if(dvName.length()==0) { dvName = "Dataview #" + unamedDv;unamedDv++; }
          dvY1MenuItem[i] = new JMenuItem(dvName);
          dvY1MenuItem[i].addActionListener(this);
          dvMenu.add(dvY1MenuItem[i]);
        }
        for(i = 0; i<y2Axis.getViewNumber(); i++ ) {
          String dvName = y2Axis.getDataView(i).getName();
          if(dvName.length()==0) { dvName = "Dataview #" + unamedDv;unamedDv++; }
          dvY2MenuItem[i] = new JMenuItem(dvName);
          dvY2MenuItem[i].addActionListener(this);
          dvMenu.add(dvY2MenuItem[i]);
        }

        // DataView Statistics item
        statMenu.removeAll();
        statMenu.add(statAllMenuItem);

        // --------
        if (y1Axis.getViewNumber() > 0) statMenu.add(new JSeparator());
        for (i = 0; i < statSingleY1MenuItem.length; i++)
          statSingleY1MenuItem[i].removeActionListener(this);
        statSingleY1MenuItem = new JMenuItem[y1Axis.getViewNumber()];
        for (i = 0; i < y1Axis.getViewNumber(); i++) {
          statSingleY1MenuItem[i] = new JMenuItem(y1Axis.getDataView(i).getName());
          statSingleY1MenuItem[i].addActionListener(this);
          statMenu.add(statSingleY1MenuItem[i]);
        }

        // --------
        if (y1Axis.getViewNumber() > 0 && y2Axis.getViewNumber() > 0) statMenu.add(new JSeparator());
        for (i = 0; i < statSingleY2MenuItem.length; i++)
          statSingleY2MenuItem[i].removeActionListener(this);
        statSingleY2MenuItem = new JMenuItem[y2Axis.getViewNumber()];
        for (i = 0; i < y2Axis.getViewNumber(); i++) {
          statSingleY2MenuItem[i] = new JMenuItem(y2Axis.getDataView(i).getName());
          statSingleY2MenuItem[i].addActionListener(this);
          statMenu.add(statSingleY2MenuItem[i]);
        }

        chartMenu.show(this, e.getX(), e.getY());
      }

    }

  //****************************************
  // redraw the panel
  private void redrawPanel(Graphics g) {

    if (!ipanelVisible) return;

    // Udpate serachInfo
    Point p;
    JLDataView vy = lastSearch.dataView;
    JLDataView vx = lastSearch.xdataView;
    DataList dy = lastSearch.value;
    DataList dx = lastSearch.xvalue;
    JLAxis yaxis = lastSearch.axis;

    if (xAxis.isXY()) {
      p = yaxis.transform(vx.getTransformedValue(dx.y),
        vy.getTransformedValue(dy.y),
        xAxis);
    } else {
      p = yaxis.transform(dy.x,
        vy.getTransformedValue(dy.y),
        xAxis);
    }

    lastSearch.x = p.x;
    lastSearch.y = p.y;

    showPanel(g, lastSearch);
  }

  public String[] buildPanelString(SearchInfo si) {

    String[] str = null;

    String xValue;
    if (xAxis.isXY()) {
      str = new String[4];
      str[0] = si.dataView.getExtendedName() + " " + si.axis.getAxeName();
      str[1] = JLAxis.formatTimeValue(si.value.x);
      str[2] = "X= " + si.xdataView.formatValue(si.xdataView.getTransformedValue(si.xvalue.y));
      str[3] = "Y= " + si.dataView.formatValue(si.dataView.getTransformedValue(si.value.y)) + " " + si.dataView.getUnit();
    } else {
      if (xAxis.getAnnotation() == JLAxis.TIME_ANNO) {
        xValue = "Time= " + JLAxis.formatTimeValue(si.value.x);
      } else {
        xValue = "Index= " + si.value.x;
      }
      str = new String[3];
      str[0] = si.dataView.getExtendedName() + " " + si.axis.getAxeName();
      str[1] = xValue;
      str[2] = "Y= " + si.dataView.formatValue(si.dataView.getTransformedValue(si.value.y)) + " " + si.dataView.getUnit();
    }

    return str;

  }

  /**
   * Display the value tooltip.
   * @param g Graphics object
   * @param si SearchInfo structure.
   * @see JLAxis#searchNearest
   */
  public void showPanel(Graphics g, SearchInfo si) {
    showPanel(g,si,null);
  }

  public void showPanel(Graphics g, SearchInfo si,MouseEvent scrEvent) {

    Graphics2D g2 = (Graphics2D) g;
    Rectangle2D bounds;
    int maxh = 0;
    int h = 0;
    int maxw = 0;
    int x0 = 0,y0 = 0;
    String[] str = null;


    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
      RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    FontRenderContext frc = g2.getFontRenderContext();

    g.setPaintMode();
    g.setFont(labelFont);

    if (listener != null) {

      // Call user listener
      JLChartEvent w = new JLChartEvent(this, si, scrEvent);
      str = listener.clickOnChart(w);

    }

    // Default behavior
    if (str == null) str = buildPanelString(si);

    // Do not show panel if no text
    if (str.length <= 0) return;

    // Compute panel size

    bounds = g.getFont().getStringBounds(str[0], frc);
    maxw = (int) bounds.getWidth();
    h = maxh = (int) bounds.getHeight();

    for (int i = 1; i < str.length; i++) {
      bounds = g.getFont().getStringBounds(str[i], frc);
      if ((int) bounds.getWidth() > maxw) maxw = (int) bounds.getWidth();
      maxh += bounds.getHeight();
    }

    maxw += 10;
    maxh += 10;

    g.setColor(Color.black);

    switch (si.placement) {
      case SearchInfo.BOTTOMRIGHT:
        x0 = si.x + 10;
        y0 = si.y + 10;
        g.drawLine(si.x, si.y, si.x + 10, si.y + 10);
        break;
      case SearchInfo.BOTTOMLEFT:
        x0 = si.x - 10 - maxw;
        y0 = si.y + 10;
        g.drawLine(si.x, si.y, si.x - 10, si.y + 10);
        break;
      case SearchInfo.TOPRIGHT:
        x0 = si.x + 10;
        y0 = si.y - 10 - maxh;
        g.drawLine(si.x, si.y, si.x + 10, si.y - 10);
        break;
      case SearchInfo.TOPLEFT:
        x0 = si.x - 10 - maxw;
        y0 = si.y - 10 - maxh;
        g.drawLine(si.x, si.y, si.x - 10, si.y - 10);
        break;
    }

    // Draw panel
    g.setColor(Color.white);
    g.fillRect(x0, y0, maxw, maxh);
    g.setColor(Color.black);
    g.drawRect(x0, y0, maxw, maxh);

    //Draw info
    g.setColor(Color.black);
    for (int i = 0; i < str.length; i++) {
      g.drawString(str[i], x0 + 3, y0 + 3 + (i + 1) * h);
    }

    lastSearch = si;
    ipanelVisible = true;

  }

  //**************************************************
  //

  /**
   * Remove points that exceed displayDuration.
   * @param v DataView containing points
   * @return Number of deleted points
   */
  public int garbageData(JLDataView v) {

    int nb = 0;

    if( !Double.isNaN(oldDisplayDuration) ) {
      if(oldDisplayDuration != Double.POSITIVE_INFINITY)
        nb = v.garbagePointTime(oldDisplayDuration);
    } else if (displayDuration != Double.POSITIVE_INFINITY) {
      nb = v.garbagePointTime(displayDuration);
    }

    return nb;

  }

  /**
   * Add data to dataview , perform fast update when possible and garbage old data
   * (if a display duration is specified).
   * @param v The dataview
   * @param x x coordinates (real space)
   * @param y y coordinates (real space)
   * @see JLChart#setDisplayDuration
   */
  public void addData(JLDataView v, double x, double y) {

    DataList lv = null;
    boolean need_repaint = false;

    //Get the last value
    if (v.getDataLength() > 0) lv = v.getLastValue();

    if( lv!=null ) {
      if( Double.isNaN(lv.y) ) {
        lv = null;
      }
    }

    //Add data
    v.add(x, y);

    // Does not repaint if zoom drag
    if (zoomDrag) return;

    // Does not paint if not visible
    if(!isVisible()) return;

    if (xAxis.isXY()) {
      // Perform full update in XY
      repaint();
      return;
    }

    // Compute update
    JLAxis yaxis = v.getAxis();

    if (yaxis != null) {

      Point lp = null;
      Point p = yaxis.transform(x, v.getTransformedValue(y), xAxis);
      if (lv != null) lp = yaxis.transform(lv.x, v.getTransformedValue(lv.y), xAxis);

      if (yaxis.getBoundRect().contains(p) && !need_repaint) {
        // We can perform fast update
        Graphics g = getGraphics();
        if(g!=null) {
          yaxis.drawFast(g, lp, p, v);
          g.dispose();
        }
      } else {
        // Full update needed
        garbageData(v);
        repaint();
      }

    }

  }

  /**
   * Sets the allowed margin to make a projection on a line on data show.
   * @param milliseconds the margin, in milliseconds
   */
  public void setTimePrecision(int milliseconds) {
      timePrecision = milliseconds;
  }
  
  /**
   * Returns the allowed margin to make a projection on a line on data show (default: 0).
   * @return The allowed margin to make a projection on a line on data show (default: 0).
   */
  public int getTimePrecision() {
      return timePrecision;
  }

  /**
   * Used with saveDataFile(). Returns the String used to represent "no data" (default : "").
   * @return The String used to represent "no data"
   */
  public String getNoValueString () {
      return noValueString;
  }

  /**
   * Used with saveDataFile(). Sets the String used to represent "no data" (default : "").
   * @param noValueString The String used to represent "no data"
   */
  public void setNoValueString (String noValueString) {
      this.noValueString = noValueString;
  }

  /**
   * Sets if you prefer to use a JDialog with showTableXXX() methods instead of the classic JFrame
   * @param preferDialog Prefer to have a dialog or not
   * @param modal The dialog should be modal or not
   */
  public void setPreferDialogForTable(boolean preferDialog, boolean modal)
  {
      this.preferDialog = preferDialog;
      this.modalDialog = modal;
  }

  /**
   * Sets the parent that the "show table" dialog should have (in case you chose to have a dialog)
   * @param parent The parent
   * @see #setPreferDialogForTable(boolean, boolean)
   */
  public void setParentForTableDialog(Frame parent)
  {
      this.dialogParent = parent;
  }

  /**
   * Sets the parent that the "show table" dialog should have (in case you chose to have a dialog)
   * @param parent The parent
   * @see #setPreferDialogForTable(boolean, boolean)
   */
  public void setParentForTableDialog(Dialog parent)
  {
      this.dialogParent = parent;
  }

  public void removeDataView(JLDataView view) {
    if (view != null) {
      JLAxis axis = view.getAxis();
      if (axis != null) {
        axis.removeDataView(view);
      }
    }
  }

  public void reset () {
    reset(true);
  }

  public void reset(boolean showConfirmDialog) {

    Vector<JLDataView> existingViews = new Vector<JLDataView>();
    if (xAxis.isXY()) existingViews.addAll(xAxis.getViews());
    existingViews.addAll(y1Axis.getViews());
    existingViews.addAll(y2Axis.getViews());
    if (existingViews.size() != 0 && showConfirmDialog) {
      String warning = "Reseting chart will remove all the existing dataviews.\n"
                     + "Your component may not work any more.\n"
                     + "Are you sure to reset chart ?";
      int choice = JOptionPane.showConfirmDialog(
              this,
              warning,
              "Risk of breaking component",
              JOptionPane.WARNING_MESSAGE
      );
      if (choice != JOptionPane.OK_OPTION) {
        existingViews.clear();
        existingViews = null;
        return;
      }
    }
    maxDisplayDuration = Double.POSITIVE_INFINITY;
    displayDuration = Double.POSITIVE_INFINITY;

    for (int i = 0; i < existingViews.size(); i++) {
      removeDataView( existingViews.get(i) );
    }

    getY1Axis().setLabels(null, null);
    getY1Axis().setScale(JLAxis.LINEAR_SCALE);
    getY1Axis().setAutoScale(true);

    getY2Axis().setLabels(null, null);
    getY2Axis().setScale(JLAxis.LINEAR_SCALE);
    getY2Axis().setAutoScale(true);

    getXAxis().setLabels(null, null);
    getXAxis().setScale(JLAxis.LINEAR_SCALE);
    getXAxis().setAutoScale(true);

    existingViews.clear();
    existingViews = null;
  }
  
  protected JMenuItem getUserActionMenuItem(String actionName)
  {
      int i;
      int correspondingIndex = -1;

      for (i = 0; i < userAction.length; i++)
      {
          if ( userAction[i].equals(actionName) )
          {
              correspondingIndex = i;
              break;
          }
      }
        
      if (correspondingIndex == -1) return null;
      return userActionMenuItem[correspondingIndex];
  }

  private void readFile() {

    int ok = JOptionPane.YES_OPTION;
    JFileChooser chooser = new JFileChooser(lastDataFileLocation);
    chooser.setDialogTitle("Load Graph Data (Text file with TAB separated fields)");
    int returnVal = chooser.showOpenDialog(this);

    if (returnVal == JFileChooser.APPROVE_OPTION) {

      loadDataFile(chooser.getSelectedFile().getAbsolutePath());

    }

  }


  //****************************************
  // Debug stuff

  static double[] ranges = { 0.1,0.15 , 0.35,0.4 , 0.5,0.57 , 0.8,0.9 };
  public static boolean isInRange(double x,int NB) {

    int nbRange = ranges.length / 2;
    double xR = x / (double)NB;
    for(int i=0;i<nbRange;i++) {
      double sx = ranges[2*i];
      double ex = ranges[2*i+1];
      if(xR>=sx && xR<=ex)
        return false;
    }

    return true;

  }

  public static void main(String args[]) {

    final JFrame f = new JFrame();
    final JLChart chart = new JLChart();
    final JLDataView[] v;

    Locale.setDefault(Locale.US);


    // Initialise axis properties
    chart.getXAxis().setName("X");
    chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
    chart.getXAxis().setPercentScrollback(0.0);
    chart.getXAxis().setAutoScale(true);

    chart.getY1Axis().setName("Y");
    chart.getY1Axis().setAutoScale(true);

    Font aFont = new Font("Dialog",Font.PLAIN,16);
    chart.getXAxis().setFont(aFont);
    chart.getY1Axis().setFont(aFont);

    chart.setLabelVisible(false);

    // ---------------------------------------------
      /*

    int NB_PTS = 100;
    int NB_CURVE = 5;
    Random rng = new Random(12345678);

    v = new JLDataView[NB_CURVE];

    for(int j=0;j<NB_CURVE;j++) {
      v[j] = new JLDataView();
      v[j].setName("DV #" + (j+1));
      v[j].setMarker(JLDataView.MARKER_NONE);
      for(int i=0;i<NB_PTS;i++) {
        double x = (double)i;
        if(isInRange(x,NB_PTS))
          v[j].add((x+5000)/10.0,rng.nextDouble()*10.0-5.0);
      }
      chart.getY1Axis().addDataView(v[j]);
    }
    */

    JLDataView b = new JLDataView();
    b.setBarWidth(5);
    b.setViewType(JLDataView.TYPE_BAR);
    b.add(1,1);
    b.add(2,2);
    b.add(3,3);
    b.add(4,4);

    b.setBarFillColorAt(2,Color.GREEN);
    b.setFillStyle(JLDataView.FILL_STYLE_SOLID);

    chart.getY1Axis().addDataView(b);

    // -----------------------------------------------------------------

    JPanel bot = new JPanel();
    bot.setLayout(new FlowLayout());

    JButton btn = new JButton("Exit");
    btn.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        System.exit(0);
      }
    });

    bot.add(btn);

    JButton c = new JButton("Options");
    c.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        chart.showOptionDialog();
      }
    });

    bot.add(c);

    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(chart, BorderLayout.CENTER);
    f.getContentPane().add(bot, BorderLayout.SOUTH);
    f.setSize(400, 300);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);

  }

}
