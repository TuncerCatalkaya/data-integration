package org.dataintegration.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("checkstyle:all")
public class DataIntegrationUtilsConfig {

    public static String USER_ID_CLAIM;

    @Autowired
    public DataIntegrationUtilsConfig(Environment env) {
        USER_ID_CLAIM = env.getProperty("jwt.user-id-claim-name");
    }

}
