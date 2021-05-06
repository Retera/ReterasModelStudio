package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.*;

public class AddMenu extends JMenu {

	public AddMenu(final MainPanel mainPanel) {
		super("Add");
		setMnemonic(KeyEvent.VK_A);
		getAccessibleContext().setAccessibleDescription("Allows the user to add new components to the model.");

		JMenu addParticleMenu = createMenu("Particle", KeyEvent.VK_P);
		add(addParticleMenu);

		AddParticlePanel.addParticleButtons(mainPanel, addParticleMenu);
		addParticleMenu.add(createMenuItem("Empty Popcorn", KeyEvent.VK_O, e -> AddParticlePanel.addEmptyPopcorn(mainPanel)));

		JMenu animationMenu = createMenu("Animation", KeyEvent.VK_A);
		add(animationMenu);

		createAndAddMenuItem("Empty", animationMenu, KeyEvent.VK_F, e -> AddSingleAnimationActions.addEmptyAnimation(mainPanel));
		createAndAddMenuItem("Rising/Falling Birth/Death", animationMenu, KeyEvent.VK_R, e -> AddBirthDeathSequences.riseFallBirthActionRes(mainPanel));

		JMenu singleAnimationMenu = createMenu("Single", KeyEvent.VK_S);
		animationMenu.add(singleAnimationMenu);

		singleAnimationMenu.add(createMenuItem("From File", KeyEvent.VK_F, e -> AddSingleAnimationActions.addAnimationFromFile(mainPanel)));
		singleAnimationMenu.add(createMenuItem("From Unit", KeyEvent.VK_U, e -> AddSingleAnimationActions.addAnimationFromUnit(mainPanel)));
		singleAnimationMenu.add(createMenuItem("From Model", KeyEvent.VK_M, e -> AddSingleAnimationActions.addAnimFromModel(mainPanel)));
		singleAnimationMenu.add(createMenuItem("From Object", KeyEvent.VK_O, e -> AddSingleAnimationActions.addAnimationFromObject(mainPanel)));

		add(createMenuItem("Material", KeyEvent.VK_M, e -> MenuBarActions.addNewMaterial(mainPanel)));
	}
}
