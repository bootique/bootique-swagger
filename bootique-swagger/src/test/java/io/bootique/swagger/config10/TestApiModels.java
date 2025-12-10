package io.bootique.swagger.config10;

import java.util.List;
import java.util.Map;

public class TestApiModels {

    public static class TestO1 {
        public String getA() {
            return null;
        }
    }

    public static class TestO2 {

        public TestO1 getO1() {
            return null;
        }

        public String getA() {
            return null;
        }
    }

    public static class TestO3 {

        public List<TestO1> getO1s() {
            return null;
        }

        public String getA() {
            return null;
        }
    }

    public static class TestO4 {
        public Map<String, TestO3> getA() {
            return null;
        }

        public List<TestO1> getB() {
            return null;
        }
    }
}
