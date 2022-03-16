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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.detection.DetectionUtils;
import fiji.plugin.trackmate.detection.DetectorFactoryBase;
import fiji.plugin.trackmate.features.FeatureAnalyzer;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.features.edges.EdgeAnalyzer;
import fiji.plugin.trackmate.features.spot.AnalyzerFactory;
import fiji.plugin.trackmate.features.spot.AnalyzerFactoryBase;
import fiji.plugin.trackmate.features.track.TrackAnalyzer;
import fiji.plugin.trackmate.providers.EdgeAnalyzerProvider;
import fiji.plugin.trackmate.providers.AnalyzerProvider;
import fiji.plugin.trackmate.providers.MorphologyAnalyzerProvider;
import fiji.plugin.trackmate.providers.TrackAnalyzerProvider;
import fiji.plugin.trackmate.tracking.TrackerFactory;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileInfo;

/**
 * This class is used to store user settings for the {@link TrackMate}
 * trackmate. It is simply made of public fields
 */
public class Settings
{

	/**
	 * The ImagePlus to operate on. Will also be used by some
	 * {@link fiji.plugin.trackmate.visualization.TrackMateModelView} as a GUI
	 * target.
	 */
	public final String Directory;

	public final String Name;



	


	
	// Filters

	/**
	 * The feature filter list.
	 */
	protected List< FeatureFilter > statFilters = new ArrayList<>();

	/**
	 * The initial quality filter value that is used to clip spots of low
	 * quality from spots.
	 */
	public Double initialStatFilterValue = Double.valueOf( 0 );

	/** The track filter list that is used to prune track and spots. */

	protected String errorMessage;

	// Stat features

	/**
	 * The {@link AnalyzerFactory}s that will be used to compute spot
	 * features. They are ordered in a {@link List} in case some analyzers
	 * requires the results of another analyzer to proceed.
	 */
	protected List< AnalyzerFactoryBase< ? > > spotAnalyzerFactories = new ArrayList<>();

	// Edge features


	/*
	 * CONSTRUCTOR.
	 */

	public Settings()
	{
		this( null, null );
	}

	public Settings( final String Directory, final String Name)
	{
		

		
			this.Directory = Directory;
			this.Name = Name;
		
	}

	/**
	 * Copy a settings objects based on this instance, but configured to run on
	 * the specified image. Detector and tracker factories and settings are
	 * copied, as well as filters, etc. The exception are analyzers: all the
	 * analyzers that are found at runtime are added, regardless of the content
	 * of the instance to copy.
	 * 
	 * @param newImp
	 *            the image to copy the settings for.
	 * @return a new settings object.
	 */
	public Settings copyOn( final String Directory, final String Name )
	{
		final Settings newSettings = new Settings( Directory, Name );
		
		newSettings.statFilters = new ArrayList<>( );
		for ( final FeatureFilter filter : statFilters )
			newSettings.statFilters.add( new FeatureFilter( filter.feature, filter.value, filter.isAbove ) );
		
		
		// Exception: we add all analyzers, regardless of the persistence.
		newSettings.addAllAnalyzers();
		return newSettings;
	}

	/*
	 * METHODS
	 */

	/**
	 * Returns a string description of the target image.
	 *
	 * @return a string representation of the target image.
	 */
	public String toStringImageInfo()
	{
		final StringBuilder str = new StringBuilder();

		str.append( "Image data:\n" );
		
		
				str.append( "in folder: " + Directory + "\n" + "file:" + Name + "\n");
			

		str.append( "Geometry:\n" );
		

		return str.toString();
	}

	public String toStringFeatureAnalyzersInfo()
	{
		final StringBuilder str = new StringBuilder();

		if ( statAnalyzerFactories.isEmpty() )
		{
			str.append( "No stat feature analyzers.\n" );
		}
		else
		{
			str.append( "Stat feature analyzers:\n" );
			prettyPrintFeatureAnalyzer( statAnalyzerFactories, str );
		}

		

		return str.toString();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();

		str.append( toStringImageInfo() );

		str.append( '\n' );
		
		str.append( '\n' );
		str.append( toStringFeatureAnalyzersInfo() );

		str.append( '\n' );
		str.append( "Initial spot filter:\n" );
		if ( null == initialStatFilterValue )
		{
			str.append( "No initial quality filter.\n" );
		}
		else
		{
			str.append( "Initial quality filter value: " + initialStatFilterValue + ".\n" );
		}

		str.append( '\n' );
		str.append( "Stat feature filters:\n" );
		if ( statFilters == null || statFilters.size() == 0 )
		{
			str.append( "No spot feature filters.\n" );
		}
		else
		{
			str.append( "Set with " + statFilters.size() + " spot feature filters:\n" );
			for ( final FeatureFilter featureFilter : statFilters )
			{
				str.append( " - " + featureFilter + "\n" );
			}
		}

		str.append( '\n' );
		str.append( "Particle linking:\n" );
		

		str.append( '\n' );
		

		return str.toString();
	}

