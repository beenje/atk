/*
 * JAutoScrolledText.java
 * Author: Jean-Luc PONS
 */

package fr.esrf.tangoatk.widget.util;

import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

// Thread to handle auto scrolling

class ScrollRun implements Runnable {

  JAutoScrolledText p;

  ScrollRun(JAutoScrolledText parent) {
    this.p = parent;
  }

  public void run() {

    while (!p.stopDemand) {
      try {
        if (p.endFlag) {
          p.endFlag = false;
          Thread.sleep(3000);
        } else {
          Thread.sleep(p.sleepTime);
        }
        p.scrollText();
      } catch (InterruptedException e) {
        System.out.println(e);
      }
    }
    // System.out.println("Stopping scroll");
    p.currentPos = 0;
    p.repaint();

  }

}

/** Text component which supports antialiased font and autoscrolling. When autoscroll is enabled and
 * when the text cannot be totaly displayed, JAutoScrolledText automagicaly scrolls the text from right
 * to left.  */
public class JAutoScrolledText extends JComponent {

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

  // Thread parameters
  /** Expert usage (Do not use) */
  int     sleepTime;
  /** Expert usage (Do not use) */
  boolean endFlag;
  /** Expert usage (Do not use) */
  boolean stopDemand;
  /** Expert usage (Do not use) */
  int     currentPos = 0;

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
    margin = new Insets(2,3,2,3);
    sizingBehavior = CLASSIC_BEHAVIOR;

  }
  
  public void setMargin(Insets i) {
    margin = i;  
  }
  
  public Insets getMargin() {
    return margin;  
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

      // Create a Buffered image to get high precision
      // Font metrics
      BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
      Graphics g = img.getGraphics();
      g.setFont(getFont());
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      FontRenderContext frc = g2.getFontRenderContext();
      Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
      int w = (int) bounds.getWidth();
      int h = (int) bounds.getHeight();
      g.dispose();
      return new Dimension(w+margin.right+margin.left, 
                           h+margin.top+margin.bottom);

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

  /**
   * Sets the text for this component.
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

  /**
   * Sets the text offset (in pixel).
   * @param x X origin offset (in pixel)
   * @param y Y origin offset (in pixel)
   */
  public void setValueOffsets(int x, int y) {
    off_x = x;
    off_y = y;
    repaint();
  }

  /**
   * Sets aligmenet policiy (when no scroll)
   * @param a Alignment mode
   * @see JAutoScrolledText#CENTER_ALIGNMENT
   * @see JAutoScrolledText#RIGHT_ALIGNMENT
   * @see JAutoScrolledText#LEFT_ALIGNMENT
   */
  public void setHorizontalAlignment(int a) {
    align = a;
  }

  /**
   *  Expert usage. Scrolls the text if needed
   */
  public void scrollText() {
    if (scrollNeeded) {
      currentPos++;
      if (currentPos > maxPos) {
        currentPos = 0;
        endFlag = true;
      }
      repaint();
    }
  }

  // Paint the component
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Prepare rendering environement

    int w = getWidth();
    int h = getHeight();
    
    g.setColor(getBackground());
    g.fillRect(0, 0, w, h);
    Insets insets = getInsets();
    g.setColor(getForeground());
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    FontRenderContext frc = g2.getFontRenderContext();
    Rectangle2D bounds = g.getFont().getStringBounds(text, frc);
    maxPos = (int) (bounds.getWidth() - (w - (insets.left + insets.right)));
    int y = (int) ((bounds.getHeight() + h) / 2);
    scrollNeeded = maxPos > 0;

    // Trigger the scrolling ON/OFF
    if (lastScroll != scrollNeeded) {
      if (sleepTime != 0) {
        if (scrollNeeded) {
          // System.out.println("Starting scroll");
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

      g.drawString(text, off_x - currentPos, off_y + y);

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

  }

}