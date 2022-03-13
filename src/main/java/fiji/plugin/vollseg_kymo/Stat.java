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

import static fiji.plugin.vollseg_kymo.StatCollection.VISIBILITY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import fiji.plugin.vollseg_kymo.util.AlphanumComparator;
import net.imglib2.AbstractEuclideanSpace;

/**
 * 
 * Each stat received at creation a unique ID (as an <code>int</code>), used
 * later for saving, retrieving and loading. Interfering with this value will
 * predictively cause undesired behavior.
 *
 * @author V Kapoor
 *
 */
public class Stat extends AbstractEuclideanSpace implements  Comparable< Stat >
{

	/*
	 * FIELDS
	 */

	public static AtomicInteger IDcounter = new AtomicInteger( -1 );

	/** Store the individual features, and their values. */
	private final ConcurrentHashMap< String, String > features = new ConcurrentHashMap< >();

	/** A user-supplied name for this stat. */
	public String name;

	/** This stat ID. */
	private final int ID;

	

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Creates a new stat.
	 *
	 * @param rate
	 *            the stat rate, in image units.
	 * @param start time event
	 *            the stat start time, in image units.
	 * @param end time event
	 *            the stat end time, in image units.
	 * @param average growth rate
	 *            the stat average growth rate, in image units.
	 * @param average shrink rate
	 *            the stat average shrink rate, in image units.
	 * @param catastrophe frequency
	 *            the stat catastrophe frequency, in image units.
	 * @param rescue frequency
	 *            the stat rescue frequency, in image units.           
	 *            
	 */
	public Stat( final double rate, final int start_time, final int end_time, final double average_growth_rate, final double average_shrink_rate, final double catastrophe_frequency,
			final double rescue_frequency, final String name)
	{
		super( 3 );
		this.ID = IDcounter.incrementAndGet();
		putFeature( RATE, String.valueOf(Double.valueOf( rate ) ));
		putFeature( START_TIME, String.valueOf(Double.valueOf( start_time ) ));
		putFeature( END_TIME, String.valueOf(Double.valueOf( end_time ) ));
		putFeature( AVERAGE_GROWTH_RATE, String.valueOf(Double.valueOf( average_growth_rate ) ));
		putFeature( AVERAGE_SHRINK_RATE, String.valueOf(Double.valueOf( average_shrink_rate ) ));
		putFeature( CATASTROPHE_FREQUENCY, String.valueOf(Double.valueOf( catastrophe_frequency ) ));
		putFeature( RESCUE_FREQUENCY, String.valueOf(Double.valueOf( rescue_frequency ) ));
		putFeature(NAME, name);
		

	
	}

	/**
	 * Blank constructor meant to be used when loading a stat collection from a
	 * file. <b>Will</b> mess with the {@link #IDcounter} field, so this
	 * constructor <u>should not be used for normal stat creation</u>.
	 *
	 * @param ID
	 *            the stat ID to set
	 */
	public Stat( final int ID )
	{
		super( 3 );
		this.ID = ID;
		synchronized ( IDcounter )
		{
			if ( IDcounter.get() < ID )
			{
				IDcounter.set( ID );
			}
		}
	}

	/*
	 * PUBLIC METHODS
	 */

	@Override
	public int hashCode()
	{
		return ID;
	}

	@Override
	public int compareTo( final Stat o )
	{
		return ID - o.ID;
	}

	@Override
	public boolean equals( final Object other )
	{
		if ( other == null )
			return false;
		if ( other == this )
			return true;
		if ( !( other instanceof Stat ) )
			return false;
		final Stat os = ( Stat ) other;
		return os.ID == this.ID;
	}

	

	

	public int ID()
	{
		return ID;
	}

	@Override
	public String toString()
	{
		String str;
		if ( null == name || name.equals( "" ) )
			str = "ID" + ID;
		else
			str = name;
		return str;
	}

	/**
	 * Return a string representation of this stat, with calculated features.
	 * 
	 * @return a string representation of the stat.
	 */
	public String echo()
	{
		final StringBuilder s = new StringBuilder();

		// Name
		if ( null == name )
			s.append( "stat: <no name>\n" );
		else
			s.append( "stat: " + name + "\n" );

		// Frame
		s.append( "Start Time: " + getFeature( START_TIME ) + '\n' );
		s.append( "End Time: " + getFeature( END_TIME ) + '\n' );
		// Coordinates

		// Feature list
		if ( null == features || features.size() < 1 )
			s.append( "No features calculated\n" );
		else
		{
			s.append( "Feature list:\n" );
			double val;
			for ( final String key : features.keySet() )
			{
				s.append( "\t" + key.toString() + ": " );
				val = Double.valueOf(features.get( key ));
				if ( val >= 1e4 )
					s.append( String.format( "%.1g", val ) );
				else
					s.append( String.format( "%.1f", val ) );
				s.append( '\n' );
			}
		}
		return s.toString();
	}

