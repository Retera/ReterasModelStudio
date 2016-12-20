package com.hiveworkshop.modding.jass;

import java.util.List;

//function Trig_Melee_Initialization_Actions takes nothing returns nothing
//	call MeleeStartingVisibility(  )
//	call MeleeStartingHeroLimit(  )
//	call MeleeGrantHeroItems(  )
//	call MeleeStartingResources(  )
//	call MeleeClearExcessUnits(  )
//	call MeleeStartingUnits(  )
//	call MeleeStartingAI(  )
//	call MeleeInitVictoryDefeat(  )
//	call DisplayTimedTextToForce( GetPlayersAll(), 30, "TRIGSTR_001" )
//endfunction
public class FunctionDeclaration {
	boolean isConstant;
	boolean isNative;
	String name;
	List<ArgumentDeclaration> arguments;
}
