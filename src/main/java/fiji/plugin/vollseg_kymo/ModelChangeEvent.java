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

import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;


public class ModelChangeEvent extends EventObject {

	private static final long serialVersionUID = -1L;
	/** Indicate that a Stat was added to the model. */
	public static final int FLAG_Stat_ADDED = 0;
	/** Indicate that a Stat was removed from the model. */
	public static final int FLAG_Stat_REMOVED = 1;
	/**
	 * Indicate a modification of the features of a Stat. It may have changed of
	 * position and feature, but not of frame.
	 */
	public static final int FLAG_Stat_MODIFIED = 2;
	/**
	 * Indicate that a Stat has changed of frame, and possible of position,
	 * features, etc.. .
	 */
	public static final int FLAG_Stat_FILE_CHANGED = 3;

	/**
	 * Indicate that an edge has been modified. Edge modifications occur when
	 * the target or source Stats are modified, or when the weight of the edge
	 * has been modified.
	 */
	public static final int FLAG_EDGE_MODIFIED = 6;

	public static final Map<Integer, String> flagsToString = new HashMap<>(4);
	static {
		flagsToString.put(FLAG_Stat_ADDED, "Stat added");
		flagsToString.put(FLAG_Stat_FILE_CHANGED, "Stat file changed");
		flagsToString.put(FLAG_Stat_MODIFIED, "Stat modified");
		flagsToString.put(FLAG_Stat_REMOVED, "Stat removed");
	}


	/**
	 * Event type indicating that the Stats of the model were computed, and
	 * are now accessible through {@link Model#getStats()}.
	 */
	public static final int 	StatS_COMPUTED = 4;
	/**
	 * Event type indicating that the Stats of the model were filtered.
	 */
	public static final int 	StatS_FILTERED = 5;

	
	/**
	 * Event type indicating that model was modified, by adding, removing or
	 * changing the feature of some Stats, and/or adding or removing edges in
	 * the tracks. Content of the modification can be accessed by
	 * {@link #getStats()}, {@link #getStatFlag(Stat)},
	 * {@link #getFromFrame(Stat)} and {@link #getToFrame(Stat)}, and for the
	 * tracks: {@link #getEdges()} and {@link #getEdgeFlag(DefaultWeightedEdge)}
	 * .
	 */
	public static final int 	MODEL_MODIFIED = 8;

	/**
	 * Event type indicated that the feature values for the objects in this
	 * model have been computed.
	 */
	public static final int FEATURES_COMPUTED = 9;

	/** Stats affected by this event. */
	private final HashSet<Stat> Stats = new HashSet<>();
	/** Edges affected by this event. */
	private final HashSet<DefaultWeightedEdge> edges = new HashSet<>();
	/** For Stats removed or moved: frame from which they were removed or moved. */
	private final HashMap<Stat, String> fromFile = new HashMap<>();
	/** For Stats removed or added: frame to which they were added or moved. */
	private final HashMap<Stat, String> toFile = new HashMap<>();
	/** Modification flag for Stats affected by this event. */
	private final HashMap<Stat, Integer> StatFlags = new HashMap<>();
	/** Modification flag for edges affected by this event. */
	private final int eventID;

	/**
	 * Create a new event, reflecting a change in a {@link Model}.
	 *
	 * @param source
	 *            the object source of this event.
	 * @param eventID
	 *            the evend ID to use for this event.
	 */
	public ModelChangeEvent(final Object source, final int eventID) {
		super(source);
		this.eventID = eventID;
	}

	public int getEventID() {
		return this.eventID;
	}

	public boolean addAllStats(final Collection<Stat> lStats) {
		return this.Stats.addAll(lStats);
	}

	public boolean addStat(final Stat Stat) {
		return this.Stats.add(Stat);
	}

	
	public Integer putStatFlag(final Stat Stat, final Integer flag) {
		return StatFlags.put(Stat, flag);
	}

	public String putFromFile(final Stat Stat, final String lFromFile) {
		return this.fromFile.put(Stat, lFromFile);
	}

	public String putToFile(final Stat Stat, final String lToFile) {
		return this.toFile.put(Stat, lToFile);
	}

	/**
	 * @return  the set of Stat that are affected by this event. Is empty
	 * if no Stat is affected by this event.
	 */
	public Set<Stat> getStats() {
		return Stats;
	}

	/**
	 * @return  the set of edges that are affected by this event. Is empty
	 * if no edge is affected by this event.
	 */
	public Set<DefaultWeightedEdge> getEdges() {
		return edges;
	}

	/**
	 * Returns the modification flag for the given Stat affected by this event.
	 * 
	 * @param Stat
	 *            the Stat to query.
	 * @return the modification flag.
	 * @see #FLAG_Stat_ADDED
	 * @see #FLAG_Stat_MODIFIED
	 * @see #FLAG_Stat_REMOVED
	 */
	public Integer getStatFlag(final Stat Stat) {
		return StatFlags.get(Stat);
	}

	/**
	 * Returns the modification flag for the given edge affected by this event.
	 * 
	 * @param edge
	 *            the edge to query.
	 * @return the modification flag.
	 * @see #FLAG_EDGE_ADDED
	 * @see #FLAG_EDGE_REMOVED
	 */

	public String getToFile(final Stat Stat) {
		return toFile.get(Stat);
	}

	public String getFromFile(final Stat Stat) {
		return fromFile.get(Stat);
	}

	public void setSource(final Object source) {
		this.source = source;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder("[ModelChangeEvent]:\n");
		str.append(" - source: "+source.getClass() + "_" + source.hashCode()+"\n");
		str.append(" - event type: ");
		switch (eventID) {
		case StatS_COMPUTED:
			str.append("Stats computed\n");
			break;
		case StatS_FILTERED:
			str.append("Stats filtered\n");
			break;
		case MODEL_MODIFIED:
			str.append("Model modified, with:\n");
			str.append("\t- Stats modified: "+ (Stats != null ? Stats.size() : 0) +"\n");
			for (final Stat Stat : Stats) {
				str.append("\t\t" + Stat + ": " + flagsToString.get(StatFlags.get(Stat)) + "\n");
			}
			
		}
		return str.toString();
	}


}
