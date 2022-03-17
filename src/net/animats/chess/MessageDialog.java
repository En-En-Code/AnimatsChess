/*
 * Animats Chess Engine, started 8 August 2005, played its first game 9 September 2005
 * Copyright (C) 2005-2009 Stuart Allen, 2022 En-En
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

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
