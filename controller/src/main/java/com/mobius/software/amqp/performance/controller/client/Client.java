/**
 * Mobius Software LTD
 * Copyright 2015-2016, Mobius Software LTD
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.mobius.software.amqp.performance.controller.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.mobius.software.amqp.parser.avps.HeaderCode;
import com.mobius.software.amqp.parser.avps.OutcomeCode;
import com.mobius.software.amqp.parser.avps.ReceiveCode;
import com.mobius.software.amqp.parser.avps.RoleCode;
import com.mobius.software.amqp.parser.avps.SectionCode;
import com.mobius.software.amqp.parser.avps.SendCode;
import com.mobius.software.amqp.parser.header.api.AMQPHeader;
import com.mobius.software.amqp.parser.header.impl.*;
import com.mobius.software.amqp.parser.sections.AMQPData;
import com.mobius.software.amqp.parser.sections.AMQPSection;
import com.mobius.software.amqp.performance.commons.data.Command;
import com.mobius.software.amqp.performance.commons.data.ErrorType;
import com.mobius.software.amqp.performance.commons.data.Link;
import com.mobius.software.amqp.performance.commons.data.PropertyType;
import com.mobius.software.amqp.performance.commons.util.AmqpInitialResponse;
import com.mobius.software.amqp.performance.commons.util.CommandParser;
import com.mobius.software.amqp.performance.commons.util.MessageGenerator;
import com.mobius.software.amqp.performance.commons.util.ReverseMap;
import com.mobius.software.amqp.performance.controller.Orchestrator;
import com.mobius.software.amqp.performance.controller.api.ConnectionListener;
import com.mobius.software.amqp.performance.controller.api.NetworkHandler;
import com.mobius.software.amqp.performance.controller.task.PingTimer;
import com.mobius.software.amqp.performance.controller.task.TimedTask;

import io.netty.channel.ChannelFuture;

public class Client implements ConnectionListener, TimedTask
{
	private static final int MAX_TCP_CONNECT_RETRY = 30;

	private String clientID;
	private AtomicBoolean status = new AtomicBoolean();
	private ConnectionContext ctx;
	private NetworkHandler listener;

	private ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
	private AtomicReference<Command> pendingCommand = new AtomicReference<>();
	private AtomicReference<ChannelFuture> channelHandler = new AtomicReference<ChannelFuture>();

	private AtomicLong timestamp;
	private Orchestrator orchestrator;

	private AtomicInteger failedCommands = new AtomicInteger(0);
	private IdentityReport report;

	private AtomicLong incomingHandleCounter = new AtomicLong();
	private ReverseMap<Long, Link> outgoingLinks = new ReverseMap<>();
	private ReverseMap<Long, Link> incomingLinks = new ReverseMap<>();

	private AtomicLong nextOutgoingID = new AtomicLong();

	private Long idleTimeout;
	private PingTimer pingTimer;

	private ConcurrentHashMap<Long, AMQPTransfer> pendingTransfers = new ConcurrentHashMap<>();

	private AtomicInteger tcpConnectRetryCount = new AtomicInteger();

	public Client(String clientID, String username, String password, Orchestrator orchestrator, NetworkHandler listener, ConcurrentLinkedQueue<Command> commands)
	{
		this.clientID = clientID;
		this.ctx = new ConnectionContext(orchestrator.getProperties().getServerAddress(), orchestrator.getProperties().getResendInterval(), username, password);
		this.listener = listener;
		this.commands = commands;
		this.report = new IdentityReport(clientID);
		this.orchestrator = orchestrator;
		this.timestamp = new AtomicLong(System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay() + orchestrator.getProperties().getScenarioDelay());
	}

	@Override
	public Boolean execute()
	{
		try
		{
			if (!status.get())
			{
				Boolean previouslyNull = (channelHandler.get() == null);
				if (!previouslyNull)
				{
					ChannelFuture future = channelHandler.get();
					if (future.isDone())
					{
						if (future.isSuccess())
						{
							ctx.updateLocalAddress(listener.finishConnection(future, this));
							report.setClientAddress((InetSocketAddress) ctx.localAddress());
							status.set(true);
						}
						else
						{
							report.reportError(ErrorType.CONNECTION_LOST, "failed to establish TCP connection");
							if (!orchestrator.getProperties().isContinueOnError())
							{
								timestamp.set(Long.MAX_VALUE);
								return false;
							}
						}
					}
					else if (tcpConnectRetryCount.incrementAndGet() > MAX_TCP_CONNECT_RETRY)
					{
						report.reportError(ErrorType.CONNECTION_LOST, "failed to establish TCP connection");
						timestamp.set(Long.MAX_VALUE);
						return false;
					}
				}
				else
					channelHandler.compareAndSet(null, listener.connect(ctx.remoteAddress()));

				timestamp.set(System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay());

				return true;
			}
			else
			{
				Command nextCommand = commands.poll();
				boolean doSchedule = true;
				if (nextCommand != null)
				{
					Command previous = pendingCommand.getAndSet(nextCommand);
					if (previous != null && nextCommand.getSendTime() > 0)
					{
						report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, previous.getCode().toString());
						failedCommands.incrementAndGet();
						if (!orchestrator.getProperties().isContinueOnError())
						{
							listener.close(ctx.localAddress());
							timestamp.set(Long.MAX_VALUE);
							return false;
						}
					}

					AMQPHeader message = CommandParser.toMessage(nextCommand, clientID);
					switch (message.getCode())
					{
					case PROTO:
					case BEGIN:
						doSchedule = false;
						break;
					case END:
					case CLOSE:
						stopPingTimer();
						break;

					case INIT:
						SASLInit init = (SASLInit) message;
						AmqpInitialResponse response = new AmqpInitialResponse(ctx.getUsername(), ctx.getPassword());
						init.setInitialResponse(response.encodeChallenge());
						doSchedule = false;
						break;

					case OPEN:
						AMQPOpen open = (AMQPOpen) message;
						this.idleTimeout = open.getIdleTimeout();
						doSchedule = false;
						break;

					case ATTACH:
						AMQPAttach attach = (AMQPAttach) message;
						attach.setHandle(incomingHandleCounter.incrementAndGet());

						if (attach.getRole() == RoleCode.RECEIVER)
						{
							Link link = attach.getRcvSettleMode() == ReceiveCode.FIRST //
									? Link.withoutSettleRequired(attach.getHandle(), attach.getTarget().getAddress()) //
									: Link.withSettleRequired(attach.getHandle(), attach.getTarget().getAddress());
							incomingLinks.putIfAbsent(attach.getHandle(), link);
						}
						else
						{
							Link link = attach.getSndSettleMode() == SendCode.SETTLED//
									? Link.withoutSettleRequired(attach.getHandle(), attach.getSource().getAddress()) //
									: Link.withSettleRequired(attach.getHandle(), attach.getSource().getAddress());
							outgoingLinks.putIfAbsent(attach.getHandle(), link);
						}

						break;

					case DETACH:
						AMQPDetach detach = (AMQPDetach) message;
						String detachAddress = nextCommand.getCommandProperties().stream().filter(c -> c.getType() == PropertyType.ADDRESS).findFirst().get().getValue();
						Long detachHandle = incomingLinks.removeByValue(Link.key(detachAddress));
						if (detachHandle != null)
							detach.setHandle(detachHandle);
						else
						{
							report.reportError(ErrorType.UNSUBSCRIBE, "can't detach unnatached handle");
							if (!orchestrator.getProperties().isContinueOnError())
							{
								listener.close(ctx.localAddress());
								timestamp.set(Long.MAX_VALUE);
								return false;
							}
						}
						break;
					case TRANSFER:
						AMQPTransfer transfer = (AMQPTransfer) message;
						String transferAddress = nextCommand.getCommandProperties().stream().filter(c -> c.getType() == PropertyType.ADDRESS).findFirst().get().getValue();
						Long transferHandle = outgoingLinks.getKey(Link.key(transferAddress));
						if (transferHandle != null)
						{
							transfer.setHandle(transferHandle);
							Map<SectionCode, AMQPSection> sections = new LinkedHashMap<>();
							Integer messageLength = Integer.parseInt(nextCommand.getCommandProperties().stream().filter(c -> c.getType() == PropertyType.MESSAGE_LENGTH).findFirst().get().getValue());
							if (messageLength > 0)
							{
								sections.put(SectionCode.DATA, new AMQPData(MessageGenerator.generateContent(10)));
								transfer.setSections(sections);
							}
							transfer.setDeliveryId(nextOutgoingID.incrementAndGet());

							Link link = outgoingLinks.getValue(transferHandle);
							if (!link.isSettleRequired())
							{
								transfer.setRcvSettleMode(ReceiveCode.FIRST);
								pendingCommand.set(null);
							}
							else
							{
								transfer.setRcvSettleMode(ReceiveCode.SECOND);
								pendingTransfers.put(transfer.getDeliveryId(), transfer);
							}
						}
						else
						{
							report.reportError(ErrorType.PUBLISH, "can't send transfer on unattached link " + transferAddress);
							if (!orchestrator.getProperties().isContinueOnError())
							{
								listener.close(ctx.localAddress());
								timestamp.set(Long.MAX_VALUE);
								return false;
							}
						}
						break;

					default:
						break;
					}

					Command next = commands.peek();
					if (next != null)
						timestamp.set(System.currentTimeMillis() + next.getSendTime());
					else
						timestamp.set(Long.MAX_VALUE);

					if (message != null)
					{
						listener.send(ctx.localAddress(), message);
						report.countOut(message.getCode());
					}
				}
				else
					timestamp.set(Long.MAX_VALUE);

				if (hasFinished())
					orchestrator.notifyOnFinished(clientID);

				return doSchedule && !commands.isEmpty();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void packetReceived(SocketAddress address, AMQPHeader message)
	{
		try
		{
			report.countIn(message.getCode());
			Command pending = pendingCommand.get();
			switch (message.getCode())
			{
			case PROTO:
				if (pending != null && pending.getCode() == HeaderCode.PROTO)
				{
					AMQPProtoHeader proto = (AMQPProtoHeader) message;
					if (proto.getProtocolId() == 0)
					{
						pendingCommand.set(null);
						orchestrator.getScheduler().store(getNextTimestamp(), this);
					}
				}
				break;

			case MECHANISMS:
				if (pending == null || pending.getCode() != HeaderCode.PROTO)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					pendingCommand.set(null);
					orchestrator.getScheduler().store(getNextTimestamp(), this);
				}
				break;

			case OUTCOME:
				if (pending == null || pending.getCode() != HeaderCode.INIT)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					SASLOutcome outcome = (SASLOutcome) message;
					if (outcome.getOutcomeCode() != OutcomeCode.OK)
						report.reportError(ErrorType.CONNECT, "received outcome code:" + outcome.getOutcomeCode() + (pending != null ? ", pending " + pending.getCode() : ""));
					else
					{
						pendingCommand.set(null);
						orchestrator.getScheduler().store(getNextTimestamp(), this);
					}
				}
				break;

			case OPEN:
				if (pending == null || pending.getCode() != HeaderCode.OPEN)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					pendingCommand.set(null);
					orchestrator.getScheduler().store(getNextTimestamp(), this);
				}
				break;

			case BEGIN:
				if (pending == null || pending.getCode() != HeaderCode.BEGIN)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					if (this.idleTimeout != null && this.idleTimeout > 0)
					{
						this.pingTimer = new PingTimer(ctx, orchestrator.getScheduler(), listener, idleTimeout, report);
						orchestrator.getScheduler().store(this.pingTimer.getRealTimestamp(), this.pingTimer);
					}
					pendingCommand.set(null);
					orchestrator.notifyOnStarted(this);
				}
				break;

			case ATTACH:
				if (pending == null || pending.getCode() != HeaderCode.ATTACH)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					AMQPAttach attach = (AMQPAttach) message;
					if (attach.getRole() == RoleCode.SENDER)
					{
						Link link = attach.getSndSettleMode() == SendCode.SETTLED//
								? Link.withoutSettleRequired(attach.getHandle(), attach.getSource().getAddress())//
								: Link.withSettleRequired(attach.getHandle(), attach.getSource().getAddress());
						outgoingLinks.put(attach.getHandle(), link);
					}
					else
					{
						Link link = attach.getRcvSettleMode() == ReceiveCode.FIRST//
								? Link.withoutSettleRequired(attach.getHandle(), attach.getTarget().getAddress())//
								: Link.withSettleRequired(attach.getHandle(), attach.getTarget().getAddress());
						incomingLinks.put(attach.getHandle(), link);
					}
					pendingCommand.set(null);
				}

				break;

			case DETACH:
				if (pending == null || pending.getCode() != HeaderCode.DETACH)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
					pendingCommand.set(null);

				AMQPDetach detach = (AMQPDetach) message;
				outgoingLinks.remove(detach.getHandle());
				break;

			case TRANSFER:
				AMQPTransfer transfer = (AMQPTransfer) message;
				Link transferLink = incomingLinks.getValue(transfer.getHandle());
				if (transferLink != null)
				{
					if (transferLink.isSettleRequired())
					{
						AMQPDisposition disposition = AMQPDisposition.builder()//
								.channel(1)//
								.role(RoleCode.RECEIVER)//
								.first(transfer.getDeliveryId())//
								.last(transfer.getDeliveryId())//
								.settled(true)//
								.withStateAccepted()//
								.build();
						listener.send(ctx.localAddress(), disposition);
						report.countOut(HeaderCode.DISPOSITION);
					}
				}
				else
					report.reportError(ErrorType.PUBLISH, "received unexpected transfer handle " + transfer.getHandle());
				break;

			case DISPOSITION:
				if (pending == null || pending.getCode() != HeaderCode.TRANSFER)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					AMQPDisposition disposition = (AMQPDisposition) message;
					Long first = disposition.getFirst();
					Long last = disposition.getLast() != null ? disposition.getLast() : disposition.getFirst();
					for (long i = first; i <= last; i++)
					{
						AMQPTransfer pendingTransfer = pendingTransfers.remove(i);
						if (pendingTransfer == null)
							report.reportError(ErrorType.PUBACK, "received disposition for unknown transferIDs");
					}
					pendingCommand.set(null);
				}
				break;

			case END:
				if (pending == null || pending.getCode() != HeaderCode.END)
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "received unexpected " + message.getCode() + (pending != null ? ", pending " + pending.getCode() : ""));
				else
				{
					stopPingTimer();
					pendingCommand.set(null);
				}
				break;

			case CLOSE:
				if (pending == null || pending.getCode() != HeaderCode.CLOSE)
				{
					AMQPClose close = (AMQPClose) message;
					StringBuilder sb = new StringBuilder()//
							.append("received unexpected ")//
							.append(message.getCode())//
							.append((pending != null ? ", pending " + pending.getCode() : ""));
					if (close.getError() != null)
					{
						if (close.getError().getCondition() != null)
							sb.append(", condition=").append(close.getError().getCondition());
						if (close.getError().getDescription() != null)
							sb.append(", description=").append(close.getError().getDescription());
					}
					report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, sb.toString());
				}
				else
				{
					stopPingTimer();
					pendingCommand.set(null);
					listener.releaseLocalPort(ctx.remoteAddress(), ((InetSocketAddress) ctx.localAddress()).getPort());
					status.set(false);
					channelHandler.set(null);
					listener.close(ctx.localAddress());
				}
				break;

			default:
				break;
			}

			if (hasFinished())
				orchestrator.notifyOnFinished(clientID);
		}
		catch (TestsuiteException e)
		{
			report.reportError(ErrorType.forMessageType(e.getType()), e.getMessage());
		}
	}

	@Override
	public Long getRealTimestamp()
	{
		return timestamp.get();
	}

	public long getNextTimestamp()
	{
		long nextTimestamp = getRealTimestamp();
		long currTimestamp = System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay();
		if (nextTimestamp <= currTimestamp)
			nextTimestamp = currTimestamp;
		return nextTimestamp;
	}

	public boolean hasFinished()
	{
		return pendingCommand.get() == null && commands.isEmpty();
	}

	@Override
	public void connectionDown(SocketAddress address)
	{
		stopPingTimer();
		if (!hasFinished())
			report.reportError(ErrorType.CONNECTION_LOST, "connection lost with unfinished commands");
	}

	@Override
	public void stop()
	{
		if (ctx.localAddress() != null)
			listener.close(ctx.localAddress());

		stopPingTimer();
	}

	private void stopPingTimer()
	{
		if (pingTimer != null)
		{
			pingTimer.stop();
			pingTimer = null;
		}
	}

	public IdentityReport retrieveReport()
	{
		int unfinishedCommands = commands.size() + failedCommands.get();
		if (pendingCommand.get() != null)
			unfinishedCommands++;
		report.setUnfinishedCommands(unfinishedCommands);

		while (!commands.isEmpty())
			report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, "command didn't execute:" + commands.poll().getCode());

		return report;
	}

	public String getClientID()
	{
		return clientID;
	}
}
