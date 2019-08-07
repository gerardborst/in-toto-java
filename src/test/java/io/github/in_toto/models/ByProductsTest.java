package io.github.in_toto.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

class ByProductsTest {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testConstructorEqualsAndHashCode() {
        ByProducts prods = new ByProducts();
        prods.setReturnValue(1);
        prods.setStdout("foo");
        prods.setStderr("bar");
        ByProducts prods2 = new ByProducts("foo", "bar", 1);
        assertEquals(prods, prods2);
        assertEquals(prods.hashCode(), prods2.hashCode());
    }
    
    @Test
    void testJsonSerialize() {
        ByProducts prods = new ByProducts("foo", "bar", 1);
        String expectedJson = "{\"stdout\":\"foo\",\"stderr\":\"bar\",\"return-value\":1}";
        Gson gson = new Gson();
        String actualsJson = gson.toJson(prods);
        assertEquals(expectedJson, actualsJson);
    }
    

}