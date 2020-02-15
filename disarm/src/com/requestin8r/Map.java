package com.requestin8r;

import java.util.ArrayList;
import java.util.List;

public class Map {
	class HeroType {
		String name;

		public HeroType(final String name) {
			super();
			this.name = name;
		}

	}

	class Tavern {
		List<HeroType> heroes = new ArrayList<>();
	}

	HeroType getHeroType(final List<Tavern> taverns) {
		final Tavern tavern = taverns.get((int) (Math.random() * taverns.size()));
		return getHeroType(tavern);
	}

	HeroType getHeroType(final Tavern tav) {
		return tav.heroes.get((int) (Math.random() * tav.heroes.size()));
	}

	void init() {
		pickRandomHero();
	}

	private void pickRandomHero() {

		final Tavern strength = new Tavern();
		strength.heroes.add(new HeroType("Warrior"));
		strength.heroes.add(new HeroType("Warrior 2"));

		final Tavern intelligence = new Tavern();
		intelligence.heroes.add(new HeroType("Pyrokinesis"));
		intelligence.heroes.add(new HeroType("Aquakinesis"));
		intelligence.heroes.add(new HeroType("Geokinesis"));

		final Tavern agility = new Tavern();
		agility.heroes.add(new HeroType("Toxopholite Archer"));

		final List<Tavern> taverns = new ArrayList<>();
		taverns.add(strength);
		taverns.add(agility);
		taverns.add(intelligence);
		final HeroType heroType = getHeroType(taverns);
		System.out.println("Randomly chosen hero is: " + heroType.name);
	}

	public static void main(final String[] args) {
		final Map map = new Map();
		map.init();
	}
}