	public boolean checkValidity()
	{
		if ( null == Directory )
		{
			errorMessage = "Directory is missing.\n";
			return false;
		}
		if ( null == Name )
		{
			errorMessage = "File Mane is missing.\n";
			return false;
		}
		
		return true;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	/*
	 * ALL FEATURES.
	 */

	/**
	 * All the spot analyzers, edge analyzers and track analyzers discovered at
	 * runtime. This method is a convenience for scripting, that simply
	 * discovers and adds all the analyzers it can find.
	 */
	public void addAllAnalyzers()
	{
		final AnalyzerProvider spotAnalyzerProvider = new AnalyzerProvider( imp == null ? 1 : imp.getNChannels() );
		final List< String > spotAnalyzerKeys = spotAnalyzerProvider.getKeys();
		for ( final String key : spotAnalyzerKeys )
			addAnalyzerFactory( spotAnalyzerProvider.getFactory( key ) );

		if ( imp != null && DetectionUtils.is2D( imp ) && detectorFactory != null && detectorFactory.has2Dsegmentation() )
		{
			final MorphologyAnalyzerProvider spotMorphologyAnalyzerProvider = new MorphologyAnalyzerProvider( imp.getNChannels() );
			final List< String > spotMorphologyAnaylyzerKeys = spotMorphologyAnalyzerProvider.getKeys();
			for ( final String key : spotMorphologyAnaylyzerKeys )
				addAnalyzerFactory( spotMorphologyAnalyzerProvider.getFactory( key ) );
		}

		
	}

	/*
	 * SPOT FEATURES
	 */

	/**
	 * Remove any {@link AnalyzerFactory} to this object.
	 */
	public void clearAnalyzerFactories()
	{
		statAnalyzerFactories.clear();
	}

	/**
	 * Returns a copy of the list of {@link AnalyzerFactory}s configured in
	 * this settings object. They are returned in an ordered list, to enforce
	 * processing order in case some analyzers requires the results of another
	 * analyzers to proceed.
	 *
	 * @return the list of {@link AnalyzerFactory}s.
	 */
	public List< AnalyzerFactoryBase< ? > > getAnalyzerFactories()
	{
		return new ArrayList<>( statAnalyzerFactories );
	}

	/**
	 * Adds a {@link AnalyzerFactory} to the {@link List} of spot analyzers
	 * configured.
	 *
	 * @param spotAnalyzer
	 *            the {@link fiji.plugin.trackmate.features.spot.Analyzer}
	 *            to add, at the end of the list.
	 */
	public void addAnalyzerFactory( final AnalyzerFactoryBase< ? > spotAnalyzer )
	{
		if ( contains( spotAnalyzer ) )
			return;
		statAnalyzerFactories.add( spotAnalyzer );
	}

	/**
	 * Adds a {@link AnalyzerFactory} to the {@link List} of spot analyzers
	 * configured, at the specified index.
	 *
	 * @param index
	 *            index at which the analyzer is to be inserted.
	 * @param spotAnalyzer
	 *            the {@link fiji.plugin.trackmate.features.spot.Analyzer}
	 *            to add, at the specified index in the list.
	 */
	public void addAnalyzerFactory( final int index, final AnalyzerFactory< ? > spotAnalyzer )
	{
		if ( contains( spotAnalyzer ) )
			return;
		statAnalyzerFactories.add( index, spotAnalyzer );
	}

	/**
	 * Removes the specified {@link AnalyzerFactory} from the analyzers
	 * configured.
	 *
	 * @param spotAnalyzer
	 *            the {@link AnalyzerFactory} to remove.
	 * @return true if the specified {@link AnalyzerFactory} was in the list
	 *         and was removed.
	 */
	public boolean removeAnalyzerFactory( final AnalyzerFactory< ? > spotAnalyzer )
	{
		return statAnalyzerFactories.remove( spotAnalyzer );
	}

	private boolean contains( final AnalyzerFactoryBase< ? > spotAnalyzer )
	{
		for ( final AnalyzerFactoryBase< ? > saf : spotAnalyzerFactories )
			if ( saf.getKey().equals( spotAnalyzer.getKey() ) )
				return true;

		return false;
	}

	/*
	 * EDGE FEATURE ANALYZERS
	 */

	

	
	
	

	
	
	/*
	 * FEATURE FILTERS
	 */

	/**
	 * Add a filter to the list of spot filters.
	 *
	 * @param filter
	 *            the filter to add.
	 */
	public void addFilter( final FeatureFilter filter )
	{
		statFilters.add( filter );
	}

	public void removeFilter( final FeatureFilter filter )
	{
		statFilters.remove( filter );
	}

	/** Remove all spot filters stored in this model. */
	public void clearFilters()
	{
		statFilters.clear();
	}

	public List< FeatureFilter > getFilters()
	{
		return statFilters;
	}

	public void setFilters( final List< FeatureFilter > spotFilters )
	{
		this.statFilters = spotFilters;
	}

	

	/*
	 * PRIVATE METHODS
	 */

	public static final void prettyPrintFeatureAnalyzer( final List< ? extends FeatureAnalyzer > analyzers, final StringBuilder str )
	{
		for ( final FeatureAnalyzer analyzer : analyzers )
		{
			str.append( " - " + analyzer.getName() + " provides: " );
			for ( final String feature : analyzer.getFeatures() )
				str.append( analyzer.getFeatureShortNames().get( feature ) + ", " );

			str.deleteCharAt( str.length() - 1 );
			str.deleteCharAt( str.length() - 1 );
			// be precise
			if ( str.charAt( str.length() - 1 ) != '.' )
				str.append( '.' );

			// manual?
			if ( analyzer.isManualFeature() )
			{
				str.deleteCharAt( str.length() - 1 );
				str.append( "; is manual." );
			}
			str.append( '\n' );
		}
	}
}
