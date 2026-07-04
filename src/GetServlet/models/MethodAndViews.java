package GetServlet.models;

import java.util.HashMap;
import java.util.Map;

public class MethodAndViews {
    private String view;
    private Map<String, Object> model = new HashMap<>();

    public MethodAndViews(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getModel() {
        return model;
    }
    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    public void setView(String view) {
        this.view = view;
    }
}
