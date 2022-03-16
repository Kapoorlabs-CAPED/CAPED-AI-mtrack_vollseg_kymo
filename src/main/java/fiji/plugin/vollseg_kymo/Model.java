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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;



/**
 * <h1>The model for the data managed by TrackMate trackmate.</h1>
 * <p>
 * This is a relatively large class, with a lot of public methods. This
 * complexity arose because this class handles data storage and manipulation,
 * through user manual editing and automatic processing. To avoid conflicting
 * accesses to the data, some specialized methods had to be created, hopefully
 * built in coherent sets.
 *
 * @author Jean-Yves Tinevez &lt;tinevez@pasteur.fr&gt; - 2010-2013
 *
 */
public class Model
{

	/*
	 * CONSTANTS
	 */
	// FEATURES

	private final FeatureModel featureModel;
	private static final boolean DEBUG = false;

	/*
	 * FIELDS
	 */

	// STATS

	/** The stats managed by this model. */
	protected StatCollection stats = new StatCollection();

	// TRANSACTION MODEL

	/**
	 * Counter for the depth of nested transactions. Each call to beginUpdate
	 * increments this counter and each call to endUpdate decrements it. When
	 * the counter reaches 0, the transaction is closed and the respective
	 * events are fired. Initial value is 0.
	 */
	private int updateLevel = 0;

	private final HashSet< Stat > statsAdded = new HashSet< >();
	
	private final HashSet< Stat > statsRemoved = new HashSet< >();

	private final HashSet< Stat > statsMoved = new HashSet< >();

	private final HashSet< Stat > statsUpdated = new HashSet< >();
	


	/**
	 * The event cache. During a transaction, some modifications might trigger
	 * the need to fire a model change event. We want to fire these events only
	 * when the transaction closes (when the updateLevel reaches 0), so we store
	 * the event ID in this cache in the meantime. The event cache contains only
	 * the int IDs of the events listed in {@link ModelChangeEvent}, namely
	 * <ul>
	 * <li> {@link ModelChangeEvent#stats_COMPUTED}
	 * <li> {@link ModelChangeEvent#TRACKS_COMPUTED}
	 * <li> {@link ModelChangeEvent#TRACKS_VISIBILITY_CHANGED}
	 * </ul>
	 * The {@link ModelChangeEvent#MODEL_MODIFIED} cannot be cached this way,
	 * for it needs to be configured with modification Stat and edge targets, so
	 * it uses a different system (see {@link #flushUpdate()}).
	 */
	private final HashSet< Integer > eventCache = new HashSet< >();

	// OTHERS

	/** The logger to append processes messages. */
	private Logger logger = Logger.DEFAULT_LOGGER;

	private String spaceUnits = "pixels";

	private String timeUnits = "frames";

	// LISTENERS

	/**
	 * The list of listeners listening to model content change.
	 */
	Set< ModelChangeListener > modelChangeListeners = new LinkedHashSet< >();

	/*
	 * CONSTRUCTOR
	 */

	public Model()
	{
		featureModel = createFeatureModel();
	}

	/*
	 * HOOKS
	 */

	/**
	 * Instantiates a blank {@link TrackModel} to use whithin this model.
	 * <p>
	 * Subclassers can override this method to have the model work with their
	 * own subclass of {@link TrackModel}.
	 *
	 * @return a new instance of {@link TrackModel}.

	/**
	 * Instantiates a blank {@link FeatureModel} to use whithin this model.
	 * <p>
	 * Subclassers can override this method to have the model work with their
	 * own subclass of {@link FeatureModel}.
	 * 
	 * @return a new instance of {@link FeatureModel}.
	 */
	protected FeatureModel createFeatureModel()
	{
		return new FeatureModel( this );
	}

	/*
	 * UTILS METHODS
	 */

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();

		str.append( '\n' );
		if ( null == stats || stats.keySet().size() == 0 )
		{
			str.append( "No stats.\n" );
		}
		else
		{
			str.append( "Contains " + stats.getNstats( false ) + " stats in total.\n" );
		}
		if ( stats.getNstats( true ) == 0 )
		{
			str.append( "No filtered stats.\n" );
		}
		else
		{
			str.append( "Contains " + stats.getNstats( true ) + " filtered stats.\n" );
		}

