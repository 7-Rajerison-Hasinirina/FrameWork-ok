package utilitaires;

public class ParamConverter {

    public static Object convert(String value, Class<?> targetType) {
        if (value == null)
            return null;
        String v = value.trim();
        if (targetType.equals(String.class))
            return v;
        if (targetType.equals(int.class) || targetType.equals(Integer.class))
            return Integer.parseInt(v);
        if (targetType.equals(long.class) || targetType.equals(Long.class))
            return Long.parseLong(v);
        if (targetType.equals(boolean.class) || targetType.equals(Boolean.class))
            return Boolean.parseBoolean(v);
        if (targetType.equals(double.class) || targetType.equals(Double.class))
            return Double.parseDouble(v);
        if (targetType.equals(float.class) || targetType.equals(Float.class))
            return Float.parseFloat(v);
        // Fallback: attempt to return the string
        return v;
    }
}
