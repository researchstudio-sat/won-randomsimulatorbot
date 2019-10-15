# won-randomsimulatorbot

This Bot creates new Atoms using TTL files in a specified folder as templates. If contacted with Hints or Connects, it randomly establishes some connections while denying others. It will, at random either respond to incoming messages in these connections or close them again. All its actions have a random delay within a few seconds. After receiving a connection message it may choose to validate the connection data.

## Running the bot

### Prerequisites

- [Java 8 JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher installed (openJDK 12 is currently not supported and won't work)
- Maven framework set up

### On the command line

```
cd won-randomsimulatorbot
export WON_NODE_URI="https://hackathonnode.matchat.org/won"
export ATOM_TEMPLATE_DIR="src/main/resources/atom-templates"
mvn clean package
java -jar target/bot.jar
```
Note: this only works if you have the bouncycastle libraries installed for your java installation. If you don't have that, the following line will start the bot 

```
#(assuming you ran the code above, so you have the environment variables set)
java -cp "target/bot.jar;target/bouncycastle-libs/bcpkix-jdk15on-1.52.jar;target/bouncycastle-libs/bcprov-jdk15on-1.52.jar" won.bot.randomsimulatorbot.RandomSimulatorBotApp
```
This code works on windows. Replace the `;`s with `:`s to run it under Unix.
