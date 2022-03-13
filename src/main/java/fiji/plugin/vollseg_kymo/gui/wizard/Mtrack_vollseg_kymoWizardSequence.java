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
package fiji.plugin.vollseg_kymo.gui.wizard;

import static fiji.plugin.vollseg_kymo.gui.Icons.SPOT_TABLE_ICON;
import static fiji.plugin.vollseg_kymo.gui.Icons.TRACK_SCHEME_ICON_16x16;
import static fiji.plugin.vollseg_kymo.gui.Icons.TRACK_TABLES_ICON;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fiji.plugin.vollseg_kymo.action.AbstractTMAction;
import fiji.plugin.vollseg_kymo.action.ExportStatsTablesAction;

import javax.swing.AbstractAction;
import fiji.plugin.vollseg_kymo.Logger;
import fiji.plugin.vollseg_kymo.Mtrack_vollseg_kymo;
import fiji.plugin.vollseg_kymo.gui.components.LogPanel;
import fiji.plugin.vollseg_kymo.gui.wizard.descriptors.StartDialogDescriptor;

public class Mtrack_vollseg_kymoWizardSequence implements WizardSequence
{

	private final Mtrack_vollseg_kymo vollsegkymo;


	private WizardPanelDescriptor current;

	private final StartDialogDescriptor startDialogDescriptor;

	private final Map< WizardPanelDescriptor, WizardPanelDescriptor > next;

	private final Map< WizardPanelDescriptor, WizardPanelDescriptor > previous;




	public Mtrack_vollseg_kymoWizardSequence( final Mtrack_vollseg_kymo vollsegkymo )
	{
		this.vollsegkymo = vollsegkymo;

		final LogPanel logPanel = new LogPanel();
		final Logger logger = logPanel.getLogger();

		startDialogDescriptor = new StartDialogDescriptor( logger );

		this.next = getForwardSequence();
		this.previous = getBackwardSequence();
		current = startDialogDescriptor;
	}

	@Override
	public void onClose()
	{
		
	}

	@Override
	public WizardPanelDescriptor next()
	{
		

		current = startDialogDescriptor;
		return current;
	}


	@Override
	public WizardPanelDescriptor previous()
	{
		

		current = startDialogDescriptor;
		return current;
	}

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public WizardPanelDescriptor current()
	{
		return current;
	}


	@Override
	public WizardPanelDescriptor logDescriptor()
	{
		return null;
	}

	@Override
	public WizardPanelDescriptor configDescriptor()
	{
		return null;
	}


	@Override
	public boolean hasPrevious()
	{
		return current != startDialogDescriptor;
	}

	private Map< WizardPanelDescriptor, WizardPanelDescriptor > getBackwardSequence()
	{
		final Map< WizardPanelDescriptor, WizardPanelDescriptor > map = new HashMap<>();
		map.put( startDialogDescriptor, startDialogDescriptor );
		return map;
	}

	private Map< WizardPanelDescriptor, WizardPanelDescriptor > getForwardSequence()
	{
		final Map< WizardPanelDescriptor, WizardPanelDescriptor > map = new HashMap<>();
		map.put( startDialogDescriptor, startDialogDescriptor );
		return map;
	}

	@Override
	public void setCurrent( final String panelIdentifier )
	{
			current = startDialogDescriptor;
			return;
	}

	

	private static final String TRACK_TABLES_BUTTON_TOOLTIP = "<html>"
			+ "Export the features of all kymographs <br>"
			+ "k to ImageJ tables."
			+ "</html>";

	private static final String SPOT_TABLE_BUTTON_TOOLTIP = "Export the features of all spots to ImageJ tables.";


	
	private class ShowTrackTablesAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		private ShowTrackTablesAction()
		{
			super( "Tracks", TRACK_TABLES_ICON );
			putValue( SHORT_DESCRIPTION, TRACK_TABLES_BUTTON_TOOLTIP );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			showTables( false );
		}
	}

	private class ShowSpotTableAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		private ShowSpotTableAction()
		{
			super( "Spots", SPOT_TABLE_ICON );
			putValue( SHORT_DESCRIPTION, SPOT_TABLE_BUTTON_TOOLTIP );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			showTables( true );
		}
	}

	private void showTables( final boolean showSpotTable )
	{
		new Thread( "Vollseg table thread." )
		{
			@Override
			public void run()
			{
				AbstractTMAction action;
				action = new ExportStatsTablesAction();

				action.execute( vollsegkymo,  null );
			}
		}.start();
	}

	@Override
	public WizardPanelDescriptor save() {
		// TODO Auto-generated method stub
		return null;
	}
}
