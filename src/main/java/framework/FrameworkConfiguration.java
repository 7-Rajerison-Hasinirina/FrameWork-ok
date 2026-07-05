package framework;

import java.util.HashMap;

public class FrameworkConfiguration {

    private String controllerPackage;
    private HashMap<UrlMethod, Mapping> urlMappings;

    public FrameworkConfiguration() {
        urlMappings = new HashMap<>();
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public HashMap<UrlMethod, Mapping> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(HashMap<UrlMethod, Mapping> urlMappings) {
        this.urlMappings = urlMappings;
    }
}