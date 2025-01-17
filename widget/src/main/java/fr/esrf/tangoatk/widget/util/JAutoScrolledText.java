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
 
/*
 * JAutoScrolledText.java
 * Author: Jean-Luc PONS
 */

package fr.esrf.tangoatk.widget.util;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;

// Thread to handle auto scrolling

class ScrollRun implements Runnable {
    JAutoScrolledText p;

    ScrollRun (JAutoScrolledText parent) {
        this.p = parent;
    }

    public void run () {
        while (!p.stopDemand) {
            try {
                switch (p.getScrollingMode()) {
                    case JAutoScrolledText.SCROLL_TO_SEE_END:
                        if ( p.endFlag ) {
                            // Restart scroll
                            p.endFlag = false;
                            Thread.sleep( p.waitTime );
                        }
                        else {
                            Thread.sleep( p.sleepTime );
                        }
                        p.scrollText();
                        break;
                    case JAutoScrolledText.SCROLL_LOOP:
                        Thread.sleep( p.sleepTime );
                        p.scrollText();
                        break;
                }
            }
            catch (InterruptedException e) {
                System.out.println( e );
            }
        }
        //System.out.println("Stopping scroll");
        p.currentPos = 0;
        p.repaint();
    }
}

/** Text component which supports antialiased font and autoscrolling. When autoscroll is enabled and
 * when the text cannot be totaly displayed, JAutoScrolledText automagicaly scrolls the text from right
 * to left.  */
public class JAutoScrolledText extends JTextField {

  // Static constant
  /** Specifies alignment to the center of the component.  */
  static public int CENTER_ALIGNMENT = 1;
  /** Specifies alignment to the left side of the component.  */
  static public int LEFT_ALIGNMENT = 2;
  /** Specifies alignment to the right side of the component.  */
  static public int RIGHT_ALIGNMENT = 3;

  /** Computes font size and return the minimun size to the layout manager */
  static public int CLASSIC_BEHAVIOR = 2;
  /** Does not compute font size and let the layout manager size the component */
  static public int MATRIX_BEHAVIOR = 1;

  /** Scroll the text until the end of the text is reached and restart */
  final static public int SCROLL_TO_SEE_END = 0;
  /** Continuous scrolling */
  final static public int SCROLL_LOOP = 1;

  // Local declarations
  private int maxPos;
  private boolean scrollNeeded = false;
  private boolean lastScroll = false;
  private String text;
  private int off_x;
  private int off_y;
  private int align;
  private int sizingBehavior;
  private Insets margin;
  private int scrollingMode = SCROLL_TO_SEE_END;

  // Thread parameters
  int sleepTime;
  boolean endFlag;
  boolean stopDemand;
  int currentPos = 0;
  int waitTime = 2000;

  EventListenerList listenerList;  // list of text listeners

  /**
   * Constructs an empty text.
   */
  public JAutoScrolledText() {

    currentPos = 0;
    maxPos = 0;
    off_x = 0;
    off_y = 0;
    sleepTime = 0;
    setBackground(Color.white);
    setForeground(Color.black);
    align = CENTER_ALIGNMENT;
    stopDemand = false;
    endFlag = false;
    text = "";
    margin = new Insets(2, 3, 2, 3);
    sizingBehavior = CLASSIC_BEHAVIOR;
    listenerList = new EventListenerList();

  }

 /**
   * Sets the margin of this components.
   * @param i Margin value
   */
  public void setMargin(Insets i) {
    margin = i;
  }

  /**
   * @return the margin of this components.
   */
  public Insets getMargin() {
    return margin;
  }

  /**
   * Sets the half time between scroll animations.
   * @param ms Half wait time (in millisecs)
   */
  public void setWaitTime(int ms) {
    waitTime = ms;
  }

  /**
   * Sets the sizing behavior.
   * @param s Sizing behavior
   * @see JAutoScrolledText#CLASSIC_BEHAVIOR
   * @see JAutoScrolledText#MATRIX_BEHAVIOR
   */
  public void setSizingBehavior(int s) {
    sizingBehavior = s;
  }

  /**
   * Gets the sizing behavior.
   * @return Actual sizing behavior
   * @see JAutoScrolledText#setSizingBehavior
   */
  public int getSizingBehavior() {
    return sizingBehavior;
  }

