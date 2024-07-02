package tkaxv7s.xposed.sesame.ui;

import lombok.Data;

@Data
public class ObjSyncReference<T> {

    private T obj;

    public ObjSyncReference() {
    }

    public ObjSyncReference(T obj) {
        this.obj = obj;
    }

    public Boolean has() {
        synchronized (this) {
            return this.obj != null;
        }
    }

    public T get() {
        synchronized (this) {
            return obj;
        }
    }

    public Boolean set(T obj) {
        synchronized (this) {
            if (this.obj == obj) {
                return true;
            }
            if (this.obj != null) {
                return false;
            }
            this.obj = obj;
            return true;
        }
    }

    public void setForce(T obj) {
        synchronized (this) {
            this.obj = obj;
        }
    }

    public void del() {
        synchronized (this) {
            this.obj = null;
        }
    }

    public void delIfEquals(T obj) {
        synchronized (this) {
            if (this.obj == obj) {
                this.obj = null;
            }
        }
    }
}
