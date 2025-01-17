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
 * DeviceFinder.java
 *
 * Created on June 18, 2002, 10:28 AM
 */

package fr.esrf.tangoatk.widget.util;

import javax.swing.*;
import javax.swing.tree.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.tangoatk.core.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * A panel for selecting device , attribute or command.
 */
public class DeviceFinder extends JPanel {

  /** Select device */
  public final static int MODE_DEVICE    = 0;
  /** Select all attributes */
  public final static int MODE_ATTRIBUTE = 1;
  /** Select command */
  public final static int MODE_COMMAND   = 2;
  /** Select all scalar attributes */
  public final static int MODE_ATTRIBUTE_SCALAR = 3;
  /** Select all number scalar attributes */
  public final static int MODE_ATTRIBUTE_NUMBER_SCALAR = 4;
  /** Select all boolean scalar attributes */
  public final static int MODE_ATTRIBUTE_BOOLEAN_SCALAR = 5;
  /** Select all string scalar attributes */
  public final static int MODE_ATTRIBUTE_STRING_SCALAR = 6;
  /** Select all number spectrum attributes */
  public final static int MODE_ATTRIBUTE_NUMBER_SPECTRUM = 7;
  /** Select all number scalar and boolean scalar attributes */
  public final static int MODE_ATTRIBUTE_NUMBER_BOOLEAN_SCALAR = 8;
  /** Select all number scalar and boolean scalar attributes and spectrum item */
  public final static int MODE_ATTRIBUTE_NUMBER_BOOLEAN_SPECTRUM_SCALAR = 9;
  /** Select all number scalar, boolean scalar and state scale attributes and spectrum item */
  public final static int MODE_ATTRIBUTE_NUMBER_BOOLEAN_STATE_SPECTRUM_SCALAR = 10;

  static Database  db;
  JTree            tree;
  JScrollPane      treeView;
  DefaultTreeModel treeModel;
  int              mode;

  /**
   * Construct a DeviceFinder panel using the given mode.
   * @param mode Mode
   * @see #MODE_DEVICE
   * @see #MODE_ATTRIBUTE
   * @see #MODE_COMMAND
   */
  public DeviceFinder(int mode) {

    this.mode = mode;
    try {
      db = ApiUtil.get_db_obj();
    } catch (DevFailed e) {
      ErrorPane.showErrorMessage(null,"Database",e);
      return;
    }
    setLayout(new BorderLayout());
    createTree();
    add(treeView,BorderLayout.CENTER);

  }

  /**
   * Sets the selection model for the tree
   * @see TreeSelectionModel#SINGLE_TREE_SELECTION
   * @see TreeSelectionModel#CONTIGUOUS_TREE_SELECTION
   * @see TreeSelectionModel#SINGLE_TREE_SELECTION
   * @param selectionModel The selection model
   */
  public void setSelectionModel(int selectionModel) {
    tree.getSelectionModel().setSelectionMode(selectionModel);
  }

  /**
   * @return the list of selected entities.
   */
  public String[] getSelectedNames() {

    TreePath[] p = tree.getSelectionPaths();
    Vector completePath = new Vector();

    if (p != null) {
      for (int i = 0; i < p.length; i++) {

        Object[] pth = p[i].getPath();
        Node lastNode = (Node) pth[pth.length - 1];

        if (lastNode.isLeaf()) {
          StringBuffer str = new StringBuffer();
          for (int j = 1; j < pth.length; j++) {
            str.append(pth[j]);
            if (j != pth.length - 1) str.append('/');
          }
          completePath.add(str.toString());
        }

      }
    }

    String[] ret = new String[completePath.size()];
    for (int i = 0; i < completePath.size(); i++)
      ret[i] = (String) completePath.get(i);
    return ret;

  }

