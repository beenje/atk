package fr.esrf.tangoatk.widget.util.interlock.shape;
/* Class generated by JDraw */

import java.awt.*;

/** ---------- Device3 class ---------- */
public class Device3 {

  private static int[][] xPolys = null;
  private static int[][] yPolys = null;

  private static Color sColor0 = new Color(204,204,204);
  private static Color sColor1 = new Color(153,153,153);
  private static Color sColor2 = new Color(102,102,102);

  private static int[][] xOrgPolys = {
    {-29,-31,-31,-28,28,31,31,28},
    {-25,-25,-23,-6,-4,-4,-6,-23},
    {-3,-1,-1,-3},
    {-3,-1,-1,-3},
    {-3,-1,-1,-3},
    {-3,-1,-1,-3},
    {-28,-26,-26,-28},
    {-28,-26,-26,-28},
    {-28,-26,-26,-28},
    {-28,-26,-26,-28},
    {23,25,25,23},
    {19,21,21,19},
    {3,4,6,7,7,6,4,3},
    {10,11,13,14,14,13,11,10},
    {2,3,5,6,6,5,3,2},
    {9,10,12,13,13,12,10,9},
    {16,17,19,20,20,19,17,16},
    {23,24,26,27,27,26,24,23},
    {3,5,5,3},
    {10,12,12,10},
  };

  private static int[][] yOrgPolys = {
    {12,9,-8,-12,-12,-8,8,12},
    {6,-7,-10,-10,-7,6,9,9},
    {-9,-9,-7,-7},
    {-4,-4,-2,-2},
    {6,6,8,8},
    {1,1,3,3},
    {-9,-9,-7,-7},
    {-4,-4,-2,-2},
    {6,6,8,8},
    {1,1,3,3},
    {-9,-9,-7,-7},
    {-9,-9,-7,-7},
    {-5,-6,-6,-5,-3,-2,-2,-3},
    {-5,-6,-6,-5,-3,-2,-2,-3},
    {7,6,6,7,9,10,10,9},
    {7,6,6,7,9,10,10,9},
    {7,6,6,7,9,10,10,9},
    {7,6,6,7,9,10,10,9},
    {0,0,2,2},
    {0,0,2,2},
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
    g.setColor(backColor);g.fillPolygon(xPolys[0],yPolys[0],xPolys[0].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[0],yPolys[0],xPolys[0].length);
    g.setColor(sColor0);g.fillPolygon(xPolys[1],yPolys[1],xPolys[1].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[1],yPolys[1],xPolys[1].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[2],yPolys[2],xPolys[2].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[2],yPolys[2],xPolys[2].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[3],yPolys[3],xPolys[3].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[3],yPolys[3],xPolys[3].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[4],yPolys[4],xPolys[4].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[4],yPolys[4],xPolys[4].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[5],yPolys[5],xPolys[5].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[5],yPolys[5],xPolys[5].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[6],yPolys[6],xPolys[6].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[6],yPolys[6],xPolys[6].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[7],yPolys[7],xPolys[7].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[7],yPolys[7],xPolys[7].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[8],yPolys[8],xPolys[8].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[8],yPolys[8],xPolys[8].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[9],yPolys[9],xPolys[9].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[9],yPolys[9],xPolys[9].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[10],yPolys[10],xPolys[10].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[10],yPolys[10],xPolys[10].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[11],yPolys[11],xPolys[11].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[11],yPolys[11],xPolys[11].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[12],yPolys[12],xPolys[12].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[12],yPolys[12],xPolys[12].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[13],yPolys[13],xPolys[13].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[13],yPolys[13],xPolys[13].length);
    g.setColor(Color.black);
    g.drawLine((int)(3.0*size+0.5)+x, (int)(-9.0*size+0.5)+y, (int)(16.0*size+0.5)+x, (int)(-9.0*size+0.5)+y);
    g.setColor(sColor2);g.fillPolygon(xPolys[14],yPolys[14],xPolys[14].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[14],yPolys[14],xPolys[14].length);
    g.setColor(sColor2);g.fillPolygon(xPolys[15],yPolys[15],xPolys[15].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[15],yPolys[15],xPolys[15].length);
    g.setColor(sColor2);g.fillPolygon(xPolys[16],yPolys[16],xPolys[16].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[16],yPolys[16],xPolys[16].length);
    g.setColor(sColor2);g.fillPolygon(xPolys[17],yPolys[17],xPolys[17].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[17],yPolys[17],xPolys[17].length);
    g.setColor(Color.black);
    g.drawLine((int)(17.0*size+0.5)+x, (int)(-3.0*size+0.5)+y, (int)(19.0*size+0.5)+x, (int)(-3.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(22.0*size+0.5)+x, (int)(-3.0*size+0.5)+y, (int)(24.0*size+0.5)+x, (int)(-3.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(17.0*size+0.5)+x, (int)(-1.0*size+0.5)+y, (int)(19.0*size+0.5)+x, (int)(-1.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(22.0*size+0.5)+x, (int)(-1.0*size+0.5)+y, (int)(24.0*size+0.5)+x, (int)(-1.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(17.0*size+0.5)+x, (int)(1.0*size+0.5)+y, (int)(19.0*size+0.5)+x, (int)(1.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(22.0*size+0.5)+x, (int)(1.0*size+0.5)+y, (int)(24.0*size+0.5)+x, (int)(1.0*size+0.5)+y);
    g.setColor(sColor1);g.fillPolygon(xPolys[18],yPolys[18],xPolys[18].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[18],yPolys[18],xPolys[18].length);
    g.setColor(sColor1);g.fillPolygon(xPolys[19],yPolys[19],xPolys[19].length);
    g.setColor(Color.black);g.drawPolygon(xPolys[19],yPolys[19],xPolys[19].length);
    g.setColor(Color.black);
    g.drawLine((int)(7.0*size+0.5)+x, (int)(0.0*size+0.5)+y, (int)(7.0*size+0.5)+x, (int)(0.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(14.0*size+0.5)+x, (int)(0.0*size+0.5)+y, (int)(14.0*size+0.5)+x, (int)(0.0*size+0.5)+y);
    g.setColor(Color.black);
    g.drawLine((int)(-18.0*size+0.5)+x, (int)(10.0*size+0.5)+y, (int)(-9.0*size+0.5)+x, (int)(10.0*size+0.5)+y);

  }

  static public void setBoundRect(int x,int y,double size,Rectangle bound) {
    bound.setRect((int)(-31.0*size+0.5)+x,(int)(-12.0*size+0.5)+y,(int)(63.0*size+0.5),(int)(25.0*size+0.5));
  }

}

