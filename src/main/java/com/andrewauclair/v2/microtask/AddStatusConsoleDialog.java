package com.andrewauclair.v2.microtask;

import com.andrewauclair.v2.MainFrame;

import javax.swing.*;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class AddStatusConsoleDialog extends JDialog {
	public AddStatusConsoleDialog(MainFrame mainFrame) {
		JTextField command = new JTextField();
		JRadioButton updateOnDemand = new JRadioButton("Update When Command Entered");
		JRadioButton updateOnIterval = new JRadioButton("Update on Interval");

		ButtonGroup group = new ButtonGroup();
		group.add(updateOnDemand);
		group.add(updateOnIterval);

		JTextField updateEveryX = new JTextField();

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		labeledComponent("Command", command, gbc);
		gbc.gridy++;

		add(updateOnDemand, gbc);
		gbc.gridy++;
		add(updateOnIterval, gbc);
		gbc.gridy++;

		updateEveryX.setText("1");
		add(updateEveryX, gbc);
		gbc.gridy++;

		add(ok, gbc);
		gbc.gridy++;
		add(cancel, gbc);

		ok.addActionListener(e -> {
			mainFrame.addStatusConsole(command.getText(), updateOnIterval.isSelected(), Integer.parseInt(updateEveryX.getText()));
		});

		pack();
	}

	private void labeledComponent(String text, Component comp, GridBagConstraints gbc) {
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel(text));
		panel.add(comp);
		add(panel, gbc);
	}
}
