package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Calculates Chapter 9-5 "Measures Of Variation" Glencoe MAC 3 homework problems which ask for range, median, upper and lower quartiles, interquartile range, and outliers.
 * 
 * This is a program I made in 6th grade to calculate my "Measures of Variation" homework.
 * 
 * @author Arka Majumdar
 */
public class MeasuresOfVariation {
	JTextField num = new JTextField(9);
	JTextField showRange, showMedian, showUq, showLq, showIq, showOut;
	JLabel showOutLimits;
	JSpinner places;
	SaveInfo s = new SaveInfo();
	JFileChooser files;
	int calcNum = 0;

	public MeasuresOfVariation() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}
		setFieldsUneditable();
		places = new JSpinner(new SpinnerNumberModel(2, 0, null, 1));
		files = new JFileChooser();
		files.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// Panel & Controls
		JPanel windowContent = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JPanel dataSetPanel = new JPanel(new BorderLayout());
		JPanel enter = new JPanel(new BorderLayout(5, 0));
		final JList<String> dataSet = new JList<String>(new DefaultListModel<String>());
		AbstractAction enterAction = new AbstractAction("Enter") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				String noChar = num.getText().replaceAll("[^0-9 && [^\\.]]", "").replaceAll(" ", "");
				if (noChar.trim() != null && !noChar.trim().equals("")) {
					((DefaultListModel<String>) dataSet.getModel()).addElement(noChar.trim());
				}
				num.setText("");
			}
		};
		num.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), enterAction);
		enter.add(num, BorderLayout.CENTER);
		enter.add(new JButton(enterAction), BorderLayout.EAST);
		enter.setBackground(num.getBackground());
		enter.setBorder(num.getBorder());
		num.setBorder(BorderFactory.createEmptyBorder());
		dataSetPanel.add(enter, BorderLayout.NORTH);
		dataSet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dataSet.setLayoutOrientation(JList.VERTICAL);
		dataSetPanel.add(new JScrollPane(dataSet), BorderLayout.CENTER);
		final JPanel calcBtn = new JPanel(new BorderLayout());
		calcBtn.add(new JButton(new AbstractAction("Calculate") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				dataSet.setModel(leastToGreatest((DefaultListModel<String>) dataSet.getModel()));
				String[] list = new String[dataSet.getModel().getSize()];
				Collections.list(((DefaultListModel<String>) dataSet.getModel()).elements()).toArray(list);
				try {
					((DefaultListModel<String>) dataSet.getModel()).firstElement();
					((DefaultListModel<String>) dataSet.getModel()).lastElement();
				} catch (NoSuchElementException nsee) {
					JOptionPane.showMessageDialog(null, "We found no first or last element in the list of numbers.\nThis probably means you have not entered any elements in the list or have forgot to press the \"Enter\" button.", "Uh-oh!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				showRange.setText("" + round(range((DefaultListModel<String>) dataSet.getModel()), (Integer) places.getValue()) + "");
				showMedian.setText("" + round(median(list), (Integer) places.getValue()) + "");
				showUq.setText("" + round(uq(list), (Integer) places.getValue()) + "");
				showLq.setText("" + round(lq(list), (Integer) places.getValue()) + "");
				showIq.setText("" + round(iq(list), (Integer) places.getValue()) + "");
				calcNum++;
				showRange.setColumns(showRange.getText().length());
				showMedian.setColumns(showMedian.getText().length());
				showUq.setColumns(showUq.getText().length());
				showLq.setColumns(showLq.getText().length());
				showIq.setColumns(showIq.getText().length());
				((JButton) evt.getSource()).setText("Calculate (Sorted!)");
				if (s.always) {
					if (calcNum != 1) {
						s.writer.println();
					}
					s.writer.println("Calculation " + calcNum + ":");
					String dataStr = "";
					for (Object str : Collections.list(((DefaultListModel<String>) dataSet.getModel()).elements()).toArray()) {
						dataStr = dataStr + str.toString();
						if (!((DefaultListModel<String>) dataSet.getModel()).lastElement().equals(str)) {
							dataStr = dataStr + ", ";
						}
					}
					s.writer.println("    Data: " + dataStr);
					s.writer.println("        Range : " + showRange.getText());
					s.writer.println("        Median: " + showMedian.getText());
					s.writer.println("        UQ    : " + showUq.getText());
					s.writer.println("        LQ    : " + showLq.getText());
					s.writer.println("        InterQ: " + showIq.getText());
				}
				if (s.writer != null) {
					s.writer.flush();
				}
			}
		}), BorderLayout.CENTER);
		Box round = Box.createHorizontalBox();
		round.add(new JLabel("Decimals to show:"));
		round.add(places);
		calcBtn.add(round, BorderLayout.EAST);
		JButton save = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				if (files.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					try {
						s.writer = new PrintWriter(new FileWriter(files.getSelectedFile().getPath()));
					} catch (IOException e) {
						JOptionPane.showOptionDialog(null, "We couldn't create a writer to write the data. If trying again doesn't work show me the error and I will try to fix it.", "Oh-no!", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
					}
					if (calcNum != 1) {
						s.writer.println();
					}
					s.writer.println("Calculation " + calcNum + ":");
					String dataStr = "";
					for (Object str : Collections.list(((DefaultListModel<String>) dataSet.getModel()).elements()).toArray()) {
						dataStr = dataStr + str.toString();
						if (!((DefaultListModel<String>) dataSet.getModel()).lastElement().equals(str)) {
							dataStr = dataStr + ", ";
						}
					}
					s.writer.println("    Data: " + dataStr);
					s.writer.println("        Range : " + showRange.getText());
					s.writer.println("        Median: " + showMedian.getText());
					s.writer.println("        UQ    : " + showUq.getText());
					s.writer.println("        LQ    : " + showLq.getText());
					s.writer.println("        InterQ: " + showIq.getText());
				}
			}
		});
		save.setLayout(new BorderLayout());
		save.add(new JLabel("<html><b>Save Data</b></html>", JLabel.CENTER), BorderLayout.CENTER);
		save.add(new JCheckBox(new AbstractAction("Always") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				s.always = ((JCheckBox) evt.getSource()).isSelected();
				if (((JCheckBox) evt.getSource()).isSelected()) {
					if (files.showDialog(null, "Save Here Always") == JFileChooser.APPROVE_OPTION) {
						try {
							s.writer = new PrintWriter(new FileWriter(files.getSelectedFile().getPath()));
						} catch (IOException e) {
							JOptionPane.showOptionDialog(null, "We couldn't create a writer to write the data. If trying again doesn't work show me the error and I will try to fix it.", "Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
							((JCheckBox) evt.getSource()).setSelected(false);
							s.always = false;
						}
					} else {
						((JCheckBox) evt.getSource()).setSelected(false);
						s.always = false;
					}
				}
			}
		}), BorderLayout.EAST);
		calcBtn.add(save, BorderLayout.SOUTH);
		dataSetPanel.add(calcBtn, BorderLayout.SOUTH);
		JPanel clears = new JPanel(new GridLayout(0, 1));
		JButton clearLast = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					((DefaultListModel<String>) dataSet.getModel()).removeElement(((DefaultListModel<String>) dataSet.getModel()).lastElement());
				} catch (NoSuchElementException nsee) {
					JOptionPane.showMessageDialog(null, "We found no last item to clear", "Uh-oh!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		clearLast.setLayout(new GridLayout(0, 1));
		clearLast.add(new JLabel("<html><b>Clear</b></html>", JLabel.CENTER));
		clearLast.add(new JLabel("<html><b>Last</b></html>", JLabel.CENTER));
		clearLast.add(new JLabel("<html><b>Entry</b></html>", JLabel.CENTER));
		clearLast.setBackground(new Color(102, 51, 0));
		clears.add(clearLast);
		JButton clearSelect = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					((DefaultListModel<String>) dataSet.getModel()).removeElement(((DefaultListModel<String>) dataSet.getModel()).remove(dataSet.getMinSelectionIndex()));
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					JOptionPane.showMessageDialog(null, "We found no selected item to clear", "Uh-oh!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		clearSelect.setLayout(new GridLayout(0, 1));
		clearSelect.add(new JLabel("<html><b>Clear</b></html>", JLabel.CENTER));
		clearSelect.add(new JLabel("<html><b>Selected</b></html>", JLabel.CENTER));
		clearSelect.setBackground(Color.BLUE.darker());
		clears.add(clearSelect);
		JButton clear = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				((DefaultListModel<String>) dataSet.getModel()).clear();
				((JButton) calcBtn.getComponent(0)).setText("Calculate");
				showRange.setText("                    ");
				showMedian.setText("                    ");
				showLq.setText("                    ");
				showUq.setText("                    ");
				showIq.setText("                    ");
			}
		});
		clear.setLayout(new GridLayout(0, 1));
		clear.add(new JLabel("<html><b>Clear</b></html>", JLabel.CENTER));
		clear.add(new JLabel("<html><b>All</b></html>", JLabel.CENTER));
		clear.setBackground(Color.RED.darker());
		clears.add(clear);
		dataSetPanel.add(clears, BorderLayout.WEST);
		dataSetPanel.setBorder(BorderFactory.createTitledBorder("Data Set"));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 6;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(dataSetPanel, c);
		JPanel rangePanel = new JPanel();
		rangePanel.setBorder(BorderFactory.createTitledBorder("Range"));
		rangePanel.add(showRange);
		// rangePanel.setPreferredSize(new Dimension(100, 50)); //Set initial size
		// rangePanel.setMinimumSize(new Dimension(100, 50)); //Set minimum size
		// rangePanel.setSize(100, 50); //Lock in place
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL; // Lock in place
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(rangePanel, c);
		JPanel medianPanel = new JPanel();
		medianPanel.setBorder(BorderFactory.createTitledBorder("Median"));
		medianPanel.add(showMedian);
		// medianPanel.setPreferredSize(new Dimension(100, 50)); //Set initial size
		// medianPanel.setMinimumSize(new Dimension(100, 50)); //Set minimum size
		// medianPanel.setSize(100, 50); //Lock in place
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL; // Lock in place
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(medianPanel, c);
		JPanel uqPanel = new JPanel();
		uqPanel.setBorder(BorderFactory.createTitledBorder("Upper Quartile"));
		uqPanel.add(showUq);
		// uqPanel.setPreferredSize(new Dimension(100, 50)); //Set initial size
		// uqPanel.setMinimumSize(new Dimension(100, 50)); //Set minimum size
		// uqPanel.setSize(100, 50); //Lock in place
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL; // Lock in place
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(uqPanel, c);
		JPanel lqPanel = new JPanel();
		lqPanel.setBorder(BorderFactory.createTitledBorder("Lower Quartile"));
		lqPanel.add(showLq);
		// lqPanel.setPreferredSize(new Dimension(100, 50)); //Set initial size
		// lqPanel.setMinimumSize(new Dimension(100, 50)); //Set minimum size
		// lqPanel.setSize(100, 50); //Lock in place
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(lqPanel, c);
		JPanel iqPanel = new JPanel();
		iqPanel.setBorder(BorderFactory.createTitledBorder("InterQuartile"));
		iqPanel.add(showIq);
		// iqPanel.setPreferredSize(new Dimension(100, 50)); //Set initial size
		// iqPanel.setMinimumSize(new Dimension(100, 50)); //Set minimum size
		// iqPanel.setSize(100, 50); //Lock in place
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		windowContent.add(iqPanel, c);
		String[] list = new String[dataSet.getModel().getSize()];
		Collections.list(((DefaultListModel<String>) dataSet.getModel()).elements()).toArray(list);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 0;
		c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		// Frame
		JFrame frame = new JFrame("\"Measures Of Variation\" Calculator");
		frame.setContentPane(windowContent);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				if (s.writer != null) {// To prevent un-closeable windows (trust me, that's BAD)
					s.writer.flush();
					s.writer.close();
				}
			}
		});
		frame.setVisible(true);
	}

	private void setFieldsUneditable() {
		showRange = new JTextField("                    ", 0);
		showMedian = new JTextField("                    ", 0);
		showUq = new JTextField("                    ", 0);
		showLq = new JTextField("                    ", 0);
		showIq = new JTextField("                    ", 0);
		showOut = new JTextField("                    ", 0);
		showRange.setEditable(false);
		showMedian.setEditable(false);
		showUq.setEditable(false);
		showLq.setEditable(false);
		showIq.setEditable(false);
		showOut.setEditable(false);
	}

	public double round(double toRound, int places) {
		return Double.parseDouble(String.format("%." + (places + 1) + "g%n", toRound));
	}

	public double range(DefaultListModel<String> model) {
		double high = 0, low = 0;
		high = Double.parseDouble(model.lastElement());
		low = Double.parseDouble(model.firstElement());
		return high - low;
	}

	public double median(String[] d) {
		if (d.length % 2 == 0) {
			return (Double.parseDouble(d[d.length / 2]) + Double.parseDouble(d[d.length / 2 - 1])) / 2;
		} else {
			return Double.parseDouble(d[(d.length - 1) / 2]);
		}
	}

	/**
	 * Gets the upper quartile of the data, exclucive
	 * 
	 * @param d
	 * @return
	 */
	public double uq(String[] d) {
		String[] subSetArray;
		if (d.length % 2 == 0) {
			subSetArray = new String[d.length / 2];
			System.arraycopy(d, d.length / 2, subSetArray, 0, d.length / 2);
			return median(subSetArray);
		} else {
			subSetArray = new String[(d.length - 1) / 2];
			System.arraycopy(d, (d.length - 1) / 2 + 1, subSetArray, 0, (d.length - 1) / 2);
			return median(subSetArray);
		}
	}

	public double lq(String[] d) {
		String[] subSetArray;
		if (d.length % 2 == 0) {
			subSetArray = new String[d.length / 2];
			System.arraycopy(d, 0, subSetArray, 0, d.length / 2);
			return median(subSetArray);
		} else {
			subSetArray = new String[(d.length - 1) / 2];
			System.arraycopy(d, 0, subSetArray, 0, (d.length - 1) / 2);
			return median(subSetArray);
		}
	}

	public double iq(String[] d) {
		return uq(d) - lq(d);
	}

	public double[] limits(String[] d) {
		double[] lim = new double[1];
		lim[0] = lq(d) - iq(d) * 1.5;
		lim[1] = uq(d) + iq(d) * 1.5;
		return lim;
	}

	public JPanel getBoxAndWhiskersGraph(Paint bg, Paint boxOutline, Paint boxFill, Paint whiskerColor, String[] d, double start, double end) {
		final Paint bgClone = bg;
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setPaint(bgClone);
				g2.fill(new Rectangle2D.Double(this.getBounds().x, this.getBounds().y, this.getBounds().width, this.getBounds().height));
			}
		};
		return panel;
	}

	private class SaveInfo {
		public boolean always = false;
		public PrintWriter writer = null;
	}

	private DefaultListModel<String> leastToGreatest(DefaultListModel<String> start) {
		DefaultListModel<String> m = new DefaultListModel<String>();
		String[] array = new String[start.getSize()];
		Collections.list(start.elements()).toArray(array);
		double[] sort = new double[start.getSize()];
		int i = 0;
		for (String str : array) {
			sort[i] = Double.parseDouble(str);
			i++;
		}
		Arrays.sort(sort);
		for (double num : sort) {
			m.addElement("" + num + "");
		}
		return m;
	}

	public static void main(String... args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MeasuresOfVariation();
			}
		});
	}
}
