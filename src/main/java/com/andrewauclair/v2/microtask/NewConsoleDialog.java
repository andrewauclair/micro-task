package com.andrewauclair.v2.microtask;

import com.formdev.flatlaf.icons.FlatWindowAbstractIcon;

import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class NewConsoleDialog extends JPanel {
	public static class Result {
		public String name;
		public String command;
	}
	private Result result;
	JTextField name = new JTextField();
	JTextField command = new JTextField();
	public NewConsoleDialog() {


		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
//		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.weightx = 1.0;
		JPanel namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Name:"));
		namePanel.add(name);

		add(namePanel, gbc);
		gbc.gridy++;

		JPanel commandPanel = new JPanel(new FlowLayout());
		commandPanel.add(new JLabel("Command:"));
		commandPanel.add(command);

		add(commandPanel, gbc);
		gbc.gridy++;

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		ok.addActionListener(e -> {
			result = new Result();
			result.name = name.getText();
			result.command = command.getText();
		});

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(cancel);
		buttonPanel.add(ok);

//		add(buttonPanel, gbc);

//		setTitle("Add New Console");
//
//		pack();
//		setModal(true);
//		setVisible(true);
	}

	public Result display() {
		result = new Result();
		result.name = name.getText();
		result.command = command.getText();
		return result;
	}
}
