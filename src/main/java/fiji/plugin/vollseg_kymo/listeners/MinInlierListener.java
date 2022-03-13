package fiji.plugin.vollseg_kymo.listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;



public class MinInlierListener implements AdjustmentListener {
	final Label label;
	final String string;
	Load_ransac_fits parent;
	final float min;
	final int scrollbarSize;

	float max;
	final JScrollBar deltaScrollbar;

	public MinInlierListener(final Load_ransac_fits parent, final Label label, final String string, final float min, float max,
			final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
	
		this.scrollbarSize = scrollbarSize;

		deltaScrollbar.addMouseListener( new StandardMouseListener( parent ) );
		this.deltaScrollbar = deltaScrollbar;
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
		max =  parent.MAX_Inlier;
		
		
		parent.minInliers = (int)Math.round(Slicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));

	
		deltaScrollbar
				.setValue(Slicer.computeScrollbarPositionFromValue(parent.minInliers, min, max, scrollbarSize));

		label.setText(string +  " = "  + parent.nf.format(parent.minInliers) + "      ");
		if(e.getValueIsAdjusting())
		parent.minInlierField.setText(Float.toString((parent.minInliers)));
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
	
		
	}
	
}