  private void createTree() {

    treeModel = new DefaultTreeModel(new RootNode(mode));
    tree = new JTree(treeModel);
    tree.setEditable(false);
    tree.setCellRenderer(new TreeNodeRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setBorder(BorderFactory.createLoweredBevelBorder());
    treeView = new JScrollPane(tree);

  }

  public JTree getTree() {
    return tree;
  }

  public static void main(String[] args) {

    final DeviceFinder df = new DeviceFinder(MODE_DEVICE);
    df.setSelectionModel(TreeSelectionModel.SINGLE_TREE_SELECTION);

    JFrame f = new JFrame();
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    JButton selButton = new JButton("Select");
    selButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String[] names = df.getSelectedNames();
        System.out.println("-------------------------");
        for(int i=0;i<names.length;i++)
          System.out.println(names[i]);
      }
    });
    p.add(selButton,BorderLayout.SOUTH);
    p.add(df,BorderLayout.CENTER);
    f.setContentPane(p);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ATKGraphicsUtils.centerFrameOnScreen(f);
    f.setVisible(true);

  }

}

// ---------------------------------------------------------------

abstract class Node extends DefaultMutableTreeNode {

  private boolean areChildrenDefined = false;
  int mode;

  public int getChildCount() {
    try {
      if(!areChildrenDefined) {
        areChildrenDefined = true;
        populateNode();
      }
    } catch (DevFailed e) {
      TreeNode[] pth = getPath();
      String nodeName = "";
      for(int i=1;i<pth.length;i++) {
        if(i<pth.length-1)
          nodeName += (pth[i].toString()+"/");
        else
          nodeName += pth[i].toString();
      }
      ErrorPane.showErrorMessage(null,nodeName,e);
    }
    return super.getChildCount();
  }

  // Fill children
  abstract void populateNode() throws DevFailed;

  public boolean isLeaf() {
    return false;
  }

}

// ---------------------------------------------------------------

class RootNode extends Node {

  RootNode(int mode) {
    this.mode = mode;
  }

  void populateNode() throws DevFailed {
    String[] list = DeviceFinder.db.get_device_domain("*");
    for(int i=0;i<list.length;i++)
      add(new DomainNode(mode,list[i]));
  }

  public String toString() {
    return "RootNode";
  }

}

// ---------------------------------------------------------------

class DomainNode extends Node {

  private String domain;

  DomainNode(int mode,String domain) {
    this.domain = domain;
    this.mode = mode;
  }

  void populateNode() throws DevFailed {
    String[] list = DeviceFinder.db.get_device_family(domain+"/*");
    for(int i=0;i<list.length;i++)
      add(new FamilyNode(mode,domain,list[i]));
  }

  public String toString() {
    return domain;
  }

}

// ---------------------------------------------------------------

class FamilyNode extends Node {

  private String domain;
  private String family;

  FamilyNode(int mode,String domain,String family) {
    this.mode = mode;
    this.domain = domain;
    this.family = family;
  }

  void populateNode() throws DevFailed {
    String[] list = DeviceFinder.db.get_device_member(domain+"/"+family+"/*");
    for(int i=0;i<list.length;i++)
      add(new MemberNode(mode,domain,family,list[i]));
  }

  public String toString() {
    return family;
  }

}

// ---------------------------------------------------------------

class MemberNode extends Node {

  private String domain;
  private String family;
  private String member;

  MemberNode(int mode,String domain,String family,String member) {
    this.mode = mode;
    this.domain = domain;
    this.family = family;
    this.member = member;
  }

