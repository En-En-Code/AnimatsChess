package net.animats.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AboutDialog extends JDialog {
	
	private static AboutDialog instance = null;
	static final long serialVersionUID = 1;
	
	public static AboutDialog getInstance(JFrame _owner) {
			if (instance == null)
				instance = new AboutDialog(_owner);
			
			return (instance);
	}
	
	private AboutDialog(JFrame _owner) {
		super (_owner, "About", true);
		
		JLabel infoLabel = new JLabel("Animats Chess by Stuart Allen 2007");
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
