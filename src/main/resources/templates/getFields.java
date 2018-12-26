
public <T extends AtkFieldList> T getFields() {
        return (T) getRefFields().stream()
        .map(f -> (AtkField) handle(() -> f.get(this)))
        .collect(Collectors.toCollection(AtkFieldList::new));
        }
