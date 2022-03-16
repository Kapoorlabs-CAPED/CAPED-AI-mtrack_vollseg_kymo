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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.imglib2.algorithm.MultiThreaded;

/**
 * A utility class that wrap the {@link java.util.SortedMap} we use to store the
 * stats contained in each frame with a few utility methods.
 * <p>
 * Internally we rely on ConcurrentSkipListMap to allow concurrent access
 * without clashes.
 * <p>
 * This class is {@link MultiThreaded}. There are a few processes that can
 * benefit from multithreaded computation ({@link #filter(Collection)},
 * {@link #filter(FeatureFilter)}
 *
 * @author Jean-Yves Tinevez - Feb 2011 -2013. Revised December 2020.
 */
public class StatCollection implements MultiThreaded
{

	public static final Double ZERO = Double.valueOf( 0d );

	public static final Double ONE = Double.valueOf( 1d );

	public static final String VISIBILITY = "VISIBILITY";

	/**
	 * Time units for filtering and cropping operation timeouts. Filtering
	 * should not take more than 1 minute.
	 */
	private static final TimeUnit TIME_OUT_UNITS = TimeUnit.MINUTES;

	/**
	 * Time for filtering and cropping operation timeouts. Filtering should not
	 * take more than 1 minute.
	 */
	private static final long TIME_OUT_DELAY = 1;

	/** The frame by frame list of stat this object wrap. */
	private ConcurrentSkipListMap< String, Set< Stat > > content = new ConcurrentSkipListMap<>();

	private int numThreads;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Construct a new empty stat collection.
	 */
	public StatCollection()
	{
		setNumThreads();
	}

	/*
	 * METHODS
	 */

	/**
	 * Retrieves and returns the {@link Stat} object in this collection with the
	 * specified ID. Returns <code>null</code> if the stat cannot be found. All
	 * stats, visible or not, are searched for.
	 *
	 * @param ID
	 *            the ID to look for.
	 * @return the stat with the specified ID or <code>null</code> if this stat
	 *         does not exist or does not belong to this collection.
	 */
	public Stat search( final int ID )
	{
		/*
		 * Having a map id -> stat would be better, but we don't have a big need
		 * for this.
		 */
		for ( final Stat stat : iterable( false ) )
			if ( stat.ID() == ID )
				return stat;

		return null;
	}

	@Override
	public String toString()
	{
		String str = super.toString();
		str += ": contains " + getNstats( false ) + " stats total in "
				+ keySet().size() + " different frames, over which "
				+ getNstats( true ) + " are visible:\n";
		for ( final String key : content.keySet() )
			str += "\tfname " + key + ": ";

		return str;
	}