	/*
	 * FEATURE RELATED METHODS
	 */

	/**
	 * Exposes the storage map of features for this stat. Altering the returned
	 * map will alter the stat.
	 *
	 * @return a map of {@link String}s to {@link Double}s.
	 */
	public Map< String, String > getFeatures()
	{
		return features;
	}

	/**
	 * Returns the value corresponding to the specified stat feature.
	 *
	 * @param feature
	 *            The feature string to retrieve the stored value for.
	 * @return the feature value, as a {@link Double}. Will be <code>null</code>
	 *         if it has not been set.
	 */
	public String getFeature( final String feature )
	{
		return features.get( feature );
	}

	/**
	 * Stores the specified feature value for this stat.
	 *
	 * @param feature
	 *            the name of the feature to store, as a {@link String}.
	 * @param value
	 *            the value to store, as a {@link Double}. Using
	 *            <code>null</code> will have unpredicted outcomes.
	 */
	public void putFeature( final String feature, final String value )
	{
		features.put( feature, value );
	}

	/**
	 * Returns the difference of the feature value for this stat with the one of
	 * the specified stat. By construction, this operation is anti-symmetric (
	 * <code>A.diffTo(B) = - B.diffTo(A)</code>).
	 * <p>
	 * Will generate a {@link NullPointerException} if one of the stats does not
	 * store the named feature.
	 *
	 * @param s
	 *            the stat to compare to.
	 * @param feature
	 *            the name of the feature to use for calculation.
	 * @return the difference in feature value.
	 */
	public double diffTo( final Stat s, final String feature )
	{
		final double f1 = Double.valueOf(features.get( feature ));
		final double f2 = Double.valueOf(s.getFeature( feature ));
		return f1 - f2;
	}

	/**
	 * Returns the absolute normalized difference of the feature value of this
	 * stat with the one of the given stat.
	 * <p>
	 * If <code>a</code> and <code>b</code> are the feature values, then the
	 * absolute normalized difference is defined as
	 * <code>Math.abs( a - b) / ( (a+b)/2 )</code>.
	 * <p>
	 * By construction, this operation is symmetric (
	 * <code>A.normalizeDiffTo(B) =
	 * B.normalizeDiffTo(A)</code>).
	 * <p>
	 * Will generate a {@link NullPointerException} if one of the stats does not
	 * store the named feature.
	 *
	 * @param s
	 *            the stat to compare to.
	 * @param feature
	 *            the name of the feature to use for calculation.
	 * @return the absolute normalized difference feature value.
	 */
	public double normalizeDiffTo( final Stat s, final String feature )
	{
		final double a = Double.valueOf(features.get( feature ));
		final double b = Double.valueOf(s.getFeature( feature ));
		if ( a == -b )
			return 0d;
		
		return Math.abs( a - b ) / ( ( a + b ) / 2 );
	}

	

	/*
	 * PUBLIC UTILITY CONSTANTS
	 */

	/*
	 * STATIC KEYS
	 */

	/** The name of the stat quality feature. */
	public static final String RATE = "RATE";

	/** The name of the radius stat feature. */
	public static final String START_TIME = "START_TIME";

	/** The name of the stat X position feature. */
	public static final String END_TIME = "END_TIME";

	/** The name of the stat Y position feature. */
	public static final String AVERAGE_GROWTH_RATE = "AVERAGE_GROWTH_RATE";

	/** The name of the stat Z position feature. */
	public static final String AVERAGE_SHRINK_RATE = "AVERAGE_SHRINK_RATE";

	/** The name of the stat T position feature. */
	public static final String CATASTROPHE_FREQUENCY = "CATASTROPHE_FREQUENCY";

	/** The name of the frame feature. */
	public static final String RESCUE_FREQUENCY = "RESCUE_FREQUENCY";
	
	/** The name feature. */
	public static final String NAME = "NAME";
	

	/** The position features. */
	public final static String[] STAT_FEATURES = new String[] { AVERAGE_GROWTH_RATE, AVERAGE_SHRINK_RATE, CATASTROPHE_FREQUENCY, RESCUE_FREQUENCY  };
	/**
	 * The 7 privileged stat features that must be set by a stat detector:
	 * {@link #RATE}, {@link #START_TIME}, {@link #END_TIME},
	 * {@link #AVERAGE_GROWTH_RATE}, {@link #AVERAGE_SHRINK_RATE}, {@link #CATASTROPHE_FREQUENCY}, {@link #RESCUE_FREQUENCY}
	 * .
	 */
	public final static Collection< String > FEATURES = new ArrayList< >( 7 );

	/** The 7 privileged stat feature names. */
	public final static Map< String, String > FEATURE_NAMES = new HashMap< >( 7 );

