package comp;
import java.util.*;


public class SymbolTable {
	private Hashtable globalTable, localTable, localClassTable;
	
	public SymbolTable() {
        globalTable = new Hashtable();
        localTable  = new Hashtable();
        localClassTable = new Hashtable();
	}
    
    public Object putInGlobal( String key, Object value ) {
       return globalTable.put(key, value);
    }

    public Object putInLocal( String key, Object value ) {
       return localTable.put(key, value);
    }
    
    public Object putInLocalClass( String key, Object value ) {
        return localClassTable.put(key, value);
    }
    
    public Object getInGlobal( String key ) {
       return globalTable.get(key);
    }
    
    public Object getInLocal( String key ) {
       return localTable.get(key);
    }
    
    public Object getInLocalClass( String key ) {
        return localClassTable.get(key);
     }
    
    public Object get( String key ) {
        // returns the object corresponding to the key. 
        Object result;
        if ( (result = localTable.get(key)) != null ) {
              // found local identifier
            return result;
        }
        else {
              // global identifier, if it is in globalTable
            return globalTable.get(key);
        }
    }

    public void removeLocalIdent() {
           // remove all local identifiers from the table
         localTable.clear();
    }
    
    public void removeLocalClassIdent() {
        // remove all local identifiers from the table
      localClassTable.clear();
    }
}
