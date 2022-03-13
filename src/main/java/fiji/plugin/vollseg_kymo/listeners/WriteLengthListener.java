package fiji.plugin.vollseg_kymo.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;

public class WriteLengthListener implements ActionListener {

	
	
	final Load_ransac_fits parent;

	public WriteLengthListener(final Load_ransac_fits parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {

		
		WriteStatsListener stats = new WriteStatsListener(parent);
  		stats.lengthDistro(parent.framenumber);

	}
}
