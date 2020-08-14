package org.valkyrienskies.mod.common.util.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.valkyrienskies.mod.common.util.jackson.annotations.PacketIgnore;

public class PacketMapperTest {

    @Test
    public void testPacketIgnore() throws IOException {
        // Create some new data
        Data obj = new Data();
        obj.toIgnore = "useless gunk";
        obj.important = "important network transmission";

        // Get the mappers
        ObjectMapper mapper = VSJacksonUtil.getDefaultMapper();
        ObjectMapper packetMapper = VSJacksonUtil.getPacketMapper();

        // Serialize the data with each mapper
        byte[] packetBytes = packetMapper.writeValueAsBytes(obj);
        byte[] persistBytes = mapper.writeValueAsBytes(obj);

        // Deserialize with each mapper
        Data deserPacket = packetMapper.readValue(packetBytes, Data.class);
        Data deserPersist = mapper.readValue(persistBytes, Data.class);

        assertThat(deserPacket.toIgnore, is(nullValue()));
        assertThat(deserPacket.important, is(obj.important));

        assertThat(deserPersist.toIgnore, is(obj.toIgnore));
        assertThat(deserPersist.important, is(obj.important));

        assertThat(packetBytes.length, is(lessThan(persistBytes.length)));
    }



}

class Data {

    @PacketIgnore // Data is ignored by packet mapper
    public String toIgnore;
    public String important;

}