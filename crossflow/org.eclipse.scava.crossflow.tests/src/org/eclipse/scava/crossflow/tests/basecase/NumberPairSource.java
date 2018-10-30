package org.eclipse.scava.crossflow.tests.basecase;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NumberPairSource extends NumberPairSourceBase {

	@Override
	public void produce() {
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				NumberPair pair = new NumberPair();
				pair.setA(new Random().nextInt(2));
				pair.setB(new Random().nextInt(2));
				//System.out.println("[" + workflow.getName() + "] Sending " + pair.getA() + " + " + pair.getB());
				getAdditions().send(pair,this.getClass().getName());
			}
		}, 0, 100);
	}

}
