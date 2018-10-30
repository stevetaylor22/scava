package org.eclipse.scava.crossflow.tests.basecase;

public class Printer extends PrinterBase {

	@Override
	public void consumeAdditionResults(Number number) {
		System.out.println("[" + workflow.getName() + "] Result is " + number.getN() + " (" + number.isCached() + ")");
	}
}
