//
// JLChart.java
// Description: A Class to handle 2D graphics plot.
//
// JL Pons (c)ESRF 2002


package fr.esrf.tangoatk.widget.util.chart;



import java.awt.event.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

class LabelRect {
  Rectangle rect;
  JLDataView view;

  LabelRect(int x, int y, int w, int h, JLDataView v) {
    rect = new Rectangle(x, y, w, h);
    view = v;
  }
}

/**
 * A Class to handle 2D graphics plot.
 * @author JL Pons
 */

public class JLChart extends JComponent implements MouseListener, MouseMotionListener,ActionListener {

  // Global graph options
  private String header = null;
  private boolean headerVisible = false;
  private Font headerFont;
  private Color headerColor;

  private boolean labelVisible = true;
  private Font labelFont;
  private Vector labelRect;

  private boolean ipanelVisible = false;
  private boolean paintAxisFirst = true;
  private Color  chartBackground;

  private double displayDuration;

  private JPopupMenu chartMenu;
  private JMenuItem  infoMenuItem;
  private JMenuItem  optionMenuItem;
  private JMenuItem  zoomBackMenuItem;
  private JSeparator sepMenuItem;

  private JMenuItem[] userActionMenuItem;
  private String[]    userAction;

  private boolean zoomDrag;
  private boolean zoomDragAllowed;
  private int zoomX;
  private int zoomY;

  private int lastX;
  private int lastY;
  private SearchInfo lastSearch;

  // Axis
  private JLAxis xAxis;
  private JLAxis y1Axis;
  private JLAxis y2Axis;

  // Listeners
  private IJLChartListener  listener;  // JLChart listener
  private EventListenerList actionlistenerList;  // list of WheelSwitch listeners


