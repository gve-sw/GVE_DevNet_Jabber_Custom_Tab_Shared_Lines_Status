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

// Basic line monitor example.

// Change the lineDNs String array below to contain the DNs of the lines you wish to monitor for
// call events 

// Be sure to rename .env.example to .env and configure your CUCM/user/DN
//   details for the scenario.

// Tested using:

// Ubuntu Linux 20.04
// OpenJDK 11.0.8
// CUCM 11.5

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import javax.telephony.*;
import com.cisco.jtapi.extensions.*;

import io.github.cdimascio.dotenv.Dotenv;

// Javalin Web Server
import io.javalin.Javalin;
//import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsContext;


public class monitorLines {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

    private static void log(String msg) {
        System.out.println(dtf.format(LocalDateTime.now()) + " " + msg);
    }
    public static  WsContext theCTX;

    public static void setContext(WsContext ctx) {
        theCTX = ctx;
    }

    public static  WsContext getContext() {
        return theCTX;
    }
    public static void main(String[] args) throws

    JtapiPeerUnavailableException, ResourceUnavailableException, MethodNotSupportedException, InvalidArgumentException,
            PrivilegeViolationException, InvalidPartyException, InvalidStateException, InterruptedException {

        // Javalin Web Server
        Javalin app = Javalin.create().start(7000);
        app.get("/getSharedLines", ctx -> {
            ctx.render("/templates/sharedlines.html");
            System.out.println("Javalin -GET requeset from host: " + ctx.host());
        });
        
        

        // Retrieve environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();



        // The Handler class provides observers for provider/address/terminal/call
        // events
        Handler handler = new Handler();

        // Create the JtapiPeer object, representing the JTAPI library
        log("Initializing Jtapi");
        CiscoJtapiPeer peer = (CiscoJtapiPeer) JtapiPeerFactory.getJtapiPeer(null);

        // Create and open the Provider, representing a JTAPI connection to CUCM CTI
        // Manager
        String providerString = String.format("%s;login=%s;passwd=%s", dotenv.get("CUCM_ADDRESS"),
                dotenv.get("JTAPI_USERNAME"), dotenv.get("JTAPI_PASSWORD"));
        log("Connecting Provider: " + providerString);
        CiscoProvider provider = (CiscoProvider) peer.getProvider(providerString);
        log("Awaiting ProvInServiceEv...");
        provider.addObserver(handler);
        handler.providerInService.waitTrue();

        // Specify here all the directory numbers (DNs) of the shared lines to montior
        String[] lineDNs = { "6016", "6017", "6020" };

        app.ws("/websocket", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Websocket Connected with host: "+ ctx.host());
                String result = "";
                for (int i=0; i<lineDNs.length;i++) {
                    result += "#" + lineDNs[i];
                }
                ctx.send("Shared Lines: " + result);
                handler.setContext(ctx);
                setContext(ctx);
            });
            ws.onClose(ctx -> {
                System.out.println("Websocket Disconnected!");
            });
        });
        

        for (int i = 0; i < lineDNs.length; i++) {
            CiscoAddress fromAddress = (CiscoAddress) provider.getAddress(lineDNs[i]);
            log("Awaiting CiscoAddrInServiceEv for: " + fromAddress.getName() + "...");
            fromAddress.addObserver(handler);
            handler.fromAddressInService.waitTrue();
            // Add a call observer to receive call events
            fromAddress.addCallObserver(handler);
            // Get/open the first Terminal for the Address. Could be multiple
            // if it's a shared line
            CiscoTerminal fromTerminal = (CiscoTerminal) fromAddress.getTerminals()[0];
            log("Awaiting CiscoTermInServiceEv for: " + fromTerminal.getName() + "...");
            fromTerminal.addObserver(handler);
            handler.fromTerminalInService.waitTrue();
        }

        while (true) {
            try {
                Thread.sleep(5000);
                System.out.println("Trying to send keep alive...");
                if (handler.getContext()!=null){
                    handler.getContext().send("KeepAlive");
                    System.out.println("Keep alive sent!...");
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }
}
