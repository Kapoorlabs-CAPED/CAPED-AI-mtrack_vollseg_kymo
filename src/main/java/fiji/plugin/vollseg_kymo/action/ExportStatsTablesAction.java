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
package fiji.plugin.vollseg_kymo.action;

import static fiji.plugin.vollseg_kymo.gui.Icons.CALCULATOR_ICON;

import java.awt.Frame;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.vollseg_kymo.Mtrack_vollseg_kymo;
import fiji.plugin.vollseg_kymo.visualization.table.TrackTableView;

public class ExportStatsTablesAction extends AbstractTMAction
{

	public static final String NAME = "Export statistics to tables";

	public static final String KEY = "EXPORT_STATS";

	public static final String INFO_TEXT = "<html>"
			+ "Compute and export all statistics to 4 tables. "
			+ "Statistics are separated in features computed for: "
			+ "<ol> "
			+ "	<li> growth rate; "
			+ "	<li> shrink rate; "
			+ "	<li> catastrophe frequency. "
			+ "	<li> rescue frequency. "
			+ "</ol> "
			+ "</html>";

	@Override
	public void execute( final Mtrack_vollseg_kymo vollsegkymo, final Frame parent )
	{
		createTrackTables(  ).render();
	}

	public static TrackTableView createTrackTables( )
	{
		return new TrackTableView( );
	}

	// Invisible because called on the view config panel.
	@Plugin( type = Mtrack_vollsegActionFactory.class, visible = false )
	public static class Factory implements Mtrack_vollsegActionFactory
	{

		@Override
		public String getInfoText()
		{
			return INFO_TEXT;
		}

		@Override
		public String getKey()
		{
			return KEY;
		}

		@Override
		public Mtrack_vollseg_kymoAction create()
		{
			return new ExportStatsTablesAction();
		}

		@Override
		public ImageIcon getIcon()
		{
			return CALCULATOR_ICON;
		}

		@Override
		public String getName()
		{
			return NAME;
		}
	}
}
