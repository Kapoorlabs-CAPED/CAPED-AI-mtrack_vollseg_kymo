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

import java.util.EventObject;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * An event that characterizes a change in the current selection. 
 * {@link Spot} selection and {@link DefaultWeightedEdge} selection are dealt with separately, 
 * to keep the use of this class general.
 */
public class SelectionChangeEvent extends EventObject {

	private static final long serialVersionUID = -8920831578922412606L;

	/** Changes in {@link DefaultWeightedEdge} selection this event represents. */

	/** Changes in {@link Spot} selection this event represents. */
	protected Map<Stat, Boolean> stats;

	/*
	 * CONSTRUCTORS 
	 */
	
	/**
	 * Represents a change in the selection of a displayed TM model.
	 * <p>
	 * Two maps are given. The first one represent changes in the spot
	 * selection. The {@link Boolean} mapped to a {@link Spot} key specifies if
	 * the spot was added to the selection (<code>true</code>) or removed from
	 * it (<code>false</code>). The same goes for the
	 * {@link DefaultWeightedEdge} map. <code>null</code>s are accepted for the
	 * two maps, to specify that no changes happened for the corresponding type.
	 * 
	 * @param source
	 *            the source object that fires this event.
	 * @param spots
	 *            the spots that are added or removed from the selection by this
	 *            event.
	 * @param edges
	 *            the edges that are added or removed from the selection by this
	 *            event.
	 */
	public SelectionChangeEvent(final Object source, final Map<Stat, Boolean> stats) {
		super(source);
		this.stats = stats;
	}
	
	/*
	 * METHODS
	 */
	
	/**
	 * Returns the spots that have been added or removed from the selection.
	 * The {@link Boolean} 
	 * mapped to a {@link Spot} key specifies if the spot was added to the selection (<code>true</code>)
	 * or removed from it (<code>false</code>).
	 * @return added or removed spots, can be <code>null</code> if no changes on spot selection happened.
	 */
	public Map<Stat, Boolean> getStats() {
		return stats;
	}

	


}
