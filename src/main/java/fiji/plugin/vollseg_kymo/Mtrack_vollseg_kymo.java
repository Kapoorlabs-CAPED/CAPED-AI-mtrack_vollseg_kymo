/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2010 - 2022 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.vollseg_kymo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.scijava.Cancelable;
import org.scijava.Named;
import org.scijava.util.VersionUtils;

import net.imglib2.algorithm.Benchmark;
import net.imglib2.algorithm.MultiThreaded;

/**
 * <p>
 * The Mtrack_vollseg_kymo class runs on the directory of kymograph images in 2D
 * </p>
 *
 * <p>
 * <b>Required input:</b> A direcotry of Kymograph images.
 * </p>
 *
 * @author V Kapoor
 */
public class Mtrack_vollseg_kymo implements Benchmark, MultiThreaded,  Named, Cancelable
{

	public static final String PLUGIN_NAME_STR = "Mtrack_vollseg_kymo";

	public static final String PLUGIN_NAME_VERSION = VersionUtils.getVersion( Mtrack_vollseg_kymo.class );

	/**
	 * The model this Mtrack_vollseg_kymo will shape.
	 */

	protected long processingTime;

	protected String errorMessage;

	protected int numThreads = Runtime.getRuntime().availableProcessors();

	private String name;

	private boolean isCanceled;

	private String cancelReason;

	private final List< Cancelable > cancelables = Collections.synchronizedList( new ArrayList<>() );

	/*
	 * CONSTRUCTORS
	 */



	public Mtrack_vollseg_kymo()
	{
		
	}

	
	
	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	// --- org.scijava.Named methods ---

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName( final String name )
	{
		this.name = name;
	}

	// --- org.scijava.Cancelable methods ---

	@Override
	public boolean isCanceled()
	{
		return isCanceled;
	}

	@Override
	public void cancel( final String reason )
	{
		isCanceled = true;
		cancelReason = reason;
		cancelables.forEach( c -> c.cancel( reason ) );
		cancelables.clear();
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
