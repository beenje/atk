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
 
/**
 * JDraw Image graphic object
 */
package fr.esrf.tangoatk.widget.util.jdraw;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

/** JDraw Image graphic object. JDraw supports GIF, JPG and PNG format. Alpha mask is supported for
    PNG image. */
public class JDImage extends JDRectangular {

  // Vars
  static private ImageIcon defaultImage = new ImageIcon(JDImage.class.getResource("/fr/esrf/tangoatk/widget/util/jdraw/gif/not_found.gif"));
  private Image theImage = null;
  private int imgWidth = 0;
  private int imgHeight = 0;
  private String fileName;

  /**
   * Contruct an image.
   * @param objectName Image name
   * @param fileName Image file name.
   * @param x Up left corner x coordinate
   * @param y Up left corner y coordinate
   */
  public JDImage(String objectName, String fileName, int x, int y) {
    initDefault();
    setOrigin(new Point.Double(0.0, 0.0));
    summit = new Point.Double[8];
    name = objectName;
    this.fileName = fileName;
    lineWidth = 0;
    createSummit();
    loadImage();
    computeSummitCoordinates(x,y,imgWidth,imgHeight);
    updateShape();
    centerOrigin();

  }

  JDImage(JDImage e, int x, int y) {
    cloneObject(e, x, y);
    fileName = new String(e.fileName);
    invalidateImage();
    updateShape();
  }

  JDImage(JLXObject jlxObj,String fileName) {

    initDefault();
    loadObject(jlxObj);

    double x = jlxObj.boundRect.getX();
    double y = jlxObj.boundRect.getY();
    double w = jlxObj.boundRect.getWidth();
    double h = jlxObj.boundRect.getHeight();

    setOrigin(new Point2D.Double(x+w/2.0, y+h/2.0));
    summit = new Point2D.Double[8];
    createSummit();

    if(fileName.startsWith("file:/"))
      fileName = fileName.substring(6);
    this.fileName = fileName;
    lineWidth = 0;
    fillStyle = FILL_STYLE_NONE;

    summit[0].x = x;
    summit[0].y = y;

    summit[1].x = x+w/2;
    summit[1].y = y;

    summit[2].x = x+w;
    summit[2].y = y;

    summit[3].x = x+w;
    summit[3].y = y+h/2;

    summit[4].x = x+w;
    summit[4].y = y+h;

    summit[5].x = x+w/2;
    summit[5].y = y+h;

    summit[6].x = x;
    summit[6].y = y+h;

    summit[7].x = x;
    summit[7].y = y+h/2;

    loadImage();

    updateShape();

  }

  JDImage(LXObject lxObj,String fileName) {

    initDefault();
    loadObject(lxObj);

    double x = lxObj.boundRect.getX();
    double y = lxObj.boundRect.getY();
    double w = lxObj.boundRect.getWidth()-1;
    double h = lxObj.boundRect.getHeight()-1;

    setOrigin(new Point2D.Double(x+w/2.0, y+h/2.0));
    summit = new Point2D.Double[8];
    createSummit();

    if(fileName.startsWith("file:/"))
      fileName = fileName.substring(6);
    this.fileName = fileName;
    lineWidth = 0;
    fillStyle = FILL_STYLE_NONE;

    summit[0].x = x;
    summit[0].y = y;

    summit[1].x = x+w/2;
    summit[1].y = y;

    summit[2].x = x+w;
    summit[2].y = y;

    summit[3].x = x+w;
    summit[3].y = y+h/2;

    summit[4].x = x+w;
    summit[4].y = y+h;

    summit[5].x = x+w/2;
    summit[5].y = y+h;

    summit[6].x = x;
    summit[6].y = y+h;

    summit[7].x = x;
    summit[7].y = y+h/2;

    loadImage();

    updateShape();

  }

  // -----------------------------------------------------------
  // Overrides
  // -----------------------------------------------------------
  public JDObject copy(int x, int y) {
    return new JDImage(this, x, y);
  }

  public void paint(JDrawEditor parent,Graphics g) {

    if (!visible) return;
    prepareRendering((Graphics2D)g);
    super.paint(parent,g);
    loadImage();
    g.drawImage(theImage,boundRect.x, boundRect.y, boundRect.width, boundRect.height,null);

  }

  void updateShape() {
    computeBoundRect();

    // Update shadow coordinates
    ptsx = new int[4];
    ptsy = new int[4];

    if (summit[0].x < summit[2].x) {
      if (summit[0].y < summit[6].y) {
        ptsx[0] = (int) summit[0].x;
        ptsy[0] = (int) summit[0].y;
        ptsx[1] = (int) summit[2].x + 1;
        ptsy[1] = (int) summit[2].y;
        ptsx[2] = (int) summit[4].x + 1;
        ptsy[2] = (int) summit[4].y + 1;
        ptsx[3] = (int) summit[6].x;
        ptsy[3] = (int) summit[6].y + 1;
      } else {
        ptsx[0] = (int) summit[6].x;
        ptsy[0] = (int) summit[6].y;
        ptsx[1] = (int) summit[4].x + 1;
        ptsy[1] = (int) summit[4].y;
        ptsx[2] = (int) summit[2].x + 1;
        ptsy[2] = (int) summit[2].y + 1;
        ptsx[3] = (int) summit[0].x;
        ptsy[3] = (int) summit[0].y + 1;
      }
    } else {
      if (summit[0].y < summit[6].y) {
        ptsx[0] = (int) summit[2].x;
        ptsy[0] = (int) summit[2].y;
        ptsx[1] = (int) summit[0].x + 1;
        ptsy[1] = (int) summit[0].y;
        ptsx[2] = (int) summit[6].x + 1;
        ptsy[2] = (int) summit[6].y + 1;
        ptsx[3] = (int) summit[4].x;
        ptsy[3] = (int) summit[4].y + 1;
      } else {
        ptsx[0] = (int) summit[4].x;
        ptsy[0] = (int) summit[4].y;
        ptsx[1] = (int) summit[6].x + 1;
        ptsy[1] = (int) summit[6].y;
        ptsx[2] = (int) summit[0].x + 1;
        ptsy[2] = (int) summit[0].y + 1;
        ptsx[3] = (int) summit[2].x;
        ptsy[3] = (int) summit[2].y + 1;
      }
    }

  }

