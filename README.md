# MossRock

This is my home automation control panel that currently just controls the lights
in my apartment. I have a set of [Intertechno](https://www.intertechno.at/)
switches installed and an [ITGW-433](https://www.intertechno.at/itgw433) gateway.
The protocol isn't public but I found some python code on the internet in random gists
and reverse engineered the rest by capturing the traffic that my Android devices
send and how the gateway responds.

## Outline

The protocol content is in  [Command](app/src/main/java/net/diibadaaba/mossrock/Command.java)
and the sending logic is in [MossRockActivity.Sender](app/src/main/java/net/diibadaaba/mossrock/MossRockActivity.java#L58)
and [MossRockActivity.Receiver](app/src/main/java/net/diibadaaba/mossrock/MossRockActivity.java#L80).
The gateway quite often fails to send the signal for some reason so if the GW doesn't send
a response, it's best to try and resend the request.