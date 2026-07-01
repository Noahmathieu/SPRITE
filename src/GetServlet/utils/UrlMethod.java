package GetServlet.utils;

import java.util.Objects;

public class UrlMethod {
    private  String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UrlMethod)) return false;
        UrlMethod o = (UrlMethod) other;
        return Objects.equals(this.url, o.url) && Objects.equals(this.method, o.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}
