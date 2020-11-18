package com.cisco.jtapi.monitorlines;

// Copyright (c) 2020 Cisco and/or its affiliates.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import javax.telephony.*;
import javax.telephony.events.*;
import javax.telephony.callcontrol.*;
import com.cisco.jtapi.extensions.*;
import com.cisco.cti.util.Condition;
import javax.telephony.callcontrol.events.CallCtlTermConnHeldEv;
import javax.telephony.callcontrol.events.CallCtlTermConnRingingEv;
import javax.telephony.callcontrol.events.CallCtlTermConnTalkingEv;
import javax.telephony.callcontrol.events.CallCtlTermConnDroppedEv;
import javax.telephony.callcontrol.events.CallCtlTermConnInUseEv;
import javax.telephony.callcontrol.events.CallCtlTermDoNotDisturbEv;

import javax.telephony.callcontrol.events.CallCtlConnEstablishedEv;
import javax.telephony.callcontrol.events.CallCtlConnDisconnectedEv;
import javax.telephony.callcontrol.events.CallCtlConnAlertingEv;
import javax.telephony.callcontrol.events.CallCtlConnDialingEv;
import javax.telephony.callcontrol.events.CallCtlConnQueuedEv;

// Websocket Context, for Javalin web server updates
import io.javalin.websocket.WsContext;