  public Dimension getPreferredSize() {

    if (sizingBehavior == MATRIX_BEHAVIOR) {

      return super.getPreferredSize();

    } else {

      Dimension d = ATKGraphicsUtils.measureString(text, getFont());
      d.width += (margin.right + margin.left);
      d.height += (margin.top + margin.bottom);
      return d;

    }

  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Set the autoscroll mode. When autoscroll is enabled and when the text cannot be
   * totaly displayed, JAutoScrolledText automagicaly scrolls the text from right
   * to left.
   * @param time Scrolling refresh rate (millisec), 0 will disable scrolling.
   */
  public void setAutoScroll(int time) {
    sleepTime = time;
  }
  
  public int getSleepTime() {
      return sleepTime;
  }

  public void setSleepTime(int sleepTime) {
      this.sleepTime = sleepTime;
  }

  public int getWaitTime() {
      return waitTime;
  }

  /**
   * Sets the text of this component.
   * @param txt Text to display
   */
  public void setText(String txt) {
    if (txt == null)
      text = "";
    else
      text = txt;
    currentPos = 0;
    repaint();
  }

  public String getText() {
    return text;
  }

  /**
   * Sets the text vertical offset in pixel.
   * @param y Offset value
   */
  public void setVerticalOffset(int y) {
    off_y = y;
    repaint();
  }

  /**
   * @return the current text vertical offset.
   */
  public int getVerticalOffset() {
    return off_y;
  }

  /**
   * Sets the text horizontal offset in pixel.
   * @param y Offset value
   */
  public void setHorizontalOffset(int y) {
    off_x = y;
    repaint();
  }

  /**
   * @return the current text horizontal offset.
   */
  public int getHorizontalOffset() {
    return off_x;
  }

  /**
   * Has no longer effects.
   * @param x Not used
   * @param y Not used
   * @see #setVerticalOffset
   * @see #setHorizontalOffset
   * @deprecated
   */
  public void setValueOffsets(int x, int y) {
    System.out.println("JAutoScrolledText.setValueOffsets() is deprecated and has no effects.");
  }

  /**
   * Sets alignment policiy (when no scroll)
   * @param a Alignment mode
   * @see JAutoScrolledText#CENTER_ALIGNMENT
   * @see JAutoScrolledText#RIGHT_ALIGNMENT
   * @see JAutoScrolledText#LEFT_ALIGNMENT
   */
  public void setHorizontalAlignment(int a) {
    align = a;
  }

  void scrollText() throws InterruptedException {
    if (scrollNeeded) {
      currentPos++;
      if (currentPos > maxPos) {
        // Repaint here to avoid a strange font drawing bug
        repaint();
        // Wait a bit before restarting the scroll
        Thread.sleep(waitTime);
        if (scrollingMode == SCROLL_TO_SEE_END)
        {
            currentPos = 0;
        }
        else
        {
            currentPos = -getWidth();
        }
        endFlag = true;
      }
      repaint();
    }
  }

  /** Paint the component. */
  public void paint(Graphics g) {

    // Prepare rendering environement

    int w = getWidth();
    int h = getHeight();

    if(isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, w, h);
    }

    g.setColor(getForeground());
    g.setFont(getFont());
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    FontRenderContext frc = g2.getFontRenderContext();
    Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
    int tempMax = (int) (bounds.getWidth() - (w - (margin.left + margin.right)));
    if (scrollingMode == SCROLL_TO_SEE_END)
    {
        maxPos = tempMax;
    }
    else
    {
        maxPos = (int) bounds.getWidth();
    }
    //int y = (int) ((bounds.getHeight() + h) / 2);
    double a = getFont().getLineMetrics(text,frc).getAscent();
    int y = (int) ( (h-bounds.getHeight())/ 2.0 + a );
    scrollNeeded = tempMax > 0;

    if(scrollNeeded) fireExceedBounds();

    // Trigger the scrolling ON/OFF
    if (lastScroll != scrollNeeded) {
      if (sleepTime != 0) {
        if (scrollNeeded) {
          //System.out.println("Starting scroll");
          currentPos = 0;
          stopDemand = false;
          endFlag = true;
          ScrollRun p = new ScrollRun(this);
          new Thread(p).start();
        } else {
          stopDemand = true;
        }
      }
    }
    lastScroll = scrollNeeded;

    if (scrollNeeded) {

      g.drawString(text, margin.left + off_x - currentPos, off_y + y);

    } else {

      int xpos = 0;
      switch (align) {
        case 1: //CENTER_ALIGNMENT
          xpos = (int) ((w - bounds.getWidth()) / 2);
          break;
        case 2: //LEFT_ALIGNMENT
          xpos = margin.left;
          break;
        case 3: //RIGHT_ALIGNMENT
          xpos = (int) (w - bounds.getWidth() - margin.right);
          break;
      }
      g.drawString(text, xpos, off_y + y);

    }
    
    paintBorder(g);

  }

  /** Add the specified JAutoScrolledTextListener Listeners
   * @param l Listener to add
   */
  public void addTextListener(JAutoScrolledTextListener l) {
    listenerList.add(JAutoScrolledTextListener.class, l);
  }

  /** Remove the specified JAutoScrolledTextListener Listeners
   * @param l Listener to remove
   */
  public void removeTextListener(JAutoScrolledTextListener l) {
    listenerList.remove(JAutoScrolledTextListener.class, l);
  }

  private void fireExceedBounds() {
    if (listenerList.getListenerCount() > 0) {
      JAutoScrolledTextListener[] list = (JAutoScrolledTextListener[]) (listenerList.getListeners(JAutoScrolledTextListener.class));
      //System.out.println("JAutoScrolledText.fireExceedBounds() called.");
      for (int i = 0; i < list.length; i++) list[i].textExceedBounds(this);
    }
  }

  /**
   * @return the scrolling mode.
   * @see #setScrollingMode
   */
  public int getScrollingMode () {
    return scrollingMode;
  }

  /**
   * Set the scrolling mode.
   * @param scrollingMode Scrolling mode
   * @see #SCROLL_TO_SEE_END
   * @see #SCROLL_LOOP
   */
  public void setScrollingMode (int scrollingMode) {
    this.scrollingMode = scrollingMode;
  }

  public static void main(String args[]) throws Exception {

    final JFrame f = new JFrame();
    final JAutoScrolledText txt = new JAutoScrolledText();
    txt.setText("Test JAutoScrolledText , with autoscrolling....");
    txt.setAutoScroll(30);
    //txt.setScrollingMode(SCROLL_LOOP);
    txt.setFont(new Font("Dialog",Font.BOLD,30));
    f.setContentPane(txt);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setVisible(true);

  }

}
