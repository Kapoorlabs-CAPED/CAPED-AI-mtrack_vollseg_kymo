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
package fiji.plugin.vollseg_kymo.visualization.table;

import static fiji.plugin.vollseg_kymo.gui.Icons.CSV_ICON;
import static fiji.plugin.vollseg_kymo.gui.Icons.TRACKMATE_ICON;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jgrapht.graph.DefaultWeightedEdge;
import fiji.plugin.vollseg_kymo.Stat;
import fiji.plugin.vollseg_kymo.Dimension;
import fiji.plugin.vollseg_kymo.util.FileChooser;
import fiji.plugin.vollseg_kymo.util.FileChooser.DialogType;
import fiji.plugin.vollseg_kymo.util.FileChooser.SelectionMode;
import fiji.plugin.vollseg_kymo.util.TMUtils;
import fiji.plugin.vollseg_kymo.Model;
import fiji.plugin.vollseg_kymo.visualization.FeatureColorGenerator;
import fiji.plugin.vollseg_kymo.visualization.TrackMateModelView;
import fiji.plugin.vollseg_kymo.visualization.trackscheme.utils.SearchBar;

public class TrackTableView extends JFrame implements TrackMateModelView, ModelChangeListener, SelectionChangeListener
{

	private static final long serialVersionUID = 1L;

	private static final String KEY = "TRACK_TABLES";

	public static String selectedFile = System.getProperty( "user.home" ) + File.separator + "export.csv";

	private final Model model;
	
	private final TablePanel< Double > rateTable;

	private final TablePanel< Double > catfrequTable;
	
	private final TablePanel< Double > resfrequTable;

	private final AtomicBoolean ignoreSelectionChange = new AtomicBoolean( false );


	public TrackTableView(final Model model  )
	{
		super( "Track tables" );
		setIconImage( TRACKMATE_ICON.getImage() );

		/*
		 * GUI.
		 */
		this.model = model;
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BorderLayout() );

		// Tables.
		this.rateTable = createRateTable( );
		this.catfrequTable = createCatfrequTable( );
		this.resfrequTable = createResfrequTable( );