  /**
   * Graph constructor.
   */
  public JLChart() {

    setBackground(new Color(180, 180, 180));
    setForeground(Color.black);
    setOpaque(true);
    setFont(new Font("Dialog", Font.PLAIN, 12));
    headerFont = getFont();
    headerColor = getForeground();
    labelFont = getFont();

    xAxis = new JLAxis(this, JLAxis.HORIZONTAL);
    xAxis.setAnnotation(JLAxis.TIME_ANNO);
    xAxis.setAutoScale(true);
    xAxis.setAxeName("(X)");
    y1Axis = new JLAxis(this, JLAxis.VERTICAL_LEFT);
    y1Axis.setAxeName("(Y1)");
    y2Axis = new JLAxis(this, JLAxis.VERTICAL_RIGHT);
    y2Axis.setAxeName("(Y2)");
    displayDuration = Double.POSITIVE_INFINITY;

    labelRect = new Vector();
    zoomDrag = false;
    zoomDragAllowed = false;

    chartMenu = new JPopupMenu();


    optionMenuItem = new JMenuItem("Show options");
    optionMenuItem.addActionListener(this);

    zoomBackMenuItem = new JMenuItem("Zoom back");
    zoomBackMenuItem.addActionListener(this);

    /*
    infoMenuItem = new JMenuItem("Chart menu");
    infoMenuItem.setEnabled(false);
    chartMenu.add(infoMenuItem);
    chartMenu.add(new JSeparator());
    */

    chartMenu.add(optionMenuItem);
    chartMenu.add(zoomBackMenuItem);

    sepMenuItem = new JSeparator();
    chartMenu.add(sepMenuItem);

    userActionMenuItem=new JMenuItem[0];
    userAction=new String[0];
    sepMenuItem.setVisible(false);

    //Set up listeners
    addMouseListener(this);
    addMouseMotionListener(this);

    listener = null;
    listenerList = new EventListenerList();

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

  public void setBackground(Color c) {
    super.setBackground(c);
    setChartBackground(c);
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
    paintAxisFirst = true;
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
   * Sets the display duration.This will garbage old data in all displayed data views.
   * Garbaging occurs when addData is called.
   * @param v Displauy duration (millisec). Pass Double.POSITIVE_INFINITY to disable.
   * @see JLChart#addData
   */
  public void setDisplayDuration(double v) {
    displayDuration = v;
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
   * Sets the header color
   * @param c Header color
   */
  public void setHeaderColor(Color c) {
    headerColor = c;
    setHeaderVisible(true);
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
   * Sets the label font
   * @param f
   */
  public void setLabelFont(Font f) {
    labelFont = f;
  }

  /**
   * Display the global graph option dialog.
   */
  public void showOptionDialog() {

    Object dlgParent = getRootPane().getParent();
    JLChartOption optionDlg;

    if(dlgParent instanceof JDialog) {
       optionDlg=new JLChartOption((JDialog)dlgParent,this);
    } else if (dlgParent instanceof JFrame) {
       optionDlg=new JLChartOption((JFrame)dlgParent,this);
    } else {
       optionDlg=new JLChartOption((JFrame)null,this);
    }

    optionDlg.setVisible(true);

  }

  /**
   * Display the data view option dialog.
   */
  public void showDataOptionDialog(JLDataView v) {

    Object dlgParent = getRootPane().getParent();
    JLDataViewOption optionDlg;

    if(dlgParent instanceof JDialog) {
       optionDlg=new JLDataViewOption((JDialog)dlgParent,this,v);
    } else if (dlgParent instanceof JFrame) {
       optionDlg=new JLDataViewOption((JFrame)dlgParent,this,v);
    } else {
       optionDlg=new JLDataViewOption((JFrame)null,this,v);
    }

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

    String[] action=new String[userAction.length+1];
    for(i=0;i<userAction.length;i++) action[i] = userAction[i];
    action[i] = name;

    // Build the menu
    for(i=0;i<userActionMenuItem.length;i++) {
      chartMenu.remove(userActionMenuItem[i]);
      userActionMenuItem[i].removeActionListener(this);
      userActionMenuItem[i]=null;
    }

    JMenuItem[] actionMenu = new JMenuItem[action.length];
    for(i=0;i<action.length;i++) {
      if( action[i].startsWith("chk") ) {
        actionMenu[i] = new JCheckBoxMenuItem(action[i].substring(3));
      } else {
        actionMenu[i] = new JMenuItem(action[i]);
      }
      actionMenu[i].addActionListener(this);
      chartMenu.add(actionMenu[i]);
    }

    userActionMenuItem = actionMenu;
    userAction         = action;
    sepMenuItem.setVisible(true);

  }

  /**
   * Add the specified JLChartAction listener to the list
   * @param l Listener to add
   */
  public void addJLChartActionListener(IJLChartActionListener l) {
    listenerList.add(IJLChartActionListener.class, (EventListener) l);
  }

  //
  /**
   * Remove the specified JLChartAction Listener
   * @param l Listener to remove
   */
  public void removeJLChartActionListener(IJLChartActionListener l) {
    listenerList.remove(IJLChartActionListener.class, (EventListener) l);
  }

  // Fire JLChartActionEvent to all registered IJLChartActionListener
  private void fireActionPerfromed(String name,boolean state) {
    IJLChartActionListener[] list = (IJLChartActionListener[]) (listenerList.getListeners(IJLChartActionListener.class));
    JLChartActionEvent w = new JLChartActionEvent(this, name,state);
    for (int i = 0; i < list.length; i++) list[i].actionPerformed(w);
  }

  // Fire JLChartActionEvent to all registered IJLChartActionListener
  private boolean fireGetActionState(String name) {
    IJLChartActionListener[] list = (IJLChartActionListener[]) (listenerList.getListeners(IJLChartActionListener.class));
    JLChartActionEvent w = new JLChartActionEvent(this, name);
    boolean ret=true;
    for (int i = 0; i < list.length; i++)
      ret = list[i].getActionState(w) && ret;

    return ret;
  }

  /**
   * Exit zoom mode.
   */
  public void exitZoom() {
    xAxis.unzoom();
    y1Axis.unzoom();
    y2Axis.unzoom();
    zoomDragAllowed = false;
    setCursor(Cursor.getDefaultCursor());
    repaint();
  }

  // -----------------------------------------------------
  // Action listener
  // -----------------------------------------------------
  public void actionPerformed(ActionEvent evt) {

    if( evt.getSource()==optionMenuItem) {
      showOptionDialog();
    } else if (evt.getSource()==zoomBackMenuItem) {
      exitZoom();
    } else {

      // Search in user action
      boolean found=false;
      int i=0;
      while(i<userActionMenuItem.length && !found) {
        found = userActionMenuItem[i] == evt.getSource();
        if(!found) i++;
      }

      if( found ) {
        if( userActionMenuItem[i] instanceof JCheckBoxMenuItem ) {
          JCheckBoxMenuItem c = (JCheckBoxMenuItem)userActionMenuItem[i];
          fireActionPerfromed(c.getText(),c.getState());
        } else {
          fireActionPerfromed(userActionMenuItem[i].getText(),false);
        }
      }

    }

  }

  /**
   * Paint the components. Use the repaint method to repaint the graph.
   * @param g Graphics object.
   */
  protected void paintComponent(Graphics g) {

    int w = getWidth();
    int h = getHeight();
    int MX = 10;
    int MY = 10;
    int y,xpos;
    int headerSize = 0;
    int labelSize = 0;

    // Create a vector containing all views
    Vector views = new Vector(y1Axis.getViews());
    views.addAll(y2Axis.getViews());


    // Check clip area
    Rectangle cr = g.getClipBounds();
    Rectangle mr = new Rectangle(0, 0, w, h);
    if (!cr.equals(mr)) {
      // Ask full repaint
      repaint();
      return;
    }


    Rectangle2D bounds = null;

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

    int nbv1 = y1Axis.getViews().size();
    int nbv2 = y2Axis.getViews().size();

    // Draw header
    if (headerVisible && header != null) {
      g.setFont(headerFont);
      bounds = g.getFont().getStringBounds(header, frc);
      y = (int) (MY + bounds.getHeight() * 0.66);
      xpos = (int) ((w - bounds.getWidth()) / 2);
      g.setColor(headerColor);
      g.drawString(header, xpos, y);
      headerSize += (int) (bounds.getHeight());
    }

    // Draw labels
    labelRect.clear();
    if (labelVisible && views.size() > 0) {

      g.setFont(labelFont);
      JLDataView v;
      int a = g.getFontMetrics(labelFont).getAscent();
      int i = 0;

      // Measure labels
      double maxLength = 0;
      for (i = 0; i < nbv1; i++) {
        v = (JLDataView) y1Axis.getViews().get(i);
        bounds = g.getFont().getStringBounds(v.getExtendedName() + " " + y1Axis.getAxeName(), frc);
        if (bounds.getWidth() > maxLength)
          maxLength = bounds.getWidth();
      }
      for (i = 0; i < nbv2; i++) {
        v = (JLDataView) y2Axis.getViews().get(i);
        bounds = g.getFont().getStringBounds(v.getExtendedName() + " " + y2Axis.getAxeName(), frc);
        if (bounds.getWidth() > maxLength)
          maxLength = bounds.getWidth();
      }

      // Center
      xpos = (int) ((w - (maxLength + 44)) / 2);
      int ch = (int) bounds.getHeight() + 2;
      labelSize = (int) (ch * views.size());

      // Draw labels
      for (i = 0; i < nbv1; i++) {
        v = (JLDataView) y1Axis.getViews().get(i);
        g.setColor(v.getColor());
        y = h - labelSize + ch * i + ch / 2;
        JLAxis.drawSampleLine(g, xpos, y - 2, v);
        g.drawString(v.getExtendedName() + " " + y1Axis.getAxeName(), xpos + 44, y + ch - a);
        labelRect.add(new LabelRect(xpos, y - a, (int) maxLength + 44, ch, v));
      }
      for (int j = 0; j < nbv2; j++) {
        v = (JLDataView) y2Axis.getViews().get(j);
        g.setColor(v.getColor());
        y = h - labelSize + ch * (i + j) + ch / 3;
        JLAxis.drawSampleLine(g, xpos, y - 2, v);
        g.drawString(v.getExtendedName() + " " + y2Axis.getAxeName(), xpos + 44, y + ch - a);
        labelRect.add(new LabelRect(xpos, y - a, (int) maxLength + 44, ch, v));
      }

    }


    // Draw Axes
    xAxis.computeXScale(views);

    labelSize += xAxis.getFontHeight(g);

    if (nbv1 > 0)
      headerSize += y1Axis.getFontHeight(g);
    else if (nbv2 > 0) headerSize += y2Axis.getFontHeight(g);

    int axeHeight = h - (headerSize + labelSize + 2 * MY);
    y1Axis.measureAxis(g, frc, 0, axeHeight);
    y2Axis.measureAxis(g, frc, 0, axeHeight);
    int axisThickness = y1Axis.getThickness() + y2Axis.getThickness();
    int axeWidth = w - 2 * MX - axisThickness;
    xAxis.measureAxis(g, frc, axeWidth, 0);

    int xOrg = MX + y1Axis.getThickness();
    int yOrg = MY + axeHeight + headerSize;

    // Paint chart background
    if( !chartBackground.equals(getBackground()) && axeWidth>0 && axeHeight>0 ) {
      g.setColor(chartBackground);
      g.fillRect(xOrg,yOrg-axeHeight,axeWidth,axeHeight);
    }

    if (paintAxisFirst) {
      //Draw axes
      y1Axis.paintAxis(g, frc, MX, headerSize + MY, xAxis, xOrg, yOrg, getBackground());
      y2Axis.paintAxis(g, frc, MX + axeWidth + y1Axis.getThickness(), headerSize + MY, xAxis, xOrg, yOrg, getBackground());
      xAxis.paintAxis(g, frc, xOrg, yOrg, y1Axis, 0, 0, getBackground());

      //Draw data
      y1Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      y2Axis.paintDataViews(g, xAxis, xOrg, yOrg);


    } else {
      //Draw data
      y1Axis.paintDataViews(g, xAxis, xOrg, yOrg);
      y2Axis.paintDataViews(g, xAxis, xOrg, yOrg);

      //Draw axes
      y1Axis.paintAxis(g, frc, MX, headerSize + MY, xAxis, xOrg, yOrg, getBackground());
      y2Axis.paintAxis(g, frc, MX + axeWidth + y1Axis.getThickness(), headerSize + MY, xAxis, xOrg, yOrg, getBackground());
      xAxis.paintAxis(g, frc, xOrg, yOrg, y1Axis, 0, 0, getBackground());
    }

    redrawPanel(g);
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
  public void mouseClicked(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    if (zoomDrag) {

      Graphics g = getGraphics();
      g.setXORMode(getBackground());
      g.setColor(Color.black);
      Rectangle r;

      // Draw old rectangle
      r = buildRect(zoomX, zoomY, lastX, lastY);
      g.drawRect(r.x, r.y, r.width, r.height);

      // Draw new one
      r = buildRect(zoomX, zoomY, e.getX(), e.getY());
      g.drawRect(r.x, r.y, r.width, r.height);

      lastX = e.getX();
      lastY = e.getY();
      // Release
      g.setPaintMode();
      g.dispose();
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
      xAxis.zoom(r.x, r.x + r.width);
      y1Axis.zoom(r.y, r.y + r.height);
      y2Axis.zoom(r.y, r.y + r.height);
    }
    ipanelVisible = false;
    repaint();
  }

  public void mousePressed(MouseEvent e) {


    // Left button click
    if (e.getButton() == MouseEvent.BUTTON1) {

      // Zoom management
      if (e.isControlDown() || zoomDragAllowed) {
        zoomDrag = true;
        zoomX = e.getX();
        zoomY = e.getY();
        lastX = e.getX();
        lastY = e.getY();
        return;
      }

      SearchInfo si;
      SearchInfo msi = null;

      // Look for the nearest value on each dataView
      msi = y1Axis.searchNearest(e.getX(), e.getY(), xAxis);
      si = y2Axis.searchNearest(e.getX(), e.getY(), xAxis);
      if (si.found && si.dist < msi.dist) msi = si;

      if (msi.found) {
        Graphics g = getGraphics();
        showPanel(g, msi);
        g.dispose();
        return;
      }

      // Click on label
      int i = 0;
      boolean found = false;
      while (i < labelRect.size() && !found) {
        LabelRect r = (LabelRect) labelRect.get(i);
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
      zoomBackMenuItem.setEnabled(isZoomed());
      // Gets user action state
      for(int i=0;i<userActionMenuItem.length;i++) {
        if( userActionMenuItem[i] instanceof JCheckBoxMenuItem ) {
          JCheckBoxMenuItem b = (JCheckBoxMenuItem)userActionMenuItem[i];
          b.setSelected(fireGetActionState(b.getText()));
        }
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

  private String[] buildPanelString(SearchInfo si) {

    String[] str = null;

    if (xAxis.isXY()) {
      str = new String[4];
      str[0] = si.dataView.getExtendedName() + " " + si.axis.getAxeName();
      str[1] = "Time= " + xAxis.formatTimeValue(si.value.x);
      str[2] = "X= " + xAxis.formatValue(si.xdataView.getTransformedValue(si.xvalue.y), 0);
      str[3] = "Y= " + si.axis.formatValue(si.dataView.getTransformedValue(si.value.y), 0) + " " + si.dataView.getUnit();
    } else {
      str = new String[3];
      str[0] = si.dataView.getExtendedName() + " " + si.axis.getAxeName();
      str[1] = "Time= " + xAxis.formatTimeValue(si.value.x);
      str[2] = "Y= " + si.axis.formatValue(si.dataView.getTransformedValue(si.value.y), 0) + " " + si.dataView.getUnit();
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
      JLChartEvent w = new JLChartEvent(this, si);
      str = listener.clickOnChart(w);

    }

      // Default behavior
    if( str==null ) str = buildPanelString(si);

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
   * Add data to dataview and perform fast update.
   * @param v The dataview
   * @param x x coordinates (real space)
   * @param y y coordinates (real space)
   */
  public void addData(JLDataView v, double x, double y) {

    DataList lv = null;
    boolean need_repaint = false;

    //Get the last value
    if (v.getDataLength() > 0) lv = v.getLastValue();

    //Add data
    v.add(x, y);

    // Garbage
    if (displayDuration != Double.POSITIVE_INFINITY) {
      int nb = v.garbagePointTime(displayDuration);
      if (nb > 0 && v.getAxis() != null) need_repaint = true;
    }

    // Does not repaint if zoom drag
    if (zoomDrag) return;

    if (xAxis.isXY()) {
      // Perform fullupate in XY
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
        yaxis.drawFast(getGraphics(), lp, p, v);
      } else {
        // Full update needed
        repaint();
      }

    }

  }

  //****************************************
  // Debug stuff

  private double startTime = System.currentTimeMillis();

  private String readLine(java.io.FileReader f) {

    int c = 0;
    String result = "";
    boolean eor = false;

    while (!eor) {
      try {
        c = f.read();
      } catch (java.io.IOException e) {
        System.out.println(f.toString() + " " + e.getMessage());
        System.exit(0);
      }
      boolean ok = (c >= 32);
      if (ok) result += (char) c;
      eor = (c == -1) || (!ok && result.length() > 0);
    }

    if (result.length() > 0)
      return result;
    else
      return null;

  }

  public static void main(String args[]) {

    final JFrame f = new JFrame();
    final JLChart chart = new JLChart();
    final JTextField d = new JTextField();
    final JLDataView v1 = new JLDataView();
    final JLDataView v2 = new JLDataView();

    chart.setHeaderFont(new Font("Times", Font.BOLD, 18));
    chart.setHeader("Test DataView");
    chart.setLabelFont(new Font("Times", Font.BOLD, 12));
    chart.getY1Axis().setName("mA");
    chart.getY2Axis().setName("unit");

    chart.getXAxis().setAutoScale(true);
    //chart.getXAxis().setAnnotation( JLAxis.VALUE_ANNO );
    chart.getXAxis().setName("Value");
    //chart.getXAxis().setScale(JLAxis.LOG_SCALE);
    chart.getXAxis().setGridVisible(true);
    chart.getXAxis().setSubGridVisible(true);

    //chart.getXAxis().setMaximum( chart.startTime + 100000.0 );

    v1.add(chart.startTime, -10.0);
    v1.add(chart.startTime + 30000, -15.0);
    v1.add(chart.startTime + 60000, 17.0);
    v1.add(chart.startTime + 90000, 21.0);
    v1.add(chart.startTime + 120000, 22.0);
    v1.add(chart.startTime + 150000, 24.0);
    v1.add(chart.startTime + 180000, 98.0);
    v1.add(chart.startTime + 210000, Double.NaN);
    v1.add(chart.startTime + 240000, 21.0);
    v1.add(chart.startTime + 270000, 99.0);
    v1.add(chart.startTime + 300000, 50.0);
    v1.add(chart.startTime + 330000, 40.0);
    v1.add(chart.startTime + 360000, 30.0);
    v1.add(chart.startTime + 390000, 20.0);

    v1.setMarker(JLDataView.MARKER_CIRCLE);
    v1.setStyle(JLDataView.STYLE_DASH);
    v1.setName("Le signal 1");
    v1.setUnit("std");

    chart.getY1Axis().setAutoScale(true);
    chart.getY1Axis().setScale(JLAxis.LOG_SCALE);
    chart.getY1Axis().setMinimum(1e-12);
    chart.getY1Axis().setMaximum(1e-6);

    chart.getY1Axis().addDataView(v1);

    //v2.add( chart.startTime      ,  0.008 );
    //v2.add( chart.startTime+10000 , 0.015 );
    //v2.add( chart.startTime+20000 , 0.017 );
    //v2.add( chart.startTime+30000 , 0.021 );
    //v2.add( chart.startTime+40000 , 0.022 );
    //v2.add( chart.startTime+50000 , 0.024 );
    //v2.add( chart.startTime+60000 , 0.037 );
    //v2.add( chart.startTime+70000 , 0.024 );
    //v2.add( chart.startTime+80000 , 0.021 );
    //v2.add( chart.startTime+90000 , 0.033 );
    java.io.FileReader fi = null;
    String s;
    int i = 0;

    try {
      fi = new java.io.FileReader("vacuum.txt");
    } catch (java.io.FileNotFoundException e) {
      System.out.println("file not found.");
      System.exit(0);
    }
    s = chart.readLine(fi);
    while (s != null) {
      double dd = Double.parseDouble(s);
      v2.add(chart.startTime + i * 1000.0, dd);
      s = chart.readLine(fi);
      i++;
    }

    try {
      fi.close();
    } catch (java.io.IOException e) {
    }

    v2.setColor(new Color(0, 120, 0));
    v2.setName("Et le signal 2 c'est moi");
    v2.setUnit("mBar");
    v2.setMarker(JLDataView.MARKER_DOT);

    chart.getY2Axis().setAutoScale(true);
    chart.getY2Axis().setScale(JLAxis.LOG_SCALE);
    chart.getY2Axis().addDataView(v2);

    JPanel bot = new JPanel();
    bot.setLayout(new FlowLayout());

    JButton b = new JButton("Exit");
    b.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        System.exit(0);
      }
    });

    bot.add(b);

    JButton c = new JButton("Test");
    c.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {

        /*
      chart.startTime+=1000.0;
      chart.getXAxis().setMinimum( chart.startTime );
      chart.getXAxis().setMaximum( chart.startTime + 100000.0 );
      chart.repaint();
      */

        /*
       JLDataViewOption dlg = new JLDataViewOption(f,chart,v1);
       dlg.setVisible(true);
       */
        chart.showOptionDialog();

      }
    });

    bot.add(c);

    d.setText("00000000");
    bot.add(d);

    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(chart, BorderLayout.CENTER);
    f.getContentPane().add(bot, BorderLayout.SOUTH);
    f.setSize(400, 300);
    f.setVisible(true);

  }

}