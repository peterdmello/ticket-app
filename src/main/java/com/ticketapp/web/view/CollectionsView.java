package com.ticketapp.web.view;

import java.util.Collection;

/**
 * A view of collection objects to be displayed. Can be updated for pagination.
 * @author peter
 *
 * @param <T>
 */
public class CollectionsView<T> {

	private final Collection<T> items;
	public CollectionsView(Collection<T> items) {
		this.items = items;
	}

	public Collection<T> getItems() {
		return items;
	}

	public long getTotal() {
		return items.size();
	}
}