		// Tabbed pane.
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.LEFT );
		tabbedPane.add( "Growth Rates", rateTable.getPanel() );
		tabbedPane.add( "Catastrophe Frequency", catfrequTable.getPanel() );
		tabbedPane.add( "Rescue Frequency", resfrequTable.getPanel() );
		
		tabbedPane.setSelectedComponent( rateTable.getPanel() );
		mainPanel.add( tabbedPane, BorderLayout.CENTER );

		// Tool bar.
		final JPanel toolbar = new JPanel();
		final BoxLayout layout = new BoxLayout( toolbar, BoxLayout.LINE_AXIS );
		toolbar.setLayout( layout );
		final JButton exportBtn = new JButton( "Export to CSV", CSV_ICON );
		exportBtn.addActionListener( e -> exportToCsv( tabbedPane.getSelectedIndex() ) );
		toolbar.add( exportBtn );
		toolbar.add( Box.createHorizontalGlue() );
		final SearchBar searchBar = new SearchBar( model, this );
		searchBar.setMaximumSize( new java.awt.Dimension( 160, 30 ) );
		toolbar.add( searchBar );
		final JToggleButton tglColoring = new JToggleButton( "coloring" );
		tglColoring.addActionListener( e -> {
			rateTable.setUseColoring( tglColoring.isSelected() );
			catfrequTable.setUseColoring( tglColoring.isSelected() );
			resfrequTable.setUseColoring( tglColoring.isSelected() );
			refresh();
		} );
		toolbar.add( tglColoring );
		mainPanel.add( toolbar, BorderLayout.NORTH );

		getContentPane().add( mainPanel );
		pack();

		

	private  void exportToCsv( final int index )
	{
		final TablePanel< ? > table;
		switch ( index )
		{
		case 0:
			table = rateTable;
			break;
		case 1:
			table = catfrequTable;
			break;
		case 2:
			table = resfrequTable;
		default:
			throw new IllegalArgumentException( "Unknown table with index " + index );
		}
		
		final File file = FileChooser.chooseFile(
				this,
				selectedFile,
				new FileNameExtensionFilter( "CSV files", "csv" ),
				"Export table to CSV",
				DialogType.SAVE,
				SelectionMode.FILES_ONLY );
		if ( null == file )
			return;

		selectedFile = file.getAbsolutePath();
		try
		{
			table.exportToCsv( file );
		}
		catch ( final IOException e )
		{
			model.getLogger().error( "Problem exporting to file "
					+ file + "\n" + e.getMessage() );
		}
	}

	

	private final TablePanel< Stat > createStatTable( final Model model )
	{
		final List< Stat > objects = new ArrayList<>();
		for ( final Integer trackID : model.getTrackModel().unsortedTrackIDs( true ) )
			objects.addAll( model.getTrackModel().trackStats( trackID ) );
		final List< String > features = new ArrayList<>( model.getFeatureModel().getStatFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getStatFeatureNames();
		final Map< String, String > featureShortNames = model.getFeatureModel().getStatFeatureShortNames();
		final Map< String, String > featureUnits = new HashMap<>();
		for ( final String feature : features )
		{
			final Dimension dimension = model.getFeatureModel().getStatFeatureDimensions().get( feature );
			final String units = TMUtils.getUnitsFor( dimension, model.getSpaceUnits(), model.getTimeUnits() );
			featureUnits.put( feature, units );
		}
		final Map< String, Boolean > isInts = model.getFeatureModel().getStatFeatureIsInt();
		final Map< String, String > infoTexts = new HashMap<>();
		final Function< Stat, String > labelGenerator = Stat -> Stat.getName();
		final BiConsumer< Stat, String > labelSetter = ( Stat, label ) -> Stat.setName( label );

		/*
		 * Feature provider. We add a fake one to show the Stat ID.
		 */
		final String Stat_ID = "ID";
		features.add( 0, Stat_ID );
		featureNames.put( Stat_ID, "Stat ID" );
		featureShortNames.put( Stat_ID, "Stat ID" );
		featureUnits.put( Stat_ID, "" );
		isInts.put( Stat_ID, Boolean.TRUE );
		infoTexts.put( Stat_ID, "The id of the Stat." );

		/*
		 * Feature provider. We add a fake one to show the Stat *track* ID.
		 */
		final String TRACK_ID = "TRACK_ID";
		features.add( 1, TRACK_ID );
		featureNames.put( TRACK_ID, "Track ID" );
		featureShortNames.put( TRACK_ID, "Track ID" );
		featureUnits.put( TRACK_ID, "" );
		isInts.put( TRACK_ID, Boolean.TRUE );
		infoTexts.put( TRACK_ID, "The id of the track this Stat belongs to." );

		final BiFunction< Stat, String, Double > featureFun = ( Stat, feature ) -> {
			if ( feature.equals( TRACK_ID ) )
			{
				final Integer trackID = model.getTrackModel().trackIDOf( Stat );
				return trackID == null ? null : trackID.doubleValue();
			}
			else if ( feature.equals( Stat_ID ) )
				return ( double ) Stat.ID(); 

			return Stat.getFeature( feature );
		};

		final BiConsumer< Stat, Color > colorSetter =
				( Stat, color ) -> Stat.putFeature( ManualStatColorAnalyzerFactory.FEATURE, Double.valueOf( color.getRGB() ) );

		final Supplier< FeatureColorGenerator< Stat > > coloring =
				() -> FeatureUtils.createStatColorGenerator( model, ds );

		final TablePanel< Stat > table =
				new TablePanel<>(
						objects,
						features,
						featureFun,
						featureNames,
						featureShortNames,
						featureUnits,
						isInts,
						infoTexts,
						coloring,
						labelGenerator,
						labelSetter,
						ManualStatColorAnalyzerFactory.FEATURE,
						colorSetter );

		table.getTable().getSelectionModel().addListSelectionListener(
				new StatTableSelectionListener() );

		return table;
	}

	@Override
	public void render()
	{
		setLocationRelativeTo( null );
		setVisible( true );
	}

	@Override
	public void refresh()
	{
		repaint();
	}

	@Override
	public void modelChanged( final ModelChangeEvent event )
	{
		if ( event.getEventID() == ModelChangeEvent.FEATURES_COMPUTED )
		{
			refresh();
			return;
		}

		final List< Stat > Stats = new ArrayList<>();
		for ( final Integer trackID : model.getTrackModel().unsortedTrackIDs( true ) )
			Stats.addAll( model.getTrackModel().trackStats( trackID ) );
		StatTable.setObjects( Stats );

		final List< DefaultWeightedEdge > edges = new ArrayList<>();
		for ( final Integer trackID : model.getTrackModel().unsortedTrackIDs( true ) )
			edges.addAll( model.getTrackModel().trackEdges( trackID ) );
		edgeTable.setObjects( edges );

		final List< Integer > trackIDs = new ArrayList<>( model.getTrackModel().trackIDs( true ) );
		trackTable.setObjects( trackIDs );

		refresh();
	}

	/*
	 * Forward selection model changes to the tables.
	 */
	@Override
	public void selectionChanged( final SelectionChangeEvent event )
	{
		if ( ignoreSelectionChange.get() )
			return;
		ignoreSelectionChange.set( true );

		// Vertices table.
		final Set< Stat > selectedVertices = selectionModel.getStatSelection();
		final JTable vt = StatTable.getTable();
		vt.getSelectionModel().clearSelection();
		for ( final Stat Stat : selectedVertices )
		{
			final int row = StatTable.getViewRowForObject( Stat );
			vt.getSelectionModel().addSelectionInterval( row, row );
		}

		// Center on selection if we added one Stat exactly
		final Map< Stat, Boolean > StatsAdded = event.getStats();
		if ( StatsAdded != null && StatsAdded.size() == 1 )
		{
			final boolean added = StatsAdded.values().iterator().next();
			if ( added )
			{
				final Stat Stat = StatsAdded.keySet().iterator().next();
				centerViewOn( Stat );
			}
		}

		// Edges table.
		final Set< DefaultWeightedEdge > selectedEdges = selectionModel.getEdgeSelection();
		final JTable et = edgeTable.getTable();
		et.getSelectionModel().clearSelection();
		for ( final DefaultWeightedEdge e : selectedEdges )
		{
			final int row = edgeTable.getViewRowForObject( e );
			et.getSelectionModel().addSelectionInterval( row, row );
		}

		// Center on selection if we added one edge exactly
		final Map< DefaultWeightedEdge, Boolean > edgesAdded = event.getEdges();
		if ( edgesAdded != null && edgesAdded.size() == 1 )
		{
			final boolean added = edgesAdded.values().iterator().next();
			if ( added )
			{
				final DefaultWeightedEdge edge = edgesAdded.keySet().iterator().next();
				centerViewOn( edge );
			}
		}

		refresh();
		ignoreSelectionChange.set( false );
	}

	public void centerViewOn( final DefaultWeightedEdge edge )
	{
		edgeTable.scrollToObject( edge );
	}

	@Override
	public void centerViewOn( final Stat Stat )
	{
		StatTable.scrollToObject( Stat );
	}

	@Override
	public Model getModel()
	{
		return model;
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public void clear()
	{}

	public TablePanel< Stat > getStatTable()
	{
		return StatTable;
	}



	/**
	 * Forward Stat table selection to selection model.
	 */
	private final class StatTableSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged( final ListSelectionEvent event )
		{
			if ( event.getValueIsAdjusting() || ignoreSelectionChange.get() )
				return;

			ignoreSelectionChange.set( true );

			final int[] selectedRows = StatTable.getTable().getSelectedRows();
			final List< Stat > toSelect = new ArrayList<>( selectedRows.length );
			for ( final int row : selectedRows )
				toSelect.add( StatTable.getObjectForViewRow( row ) );

			selectionModel.clearSelection();
			selectionModel.addStatToSelection( toSelect );
			refresh();

			ignoreSelectionChange.set( false );
		}
	}

	/**
	 * Forward edge table selection to selection model.
	 */
	private final class EdgeTableSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged( final ListSelectionEvent event )
		{
			if ( event.getValueIsAdjusting() || ignoreSelectionChange.get() )
				return;

			ignoreSelectionChange.set( true );

			final int[] selectedRows = edgeTable.getTable().getSelectedRows();
			final List< DefaultWeightedEdge > toSelect = new ArrayList<>( selectedRows.length );
			for ( final int row : selectedRows )
				toSelect.add( edgeTable.getObjectForViewRow( row ) );

			selectionModel.clearSelection();
			selectionModel.addEdgeToSelection( toSelect );
			refresh();

			ignoreSelectionChange.set( false );
		}
	}

	/**
	 * Forward track table selection to selection model.
	 */
	private class TrackTableSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged( final ListSelectionEvent event )
		{
			if ( event.getValueIsAdjusting() || ignoreSelectionChange.get() )
				return;

			ignoreSelectionChange.set( true );

			final Set< Stat > Stats = new HashSet<>();
			final Set< DefaultWeightedEdge > edges = new HashSet<>();
			final int[] selectedRows = trackTable.getTable().getSelectedRows();
			for ( final int row : selectedRows )
			{
				final Integer trackID = trackTable.getObjectForViewRow( row );
				Stats.addAll( model.getTrackModel().trackStats( trackID ) );
			}
			selectionModel.clearSelection();
			selectionModel.addStatToSelection( Stats );

			refresh();

			ignoreSelectionChange.set( false );

		}
	}
}
