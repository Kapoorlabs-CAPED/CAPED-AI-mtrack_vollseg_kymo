package fiji.plugin.vollseg_kymo;

public enum Dimension {
		NONE,
		RATE,
		START_TIME,
		END_TIME,
		AVERAGE_GROWTH_RATE,
		AVERAGE_SHRINK_RATE,
		CATASTROPHE_FREQUENCY,
		RESCUE_FREQUENCY,   // we separate length and position so that x,y,z are plotted on a different graph from spot sizes
		NAME,
		STRING;
	    // for non-numeric features
	}

	
