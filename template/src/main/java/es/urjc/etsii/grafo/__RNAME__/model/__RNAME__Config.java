package es.urjc.etsii.grafo.__RNAME__.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
// Note that 'custom' is an example prefix, and can be replaced by whatever you want
@ConfigurationProperties(prefix = "custom")
public class __RNAME__Config {
    // Define any configuration property to automatically set from any source,
    // including the command line, the application.yml or the environment
    // See: https://mork-optimization.readthedocs.io/en/latest/features/config/ for more details

    /**
     * Value from: custom.my-property, for example from the application.yml file,
     * can also be overridden as a command line parameter: --custom.my-property=value
     * Upper case letters are converted to lower case and separated by hyphens automatically if necessary
     */
    private String myProperty;

    public String getMyProperty() {
        return myProperty;
    }

    public void setMyProperty(String myProperty) {
        this.myProperty = myProperty;
    }
}
