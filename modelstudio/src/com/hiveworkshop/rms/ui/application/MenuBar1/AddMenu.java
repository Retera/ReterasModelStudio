package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.AddBirthDeathSequences;
import com.hiveworkshop.rms.ui.application.AddParticlePanel;
import com.hiveworkshop.rms.ui.application.AddSingleAnimationActions;
import com.hiveworkshop.rms.ui.application.MenuBarActions;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.*;

public class AddMenu extends JMenu {

	public AddMenu() {
		super("Add");
		setMnemonic(KeyEvent.VK_A);
		getAccessibleContext().setAccessibleDescription("Allows the user to add new components to the model.");

		JMenu addParticleMenu = createMenu("Particle", KeyEvent.VK_P);
		add(addParticleMenu);

		AddParticlePanel.addParticleButtons(addParticleMenu);
		addParticleMenu.add(createMenuItem("Empty Popcorn", KeyEvent.VK_O, e -> AddParticlePanel.addEmptyPopcorn()));

		JMenu animationMenu = createMenu("Animation", KeyEvent.VK_A);
		add(animationMenu);

		createAndAddMenuItem("Empty", animationMenu, KeyEvent.VK_F, e -> AddSingleAnimationActions.addEmptyAnimation());
		createAndAddMenuItem("Rising/Falling Birth/Death", animationMenu, KeyEvent.VK_R, e -> AddBirthDeathSequences.riseFallBirthActionRes());

		JMenu singleAnimationMenu = createMenu("Single", KeyEvent.VK_S);
		animationMenu.add(singleAnimationMenu);

		singleAnimationMenu.add(createMenuItem("From File", KeyEvent.VK_F, e -> AddSingleAnimationActions.addAnimationFromFile()));
		singleAnimationMenu.add(createMenuItem("From Unit", KeyEvent.VK_U, e -> AddSingleAnimationActions.addAnimationFromUnit()));
		singleAnimationMenu.add(createMenuItem("From Model", KeyEvent.VK_M, e -> AddSingleAnimationActions.addAnimFromModel()));
		singleAnimationMenu.add(createMenuItem("From Object", KeyEvent.VK_O, e -> AddSingleAnimationActions.addAnimationFromObject()));

		add(createMenuItem("Material", KeyEvent.VK_M, e -> MenuBarActions.addNewMaterial()));
	}
}
