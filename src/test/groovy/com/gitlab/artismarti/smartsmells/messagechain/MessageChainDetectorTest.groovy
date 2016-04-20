package com.gitlab.artismarti.smartsmells.messagechain

import com.gitlab.artismarti.smartsmells.common.Test
import spock.lang.Specification

/**
 * @author artur
 */
class MessageChainDetectorTest extends Specification {

	def "find one message chains with size 2"() {
		expect:
		smells.size() == 1
		smells.get(0).chainSize == 2
		smells.get(0).chainSizeThreshold == 2
		smells.get(0).sourceEntity == "chainMiddle"
		smells.get(0).targetEntity == "complexComputation"
		smells.get(0).sourcePath != null
		smells.get(0).sourceRange != null

		where:
		smells = new MessageChainDetector().run(Test.MESSAGE_CHAIN_PATH)
	}
}