		str.append( '\n' );

		str.append( '\n' );
		str.append( "Physical units:\n  space units: " + spaceUnits + "\n  time units: " + timeUnits + '\n' );

		str.append( '\n' );
		str.append( featureModel.toString() );

		return str.toString();
	}

	/*
	 * DEAL WITH MODEL CHANGE LISTENER
	 */

	public void addModelChangeListener( final ModelChangeListener listener )
	{
		modelChangeListeners.add( listener );
	}

	public boolean removeModelChangeListener( final ModelChangeListener listener )
	{
		return modelChangeListeners.remove( listener );
	}

	public Set< ModelChangeListener > getModelChangeListener()
	{
		return modelChangeListeners;
	}

	/*
	 * PHYSICAL UNITS
	 */

	/**
	 * Sets the physical units for the quantities stored in this model.
	 *
	 * @param spaceUnits
	 *            the spatial units (e.g. μm).
	 * @param timeUnits
	 *            the time units (e.g. min).
	 */
	public void setPhysicalUnits( final String spaceUnits, final String timeUnits )
	{
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
	}

	/**
	 * Returns the spatial units for the quantities stored in this model.
	 *
	 * @return the spatial units.
	 */
	public String getSpaceUnits()
	{
		return spaceUnits;
	}

	/**
	 * Returns the time units for the quantities stored in this model.
	 *
	 * @return the time units.
	 */
	public String getTimeUnits()
	{
		return timeUnits;
	}

	/*
	 * GRAPH MODIFICATION
	 */

	public synchronized void beginUpdate()
	{
		updateLevel++;
		if ( DEBUG )
			System.out.println( "[TrackMateModel] #beginUpdate: increasing update level to " + updateLevel + "." );
	}

	public synchronized void endUpdate()
	{
		updateLevel--;
		if ( DEBUG )
			System.out.println( "[TrackMateModel] #endUpdate: decreasing update level to " + updateLevel + "." );
		if ( updateLevel == 0 )
		{
			if ( DEBUG )
				System.out.println( "[TrackMateModel] #endUpdate: update level is 0, calling flushUpdate()." );
			flushUpdate();
		}
	}

	

	
	/*
	 * GETTERS / SETTERS FOR stats
	 */

	/**
	 * Returns the Stat collection managed by this model.
	 *
	 * @return the Stat collection managed by this model.
	 */
	public StatCollection getstats()
	{
		return stats;
	}

	/**
	 * Removes all the stats from this model.
	 *
	 * @param doNotify
	 *            if <code>true</code>, model listeners will be notified with a
	 *            {@link ModelChangeEvent#stats_COMPUTED} event.
	 */
	public void clearstats( final boolean doNotify )
	{
		stats.clear();
		if ( doNotify )
		{
			final ModelChangeEvent event = new ModelChangeEvent( this, ModelChangeEvent.StatS_COMPUTED );
			for ( final ModelChangeListener listener : modelChangeListeners )
				listener.modelChanged( event );
		}
	}

	/**
	 * Set the {@link StatCollection} managed by this model.
	 *
	 * @param doNotify
	 *            if true, will file a {@link ModelChangeEvent#stats_COMPUTED}
	 *            event.
	 * @param stats
	 *            the {@link StatCollection} to set.
	 */
	public void setstats( final StatCollection stats, final boolean doNotify )
	{
		this.stats = stats;
		if ( doNotify )
		{
			final ModelChangeEvent event = new ModelChangeEvent( this, ModelChangeEvent.StatS_COMPUTED );
			for ( final ModelChangeListener listener : modelChangeListeners )
				listener.modelChanged( event );
		}
	}

	/**
	 * Filters the {@link StatCollection} managed by this model with the
	 * {@link FeatureFilter}s specified.
	 *
	 * @param StatFilters
	 *            the {@link FeatureFilter} collection to use for filtering.
	 * @param doNotify
	 *            if true, will file a {@link ModelChangeEvent#stats_FILTERED}
	 *            event.
	 */
	public void filterstats( final Collection< FeatureFilter > StatFilters, final boolean doNotify )
	{
		stats.filter( StatFilters );
		if ( doNotify )
		{
			final ModelChangeEvent event = new ModelChangeEvent( this, ModelChangeEvent.StatS_FILTERED );
			for ( final ModelChangeListener listener : modelChangeListeners )
				listener.modelChanged( event );
		}

	}

	/**
	 * Notify the {@link ModelChangeListener}s of this model that feature values
	 * have been computed. This method serves as a manual trigger for this
	 * event.
	 */
	public void notifyFeaturesComputed()
	{
		final ModelChangeEvent event = new ModelChangeEvent( this, ModelChangeEvent.FEATURES_COMPUTED );
		for ( final ModelChangeListener listener : modelChangeListeners )
			listener.modelChanged( event );
	}

	/*
	 * LOGGER
	 */

	/**
	 * Set the logger that will receive the messages from the processes
	 * occurring within this trackmate.
	 * 
	 * @param logger
	 *            the {@link Logger} to use.
	 */
	public void setLogger( final Logger logger )
	{
		this.logger = logger;
	}

	/**
	 * Return the logger currently set for this model.
	 * 
	 * @return the {@link Logger} used.
	 */
	public Logger getLogger()
	{
		return logger;
	}

	/*
	 * FEATURES
	 */

	public FeatureModel getFeatureModel()
	{
		return featureModel;
	}

	/*
	 * MODEL CHANGE METHODS
	 */

	/**
	 * Moves a single Stat from a frame to another, make it visible if it was
	 * not, then mark it for feature update. If the source Stat could not be
	 * found in the source frame, nothing is done and <code>null</code> is
	 * returned.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param StatToMove
	 *            the Stat to move
	 * @param fromFrame
	 *            the frame the Stat originated from
	 * @param toFrame
	 *            the destination frame
	 * @return the Stat that was moved, or <code>null</code> if it could not be
	 *         found in the source frame
	 */
	public synchronized Stat moveStatFrom( final Stat StatToMove, final String fromFile )
	{
		final boolean ok = stats.remove( StatToMove, fromFile );
		if ( !ok )
		{
			if ( DEBUG )
			{
				System.err.println( "[Vollseg_kymo_model] Could not find Stat " + StatToMove + " in frame " + fromFile );
			}
			return null;
		}
		stats.add( StatToMove, fromFile );
		if ( DEBUG )
		{
			System.out.println( "[Vollseg_kymo_model] Moving " + StatToMove + " from file " + fromFile );
		}

		statsMoved.add( StatToMove );
		return StatToMove;
	}

	/**
	 * Adds a single Stat to the collections managed by this model, mark it as
	 * visible, then update its features.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 * 
	 * @param StatToAdd
	 *            the Stat to add.
	 * @param toFrame
	 *            the frame to add it to.
	 *
	 * @return the Stat just added.
	 */
	public synchronized Stat addStatTo( final Stat StatToAdd, final String toFile )
	{
		stats.add( StatToAdd, toFile );
		statsAdded.add( StatToAdd ); // TRANSACTION
		if ( DEBUG )
		{
			System.out.println( "[TrackMateModel] Adding Stat " + StatToAdd + " to frame " + toFile );
		}
		return StatToAdd;
	}

	/**
	 * Removes a single Stat from the collections managed by this model. If the
	 * Stat cannot be found, nothing is done and <code>null</code> is returned.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param StatToRemove
	 *            the Stat to remove.
	 * @return the Stat removed, or <code>null</code> if it could not be found.
	 */
	public synchronized Stat removeStat( final Stat StatToRemove )
	{
		final String fromFile = StatToRemove.getFeature( Stat.NAME );
		if ( stats.remove( StatToRemove, fromFile ) )
		{
			statsRemoved.add( StatToRemove ); // TRANSACTION
			if ( DEBUG )
				System.out.println( "[Vollseg_kymo_model] Removing Stat " + StatToRemove + " from frame " + fromFile );

			// changes to edges will be caught automatically by the TrackGraphModel
			return StatToRemove;
		}
		if ( DEBUG )
			System.err.println( "[Vollseg_kymo_model] The Stat " + StatToRemove + " cannot be found in frame " + fromFile );

		return null;
	}

	/**
	 * Mark the specified Stat for update. At the end of the model transaction,
	 * its features will be recomputed, and other edge and track features that
	 * depends on it will be as well.
	 * <p>
	 * For the model update to happen correctly and listeners to be notified
	 * properly, a call to this method must happen within a transaction, as in:
	 *
	 * <pre>
	 * model.beginUpdate();
	 * try {
	 * 	... // model modifications here
	 * } finally {
	 * 	model.endUpdate();
	 * }
	 * </pre>
	 *
	 * @param StatToUpdate
	 *            the Stat to mark for update
	 */
	public synchronized void updateFeatures( final Stat StatToUpdate )
	{
		statsUpdated.add( StatToUpdate ); // Enlist for feature update when
											// transaction is marked as finished
		
	}

	
	/**
	 * Returns a copy of this model.
	 * <p>
	 * The copy is made of the same Stat objects but on a different graph, that
	 * can be safely edited. The copy does not include the feature values for
	 * edges and tracks, but the features are declared.
	 * 
	 * @return a new model.
	 */
	public Model copy()
	{
		final Model copy = new Model();

		// Physical units.
		copy.setPhysicalUnits( spaceUnits, timeUnits );

		// stats.
		final StatCollection stats2 = StatCollection.fromCollection( stats.iterable( true ) );
		copy.setstats( stats2, false );

		// Feature model.
		final FeatureModel fm2 = copy.getFeatureModel();
		fm2.declarestatFeatures(
				featureModel.getstatFeatures(),
				featureModel.getstatFeatureNames(),
				featureModel.getstatFeatureShortNames(),
				featureModel.getstatFeatureDimensions(),
				featureModel.getstatFeatureIsInt() );
	
		
		// Feature values are not copied.
		return copy;
	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * Fire events. Regenerate fields derived from the filtered graph.
	 */
	private void flushUpdate()
	{

		if ( DEBUG )
		{
			System.out.println( "[Vollseg_kymo_model] #flushUpdate()." );
			System.out.println( "[Vollseg_kymo_model] #flushUpdate(): Event cache is :" + eventCache );
		}

	

		// Deal with new or moved stats: we need to update their features.
		final int nstatsToUpdate = statsAdded.size() + statsMoved.size() + statsUpdated.size();
		if ( nstatsToUpdate > 0 )
		{
			final HashSet< Stat > statsToUpdate = new HashSet< >( nstatsToUpdate );
			statsToUpdate.addAll( statsAdded );
			statsToUpdate.addAll( statsMoved );
			statsToUpdate.addAll( statsUpdated );
		}

		// Initialize event
		final ModelChangeEvent event = new ModelChangeEvent( this, ModelChangeEvent.MODEL_MODIFIED );

		// Configure it with stats to signal.
		final int nstatsToSignal = nstatsToUpdate + statsRemoved.size();
		if ( nstatsToSignal > 0 )
		{
			event.addAllStats( statsAdded );
			event.addAllStats( statsRemoved );
			event.addAllStats( statsMoved );
			event.addAllStats( statsUpdated );

			for ( final Stat Stat : statsAdded )
			{
				event.putStatFlag( Stat, ModelChangeEvent.FLAG_Stat_ADDED );
			}
			for ( final Stat Stat : statsRemoved )
			{
				event.putStatFlag( Stat, ModelChangeEvent.FLAG_Stat_REMOVED );
			}
			for ( final Stat Stat : statsMoved )
			{
				event.putStatFlag( Stat, ModelChangeEvent.FLAG_Stat_FILE_CHANGED );
			}
			for ( final Stat Stat : statsUpdated )
			{
				event.putStatFlag( Stat, ModelChangeEvent.FLAG_Stat_MODIFIED );
			}
		}

		

		
			statsAdded.clear();
			statsRemoved.clear();
			statsMoved.clear();
			statsUpdated.clear();
			eventCache.clear();
	}

}
