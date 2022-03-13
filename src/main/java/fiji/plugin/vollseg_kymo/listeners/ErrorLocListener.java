package fiji.plugin.vollseg_kymo.listeners;

import java.awt.TextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;



public class ErrorLocListener implements TextListener {
	
	
	final Load_ransac_fits parent;
	boolean pressed;
	public ErrorLocListener(final Load_ransac_fits parent, final boolean pressed) {
		
		this.parent = parent;
		this.pressed = pressed;
		
	}
	
	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	 	String s = tc.getText();
	 	if(s.length() > 0)
		parent.maxError = Float.parseFloat(s);
		parent.maxErrorLabel.setText("Maximum Error (px) = " + parent.nf.format((parent.maxError)) + "      ");
		parent.MAX_ERROR = Math.max(parent.maxError, parent.MAX_ERROR);
		if(parent.MAX_ERROR > 1.0E5)
			parent.MAX_ERROR = 100;
		parent.maxErrorSB.setValue(Slicer.computeScrollbarPositionFromValue((float)parent.maxError, parent.MIN_ERROR, parent.MAX_ERROR, parent.scrollbarSize));
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
					parent.maxErrorSB.repaint();
					parent.maxErrorSB.validate();
					
					
					
			    		
					 }

			    }
			});
	

	

}

}
