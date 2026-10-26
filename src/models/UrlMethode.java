package models;

public class UrlMethode {
    private final String url;
    private final String methode;

    public UrlMethode(String url, String methode) {
        this.url = url;
        this.methode = methode;
    }

    public String getUrl() {
        return url;
    }

    public String getMethode() {
        return methode;
    }

    public String geturl() {
        return getUrl();
    }

    public String getmethode() {
        return getMethode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        UrlMethode other = (UrlMethode) obj;
        if (methode == null) {
            if (other.methode != null) {
                return false;
            }
        } else if (!methode.equals(other.methode)) {
            return false;
        }

        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methode == null) ? 0 : methode.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }
}