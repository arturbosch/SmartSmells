package com.gitlab.artismarti.smartsmells.java;

/**
 * @author artur
 */
@SuppressWarnings("ALL")
class CycleDummy {
	OtherCycle meCycle = new OtherCycle();

	public void compute() {
		meCycle.cycleDummy.meah();
	}

	private void meah() {
	}

	class InnerCycleOne {
		InnerCycleTwo cycleDummy = new InnerCycleTwo();
	}

	class InnerCycleTwo {
		String maybe = "";
		InnerCycleOne cycleOne = new InnerCycleOne();
	}
}
