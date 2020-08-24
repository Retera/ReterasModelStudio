package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerTreeController;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WEString;

public final class TriggerEnvironment implements TriggerTreeController {
	private String name;
	private final List<TriggerCategory> categories = new ArrayList<>();

	public TriggerEnvironment(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
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
		} while (!isNameAvailable(triggerName));

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
		if (previousCategory != null) {
			previousCategory.getTriggers().remove(trigger);
		}
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

	public boolean isNameAvailable(String name) {
		for (final TriggerCategory category : categories) {
			for (final Trigger trigger : category.getTriggers()) {
				if (trigger.getName().equals(name)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void deleteTrigger(final Trigger trigger) {
		trigger.getCategory().getTriggers().remove(trigger);
		trigger.setCategory(null);
	}

	@Override
	public void deleteCategory(final TriggerCategory category) {
		categories.remove(category);
	}

	@Override
	public void moveCategory(final TriggerCategory triggerCategory, final int index) {
		categories.remove(triggerCategory);
		categories.add(index, triggerCategory);
	}
}
