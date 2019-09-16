/*
 * Copyright 2012 Research Studios Austria Forschungsges.m.b.H. Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package won.bot.randomsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.lang.invoke.MethodHandles;

@Component
public class RandomSimulatorBotApp implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static void main(String[] args) throws Exception {
        boolean failed = false;
        if(Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
            logger.error("JCE unlimited strength encryption policy is not enabled, WoN applications will not work. Please consult the setup guide.");
            failed = true;
        }
        if(System.getProperty("WON_NODE_URI") == null && System.getenv("WON_NODE_URI") == null) {
            logger.error("WON_NODE_URI needs to be set to the node you want to connect to. e.g. https://hackathonnode.matchat.org/won");
            failed = true;
        }
        if(System.getProperty("WON_CONFIG_DIR") == null && System.getenv("WON_CONFIG_DIR") == null) {
            logger.error("WON_CONFIG_DIR needs to be set");
            failed = true;
        }

        if (failed) {
            System.exit(1);
        }
        SpringApplication app = new SpringApplication("classpath:/spring/app/botApp.xml");
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(final String... strings) throws Exception {
    }
}
