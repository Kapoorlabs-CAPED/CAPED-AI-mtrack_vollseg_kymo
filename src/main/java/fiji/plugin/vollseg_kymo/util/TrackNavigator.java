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
package fiji.plugin.vollseg_kymo.util;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.graph.TimeDirectedNeighborIndex;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultWeightedEdge;

public class TrackNavigator {

	private final Model model;
	private final SelectionModel selectionModel;
	private final TimeDirectedNeighborIndex neighborIndex;

	public TrackNavigator(final Model model, final SelectionModel selectionModel) {
		this.model = model;
		this.selectionModel = selectionModel;
		this.neighborIndex = model.getTrackModel().getDirectedNeighborIndex();
	}

	public synchronized void nextTrack() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		final Set<Integer> trackIDs = model.getTrackModel().trackIDs(true); // if only it was navigable...
		if (trackIDs.isEmpty()) {
			return;
		}

		Integer trackID = model.getTrackModel().trackIDOf(spot);
		if (null == trackID) {
			// No track? Then move to the first one.
			trackID = model.getTrackModel().trackIDs(true).iterator().next();
		}

		final Iterator<Integer> it = trackIDs.iterator();
		Integer nextTrackID = null;
		while (it.hasNext()) {
			final Integer id = it.next();
			if (id.equals(trackID)) {
				if (it.hasNext()) {
					nextTrackID = it.next();
					break;
				}
				nextTrackID = trackIDs.iterator().next(); // loop
			}
		}

		final Set<Stat> spots = model.getTrackModel().trackSpots(nextTrackID);
		final TreeSet<Stat> ring = new TreeSet<>(Stat.frameComparator);
		ring.addAll(spots);
		Stat target = ring.ceiling(spot);
		if (null == target) {
			target = ring.floor(spot);
		}

		selectionModel.clearSelection();
		selectionModel.addSpotToSelection(target);
	}

	public synchronized void previousTrack() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		Integer trackID = model.getTrackModel().trackIDOf(spot);
		final Set<Integer> trackIDs = model.getTrackModel().trackIDs(true); // if only it was navigable...
		if (trackIDs.isEmpty()) {
			return;
		}

		Integer lastID = null;
		for (final Integer id : trackIDs) {
			lastID = id;
		}

		if (null == trackID) {
			// No track? Then take the last one.
			trackID = lastID;
		}

		final Iterator<Integer> it = trackIDs.iterator();
		Integer previousTrackID = null;
		while (it.hasNext()) {
			final Integer id = it.next();
			if (id.equals(trackID)) {
				if (previousTrackID != null) {
					break;
				}
				previousTrackID = lastID;
				break;
			}
			previousTrackID = id;
		}

		final Set<Stat> spots = model.getTrackModel().trackSpots(previousTrackID);
		final TreeSet<Stat> ring = new TreeSet<>(Stat.frameComparator);
		ring.addAll(spots);
		Stat target = ring.ceiling(spot);
		if (null == target) {
			target = ring.floor(spot);
		}

		selectionModel.clearSelection();
		selectionModel.addSpotToSelection(target);
	}

	public synchronized void nextSibling() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		final Integer trackID = model.getTrackModel().trackIDOf(spot);
		if (null == trackID) {
			return;
		}

		final int frame = spot.getFeature(Stat.FRAME).intValue();
		final TreeSet<Stat> ring = new TreeSet<>(Stat.nameComparator);

		final Set<Stat> spots = model.getTrackModel().trackSpots(trackID);
		for (final Stat s : spots) {
			final int fs = s.getFeature(Stat.FRAME).intValue();
			if (frame == fs && s != spot) {
				ring.add(s);
			}
		}

		if (!ring.isEmpty()) {
			Stat nextSibling = ring.ceiling(spot);
			if (null == nextSibling) {
				nextSibling = ring.first(); // loop
			}
			selectionModel.clearSelection();
			selectionModel.addSpotToSelection(nextSibling);
		}
	}

	public synchronized void previousSibling() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		final Integer trackID = model.getTrackModel().trackIDOf(spot);
		if (null == trackID) {
			return;
		}

		final int frame = spot.getFeature(Stat.FRAME).intValue();
		final TreeSet<Stat> ring = new TreeSet<>(Stat.nameComparator);

		final Set<Stat> spots = model.getTrackModel().trackSpots(trackID);
		for (final Stat s : spots) {
			final int fs = s.getFeature(Stat.FRAME).intValue();
			if (frame == fs && s != spot) {
				ring.add(s);
			}
		}

		if (!ring.isEmpty()) {
			Stat previousSibling = ring.floor(spot);
			if (null == previousSibling) {
				previousSibling = ring.last(); // loop
			}
			selectionModel.clearSelection();
			selectionModel.addSpotToSelection(previousSibling);
		}
	}

	public synchronized void previousInTime() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		final Set<Stat> predecessors = neighborIndex.predecessorsOf(spot);
		if (!predecessors.isEmpty()) {
			final Stat next = predecessors.iterator().next();
			selectionModel.clearSelection();
			selectionModel.addSpotToSelection(next);
		}
	}

	public synchronized void nextInTime() {
		final Stat spot = getASpot();
		if (null == spot) {
			return;
		}

		final Set<Stat> successors = neighborIndex.successorsOf(spot);
		if (!successors.isEmpty()) {
			final Stat next = successors.iterator().next();
			selectionModel.clearSelection();
			selectionModel.addSpotToSelection(next);
		}
	}

	/*
	 * STATIC METHODS
	 */

	/**
	 * Return a meaningful spot from the current selection, or <code>null</code>
	 * if the selection is empty.
	 */
	private Stat getASpot() {
		// Get it from spot selection
		final Set<Stat> spotSelection = selectionModel.getSpotSelection();
		if (!spotSelection.isEmpty()) {
			final Iterator<Stat> it = spotSelection.iterator();
			Stat spot = it.next();
			int minFrame = spot.getFeature(Stat.FRAME).intValue();
			while (it.hasNext()) {
				final Stat s = it.next();
				final int frame = s.getFeature(Stat.FRAME).intValue();
				if (frame < minFrame) {
					minFrame = frame;
					spot = s;
				}
			}
			return spot;
		}

		// Nope? Then get it from edges
		final Set<DefaultWeightedEdge> edgeSelection = selectionModel.getEdgeSelection();
		if (!edgeSelection.isEmpty()) {
			final Iterator<DefaultWeightedEdge> it = edgeSelection.iterator();
			final DefaultWeightedEdge edge = it.next();
			Stat spot = model.getTrackModel().getEdgeSource(edge);
			int minFrame = spot.getFeature(Stat.FRAME).intValue();
			while (it.hasNext()) {
				final DefaultWeightedEdge e = it.next();
				final Stat s = model.getTrackModel().getEdgeSource(e);
				final int frame = s.getFeature(Stat.FRAME).intValue();
				if (frame < minFrame) {
					minFrame = frame;
					spot = s;
				}
			}
			return spot;
		}

		// Still nothing? Then give up.
		return null;
	}
}
