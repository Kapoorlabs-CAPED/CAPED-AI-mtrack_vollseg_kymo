package vollseg_kymo.listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;



public class MaxSlopeListener implements AdjustmentListener {
	final Label label;
	final String string;
	InteractiveRANSAC parent;
	final float min;
	final int scrollbarSize;

	float max;
	final JScrollBar deltaScrollbar;

	public MaxSlopeListener(final InteractiveRANSAC parent, final Label label, final String string, final float min, float max,
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
		
		max =  parent.MAX_ABS_SLOPE;
		
		
		parent.maxSlope = Slicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize);

	
		deltaScrollbar
				.setValue(Slicer.computeScrollbarPositionFromValue(parent.maxSlope, min, max, scrollbarSize));

		label.setText(string +  " = "  + parent.df.format(parent.maxSlope) + "      ");
		if(e.getValueIsAdjusting())
		parent.maxSlopeField.setText(parent.df.format(parent.maxSlope));
		parent.panelFirst.validate();
		parent.panelFirst.repaint();
	
		
	}
	
}


