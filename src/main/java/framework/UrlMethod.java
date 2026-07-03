package framework;

import java.util.Objects;

public class UrlMethod {

    private String url;
    private String methodeHttp;

    public UrlMethod(String url, String methodeHttp) {
        this.url = url;
        this.methodeHttp = methodeHttp;
    }

    public String getUrl() {
        return url;
    }

    public String getMethodeHttp() {
        return methodeHttp;
    }

    @Override
    public boolean equals(Object objet) {

        if (this == objet) {
            return true;
        }

        if (objet == null || getClass() != objet.getClass()) {
            return false;
        }

        UrlMethod autre = (UrlMethod) objet;

        return Objects.equals(url, autre.url)
                && Objects.equals(
                        methodeHttp,
                        autre.methodeHttp
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(  url,  methodeHttp );
    }
}