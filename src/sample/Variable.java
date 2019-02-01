package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Variable {
    private final StringProperty identity;
    private final StringProperty  value;

    public Variable(String identity, String value) {
        this.identity = new SimpleStringProperty(identity);
        this.value = new SimpleStringProperty(value);
    }

    public StringProperty getIdentityProperty() {
        return this.identity;
    }

    public String getIdentity() {
        return this.identity.get();
    }

    public StringProperty getValueProperty() {
        return this.value;
    }

    public String getValue() {
        return this.value.get();
    }

    public void setValue(String newValue) {
        this.value.setValue(newValue);
    }

    @Override
    public String toString() {
        return "|" + this.identity.get() + "|: " + this.value.get();
    }
}
