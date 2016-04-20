package com.bouncestorage.chaoshttpproxy;

import java.util.List;
import java.util.Properties;

public interface ChaosConfig {

    Properties getProperties();

    List<Failure> getFailures();

}