	/** The 7 privileged stat feature short names. */
	public final static Map< String, String > FEATURE_SHORT_NAMES = new HashMap< >( 7 );

	/** The 7 privileged stat feature dimensions. */
	public final static Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap< >( 7 );

	/** The 7 privileged stat feature isInt flags. */
	public final static Map< String, Boolean > IS_INT = new HashMap< >( 7 );

	static
	{
		FEATURES.add( RATE );
		FEATURES.add( START_TIME );
		FEATURES.add( END_TIME );
		FEATURES.add( AVERAGE_GROWTH_RATE );
		FEATURES.add( AVERAGE_SHRINK_RATE );
		FEATURES.add( CATASTROPHE_FREQUENCY );
		FEATURES.add( RESCUE_FREQUENCY );
		FEATURES.add( StatCollection.VISIBILITY );

		FEATURE_NAMES.put( RATE, "R" );
		FEATURE_NAMES.put( START_TIME, "Start_time" );
		FEATURE_NAMES.put( END_TIME, "End_time" );
		FEATURE_NAMES.put( AVERAGE_GROWTH_RATE, "Average_growth_rate" );
		FEATURE_NAMES.put( AVERAGE_SHRINK_RATE, "Average_shrink_rate" );
		FEATURE_NAMES.put( CATASTROPHE_FREQUENCY, "Catastrophe_frequency" );
		FEATURE_NAMES.put( RESCUE_FREQUENCY, "Rescue_frequency" );
		FEATURE_NAMES.put( VISIBILITY, "Visibility" );

		FEATURE_SHORT_NAMES.put( RATE, "R"  );
		FEATURE_SHORT_NAMES.put( START_TIME, "Start_time"  );
		FEATURE_SHORT_NAMES.put( END_TIME, "End_time" );
		FEATURE_SHORT_NAMES.put( AVERAGE_GROWTH_RATE, "Average_growth_rate" );
		FEATURE_SHORT_NAMES.put( AVERAGE_SHRINK_RATE, "Average_shrink_rate" );
		FEATURE_SHORT_NAMES.put( CATASTROPHE_FREQUENCY, "Catastrophe_frequency" );
		FEATURE_SHORT_NAMES.put( RESCUE_FREQUENCY, "Rescue_frequency" );
		FEATURE_SHORT_NAMES.put( VISIBILITY, "Visibility" );

		FEATURE_DIMENSIONS.put( RATE, Dimension.RATE );
		FEATURE_DIMENSIONS.put( START_TIME, Dimension.START_TIME );
		FEATURE_DIMENSIONS.put( END_TIME, Dimension.END_TIME );
		FEATURE_DIMENSIONS.put( AVERAGE_GROWTH_RATE, Dimension.AVERAGE_GROWTH_RATE );
		FEATURE_DIMENSIONS.put( AVERAGE_SHRINK_RATE, Dimension.AVERAGE_SHRINK_RATE );
		FEATURE_DIMENSIONS.put( CATASTROPHE_FREQUENCY, Dimension.CATASTROPHE_FREQUENCY );
		FEATURE_DIMENSIONS.put( RESCUE_FREQUENCY, Dimension.RESCUE_FREQUENCY );
		FEATURE_DIMENSIONS.put( VISIBILITY, Dimension.NONE );

		IS_INT.put( RATE, Boolean.FALSE );
		IS_INT.put( START_TIME, Boolean.TRUE );
		IS_INT.put( END_TIME, Boolean.TRUE );
		IS_INT.put( AVERAGE_GROWTH_RATE, Boolean.FALSE );
		IS_INT.put( AVERAGE_SHRINK_RATE, Boolean.TRUE );
		IS_INT.put( CATASTROPHE_FREQUENCY, Boolean.FALSE );
		IS_INT.put( RESCUE_FREQUENCY, Boolean.FALSE );
		IS_INT.put( VISIBILITY, Boolean.TRUE );
	}

	

	/**
	 * A comparator used to sort stats by ascending feature values.
	 *
	 * @param feature
	 *            the feature to use for comparison. It is the caller
	 *            responsibility to ensure that all stats have the target
	 *            feature.
	 * @return a new {@link Comparator}.
	 */
	public final static Comparator< Stat > featureComparator( final String feature )
	{
		final Comparator< Stat > comparator = new Comparator< Stat >()
		{
			@Override
			public int compare( final Stat o1, final Stat o2 )
			{
				final double diff = o2.diffTo( o1, feature );
				if ( diff == 0 )
					return 0;
				else if ( diff < 0 )
					return 1;
				else
					return -1;
			}
		};
		return comparator;
	}

	/** A comparator used to sort stats by ascending time feature. */
	public final static Comparator< Stat > timeComparator = featureComparator( START_TIME );

	/** A comparator used to sort stats by ascending frame. */
	public final static Comparator< Stat > frameComparator = featureComparator( END_TIME );

	
}
