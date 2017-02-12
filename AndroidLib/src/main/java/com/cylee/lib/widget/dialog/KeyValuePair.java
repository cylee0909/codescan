package com.cylee.lib.widget.dialog;

/**
 * Created by sn on 13-12-13.
 */
public class KeyValuePair<Key,Value> {
    private Key key;
    private Value value;
    
    
    
    public KeyValuePair() {
		super();
	}
	public KeyValuePair(Key key, Value value){
        this.key = key;
        this.value = value;
    }
    public Key getKey() {
        return key;
    }

    public Value getValue() {
        return value;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof KeyValuePair){
            return this.key.equals(((KeyValuePair)o).key);
        }else{
            return super.equals(o);
        }
    }

    @Override
    public String toString(){
        return key.toString()+":"+value.toString();
    }

}