	/**
	 * Adds the given stat to this collection, at the specified frame, and mark
	 * it as visible.
	 * <p>
	 * If the frame does not exist yet in the collection, it is created and
	 * added. Upon adding, the added stat has its feature {@link Stat#FRAME}
	 * updated with the passed frame value.
	 * 
	 * @param stat
	 *            the stat to add.
	 * @param frame
	 *            the frame to add it to.
	 */
	public void add( final Stat stat, final String fname )
	{
		Set< Stat > stats = content.get( fname );
		if ( null == stats )
		{
			stats = new HashSet<>();
			content.put( fname, stats );
		}
		stats.add( stat );
		stat.putFeature( Stat.NAME, fname );
		stat.putFeature(VISIBILITY, String.valueOf(ONE));
	}
	
	
	/**
	 * Filters out the content of this collection using the specified
	 * {@link FeatureFilter}. Spots that are filtered out are marked as
	 * invisible, and visible otherwise.
	 *
	 * @param featurefilter
	 *            the filter to use.
	 */
	public final void filter( final FeatureFilter featurefilter )
	{

		final Collection< String > fnames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool( numThreads );

		for ( final String fname : fnames )
		{

			final Runnable command = new Runnable()
			{
				@Override
				public void run()
				{
					final Set< Stat > stats = content.get( fname );
					final double tval = featurefilter.value;

					if ( featurefilter.isAbove )
					{
						for ( final Stat stat : stats )
						{
							final Double val = Double.valueOf( stat.getFeature( featurefilter.feature ) );
							stat.putFeature( VISIBILITY, val.compareTo( tval ) < 0 ? String.valueOf( ZERO ) : String.valueOf( ONE ) );
						}

					}
					else
					{
						for ( final Stat stat : stats )
						{
							final Double val = Double.valueOf( stat.getFeature( featurefilter.feature ) );
							stat.putFeature( VISIBILITY, val.compareTo( tval ) > 0 ? String.valueOf( ZERO ) : String.valueOf( ONE ));
						}
					}
				}
			};
			executors.execute( command );
		}

		executors.shutdown();
		try
		{
			final boolean ok = executors.awaitTermination( TIME_OUT_DELAY, TIME_OUT_UNITS );
			if ( !ok )
				System.err.println( "[SpotCollection.filter()] Timeout of " + TIME_OUT_DELAY + " " + TIME_OUT_UNITS + " reached while filtering." );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	
	/**
	 * Filters out the content of this collection using the specified
	 * {@link FeatureFilter} collection. Spots that are filtered out are marked
	 * as invisible, and visible otherwise. To be marked as visible, a spot must
	 * pass <b>all</b> of the specified filters (AND chaining).
	 *
	 * @param filters
	 *            the filter collection to use.
	 */
	public final void filter( final Collection< FeatureFilter > filters )
	{

		final Collection< String > fnames = content.keySet();
		final ExecutorService executors = Executors.newFixedThreadPool( numThreads );

		for ( final String fname : fnames )
		{
			final Runnable command = new Runnable()
			{
				@Override
				public void run()
				{
					final Set< Stat > stats = content.get( fname );
					for ( final Stat stat : stats )
					{

						boolean shouldNotBeVisible = false;
						for ( final FeatureFilter featureFilter : filters )
						{

							final Double val =  Double.valueOf( stat.getFeature( featureFilter.feature ) );
							final double tval = featureFilter.value;
							final boolean isAbove = featureFilter.isAbove;

							if ( null == val || isAbove && val.compareTo( tval ) < 0 || !isAbove && val.compareTo( tval ) > 0 )
							{
								shouldNotBeVisible = true;
								break;
							}
						} // loop over filters
						stat.putFeature( VISIBILITY, shouldNotBeVisible ? String.valueOf( ZERO ) : String.valueOf( ONE ) );

					} // loop over spots
				}
			};
			executors.execute( command );
		}

		executors.shutdown();
		try
		{
			final boolean ok = executors.awaitTermination( TIME_OUT_DELAY, TIME_OUT_UNITS );
			if ( !ok )
				System.err.println( "[SpotCollection.filter()] Timeout of " + TIME_OUT_DELAY + " " + TIME_OUT_UNITS + " reached while filtering." );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Removes the given stat from this collection, at the specified frame.
	 * <p>
	 * If the stat frame collection does not exist yet, nothing is done and
	 * <code>false</code> is returned. If the stat cannot be found in the frame
	 * content, nothing is done and <code>false</code> is returned.
	 * 
	 * @param stat
	 *            the stat to remove.
	 * @param frame
	 *            the frame to remove it from.
	 * @return <code>true</code> if the stat was succesfully removed.
	 */
	public boolean remove( final Stat stat, final String fname )
	{
		final Set< Stat > stats = content.get( fname );
		if ( null == stats )
			return false;
		return stats.remove( stat );
	}

	/**
	 * Marks all the content of this collection as visible or invisible.
	 *
	 * @param visible
	 *            if true, all stats will be marked as visible.
	 */
	public void setVisible( final boolean visible )
	{
		final Double val = visible ? ONE : ZERO;
		final Collection< String > fnames = content.keySet();

		final ExecutorService executors = Executors.newFixedThreadPool( numThreads );
		for ( final String fname : fnames )
		{

			final Runnable command = new Runnable()
			{
				@Override
				public void run()
				{

					final Set< Stat > stats = content.get( fname );
					for ( final Stat stat : stats )
						stat.putFeature( VISIBILITY, String.valueOf(val) );
				}
			};
			executors.execute( command );
		}

		executors.shutdown();
		try
		{
			final boolean ok = executors.awaitTermination( TIME_OUT_DELAY, TIME_OUT_UNITS );
			if ( !ok )
				System.err.println( "[statCollection.setVisible()] Timeout of " + TIME_OUT_DELAY + " " + TIME_OUT_UNITS + " reached." );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	
	
	

	/**
	 * Returns the total number of stats in this collection, over all frames.
	 *
	 * @param visiblestatsOnly
	 *            if true, will only count visible stats. If false count all
	 *            stats.
	 * @return the total number of stats in this collection.
	 */
	public final int getNstats( final boolean visiblestatsOnly )
	{
		int nstats = 0;
		if ( visiblestatsOnly )
		{
			final Iterator< Stat > it = iterator( true );
			while ( it.hasNext() )
			{
				it.next();
				nstats++;
			}

		}
		else
		{
			for ( final Set< Stat > stats : content.values() )
				nstats += stats.size();
		}
		return nstats;
	}

	/**
	 * Returns the number of stats at the given frame.
	 *
	 * @param frame
	 *            the frame.
	 * @param visiblestatsOnly
	 *            if true, will only count visible stats. If false count all
	 *            stats.
	 * @return the number of stats at the given frame.
	 */
	public int getNstats( final String fname, final boolean visiblestatsOnly )
	{
		if ( visiblestatsOnly )
		{
			final Iterator< Stat > it = iterator( fname, true );
			int nstats = 0;
			while ( it.hasNext() )
			{
				it.next();
				nstats++;
			}
			return nstats;
		}

		final Set< Stat > stats = content.get( fname );
		if ( null == stats )
			return 0;

		return stats.size();
	}

	/*
	 * ITERABLE & co
	 */

	/**
	 * Return an iterator that iterates over all the stats contained in this
	 * collection.
	 *
	 * @param visiblestatsOnly
	 *            if true, the returned iterator will only iterate through
	 *            visible stats. If false, it will iterate over all stats.
	 * @return an iterator that iterates over this collection.
	 */
	public Iterator< Stat > iterator( final boolean visiblestatsOnly )
	{
		if ( visiblestatsOnly )
			return new VisiblestatsIterator();

		return new AllstatsIterator();
	}

	/**
	 * Return an iterator that iterates over the stats in the specified frame.
	 *
	 * @param visiblestatsOnly
	 *            if true, the returned iterator will only iterate through
	 *            visible stats. If false, it will iterate over all stats.
	 * @param frame
	 *            the frame to iterate over.
	 * @return an iterator that iterates over the content of a frame of this
	 *         collection.
	 */
	public Iterator< Stat > iterator( final String fname, final boolean visiblestatsOnly )
	{
		final Set< Stat > frameContent = content.get( fname );
		if ( null == frameContent )
			return EMPTY_ITERATOR;

		if ( visiblestatsOnly )
			return new VisiblestatsFrameIterator( frameContent );

		return frameContent.iterator();
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for this
	 * collection as a whole.
	 *
	 * @param visiblestatsOnly
	 *            if true, the iterable will contains only visible stats.
	 *            Otherwise, it will contain all the stats.
	 * @return an iterable view of this stat collection.
	 */
	public Iterable< Stat > iterable( final boolean visiblestatsOnly )
	{
		return new WholeCollectionIterable( visiblestatsOnly );
	}

	/**
	 * A convenience methods that returns an {@link Iterable} wrapper for a
	 * specific frame of this stat collection. The iterable is backed-up by the
	 * actual collection content, so modifying it can have unexpected results.
	 *
	 * @param visiblestatsOnly
	 *            if true, the iterable will contains only visible stats of the
	 *            specified frame. Otherwise, it will contain all the stats of
	 *            the specified frame.
	 * @param frame
	 *            the frame of the content the returned iterable will wrap.
	 * @return an iterable view of the content of a single frame of this stat
	 *         collection.
	 */
	public Iterable< Stat > iterable( final String fname, final boolean visiblestatsOnly )
	{
		if ( visiblestatsOnly )
			return new FrameVisibleIterable( fname );

		return content.get( fname );
	}

	/*
	 * SORTEDMAP
	 */

	/**
	 * Stores the specified stats as the content of the specified frame. The
	 * added stats are all marked as not visible. Their {@link Stat#FRAME} is
	 * updated to be the specified frame.
	 *
	 * @param frame
	 *            the frame to store these stats at. The specified stats replace
	 *            the previous content of this frame, if any.
	 * @param stats
	 *            the stats to store.
	 */
	public void put( final String fname, final Collection< Stat > stats )
	{
		final Set< Stat > value = new HashSet<>( stats );
		for ( final Stat stat : value )
		{
			stat.putFeature( Stat.NAME, fname  );
			stat.putFeature( VISIBILITY, String.valueOf( ZERO ) );
		}
		content.put( fname, value );
	}

	/**
	 * Returns the first (lowest) frame currently in this collection.
	 *
	 * @return the first (lowest) frame currently in this collection.
	 */
	public String firstKey()
	{
		if ( content.isEmpty() )
			return String.valueOf( 0 );
		return content.firstKey();
	}

	/**
	 * Returns the last (highest) frame currently in this collection.
	 *
	 * @return the last (highest) frame currently in this collection.
	 */
	public String lastKey()
	{
		if ( content.isEmpty() )
			return  String.valueOf( 0 ) ;
		return content.lastKey();
	}

	/**
	 * Returns a NavigableSet view of the frames contained in this collection.
	 * The set's iterator returns the keys in ascending order. The set is backed
	 * by the map, so changes to the map are reflected in the set, and
	 * vice-versa. The set supports element removal, which removes the
	 * corresponding mapping from the map, via the Iterator.remove, Set.remove,
	 * removeAll, retainAll, and clear operations. It does not support the add
	 * or addAll operations.
	 * <p>
	 * The view's iterator is a "weakly consistent" iterator that will never
	 * throw ConcurrentModificationException, and guarantees to traverse
	 * elements as they existed upon construction of the iterator, and may (but
	 * is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 *
	 * @return a navigable set view of the frames in this collection.
	 */
	public NavigableSet< String > keySet()
	{
		return content.keySet();
	}

	/**
	 * Removes all the content from this collection.
	 */
	public void clear()
	{
		content.clear();
	}

	/*
	 * MULTITHREADING
	 */

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	/*
	 * PRIVATE CLASSES
	 */

	private class AllstatsIterator implements Iterator< Stat >
	{

		private boolean hasNext = true;

		private final Iterator< String > frameIterator;

		private Iterator< Stat > contentIterator;

		private Stat next = null;

		public AllstatsIterator()
		{
			this.frameIterator = content.keySet().iterator();
			if ( !frameIterator.hasNext() )
			{
				hasNext = false;
				return;
			}
			final Set< Stat > currentFrameContent = content.get( frameIterator.next() );
			contentIterator = currentFrameContent.iterator();
			iterate();
		}

		private void iterate()
		{
			while ( true )
			{

				// Is there still stats in current content?
				if ( !contentIterator.hasNext() )
				{
					// No. Then move to next frame.
					// Is there still frames to iterate over?
					if ( !frameIterator.hasNext() )
					{
						// No. Then we are done
						hasNext = false;
						next = null;
						return;
					}

					contentIterator = content.get( frameIterator.next() ).iterator();
					continue;
				}
				next = contentIterator.next();
				return;
			}
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public Stat next()
		{
			final Stat toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException( "Remove operation is not supported for statCollection iterators." );
		}
	}

	private class VisiblestatsIterator implements Iterator< Stat >
	{

		private boolean hasNext = true;

		private final Iterator< String > frameIterator;

		private Iterator< Stat > contentIterator;

		private Stat next = null;

		private Set< Stat > currentFrameContent;

		public VisiblestatsIterator()
		{
			this.frameIterator = content.keySet().iterator();
			if ( !frameIterator.hasNext() )
			{
				hasNext = false;
				return;
			}
			currentFrameContent = content.get( frameIterator.next() );
			contentIterator = currentFrameContent.iterator();
			iterate();
		}

		private void iterate()
		{

			while ( true )
			{
				// Is there still stats in current content?
				if ( !contentIterator.hasNext() )
				{
					// No. Then move to next frame.
					// Is there still frames to iterate over?
					if ( !frameIterator.hasNext() )
					{
						// No. Then we are done
						hasNext = false;
						next = null;
						return;
					}

					// Yes. Then start iterating over the next frame.
					currentFrameContent = content.get( frameIterator.next() );
					contentIterator = currentFrameContent.iterator();
					continue;
				}
				next = contentIterator.next();
				// Is it visible?
				if ( next.getFeature( VISIBILITY ).compareTo( String.valueOf( ZERO )  ) > 0 )
				{
					// Yes! Be happy and return
					return;
				}
			}
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public Stat next()
		{
			final Stat toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException( "Remove operation is not supported for statCollection iterators." );
		}
	}

	private class VisiblestatsFrameIterator implements Iterator< Stat >
	{

		private boolean hasNext = true;

		private Stat next = null;

		private final Iterator< Stat > contentIterator;

		public VisiblestatsFrameIterator( final Set< Stat > frameContent )
		{
			this.contentIterator = ( null == frameContent ) ? EMPTY_ITERATOR : frameContent.iterator();
			iterate();
		}

		private void iterate()
		{
			while ( true )
			{
				if ( !contentIterator.hasNext() )
				{
					// No. Then we are done
					hasNext = false;
					next = null;
					return;
				}
				next = contentIterator.next();
				// Is it visible?
				if ( next.getFeature( VISIBILITY ).compareTo( String.valueOf( ZERO )  ) > 0 )
				{
					// Yes. Be happy, and return.
					return;
				}
			}
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public Stat next()
		{
			final Stat toReturn = next;
			iterate();
			return toReturn;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException( "Remove operation is not supported for statCollection iterators." );
		}
	}

	/**
	 * Remove all the non-visible stats of this collection.
	 */
	public void crop()
	{
		final Collection< String > fnames = content.keySet();
		for ( final String fname : fnames )
		{
			final Set< Stat > fc = content.get( fname );
			final List< Stat > toRemove = new ArrayList<>();
			for ( final Stat stat : fc )
				if ( !isVisible( stat ) )
					toRemove.add( stat );

			fc.removeAll( toRemove );
		}
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this stat
	 * collection.
	 */
	private final class WholeCollectionIterable implements Iterable< Stat >
	{

		private final boolean visiblestatsOnly;

		public WholeCollectionIterable( final boolean visiblestatsOnly )
		{
			this.visiblestatsOnly = visiblestatsOnly;
		}

		@Override
		public Iterator< Stat > iterator()
		{
			if ( visiblestatsOnly )
				return new VisiblestatsIterator();

			return new AllstatsIterator();
		}
	}

	/**
	 * A convenience wrapper that implements {@link Iterable} for this stat
	 * collection.
	 */
	private final class FrameVisibleIterable implements Iterable< Stat >
	{

		private final String fname;

		public FrameVisibleIterable( final String fname )
		{
			this.fname = fname;
		}

		@Override
		public Iterator< Stat > iterator()
		{
			return new VisiblestatsFrameIterator( content.get( fname ) );
		}
	}

	private static final Iterator< Stat > EMPTY_ITERATOR = new Iterator< Stat >()
	{

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Stat next()
		{
			return null;
		}

		@Override
		public void remove()
		{}
	};

	/*
	 * STATIC METHODS
	 */

	/**
	 * Creates a new {@link StatCollection} containing only the specified stats.
	 * Their frame origin is retrieved from their {@link Stat#FRAME} feature, so
	 * it must be set properly for all stats. All the stats of the new
	 * collection have the same visibility that the one they carry.
	 *
	 * @param stats
	 *            the stat collection to build from.
	 * @return a new {@link StatCollection} instance.
	 */
	public static StatCollection fromCollection( final Iterable< Stat > stats )
	{
		final StatCollection sc = new StatCollection();
		for ( final Stat stat : stats )
		{
			final String fname = stat.getFeature( Stat.NAME );
			Set< Stat > fc = sc.content.get( fname );
			if ( null == fc )
			{
				fc = new HashSet<>();
				sc.content.put( fname, fc );
			}
			fc.add( stat );
		}
		return sc;
	}

	/**
	 * Creates a new {@link StatCollection} from a copy of the specified map of
	 * sets. The stats added this way are completely untouched. In particular,
	 * their {@link #VISIBILITY} feature is left untouched, which makes this
	 * method suitable to de-serialize a {@link StatCollection}.
	 *
	 * @param source
	 *            the map to buidl the stat collection from.
	 * @return a new statCollection.
	 */
	public static StatCollection fromMap( final Map< String, Set< Stat > > source )
	{
		final StatCollection sc = new StatCollection();
		sc.content = new ConcurrentSkipListMap<>( source );
		return sc;
	}

	private static final boolean isVisible( final Stat stat )
	{
		return stat.getFeature( VISIBILITY ).compareTo( String.valueOf( ZERO )  ) > 0;
	}
}
