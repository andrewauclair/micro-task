package com.andrewauclair.v2.microtask;

import com.andrewauclair.v2.MainFrame;

import javax.swing.*;
import java.awt.*;

public class AddStatusConsoleDialog extends JDialog {
	public AddStatusConsoleDialog(MainFrame mainFrame) {
		JTextField command = new JTextField();
		JRadioButton updateOnDemand = new JRadioButton("Update When Command Entered");
		JRadioButton updateOnInterval = new JRadioButton("Update on Interval");

		ButtonGroup group = new ButtonGroup();
		group.add(updateOnDemand);
		group.add(updateOnInterval);

		updateOnDemand.setSelected(true);

		JTextField updateEveryX = new JTextField();
		updateEveryX.setEnabled(false);

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		labeledComponent("Command", command, gbc);
		gbc.gridy++;

		add(updateOnDemand, gbc);
		gbc.gridy++;
		add(updateOnInterval, gbc);
		gbc.gridy++;

		updateEveryX.setText("1");
		add(updateEveryX, gbc);
		gbc.gridy++;

		gbc.gridwidth = 1;
		add(ok, gbc);
		gbc.gridx++;
		add(cancel, gbc);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		updateOnInterval.addActionListener(e -> {
			updateEveryX.setEnabled(updateOnInterval.isSelected());
		});

		ok.addActionListener(e -> {
			mainFrame.addStatusConsole(command.getText(), updateOnInterval.isSelected(), Integer.parseInt(updateEveryX.getText()));
			dispose();
		});

		cancel.addActionListener(e -> {
			dispose();
		});

		pack();
		setLocationRelativeTo(mainFrame);
	}

	private void labeledComponent(String text, Component comp, GridBagConstraints gbc) {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel(text));
		panel.add(comp);
		add(panel, gbc);
	}
}
