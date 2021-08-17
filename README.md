# Spring Boot with Rsocket requester / responder

This project is a demo of Spring Boot app using `RSocket protocol` with a requester and a responder.

This project contains the 4 `modes` of RSocket exchanges:
- fire-and-forget
- request-response
- streaming
- channel 

The requester can call the responder.


# RSocket client

The easiest way to test the responder is to use the RSocket client.

To interact with the responder, you need to be authenticated because security is added for each routes with differents roles.
You can launch the command bellow to authenticate against the responder.

## To download the RSocket Client CLI
  `wget -O rsc.jar https://github.com/making/rsc/releases/download/0.9.1/rsc-0.9.1.jar`

## To make the client easier to work with, set an alias
  ``alias rsc='java -jar rsc.jar'```

## To use the client to do request-response against a server on tcp://localhost:7000
  `rsc --debug --sm simple:user:pass --smmt message/x.rsocket.authentication.v0 --request --data "{ 'origin': 'Client', 'interaction': 'Request-Response' }" --route request-response tcp://localhost:7000`

## To use the client to do fire-and-forget against a server on tcp://localhost:7000
  `rsc --debug --sm simple:user:pass --smmt message/x.rsocket.authentication.v0 --fnf --data "{ 'origin': 'Client', 'interaction': 'Fire And Forget' }" --route fire-and-forget tcp://localhost:7000`

## To use the client to do stream against a server on tcp://localhost:7000
  `rsc --debug --sm simple:admin:pass --smmt message/x.rsocket.authentication.v0 --stream --data "{ 'origin': 'Client', 'interaction': 'Stream' }" --route stream tcp://localhost:7000`

## To use the client to do channel against a server on tcp://localhost:7000
  `rsc --debug --sm simple:admin:pass --smmt message/x.rsocket.authentication.v0 --channel --data - --route channel tcp://localhost:7000`

After this command, you can send input to have a stream-stream interaction in the command prompt.

# Integration test

You can launch test by launching the `mvn clean integration-test` command.