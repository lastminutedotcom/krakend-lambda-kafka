package com.lastminute.titans.core.krakend.lambda.kafka;

import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomSerializerTest {

    private static final byte[] EXPECTED_BYTEARRAY = "".getBytes();
    @Mock
    private Message protoMessageMocked;



    @Test
    void testSerialize() {
        CustomSerializer customSerializerSut = new CustomSerializer();
        when(this.protoMessageMocked.toByteArray()).thenReturn(EXPECTED_BYTEARRAY);
        byte[] actualSerialized = customSerializerSut.serialize(null,this.protoMessageMocked);
        assertEquals(EXPECTED_BYTEARRAY,actualSerialized);
    }

    @Test
    void testSerializeOverride() {
        CustomSerializer customSerializerSut = new CustomSerializer();
        when(this.protoMessageMocked.toByteArray()).thenReturn(EXPECTED_BYTEARRAY);
        byte[] actualSerialized = customSerializerSut.serialize(null,null,this.protoMessageMocked);
        assertEquals(EXPECTED_BYTEARRAY,actualSerialized);
    }

    @Test
    void testSerializeNullMessage() {
        CustomSerializer customSerializerSut = new CustomSerializer();
        assertThrows(NullPointerException.class,()->customSerializerSut.serialize(null,null,null));
    }
}