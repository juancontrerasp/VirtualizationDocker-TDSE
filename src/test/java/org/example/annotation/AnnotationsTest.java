package org.example.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationsTest {

    @Test
    void restController_hasRuntimeRetention() {
        Retention retention = RestController.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void restController_targetsType() {
        Target target = RestController.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value());
    }

    @Test
    void getMapping_hasRuntimeRetention() {
        Retention retention = GetMapping.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void getMapping_targetsMethod() {
        Target target = GetMapping.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertArrayEquals(new ElementType[]{ElementType.METHOD}, target.value());
    }

    @Test
    void requestParam_hasRuntimeRetention() {
        Retention retention = RequestParam.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void requestParam_targetsParameter() {
        Target target = RequestParam.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertArrayEquals(new ElementType[]{ElementType.PARAMETER}, target.value());
    }

    @Test
    void requestParam_defaultValueAttribute_defaultsToEmptyString() throws Exception {
        String defaultVal = (String) RequestParam.class
                .getDeclaredMethod("defaultValue")
                .getDefaultValue();
        assertEquals("", defaultVal);
    }
}
