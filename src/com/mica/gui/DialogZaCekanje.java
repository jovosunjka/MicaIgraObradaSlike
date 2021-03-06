package com.mica.gui;

import static  com.mica.gui.GlavniProzor.SIZE_SCREEN;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;;

@SuppressWarnings("serial")
public class DialogZaCekanje extends JDialog {

	public DialogZaCekanje(GlavniProzor glavniProzor, String poruka, boolean bezDugmeta) {
		super(glavniProzor, "Cekanje", true);
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		setUndecorated(true);
		
		int height;
		if(bezDugmeta) height = 90;
		else height = 120;
		
		setSize(250, height);
		setLocation(SIZE_SCREEN.width/3 - 75, SIZE_SCREEN.height/3 + 60);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        //progressBar.setBorderPainted(true);
        progressBar.setIndeterminate(true);

        JLabel labelMolimSacekajte = new JLabel(poruka);
        
        GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 0.0;
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		gbl.setConstraints(labelMolimSacekajte, constraints);
		getContentPane().add(labelMolimSacekajte);
        
        constraints.gridx = 0;
		constraints.gridy = 1;
		gbl.setConstraints(progressBar, constraints);
		getContentPane().add(progressBar);
		
		if(!bezDugmeta) {
			constraints.gridx = 0;
			constraints.gridy = 2;
			JLabel label = new JLabel("     ");
			gbl.setConstraints(label, constraints);
			getContentPane().add(label);
		
			JButton buttonOdustani = new JButton("Odustani");
	        buttonOdustani.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					glavniProzor.getController().odustaniOdObradeSlike();
				}
			});
			
			constraints.gridx = 0;
			constraints.gridy = 3;
			gbl.setConstraints(buttonOdustani, constraints);
			getContentPane().add(buttonOdustani);
		}

	}
}