  void populateNode() throws DevFailed {

    if(!isLeaf()) {
      String devName = domain + "/" + family + "/" + member;
      try {
        Device ds = DeviceFactory.getInstance().getDevice(devName);
        switch(mode) {

          case DeviceFinder.MODE_ATTRIBUTE:
            String[] attList = ds.get_attribute_list();
            for(int i=0;i<attList.length;i++)
              add(new EntityNode(mode,attList[i]));
            break;

          case DeviceFinder.MODE_ATTRIBUTE_SCALAR:
              AttributeInfo[] ai = ds.get_attribute_info();
              for(int i=0;i<ai.length;i++) {
                if(ai[i].data_format.value() == AttrDataFormat._SCALAR)
                  add(new EntityNode(mode,ai[i].name));
              }
              ai = null;
              break;

          case DeviceFinder.MODE_ATTRIBUTE_BOOLEAN_SCALAR:
              AttributeInfo[] aib = ds.get_attribute_info();
              for(int i=0;i<aib.length;i++) {
                if(aib[i].data_format.value() == AttrDataFormat._SCALAR && aib[i].data_type == TangoConst.Tango_DEV_BOOLEAN)
                  add(new EntityNode(mode,aib[i].name));
              }
              aib = null;
              break;

          case DeviceFinder.MODE_ATTRIBUTE_NUMBER_SCALAR:
              AttributeInfo[] ain = ds.get_attribute_info();
              for(int i=0;i<ain.length;i++) {
                if(ain[i].data_format.value() == AttrDataFormat._SCALAR)
                  switch(ain[i].data_type)
                  {
                      case TangoConst.Tango_DEV_CHAR:
                      case TangoConst.Tango_DEV_UCHAR:
                      case TangoConst.Tango_DEV_SHORT:
                      case TangoConst.Tango_DEV_USHORT:
                      case TangoConst.Tango_DEV_LONG:
                      case TangoConst.Tango_DEV_ULONG:
                      case TangoConst.Tango_DEV_LONG64:
                      case TangoConst.Tango_DEV_ULONG64:
                      case TangoConst.Tango_DEV_FLOAT:
                      case TangoConst.Tango_DEV_DOUBLE:
                          add(new EntityNode(mode,ain[i].name));
                          break;
                  }
              }
              ain = null;
              break;

          case DeviceFinder.MODE_ATTRIBUTE_NUMBER_SPECTRUM:
              AttributeInfo[] aisp = ds.get_attribute_info();
              for(int i=0;i<aisp.length;i++) {
                if(aisp[i].data_format.value() == AttrDataFormat._SPECTRUM)
                  switch(aisp[i].data_type)
                  {
                      case TangoConst.Tango_DEV_CHAR:
                      case TangoConst.Tango_DEV_UCHAR:
                      case TangoConst.Tango_DEV_SHORT:
                      case TangoConst.Tango_DEV_USHORT:
                      case TangoConst.Tango_DEV_LONG:
                      case TangoConst.Tango_DEV_ULONG:
                      case TangoConst.Tango_DEV_LONG64:
                      case TangoConst.Tango_DEV_ULONG64:
                      case TangoConst.Tango_DEV_FLOAT:
                      case TangoConst.Tango_DEV_DOUBLE:
                          add(new EntityNode(mode,aisp[i].name));
                          break;
                  }
              }
              ain = null;
              break;

          case DeviceFinder.MODE_ATTRIBUTE_STRING_SCALAR:
            AttributeInfo[] ais = ds.get_attribute_info();
            for(int i=0;i<ais.length;i++) {
              if(ais[i].data_format.value() == AttrDataFormat._SCALAR && ais[i].data_type == TangoConst.Tango_DEV_STRING)
                add(new EntityNode(mode,ais[i].name));
            }
            ais = null;
            break;

          case DeviceFinder.MODE_COMMAND:
            CommandInfo[] cmdList = ds.command_list_query();
            for(int i=0;i<cmdList.length;i++)
              add(new EntityNode(mode,cmdList[i].cmd_name));
            break;

          case DeviceFinder.MODE_ATTRIBUTE_NUMBER_BOOLEAN_SCALAR:
            AttributeInfo[] ainb = ds.get_attribute_info();
            for(int i=0;i<ainb.length;i++) {
              // Add boolean
              if(ainb[i].data_format.value() == AttrDataFormat._SCALAR && ainb[i].data_type == TangoConst.Tango_DEV_BOOLEAN)
                add(new EntityNode(mode,ainb[i].name));

              // Add number scalar
              if(ainb[i].data_format.value() == AttrDataFormat._SCALAR)
                switch(ainb[i].data_type)
                {
                    case TangoConst.Tango_DEV_CHAR:
                    case TangoConst.Tango_DEV_UCHAR:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_LONG64:
                    case TangoConst.Tango_DEV_ULONG64:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_DOUBLE:
                        add(new EntityNode(mode,ainb[i].name));
                        break;
                }
            }
            ainb = null;
            break;

          case DeviceFinder.MODE_ATTRIBUTE_NUMBER_BOOLEAN_SPECTRUM_SCALAR:
            AttributeInfo[] ainbs = ds.get_attribute_info();
            for(int i=0;i<ainbs.length;i++) {

              // Add number scalar
              if(ainbs[i].data_format.value() == AttrDataFormat._SCALAR)
                switch(ainbs[i].data_type)
                {
                    case TangoConst.Tango_DEV_CHAR:
                    case TangoConst.Tango_DEV_UCHAR:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_LONG64:
                    case TangoConst.Tango_DEV_ULONG64:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_DOUBLE:
                    case TangoConst.Tango_DEV_BOOLEAN:
                        add(new EntityNode(mode,ainbs[i].name));
                        break;
                }

              // Add spectrum (each item seen as scalar)
              if(ainbs[i].data_format.value() == AttrDataFormat._SPECTRUM)
                switch(ainbs[i].data_type)
                {
                    case TangoConst.Tango_DEV_CHAR:
                    case TangoConst.Tango_DEV_UCHAR:
                    case TangoConst.Tango_DEV_SHORT:
                    case TangoConst.Tango_DEV_USHORT:
                    case TangoConst.Tango_DEV_LONG64:
                    case TangoConst.Tango_DEV_ULONG64:
                    case TangoConst.Tango_DEV_LONG:
                    case TangoConst.Tango_DEV_ULONG:
                    case TangoConst.Tango_DEV_FLOAT:
                    case TangoConst.Tango_DEV_DOUBLE:
                      int maxx = ainbs[i].max_dim_x;
                      if(maxx>1024) maxx = 1024;
                      add(new SpectrumItemNode(mode,ainbs[i].name,maxx));
                      break;
                }


            }
            ainbs = null;
            break;

          case DeviceFinder.MODE_ATTRIBUTE_NUMBER_BOOLEAN_STATE_SPECTRUM_SCALAR:
            AttributeInfo[] ainbss = ds.get_attribute_info();
            for(int i=0;i<ainbss.length;i++) {

              // Add number scalar
              if(ainbss[i].data_format.value() == AttrDataFormat._SCALAR)
                switch(ainbss[i].data_type)
                {
                  case TangoConst.Tango_DEV_CHAR:
                  case TangoConst.Tango_DEV_UCHAR:
                  case TangoConst.Tango_DEV_SHORT:
                  case TangoConst.Tango_DEV_USHORT:
                  case TangoConst.Tango_DEV_LONG:
                  case TangoConst.Tango_DEV_ULONG:
                  case TangoConst.Tango_DEV_LONG64:
                  case TangoConst.Tango_DEV_ULONG64:
                  case TangoConst.Tango_DEV_FLOAT:
                  case TangoConst.Tango_DEV_DOUBLE:
                  case TangoConst.Tango_DEV_BOOLEAN:
                  case TangoConst.Tango_DEV_STATE:
                    add(new EntityNode(mode,ainbss[i].name));
                    break;
                }

              // Add spectrum (each item seen as scalar)
              if(ainbss[i].data_format.value() == AttrDataFormat._SPECTRUM)
                switch(ainbss[i].data_type)
                {
                  case TangoConst.Tango_DEV_CHAR:
                  case TangoConst.Tango_DEV_UCHAR:
                  case TangoConst.Tango_DEV_SHORT:
                  case TangoConst.Tango_DEV_USHORT:
                  case TangoConst.Tango_DEV_LONG:
                  case TangoConst.Tango_DEV_ULONG:
                  case TangoConst.Tango_DEV_LONG64:
                  case TangoConst.Tango_DEV_ULONG64:
                  case TangoConst.Tango_DEV_FLOAT:
                  case TangoConst.Tango_DEV_DOUBLE:
                    int maxx = ainbss[i].max_dim_x;
                    if(maxx>1024) maxx = 1024;
                    add(new SpectrumItemNode(mode,ainbss[i].name,maxx));
                    break;
                }


            }
            ainbss = null;
            break;

        }
      } catch (ConnectionException e) {
        ErrorPane.showErrorMessage(null,devName,e);
      }
    }

  }

