package util;

public class TestUtil {
    public static void setEnvironmentToTest() {
        //set env variable to test
        System.setProperty("env", "test");
        String env = System.getProperty("env");
        System.out.println("Environment Variable env: " + env);
    }
}
