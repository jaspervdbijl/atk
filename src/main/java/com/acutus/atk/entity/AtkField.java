package com.acutus.atk.entity;


import com.acutus.atk.util.Assert;
import com.acutus.atk.util.AtkUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class AtkField<T,R> {

    @Getter
    private Field field;

    @Getter
    private T oldValue;

    @Getter
    private R entity;

    private com.acutus.atk.entity.processor.AtkField atkField;

    @Getter @Setter
    private boolean audit,changed,set;

    public AtkField(Field field, R entity) {
        this.field = field;
        this.entity = entity;
        this.field.setAccessible(true);
        init(field.getAnnotation(com.acutus.atk.entity.processor.AtkField.class));
    }

    public void init(com.acutus.atk.entity.processor.AtkField atkField) {
        if (atkField == null) return;
        audit = atkField.audit();
    }

    public Class<T> getType() {
        return (Class<T>) field.getType();
    }

    @SneakyThrows
    public R set(T value) {
        if (audit) {
            oldValue = (T) field.get(entity);
        }
        changed = !AtkUtil.equals(field.get(entity),value);
        set = true;
        field.set(entity,value);
        return entity;
    }

    public void reset() {
        changed = false;
        set = false;
    }

    @SneakyThrows
    public T get() {
        return (T) field.get(entity);
    }

    public void revert() {
        Assert.isTrue(audit,"Can only revert when audit is enabled");
        set(oldValue);
    }

    public String toString() {
        return String.format("%s: %s",getField().getName(),""+get());
    }

}