  /** @return false, Image cannot be shadowed. */
  public boolean hasShadow() {
    return false;
  }

  // -----------------------------------------------------------
  // Property stuff
  // -----------------------------------------------------------

  /**
   * Loads a new image into this object.
   * @param fileName Image file name.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
    invalidateImage();
    loadImage();
    computeSummitCoordinates((int)summit[0].x, (int)summit[0].y, imgWidth,imgHeight);
    updateShape();
    centerOrigin();
  }

  /** @return current image filename. */
  public String getFileName() {
    return fileName;
  }

  /** Reset the size to its original size. */
  public void resetToOriginalSize() {
    computeSummitCoordinates((int)summit[0].x, (int)summit[0].y, imgWidth,imgHeight);
    updateShape();
    centerOrigin();
  }

  /**
   * @return original image width.
   */
  public int getImageWidth() {
    return imgWidth;
  }

  /**
   * @return original image height.
   */
  public int getImageHeight() {
    return imgHeight;
  }

  // -----------------------------------------------------------
  // File management
  // -----------------------------------------------------------
  void recordObject(StringBuffer to_write, int level) {

    StringBuffer decal = recordObjectHeader(to_write, level);
    to_write.append(decal).append("file_name:\"").append(fileName).append("\"\n");
    closeObjectHeader(to_write, level);

  }

  JDImage(JDFileLoader f) throws IOException {

    initDefault();
    f.startBlock();
    summit = f.parseRectangularSummitArray();

    while (!f.isEndBlock()) {
      String propName = f.parseProperyName();
      if (propName.equals("file_name")) {
        fileName = f.parseString();
      } else
        loadDefaultPropery(f, propName);
    }

    f.endBlock();

    invalidateImage();
    updateShape();
  }

  // -----------------------------------------------------------
  // Undo buffer
  // -----------------------------------------------------------
  UndoPattern getUndoPattern() {

    UndoPattern u = new UndoPattern(UndoPattern._JDImage);
    fillUndoPattern(u);
    u.fileName = new String(fileName);

    return u;
  }

  JDImage(UndoPattern e) {

    initDefault();
    applyUndoPattern(e);
    fileName = e.fileName;
    invalidateImage();
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

    summit[2].x = x + width-1;
    summit[2].y = y;

    summit[4].x = x + width-1;
    summit[4].y = y + height-1;

    summit[6].x = x;
    summit[6].y = y + height-1;

    centerSummit();

  }


  private void invalidateImage() {
    theImage=null;
  }

  private void loadImage() {

    if (theImage == null) {

      try {

        if( fileName.startsWith("jar:")) {
          // Load from resource
          InputStream stream = getClass().getResourceAsStream(fileName.substring(4));

          if(stream==null) {

            if( getParent()==null ) {

              // should not happen
              theImage = defaultImage.getImage();
              imgWidth = defaultImage.getIconWidth();
              imgHeight = defaultImage.getIconHeight();
              System.out.println("JDImage.setParent() Warning " + fileName + " load failed : parent is null");
              return;

            }

            // Try to load from disk using the root paths
            boolean loaded = false;

            String[] paths = getParent().getRootPaths();

            if( paths!=null ) {

              String relPath = fileName.substring(4);
              if(relPath.startsWith("/")) relPath = relPath.substring(1);

              int i = 0;

              while(!loaded && i<paths.length) {

                File in = new File(paths[i]+relPath);
                if( in.exists() ) {
                  try {
                    theImage = ImageIO.read(in);
                    imgWidth = ((BufferedImage)theImage).getWidth();
                    imgHeight = ((BufferedImage)theImage).getHeight();
                    updateShape();
                    loaded = true;
                  } catch(IOException e) {
                  }
                }

                i++;

              }

            }

            if(!loaded) {

              theImage = defaultImage.getImage();
              imgWidth = defaultImage.getIconWidth();
              imgHeight = defaultImage.getIconHeight();
              System.out.println("JDImage.setParent() Warning " + fileName + " load failed : resource not found");

            }

          } else {
            // Resource has been found
            theImage = ImageIO.read(stream);
            imgWidth = ((BufferedImage)theImage).getWidth();
            imgHeight = ((BufferedImage)theImage).getHeight();
          }

        } else {
          // Load form disk
          File in = new File(fileName);
          theImage = ImageIO.read(in);
          imgWidth = ((BufferedImage)theImage).getWidth();
          imgHeight = ((BufferedImage)theImage).getHeight();
        }

      } catch (IOException e) {
        // Load failure
        System.out.println("JDImage.loadImage() Warning " + fileName + " load failed : " + e.getMessage());
        theImage = defaultImage.getImage();
        imgWidth = defaultImage.getIconWidth();
        imgHeight = defaultImage.getIconHeight();
      }

    }

  }

}