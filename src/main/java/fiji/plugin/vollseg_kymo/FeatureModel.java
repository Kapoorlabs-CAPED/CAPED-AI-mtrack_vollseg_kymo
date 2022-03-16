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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * This class represents the part of the {@link Model} that is in charge of
 * dealing with stat features and track features.
 *
 * @author Jean-Yves Tinevez, 2011, 2012. Revised December 2020.
 *
 */
public class FeatureModel
{

	/*
	 * FIELDS
	 */


	/**
	 * Feature storage. We use a Map of Map as a 2D Map. The list maps each
	 * track to its feature map. The feature map maps each feature to the double
	 * value for the specified feature.
	 */

	private final Collection< String > statFeatures = new LinkedHashSet<>();

	private final Map< String, String > statFeatureNames = new HashMap<>();

	private final Map< String, String > statFeatureShortNames = new HashMap<>();

	private final Map< String, Dimension > statFeatureDimensions = new HashMap<>();

	private final Map< String, Boolean > statFeatureIsInt = new HashMap<>();

	private final Model model;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Instantiates a new feature model. The basic stat features (POSITON_*,
	 * RADIUS, FRAME, QUALITY) are declared. Edge and track feature declarations
	 * are left blank.
	 *
	 * @param model
	 *            the parent {@link Model}.
	 */
	protected FeatureModel( final Model model )
	{
		this.model = model;
		// Adds the base stat, edge & track features
		declarestatFeatures(
				Stat.FEATURES,
				Stat.FEATURE_NAMES,
				Stat.FEATURE_SHORT_NAMES,
				Stat.FEATURE_DIMENSIONS,
				Stat.IS_INT );
		
	}


	

	/*
	 * stat FEATURES the stat features are stored in the stat object themselves,
	 * but we declare them here.
	 */

	/**
	 * Declares stat features, by specifying their names, short name and
	 * dimension. An {@link IllegalArgumentException} will be thrown if any of
	 * the map misses a feature.
	 *
	 * @param features
	 *            the list of stat feature to register.
	 * @param featureNames
	 *            the name of these features.
	 * @param featureShortNames
	 *            the short name of these features.
	 * @param featureDimensions
	 *            the dimension of these features.
	 * @param isIntFeature
	 *            whether some of these features are made of <code>int</code>s (
	 *            <code>true</code>) or <code>double</code>s (<code>false</code>
	 *            ).
	 */
	
	
	public void declarestatFeatures( final Collection< String > features, final Map< String, String > featureNames, 
			final Map< String, String > featureShortNames, final Map< String, Dimension > featureDimensions, final Map< String, Boolean > isIntFeature )
	{
		statFeatures.addAll( features );
		for ( final String feature : features )
		{

			final String name = featureNames.get( feature );
			if ( null == name )
				throw new IllegalArgumentException( "Feature " + feature + " misses a name." );
			statFeatureNames.put( feature, name );

			final String shortName = featureShortNames.get( feature );
			if ( null == shortName )
				throw new IllegalArgumentException( "Feature " + feature + " misses a short name." );
			statFeatureShortNames.put( feature, shortName );

			final Dimension dimension = featureDimensions.get( feature );
			if ( null == dimension )
				throw new IllegalArgumentException( "Feature " + feature + " misses a dimension." );
			statFeatureDimensions.put( feature, dimension );

			final Boolean isInt = isIntFeature.get( feature );
			if ( null == isInt )
				throw new IllegalArgumentException( "Feature " + feature + " misses the isInt flag." );
			statFeatureIsInt.put( feature, isInt );

		}
	}

	/**
	 * Returns stat features as declared in this model.
	 *
	 * @return the stat features.
	 */
	public Collection< String > getstatFeatures()
	{
		return statFeatures;
	}

	/**
	 * Returns the name mapping of the stat features that are dealt with in this
	 * model.
	 *
	 * @return the map of stat feature names.
	 */
	public Map< String, String > getstatFeatureNames()
	{
		return statFeatureNames;
	}

	/**
	 * Returns the short name mapping of the stat features that are dealt with
	 * in this model.
	 *
	 * @return the map of stat short names.
	 */
	public Map< String, String > getstatFeatureShortNames()
	{
		return statFeatureShortNames;
	}

	/**
	 * Returns the dimension mapping of the stat features that are dealt with in
	 * this model.
	 *
	 * @return the map of stat feature dimensions.
	 */
	public Map< String, Dimension > getstatFeatureDimensions()
	{
		return statFeatureDimensions;
	}

	/**
	 * Returns the map that states whether the target feature is integer values
	 * (<code>true</code>) or double valued (<code>false</code>).
	 *
	 * @return the map of isInt flag.
	 */
	public Map< String, Boolean > getstatFeatureIsInt()
	{
		return statFeatureIsInt;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();

		// stats
		str.append( "stat features declared:\n" );
		appendFeatureDeclarations( str, statFeatures, statFeatureNames, statFeatureShortNames, statFeatureDimensions, statFeatureIsInt );
		str.append( '\n' );

		

		return str.toString();
	}

	/**
	 * Echoes the full content of this {@link FeatureModel}.
	 * 
	 * @return a String representation of the full content of this model.
	 */
	public String echo()
	{
		final StringBuilder str = new StringBuilder();

		// stats
		str.append( "stat features:\n" );
		str.append( " - Declared:\n" );
		appendFeatureDeclarations( str, statFeatures, statFeatureNames, statFeatureShortNames, statFeatureDimensions, statFeatureIsInt );
		str.append( '\n' );

		

		return str.toString();
	}

	/*
	 * STATIC UTILS
	 */

	
	private static final void appendFeatureDeclarations( final StringBuilder str, final Collection< String > features, final Map< String, String > featureNames, final Map< String, String > featureShortNames, final Map< String, Dimension > featureDimensions, final Map< String, Boolean > isIntFeature )
	{
		for ( final String feature : features )
		{
			str.append( "   - " + feature + ": " + featureNames.get( feature ) + ", '" + featureShortNames.get( feature ) + "' (" + featureDimensions.get( feature ) + ")" );
			if ( isIntFeature.get( feature ).booleanValue() )
				str.append( " - integer valued.\n" );
			else
				str.append( " - double valued.\n" );
		}
	}




	}




