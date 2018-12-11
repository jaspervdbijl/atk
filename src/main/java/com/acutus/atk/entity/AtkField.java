package com.acutus.atk.entity;


import com.acutus.atk.util.Assert;
import com.acutus.atk.util.AtkUtil;
import lombok.*;

import java.lang.reflect.Field;

public class AtkField<T,R> {

    @Getter
    private Field field;

    @Getter
    private T oldValue;

    @Getter
    private R entity;

    private com.acutus.atk.entity.processor.AtkField atkField;

    @Getter
    private Class<T> type;

    @Getter @Setter
    private boolean audit,changed;

    public AtkField(Class<T> type,Field field,R entity) {
        this.type = type;
        this.field = field;
        this.entity = entity;
        this.field.setAccessible(true);
        init(field.getAnnotation(com.acutus.atk.entity.processor.AtkField.class));
    }

    public void init(com.acutus.atk.entity.processor.AtkField atkField) {
        if (atkField == null) return;
        audit = atkField.audit();
    }

    @SneakyThrows
    public R set(T value) {
        if (audit) {
            oldValue = (T) field.get(entity);
        }
        changed = !AtkUtil.equals(field.get(entity),value);
        field.set(entity,value);
        return entity;
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
