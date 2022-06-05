/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.code.rudp

class Counters {
    private val MAX_SEQUENCE_NUMBER = 255

    @Synchronized
    fun nextSequenceNumber(): Int {
        return nextSequenceNumber(seqn).also { seqn = it }
    }

    /**
     * Computes the consecutive sequence number.
     *
     * @return the next number in the sequence.
     */
    private fun nextSequenceNumber(seqn: Int): Int {
        return (seqn + 1) % MAX_SEQUENCE_NUMBER
    }

    @Synchronized
    fun setSequenceNumber(n: Int): Int {
        seqn = n
        return seqn
    }

    @Synchronized
    fun setLastInSequence(n: Int) {
        lastInSequence = n
    }

    @Synchronized
    fun getLastInSequence(): Int {
        return lastInSequence
    }

    @Synchronized
    fun incCumulativeAckCounter() {
        cumAckCounter++
    }

    @Synchronized
    fun getCumulativeAckCounter(): Int {
        return cumAckCounter
    }

    @Synchronized
    fun getAndResetCumulativeAckCounter(): Int {
        val tmp = cumAckCounter
        cumAckCounter = 0
        return tmp
    }

    @Synchronized
    fun incOutOfSequenceCounter() {
        outOfSeqCounter++
    }

    @Synchronized
    fun getOutOfSequenceCounter(): Int {
        return outOfSeqCounter
    }

    @Synchronized
    fun getAndResetOutOfSequenceCounter() {
        outOfSeqCounter = 0
    }

    @Synchronized
    fun incOutstandingSegsCounter() {
        outSegsCounter++
    }

    @Synchronized
    fun getOutstandingSegsCounter(): Int {
        return outSegsCounter
    }

    @Synchronized
    fun getAndResetOutstandingSegsCounter() {
        outSegsCounter = 0
    }

    @Synchronized
    fun reset() {
        outOfSeqCounter = 0
        outSegsCounter = 0
        cumAckCounter = 0
    }

    private var seqn /* Segment sequence number */ = 0
    private var lastInSequence /* Last in-sequence received segment */ = 0

    /*
     * The receiver maintains a counter of unacknowledged segments received
     * without an acknowledgment being sent to the transmitter. The maximum
     * value of this counter is configurable. If this counter's maximum is
     * exceeded, the receiver sends either a stand-alone acknowledgment, or
     * an extended acknowledgment if there are currently any out-of-sequence
     * segments. The recommended value for the cumulative acknowledge counter
     * is 3.
     */
    /* Cumulative acknowledge counter */
    private var cumAckCounter = 0

    /*
     * The receiver maintains a counter of the number of segments that have
     * arrived out-of-sequence. Each time this counter exceeds its configurable
     * maximum, an extended acknowledgment segment containing the sequence
     * numbers of all current out-of-sequence segments that have been received
     * is sent to the transmitter. The counter is then reset to zero. The
     * recommended value for the out-of-sequence acknowledgments counter is 3.
     */
    /* Out-of-sequence acknowledgments counter */
    private var outOfSeqCounter = 0

    /*
     * The transmitter maintains a counter of the number of segments that
     * have been sent without getting an acknowledgment. This is used
     * by the receiver as a mean of flow control.
     */
    /* Outstanding segments counter */
    private var outSegsCounter = 0
}