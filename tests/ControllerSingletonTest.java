import java.lang.reflect.Method;

import controllers.FrontServletController;

public class ControllerSingletonTest {
    public static void main(String[] args) throws Exception {
        FrontServletController servlet = new FrontServletController();
        Method getControllerInstance = FrontServletController.class.getDeclaredMethod("getControllerInstance",
                Class.class);
        getControllerInstance.setAccessible(true);

        Object first = getControllerInstance.invoke(servlet, TestController.class);
        Object second = getControllerInstance.invoke(servlet, TestController.class);

        if (first != second) {
            throw new AssertionError("Le framework doit réutiliser la même instance du contrôleur.");
        }

        System.out.println("ControllerSingletonTest OK");
    }

    public static class TestController {
    }
}
