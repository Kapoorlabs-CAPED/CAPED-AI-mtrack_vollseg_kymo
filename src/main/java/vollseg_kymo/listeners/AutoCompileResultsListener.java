/*-
 * #%L
 * Microtubule tracker.
 * %%
 * Copyright (C) 2017 MTrack developers.
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
package vollseg_kymo.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoCompileResultsListener implements ActionListener {

	final InteractiveRANSAC parent;
	

	public AutoCompileResultsListener(final InteractiveRANSAC parent) {
		this.parent = parent;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
	
		
		
		int nThreads = 1;
		// set up executor service
		final ExecutorService taskexecutor = Executors.newFixedThreadPool(nThreads);
		 List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		for (int trackindex = 0; trackindex < parent.inputfiles.length; ++trackindex){
			
			tasks.add(Executors.callable(new Split(parent, trackindex)));
			
		}
		try {
			taskexecutor.invokeAll(tasks);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	
				
			
		
           parent.table.validate();
           parent.scrollPane.validate();
           
	}

	

}
