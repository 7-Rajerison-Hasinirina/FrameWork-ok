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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlMethod that = (UrlMethod) o;
        return Objects.equals(url, that.url) &&
               Objects.equals(methodeHttp.toUpperCase(), that.methodeHttp.toUpperCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methodeHttp.toUpperCase());
    }
}