public class Handler implements

        ProviderObserver, TerminalObserver, AddressObserver, CallControlCallObserver {

    public Condition providerInService = new Condition();
    public Condition fromTerminalInService = new Condition();
    public Condition fromAddressInService = new Condition();
    public Condition callActive = new Condition();
    public WsContext ctx;

    public void providerChangedEvent(ProvEv[] events) {
        for (ProvEv ev : events) {
            System.out.println("    Received--> Provider/" + ev);
            switch (ev.getID()) {
                case ProvInServiceEv.ID:
                    providerInService.set();
                    break;
            }
        }
    }

    public void terminalChangedEvent(TermEv[] events) {
        for (TermEv ev : events) {
            // System.out.println(" Received--> Terminal/"+ev);
            switch (ev.getID()) {
                case CiscoTermInServiceEv.ID:
                    fromTerminalInService.set();
                    break;
            }
        }
    }

    public void addressChangedEvent(AddrEv[] events) {
        for (AddrEv ev : events) {
            // System.out.println(" Received--> Address/"+ev);
            switch (ev.getID()) {
                case CiscoAddrInServiceEv.ID:
                    fromAddressInService.set();
                    break;
            }
        }
    }

    // getDN is not the most elegant way to extract the associated DN for a Call
    // Event, but it is here
    // as a placeholder until a more deterministic way to extract the DN from a call
    // event is sorted out
    public String getDN(String evText, String textEv) {
        int startEv = evText.indexOf(textEv);
        int endEv = startEv + textEv.length();
        int colonEv = evText.indexOf(':');
        String sDN = evText.substring(endEv, colonEv);
        return sDN;
    }

    public void callChangedEvent(CallEv[] events) {
        for (CallEv ev : events) {
            // System.out.println(" Received--> Call/"+ev);
            // System.out.println(" From Observed: " );
            String sEventText = "";
            String sAffectedDN = "";
            String sFromDN = "";
            String sToDN = "";
            switch (ev.getID()) {
                case CallActiveEv.ID:
                    callActive.set();
                    // System.out.println(" Received----> CallActiveEv");
                    break;
                case CallCtlTermConnRingingEv.ID:
                    // System.out.println(" Received----> CallCtlTermConnRingingEv");
                    break;
                case CallCtlTermConnTalkingEv.ID:
                    // System.out.println(" Received----> CallCtlTermConnTalkingEv");
                    break;
                case CallCtlTermConnHeldEv.ID:
                    // System.out.println(" Received----> CallCtlTermConnHeldEv");
                    break;
                case CallCtlTermConnDroppedEv.ID:
                    // System.out.println(" Received----> CallCtlTermConnDroppedEv");
                    break;
                case CallCtlTermConnInUseEv.ID:
                    // System.out.println(" Received----> CallCtlTermConnInUseEv");
                    break;
                case CallCtlTermDoNotDisturbEv.ID:
                    // System.out.println(" Received----> CallCtlTermDoNotDisturbEv");
                    break;

                case CallCtlConnEstablishedEv.ID:
                    sEventText = "CallCtlConnEstablishedEv";
                    sAffectedDN = this.getDN(ev.toString(), sEventText);
                    sFromDN = "";
                    sToDN = "";
                    // Sending to Websocket
                    // TODO: Handle the From and To DN names better, below it assumes From in index 0 and to in 
                    // index 1 which works fine for dCloud demo Jabber extensions but not for lines on physical phones
                    // TODO:  Handle extension/DN names longer than 4 characteres correctly (in dCloud they are 4 digits long so it 
                    // works fine there for now)
                    System.out.println("##### Status Changed(Established)- ESTB: " + sAffectedDN);
                    System.out.println("Received----> " + sEventText + " for DN " + sAffectedDN);
                    if (ev.getCall().getConnections().length > 1) {
                        sToDN = ev.getCall().getConnections()[1].getAddress().getName();
                        System.out.println(" Destination DN: " + sToDN);
                    }
                    if (ev.getCall().getConnections().length > 0) {
                        sFromDN = ev.getCall().getConnections()[0].getAddress().getName();
                        System.out.println(" Source DN (CallerID): " + sFromDN);
                    }
                    System.out.println(" FROM: " + sFromDN + " TO: " + sToDN);
                    ctx.send("Status Changed: ESTB--" + sAffectedDN + " (FROM: " + sFromDN + " TO: " + sToDN);
                    System.out.println("");
                    System.out.println("========================================");
                    break;
                case CallCtlConnDisconnectedEv.ID:
                    sEventText = "CallCtlConnDisconnectedEv";
                    sAffectedDN = this.getDN(ev.toString(), sEventText);
                    // Sending to Websocket
                    System.out.println("##### Status Changed(Disconnected)- IDLE: " + sAffectedDN);
                    ctx.send("Status Changed: IDLE--" + sAffectedDN);
                    System.out.println("Received----> " + sEventText + " for DN " + sAffectedDN);
                    System.out.println("========================================");
                    break;
                case CallCtlConnAlertingEv.ID:
                    sEventText = "CallCtlConnAlertingEv";
                    sAffectedDN = this.getDN(ev.toString(), sEventText);
                    // Sending the status to web server
                    System.out.println("##### Status Changed(Alerting)- ALERTING: " + sAffectedDN + " (CallerID: "
                            + ev.getCall().getConnections()[0].getAddress().getName() + ")");
                    ctx.send("Status Changed: ALERTING--" + sAffectedDN + " (CallerID: "
                            + ev.getCall().getConnections()[0].getAddress().getName() + ")");
                    System.out.println("Received----> " + sEventText + " for DN " + sAffectedDN);
                    if (ev.getCall().getConnections().length > 0) {
                        System.out.print(" Destination DN: " + ev.getCall().getConnections()[1].getAddress().getName());
                    }
                    if (ev.getCall().getConnections().length > 1) {
                        System.out.print(
                                " Source DN (CallerID): " + ev.getCall().getConnections()[0].getAddress().getName());
                    }
                    System.out.println("");
                    System.out.println("========================================");
                    break;
                case CallCtlConnDialingEv.ID:
                    sEventText = "CallCtlConnDialingEv";
                    sAffectedDN = this.getDN(ev.toString(), sEventText);
                    // Sending the status to web server
                    System.out.println("##### Status Changed(Dialing)- HELD: " + sAffectedDN);
                    ctx.send("Status Changed: HELD--" + sAffectedDN);
                    System.out.println("Received----> " + sEventText + " for DN " + sAffectedDN);
                    System.out.println("Dialing to " + ev.getCall().getConnections()[1].getAddress().getName());
                    if (ev.getCall().getConnections().length > 0) {
                        System.out.print(" Destination DN: " + ev.getCall().getConnections()[1].getAddress().getName());
                    }
                    if (ev.getCall().getConnections().length > 1) {
                        System.out.print(
                                " Source DN (CallerID): " + ev.getCall().getConnections()[0].getAddress().getName());
                    }
                    System.out.println("");
                    System.out.println("========================================");
                    break;
                case CallCtlConnQueuedEv.ID:
                    sEventText = "CallCtlConnQueuedEv";
                    sAffectedDN = this.getDN(ev.toString(), sEventText);
                    // Sending to Websocket
                    System.out.println("##### Status Changed(Queued)- QUEUED: " + sAffectedDN);
                    ctx.send("Status Changed: QUEUED--" + sAffectedDN);
                    System.out.println("Received----> " + sEventText + " for DN " + sAffectedDN);
                    if (ev.getCall().getConnections().length > 0) {
                        System.out.print(" Destination DN: " + ev.getCall().getConnections()[1].getAddress().getName());
                    }
                    if (ev.getCall().getConnections().length > 1) {
                        System.out.print(
                                " Source DN (CallerID): " + ev.getCall().getConnections()[0].getAddress().getName());
                    }
                    System.out.println("");
                    System.out.println("========================================");
                    break;
            }
        }

    }

    public void setContext(WsContext ctx) {
        this.ctx = ctx;
    }

    public WsContext getContext() {
        return this.ctx;
    }

}