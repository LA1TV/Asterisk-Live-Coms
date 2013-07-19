Asterisk-Live-Coms
==================

A server written in java which provides an interface to Asterisk to facilitate dynamically routing channels between holding and other channels.

It communicates with asterisk who's website is http://www.asterisk.org/ .

The server requires the following arguments: [Asterisk Server IP] [Asterisk Server Port] [Asterisk Server User] [Asterisk Server Password]

It uses the Asterisk Manager Interface (AMI) to connect to Asterisk and this is configured in "manager.conf". I have provided the configuration files I have used (information below).

Huge thanks to the people at asteriskjava.org for writing the library that is used to communicate with the AMU.

The "Asterisk Live Coms" server expects the structure of "extensions.conf" to be configured like the sample to work and is not meant to manage anything itself whilst this server is used.

The "Asterisk Live Coms" communication protocol uses requests encoded in the JSON format and sends responses also in JSON. I haven't written any documentation yet so please look in the source code. Specifically "IncomingCommandHandler.java". It also sends out events when a call is connected and disconnected so you don't need to keep polling the "getChannels" action.

The sample config files are in the "Asterisk Server Config" folder.

Please let me know if you use thie for anything.

Thanks!
