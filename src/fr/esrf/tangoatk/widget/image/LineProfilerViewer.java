package fr.esrf.tangoatk.widget.image;

import fr.esrf.tangoatk.widget.util.chart.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A Class to display a line profile
 * @author pons
 */

public class LineProfilerViewer extends JFrame implements IJLChartListener, ActionListener {

  JPanel cfgPanel;
  JLChart theGraph;
  JLDataView theDataX;
  JLDataView theDataY;
  JTable theTable = null;
  JScrollPane tableView;
  DefaultTableModel dm = null;
  Font panelFont;

  JCheckBox tableCheck;
  JLabel minLabel;
  JTextField minText;
  JLabel maxLabel;
  JTextField maxText;

  static String[]   colName  = {"Index", "Value"};
  static String[][] emptyStr = {{"", ""}};

  public LineProfilerViewer() {

    Container pane = getContentPane();

    pane.setLayout(new BorderLayout());

    // -----------------------------------------------
    // Cfg panel
    // -----------------------------------------------
    panelFont = new Font("Dialog", Font.PLAIN, 11);

    cfgPanel = new JPanel();
    cfgPanel.setLayout(null);
    cfgPanel.setPreferredSize(new Dimension(0, 25));
    pane.add(cfgPanel, BorderLayout.SOUTH);

    tableCheck = new JCheckBox("View table");
    tableCheck.setSelected(false);
    tableCheck.setFont(panelFont);
    tableCheck.setBounds(5, 3, 80, 20);
    tableCheck.addActionListener(this);
    cfgPanel.add(tableCheck);

    minLabel = new JLabel("Minimum");
    minLabel.setFont(panelFont);
    minLabel.setHorizontalAlignment(JLabel.RIGHT);
    minLabel.setBounds(85, 3, 70, 20);
    cfgPanel.add(minLabel);
    minText = new JTextField("");
    minText.setFont(panelFont);
    minText.setBounds(155, 3, 60, 20);
    cfgPanel.add(minText);

    maxLabel = new JLabel("Maximum");
    maxLabel.setFont(panelFont);
    maxLabel.setHorizontalAlignment(JLabel.RIGHT);
    maxLabel.setBounds(215, 3, 70, 20);
    cfgPanel.add(maxLabel);
    maxText = new JTextField("");
    maxText.setFont(panelFont);
    maxText.setBounds(285, 3, 60, 20);
    cfgPanel.add(maxText);

    // -----------------------------------------------
    // Graph
    // -----------------------------------------------

    theGraph = new JLChart();
    theGraph.setBorder(new javax.swing.border.EtchedBorder());
    theGraph.setChartBackground(new java.awt.Color(160, 160, 160));
    theGraph.getY1Axis().setAutoScale(true);
    theGraph.getY2Axis().setAutoScale(true);
    theGraph.getXAxis().setAutoScale(true);
    theDataX = new JLDataView();
    theDataY = new JLDataView();
    theGraph.getY1Axis().addDataView(theDataY);
    theGraph.getY1Axis().setGridVisible(true);
    theGraph.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
    theGraph.getXAxis().setGridVisible(true);
    theGraph.getXAxis().addDataView(theDataX);
    theGraph.getXAxis().setLabelFormat(JLAxis.DECINT_FORMAT);
    theGraph.setPreferredSize(new Dimension(640, 480));
    theGraph.setMinimumSize(new Dimension(640, 480));
    theGraph.setHeaderFont(new Font("Dialog",Font.BOLD,18));
    theGraph.setJLChartListener(this);
    pane.add(theGraph, BorderLayout.CENTER);

    dm = new DefaultTableModel() {
      public Class getColumnClass(int columnIndex) {
        return String.class;
      }
    };

    theTable = new JTable();
    dm.setDataVector(emptyStr, colName);
    theTable.setModel(dm);
    tableView = new JScrollPane(theTable);
    tableView.setPreferredSize(new Dimension(160, 0));
    tableView.setVisible(false);
    pane.add(tableView, BorderLayout.EAST);

    pack();
  }

  public void setLineProfileMode() {
    setTitle("[profile] ImageViewer");
    theGraph.setHeader("Line profile");
    theGraph.getXAxis().setName("Pixel index");
    theDataX.setName("Pixel index");
    theGraph.getY1Axis().setName("Value");
    theDataY.setName("Pixel value");
    if(theGraph.isZoomed()) theGraph.exitZoom();
  }

  public void setHistogramMode() {
    setTitle("[Histogram] ImageViewer");
    theGraph.setHeader("Histogram");
    theGraph.getXAxis().setName("Pixel value");
    theDataX.setName("Pixel value");
    theGraph.getY1Axis().setName("Number");
    theDataY.setName("pixel number");
    if(theGraph.isZoomed()) theGraph.exitZoom();
  }

  private void refreshTable() {

    if (tableView.isVisible()) {

	  colName[0] = theGraph.getXAxis().getName();
	  colName[1] = theGraph.getY1Axis().getName();

      String[][] dv;
	  if( theDataY.getDataLength() > 0 ) {
        dv = new String[theDataY.getDataLength()][2];
        DataList dlx = theDataX.getData();
        DataList dly = theDataY.getData();

        for (int i = 0; i < theDataY.getDataLength(); i++) {
          dv[i][0] = Double.toString(dlx.y);
          dv[i][1] = Double.toString(dly.y);
          dlx = dlx.next;
          dly = dly.next;
		}
	  } else {
        dv = emptyStr;
	  }
      dm.setDataVector(dv, colName);
      theTable.repaint();
    }

  }

  public void setData(double[] v) {
    setData(v,0);
  }

  public void setData(double[] v,int startIndexing) {
    
	theDataX.reset();
    theDataY.reset();

	if( v!=null ) {

      for (int i = 0; i < v.length; i++) {
        theDataX.add(i, (double)(i+startIndexing));
        theDataY.add(i, v[i]);
      }
      minText.setText(Double.toString(theDataY.getMinimum()));
      maxText.setText(Double.toString(theDataY.getMaximum()));
	} else {
	  minText.setText("");
	  maxText.setText("");
	}

    theGraph.repaint();
    refreshTable();
  }

  // -------------------------------------------------------------
  // Action listener
  // -------------------------------------------------------------
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == tableCheck) {
      tableView.setVisible(tableCheck.isSelected());
      refreshTable();
      JPanel jp = (JPanel)getContentPane();
      jp.revalidate();
    }
  }

  // -------------------------------------------------------------
  // Chart listener
  // -------------------------------------------------------------
  public String[] clickOnChart(JLChartEvent evt) {

    String[] ret = new String[2];
    ret[0] = theDataX.getName() + " = " + evt.getTransformedXValue();
    ret[1] = theDataY.getName() + " = " + evt.getTransformedYValue();

    return ret;
  }


  public static void main(String[] args) {
    final LineProfilerViewer l = new LineProfilerViewer();
    l.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    l.setVisible(true);
  }

}