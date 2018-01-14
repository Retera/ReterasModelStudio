package com.hiveworkshop.wc3.jworldedit.triggers.impl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.collections.CollectionView;
import com.etheller.collections.CollectionView.ForEach;
import com.hiveworkshop.wc3.jworldedit.triggers.gui.TriggerTreeController;
import com.hiveworkshop.wc3.resources.WEString;

public final class TriggerEnvironment implements TriggerTreeController {
	private final String name;
	private final List<TriggerCategory> categories;
	private transient final DefaultTriggerNameFinder nameFinder = new DefaultTriggerNameFinder();

	public TriggerEnvironment(final String name) {
		this.name = name;
		this.categories = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public List<TriggerCategory> getCategories() {
		return categories;
	}

	@Override
	public Trigger createTrigger(final TriggerCategory triggerCategory) {
		if (!categories.contains(triggerCategory)) {
			throw new IllegalStateException("creating trigger in invalid category!");
		}
		String triggerName;
		int triggerNameIndex = 0;
		do {
			triggerNameIndex++;
			triggerName = String.format("%s %s", WEString.getString("WESTRING_UNTITLEDTRIGGER"),
					String.format("%3d", triggerNameIndex).replace(' ', '0'));
			forEachTrigger(nameFinder.reset(triggerName));
		} while (nameFinder.isNameFound());
		final TriggerImpl triggerImpl = new TriggerImpl(triggerName);
		triggerImpl.setCategory(triggerCategory);
		triggerCategory.getTriggers().add(0, triggerImpl);
		return triggerImpl;
	}

	@Override
	public Trigger createTriggerComment(final TriggerCategory triggerCategory) {
		if (!categories.contains(triggerCategory)) {
			throw new IllegalStateException("creating trigger in invalid category!");
		}
		final TriggerComment comment = new TriggerComment(WEString.getString("WESTRING_TRIGGERCOMMENT_DEFAULT"));
		comment.setCategory(triggerCategory);
		triggerCategory.getTriggers().add(comment);
		return comment;
	}

	@Override
	public TriggerCategory createCategory() {
		final TriggerCategory triggerCategory = new TriggerCategory(
				WEString.getString("WESTRING_UNTITLEDTRIGGERCATEGORY"));
		categories.add(triggerCategory);
		return triggerCategory;
	}

	@Override
	public void renameTrigger(final Trigger trigger, final String name) {
		trigger.setName(name);
	}

	@Override
	public void moveTrigger(final Trigger trigger, final TriggerCategory triggerCategory, final int index) {
		final TriggerCategory previousCategory = trigger.getCategory();
		previousCategory.getTriggers().remove(trigger);
		trigger.setCategory(triggerCategory);
		triggerCategory.getTriggers().add(index, trigger);
	}

	@Override
	public void toggleCategoryIsComment(final TriggerCategory triggerCategory) {
		triggerCategory.setComment(!triggerCategory.isComment());
	}

	@Override
	public void renameCategory(final TriggerCategory category, final String name) {
		category.setName(name);
	}

	public void forEachTrigger(final ForEach<Trigger> triggerForEach) {
		AllTriggers: for (final TriggerCategory category : categories) {
			for (final Trigger trigger : category.getTriggers()) {
				if (!triggerForEach.onEntry(trigger)) {
					break AllTriggers;
				}
			}
		}
	}

	private final class DefaultTriggerNameFinder implements CollectionView.ForEach<Trigger> {
		private String name;
		private boolean nameFound = false;

		public DefaultTriggerNameFinder reset(final String name) {
			this.name = name;
			this.nameFound = false;
			return this;
		}

		@Override
		public boolean onEntry(final Trigger trigger) {
			if (name.equals(trigger.getName())) {
				nameFound = true;
				return false;
			}
			return true;
		}

		public boolean isNameFound() {
			return nameFound;
		}
	}
}
