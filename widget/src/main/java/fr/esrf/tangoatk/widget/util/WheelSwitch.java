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
 * WheelSwitch.java
 * Author: JL Pons 2002 E.S.R.F.
 */

package fr.esrf.tangoatk.widget.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

/** A WheelSwitch editor. */
public class WheelSwitch extends JComponent {

    private static Font defaultFont = new Font("Lucida Bright", Font.PLAIN, 16);
    private static Color defaultBackground = new Color(200, 200, 200);
    private static Color defaultSelectionColor = new Color(156, 154, 206);
    private int off_x; // Postion horz offset
    private int off_y; // Postion vert offset
    private int intNumber; // Number of integer digit
    private int fracNumber; // Number of decimal digit
    private int expNumber; // Number of exponential digit
    private double value; // Current value
    private double maxValue; // Maximun value
    private double minValue; // Minimun value
    private Dimension dz; // digit size
    private boolean editMode; // edition mode
    private String editValue; // value entered by keyboard
    private EventListenerList listenerList; // list of WheelSwitch listeners
    private String format = "%5.2f";
    private boolean editable; // Edition
    private char decimalSeparator = '.';
    private double minimumIncrement=0.0;

    // Arrow buttons
    private JArrowButton buttons_up[];
    private JArrowButton buttons_down[];
    private int nbButton = 0;
    private int selButton = 0;
    private Color buttonBackground;
    private Color selectionColor;
    private Color textBackground;
    private String valueFormatted = null;
    private int totalWidth;
    private int totalHeight;

    /**
     * WheelSwitch constructor.
     */
    public WheelSwitch() {
      this(true);

    }

   /**
    * WheelSwitch constructor.
    * @param editable Editable flag
    */
    public WheelSwitch(boolean editable) {

        setLayout(null);
        setForeground(Color.black);
        setBackground(defaultBackground);
        setBorder(null);
        setFont(defaultFont);
        buttonBackground = getBackground();
        textBackground = null;
        selectionColor = defaultSelectionColor;
        setOpaque(true);
        this.editable = editable;
        setPrecision(3, 2, 0);

        value = 0.0;
        editMode = false;
        editValue = "";

        listenerList = new EventListenerList();

        /* Removed because the followings calls ignore the local JVM setting made by Locale.setDefault()
        try {
          DecimalFormat format=(DecimalFormat)DecimalFormat.getInstance();
          DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
          decimalSeparator=symbols.getDecimalSeparator();
        } catch(Exception e) {}
        */

        if (editable) {
          addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
              processKey(e);
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
          });

          addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
              grabFocus();
            }
          });

          addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
              updateButtonFocus();
              repaint();
            }

            public void focusLost(FocusEvent e) {
              updateButtonFocus();
              repaint();
            }
          });
        }
    }

  /**
   * Sets the minimum increment. When try to add a value lower than minimumIncrement,
   * minimumIncrement is added.
   * @param inc Minimum increment
   */
    public void setMinimumIncrement(double inc) {
      minimumIncrement = inc;
    }

    public double getMinimumIncrement() {
      return minimumIncrement;
    }

  /**
   * Sets the text background, null to disable.
   * @param c Text background color
   */
    public void setTextBackground(Color c) {
      textBackground = c;
    }

    public Dimension getPreferredSize() {

        int w, h;
        Insets borderSize;

        if (hasBorder())
            borderSize = getInsets();
        else
            borderSize = new Insets(1, 1, 1, 1);

        computeDigitSize();

        if (expNumber > 0) {
            w = dz.width * (intNumber + fracNumber + expNumber + 4);
        }
        else if (fracNumber > 0) {
            w = dz.width * (intNumber + fracNumber + 2);
        }
        else {
            w = dz.width * (intNumber + fracNumber + 1);
        }

        if(editable) {
          h = dz.width * 2 + dz.height;
        } else {
          h = dz.height;
        }

        return new Dimension(w + borderSize.right + borderSize.left, h
                + borderSize.top + borderSize.bottom);

    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Sets the WheelSwitch value.
     * 
     * @param v
     *            New WheelSwitch value.
     */
    public void setValue(double v) {

        double dval1 = 0;
        double dval2 = 0;
        value = v;
        if (!Double.isNaN(v) && !Double.isInfinite(v))
        {
            if (format.indexOf('e') > 0) {
                dval1 = getFloatPart().doubleValue();
                dval2 = getExpPart().doubleValue();
            }
            else {
                dval1 = value;
            }
            double newValue = near(dval1) * Math.pow(10, dval2);
            Double tempDouble = new Double(Double.NaN);
            try {
                if (isGoodFormat())
                {
                    String valFormated = ATKFormat.format(format, newValue);
                    if(!isHexFormat(format))
                      tempDouble = Double.valueOf(valFormated);
                    else
                      tempDouble = (double)Integer.parseInt(valFormated,16);
                }
                else
                {
                    tempDouble = new Double(newValue);
                }
            }
            catch(Exception e) {
                tempDouble = new Double(Double.NaN);
            }
            if (!tempDouble.equals(new Double(Double.NaN))) {
                value = tempDouble.doubleValue();
            }
            else {
                value = newValue;
            }
        }

        updateButtonVisibility();
        repaint();
    }

    /**
     * Returns the current wheelswitch value.
     * @return Current value
     */
    public double getValue() {
        return value;
    }

    public void setFont(Font f) {
        super.setFont(f);
        // reset digit size
        dz = null;
        computeDigitSize();
        if(isVisible()) placeComponents(getSize());
    }

    public boolean isGoodFormat()
    {
        if (format.indexOf("%") == 0 && format.lastIndexOf("%") == format.indexOf("%"))
        {
            if (format.indexOf(".") == -1)
            {
                //case %xd
                try
                {
                    int x = Integer.parseInt(format.substring(1, format.length()-1));
                    if (x > 0)
                    {
                        return true;
                    }
                    else return false;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            else
            {
                if (format.indexOf(".") == format.lastIndexOf("."))
                {
                    if (
                         (
                            (format.toLowerCase().indexOf("f") == format.length() - 1)
                            && (format.toLowerCase().indexOf("f") > 0)
                         )
                         ||
                         (
                            (format.toLowerCase().indexOf("e") == format.length() - 1)
                            && (format.toLowerCase().indexOf("e") > 0)
                         )
                       )
                    {
                        //case %x.yf, %x.ye
                        try
                        {
                            int x = Integer.parseInt( format.substring(1, format.indexOf(".")) );
                            int y = Integer.parseInt( format.substring(format.indexOf(".")+1, format.length()-1) );
                            if (x > y && x > 0 && y >= 0)
                            {
                                return true;
                            }
                            else return false;
                        }
                        catch (Exception e)
                        {
                            return false;
                        }
                    }
                    else return false;
                }
                else return false;
            }
        }
        else return false;
    }

    /**
     * @return the current digit size according the the component Font.
     */
    public Dimension getDigitSize() {
        return dz;
    }

    /**
     * Sets the color of arrow buttons.
     * 
     * @param c New button color.
     */
    public void setButtonColor(Color c) {

      if(!editable) return;

        int i;

        buttonBackground = c;

        for (i = 0; i < nbButton; i++) {
            buttons_up[i].setBackground(buttonBackground);
            buttons_down[i].setBackground(buttonBackground);
        }

    }

    /**
     * @return the current button color.
     * @see #setButtonColor
     */
    public Color getButtonColor() {
        return buttonBackground;
    }

    /**
     * Sets the selected button color.
     * 
     * @param c
     *            Color for selected button.
     */
    public void setSelButtonColor(Color c) {
        selectionColor = c;
    }

    /**
     * @return the current button selection color.
     */
    public Color getSelButtonColor() {
        return selectionColor;
    }

  /**
   * Sets the max value. Must be called after setFormat() or setPrecision().
   * Ingnored if scientific format is used.
   * @param max Maximum allowed value
   */
    public void setMaxValue(double max) {
      if (format.indexOf('e') == -1) {
        maxValue = max;
        updateButtonVisibility();
      }
    }

    public void setEnabled(boolean arg0) {
        super.setEnabled(arg0);
        if(buttons_up == null || buttons_down == null)
            return;
        for (int i = 0; i < buttons_up.length; i++)
            buttons_up[i].setEnabled(arg0);
        for (int i = 0; i < buttons_down.length; i++)
            buttons_down[i].setEnabled(arg0);        
    }
  /**
   * Sets the min value. Must be called after setFormat() or setPrecision().
   * Ingnored if scientific format is used.
   * @param min Minimum allowed value
   */
    public void setMinValue(double min) {
      if (format.indexOf('e') == -1) {
        minValue = min;
        updateButtonVisibility();
      }
    }

    public void setFormat(String aformat) {

      setFormat(aformat,null);

    }

    /**
     * Set the format as C format (only "%x.yf" or "%xd" is supported). This
     * will change the button configuration.
     * 
     * @param aformat New wheelswitch format.
     * @param attName Attribute name
     */
    public void setFormat(String aformat, String attName) {

      String oldFormat = format;
      double oldValue = value;

      String errStr = (attName==null)?"WheelSwitch: Invalid format \"" + aformat + "\"":
          "WheelSwitch: Invalid format \"" + aformat + "\" for " + attName;
      errStr += ": use %x.yf, %x.yF, %x.ye, %x.yE, %xd or %xD or %xX";

      int pIdx = aformat.indexOf("%");
      if(pIdx<0) {
        System.out.println(errStr);
        System.out.println("==> WheelSwitch: format used instead: " + format);
        return;
      } else {
        aformat = aformat.substring(pIdx,aformat.length());
      }

      if (aformat.equalsIgnoreCase("%d")) {
        // Default to %5d
        aformat = "%5d";
      }

      if (aformat.equals("%x")) {
        // Default to %4x
        aformat = "%4x";
      }

      if (aformat.equals("%X")) {
        // Default to %4X
        aformat = "%4X";
      }

      // format validation
      format = aformat;

      if (!isGoodFormat()) {
        System.out.println(errStr);
        // comment next lines if you don't want to format the value
        format = oldFormat;
        System.out.println("==> WheelSwitch: format used instead: " + format);
        return;
      }

      if(!isHexFormat(format))
        format = format.toLowerCase(); // to associate %x.ye with %x.yE

      String f = format.replace('.', '_');
      f = f.replace('%', '0');
      f = f.substring(0, f.length() - 1);

      String[] s = f.split("_");

      try {

        if (s.length == 2) {
          int a = Integer.parseInt(s[0]);
          int b = Integer.parseInt(s[1]);

          // scientific format
          if (format.indexOf('e') != -1) {
            String temp = ATKFormat.format(format, value);
            if (temp != null) {
              reformat(temp, a - b, b);
            } else {
              // should never happen (due to isGoodFormat() test)
              System.out.println(errStr);
              format = oldFormat;
              value = oldValue;
            }
          }
          // float
          else {
            setPrecision(a - b, b, 0);
          }
        }
        // integer
        else if (s.length == 1) {
          int a = Integer.parseInt(s[0]);
          setPrecision(a, 0, 0);
        }

      } catch (NumberFormatException n) {
        // should never happen (due to isGoodFormat() test)
        System.out.println(errStr);
        format = oldFormat;
        value = oldValue;
      }

    }

  /*
   * This function is necessary to recalculate the number of digits for scientific format
   */
  private void reformat(String valFormated, int integerPart, int decimalPart) {
    String valueFormated = valFormated.toLowerCase();
    int e = valueFormated.indexOf('e');
    int plus = valueFormated.lastIndexOf('+');
    int moins = valueFormated.lastIndexOf('-');
    if (plus > e) {
      e = plus;
    } else if (moins > e) {
      e = moins;
    }
    valueFormated = valueFormated.substring(e + 1, valueFormated.length());
    setPrecision(integerPart, decimalPart, valueFormated.length());
  }

  /**
     * Set the precision of this wheelswitch.
     * 
     * @param inb number of digit for the integer part
     * @param fnb number of digit for the decimal part
     * @param enb number of digit for the exponent part
     */
    synchronized public void setPrecision(int inb, int fnb, int enb) {

        int i;
        int nb = nbButton;

        // Reset nbButton to zero during precision update
        nbButton = 0;

        // Remove old button
        if(editable) {
          for (i = 0; i < nb; i++) {
            remove(buttons_up[i]);
            remove(buttons_down[i]);
            buttons_up[i] = null;
            buttons_down[i] = null;
          }
        }

        // Create new buttons

        intNumber = inb;
        if (intNumber < 1) {
            intNumber = 1;
        }
        fracNumber = fnb;
        if (fracNumber < 0) {
            fracNumber = 0;
        }
        expNumber = enb;
        if (expNumber < 0) {
            expNumber = 0;
        }

        nb = intNumber + fracNumber + expNumber;
        if(editable) {
          buttons_up = new JArrowButton[nb];
          buttons_down = new JArrowButton[nb];
        }

        if (expNumber == 0) {
          if(!isHexFormat(format)) {
            maxValue =  Math.pow(10, intNumber);
            minValue = -Math.pow(10, intNumber);
          } else {
            maxValue =  Math.pow(16, intNumber);
            minValue = -Math.pow(16, intNumber);
          }
        } else {
            maxValue =  Double.MAX_VALUE;
            minValue = -Double.MAX_VALUE;
        }

        for (i = 0; i < nb && editable; i++) {

            // Top buttons
            buttons_up[i] = new JArrowButton();
            add(buttons_up[i]);
            buttons_up[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    clickUp(evt);
                }
            });

            // Bottom buttons
            buttons_down[i] = new JArrowButton();
            add(buttons_down[i]);
            buttons_down[i].setOrientation(JArrowButton.DOWN);
            buttons_down[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    clickDown(evt);
                }
            });

        }
        double dval1 = 0;
        double dval2 = 0;
        if (format.indexOf('e') > 0) {
            dval1 = getFloatPart().doubleValue();
            dval2 = getExpPart().doubleValue();
        }
        else {
            dval1 = value;
        }
        //value = near(dval1) * Math.pow(10, dval2);
        setValue(near(dval1) * Math.pow(10, dval2));
        nbButton = nb;
        selButton = nb - 1;

        if(isVisible()) placeComponents(getSize());
        updateButtonFocus();
        repaint();

    }

    // Returns if true if the component has a border
    private boolean hasBorder() {
        return getBorder() != null;
    }

    // Call when user click on top button
    private void clickUp(MouseEvent evt) {
        if(!isEnabled())
            return;
        
        int i = 0;
        boolean found = false;

        grabFocus();

        if (Double.isNaN(value) || Double.isInfinite(value))
            return;

        while (i < nbButton && !found) {
            found = (evt.getSource() == buttons_up[i]);
            if (!found)
                i++;
        }

        if (!editMode && found) {
            increaseValue(i);
            selButton = i;
            updateButtonFocus();
            repaint();
        }
    }

    // Call when user click on bottom buttons
    private void clickDown(MouseEvent evt) {
        if(!isEnabled())
            return;
        int i = 0;
        boolean found = false;

        grabFocus();

        if (Double.isNaN(value) || Double.isInfinite(value))
            return;

        while (i < nbButton && !found) {
            found = (evt.getSource() == buttons_down[i]);
            if (!found)
                i++;
        }

        if (!editMode && found) {
            decreaseValue(i);
            selButton = i;
            updateButtonFocus();
            repaint();
        }
    }

    // Compute minimun size for the digit
    // according to current font
    private void computeDigitSize() {

        int max_width = 0;
        int max_height = 0;
        int i;

        if (dz == null) {

            String[] charSet = { "0", "1", "2", "3", "4", "5", "6", "7", "8",
                    "9", "E", "+", "-", " " , "A" , "B" , "C" , "D" , "E" , "F" };

            for (i = 0; i < charSet.length; i++) {

                Dimension b = ATKGraphicsUtils.measureString(charSet[i],
                        getFont());
                int w = b.width + 1;
                if (w > max_width)
                    max_width = w;

            }

            max_height = (int) ATKGraphicsUtils.getLineMetrics("0123456789E+- ABCDEF",
                    getFont()).getAscent() + 1;
            dz = new Dimension(max_width, max_height);

        }

    }

    // Round according to desired precision (fracNumber)
    private double near(double d) {
        double r = Math.pow(10, fracNumber);
        return Math.rint(d * r) / r;
    }

  private void computeValueFormatted() {

    if (Double.isNaN(value) || Double.isInfinite(value)) {
      valueFormatted = "";
      return;
    }

    if (isGoodFormat()) {
      if(isHexFormat(format)) {
        if(value<0) {
          valueFormatted = "-"+ATKFormat.format(format, -value);
        } else {
          valueFormatted = ATKFormat.format(format, value);
        }
      } else
        valueFormatted = ATKFormat.format(format, value);
    } else {
      valueFormatted = Double.toString(value);
    }

    if(!isHexFormat(format))
      valueFormatted = valueFormatted.toLowerCase();

    valueFormatted = valueFormatted.replaceAll("\\-", "");
    valueFormatted = valueFormatted.replaceAll("\\+", "");
    valueFormatted = valueFormatted.replaceAll(" ", "");

  }

  //Return the digit at the specified position
  private char getDigit(int pos) {

    if (Double.isNaN(value) || Double.isInfinite(value))
      return '-';

    String intPart = "";

    if (format.indexOf('d') != -1 || isHexFormat(format) ) {
      intPart = valueFormatted;
    } else {
      int idx = valueFormatted.indexOf(decimalSeparator);
      if (idx >= 0) {
        intPart = valueFormatted.substring(0, idx);
      } else {
        intPart = valueFormatted;
      }
    }

    if (pos < intNumber) {

      // integer part
      if (intPart.length() < intNumber) {
        if (pos < intNumber - intPart.length()) {
          return ' ';
        } else {
          return intPart.charAt(pos + intPart.length() - intNumber);
          //return intPart.substring(pos + intPart.length() - intNumber, pos + 1 + intPart.length() - intNumber);
        }
      } else if (intPart.length() == intNumber) {
        return intPart.charAt(pos);
      } else {
        // Here, intPart.length() > intNumber
        int over = intPart.length() - intNumber;
        return intPart.charAt(pos + over);
      }

    } else if (pos < intNumber + fracNumber) {

      // Decimal part
      return valueFormatted.charAt(pos + 1 + intPart.length() - intNumber);

    } else {

      // Exponential part
      int e = valueFormatted.indexOf('e');
      return valueFormatted.charAt(pos + e + 1 - intNumber - fracNumber);

    }

  }


  // Paint the component
  protected void paintComponent(Graphics g) {

    int w = getWidth();
    int h = getHeight();

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    g.setPaintMode();

    computeDigitSize();

    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, w, h);
    }

    g.setFont(getFont());

    if (editMode) {

      g.setColor(getForeground());
      FontMetrics fm = getFontMetrics(getFont());
      Rectangle2D b = fm.getStringBounds(editValue, g);
      int xpos = (w - (int) b.getWidth()) / 2;
      g.drawString(editValue, xpos, off_y + dz.width + dz.height - 2);

    } else {

      int ypos;

      if(textBackground!=null) {
        g.setColor(textBackground);
        g.fillRect(off_x+1,off_y+1,totalWidth-2,totalHeight-2);
      }

      if(isEnabled())
        g.setColor(getForeground());
      else
        g.setColor(getBackground().darker());

      if (editable) {
        ypos = off_y + dz.width + dz.height - 2;
      } else {
        ypos = off_y + dz.height - 2;
      }
      if (value < 0.0) {
        g.drawString("-", off_x, ypos);
      }
      if (fracNumber > 0) {
        g.drawString(".", off_x + (intNumber + 1) * dz.width + 2, ypos);
      }
      if (expNumber > 0) {
        g.drawString("E", off_x + (intNumber + fracNumber + 2)
            * dz.width + 2, ypos);
        String ePart = "" + getExpPart();
        String s = ePart.substring(0, 1);
        if (!"-".equals(s)) {
          s = "+";
        }
        g.drawString(s, off_x + (intNumber + fracNumber + 3)
            * dz.width + 2, ypos);
      }

      computeValueFormatted();
      char[] CH = new char[1];

      for (int i = 0; i < nbButton; i++) {
        int xpos;
        if (i < intNumber) {
          xpos = off_x + (i + 1) * dz.width;
        } else if (i < intNumber + fracNumber) {
          xpos = off_x + (i + 2) * dz.width;
        } else {
          xpos = off_x + (i + 4) * dz.width;
        }
        CH[0] = getDigit(i);
        g.drawString(new String(CH), xpos, ypos);
      }

    }

    // Draw the focus
    if (hasFocus() && editable) {

      Insets b;
      if (hasBorder())
        b = getInsets();
      else
        b = new Insets(1, 1, 1, 1);

      g.setColor(defaultSelectionColor);
      g.drawLine(b.left + 1, b.top + 1, w - b.right - 1, b.top + 1);
      g.drawLine(w - b.right - 1, b.top + 1, w - b.right - 1, h
          - b.bottom - 1);
      g.drawLine(w - b.right - 1, h - b.bottom - 1, b.left + 1, h
          - b.bottom - 1);
      g.drawLine(b.left + 1, h - b.bottom - 1, b.left + 1, b.top + 1);

    }

  }

  /**
     * Add the specified WheelSwitch Listener.
     * 
     * @param l
     *            WheelSwitch Listener
     */
    public void addWheelSwitchListener(IWheelSwitchListener l) {
        listenerList.add(IWheelSwitchListener.class, l);
    }

    /**
     * Remove the specified WheelSwitch Listener.
     * 
     * @param l
     *            WheelSwitch Listener to be removed
     */
    public void removeWheelSwitchListener(IWheelSwitchListener l) {
        listenerList.remove(IWheelSwitchListener.class, l);
    }

    private Double getExpPart() {
        int e = format.indexOf('e');
        if (e == -1) {
            return null;
        }
        else {
            String displayVal = ATKFormat.format(format, value);
            if (!Double.isNaN(value) && !Double.isInfinite(value) && displayVal != null) {
                displayVal = displayVal.toLowerCase();
                return Double.valueOf(displayVal.substring(displayVal.indexOf('e') + 1));
            }
            else {
                return null;
            }
        }
    }
    
    private Double getFloatPart() {
        int e = format.indexOf('e');
        if (e == -1) {
            return new Double(value);
        }
        else {
            String displayVal = ATKFormat.format(format, value);
            if (!Double.isNaN(value) && !Double.isInfinite(value) && displayVal != null) {
                displayVal = displayVal.toLowerCase();
                return Double.valueOf(displayVal.substring(0,displayVal.indexOf('e')));
            }
            else {
                return null;
            }
        }
    }
    
    // Fire WheelSwitchEvent to all registered listeners
    private void fireValueChange() {
        IWheelSwitchListener[] list = (IWheelSwitchListener[]) (listenerList
                .getListeners(IWheelSwitchListener.class));
        WheelSwitchEvent w = new WheelSwitchEvent(this, value);
        for (int i = 0; i < list.length; i++) {
            list[i].valueChange(w);
        }
    }

    // Update button color according to the focus.
    private void updateButtonFocus() {
      if(!isEnabled())
            return;
      if(!editable) return;
        for (int i = 0; i < nbButton; i++) {
            if (i == selButton && hasFocus()) {
                buttons_up[i].setBackground(selectionColor);
                buttons_down[i].setBackground(selectionColor);
            }
            else {
                buttons_up[i].setBackground(buttonBackground);
                buttons_down[i].setBackground(buttonBackground);
            }
        }
    }
    
    // Increase value by 10^diz(idx)
    private void increaseValue(int idx) {

        double dval1 = 0;
        double dval2 = 0;

        if (format.indexOf('e') > 0) {
            dval1 = getFloatPart().doubleValue();
            dval2 = getExpPart().doubleValue();
        }
        else {
            dval1 = value;
        }

        if (idx < intNumber + fracNumber) {
            dval1 = incValue(dval1, idx, intNumber);
        }
        else {
            int idx2 = idx - intNumber - fracNumber;
            dval2 = incValue(dval2, idx2, expNumber);
        }

/*        System.out.println("near(dval1) : " + near(dval1));
        System.out.println("dval2 : " + dval2);
        System.out.println("Math.pow(10, dval2) : " + Math.pow(10, dval2));*/
        double newValue = near(dval1) * Math.pow(10, dval2);

        if (!Double.isNaN(newValue) && !Double.isInfinite(newValue)) {

            double curValue = getValue();
            double delta = Math.abs(curValue-newValue);
            if(delta<minimumIncrement) {
              newValue = curValue + minimumIncrement;
            }

            if ((newValue <= maxValue) && (newValue >= minValue)) {
                //value = newValue;
                setValue(newValue);
                if (format.indexOf('e') > 0) {
                    reformat(ATKFormat.format(format, value), intNumber, fracNumber);
                }
            }
            fireValueChange();
        }
        updateButtonVisibility();
    }

  private double incValue(double val, int idx, int ref) {

    double newValue;

    if (!isHexFormat(format))
      newValue = val + Math.pow(10, (ref - idx - 1));
    else
      newValue = val + Math.pow(16, (ref - idx - 1));

    return newValue;
  }

  private double decValue(double val, int idx, int ref) {

    double newValue;

    if (!isHexFormat(format))
      newValue = val - Math.pow(10, (ref - idx - 1));
    else
      newValue = val - Math.pow(16, (ref - idx - 1));

    return newValue;
  }

  // Decrease value by 10^diz(idx)
    private void decreaseValue(int idx) {

        double dval1 = 0;
        double dval2 = 0;
        
        if (format.indexOf('e') > 0) {
            dval1 = getFloatPart().doubleValue();
            dval2 = getExpPart().doubleValue();
        }
        else {
            dval1 = value;
        }
        if (idx < intNumber + fracNumber) {
            dval1 = decValue(dval1, idx, intNumber);
        }
        else {
            int idx2 = idx - (intNumber + fracNumber);
            dval2 = decValue(dval2, idx2, expNumber);//PROBLEME : 1-->0.99999
        }

/*      System.out.println("near(dval1) : " + near(dval1));
        System.out.println("dval2 : " + dval2);
        System.out.println("Math.pow(10, dval2) : " + Math.pow(10, dval2));*/
        double newValue = near(dval1) * Math.pow(10, dval2);

        if (!Double.isNaN(newValue) && !Double.isInfinite(newValue)) {

          double curValue = getValue();
          double delta = Math.abs(curValue-newValue);
          if(delta<minimumIncrement) {
            newValue = curValue - minimumIncrement;
          }

          if ((newValue <= maxValue) && (newValue >= minValue)) {
              //value = newValue;
              setValue(newValue);
              if (format.indexOf('e') > 0) {
                  reformat(ATKFormat.format(format, value), intNumber, fracNumber);
              }
          }
          fireValueChange();
        }
        updateButtonVisibility();
        /*if (!Double.isNaN(value)) {
            double newValue = near(value - Math.pow(10, (intNumber - idx - 1)));
            if (Math.abs(newValue) < maxValue)
                value = newValue;
            fireValueChange();
        }*/
    }

    // Process Key Envent
    private void processKey(KeyEvent e) {

        char c = e.getKeyChar();
        c = Character.toUpperCase(c);
        int code = e.getKeyCode();

        // Button selection
        if (!editMode && nbButton > 0) {
            switch (code) {
            case KeyEvent.VK_RIGHT:
                selButton++;
                if (selButton >= nbButton)
                    selButton = nbButton - 1;
                updateButtonFocus();
                repaint();
                break;
            case KeyEvent.VK_LEFT:
                selButton--;
                if (selButton < 0)
                    selButton = 0;
                updateButtonFocus();
                repaint();
                break;
            case KeyEvent.VK_UP:
                increaseValue(selButton);
                repaint();
                break;
            case KeyEvent.VK_DOWN:
                decreaseValue(selButton);
                repaint();
                break;
            }
        }

        if ((c >= '0' && c <= '9') || c == decimalSeparator || c == '-' ||
            (isHexFormat(format) && c>='A' && c<='F')) {
            editValue += c;
            editMode = true;
            repaint();
        }
        
        if ((c == 'e') || (c == 'E')) {
            if (editValue.toLowerCase().indexOf('e') == -1 && !"".equals(editValue)) {
                editValue += c;
                editMode = true;
                repaint();
            }
        }

        if (code == KeyEvent.VK_CLEAR || code == KeyEvent.VK_CANCEL
                || code == KeyEvent.VK_ESCAPE) {
            editValue = "";
            editMode = false;
            repaint();
        }

        if (editMode
                && editValue.length() > 0
                && (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE)) {
            editValue = editValue.substring(0, editValue.length() - 1);
            repaint();
        }

        if (code == KeyEvent.VK_ENTER) {
            if (editMode) {
                try {

                    double newValue;

                    if(isHexFormat(format)) {
                      newValue = (double)Integer.parseInt(editValue,16);
                    } else {
                      newValue = Double.parseDouble(editValue);
                    }

                    if ((newValue <= maxValue) && (newValue >= minValue)) {
                        value = newValue; // For a value entered manually, we don't want to round it
                        updateButtonVisibility();
                        fireValueChange();
                        editValue = "";
                        editMode = false;
                        repaint();
                    }
                    else {
                      String mesg = "Warning, value is out of the range ["+minValue+","+maxValue+"]\nCancel editing ?";
                        int r = JOptionPane
                                .showConfirmDialog(
                                        this,mesg,
                                        "[WheelSwitch error]",
                                        JOptionPane.YES_NO_OPTION);
                        if (r == JOptionPane.YES_OPTION) {
                            editValue = "";
                            editMode = false;
                            repaint();
                        }
                    }
                }
                catch (NumberFormatException n) {
                    // nothing to do
                }
            }
            else {
                // allowing to quickly enter the same value
                fireValueChange();
                repaint();
            }
        }

    }

    private void updateButtonVisibility() {
    
      double val;

      // Update button visibility
      if(editable) {

        // Don't affect button in scientific format
        if (format.indexOf('e') == -1) {

          for(int i=0;i<nbButton;i++) {
            val = incValue(value, i, intNumber);
            buttons_up[i].setVisible(val<=maxValue);
            val = decValue(value, i, intNumber);
            buttons_down[i].setVisible(val>=minValue);
          }

        }

      }

    }

    private boolean isHexFormat(String f) {
      return f.indexOf('x')!=-1 || f.indexOf('X')!=-1;
    }

    public void setBounds(int x,int y,int w,int h) {
      placeComponents(new Dimension(w, h));
      super.setBounds(x,y,w,h);
    }

    // Place the components
    synchronized private void placeComponents(Dimension sz) {

        int i;

        computeDigitSize();

        // Place buttons

        if (expNumber >0)
            totalWidth = dz.width * (nbButton + 4);
        else if (fracNumber > 0)
            totalWidth = dz.width * (nbButton + 2);
        else
            totalWidth = dz.width * (nbButton + 1);

        if(editable) {
          totalHeight = dz.height + 2 * dz.width;
        } else {
          totalHeight = dz.height;
        }

        off_x = (sz.width - totalWidth) / 2;
        off_y = (sz.height - totalHeight) / 2;

        for (i = 0; i < nbButton && editable; i++) {
            int xpos;
            if (i < intNumber) {
                xpos = off_x + (i + 1) * dz.width + 1;
            }
            else if (i < intNumber + fracNumber) {
                xpos = off_x + (i + 2) * dz.width + 1;
            }
            else {
                xpos = off_x + (i + 4) * dz.width + 1;
            }

            // Top buttons
            buttons_up[i].setBounds(xpos, off_y + 2, dz.width - 2, dz.width - 2);

            // Bottom buttons
            int h = dz.width + dz.height + off_y;
            buttons_down[i].setBounds(xpos, h, dz.width - 2, dz.width - 2);
        }

    }

    /* main: Test the wheel switch */
    public static void main(String args[]) {
        
        JFrame f = new JFrame();

        WheelSwitch ws1 = new WheelSwitch();
        WheelSwitch ws2 = new WheelSwitch();
        WheelSwitch ws3 = new WheelSwitch();
        WheelSwitch ws4 = new WheelSwitch();
        WheelSwitch ws5 = new WheelSwitch();
        WheelSwitch ws6 = new WheelSwitch();
        WheelSwitch ws7 = new WheelSwitch();

        // scientific format
        ws1.setFormat("%4.3e");
        ws1.setButtonColor(Color.RED);
        ws1.setValue(-42.9995);
        ws1.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws1:" + e.getValue());
            }
        });

        // float format
        ws2.setFormat("%6.2f");
        ws2.setMinimumIncrement(0.05);
        ws2.setButtonColor(Color.GREEN);
        ws2.setValue(-12.5);
        ws2.setMaxValue(75.0);
        ws2.setMinValue(-25.0);
        ws2.setFont(new Font("Dialog", Font.BOLD, 50));
        ws2.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws2:" + e.getValue());
            }
        });

        // integer format
        ws3.setFormat("%6d");
        ws3.setButtonColor(Color.BLUE);
        ws3.setValue(48.0);
        ws3.setFont(new Font("Lucida Bright", Font.BOLD, 30));
        ws3.setBorder(BorderFactory.createEtchedBorder());
        ws3.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws3:" + e.getValue());
            }
        });

        //hex format
        ws4.setFormat("0x%08X");
        ws4.setButtonColor(new Color(100, 200, 160));
        ws4.setValue(0xA023);
        ws4.setFont(new Font("Lucida Bright", Font.BOLD, 12));
        ws4.setBorder(BorderFactory.createEtchedBorder());
        ws4.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws4:" + e.getValue());
            }
        });

        //NaN with scientific format
        ws5.setFormat("%5.2e");
        ws5.setButtonColor(new Color(100, 0, 0));
        ws5.setValue(Double.NaN);
        ws5.setFont(new Font("Dialog", Font.PLAIN, 16));
        ws5.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws5:" + e.getValue());
            }
        });

        //NaN with float format
        ws6.setFormat("%5.2f");
        ws6.setButtonColor(new Color(0, 100, 0));
        ws6.setValue(Double.NaN);
        ws6.setFont(new Font("Dialog", Font.PLAIN, 16));
        ws6.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws6:" + e.getValue());
            }
        });

        //NaN with integer format
        ws7.setFormat("%5d");
        ws7.setButtonColor(new Color(0, 0, 100));
        ws7.setValue(Double.NaN);
        ws7.setFont(new Font("Dialog", Font.PLAIN, 16));
        ws7.addWheelSwitchListener(new IWheelSwitchListener() {
            public void valueChange(WheelSwitchEvent e) {
                System.out.println("Value changed ws7:" + e.getValue());
            }
        });

        JButton b = new JButton("OK");
        b.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });

        f.getContentPane().setBackground(Color.white);
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(ws1);
        f.getContentPane().add(ws2);
        f.getContentPane().add(ws3);
        f.getContentPane().add(ws4);
        f.getContentPane().add(ws5);
        f.getContentPane().add(ws6);
        f.getContentPane().add(ws7);
        f.getContentPane().add(b);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

    }

}
