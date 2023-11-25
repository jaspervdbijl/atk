package com.acutus.atk.entity;


import com.acutus.atk.util.Assert;
import com.acutus.atk.util.AtkUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.acutus.atk.beans.BeanHelper.decode;
import static com.acutus.atk.beans.BeanHelper.encode;

public class AtkField<T,R> {

    @Getter
    private Field field;

    @Getter
    private T oldValue;

    @Getter
    private R entity;

    private com.acutus.atk.entity.processor.AtkField atkField;

    private List<ChangeListener> changeListeners = new ArrayList<>();

    @Getter @Setter
    private boolean audit,changed,set,ignore;

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

    public void addListener(ChangeListener<T> tChangeListener) {
        this.changeListeners.add(tChangeListener);
    }

    public void removeListener(ChangeListener<T> tChangeListener) {
        this.changeListeners.remove(tChangeListener);
    }

    @SneakyThrows
    public R set(T value) {
        if (audit) {
            oldValue = (T) field.get(entity);
        }
        changeListeners.stream().forEach(c -> AtkUtil.handle(() -> c.changed((T) field.get(entity),value)));
        // transform value if
        changed = !AtkUtil.equals(field.get(entity),value);
        set = true;
        field.set(entity,value);
        return entity;
    }

    public R deSerialize(String value) {
        set((T) decode(getType(), value));
        return entity;
    }

    public String serialize() {
        return encode(get());
    }

    public void reset() {
        changed = false;
        set = false;
        ignore = false;
    }

    @SneakyThrows
    public T get() {
        return (T) field.get(entity);
    }

    public void revert() {
        Assert.isTrue(audit,"Can only revert when audit is enabled");
        set(oldValue);
    }

    /**
     * you can choose to temporarily exclude (ignore) fields requests
     * for example if a field represents a big data blob, you want to exclude it from certain queries
     * @return
     */
    public R ignore(boolean ignore) {
        this.ignore = ignore;
        return entity;
    }

    public R ignore() {
        return ignore(true);
    }

    /**
     * copy value and state
     *
     * @param field
     */
    public void initFrom(AtkField field) {
        set((T) field.get());
        changed = field.changed;
        set = field.set;
        oldValue = (T) field.getOldValue();
        ignore = field.ignore;

    }

    public boolean isEqual(AtkField field) {
        return getField().getName().equals(field.getField().getName()) &&
                (get() == null && field.get() == null ||
                get().equals(field.get()));
    }


    public String toString() {
        return String.format("%s: %s",getField().getName(),""+get());
    }

}
