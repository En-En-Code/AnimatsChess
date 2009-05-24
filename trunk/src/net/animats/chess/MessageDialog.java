package net.animats.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MessageDialog extends JDialog {
	
	static final long serialVersionUID = 1;
	
	public MessageDialog(JFrame _owner, String _title, String _message) {
		super (_owner, _title, true);
		
		JLabel infoLabel = new JLabel(_message, JLabel.CENTER);
		infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
		infoLabel.setForeground(new Color(0, 0, 255));
		
		add (infoLabel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		
		okButton.addActionListener(new 
			ActionListener() {
				public void actionPerformed(ActionEvent _event) {
					setVisible(false);
				}
			});
		
		buttonPanel.add(okButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		setLocation(84,215);
		
		setSize(340, 111);
				
	}
	
}
