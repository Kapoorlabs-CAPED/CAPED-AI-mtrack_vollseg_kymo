
package fiji.plugin.vollseg_kymo.action;

import java.awt.Frame;

import fiji.plugin.vollseg_kymo.Logger;
import fiji.plugin.vollseg_kymo.Mtrack_vollseg_kymo;

/**
 * This interface describe a track mate action, that can be run on a
 * {@link Mtrack_vollseg_kymo} object to change its content or properties.
 *
 * @author V Kapoor, 2011-2013 revised in 2021
 */
public interface Mtrack_vollseg_kymoAction
{

	/**
	 * Executes this action within an application specified by the parameters.
	 *
	 * @param Mtrack_vollseg_kymo
	 *            the {@link Mtrack_vollseg_kymo} instance to use to execute the action.
	 * @param selectionModel
	 * @param parent
	 *            the user-interface parent window.
	 */
	public void execute( Mtrack_vollseg_kymo vollsegkymo, Frame parent );

	/**
	 * Sets the logger that will receive logs when this action is executed.
	 */
	public void setLogger( Logger logger );
}
