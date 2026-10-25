package models;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private final HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getHttpSession() {
        return session;
    }

    public void set(String key, Object value) {
        session.setAttribute(key, value);
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void remove(String key) {
        session.removeAttribute(key);
    }

    public void invalidate() {
        session.invalidate();
    }

    public Map<String, Object> getAll() {
        Map<String, Object> map = new HashMap<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            map.put(key, session.getAttribute(key));
        }
        return Collections.unmodifiableMap(map);
    }
}
