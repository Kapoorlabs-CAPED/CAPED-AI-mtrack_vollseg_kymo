/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2010 - 2022 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.vollseg_kymo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * A component of {@link Model} that handles Stat  selection.
 * @author V Kapoor
 */
public class SelectionModel {

	private static final boolean DEBUG = false;

	/** The Stat current selection. */
	private Set<Stat> StatSelection = new HashSet<>();
	/** The edge current selection. */
	private Set<DefaultWeightedEdge> edgeSelection = new HashSet<>();
	/** The list of listener listening to change in selection. */
	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();


	/*
	 * DEFAULT VISIBILITY CONSTRUCTOR
	 */

	public SelectionModel() {
	}

	/*
	 * DEAL WITH SELECTION CHANGE LISTENER
	 */

	public boolean addSelectionChangeListener(SelectionChangeListener listener) {
		return selectionChangeListeners.add(listener);
	}

	public boolean removeSelectionChangeListener(SelectionChangeListener listener) {
		return selectionChangeListeners.remove(listener);
	}

	public List<SelectionChangeListener> getSelectionChangeListener() {
		return selectionChangeListeners;
	}

	/*
	 * SELECTION CHANGES
	 */

	public void clearSelection() {
		if (DEBUG)
			System.out.println("[SelectionModel] Clearing selection");
		// Prepare event
		Map<Stat, Boolean> StatMap = new HashMap<>(StatSelection.size());
		for (Stat Stat : StatSelection)
			StatMap.put(Stat, false);
		Map<DefaultWeightedEdge, Boolean> edgeMap = new HashMap<>(edgeSelection.size());
		for (DefaultWeightedEdge edge : edgeSelection)
			edgeMap.put(edge, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, StatMap);
		// Clear fields
		clearStatSelection();
		// Fire event
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void clearStatSelection() {
		if (DEBUG)
			System.out.println("[SelectionModel] Clearing Stat selection");
		// Prepare event
		Map<Stat, Boolean> StatMap = new HashMap<>(StatSelection.size());
		for (Stat Stat : StatSelection)
			StatMap.put(Stat, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, StatMap);
		// Clear field
		StatSelection.clear();
		// Fire event
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	

	public void addStatToSelection(final Stat Stat) {
		if (!StatSelection.add(Stat))
			return; // Do nothing if already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Adding Stat " + Stat + " to selection");
		Map<Stat, Boolean> StatMap = new HashMap<>(1);
		StatMap.put(Stat, true);
		if (DEBUG)
			System.out.println("[SelectionModel] Seding event to listeners: "+selectionChangeListeners);
		SelectionChangeEvent event = new SelectionChangeEvent(this, StatMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void removeStatFromSelection(final Stat Stat) {
		if (!StatSelection.remove(Stat))
			return; // Do nothing was not already present in selection
		if (DEBUG)
			System.out.println("[SelectionModel] Removing Stat " + Stat + " from selection");
		Map<Stat, Boolean> StatMap = new HashMap<>(1);
		StatMap.put(Stat, false);
		SelectionChangeEvent event = new SelectionChangeEvent(this, StatMap);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	public void addStatToSelection(final Collection<Stat> Stats) {
		Map<Stat, Boolean> StatMap = new HashMap<>(Stats.size());
		for (Stat Stat : Stats) {
			if (StatSelection.add(Stat)) {
				StatMap.put(Stat, true);
				if (DEBUG)
					System.out.println("[SelectionModel] Adding Stat " + Stat + " to selection");
			}
		}
		SelectionChangeEvent event = new SelectionChangeEvent(this, StatMap);
		if (DEBUG) 
			System.out.println("[SelectionModel] Seding event "+event.hashCode()+" to "+selectionChangeListeners.size()+" listeners: "+selectionChangeListeners);
		for (SelectionChangeListener listener : selectionChangeListeners)
			listener.selectionChanged(event);
	}

	

	
	

	public Set<Stat> getStatSelection() {
		return StatSelection;
	}

	public Set<DefaultWeightedEdge> getEdgeSelection() {
		return edgeSelection;
	}

	/*
	 * SPECIAL METHODS
	 */


	

}
