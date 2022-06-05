/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.code.rudp

import cn.rwhps.server.net.code.rudp.RDUPPackageParsing.RudpStatus.*
import cn.rwhps.server.util.alone.annotations.DidNotFinish
import net.udp.ReliableSocketProfile
import net.udp.impl.*
import okhttp3.internal.wait
import java.io.IOException
import java.net.SocketException
import java.util.*

// 我是真不想写 RUDP
@DidNotFinish
class RDUPPackageParsing {
    private var closed = false
    private var connected = false

    private val counters = Counters()

    private val MAX_SEQUENCE_NUMBER = 255

    /* Maximum number of received segments */
    private val sendQueueSize = 32
    /* Maximum number of sent segments */
    private val recvQueueSize = 32

    private val sendBufferSize = 0
    private val recvBufferSize = 0

    /* Unacknowledged segments send queue */
    private val unackedSentQueue = ArrayList<Segment>()


    /* RUDP connection parameters */
    private var profile = ReliableSocketProfile()
    
    private var state = CLOSED

    fun parsing(segment: Segment) {
        if (segment is DATSegment || segment is NULSegment ||
            segment is RSTSegment || segment is FINSegment ||
            segment is SYNSegment
        ) {
            counters.incCumulativeAckCounter()
        }
        /*
        if (_keepAlive) {
            _keepAliveTimer.reset()
        }*/

        if (segment is SYNSegment) {
            handleSYNSegment(segment)
        } else if (segment is EAKSegment) {
            //handleEAKSegment(segment)
        } else if (segment is ACKSegment) {
            // do nothing.
        } else {
            //handleSegment(segment)
        }

        //checkAndGetAck(segment)
    }


    /**
     * Handles a received SYN segment.
     *
     *
     * When a client initiates a connection it sends a SYN segment which
     * contains the negotiable parameters defined by the Upper Layer Protocol
     * via the API. The server can accept these parameters by echoing them back
     * in its SYN with ACK response or propose different parameters in its SYN
     * with ACK response. The client can then choose to accept the parameters
     * sent by the server by sending an ACK to establish the connection or it can
     * refuse the connection by sending a FIN.
     *
     * @param segment the SYN segment.
     */
    private fun handleSYNSegment(segment: SYNSegment) {
        try {
            when (state) {
                CLOSED -> {
                    counters.setLastInSequence(segment.seq())
                    state = SYN_RCVD
                    val rand = Random(System.currentTimeMillis())
                    profile = ReliableSocketProfile(
                        sendQueueSize,
                        recvQueueSize,
                        segment.maxSegmentSize,
                        segment.maxOutstandingSegments,
                        segment.maxRetransmissions,
                        segment.maxCumulativeAcks,
                        segment.maxOutOfSequence,
                        segment.maxAutoReset,
                        segment.nulSegmentTimeout,
                        segment.retransmissionTimeout,
                        segment.cummulativeAckTimeout)

                    val syn: Segment =
                        SYNSegment(
                            counters.setSequenceNumber(rand.nextInt(MAX_SEQUENCE_NUMBER)),
                            profile.maxOutstandingSegs(),
                            profile.maxSegmentSize(),
                            profile.retransmissionTimeout(),
                            profile.cumulativeAckTimeout(),
                            profile.nullSegmentTimeout(),
                            profile.maxRetrans(),
                            profile.maxCumulativeAcks(),
                            profile.maxOutOfSequence(),
                            profile.maxAutoReset())
                    syn.ack = segment.seq()
                    sendAndQueueSegment(syn)
                }
                SYN_SENT -> {
                    counters.setLastInSequence(segment.seq());
                    state = ESTABLISHED
                    /*
                     * Here the client accepts or rejects the parameters sent by the
                     * server. For now we will accept them.
                     */
                    //sendAck()
                    //connectionOpened()
                }
                else -> {}
            }
        } catch (xcp: IOException) {
            xcp.printStackTrace()
        }
    }


    /**
     * Sends a segment and queues a copy of it in the queue of unacknowledged segments.
     *
     * @param  segment     a segment for which delivery must be guaranteed.
     * @throws IOException if an I/O error occurs in the
     * underlying UDP socket.
     */
    @Throws(IOException::class)
    private fun sendAndQueueSegment(segment: Segment) {
        synchronized (unackedSentQueue) {
            while (unackedSentQueue.size >= sendQueueSize || counters.getOutstandingSegsCounter() > profile.maxOutstandingSegs()) {
                if (!connected) {
                    throw SocketException("Socket is closed")
                }
                try {
                    unackedSentQueue.wait()
                } catch (xcp: InterruptedException) {
                    xcp.printStackTrace()
                }
            }
            counters.incOutstandingSegsCounter()
            unackedSentQueue.add(segment)
        }


        if (closed) {
            throw SocketException("Socket is closed")
        }

        /* Re-start retransmission timer */
        /*
        超时重传
        if (segment !is ACKSegment) {
            synchronized(retransmissionTimer) {
                if (retransmissionTimer.isIdle()) {
                    retransmissionTimer.schedule(profile.retransmissionTimeout().toLong(), profile.retransmissionTimeout().toLong())
                }
            }
        }
        sendSegment(segment)
        */
    }
    
    private enum class RudpStatus {
        CLOSED,
        SYN_RCVD,
        SYN_SENT,
        ESTABLISHED,
        CLOSE_WAIT;
    }
}