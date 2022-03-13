package fiji.plugin.vollseg_kymo.listeners;

import java.awt.TextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;



public class MinInlierLocListener implements TextListener {
	
	
	final Load_ransac_fits parent;
	boolean pressed;
	public MinInlierLocListener(final Load_ransac_fits parent, final boolean pressed) {
		
		this.parent = parent;
		this.pressed = pressed;
		
	}
	
	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	 	String s = tc.getText();
	 	if(s.length() > 0)
		parent.minInliers = (int) Float.parseFloat(s);
		parent.minInliersLabel.setText("Minimum No. of timepoints (tp) = " + parent.nf.format((parent.minInliers)) + "      ");
		parent.MAX_Inlier = Math.max(parent.minInliers, parent.MAX_Inlier);
		if(parent.MAX_Inlier > 1.0E5)
			parent.MAX_Inlier = 100;
		parent.minInliersSB.setValue(Slicer.computeScrollbarPositionFromValue(parent.minInliers, parent.MIN_Inlier, parent.MAX_Inlier, parent.scrollbarSize));
		

		
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
					parent.minInliersSB.repaint();
					parent.minInliersSB.validate();
					
					
					
			    		
					 }

			    }
			});
	

	

}

}
