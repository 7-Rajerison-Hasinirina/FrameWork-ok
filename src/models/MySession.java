package models;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class MySession {
    private final HttpSession session;

    public MySession() {
        this.session = null;
    }

    public MySession(HttpSession session) {
        this.session = session;
    }

    public static MySession fromRequest(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("HttpServletRequest ne peut pas être nul.");
        }
        return new MySession(request.getSession(true));
    }

    public HttpSession getHttpSession() {
        return session;
    }

    public void set(String key, Object value) {
        if (session == null) {
            throw new IllegalStateException(
                    "La session est inactive. Utiliser HttpServletRequest.getSession(true) avant de stocker un attribut.");
        }
        session.setAttribute(key, value);
    }

    public Object get(String key) {
        if (session == null || key == null) {
            return null;
        }
        return session.getAttribute(key);
    }

    public boolean contains(String key) {
        return key != null && get(key) != null;
    }

    public boolean isEmpty() {
        if (session == null) {
            return true;
        }
        Enumeration<String> names = session.getAttributeNames();
        return names == null || !names.hasMoreElements();
    }

    public void remove(String key) {
        if (session != null && key != null) {
            session.removeAttribute(key);
        }
    }

    public void invalidate() {
        if (session != null) {
            session.invalidate();
        }
    }

    public Map<String, Object> getAll() {
        Map<String, Object> map = new HashMap<>();
        if (session == null) {
            return Collections.unmodifiableMap(map);
        }
        Enumeration<String> names = session.getAttributeNames();
        while (names != null && names.hasMoreElements()) {
            String key = names.nextElement();
            map.put(key, session.getAttribute(key));
        }
        return Collections.unmodifiableMap(map);
    }
}
