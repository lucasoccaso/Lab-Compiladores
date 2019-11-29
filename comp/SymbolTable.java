// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package comp;


import java.util.Hashtable;
import ast.CianetoClass;

public class SymbolTable {
    private Hashtable<String, Object> classes;

    public SymbolTable () {
        this.classes = new Hashtable<String, Object>();
    }

    public CianetoClass returnClass(String key) {
        return (CianetoClass) this.classes.get(key);
    }

    public void putClass(String key, Object value) {
        this.classes.put(key, value);
    }
}
