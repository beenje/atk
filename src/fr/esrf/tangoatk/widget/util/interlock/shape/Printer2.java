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
 
package fr.esrf.tangoatk.widget.util.interlock.shape;
/* Class generated by JDraw */

import java.awt.*;

/** ---------- Printer2 class ---------- */
public class Printer2 {

  private static int[][] xPolys = null;
  private static int[][] yPolys = null;

  private static Color sColor0 = new Color(217,218,220);
  private static Color sColor1 = new Color(0,169,0);
  private static Color sColor2 = new Color(0,188,0);

  private static int[][] xOrgPolys = {
    {-11,-10,8,9},
    {-11,-11,10,10},
    {-17,-11,-10,8,9,15,19,19,18,-19,-20,-20},
    {-18,-18,-16,-11,-10,9,10,15,17,17},
    {7,7,10,10},
    {12,12,15,15},
  };

  private static int[][] yOrgPolys = {
    {-10,-20,-20,-10},
    {11,19,19,12},
    {-12,-12,-11,-11,-12,-12,-3,4,5,5,4,-3},
    {5,13,15,15,13,13,15,15,13,5},
    {0,2,2,0},
    {0,2,2,0},
  };

  static public void paint(Graphics g,Color backColor,int x,int y,double size) {

    // Allocate array once
    if( xPolys == null ) {
      xPolys = new int [xOrgPolys.length][];
      yPolys = new int [yOrgPolys.length][];
      for( int i=0 ; i<xOrgPolys.length ; i++ ) {
        xPolys[i] = new int [xOrgPolys[i].length];
        yPolys[i] = new int [yOrgPolys[i].length];
      }
    }

    // Scale and translate poly
    for( int i=0 ; i<xOrgPolys.length ; i++ ) {
      for( int j=0 ; j<xOrgPolys[i].length ; j++ ) {
        xPolys[i][j] = (int)((double)xOrgPolys[i][j]*size+0.5) + x;
        yPolys[i][j] = (int)((double)yOrgPolys[i][j]*size+0.5) + y;
      }
    }

    // Paint object
    g.setColor(sColor0);g.fillPolygon(xPolys[0],yPolys[0],xPolys[0].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[0],yPolys[0],xPolys[0].length);
    g.setColor(Color.white);g.fillPolygon(xPolys[1],yPolys[1],xPolys[1].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[1],yPolys[1],xPolys[1].length);
    g.setColor(backColor);g.fillPolygon(xPolys[2],yPolys[2],xPolys[2].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[2],yPolys[2],xPolys[2].length);
    g.setColor(Color.black);
    g.drawLine((int)(-20.0*size+0.5)+x, (int)(-3.0*size+0.5)+y, (int)(19.0*size+0.5)+x, (int)(-3.0*size+0.5)+y);
    g.setColor(backColor);g.fillPolygon(xPolys[3],yPolys[3],xPolys[3].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[3],yPolys[3],xPolys[3].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[4],yPolys[4],xPolys[4].length);
    g.setColor(sColor2);g.drawPolygon(xPolys[4],yPolys[4],xPolys[4].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[5],yPolys[5],xPolys[5].length);
    g.setColor(sColor2);g.drawPolygon(xPolys[5],yPolys[5],xPolys[5].length);
    g.setColor(Color.black);
    g.drawLine((int)(-8.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(-8.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-6.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(-6.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-4.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(-4.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-2.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(-2.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(0.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(0.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(2.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(2.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(4.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(4.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(6.0*size+0.5)+x, (int)(-11.0*size+0.5)+y, (int)(6.0*size+0.5)+x, (int)(-17.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-13.0*size+0.5)+x, (int)(14.0*size+0.5)+y, (int)(-13.0*size+0.5)+x, (int)(5.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(12.0*size+0.5)+x, (int)(15.0*size+0.5)+y, (int)(12.0*size+0.5)+x, (int)(6.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-6.0*size+0.5)+x, (int)(7.0*size+0.5)+y, (int)(4.0*size+0.5)+x, (int)(7.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-6.0*size+0.5)+x, (int)(9.0*size+0.5)+y, (int)(4.0*size+0.5)+x, (int)(9.0*size+0.5)+y);

  }

  static public void setBoundRect(int x,int y,double size,Rectangle bound) {
    bound.setRect((int)(-20.0*size+0.5)+x,(int)(-20.0*size+0.5)+y,(int)(40.0*size+0.5),(int)(40.0*size+0.5));
  }

}
