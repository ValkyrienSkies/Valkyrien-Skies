package org.valkyrienskies.mod.common.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import org.valkyrienskies.mod.common.util.cqengine.TransactionalUpdatableIndexedCollection;
import sun.plugin.dom.exception.InvalidStateException;

public class CQEngineSerializationModule<O> extends SimpleModule {

    public CQEngineSerializationModule(Class<O> vc) {
        super.addDeserializer(TransactionalUpdatableIndexedCollection.class,
            new TransactionalUpdatableIndexedCollectionDeserializer<>(vc));
    }

    static class TransactionalUpdatableIndexedCollectionDeserializer<O> extends
        StdDeserializer<TransactionalUpdatableIndexedCollection<O>> {

        public TransactionalUpdatableIndexedCollectionDeserializer(Class<O> vc) {
            super(vc);
        }

        @Override
        @SuppressWarnings("unchecked")
        public TransactionalUpdatableIndexedCollection<O> deserialize(JsonParser p,
            DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ObjectCodec codec = p.getCodec();
            JsonNode collectionJson = codec.readTree(p);

            TransactionalUpdatableIndexedCollection<O> collection =
                new TransactionalUpdatableIndexedCollection<>((Class<O>) _valueClass);

            if (!collectionJson.isArray()) throw new InvalidStateException("");
            for (JsonNode jsonElem : collectionJson) {
                O element = (O) jsonElem.traverse(codec).readValueAs(_valueClass);
                collection.add(element);
            }

            return collection;
        }
    }

}
