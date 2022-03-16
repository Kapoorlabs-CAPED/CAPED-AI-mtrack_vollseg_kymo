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

import static fiji.plugin.vollseg_kymo.gui.Fonts.FONT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;

import fiji.plugin.vollseg_kymo.Model;
import fiji.plugin.vollseg_kymo.Stat;
import fiji.plugin.vollseg_kymo.visualization.Mtrack_vollseg_kymo_model_view;

@SuppressWarnings( "unchecked" )
public class SearchBar extends JTextField
{
	private static final long serialVersionUID = 1L;

	private final static Font NORMAL_FONT = FONT.deriveFont( 10f );

	private final static Font NOTFOUND_FONT;
	static
	{
		@SuppressWarnings( "rawtypes" )
		final Map attributes = NORMAL_FONT.getAttributes();
		attributes.put( TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON );
		attributes.put( TextAttribute.FOREGROUND, Color.RED.darker() );
		NOTFOUND_FONT = new Font( attributes );
	}

	private final PropertyChangeSupport observer = new PropertyChangeSupport( this );

	private final Model model;

	private final Mtrack_vollseg_kymo_model_view view;


	/**
	 * Creates new form SearchBox
	 * 
	 * @param model
	 *            the model to search in.
	 * @param view
	 *            the view to update when a stat is found.
	 */
	public SearchBar( final Model model, final Mtrack_vollseg_kymo_model_view view )
	{
		this.model = model;
		this.view = view;
		putClientProperty( "JTextField.variant", "search" );
		putClientProperty( "JTextField.Search.Prompt", "Search" );
		setPreferredSize( new Dimension( 80, 25 ) );
		setFont( NORMAL_FONT );

		addFocusListener( new java.awt.event.FocusAdapter()
		{
			@Override
			public void focusGained( final java.awt.event.FocusEvent evt )
			{
				searchBoxFocusGained( evt );
			}

			@Override
			public void focusLost( final java.awt.event.FocusEvent evt )
			{
				searchBoxFocusLost( evt );
			}
		} );
		addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyReleased( final KeyEvent e )
			{
				searchBoxKey( e );
			}
		} );
		observer.addPropertyChangeListener( new SearchAction() );
	}

	private void searchBoxKey( final KeyEvent e )
	{
		setFont( NORMAL_FONT );
		if ( getText().length() > 1 || e.getKeyCode() == KeyEvent.VK_ENTER )
		{
			observer.firePropertyChange( "Searching started", null, getText() );
		}
	}

	/**
	 * @param evt  
	 */
	private void searchBoxFocusGained( final java.awt.event.FocusEvent evt )
	{
		setFont( NORMAL_FONT );
		setFont( getFont().deriveFont( Font.PLAIN ) );
//		setText( null );
	}

	/**
	 * @param evt  
	 */
	private void searchBoxFocusLost( final java.awt.event.FocusEvent evt )
	{
		setFont( NORMAL_FONT );
		setFont( getFont().deriveFont( Font.ITALIC ) );
//		setText( "Search" );
	}

	private class SearchAction implements PropertyChangeListener, Iterator< stat >
	{

		private Iterator< stat > iterator;

		private Iterator< Integer > trackIterator;

		public SearchAction()
		{
			trackIterator = model.getTrackModel().trackIDs( true ).iterator();
			if ( trackIterator.hasNext() )
			{
				final Integer currentTrackID = trackIterator.next();
				final stat trackStart = firststatOf( currentTrackID );
				iterator = model.getTrackModel().getSortedDepthFirstIterator( trackStart, stat.nameComparator, false );
			}
			else
			{
				iterator = Collections.EMPTY_LIST.iterator();
			}
		}

		@Override
		public void propertyChange( final PropertyChangeEvent evt )
		{
			final String text = ( String ) evt.getNewValue();
			if ( !text.isEmpty() )
			{
				search( text );
			}
		}

		private void search( final String text )
		{
			stat start = null;
			stat stat;
			while ( ( stat = next() ) != start )
			{
				if ( start == null )
				{
					start = stat;
				}
				if ( stat.getName().contains( text ) )
				{
					view.centerViewOn( stat );
					return;
				}
			}
			setFont( NOTFOUND_FONT );
		}

		@Override
		public boolean hasNext()
		{
			return true;
		}

		@Override
		public stat next()
		{
			if ( null == iterator || !iterator.hasNext() )
			{
				if ( null == trackIterator || !trackIterator.hasNext() )
				{
					trackIterator = model.getTrackModel().trackIDs( true ).iterator();
				}
				final Integer currentTrackID = trackIterator.next();
				final stat trackStart = firststatOf( currentTrackID );
				iterator = model.getTrackModel().getSortedDepthFirstIterator( trackStart, stat.nameComparator, false );
			}
			return iterator.next();
		}

		private stat firststatOf( final Integer trackID )
		{
			final List< stat > trackstats = new ArrayList<>( model.getTrackModel().trackstats( trackID ) );
			Collections.sort( trackstats, stat.frameComparator );
			return trackstats.get( 0 );
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
