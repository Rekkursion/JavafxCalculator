package sample;

import javafx.beans.property.*;

import java.util.List;
import java.util.Objects;

public class Function {
    private final StringProperty identity;
    private final IntegerProperty paramNum;
    private final BooleanProperty isBuiltIn;

    public Function(String identity, int paramNum, boolean isBuiltIn) {
        this.identity = new SimpleStringProperty(identity);
        this.paramNum = new SimpleIntegerProperty(paramNum);
        this.isBuiltIn = new SimpleBooleanProperty(isBuiltIn);
    }

    public StringProperty getIdentityProperty() {
        return this.identity;
    }

    public String getIdentity() {
        return this.identity.get();
    }

    public IntegerProperty getParamNumProperty() { return this.paramNum; }

    public int getParamNum() { return this.paramNum.get(); }

    public BooleanProperty getIsBuiltInProperty() { return this.isBuiltIn; }

    public boolean getIsBuiltIn() { return this.isBuiltIn.get(); }

    public ExactNumber call(List<ExactNumber> numbers) {
        // TODO
        if(identity.equals("sin")) {

        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof Function)
            return this.identity.get().equals(((Function)obj).identity.get());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identity.get());
    }

    @Override
    public String toString() {
        return this.identity.get() + " param: " + this.paramNum.get();
    }
}