  public boolean isLeaf() {
    return (mode == DeviceFinder.MODE_DEVICE);
  }

  public String toString() {
    return member;
  }

}

// ---------------------------------------------------------------

class SpectrumItemNode extends Node {

  private String attName;
  private int itemNumber;

  SpectrumItemNode(int mode,String attribute,int itemNumber) {
    this.mode = mode;
    this.attName = attribute;
    this.itemNumber = itemNumber;
  }

  void populateNode() throws DevFailed {
    for(int i=0;i<itemNumber;i++) {
      add(new EntityNode(mode,Integer.toString(i)));
    }
  }

  public boolean isLeaf() {
    return false;
  }

  public String toString() {
    return attName;
  }

}

// ---------------------------------------------------------------

class EntityNode extends Node {

  private String entitytName;

  EntityNode(int mode,String entitytName) {
    this.entitytName = entitytName;
    this.mode = mode;
  }

  void populateNode() throws DevFailed {}

  public boolean isLeaf() {
    return true;
  }

  public String toString() {
    return entitytName;
  }

}

// ---------------------------------------------------------------

class TreeNodeRenderer extends DefaultTreeCellRenderer {

  ImageIcon devicon;
  ImageIcon cmdicon;
  ImageIcon atticon;

  public TreeNodeRenderer() {
    devicon = new ImageIcon(getClass().getResource("/fr/esrf/tangoatk/widget/util/device.gif"));
    cmdicon = new ImageIcon(getClass().getResource("/fr/esrf/tangoatk/widget/util/command.gif"));
    atticon = new ImageIcon(getClass().getResource("/fr/esrf/tangoatk/widget/util/attribute.gif"));
  }

  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    super.getTreeCellRendererComponent(
        tree, value, sel,
        expanded, leaf, row,
        hasFocus);

    // Device Icon
    if (value instanceof MemberNode) {
      setIcon(devicon);
      return this;
    }

    if (value instanceof EntityNode) {
      switch (((Node) value).mode) {
        case DeviceFinder.MODE_COMMAND:
          setIcon(cmdicon);
          break;
        case DeviceFinder.MODE_ATTRIBUTE:
        case DeviceFinder.MODE_ATTRIBUTE_SCALAR:
        case DeviceFinder.MODE_ATTRIBUTE_BOOLEAN_SCALAR:
        case DeviceFinder.MODE_ATTRIBUTE_NUMBER_SCALAR:
        case DeviceFinder.MODE_ATTRIBUTE_NUMBER_SPECTRUM:
        case DeviceFinder.MODE_ATTRIBUTE_STRING_SCALAR:
        case DeviceFinder.MODE_ATTRIBUTE_NUMBER_BOOLEAN_SCALAR:
        case DeviceFinder.MODE_ATTRIBUTE_NUMBER_BOOLEAN_SPECTRUM_SCALAR:
          setIcon(atticon);
          break;
      }
    }

    return this;
  }

}
