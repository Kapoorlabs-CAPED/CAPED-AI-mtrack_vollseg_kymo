package fiji.plugin.vollseg_kymo.listeners;

import java.awt.TextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;



public class MaxDistLocListener implements TextListener {
	
	
	final Load_ransac_fits parent;
	boolean pressed;
	public MaxDistLocListener(final Load_ransac_fits parent, final boolean pressed) {
		
		this.parent = parent;
		this.pressed = pressed;
		
	}
	
	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	 	String s = tc.getText();
	 	if(s.length() > 0)
		parent.maxDist = (int) Float.parseFloat(s);
		parent.maxDistLabel.setText("Maximum Gap (tp) = " + parent.df.format((parent.maxDist)) + "      ");
		parent.MAX_Gap = Math.max(parent.maxDist, parent.MAX_Gap);
		if(parent.MAX_Gap > 1.0E5)
			parent.MAX_Gap = 100;
		parent.maxDistSB.setValue(Slicer.computeScrollbarPositionFromValue(parent.maxDist, parent.MIN_Gap, parent.MAX_Gap, parent.scrollbarSize));
		 tc.addKeyListener(new KeyListener(){
			 @Override
			    public void keyTyped(KeyEvent arg0) {
				   
			    }

			    @Override
			    public void keyReleased(KeyEvent arg0) {
			    	
			    	if (arg0.getKeyChar() == KeyEvent.VK_ENTER ) {
						
						
						pressed = false;
						
					}

			    }

			    @Override
			    public void keyPressed(KeyEvent arg0) {
			   
			    	if (arg0.getKeyChar() == KeyEvent.VK_ENTER&& !pressed) {
						pressed = true;
			    		
						
			  
				
					parent.updateRANSAC();
					parent.maxDistSB.repaint();
					parent.maxDistSB.validate();
					
					
					
			    		
					 }

			    }
			});
	

	

}

}
