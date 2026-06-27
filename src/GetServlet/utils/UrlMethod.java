package GetServlet.utils;

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
        return this.url.equals(o.url) && this.method.equals(o.method);
    }
}
