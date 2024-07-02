package tkaxv7s.xposed.sesame.ui;

import lombok.Data;

@Data
public class ObjReference<T> {

    private T obj;

    public ObjReference() {
    }

    public ObjReference(T obj) {
        this.obj = obj;
    }

    public Boolean has() {
        return this.obj != null;
    }

    public T get() {
        return obj;
    }

    public Boolean set(T obj) {
        if (this.obj == obj) {
            return true;
        }
        if (this.obj != null) {
            return false;
        }
        this.obj = obj;
        return true;
    }

    public void setForce(T obj) {
        this.obj = obj;
    }

    public void del() {
        this.obj = null;
    }

    public void delIfEquals(T obj) {
        if (this.obj == obj) {
            this.obj = null;
        }
    }
}
