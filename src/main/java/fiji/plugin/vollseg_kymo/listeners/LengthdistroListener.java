package fiji.plugin.vollseg_kymo.listeners;

import java.awt.TextComponent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import fiji.plugin.vollseg_kymo.Load_ransac_fits;
public class LengthdistroListener implements TextListener {

	final Load_ransac_fits parent;

	public LengthdistroListener(final Load_ransac_fits parent) {

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
