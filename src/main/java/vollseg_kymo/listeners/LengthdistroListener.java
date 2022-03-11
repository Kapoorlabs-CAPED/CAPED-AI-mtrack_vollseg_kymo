package vollseg_kymo.listeners;

import java.awt.TextComponent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
public class LengthdistroListener implements TextListener {

	final InteractiveRANSAC parent;

	public LengthdistroListener(final InteractiveRANSAC parent) {

		this.parent = parent;

	}

	@Override
	public void textValueChanged(TextEvent e) {

		final TextComponent tc = (TextComponent) e.getSource();
		String s = tc.getText();

		if (s.length() > 0)
			parent.framenumber = (int) Float.parseFloat(s);

	}

	public void removeListener(KeyListener key, TextComponent tc) {

		tc.removeKeyListener(key);

	}

}
