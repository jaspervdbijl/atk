package com.acutus.atk.util.collection;

import com.acutus.atk.reflection.Reflect;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.acutus.atk.util.AtkUtil.handle;

@Slf4j
public abstract class Collectable implements Closeable, AutoCloseable {

    public abstract Collectable initFromList(List values);

    public List getValues() {
        return Reflect.getFields(getClass()).stream().map(field -> handle(() -> field.get(this))).collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        for (Object o : getValues()) {
            if (o instanceof Closeable) {
                try {
                    ((Closeable) o).close();
                } catch (Exception ex) {
                    log.warn(ex.getMessage(),ex);
                }
            }
            if (o instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) o).close();
                } catch (Exception ex) {
                    log.warn(ex.getMessage(),ex);
                }
            }
        }
    }

}
