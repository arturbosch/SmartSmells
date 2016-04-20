package com.gitlab.artismarti.smartsmells.java;

/**
 * @author artur
 */
@SuppressWarnings("ALL") public class MessageChainDummy {

	private MessageChainer messageChainer = new MessageChainer();
	private ChainMiddle chainMiddle = new ChainMiddle();

	public void chain() {
		messageChainer.getChainEnd().complexComputation();
	}

	public void chainSizeTwo() {
		chainMiddle.getMessageChainer().getChainEnd().complexComputation();
	}

	private class MessageChainer {
		ChainEnd chainEnd = new ChainEnd();

		ChainEnd getChainEnd() {
			return chainEnd;
		}
	}


	private class ChainMiddle {
		MessageChainer messageChainer = new MessageChainer();

		MessageChainer getMessageChainer() {
			return messageChainer;
		}
	}


	private class ChainEnd {
		String complexComputation() {
			return "";
		}
	}
}
